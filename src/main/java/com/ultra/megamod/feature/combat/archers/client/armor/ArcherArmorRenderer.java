package com.ultra.megamod.feature.combat.archers.client.armor;

import com.ultra.megamod.feature.combat.archers.ArchersMod;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRenderer;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRendererConfig;
import com.ultra.megamod.lib.azurelib.common.render.layer.AzArmorTrimLayer;
import net.minecraft.resources.Identifier;

public class ArcherArmorRenderer extends AzArmorRenderer {
    public static ArcherArmorRenderer archer() {
        return new ArcherArmorRenderer("archer_armor", "archer_armor", "archer_armor_generic");
    }
    public static ArcherArmorRenderer ranger() {
        return new ArcherArmorRenderer("ranger_armor", "ranger_armor", "ranger_armor_generic");
    }
    public static ArcherArmorRenderer netheriteRanger() {
        return new ArcherArmorRenderer("ranger_armor", "netherite_ranger_armor", "ranger_armor_generic");
    }

    public ArcherArmorRenderer(String modelName, String textureName, String trimTextureName) {
        super(AzArmorRendererConfig.builder(
                        Identifier.fromNamespaceAndPath(ArchersMod.ID, "geo/" + modelName + ".geo.json"),
                        Identifier.fromNamespaceAndPath(ArchersMod.ID, "textures/armor/" + textureName + ".png"))
                .addRenderLayer(new AzArmorTrimLayer(Identifier.fromNamespaceAndPath(ArchersMod.ID, "armor/trim/" + trimTextureName), false))
                .build()
        );
    }
}
