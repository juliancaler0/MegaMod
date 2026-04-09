package com.ultra.megamod.feature.combat.spell.client.render;

import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.Map;

/**
 * Maps LightEmission levels to cached RenderType instances.
 * Ported 1:1 from SpellEngine (net.spell_engine.client.render.SpellModelHelper).
 */
public class SpellModelHelper {
    public static final Map<LightEmission, RenderType> LAYERS = Map.of(
            LightEmission.NONE, CustomLayers.spellObject(LightEmission.NONE),
            LightEmission.GLOW, CustomLayers.spellObject(LightEmission.GLOW),
            LightEmission.RADIATE, CustomLayers.spellObject(LightEmission.RADIATE)
    );
}
