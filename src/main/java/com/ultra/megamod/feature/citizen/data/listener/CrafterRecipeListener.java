package com.ultra.megamod.feature.citizen.data.listener;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * SimpleJsonResourceReloadListener loading crafter recipes from
 * {@code data/megamod/crafterrecipes/}. Stores recipes per crafter type
 * (baker, blacksmith, etc.). Supports recipe templates.
 *
 * <h2>JSON Format</h2>
 * <pre>
 * {
 *   "crafter": "baker",
 *   "recipes": [
 *     {
 *       "output": "minecraft:bread",
 *       "outputCount": 1,
 *       "inputs": [
 *         { "item": "minecraft:wheat", "count": 3 }
 *       ],
 *       "craftTime": 200,
 *       "minLevel": 1
 *     }
 *   ]
 * }
 * </pre>
 *
 * Registration: add via {@code AddReloadListenerEvent}:
 * <pre>
 *   event.addListener(new CrafterRecipeListener());
 * </pre>
 */
public class CrafterRecipeListener extends SimpleJsonResourceReloadListener<JsonElement> {

    private static final String DIRECTORY = "crafterrecipes";

    /** Singleton instance populated on reload */
    public static final CrafterRecipeListener INSTANCE = new CrafterRecipeListener();

    // crafterType -> list of recipes
    private final Map<String, List<CrafterRecipe>> recipesByCrafter = new LinkedHashMap<>();

    public CrafterRecipeListener() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(DIRECTORY));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        recipesByCrafter.clear();

        int totalRecipes = 0;
        for (Map.Entry<Identifier, JsonElement> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            JsonElement element = entry.getValue();
            if (!element.isJsonObject()) continue;
            JsonObject json = element.getAsJsonObject();

            String crafter = getStringOr(json, "crafter", "unknown").toLowerCase(Locale.ROOT);

            if (json.has("recipes") && json.get("recipes").isJsonArray()) {
                List<CrafterRecipe> recipes = recipesByCrafter.computeIfAbsent(crafter, k -> new ArrayList<>());
                for (JsonElement recipeEl : json.getAsJsonArray("recipes")) {
                    if (!recipeEl.isJsonObject()) continue;
                    try {
                        CrafterRecipe recipe = parseRecipe(id, recipeEl.getAsJsonObject());
                        recipes.add(recipe);
                        totalRecipes++;
                    } catch (Exception e) {
                        MegaMod.LOGGER.warn("Failed to parse crafter recipe in {}: {}", id, e.getMessage());
                    }
                }
            }
        }

        MegaMod.LOGGER.info("Loaded {} crafter recipes across {} crafter types", totalRecipes, recipesByCrafter.size());
    }

    private CrafterRecipe parseRecipe(Identifier fileId, JsonObject json) {
        // Output
        Identifier outputId = Identifier.tryParse(getStringOr(json, "output", "minecraft:air"));
        Item outputItem = outputId != null ? BuiltInRegistries.ITEM.getValue(outputId) : Items.AIR;
        int outputCount = getIntOr(json, "outputCount", 1);
        ItemStack output = new ItemStack(outputItem, outputCount);

        // Inputs
        List<ItemStack> inputs = new ArrayList<>();
        if (json.has("inputs") && json.get("inputs").isJsonArray()) {
            for (JsonElement inputEl : json.getAsJsonArray("inputs")) {
                if (!inputEl.isJsonObject()) continue;
                JsonObject inputJson = inputEl.getAsJsonObject();
                Identifier inputId = Identifier.tryParse(getStringOr(inputJson, "item", "minecraft:air"));
                Item inputItem = inputId != null ? BuiltInRegistries.ITEM.getValue(inputId) : Items.AIR;
                int inputCount = getIntOr(inputJson, "count", 1);
                inputs.add(new ItemStack(inputItem, inputCount));
            }
        }

        int craftTime = getIntOr(json, "craftTime", 200);
        int minLevel = getIntOr(json, "minLevel", 1);
        String template = getStringOr(json, "template", "");

        return new CrafterRecipe(output, inputs, craftTime, minLevel, template);
    }

    /**
     * Get all recipes for a given crafter type.
     */
    public List<CrafterRecipe> getRecipesForCrafter(String crafterType) {
        return recipesByCrafter.getOrDefault(crafterType.toLowerCase(Locale.ROOT), Collections.emptyList());
    }

    /**
     * Get recipes for a crafter type filtered by minimum level.
     */
    public List<CrafterRecipe> getRecipesForCrafter(String crafterType, int crafterLevel) {
        List<CrafterRecipe> all = getRecipesForCrafter(crafterType);
        List<CrafterRecipe> filtered = new ArrayList<>();
        for (CrafterRecipe recipe : all) {
            if (recipe.minLevel() <= crafterLevel) {
                filtered.add(recipe);
            }
        }
        return filtered;
    }

    /**
     * Get all registered crafter types.
     */
    public Set<String> getCrafterTypes() {
        return Collections.unmodifiableSet(recipesByCrafter.keySet());
    }

    /**
     * A crafter recipe definition.
     */
    public record CrafterRecipe(
            ItemStack output,
            List<ItemStack> inputs,
            int craftTime,
            int minLevel,
            String template
    ) {
        /**
         * Check if a template is defined for this recipe.
         */
        public boolean hasTemplate() {
            return template != null && !template.isEmpty();
        }
    }

    // --- JSON helpers ---

    private static String getStringOr(JsonObject json, String key, String defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) return json.get(key).getAsString();
        return defaultValue;
    }

    private static int getIntOr(JsonObject json, String key, int defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) return json.get(key).getAsInt();
        return defaultValue;
    }
}
