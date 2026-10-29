package net.eviz.farmersdismay;

import net.fabricmc.api.ClientModInitializer;
import net.eviz.farmersdismay.screen.DecomposterScreen;
import net.eviz.farmersdismay.screen.ModScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class FarmersDismayClient implements ClientModInitializer{
    @Override
    public void onInitializeClient() {

        HandledScreens.register(
                ModScreenHandlers.DECOMPOSTER,
                DecomposterScreen::new
        );

    }
}