package com.ultra.megamod.feature.combat.spell.client.util;

import net.minecraft.resources.Identifier;

/**
 * Utility for spell texture/icon path resolution.
 * Ported 1:1 from SpellEngine (net.spell_engine.client.util.SpellRender).
 */
public class SpellRender {
    public static Identifier iconTexture(Identifier spellId) {
        return Identifier.fromNamespaceAndPath(spellId.getNamespace(), "textures/spell/" + spellId.getPath() + ".png");
    }
}
