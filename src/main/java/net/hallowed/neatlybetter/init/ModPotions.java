package net.hallowed.neatlybetter.init;

import net.hallowed.neatlybetter.api.NTRegistry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;

import org.jetbrains.annotations.NotNull;

public final class ModPotions {
    private ModPotions() {}

    public static Holder<@NotNull Potion> HASTE;
    public static Holder<@NotNull Potion> LONG_HASTE;
    public static Holder<@NotNull Potion> STRONG_HASTE;
    public static Holder<@NotNull Potion> STRONG_LONG_HASTE;
    public static Holder<@NotNull Potion> SHORT_POISON;

    private static Holder<@NotNull Potion> register(String name, Potion potion) {
        return Registry.registerForHolder(BuiltInRegistries.POTION, NTRegistry.id(name), potion);
    }

    public static void registerAll() {
        // 3:00 Haste I
        HASTE = register("haste",
                new Potion("haste", new MobEffectInstance(MobEffects.HASTE, 20 * 180, 0)));

        // 8:00 Haste I
        LONG_HASTE = register("long_haste",
                new Potion("haste", new MobEffectInstance(MobEffects.HASTE, 20 * 480, 0)));

        // 3:00 Haste II
        STRONG_HASTE = register("strong_haste",
                new Potion("haste", new MobEffectInstance(MobEffects.HASTE, 20 * 180, 1)));

        // 8:00 Haste II
        STRONG_LONG_HASTE = register("strong_long_haste",
                new Potion("haste", new MobEffectInstance(MobEffects.HASTE, 20 * 480, 1)));

        SHORT_POISON = register("poison",
                new Potion("poison", new MobEffectInstance(MobEffects.POISON, 20 * 25, 0)));
    }
}
