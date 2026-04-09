package com.ultra.megamod.feature.combat.spell.client.render;

import net.minecraft.resources.Identifier;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe registry for custom model IDs that need to be loaded at startup.
 * Ported 1:1 from SpellEngine (net.spell_engine.client.render.CustomModelRegistry).
 */
public class CustomModelRegistry {
    public static final ConcurrentLinkedQueue<Identifier> modelIds = new ConcurrentLinkedQueue<>();

    public static void register(Identifier id) {
        modelIds.add(id);
    }
}
