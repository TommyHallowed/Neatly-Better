package net.hallowed.neatlybetter.client.mixin.screen;

import net.hallowed.neatlybetter.client.util.PortalInventoryGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$preventPortalScreenDismissal(@Nullable Screen screen, CallbackInfo ci) {
        if (screen != null) return;

        if (PortalInventoryGuard.isInputActive) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!(mc.gui.screen() instanceof AbstractContainerScreen)) return;

        LocalPlayer player = mc.player;

        if (player.portalProcess != null) {
            ci.cancel();
            return;
        }

        if (neatlybetter$hasPortalBlockInBounds(player)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean neatlybetter$hasPortalBlockInBounds(LocalPlayer player) {
        AABB box = player.getBoundingBox();
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {

            if (BuiltInRegistries.BLOCK
                    .getKey(player.level().getBlockState(pos).getBlock())
                    .getPath()
                    .contains("portal")) {
                return true;
            }
        }
        return false;
    }
}