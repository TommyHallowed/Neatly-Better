package net.hallowed.oldways.content.feature;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public final class MagmaSpawnsLava {
    private MagmaSpawnsLava() {}

    public static void init() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerLevel serverWorld)) return true;
            if (!state.is(Blocks.MAGMA_BLOCK)) return true;

            ItemStack stack = player.getMainHandItem();
            if (!hasSilkTouch(stack)) {
                BlockPos targetPos = pos.immutable();

                world.removeBlock(pos, false);

                serverWorld.setBlockAndUpdate(targetPos, Blocks.LAVA.defaultBlockState());

                double cx = targetPos.getX() + 0.5;
                double cy = targetPos.getY() + 0.9;
                double cz = targetPos.getZ() + 0.5;

                int count = 15;
                double radius = 0.4;

                for (int i = 0; i < count; i++) {
                    double angle = (2 * Math.PI / count) * i;
                    double x = cx + Math.cos(angle) * radius;
                    double z = cz + Math.sin(angle) * radius;
                    double vy = 0.05 + world.getRandom().nextDouble() * 0.04;

                    serverWorld.sendParticles(
                            ParticleTypes.FLAME,
                            x, cy, z,
                            1,
                            0.0, vy, 0.0,
                            0.01
                    );
                }

                serverWorld.playSound(
                        null,
                        cx, cy, cz,
                        SoundEvents.LAVA_EXTINGUISH,
                        SoundSource.BLOCKS,
                        0.6f,
                        1.0f + (serverWorld.getRandom().nextFloat() - 0.5f) * 0.2f
                );

                return false;
            }
            return true;
        });
    }

    private static boolean hasSilkTouch(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ItemEnchantments ench = stack.getOrDefault(
                DataComponents.ENCHANTMENTS,
                ItemEnchantments.EMPTY
        );
        for (Object2IntMap.Entry<Holder<@NotNull Enchantment>> e : ench.entrySet()) {
            if (e.getKey().is(Enchantments.SILK_TOUCH) && e.getIntValue() > 0) {
                return true;
            }
        }
        return false;
    }
}
