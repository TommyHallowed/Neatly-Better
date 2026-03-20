package net.hallowed.neatlybetter.mixin.entity.passive;

import net.hallowed.neatlybetter.config.NTServerConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerTrades.EnchantBookForEmeralds.class)
public class EnchantBookForEmeraldsMixin {

    @Unique
    private boolean neatlybetter$capActive;

    @Inject(method = "getOffer", at = @At("HEAD"))
    private void neatlybetter$checkCap(ServerLevel level, Entity entity, RandomSource random, CallbackInfoReturnable<MerchantOffer> cir) {
        neatlybetter$capActive = NTServerConfig.CONFIG.villagerBookLevelCap.get();
    }

    @Redirect(method = "getOffer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I"))
    private int neatlybetter$capLevel(RandomSource random, int min, int max) {
        int level = Mth.nextInt(random, min, max);
        return neatlybetter$capActive ? 1 : level;
    }
}
