package net.hallowed.oldways.init;

import net.hallowed.oldways.api.OWRegistry;

import net.hallowed.oldways.content.entity.vehicle.LavaBoatEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.function.Supplier;

public final class ModEntities {
    private ModEntities() {}

    // Handles you can use anywhere (item, renderer, etc.)
    public static EntityType<LavaBoatEntity> WARPED_BOAT;
    public static EntityType<LavaBoatEntity> CRIMSON_BOAT;

    public static void register() {
        WARPED_BOAT  = registerBoat("warped_boat",  () -> ModItems.WARPED_BOAT);
        CRIMSON_BOAT = registerBoat("crimson_boat", () -> ModItems.CRIMSON_BOAT);
    }

    /** Small helper so both boats share the exact same setup */
    private static EntityType<LavaBoatEntity> registerBoat(String id, Supplier<Item> dropItem) {
        RegistryKey<EntityType<?>> key =
                RegistryKey.of(RegistryKeys.ENTITY_TYPE, OWRegistry.id(id));

        EntityType<LavaBoatEntity> type = EntityType.Builder
                .<LavaBoatEntity>create((t, world) -> new LavaBoatEntity(t, world, dropItem),
                        SpawnGroup.MISC)
                .dropsNothing()
                .dimensions(1.375F, 0.5625F)
                .eyeHeight(0.5625F)
                .maxTrackingRange(10)
                .trackingTickInterval(3)
                .makeFireImmune()
                .build(key);

        return Registry.register(Registries.ENTITY_TYPE, key.getValue(), type);
    }
}
