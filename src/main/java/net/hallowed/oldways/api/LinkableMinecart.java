package net.hallowed.oldways.api;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface LinkableMinecart {
    @Nullable AbstractMinecartEntity oldways$getFollowing();
    void oldways$setFollowing(AbstractMinecartEntity following);

    @Nullable AbstractMinecartEntity oldways$getFollower();
    void oldways$setFollower(AbstractMinecartEntity follower);

    ItemStack oldways$getLinkItem();
    void oldways$setLinkItem(ItemStack linkItem);

    int oldways$getFollowingId();
}