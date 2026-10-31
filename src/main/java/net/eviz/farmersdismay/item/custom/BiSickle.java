
package net.eviz.farmersdismay.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BiSickle extends Item {

    public BiSickle(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();

        if (player == null) {
            return ActionResult.PASS;
        }

        BlockPos origin = context.getBlockPos();
        Direction forward = player.getHorizontalFacing();
        Direction side = forward.rotateYClockwise();

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        for (int depth = 0; depth < 2; depth++) {
            for (int width = 0; width < 2; width++) {
                BlockPos cropPos = origin
                        .offset(forward, depth)
                        .offset(side, width);

                BlockState state = world.getBlockState(cropPos);

                if (!isSupportedCrop(state) || !isMature(state)) {
                    continue;
                }

                Block.dropStacks(
                        state,
                        world,
                        cropPos,
                        world.getBlockEntity(cropPos),
                        player,
                        context.getStack()
                );

                world.setBlockState(
                        cropPos,
                        Blocks.AIR.getDefaultState(),
                        Block.NOTIFY_ALL
                );
            }
        }

        return ActionResult.SUCCESS;
    }

    private boolean isSupportedCrop(BlockState state) {
        return state.isOf(Blocks.WHEAT)
                || state.isOf(Blocks.CARROTS)
                || state.isOf(Blocks.POTATOES)
                || state.isOf(Blocks.BEETROOTS);
    }

    private boolean isMature(BlockState state) {
        if (state.isOf(Blocks.BEETROOTS)) {
            return state.get(net.minecraft.state.property.Properties.AGE_3) >= 3;
        }

        return state.get(net.minecraft.state.property.Properties.AGE_7) >= 7;
    }
}
