package net.eviz.farmersdismay.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.eviz.farmersdismay.FarmersDismay;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class DecomposterRecipes
        implements SimpleSynchronousResourceReloadListener {

    private static final DecomposterRecipes INSTANCE =
            new DecomposterRecipes();

    private static final Map<Item, Recipe> RECIPES = new HashMap<>();

    public record Recipe(
            Item input,
            Item output,
            int outputCount,
            int processingTime
    ) {}

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(INSTANCE);
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(FarmersDismay.MOD_ID, "decomposter_recipes");
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Item, Recipe> loaded = new HashMap<>();

        manager.findResources(
                "decomposter_recipes",
                id -> id.getPath().endsWith(".json")
        ).forEach((id, resource) -> {
            try (Reader reader = resource.getReader()) {
                JsonObject json = JsonParser.parseReader(reader)
                        .getAsJsonObject();

                Item input = Registries.ITEM.get(
                        Identifier.of(json.getAsJsonObject("ingredient")
                                .get("item").getAsString())
                );

                JsonObject result = json.getAsJsonObject("result");

                Item output = Registries.ITEM.get(
                        Identifier.of(result.get("id").getAsString())
                );

                int count = result.has("count")
                        ? result.get("count").getAsInt() : 1;

                int time = json.has("processing_time")
                        ? json.get("processing_time").getAsInt() : 100;

                if (input != Items.AIR && output != Items.AIR
                        && count > 0 && count <= 64 && time > 0) {
                    loaded.put(input, new Recipe(input, output, count, time));
                } else {
                    FarmersDismay.LOGGER.error(
                            "Invalid Decomposter recipe: " + id
                    );
                }
            } catch (Exception e) {
                FarmersDismay.LOGGER.error(
                        "Failed to load Decomposter recipe: " + id, e
                );
            }
        });

        RECIPES.clear();
        RECIPES.putAll(loaded);

        FarmersDismay.LOGGER.info(
                "Loaded " + RECIPES.size() + " Decomposter recipes"
        );
    }

    public static Recipe find(ItemStack input) {
        if (input.isEmpty()) {
            return null;
        }

        return RECIPES.get(input.getItem());
    }
}