package com.ultra.megamod.feature.combat.paladins.client.armor;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRenderer;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRendererConfig;
import com.ultra.megamod.lib.azurelib.common.render.layer.AzArmorTrimLayer;
import net.minecraft.resources.Identifier;

public class PaladinArmorRenderer extends AzArmorRenderer {
    public static PaladinArmorRenderer paladin() {
        return new PaladinArmorRenderer("paladin_armor", "paladin_armor", "paladin_armor_generic");
    }
    public static PaladinArmorRenderer crusader() {
        return new PaladinArmorRenderer("paladin_armor", "crusader_armor", "crusader_armor_generic");
    }
    public static PaladinArmorRenderer netheriteCrusader() {
        return new PaladinArmorRenderer("paladin_armor", "netherite_crusader_armor", "netherite_crusader_armor_generic");
    }

    public PaladinArmorRenderer(String modelName, String textureName, String trimTextureName) {
        super(AzArmorRendererConfig.builder(
                        Identifier.fromNamespaceAndPath(MegaMod.MODID, "geo/" + modelName + ".geo.json"),
                        Identifier.fromNamespaceAndPath(MegaMod.MODID, "textures/armor/" + textureName + ".png"))
                .addRenderLayer(new AzArmorTrimLayer(Identifier.fromNamespaceAndPath(MegaMod.MODID, "armor/trim/" + trimTextureName), false))
                .build()
        );
    }
}
