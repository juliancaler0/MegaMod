package com.ultra.megamod.feature.citizen.ornament;

import java.util.List;

/**
 * Interface for blocks that support combinatorial retexturing via the Architects Cutter.
 * Each block declares its texture components (e.g. frame, panel, accent) and the Cutter
 * maps player-supplied materials to those components.
 */
public interface IMateriallyTexturedBlock {

    /**
     * Returns the list of texture components this block supports.
     * The order matters — component index maps to input slot position.
     */
    List<IMateriallyTexturedBlockComponent> getComponents();

    /**
     * Maximum number of texture slots. Defaults to the component count.
     */
    default int getMaxTextureCount() {
        return getComponents().size();
    }
}
