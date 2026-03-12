package net.hallowed.oldways.content.feature;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;

/**
 * Adds custom compostable items to the composter.
 * Replaces ComposterBlockMixin.
 C
 * Place in: content/feature/
 */
public final class ComposterRegistration {

    private ComposterRegistration() {}

    /**
     * Call from ModEvents.register() or TheOldWays.onInitialize().
     * ComposterBlock.COMPOSTABLES is a public static map, safe to modify after bootstrap.
     */
    public static void register() {
        ComposterBlock.COMPOSTABLES.put(Items.POISONOUS_POTATO, 0.5f);
        ComposterBlock.COMPOSTABLES.put(Items.ROTTEN_FLESH,     0.3f);
    }
}
