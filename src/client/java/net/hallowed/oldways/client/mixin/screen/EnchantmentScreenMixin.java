package net.hallowed.oldways.client.mixin.screen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.hallowed.oldways.init.ModGameRules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin {

    @Unique
    private static boolean oldways$isFullCostEnabled() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return false;

        if (mc.getServer() != null) {
            return mc.getServer().getGameRules().getBoolean(ModGameRules.FULL_ENCHANTING_COST);
        }

        if (mc.world != null) {
            try {
                Object rules = mc.world.getClass().getMethod("getGameRules").invoke(mc.world);
                if (rules != null) {
                    Object val = rules.getClass()
                            .getMethod("getBoolean", GameRules.Key.class)
                            .invoke(rules, ModGameRules.FULL_ENCHANTING_COST);
                    return (boolean) val;
                }
            } catch (Throwable ignored) {}
        }
        return false;
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;"
            )
    )
    private MutableText oldways$levelOne(
            String key,
            Operation<MutableText> original,
            @Local(name = "k") int k
    ) {
        if (!"container.enchant.level.one".equals(key) || !oldways$isFullCostEnabled()) {
            return original.call(key);
        }
        int cost = Math.max(1, k);
        return (cost == 1)
                ? Text.translatable("container.enchant.level.one")
                : Text.translatable("container.enchant.level.many", cost);
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"
            )
    )
    private MutableText oldways$levelMany(
            String key,
            Object[] args,
            Operation<MutableText> original,
            @Local(name = "k") int k
    ) {
        if (!"container.enchant.level.many".equals(key) || !oldways$isFullCostEnabled()) {
            return original.call(key, args);
        }
        int cost = Math.max(1, k);
        return (cost == 1)
                ? Text.translatable("container.enchant.level.one")
                : Text.translatable("container.enchant.level.many", cost);
    }
}
