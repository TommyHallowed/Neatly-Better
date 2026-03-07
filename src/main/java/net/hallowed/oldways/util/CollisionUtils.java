package net.hallowed.oldways.util;

import net.hallowed.oldways.api.LinkableMinecart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public class CollisionUtils {
    public static boolean shouldCollide(Entity source, Entity target) {
        if (!(source instanceof AbstractMinecart check)) {
            return true;
        } else {
            int i = 0;
            int collisionDepth = 20; // Safe threshold from Linkart

            while (check != target) {
                check = ((LinkableMinecart) check).oldways$getFollower();
                ++i;
                if (check == null || i >= collisionDepth) {
                    check = (AbstractMinecart) source;
                    i = 0;

                    while (check != target) {
                        check = ((LinkableMinecart) check).oldways$getFollowing();
                        ++i;
                        if (check == null || i >= collisionDepth) {
                            return true;
                        }
                    }
                    return false;
                }
            }
            return false;
        }
    }
}