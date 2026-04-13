package com.ultra.megamod.feature.combat.rogues.client.armor;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRenderer;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRendererConfig;
import com.ultra.megamod.lib.azurelib.common.render.layer.AzArmorTrimLayer;
import net.minecraft.resources.Identifier;

public class RogueArmorRenderer extends AzArmorRenderer {
    public static RogueArmorRenderer rogue() {
        return new RogueArmorRenderer("rogue_armor", "rogue_armor", "rogue_armor_generic");
    }
    public static RogueArmorRenderer assassin() {
        return new RogueArmorRenderer("rogue_armor", "assassin_armor", "assassin_armor_generic");
    }
    public static RogueArmorRenderer netheriteAssassin() {
        return new RogueArmorRenderer("rogue_armor", "netherite_assassin_armor", "netherite_assassin_armor_generic");
    }

    public RogueArmorRenderer(String modelName, String textureName, String trimTextureName) {
        super(AzArmorRendererConfig.builder(
                        Identifier.fromNamespaceAndPath(MegaMod.MODID, "geo/" + modelName + ".geo.json"),
                        Identifier.fromNamespaceAndPath(MegaMod.MODID, "textures/armor/" + textureName + ".png"))
                .addRenderLayer(new AzArmorTrimLayer(Identifier.fromNamespaceAndPath(MegaMod.MODID, "armor/trim/" + trimTextureName), false))
                .build()
        );
    }
}
