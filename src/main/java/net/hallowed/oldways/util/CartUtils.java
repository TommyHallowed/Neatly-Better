package net.hallowed.oldways.util;

import net.hallowed.oldways.api.LinkableMinecart;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CartUtils {


    public static void unlinkFromParent(AbstractMinecart entity) {
        if (entity != null) {
            LinkableMinecart linkable = (LinkableMinecart) entity;
            AbstractMinecart following = linkable.oldways$getFollowing();

            if (following != null) {
                ((LinkableMinecart) following).oldways$setFollower(null);
                linkable.oldways$setFollowing(null);
                entity.setDeltaMovement(0.0, 0.0, 0.0);

                ItemStack linkItem = linkable.oldways$getLinkItem();
                // FIX: Check for ServerWorld and pass it into dropStack!
                if (!linkItem.isEmpty() && entity.level() instanceof ServerLevel serverWorld) {
                    entity.spawnAtLocation(serverWorld, linkItem);
                }
                linkable.oldways$setLinkItem(ItemStack.EMPTY);
            }
        }
    }

    public static void linkTo(AbstractMinecart minecart, AbstractMinecart to, ItemStack linkingItem) {
        ((LinkableMinecart) minecart).oldways$setFollowing(to);
        ((LinkableMinecart) to).oldways$setFollower(minecart);

        if (!linkingItem.isEmpty()) {
            ItemStack linkStack = linkingItem.copy();
            linkStack.setCount(1);
            ((LinkableMinecart) minecart).oldways$setLinkItem(linkStack);
        } else {
            ((LinkableMinecart) minecart).oldways$setLinkItem(new ItemStack(Items.IRON_CHAIN));
        }
    }
}