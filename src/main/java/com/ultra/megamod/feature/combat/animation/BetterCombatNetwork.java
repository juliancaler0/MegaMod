package com.ultra.megamod.feature.combat.animation;

import com.ultra.megamod.feature.combat.animation.network.C2S_AttackRequest;
import com.ultra.megamod.feature.combat.animation.network.S2C_AttackAnimation;
import com.ultra.megamod.feature.combat.animation.network.S2C_AttackSound;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers all BetterCombat network payloads.
 * Ported 1:1 from BetterCombat's packet registration.
 */
public class BetterCombatNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");

        // ── BetterCombat packets ──

        // Client → Server: attack request with target entity IDs
        registrar.playToServer(
                C2S_AttackRequest.TYPE,
                C2S_AttackRequest.STREAM_CODEC,
                BetterCombatHandler::handleAttackRequest
        );

        // Server → Client: attack animation broadcast
        registrar.playToClient(
                S2C_AttackAnimation.TYPE,
                S2C_AttackAnimation.STREAM_CODEC,
                S2C_AttackAnimation::handleClient
        );

        // Server → Client: attack swing sound
        registrar.playToClient(
                S2C_AttackSound.TYPE,
                S2C_AttackSound.STREAM_CODEC,
                S2C_AttackSound::handleClient
        );

        // ── Legacy packets (kept for old BetterCombatAttackPayload during transition) ──
        registrar.playToServer(
                BetterCombatAttackPayload.TYPE,
                BetterCombatAttackPayload.STREAM_CODEC,
                BetterCombatAttackPayload::handleOnServer
        );
        registrar.playToClient(
                AttackAnimationPayload.TYPE,
                AttackAnimationPayload.STREAM_CODEC,
                AttackAnimationPayload::handleOnClient
        );

        // ── Spell animation sync ──
        registrar.playToClient(
                SpellAnimationPayload.TYPE,
                SpellAnimationPayload.STREAM_CODEC,
                SpellAnimationPayload::handleClient
        );

        // ── Ability trigger HUD popup (manual cast or passive proc) ──
        registrar.playToClient(
                com.ultra.megamod.feature.hud.network.AbilityTriggerPayload.TYPE,
                com.ultra.megamod.feature.hud.network.AbilityTriggerPayload.STREAM_CODEC,
                com.ultra.megamod.feature.hud.network.AbilityTriggerPayload::handleOnClient
        );
    }
}
