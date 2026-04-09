/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 *  net.neoforged.neoforge.network.registration.PayloadRegistrar
 */
package com.ultra.megamod.feature.dungeons.network;

import com.ultra.megamod.feature.dungeons.network.BossMusicPayload;
import com.ultra.megamod.feature.dungeons.network.DungeonSyncPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class DungeonNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToClient(DungeonSyncPayload.TYPE, DungeonSyncPayload.STREAM_CODEC, DungeonSyncPayload::handleOnClient);
        registrar.playToClient(BossMusicPayload.TYPE, BossMusicPayload.STREAM_CODEC, BossMusicPayload::handleOnClient);
    }
}

