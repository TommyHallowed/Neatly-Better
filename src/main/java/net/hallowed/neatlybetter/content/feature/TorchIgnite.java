package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.TorchBlock;

public final class TorchIgnite {
    private TorchIgnite() {}

    private record IgniteProfile(float chance, int seconds) {}

    public static void init() {
        AttackEntityCallback.EVENT.register((player, world, _, entity, _) -> {
            if (!(world instanceof ServerLevel serverLevel)) return InteractionResult.PASS;
            if (NTServerConfig.CONFIG.torchIgnite.isFalse()) return InteractionResult.PASS;

            ItemStack mainHand = player.getMainHandItem();
            if (!(mainHand.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof TorchBlock)) {
                return InteractionResult.PASS;
            }

            IgniteProfile profile = profileFor(serverLevel.getDifficulty());
            if (serverLevel.getRandom().nextFloat() >= profile.chance()) {
                return InteractionResult.PASS;
            }

            world.playSound(null, entity.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.0F);
            entity.igniteForSeconds(profile.seconds());

            return InteractionResult.PASS;
        });
    }

    private static IgniteProfile profileFor(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL, EASY -> new IgniteProfile(1.0F, 6);
            case NORMAL -> new IgniteProfile(0.6F, 5);
            case HARD -> new IgniteProfile(0.2F, 4);
        };
    }
}