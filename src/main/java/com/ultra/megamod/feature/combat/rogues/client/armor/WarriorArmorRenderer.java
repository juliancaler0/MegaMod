package com.ultra.megamod.feature.combat.rogues.client.armor;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRenderer;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRendererConfig;
import com.ultra.megamod.lib.azurelib.common.render.layer.AzArmorTrimLayer;
import net.minecraft.resources.Identifier;

public class WarriorArmorRenderer extends AzArmorRenderer {
    public static WarriorArmorRenderer warrior() {
        return new WarriorArmorRenderer("warrior_armor", "warrior_armor", "warrior_armor_generic");
    }
    public static WarriorArmorRenderer berserker() {
        return new WarriorArmorRenderer("warrior_armor", "berserker_armor", "berserker_armor_generic");
    }
    public static WarriorArmorRenderer netheriteBerserker() {
        return new WarriorArmorRenderer("warrior_armor", "netherite_berserker_armor", "netherite_berserker_armor_generic");
    }

    public WarriorArmorRenderer(String modelName, String textureName, String trimTextureName) {
        super(AzArmorRendererConfig.builder(
                        Identifier.fromNamespaceAndPath(MegaMod.MODID, "geo/" + modelName + ".geo.json"),
                        Identifier.fromNamespaceAndPath(MegaMod.MODID, "textures/armor/" + textureName + ".png"))
                .addRenderLayer(new AzArmorTrimLayer(Identifier.fromNamespaceAndPath(MegaMod.MODID, "armor/trim/" + trimTextureName), false))
                .build()
        );
    }
}
