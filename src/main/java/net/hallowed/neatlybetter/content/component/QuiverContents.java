package net.hallowed.neatlybetter.content.component;

import com.google.common.collect.ImmutableList;

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class QuiverContents {

    public static final int MAX_SLOTS = 4;
    public static final int NO_SELECTED = -1;

    public static final QuiverContents EMPTY = new QuiverContents(List.of(), NO_SELECTED);

    public static final Codec<QuiverContents> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, QuiverContents> STREAM_CODEC;

    private final List<ItemStackTemplate> items;
    private final int selectedItem;

    private QuiverContents(List<ItemStackTemplate> items, int selectedItem) {
        this.items = items;
        this.selectedItem = selectedItem;
    }

    public QuiverContents(List<ItemStackTemplate> items) {
        this(items, NO_SELECTED);
    }

    public static boolean canItemBeInQuiver(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemTags.ARROWS);
    }

    public List<ItemStackTemplate> items()       { return items; }
    public int size()                            { return items.size(); }
    public boolean isEmpty()                     { return items.isEmpty(); }
    public int getSelectedItemIndex()            { return selectedItem; }

    public @Nullable ItemStackTemplate getSelectedItem() {
        return (selectedItem == NO_SELECTED || selectedItem >= items.size())
                ? null : items.get(selectedItem);
    }

    public @Nullable ItemStackTemplate getFirstItem() {
        return items.isEmpty() ? null : items.getFirst();
    }

    public float fullness() {
        int total = items.stream().mapToInt(ItemStackTemplate::count).sum();
        return total / (float) (MAX_SLOTS * 64);
    }

    public Stream<ItemStack> itemCopyStream() {
        return items.stream().map(ItemStackTemplate::create);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof QuiverContents other) {
            return items.equals(other.items) && selectedItem == other.selectedItem;
        }
        return false;
    }

    @Override public int hashCode() { return items.hashCode() * 31 + selectedItem; }
    @Override public String toString() { return "QuiverContents" + items; }

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStackTemplate.CODEC.listOf()
                        .fieldOf("items")
                        .forGetter(c -> c.items),
                Codec.INT
                        .optionalFieldOf("selected", NO_SELECTED)
                        .forGetter(c -> c.selectedItem)
        ).apply(instance, (items, sel) -> new QuiverContents(
                items.size() <= MAX_SLOTS ? items : items.subList(0, MAX_SLOTS), sel
        )));

        STREAM_CODEC = StreamCodec.composite(
                ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()), c -> c.items,
                ByteBufCodecs.VAR_INT, c -> c.selectedItem,
                (items, sel) -> new QuiverContents(items, sel)
        );
    }

    public static final class Mutable {

        private final List<ItemStack> items;
        private int selectedItem;

        public Mutable(QuiverContents contents) {
            this.items = new ArrayList<>(contents.items.size());
            for (ItemStackTemplate t : contents.items) this.items.add(t.create());
            this.selectedItem = contents.selectedItem;
        }

        public int tryInsert(ItemStack toAdd) {
            if (!canItemBeInQuiver(toAdd)) return 0;
            int totalAdded = 0;

            int existingIdx = findStackIndex(toAdd);
            if (existingIdx != -1) {
                ItemStack existing = items.get(existingIdx);
                int canAdd = existing.getMaxStackSize() - existing.getCount();
                int adding = Math.min(toAdd.getCount(), canAdd);
                if (adding > 0) {
                    existing.grow(adding);
                    toAdd.shrink(adding);
                    totalAdded += adding;
                    items.addFirst(items.remove(existingIdx));
                }
            }

            if (!toAdd.isEmpty() && items.size() < MAX_SLOTS) {
                int adding = toAdd.getCount();
                items.addFirst(toAdd.split(adding));
                totalAdded += adding;
            }

            return totalAdded;
        }

        public int tryTransfer(Slot slot, Player player) {
            ItemStack other = slot.getItem();
            if (!canItemBeInQuiver(other)) return 0;
            int space = getSpaceFor(other);
            if (space <= 0) return 0;
            return tryInsert(slot.safeTake(other.getCount(), space, player));
        }

        private int getSpaceFor(ItemStack stack) {
            int total = 0;
            int idx = findStackIndex(stack);
            if (idx != -1) {
                total += items.get(idx).getMaxStackSize() - items.get(idx).getCount();
            }
            if (items.size() < MAX_SLOTS) {
                total += stack.getMaxStackSize();
            }
            return total;
        }

        private int findStackIndex(ItemStack stack) {
            if (!stack.isStackable()) return -1;
            for (int i = 0; i < items.size(); i++) {
                if (ItemStack.isSameItemSameComponents(items.get(i), stack)) return i;
            }
            return -1;
        }

        public void toggleSelectedItem(int idx) {
            boolean alreadySelected = (selectedItem == idx);
            boolean outOfBounds    = (idx < 0 || idx >= items.size());
            selectedItem = (!alreadySelected && !outOfBounds) ? idx : NO_SELECTED;
        }

        public void promoteSelected() {
            if (selectedItem > 0 && selectedItem < items.size()) {
                items.addFirst(items.remove(selectedItem));
            }
            selectedItem = NO_SELECTED;
        }

        public @Nullable ItemStack removeOne() {
            if (items.isEmpty()) return null;
            int idx = (selectedItem >= 0 && selectedItem < items.size()) ? selectedItem : 0;
            ItemStack stack = items.remove(idx).copy();
            selectedItem = NO_SELECTED;
            return stack;
        }

        public @Nullable ItemStack removeOneFromSelected() {
            if (items.isEmpty()) return null;
            int idx = (selectedItem >= 0 && selectedItem < items.size()) ? selectedItem : 0;
            ItemStack target = items.get(idx);
            ItemStack result = target.copyWithCount(1);
            target.shrink(1);
            if (target.isEmpty()) {
                items.remove(idx);
                selectedItem = NO_SELECTED;
            }
            return result;
        }

        public QuiverContents toImmutable() {
            ImmutableList.Builder<ItemStackTemplate> builder = ImmutableList.builder();
            for (ItemStack item : items) {
                if (!item.isEmpty()) builder.add(ItemStackTemplate.fromNonEmptyStack(item));
            }
            return new QuiverContents(builder.build(), selectedItem);
        }
    }
}