package com.ultra.megamod.lib.skilltree.skills;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.api.datagen.SpellBuilder;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.fx.ParticleBatch;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineParticles;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;

import java.util.List;

public class SkillsCommon {
    public static final float WIZARD_WARD_CHANCE = 0.25F;
    public static final float WIZARD_WARD_DURATION = 8F;
    public static final long ARCANE_COLOR = Color.from(SpellSchools.ARCANE.color).toRGBA();
    public static final long FROST_COLOR = Color.from(SpellSchools.FROST.color).toRGBA();
    public static final long HOLY_COLOR = Color.HOLY.toRGBA();
    public static final Color MIGHT_COLOR = Color.from(0xccffff);
    static final Identifier HOLY_DECELERATE = SpellEngineParticles.MagicParticles.get(
            SpellEngineParticles.MagicParticles.Shape.HOLY,
            SpellEngineParticles.MagicParticles.Motion.DECELERATE
    ).id();
    static final Identifier SPARK_DECELERATE = SpellEngineParticles.MagicParticles.get(
            SpellEngineParticles.MagicParticles.Shape.SPARK,
            SpellEngineParticles.MagicParticles.Motion.DECELERATE
    ).id();
    static final Identifier SPARK_FLOAT = SpellEngineParticles.MagicParticles.get(
            SpellEngineParticles.MagicParticles.Shape.SPARK,
            SpellEngineParticles.MagicParticles.Motion.FLOAT
    ).id();
    static final Identifier HEAL_DECELERATE = SpellEngineParticles.MagicParticles.get(
            SpellEngineParticles.MagicParticles.Shape.HEAL,
            SpellEngineParticles.MagicParticles.Motion.DECELERATE
    ).id();

    public static Spell createModifierAlikePassiveSpell() {
        var spell = SpellBuilder.createSpellPassive();
        spell.range = 0;
        spell.tooltip = new Spell.Tooltip();
        spell.tooltip.show_activation = false;
        return spell;
    }

    public static ParticleBatch[] poisonImpactParticles() {
        return new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPARK,
                                SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        10, 0.5F, 0.8F)
                        .color(Color.POISON_MID.toRGBA()),
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPARK,
                                SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        10, 0.5F, 0.8F)
                        .color(Color.POISON_DARK.toRGBA()),
        };
    }

    public static ParticleBatch[] leechImpactParticles() {
        return new ParticleBatch[]{
                new ParticleBatch(SPARK_FLOAT.toString(),
                        ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.CENTER,
                        15, 0.02F, 0.1F)
                        .color(Color.BLOOD.toRGBA()),
                new ParticleBatch(SPARK_DECELERATE.toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.08F, 0.12F)
                        .invert()
                        .preSpawnTravel(5)
                        .followEntity(true)
                        .color(Color.BLOOD.toRGBA()),
                new ParticleBatch(
                        SpellEngineParticles.ground_glow.id().toString(),
                        ParticleBatch.Shape.LINE_VERTICAL, ParticleBatch.Origin.GROUND,
                        1, 0.0F, 0.F)
                        .followEntity(true)
                        .scale(0.8F)
                        .color(Color.BLOOD.alpha(0.2F).toRGBA())
        };
    }

    public static void explosionImpact(Spell spell, float coefficient) {
        var impact = SpellBuilder.Impacts.damage(coefficient, 0.2F);
        spell.area_impact = SpellBuilder.Complex.fireExplosion(2.5F);
        spell.impacts = List.of(impact);
    }
}
