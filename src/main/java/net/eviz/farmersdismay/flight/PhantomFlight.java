
package net.eviz.farmersdismay.flight;

import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public final class PhantomFlight {

    private PhantomFlight() {
    }

    public static void register() {

        EntityElytraEvents.CUSTOM.register((LivingEntity entity, boolean tickElytra) -> {
            if (entity instanceof PlayerEntity player) {
                return ((PhantomFlightAccess) player).farmersDismay$isPhantomFlying();
            }

            return false;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tick(player);
            }
        });
    }

    public static boolean activate(PlayerEntity player) {
        PhantomFlightAccess access = (PhantomFlightAccess) player;

        if (access.farmersDismay$isPhantomFlying()) {
            return false;
        }

        access.farmersDismay$setPhantomFlying(true);

        Vec3d velocity = player.getVelocity();

        player.setVelocity(velocity.x, 2.2, velocity.z);
        player.velocityModified = true;

        if (player.getWorld() instanceof ServerWorld world) {

            double x = player.getX();
            double y = player.getY() + 1.0;
            double z = player.getZ();

            world.spawnParticles(
                    ParticleTypes.FLASH,
                    x, y, z,
                    1,
                    0, 0, 0,
                    0
            );


            world.spawnParticles(
                    ParticleTypes.SOUL,
                    x, y, z,
                    35,
                    0.6, 0.8, 0.6,
                    0.12
            );

            world.spawnParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    x, y, z,
                    50,
                    0.7, 0.8, 0.7,
                    0.15
            );

            world.playSound(
                    null,
                    player.getBlockPos(),
                    SoundEvents.ENTITY_PHANTOM_FLAP,
                    SoundCategory.PLAYERS,
                    1.0f,
                    0.7f
            );
        }

        return true;
    }

    private static void tick(ServerPlayerEntity player) {
        PhantomFlightAccess access = (PhantomFlightAccess) player;

        if (!access.farmersDismay$isPhantomFlying()) {
            return;
        }

        if (player.isOnGround() || player.isTouchingWater()) {
            player.stopFallFlying();
            access.farmersDismay$setPhantomFlying(false);
            return;
        }

        if (!player.isFallFlying() && player.getVelocity().y <= 0.0) {
            player.startFallFlying();
        }

        ServerWorld world = player.getServerWorld();

        world.spawnParticles(
                ParticleTypes.SOUL,
                player.getX(),
                player.getY() + 0.8,
                player.getZ(),
                2,
                0.25, 0.35, 0.25,
                0.02
        );

        world.spawnParticles(
                ParticleTypes.REVERSE_PORTAL,
                player.getX(),
                player.getY() + 0.8,
                player.getZ(),
                2,
                0.3, 0.4, 0.3,
                0.05
        );
    }
}
