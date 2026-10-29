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

public final class SugarRushGrowthManager {

    private static final long GROWTH_INTERVAL = 40L;
    private static final long BOOST_DURATION = 200L;
    private static final long COOLDOWN_DURATION = 400L;
    private static final String STATE_ID = "farmers_dismay_sugar_rush";

    private static final PersistentState.Type<SugarData> STATE_TYPE =
            new PersistentState.Type<>(
                    SugarData::new,
                    SugarData::fromNbt,
                    null
            );

    private SugarRushGrowthManager() {
    }

    private static SugarData getData(ServerWorld world) {
        return world.getPersistentStateManager()
                .getOrCreate(STATE_TYPE, STATE_ID);
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            SugarData data = getData(world);
            long now = world.getTime();

            if (data.crops.isEmpty()) {
                return;
            }

            Iterator<Map.Entry<BlockPos, SugarState>> iterator =
                    data.crops.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<BlockPos, SugarState> entry = iterator.next();
                BlockPos pos = entry.getKey();
                SugarState sugar = entry.getValue();

                if (!world.isChunkLoaded(pos)) {
                    continue;
                }

                BlockState state = world.getBlockState(pos);

                if (!(state.getBlock() instanceof CropBlock crop)) {
                    iterator.remove();
                    data.markDirty();
                    continue;
                }

                IntProperty ageProperty =
                        CropDecayManager.getAgeProperty(state);

                if (ageProperty == null
                        || state.get(ageProperty) >= crop.getMaxAge()) {
                    iterator.remove();
                    data.markDirty();
                    continue;
                }

                if (now >= sugar.phaseEnds) {
                    sugar.coolingDown = !sugar.coolingDown;

                    long duration = sugar.coolingDown
                            ? COOLDOWN_DURATION
                            : BOOST_DURATION;

                    sugar.phaseEnds = now + duration;

                    if (!sugar.coolingDown) {
                        sugar.nextGrowth = now + GROWTH_INTERVAL;
                    }

                    data.markDirty();
                }

                if (sugar.coolingDown || now < sugar.nextGrowth) {
                    continue;
                }

                int age = state.get(ageProperty);
                int maxAge = crop.getMaxAge();

                world.setBlockState(pos, state.with(ageProperty, age + 1));

                sugar.nextGrowth = now + GROWTH_INTERVAL;
                data.markDirty();

                if (age + 1 >= maxAge) {
                    iterator.remove();
                    data.markDirty();
                }
            }
        });
    }

    public static void fertilize(ServerWorld world, BlockPos pos) {
        SugarData data = getData(world);
        long now = world.getTime();

        data.crops.put(
                pos.toImmutable(),
                new SugarState(
                        now + GROWTH_INTERVAL,
                        now + BOOST_DURATION,
                        false
                )
        );

        data.markDirty();
    }

    private static class SugarState {
        private long nextGrowth;
        private long phaseEnds;
        private boolean coolingDown;

        private SugarState(
                long nextGrowth,
                long phaseEnds,
                boolean coolingDown
        ) {
            this.nextGrowth = nextGrowth;
            this.phaseEnds = phaseEnds;
            this.coolingDown = coolingDown;
        }
    }

    private static class SugarData extends PersistentState {
        private final Map<BlockPos, SugarState> crops = new HashMap<>();

        private SugarData() {
        }

        private static SugarData fromNbt(
                NbtCompound nbt,
                RegistryWrapper.WrapperLookup registries
        ) {
            SugarData data = new SugarData();
            NbtList list = nbt.getList("Crops", NbtList.COMPOUND_TYPE);

            for (int i = 0; i < list.size(); i++) {
                NbtCompound entry = list.getCompound(i);

                BlockPos pos = BlockPos.fromLong(entry.getLong("Pos"));

                SugarState state = new SugarState(
                        entry.getLong("NextGrowth"),
                        entry.getLong("PhaseEnds"),
                        entry.getBoolean("CoolingDown")
                );

                data.crops.put(pos, state);
            }

            return data;
        }

        @Override
        public NbtCompound writeNbt(
                NbtCompound nbt,
                RegistryWrapper.WrapperLookup registries
        ) {
            NbtList list = new NbtList();

            for (Map.Entry<BlockPos, SugarState> entry : crops.entrySet()) {
                NbtCompound crop = new NbtCompound();
                SugarState state = entry.getValue();

                crop.putLong("Pos", entry.getKey().asLong());
                crop.putLong("NextGrowth", state.nextGrowth);
                crop.putLong("PhaseEnds", state.phaseEnds);
                crop.putBoolean("CoolingDown", state.coolingDown);

                list.add(crop);
            }

            nbt.put("Crops", list);
            return nbt;
        }
    }
}