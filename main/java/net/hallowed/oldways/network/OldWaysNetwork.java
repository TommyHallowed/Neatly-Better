package net.hallowed.oldways.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Common networking: packet types, codecs, and server handlers. */
public final class OldWaysNetwork {
    private OldWaysNetwork() {}

    public static final String MODID = "old-ways";
    private static Identifier id(String path) { return Identifier.of(MODID, path); }

    /* ===================== Packets ===================== */

    /** C2S: empty request asking server to scan player's ender chest. */
    public record EnderCheckRequest() implements CustomPayload {
        public static final CustomPayload.Id<EnderCheckRequest> ID =
                new CustomPayload.Id<>(id("ender_check_request"));
        public static final PacketCodec<RegistryByteBuf, EnderCheckRequest> CODEC =
                PacketCodec.unit(new EnderCheckRequest());
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** S2C: overlay booleans (compass/clock) from ender chest deep scan. */
    public record EnderCheckResponse(boolean hasCompass, boolean hasClock) implements CustomPayload {
        public static final CustomPayload.Id<EnderCheckResponse> ID =
                new CustomPayload.Id<>(id("ender_check_response"));
        public static final PacketCodec<RegistryByteBuf, EnderCheckResponse> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.BOOLEAN, EnderCheckResponse::hasCompass,
                        PacketCodecs.BOOLEAN, EnderCheckResponse::hasClock,
                        EnderCheckResponse::new
                );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** S2C: lodestone waypoints discovered inside the player's ender chest (deep). */
    public record EnderLodestones(List<LodestoneEntry> entries) implements CustomPayload {
        public static final CustomPayload.Id<EnderLodestones> ID =
                new CustomPayload.Id<>(id("ender_lodestones"));
        public static final PacketCodec<RegistryByteBuf, EnderLodestones> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.collection(ArrayList::new, LodestoneEntry.CODEC),
                        EnderLodestones::entries,
                        EnderLodestones::new
                );
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** A single lodestone target from an ender-chest item (possibly nested). */
    public record LodestoneEntry(Identifier dim, int x, int y, int z, int color, String label) {
        // color==-1 means "absent", label=="" means "absent"
        static final PacketCodec<RegistryByteBuf, LodestoneEntry> CODEC =
                PacketCodec.tuple(
                        Identifier.PACKET_CODEC, LodestoneEntry::dim, // ← correct identifier codec
                        PacketCodecs.VAR_INT,    LodestoneEntry::x,
                        PacketCodecs.VAR_INT,    LodestoneEntry::y,
                        PacketCodecs.VAR_INT,    LodestoneEntry::z,
                        PacketCodecs.VAR_INT,    LodestoneEntry::color,
                        PacketCodecs.STRING,     LodestoneEntry::label,
                        LodestoneEntry::new
                );
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
    }

    /** Computes ender-chest state and pushes both overlay booleans and lodestone targets. */
    public static void pushEnderChestState(ServerPlayerEntity player) {
        boolean compass = hasInEnderDeep(player, s -> s.isOf(Items.COMPASS));
        boolean clock   = hasInEnderDeep(player, s -> s.isOf(Items.CLOCK));
        ServerPlayNetworking.send(player, new EnderCheckResponse(compass, clock));

        List<LodestoneEntry> lodestones = collectEnderLodestones(player);
        ServerPlayNetworking.send(player, new EnderLodestones(lodestones));
    }

    // ---- deep scan helpers ----

    private static boolean hasInEnderDeep(ServerPlayerEntity p, Predicate<ItemStack> test) {
        var inv = p.getEnderChestInventory();
        for (int i = 0; i < inv.size(); i++) {
            if (matchesDeep(inv.getStack(i), test)) return true;
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

    private static List<LodestoneEntry> collectEnderLodestones(ServerPlayerEntity player) {
        List<LodestoneEntry> out = new ArrayList<>();
        var inv = player.getEnderChestInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty()) collectFromStack(s, out);
        }
        return out;
    }

    private static void collectFromStack(ItemStack stack, List<LodestoneEntry> out) {
        // lodestone on this stack?
        LodestoneTrackerComponent lc = stack.get(DataComponentTypes.LODESTONE_TRACKER);
        if (lc != null && lc.target().isPresent()) {
            GlobalPos gp = lc.target().get();
            BlockPos bp = gp.pos();
            Identifier dimId = gp.dimension().getValue();
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
        BundleContentsComponent bundle = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        return (bundle == null) ? List.of() : bundle.iterate();
    }

    private static Iterable<ItemStack> iterateContainer(ItemStack stack) {
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        return (container == null) ? List.of() : container.iterateNonEmpty();
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
        Text t = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (t == null) t = stack.get(DataComponentTypes.ITEM_NAME);
        return t == null ? null : t.getString();
    }
}
