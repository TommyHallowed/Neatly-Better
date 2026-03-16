package net.hallowed.neatlybetter.client.mixin.render.banner;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.hallowed.neatlybetter.api.EmissiveBannerAccessor;

import net.minecraft.client.renderer.blockentity.state.BannerRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(BannerRenderState.class)
public class BannerRenderStateMixin implements EmissiveBannerAccessor {

    @Unique
    private boolean neatlybetter$emissive;

    @Override
    public boolean neatlybetter$isEmissive() {
        return neatlybetter$emissive;
    }

    @Override
    public void neatlybetter$setEmissive(boolean emissive) {
        this.neatlybetter$emissive = emissive;
    }
}
