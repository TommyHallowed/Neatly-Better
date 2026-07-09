package net.hallowed.neatlybetter.content.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jspecify.annotations.NonNull;

public class MilkBottleItem extends Item {

    public MilkBottleItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull ItemStack finishUsingItem(@NonNull ItemStack stack, @NonNull Level level, @NonNull LivingEntity entityLiving) {
        Player player = entityLiving instanceof Player p ? p : null;

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }

        if (!level.isClientSide()) {
            removeRandomEffect(entityLiving);
        }

        entityLiving.gameEvent(GameEvent.DRINK);

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));

            if (!player.isCreative()) {
                stack.shrink(1);

                if (stack.isEmpty()) {
                    return new ItemStack(Items.GLASS_BOTTLE);
                }

                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }

            return stack;
        }

        stack.shrink(1);
        return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
    }

    private void removeRandomEffect(LivingEntity entity) {
        List<MobEffectInstance> active = new ArrayList<>(entity.getActiveEffects());
        if (active.isEmpty()) return;

        List<MobEffectInstance> negative = active.stream()
                .filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                .toList();

        List<MobEffectInstance> pool = negative.isEmpty() ? active : negative;

        MobEffectInstance toRemove = pool.get(entity.getRandom().nextInt(pool.size()));
        entity.removeEffect(toRemove.getEffect());
    }
}