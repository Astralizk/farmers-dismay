
package net.eviz.farmersdismay.mixin;

import net.eviz.farmersdismay.crop.CropRouletteManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public abstract class CropRouletteGrowthMixin {

    @Inject(method = "randomTick", at = @At("TAIL"))
    private void farmersDismay$checkNaturalGrowth(
            BlockState oldState,
            ServerWorld world,
            BlockPos pos,
            Random random,
            CallbackInfo ci
    ) {
        farmersDismay$checkMaturity(oldState, world, pos, random);
    }

    @Inject(method = "grow", at = @At("TAIL"))
    private void farmersDismay$checkBonemealGrowth(
            ServerWorld world,
            Random random,
            BlockPos pos,
            BlockState oldState,
            CallbackInfo ci
    ) {
        farmersDismay$checkMaturity(oldState, world, pos, random);
    }

    private void farmersDismay$checkMaturity(
            BlockState oldState,
            ServerWorld world,
            BlockPos pos,
            Random random
    ) {
        CropBlock crop = (CropBlock) (Object) this;
        BlockState newState = world.getBlockState(pos);

        if (!CropRouletteManager.isSupportedCrop(newState)) {
            return;
        }

        if (!crop.isMature(oldState) && crop.isMature(newState)) {
            if (random.nextFloat() < 0.03f) {
                CropRouletteManager.markSpecial(world, pos);
            }
        }
    }
}
