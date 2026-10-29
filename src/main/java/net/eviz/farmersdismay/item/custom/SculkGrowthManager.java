package net.eviz.farmersdismay.item.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SculkGrowthManager {

    private static final long GROWTH_INTERVAL = 500L;
    private static final double SPREAD_CHANCE = 0.5;

    private static final Map<ServerWorld, Map<BlockPos, Long>> CROPS =
            new HashMap<>();

    private static final Random RANDOM = new Random();

    private SculkGrowthManager() {
    }

    public static void fertilize(ServerWorld world, BlockPos pos) {
        Map<BlockPos, Long> crops =
                CROPS.computeIfAbsent(world, key -> new HashMap<>());

        crops.put(pos.toImmutable(), world.getTime() + GROWTH_INTERVAL);
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            Map<BlockPos, Long> crops = CROPS.get(world);

            if (crops == null || crops.isEmpty()) {
                return;
            }

            Set<BlockPos> processed = new HashSet<>();

            Iterator<Map.Entry<BlockPos, Long>> iterator =
                    crops.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<BlockPos, Long> entry = iterator.next();
                BlockPos pos = entry.getKey();

                if (!world.isChunkLoaded(pos)) {
                    continue;
                }

                BlockState state = world.getBlockState(pos);

                if (!(state.getBlock() instanceof CropBlock crop)) {
                    iterator.remove();
                    continue;
                }

                IntProperty ageProperty =
                        CropDecayManager.getAgeProperty(state);

                if (ageProperty == null
                        || state.get(ageProperty) >= crop.getMaxAge()) {
                    iterator.remove();
                    continue;
                }

                if (world.getTime() < entry.getValue()) {
                    continue;
                }

                growCrop(world, pos, processed);

                BlockState updated = world.getBlockState(pos);

                if (updated.getBlock() instanceof CropBlock updatedCrop) {
                    IntProperty updatedAge =
                            CropDecayManager.getAgeProperty(updated);

                    if (updatedAge != null
                            && updated.get(updatedAge)
                            < updatedCrop.getMaxAge()) {
                        entry.setValue(world.getTime() + GROWTH_INTERVAL);
                    } else {
                        iterator.remove();
                    }
                } else {
                    iterator.remove();
                }
            }

            if (crops.isEmpty()) {
                CROPS.remove(world);
            }
        });
    }

    private static void growCrop(
            ServerWorld world,
            BlockPos pos,
            Set<BlockPos> processed
    ) {
        if (!processed.add(pos.toImmutable())) {
            return;
        }

        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof CropBlock crop)) {
            return;
        }

        IntProperty ageProperty =
                CropDecayManager.getAgeProperty(state);

        if (ageProperty == null) {
            return;
        }

        int age = state.get(ageProperty);

        if (age >= crop.getMaxAge()) {
            return;
        }

        world.setBlockState(pos, state.with(ageProperty, age + 1));

        if (RANDOM.nextDouble() >= SPREAD_CHANCE) {
            return;
        }

        BlockPos[] neighbors = {
                pos.north(),
                pos.south(),
                pos.east(),
                pos.west()
        };

        BlockPos target = neighbors[RANDOM.nextInt(neighbors.length)];
        BlockState neighborState = world.getBlockState(target);

        if (!(neighborState.getBlock() instanceof CropBlock neighborCrop)) {
            return;
        }

        IntProperty neighborAge =
                CropDecayManager.getAgeProperty(neighborState);

        if (neighborAge == null
                || neighborState.get(neighborAge) >= neighborCrop.getMaxAge()) {
            return;
        }

        growCrop(world, target, processed);
    }
}