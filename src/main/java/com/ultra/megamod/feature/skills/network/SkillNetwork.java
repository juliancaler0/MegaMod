/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 *  net.neoforged.neoforge.network.registration.PayloadRegistrar
 */
package com.ultra.megamod.feature.skills.network;

import com.ultra.megamod.feature.skills.network.SkillActionPayload;
import com.ultra.megamod.feature.skills.network.SkillSyncPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class SkillNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToClient(SkillSyncPayload.TYPE, SkillSyncPayload.STREAM_CODEC, SkillSyncPayload::handleOnClient);
        registrar.playToServer(SkillActionPayload.TYPE, SkillActionPayload.STREAM_CODEC, SkillActionPayload::handleOnServer);
    }
}

