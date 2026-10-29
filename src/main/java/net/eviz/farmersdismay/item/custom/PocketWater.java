package net.eviz.farmersdismay.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import java.util.Map;

public class PocketWater extends Item {
    public static final Map<Block, Block> WATER_MAP=
            Map.of(
                    Blocks.DIRT, Blocks.FARMLAND,
                    Blocks.SPONGE, Blocks.WET_SPONGE,
                    Blocks.LAVA, Blocks.OBSIDIAN,
                    Blocks.GRASS_BLOCK, Blocks.FARMLAND
            );
    public PocketWater(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block clickedBlock = world.getBlockState(context.getBlockPos()).getBlock();

        if (WATER_MAP.containsKey(clickedBlock)) ;
        if (!world.isClient()) {
            world.setBlockState(context.getBlockPos(), WATER_MAP.get(clickedBlock).getDefaultState());

            context.getStack().damage(1, ((ServerWorld) world), ((ServerPlayerEntity) context.getPlayer()),
                    item -> context.getPlayer().sendEquipmentBreakStatus(item, EquipmentSlot.MAINHAND));

            world.playSound(null, context.getBlockPos(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS);
        }

     return ActionResult.SUCCESS;
    }
}
