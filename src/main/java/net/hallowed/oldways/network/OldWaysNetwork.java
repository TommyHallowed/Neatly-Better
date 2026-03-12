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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
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

    /**
     * C2S: request to swap an equippable item from a container slot into its
     * armor slot.  The server validates everything from the actual item — the
     * client only sends the container ID and slot index.
     */
    public record ArmorSwapRequest(int containerId, int sourceSlotIndex) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<@NotNull ArmorSwapRequest> ID =
                new CustomPacketPayload.Type<>(id("armor_swap_request"));
        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull ArmorSwapRequest> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, ArmorSwapRequest::containerId,
                        ByteBufCodecs.VAR_INT, ArmorSwapRequest::sourceSlotIndex,
                        ArmorSwapRequest::new
                );
        @Override public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    /* ===================== Registration (common/server) ===================== */

    /** Call from your common init (TheOldWays#onInitialize). */
    public static void registerCommon() {
        // ── Packet type registration ──
        PayloadTypeRegistry.playC2S().register(EnderCheckRequest.ID, EnderCheckRequest.CODEC);
        PayloadTypeRegistry.playS2C().register(EnderCheckResponse.ID, EnderCheckResponse.CODEC);
        PayloadTypeRegistry.playS2C().register(EnderLodestones.ID,    EnderLodestones.CODEC);
        PayloadTypeRegistry.playC2S().register(ArmorSwapRequest.ID,   ArmorSwapRequest.CODEC);

        // ── Server-side receivers ──
        ServerPlayNetworking.registerGlobalReceiver(EnderCheckRequest.ID,
                (payload, ctx) -> pushEnderChestState(ctx.player()));

        ServerPlayNetworking.registerGlobalReceiver(ArmorSwapRequest.ID,
                (payload, ctx) -> handleArmorSwap(ctx.player(), payload));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                pushEnderChestState(handler.player));
    }

    /* ===================== Armor Swap Handler ===================== */

    /**
     * Server-side handler for the armor swap request.
     * <p>
     * Two paths depending on whether the armor slot is part of the current menu:
     * <ul>
     *   <li><b>In menu</b> — uses standard {@code menu.clicked()} calls for proper
     *       menu bookkeeping, state tracking, and client sync.</li>
     *   <li><b>Not in menu</b> (chests, furnaces, hoppers, etc.) — swaps directly
     *       via inventory manipulation, then broadcasts changes.</li>
     * </ul>
     */
    private static void handleArmorSwap(ServerPlayer player, ArmorSwapRequest request) {
        AbstractContainerMenu menu = player.containerMenu;

        // Validate container ID matches the currently open menu
        if (menu.containerId != request.containerId()) return;

        // Validate slot index bounds
        int srcIdx = request.sourceSlotIndex();
        if (srcIdx < 0 || srcIdx >= menu.slots.size()) return;

        // Must not have an item on the cursor
        if (!menu.getCarried().isEmpty()) return;

        Slot sourceSlot = menu.getSlot(srcIdx);
        ItemStack sourceItem = sourceSlot.getItem();
        if (sourceItem.isEmpty()) return;

        // Can the player take from this slot?
        if (!sourceSlot.mayPickup(player)) return;

        // Validate the item is equippable armor (HEAD/CHEST/LEGS/FEET only)
        Equippable equippable = sourceItem.get(DataComponents.EQUIPPABLE);
        if (equippable == null) return;

        EquipmentSlot eqSlot = equippable.slot();
        if (eqSlot != EquipmentSlot.HEAD && eqSlot != EquipmentSlot.CHEST &&
                eqSlot != EquipmentSlot.LEGS && eqSlot != EquipmentSlot.FEET) {
            return;
        }

        // Don't swap if the source slot IS already the target armor slot
        if (sourceSlot.container == player.getInventory()) {
            int containerSlot = sourceSlot.getContainerSlot();
            if (containerSlot >= 36 && containerSlot <= 39) return;
        }

        // Map equipment slot → player inventory index (36=feet, 37=legs, 38=chest, 39=head)
        int armorInvIndex = switch (eqSlot) {
            case FEET  -> 36;
            case LEGS  -> 37;
            case CHEST -> 38;
            case HEAD  -> 39;
            default -> -1;
        };
        if (armorInvIndex == -1) return;

        // Try to find the armor slot within the current menu
        OptionalInt menuArmorSlot = menu.findSlot(player.getInventory(), armorInvIndex);

        if (menuArmorSlot.isPresent()) {
            // ── Armor slot IS in the menu: use standard click operations ──
            // This mirrors vanilla's ServerGamePacketListenerImpl.handleContainerClick
            // with suppress/resume for proper remote sync.
            int armorMenuIdx = menuArmorSlot.getAsInt();
            menu.suppressRemoteUpdates();
            menu.clicked(srcIdx, 0, ClickType.PICKUP, player);         // pick up source item
            menu.clicked(armorMenuIdx, 0, ClickType.PICKUP, player);   // place in armor (swap if occupied)
            if (!menu.getCarried().isEmpty()) {
                menu.clicked(srcIdx, 0, ClickType.PICKUP, player);     // put old armor back in source
            }
            menu.resumeRemoteUpdates();
            menu.broadcastFullState();
        } else {
            // ── Armor slot NOT in menu: direct inventory manipulation ──
            // This is the path that makes chests, furnaces, hoppers, etc. work.
            ItemStack currentlyEquipped = player.getItemBySlot(eqSlot);

            // If the source slot can't accept the currently equipped item, bail
            if (!currentlyEquipped.isEmpty() && !sourceSlot.mayPlace(currentlyEquipped)) return;

            ItemStack toEquip = sourceItem.copy();
            ItemStack toReturn = currentlyEquipped.copy();

            sourceSlot.setByPlayer(toReturn);
            player.setItemSlot(eqSlot, toEquip);
            menu.broadcastChanges();
        }
    }

    /* ===================== Ender Chest Helpers ===================== */

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
