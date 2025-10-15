package net.hallowed.oldways.client.feature.locator;

import net.hallowed.oldways.client.util.SettingsPrefs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.resource.waypoint.WaypointStyleAsset;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.entity.player.SkinTextures;
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

    private static final Map<String, CacheEntry> NAME_CACHE = new ConcurrentHashMap<>();
    private static final long NAME_TTL_MS = 2000L;
    private record CacheEntry(UUID uuid, long expiresAt) {}

    private static final ArrayList<Entry> VISIBLE = new ArrayList<>(64);
    private static final ArrayDeque<Entry> POOL   = new ArrayDeque<>(64);
    private static final Comparator<Entry> BY_DIST_DESC = (a, b) -> Double.compare(b.distSq, a.distSq);

    private static final SettingsPrefs P = SettingsPrefs.get();

    private static final class Entry {
        ClientWaypoint wp;
        double yaw;
        double distSq;
        int x;
        Entry set(ClientWaypoint w, double y, double d2, int px) { this.wp = w; this.yaw = y; this.distSq = d2; this.x = px; return this; }
    }

    public static void renderWaypoints(MinecraftClient client, DrawContext ctx, int centerY) {
        if (client.player == null) return;

        var cam     = client.gameRenderer.getCamera();
        Vec3d camPos = cam.getCameraPos();

        VISIBLE.clear();

        Entry best = null;
        double bestAbsYaw = 61.0;

        for (ClientWaypoint wp : WaypointTracking.WAYPOINTS) {
            double yaw = relativeYaw(wp.pos(), cam);
            if (yaw <= -61.0 || yaw > 60.0) continue;

            double d2 = wp.pos().squaredDistanceTo(camPos);
            int x = xFromYaw(ctx, yaw);

            Entry e = POOL.pollFirst();
            if (e == null) e = new Entry();
            e.set(wp, yaw, d2, x);
            VISIBLE.add(e);

            double ay = Math.abs(yaw);
            if (ay < bestAbsYaw) { bestAbsYaw = ay; best = e; }
        }

        if (VISIBLE.size() > 1) VISIBLE.sort(BY_DIST_DESC);

        for (Entry e : VISIBLE) {
            drawWaypoint(client, ctx, centerY, e);
        }

        if (best != null && P.tabShowsNames && client.options.playerListKey.isPressed()) {
            drawNamePopup(client, ctx, centerY, best.x, best.wp.text().orElse(null));
        }

        // Return to pool
        for (Entry entry : VISIBLE) POOL.offerFirst(entry);
        VISIBLE.clear();
    }

    /* ---------------- draws ---------------- */

    private static void drawNamePopup(MinecraftClient client, DrawContext ctx, int centerY, int iconX, Text txt) {
        if (txt == null) return;
        TextRenderer tr = client.textRenderer;
        int w = tr.getWidth(txt);
        int x = iconX - (w / 2);

        ctx.fill(x + 1, centerY - 12, x + w + 5, centerY - 1, ColorHelper.withAlpha(0.5F, Colors.BLACK));
        ctx.drawTextWithShadow(tr, txt, x + 3, centerY - 10, Colors.WHITE);
    }

    private static void drawWaypoint(MinecraftClient client, DrawContext ctx, int centerY, Entry e) {
        WaypointStyleAsset asset = client.getWaypointStyleAssetManager()
                .get(RegistryKey.of(WaypointStyles.REGISTRY, e.wp.style()));
        if (asset == null) return;

        Identifier sprite = asset.getSpriteForDistance((float) Math.sqrt(e.distSq));
        if (sprite == null) return;

        int color = ColorHelper.withAlpha(255, e.wp.getColor());
        ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, sprite, e.x, centerY - 2, 9, 9, color);

        if (P.renderPlayerHeads) drawHeadIfNameMatches(client, ctx, e.x, centerY, e.wp);

        TrackedWaypoint.Pitch pitch = pitch(e.wp.pos(), client.gameRenderer);
        if (pitch != TrackedWaypoint.Pitch.NONE) {
            int off = (pitch == TrackedWaypoint.Pitch.DOWN) ? 6 : -6;
            Identifier tex = (pitch == TrackedWaypoint.Pitch.DOWN) ? ARROW_DOWN : ARROW_UP;
            ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, tex, e.x + 1, centerY + off, 7, 5);
        }
    }

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

        var pe = mc.world.getPlayerByUuid(entry.uuid());
        if (!(pe instanceof AbstractClientPlayerEntity player)) return;

        var net = mc.getNetworkHandler();
        if (net == null) return;
        PlayerListEntry ple = net.getPlayerListEntry(player.getUuid());
        if (ple == null) return;

        SkinTextures skins = ple.getSkinTextures();

        int size = (int) (9 * P.headSizeMultiplier);
        int left = x + (9 - size) / 2;
        int top  = (centerY - 2) + (9 - size) / 2;

        if (P.coloredHeadOutline) {
            int argb = 0xFF000000 | wp.getColor();
            ctx.fill(left - 1, top - 1, left + size + 1, top, argb);
            ctx.fill(left - 1, top + size, left + size + 1, top + size + 1, argb);
            ctx.fill(left - 1, top, left, top + size, argb);
            ctx.fill(left + size, top, left + size + 1, top + size, argb);
        }

        PlayerSkinDrawer.draw(ctx, skins, left, top, size);
    }

    /* ---------------- math helpers (vanilla-adapted) ---------------- */
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
