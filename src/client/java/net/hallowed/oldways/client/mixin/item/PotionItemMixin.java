package net.hallowed.oldways.client.mixin.item;

import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin extends Item {
    public PotionItemMixin(Settings settings) { super(settings); }

    @Unique
    private static final SettingsPrefs OW$prefs = SettingsPrefs.get();

    @Override
    public boolean hasGlint(ItemStack stack) {
        if (!OW$prefs.showPotionGlint) return false;

        PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        return super.hasGlint(stack) || (contents != null && contents.hasEffects());
    }
}
