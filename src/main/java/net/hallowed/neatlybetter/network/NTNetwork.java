package net.hallowed.neatlybetter.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.hallowed.neatlybetter.compat.BackpackedServerCompat;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.config.ShieldDelayHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.equipment.Equippable;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/** Common networking: packet types, codecs, and server handlers. */
public final class NTNetwork {
    private NTNetwork() {}

    public static final String MODID = "neatly-better";
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

    /** S2C: overlay booleans (compass/clock) from backpack deep scan. */
    public record BackpackCheckResponse(boolean hasCompass, boolean hasClock) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<@NotNull BackpackCheckResponse> ID =
                new CustomPacketPayload.Type<>(id("backpack_check_response"));
        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull BackpackCheckResponse> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.BOOL, BackpackCheckResponse::hasCompass,
                        ByteBufCodecs.BOOL, BackpackCheckResponse::hasClock,
                        BackpackCheckResponse::new
                );
        @Override public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
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

    /** S2C: syncs the server's shield raise delay to the client. */
    public record ShieldDelaySyncPayload(int shieldRaiseDelay) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<@NotNull ShieldDelaySyncPayload> ID =
                new CustomPacketPayload.Type<>(id("shield_delay_sync"));
        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull ShieldDelaySyncPayload> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, ShieldDelaySyncPayload::shieldRaiseDelay,
                        ShieldDelaySyncPayload::new
                );
        @Override public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return ID; }
    }

    /**
     * S2C: syncs the stored lapis count for a specific enchanting table to
     * all clients in the level, so players sharing a table stay in sync.
     */
    public record LapisCountPayload(int lapisCount, int x, int y, int z) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<@NotNull LapisCountPayload> ID =
                new CustomPacketPayload.Type<>(id("lapis_count_sync"));
        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull LapisCountPayload> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, LapisCountPayload::lapisCount,
                        ByteBufCodecs.VAR_INT, LapisCountPayload::x,
                        ByteBufCodecs.VAR_INT, LapisCountPayload::y,
                        ByteBufCodecs.VAR_INT, LapisCountPayload::z,
                        LapisCountPayload::new
                );
        @Override public @NotNull Type<? extends @NotNull CustomPacketPayload> type() { return ID; }

        /** Convenience constructor from a {@link BlockPos}. */
        public LapisCountPayload(int lapisCount, BlockPos pos) {
            this(lapisCount, pos.getX(), pos.getY(), pos.getZ());
        }

        public BlockPos pos() {
            return new BlockPos(x, y, z);
        }
    }

    /* ===================== Registration (common/server) ===================== */

    /** Call from your common init (TheNeatlyBetter#onInitialize). */
    public static void registerCommon() {
        // Ender Chest packets
        PayloadTypeRegistry.serverboundPlay().register(EnderCheckRequest.ID, EnderCheckRequest.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EnderCheckResponse.ID, EnderCheckResponse.CODEC);

        // ArmorSwap packets
        PayloadTypeRegistry.serverboundPlay().register(ArmorSwapRequest.ID, ArmorSwapRequest.CODEC);

        // Backpack packets
        PayloadTypeRegistry.clientboundPlay().register(BackpackCheckResponse.ID, BackpackCheckResponse.CODEC);

        // Shield delay sync
        PayloadTypeRegistry.clientboundPlay().register(ShieldDelaySyncPayload.ID, ShieldDelaySyncPayload.CODEC);

        // Lapis count sync
        PayloadTypeRegistry.clientboundPlay().register(LapisCountPayload.ID, LapisCountPayload.CODEC);

        // -- Server-side receivers --
        ServerPlayNetworking.registerGlobalReceiver(EnderCheckRequest.ID,
                (_, ctx) -> pushEnderChestState(ctx.player()));

        ServerPlayNetworking.registerGlobalReceiver(ArmorSwapRequest.ID,
                (payload, ctx) -> handleArmorSwap(ctx.player(), payload));

        // -- push ender chest and backpack state on join --
        ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> {
            pushEnderChestState(handler.player);
//            pushBackpackState(handler.player);
            pushShieldDelay(handler.player);
        });
    }

    /* ===================== Shield Delay Sync ===================== */

    /** Push the current shield raise delay to a single player. */
    private static void pushShieldDelay(ServerPlayer player) {
        int delay = NTServerConfig.CONFIG.shieldRaiseDelay.get();
        ShieldDelayHolder.setShieldRaiseDelay(delay);
        ServerPlayNetworking.send(player, new ShieldDelaySyncPayload(delay));
    }

    /** Re-sync shield delay to all online players (call after config reload). */
    public static void syncShieldDelayToAll(MinecraftServer server) {
        int delay = NTServerConfig.CONFIG.shieldRaiseDelay.get();
        ShieldDelayHolder.setShieldRaiseDelay(delay);
        ShieldDelaySyncPayload payload = new ShieldDelaySyncPayload(delay);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
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
            menu.clicked(srcIdx, 0, ContainerInput.PICKUP, player);
            menu.clicked(armorMenuIdx, 0, ContainerInput.PICKUP, player);
            if (!menu.getCarried().isEmpty()) {
                menu.clicked(srcIdx, 0, ContainerInput.PICKUP, player);
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
    }

    private static boolean hasInEnderDeep(ServerPlayer p, Predicate<ItemStack> test) {
        var inv = p.getEnderChestInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (matchesDeep(inv.getItem(i), test, 0)) return true;
        }
        return false;
    }

    /* ===================== Backpack Helpers ===================== */

//    private static final Set<UUID> DIRTY_BACKPACK_PLAYERS = new HashSet<>();

//    public static void markBackpackDirty(ServerPlayer player) {
//        DIRTY_BACKPACK_PLAYERS.add(player.getUUID());
//    }

//    public static void flushDirtyBackpacks(MinecraftServer server) {
//        if (DIRTY_BACKPACK_PLAYERS.isEmpty()) return;
//        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
//            if (DIRTY_BACKPACK_PLAYERS.remove(player.getUUID())) {
//                pushBackpackState(player);
//            }
//        }
//        DIRTY_BACKPACK_PLAYERS.clear();
//    }

//    public static void pushBackpackState(ServerPlayer player) {
//        List<ItemStack> backpacks = BackpackedServerCompat.getBackpackStacks(player);

//        boolean compass = false, clock = false;

//        for (ItemStack bp : backpacks) {
//            if (bp.isEmpty()) continue;
//            if (!compass) compass = matchesDeep(bp, s -> s.is(Items.COMPASS), 0);
//            if (!clock)   clock   = matchesDeep(bp, s -> s.is(Items.CLOCK), 0);
//        }

//        ServerPlayNetworking.send(player, new BackpackCheckResponse(compass, clock));
//    }

    /* ===================== Shared Deep-Scan Helpers ===================== */

    private static boolean matchesDeep(ItemStack stack, Predicate<ItemStack> test, int depth) {
        if (stack == null || stack.isEmpty()) return false;
        if (test.test(stack)) return true;
        if (depth >= MAX_DEPTH) return false;
        for (ItemStack child : iterateBundle(stack))    if (matchesDeep(child, test, depth + 1)) return true;
        for (ItemStack child : iterateContainer(stack)) if (matchesDeep(child, test, depth + 1)) return true;
        return false;
    }

    private static Iterable<ItemStack> iterateBundle(ItemStack stack) {
        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle == null) return List.of();
        return bundle.items().stream().map(ItemStackTemplate::create).toList();
    }

    private static Iterable<ItemStack> iterateContainer(ItemStack stack) {
        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container == null) return List.of();
        List<ItemStack> result = new java.util.ArrayList<>();
        for (ItemStackTemplate template : container.nonEmptyItems()) {
            result.add(template.create());
        }
        return result;
    }
}
