package com.ultra.megamod.lib.spellengine.client.render;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CustomModelRegistry {
    // NeoForge client initializers may run parallel, so use a thread-safe collection
    public static final ConcurrentLinkedQueue<Identifier> modelIds = new ConcurrentLinkedQueue<>();

    public static Collection<Identifier> getModelIds() {
        return modelIds;
    }
}