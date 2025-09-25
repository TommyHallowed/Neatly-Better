package net.hallowed.oldways.mixin.entity;

import net.minecraft.entity.ai.goal.TemptGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;


@Mixin(TemptGoal.class)
public abstract class NoAnimalTemptDelayMixin {


    @Unique
    private static volatile Field COOLDOWN_FIELD;
    @Unique
    private static final String[] CANDIDATE_FIELD_NAMES = new String[] {
            "calmDown", "cooldown", "coolDown", "calm_down", "temptCooldown"
    };

    @Inject(method = "stop()V", at = @At("TAIL"))
    private void oldways$noTemptCooldown(CallbackInfo ci) {
        Object self = this;
        try {
            Field f = COOLDOWN_FIELD;
            if (f == null) {
                Class<?> cls = self.getClass();
                for (String name : CANDIDATE_FIELD_NAMES) {
                    try {
                        f = cls.getDeclaredField(name);
                        f.setAccessible(true);
                        break;
                    } catch (NoSuchFieldException ignored) {}
                }

                if (f == null) {
                    for (Field cand : cls.getDeclaredFields()) {
                        if (cand.getType() == int.class) {
                            cand.setAccessible(true);
                            f = cand;
                            break;
                        }
                    }
                }
                COOLDOWN_FIELD = f;
            }
            if (f != null) {
                f.setInt(self, 0);
            }
        } catch (Throwable ignored) {
        }
    }
}
