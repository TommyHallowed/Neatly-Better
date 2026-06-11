package net.hallowed.neatlybetter.mixin.blockentity;

import net.hallowed.neatlybetter.api.PremiumBaseAccessor;
import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BeaconMenu.class, priority = 900)
public class BeaconMenuMixin implements PremiumBaseAccessor {

    @Unique
    private int neatlybetter$premiumBaseValue = 0;

    @Inject(
            method = "<init>(ILnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL")
    )
    private void neatlybetter$injectExtraDataSlot(
            int containerId,
            net.minecraft.world.Container inventory,
            ContainerData beaconData,
            ContainerLevelAccess access,
            CallbackInfo ci) {

        BeaconMenuMixin self = this;

        if (!NTServerConfig.CONFIG.beaconSaturationEffect.get()) return;

        ContainerData extraSlot = new ContainerData() {
            @Override
            public int get(int index) {
                if (index != 0) return 0;

                return access.evaluate((level, pos) -> {
                    if (level.getBlockEntity(pos) instanceof BeaconBlockEntity be) {
                        return ((PremiumBaseAccessor) be).neatlybetter$getPremiumBase();
                    }
                    return 0;
                }).orElse(self.neatlybetter$premiumBaseValue);

            }

            @Override
            public void set(int index, int value) {
                if (index != 0) return;

                self.neatlybetter$premiumBaseValue = value;
            }

            @Override
            public int getCount() {
                return 1;
            }
        };

        ((AbstractContainerMenuAccessor) this).invokeAddDataSlots(extraSlot);
    }

    @Override
    @Unique
    public int neatlybetter$getPremiumBase() {
        return neatlybetter$premiumBaseValue;
    }
}