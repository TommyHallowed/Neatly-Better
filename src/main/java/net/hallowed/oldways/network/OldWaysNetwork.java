package net.hallowed.oldways.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
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

    /** Maximum nesting depth for container scanning. Prevents StackOverflow from malicious data. */
    private static final int MAX_DEPTH = 6;

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
        static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull LodestoneEntry> CODEC =
                StreamCodec.composite(
                        Identifier.STREAM_CODEC, LodestoneEntry::dim,
                        ByteBufCodecs.VAR_INT,    LodestoneEntry::x,
                        ByteBufCodecs.VAR_INT,    LodestoneEntry::y,
                        ByteBufCodecs.VAR_INT,    LodestoneEntry::z,
                        ByteBufCodecs.VAR_INT,    LodestoneEntry::color,
                        ByteBufCodecs.STRING_UTF8, LodestoneEntry::label,
                        LodestoneEntry::new
                );
    }

    /* ===================== Registration (common/server) ===================== */

    /** Call from your common init (TheOldWays#onInitialize). */
    public static void registerCommon() {
        PayloadTypeRegistry.playC2S().register(EnderCheckRequest.ID, EnderCheckRequest.CODEC);
        PayloadTypeRegistry.playS2C().register(EnderCheckResponse.ID, EnderCheckResponse.CODEC);
        PayloadTypeRegistry.playS2C().register(EnderLodestones.ID,    EnderLodestones.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(EnderCheckRequest.ID,
                (payload, ctx) -> pushEnderChestState(ctx.player()));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                pushEnderChestState(handler.player));
    }

    /** Computes ender-chest state and pushes both overlay booleans and lodestone targets. */
    public static void pushEnderChestState(ServerPlayer player) {
        boolean compass = hasInEnderDeep(player, s -> s.is(Items.COMPASS));
        boolean clock   = hasInEnderDeep(player, s -> s.is(Items.CLOCK));
        ServerPlayNetworking.send(player, new EnderCheckResponse(compass, clock));

        List<LodestoneEntry> lodestones = collectEnderLodestones(player);
        ServerPlayNetworking.send(player, new EnderLodestones(lodestones));
    }

    // ---- deep scan helpers (depth-limited) ----

    private static boolean hasInEnderDeep(ServerPlayer p, Predicate<ItemStack> test) {
        var inv = p.getEnderChestInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (matchesDeep(inv.getItem(i), test, 0)) return true;
        }
        return false;
    }

    private static boolean matchesDeep(ItemStack stack, Predicate<ItemStack> test, int depth) {
        if (stack == null || stack.isEmpty()) return false;
        if (test.test(stack)) return true;
        if (depth >= MAX_DEPTH) return false; // ← prevents StackOverflow
        for (ItemStack child : iterateBundle(stack))    if (matchesDeep(child, test, depth + 1)) return true;
        for (ItemStack child : iterateContainer(stack)) if (matchesDeep(child, test, depth + 1)) return true;
        return false;
    }

    private static List<LodestoneEntry> collectEnderLodestones(ServerPlayer player) {
        List<LodestoneEntry> out = new ArrayList<>();
        var inv = player.getEnderChestInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty()) collectFromStack(s, out, 0);
        }
        return out;
    }

    private static void collectFromStack(ItemStack stack, List<LodestoneEntry> out, int depth) {
        LodestoneTracker lc = stack.get(DataComponents.LODESTONE_TRACKER);
        if (lc != null && lc.target().isPresent()) {
            GlobalPos gp = lc.target().get();
            BlockPos bp = gp.pos();
            Identifier dimId = gp.dimension().identifier();
            int color = parseHexColor(stack);
            String label = getPlainName(stack);
            out.add(new LodestoneEntry(dimId, bp.getX(), bp.getY(), bp.getZ(), color, label));
        }
        if (depth >= MAX_DEPTH) return; // ← prevents StackOverflow
        for (ItemStack child : iterateBundle(stack))    collectFromStack(child, out, depth + 1);
        for (ItemStack child : iterateContainer(stack)) collectFromStack(child, out, depth + 1);
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
        return -1;
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