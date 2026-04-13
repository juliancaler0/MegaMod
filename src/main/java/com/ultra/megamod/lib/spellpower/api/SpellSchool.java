package com.ultra.megamod.lib.spellpower.api;

import com.google.gson.annotations.JsonAdapter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.alchemy.Potion;
import com.ultra.megamod.lib.spellpower.api.misc.SpellSchoolJSONAdapter;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Function;

@JsonAdapter(SpellSchoolJSONAdapter.class)
public class SpellSchool {
    public enum Archetype { ARCHERY, MAGIC, MELEE }
    public final Archetype archetype;
    /**
     * ID of the:
     * - Spell School itself
     * - Powering Entity Attribute if managed internally
     * - Powering Status Effect if managed internally
     */
    public final Identifier id;

    /**
     * Theme color of the spell school.
     * Format: 0xRRGGBB. For example, 0xff0000 is red, 0x00ff00 is green, 0x0000ff is blue.
     * Used for:
     * - Cast bar tinting
     * - Boosting status effect color
     */
    public final int color;

    /**
     * Internally managed entity attribute that boosts this spell school.
     */
    @Nullable private final Attribute ownedAttribute;

    /**
     * Status effect that boosts this spell school.
     * Maybe left null, if status effect that boosts the respective attribute already exists.
     * (Like how vanilla Strength boosts attack damage)
     */
    @Nullable public final MobEffect ownedBoostEffect;

    /**
     * Spells of this school deal this type of damage
     */
    public final ResourceKey<DamageType> damageType;

    /**
     * DeferredHolder for NeoForge registration of the owned attribute.
     */
    @Nullable public DeferredHolder<Attribute, Attribute> deferredHolder;

    /**
     * DeferredHolder for NeoForge registration of the boost effect.
     */
    @Nullable public DeferredHolder<MobEffect, MobEffect> effectHolder;

    /**
     * DeferredHolder for NeoForge registration of the potion.
     */
    @Nullable public DeferredHolder<Potion, Potion> potionHolder;

    public SpellSchool(Archetype archetype, Identifier id, int color, ResourceKey<DamageType> damageType, DeferredHolder<Attribute, Attribute> attributeHolder) {
        this(archetype, id, color, damageType, null, null);
        this.deferredHolder = attributeHolder;
    }

    public SpellSchool(Archetype archetype, Identifier id, int color, ResourceKey<DamageType> damageType, Attribute attribute, @Nullable MobEffect boostEffect) {
        this.archetype = archetype;
        this.id = id;
        this.color = color;
        this.damageType = damageType;
        this.ownedAttribute = attribute;
        this.ownedBoostEffect = boostEffect;
    }

    public float attributeBaseValue() {
        return ownedAttribute != null ? (float) ownedAttribute.getDefaultValue() : 0;
    }

    /**
     * Get the owned attribute for deferred registration.
     */
    public Attribute getOwnedAttribute() {
        return ownedAttribute;
    }

    public boolean ownsAttribute() {
        return ownedAttribute != null;
    }

    public boolean isMagicArchetype() {
        return archetype == Archetype.MAGIC;
    }

    // Sources
    public enum Apply { ADD, MULTIPLY }
    public record QueryArgs(LivingEntity entity) { }
    public record Source(Apply apply, Function<QueryArgs, Double> function) { }
    public enum Trait { POWER, HASTE, CRIT_CHANCE, CRIT_DAMAGE }
    private static HashMap<Trait, ArrayList<Source>> emptyTraits() {
        var map = new HashMap<Trait, ArrayList<Source>>();
        for (var trait: Trait.values()) {
            map.put(trait, new ArrayList<>());
        }
        return map;
    }
    private HashMap<Trait, ArrayList<Source>> sources = emptyTraits();

    public void addSource(Trait trait, Apply apply, Function<QueryArgs, Double> function) {
        addSource(trait, new Source(apply, function));
    }

    public void addSource(Trait trait, Source source) {
        sources.get(trait).add(source);
        sources.get(trait).sort(Comparator.comparingInt(a -> a.apply.ordinal()));
    }

    public double getValue(Trait trait, QueryArgs query) {
        var traitSources = sources.get(trait);
        var value = 0F;
        switch (trait) {
            // Base value
            case POWER, CRIT_CHANCE -> { value = 0; }
            case HASTE, CRIT_DAMAGE -> { value = 1; }
        }
        var multiplier = 1F;
        for (var source: traitSources) {
            switch (source.apply) {
                case ADD -> value += source.function.apply(query);
                case MULTIPLY -> multiplier += source.function.apply(query);
            };
        }
        value *= multiplier;
        return value;
    }
}
