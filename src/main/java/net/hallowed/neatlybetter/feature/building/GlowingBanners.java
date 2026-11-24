package net.hallowed.neatlybetter.feature.building;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.hallowed.neatlybetter.registry.ModData;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.List;
import java.util.function.Consumer;

/**
 * Glow ink sac on a banner with patterns makes the patterns glow (full brightness); the base colour stays lit as
 * normal. An ink sac removes the glow again, like on signs.
 *
 * <p>In the world the state is a synced block entity attachment. As an item it is the {@code glowing_banner}
 * component: added to the dropped banner, and turned back into the attachment when the banner is placed.
 */
public final class GlowingBanners {
    private GlowingBanners() {}

    /** Item component: present on glowing banner items. Shows a "Glowing" tooltip line. */
    public record Glowing() implements TooltipProvider {
        public static final Glowing INSTANCE = new Glowing();
        public static final Codec<Glowing> CODEC = MapCodec.unitCodec(INSTANCE);
        public static final StreamCodec<ByteBuf, Glowing> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
            consumer.accept(Component.translatable("tooltip.neatly-better.glowing_banner").withStyle(ChatFormatting.GRAY));
        }
    }

    public static boolean isGlowing(BlockEntity banner) {
        return banner.getAttachedOrElse(ModData.GLOWING_BANNER, false);
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            ItemStack stack = player.getItemInHand(hand);
            boolean glowInk = stack.is(Items.GLOW_INK_SAC);
            if (!glowInk && !stack.is(Items.INK_SAC)) return InteractionResult.PASS;
            if (player.isSpectator() || !player.mayBuild()) return InteractionResult.PASS;
            if (!(level.getBlockEntity(hitResult.getBlockPos()) instanceof BannerBlockEntity banner)) return InteractionResult.PASS;
            if (banner.getPatterns().layers().isEmpty() || isGlowing(banner) == glowInk) return InteractionResult.PASS;

            if (!level.isClientSide()) {
                if (glowInk) banner.setAttached(ModData.GLOWING_BANNER, true);
                else banner.removeAttached(ModData.GLOWING_BANNER);
                banner.setChanged();

                SoundEvent sound = glowInk ? SoundEvents.GLOW_INK_SAC_USE : SoundEvents.INK_SAC_USE;
                level.playSound(null, hitResult.getBlockPos(), sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, hitResult.getBlockPos());
                stack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        });
    }

    /** Server: marks dropped banner items from a glowing banner. */
    public static void markDrops(BlockEntity blockEntity, List<ItemStack> drops) {
        if (!(blockEntity instanceof BannerBlockEntity) || !isGlowing(blockEntity)) return;
        for (ItemStack drop : drops) {
            if (drop.has(DataComponents.BANNER_PATTERNS)) drop.set(ModData.GLOWING_BANNER_ITEM, Glowing.INSTANCE);
        }
    }
}
