package com.ultra.megamod.feature.combat.spell.client;

import com.ultra.megamod.feature.combat.spell.SpellBookCastPayload;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.SpellRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

/**
 * Client-only helper for sending spell book cast payloads from right-click.
 * Separated from SpellBookItem to avoid loading client-only classes on the server.
 */
public class SpellBookClientCast {

    public static void sendCast(Player player, List<String> spells, InteractionHand hand) {
        SpellBookSelection.clamp(spells.size());
        int idx = SpellBookSelection.getSelected();
        if (idx >= spells.size()) idx = 0;

        String spellId = spells.get(idx);
        SpellBookCastPayload payload = new SpellBookCastPayload(spellId);
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) payload, (CustomPacketPayload[]) new CustomPacketPayload[0]);

        SpellDefinition def = SpellRegistry.get(spellId);
        if (def != null) {
            player.displayClientMessage(
                    Component.literal("\u00a76[\u00a7eMegaMod\u00a76] \u00a7aCasting: \u00a7e" + def.name()), true);
        }

        player.swing(hand);
    }
}
