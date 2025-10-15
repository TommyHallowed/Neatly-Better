package net.hallowed.oldways.content.item;

import net.hallowed.oldways.content.entity.DragonBurstTrailEntity;
import net.hallowed.oldways.init.ModEntities;
import net.hallowed.oldways.init.ModItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

public class DragonBurstRocketItem extends FireworkRocketItem {
    public DragonBurstRocketItem(Item.Settings settings) {
        super(settings);
    }

    private static ItemStack asFlight2(ItemStack original) {
        ItemStack one = original.copyWithCount(1);
        one.set(DataComponentTypes.FIREWORKS, new FireworksComponent(2, List.of()));
        return one;
    }

    private static ActionResult boost(World world, PlayerEntity user, ItemStack stack) {
        if (!(world instanceof ServerWorld sw)) return ActionResult.SUCCESS;

        ItemStack flight2 = asFlight2(stack);
        sw.spawnEntity(new FireworkRocketEntity(world, flight2, user));

        int lifeTime = 10 * 3 + world.getRandom().nextInt(6) + world.getRandom().nextInt(7);
        DragonBurstTrailEntity trail =
                new DragonBurstTrailEntity(ModEntities.DRAGON_BURST_TRAIL, sw, user.getId(), lifeTime);
        sw.spawnEntity(trail);

        stack.decrementUnlessCreative(1, user);
        user.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
        if (!user.isInCreativeMode()) {
        user.getItemCooldownManager().set(new ItemStack(ModItems.DRAGON_BURST_ROCKET), 1200);
        }
        return ActionResult.SUCCESS_SERVER;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!user.isGliding()) return ActionResult.PASS;
        return boost(world, user, user.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        PlayerEntity user = ctx.getPlayer();
        if (user == null || !user.isGliding()) return ActionResult.PASS;
        return boost(ctx.getWorld(), user, ctx.getStack());
    }
}
