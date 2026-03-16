package net.hallowed.neatlybetter.compat;

import net.hallowed.neatlybetter.api.NTCompat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public final class BackpackedServerCompat {
    private BackpackedServerCompat() {}

    public static List<ItemStack> getBackpackStacks(Player player) {
        if (!NTCompat.BACKPACKED || player == null) return Collections.emptyList();
        try {
            return Accessor.getStacks(player);
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }

    private static final class Accessor {
        static List<ItemStack> getStacks(Player player) {
            return com.mrcrayfish.backpacked.BackpackHelper.getBackpacks(player);
        }
    }
}
