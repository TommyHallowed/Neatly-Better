package net.hallowed.neatlybetter.client.mixin.ui;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.hallowed.neatlybetter.client.util.FloatBlitSprite;
import net.hallowed.neatlybetter.client.util.SubpixelTexturedQuad;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin implements FloatBlitSprite {

    // ── Existing shadows (crosshair feature) ─────────────────────────────────

    @Shadow @Final private TextureAtlas guiSprites;
    @Shadow @Final public GuiRenderState guiRenderState;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private Matrix3x2fStack pose;
    @Shadow @Final public GuiGraphicsExtractor.ScissorStack scissorStack;

    // ── Additional shadows (durability feature) ───────────────────────────────

    @Shadow public abstract Matrix3x2fStack pose();

    @Shadow public abstract void text(Font font, @Nullable String str,
                                      int x, int y, int color, boolean dropShadow);

    // ── Crosshair: float-precision sprite blit ────────────────────────────────

    @Override
    @Unique
    public void neatlybetter$blitSpriteFloat(RenderPipeline pipeline, Identifier texture,
                                             float x, float y, int width, int height) {
        if (width == 0 || height == 0) return;

        TextureAtlasSprite sprite = this.guiSprites.getSprite(texture);

        float u0 = sprite.getU0() + (1f / 32768f);
        float u1 = sprite.getU1() + (1f / 32768f);
        float v0 = sprite.getV0() - (1f / 32768f);
        float v1 = sprite.getV1() - (1f / 32768f);

        AbstractTexture tex = this.minecraft.getTextureManager().getTexture(sprite.atlasLocation());
        this.guiRenderState.addGuiElement(new SubpixelTexturedQuad(
                pipeline,
                TextureSetup.singleTexture(tex.getTextureView(), tex.getSampler()),
                new Matrix3x2f(this.pose),
                x,         y,
                x + width, y + height,
                u0, u1, v0, v1,
                -1,
                this.scissorStack.peek()
        ));
    }

    // ── Durability: small number in item slot top-right corner ────────────────

    @Inject(
            method = "itemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("TAIL")
    )
    private void neatlybetter$renderDurability(Font font, ItemStack itemStack,
                                               int x, int y, @Nullable String countText,
                                               CallbackInfo ci) {
        if (!NTClientConfig.CONFIG.showDurability.get()) return;

        if (itemStack.getCount() != 1) return;
        if (!itemStack.isDamageableItem()) return;
        if (itemStack.getDamageValue() == 0) return;

        int current = itemStack.getMaxDamage() - itemStack.getDamageValue();
        String durability = String.valueOf(current);

        int color = itemStack.getBarColor() | 0xFF000000;

        Matrix3x2fStack poseStack = this.pose();
        poseStack.pushMatrix();

        poseStack.scale(1f / 2, 1f / 2);

        int textX = (2 * x) + (16 / 2) + 5 + 19 - 2 - font.width(durability);
        int textY = (2 * y) + (16 / 2) + 1 + 6 + 3;

        this.text(font, durability, textX, textY, color, true);

        poseStack.popMatrix();
    }
}