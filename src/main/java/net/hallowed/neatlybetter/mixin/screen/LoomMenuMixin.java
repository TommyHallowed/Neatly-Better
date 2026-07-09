package net.hallowed.neatlybetter.mixin.screen;

import com.mojang.logging.LogUtils;

import net.hallowed.neatlybetter.data.CarpetPatternData;
import net.hallowed.neatlybetter.init.ModData;
import net.hallowed.neatlybetter.util.CarpetLoomSupport;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import org.slf4j.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LoomMenu.class)
public abstract class LoomMenuMixin {

    @Unique
    private static final Logger neatlybetter$LOGGER = LogUtils.getLogger();

    @ModifyConstant(
            method = "slotsChanged(Lnet/minecraft/world/Container;)V",
            constant = @Constant(intValue = 6)
    )
    private int neatlybetter$raisePatternCap(int original) {
        return 16;
    }

    @Redirect(
            method = "slotsChanged",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object neatlybetter$patternsForMaxCheck(ItemStack bannerStack, DataComponentType<?> type, Object def) {
        if (CarpetLoomSupport.isCarpetItem(bannerStack)) {
            CarpetPatternData data = bannerStack.getOrDefault(ModData.CARPET_PATTERNS, CarpetPatternData.EMPTY);
            return data.patterns();
        }
        return bannerStack.getOrDefault(type, def);
    }

    @Inject(method = "setupResultSlot", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$setupCarpetResultSlot(Holder<BannerPattern> pattern, CallbackInfo ci) {
        LoomMenu self = (LoomMenu) (Object) this;
        ItemStack bannerStack = self.getBannerSlot().getItem();
        ItemStack dyeStack = self.getDyeSlot().getItem();

        if (bannerStack.isEmpty() || dyeStack.isEmpty() || !CarpetLoomSupport.isCarpetItem(bannerStack)) {
            return;
        }

        ItemStack result = ItemStack.EMPTY;
        DyeColor patternColor = dyeStack.get(DataComponents.DYE);
        if (patternColor != null) {
            result = bannerStack.copyWithCount(1);
            CarpetPatternData current = result.getOrDefault(ModData.CARPET_PATTERNS, CarpetPatternData.EMPTY);
            BannerPatternLayers newLayers = new BannerPatternLayers.Builder()
                    .addAll(current.patterns())
                    .add(pattern, patternColor)
                    .build();
            result.set(ModData.CARPET_PATTERNS, new CarpetPatternData(newLayers, current.rotation()));
        } else {
            neatlybetter$LOGGER.debug("[NeatlyBetter/LoomCarpet] setupResultSlot: dye stack has no DYE component");
        }

        Slot resultSlot = self.getResultSlot();
        if (!ItemStack.matches(result, resultSlot.getItem())) {
            resultSlot.set(result);
        }
        ci.cancel();
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$quickMoveCarpetIntoBannerSlot(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        LoomMenu self = (LoomMenu) (Object) this;
        Slot slot = self.getSlot(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return;
        }

        ItemStack stack = slot.getItem();
        if (!CarpetLoomSupport.isCarpetItem(stack)) {
            return;
        }

        Slot bannerSlot = self.getBannerSlot();
        Slot dyeSlot = self.getDyeSlot();
        Slot patternSlot = self.getPatternSlot();
        Slot resultSlot = self.getResultSlot();

        if (slotIndex == resultSlot.index || slotIndex == bannerSlot.index
                || slotIndex == dyeSlot.index || slotIndex == patternSlot.index) {
            return;
        }

        ItemStack clicked = stack.copy();
        if (!self.moveItemStackTo(stack, bannerSlot.index, bannerSlot.index + 1, false)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == clicked.getCount()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        neatlybetter$LOGGER.debug("[NeatlyBetter/LoomCarpet] quickMoveStack routed carpet into banner slot: {}", stack);
        slot.onTake(player, stack);
        cir.setReturnValue(clicked);
    }
}