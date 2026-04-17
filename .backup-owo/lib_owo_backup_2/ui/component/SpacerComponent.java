package com.ultra.megamod.lib.owo.ui.component;

import com.ultra.megamod.lib.owo.ui.base.BaseUIComponent;
import com.ultra.megamod.lib.owo.ui.core.OwoUIGraphics;
import com.ultra.megamod.lib.owo.ui.core.Sizing;
import com.ultra.megamod.lib.owo.ui.parsing.UIParsing;
import org.w3c.dom.Element;

public class SpacerComponent extends BaseUIComponent {

    protected SpacerComponent(int percent) {
        this.sizing(Sizing.expand(percent));
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {}

    public static SpacerComponent parse(Element element) {
        if (!element.hasAttribute("percent")) return UIComponents.spacer();
        return UIComponents.spacer(UIParsing.parseUnsignedInt(element.getAttributeNode("percent")));
    }
}
