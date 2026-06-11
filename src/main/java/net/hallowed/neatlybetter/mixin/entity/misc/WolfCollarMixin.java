package net.hallowed.neatlybetter.mixin.entity.misc;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.init.ModDataComponents;
import net.hallowed.neatlybetter.init.ModItems;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.TagValueInput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class WolfCollarMixin {

    @Inject(method = "thunderHit", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$collarRevival(ServerLevel level, LightningBolt lightningBolt, CallbackInfo ci) {
        if (!NTServerConfig.CONFIG.wolfImprovements.get()) return;

        Entity self = (Entity) (Object) this;
        if (!(self instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (!stack.is(ModItems.WOLF_COLLAR)) return;

        CustomData wolfData = stack.get(ModDataComponents.WOLF_DATA);
        if (wolfData == null || wolfData.isEmpty()) return;

        CompoundTag tag = wolfData.copyTag();

        tag.remove("UUID");
        tag.remove("Health");
        tag.remove("DeathTime");
        tag.remove("HurtTime");
        tag.remove("HurtByTimestamp");

        Wolf wolf = new Wolf(EntityType.WOLF, level);
        wolf.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
        wolf.setHealth(wolf.getMaxHealth());

        wolf.snapTo(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        wolf.setOrderedToSit(false);

        level.addFreshEntity(wolf);
        itemEntity.discard();
        ci.cancel();
    }
}
