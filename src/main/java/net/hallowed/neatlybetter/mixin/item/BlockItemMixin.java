package net.hallowed.neatlybetter.mixin.item;

import net.hallowed.neatlybetter.data.CarpetPatternData;
import net.hallowed.neatlybetter.data.ChunkCarpetPatterns;
import net.hallowed.neatlybetter.init.ModData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(
            method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"
            )
    )
    private void neatlybetter$transferCarpetPattern(
            BlockPlaceContext placeContext,
            CallbackInfoReturnable<InteractionResult> cir,
            @Local(name = "pos") BlockPos pos,
            @Local(name = "level") Level level,
            @Local(name = "itemStack") ItemStack itemStack,
            @Local(name = "placementState") BlockState placementState,
            @Local(name = "placedState") BlockState placedState
    ) {
        if (level.isClientSide()) {
            return;
        }

        if (!placedState.is(placementState.getBlock()) || !(placedState.getBlock() instanceof WoolCarpetBlock)) {
            return;
        }

        CarpetPatternData data = itemStack.getOrDefault(ModData.CARPET_PATTERNS, CarpetPatternData.EMPTY);
        if (data.isEmpty()) {
            return;
        }

        int rotation = neatlybetter$rotationFromFacing(placeContext.getHorizontalDirection());
        CarpetPatternData rotatedData = data.withRotation(rotation);

        LevelChunk chunk = level.getChunkAt(pos);
        ChunkCarpetPatterns existing = chunk.getAttachedOrCreate(ModData.CARPET_PATTERN_DATA);
        ChunkCarpetPatterns patterns = existing.withSet(pos, rotatedData);
        chunk.setAttached(ModData.CARPET_PATTERN_DATA, patterns);

        level.sendBlockUpdated(pos, placedState, placedState, Block.UPDATE_CLIENTS);
    }

    @Unique
    private static int neatlybetter$rotationFromFacing(Direction facing) {
        return switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }
}