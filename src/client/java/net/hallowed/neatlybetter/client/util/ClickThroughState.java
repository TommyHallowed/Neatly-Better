package net.hallowed.neatlybetter.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ClickThroughState {

    public static boolean isDyeOnSign = false;

    private ClickThroughState() {}
}