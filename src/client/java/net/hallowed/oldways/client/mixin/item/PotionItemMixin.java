package net.hallowed.oldways.client.mixin.item;

import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin extends Item {
    public PotionItemMixin(Properties settings) { super(settings); }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        if (!SettingsPrefs.get().showPotionGlint) return false;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        OminousBottleAmplifier ominousContents = stack.get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
        return super.isFoil(stack) || (contents != null && contents.hasEffects() || ominousContents != null);
    }
}
