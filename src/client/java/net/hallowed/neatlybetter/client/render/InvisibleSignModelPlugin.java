package net.hallowed.neatlybetter.client.render;

import java.util.List;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

import net.hallowed.neatlybetter.content.feature.InvisibleSign;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.sprite.Material;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class InvisibleSignModelPlugin implements ModelLoadingPlugin {

    @Override
    public void initialize(Context ctx) {
        ctx.modifyBlockModelAfterBake().register(
                ModelModifier.WRAP_PHASE,
                InvisibleSignModelPlugin::modify
        );
    }

    private static BlockStateModel modify(BlockStateModel model, ModelModifier.AfterBakeBlock.Context ctx) {
        if (ctx.state().getBlock() instanceof WallSignBlock) {
            return new InvisibleWallSignModel(model);
        }
        return model;
    }

    private record InvisibleWallSignModel(BlockStateModel wrapped) implements BlockStateModel, FabricBlockStateModel {

        @Override
            public void emitQuads(@NonNull QuadEmitter emitter, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos,
                                  @NonNull BlockState state, @NonNull RandomSource random,
                                  @NonNull Predicate<@Nullable Direction> cullTest) {
                if (isInvisible(level, pos)) {
                    return;
                }
                this.wrapped.emitQuads(emitter, level, pos, state, random, cullTest);
            }

            @Override
            public @Nullable Object createGeometryKey(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos,
                                                      @NonNull BlockState state, @NonNull RandomSource random) {
                if (isInvisible(level, pos)) {
                    return InvisibleSignGeometryKey.INSTANCE;
                }
                return this.wrapped.createGeometryKey(level, pos, state, random);
            }

            @Override
            public Material.@NonNull Baked particleMaterial(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state) {
                return this.wrapped.particleMaterial(level, pos, state);
            }

            @Override
            public void collectParts(@NonNull RandomSource random, @NonNull List<BlockStateModelPart> parts) {
                this.wrapped.collectParts(random, parts);
            }

            @Override
            public Material.@NonNull Baked particleMaterial() {
                return this.wrapped.particleMaterial();
            }

            @Override
            public int materialFlags() {
                return this.wrapped.materialFlags();
            }

            private static boolean isInvisible(BlockAndTintGetter level, BlockPos pos) {
                BlockEntity be = level.getBlockEntity(pos);
                return be instanceof SignBlockEntity sign
                        && Boolean.TRUE.equals(sign.getAttached(InvisibleSign.INVISIBLE));
            }
        }

    private enum InvisibleSignGeometryKey {
        INSTANCE
    }
}