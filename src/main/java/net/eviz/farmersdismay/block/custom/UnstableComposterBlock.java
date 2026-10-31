
package net.eviz.farmersdismay.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class UnstableComposterBlock extends ComposterBlock {

    private static final int CROP_RADIUS = 5;
    private static final int MIN_GROWTH = 1;
    private static final int MAX_GROWTH = 3;

    public UnstableComposterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ItemActionResult onUseWithItem(
            ItemStack stack,
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit
    ) {
        int oldLevel = state.get(LEVEL);

        if (oldLevel >= 7) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        float compostChance =
                ITEM_TO_LEVEL_INCREASE_CHANCE.getFloat(stack.getItem());

        if (compostChance <= 0.0F) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;

            boolean increasesLevel =
                    oldLevel == 0 ||
                            serverWorld.getRandom().nextFloat() < compostChance * 0.8F;

            if (increasesLevel) {
                int newLevel = oldLevel + 1;

                world.setBlockState(
                        pos,
                        state.with(LEVEL, newLevel),
                        3
                );

                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }

                world.playSound(
                        null,
                        pos,
                        SoundEvents.BLOCK_COMPOSTER_FILL,
                        SoundCategory.BLOCKS,
                        1.0F,
                        1.0F
                );

                serverWorld.spawnParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5,
                        pos.getY() + 0.8,
                        pos.getZ() + 0.5,
                        5,
                        0.25,
                        0.2,
                        0.25,
                        0.02
                );

                if (newLevel >= 7) {
                    activateComposter(serverWorld, pos);
                }
            } else {
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }

                ComposterBlock.playEffects(world, pos, false);
            }
        }

        return ItemActionResult.SUCCESS;
    }

    private void activateComposter(ServerWorld world, BlockPos pos) {

        if (world.getRandom().nextFloat() < 0.40F) {

            world.createExplosion(
                    null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    4.0F,
                    World.ExplosionSourceType.TNT
            );

        } else {
            growNearbyCrops(world, pos);

            world.setBlockState(
                    pos,
                    world.getBlockState(pos).with(LEVEL, 0),
                    3
            );

            world.playSound(
                    null,
                    pos,
                    SoundEvents.BLOCK_COMPOSTER_EMPTY,
                    SoundCategory.BLOCKS,
                    1.0F,
                    1.0F
            );

            world.spawnParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5,
                    pos.getY() + 0.8,
                    pos.getZ() + 0.5,
                    35,
                    2.0,
                    1.0,
                    2.0,
                    0.15
            );

            world.spawnParticles(
                    ParticleTypes.COMPOSTER,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    20,
                    1.5,
                    0.5,
                    1.5,
                    0.1
            );
        }
    }

    private void growNearbyCrops(ServerWorld world, BlockPos center) {

        Random random = world.getRandom();

        for (int x = -CROP_RADIUS; x <= CROP_RADIUS; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -CROP_RADIUS; z <= CROP_RADIUS; z++) {

                    BlockPos cropPos = center.add(x, y, z);
                    BlockState cropState = world.getBlockState(cropPos);

                    if (cropState.getBlock() instanceof CropBlock crop) {

                        int currentAge = crop.getAge(cropState);
                        int maxAge = crop.getMaxAge();

                        if (currentAge < maxAge) {

                            int growth = MIN_GROWTH +
                                    random.nextInt(MAX_GROWTH);

                            int newAge = Math.min(
                                    currentAge + growth,
                                    maxAge
                            );

                            world.setBlockState(
                                    cropPos,
                                    crop.withAge(newAge),
                                    3
                            );

                            world.spawnParticles(
                                    ParticleTypes.HAPPY_VILLAGER,
                                    cropPos.getX() + 0.5,
                                    cropPos.getY() + 0.7,
                                    cropPos.getZ() + 0.5,
                                    3,
                                    0.2,
                                    0.2,
                                    0.2,
                                    0.02
                            );
                        }
                    }
                }
            }
        }
    }
}
