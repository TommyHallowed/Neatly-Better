package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TorchIgnite {
    private TorchIgnite() {}

    public static void init() {
        AttackEntityCallback.EVENT.register((player, world, _, entity, _) -> {
            if (!(world instanceof ServerLevel)) return InteractionResult.PASS;

            ItemStack mainHand = player.getMainHandItem();
            if (!mainHand.is(Items.TORCH) && !mainHand.is(Items.SOUL_TORCH) && !mainHand.is(Items.COPPER_TORCH)) {
                return InteractionResult.PASS;
            }
            world.playSound(null, entity.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.0F);
            entity.igniteForSeconds(4);

            return InteractionResult.PASS;
        });
    }
}
