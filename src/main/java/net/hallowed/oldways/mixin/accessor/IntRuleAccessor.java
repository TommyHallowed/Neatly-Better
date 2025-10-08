package net.hallowed.oldways.mixin.accessor;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.IntRule.class)
public interface IntRuleAccessor {
    @Invoker("create")
    static GameRules.Type<GameRules.IntRule> oldways$create(int defaultValue) {
        throw new AssertionError("mixin");
    }
}
