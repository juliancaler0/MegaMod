package com.ultra.megamod.lib.owo.ui.inject;

import com.ultra.megamod.lib.owo.ui.core.UIComponent;

/**
 * A marker interface for components which consume
 * text input when focused - this is used to prevent handled
 * screens from closing when said component is focused and the
 * inventory key is pressed
 */
public interface GreedyInputUIComponent extends UIComponent {}