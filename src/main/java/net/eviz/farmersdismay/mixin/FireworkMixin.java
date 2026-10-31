
package net.eviz.farmersdismay.mixin;

import net.eviz.farmersdismay.flight.PhantomFlightAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public abstract class FireworkMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void farmersDismay$disableRocketBoost(
            World world,
            PlayerEntity user,
            Hand hand,
            CallbackInfoReturnable<TypedActionResult<ItemStack>> cir
    ) {
        PhantomFlightAccess access = (PhantomFlightAccess) user;

        if (access.farmersDismay$isPhantomFlying()) {
            cir.setReturnValue(
                    TypedActionResult.fail(user.getStackInHand(hand))
            );
        }
    }
}
