package net.hallowed.neatlybetter.mixin.blockentity;

import net.hallowed.neatlybetter.feature.building.GlowingBanners;
import net.hallowed.neatlybetter.registry.ModData;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Glowing banner item component ⇄ block entity attachment (placing, pick block, getItem). */
@Mixin(BannerBlockEntity.class)
public abstract class BannerBlockEntityMixin {

    @Inject(method = "applyImplicitComponents", at = @At("TAIL"))
    private void neatlybetter$readGlow(DataComponentGetter components, CallbackInfo ci) {
        BannerBlockEntity self = (BannerBlockEntity) (Object) this;
        if (components.get(ModData.GLOWING_BANNER_ITEM) != null) self.setAttached(ModData.GLOWING_BANNER, true);
    }

    @Inject(method = "collectImplicitComponents", at = @At("TAIL"))
    private void neatlybetter$writeGlow(DataComponentMap.Builder components, CallbackInfo ci) {
        if (GlowingBanners.isGlowing((BannerBlockEntity) (Object) this)) components.set(ModData.GLOWING_BANNER_ITEM, GlowingBanners.Glowing.INSTANCE);
    }
}
