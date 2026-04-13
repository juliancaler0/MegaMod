package com.ultra.megamod.lib.accessories.pond;

import com.ultra.megamod.lib.accessories.api.client.tooltip.DeferredTooltip;
import org.jetbrains.annotations.Nullable;

public interface DeferredTooltipGetter {
    @Nullable DeferredTooltip accessories$getTooltip();
}
