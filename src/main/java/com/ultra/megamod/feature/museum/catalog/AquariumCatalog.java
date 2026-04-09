/*
 * Decompiled with CFR 0.152.
 */
package com.ultra.megamod.feature.museum.catalog;

import java.util.List;

public class AquariumCatalog {
    public static final List<MobEntry> ENTRIES = List.of(new MobEntry("minecraft:cod", "Cod", "Common ocean fish"), new MobEntry("minecraft:salmon", "Salmon", "River and ocean fish"), new MobEntry("minecraft:tropical_fish", "Tropical Fish", "Colorful warm ocean fish"), new MobEntry("minecraft:pufferfish", "Pufferfish", "Inflates when threatened"), new MobEntry("minecraft:squid", "Squid", "Common ocean cephalopod"), new MobEntry("minecraft:glow_squid", "Glow Squid", "Bioluminescent deep squid"), new MobEntry("minecraft:dolphin", "Dolphin", "Friendly ocean mammal"), new MobEntry("minecraft:turtle", "Turtle", "Beach-nesting reptile"), new MobEntry("minecraft:axolotl", "Axolotl", "Cute cave-dwelling amphibian"), new MobEntry("minecraft:frog", "Frog", "Swamp-dwelling amphibian"), new MobEntry("minecraft:tadpole", "Tadpole", "Baby frog"), new MobEntry("minecraft:guardian", "Guardian", "Ocean monument defender"), new MobEntry("minecraft:elder_guardian", "Elder Guardian", "Ancient monument boss"), new MobEntry("minecraft:drowned", "Drowned", "Underwater zombie variant"));

    public static int getTotalCount() {
        return ENTRIES.size();
    }

    public static MobEntry getById(String entityId) {
        for (MobEntry entry : ENTRIES) {
            if (!entry.entityId().equals(entityId)) continue;
            return entry;
        }
        return null;
    }

    public static boolean contains(String entityId) {
        return AquariumCatalog.getById(entityId) != null;
    }

    public record MobEntry(String entityId, String displayName, String description) {
    }
}

