package net.hallowed.neatlybetter.client.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.hallowed.neatlybetter.config.ShieldDelayHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class ClientLivingEntityMixin {

    @WrapOperation(
            method = "getItemBlockingWith",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/component/BlocksAttacks;blockDelayTicks()I"
            )
    )
    private int neatlybetter$customBlockDelay(BlocksAttacks instance, Operation<Integer> original) {
        int configDelay = ShieldDelayHolder.getShieldRaiseDelay();
        if (configDelay == 5) return original.call(instance);
        return configDelay;
    }
}
