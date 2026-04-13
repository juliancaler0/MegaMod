package com.ultra.megamod.lib.accessories.endec.adapter.format.edm;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;

import java.util.Map;

/**
 * Adapter for io.wispforest.endec.format.edm.EdmEndec.
 * Provides Endec for EDM element types - stub for compatibility.
 */
public final class EdmEndec {
    private EdmEndec() {}

    @SuppressWarnings("unchecked")
    public static final Endec<Map<String, EdmElement<?>>> MAP = (Endec<Map<String, EdmElement<?>>>) (Object) Endec.STRING.mapOf();
}
