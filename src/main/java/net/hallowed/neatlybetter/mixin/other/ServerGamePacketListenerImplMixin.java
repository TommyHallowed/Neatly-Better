package net.hallowed.neatlybetter.mixin.other;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.hallowed.neatlybetter.data.CarpetPatternData;
import net.hallowed.neatlybetter.data.ChunkCarpetPatterns;
import net.hallowed.neatlybetter.registry.ModData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** Middle-click (pick block) on a patterned carpet picks a carpet with that pattern. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @ModifyExpressionValue(
            method = "handlePickItemFromBlock",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack neatlybetter$pickCarpetPattern(ItemStack picked, ServerboundPickItemFromBlockPacket packet) {
        if (picked.isEmpty()) return picked;

        ServerLevel level = ((ServerGamePacketListenerImpl) (Object) this).player.level();
        BlockPos pos = packet.pos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof WoolCarpetBlock)) return picked;

        ChunkCarpetPatterns patterns = level.getChunkAt(pos).getAttached(ModData.CARPET_PATTERN_DATA);
        CarpetPatternData data = patterns == null ? null : patterns.get(pos);
        if (data != null && !data.isEmpty()) {
            // Rotation is recomputed on placement; item stacks always carry 0 so they stack and match.
            picked.set(ModData.CARPET_PATTERNS, data.withRotation(0));
        }
        return picked;
    }

    /**
     * Survival: find the patterned carpet even if an older stack still carries a rotation; with none in the
     * inventory, fall back to a plain carpet of the same color (the vanilla pick result before patterns).
     */
    @WrapOperation(
            method = "tryPickItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingItem(Lnet/minecraft/world/item/ItemStack;)I")
    )
    private int neatlybetter$matchCarpetPattern(Inventory inventory, ItemStack wanted, Operation<Integer> original) {
        int slot = original.call(inventory, wanted);
        if (slot != -1) return slot;

        CarpetPatternData data = wanted.get(ModData.CARPET_PATTERNS);
        if (data == null || data.isEmpty()) return slot;

        int samePattern = neatlybetter$findSlot(inventory, wanted, data);
        if (samePattern != -1) return samePattern;

        if (((ServerGamePacketListenerImpl) (Object) this).player.hasInfiniteMaterials()) return -1; // creative: give the patterned one
        return original.call(inventory, new ItemStack(wanted.getItem()));
    }

    @Unique
    private static int neatlybetter$findSlot(Inventory inventory, ItemStack wanted, CarpetPatternData data) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(wanted.getItem())) continue;
            CarpetPatternData other = stack.get(ModData.CARPET_PATTERNS);
            if (other != null && other.patterns().equals(data.patterns())) return i;
        }
        return -1;
    }
}
