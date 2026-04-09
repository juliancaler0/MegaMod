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
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;

/**
 * SimpleJsonResourceReloadListener loading story/lore text from
 * {@code data/megamod/colony/stories/}. Provides supply camp stories
 * and abandoned colony names.
 *
 * <h2>JSON Format</h2>
 * <pre>
 * {
 *   "type": "supply_camp",
 *   "stories": [
 *     "The supply camp was set up by a group of wandering traders...",
 *     "A small band of settlers left these supplies before moving on..."
 *   ],
 *   "abandonedNames": [
 *     "Ruins of Oakshire",
 *     "The Lost Settlement",
 *     "Abandoned Fort Greymoor"
 *   ]
 * }
 * </pre>
 *
 * Registration: add via {@code AddReloadListenerEvent}:
 * <pre>
 *   event.addListener(new ColonyStoryListener());
 * </pre>
 */
public class ColonyStoryListener extends SimpleJsonResourceReloadListener<JsonElement> {

    private static final String DIRECTORY = "colony/stories";

    /** Singleton instance populated on reload */
    public static final ColonyStoryListener INSTANCE = new ColonyStoryListener();

    // type -> list of story texts
    private final Map<String, List<String>> stories = new LinkedHashMap<>();
    // Abandoned colony names
    private final List<String> abandonedNames = new ArrayList<>();

    private static final Random RANDOM = new Random();

    // Fallback data
    private static final List<String> DEFAULT_STORIES = List.of(
            "A forgotten settlement once stood here, its people long departed.",
            "Supplies were left behind by travelers who never returned.",
            "The remnants of a colony tell a tale of hardship and perseverance."
    );
    private static final List<String> DEFAULT_ABANDONED_NAMES = List.of(
            "Ruins of the Forgotten",
            "The Lost Colony",
            "Abandoned Settlement",
            "Desolate Outpost",
            "The Fallen Village"
    );

    public ColonyStoryListener() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(DIRECTORY));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        stories.clear();
        abandonedNames.clear();

        int fileCount = 0;
        for (Map.Entry<Identifier, JsonElement> entry : resources.entrySet()) {
            JsonElement element = entry.getValue();
            if (!element.isJsonObject()) continue;
            JsonObject json = element.getAsJsonObject();

            String type = getStringOr(json, "type", "general").toLowerCase(Locale.ROOT);

            // Parse stories
            if (json.has("stories") && json.get("stories").isJsonArray()) {
                List<String> typeStories = stories.computeIfAbsent(type, k -> new ArrayList<>());
                for (JsonElement storyEl : json.getAsJsonArray("stories")) {
                    if (storyEl.isJsonPrimitive()) {
                        typeStories.add(storyEl.getAsString());
                    }
                }
            }

            // Parse abandoned colony names
            if (json.has("abandonedNames") && json.get("abandonedNames").isJsonArray()) {
                for (JsonElement nameEl : json.getAsJsonArray("abandonedNames")) {
                    if (nameEl.isJsonPrimitive()) {
                        abandonedNames.add(nameEl.getAsString());
                    }
                }
            }

            fileCount++;
        }

        MegaMod.LOGGER.info("Loaded colony stories from {} files ({} types, {} abandoned names)",
                fileCount, stories.size(), abandonedNames.size());
    }

    /**
     * Get a random story of the given type. Falls back to default stories.
     */
    public String getRandomStory(String type) {
        List<String> pool = stories.getOrDefault(type.toLowerCase(Locale.ROOT), null);
        if (pool == null || pool.isEmpty()) {
            pool = stories.getOrDefault("general", DEFAULT_STORIES);
        }
        if (pool.isEmpty()) pool = DEFAULT_STORIES;
        return pool.get(RANDOM.nextInt(pool.size()));
    }

    /**
     * Get a random supply camp story.
     */
    public String getRandomSupplyCampStory() {
        return getRandomStory("supply_camp");
    }

    /**
     * Get a random abandoned colony name. Falls back to defaults.
     */
    public String getRandomAbandonedName() {
        List<String> pool = abandonedNames.isEmpty() ? DEFAULT_ABANDONED_NAMES : abandonedNames;
        return pool.get(RANDOM.nextInt(pool.size()));
    }

    /**
     * Get all story types loaded.
     */
    public Set<String> getStoryTypes() {
        return Collections.unmodifiableSet(stories.keySet());
    }

    /**
     * Get all abandoned colony names.
     */
    public List<String> getAllAbandonedNames() {
        return Collections.unmodifiableList(abandonedNames);
    }

    private static String getStringOr(JsonObject json, String key, String defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) return json.get(key).getAsString();
        return defaultValue;
    }
}
