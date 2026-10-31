
package net.eviz.farmersdismay.mixin;

import net.eviz.farmersdismay.crop.CropRouletteManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractBlock.class)
public abstract class CropRouletteLootMixin {

    @Inject(
            method = "getDroppedStacks",
            at = @At("RETURN"),
            cancellable = true
    )
    private void farmersDismay$doubleSpecialCropDrops(
            BlockState state,
            LootContextParameterSet.Builder builder,
            CallbackInfoReturnable<List<ItemStack>> cir
    ) {
        if (!CropRouletteManager.isSupportedCrop(state)) {
            return;
        }

        Vec3d origin = builder.getOptional(LootContextParameters.ORIGIN);

        if (origin == null) {
            return;
        }

        ServerWorld world = builder.getWorld();
        BlockPos pos = BlockPos.ofFloored(origin);

        if (!CropRouletteManager.consumeSpecial(world, pos)) {
            return;
        }

        List<ItemStack> originalDrops = cir.getReturnValue();
        List<ItemStack> doubledDrops = new ArrayList<>();

        for (ItemStack stack : originalDrops) {
            doubledDrops.add(stack);
            doubledDrops.add(stack.copy());
        }

        cir.setReturnValue(doubledDrops);
    }
}
