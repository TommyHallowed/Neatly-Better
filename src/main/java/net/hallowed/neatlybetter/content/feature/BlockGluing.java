package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.NeatlyBetter;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.particles.ParticleTypes;

public final class BlockGluing {
    private BlockGluing() {}

    public static void init() {
        UseBlockCallback.EVENT.register(BlockGluing::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof TrapDoorBlock)
                && !(state.getBlock() instanceof DoorBlock)
                && !(state.getBlock() instanceof FenceGateBlock)) return InteractionResult.PASS;
        if (!state.hasProperty(NeatlyBetter.GLUED)) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);

        // --- Axe: remove glue ---
        if (stack.getItem() instanceof AxeItem) {
            if (!state.getValue(NeatlyBetter.GLUED)) return InteractionResult.PASS;

            if (!level.isClientSide()) {
                BlockState unglued = state.setValue(NeatlyBetter.GLUED, false);
                level.setBlock(pos, unglued, 3);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, unglued));

                level.playSound(null, pos, SoundEvents.SLIME_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);

                spawnRemoveParticles((ServerLevel) level, pos);

                stack.hurtAndBreak(1, (ServerLevel) level, player instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null,
                        item -> player.onEquippedItemBroken(item, net.minecraft.world.entity.EquipmentSlot.MAINHAND));
            }

            return InteractionResult.SUCCESS;
        }

        // --- Slimeball: apply glue ---
        if (!stack.is(Items.SLIME_BALL)) return InteractionResult.PASS;

        if (state.getValue(NeatlyBetter.GLUED)) {
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide()) {
            BlockState glued = state.setValue(NeatlyBetter.GLUED, true);
            level.setBlock(pos, glued, 3);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, glued));

            level.playSound(null, pos, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS, 1.0F, 0.8F);

            spawnGlueParticles((ServerLevel) level, pos);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static void spawnGlueParticles(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        for (int i = 0; i < 8; i++) {
            double ox = (level.getRandom().nextDouble() - 0.5) * 0.8;
            double oy =  level.getRandom().nextDouble()        * 0.6;
            double oz = (level.getRandom().nextDouble() - 0.5) * 0.8;
            level.sendParticles(ParticleTypes.WAX_ON,
                    cx + ox, cy + oy, cz + oz,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static void spawnRemoveParticles(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        for (int i = 0; i < 8; i++) {
            double ox = (level.getRandom().nextDouble() - 0.5) * 0.8;
            double oy =  level.getRandom().nextDouble()        * 0.6;
            double oz = (level.getRandom().nextDouble() - 0.5) * 0.8;
            level.sendParticles(ParticleTypes.WAX_OFF,
                    cx + ox, cy + oy, cz + oz,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}