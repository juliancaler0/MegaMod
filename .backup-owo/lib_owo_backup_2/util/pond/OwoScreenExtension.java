package com.ultra.megamod.lib.owo.util.pond;

import com.ultra.megamod.lib.owo.ui.core.ParentUIComponent;
import com.ultra.megamod.lib.owo.ui.layers.Layer;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface OwoScreenExtension {
    List<Layer<?, ?>.Instance> owo$getInstancesView();
    <S extends Screen, R extends ParentUIComponent> Layer<S, R>.Instance owo$getInstance(Layer<S, R> layer);

    void owo$updateLayers();

    // ---

    void owo$setBraidLayersState(BraidLayersBinding.LayersState state);
    @Nullable BraidLayersBinding.LayersState owo$getBraidLayersState();
    default @Nullable AppState owo$getBraidLayersApp() {
        var state = this.owo$getBraidLayersState();
        if (state == null) {
            return null;
        }

        return state.app();
    }

}
