package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.feature.attributes.MegaModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Magic schools ported from SpellPower. Each maps to an existing MegaMod attribute.
 */
public enum SpellSchool {
    ARCANE("Arcane", 0xFF7E3BFF, MegaModAttributes.ARCANE_POWER),
    FIRE("Fire", 0xFFFF6B1A, MegaModAttributes.FIRE_DAMAGE_BONUS),
    FROST("Frost", 0xFF4DA6FF, MegaModAttributes.ICE_DAMAGE_BONUS),
    HEALING("Healing", 0xFFCCFF00, MegaModAttributes.HEALING_POWER),
    LIGHTNING("Lightning", 0xFFFFFF00, MegaModAttributes.LIGHTNING_DAMAGE_BONUS),
    SOUL("Soul", 0xFF9966CC, MegaModAttributes.SOUL_POWER),
    PHYSICAL_MELEE("Physical", 0xFFCC8844, Attributes.ATTACK_DAMAGE),
    PHYSICAL_RANGED("Ranged", 0xFF88CC44, MegaModAttributes.RANGED_DAMAGE);

    public final String displayName;
    public final int color;
    public final Holder<Attribute> powerAttribute;

    SpellSchool(String displayName, int color, Holder<Attribute> powerAttribute) {
        this.displayName = displayName;
        this.color = color;
        this.powerAttribute = powerAttribute;
    }
}
