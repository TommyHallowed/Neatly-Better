package net.hallowed.oldways.content.feature;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class HostileAttributeTweaks {
    private HostileAttributeTweaks() {}

    public static double SPIDER_SPEED_MULT = 0.2;
    public static double SPIDER_KB_RESIST_ADD = 0.2;
    public static double SPIDER_EXTRA_REACH = 0.65;

    public static double ZOMBIE_SPEED_MULT = 0.15;
    public static double ZOMBIE_KB_RESIST_ADD = 0.10;

    public static double IRON_GOLEM_EXTRA_REACH = 1.35;

    public static double DRAGON_KB_RESIST_ADD = 1.00;

    private static final Identifier ID_SPIDER_SPEED   = Identifier.of("old-ways", "mods/spider_speed");
    private static final Identifier ID_SPIDER_KBRES   = Identifier.of("old-ways", "mods/spider_kbres");

    private static final Identifier ID_ZOMBIELIKE_SPEED = Identifier.of("old-ways", "mods/zombie_like_speed");
    private static final Identifier ID_ZOMBIELIKE_KBRES = Identifier.of("old-ways", "mods/zombie_like_kbres");

    private static final Identifier ID_DRAGON_KBRES   = Identifier.of("old-ways", "mods/dragon_kbres");

    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register(HostileAttributeTweaks::onEntityLoad);
    }

    private static void onEntityLoad(Entity e, ServerWorld w) {
        if (!(e instanceof LivingEntity living)) return;

        if (e instanceof SpiderEntity) {
            apply(living, EntityAttributes.MOVEMENT_SPEED,
                    new EntityAttributeModifier(ID_SPIDER_SPEED, SPIDER_SPEED_MULT, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            apply(living, EntityAttributes.KNOCKBACK_RESISTANCE,
                    new EntityAttributeModifier(ID_SPIDER_KBRES, SPIDER_KB_RESIST_ADD, EntityAttributeModifier.Operation.ADD_VALUE));

        } else if (e instanceof ZombieEntity || e instanceof ZombieVillagerEntity || e instanceof HuskEntity) {
            if (ZOMBIE_SPEED_MULT != 0.0) {
                apply(living, EntityAttributes.MOVEMENT_SPEED,
                        new EntityAttributeModifier(ID_ZOMBIELIKE_SPEED, ZOMBIE_SPEED_MULT, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            if (ZOMBIE_KB_RESIST_ADD != 0.0) {
                apply(living, EntityAttributes.KNOCKBACK_RESISTANCE,
                        new EntityAttributeModifier(ID_ZOMBIELIKE_KBRES, ZOMBIE_KB_RESIST_ADD, EntityAttributeModifier.Operation.ADD_VALUE));
            }

        } else if (e instanceof EnderDragonEntity) {
            if (DRAGON_KB_RESIST_ADD != 0.0) {
                apply(living, EntityAttributes.KNOCKBACK_RESISTANCE,
                        new EntityAttributeModifier(ID_DRAGON_KBRES, DRAGON_KB_RESIST_ADD, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }
            else if (e instanceof WitherSkeletonEntity wither) {
            wither.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0F);
        }
    }

    private static void apply(LivingEntity living, RegistryEntry<EntityAttribute> attr, EntityAttributeModifier mod) {
        EntityAttributeInstance inst = living.getAttributeInstance(attr);
        if (inst == null) return;
        inst.removeModifier(mod.id());
        inst.addPersistentModifier(mod);
    }
}
