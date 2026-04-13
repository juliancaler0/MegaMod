package com.ultra.megamod.lib.accessories.utils;

import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrierDecodable;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrierEncodable;

public interface InstanceEndec {
    void encode(MapCarrierEncodable encoder, SerializationContext ctx);

    void decode(MapCarrierDecodable decoder, SerializationContext ctx);
}
