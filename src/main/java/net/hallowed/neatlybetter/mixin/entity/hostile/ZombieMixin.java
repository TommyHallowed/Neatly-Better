package net.hallowed.neatlybetter.mixin.entity.hostile;

import net.hallowed.neatlybetter.feature.mobs.MobSpawnTweaks;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieMixin {

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void neatlybetter$spawnTweaks(ServerLevelAccessor level, DifficultyInstance difficulty,
                                          EntitySpawnReason reason, SpawnGroupData groupData,
                                          CallbackInfoReturnable<SpawnGroupData> cir) {
        MobSpawnTweaks.onFinalizeSpawn((Zombie) (Object) this, level, difficulty, reason);
    }
}
