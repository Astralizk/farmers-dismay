
package net.eviz.farmersdismay.mixin;

import net.eviz.farmersdismay.flight.PhantomFlightAccess;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements PhantomFlightAccess {

    @Unique
    private static final TrackedData<Boolean> PHANTOM_FLYING =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void farmersDismay$initDataTracker(
            DataTracker.Builder builder,
            CallbackInfo ci
    ) {
        builder.add(PHANTOM_FLYING, false);
    }

    @Override
    public boolean farmersDismay$isPhantomFlying() {
        PlayerEntity player = (PlayerEntity) (Object) this;
        return player.getDataTracker().get(PHANTOM_FLYING);
    }

    @Override
    public void farmersDismay$setPhantomFlying(boolean flying) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        player.getDataTracker().set(PHANTOM_FLYING, flying);
    }
}
