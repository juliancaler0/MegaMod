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
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * SimpleJsonResourceReloadListener loading disease definitions from
 * {@code data/megamod/colony/diseases/}. Each disease has: name, rarity,
 * symptoms, cure items, duration.
 *
 * <h2>JSON Format</h2>
 * <pre>
 * {
 *   "name": "Flu",
 *   "rarity": 0.3,
 *   "symptoms": ["coughing", "slowness", "weakness"],
 *   "cureItems": ["minecraft:golden_apple", "minecraft:potion"],
 *   "duration": 6000
 * }
 * </pre>
 *
 * Registration: add via {@code AddReloadListenerEvent}:
 * <pre>
 *   event.addListener(new DiseaseListener());
 * </pre>
 */
public class DiseaseListener extends SimpleJsonResourceReloadListener<JsonElement> {

    private static final String DIRECTORY = "colony/diseases";

    /** Singleton instance populated on reload */
    public static final DiseaseListener INSTANCE = new DiseaseListener();

    private final Map<Identifier, DiseaseDefinition> diseases = new LinkedHashMap<>();
    private final List<DiseaseDefinition> diseaseList = new ArrayList<>();

    private static final Random RANDOM = new Random();

    public DiseaseListener() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(DIRECTORY));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        diseases.clear();
        diseaseList.clear();

        for (Map.Entry<Identifier, JsonElement> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            JsonElement element = entry.getValue();
            if (!element.isJsonObject()) continue;
            JsonObject json = element.getAsJsonObject();

            try {
                DiseaseDefinition disease = parseDisease(id, json);
                diseases.put(id, disease);
                diseaseList.add(disease);
            } catch (Exception e) {
                MegaMod.LOGGER.warn("Failed to parse disease {}: {}", id, e.getMessage());
            }
        }

        MegaMod.LOGGER.info("Loaded {} disease definitions", diseases.size());
    }

    private DiseaseDefinition parseDisease(Identifier id, JsonObject json) {
        String name = getStringOr(json, "name", capitalize(id.getPath()));
        double rarity = getDoubleOr(json, "rarity", 0.1);
        int duration = getIntOr(json, "duration", 6000);

        List<String> symptoms = new ArrayList<>();
        if (json.has("symptoms") && json.get("symptoms").isJsonArray()) {
            for (JsonElement el : json.getAsJsonArray("symptoms")) {
                if (el.isJsonPrimitive()) symptoms.add(el.getAsString());
            }
        }

        List<Item> cureItems = new ArrayList<>();
        if (json.has("cureItems") && json.get("cureItems").isJsonArray()) {
            for (JsonElement el : json.getAsJsonArray("cureItems")) {
                if (el.isJsonPrimitive()) {
                    Identifier itemId = Identifier.tryParse(el.getAsString());
                    if (itemId != null) {
                        Item item = BuiltInRegistries.ITEM.getValue(itemId);
                        if (item != null && item != Items.AIR) {
                            cureItems.add(item);
                        }
                    }
                }
            }
        }

        return new DiseaseDefinition(id, name, rarity, symptoms, cureItems, duration);
    }

    /**
     * Get a disease definition by ID.
     */
    public DiseaseDefinition getDisease(Identifier id) {
        return diseases.get(id);
    }

    /**
     * Get all loaded diseases.
     */
    public Collection<DiseaseDefinition> getAllDiseases() {
        return Collections.unmodifiableCollection(diseaseList);
    }

    /**
     * Randomly select a disease based on rarity weights.
     * Returns null if no diseases are loaded.
     */
    public DiseaseDefinition getRandomDisease() {
        if (diseaseList.isEmpty()) return null;

        double totalWeight = 0;
        for (DiseaseDefinition d : diseaseList) {
            totalWeight += d.rarity();
        }

        double roll = RANDOM.nextDouble() * totalWeight;
        double cumulative = 0;
        for (DiseaseDefinition d : diseaseList) {
            cumulative += d.rarity();
            if (roll <= cumulative) return d;
        }

        return diseaseList.get(diseaseList.size() - 1);
    }

    /**
     * Disease definition record.
     */
    public record DiseaseDefinition(
            Identifier id,
            String name,
            double rarity,
            List<String> symptoms,
            List<Item> cureItems,
            int duration
    ) {
        /**
         * Check if a given item can cure this disease.
         */
        public boolean canCure(Item item) {
            return cureItems.contains(item);
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

    private static double getDoubleOr(JsonObject json, String key, double defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) return json.get(key).getAsDouble();
        return defaultValue;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        int slash = s.lastIndexOf('/');
        if (slash >= 0) s = s.substring(slash + 1);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace('_', ' ');
    }
}
