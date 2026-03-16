package net.hallowed.neatlybetter.util;

import net.hallowed.neatlybetter.api.LinkableMinecart;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public class CollisionUtils {
    public static boolean shouldCollide(Entity source, Entity target) {
        if (!(source instanceof AbstractMinecart check)) {
            return true;
        } else {
            int i = 0;
            int collisionDepth = 20;

            while (check != target) {
                check = ((LinkableMinecart) check).neatlybetter$getFollower();
                ++i;
                if (check == null || i >= collisionDepth) {
                    check = (AbstractMinecart) source;
                    i = 0;

                    while (check != target) {
                        check = ((LinkableMinecart) check).neatlybetter$getFollowing();
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