package com.ultra.megamod.lib.accessories.owo.util.pond;

import com.ultra.megamod.lib.accessories.owo.ui.core.PositionedRectangle;
import org.jetbrains.annotations.Nullable;

public interface OwoSlotExtension {

    void owo$setDisabledOverride(boolean disabled);

    boolean owo$getDisabledOverride();

    void owo$setScissorArea(@Nullable PositionedRectangle scissor);

    @Nullable PositionedRectangle owo$getScissorArea();
}
