package com.ultra.megamod.lib.spellpower.api;

import com.ultra.megamod.lib.spellpower.SpellPowerMod;
import com.ultra.megamod.lib.spellpower.internals.CustomEntityAttribute;
import com.ultra.megamod.lib.spellpower.internals.SpellStatusEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class SpellPowerMechanics {
    public final static float PERCENT_ATTRIBUTE_BASELINE = 100F;
    public static String translationPrefix() {
        return "attribute.name." + SpellPowerMod.ID + ".";
    }

    public static class Entry {
        public final String name;
        public final Identifier id;
        public final float defaultValue, min, max;
        public final CustomEntityAttribute attribute;
        public final SpellStatusEffect boostEffect;
        public @Nullable AttributeModifier innateModifier;

        @Nullable
        public DeferredHolder<Attribute, Attribute> deferredHolder;

        @Nullable
        public DeferredHolder<MobEffect, MobEffect> effectHolder;

        public Entry(String name, float defaultValue, float min, float max, int color) {
            this.name = name;
            this.id = Identifier.fromNamespaceAndPath(SpellPowerMod.ID, name);
            this.defaultValue = defaultValue;
            this.min = min;
            this.max = max;
            this.attribute = new CustomEntityAttribute(translationPrefix() + name, defaultValue, min, max, id);
            this.attribute.setSyncable(true);
            this.boostEffect = new SpellStatusEffect(MobEffectCategory.BENEFICIAL, color);
        }

        public Entry innateModifier(AttributeModifier.Operation operation, float value) {
            innateModifier = new AttributeModifier(ModifierDefinitions.INNATE_BONUS, value, operation);
            return this;
        }
    }

    public static final HashMap<String, Entry> all = new HashMap<>();

    public static Entry entry(String name, float defaultValue, float min, float max, int color) {
        var entry = new Entry(name, defaultValue, min, max, color);
        all.put(name, entry);
        return entry;
    }

    // Prefixed with "sp_" to avoid collision with MegaModAttributes which already has
    // critical_chance, critical_damage, and spell_haste under the same "megamod" namespace.
    // Original SpellPower used namespace "spell_power:" which was distinct from "megamod:",
    // but since both now share "megamod:" namespace, we need unique registry names.
    public static final Entry CRITICAL_CHANCE = entry("sp_critical_chance", PERCENT_ATTRIBUTE_BASELINE, PERCENT_ATTRIBUTE_BASELINE, PERCENT_ATTRIBUTE_BASELINE * 10, 0x66ccff)
            .innateModifier(AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ((float) SpellPowerMod.safeConfig().base_spell_critical_chance_percentage) / 100F);
    public static final Entry CRITICAL_DAMAGE = entry("sp_critical_damage", PERCENT_ATTRIBUTE_BASELINE, PERCENT_ATTRIBUTE_BASELINE, PERCENT_ATTRIBUTE_BASELINE * 10, 0x66ffcc)
            .innateModifier(AttributeModifier.Operation.ADD_MULTIPLIED_BASE, ((float) SpellPowerMod.safeConfig().base_spell_critical_damage_percentage) / 100F);
    public static final Entry HASTE = entry("sp_haste", PERCENT_ATTRIBUTE_BASELINE, PERCENT_ATTRIBUTE_BASELINE, PERCENT_ATTRIBUTE_BASELINE * 10, 0xcc99ff);

    // Upstream SpellPower exposed this attribute as `spell_power:haste`; the spell_power_haste.json
    // enchantment datapack file references it as `megamod:haste`. Register it with the upstream
    // defaults (base 100, min 100, max 1000) so datapack load resolves the reference.
    // Source: Ref Code/Combat and Weapons/SpellPower-1.21.1/common/src/main/java/net/spell_power/api/SpellPowerMechanics.java:75
    public static final Entry HASTE_UNPREFIXED = entry("haste", PERCENT_ATTRIBUTE_BASELINE, PERCENT_ATTRIBUTE_BASELINE, PERCENT_ATTRIBUTE_BASELINE * 10, 0xcc99ff);
}
