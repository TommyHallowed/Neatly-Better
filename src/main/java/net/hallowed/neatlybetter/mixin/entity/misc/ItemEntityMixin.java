package net.hallowed.neatlybetter.mixin.entity.misc;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.content.feature.SaplingAutoPlanter;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Shadow public abstract ItemStack getItem();
    @Shadow private int age;

    // Replaces a per-tick scan of every item entity in the level. Vanilla despawns at age 6000.
    @Inject(method = "tick", at = @At("HEAD"))
    private void neatlybetter$autoPlantSapling(CallbackInfo ci) {
        if (this.age < 5980) return;
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level() instanceof ServerLevel level && NTServerConfig.CONFIG.saplingAutoReplant.isTrue()) {
            SaplingAutoPlanter.tryPlantSapling(level, self);
        }
    }

    @Inject(method = "fireImmune()Z", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$charcoalIsFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem().is(Items.CHARCOAL)) {
            cir.setReturnValue(true);
        }
    }
}