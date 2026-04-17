package com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Compile-time stub mirroring {@code io.redspace.ironsspellbooks.api.registry.SchoolRegistry}.
 * When Iron's Spellbooks is present at runtime the actual registry binds to
 * {@code SCHOOL_REGISTRY_KEY}; with the mod absent the {@link #REGISTRY} field is {@code null}
 * and all code that references it is gated behind {@code ModList.isLoaded("irons_spellbooks")}.
 */
public final class SchoolRegistry {
    private SchoolRegistry() { }

    @SuppressWarnings("unchecked")
    public static final ResourceKey<Registry<SchoolType>> SCHOOL_REGISTRY_KEY = (ResourceKey<Registry<SchoolType>>)
            (ResourceKey<?>) ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("irons_spellbooks", "schools"));

    /** Populated at runtime by Iron's Spellbooks when present; {@code null} otherwise. */
    public static Registry<SchoolType> REGISTRY = null;
}
