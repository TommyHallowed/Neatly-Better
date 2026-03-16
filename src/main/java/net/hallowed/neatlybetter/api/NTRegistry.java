package net.hallowed.neatlybetter.api;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.hallowed.NeatlyBetter;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NTRegistry {
    private NTRegistry() {}

    /* ----------------- IDs ----------------- */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NeatlyBetter.MOD_ID, path);
    }

    /* ----------------- Registry Keys ----------------- */
    public static ResourceKey<@NotNull Block> blockKey(String path) {
        return ResourceKey.create(Registries.BLOCK, id(path));
    }

    public static ResourceKey<@NotNull Item> itemKey(String path) {
        return ResourceKey.create(Registries.ITEM, id(path));
    }

    /* ----------------- Register helpers ----------------- */
    public static <T> T register(Registry<@NotNull T> reg, String path, T value) {
        return Registry.register(reg, id(path), value);
    }

    public static Block registerBlock(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, id(name), block);
    }

    public static Block registerBlockWithItem(String name, Block block) {
        Block b = registerBlock(name, block);
        Registry.register(BuiltInRegistries.ITEM, id(name),
                new BlockItem(b, new Item.Properties().setId(itemKey(name))));
        return b;
    }

    public static Block registerBlockWithItem(String name, Block block, Item.Properties itemSettings) {
        Block b = registerBlock(name, block);
        Registry.register(BuiltInRegistries.ITEM, id(name), new BlockItem(b, itemSettings));
        return b;
    }

    public static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, id(name), item);
    }

    /* ----------------- ItemGroup batching -----------------*/
    private static final Map<ResourceKey<@NotNull CreativeModeTab>, List<ItemGroupEvents.ModifyEntries>> PENDING = new HashMap<>();

    public static void addToGroup(ResourceKey<@NotNull CreativeModeTab> group, ItemGroupEvents.ModifyEntries handler) {
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