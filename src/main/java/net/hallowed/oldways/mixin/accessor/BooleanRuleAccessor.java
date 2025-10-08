package net.hallowed.oldways.mixin.accessor;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.BooleanRule.class)
public interface BooleanRuleAccessor {
    @Invoker("create")
    static GameRules.Type<GameRules.BooleanRule> oldways$create(boolean defaultValue) {
        throw new AssertionError("mixin");
    }
}
