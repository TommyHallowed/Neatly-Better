package net.hallowed.oldways.client.mixin.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hallowed.oldways.network.OldWaysNetwork;
import net.hallowed.oldways.util.StonecutterMemory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.StonecutterScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class StonecutterScreenMixin extends Screen {

    @Shadow public int x;
    @Shadow public int y;

    @Unique
    private static final Identifier BUTTON_TEXTURE = Identifier.of("old-ways", "textures/gui/recraft_button.png");

    @Unique
    private Item oldways$lastItem = null;

    protected StonecutterScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void oldways$loadRecraftMemory(CallbackInfo ci) {
        if (!((Object) this instanceof StonecutterScreen)) {
            return;
        }

        if (this.client != null && this.client.player instanceof StonecutterMemory memory) {
            String lastId = memory.oldways$getLastCraftedItem();
            if (lastId != null && !lastId.isEmpty()) {
                Identifier id = Identifier.tryParse(lastId);
                if (id != null) {
                    this.oldways$lastItem = Registries.ITEM.get(id);
                }
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void oldways$onMouseClick(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof StonecutterScreen)) {
            return;
        }

        if (this.oldways$lastItem != null) {
            int buttonX = this.x + 143;
            int buttonY = this.y + 58;

            // Manually check if the mouse click is within our 16x16 button coordinate space
            if (click.x() >= buttonX && click.x() < buttonX + 16 && click.y() >= buttonY && click.y() < buttonY + 16) {

                boolean isShift = click.hasShift();
                Identifier itemId = Registries.ITEM.getId(this.oldways$lastItem);

                ClientPlayNetworking.send(new OldWaysNetwork.StonecutterRecraftPayload(itemId, isShift));
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void oldways$renderRecraftIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (!((Object) this instanceof StonecutterScreen)) {
            return;
        }

        if (this.oldways$lastItem != null) {
            int buttonX = this.x + 143;
            int buttonY = this.y + 58;
            boolean hovered = mouseX >= buttonX && mouseY >= buttonY && mouseX < buttonX + 16 && mouseY < buttonY + 16;

            // 1. Render the item FIRST so it acts as the base layer.
            context.drawItem(new ItemStack(this.oldways$lastItem), buttonX, buttonY);

            // 2. Because 1.21.2 matrices are strictly 2D, Z-index is handled by call order.
            // Drawing the texture AFTER the item natively guarantees it renders on top!
            float vOffset = hovered ? 16.0F : 0.0F;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, buttonX, buttonY, 0.0F, vOffset, 16, 16, 16, 32);
        }
    }
}