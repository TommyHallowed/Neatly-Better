package net.hallowed.neatlybetter.client.render;

import java.util.List;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;

import net.hallowed.neatlybetter.content.feature.InvisibleSign;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WallSignBlock;

import org.jspecify.annotations.NonNull;

/**
 * Invisible wall signs get an empty model, chosen once per block state at bake time.
 * No per-mesh block entity lookups; toggling the state re-meshes the chunk like any block change.
 */
public class InvisibleSignModelPlugin implements ModelLoadingPlugin {

    @Override
    public void initialize(Context ctx) {
        ctx.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, context) ->
                context.state().getBlock() instanceof WallSignBlock && InvisibleSign.isInvisible(context.state())
                        ? new EmptyModel(model)
                        : model);
    }

    /** Draws nothing; keeps the wrapped model's particle texture for break particles. */
    private record EmptyModel(BlockStateModel wrapped) implements BlockStateModel {
        @Override
        public void collectParts(@NonNull RandomSource random, @NonNull List<BlockStateModelPart> parts) {}

        @Override
        public Material.@NonNull Baked particleMaterial() {
            return this.wrapped.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return this.wrapped.materialFlags();
        }
    }
}
