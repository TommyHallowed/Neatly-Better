package net.hallowed.oldways.client.mixin.item;

import net.hallowed.oldways.client.config.ClientConfigManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Make potions glint when they actually have effects (configurable).
 * Client-only: include this class in your *client* mixin JSON.
 */
@Mixin(PotionItem.class)
public abstract class PotionItemMixin extends Item {
    public PotionItemMixin(Settings settings) { super(settings); }

    @Override
    public boolean hasGlint(ItemStack stack) {
        if (!ClientConfigManager.potionGlintEnabled()) return false;

        // Keep vanilla glint if present (e.g., enchanted) OR glint when potion has effects.
        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        return super.hasGlint(stack) || (contents != null && contents.hasEffects());
    }
}
