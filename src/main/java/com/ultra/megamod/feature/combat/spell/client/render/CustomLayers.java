package com.ultra.megamod.feature.combat.spell.client.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Custom RenderType factories for spell beams, projectiles, and spell effects.
 * Ported from SpellEngine (net.spell_engine.api.render.CustomLayers).
 * Adapted for NeoForge 1.21.11's render pipeline.
 *
 * In 1.21.11, RenderType creation is more restricted. We use the existing
 * RenderTypes utility methods that match the needed visual properties.
 */
public class CustomLayers {

    /**
     * Beam render type — uses the beacon beam shader for proper glow.
     */
    public static RenderType beam(Identifier texture, boolean cull, boolean transparent) {
        // In 1.21.11, use the beacon beam render type for beams
        return RenderTypes.beaconBeam(texture, transparent);
    }

    /**
     * Spell object render type — varies by light emission level.
     */
    public static RenderType spellObject(LightEmission lightEmission) {
        return switch (lightEmission) {
            case RADIATE -> RenderTypes.entityTranslucentEmissive(
                    Identifier.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png"));
            case GLOW -> RenderTypes.beaconBeam(
                    Identifier.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png"), false);
            case NONE -> RenderTypes.entityTranslucent(
                    Identifier.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png"));
        };
    }

    public static RenderType spellObject(Identifier texture, LightEmission lightEmission, boolean translucent) {
        return switch (lightEmission) {
            case RADIATE -> RenderTypes.entityTranslucentEmissive(texture);
            case GLOW -> RenderTypes.beaconBeam(texture, translucent);
            case NONE -> translucent
                    ? RenderTypes.entityTranslucent(texture)
                    : RenderTypes.entitySolid(texture);
        };
    }

    public static RenderType spellEffect(LightEmission lightEmission, boolean translucent) {
        return spellObject(Identifier.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png"),
                lightEmission, translucent);
    }

    public static RenderType projectile(LightEmission lightEmission) {
        return spellObject(lightEmission);
    }
}
