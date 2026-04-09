package com.ultra.megamod.feature.hud.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class HudNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToClient(PartyHealthPayload.TYPE, PartyHealthPayload.STREAM_CODEC, PartyHealthPayload::handleOnClient);
        registrar.playToClient(TrackerSyncPayload.TYPE, TrackerSyncPayload.STREAM_CODEC, TrackerSyncPayload::handleOnClient);
        registrar.playToClient(ComboPayload.TYPE, ComboPayload.STREAM_CODEC, ComboPayload::handleOnClient);
        registrar.playToClient(LootPickupPayload.TYPE, LootPickupPayload.STREAM_CODEC, LootPickupPayload::handleOnClient);
        registrar.playToClient(ScreenShakePayload.TYPE, ScreenShakePayload.STREAM_CODEC, ScreenShakePayload::handleOnClient);
        registrar.playToClient(DeathRecapPayload.TYPE, DeathRecapPayload.STREAM_CODEC, DeathRecapPayload::handleOnClient);
        registrar.playToClient(com.ultra.megamod.feature.hud.combos.CombatComboPayload.TYPE, com.ultra.megamod.feature.hud.combos.CombatComboPayload.STREAM_CODEC, com.ultra.megamod.feature.hud.combos.CombatComboPayload::handleOnClient);
    }
}
