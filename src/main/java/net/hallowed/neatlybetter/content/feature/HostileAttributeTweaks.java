package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;

import org.jetbrains.annotations.NotNull;

public final class HostileAttributeTweaks {
    private HostileAttributeTweaks() {}

    private static final Identifier ID_SPIDER_SPEED  = Identifier.fromNamespaceAndPath("neatly-better", "mods/spider_speed");
    private static final Identifier ID_SPIDER_KBRES  = Identifier.fromNamespaceAndPath("neatly-better", "mods/spider_kbres");
    private static final Identifier ID_DRAGON_KBRES  = Identifier.fromNamespaceAndPath("neatly-better", "mods/dragon_kbres");

    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register(HostileAttributeTweaks::onEntityLoad);
    }

    private static void onEntityLoad(Entity e, ServerLevel world) {
        if (!(e instanceof LivingEntity living)) return;

        //noinspection IfCanBeSwitch
        if (e instanceof Spider) {
            apply(living, Attributes.MOVEMENT_SPEED,
                    new AttributeModifier(ID_SPIDER_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            apply(living, Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(ID_SPIDER_KBRES, 0.2, AttributeModifier.Operation.ADD_VALUE));
        }

        else if (e instanceof EnderDragon) {
            apply(living, Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(ID_DRAGON_KBRES, 1, AttributeModifier.Operation.ADD_VALUE));
        }

        else if (e instanceof WitherSkeleton wither) {
            wither.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        }
    }

    private static void apply(LivingEntity living, Holder<@NotNull Attribute> attr, AttributeModifier mod) {
        AttributeInstance inst = living.getAttribute(attr);
        if (inst == null) return;
        inst.removeModifier(mod.id());
        inst.addPermanentModifier(mod);
    }
}
