package net.hallowed.neatlybetter.mixin.entity.player;

import net.hallowed.neatlybetter.util.DeathSlotData;
import net.hallowed.neatlybetter.init.ModDataComponents;
import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow @Final private NonNullList<ItemStack> items;
    @Shadow @Final public Player player;
    @Shadow @Final private EntityEquipment equipment;

    @Shadow public abstract ItemStack getItem(int slot);
    @Shadow public abstract void setItem(int slot, ItemStack itemStack);
    @Shadow public abstract boolean add(ItemStack itemStack);

    @Inject(method = "dropAll", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$keepRecoveryCompassesOnDeath(CallbackInfo ci) {
        boolean restoreSlots = NTServerConfig.CONFIG.restoreDeathDropSlots.get();
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack = this.items.get(i);
            if (itemStack.isEmpty() || neatlybetter$protectsAgainstDeath(itemStack)) {
                continue;
            }

            if (restoreSlots) {
                itemStack.set(ModDataComponents.DEATH_SLOT, new DeathSlotData(this.player.getUUID(), i));
            }
            this.player.drop(itemStack, true, false);
            this.items.set(i, ItemStack.EMPTY);
        }

        this.equipment.dropAll(this.player);
        ci.cancel();
    }

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$restoreDeathSlot(int slot, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (!NTServerConfig.CONFIG.restoreDeathDropSlots.get()) return;
        if (slot != -1 || itemStack.isEmpty()) return;

        DeathSlotData data = itemStack.get(ModDataComponents.DEATH_SLOT);
        if (data == null) return;

        int target = data.slot();
        if (!data.owner().equals(this.player.getUUID()) || target < 0 || target >= Inventory.INVENTORY_SIZE) return;

        ItemStack occupant = this.getItem(target);
        ItemStack toPlace = itemStack.copyAndClear();
        toPlace.remove(ModDataComponents.DEATH_SLOT);
        this.setItem(target, toPlace);

        if (!occupant.isEmpty() && !this.add(occupant)) {
            this.player.drop(occupant, false);
        }

        cir.setReturnValue(true);
    }

    @Unique
    private static boolean neatlybetter$protectsAgainstDeath(ItemStack itemStack) {
        return itemStack.is(Items.RECOVERY_COMPASS);
    }
}