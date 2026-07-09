package net.hallowed.neatlybetter.mixin.entity.misc;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$onTick(ServerLevel level, boolean spawnEnemies, CallbackInfo ci) {
        if (NTServerConfig.CONFIG.phantomSpawnerMode.get() != NTServerConfig.PhantomSpawner.VANILLA) {
            ci.cancel();
        }
    }
}