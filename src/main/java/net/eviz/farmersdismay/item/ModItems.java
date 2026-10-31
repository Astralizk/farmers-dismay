package net.eviz.farmersdismay.item;

import net.eviz.farmersdismay.FarmersDismay;
import net.eviz.farmersdismay.item.custom.*;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item FARMERS_XP = registerItem("farmers_xp", new Item(new Item.Settings()));

    public static final Item CRUSHED_FARMERS_XP = registerItem("crushed_farmers_xp", new Item(new Item.Settings()));

    public static final Item PHANTOM_FEATHER = registerItem("phantom_feather", new Item(new Item.Settings()));

    public static final Item ROTTEN_COMPOST = registerItem("rotten_compost", new Item(new Item.Settings()));

    public static final Item SACCHAROSE = registerItem("saccharose", new Item(new Item.Settings()));

    public static final Item SCULK_DUST = registerItem("sculk_dust", new Item(new Item.Settings()));

    public static final Item POCKET_WATERER = registerItem("pocket_waterer", new PocketWaterer(new Item.Settings().maxCount(1).maxDamage(6).component(net.minecraft.component.DataComponentTypes.DAMAGE, 5)));

    public static final Item BI_SICKLE = registerItem("bi_sickle", new BiSickle(new Item.Settings().maxCount(1)));

    public static final Item BONEFEAST = registerItem("bonefeast", new Bonefeast(new Item.Settings().maxCount(64)));

    public static final Item ROT_FERTILIZER = registerItem("rot_fertilizer", new RotFertilizer(new Item.Settings().maxCount(64)));

    public static final Item SCULK_FERTILIZER = registerItem("sculk_fertilizer", new SculkFertilizer(new Item.Settings().maxCount(64)));

    public static final Item PHANTOM_FERTILIZER = registerItem("phantom_fertilizer", new PhantomFertilizer(new Item.Settings().maxCount(64)));

    public static final Item SUGAR_FERTILIZER = registerItem("sugar_fertilizer", new SugarFertilizer(new Item.Settings().maxCount(64)));

    public static final Item FERTILIZER = registerItem("fertilizer", new Fertilizer(new Item.Settings().maxDamage(16)));

    public static final Item PHANTOM_WINGS = registerItem("phantom_wings", new PhantomWings(new Item.Settings().maxCount(16)));

    public static final Item UNI_SICKLE = registerItem("uni_sickle", new UniSickle(new Item.Settings().maxCount(1)));

    public static final Item RE_SICKLER = registerItem("re_sickler", new ReSickler(new Item.Settings().maxCount(1)));

    public static final Item DIGGER = registerItem("digger", new Digger(new Item.Settings().maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(FarmersDismay.MOD_ID, name), item );
    }
    public static void registerModItems() {
        FarmersDismay.LOGGER.info("Registering Mod Items for " + FarmersDismay.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(FARMERS_XP);
            entries.add(CRUSHED_FARMERS_XP);
            entries.add(PHANTOM_FEATHER);
            entries.add(ROTTEN_COMPOST);
            entries.add(SCULK_DUST);
            entries.add(SACCHAROSE);

        });
    }
}
