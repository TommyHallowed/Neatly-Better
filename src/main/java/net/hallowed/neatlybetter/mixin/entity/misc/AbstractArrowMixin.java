package net.hallowed.neatlybetter.mixin.entity.misc;

import net.hallowed.neatlybetter.content.component.QuiverContents;
import net.hallowed.neatlybetter.content.item.QuiverItem;
import net.hallowed.neatlybetter.init.ModData;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @Shadow protected abstract void setPickupItemStack(ItemStack itemStack);

    @Shadow
    public AbstractArrow.Pickup pickup;

    @Shadow
    private @Nullable ItemStack firedFromWeapon;

    @Inject(method = "tryPickup", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$arrowPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (this.pickup == AbstractArrow.Pickup.CREATIVE_ONLY && neatlybetter$hasInfinity(this.firedFromWeapon)) {
            cir.setReturnValue(true);
            return;
        }

        if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
            ItemStack pickupItem = ((AbstractArrow) (Object) this).getPickupItem();
            if (neatlybetter$tryInsertIntoQuiver(player, pickupItem)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private static boolean neatlybetter$tryInsertIntoQuiver(Player player, ItemStack arrowStack) {
        if (!QuiverContents.canItemBeInQuiver(arrowStack)) return false;

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack slot = inventory.getItem(i);
            if (!(slot.getItem() instanceof QuiverItem)) continue;

            QuiverContents contents = slot.get(ModData.QUIVER_CONTENTS);
            if (contents == null) continue;

            QuiverContents.Mutable mutable = new QuiverContents.Mutable(contents);
            int inserted = mutable.tryInsert(arrowStack);
            if (inserted > 0) {
                slot.set(ModData.QUIVER_CONTENTS, mutable.toImmutable());
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean neatlybetter$hasInfinity(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ItemEnchantments ench = stack.getOrDefault(
                DataComponents.ENCHANTMENTS,
                ItemEnchantments.EMPTY
        );
        for (Object2IntMap.Entry<Holder<@NotNull Enchantment>> e : ench.entrySet()) {
            if (e.getKey().is(Enchantments.INFINITY) && e.getIntValue() > 0) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "onHitBlock", at = @At("RETURN"))
    private void neatlybetter$spawnTippedCloud(BlockHitResult hitResult, CallbackInfo ci) {
        if (!((Object) this instanceof Arrow self)) return;
        if (self.level().isClientSide()) return;

        ItemStack origin = self.getPickupItemStackOrigin();
        PotionContents contents = origin.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (contents.equals(PotionContents.EMPTY)) return;

        float durationScale = origin.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F);

        AreaEffectCloud cloud = new AreaEffectCloud(self.level(), self.getX(), self.getY(), self.getZ());
        cloud.setOwner((LivingEntity) self.getOwner());
        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setDuration(600);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
        cloud.setPotionContents(contents);
        cloud.setPotionDurationScale(durationScale);
        self.level().addFreshEntity(cloud);

        this.setPickupItemStack(new ItemStack(Items.ARROW));
    }
}
