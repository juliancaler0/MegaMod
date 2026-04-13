package com.ultra.megamod.lib.accessories.client.gui.components;

import com.ultra.megamod.lib.accessories.owo.ui.container.CollapsibleContainer;
import com.ultra.megamod.lib.accessories.owo.ui.container.FlowLayout;
import com.ultra.megamod.lib.accessories.owo.ui.core.*;
import net.minecraft.network.chat.Component;

public class ExtendedCollapsibleContainer extends CollapsibleContainer {
    public ExtendedCollapsibleContainer(Sizing horizontalSizing, Sizing verticalSizing, boolean expanded) {
        super(horizontalSizing, verticalSizing, Component.empty(), expanded);
        this.margins(Insets.top(0));

        this.configure((CollapsibleContainer component) -> {
            component.horizontalAlignment(HorizontalAlignment.CENTER);

            var tLayout = component.titleLayout();

            tLayout.padding(Insets.of(0));

            var children = tLayout.children();
            if (children.size() > 1) {
                var spinyThing = children.get(1);
                spinyThing.sizing(Sizing.fixed(9));

                var contentChildren = component.children();
                if (contentChildren.size() > 1) {
                    var contentLayout = contentChildren.get(1);

                    if (expanded) {
                        contentLayout.margins(Insets.top(-2));
                        spinyThing.margins(Insets.of(0, 1, 2, 0));
                    } else {
                        contentLayout.margins(Insets.top(0));
                        spinyThing.margins(Insets.of(0, 2, 2, 0));
                    }

                    if (contentLayout instanceof FlowLayout contentFlow) {
                        contentFlow.surface(Surface.BLANK)
                                .padding(Insets.of(0))
                                .horizontalAlignment(HorizontalAlignment.CENTER);
                    }
                }
            }
        });

        this.onToggled().subscribe(nowExpanded -> {
            var contentChildren = this.children();
            if (contentChildren.size() > 1) {
                var contentLayout = contentChildren.get(1);

                if (contentLayout instanceof FlowLayout contentFlow) {
                    var tLayout = titleLayout();
                    var tChildren = tLayout.children();
                    if (tChildren.size() > 1) {
                        var spinyThing = tChildren.get(1);

                        if (nowExpanded) {
                            contentFlow.margins(Insets.top(-2));
                            spinyThing.margins(Insets.of(0, 1, 2, 0));
                        } else {
                            contentFlow.margins(Insets.top(0));
                            spinyThing.margins(Insets.of(0, 2, 2, 0));
                        }
                    }
                }
            }
        });
    }
}
