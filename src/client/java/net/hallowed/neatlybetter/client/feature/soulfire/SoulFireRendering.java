package net.hallowed.neatlybetter.client.feature.soulfire;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.sprite.SpriteId;

/** Client side of {@code SoulFire}: soul flame sprites and the render-state flag. */
public final class SoulFireRendering {
    private SoulFireRendering() {}

    /** The soul fire block's own textures, already in the block atlas. */
    public static final SpriteId SOUL_FIRE_0 = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("soul_fire_0");
    public static final SpriteId SOUL_FIRE_1 = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("soul_fire_1");

    /** Set on a player's render state while they burn with soul fire. */
    public static final RenderStateDataKey<Boolean> SOUL_FIRE = RenderStateDataKey.create(() -> "neatly-better:soul_fire");
}
