package com.ultra.megamod.lib.spellengine.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.BiConsumer;

/**
 * Client-side handlers for SpellEngine packets.
 * <p>
 * These are referenced by method-reference from the common-side
 * {@link ServerNetwork#registerPayloadHandlers} during
 * {@code RegisterPayloadHandlersEvent}, which fires on the dedicated server too.
 * To keep the server's classloader away from {@code net.minecraft.client.*}
 * types (rejected by {@code NeoForgeDevDistCleaner}), this class holds static
 * {@code BiConsumer} fields defaulted to no-ops. The real client logic lives
 * in {@code SpellEngineClientHandlerImpl} and is installed from
 * {@code SpellEngineClient.init()}.
 */
public class SpellEngineClientHandler {

    public static BiConsumer<Packets.ParticleBatches, IPayloadContext> PARTICLE_BATCHES = (p, c) -> {};
    public static BiConsumer<Packets.SpellAnimation, IPayloadContext> SPELL_ANIMATION = (p, c) -> {};
    public static BiConsumer<Packets.SpellCooldown, IPayloadContext> SPELL_COOLDOWN = (p, c) -> {};
    public static BiConsumer<Packets.SpellMessage, IPayloadContext> SPELL_MESSAGE = (p, c) -> {};
    public static BiConsumer<Packets.SpellCooldownSync, IPayloadContext> SPELL_COOLDOWN_SYNC = (p, c) -> {};
    public static BiConsumer<Packets.SpellContainerSync, IPayloadContext> SPELL_CONTAINER_SYNC = (p, c) -> {};
    public static BiConsumer<Packets.AttackAvailable, IPayloadContext> ATTACK_AVAILABLE = (p, c) -> {};

    public static void handleParticleBatches(Packets.ParticleBatches packet, IPayloadContext context) {
        PARTICLE_BATCHES.accept(packet, context);
    }

    public static void handleSpellAnimation(Packets.SpellAnimation packet, IPayloadContext context) {
        SPELL_ANIMATION.accept(packet, context);
    }

    public static void handleSpellCooldown(Packets.SpellCooldown packet, IPayloadContext context) {
        SPELL_COOLDOWN.accept(packet, context);
    }

    public static void handleSpellMessage(Packets.SpellMessage packet, IPayloadContext context) {
        SPELL_MESSAGE.accept(packet, context);
    }

    public static void handleSpellCooldownSync(Packets.SpellCooldownSync packet, IPayloadContext context) {
        SPELL_COOLDOWN_SYNC.accept(packet, context);
    }

    public static void handleSpellContainerSync(Packets.SpellContainerSync packet, IPayloadContext context) {
        SPELL_CONTAINER_SYNC.accept(packet, context);
    }

    public static void handleAttackAvailable(Packets.AttackAvailable packet, IPayloadContext context) {
        ATTACK_AVAILABLE.accept(packet, context);
    }
}
