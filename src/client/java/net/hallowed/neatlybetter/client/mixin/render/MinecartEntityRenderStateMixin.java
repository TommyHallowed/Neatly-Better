package net.hallowed.neatlybetter.client.mixin.render;

import net.hallowed.neatlybetter.client.util.ChainableRenderState;

import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecartRenderState.class)
public class MinecartEntityRenderStateMixin implements ChainableRenderState {
    @Unique private Vec3 neatlybetter$linkedPos;
    @Unique private double neatlybetter$startOffset;
    @Unique private double neatlybetter$endOffset;
    @Unique private Vec3 neatlybetter$visualOffset;

    @Override public Vec3 neatlybetter$getLinkedPos() { return this.neatlybetter$linkedPos; }
    @Override public void neatlybetter$setLinkedPos(Vec3 pos) { this.neatlybetter$linkedPos = pos; }

    @Override public double neatlybetter$getStartOffset() { return this.neatlybetter$startOffset; }
    @Override public void neatlybetter$setStartOffset(double offset) { this.neatlybetter$startOffset = offset; }

    @Override public double neatlybetter$getEndOffset() { return this.neatlybetter$endOffset; }
    @Override public void neatlybetter$setEndOffset(double offset) { this.neatlybetter$endOffset = offset; }

    @Override public Vec3 neatlybetter$getVisualOffset() { return this.neatlybetter$visualOffset; }
    @Override public void neatlybetter$setVisualOffset(Vec3 offset) { this.neatlybetter$visualOffset = offset; }
}