package net.hallowed.oldways.mixin.plugin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class OWMixinPlugin implements IMixinConfigPlugin {
    private static final boolean HAS_BETTERF3 = FabricLoader.getInstance().isModLoaded("betterf3");

    private static final String DEBUG_HUD_MIXIN =
            "net.hallowed.oldways.client.mixin.ui.DebugHudMixin";
    private static final String BETTERF3_MIXIN_PREFIX =
            "net.hallowed.oldways.client.mixin.betterf3.";

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (DEBUG_HUD_MIXIN.equals(mixinClassName)) {
            return !HAS_BETTERF3;
        }
        if (mixinClassName.startsWith(BETTERF3_MIXIN_PREFIX)) {
            return HAS_BETTERF3;
        }
        return true;
    }

    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String t, ClassNode n, String m, IMixinInfo i) {}
    @Override public void postApply(String t, ClassNode n, String m, IMixinInfo i) {}
}
