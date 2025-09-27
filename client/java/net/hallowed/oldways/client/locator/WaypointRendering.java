package net.hallowed.oldways.client.locator;

import net.hallowed.oldways.client.config.ClientConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.resource.waypoint.WaypointStyleAsset;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.waypoint.TrackedWaypoint;
import net.minecraft.world.waypoint.WaypointStyles;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class WaypointRendering {
    private WaypointRendering() {}

    private static final Identifier ARROW_UP   = Identifier.ofVanilla("hud/locator_bar_arrow_up");
    private static final Identifier ARROW_DOWN = Identifier.ofVanilla("hud/locator_bar_arrow_down");

    // ---- tiny 2s name -> uuid cache (client-only) ----
    private static final Map<String, CacheEntry> NAME_CACHE = new ConcurrentHashMap<>();
    private static final long NAME_TTL_MS = 2000L;
    private record CacheEntry(UUID uuid, long expiresAt) {}

    public static void renderWaypoints(MinecraftClient client, DrawContext ctx, int centerY) {
        if (!ClientConfigManager.locatorBarEnabled() || client.player == null || client.cameraEntity == null) return;

        // Only for distance sort & distance grade — arrows still use the live camera providers.
        final Vec3d camPos = client.cameraEntity.getPos();

        WaypointTracking.WAYPOINTS.stream()
                .sorted(Comparator.comparingDouble(w -> -w.pos().squaredDistanceTo(camPos)))
                .forEachOrdered(w -> drawWaypoint(client, ctx, centerY, w, camPos));

        if (ClientConfigManager.tabShowsNames() && client.options.playerListKey.isPressed()) {
            drawClosestName(client, ctx, centerY);
        }
    }

    private static void drawClosestName(MinecraftClient client, DrawContext ctx, int centerY) {
        Optional<Text> best = Optional.empty();
        double bestYaw = 61;
        for (ClientWaypoint wp : WaypointTracking.WAYPOINTS) {
            double yaw = relativeYaw(wp.pos(), client.gameRenderer.getCamera());
            if (Math.abs(yaw) < Math.abs(bestYaw)) { bestYaw = yaw; best = wp.text(); }
        }
        if (best.isEmpty()) return;

        Text txt = best.get();
        TextRenderer tr = client.textRenderer;
        int x = xFromYaw(ctx, bestYaw) - tr.getWidth(txt) / 2;
        int w = tr.getWidth(txt);

        ctx.fill(x + 1, centerY - 12, x + w + 5, centerY - 1, ColorHelper.withAlpha(0.5F, Colors.BLACK));
        ctx.drawTextWithShadow(tr, txt, x + 3, centerY - 10, Colors.WHITE);
    }

    private static void drawWaypoint(MinecraftClient client, DrawContext ctx, int centerY, ClientWaypoint wp, Vec3d camPos) {
        var cam = client.gameRenderer.getCamera();
        double yaw = relativeYaw(wp.pos(), cam);
        if (yaw <= -61.0 || yaw > 60.0) return;

        WaypointStyleAsset asset = client.getWaypointStyleAssetManager()
                .get(RegistryKey.of(WaypointStyles.REGISTRY, wp.style()));
        if (asset == null) return;

        double distSq = wp.pos().squaredDistanceTo(camPos);
        Identifier sprite = asset.getSpriteForDistance((float) Math.sqrt(distSq));
        if (sprite == null) return;

        int x = xFromYaw(ctx, yaw);
        int color = ColorHelper.withAlpha(255, wp.getColor());
        ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, sprite, x, centerY - 2, 9, 9, color);

        if (ClientConfigManager.renderPlayerHeads()) drawHeadIfNameMatches(client, ctx, x, centerY, wp);

        TrackedWaypoint.Pitch pitch = pitch(wp.pos(), client.gameRenderer);
        if (pitch != TrackedWaypoint.Pitch.NONE) {
            int off = (pitch == TrackedWaypoint.Pitch.DOWN) ? 6 : -6;
            Identifier tex = (pitch == TrackedWaypoint.Pitch.DOWN) ? ARROW_DOWN : ARROW_UP;
            ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, tex, x + 1, centerY + off, 7, 5);
        }
    }

    /** Best-effort head overlay using a tiny name->uuid cache to avoid scanning every frame. */
    private static void drawHeadIfNameMatches(MinecraftClient mc, DrawContext ctx, int x, int centerY, ClientWaypoint wp) {
        if (wp.text().isEmpty() || mc.world == null) return;
        String name = wp.text().get().getString().trim();
        if (name.isEmpty()) return;

        long now = System.currentTimeMillis();
        String key = name.toLowerCase(Locale.ROOT);
        CacheEntry entry = NAME_CACHE.get(key);
        if (entry == null || entry.expiresAt() < now) {
            UUID found = null;
            for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
                if (p.getName().getString().equalsIgnoreCase(name)) { found = p.getUuid(); break; }
            }
            entry = new CacheEntry(found, now + NAME_TTL_MS);
            NAME_CACHE.put(key, entry);
        }
        if (entry.uuid() == null) return;

        // getPlayerByUuid returns PlayerEntity on this mapping; guard-cast to client type
        net.minecraft.entity.player.PlayerEntity pe = mc.world.getPlayerByUuid(entry.uuid());
        if (!(pe instanceof net.minecraft.client.network.AbstractClientPlayerEntity player)) return;


        SkinTextures skins = player.getSkinTextures();
        Identifier skin = skins.texture();
        if (skin == null) return;

        int size = (int) (9 * ClientConfigManager.headSizeMultiplier());
        int left = x + (9 - size) / 2;
        int top  = (centerY - 2) + (9 - size) / 2;

        if (ClientConfigManager.coloredHeadOutline()) {
            int argb = 0xFF000000 | wp.getColor();
            ctx.fill(left - 1, top - 1, left + size + 1, top, argb);
            ctx.fill(left - 1, top + size, left + size + 1, top + size + 1, argb);
            ctx.fill(left - 1, top, left, top + size, argb);
            ctx.fill(left + size, top, left + size + 1, top + size, argb);
        }

        // face (8,8)-(16,16) then hat (40,8)-(48,16) on 64×64 skins
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, skin, left, top, 8f, 8f,  size, size, 8, 8, 64, 64);
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, skin, left, top, 40f, 8f, size, size, 8, 8, 64, 64);
    }

    /* -------- math helpers (vanilla-adapted) -------- */
    private static int xFromYaw(DrawContext ctx, double yaw) {
        return MathHelper.ceil((ctx.getScaledWindowWidth() - 9) / 2.0F) + (int)(yaw * 173.0 / 2.0 / 60.0);
    }
    private static double relativeYaw(Vec3d pos, TrackedWaypoint.YawProvider cam) {
        Vec3d v = cam.getCameraPos().subtract(pos).rotateYClockwise();
        float f = (float)MathHelper.atan2(v.getZ(), v.getX()) * (180.0F / (float)Math.PI);
        return MathHelper.subtractAngles(cam.getCameraYaw(), f);
    }
    private static TrackedWaypoint.Pitch pitch(Vec3d pos, TrackedWaypoint.PitchProvider cam) {
        Vec3d v = cam.project(pos);
        boolean bl = v.z > 1.0;
        double d = bl ? -v.y : v.y;
        if (d < -1.0) return TrackedWaypoint.Pitch.DOWN;
        if (d > 1.0)  return TrackedWaypoint.Pitch.UP;
        if (bl) { if (v.y > 0.0) return TrackedWaypoint.Pitch.UP; if (v.y < 0.0) return TrackedWaypoint.Pitch.DOWN; }
        return TrackedWaypoint.Pitch.NONE;
    }
}
