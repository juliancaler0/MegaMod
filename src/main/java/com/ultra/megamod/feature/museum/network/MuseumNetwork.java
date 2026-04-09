/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 *  net.neoforged.neoforge.network.registration.PayloadRegistrar
 */
package com.ultra.megamod.feature.museum.network;

import com.ultra.megamod.feature.museum.network.MuseumActionPayload;
import com.ultra.megamod.feature.museum.network.OpenMuseumPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MuseumNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToClient(OpenMuseumPayload.TYPE, OpenMuseumPayload.STREAM_CODEC, OpenMuseumPayload::handleOnClient);
        registrar.playToServer(MuseumActionPayload.TYPE, MuseumActionPayload.STREAM_CODEC, MuseumActionPayload::handleOnServer);
    }
}

