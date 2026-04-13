package com.ultra.megamod.lib.spellengine.api.tags;

import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;

import java.util.ArrayList;

public class SpellEngineDamageTypeTags {
    public static final ArrayList<TagKey<DamageType>> ALL = new ArrayList<>();
    private static TagKey<DamageType> create(String id) {
        var tag = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("megamod", id));
        ALL.add(tag);
        return tag;
    }
    public static final TagKey<DamageType> EVADABLE = create("evadable");
}
