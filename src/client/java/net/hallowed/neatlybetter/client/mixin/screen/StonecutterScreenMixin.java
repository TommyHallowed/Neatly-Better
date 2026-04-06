package net.hallowed.neatlybetter.client.mixin.screen;

import net.hallowed.neatlybetter.client.util.ModTextures;
import net.hallowed.neatlybetter.util.StonecutterMemory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.ContainerInput;
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
    private static final Map<Item, Item> NEATLYBETTER_INGREDIENT_CACHE = new HashMap<>();
    @Unique
    private static final Map<Item, Integer> NEATLYBETTER_INDEX_CACHE = new HashMap<>();

    public StonecutterScreenMixin() { super(null, null, null); }

    @Unique
    private Item neatlybetter$getLastCraftedItem() {
        if (this.minecraft.player instanceof StonecutterMemory mem) {
            String id = mem.neatlybetter$getLastCraftedItem();
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
    private int neatlybetter$findRecipeIndex(Item targetItem) {
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
    private void neatlybetter$handleRecraftClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        Item lastItem = neatlybetter$getLastCraftedItem();

        if (lastItem != null) {
            int itemX = this.leftPos + 143;
            int hitTop = this.topPos + 58;
            int iconSize = 12;
            float iconX = itemX - iconSize / 2.0F + 2;
            float iconY = hitTop + 16 - iconSize / 2.0F -2;

            int hitLeft   = (int) iconX;
            int hitRight  = itemX + 16;
            int hitBottom = (int) (iconY + iconSize);

            double mouseX = event.x();
            double mouseY = event.y();

            if (mouseX >= hitLeft && mouseY >= hitTop && mouseX < hitRight && mouseY < hitBottom) {
                if (this.minecraft.gameMode == null || this.minecraft.player == null) return;

                boolean isShift = event.hasShiftDown();
                ItemStack inputStack = this.menu.getSlot(StonecutterMenu.INPUT_SLOT).getItem();

                if (!inputStack.isEmpty()) {
                    int currentIndex = neatlybetter$findRecipeIndex(lastItem);
                    if (currentIndex != -1) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, currentIndex);
                        ContainerInput ContainerInput = isShift ? net.minecraft.world.inventory.ContainerInput.QUICK_MOVE : net.minecraft.world.inventory.ContainerInput.PICKUP;
                        this.minecraft.gameMode.handleContainerInput(this.menu.containerId, StonecutterMenu.RESULT_SLOT, 0, ContainerInput, this.minecraft.player);
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 1.0F));
                        cir.setReturnValue(true);
                        return;
                    }
                } else {
                    Item cachedIngredient = NEATLYBETTER_INGREDIENT_CACHE.get(lastItem);
                    Integer cachedIndex = NEATLYBETTER_INDEX_CACHE.get(lastItem);

                    if (cachedIngredient != null && cachedIndex != null) {
                        int actualIngredientSlot = -1;
                        for (int i = 2; i < 38; i++) {
                            if (this.menu.getSlot(i).getItem().is(cachedIngredient)) {
                                actualIngredientSlot = i;
                                break;
                            }
                        }

                        if (actualIngredientSlot != -1) {
                            if (isShift) {
                                this.minecraft.gameMode.handleContainerInput(this.menu.containerId, actualIngredientSlot, 0, ContainerInput.QUICK_MOVE, this.minecraft.player);
                            } else {
                                this.minecraft.gameMode.handleContainerInput(this.menu.containerId, actualIngredientSlot, 0, ContainerInput.PICKUP, this.minecraft.player);
                                this.minecraft.gameMode.handleContainerInput(this.menu.containerId, StonecutterMenu.INPUT_SLOT, 1, ContainerInput.PICKUP, this.minecraft.player);
                                this.minecraft.gameMode.handleContainerInput(this.menu.containerId, actualIngredientSlot, 0, ContainerInput.PICKUP, this.minecraft.player);
                            }

                            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, cachedIndex);
                            this.minecraft.gameMode.handleContainerInput(this.menu.containerId, StonecutterMenu.RESULT_SLOT, 0, ContainerInput.QUICK_MOVE, this.minecraft.player);

                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 1.0F));
                        }
                    }
                }
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void neatlybetter$renderRecraftIcon(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (this.minecraft.level == null) return;

        ItemStack inputStack = this.menu.getSlot(StonecutterMenu.INPUT_SLOT).getItem();

        if (!inputStack.isEmpty()) {
            SelectableRecipe.SingleInputSet<@NotNull StonecutterRecipe> recipes = this.menu.getVisibleRecipes();
            ContextMap contextMap = SlotDisplayContext.fromLevel(this.minecraft.level);

            for (int i = 0; i < recipes.size(); i++) {
                var entry = recipes.entries().get(i);

                SlotDisplay display = entry.recipe().optionDisplay();
                ItemStack output = display.resolveForFirstStack(contextMap);

                if (!output.isEmpty()) {
                    NEATLYBETTER_INGREDIENT_CACHE.put(output.getItem(), inputStack.getItem());
                    NEATLYBETTER_INDEX_CACHE.put(output.getItem(), i);
                }
            }
        }

        Item lastItem = neatlybetter$getLastCraftedItem();

        if (lastItem != null) {
            boolean canCraftNow = false;

            if (!inputStack.isEmpty()) {
                if (neatlybetter$findRecipeIndex(lastItem) != -1) {
                    canCraftNow = true;
                }
            } else {
                Item cachedIngredient = NEATLYBETTER_INGREDIENT_CACHE.get(lastItem);
                if (cachedIngredient != null) {
                    for (int i = 2; i < 38; i++) {
                        if (this.menu.getSlot(i).getItem().is(cachedIngredient)) {
                            canCraftNow = true;
                            break;
                        }
                    }
                }
            }

            if (canCraftNow) {
                int itemX = this.leftPos + 143;
                int hitTop = this.topPos + 58;

                float iconScale = 0.75F;
                int iconSize = 12;
                float iconX = itemX - iconSize / 2.0F + 2;
                float iconY = hitTop + 16 - iconSize / 2.0F - 2;

                int hitLeft   = (int) iconX;
                int hitRight  = itemX + 16;
                int hitBottom = (int) (iconY + iconSize);
                boolean hovered = mouseX >= hitLeft && mouseY >= hitTop
                        && mouseX < hitRight && mouseY < hitBottom;

                graphics.item(new ItemStack(lastItem), itemX, hitTop);

                float u = 0.0F;
                float v = hovered ? 16.0F : 0.0F;

                graphics.pose().pushMatrix();
                graphics.pose().translate(iconX, iconY);
                graphics.pose().scale(iconScale, iconScale);
                graphics.blit(RenderPipelines.GUI_TEXTURED, ModTextures.RECRAFT_BUTTON, 0, 0, u, v, 16, 16, 32, 32);
                graphics.pose().popMatrix();
            }
        }
    }
}