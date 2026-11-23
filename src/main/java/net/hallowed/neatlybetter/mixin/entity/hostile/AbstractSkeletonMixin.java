package net.hallowed.neatlybetter.mixin.entity.hostile;

import net.hallowed.neatlybetter.content.feature.JockeyAdditions;
import net.hallowed.neatlybetter.content.feature.WitherSkeletonArcher;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.level.ServerLevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin {

    // After vanilla equipment is set, so the bow replaces the default sword.
    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void neatlybetter$spawnTweaks(ServerLevelAccessor level, DifficultyInstance difficulty,
                                          EntitySpawnReason reason, SpawnGroupData groupData,
                                          CallbackInfoReturnable<SpawnGroupData> cir) {
        AbstractSkeleton self = (AbstractSkeleton) (Object) this;
        if (self instanceof WitherSkeleton wsk) {
            WitherSkeletonArcher.onFinalizeSpawn(wsk, level, difficulty, reason);
        }
        JockeyAdditions.onFinalizeSpawn(self, level, difficulty, reason);
    }
}
