package net.eviz.farmersdismay.block.custom;

import com.mojang.serialization.MapCodec;
import net.eviz.farmersdismay.entity.custom.DecomposterBlockEntity;
import net.eviz.farmersdismay.entity.renderer.ModBlockEntities;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Decomposter extends BlockWithEntity {

    public static final MapCodec<Decomposter> CODEC =
            createCodec(Decomposter::new);

    public Decomposter(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DecomposterBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            BlockHitResult hit
    ) {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof DecomposterBlockEntity decomposter) {
                player.openHandledScreen(decomposter);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return validateTicker(
                type,
                ModBlockEntities.DECOMPOSTER,
                DecomposterBlockEntity::tick
        );
    }
}