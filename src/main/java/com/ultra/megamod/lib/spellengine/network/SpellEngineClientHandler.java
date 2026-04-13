package com.ultra.megamod.lib.spellengine.network;

import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.client.animation.AnimatablePlayer;
import com.ultra.megamod.lib.spellengine.client.gui.HudMessages;
import com.ultra.megamod.lib.spellengine.fx.ParticleHelper;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterClient;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import com.ultra.megamod.lib.spellengine.internals.container.SpellContainerSource;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-side handlers for SpellEngine packets.
 * These are called from the registrar in ServerNetwork.registerPayloadHandlers.
 */
public class SpellEngineClientHandler {

    public static void handleParticleBatches(Packets.ParticleBatches packet, IPayloadContext context) {
        var client = Minecraft.getInstance();
        var level = client.level;
        if (level == null) return;
        var instructions = ParticleHelper.convertToInstructions(level, packet);
        client.execute(() -> {
            for (var instruction : instructions) {
                instruction.perform(level);
            }
        });
    }

    public static void handleSpellAnimation(Packets.SpellAnimation packet, IPayloadContext context) {
        var client = Minecraft.getInstance();
        client.execute(() -> {
            if (client.level == null) return;
            var entity = client.level.getEntity(packet.playerId());
            if (entity instanceof Player player) {
                ((AnimatablePlayer) player).playSpellAnimation(packet.animationType(), packet.name(), packet.speed());
            }
        });
    }

    public static void handleSpellCooldown(Packets.SpellCooldown packet, IPayloadContext context) {
        var client = Minecraft.getInstance();
        client.execute(() -> {
            if (client.level == null) return;
            var registry = SpellRegistry.from(client.level);
            var spell = registry.get(packet.spellId());
            if (spell.isEmpty()) return;
            ((SpellCasterEntity) client.player).getCooldownManager().set(spell.get(), packet.duration());
        });
    }

    public static void handleSpellMessage(Packets.SpellMessage packet, IPayloadContext context) {
        var client = Minecraft.getInstance();
        client.execute(() -> {
            var translation = Language.getInstance().getOrDefault(packet.translationKey());
            HudMessages.INSTANCE.error(translation);
        });
    }

    public static void handleSpellCooldownSync(Packets.SpellCooldownSync packet, IPayloadContext context) {
        var client = Minecraft.getInstance();
        client.execute(() -> {
            var cooldownManager = ((SpellCasterEntity) client.player).getCooldownManager();
            var cooldownsBefore = cooldownManager.spellsOnCooldown();
            cooldownManager.acceptSync(packet.baseTick(), packet.cooldowns());
            var cooldownsAfter = cooldownManager.spellsOnCooldown();
            HudMessages.INSTANCE.onCooldownsChanged(cooldownsBefore, cooldownsAfter);
        });
    }

    public static void handleSpellContainerSync(Packets.SpellContainerSync packet, IPayloadContext context) {
        var client = Minecraft.getInstance();
        client.execute(() -> {
            var player = client.player;
            if (player != null) {
                var containers = ((SpellContainerSource.Owner) player).serverSideSpellContainers();
                containers.clear();
                containers.putAll(packet.containers());
            }
            SpellContainerSource.setDirty(client.player, SpellContainerSource.MAIN_HAND);
        });
    }

    public static void handleAttackAvailable(Packets.AttackAvailable packet, IPayloadContext context) {
        var client = Minecraft.getInstance();
        client.execute(() -> {
            var player = client.player;
            if (player instanceof SpellCasterClient caster) {
                caster.onAttacksAvailable(packet.attacks());
            }
        });
    }
}
