package net.hallowed.neatlybetter.mixin.entity.generic;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "getAttackBoundingBox", at = @At("RETURN"), cancellable = true)
    private void neatlybetter$extraReach(CallbackInfoReturnable<AABB> cir) {
        Object self = this;
        AABB base = cir.getReturnValue();

        if (!NTServerConfig.CONFIG.oldSpiderAttacks.get()) {
            if (self instanceof Spider) {
                cir.setReturnValue(base.inflate(0.65, 0.65, 0.65));
            }
        }
    }
}
