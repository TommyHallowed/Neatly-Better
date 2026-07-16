package net.hallowed.neatlybetter.client.init;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

import net.hallowed.neatlybetter.client.config.NTClientConfig;
import net.hallowed.neatlybetter.config.NTServerConfig;
import net.hallowed.neatlybetter.content.item.MapBuilderItem;
import net.hallowed.neatlybetter.init.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.Equippable;

import java.util.List;

import static net.hallowed.neatlybetter.init.ModBlocks.GLOW_TORCH;

public final class ModTooltips {
    private ModTooltips() {}

    private static final MutableComponent GLOW_SAC_HINT =
            Component.translatable("tooltip.neatly-better.glow_ink_sac_hint")
                    .withStyle(ChatFormatting.YELLOW);

    private static final MutableComponent ECHO_SHARD_HINT =
            Component.translatable("tooltip.neatly-better.echo_shard_hint")
                    .withStyle(ChatFormatting.YELLOW);

    private static final MutableComponent PHANTOM_MEMBRANE_HINT =
            Component.translatable("tooltip.neatly-better.phantom_membrane_hint")
                    .withStyle(ChatFormatting.YELLOW);

    private static final String TOTEM_COOLDOWN_KEY = "tooltip.neatly-better.totem_cooldown";

    private static final MutableComponent EQUIP_HINT =
            Component.translatable("tooltip.neatly-better.right_click_equip")
                    .withStyle(ChatFormatting.YELLOW);

    private static final List<MutableComponent> MAP_BUILDER_LINES = List.of(
            Component.empty(),
            Component.translatable("tooltip.neatly-better.map_builder.right_click")
                    .withStyle(ChatFormatting.YELLOW),
            Component.translatable("tooltip.neatly-better.map_builder.left_click")
                    .withStyle(ChatFormatting.YELLOW),
            Component.translatable("tooltip.neatly-better.map_builder.build")
                    .withStyle(ChatFormatting.YELLOW),
            Component.translatable("tooltip.neatly-better.map_builder.zoom")
                    .withStyle(ChatFormatting.YELLOW),
            Component.empty(),
            Component.translatable("tooltip.neatly-better.map_builder.requires")
                    .withStyle(ChatFormatting.RED)
    );

    private static final MutableComponent MILK_BOTTLE_HINT =
            Component.translatable("tooltip.neatly-better.milk_bottle_hint")
                    .withStyle(ChatFormatting.YELLOW);

    private static final MutableComponent MILK_BUCKET_HINT =
            Component.translatable("tooltip.neatly-better.milk_bucket_hint")
                    .withStyle(ChatFormatting.YELLOW);

    private static final MutableComponent SLIME_BALL_HINT =
            Component.translatable("tooltip.neatly-better.slime_ball_hint")
                    .withStyle(ChatFormatting.YELLOW);

    private static final MutableComponent WOLF_COLLAR_HINT =
            Component.translatable("tooltip.neatly-better.wolf_collar_hint")
                    .withStyle(ChatFormatting.YELLOW);

    private static final MutableComponent GLOW_TORCH_HINT =
            Component.translatable("tooltip.neatly-better.glow_torch_hint")
                    .withStyle(ChatFormatting.YELLOW);

    public static void init() {
        ItemTooltipCallback.EVENT.register(ModTooltips::onTooltip);
    }

    private static void onTooltip(ItemStack stack,
                                  Item.TooltipContext ctx,
                                  TooltipFlag type,
                                  List<Component> lines) {
        if (stack == null || stack.isEmpty()) return;

        if (!NTClientConfig.CONFIG.tooltipGuide.get()) return;

        if (stack.is(Items.GLOW_INK_SAC)) {
            addBasicUnderName(lines, GLOW_SAC_HINT);
        }
        if (stack.is(Items.ECHO_SHARD)) {
            addBasicUnderName(lines, ECHO_SHARD_HINT);
        }
        if (stack.is(Items.PHANTOM_MEMBRANE)) {
            addBasicUnderName(lines, PHANTOM_MEMBRANE_HINT);
        }
        if (stack.is(Items.TOTEM_OF_UNDYING)) {
            int seconds = NTServerConfig.CONFIG.totemCooldown.get();
            addBasicUnderName(lines,
                    Component.translatable(TOTEM_COOLDOWN_KEY, seconds)
                            .withStyle(ChatFormatting.YELLOW));
        }
        if (stack.getItem() instanceof MapBuilderItem) {
            lines.addAll(MAP_BUILDER_LINES);
        }
        if (stack.is(ModItems.MILK_BOTTLE)) {
            addBasicUnderName(lines, MILK_BOTTLE_HINT);
        }
        if (stack.is(Items.MILK_BUCKET)) {
            addBasicUnderName(lines, MILK_BUCKET_HINT);
        }
        if (stack.is(Items.SLIME_BALL)) {
            addBasicUnderName(lines, SLIME_BALL_HINT);
        }
        if (stack.is(ModItems.WOLF_COLLAR)) {
            addBasicUnderName(lines, WOLF_COLLAR_HINT);
        }
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == GLOW_TORCH) {
            addBasicUnderName(lines, GLOW_TORCH_HINT);
        }

        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null) {
            EquipmentSlot slot = equippable.slot();
            if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST
                    || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {

                LocalPlayer player = Minecraft.getInstance().player;

                if ((player == null || player.getItemBySlot(slot) != stack) && !player.isCreative()) {
                    int insertPos = lines.size();

                    if (type.isAdvanced()) {
                        String regName = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                        for (int i = 0; i < lines.size(); i++) {
                            if (lines.get(i).getString().contains(regName)) {
                                insertPos = i;
                                if (i > 0 && stack.isDamaged()
                                        && lines.get(i - 1).getString().contains("/")) {
                                    insertPos = i - 1;
                                }
                                break;
                            }
                        }
                    }
                    lines.add(insertPos, Component.empty());
                    lines.add(insertPos + 1, EQUIP_HINT);
                }
            }
        }
    }

    public static void addBasicUnderName(List<Component> lines, Component tip) {
        final int insertAt = Math.min(lines.size(), 1);
        if (insertAt < lines.size()) {
            Component existing = lines.get(insertAt);
            if (existing.getString().equals(tip.getString())) return;
            if (existing.getString().isBlank()) {
                lines.set(insertAt, tip);
                return;
            }
        }
        lines.add(insertAt, tip);
    }
}