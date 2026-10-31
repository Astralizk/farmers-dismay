
package net.eviz.farmersdismay.item.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.*;

public class DiggerManager {

    private static final Map<UUID, BurrowData> ACTIVE = new HashMap<>();

    private static final double SPEED = 1.4;
    private static final double DIVE_SPEED = 0.9;
    private static final int RESTORE_DELAY = 20;

    private static class RestoreBlock {
        ServerWorld world;
        BlockPos pos;
        BlockState state;
        long restoreAt;

        RestoreBlock(ServerWorld world, BlockPos pos,
                     BlockState state, long restoreAt) {
            this.world = world;
            this.pos = pos;
            this.state = state;
            this.restoreAt = restoreAt;
        }
    }

    private static class BurrowData {
        double dx;
        double dz;
        double startY;
        long ticks = 0;
        boolean active = true;

        List<RestoreBlock> blocks = new ArrayList<>();
        Set<BlockPos> queued = new HashSet<>();

        BurrowData(double dx, double dz, double startY) {
            this.dx = dx;
            this.dz = dz;
            this.startY = startY;
        }
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(DiggerManager::tick);
    }

    public static boolean isDigging(PlayerEntity player) {
        BurrowData data = ACTIVE.get(player.getUuid());
        return data != null && data.active;
    }

    public static void start(ServerPlayerEntity player) {

        if (isDigging(player)) return;

        double yaw = Math.toRadians(player.getYaw());

        double dx = -Math.sin(yaw);
        double dz = Math.cos(yaw);

        BurrowData data = ACTIVE.get(player.getUuid());

        if (data == null) {
            data = new BurrowData(dx, dz, player.getY());
            ACTIVE.put(player.getUuid(), data);
        } else {
            data.dx = dx;
            data.dz = dz;
            data.startY = player.getY();
            data.active = true;
        }

        player.noClip = true;
        player.setNoGravity(true);
        player.setVelocity(0, 0, 0);
    }

    public static void stop(ServerPlayerEntity player) {

        BurrowData data = ACTIVE.get(player.getUuid());

        if (data != null) {
            data.active = false;
        }

        player.noClip = false;
        player.setNoGravity(false);
        player.setVelocity(0, 0, 0);
    }

    private static void tick(MinecraftServer server) {

        Iterator<Map.Entry<UUID, BurrowData>> iterator =
                ACTIVE.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<UUID, BurrowData> entry = iterator.next();

            ServerPlayerEntity player =
                    server.getPlayerManager().getPlayer(entry.getKey());

            BurrowData data = entry.getValue();

            data.ticks++;

            if (player == null || !player.isAlive()) {
                data.active = false;
            }

            if (data.active && player != null) {

                double nextX = player.getX();
                double nextY = player.getY();
                double nextZ = player.getZ();

                boolean diving = nextY > data.startY - 2.5;

                if (diving) {
                    nextY -= DIVE_SPEED;
                } else {
                    nextX += data.dx * SPEED;
                    nextZ += data.dz * SPEED;
                }

                double probeX = nextX + data.dx * 1.0;
                double probeY = nextY - (diving ? 0.8 : 0);
                double probeZ = nextZ + data.dz * 1.0;

                Box probe = playerBox(probeX, probeY, probeZ);

                if (!hasSolidBlock(player.getServerWorld(), probe)) {
                    data.active = false;
                } else {

                    double distance = Math.sqrt(
                            Math.pow(nextX - player.getX(), 2) +
                                    Math.pow(nextY - player.getY(), 2) +
                                    Math.pow(nextZ - player.getZ(), 2)
                    );

                    int steps = Math.max(1,
                            (int) Math.ceil(distance / 0.35));

                    boolean success = true;

                    for (int i = 0; i <= steps; i++) {

                        double progress = (double) i / steps;

                        double x = player.getX() +
                                (nextX - player.getX()) * progress;

                        double y = player.getY() +
                                (nextY - player.getY()) * progress;

                        double z = player.getZ() +
                                (nextZ - player.getZ()) * progress;

                        if (!digBox(
                                player.getServerWorld(),
                                playerBox(x, y, z),
                                data
                        )) {
                            success = false;
                            break;
                        }
                    }

                    if (success) {
                        player.setPosition(nextX, nextY, nextZ);
                        player.fallDistance = 0;
                    } else {
                        data.active = false;
                    }
                }
            }

            boolean pushing = restoreBlocks(data, player);

            if (player != null) {

                if (data.active) {
                    player.noClip = true;
                    player.setNoGravity(true);
                } else {
                    player.noClip = false;
                    player.setNoGravity(pushing);
                }
            }

            if (!data.active && data.blocks.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private static Box playerBox(double x, double y, double z) {

        return new Box(
                x - 0.3, y, z - 0.3,
                x + 0.3, y + 1.8, z + 0.3
        );
    }

    private static boolean hasSolidBlock(
            ServerWorld world,
            Box box
    ) {

        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);

        int maxX = (int) Math.floor(box.maxX);
        int maxY = (int) Math.floor(box.maxY);
        int maxZ = (int) Math.floor(box.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    BlockState state = world.getBlockState(
                            new BlockPos(x, y, z)
                    );

                    if (!state.isAir()) return true;
                }
            }
        }

        return false;
    }

    private static boolean digBox(
            ServerWorld world,
            Box box,
            BurrowData data
    ) {

        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);

        int maxX = (int) Math.floor(box.maxX);
        int maxY = (int) Math.floor(box.maxY);
        int maxZ = (int) Math.floor(box.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    BlockPos pos = new BlockPos(x, y, z);

                    if (!world.isInBuildLimit(pos)) return false;

                    BlockState state = world.getBlockState(pos);

                    if (state.isAir()) continue;

                    if (state.hasBlockEntity()) return false;
                    if (!state.getFluidState().isEmpty()) return false;
                    if (state.getHardness(world, pos) < 0) return false;

                    if (data.queued.contains(pos)) continue;

                    data.queued.add(pos.toImmutable());

                    data.blocks.add(new RestoreBlock(
                            world,
                            pos.toImmutable(),
                            state,
                            data.ticks + RESTORE_DELAY
                    ));

                    world.setBlockState(
                            pos,
                            Blocks.AIR.getDefaultState(),
                            Block.NOTIFY_ALL
                    );
                }
            }
        }

        return true;
    }

    private static boolean restoreBlocks(
            BurrowData data,
            ServerPlayerEntity player
    ) {

        Iterator<RestoreBlock> iterator = data.blocks.iterator();

        boolean pushing = false;

        while (iterator.hasNext()) {

            RestoreBlock block = iterator.next();

            if (data.ticks < block.restoreAt) continue;

            Box blockBox = new Box(block.pos);

            if (player != null &&
                    player.getBoundingBox().intersects(blockBox)) {

                block.restoreAt = data.ticks + 2;

                player.setVelocity(0, 0.45, 0);
                pushing = true;

                continue;
            }

            if (block.world.getBlockState(block.pos).isAir()) {

                block.world.setBlockState(
                        block.pos,
                        block.state,
                        Block.NOTIFY_ALL
                );
            }

            data.queued.remove(block.pos);
            iterator.remove();
        }

        return pushing;
    }
}
