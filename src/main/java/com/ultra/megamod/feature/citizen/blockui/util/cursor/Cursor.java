package com.ultra.megamod.feature.citizen.blockui.util.cursor;

import com.ultra.megamod.feature.citizen.blockui.util.cursor.CursorUtils.StandardCursor;
import net.minecraft.resources.Identifier;

/**
 * Interface to wrap various cursors.
 * Simplified from BlockUI - removed custom cursor texture support for now.
 */
@FunctionalInterface
public interface Cursor
{
    /** Probably arrow, but OS dependent */
    public static final Cursor DEFAULT = named(() -> CursorUtils.setStandardCursor(StandardCursor.DEFAULT), StandardCursor.DEFAULT);
    public static final Cursor ARROW = named(() -> CursorUtils.setStandardCursor(StandardCursor.ARROW), StandardCursor.ARROW);
    public static final Cursor TEXT_CURSOR = named(() -> CursorUtils.setStandardCursor(StandardCursor.TEXT_CURSOR), StandardCursor.TEXT_CURSOR);
    public static final Cursor CROSSHAIR = named(() -> CursorUtils.setStandardCursor(StandardCursor.CROSSHAIR), StandardCursor.CROSSHAIR);
    public static final Cursor HAND = named(() -> CursorUtils.setStandardCursor(StandardCursor.HAND), StandardCursor.HAND);
    public static final Cursor HORIZONTAL_RESIZE = named(() -> CursorUtils.setStandardCursor(StandardCursor.HORIZONTAL_RESIZE), StandardCursor.HORIZONTAL_RESIZE);
    public static final Cursor VERTICAL_RESIZE = named(() -> CursorUtils.setStandardCursor(StandardCursor.VERTICAL_RESIZE), StandardCursor.VERTICAL_RESIZE);
    public static final Cursor RESIZE = named(() -> CursorUtils.setStandardCursor(StandardCursor.RESIZE), StandardCursor.RESIZE);

    // TODO: Port custom cursor texture support if needed
    // public static Cursor of(final Identifier resLoc) { ... }

    /**
     * Apply cursor to main window
     */
    void apply();

    static Cursor named(final Runnable applier, final Object name)
    {
        return new Cursor()
        {
            @Override
            public void apply()
            {
                applier.run();
            }

            @Override
            public String toString()
            {
                return "Cursor: " + name;
            }
        };
    }
}
