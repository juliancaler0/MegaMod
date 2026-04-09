package com.ultra.megamod.feature.citizen.screen.modules;

import com.ultra.megamod.feature.citizen.blockui.controls.Button;

/**
 * Extensible logic for windows, without having to use new base classes.
 * Ported from MineColonies.
 */
public interface IWindowModule
{
    /**
     * Called when the Window is displayed.
     */
    default void onOpened() {}

    /**
     * Called upon each render frame to update the window.
     */
    default void onUpdate() {}

    /**
     * Called when the Window is closed.
     */
    default void onClosed() {}

    /**
     * Method called when any button is clicked.
     *
     * @param button the button clicked.
     */
    default void onButtonClicked(final Button button) {}
}
