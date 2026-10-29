package net.eviz.farmersdismay.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;

public class SugarFertilizer extends Item {

    public SugarFertilizer(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getWorld() instanceof ServerWorld world)) {
            return ActionResult.SUCCESS;
        }

        var pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof CropBlock crop)) {
            return ActionResult.PASS;
        }

        IntProperty ageProperty = CropDecayManager.getAgeProperty(state);

        if (ageProperty == null
                || state.get(ageProperty) >= crop.getMaxAge()) {
            return ActionResult.PASS;
        }

        SugarRushGrowthManager.fertilize(world, pos);

        PlayerEntity player = context.getPlayer();

        if (player == null || !player.isCreative()) {
            context.getStack().decrement(1);
        }

        return ActionResult.SUCCESS;
    }
}