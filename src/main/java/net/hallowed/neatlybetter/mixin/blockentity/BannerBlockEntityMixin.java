package net.hallowed.neatlybetter.mixin.blockentity;

import com.mojang.serialization.Codec;

import net.hallowed.neatlybetter.api.EmissiveBannerAccessor;
import net.hallowed.neatlybetter.init.ModDataComponents;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BannerBlockEntity.class)
public abstract class BannerBlockEntityMixin implements EmissiveBannerAccessor {

    @Unique
    private boolean neatlybetter$emissive = false;

    /* ---------- EmissiveBannerAccessor ---------- */

    @Override
    public boolean neatlybetter$isEmissive() {
        return neatlybetter$emissive;
    }

    @Override
    public void neatlybetter$setEmissive(boolean emissive) {
        this.neatlybetter$emissive = emissive;
    }

    /* ---------- NBT persistence ---------- */

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void neatlybetter$saveEmissive(ValueOutput output, CallbackInfo ci) {
        if (neatlybetter$emissive) {
            output.store("neatlybetter_emissive", Codec.BOOL, true);
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void neatlybetter$loadEmissive(ValueInput input, CallbackInfo ci) {
        neatlybetter$emissive = input.read("neatlybetter_emissive", Codec.BOOL).orElse(false);
    }

    /* ---------- Data-component round-trip (pick-up / place) ---------- */

    @Inject(method = "collectImplicitComponents", at = @At("TAIL"))
    private void neatlybetter$collectEmissive(DataComponentMap.Builder builder, CallbackInfo ci) {
        if (neatlybetter$emissive) {
            builder.set(ModDataComponents.EMISSIVE_BANNER, true);
        }
    }

    @Inject(method = "applyImplicitComponents", at = @At("TAIL"))
    private void neatlybetter$applyEmissive(DataComponentGetter getter, CallbackInfo ci) {
        neatlybetter$emissive = getter.getOrDefault(ModDataComponents.EMISSIVE_BANNER, false);
    }

    @Inject(method = "removeComponentsFromTag", at = @At("TAIL"))
    private void neatlybetter$removeEmissiveTag(ValueOutput output, CallbackInfo ci) {
        output.discard("neatlybetter_emissive");
    }
}
