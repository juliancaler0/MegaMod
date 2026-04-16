package com.ultra.megamod.lib.skilltree.client.effect;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.skilltree.SkillTreeMod;
import com.ultra.megamod.lib.spellengine.api.render.OrbitingEffectRenderer;

import java.util.List;

public class DeflectionEffectRenderer extends OrbitingEffectRenderer {
    public static final Identifier modelId = Identifier.fromNamespaceAndPath(SkillTreeMod.NAMESPACE, "spell_effect/deflection_charge");
    // private static final RenderType GLOWING_RENDER_LAYER = CustomLayers.spellEffect(LightEmission.GLOW, false);
    private static final RenderType BASE_RENDER_LAYER = RenderType.entityTranslucent(TextureAtlas.BLOCK_ATLAS);

    public DeflectionEffectRenderer() {
        super(List.of(new Model(BASE_RENDER_LAYER, modelId)), 0.75F, 0.9F);
        this.orbitingSpeed *= 2F;
    }
}
