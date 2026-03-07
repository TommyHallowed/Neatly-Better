package net.hallowed.oldways.client.mixin.screen;

import net.hallowed.oldways.client.util.ModTextures;
import net.hallowed.oldways.util.StonecutterMemory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(StonecutterScreen.class)
public abstract class StonecutterScreenMixin extends AbstractContainerScreen<@NotNull StonecutterMenu> {

    @Unique
    private static final Map<Item, Item> OLDWAYS_INGREDIENT_CACHE = new HashMap<>();
    @Unique
    private static final Map<Item, Integer> OLDWAYS_INDEX_CACHE = new HashMap<>();

    public StonecutterScreenMixin() { super(null, null, null); }

    @Unique
    private Item oldways$getLastCraftedItem() {
        if (this.minecraft.player instanceof StonecutterMemory mem) {
            String id = mem.oldways$getLastCraftedItem();
            if (id != null && !id.isEmpty()) {
                Identifier identifier = Identifier.tryParse(id);
                if (identifier != null) {
                    return BuiltInRegistries.ITEM.getOptional(identifier).orElse(null);
                }
            }
        }
        return null;
    }

    @Unique
    private int oldways$findRecipeIndex(Item targetItem) {
        SelectableRecipe.SingleInputSet<@NotNull StonecutterRecipe> recipes = this.menu.getVisibleRecipes();
        if (this.minecraft.level == null) return -1;
        ContextMap contextMap = SlotDisplayContext.fromLevel(this.minecraft.level);

        for (int i = 0; i < recipes.size(); i++) {
            var entry = recipes.entries().get(i);

            SlotDisplay display = entry.recipe().optionDisplay();
            ItemStack output = display.resolveForFirstStack(contextMap);
            if (output.is(targetItem)) {
                return i;
            }
        }
        return -1;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void oldways$handleRecraftClick(MouseButtonEvent mouseButtonEvent, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        Item lastItem = oldways$getLastCraftedItem();

        if (lastItem != null) {
            int buttonX = this.leftPos + 143;
            int buttonY = this.topPos + 58;
            double mouseX = mouseButtonEvent.x();
            double mouseY = mouseButtonEvent.y();

            if (mouseX >= buttonX && mouseX < buttonX + 16 && mouseY >= buttonY && mouseY < buttonY + 16) {
                if (this.minecraft.gameMode == null || this.minecraft.player == null) return;

                boolean isShift = mouseButtonEvent.hasShiftDown();
                ItemStack inputStack = this.menu.getSlot(StonecutterMenu.INPUT_SLOT).getItem();

                // SCENARIO 1: The input slot ALREADY has a valid ingredient
                if (!inputStack.isEmpty()) {
                    int currentIndex = oldways$findRecipeIndex(lastItem);
                    if (currentIndex != -1) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, currentIndex);
                        ClickType clickType = isShift ? ClickType.QUICK_MOVE : ClickType.PICKUP;
                        this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, StonecutterMenu.RESULT_SLOT, 0, clickType, this.minecraft.player);
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 1.0F));
                        cir.setReturnValue(true);
                        return;
                    }
                } else {
                    // SCENARIO 2: Input slot is empty. We rely on our perfectly optimized cache!
                    Item cachedIngredient = OLDWAYS_INGREDIENT_CACHE.get(lastItem);
                    Integer cachedIndex = OLDWAYS_INDEX_CACHE.get(lastItem);

                    if (cachedIngredient != null && cachedIndex != null) {
                        int actualIngredientSlot = -1;
                        for (int i = 2; i < 38; i++) {
                            if (this.menu.getSlot(i).getItem().is(cachedIngredient)) {
                                actualIngredientSlot = i;
                                break;
                            }
                        }

                        if (actualIngredientSlot != -1) {
                            // SWAP PHASE
                            if (isShift) {
                                // Full stack -> Input
                                this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, actualIngredientSlot, 0, ClickType.QUICK_MOVE, this.minecraft.player);
                            } else {
                                // Pick up stack -> drop exactly 1 in input -> return stack
                                this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, actualIngredientSlot, 0, ClickType.PICKUP, this.minecraft.player);
                                this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, StonecutterMenu.INPUT_SLOT, 1, ClickType.PICKUP, this.minecraft.player);
                                this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, actualIngredientSlot, 0, ClickType.PICKUP, this.minecraft.player);
                            }

                            // Select recipe & auto-move to inventory (QUICK_MOVE crafts 1 if non-shift, or max if shift)
                            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, cachedIndex);
                            this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, StonecutterMenu.RESULT_SLOT, 0, ClickType.QUICK_MOVE, this.minecraft.player);

                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 1.0F));
                        }
                    }
                }
                cir.setReturnValue(true); // Always consume click
            }
        }
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void oldways$renderRecraftIcon(GuiGraphics context, float tickDelta, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.minecraft.level == null) return;

        ItemStack inputStack = this.menu.getSlot(StonecutterMenu.INPUT_SLOT).getItem();

        // 1. PROACTIVE CACHING:
        // Instantly memorize EVERY possible output the second an item touches the input slot!
        // This completely fixes the "missing button on first craft" bug!
        if (!inputStack.isEmpty()) {
            SelectableRecipe.SingleInputSet<@NotNull StonecutterRecipe> recipes = this.menu.getVisibleRecipes();
            ContextMap contextMap = SlotDisplayContext.fromLevel(this.minecraft.level);

            for (int i = 0; i < recipes.size(); i++) {
                var entry = recipes.entries().get(i);

                SlotDisplay display = entry.recipe().optionDisplay();
                ItemStack output = display.resolveForFirstStack(contextMap);

                if (!output.isEmpty()) {
                    OLDWAYS_INGREDIENT_CACHE.put(output.getItem(), inputStack.getItem());
                    OLDWAYS_INDEX_CACHE.put(output.getItem(), i);
                }
            }
        }

        Item lastItem = oldways$getLastCraftedItem();

        if (lastItem != null) {
            boolean canCraftNow = false;

            // 2. Check if we can craft (using the slot if full, or inventory if empty)
            if (!inputStack.isEmpty()) {
                if (oldways$findRecipeIndex(lastItem) != -1) {
                    canCraftNow = true;
                }
            } else {
                Item cachedIngredient = OLDWAYS_INGREDIENT_CACHE.get(lastItem);
                if (cachedIngredient != null) {
                    for (int i = 2; i < 38; i++) {
                        if (this.menu.getSlot(i).getItem().is(cachedIngredient)) {
                            canCraftNow = true;
                            break;
                        }
                    }
                }
            }

            // 3. Render the Overlay
            if (canCraftNow) {
                int buttonX = this.leftPos + 143;
                int buttonY = this.topPos + 58;
                boolean hovered = mouseX >= buttonX && mouseY >= buttonY && mouseX < buttonX + 16 && mouseY < buttonY + 16;

                // Render item FIRST as background
                context.renderItem(new ItemStack(lastItem), buttonX, buttonY);

                // Render custom overlay texture ON TOP!
                // Using the exact 32x32 texture layout: non-hovered (u:1, v:1), hovered (u:1, v:19)
                float u = 0.0F;
                float v = hovered ? 16.0F : 0.0F;
                context.blit(RenderPipelines.GUI_TEXTURED, ModTextures.RECRAFT_BUTTON, buttonX, buttonY, u, v, 16, 16, 32, 32);
            }
        }
    }
}