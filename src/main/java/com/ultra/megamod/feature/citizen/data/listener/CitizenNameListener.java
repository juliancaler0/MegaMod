package com.ultra.megamod.feature.citizen.data.listener;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ultra.megamod.MegaMod;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;

/**
 * SimpleJsonResourceReloadListener loading citizen names from
 * {@code data/megamod/citizennames/}. Stores male/female first names
 * and surnames per culture. Provides a random name generator.
 *
 * <h2>JSON Format (MineColonies-compatible)</h2>
 * <pre>
 * {
 *   "parts": 3,
 *   "order": "WESTERN",
 *   "male_firstname": ["John", "William", "James"],
 *   "female_firstname": ["Mary", "Elizabeth", "Margaret"],
 *   "surnames": ["Smith", "Johnson", "Williams"]
 * }
 * </pre>
 *
 * Registration: add this listener via {@code AddReloadListenerEvent}:
 * <pre>
 *   event.addListener(CitizenNameListener.INSTANCE);
 * </pre>
 */
public class CitizenNameListener extends SimpleJsonResourceReloadListener<JsonElement> {

    private static final String DIRECTORY = "citizennames";

    /** Singleton instance populated on reload */
    public static final CitizenNameListener INSTANCE = new CitizenNameListener();

    // culture -> male first names
    private final Map<String, List<String>> maleNames = new HashMap<>();
    // culture -> female first names
    private final Map<String, List<String>> femaleNames = new HashMap<>();
    // culture -> surnames
    private final Map<String, List<String>> surnames = new HashMap<>();

    private static final RandomSource RANDOM = RandomSource.create();

    // Fallback names if no data is loaded
    private static final List<String> DEFAULT_MALE = List.of(
            "John", "William", "James", "Robert", "Charles", "Thomas", "Henry", "George",
            "Edward", "Richard", "Arthur", "Albert", "Frederick", "Alfred", "Harold"
    );
    private static final List<String> DEFAULT_FEMALE = List.of(
            "Mary", "Elizabeth", "Margaret", "Anne", "Catherine", "Sarah", "Jane", "Alice",
            "Eleanor", "Rose", "Florence", "Beatrice", "Clara", "Dorothy", "Edith"
    );
    private static final List<String> DEFAULT_SURNAMES = List.of(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Wilson", "Anderson", "Taylor", "Thomas", "Moore", "Jackson", "Martin"
    );

    public CitizenNameListener() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(DIRECTORY));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        maleNames.clear();
        femaleNames.clear();
        surnames.clear();

        int fileCount = 0;
        for (Map.Entry<Identifier, JsonElement> entry : resources.entrySet()) {
            JsonElement element = entry.getValue();
            if (!element.isJsonObject()) continue;

            try {
                JsonObject json = element.getAsJsonObject();

                // Use the resource path as the culture key (e.g., "default", "french", "dwarven")
                String culture = entry.getKey().getPath().toLowerCase(Locale.ROOT);

                // Parse male first names
                if (json.has("male_firstname") && json.get("male_firstname").isJsonArray()) {
                    List<String> names = maleNames.computeIfAbsent(culture, k -> new ArrayList<>());
                    for (JsonElement nameEl : json.getAsJsonArray("male_firstname")) {
                        if (nameEl.isJsonPrimitive()) {
                            names.add(nameEl.getAsString());
                        }
                    }
                }

                // Parse female first names
                if (json.has("female_firstname") && json.get("female_firstname").isJsonArray()) {
                    List<String> names = femaleNames.computeIfAbsent(culture, k -> new ArrayList<>());
                    for (JsonElement nameEl : json.getAsJsonArray("female_firstname")) {
                        if (nameEl.isJsonPrimitive()) {
                            names.add(nameEl.getAsString());
                        }
                    }
                }

                // Parse surnames
                if (json.has("surnames") && json.get("surnames").isJsonArray()) {
                    List<String> surnameList = surnames.computeIfAbsent(culture, k -> new ArrayList<>());
                    for (JsonElement nameEl : json.getAsJsonArray("surnames")) {
                        if (nameEl.isJsonPrimitive()) {
                            surnameList.add(nameEl.getAsString());
                        }
                    }
                }

                // Legacy format support: "male" / "female" arrays with optional "culture" key
                if (json.has("male") && json.get("male").isJsonArray()) {
                    String legacyCulture = getStringOr(json, "culture", culture);
                    List<String> names = maleNames.computeIfAbsent(legacyCulture, k -> new ArrayList<>());
                    for (JsonElement nameEl : json.getAsJsonArray("male")) {
                        if (nameEl.isJsonPrimitive()) {
                            names.add(nameEl.getAsString());
                        }
                    }
                }
                if (json.has("female") && json.get("female").isJsonArray()) {
                    String legacyCulture = getStringOr(json, "culture", culture);
                    List<String> names = femaleNames.computeIfAbsent(legacyCulture, k -> new ArrayList<>());
                    for (JsonElement nameEl : json.getAsJsonArray("female")) {
                        if (nameEl.isJsonPrimitive()) {
                            names.add(nameEl.getAsString());
                        }
                    }
                }

                fileCount++;
            } catch (Exception e) {
                MegaMod.LOGGER.warn("Failed to parse citizen names from {}", entry.getKey(), e);
            }
        }

        MegaMod.LOGGER.info("Loaded citizen names from {} files ({} cultures, {} male names, {} female names, {} surnames)",
                fileCount, maleNames.size(),
                maleNames.values().stream().mapToInt(List::size).sum(),
                femaleNames.values().stream().mapToInt(List::size).sum(),
                surnames.values().stream().mapToInt(List::size).sum());
    }

    /**
     * Get a random male first name for the given culture. Falls back to defaults.
     */
    public String getRandomMaleName(String culture) {
        List<String> names = maleNames.getOrDefault(culture.toLowerCase(Locale.ROOT), null);
        if (names == null || names.isEmpty()) {
            names = maleNames.getOrDefault("default", DEFAULT_MALE);
        }
        if (names.isEmpty()) names = DEFAULT_MALE;
        return names.get(RANDOM.nextInt(names.size()));
    }

    /**
     * Get a random female first name for the given culture. Falls back to defaults.
     */
    public String getRandomFemaleName(String culture) {
        List<String> names = femaleNames.getOrDefault(culture.toLowerCase(Locale.ROOT), null);
        if (names == null || names.isEmpty()) {
            names = femaleNames.getOrDefault("default", DEFAULT_FEMALE);
        }
        if (names.isEmpty()) names = DEFAULT_FEMALE;
        return names.get(RANDOM.nextInt(names.size()));
    }

    /**
     * Get a random first name (50/50 male/female) for the given culture.
     */
    public String getRandomName(String culture) {
        return RANDOM.nextBoolean() ? getRandomMaleName(culture) : getRandomFemaleName(culture);
    }

    /**
     * Get a random surname for the given culture. Falls back to defaults.
     */
    public String getRandomSurname(String culture) {
        List<String> surnameList = surnames.getOrDefault(culture.toLowerCase(Locale.ROOT), null);
        if (surnameList == null || surnameList.isEmpty()) {
            surnameList = surnames.getOrDefault("default", DEFAULT_SURNAMES);
        }
        if (surnameList.isEmpty()) surnameList = DEFAULT_SURNAMES;
        return surnameList.get(RANDOM.nextInt(surnameList.size()));
    }

    /**
     * Get a random full name (first + surname).
     */
    public String getRandomFullName(String culture) {
        String first = getRandomName(culture);
        String last = getRandomSurname(culture);
        return first + " " + last;
    }

    /**
     * Get a random full name with gender-appropriate first name.
     *
     * @param culture the culture to pull names from
     * @param female  true for female first name, false for male
     * @return a full name like "Maria Garcia" or "John Smith"
     */
    public String getRandomFullName(String culture, boolean female) {
        String first = female ? getRandomFemaleName(culture) : getRandomMaleName(culture);
        String last = getRandomSurname(culture);
        return first + " " + last;
    }

    /**
     * Get all loaded cultures.
     */
    public Set<String> getCultures() {
        Set<String> cultures = new HashSet<>(maleNames.keySet());
        cultures.addAll(femaleNames.keySet());
        cultures.addAll(surnames.keySet());
        return cultures;
    }

    private static String getStringOr(JsonObject json, String key, String defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsString();
        }
        return defaultValue;
    }
}
