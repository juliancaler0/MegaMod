package com.ultra.megamod.lib.spellengine.api.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.ultra.megamod.lib.spellengine.utils.AttributeModifierUtil;
import com.ultra.megamod.lib.spellpower.api.SpellSchool;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;

public class ExternalSpellSchools {
    public static final SpellSchool PHYSICAL_MELEE = new SpellSchool(SpellSchool.Archetype.MELEE,
            Identifier.fromNamespaceAndPath("megamod", "physical_melee"),
            0xb3b3b3,
            DamageTypes.PLAYER_ATTACK,
            Attributes.ATTACK_DAMAGE.value(), null);
    public static final SpellSchool PHYSICAL_RANGED = new SpellSchool(SpellSchool.Archetype.ARCHERY,
            Identifier.fromNamespaceAndPath("megamod", "physical_ranged"),
            0x805e4d,
            DamageTypes.ARROW,
            Attributes.ATTACK_DAMAGE.value(), null);
    public static final SpellSchool DEFENSE = new SpellSchool(SpellSchool.Archetype.MELEE,
            Identifier.fromNamespaceAndPath("megamod", "defense"),
            0xcccccc,
            DamageTypes.PLAYER_ATTACK,
            Attributes.ARMOR.value(), null);
    public static final SpellSchool HEALTH = new SpellSchool(SpellSchool.Archetype.MELEE,
            Identifier.fromNamespaceAndPath("megamod", "health"),
            0xcc0000,
            DamageTypes.PLAYER_ATTACK,
            Attributes.MAX_HEALTH.value(), null);

    private static boolean initialized = false;
    public static void init() {
        if (initialized) { return; }

        PHYSICAL_MELEE.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            return query.entity().getAttributeValue(Attributes.ATTACK_DAMAGE);
        });
        PHYSICAL_MELEE.addSource(SpellSchool.Trait.HASTE, SpellSchool.Apply.ADD, query -> {
            return AttributeModifierUtil.multipliersOf(Attributes.ATTACK_SPEED, query.entity()) - 1.0;
        });
        SpellSchools.configureSpellHaste(PHYSICAL_MELEE);
        SpellSchools.register(PHYSICAL_MELEE);

        // Physical ranged uses vanilla attack damage as power source
        PHYSICAL_RANGED.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            return query.entity().getAttributeValue(Attributes.ATTACK_DAMAGE);
        });
        SpellSchools.register(PHYSICAL_RANGED);

        DEFENSE.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            return query.entity().getAttributeValue(Attributes.ARMOR);
        });
        SpellSchools.register(DEFENSE);

        HEALTH.addSource(SpellSchool.Trait.POWER, SpellSchool.Apply.ADD, query -> {
            return query.entity().getAttributeValue(Attributes.MAX_HEALTH);
        });
        SpellSchools.register(HEALTH);

        initialized = true;
    }
}
