package net.hallowed.oldways.mixin.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;

    @Unique private boolean oldways$consumeRightOnTake = false;
    @Unique private int oldways$cachedLevelCost = 0;
    @Unique private int oldways$costAtTake = 0;
    @Unique private int oldways$deltaSeen = 0;

    protected AnvilMenuMixin(MenuType<?> type, int syncId,
                             Inventory inv, ContainerLevelAccess ctx, ItemCombinerMenuSlotDefinition slots) {
        super(type, syncId, inv, ctx, slots);
    }

    // --- Color Codes ---
    @Inject(method = "validateName", at = @At("HEAD"), cancellable = true)
    private static void oldways$allowColorsAndFormat(String string, CallbackInfoReturnable<String> cir) {
        String translated = string.replaceAll("&([0-9a-fA-Fk-oK-OrR])", "§$1");

        StringBuilder builder = new StringBuilder();
        for (char c : translated.toCharArray()) {
            if (net.minecraft.util.StringUtil.isAllowedChatCharacter(c) || c == '§') {
                builder.append(c);
            }
        }
        String result = builder.toString();
        cir.setReturnValue(result.length() <= 50 ? result : null);
    }

    // --- Remove default italics from renames ---
    @ModifyExpressionValue(
            method = {"setItemName", "createResult"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    )
    private MutableComponent oldways$makeRenamesNonItalic(MutableComponent original) {
        return original.withStyle(style -> style.withItalic(false));
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void oldways$featherBypass(CallbackInfo ci) {
        ItemStack left = this.getSlot(0).getItem();
        if (!left.is(Items.FEATHER)) return;

        ItemStack right = this.getSlot(1).getItem();
        ItemStack out = left.copy();
        ItemEnchantments existing =
                out.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (right.is(Items.ENCHANTED_BOOK)) {
            int lvl = oldways$getKnockbackLevelFromBook(right);
            if (lvl <= 0) return;
            int target = Math.min(2, lvl);
            this.access.execute((world, pos) -> {
                Holder<@NotNull Enchantment> kb = world.registryAccess().get(Enchantments.KNOCKBACK).orElseThrow();
                ItemEnchantments.Mutable b = new ItemEnchantments.Mutable(existing);
                b.set(kb, target);
                out.set(DataComponents.ENCHANTMENTS, b.toImmutable());
            });
            this.resultSlots.setItem(0, out);
            int count = Math.max(1, left.getCount());
            this.cost.set(target * count);
            this.repairItemCountCost = 0;
            oldways$consumeRightOnTake = true;
            ci.cancel();
            return;
        }

        if (right.is(Items.FEATHER)) {
            int l = oldways$getKnockbackLevelFromItem(left);
            int r = oldways$getKnockbackLevelFromItem(right);
            if (l == 0 && r == 0) return;
            int target = (l == r && l > 0) ? Math.min(2, l + 1) : Math.max(l, r);
            this.access.execute((world, pos) -> {
                Holder<@NotNull Enchantment> kb = world.registryAccess().get(Enchantments.KNOCKBACK).orElseThrow();
                ItemEnchantments.Mutable b = new ItemEnchantments.Mutable(existing);
                b.set(kb, target);
                out.set(DataComponents.ENCHANTMENTS, b.toImmutable());
            });
            this.resultSlots.setItem(0, out);
            int count = Math.max(1, left.getCount());
            this.cost.set(target * count);
            this.repairItemCountCost = 1;
            oldways$consumeRightOnTake = false;
            ci.cancel();
        }
    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void oldways$handleRenameCost(CallbackInfo ci) {
        oldways$consumeRightOnTake = false;

        if (oldways$isPureRename()) {
            this.cost.set(0);
            oldways$cachedLevelCost = 0;
        } else {
            oldways$cachedLevelCost = this.cost.get();
        }
    }

    @Inject(method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"))
    private void oldways$preTake(Player player, ItemStack taken, CallbackInfo ci) {
        oldways$costAtTake = this.cost.get();
        oldways$deltaSeen = 0;
        if (!oldways$consumeRightOnTake) return;
        ItemStack right = this.getSlot(1).getItem();
        if (right.is(Items.ENCHANTED_BOOK)) {
            right.shrink(1);
            this.getSlot(1).setByPlayer(right.isEmpty() ? ItemStack.EMPTY : right);
        }
        oldways$consumeRightOnTake = false;
    }

    @ModifyArg(
            method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"),
            index = 0
    )
    private int oldways$captureDelta(int delta) {
        oldways$deltaSeen = delta;
        return delta;
    }

    @Inject(
            method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL")
    )
    private void oldways$chargeIfSkipped(Player player, ItemStack taken, CallbackInfo ci) {
        if (!player.getAbilities().instabuild && oldways$deltaSeen == 0 && oldways$costAtTake > 0 && player.experienceLevel >= oldways$costAtTake) {
            player.giveExperienceLevels(-oldways$costAtTake);
        }
        oldways$costAtTake = 0;
        oldways$deltaSeen = 0;
    }

    @Inject(method = "getCost", at = @At("HEAD"), cancellable = true)
    private void oldways$getLevelCostMirror(CallbackInfoReturnable<Integer> cir) {
        if (oldways$isPureRename()) {
            cir.setReturnValue(oldways$cachedLevelCost);
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void oldways$allowTakeWhenZeroCost(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (oldways$isPureRename()) {
            cir.setReturnValue(present);
        }
    }

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 2))
    private int oldways$removeTooExpensiveGate(int original) {
        return Integer.MAX_VALUE;
    }

    @Unique
    private boolean oldways$outputIsRenamed() {
        ItemStack input = this.getSlot(0).getItem();
        ItemStack output = this.getSlot(2).getItem();
        if (output.isEmpty()) return false;
        Component inName  = input.get(DataComponents.CUSTOM_NAME);
        Component outName = output.get(DataComponents.CUSTOM_NAME);
        if (inName == null && outName == null) return false;
        if (inName == null) return true;
        if (outName == null) return true;
        return !inName.equals(outName);
    }

    @Unique
    private boolean oldways$isPureRename() {
        ItemStack right = this.getSlot(1).getItem();
        return right.isEmpty() && oldways$outputIsRenamed();
    }

    @Unique
    private static int oldways$getKnockbackLevelFromBook(ItemStack stack) {
        if (!stack.is(Items.ENCHANTED_BOOK)) return 0;
        ItemEnchantments stored =
                stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<@NotNull Enchantment>> e : stored.entrySet()) {
            if (e.getKey().is(Enchantments.KNOCKBACK)) {
                int lvl = e.getIntValue();
                return Math.min(Math.max(lvl, 1), 2);
            }
        }
        return 0;
    }

    @Unique
    private static int oldways$getKnockbackLevelFromItem(ItemStack stack) {
        ItemEnchantments ench =
                stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<@NotNull Enchantment>> e : ench.entrySet()) {
            if (e.getKey().is(Enchantments.KNOCKBACK)) {
                int lvl = e.getIntValue();
                return Math.min(Math.max(lvl, 0), 2);
            }
        }
        return 0;
    }
}