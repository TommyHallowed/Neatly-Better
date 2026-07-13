package net.hallowed.neatlybetter.mixin.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.config.NTServerConfig.MendingScope;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;

    @Unique private int neatlybetter$cachedLevelCost = 0;
    @Unique private boolean neatlybetter$mendingOverhaulResult = false;

    protected AnvilMenuMixin(MenuType<?> type, int syncId,
                             Inventory inv, ContainerLevelAccess ctx, ItemCombinerMenuSlotDefinition slots) {
        super(type, syncId, inv, ctx, slots);
    }

    // ── anvilRenameColors ──
    @Inject(method = "validateName", at = @At("HEAD"), cancellable = true)
    private static void neatlybetter$allowColorsAndFormat(String name, CallbackInfoReturnable<String> cir) {
        if (NTServerConfig.CONFIG.anvilRenameColors.isFalse()) return;

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
        if (NTServerConfig.CONFIG.anvilNoItalicsRename.isFalse()) return original;
        return original.withStyle(style -> style.withItalic(false));
    }

    // ── anvilNetheriteIngotFullRepair / anvilEnchantFeather ──
    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$specialCaseBypass(CallbackInfo ci) {
        ItemStack left = this.getSlot(0).getItem();
        ItemStack right = this.getSlot(1).getItem();
        neatlybetter$mendingOverhaulResult = false;

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

        if (NTServerConfig.CONFIG.mendingInventory.get() == MendingScope.OVERHAUL
                && !left.isEmpty() && neatlybetter$isMendingBook(right)
                && EnchantmentHelper.canStoreEnchantments(left)) {
            neatlybetter$mendingOverhaulResult = true;
            neatlybetter$applyMendingOverhaul(left, ci);
            return;
        }

        if (NTServerConfig.CONFIG.anvilEnchantFeather.isTrue() && left.is(Items.FEATHER)) {
            neatlybetter$applyFeatherKnockback(left, right, ci);
        }
    }

    // ── mendingInventory (OVERHAUL) ──
    @Unique
    private void neatlybetter$applyMendingOverhaul(ItemStack left, CallbackInfo ci) {
        ItemStack result = left.copy();
        ItemEnchantments existing = result.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        boolean hadMending = false;
        for (Object2IntMap.Entry<Holder<@NotNull Enchantment>> e : existing.entrySet()) {
            if (e.getKey().is(Enchantments.MENDING)) {
                hadMending = true;
                break;
            }
        }

        if (hadMending) {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (Object2IntMap.Entry<Holder<@NotNull Enchantment>> e : existing.entrySet()) {
                if (!e.getKey().is(Enchantments.MENDING)) {
                    mutable.set(e.getKey(), e.getIntValue());
                }
            }
            result.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        }

        result.remove(DataComponents.REPAIR_COST);

        this.resultSlots.setItem(0, result);
        this.cost.set(0);
        this.repairItemCountCost = 1;
        ci.cancel();
    }

    @Unique
    private static boolean neatlybetter$isMendingBook(ItemStack stack) {
        if (!stack.is(Items.ENCHANTED_BOOK)) return false;
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<@NotNull Enchantment>> e : stored.entrySet()) {
            if (e.getKey().is(Enchantments.MENDING)) return true;
        }
        return false;
    }

    @Unique
    private void neatlybetter$applyFeatherKnockback(ItemStack left, ItemStack right, CallbackInfo ci) {
        Integer target = null;

        if (right.is(Items.ENCHANTED_BOOK)) {
            int lvl = neatlybetter$getKnockbackLevelFromBook(right);
            if (lvl > 0) target = Math.min(2, lvl);
        } else if (right.is(Items.FEATHER)) {
            int l = neatlybetter$getKnockbackLevelFromItem(left);
            int r = neatlybetter$getKnockbackLevelFromItem(right);
            if (l > 0 || r > 0) {
                target = l == r ? Math.min(2, l + 1) : Math.max(l, r);
            }
        }

        if (target == null) return;

        int finalTarget = target;
        ItemStack out = left.copy();
        ItemEnchantments existing = out.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        this.access.execute((world, _) -> {
            Holder<@NotNull Enchantment> kb = world.registryAccess().get(Enchantments.KNOCKBACK).orElseThrow();
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(existing);
            mutable.set(kb, finalTarget);
            out.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        });

        this.resultSlots.setItem(0, out);
        this.cost.set(finalTarget * Math.max(1, left.getCount()));
        this.repairItemCountCost = 1;
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
        if (NTServerConfig.CONFIG.anvilNoRenameCost.isTrue() && neatlybetter$isPureRename()) {
            this.cost.set(0);
            neatlybetter$cachedLevelCost = 0;
        } else {
            neatlybetter$cachedLevelCost = this.cost.get();
        }
    }

    // ── anvilNoRenameCost (charge players who were granted infinite materials without being creative) ──
    @Inject(method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"))
    private void neatlybetter$chargeGrantedInfiniteMaterials(Player player, ItemStack carried, CallbackInfo ci) {
        int currentCost = this.cost.get();
        if (!player.isCreative() && player.hasInfiniteMaterials()
                && currentCost > 0 && player.experienceLevel >= currentCost) {
            player.giveExperienceLevels(-currentCost);
        }
    }

    // ── anvilNoRenameCost ──
    @Inject(method = "getCost", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$getLevelCostMirror(CallbackInfoReturnable<Integer> cir) {
        if (NTServerConfig.CONFIG.anvilNoRenameCost.isFalse()) return;
        if (neatlybetter$isPureRename()) {
            cir.setReturnValue(neatlybetter$cachedLevelCost);
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$allowTakeWhenZeroCost(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        if (neatlybetter$mendingOverhaulResult) {
            cir.setReturnValue(hasItem);
            return;
        }
        if (NTServerConfig.CONFIG.anvilNoRenameCost.isFalse()) return;
        if (neatlybetter$isPureRename()) {
            cir.setReturnValue(hasItem);
        }
    }

    // ── anvilNoTooExpensive ──
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 2))
    private int neatlybetter$removeTooExpensiveGate(int original) {
        if (NTServerConfig.CONFIG.anvilNoTooExpensive.isFalse()) return original;
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
                return Math.clamp(lvl, 1, 2);
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
                return Math.clamp(lvl, 0, 2);
            }
        }
        return 0;
    }
}