
package net.eviz.farmersdismay.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ReSickler extends Item {

    public ReSickler(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack tool = context.getStack();

        BlockState state = world.getBlockState(pos);

        if (!isSupportedCrop(state) || !isMature(state)) {
            return ActionResult.PASS;
        }

        if (player == null) {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        Item seed = getSeed(state);

        boolean hasSeed = false;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);

            if (stack.isOf(seed)) {
                stack.decrement(1);
                hasSeed = true;
                break;
            }
        }

        Block.dropStacks(
                state,
                world,
                pos,
                world.getBlockEntity(pos),
                player,
                tool
        );

        if (hasSeed) {

            if (state.isOf(Blocks.BEETROOTS)) {
                world.setBlockState(
                        pos,
                        state.with(Properties.AGE_3, 0),
                        Block.NOTIFY_ALL
                );
            } else {
                world.setBlockState(
                        pos,
                        state.with(Properties.AGE_7, 0),
                        Block.NOTIFY_ALL
                );
            }

        } else {
            world.setBlockState(
                    pos,
                    Blocks.AIR.getDefaultState(),
                    Block.NOTIFY_ALL
            );
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
            return state.get(Properties.AGE_3) >= 3;
        }

        return state.get(Properties.AGE_7) >= 7;
    }

    private Item getSeed(BlockState state) {
        if (state.isOf(Blocks.WHEAT)) {
            return Items.WHEAT_SEEDS;
        }

        if (state.isOf(Blocks.CARROTS)) {
            return Items.CARROT;
        }

        if (state.isOf(Blocks.POTATOES)) {
            return Items.POTATO;
        }

        return Items.BEETROOT_SEEDS;
    }
}
