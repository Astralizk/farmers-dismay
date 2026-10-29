
package net.eviz.farmersdismay.item.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FertilizerGrowthManager {

    private static final long GROWTH_INTERVAL = 1000L;

    private static final Map<ServerWorld, Map<BlockPos, Long>> CROPS =
            new HashMap<>();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            Map<BlockPos, Long> crops = CROPS.get(world);

            if (crops == null || crops.isEmpty()) {
                return;
            }

            long time = world.getTime();
            Iterator<Map.Entry<BlockPos, Long>> iterator =
                    crops.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<BlockPos, Long> entry = iterator.next();
                BlockPos pos = entry.getKey();

                if (!world.isChunkLoaded(pos)) {
                    continue;
                }

                BlockState state = world.getBlockState(pos);

                if (!(state.getBlock() instanceof CropBlock crop)
                        || crop.isMature(state)) {
                    iterator.remove();
                    continue;
                }

                if (time >= entry.getValue()) {
                    world.setBlockState(pos, crop.withAge(crop.getAge(state) + 1));

                    BlockState updated = world.getBlockState(pos);

                    if (updated.getBlock() instanceof CropBlock updatedCrop
                            && !updatedCrop.isMature(updated)) {
                        entry.setValue(time + GROWTH_INTERVAL);
                    } else {
                        iterator.remove();
                    }
                }
            }
        });
    }

    public static void fertilize(World world, BlockPos pos) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        CROPS.computeIfAbsent(serverWorld, w -> new HashMap<>())
                .put(pos.toImmutable(), serverWorld.getTime() + GROWTH_INTERVAL);
    }
}
