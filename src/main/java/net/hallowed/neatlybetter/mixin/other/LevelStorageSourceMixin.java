package net.hallowed.neatlybetter.mixin.other;

import com.mojang.serialization.Dynamic;
import net.hallowed.neatlybetter.data.PlaytimeData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelStorageSource.class)
public class LevelStorageSourceMixin {

    @Inject(
            method = "makeLevelSummary(Lcom/mojang/serialization/Dynamic;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelDirectory;ZI)Lnet/minecraft/world/level/storage/LevelSummary;",
            at = @At("RETURN")
    )
    private void neatlybetter$attachPlaytime(
            Dynamic<?> dataTag, LevelStorageSource.LevelDirectory levelDirectory,
            boolean locked, int dataVersion,
            CallbackInfoReturnable<LevelSummary> cir
    ) {
        LevelSummary summary = cir.getReturnValue();
        if (summary == null) return;

        long ticks = dataTag.get("Time").asLong(-1L);
        PlaytimeData.put(summary, ticks);
    }
}