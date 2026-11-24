package net.hallowed.neatlybetter.mixin.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.config.NTServerConfig.MendingScope;
import net.hallowed.neatlybetter.feature.items.AddedItemProperties;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
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
import org.jetbrains.annotations.Nullable;

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

/**
 * Anvil tweaks. Custom recipes (netherite full repair, mending overhaul, feather knockback) are resolved
 * in {@link #neatlybetter$customRecipes} and follow the same rules as vanilla results: the typed name is
 * applied, the prior-work penalty is charged and increased, and the "too expensive" limit is respected.
 * Everything here runs identically on client and server so the preview matches what you get.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Unique private static final int neatlybetter$TOO_EXPENSIVE = 40;
    @Unique private static final int neatlybetter$MAX_FEATHER_KNOCKBACK = 2;

    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;
    @Shadow private @Nullable String itemName;
    @Shadow private boolean onlyRenaming;

    /** Set when the current result is a mending-overhaul reset, which is allowed to cost 0 levels. */
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
            if (StringUtil.isAllowedChatCharacter(c) || c == '§') {
                builder.append(c);
            }
        }
        String result = builder.toString();
        cir.setReturnValue(result.length() <= AnvilMenu.MAX_NAME_LENGTH ? result : null);
    }

    // ── anvilNoItalicsRename (vanilla rename paths) ──
    @ModifyExpressionValue(
            method = {"setItemName", "createResult"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    )
    private MutableComponent neatlybetter$makeRenamesNonItalic(MutableComponent original) {
        return neatlybetter$styleName(original);
    }

    @Unique
    private static MutableComponent neatlybetter$styleName(MutableComponent name) {
        if (NTServerConfig.CONFIG.anvilNoItalicsRename.isFalse()) return name;
        return name.withStyle(style -> style.withItalic(false));
    }

    // ── anvilNetheriteRepair / mendingInventory (OVERHAUL) / anvilEnchantFeather ──
    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$customRecipes(CallbackInfo ci) {
        this.neatlybetter$mendingOverhaulResult = false;

        ItemStack left = this.inputSlots.getItem(0);
        ItemStack right = this.inputSlots.getItem(1);
        if (left.isEmpty() || right.isEmpty()) return;

        ItemStack result;
        int price;
        boolean workPenalty = true;

        if (NTServerConfig.CONFIG.anvilNetheriteRepair.isTrue()
                && right.is(Items.NETHERITE_INGOT)
                && AddedItemProperties.NETHERITE_REPAIR_ITEMS.contains(left.getItem())
                && left.isDamaged()) {
            result = left.copy();
            result.setDamageValue(0);
            price = 1;
        } else if (NTServerConfig.CONFIG.mendingInventory.get() == MendingScope.OVERHAUL
                && neatlybetter$isMendingBook(right)
                && !left.is(Items.ENCHANTED_BOOK)
                && EnchantmentHelper.canStoreEnchantments(left)) {
            result = neatlybetter$resetWithMendingBook(left);
            if (result == null) {
                // Nothing to remove: don't consume the book for an identical item.
                neatlybetter$setResult(ItemStack.EMPTY, 0, ci);
                return;
            }
            price = 0;
            workPenalty = false; // the reset *is* the penalty removal
            this.neatlybetter$mendingOverhaulResult = true;
        } else if (NTServerConfig.CONFIG.anvilEnchantFeather.isTrue() && left.is(Items.FEATHER)) {
            result = neatlybetter$applyFeatherKnockback(left, right);
            if (result == null) return; // not a valid combination; vanilla rejects feathers
            price = neatlybetter$knockbackLevel(result, DataComponents.ENCHANTMENTS) * left.getCount();
        } else {
            return;
        }

        price += neatlybetter$applyTypedName(left, result);

        long tax = workPenalty
                ? (long) left.getOrDefault(DataComponents.REPAIR_COST, 0) + right.getOrDefault(DataComponents.REPAIR_COST, 0)
                : 0L;
        int finalCost = price <= 0 ? 0 : (int) Mth.clamp(tax + price, 0L, Integer.MAX_VALUE);

        if (workPenalty) {
            int baseCost = Math.max(
                    result.getOrDefault(DataComponents.REPAIR_COST, 0),
                    right.getOrDefault(DataComponents.REPAIR_COST, 0));
            result.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(baseCost));
        }

        if (finalCost >= neatlybetter$TOO_EXPENSIVE
                && NTServerConfig.CONFIG.anvilNoTooExpensive.isFalse()
                && !this.player.hasInfiniteMaterials()) {
            result = ItemStack.EMPTY; // keep the cost so the screen shows "Too Expensive!"
        }

        neatlybetter$setResult(result, finalCost, ci);
    }

    @Unique
    private void neatlybetter$setResult(ItemStack result, int levelCost, CallbackInfo ci) {
        this.onlyRenaming = false;
        this.repairItemCountCost = result.isEmpty() ? 0 : 1;
        this.resultSlots.setItem(0, result);
        this.cost.set(levelCost);
        this.broadcastChanges();
        ci.cancel();
    }

    /**
     * Mirrors vanilla naming in {@code createResult}: applies (or clears) the typed name.
     * Returns the level cost of renaming, which is 0 when renames are free.
     */
    @Unique
    private int neatlybetter$applyTypedName(ItemStack input, ItemStack result) {
        boolean renamed = false;
        if (this.itemName != null && !StringUtil.isBlank(this.itemName)) {
            if (!this.itemName.equals(input.getHoverName().getString())) {
                result.set(DataComponents.CUSTOM_NAME, neatlybetter$styleName(Component.literal(this.itemName)));
                renamed = true;
            }
        } else if (input.has(DataComponents.CUSTOM_NAME)) {
            result.remove(DataComponents.CUSTOM_NAME);
            renamed = true;
        }
        return renamed && NTServerConfig.CONFIG.anvilNoRenameCost.isFalse() ? 1 : 0;
    }

    /** Removes Mending and the prior-work penalty; null when the item has neither. */
    @Unique
    private static @Nullable ItemStack neatlybetter$resetWithMendingBook(ItemStack left) {
        boolean hasMending = false;
        for (Holder<@NotNull Enchantment> e : left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).keySet()) {
            if (e.is(Enchantments.MENDING)) {
                hasMending = true;
                break;
            }
        }
        boolean hasPenalty = left.getOrDefault(DataComponents.REPAIR_COST, 0) > 0;
        if (!hasMending && !hasPenalty) return null;

        ItemStack result = left.copy();
        if (hasMending) {
            EnchantmentHelper.updateEnchantments(result, m -> m.removeIf(e -> e.is(Enchantments.MENDING)));
        }
        result.remove(DataComponents.REPAIR_COST);
        return result;
    }

    @Unique
    private static boolean neatlybetter$isMendingBook(ItemStack stack) {
        if (!stack.is(Items.ENCHANTED_BOOK)) return false;
        for (Holder<@NotNull Enchantment> e : stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).keySet()) {
            if (e.is(Enchantments.MENDING)) return true;
        }
        return false;
    }

    /**
     * Feather + knockback book, or feather + knockback feather. Levels merge like vanilla
     * (equal levels go up by one, otherwise the higher one wins), capped at 2.
     * Returns null if the right item offers no knockback or the feathers wouldn't improve.
     */
    @Unique
    private @Nullable ItemStack neatlybetter$applyFeatherKnockback(ItemStack left, ItemStack right) {
        int current = neatlybetter$knockbackLevel(left, DataComponents.ENCHANTMENTS);
        int offered;
        if (right.is(Items.ENCHANTED_BOOK)) {
            offered = neatlybetter$knockbackLevel(right, DataComponents.STORED_ENCHANTMENTS);
        } else if (right.is(Items.FEATHER)) {
            offered = neatlybetter$knockbackLevel(right, DataComponents.ENCHANTMENTS);
        } else {
            return null;
        }
        if (offered <= 0) return null;

        int target = Math.min(neatlybetter$MAX_FEATHER_KNOCKBACK,
                current == offered ? current + 1 : Math.max(current, offered));
        if (target <= current) return null;

        // Resolve through the player, not ContainerLevelAccess: the access is NULL on the client,
        // which made the preview show a plain feather.
        Holder<@NotNull Enchantment> knockback = this.player.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.KNOCKBACK);

        ItemStack result = left.copy();
        EnchantmentHelper.updateEnchantments(result, m -> m.set(knockback, target));
        return result;
    }

    @Unique
    private static int neatlybetter$knockbackLevel(ItemStack stack, DataComponentType<ItemEnchantments> component) {
        ItemEnchantments enchantments = stack.getOrDefault(component, ItemEnchantments.EMPTY);
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().is(Enchantments.KNOCKBACK)) {
                return Math.clamp(entry.getIntValue(), 0, neatlybetter$MAX_FEATHER_KNOCKBACK);
            }
        }
        return 0;
    }

    // ── anvilNoRenameCost (vanilla results) ──
    // Pure renames become free; renaming while combining no longer adds the extra level either.
    @Inject(method = "createResult", at = @At("TAIL"))
    private void neatlybetter$freeRenames(CallbackInfo ci) {
        if (NTServerConfig.CONFIG.anvilNoRenameCost.isFalse() || !neatlybetter$outputIsRenamed()) return;

        if (this.onlyRenaming) {
            this.cost.set(0);
        } else if (this.cost.get() > 1) {
            this.cost.set(this.cost.get() - 1);
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void neatlybetter$mayPickup(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        int levelCost = this.cost.get();

        // Players with infinite materials who aren't in creative still pay (see onTake), so they need the levels.
        if (!player.isCreative() && player.hasInfiniteMaterials() && levelCost > 0 && player.experienceLevel < levelCost) {
            cir.setReturnValue(false);
            return;
        }

        // Results that are allowed to cost 0 levels (vanilla requires cost > 0).
        if (levelCost == 0 && (this.neatlybetter$mendingOverhaulResult
                || (this.onlyRenaming && NTServerConfig.CONFIG.anvilNoRenameCost.isTrue()))) {
            cir.setReturnValue(hasItem);
        }
    }

    // ── charge players who were granted infinite materials without being creative ──
    @Inject(method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"))
    private void neatlybetter$chargeGrantedInfiniteMaterials(Player player, ItemStack carried, CallbackInfo ci) {
        int currentCost = this.cost.get();
        if (!player.isCreative() && player.hasInfiniteMaterials()
                && currentCost > 0 && player.experienceLevel >= currentCost) {
            player.giveExperienceLevels(-currentCost);
        }
    }

    // ── anvilNoTooExpensive (vanilla results; ordinal 2 = `cost >= 40` result gate) ──
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 2))
    private int neatlybetter$removeTooExpensiveGate(int original) {
        if (NTServerConfig.CONFIG.anvilNoTooExpensive.isFalse()) return original;
        return Integer.MAX_VALUE;
    }

    @Unique
    private boolean neatlybetter$outputIsRenamed() {
        ItemStack output = this.resultSlots.getItem(0);
        if (output.isEmpty()) return false;
        Component inName = this.inputSlots.getItem(0).get(DataComponents.CUSTOM_NAME);
        Component outName = output.get(DataComponents.CUSTOM_NAME);
        if (inName == null) return outName != null;
        return !inName.equals(outName);
    }
}
