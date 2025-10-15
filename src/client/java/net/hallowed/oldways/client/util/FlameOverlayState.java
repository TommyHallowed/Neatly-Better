package net.hallowed.oldways.client.util;

import net.hallowed.oldways.util.FireSourceHolder;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public final class FlameOverlayState {
    private FlameOverlayState() {}

    private static Identifier id(String path){ return Identifier.ofVanilla(path); }

    private static final Identifier FIRE_0      = id("block/fire_0");
    private static final Identifier FIRE_1      = id("block/fire_1");
    private static final Identifier SOUL_FIRE_0 = id("block/soul_fire_0");
    private static final Identifier SOUL_FIRE_1 = id("block/soul_fire_1");

    public static Sprite sprite0(Entity e) { return sprite(e, SOUL_FIRE_0, FIRE_0); }
    public static Sprite sprite1(Entity e) { return sprite(e, SOUL_FIRE_1, FIRE_1); }

    public static Sprite sprite(Entity e, Identifier soul, Identifier normal) {
        final boolean soulFire = ((FireSourceHolder) e).oldways$getLastFireSource() == Blocks.SOUL_FIRE;
        final Identifier which = soulFire ? soul : normal;

        // Get the block atlas from the TextureManager and fetch the sprite from it
        TextureManager tm = MinecraftClient.getInstance().getTextureManager();
        SpriteAtlasTexture atlas = (SpriteAtlasTexture) tm.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);

        Sprite s = atlas.getSprite(which);
        if (s == null) s = atlas.getSprite(MissingSprite.getMissingSpriteId());
        return s;
    }
}
