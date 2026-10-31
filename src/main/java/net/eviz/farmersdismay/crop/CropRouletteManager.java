
package net.eviz.farmersdismay.crop;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CropRouletteManager {

    private static final String STATE_ID = "farmersdismay_crop_roulette";

    private static final PersistentState.Type<CropRouletteData> STATE_TYPE =
            new PersistentState.Type<>(
                    CropRouletteData::new,
                    CropRouletteData::fromNbt,
                    DataFixTypes.LEVEL
            );

    private static CropRouletteData getData(ServerWorld world) {
        return world.getPersistentStateManager()
                .getOrCreate(STATE_TYPE, STATE_ID);
    }

    public static boolean isSupportedCrop(BlockState state) {
        return state.isOf(Blocks.WHEAT)
                || state.isOf(Blocks.CARROTS)
                || state.isOf(Blocks.POTATOES)
                || state.isOf(Blocks.BEETROOTS);
    }

    public static void markSpecial(ServerWorld world, BlockPos pos) {
        getData(world).add(pos);
    }

    public static boolean consumeSpecial(ServerWorld world, BlockPos pos) {
        return getData(world).remove(pos);
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getTime() % 8 != 0) {
                return;
            }

            CropRouletteData data = getData(world);

            for (long packedPos : data.getPositions()) {
                BlockPos pos = BlockPos.fromLong(packedPos);

                if (!world.isChunkLoaded(pos)) {
                    continue;
                }

                BlockState state = world.getBlockState(pos);

                if (!(state.getBlock() instanceof CropBlock crop)
                        || !isSupportedCrop(state)
                        || !crop.isMature(state)) {
                    data.remove(pos);
                    continue;
                }

                double x = pos.getX() + 0.2 + world.random.nextDouble() * 0.6;
                double y = pos.getY() + 0.8 + world.random.nextDouble() * 0.3;
                double z = pos.getZ() + 0.2 + world.random.nextDouble() * 0.6;

                world.spawnParticles(
                        ParticleTypes.END_ROD,
                        x, y, z,
                        1,
                        0.0, 0.025, 0.0,
                        0.005
                );
            }
        });
    }

    private static class CropRouletteData extends PersistentState {

        private final Set<Long> specialCrops = new HashSet<>();

        public void add(BlockPos pos) {
            if (specialCrops.add(pos.asLong())) {
                markDirty();
            }
        }

        public boolean remove(BlockPos pos) {
            boolean removed = specialCrops.remove(pos.asLong());

            if (removed) {
                markDirty();
            }

            return removed;
        }

        public ArrayList<Long> getPositions() {
            return new ArrayList<>(specialCrops);
        }

        public static CropRouletteData fromNbt(
                NbtCompound nbt,
                RegistryWrapper.WrapperLookup registries
        ) {
            CropRouletteData data = new CropRouletteData();

            for (long packedPos : nbt.getLongArray("SpecialCrops")) {
                data.specialCrops.add(packedPos);
            }

            return data;
        }

        @Override
        public NbtCompound writeNbt(
                NbtCompound nbt,
                RegistryWrapper.WrapperLookup registries
        ) {
            long[] positions = new long[specialCrops.size()];

            int index = 0;
            for (long packedPos : specialCrops) {
                positions[index++] = packedPos;
            }

            nbt.putLongArray("SpecialCrops", positions);

            return nbt;
        }
    }
}
