package com.ultra.megamod.lib.owo.ui.hud;

import com.ultra.megamod.lib.owo.ui.container.FlowLayout;
import com.ultra.megamod.lib.owo.ui.core.Positioning;
import com.ultra.megamod.lib.owo.ui.core.Sizing;
import com.ultra.megamod.lib.owo.ui.core.UIComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Very simple extension of {@link com.ultra.megamod.lib.owo.ui.container.FlowLayout} that
 * does not allow children to be layout-positioned, used by {@link Hud}
 */
public class HudContainer extends FlowLayout {

    protected HudContainer(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);
    }

    @Override
    protected void mountChild(@Nullable UIComponent child, Consumer<UIComponent> layoutFunc) {
        if (child == null) return;

        if (child.positioning().get().type == Positioning.Type.LAYOUT) {
            throw new IllegalStateException("owo-ui HUD components must be explicitly positioned");
        } else {
            super.mountChild(child, layoutFunc);
        }
    }
}
