package com.ultra.megamod.feature.alchemy;

import java.util.*;

/**
 * Static registry of all alchemy recipes: grinding and brewing.
 */
public class AlchemyRecipeRegistry {

    // ==================== Grinding Recipes ====================

    public record GrindingRecipe(String id, List<String> inputs, String output, int outputCount, int grindTicks) {}

    private static final List<GrindingRecipe> GRINDING_RECIPES = new ArrayList<>();
    private static final Map<String, GrindingRecipe> GRINDING_BY_INPUT = new LinkedHashMap<>();

    // ==================== Brewing Recipes ====================

    public record BrewingRecipe(String id, Set<String> reagents, List<String> reagentList, String output, int tier) {}

    private static final List<BrewingRecipe> BREWING_RECIPES = new ArrayList<>();
    private static final Map<String, BrewingRecipe> BREWING_BY_KEY = new LinkedHashMap<>();
    private static final Map<String, BrewingRecipe> BREWING_BY_OUTPUT = new LinkedHashMap<>();

    static {
        // Grinding recipes: (id, inputs, output, count, ticks)
        addGrinding("grind_blaze_powder", List.of("minecraft:blaze_powder"), "megamod:reagent_ember_dust", 2, 100);
        addGrinding("grind_blue_ice", List.of("minecraft:blue_ice"), "megamod:reagent_frost_crystal", 1, 100);
        addGrinding("grind_packed_ice", List.of("minecraft:packed_ice"), "megamod:reagent_frost_crystal", 1, 100);
        addGrinding("grind_ink_sac_coal", List.of("minecraft:ink_sac", "minecraft:coal"), "megamod:reagent_shadow_essence", 2, 120);
        addGrinding("grind_golden_apple", List.of("minecraft:golden_apple"), "megamod:reagent_life_bloom", 1, 100);
        addGrinding("grind_glistering_melon", List.of("minecraft:glistering_melon_slice"), "megamod:reagent_life_bloom", 2, 100);
        addGrinding("grind_ender_pearl", List.of("minecraft:ender_pearl"), "megamod:reagent_void_salt", 1, 100);
        addGrinding("grind_chorus_fruit", List.of("minecraft:chorus_fruit"), "megamod:reagent_void_salt", 1, 100);
        addGrinding("grind_gunpowder", List.of("minecraft:gunpowder"), "megamod:reagent_storm_charge", 2, 100);
        addGrinding("grind_spider_eye", List.of("minecraft:spider_eye"), "megamod:reagent_blood_moss", 2, 100);
        addGrinding("grind_red_mushroom", List.of("minecraft:red_mushroom"), "megamod:reagent_blood_moss", 1, 100);
        addGrinding("grind_glowstone_dust", List.of("minecraft:glowstone_dust"), "megamod:reagent_starlight_dew", 2, 100);
        addGrinding("grind_amethyst_shard", List.of("minecraft:amethyst_shard"), "megamod:reagent_starlight_dew", 1, 100);
        addGrinding("grind_clay_ball", List.of("minecraft:clay_ball"), "megamod:reagent_earth_root", 2, 100);
        addGrinding("grind_lapis", List.of("minecraft:lapis_lazuli"), "megamod:reagent_arcane_flux", 1, 100);
        addGrinding("grind_redstone", List.of("minecraft:redstone"), "megamod:reagent_arcane_flux", 1, 100);
        addGrinding("grind_xp_bottle", List.of("minecraft:experience_bottle"), "megamod:reagent_arcane_flux", 3, 120);

        // Brewing recipes: (id, reagentSet, output, tier)
        addBrewing("brew_inferno", List.of("ember_dust", "ember_dust", "life_bloom"), "megamod:potion_inferno", 2);
        addBrewing("brew_glacier", List.of("frost_crystal", "frost_crystal", "storm_charge"), "megamod:potion_glacier", 2);
        addBrewing("brew_shadow_step", List.of("shadow_essence", "shadow_essence", "void_salt"), "megamod:potion_shadow_step", 3);
        addBrewing("brew_vitality", List.of("life_bloom", "life_bloom", "earth_root"), "megamod:potion_vitality", 2);
        addBrewing("brew_void_walk", List.of("void_salt", "void_salt", "starlight_dew"), "megamod:potion_void_walk", 4);
        addBrewing("brew_tempest", List.of("storm_charge", "storm_charge", "ember_dust"), "megamod:potion_tempest", 3);
        addBrewing("brew_berserker", List.of("blood_moss", "blood_moss", "ember_dust"), "megamod:potion_berserker", 3);
        addBrewing("brew_starlight", List.of("starlight_dew", "starlight_dew", "arcane_flux"), "megamod:potion_starlight", 3);
        addBrewing("brew_stone_skin", List.of("earth_root", "earth_root", "frost_crystal"), "megamod:potion_stone_skin", 1);
        addBrewing("brew_arcane_surge", List.of("arcane_flux", "arcane_flux", "starlight_dew"), "megamod:potion_arcane_surge", 4);
        addBrewing("brew_swiftbrew", List.of("storm_charge", "earth_root", "life_bloom"), "megamod:potion_swiftbrew", 1);
        addBrewing("brew_iron_gut", List.of("earth_root", "life_bloom", "blood_moss"), "megamod:potion_iron_gut", 1);
        addBrewing("brew_midas_touch", List.of("arcane_flux", "ember_dust", "starlight_dew"), "megamod:potion_midas_touch", 4);
        addBrewing("brew_eagle_eye", List.of("frost_crystal", "storm_charge", "void_salt"), "megamod:potion_eagle_eye", 4);
        addBrewing("brew_undying", List.of("life_bloom", "void_salt", "arcane_flux"), "megamod:potion_undying", 5);
        addBrewing("brew_phantom", List.of("frost_crystal", "shadow_essence", "void_salt"), "megamod:potion_phantom", 4);
        addBrewing("brew_titan", List.of("blood_moss", "earth_root", "storm_charge"), "megamod:potion_titan", 3);
        addBrewing("brew_tidal_wave", List.of("earth_root", "frost_crystal", "life_bloom"), "megamod:potion_tidal_wave", 2);
        addBrewing("brew_chronos", List.of("arcane_flux", "shadow_essence", "starlight_dew"), "megamod:potion_chronos", 5);
        addBrewing("brew_blood_rage", List.of("blood_moss", "ember_dust", "shadow_essence"), "megamod:potion_blood_rage", 4);

        // Spell Power Potions
        addBrewing("brew_spell_arcane_surge", List.of("arcane_flux", "arcane_flux", "arcane_flux"), "megamod:potion_spell_arcane_surge", 4);
        addBrewing("brew_fire_attunement", List.of("ember_dust", "ember_dust", "arcane_flux"), "megamod:potion_fire_attunement", 3);
        addBrewing("brew_frost_attunement", List.of("frost_crystal", "frost_crystal", "arcane_flux"), "megamod:potion_frost_attunement", 3);
        addBrewing("brew_healing_grace", List.of("life_bloom", "life_bloom", "starlight_dew"), "megamod:potion_healing_grace", 3);
    }

    private static void addGrinding(String id, List<String> inputs, String output, int count, int ticks) {
        GrindingRecipe recipe = new GrindingRecipe(id, inputs, output, count, ticks);
        GRINDING_RECIPES.add(recipe);
        // For single-input recipes, index by the input item
        if (inputs.size() == 1) {
            GRINDING_BY_INPUT.put(inputs.get(0), recipe);
        } else {
            // Multi-input: index by sorted concatenation
            List<String> sorted = new ArrayList<>(inputs);
            Collections.sort(sorted);
            GRINDING_BY_INPUT.put(String.join("+", sorted), recipe);
        }
    }

    private static void addBrewing(String id, List<String> reagents, String output, int tier) {
        // Normalize reagent names to full ids
        List<String> fullReagents = new ArrayList<>();
        for (String r : reagents) {
            fullReagents.add("megamod:reagent_" + r);
        }
        Set<String> reagentSet = new HashSet<>(fullReagents);
        BrewingRecipe recipe = new BrewingRecipe(id, reagentSet, fullReagents, output, tier);
        BREWING_RECIPES.add(recipe);
        // Key is sorted reagent list (preserving duplicates)
        List<String> sortedReagents = new ArrayList<>(fullReagents);
        Collections.sort(sortedReagents);
        BREWING_BY_KEY.put(String.join("+", sortedReagents), recipe);
        BREWING_BY_OUTPUT.put(output, recipe);
    }

    // ==================== Lookup Methods ====================

    /**
     * Find a grinding recipe by a single input item ID (e.g., "minecraft:blaze_powder").
     */
    public static GrindingRecipe findGrindingRecipe(String inputItemId) {
        return GRINDING_BY_INPUT.get(inputItemId);
    }

    /**
     * Find a grinding recipe by multiple input item IDs.
     */
    public static GrindingRecipe findGrindingRecipe(List<String> inputItemIds) {
        if (inputItemIds.size() == 1) {
            return GRINDING_BY_INPUT.get(inputItemIds.get(0));
        }
        List<String> sorted = new ArrayList<>(inputItemIds);
        Collections.sort(sorted);
        return GRINDING_BY_INPUT.get(String.join("+", sorted));
    }

    /**
     * Find a brewing recipe by exactly 3 reagent item IDs (unordered).
     */
    public static BrewingRecipe findBrewingRecipe(List<String> reagentIds) {
        List<String> sorted = new ArrayList<>(reagentIds);
        Collections.sort(sorted);
        return BREWING_BY_KEY.get(String.join("+", sorted));
    }

    /**
     * Get all brewing recipes.
     */
    public static List<BrewingRecipe> getAllBrewingRecipes() {
        return Collections.unmodifiableList(BREWING_RECIPES);
    }

    /**
     * Get all grinding recipes.
     */
    public static List<GrindingRecipe> getAllGrindingRecipes() {
        return Collections.unmodifiableList(GRINDING_RECIPES);
    }

    /**
     * Find a brewing recipe by its output potion ID.
     */
    public static BrewingRecipe getBrewingByOutput(String outputId) {
        return BREWING_BY_OUTPUT.get(outputId);
    }

    /**
     * Get possible brewing recipes given at least 2 of 3 reagents.
     */
    public static List<BrewingRecipe> getPossibleRecipes(List<String> currentReagents) {
        List<BrewingRecipe> possibles = new ArrayList<>();
        for (BrewingRecipe recipe : BREWING_RECIPES) {
            // Check if currentReagents is a subset of the recipe reagents (with duplicates)
            List<String> recipeCopy = new ArrayList<>(recipe.reagentList());
            boolean match = true;
            for (String r : currentReagents) {
                if (!recipeCopy.remove(r)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                possibles.add(recipe);
            }
        }
        return possibles;
    }

    /**
     * Get the display name for a reagent ID (strips prefix).
     */
    public static String getReagentDisplayName(String reagentId) {
        String name = reagentId.replace("megamod:reagent_", "");
        return capitalize(name.replace("_", " "));
    }

    /**
     * Get the display name for a potion ID.
     */
    public static String getPotionDisplayName(String potionId) {
        String name = potionId.replace("megamod:potion_", "");
        return "Potion of " + capitalize(name.replace("_", " "));
    }

    /**
     * Get the tier requirement description.
     */
    public static String getTierRequirement(int tier) {
        return switch (tier) {
            case 1 -> "Arcane Level 5+";
            case 2 -> "Arcane Level 10+";
            case 3 -> "Mana Weaver I";
            case 4 -> "Mana Weaver III";
            case 5 -> "Mana Weaver V (Capstone)";
            default -> "Unknown";
        };
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder result = new StringBuilder();
        for (String word : s.split(" ")) {
            if (!result.isEmpty()) result.append(" ");
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
