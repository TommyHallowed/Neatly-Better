package net.hallowed.neatlybetter.feature.mobs;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import net.hallowed.neatlybetter.registry.ModItems;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public final class CowBottleInteraction {
    private CowBottleInteraction() {}

    public static void register() {
        UseEntityCallback.EVENT.register(CowBottleInteraction::onUseEntity);
    }

    private static InteractionResult onUseEntity(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!(entity instanceof Cow cow) || cow.isBaby()) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.GLASS_BOTTLE)) return InteractionResult.PASS;

        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        world.playSound(null, cow.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0f, 1.0f);

        ItemStack result = ItemUtils.createFilledResult(held, player, new ItemStack(ModItems.MILK_BOTTLE));
        player.setItemInHand(hand, result);

        return InteractionResult.SUCCESS_SERVER;
    }
}