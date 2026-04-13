package com.ultra.megamod.feature.combat.paladins.effect;

import com.ultra.megamod.lib.spellengine.api.spell.fx.ParticleBatch;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineParticles;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class DivineProtectionStatusEffect extends MobEffect {
    public DivineProtectionStatusEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static final ParticleBatch particles = new ParticleBatch(
            SpellEngineParticles.MagicParticles.get(
                    SpellEngineParticles.MagicParticles.Shape.HOLY,
                    SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
            ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.CENTER,
            25, 0.1F, 0.15F)
            .color(Color.HOLY.toRGBA());
}
