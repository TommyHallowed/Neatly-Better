package net.hallowed.oldways.util;

import net.hallowed.oldways.api.LinkableMinecart;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;

public class CartUtils {


    public static void unlinkFromParent(AbstractMinecartEntity entity) {
        if (entity != null) {
            LinkableMinecart linkable = (LinkableMinecart) entity;
            AbstractMinecartEntity following = linkable.oldways$getFollowing();

            if (following != null) {
                ((LinkableMinecart) following).oldways$setFollower(null);
                linkable.oldways$setFollowing(null);
                entity.setVelocity(0.0, 0.0, 0.0);

                ItemStack linkItem = linkable.oldways$getLinkItem();
                // FIX: Check for ServerWorld and pass it into dropStack!
                if (!linkItem.isEmpty() && entity.getEntityWorld() instanceof ServerWorld serverWorld) {
                    entity.dropStack(serverWorld, linkItem);
                }
                linkable.oldways$setLinkItem(ItemStack.EMPTY);
            }
        }
    }

    public static void linkTo(AbstractMinecartEntity minecart, AbstractMinecartEntity to, ItemStack linkingItem) {
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