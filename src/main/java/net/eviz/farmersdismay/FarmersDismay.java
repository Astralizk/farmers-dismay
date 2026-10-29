
package net.eviz.farmersdismay;

import net.eviz.farmersdismay.block.ModBlocks;
import net.eviz.farmersdismay.entity.renderer.ModBlockEntities;
import net.eviz.farmersdismay.item.ModItemGroups;
import net.eviz.farmersdismay.item.ModItems;
import net.eviz.farmersdismay.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.eviz.farmersdismay.recipe.DecomposterRecipes;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FarmersDismay implements ModInitializer {
	public static final String MOD_ID = "farmersdismay";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		ModScreenHandlers.registerScreenHandlers();
		DecomposterRecipes.register();
	}
}
