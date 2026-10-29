
package net.eviz.farmersdismay.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Fertilizer extends Item {

    public Fertilizer(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof CropBlock crop)) {
            return ActionResult.PASS;
        }

        if (!crop.isMature(state)) {
            if (!world.isClient) {
                FertilizerGrowthManager.fertilize(world, pos);

                PlayerEntity player = context.getPlayer();
                if (player != null && !player.getAbilities().creativeMode) {
                    context.getStack().decrement(1);
                }
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
