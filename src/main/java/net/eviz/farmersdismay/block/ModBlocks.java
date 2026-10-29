
package net.eviz.farmersdismay.block;

import net.eviz.farmersdismay.FarmersDismay;
import net.eviz.farmersdismay.block.custom.Decomposter;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block DECOMPOSTER = registerBlock(
            "decomposter",
            new Decomposter(
                    AbstractBlock.Settings.copy(Blocks.COMPOSTER)
                            .strength(3f)
                            .requiresTool()
                            .sounds(BlockSoundGroup.ANVIL)
            )
    );

    public static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(
                Registries.BLOCK,
                Identifier.of(FarmersDismay.MOD_ID, name),
                block
        );
    }

    public static void registerBlockItem(String name, Block block) {
        Registry.register(
                Registries.ITEM,
                Identifier.of(FarmersDismay.MOD_ID, name),
                new BlockItem(block, new Item.Settings())
        );
    }

    public static void registerModBlocks() {
        FarmersDismay.LOGGER.info(
                "Registering Mod Blocks for " + FarmersDismay.MOD_ID
        );

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(entries -> entries.add(DECOMPOSTER));
    }
}
