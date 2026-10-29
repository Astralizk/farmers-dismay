package net.eviz.farmersdismay.entity.renderer;

import net.eviz.farmersdismay.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.eviz.farmersdismay.FarmersDismay;
import net.eviz.farmersdismay.block.ModBlocks;
import net.eviz.farmersdismay.entity.custom.DecomposterBlockEntity;

public class ModBlockEntities {

    public static BlockEntityType<DecomposterBlockEntity> DECOMPOSTER;

    public static void registerBlockEntities() {
        DECOMPOSTER = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(FarmersDismay.MOD_ID, "decomposter"),
                FabricBlockEntityTypeBuilder.create(
                        DecomposterBlockEntity::new,
                        ModBlocks.DECOMPOSTER
                ).build()
        );
    }
}