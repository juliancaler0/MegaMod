package com.ultra.megamod.feature.citizen.research;

import com.google.gson.*;
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

import java.util.Map;

/**
 * Data pack loader for the research system. Reads JSON files from
 * {@code data/megamod/research/} and populates the {@link GlobalResearchTree}.
 * <p>
 * Registration: add this listener via the {@code AddReloadListenerEvent} in
 * the mod's event handler:
 * <pre>
 *   event.addListener(new ResearchDataListener());
 * </pre>
 *
 * <h2>JSON Format</h2>
 *
 * <h3>Branch files: {@code data/megamod/research/branches/<id>.json}</h3>
 * <pre>
 * {
 *   "type": "branch",
 *   "name": "Technology",
 *   "sortOrder": 1
 * }
 * </pre>
 *
 * <h3>Research files: {@code data/megamod/research/<branch>/<id>.json}</h3>
 * <pre>
 * {
 *   "name": "Sharper Swords",
 *   "description": "Improve melee weapon damage for all citizens.",
 *   "branch": "combat",
 *   "tier": 2,
 *   "researchTime": 12000,
 *   "hidden": false,
 *   "autoStart": false,
 *   "parents": ["megamod:combat/basic_training"],
 *   "costs": [
 *     { "type": "item", "item": "minecraft:iron_ingot", "count": 64 },
 *     { "type": "experience", "levels": 10 }
 *   ],
 *   "requirements": [
 *     { "type": "building", "building": "university", "level": 2 },
 *     { "type": "research", "research": "megamod:combat/basic_training" }
 *   ],
 *   "effects": [
 *     { "id": "melee_damage_1", "type": "MULTIPLIER", "stat": "melee_damage", "value": 1.1, "description": "+10% melee damage" },
 *     { "id": "unlock_diamond_swords", "type": "UNLOCK", "stat": "diamond_weapons", "value": 1, "description": "Unlocks diamond weapons" }
 *   ]
 * }
 * </pre>
 */
public class ResearchDataListener extends SimpleJsonResourceReloadListener<JsonElement> {

    private static final String DIRECTORY = "research";

    public ResearchDataListener() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(DIRECTORY));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        GlobalResearchTree tree = GlobalResearchTree.INSTANCE;
        tree.clear();

        int branchCount = 0;
        int researchCount = 0;

        // First pass: load branches
        for (Map.Entry<Identifier, JsonElement> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            JsonElement element = entry.getValue();

            if (!element.isJsonObject()) continue;
            JsonObject json = element.getAsJsonObject();

            // Branch files have "type": "branch"
            if (json.has("type") && "branch".equals(getStringOr(json, "type", ""))) {
                try {
                    ResearchBranch branch = parseBranch(id, json);
                    tree.addBranch(branch);
                    branchCount++;
                } catch (Exception e) {
                    MegaMod.LOGGER.warn("Failed to parse research branch {}: {}", id, e.getMessage());
                }
            }
        }

        // Second pass: load researches
        for (Map.Entry<Identifier, JsonElement> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            JsonElement element = entry.getValue();

            if (!element.isJsonObject()) continue;
            JsonObject json = element.getAsJsonObject();

            // Skip branches
            if (json.has("type") && "branch".equals(getStringOr(json, "type", ""))) {
                continue;
            }

            try {
                Identifier researchId = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath());
                GlobalResearch research = parseResearch(researchId, json);
                tree.addResearch(research);
                researchCount++;
            } catch (Exception e) {
                MegaMod.LOGGER.warn("Failed to parse research {}: {}", id, e.getMessage());
            }
        }

        tree.validate();
        MegaMod.LOGGER.info("Loaded {} research branches and {} researches", branchCount, researchCount);
    }

    // ---- Parsing ----

    private ResearchBranch parseBranch(Identifier id, JsonObject json) {
        String branchId = id.getPath();
        // Strip "branches/" prefix if present
        if (branchId.startsWith("branches/")) {
            branchId = branchId.substring("branches/".length());
        }
        String name = getStringOr(json, "name", capitalize(branchId));
        int sortOrder = getIntOr(json, "sortOrder", 0);
        double baseTime = getDoubleOr(json, "baseTime", 1.0);
        boolean hidden = getBoolOr(json, "hidden", false);
        return new ResearchBranch(branchId, name, sortOrder, baseTime, hidden);
    }

    private GlobalResearch parseResearch(Identifier id, JsonObject json) {
        String name = getStringOr(json, "name", capitalize(id.getPath()));
        String description = getStringOr(json, "description", "");
        String branchId = getStringOr(json, "branch", "");
        int tier = getIntOr(json, "tier", 1);

        GlobalResearch research = new GlobalResearch(id, name, description, branchId, tier);
        research.setResearchTime(getIntOr(json, "researchTime", 6000));
        research.setHidden(getBoolOr(json, "hidden", false));
        research.setAutoStart(getBoolOr(json, "autoStart", false));
        research.setExclusive(getBoolOr(json, "exclusive", false));
        research.setInstant(getBoolOr(json, "instant", false));
        research.setImmutable(getBoolOr(json, "immutable", getBoolOr(json, "noReset", false)));
        research.setOnlyChild(getBoolOr(json, "exclusiveChildResearch", getBoolOr(json, "onlyChild", false)));
        research.setSortOrder(getIntOr(json, "sortOrder", 1000));

        // Parents
        if (json.has("parents") && json.get("parents").isJsonArray()) {
            for (JsonElement parentEl : json.getAsJsonArray("parents")) {
                Identifier parentId = Identifier.tryParse(parentEl.getAsString());
                if (parentId != null) {
                    research.addParentId(parentId);
                }
            }
        }

        // Blocking
        if (json.has("blocking") && json.get("blocking").isJsonArray()) {
            for (JsonElement blockEl : json.getAsJsonArray("blocking")) {
                Identifier blockedId = Identifier.tryParse(blockEl.getAsString());
                if (blockedId != null) {
                    research.addBlocking(blockedId);
                }
            }
        }

        // Costs
        if (json.has("costs") && json.get("costs").isJsonArray()) {
            for (JsonElement costEl : json.getAsJsonArray("costs")) {
                if (!costEl.isJsonObject()) continue;
                IResearchCost cost = parseCost(costEl.getAsJsonObject());
                if (cost != null) {
                    research.addCost(cost);
                }
            }
        }

        // Requirements
        if (json.has("requirements") && json.get("requirements").isJsonArray()) {
            for (JsonElement reqEl : json.getAsJsonArray("requirements")) {
                if (!reqEl.isJsonObject()) continue;
                IResearchRequirement req = parseRequirement(reqEl.getAsJsonObject());
                if (req != null) {
                    research.addRequirement(req);
                }
            }
        }

        // Effects
        if (json.has("effects") && json.get("effects").isJsonArray()) {
            for (JsonElement effectEl : json.getAsJsonArray("effects")) {
                if (!effectEl.isJsonObject()) continue;
                IResearchEffect effect = parseEffect(effectEl.getAsJsonObject());
                if (effect != null) {
                    research.addEffect(effect);
                }
            }
        }

        return research;
    }

    private IResearchCost parseCost(JsonObject json) {
        String type = getStringOr(json, "type", "");
        return switch (type) {
            case "item" -> {
                Identifier itemId = Identifier.tryParse(getStringOr(json, "item", "minecraft:air"));
                Item item = itemId != null ? BuiltInRegistries.ITEM.getValue(itemId) : null;
                if (item == null) item = Items.AIR;
                int count = getIntOr(json, "count", 1);
                yield new ItemResearchCost(item, count);
            }
            case "experience" -> {
                int levels = getIntOr(json, "levels", 1);
                yield new ExperienceResearchCost(levels);
            }
            default -> {
                MegaMod.LOGGER.warn("Unknown research cost type: {}", type);
                yield null;
            }
        };
    }

    private IResearchRequirement parseRequirement(JsonObject json) {
        String type = getStringOr(json, "type", "");
        return switch (type) {
            case "building" -> {
                String buildingId = getStringOr(json, "building", "");
                int level = getIntOr(json, "level", 1);
                yield new BuildingResearchRequirement(buildingId, level);
            }
            case "research" -> {
                Identifier researchId = Identifier.tryParse(getStringOr(json, "research", ""));
                if (researchId == null) {
                    MegaMod.LOGGER.warn("Invalid research requirement ID in JSON");
                    yield null;
                }
                yield new ResearchResearchRequirement(researchId);
            }
            default -> {
                MegaMod.LOGGER.warn("Unknown research requirement type: {}", type);
                yield null;
            }
        };
    }

    private IResearchEffect parseEffect(JsonObject json) {
        String id = getStringOr(json, "id", "");
        String typeStr = getStringOr(json, "type", "MODIFIER");
        String stat = getStringOr(json, "stat", "");
        double value = getDoubleOr(json, "value", 0.0);
        String description = getStringOr(json, "description", "");

        ResearchEffect.EffectType type = ResearchEffect.EffectType.fromString(typeStr);
        return new ResearchEffect(id, type, stat, value, description);
    }

    // ---- JSON helpers (avoid dependency on GsonHelper which may not be available) ----

    private static String getStringOr(JsonObject json, String key, String defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsString();
        }
        return defaultValue;
    }

    private static int getIntOr(JsonObject json, String key, int defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsInt();
        }
        return defaultValue;
    }

    private static double getDoubleOr(JsonObject json, String key, double defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsDouble();
        }
        return defaultValue;
    }

    private static boolean getBoolOr(JsonObject json, String key, boolean defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsBoolean();
        }
        return defaultValue;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        // Extract last path segment
        int slash = s.lastIndexOf('/');
        if (slash >= 0) s = s.substring(slash + 1);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace('_', ' ');
    }
}
