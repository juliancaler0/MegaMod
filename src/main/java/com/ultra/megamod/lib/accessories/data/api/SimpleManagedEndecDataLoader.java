package com.ultra.megamod.lib.accessories.data.api;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import java.util.Map;

public class SimpleManagedEndecDataLoader<T> extends ManagedEndecDataLoader<T, T> {

    protected SimpleManagedEndecDataLoader(Identifier id, String type, Endec<T> endec, PackType packType) {
        super(id, type, endec, endec, packType);
    }

    @Override
    public Map<Identifier, T> mapFrom(Map<Identifier, T> rawData) {
        return rawData;
    }
}
