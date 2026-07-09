package net.hallowed.neatlybetter.client.mixin.screen;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;

import net.hallowed.neatlybetter.data.CarpetPatternData;
import net.hallowed.neatlybetter.init.ModData;
import net.hallowed.neatlybetter.util.CarpetLoomSupport;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin {

    @Unique
    private static final Logger neatlybetter$LOGGER = LogUtils.getLogger();

    @Shadow
    private boolean hasMaxPatterns;

    @Unique
    private static final Set<String> neatlybetter$WARNED_MISSING_PATTERNS =
            Collections.synchronizedSet(new HashSet<>());

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void neatlybetter$drawCarpetResultPreview(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a,
                                                      CallbackInfo ci,
                                                      @Local(name = "xo") int xo,
                                                      @Local(name = "yo") int yo,
                                                      @Local(name = "resultSlot") Slot resultSlot) {
        if (this.hasMaxPatterns) {
            return;
        }

        ItemStack resultStack = resultSlot.getItem();
        if (resultStack.isEmpty() || !CarpetLoomSupport.isCarpetItem(resultStack)) {
            return;
        }

        Identifier baseTexture = neatlybetter$carpetBaseTexture(resultStack);
        if (baseTexture == null) {
            return;
        }

        int size = 18;
        int x0 = xo + 142;
        int y0 = yo + 18;

        graphics.blit(RenderPipelines.GUI_TEXTURED, baseTexture, x0, y0,
                0.0f, 0.0f, size, size, 1, 1, 1, 1, -1);

        CarpetPatternData data = resultStack.getOrDefault(ModData.CARPET_PATTERNS, CarpetPatternData.EMPTY);
        for (BannerPatternLayers.Layer layer : data.patterns().layers()) {
            Identifier patternTexture = neatlybetter$patternTexture(layer);
            if (patternTexture == null) {
                continue;
            }

            int tint = 0xFF000000 | layer.color().getTextureDiffuseColor();
            graphics.blit(RenderPipelines.GUI_TEXTURED, patternTexture, x0, y0,
                    0.0f, 0.0f, size, size, 1, 1, 1, 1, tint);
        }
    }

    @Unique
    private static @Nullable Identifier neatlybetter$carpetBaseTexture(ItemStack resultStack) {
        Item item = resultStack.getItem();
        if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof WoolCarpetBlock carpetBlock) {
            DyeColor color = carpetBlock.getColor();
            return Identifier.withDefaultNamespace("textures/block/" + color.getName() + "_wool.png");
        }
        return null;
    }

    @Unique
    private static @Nullable Identifier neatlybetter$patternTexture(BannerPatternLayers.Layer layer) {
        Optional<ResourceKey<BannerPattern>> key = layer.pattern().unwrapKey();
        if (key.isEmpty()) {
            return null;
        }

        String path = key.get().identifier().getPath();
        Identifier texture = Identifier.fromNamespaceAndPath("neatly-better", "textures/block/pattern/" + path + ".png");

        if (neatlybetter$WARNED_MISSING_PATTERNS.add(path)) {
            neatlybetter$LOGGER.debug("[NeatlyBetter/LoomCarpet] result preview using pattern texture '{}'", texture);
        }

        return texture;
    }

    @ModifyConstant(
            method = {
                    "containerChanged",
                    "extractBackground",
                    "extractBannerOnButton"
            },
            constant = @Constant(intValue = 6),
            require = 0
    )
    private int neatlybetter$raiseClientPatternCap(int original) {
        return 16;
    }

    @Redirect(
            method = "containerChanged",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 0)
    )
    private Object neatlybetter$skipCarpetFlagPreview(ItemStack resultStack, DataComponentType<?> type, Object def) {
        if (CarpetLoomSupport.isCarpetItem(resultStack)) {
            neatlybetter$LOGGER.debug("[NeatlyBetter/LoomCarpet] result is a carpet, skipping flag preview");
            return null;
        }
        return resultStack.getOrDefault(type, def);
    }

    @Redirect(
            method = "containerChanged",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 1)
    )
    private Object neatlybetter$carpetPatternsForMaxCheck(ItemStack bannerStack, DataComponentType<?> type, Object def) {
        if (CarpetLoomSupport.isCarpetItem(bannerStack)) {
            CarpetPatternData data = bannerStack.getOrDefault(ModData.CARPET_PATTERNS, CarpetPatternData.EMPTY);
            return data.patterns();
        }
        return bannerStack.getOrDefault(type, def);
    }
}