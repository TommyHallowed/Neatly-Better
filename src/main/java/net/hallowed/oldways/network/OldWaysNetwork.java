package net.hallowed.oldways.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hallowed.oldways.compat.BackpackedServerCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
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
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
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

    /** S2C: overlay booleans (compass/clock/recovery compass) from backpack deep scan. */
    public record BackpackCheckResponse(boolean hasCompass, boolean hasClock, boolean hasRecoveryCompass) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<@NotNull BackpackCheckResponse> ID =
                new CustomPacketPayload.Type<>(id("backpack_check_response"));
        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull BackpackCheckResponse> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.BOOL, BackpackCheckResponse::hasCompass,
                        ByteBufCodecs.BOOL, BackpackCheckResponse::hasClock,
                        ByteBufCodecs.BOOL, BackpackCheckResponse::hasRecoveryCompass,
                        BackpackCheckResponse::new
                );
        @Override public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    /** S2C: lodestone waypoints discovered inside the player's backpacks (deep). */
    public record BackpackLodestones(List<LodestoneEntry> entries) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<@NotNull BackpackLodestones> ID =
                new CustomPacketPayload.Type<>(id("backpack_lodestones"));
        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull BackpackLodestones> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.collection(ArrayList::new, LodestoneEntry.CODEC),
                        BackpackLodestones::entries,
                        BackpackLodestones::new
                );
        @Override public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    /** A single lodestone target from a container item (possibly nested). */
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
        // -- Packet type registration --
        PayloadTypeRegistry.playC2S().register(EnderCheckRequest.ID, EnderCheckRequest.CODEC);
        PayloadTypeRegistry.playS2C().register(EnderCheckResponse.ID, EnderCheckResponse.CODEC);
        PayloadTypeRegistry.playS2C().register(EnderLodestones.ID,    EnderLodestones.CODEC);
        PayloadTypeRegistry.playC2S().register(ArmorSwapRequest.ID,   ArmorSwapRequest.CODEC);

        // Backpack packets (S2C only -- server pushes data to client)
        PayloadTypeRegistry.playS2C().register(BackpackCheckResponse.ID, BackpackCheckResponse.CODEC);
        PayloadTypeRegistry.playS2C().register(BackpackLodestones.ID,    BackpackLodestones.CODEC);

        // -- Server-side receivers --
        ServerPlayNetworking.registerGlobalReceiver(EnderCheckRequest.ID,
                (payload, ctx) -> pushEnderChestState(ctx.player()));

        ServerPlayNetworking.registerGlobalReceiver(ArmorSwapRequest.ID,
                (payload, ctx) -> handleArmorSwap(ctx.player(), payload));

        // -- On-join: push both ender chest and backpack state --
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            pushEnderChestState(handler.player);
            pushBackpackState(handler.player);
        });
    }

    /* ===================== Armor Swap Handler ===================== */

    private static void handleArmorSwap(ServerPlayer player, ArmorSwapRequest request) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu.containerId != request.containerId()) return;
        int srcIdx = request.sourceSlotIndex();
        if (srcIdx < 0 || srcIdx >= menu.slots.size()) return;
        if (!menu.getCarried().isEmpty()) return;

        Slot sourceSlot = menu.getSlot(srcIdx);
        ItemStack sourceItem = sourceSlot.getItem();
        if (sourceItem.isEmpty()) return;
        if (!sourceSlot.mayPickup(player)) return;

        Equippable equippable = sourceItem.get(DataComponents.EQUIPPABLE);
        if (equippable == null) return;

        EquipmentSlot eqSlot = equippable.slot();
        if (eqSlot != EquipmentSlot.HEAD && eqSlot != EquipmentSlot.CHEST &&
                eqSlot != EquipmentSlot.LEGS && eqSlot != EquipmentSlot.FEET) {
            return;
        }

        if (sourceSlot.container == player.getInventory()) {
            int containerSlot = sourceSlot.getContainerSlot();
            if (containerSlot >= 36 && containerSlot <= 39) return;
        }

        int armorInvIndex = switch (eqSlot) {
            case FEET  -> 36;
            case LEGS  -> 37;
            case CHEST -> 38;
            case HEAD  -> 39;
            default -> -1;
        };
        if (armorInvIndex == -1) return;

        OptionalInt menuArmorSlot = menu.findSlot(player.getInventory(), armorInvIndex);

        if (menuArmorSlot.isPresent()) {
            int armorMenuIdx = menuArmorSlot.getAsInt();
            menu.suppressRemoteUpdates();
            menu.clicked(srcIdx, 0, ClickType.PICKUP, player);
            menu.clicked(armorMenuIdx, 0, ClickType.PICKUP, player);
            if (!menu.getCarried().isEmpty()) {
                menu.clicked(srcIdx, 0, ClickType.PICKUP, player);
            }
            menu.resumeRemoteUpdates();
            menu.broadcastFullState();
        } else {
            ItemStack currentlyEquipped = player.getItemBySlot(eqSlot);
            if (!currentlyEquipped.isEmpty() && !sourceSlot.mayPlace(currentlyEquipped)) return;
            ItemStack toEquip = sourceItem.copy();
            ItemStack toReturn = currentlyEquipped.copy();
            sourceSlot.setByPlayer(toReturn);
            player.setItemSlot(eqSlot, toEquip);
            menu.broadcastChanges();
        }
    }

    /* ===================== Ender Chest Helpers ===================== */

    public static void pushEnderChestState(ServerPlayer player) {
        boolean compass = hasInEnderDeep(player, s -> s.is(Items.COMPASS));
        boolean clock   = hasInEnderDeep(player, s -> s.is(Items.CLOCK));
        ServerPlayNetworking.send(player, new EnderCheckResponse(compass, clock));

        List<LodestoneEntry> lodestones = collectEnderLodestones(player);
        ServerPlayNetworking.send(player, new EnderLodestones(lodestones));
    }

    private static boolean hasInEnderDeep(ServerPlayer p, Predicate<ItemStack> test) {
        var inv = p.getEnderChestInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (matchesDeep(inv.getItem(i), test, 0)) return true;
        }
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

    /* ===================== Backpack Helpers ===================== */

    /**
     * Set of player UUIDs whose backpack contents changed since the last flush.
     * Populated by {@link net.hallowed.oldways.mixin.compat.BackpackInventoryMixin}
     * when {@code BackpackInventory.setChanged()} fires.
     */
    private static final Set<UUID> DIRTY_BACKPACK_PLAYERS = new HashSet<>();

    /** Called from the BackpackInventoryMixin when a backpack slot changes. */
    public static void markBackpackDirty(ServerPlayer player) {
        DIRTY_BACKPACK_PLAYERS.add(player.getUUID());
    }

    /**
     * Called from {@code ServerTickEvents.END_SERVER_TICK}. Only processes
     * players whose backpack contents actually changed (dirty set), then
     * clears the set.  No work is done if nobody touched a backpack.
     */
    public static void flushDirtyBackpacks(MinecraftServer server) {
        if (DIRTY_BACKPACK_PLAYERS.isEmpty()) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (DIRTY_BACKPACK_PLAYERS.remove(player.getUUID())) {
                pushBackpackState(player);
            }
        }
        DIRTY_BACKPACK_PLAYERS.clear(); // catch any stale entries from disconnected players
    }

    /**
     * Scans the player's Backpacked backpack inventories on the server and
     * pushes overlay booleans + lodestone waypoints to the client.
     */
    public static void pushBackpackState(ServerPlayer player) {
        List<ItemStack> backpacks = BackpackedServerCompat.getBackpackStacks(player);

        boolean compass = false, clock = false, recovery = false;
        List<LodestoneEntry> lodestones = new ArrayList<>();

        for (ItemStack bp : backpacks) {
            if (bp.isEmpty()) continue;
            // Scan the backpack stack and its DataComponents.CONTAINER recursively
            if (!compass)  compass  = matchesDeep(bp, s -> s.is(Items.COMPASS), 0);
            if (!clock)    clock    = matchesDeep(bp, s -> s.is(Items.CLOCK), 0);
            if (!recovery) recovery = matchesDeep(bp, s -> s.is(Items.RECOVERY_COMPASS), 0);
            collectFromStack(bp, lodestones, 0);
        }

        ServerPlayNetworking.send(player, new BackpackCheckResponse(compass, clock, recovery));
        ServerPlayNetworking.send(player, new BackpackLodestones(lodestones));
    }

    /* ===================== Shared Deep-Scan Helpers ===================== */

    private static boolean matchesDeep(ItemStack stack, Predicate<ItemStack> test, int depth) {
        if (stack == null || stack.isEmpty()) return false;
        if (test.test(stack)) return true;
        if (depth >= MAX_DEPTH) return false;
        for (ItemStack child : iterateBundle(stack))    if (matchesDeep(child, test, depth + 1)) return true;
        for (ItemStack child : iterateContainer(stack)) if (matchesDeep(child, test, depth + 1)) return true;
        return false;
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
        if (depth >= MAX_DEPTH) return;
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
