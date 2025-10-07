package net.hallowed.oldways.api;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.hallowed.TheOldWays;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Small utilities to keep registration tidy & fast. */
public final class OWRegistry {
    private OWRegistry() {}


    /* ----------------- IDs ----------------- */
    public static Identifier id(String path) {
        return Identifier.of(TheOldWays.MOD_ID, path);
    }

    /* ----------------- Register helpers ----------------- */
    public static <T> T register(Registry<T> reg, String path, T value) {
        return Registry.register(reg, id(path), value);
    }

    public static Block registerBlock(String name, Block block) {
        return Registry.register(Registries.BLOCK, id(name), block);
    }

    public static Block registerBlockWithItem(String name, Block block, Item.Settings itemSettings) {
        Block b = registerBlock(name, block);
        Registry.register(Registries.ITEM, id(name), new BlockItem(b, itemSettings));
        return b;
    }

    public static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, id(name), item);
    }

    /* ----------------- ItemGroup batching -----------------*/
    private static final Map<RegistryKey<ItemGroup>, List<ItemGroupEvents.ModifyEntries>> PENDING = new HashMap<>();

    public static void addToGroup(RegistryKey<ItemGroup> group, ItemGroupEvents.ModifyEntries handler) {
        PENDING.computeIfAbsent(group, g -> new ArrayList<>()).add(handler);
    }

    public static void flushItemGroups() {
        if (PENDING.isEmpty()) return;

        PENDING.forEach((group, handlers) -> {
            ItemGroupEvents
                    .modifyEntriesEvent(group)
                    .register(entries -> {
                        for (ItemGroupEvents.ModifyEntries h : handlers) {
                            h.modifyEntries(entries);
                        }
                    });
        });
        PENDING.clear();
    }
}
