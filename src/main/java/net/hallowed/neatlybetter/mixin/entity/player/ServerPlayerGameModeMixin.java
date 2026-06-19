package net.hallowed.neatlybetter.mixin.entity.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.hallowed.neatlybetter.config.NTCommonConfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SignApplicator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @WrapOperation(
            method = "useItemOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z")
    )
    private boolean neatlybetter$allowSignApplicatorWhenSneaking(
            ServerPlayer player,
            Operation<Boolean> op,
            @Local(argsOnly = true, name = "itemStack") ItemStack itemStack
    ) {
        if (!NTCommonConfig.CONFIG.clickThrough.get()) return false;
        if (!op.call(player)) return false;
        return !(itemStack.getItem() instanceof SignApplicator || itemStack.is(Items.PHANTOM_MEMBRANE));
    }
}