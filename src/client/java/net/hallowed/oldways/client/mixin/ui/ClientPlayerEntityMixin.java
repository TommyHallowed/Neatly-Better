package net.hallowed.oldways.client.mixin.ui;

import net.hallowed.oldways.client.accessor.ClientPlayerEntityAccessor;
import net.hallowed.oldways.client.mixin.accessor.RecipeBookAccessor;
import net.hallowed.oldways.client.mixin.accessor.RecipeBookScreenAccessor;
import net.hallowed.oldways.client.util.RecipeBookUtil;
import net.hallowed.oldways.client.util.SettingsPrefs;
import net.hallowed.oldways.client.util.SettingsPrefs.RecipeBookMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.recipe.book.RecipeBookType;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin implements ClientPlayerEntityAccessor {
    @Shadow @Final private ClientRecipeBook recipeBook;
    @Shadow @Final public ClientPlayNetworkHandler networkHandler;
    @Unique
    private static final SettingsPrefs OW$prefs = SettingsPrefs.get();

    // remember last-seen fire type for the overlay (true = soul fire)
    @Unique
    private boolean oldways$soulFire = false;

    public boolean oldways$isSoulFire() {
        return this.oldways$soulFire;
    }

    public void oldways$setSoulFire(boolean val) {
        this.oldways$soulFire = val;
    }

    @Inject(method = "closeScreen", at = @At("HEAD"))
    private void oldways$closeRecipeBookIfOpen(CallbackInfo ci) {
        RecipeBookMode mode = OW$prefs.recipeBookMode;

        if (mode == RecipeBookMode.SHOWN) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.currentScreen == null) return;

        if (client.currentScreen instanceof RecipeBookScreen<?> recipeScreen) {
            RecipeBookWidget<?> widget = ((RecipeBookScreenAccessor) recipeScreen).getRecipeBook();
            RecipeBookType category = ((RecipeBookAccessor) widget).getCraftingHandler().getCategory();
            RecipeBookUtil.closeRecipeBook(this.recipeBook, this.networkHandler, category);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void oldways$clientTick(CallbackInfo ci) {
        try {
            ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
            if (player == null || player.getEntityWorld() == null) return;

            var bbox = player.getBoundingBox();
            int x0 = (int)Math.floor(bbox.minX - 0.001D);
            int x1 = (int)Math.floor(bbox.maxX + 0.001D);
            int y0 = (int)Math.floor(bbox.minY - 0.001D);
            int y1 = (int)Math.floor(bbox.maxY + 0.001D);
            int z0 = (int)Math.floor(bbox.minZ - 0.001D);
            int z1 = (int)Math.floor(bbox.maxZ + 0.001D);

            boolean atSoul = false;
            boolean atFire = false;
            for (int xi = x0; xi <= x1 && !(atSoul && atFire); xi++) {
                for (int yi = y0; yi <= y1 && !(atSoul && atFire); yi++) {
                    for (int zi = z0; zi <= z1 && !(atSoul && atFire); zi++) {
                        BlockPos checkPos = new BlockPos(xi, yi, zi);
                        try {
                            if (player.getEntityWorld().getBlockState(checkPos).isOf(Blocks.SOUL_FIRE)) {
                                atSoul = true;
                            } else if (player.getEntityWorld().getBlockState(checkPos).isOf(Blocks.FIRE)) {
                                atFire = true;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }

            if (atSoul) this.oldways$setSoulFire(true);
            else if (atFire) this.oldways$setSoulFire(false);
            // if neither, do not clear — remember last state
        } catch (Throwable ignored) {
        }
    }
}
