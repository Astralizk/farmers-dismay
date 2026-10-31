
package net.eviz.farmersdismay.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class Digger extends Item {

    public Digger(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(
            World world,
            PlayerEntity player,
            Hand hand
    ) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {

            if (DiggerManager.isDigging(player)) {
                DiggerManager.stop(serverPlayer);
            }

            else if (player.getPitch() > 45) {
                DiggerManager.start(serverPlayer);
            }
        }

        return TypedActionResult.success(stack, world.isClient);
    }
}
