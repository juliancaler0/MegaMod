package com.ultra.megamod.lib.accessories.owo.ui.container;

import com.ultra.megamod.lib.accessories.owo.ui.core.*;
import com.ultra.megamod.lib.accessories.owo.ui.core.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub for OWO's GridLayout.
 */
public class GridLayout extends FlowLayout {

    public GridLayout(Sizing horizontalSizing, Sizing verticalSizing, int rows, int columns) {
        super(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL);
    }

    public GridLayout child(Component child, int row, int column) {
        if (child != null) super.child(child);
        return this;
    }
}
