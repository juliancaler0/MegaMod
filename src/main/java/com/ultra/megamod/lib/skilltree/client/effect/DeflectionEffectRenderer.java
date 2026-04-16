package com.ultra.megamod.lib.skilltree.client.effect;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.skilltree.SkillTreeMod;
import com.ultra.megamod.lib.spellengine.api.render.OrbitingEffectRenderer;

import java.util.List;

/**
 * TODO: Reimplement with 1.21.11 RenderType API (entityTranslucent signature changed)
 */
public class DeflectionEffectRenderer extends OrbitingEffectRenderer {
    public static final Identifier modelId = Identifier.fromNamespaceAndPath(SkillTreeMod.NAMESPACE, "spell_effect/deflection_charge");

    public DeflectionEffectRenderer() {
        super(List.of(), 0.75F, 0.9F);
        this.orbitingSpeed *= 2F;
    }
}
