package net.hallowed.neatlybetter.content.feature;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;

public final class ComposterRegistration {

    private ComposterRegistration() {}

    public static void register() {
        ComposterBlock.COMPOSTABLES.put(Items.POISONOUS_POTATO, 0.5f);
        ComposterBlock.COMPOSTABLES.put(Items.ROTTEN_FLESH,     0.3f);
    }
}
