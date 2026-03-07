package net.hallowed.oldways.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hallowed.oldways.content.item.MapBuilderItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.LodestoneTracker;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Common networking: packet types, codecs, and server handlers. */
public final class OldWaysNetwork {
    private OldWaysNetwork() {}

    public static final String MODID = "old-ways";
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MODID, path); }

    /* ===================== Packets ===================== */

    /** C2S: empty request asking server to scan player's ender chest. */
    public record EnderCheckRequest() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<@NotNull EnderCheckRequest> ID =
                new CustomPacketPayload.Type<>(id("ender_check_request"));
        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull EnderCheckRequest> CODEC =
                StreamCodec.unit(new EnderCheckRequest());
        @Override public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    /** S2C: overlay booleans (compass/clock) from ender chest deep scan. */
    public record EnderCheckResponse(boolean hasCompass, boolean hasClock) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<@NotNull EnderCheckResponse> ID =
                new CustomPacketPayload.Type<>(id("ender_check_response"));
        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull EnderCheckResponse> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.BOOL, EnderCheckResponse::hasCompass,
                        ByteBufCodecs.BOOL, EnderCheckResponse::hasClock,
                        EnderCheckResponse::new
                );
        @Override public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    /** S2C: lodestone waypoints discovered inside the player's ender chest (deep). */
    public record EnderLodestones(List<LodestoneEntry> entries) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<@NotNull EnderLodestones> ID =
                new CustomPacketPayload.Type<>(id("ender_lodestones"));
        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull EnderLodestones> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.collection(ArrayList::new, LodestoneEntry.CODEC),
                        EnderLodestones::entries,
                        EnderLodestones::new
                );
        @Override public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    /** A single lodestone target from an ender-chest item (possibly nested). */
    public record LodestoneEntry(Identifier dim, int x, int y, int z, int color, String label) {
        // color==-1 means "absent", label=="" means "absent"
        static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull LodestoneEntry> CODEC =
                StreamCodec.composite(
                        Identifier.STREAM_CODEC, LodestoneEntry::dim,
                        ByteBufCodecs.VAR_INT,    LodestoneEntry::x,
                        ByteBufCodecs.VAR_INT,    LodestoneEntry::y,
                        ByteBufCodecs.VAR_INT,    LodestoneEntry::z,
                        ByteBufCodecs.VAR_INT,    LodestoneEntry::color,
                        ByteBufCodecs.STRING_UTF8,     LodestoneEntry::label,
                        LodestoneEntry::new
                );
    }

    /** C2S: Action payload for the Map Builder (0=Zoom, 1=Facing, 2=Reset) */
    public record MapBuilderPayload(int action) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<@NotNull MapBuilderPayload> ID = new CustomPacketPayload.Type<>(id("map_builder_action"));
        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull MapBuilderPayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.INT, MapBuilderPayload::action, MapBuilderPayload::new);
        @Override public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    /* ===================== Registration (common/server) ===================== */

    /** Call from your common init (TheOldWays#onInitialize). */
    public static void registerCommon() {
        // codecs
        PayloadTypeRegistry.playC2S().register(EnderCheckRequest.ID, EnderCheckRequest.CODEC);
        PayloadTypeRegistry.playS2C().register(EnderCheckResponse.ID, EnderCheckResponse.CODEC);
        PayloadTypeRegistry.playS2C().register(EnderLodestones.ID,    EnderLodestones.CODEC);

        // explicit client ping
        ServerPlayNetworking.registerGlobalReceiver(EnderCheckRequest.ID,
                (payload, ctx) -> pushEnderChestState(ctx.player()));

        // push once on join (fresh load)
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                pushEnderChestState(handler.player));

        // Map Builder Registrations
        PayloadTypeRegistry.playC2S().register(MapBuilderPayload.ID, MapBuilderPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(MapBuilderPayload.ID, (payload, ctx) ->
                ctx.player().server.execute(() -> {
                    ItemStack stack = ctx.player().getMainHandItem();
                    if (!(stack.getItem() instanceof MapBuilderItem)) return;

                    CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    CompoundTag nbt = data.copyTag();
                    int step = nbt.getIntOr(MapBuilderItem.NBT_STEP, 0);

                    // Action 1: Reset (Shift + Left Click Air)
                    if (payload.action() == 1) {
                        nbt.putInt(MapBuilderItem.NBT_STEP, 0);
                        nbt.putBoolean(MapBuilderItem.NBT_HAS_P1, false);
                        nbt.putBoolean(MapBuilderItem.NBT_HAS_P2, false);
                        ctx.player().displayClientMessage(Component.literal("Right Click a block to set corner 1\nLeft Click a block to set corner 2").withStyle(ChatFormatting.YELLOW), false);
                    }
                    // Action 0: Zoom (Shift + Z)
                    else if (step == 2 && payload.action() == 0) {
                        int currentZoom = nbt.getIntOr(MapBuilderItem.NBT_ZOOM, 1);
                        int zoom = (currentZoom + 1) % 5;
                        nbt.putInt(MapBuilderItem.NBT_ZOOM, zoom);
                        ctx.player().displayClientMessage(Component.literal("Map zoom in: " + zoom + "x").withStyle(ChatFormatting.AQUA), true);
                    }

                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                })
        );
    }


    /** Computes ender-chest state and pushes both overlay booleans and lodestone targets. */
    public static void pushEnderChestState(ServerPlayer player) {
        boolean compass = hasInEnderDeep(player, s -> s.is(Items.COMPASS));
        boolean clock   = hasInEnderDeep(player, s -> s.is(Items.CLOCK));
        ServerPlayNetworking.send(player, new EnderCheckResponse(compass, clock));

        List<LodestoneEntry> lodestones = collectEnderLodestones(player);
        ServerPlayNetworking.send(player, new EnderLodestones(lodestones));
    }

    // ---- deep scan helpers ----

    private static boolean hasInEnderDeep(ServerPlayer p, Predicate<ItemStack> test) {
        var inv = p.getEnderChestInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (matchesDeep(inv.getItem(i), test)) return true;
        }
        return false;
    }

    private static boolean matchesDeep(ItemStack stack, Predicate<ItemStack> test) {
        if (stack == null || stack.isEmpty()) return false;
        if (test.test(stack)) return true;
        for (ItemStack child : iterateBundle(stack))   if (matchesDeep(child, test)) return true;
        for (ItemStack child : iterateContainer(stack)) if (matchesDeep(child, test)) return true;
        return false;
    }

    private static List<LodestoneEntry> collectEnderLodestones(ServerPlayer player) {
        List<LodestoneEntry> out = new ArrayList<>();
        var inv = player.getEnderChestInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty()) collectFromStack(s, out);
        }
        return out;
    }

    private static void collectFromStack(ItemStack stack, List<LodestoneEntry> out) {
        // lodestone on this stack?
        LodestoneTracker lc = stack.get(DataComponents.LODESTONE_TRACKER);
        if (lc != null && lc.target().isPresent()) {
            GlobalPos gp = lc.target().get();
            BlockPos bp = gp.pos();
            Identifier dimId = gp.dimension().identifier();
            // parse color once from name
            int color = parseHexColor(stack);
            String label = getPlainName(stack);
            out.add(new LodestoneEntry(dimId, bp.getX(), bp.getY(), bp.getZ(), color, label));
        }
        // recurse
        for (ItemStack child : iterateBundle(stack))    collectFromStack(child, out);
        for (ItemStack child : iterateContainer(stack)) collectFromStack(child, out);
    }

    private static Iterable<ItemStack> iterateBundle(ItemStack stack) {
        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        return (bundle == null) ? List.of() : bundle.items();
    }

    private static Iterable<ItemStack> iterateContainer(ItemStack stack) {
        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        return (container == null) ? List.of() : container.nonEmptyItems();
    }

    // ---- color & label (single pass, server side) ----

    private static final Pattern HEX = Pattern.compile("#[0-9a-fA-F]{6}");

    private static int parseHexColor(ItemStack stack) {
        String s = getNameString(stack);
        if (s == null) return -1;
        Matcher m = HEX.matcher(s);
        if (m.find()) {
            try { return (int) Long.parseLong(m.group().substring(1), 16); }
            catch (NumberFormatException ignored) {}
        }
        return -1; // absent
    }

    private static String getPlainName(ItemStack stack) {
        String s = getNameString(stack);
        if (s == null) return "";
        return HEX.matcher(s).replaceAll("").trim();
    }

    private static String getNameString(ItemStack stack) {
        Component t = stack.get(DataComponents.CUSTOM_NAME);
        if (t == null) t = stack.get(DataComponents.ITEM_NAME);
        return t == null ? null : t.getString();
    }
}