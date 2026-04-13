package com.ultra.megamod.feature.combat.paladins.client.effect;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.spellengine.api.render.CustomLayers;
import com.ultra.megamod.lib.spellengine.api.render.LightEmission;
import com.ultra.megamod.lib.spellengine.api.render.OrbitingEffectRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

import java.util.List;

public class DivineProtectionRenderer extends OrbitingEffectRenderer {
    public static final Identifier modelId_base = Identifier.fromNamespaceAndPath(MegaMod.MODID, "spell_effect/divine_protection");
    public static final Identifier modelId_overlay = Identifier.fromNamespaceAndPath(MegaMod.MODID, "spell_effect/divine_protection_glow");

    private static final RenderType BASE_RENDER_LAYER =
            RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
    private static final RenderType GLOWING_RENDER_LAYER =
            CustomLayers.spellEffect(LightEmission.RADIATE, false);

    public DivineProtectionRenderer() {
        super(List.of(
                new Model(GLOWING_RENDER_LAYER, modelId_overlay),
                new Model(BASE_RENDER_LAYER, modelId_base)),
                1F,
                0.35F);
    }
}
