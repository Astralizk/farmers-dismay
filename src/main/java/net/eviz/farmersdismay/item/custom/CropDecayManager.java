package net.eviz.farmersdismay.item.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class CropDecayManager {

    private static final long GROWTH_INTERVAL = 200L;
    private static final long DECAY_TIME = 1200L;
    private static final String STATE_ID = "farmers_dismay_rot_crops";

    private static final PersistentState.Type<RotData> STATE_TYPE =
            new PersistentState.Type<>(
                    RotData::new,
                    RotData::fromNbt,
                    null
            );

    private CropDecayManager() {
    }

    private static RotData getData(ServerWorld world) {
        return world.getPersistentStateManager()
                .getOrCreate(STATE_TYPE, STATE_ID);
    }

    public static IntProperty getAgeProperty(BlockState state) {
        for (var property : state.getProperties()) {
            if (property instanceof IntProperty intProperty
                    && property.getName().equals("age")) {
                return intProperty;
            }
        }

        return null;
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            RotData data = getData(world);
            long now = world.getTime();

            if (data.crops.isEmpty()) {
                return;
            }

            Iterator<Map.Entry<BlockPos, CropData>> iterator =
                    data.crops.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<BlockPos, CropData> entry = iterator.next();
                BlockPos pos = entry.getKey();
                CropData cropData = entry.getValue();

                if (!world.isChunkLoaded(pos)) {
                    continue;
                }

                BlockState state = world.getBlockState(pos);

                if (!(state.getBlock() instanceof CropBlock crop)) {
                    iterator.remove();
                    data.markDirty();
                    continue;
                }

                IntProperty ageProperty = getAgeProperty(state);

                if (ageProperty == null) {
                    iterator.remove();
                    data.markDirty();
                    continue;
                }

                int age = state.get(ageProperty);

                if (age >= crop.getMaxAge() && cropData.decayAt == 0L) {
                    cropData.decayAt = now + DECAY_TIME;
                    data.markDirty();
                }

                if (cropData.decayAt > 0L) {
                    if (now >= cropData.decayAt) {
                        world.breakBlock(pos, false);
                        iterator.remove();
                        data.markDirty();
                    }

                    continue;
                }

                if (now < cropData.nextGrowth) {
                    continue;
                }

                BlockState currentState = world.getBlockState(pos);

                if (!(currentState.getBlock() instanceof CropBlock currentCrop)) {
                    iterator.remove();
                    data.markDirty();
                    continue;
                }

                IntProperty currentAgeProperty = getAgeProperty(currentState);

                if (currentAgeProperty == null) {
                    iterator.remove();
                    data.markDirty();
                    continue;
                }

                int currentAge = currentState.get(currentAgeProperty);

                if (currentAge < currentCrop.getMaxAge()) {
                    world.setBlockState(
                            pos,
                            currentState.with(currentAgeProperty, currentAge + 1)
                    );

                    if (currentAge + 1 >= currentCrop.getMaxAge()) {
                        cropData.decayAt = now + DECAY_TIME;
                    }
                }

                cropData.nextGrowth = now + GROWTH_INTERVAL;
                data.markDirty();
            }
        });
    }

    public static void markFertilized(ServerWorld world, BlockPos pos) {
        RotData data = getData(world);

        data.crops.put(
                pos.toImmutable(),
                new CropData(world.getTime() + GROWTH_INTERVAL, 0L)
        );

        data.markDirty();
    }

    private static class CropData {
        private long nextGrowth;
        private long decayAt;

        private CropData(long nextGrowth, long decayAt) {
            this.nextGrowth = nextGrowth;
            this.decayAt = decayAt;
        }
    }

    private static class RotData extends PersistentState {
        private final Map<BlockPos, CropData> crops = new HashMap<>();

        private RotData() {
        }

        private static RotData fromNbt(
                NbtCompound nbt,
                RegistryWrapper.WrapperLookup registries
        ) {
            RotData data = new RotData();
            NbtList list = nbt.getList("Crops", NbtList.COMPOUND_TYPE);

            for (int i = 0; i < list.size(); i++) {
                NbtCompound entry = list.getCompound(i);

                BlockPos pos = BlockPos.fromLong(entry.getLong("Pos"));

                CropData cropData = new CropData(
                        entry.getLong("NextGrowth"),
                        entry.getLong("DecayAt")
                );

                data.crops.put(pos, cropData);
            }

            return data;
        }

        @Override
        public NbtCompound writeNbt(
                NbtCompound nbt,
                RegistryWrapper.WrapperLookup registries
        ) {
            NbtList list = new NbtList();

            for (Map.Entry<BlockPos, CropData> entry : crops.entrySet()) {
                NbtCompound crop = new NbtCompound();

                crop.putLong("Pos", entry.getKey().asLong());
                crop.putLong("NextGrowth", entry.getValue().nextGrowth);
                crop.putLong("DecayAt", entry.getValue().decayAt);

                list.add(crop);
            }

            nbt.put("Crops", list);
            return nbt;
        }
    }
}