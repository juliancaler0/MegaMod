package com.ultra.megamod.feature.combat.wizards.client.effect;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.spell.client.render.CustomLayers;
import com.ultra.megamod.feature.combat.spell.client.render.LightEmission;
import com.ultra.megamod.lib.spellengine.api.render.OrbitingEffectRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.List;

public class ArcaneChargeRenderer extends OrbitingEffectRenderer {
    public static final Identifier modelId = Identifier.fromNamespaceAndPath(MegaMod.MODID, "spell_effect/arcane_charge");
    private static final RenderType GLOWING_RENDER_LAYER =
            CustomLayers.spellEffect(LightEmission.GLOW, true);

    public ArcaneChargeRenderer() {
        super(List.of(new Model(GLOWING_RENDER_LAYER, modelId)), 0.5F, 0.6F);
    }
}
