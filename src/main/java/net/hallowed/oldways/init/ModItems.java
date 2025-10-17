package net.hallowed.oldways.init;

import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.content.item.DragonBurstRocketItem;
import net.hallowed.oldways.content.item.GlowTorchItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.sound.SoundEvents;

public final class ModItems {
    private ModItems() {}

    public static Item WARPED_BOAT, CRIMSON_BOAT, DRAGON_BURST_ROCKET, NETHERITE_HORSE_ARMOR, GLOW_TORCH;

    public static void register() {
        RegistryEntryLookup<EntityType<?>> registryEntryLookup = Registries.createEntryLookup(Registries.ENTITY_TYPE);

        WARPED_BOAT = OWRegistry.registerItem("warped_boat",
                new BoatItem(ModEntities.WARPED_BOAT,
                        new Item.Settings().maxCount(1).fireproof().registryKey(OWRegistry.itemKey("warped_boat"))));

        CRIMSON_BOAT = OWRegistry.registerItem("crimson_boat",
                new BoatItem(ModEntities.CRIMSON_BOAT,
                        new Item.Settings().maxCount(1).fireproof().registryKey(OWRegistry.itemKey("crimson_boat"))));

        DRAGON_BURST_ROCKET = OWRegistry.registerItem("dragon_burst_rocket",
                new DragonBurstRocketItem(new Item.Settings().maxCount(64).registryKey(OWRegistry.itemKey("dragon_burst_rocket"))));

        GLOW_TORCH = OWRegistry.registerItem("glow_torch",
                new GlowTorchItem(ModBlocks.GLOW_TORCH, ModBlocks.GLOW_WALL_TORCH,
                        new Item.Settings().registryKey(OWRegistry.itemKey("glow_torch"))));

        NETHERITE_HORSE_ARMOR = OWRegistry.registerItem("netherite_horse_armor",
                new Item(new Item.Settings()
                        .attributeModifiers(ArmorMaterials.NETHERITE.createAttributeModifiers(EquipmentType.BODY))
                        .component(DataComponentTypes.EQUIPPABLE,
                                EquippableComponent.builder(net.minecraft.entity.EquipmentSlot.BODY)
                                        .equipSound(SoundEvents.ENTITY_HORSE_ARMOR)
                                        .model(ArmorMaterials.NETHERITE.assetId())
                                        .allowedEntities(registryEntryLookup.getOrThrow(EntityTypeTags.CAN_WEAR_HORSE_ARMOR))
                                        .damageOnHurt(false)
                                        .canBeSheared(true)
                                        .shearingSound(SoundEvents.ITEM_HORSE_ARMOR_UNEQUIP)
                                        .build())
                        .fireproof()
                        .maxCount(1)
                        .registryKey(OWRegistry.itemKey("netherite_horse_armor"))));
    }
}