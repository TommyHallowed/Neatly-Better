package net.hallowed.neatlybetter.mixin.entity.passive;

import net.hallowed.neatlybetter.init.ModBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheep.class)
public abstract class SheepMixin {

    @Inject(method = "shear", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$shearJebSheep(ServerLevel level, SoundSource soundSource, ItemStack tool, CallbackInfo ci) {
        Sheep self = (Sheep) (Object) this;
        Component customName = self.getCustomName();

        if (customName != null && "jeb_".equals(customName.getString())) {
            level.playSound(null, self, SoundEvents.SHEEP_SHEAR, soundSource, 1.0F, 1.0F);

            int count = 1 + self.getRandom().nextInt(3);
            for (int i = 0; i < count; i++) {
                ItemEntity itemEntity = self.spawnAtLocation(level, new ItemStack(ModBlocks.RAINBOW_WOOL), 1.0F);
                if (itemEntity != null) {
                    itemEntity.setDeltaMovement(
                            itemEntity.getDeltaMovement().add(
                                    (self.getRandom().nextFloat() - self.getRandom().nextFloat()) * 0.1F,
                                    self.getRandom().nextFloat() * 0.05F,
                                    (self.getRandom().nextFloat() - self.getRandom().nextFloat()) * 0.1F
                            )
                    );
                }
            }

            self.setSheared(true);
            ci.cancel();
        }
    }
}
