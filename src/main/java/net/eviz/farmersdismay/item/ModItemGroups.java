package net.eviz.farmersdismay.item;

import net.eviz.farmersdismay.FarmersDismay;
import net.eviz.farmersdismay.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup FARMERS_DISMAY_ITEM_GROUPS = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(FarmersDismay.MOD_ID, "farmers_dismay_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.FARMERS_XP))
                    .displayName(Text.translatable("itemgroup.farmersdismay.farmers_dismay_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.CRUSHED_FARMERS_XP);
                        entries.add(ModItems.FARMERS_XP);
                        entries.add(ModItems.POCKET_WATER);
                        entries.add(ModItems.BONEFEAST);
                        entries.add(ModBlocks.DECOMPOSTER);
                        entries.add(ModItems.ROT_FERTILIZER);
                        entries.add(ModItems.SCULK_FERTILIZER);
                        entries.add(ModItems.PHANTOM_FERTILIZER);
                        entries.add(ModItems.SUGAR_FERTILIZER);
                        entries.add(ModItems.FERTILIZER);
                        entries.add(ModItems.SACCHAROSE);
                        entries.add(ModItems.SCULK_DUST);
                        entries.add(ModItems.ROTTEN_COMPOST);
                        entries.add(ModItems.PHANTOM_FEATHER);


                    }).build());



    public static void registerItemGroups() {
        FarmersDismay.LOGGER.info("Registering Item Groups for " + FarmersDismay.MOD_ID);
    }
}
