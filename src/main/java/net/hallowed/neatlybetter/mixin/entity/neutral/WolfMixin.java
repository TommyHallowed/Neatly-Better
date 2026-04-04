package net.hallowed.neatlybetter.mixin.entity.neutral;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Wolf.class)
public abstract class WolfMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void neatlybetter$sittingRegen(CallbackInfo ci) {
        Wolf self = (Wolf) (Object) this;
        if (!self.level().isClientSide()
                && NTServerConfig.CONFIG.wolfImprovements.get()
                && self.isTame()
                && self.isOrderedToSit()
                && self.getHealth() < self.getMaxHealth()
                && self.tickCount % 60 == 0) {
            self.heal(1.0F);
        }
    }
}
