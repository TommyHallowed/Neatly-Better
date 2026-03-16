package net.hallowed.neatlybetter.api;

import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public interface LinkableMinecart {
    @Nullable AbstractMinecart neatlybetter$getFollowing();
    void neatlybetter$setFollowing(AbstractMinecart following);

    @Nullable AbstractMinecart neatlybetter$getFollower();
    void neatlybetter$setFollower(AbstractMinecart follower);

    ItemStack neatlybetter$getLinkItem();
    void neatlybetter$setLinkItem(ItemStack linkItem);

    int neatlybetter$getFollowingId();
}