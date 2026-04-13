package com.ultra.megamod.lib.spellengine.client.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import com.ultra.megamod.lib.spellengine.api.render.CustomLayers;
import com.ultra.megamod.lib.spellengine.api.render.LightEmission;

import java.util.Map;

public class SpellModelHelper {
    public static final Map<LightEmission, RenderType> LAYERS = Map.of(
            LightEmission.NONE, CustomLayers.spellObject(LightEmission.NONE),
            LightEmission.GLOW, CustomLayers.spellObject(LightEmission.GLOW),
            LightEmission.RADIATE, CustomLayers.spellObject(LightEmission.RADIATE)
    );
}
