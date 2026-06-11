package net.hallowed.neatlybetter.mixin.entity.misc;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractThrownPotion.class)
public class AbstractThrownPotionMixin {

    @Inject(method = "onHit", at = @At("HEAD"))
    private void neatlybetter$invisibleFrameOnPotionHit(HitResult hitResult, CallbackInfo ci) {
        AbstractThrownPotion self = (AbstractThrownPotion)(Object) this;
        if (!(self.level() instanceof ServerLevel)) return;

        ItemStack potionStack = self.getItem();
        PotionContents contents = potionStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

        boolean hasInvisibility = false;
        for (var effect : contents.getAllEffects()) {
            if (effect.getEffect().is(MobEffects.INVISIBILITY)) {
                hasInvisibility = true;
                break;
            }
        }
        if (!hasInvisibility) return;

        AABB potionAabb = self.getBoundingBox().move(hitResult.getLocation().subtract(self.position()));
        AABB searchAabb = potionAabb.inflate(4.0, 2.0, 4.0);
        float margin = ProjectileUtil.computeMargin(self);

        for (ItemFrame frame : self.level().getEntitiesOfClass(ItemFrame.class, searchAabb)) {
            double distSq = potionAabb.distanceToSqr(frame.getBoundingBox().inflate(margin));
            if (distSq < 16.0) {
                frame.setInvisible(true);
            }
        }
    }
}