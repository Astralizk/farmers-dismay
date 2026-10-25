package net.eviz.farmersdismay.item;

import net.eviz.farmersdismay.FarmersDismay;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item FARMERS_XP = registerItem("farmers_xp", new Item(new Item.Settings()));
    public static final Item CRUSHED_FARMERS_XP = registerItem("crushed_farmers_xp", new Item(new Item.Settings()));
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(FarmersDismay.MOD_ID,name), item );
    }
    public static void registerModItems() {
        FarmersDismay.LOGGER.info("Registering Mod Items for " + FarmersDismay.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(FARMERS_XP);
            entries.add(CRUSHED_FARMERS_XP);

        });
    }
}
