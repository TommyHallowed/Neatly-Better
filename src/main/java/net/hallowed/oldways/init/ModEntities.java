package net.hallowed.oldways.init;

import net.hallowed.oldways.api.OWRegistry;
import net.hallowed.oldways.content.entity.DragonBurstTrailEntity;
import net.hallowed.oldways.content.entity.vehicle.LavaBoatEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModEntities {
    private ModEntities() {}

    public static EntityType<LavaBoatEntity> WARPED_BOAT;
    public static EntityType<LavaBoatEntity> CRIMSON_BOAT;

    public static EntityType<DragonBurstTrailEntity> DRAGON_BURST_TRAIL;

    public static void register() {
        WARPED_BOAT  = registerBoat("warped_boat",  () -> ModItems.WARPED_BOAT);
        CRIMSON_BOAT = registerBoat("crimson_boat", () -> ModItems.CRIMSON_BOAT);

        DRAGON_BURST_TRAIL = registerTrail("dragon_burst_trail");
    }

    private static EntityType<LavaBoatEntity> registerBoat(String id, java.util.function.Supplier<net.minecraft.item.Item> dropItem) {
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

    private static EntityType<DragonBurstTrailEntity> registerTrail(String id) {
        RegistryKey<EntityType<?>> key =
                RegistryKey.of(RegistryKeys.ENTITY_TYPE, OWRegistry.id(id));

        EntityType<DragonBurstTrailEntity> type = EntityType.Builder
                .<DragonBurstTrailEntity>create(DragonBurstTrailEntity::new, SpawnGroup.MISC)
                .dimensions(0.25F, 0.25F)
                .eyeHeight(0.0F)
                .maxTrackingRange(64)
                .trackingTickInterval(10)
                .build(key);

        return Registry.register(Registries.ENTITY_TYPE, key.getValue(), type);
    }
}
