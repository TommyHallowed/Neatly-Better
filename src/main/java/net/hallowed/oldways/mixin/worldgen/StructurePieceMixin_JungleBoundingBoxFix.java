package net.hallowed.oldways.mixin.worldgen;

import net.minecraft.structure.JungleTempleGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructurePiece.class)
public abstract class StructurePieceMixin_JungleBoundingBoxFix {
    @Unique
    private static final int PAD_DOWN_Y = 4;

    @Shadow protected BlockBox boundingBox;

    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    private void oldways$returnExpandedForJungle(CallbackInfoReturnable<BlockBox> cir) {
        if (!(((Object) this) instanceof JungleTempleGenerator)) return;
        if (boundingBox == null) return;

        BlockBox bb = boundingBox;
        BlockBox expanded = new BlockBox(
                bb.getMinX(),
                bb.getMinY() - PAD_DOWN_Y,
                bb.getMinZ(),
                bb.getMaxX(),
                bb.getMaxY(),
                bb.getMaxZ()
        );
        cir.setReturnValue(expanded);
    }
}
