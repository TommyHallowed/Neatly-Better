package net.hallowed.neatlybetter.util;

import net.hallowed.neatlybetter.api.LinkableMinecart;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CartUtils {


    public static void unlinkFromParent(AbstractMinecart entity) {
        if (entity != null) {
            LinkableMinecart linkable = (LinkableMinecart) entity;
            AbstractMinecart following = linkable.neatlybetter$getFollowing();

            if (following != null) {
                ((LinkableMinecart) following).neatlybetter$setFollower(null);
                linkable.neatlybetter$setFollowing(null);
                entity.setDeltaMovement(0.0, 0.0, 0.0);

                ItemStack linkItem = linkable.neatlybetter$getLinkItem();
                if (!linkItem.isEmpty() && entity.level() instanceof ServerLevel serverWorld) {
                    entity.spawnAtLocation(serverWorld, linkItem);
                }
                linkable.neatlybetter$setLinkItem(ItemStack.EMPTY);
            }
        }
    }

    public static void linkTo(AbstractMinecart minecart, AbstractMinecart to, ItemStack linkingItem) {
        ((LinkableMinecart) minecart).neatlybetter$setFollowing(to);
        ((LinkableMinecart) to).neatlybetter$setFollower(minecart);

        if (!linkingItem.isEmpty()) {
            ItemStack linkStack = linkingItem.copy();
            linkStack.setCount(1);
            ((LinkableMinecart) minecart).neatlybetter$setLinkItem(linkStack);
        } else {
            ((LinkableMinecart) minecart).neatlybetter$setLinkItem(new ItemStack(Items.IRON_CHAIN));
        }
    }
}