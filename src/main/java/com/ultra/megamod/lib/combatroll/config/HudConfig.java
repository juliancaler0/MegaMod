package com.ultra.megamod.lib.combatroll.config;

import com.ultra.megamod.lib.combatroll.client.gui.HudElement;
import org.joml.Vector2f;

public class HudConfig {
    public HudElement rollWidget;

    public static HudConfig createDefault() {
        var config = new HudConfig();
        config.rollWidget = createDefaultRollWidget();
        return config;
    }

    public static HudElement createDefaultRollWidget() {
        var origin = HudElement.Origin.BOTTOM;
        var initial = origin.initialOffset();
        var offset = new Vector2f(initial.x + 108, initial.y);
        return new HudElement(origin, offset);
    }
}
