package net.hallowed.neatlybetter.init;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.hallowed.neatlybetter.api.NTRegistry;
import net.hallowed.neatlybetter.content.blockentity.DyeCauldronBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static BlockEntityType<DyeCauldronBlockEntity> DYE_CAULDRON;

    public static void register() {
        DYE_CAULDRON = NTRegistry.registerBlockEntity("dye_cauldron",
                FabricBlockEntityTypeBuilder.create(DyeCauldronBlockEntity::new, ModBlocks.DYE_CAULDRON));
    }
}