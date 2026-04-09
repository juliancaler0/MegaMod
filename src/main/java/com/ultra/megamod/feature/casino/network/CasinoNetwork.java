package com.ultra.megamod.feature.casino.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class CasinoNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("megamod");

        // Slot machine payloads
        registrar.playToServer(SlotSpinPayload.TYPE, SlotSpinPayload.STREAM_CODEC, SlotSpinPayload::handleOnServer);
        registrar.playToClient(SlotResultPayload.TYPE, SlotResultPayload.STREAM_CODEC, SlotResultPayload::handleOnClient);
        registrar.playToServer(SlotConfigPayload.TYPE, SlotConfigPayload.STREAM_CODEC, SlotConfigPayload::handleOnServer);

        // Blackjack payloads
        registrar.playToServer(BlackjackActionPayload.TYPE, BlackjackActionPayload.STREAM_CODEC, BlackjackActionPayload::handleOnServer);
        registrar.playToClient(BlackjackSyncPayload.TYPE, BlackjackSyncPayload.STREAM_CODEC, BlackjackSyncPayload::handleOnClient);

        // Wheel payloads
        registrar.playToServer(WheelBetPayload.TYPE, WheelBetPayload.STREAM_CODEC, WheelBetPayload::handleOnServer);
        registrar.playToClient(WheelSyncPayload.TYPE, WheelSyncPayload.STREAM_CODEC, WheelSyncPayload::handleOnClient);

        // Baccarat payloads
        registrar.playToServer(BaccaratActionPayload.TYPE, BaccaratActionPayload.STREAM_CODEC, BaccaratActionPayload::handleOnServer);
        registrar.playToClient(BaccaratGameSyncPayload.TYPE, BaccaratGameSyncPayload.STREAM_CODEC, BaccaratGameSyncPayload::handleOnClient);

        // Roulette payloads
        registrar.playToServer(RouletteActionPayload.TYPE, RouletteActionPayload.STREAM_CODEC, RouletteActionPayload::handleOnServer);
        registrar.playToClient(RouletteGameSyncPayload.TYPE, RouletteGameSyncPayload.STREAM_CODEC, RouletteGameSyncPayload::handleOnClient);

        // Craps payloads
        registrar.playToServer(CrapsActionPayload.TYPE, CrapsActionPayload.STREAM_CODEC, CrapsActionPayload::handleOnServer);
        registrar.playToClient(CrapsGameSyncPayload.TYPE, CrapsGameSyncPayload.STREAM_CODEC, CrapsGameSyncPayload::handleOnClient);

        // Chip system payloads
        registrar.playToServer(com.ultra.megamod.feature.casino.chips.ChipActionPayload.TYPE, com.ultra.megamod.feature.casino.chips.ChipActionPayload.STREAM_CODEC, com.ultra.megamod.feature.casino.chips.ChipActionPayload::handleServer);
        registrar.playToClient(com.ultra.megamod.feature.casino.chips.ChipSyncPayload.TYPE, com.ultra.megamod.feature.casino.chips.ChipSyncPayload.STREAM_CODEC, com.ultra.megamod.feature.casino.chips.ChipSyncPayload::handleClient);

        // Screen opening payloads (S2C)
        registrar.playToClient(OpenSlotMachinePayload.TYPE, OpenSlotMachinePayload.STREAM_CODEC, OpenSlotMachinePayload::handleOnClient);
        registrar.playToClient(OpenWheelPayload.TYPE, OpenWheelPayload.STREAM_CODEC, OpenWheelPayload::handleOnClient);
        registrar.playToClient(OpenBlackjackPayload.TYPE, OpenBlackjackPayload.STREAM_CODEC, OpenBlackjackPayload::handleOnClient);
    }
}
