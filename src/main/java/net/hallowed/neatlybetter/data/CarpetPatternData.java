package net.hallowed.neatlybetter.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public record CarpetPatternData(BannerPatternLayers patterns, int rotation) implements TooltipProvider {

    public static final CarpetPatternData EMPTY = new CarpetPatternData(BannerPatternLayers.EMPTY, 0);

    public static final Codec<CarpetPatternData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BannerPatternLayers.CODEC.fieldOf("patterns").forGetter(CarpetPatternData::patterns),
                    Codec.INT.optionalFieldOf("rotation", 0).forGetter(CarpetPatternData::rotation)
            ).apply(instance, CarpetPatternData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CarpetPatternData> STREAM_CODEC =
            StreamCodec.composite(
                    BannerPatternLayers.STREAM_CODEC, CarpetPatternData::patterns,
                    ByteBufCodecs.VAR_INT, CarpetPatternData::rotation,
                    CarpetPatternData::new
            );

    public boolean isEmpty() {
        return patterns.layers().isEmpty();
    }

    public int layerCount() {
        return patterns.layers().size();
    }

    public CarpetPatternData withRotation(int newRotation) {
        return new CarpetPatternData(this.patterns, newRotation & 3);
    }

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
        patterns.addToTooltip(context, consumer, flag, components);
    }
}