package net.hallowed.neatlybetter.client.tooltip;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public final class EffectTooltipData implements TooltipComponent {

    private final List<MobEffectInstance> effects;
    private final float durationMultiplier;
    private final List<Float> chances;

    public EffectTooltipData(List<MobEffectInstance> effects,
                             float durationMultiplier,
                             List<Float> chances) {
        this.effects = List.copyOf(effects);
        this.durationMultiplier = durationMultiplier;
        this.chances = List.copyOf(chances);
    }

    public List<MobEffectInstance> effects() {
        return effects;
    }

    public float durationMultiplier() {
        return durationMultiplier;
    }

    public float getChance(int index) {
        if (index < 0 || index >= chances.size()) return 1.0F;
        return chances.get(index);
    }
}
