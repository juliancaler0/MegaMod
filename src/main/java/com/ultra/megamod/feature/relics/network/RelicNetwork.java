package com.ultra.megamod.feature.relics.network;

import com.ultra.megamod.feature.attributes.network.CombatTextPayload;
import com.ultra.megamod.feature.relics.network.AbilityCooldownSyncPayload;
import com.ultra.megamod.feature.relics.network.AccessoryPayload;
import com.ultra.megamod.feature.relics.network.AccessoryQuickEquipPayload;
import com.ultra.megamod.feature.relics.network.OpenRelicScreenPayload;
import com.ultra.megamod.feature.relics.network.RelicExchangePayload;
import com.ultra.megamod.feature.relics.network.RelicTweakPayload;
import com.ultra.megamod.feature.relics.network.WeaponAbilitySyncPayload;
import com.ultra.megamod.feature.relics.research.RerollPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RelicNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToServer(AccessoryPayload.AccessoryEquipPayload.TYPE, AccessoryPayload.AccessoryEquipPayload.STREAM_CODEC, AccessoryPayload.AccessoryEquipPayload::handleOnServer);
        registrar.playToClient(AccessoryPayload.AccessorySyncPayload.TYPE, AccessoryPayload.AccessorySyncPayload.STREAM_CODEC, AccessoryPayload.AccessorySyncPayload::handleOnClient);
        registrar.playToServer(AccessoryQuickEquipPayload.TYPE, AccessoryQuickEquipPayload.STREAM_CODEC, AccessoryQuickEquipPayload::handleOnServer);
        // Phase G.1: relic accessory abilities route through SpellEngine via this payload.
        registrar.playToServer(RelicSpellCastPayload.TYPE, RelicSpellCastPayload.STREAM_CODEC, RelicSpellCastPayload::handleOnServer);
        registrar.playToClient(AbilityCooldownSyncPayload.TYPE, AbilityCooldownSyncPayload.STREAM_CODEC, AbilityCooldownSyncPayload::handleOnClient);
        registrar.playToServer(RelicTweakPayload.TYPE, RelicTweakPayload.STREAM_CODEC, RelicTweakPayload::handleOnServer);
        registrar.playToServer(RelicExchangePayload.TYPE, RelicExchangePayload.STREAM_CODEC, RelicExchangePayload::handleOnServer);
        registrar.playToClient(OpenRelicScreenPayload.TYPE, OpenRelicScreenPayload.STREAM_CODEC, OpenRelicScreenPayload::handleOnClient);
        registrar.playToClient(RerollPayload.OpenRerollPayload.TYPE, RerollPayload.OpenRerollPayload.STREAM_CODEC, RerollPayload.OpenRerollPayload::handleOnClient);
        registrar.playToServer(RerollPayload.RerollActionPayload.TYPE, RerollPayload.RerollActionPayload.STREAM_CODEC, RerollPayload.RerollActionPayload::handleOnServer);
        registrar.playToClient(CombatTextPayload.TYPE, CombatTextPayload.STREAM_CODEC, CombatTextPayload::handleOnClient);
        // Weapon ability cooldown sync (RPG weapons)
        registrar.playToClient(WeaponAbilitySyncPayload.TYPE, WeaponAbilitySyncPayload.STREAM_CODEC, WeaponAbilitySyncPayload::handleOnClient);
    }
}
