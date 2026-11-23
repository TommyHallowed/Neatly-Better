package net.hallowed.neatlybetter.mixin.block;

import net.minecraft.world.level.block.state.BlockBehaviour;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.BlockStateBase.class)
public interface BlockStateBaseAccessor {
    /** Vanilla caches light emission per state in the constructor; this overwrites that cached value. */
    @Mutable
    @Accessor("lightEmission")
    void neatlybetter$setLightEmission(int lightEmission);
}
