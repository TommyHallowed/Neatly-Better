// src/main/java/net/hallowed/oldways/mixin/SheepInteractMixin.java
package net.hallowed.oldways.mixin;

import net.hallowed.oldways.content.RainbowSet;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SheepEntity.class)
public class SheepInteractMixin {

    @Inject(
            method = "interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void oldways$jebShear(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        SheepEntity self = (SheepEntity) (Object) this;

        // holding shears?
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(Items.SHEARS)) return;

        // can be sheared?
        if (self.isBaby() || self.isSheared() || !self.isAlive()) return;

        // name check (avoid NPE)
        if (!self.hasCustomName()) return;
        String name = self.getCustomName() == null ? "" : self.getCustomName().getString();
        if (!"jeb_".equals(name) && !"_jeb".equals(name)) return;

        // server-only logic
        if (!(self.getWorld() instanceof ServerWorld world)) return;

        // shear sound
        world.playSoundFromEntity(null, self, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1.0F, 1.0F);

        // mark sheared and drop 1–3 rainbow wool
        self.setSheared(true);
        int count = 1 + world.getRandom().nextInt(3);
        for (int i = 0; i < count; i++) {
            self.dropStack(world, RainbowSet.RAINBOW_WOOL_ITEM.getDefaultStack());
        }

        // damage the shears
        EquipmentSlot slot = (hand == Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.damage(1, player, slot);

        // stop vanilla: SUCCESS on client, CONSUME on server (equivalent of sidedSuccess)
        cir.setReturnValue(world.isClient ? ActionResult.SUCCESS : ActionResult.CONSUME);
    }
}
