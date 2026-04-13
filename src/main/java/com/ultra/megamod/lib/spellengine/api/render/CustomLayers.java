package com.ultra.megamod.lib.spellengine.api.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Custom RenderType factories for spell beams, projectiles, and spell effects.
 * Adapted for NeoForge 1.21.11's render pipeline.
 *
 * In 1.21.11, RenderType creation is more restricted. We use the existing
 * RenderTypes utility methods that match the needed visual properties.
 */
public class CustomLayers {

    private static final Identifier BLOCK_ATLAS =
            Identifier.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png");

    /**
     * Beam render type - uses the beacon beam shader for proper glow.
     */
    public static RenderType beam(Identifier texture, boolean cull, boolean transparent) {
        return RenderTypes.beaconBeam(texture, transparent);
    }

    /**
     * Spell object render type - varies by light emission level.
     * Uses block atlas texture.
     */
    public static RenderType spellObject(LightEmission lightEmission) {
        return switch (lightEmission) {
            case RADIATE -> RenderTypes.entityTranslucentEmissive(BLOCK_ATLAS);
            case GLOW -> RenderTypes.beaconBeam(BLOCK_ATLAS, false);
            case NONE -> RenderTypes.entityTranslucent(BLOCK_ATLAS);
        };
    }

    /**
     * Spell object render type with specific texture.
     */
    public static RenderType spellObject(Identifier texture, LightEmission lightEmission, boolean translucent) {
        return switch (lightEmission) {
            case RADIATE -> RenderTypes.entityTranslucentEmissive(texture);
            case GLOW -> RenderTypes.beaconBeam(texture, translucent);
            case NONE -> translucent
                    ? RenderTypes.entityTranslucent(texture)
                    : RenderTypes.entitySolid(texture);
        };
    }

    /**
     * Spell effect render type using block atlas.
     */
    public static RenderType spellEffect(LightEmission lightEmission, boolean translucent) {
        return spellObject(BLOCK_ATLAS, lightEmission, translucent);
    }

    /**
     * Projectile render type.
     */
    public static RenderType projectile(LightEmission lightEmission) {
        return spellObject(lightEmission);
    }
}
