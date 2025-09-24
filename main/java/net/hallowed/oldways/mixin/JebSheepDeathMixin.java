package net.hallowed.oldways.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class JebSheepDeathMixin {

    @Inject(
            method = "drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V",
            at = @At("TAIL")
    )
    private void oldways$replaceWoolWithRainbow(ServerWorld world, DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof SheepEntity sheep)) return;

        if (!sheep.hasCustomName()) return;
        String name = sheep.getCustomName() == null ? "" : sheep.getCustomName().getString();
        if (!"jeb_".equals(name)) return;

        // Collect freshly-dropped vanilla wool items around the sheep
        Box area = sheep.getBoundingBox().expand(2.0); // small radius around death spot
        List<ItemEntity> nearby = world.getEntitiesByClass(ItemEntity.class, area, ie ->
                !ie.isRemoved()
                        && ie.getOwner() == null // normal mob drops have no owner
                        && ie.getStack().isIn(ItemTags.WOOL)
                        && ie.age <= 5 // just spawned this tick or very recently
        );

        int totalWool = 0;
        for (ItemEntity ie : nearby) {
            totalWool += ie.getStack().getCount();
            ie.discard(); // delete vanilla color wool
        }
        if (totalWool <= 0) return;

        var rainbow = Registries.ITEM.get(Identifier.of("old-ways", "rainbow_wool"));
        if (rainbow == null) return;

        // Spawn the replacement rainbow wool (preserve approximate count)
        ItemStack out = new ItemStack(rainbow, totalWool);
        sheep.dropStack(world, out);
    }
}
