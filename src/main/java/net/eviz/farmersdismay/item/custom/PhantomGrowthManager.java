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

public final class PhantomGrowthManager {

    private static final long NIGHT_GROWTH_INTERVAL = 100L;
    private static final long FULL_MOON_INTERVAL = 40L;
    private static final String STATE_ID = "farmers_dismay_phantom_crops";

    private static final PersistentState.Type<PhantomData> STATE_TYPE =
            new PersistentState.Type<>(
                    PhantomData::new,
                    PhantomData::fromNbt,
                    null
            );

    private PhantomGrowthManager() {
    }

    private static PhantomData getData(ServerWorld world) {
        return world.getPersistentStateManager()
                .getOrCreate(STATE_TYPE, STATE_ID);
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            PhantomData data = getData(world);
            long now = world.getTime();

            if (data.crops.isEmpty()) {
                return;
            }

            long timeOfDay = world.getTimeOfDay() % 24000L;
            boolean night = timeOfDay >= 13000L && timeOfDay < 23000L;
            boolean fullMoon = world.getMoonPhase() == 0;

            Iterator<Map.Entry<BlockPos, Long>> iterator =
                    data.crops.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<BlockPos, Long> entry = iterator.next();
                BlockPos pos = entry.getKey();
                long nextGrowth = entry.getValue();

                if (!world.isChunkLoaded(pos)) {
                    continue;
                }

                BlockState state = world.getBlockState(pos);

                if (!(state.getBlock() instanceof CropBlock crop)) {
                    iterator.remove();
                    data.markDirty();
                    continue;
                }

                IntProperty ageProperty = CropDecayManager.getAgeProperty(state);

                if (ageProperty == null
                        || state.get(ageProperty) >= crop.getMaxAge()) {
                    iterator.remove();
                    data.markDirty();
                    continue;
                }

                if (!night || now < nextGrowth) {
                    continue;
                }

                int age = state.get(ageProperty);
                int maxAge = crop.getMaxAge();

                world.setBlockState(pos, state.with(ageProperty, age + 1));

                long interval = fullMoon
                        ? FULL_MOON_INTERVAL
                        : NIGHT_GROWTH_INTERVAL;

                if (age + 1 >= maxAge) {
                    iterator.remove();
                } else {
                    entry.setValue(now + interval);
                }

                data.markDirty();
            }
        });
    }

    public static void fertilize(ServerWorld world, BlockPos pos) {
        PhantomData data = getData(world);

        data.crops.put(
                pos.toImmutable(),
                world.getTime() + NIGHT_GROWTH_INTERVAL
        );

        data.markDirty();
    }

    private static class PhantomData extends PersistentState {

        private final Map<BlockPos, Long> crops = new HashMap<>();

        private PhantomData() {
        }

        private static PhantomData fromNbt(
                NbtCompound nbt,
                RegistryWrapper.WrapperLookup registries
        ) {
            PhantomData data = new PhantomData();
            NbtList list = nbt.getList("Crops", NbtList.COMPOUND_TYPE);

            for (int i = 0; i < list.size(); i++) {
                NbtCompound entry = list.getCompound(i);

                BlockPos pos = BlockPos.fromLong(entry.getLong("Pos"));
                long nextGrowth = entry.getLong("NextGrowth");

                data.crops.put(pos, nextGrowth);
            }

            return data;
        }

        @Override
        public NbtCompound writeNbt(
                NbtCompound nbt,
                RegistryWrapper.WrapperLookup registries
        ) {
            NbtList list = new NbtList();

            for (Map.Entry<BlockPos, Long> entry : crops.entrySet()) {
                NbtCompound crop = new NbtCompound();

                crop.putLong("Pos", entry.getKey().asLong());
                crop.putLong("NextGrowth", entry.getValue());

                list.add(crop);
            }

            nbt.put("Crops", list);
            return nbt;
        }
    }
}