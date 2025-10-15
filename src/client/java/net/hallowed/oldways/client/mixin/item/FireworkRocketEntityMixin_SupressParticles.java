package net.hallowed.oldways.client.mixin.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hallowed.oldways.init.ModItems;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin_SupressParticles {

    @Shadow public abstract ItemStack getStack();

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;addParticleClient(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"
            )
    )
    private void oldways$maybeSuppressVanillaTrail(World world,
                                                   ParticleEffect effect,
                                                   double x, double y, double z,
                                                   double vx, double vy, double vz) {
        if (this.getStack().isOf(ModItems.DRAGON_BURST_ROCKET)) {
            return;
        }
        world.addParticleClient(effect, x, y, z, vx, vy, vz);
    }
}
