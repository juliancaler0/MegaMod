/*
 * Decompiled with CFR 0.152.
 */
package com.ultra.megamod.feature.museum.catalog;

import java.util.List;
import java.util.stream.Collectors;

public class WildlifeCatalog {
    public static final List<MobEntry> ENTRIES = List.of(new MobEntry("minecraft:cow", "Cow", "Passive"), new MobEntry("minecraft:pig", "Pig", "Passive"), new MobEntry("minecraft:sheep", "Sheep", "Passive"), new MobEntry("minecraft:chicken", "Chicken", "Passive"), new MobEntry("minecraft:horse", "Horse", "Passive"), new MobEntry("minecraft:donkey", "Donkey", "Passive"), new MobEntry("minecraft:mule", "Mule", "Passive"), new MobEntry("minecraft:skeleton_horse", "Skeleton Horse", "Passive"), new MobEntry("minecraft:zombie_horse", "Zombie Horse", "Passive"), new MobEntry("minecraft:rabbit", "Rabbit", "Passive"), new MobEntry("minecraft:mooshroom", "Mooshroom", "Passive"), new MobEntry("minecraft:cat", "Cat", "Passive"), new MobEntry("minecraft:ocelot", "Ocelot", "Passive"), new MobEntry("minecraft:parrot", "Parrot", "Passive"), new MobEntry("minecraft:villager", "Villager", "Passive"), new MobEntry("minecraft:wandering_trader", "Wandering Trader", "Passive"), new MobEntry("minecraft:snow_golem", "Snow Golem", "Passive"), new MobEntry("minecraft:iron_golem", "Iron Golem", "Passive"), new MobEntry("minecraft:allay", "Allay", "Passive"), new MobEntry("minecraft:strider", "Strider", "Passive"), new MobEntry("minecraft:camel", "Camel", "Passive"), new MobEntry("minecraft:sniffer", "Sniffer", "Passive"), new MobEntry("minecraft:armadillo", "Armadillo", "Passive"), new MobEntry("minecraft:wolf", "Wolf", "Neutral"), new MobEntry("minecraft:fox", "Fox", "Neutral"), new MobEntry("minecraft:panda", "Panda", "Neutral"), new MobEntry("minecraft:bee", "Bee", "Neutral"), new MobEntry("minecraft:llama", "Llama", "Neutral"), new MobEntry("minecraft:trader_llama", "Trader Llama", "Neutral"), new MobEntry("minecraft:polar_bear", "Polar Bear", "Neutral"), new MobEntry("minecraft:goat", "Goat", "Neutral"), new MobEntry("minecraft:enderman", "Enderman", "Neutral"), new MobEntry("minecraft:zombified_piglin", "Zombified Piglin", "Neutral"), new MobEntry("minecraft:spider", "Spider", "Neutral"), new MobEntry("minecraft:cave_spider", "Cave Spider", "Neutral"), new MobEntry("minecraft:piglin", "Piglin", "Neutral"), new MobEntry("minecraft:zombie", "Zombie", "Hostile"), new MobEntry("minecraft:zombie_villager", "Zombie Villager", "Hostile"), new MobEntry("minecraft:husk", "Husk", "Hostile"), new MobEntry("minecraft:skeleton", "Skeleton", "Hostile"), new MobEntry("minecraft:stray", "Stray", "Hostile"), new MobEntry("minecraft:wither_skeleton", "Wither Skeleton", "Hostile"), new MobEntry("minecraft:bogged", "Bogged", "Hostile"), new MobEntry("minecraft:creeper", "Creeper", "Hostile"), new MobEntry("minecraft:witch", "Witch", "Hostile"), new MobEntry("minecraft:slime", "Slime", "Hostile"), new MobEntry("minecraft:magma_cube", "Magma Cube", "Hostile"), new MobEntry("minecraft:phantom", "Phantom", "Hostile"), new MobEntry("minecraft:blaze", "Blaze", "Hostile"), new MobEntry("minecraft:ghast", "Ghast", "Hostile"), new MobEntry("minecraft:piglin_brute", "Piglin Brute", "Hostile"), new MobEntry("minecraft:hoglin", "Hoglin", "Hostile"), new MobEntry("minecraft:zoglin", "Zoglin", "Hostile"), new MobEntry("minecraft:ravager", "Ravager", "Hostile"), new MobEntry("minecraft:vindicator", "Vindicator", "Hostile"), new MobEntry("minecraft:evoker", "Evoker", "Hostile"), new MobEntry("minecraft:pillager", "Pillager", "Hostile"), new MobEntry("minecraft:illusioner", "Illusioner", "Hostile"), new MobEntry("minecraft:vex", "Vex", "Hostile"), new MobEntry("minecraft:shulker", "Shulker", "Hostile"), new MobEntry("minecraft:endermite", "Endermite", "Hostile"), new MobEntry("minecraft:silverfish", "Silverfish", "Hostile"), new MobEntry("minecraft:warden", "Warden", "Hostile"), new MobEntry("minecraft:breeze", "Breeze", "Hostile"), new MobEntry("minecraft:ender_dragon", "Ender Dragon", "Boss"), new MobEntry("minecraft:wither", "Wither", "Boss"), new MobEntry("minecraft:bat", "Bat", "Ambient"));

    public static int getTotalCount() {
        return ENTRIES.size();
    }

    public static List<MobEntry> getByCategory(String category) {
        return ENTRIES.stream().filter(e -> e.category().equals(category)).collect(Collectors.toList());
    }

    public static MobEntry getById(String entityId) {
        for (MobEntry entry : ENTRIES) {
            if (!entry.entityId().equals(entityId)) continue;
            return entry;
        }
        return null;
    }

    public static boolean contains(String entityId) {
        return WildlifeCatalog.getById(entityId) != null;
    }

    public static int getCategoryCount(String category) {
        return (int)ENTRIES.stream().filter(e -> e.category().equals(category)).count();
    }

    public record MobEntry(String entityId, String displayName, String category) {
    }
}

