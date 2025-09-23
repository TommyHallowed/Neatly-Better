package net.hallowed.oldways.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class JebSheepDeathMixin {

    @Inject(
            method = "drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V",
            at = @At("HEAD")
    )
    private void oldways$preDrop(ServerWorld world, DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof SheepEntity sheep)) return;

        if (!sheep.hasCustomName()) return;
        String n = sheep.getCustomName() == null ? "" : sheep.getCustomName().getString();
        if (!"jeb_".equals(n)) return;

        // Prevent vanilla colored-wool alternatives from the loot table
        sheep.setSheared(true);
    }

    @Inject(
            method = "drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V",
            at = @At("TAIL")
    )
    private void oldways$postDrop(ServerWorld world, DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof SheepEntity sheep)) return;

        if (!sheep.hasCustomName()) return;
        String n = sheep.getCustomName() == null ? "" : sheep.getCustomName().getString();
        if (!"jeb_".equals(n)) return;

        int count = 1 + world.getRandom().nextInt(3);
        var rainbow = Registries.ITEM.get(Identifier.of("old-ways", "rainbow_wool"));

        if (count > 0) {
            sheep.dropStack(world, new ItemStack(rainbow, count));
        }
    }
}
