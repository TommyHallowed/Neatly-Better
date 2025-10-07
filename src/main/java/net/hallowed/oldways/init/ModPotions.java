package net.hallowed.oldways.init;

import net.hallowed.oldways.api.OWRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

/** Registers custom potions and keeps handles as RegistryEntry<Potion>. */
public final class ModPotions {
    private ModPotions() {}

    public static RegistryEntry<Potion> HASTE;
    public static RegistryEntry<Potion> LONG_HASTE;
    public static RegistryEntry<Potion> STRONG_HASTE;

    private static RegistryEntry<Potion> register(String name, Potion potion) {
        // Mirror vanilla: create a reference handle for later use (brewing, etc.)
        return Registry.registerReference(Registries.POTION, OWRegistry.id(name), potion);
    }

    public static void registerAll() {
        // 3:00 Haste I
        HASTE = register("haste",
                new Potion("haste", new StatusEffectInstance(StatusEffects.HASTE, 20 * 180, 0)));

        // 8:00 Haste I
        LONG_HASTE = register("long_haste",
                new Potion("haste", new StatusEffectInstance(StatusEffects.HASTE, 20 * 480, 0)));

        // 1:30 Haste II
        STRONG_HASTE = register("strong_haste",
                new Potion("haste", new StatusEffectInstance(StatusEffects.HASTE, 20 * 90, 1)));
    }
}
