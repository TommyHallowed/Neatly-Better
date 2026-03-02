package net.hallowed.oldways.content.feature;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public final class MagmaSpawnsLava {
    private MagmaSpawnsLava() {}

    public static void init() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld)) return true;
            if (!state.isOf(Blocks.MAGMA_BLOCK)) return true;

            ItemStack stack = player.getMainHandStack();
            if (!hasSilkTouch(stack)) {
                BlockPos targetPos = pos.toImmutable();

                world.removeBlock(pos, false);

                serverWorld.setBlockState(targetPos, Blocks.LAVA.getDefaultState());

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

                    serverWorld.spawnParticles(
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
                        SoundEvents.BLOCK_LAVA_EXTINGUISH,
                        SoundCategory.BLOCKS,
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
        ItemEnchantmentsComponent ench = stack.getOrDefault(
                DataComponentTypes.ENCHANTMENTS,
                ItemEnchantmentsComponent.DEFAULT
        );
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> e : ench.getEnchantmentEntries()) {
            if (e.getKey().matchesKey(Enchantments.SILK_TOUCH) && e.getIntValue() > 0) {
                return true;
            }
        }
        return false;
    }
}
