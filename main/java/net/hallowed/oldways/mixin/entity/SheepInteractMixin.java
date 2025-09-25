package net.hallowed.oldways.mixin.entity;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SheepEntity.class)
public class SheepInteractMixin {

    @Unique
    private static final Identifier RAINBOW_WOOL_ID = Identifier.of("old-ways", "rainbow_wool");

    @Inject(
            method = "interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void oldways$jebShear(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        SheepEntity self = (SheepEntity)(Object)this;

        ItemStack shears = player.getStackInHand(hand);
        if (!shears.isOf(Items.SHEARS)) return;
        if (self.isBaby() || self.isSheared() || !self.isAlive()) return;

        if (!self.hasCustomName()) return;
        String name = self.getCustomName() == null ? "" : self.getCustomName().getString();
        if (!"jeb_".equals(name)) return;

        if (!(self.getWorld() instanceof ServerWorld world)) return;

        world.playSoundFromEntity(null, self, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1.0F, 1.0F);
        self.setSheared(true);

        // Look up the item by id at use-time (avoids initializing your ModItems class early)
        var rainbowWool = Registries.ITEM.get(RAINBOW_WOOL_ID);

        int count = 1 + world.getRandom().nextInt(3);
        for (int i = 0; i < count; i++) {
            self.dropStack(world, new ItemStack(rainbowWool));
        }

        EquipmentSlot slot = (hand == Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        shears.damage(1, player, slot);

        cir.setReturnValue(world.isClient ? ActionResult.SUCCESS : ActionResult.CONSUME);
    }
}
