package com.ultra.megamod.lib.spellpower.api;

import com.ultra.megamod.lib.spellpower.SpellPowerMod;
import com.ultra.megamod.lib.spellpower.internals.CustomEntityAttribute;
import com.ultra.megamod.lib.spellpower.internals.SpellStatusEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

import static com.ultra.megamod.lib.spellpower.api.SpellPowerMechanics.PERCENT_ATTRIBUTE_BASELINE;

public class SpellSchools {
    private static final Supplier<Float> defaultBaseValue = () -> SpellPowerMod.safeConfig().base_spell_power;

    // Registration

    /**
     * Default namespace for spell schools
     */
    public static final String DEFAULT_NAMESPACE = SpellPowerMod.ID;
    private static final LinkedHashMap<Identifier, SpellSchool> REGISTRY = new LinkedHashMap<>();

    public static SpellSchool register(SpellSchool school) {
        REGISTRY.put(school.id, school);
        return school;
    }

    public static Set<SpellSchool> all() {
        // Using linked hash set to preserve order
        return new LinkedHashSet<SpellSchool>(REGISTRY.values());
    }

    // Predefined Spell Schools

    public static final SpellSchool GENERIC = register(createMagic("generic", 0x9999BB, 100));
    public static final SpellSchool ARCANE = register(createMagic("arcane", 0xff66ff));
    public static final SpellSchool FIRE = register(createMagic("fire", 0xff3300));
    public static final SpellSchool FROST = register(createMagic("frost", 0xccffff));
    public static final SpellSchool HEALING = register(createMagic("healing", 0x66ff66));
    public static final SpellSchool LIGHTNING = register(createMagic("lightning", 0xffff99));
    public static final SpellSchool SOUL = register(createMagic("soul", 0x2dd4da));

    // School Creation

    public static SpellSchool createMagic(String name, int color) {
        return createMagic(Identifier.fromNamespaceAndPath(DEFAULT_NAMESPACE, name.toLowerCase()), color, defaultBaseValue.get());
    }

    public static SpellSchool createMagic(String name, int color, float base) {
        return createMagic(Identifier.fromNamespaceAndPath(DEFAULT_NAMESPACE, name.toLowerCase()), color, base);
    }

    public static SpellSchool createMagic(Identifier id, int color) {
        return createMagic(id, color, 0);
    }

    public static SpellSchool createMagic(Identifier id, int color, float base) {
        var powerEffect = new SpellStatusEffect(MobEffectCategory.BENEFICIAL, color);

        var translationPrefix = "attribute.name." + id.getNamespace() + ".";
        var attribute = new CustomEntityAttribute(translationPrefix + id.getPath(), base, 0, 2048, id);
        attribute.setSyncable(true);

        return createMagic(id, color, true, attribute, powerEffect);
    }

    public static SpellSchool createMagic(Identifier id, int color, boolean customDamageType, Attribute powerAttribute, MobEffect powerEffect) {
        var school = new SpellSchool(
                SpellSchool.Archetype.MAGIC,
                id,
                color,
                customDamageType ? ResourceKey.create(Registries.DAMAGE_TYPE, id) : DamageTypes.MAGIC,
                powerAttribute,
                powerEffect);
        return configureAsMagic(school);
    }


    public static SpellSchool configureAsMagic(SpellSchool school) {
        school.addSource(SpellSchool.Trait.POWER, new SpellSchool.Source(SpellSchool.Apply.ADD, query -> {
            if (school.deferredHolder != null) {
                return query.entity().getAttributeValue(school.deferredHolder);
            }
            return 0.0;
        }));
        // Spell Power Enchantments added by Enchantments_SpellDamage.attach
        configureSpellHaste(school);
        configureSpellCritChance(school);
        configureSpellCritDamage(school);
        return school;
    }

    public static SpellSchool configureSpellHaste(SpellSchool school) {
        school.addSource(SpellSchool.Trait.HASTE, new SpellSchool.Source(SpellSchool.Apply.ADD, query -> {
            if (SpellPowerMechanics.HASTE.deferredHolder == null) return 0.0;
            var value = query.entity().getAttributeValue(SpellPowerMechanics.HASTE.deferredHolder); // 110
            var rate = (value / PERCENT_ATTRIBUTE_BASELINE);    // For example: 110/100 = 1.1
            return rate - 1;  // 0.1
        }));
        return school;
    }

    public static SpellSchool configureSpellCritChance(SpellSchool school) {
        school.addSource(SpellSchool.Trait.CRIT_CHANCE, new SpellSchool.Source(SpellSchool.Apply.ADD, query ->  {
            if (SpellPowerMechanics.CRITICAL_CHANCE.deferredHolder == null) return 0.0;
            var value = query.entity().getAttributeValue(SpellPowerMechanics.CRITICAL_CHANCE.deferredHolder);    // 20
            return (value / PERCENT_ATTRIBUTE_BASELINE) - 1;    // For example: (120/100) - 1 = 0.25
        }));
        return school;
    }

    public static SpellSchool configureSpellCritDamage(SpellSchool school) {
        school.addSource(SpellSchool.Trait.CRIT_DAMAGE, new SpellSchool.Source(SpellSchool.Apply.ADD, query -> {
            if (SpellPowerMechanics.CRITICAL_DAMAGE.deferredHolder == null) return 0.0;
            var value = query.entity().getAttributeValue(SpellPowerMechanics.CRITICAL_DAMAGE.deferredHolder);    // 160
            var rate = (value / PERCENT_ATTRIBUTE_BASELINE);    // For example: 160/100 = 1.6
            return rate - 1;    // 0.6
        }));
        return school;
    }

    // Utility

    @Nullable public static SpellSchool getSchool(String idString) {
        var string = idString.toLowerCase(Locale.US);
        var id = Identifier.parse(string);
        // Replacing default namespace
        if (id.getNamespace().equals("minecraft")) {
            id = Identifier.fromNamespaceAndPath(DEFAULT_NAMESPACE, id.getPath());
        }
        return REGISTRY.get(id);
    }
}
