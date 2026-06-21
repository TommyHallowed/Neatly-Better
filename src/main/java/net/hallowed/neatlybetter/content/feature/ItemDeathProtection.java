package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.hallowed.neatlybetter.config.NTServerConfig;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;

public final class ItemDeathProtection {
    private ItemDeathProtection() {}

    public static void init() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!NTServerConfig.CONFIG.keepRecoveryCompassOnDeath.get()) {
                return;
            }

            if (alive || oldPlayer.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
                return;
            }

            Inventory oldInventory = oldPlayer.getInventory();
            Inventory newInventory = newPlayer.getInventory();
            for (int i = 0; i < Inventory.INVENTORY_SIZE; ++i) {
                ItemStack itemStack = oldInventory.getItem(i);
                if (!itemStack.isEmpty() && itemStack.is(Items.RECOVERY_COMPASS)) {
                    newInventory.setItem(i, itemStack.copy());
                }
            }
        });
    }
}