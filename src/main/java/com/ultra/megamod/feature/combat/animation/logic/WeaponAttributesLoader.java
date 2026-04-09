package com.ultra.megamod.feature.combat.animation.logic;

import com.google.gson.*;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.animation.WeaponAttributeRegistry;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.*;

/**
 * JSON-driven weapon attributes loader with inheritance support.
 * Ported from BetterCombat's WeaponRegistry JSON loading system.
 *
 * Loads weapon_attributes JSON files from data packs at:
 *   data/{namespace}/weapon_attributes/{item_id}.json
 *
 * Supports:
 * - "parent" field for attribute inheritance (e.g., "parent": "megamod:sword")
 * - Full WeaponAttributes deserialization via GSON
 * - Override individual attack fields while inheriting the rest
 *
 * Falls back to WeaponAttributeRegistry's hardcoded values if no JSON exists.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class WeaponAttributesLoader {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        loadFromResources(event.getServer().getResourceManager());
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("WeaponAttributesLoader");
    private static final Gson GSON = new GsonBuilder().create();

    /** Loaded attributes from JSON, keyed by item ID (e.g., "megamod:diamond_claymore") */
    private static final Map<String, WeaponAttributes> JSON_ATTRIBUTES = new HashMap<>();

    /** Base archetypes for inheritance (e.g., "megamod:sword", "megamod:claymore") */
    private static final Map<String, WeaponAttributes> ARCHETYPES = new HashMap<>();

    /**
     * Register a base archetype that weapons can inherit from.
     * Called during mod init to set up sword, dagger, axe, etc. templates.
     */
    public static void registerArchetype(String id, WeaponAttributes attrs) {
        ARCHETYPES.put(id, attrs);
    }

    /**
     * Load all weapon_attributes JSON files from the resource manager.
     * Called during server resource reload.
     */
    public static void loadFromResources(ResourceManager resourceManager) {
        JSON_ATTRIBUTES.clear();
        var resources = resourceManager.listResources("weapon_attributes",
                id -> id.getPath().endsWith(".json"));

        int loaded = 0;
        for (var entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            try (var stream = entry.getValue().open()) {
                var reader = new InputStreamReader(stream);
                var json = JsonParser.parseReader(reader).getAsJsonObject();

                String itemId = id.getNamespace() + ":" + id.getPath()
                        .replace("weapon_attributes/", "")
                        .replace(".json", "");

                WeaponAttributes attrs = parseWithInheritance(json);
                if (attrs != null) {
                    JSON_ATTRIBUTES.put(itemId, attrs);
                    loaded++;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to load weapon_attributes {}: {}", id, e.getMessage());
            }
        }

        if (loaded > 0) {
            LOGGER.info("[WeaponAttributesLoader] Loaded {} weapon attribute overrides from data packs", loaded);
        }
    }

    /**
     * Get attributes for an item, checking JSON overrides first, then hardcoded registry.
     */
    public static WeaponAttributes getAttributes(String itemId) {
        var jsonAttrs = JSON_ATTRIBUTES.get(itemId);
        if (jsonAttrs != null) return jsonAttrs;
        return null; // Caller should fall back to hardcoded
    }

    /**
     * Parse a weapon_attributes JSON with optional parent inheritance.
     */
    private static WeaponAttributes parseWithInheritance(JsonObject json) {
        WeaponAttributes parent = null;
        if (json.has("parent")) {
            String parentId = json.get("parent").getAsString();
            parent = ARCHETYPES.get(parentId);
            if (parent == null) {
                parent = JSON_ATTRIBUTES.get(parentId);
            }
        }

        // Parse the attributes object (or use the whole JSON as attributes)
        JsonObject attrsJson = json.has("attributes") ? json.getAsJsonObject("attributes") : json;

        double attackRange = getDouble(attrsJson, "attack_range", parent != null ? parent.attackRange() : 0);
        double rangeBonus = getDouble(attrsJson, "range_bonus", parent != null ? parent.rangeBonus() : 0);
        boolean twoHanded = getBoolean(attrsJson, "two_handed", parent != null && parent.twoHanded());
        String category = getString(attrsJson, "category", parent != null ? parent.category() : "");
        String pose = getString(attrsJson, "pose", parent != null ? parent.pose() : null);

        // Parse attacks array
        WeaponAttributes.Attack[] attacks;
        if (attrsJson.has("attacks")) {
            JsonArray attacksJson = attrsJson.getAsJsonArray("attacks");
            attacks = new WeaponAttributes.Attack[attacksJson.size()];
            WeaponAttributes.Attack[] parentAttacks = parent != null ? parent.attacks() : null;
            for (int i = 0; i < attacksJson.size(); i++) {
                JsonObject attackJson = attacksJson.get(i).getAsJsonObject();
                WeaponAttributes.Attack parentAttack = (parentAttacks != null && i < parentAttacks.length) ? parentAttacks[i] : null;
                attacks[i] = parseAttack(attackJson, parentAttack);
            }
        } else if (parent != null) {
            attacks = parent.attacks();
        } else {
            attacks = new WeaponAttributes.Attack[0];
        }

        return new WeaponAttributes(attackRange, rangeBonus, twoHanded, category, pose, attacks);
    }

    private static WeaponAttributes.Attack parseAttack(JsonObject json, WeaponAttributes.Attack parent) {
        var hitbox = parseHitboxShape(getString(json, "hitbox",
                parent != null ? parent.hitbox().name() : "HORIZONTAL_PLANE"));
        double damageMult = getDouble(json, "damage_multiplier", parent != null ? parent.damageMultiplier() : 1);
        double angle = getDouble(json, "angle", parent != null ? parent.angle() : 0);
        double upswing = getDouble(json, "upswing", parent != null ? parent.upswing() : 0.5);
        String animation = getString(json, "animation", parent != null ? parent.animation() : null);

        var swingDir = parseSwingDirection(getString(json, "swing_direction",
                parent != null && parent.swingDirection() != null ? parent.swingDirection().name() : null));

        return new WeaponAttributes.Attack(hitbox, damageMult, angle, upswing, swingDir, animation, null);
    }

    private static WeaponAttributes.HitboxShape parseHitboxShape(String name) {
        if (name == null) return WeaponAttributes.HitboxShape.HORIZONTAL_PLANE;
        try { return WeaponAttributes.HitboxShape.valueOf(name.toUpperCase()); }
        catch (Exception e) { return WeaponAttributes.HitboxShape.HORIZONTAL_PLANE; }
    }

    private static WeaponAttributes.SwingDirection parseSwingDirection(String name) {
        if (name == null) return null;
        try { return WeaponAttributes.SwingDirection.valueOf(name.toUpperCase()); }
        catch (Exception e) { return null; }
    }

    private static double getDouble(JsonObject json, String key, double defaultVal) {
        return json.has(key) ? json.get(key).getAsDouble() : defaultVal;
    }

    private static boolean getBoolean(JsonObject json, String key, boolean defaultVal) {
        return json.has(key) ? json.get(key).getAsBoolean() : defaultVal;
    }

    private static String getString(JsonObject json, String key, String defaultVal) {
        return json.has(key) ? json.get(key).getAsString() : defaultVal;
    }
}
