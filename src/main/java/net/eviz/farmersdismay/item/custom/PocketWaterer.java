
package net.eviz.farmersdismay.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.List;
import java.util.Map;

public class PocketWaterer extends Item {

    private static final int MAX_CHARGES = 5;

    private static final Map<Block, Block> CONCRETE_MAP = Map.ofEntries(
            Map.entry(Blocks.WHITE_CONCRETE_POWDER, Blocks.WHITE_CONCRETE),
            Map.entry(Blocks.ORANGE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE),
            Map.entry(Blocks.MAGENTA_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE),
            Map.entry(Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE),
            Map.entry(Blocks.YELLOW_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE),
            Map.entry(Blocks.LIME_CONCRETE_POWDER, Blocks.LIME_CONCRETE),
            Map.entry(Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_CONCRETE),
            Map.entry(Blocks.GRAY_CONCRETE_POWDER, Blocks.GRAY_CONCRETE),
            Map.entry(Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE),
            Map.entry(Blocks.CYAN_CONCRETE_POWDER, Blocks.CYAN_CONCRETE),
            Map.entry(Blocks.PURPLE_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE),
            Map.entry(Blocks.BLUE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE),
            Map.entry(Blocks.BROWN_CONCRETE_POWDER, Blocks.BROWN_CONCRETE),
            Map.entry(Blocks.GREEN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE),
            Map.entry(Blocks.RED_CONCRETE_POWDER, Blocks.RED_CONCRETE),
            Map.entry(Blocks.BLACK_CONCRETE_POWDER, Blocks.BLACK_CONCRETE)
    );

    public PocketWaterer(Settings settings) {
        super(settings);
    }

    private int getCharges(ItemStack stack) {
        return MAX_CHARGES - stack.getDamage();
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        BlockHitResult hit = raycast(
                world,
                user,
                RaycastContext.FluidHandling.SOURCE_ONLY
        );

        if (hit.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(stack);
        }

        BlockPos pos = hit.getBlockPos();

        if (!world.getFluidState(pos).isOf(Fluids.WATER)
                || !world.getFluidState(pos).isStill()) {
            return TypedActionResult.pass(stack);
        }

        if (getCharges(stack) >= MAX_CHARGES) {
            return TypedActionResult.pass(stack);
        }

        if (!world.isClient()) {
            stack.setDamage(Math.max(0, stack.getDamage() - 1));

            world.playSound(
                    null,
                    pos,
                    SoundEvents.ITEM_BUCKET_FILL,
                    SoundCategory.BLOCKS,
                    1.0f,
                    1.0f
            );
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ItemStack stack = context.getStack();

        if (getCharges(stack) <= 0) {
            return ActionResult.PASS;
        }

        BlockState result = getWaterReaction(state, world, pos);

        if (result == null) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            world.setBlockState(pos, result, Block.NOTIFY_ALL);
            stack.setDamage(stack.getDamage() + 1);

            world.playSound(
                    null,
                    pos,
                    SoundEvents.ITEM_BUCKET_EMPTY,
                    SoundCategory.BLOCKS,
                    0.8f,
                    1.0f
            );
        }

        return ActionResult.SUCCESS;
    }

    private BlockState getWaterReaction(BlockState state, World world, BlockPos pos) {
        Block block = state.getBlock();

        if (block == Blocks.FARMLAND) {
            if (state.get(FarmlandBlock.MOISTURE) < FarmlandBlock.MAX_MOISTURE) {
                return state.with(
                        FarmlandBlock.MOISTURE,
                        FarmlandBlock.MAX_MOISTURE
                );
            }
            return null;
        }

        if (block == Blocks.DIRT
                || block == Blocks.COARSE_DIRT
                || block == Blocks.ROOTED_DIRT) {
            return Blocks.MUD.getDefaultState();
        }

        if (block == Blocks.SPONGE) {
            return Blocks.WET_SPONGE.getDefaultState();
        }

        if (CONCRETE_MAP.containsKey(block)) {
            return CONCRETE_MAP.get(block).getDefaultState();
        }

        if (state.contains(Properties.LIT) && state.get(Properties.LIT)) {
            if (block instanceof CampfireBlock
                    || block instanceof CandleBlock
                    || block == Blocks.CANDLE_CAKE) {
                return state.with(Properties.LIT, false);
            }
        }

        if (block == Blocks.FIRE || block == Blocks.SOUL_FIRE) {
            return Blocks.AIR.getDefaultState();
        }

        if (state.getFluidState().isOf(Fluids.LAVA)) {
            if (state.getFluidState().isStill()) {
                return Blocks.OBSIDIAN.getDefaultState();
            }
            return Blocks.COBBLESTONE.getDefaultState();
        }

        return null;
    }

    @Override
    public void appendTooltip(
            ItemStack stack,
            Item.TooltipContext context,
            List<Text> tooltip,
            TooltipType type
    ) {
        tooltip.add(
                Text.literal("Water: " + getCharges(stack) + "/" + MAX_CHARGES)
                        .formatted(Formatting.AQUA)
        );
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0f * getCharges(stack) / MAX_CHARGES);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0x3399FF;
    }
}
