package com.ultra.megamod.feature.combat.paladins.client.armor;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRenderer;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRendererConfig;
import com.ultra.megamod.lib.azurelib.common.render.layer.AzArmorTrimLayer;
import net.minecraft.resources.Identifier;

public class PriestArmorRenderer extends AzArmorRenderer {
    public static PriestArmorRenderer priest() {
        return new PriestArmorRenderer("priest_robes", "priest_robe", "priest_robe_generic");
    }
    public static PriestArmorRenderer prior() {
        return new PriestArmorRenderer("priest_robes", "prior_robe", "prior_robe_generic");
    }
    public static PriestArmorRenderer netheritePrior() {
        return new PriestArmorRenderer("priest_robes", "netherite_prior_robe", "netherite_prior_robe_generic");
    }

    public PriestArmorRenderer(String modelName, String textureName, String trimTextureName) {
        super(AzArmorRendererConfig.builder(
                        Identifier.fromNamespaceAndPath(MegaMod.MODID, "geo/" + modelName + ".geo.json"),
                        Identifier.fromNamespaceAndPath(MegaMod.MODID, "textures/armor/" + textureName + ".png"))
                .addRenderLayer(new AzArmorTrimLayer(Identifier.fromNamespaceAndPath(MegaMod.MODID, "armor/trim/" + trimTextureName), false))
                .build()
        );
    }
}
