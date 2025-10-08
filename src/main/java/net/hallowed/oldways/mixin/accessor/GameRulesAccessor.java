package net.hallowed.oldways.mixin.accessor;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(GameRules.class)
public interface GameRulesAccessor {
    @Invoker("register")
    static <T extends GameRules.Rule<T>> GameRules.Key<T> oldways$register(
            String name, GameRules.Category category, GameRules.Type<T> type) {
        throw new AssertionError("mixin");
    }

    // Access the private static RULE_TYPES map (Key -> Type)
    @Accessor("RULE_TYPES")
    static Map<GameRules.Key<?>, GameRules.Type<?>> oldways$getRuleTypes() {
        throw new AssertionError("mixin");
    }
}

