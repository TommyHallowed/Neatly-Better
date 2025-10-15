package net.hallowed.oldways.init;

import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.content.item.DragonBurstRocketItem;
import net.hallowed.oldways.content.item.GlowTorchItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModItems {
    private ModItems() {}

    public static Item RAINBOW_WOOL, RAINBOW_CARPET, WARPED_BOAT, CRIMSON_BOAT, DRAGON_BURST_ROCKET, GLOW_TORCH;

    private static RegistryKey<Item> key(String path) {
        return RegistryKey.of(RegistryKeys.ITEM, OWRegistry.id(path));
    }

    public static void register() {
        // rainbow_wool item
        RAINBOW_WOOL = OWRegistry.registerItem(
                "rainbow_wool",
                new BlockItem(
                        ModBlocks.RAINBOW_WOOL,
                        new Item.Settings().registryKey(key("rainbow_wool"))
                )
        );

        // rainbow_carpet item
        RAINBOW_CARPET = OWRegistry.registerItem(
                "rainbow_carpet",
                new BlockItem(
                        ModBlocks.RAINBOW_CARPET,
                        new Item.Settings().registryKey(key("rainbow_carpet"))
                )
        );

        // warped_boat boat item
        WARPED_BOAT = OWRegistry.registerItem(
                "warped_boat",
                new BoatItem(
                        ModEntities.WARPED_BOAT,
                        new Item.Settings().maxCount(1).fireproof().registryKey(key("warped_boat")) )
        );

        // crimson_boat boat item
        CRIMSON_BOAT = OWRegistry.registerItem(
                "crimson_boat",
                new BoatItem(
                        ModEntities.CRIMSON_BOAT,
                        new Item.Settings().maxCount(1).fireproof().registryKey(key("crimson_boat")) )
        );

        // dragon_burst_rocket item
        DRAGON_BURST_ROCKET = OWRegistry.registerItem(
                "dragon_burst_rocket",
                new DragonBurstRocketItem(new Item.Settings().maxCount(64).registryKey(key("dragon_burst_rocket")))
        );

        GLOW_TORCH = OWRegistry.registerItem(
                "glow_torch",
                new GlowTorchItem(
                        ModBlocks.GLOW_TORCH,
                        ModBlocks.GLOW_WALL_TORCH,
                        new Item.Settings().registryKey(key("glow_torch"))
                )
        );
    }
}
