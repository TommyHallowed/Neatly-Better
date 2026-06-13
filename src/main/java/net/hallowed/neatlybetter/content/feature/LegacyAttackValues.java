package net.hallowed.neatlybetter.content.feature;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.handler.LegacyItemAttributeHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class LegacyAttackValues {

    private LegacyAttackValues() {}

    private static Boolean legacyEnabled = null;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            legacyEnabled = NTServerConfig.CONFIG.legacyCombat.get();
        });

        DefaultItemComponentEvents.MODIFY.register(context -> {
            if (!Boolean.TRUE.equals(legacyEnabled)) return;

            context.modify(item -> true, (builder, item) -> {
                ItemAttributeModifiers current = builder.getOrDefault(
                        DataComponents.ATTRIBUTE_MODIFIERS,
                        ItemAttributeModifiers.EMPTY);

                ItemAttributeModifiers patched =
                        LegacyItemAttributeHandler.applyLegacyAttackDamage(item, current);

                if (patched != null) {
                    builder.set(DataComponents.ATTRIBUTE_MODIFIERS, patched);
                }
            });
        });
    }
}