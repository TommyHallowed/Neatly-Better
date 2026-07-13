package net.hallowed.neatlybetter.mixin.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.hallowed.neatlybetter.config.NTServerConfig;
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

    @Unique private boolean neatlybetter$consumeRightOnTake = false;
    @Unique private int neatlybetter$cachedLevelCost = 0;
    @Unique private int neatlybetter$costAtTake = 0;
    @Unique private int neatlybetter$deltaSeen = 0;

    protected AnvilMenuMixin(MenuType<?> type, int syncId,
                             Inventory inv, ContainerLevelAccess ctx, ItemCombinerMenuSlotDefinition slots) {
        super(type, syncId, inv, ctx, slots);
    }

    // ── anvilRenameColors ──
    @Inject(method = "validateName", at = @At("HEAD"), cancellable = true)
    private static void neatlybetter$allowColorsAndFormat(String name, CallbackInfoReturnable<String> cir) {
        if (!NTServerConfig.CONFIG.anvilRenameColors.get()) return;

        String translated = name.replaceAll("&([0-9a-fA-Fk-oK-OrR])", "§$1");

        StringBuilder builder = new StringBuilder();
        for (char c : translated.toCharArray()) {
            if (net.minecraft.util.StringUtil.isAllowedChatCharacter(c) || c == '§') {
                builder.append(c);
            }
        }
        String result = builder.toString();
        cir.setReturnValue(result.length() <= 50 ? result : null);
    }

    // ── anvilNoItalicsRename ──
    @ModifyExpressionValue(
            method = {"setItemName", "createResult"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    )
    private MutableComponent neatlybetter$makeRenamesNonItalic(MutableComponent original) {
        if (!NTServerConfig.CONFIG.anvilNoItalicsRename.get()) return original;
        return original.withStyle(style -> style.withItalic(false));
    }

    // ── anvilNetheriteIngotFullRepair / anvilEnchantFeather ──
    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$featherBypass(CallbackInfo ci) {
        if (!NTServerConfig.CONFIG.anvilEnchantFeather.get()) return;

        ItemStack left = this.getSlot(0).getItem();
        if (!left.is(Items.FEATHER)) return;

        ItemStack right = this.getSlot(1).getItem();
        ItemStack out = left.copy();
        ItemEnchantments existing =
                out.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (right.is(Items.ENCHANTED_BOOK)) {
            int lvl = neatlybetter$getKnockbackLevelFromBook(right);
            if (lvl <= 0) return;
            int target = Math.min(2, lvl);
            this.access.execute((world, _) -> {
                Holder<@NotNull Enchantment> kb = world.registryAccess().get(Enchantments.KNOCKBACK).orElseThrow();
                ItemEnchantments.Mutable b = new ItemEnchantments.Mutable(existing);
                b.set(kb, target);
                out.set(DataComponents.ENCHANTMENTS, b.toImmutable());
            });
            this.resultSlots.setItem(0, out);
            int count = Math.max(1, left.getCount());
            this.cost.set(target * count);
            this.repairItemCountCost = 0;
            neatlybetter$consumeRightOnTake = true;
        if (right.is(Items.NETHERITE_INGOT) && neatlybetter$isNetheriteTool(left)
                && left.isDamageableItem() && left.getDamageValue() > 0) {
            ItemStack result = left.copy();
            result.setDamageValue(0);
            this.resultSlots.setItem(0, result);
            this.cost.set(1);
            this.repairItemCountCost = 1;
            ci.cancel();
            return;
        }

        if (right.is(Items.FEATHER)) {
            int l = neatlybetter$getKnockbackLevelFromItem(left);
            int r = neatlybetter$getKnockbackLevelFromItem(right);
            if (l == 0 && r == 0) return;
            int target = (l == r && l > 0) ? Math.min(2, l + 1) : Math.max(l, r);
            this.access.execute((world, _) -> {
                Holder<@NotNull Enchantment> kb = world.registryAccess().get(Enchantments.KNOCKBACK).orElseThrow();
                ItemEnchantments.Mutable b = new ItemEnchantments.Mutable(existing);
                b.set(kb, target);
                out.set(DataComponents.ENCHANTMENTS, b.toImmutable());
            });
            this.resultSlots.setItem(0, out);
            int count = Math.max(1, left.getCount());
            this.cost.set(target * count);
            this.repairItemCountCost = 1;
            neatlybetter$consumeRightOnTake = false;
            ci.cancel();
        }
    @Unique
    private static boolean neatlybetter$isNetheriteTool(ItemStack stack) {
        return stack.is(Items.NETHERITE_SWORD)
                || stack.is(Items.NETHERITE_SHOVEL)
                || stack.is(Items.NETHERITE_PICKAXE)
                || stack.is(Items.NETHERITE_AXE)
                || stack.is(Items.NETHERITE_HOE);
    }

    // ── anvilNoRenameCost (+ general bookkeeping) ──
    @Inject(method = "createResult", at = @At("TAIL"))
    private void neatlybetter$handleRenameCost(CallbackInfo ci) {
        neatlybetter$consumeRightOnTake = false;

        if (NTServerConfig.CONFIG.anvilNoRenameCost.get() && neatlybetter$isPureRename()) {
            this.cost.set(0);
            neatlybetter$cachedLevelCost = 0;
        } else {
            neatlybetter$cachedLevelCost = this.cost.get();
        }
    }

    @Inject(method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"))
    private void neatlybetter$preTake(Player player, ItemStack carried, CallbackInfo ci) {
        neatlybetter$costAtTake = this.cost.get();
        neatlybetter$deltaSeen = 0;
        if (!neatlybetter$consumeRightOnTake) return;
        ItemStack right = this.getSlot(1).getItem();
        if (right.is(Items.ENCHANTED_BOOK)) {
            right.shrink(1);
            this.getSlot(1).setByPlayer(right.isEmpty() ? ItemStack.EMPTY : right);
        }
        neatlybetter$consumeRightOnTake = false;
    }

    @ModifyArg(
            method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"),
            index = 0
    )
    private int neatlybetter$captureDelta(int delta) {
        neatlybetter$deltaSeen = delta;
        return delta;
    }

    @Inject(
            method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL")
    )
    private void neatlybetter$chargeIfSkipped(Player player, ItemStack carried, CallbackInfo ci) {
        if (!player.getAbilities().instabuild && neatlybetter$deltaSeen == 0 && neatlybetter$costAtTake > 0 && player.experienceLevel >= neatlybetter$costAtTake) {
            player.giveExperienceLevels(-neatlybetter$costAtTake);
        }
        neatlybetter$costAtTake = 0;
        neatlybetter$deltaSeen = 0;
    }

    // ── anvilNoRenameCost ──
    @Inject(method = "getCost", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$getLevelCostMirror(CallbackInfoReturnable<Integer> cir) {
        if (!NTServerConfig.CONFIG.anvilNoRenameCost.get()) return;
        if (neatlybetter$isPureRename()) {
            cir.setReturnValue(neatlybetter$cachedLevelCost);
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$allowTakeWhenZeroCost(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        if (!NTServerConfig.CONFIG.anvilNoRenameCost.get()) return;
        if (neatlybetter$isPureRename()) {
            cir.setReturnValue(hasItem);
        }
    }

    // ── anvilNoTooExpensive ──
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 2))
    private int neatlybetter$removeTooExpensiveGate(int original) {
        if (!NTServerConfig.CONFIG.anvilNoTooExpensive.get()) return original;
        return Integer.MAX_VALUE;
    }

    @Unique
    private boolean neatlybetter$outputIsRenamed() {
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
    private boolean neatlybetter$isPureRename() {
        ItemStack right = this.getSlot(1).getItem();
        return right.isEmpty() && neatlybetter$outputIsRenamed();
    }

    @Unique
    private static int neatlybetter$getKnockbackLevelFromBook(ItemStack stack) {
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
    private static int neatlybetter$getKnockbackLevelFromItem(ItemStack stack) {
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