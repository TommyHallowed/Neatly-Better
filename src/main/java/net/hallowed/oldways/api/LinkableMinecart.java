package net.hallowed.oldways.api;

import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface LinkableMinecart {
    @Nullable AbstractMinecart oldways$getFollowing();
    void oldways$setFollowing(AbstractMinecart following);

    @Nullable AbstractMinecart oldways$getFollower();
    void oldways$setFollower(AbstractMinecart follower);

    ItemStack oldways$getLinkItem();
    void oldways$setLinkItem(ItemStack linkItem);

    int oldways$getFollowingId();
}