package net.hallowed.neatlybetter.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.hallowed.neatlybetter.content.item.QuiverItem;
import net.hallowed.neatlybetter.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
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

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
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

    /** S2C: the player's full Ender Chest slot contents, for the tooltip preview. */
    public record EnderChestContentsPayload(List<ItemStack> items) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<@NotNull EnderChestContentsPayload> ID =
                new CustomPacketPayload.Type<>(id("ender_chest_contents"));
        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull EnderChestContentsPayload> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC), EnderChestContentsPayload::items,
                        EnderChestContentsPayload::new
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

    public record SelectQuiverItemPacket(int slotIndex, int selectedItem) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<@NotNull SelectQuiverItemPacket> ID =
                new CustomPacketPayload.Type<>(id("select_quiver_item"));
        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull SelectQuiverItemPacket> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, SelectQuiverItemPacket::slotIndex,
                        ByteBufCodecs.VAR_INT, SelectQuiverItemPacket::selectedItem,
                        SelectQuiverItemPacket::new
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
        PayloadTypeRegistry.clientboundPlay().register(EnderChestContentsPayload.ID, EnderChestContentsPayload.CODEC);

        // ArmorSwap packets
        PayloadTypeRegistry.serverboundPlay().register(ArmorSwapRequest.ID, ArmorSwapRequest.CODEC);

        // Lapis count sync
        PayloadTypeRegistry.clientboundPlay().register(LapisCountPayload.ID, LapisCountPayload.CODEC);

        // Quiver selection (persistent)
        PayloadTypeRegistry.serverboundPlay().register(SelectQuiverItemPacket.ID, SelectQuiverItemPacket.CODEC);

        // -- Server-side receivers --
        ServerPlayNetworking.registerGlobalReceiver(EnderCheckRequest.ID,
                (_, ctx) -> pushEnderChestState(ctx.player()));

        ServerPlayNetworking.registerGlobalReceiver(ArmorSwapRequest.ID,
                (payload, ctx) -> handleArmorSwap(ctx.player(), payload));

        ServerPlayNetworking.registerGlobalReceiver(SelectQuiverItemPacket.ID,
                (payload, ctx) -> handleQuiverSelect(ctx.player(), payload));

        // -- push ender chest state on join --
        ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> {
            pushEnderChestState(handler.player);
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

    /* ===================== Quiver Selection Handler ===================== */


    private static void handleQuiverSelect(ServerPlayer player, SelectQuiverItemPacket payload) {
        AbstractContainerMenu menu = player.containerMenu;

        int idx = payload.slotIndex();
        if (idx < 0 || idx >= menu.slots.size()) return;

        Slot slot = menu.getSlot(idx);
        ItemStack stack = slot.getItem();
        if (stack.isEmpty() || stack.getItem() != ModItems.QUIVER) return;

        QuiverItem.toggleSelectedItem(stack, payload.selectedItem());
        menu.slotsChanged(slot.container);
    }

    /* ===================== Ender Chest Helpers ===================== */

    public static void pushEnderChestState(ServerPlayer player) {
        boolean compass = hasInEnderDeep(player, s -> s.is(Items.COMPASS));
        boolean clock   = hasInEnderDeep(player, s -> s.is(Items.CLOCK));
        ServerPlayNetworking.send(player, new EnderCheckResponse(compass, clock));
        ServerPlayNetworking.send(player, new EnderChestContentsPayload(collectEnderChestItems(player)));
    }

    private static List<ItemStack> collectEnderChestItems(ServerPlayer player) {
        var inv = player.getEnderChestInventory();
        List<ItemStack> items = new ArrayList<>(inv.getContainerSize());
        for (int i = 0; i < inv.getContainerSize(); i++) {
            items.add(inv.getItem(i).copy());
        }
        return items;
    }

    private static boolean hasInEnderDeep(ServerPlayer p, Predicate<ItemStack> test) {
        var inv = p.getEnderChestInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (matchesDeep(inv.getItem(i), test, 0)) return true;
        }
        return false;
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
