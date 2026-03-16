package net.hallowed.neatlybetter.client.feature.locator;

import net.hallowed.neatlybetter.client.util.SettingsPrefs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.WaypointStyleAssets;

import java.util.*;

public final class WaypointRendering {
    private WaypointRendering() {}

    private static final Identifier ARROW_UP   = Identifier.withDefaultNamespace("hud/locator_bar_arrow_up");
    private static final Identifier ARROW_DOWN = Identifier.withDefaultNamespace("hud/locator_bar_arrow_down");

    private static final ArrayList<Entry> VISIBLE = new ArrayList<>(64);
    private static final ArrayDeque<Entry> POOL   = new ArrayDeque<>(64);
    private static final Comparator<Entry> BY_DIST_DESC = (a, b) -> Double.compare(b.distSq, a.distSq);
    private static final int MAX_POOL_SIZE = 64;


    private static final class Entry {
        ClientWaypoint wp;
        double yaw;
        double distSq;
        int x;
        void set(ClientWaypoint w, double y, double d2, int px) { this.wp = w; this.yaw = y; this.distSq = d2; this.x = px;
        }
    }

    public static void renderWaypoints(Minecraft client, GuiGraphics ctx, int centerY) {
        if (client.player == null) return;

        var cam     = client.gameRenderer.getMainCamera();
        Vec3 camPos = cam.position();

        VISIBLE.clear();

        Entry best = null;
        double bestAbsYaw = 61.0;

        for (ClientWaypoint wp : WaypointTracking.WAYPOINTS) {
            double yaw = relativeYaw(wp.pos(), cam);
            if (yaw <= -61.0 || yaw > 60.0) continue;

            double d2 = wp.pos().distanceToSqr(camPos);
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

        if (best != null && SettingsPrefs.get().tabShowsNames && client.options.keyPlayerList.isDown()) {
            drawNamePopup(client, ctx, centerY, best.x, best.wp.text().orElse(null));
        }


        for (Entry entry : VISIBLE) {
            entry.wp = null;
            if (POOL.size() < MAX_POOL_SIZE) {
                POOL.offerFirst(entry);
            }
        }
        VISIBLE.clear();
    }

    private static void drawNamePopup(Minecraft client, GuiGraphics ctx, int centerY, int iconX, Component txt) {
        if (txt == null) return;
        Font tr = client.font;
        int w = tr.width(txt);
        int x = iconX - (w / 2);

        ctx.fill(x + 1, centerY - 12, x + w + 5, centerY - 1, ARGB.color(0.5F, CommonColors.BLACK));
        ctx.drawString(tr, txt, x + 3, centerY - 10, CommonColors.WHITE);
    }

    private static void drawWaypoint(Minecraft client, GuiGraphics ctx, int centerY, Entry e) {
        WaypointStyle asset = client.getWaypointStyles()
                .get(ResourceKey.create(WaypointStyleAssets.ROOT_ID, e.wp.style()));

        Identifier sprite = asset.sprite((float) Math.sqrt(e.distSq));

        int color = ARGB.color(255, e.wp.getColor());
        ctx.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, e.x, centerY - 2, 9, 9, color);

        TrackedWaypoint.PitchDirection pitch = pitch(e.wp.pos(), client.gameRenderer);
        if (pitch != TrackedWaypoint.PitchDirection.NONE) {
            int off = (pitch == TrackedWaypoint.PitchDirection.DOWN) ? 6 : -6;
            Identifier tex = (pitch == TrackedWaypoint.PitchDirection.DOWN) ? ARROW_DOWN : ARROW_UP;
            ctx.blitSprite(RenderPipelines.GUI_TEXTURED, tex, e.x + 1, centerY + off, 7, 5);
        }
    }

    private static int xFromYaw(GuiGraphics ctx, double yaw) {
        return Mth.ceil((ctx.guiWidth() - 9) / 2.0F) + (int)(yaw * 173.0 / 2.0 / 60.0);
    }
    private static double relativeYaw(Vec3 pos, TrackedWaypoint.Camera cam) {
        Vec3 v = cam.position().subtract(pos).rotateClockwise90();
        float f = (float)Mth.atan2(v.z(), v.x()) * (180.0F / (float)Math.PI);
        return Mth.degreesDifference(cam.yaw(), f);
    }
    private static TrackedWaypoint.PitchDirection pitch(Vec3 pos, TrackedWaypoint.Projector cam) {
        Vec3 v = cam.projectPointToScreen(pos);
        boolean bl = v.z > 1.0;
        double d = bl ? -v.y : v.y;
        if (d < -1.0) return TrackedWaypoint.PitchDirection.DOWN;
        if (d > 1.0)  return TrackedWaypoint.PitchDirection.UP;
        if (bl) { if (v.y > 0.0) return TrackedWaypoint.PitchDirection.UP; if (v.y < 0.0) return TrackedWaypoint.PitchDirection.DOWN; }
        return TrackedWaypoint.PitchDirection.NONE;
    }
}
