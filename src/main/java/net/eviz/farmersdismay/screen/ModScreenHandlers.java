package net.eviz.farmersdismay.screen;

import net.eviz.farmersdismay.FarmersDismay;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {

    public static final ScreenHandlerType<DecomposterScreenHandler> DECOMPOSTER =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(FarmersDismay.MOD_ID, "decomposter"),
                    new ScreenHandlerType<>(
                            (syncId, playerInventory) ->
                                    new DecomposterScreenHandler(
                                            syncId,
                                            playerInventory,
                                            new net.minecraft.inventory.SimpleInventory(2),
                                            net.minecraft.screen.ScreenHandlerContext.EMPTY,
                                            new ArrayPropertyDelegate(2)
                                    ),
                            FeatureFlags.VANILLA_FEATURES
                    )
            );

    public static void registerScreenHandlers() {}
}