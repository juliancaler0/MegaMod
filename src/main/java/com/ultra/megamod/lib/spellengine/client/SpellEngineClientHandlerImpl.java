package com.ultra.megamod.lib.spellengine.client;

import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.client.animation.AnimatablePlayer;
import com.ultra.megamod.lib.spellengine.client.gui.HudMessages;
import com.ultra.megamod.lib.spellengine.fx.ParticleHelper;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterClient;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import com.ultra.megamod.lib.spellengine.internals.container.SpellContainerSource;
import com.ultra.megamod.lib.spellengine.network.SpellEngineClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.world.entity.player.Player;

/**
 * Client-only implementation of the SpellEngine play-to-client packet handlers.
 * Populated into the common-side {@link SpellEngineClientHandler} proxy fields
 * from {@link SpellEngineClient#init()}. The common proxy holds no client-class
 * references so the dedicated-server classloader never touches this class.
 */
public final class SpellEngineClientHandlerImpl {
    private SpellEngineClientHandlerImpl() {}

    public static void install() {
        SpellEngineClientHandler.PARTICLE_BATCHES = (packet, context) -> {
            var client = Minecraft.getInstance();
            var level = client.level;
            if (level == null) return;
            var instructions = ParticleHelper.convertToInstructions(level, packet);
            client.execute(() -> {
                for (var instruction : instructions) {
                    instruction.perform(level);
                }
            });
        };

        SpellEngineClientHandler.SPELL_ANIMATION = (packet, context) -> {
            var client = Minecraft.getInstance();
            client.execute(() -> {
                if (client.level == null) return;
                var entity = client.level.getEntity(packet.playerId());
                if (entity instanceof Player player) {
                    ((AnimatablePlayer) player).playSpellAnimation(packet.animationType(), packet.name(), packet.speed());
                }
            });
        };

        SpellEngineClientHandler.SPELL_COOLDOWN = (packet, context) -> {
            var client = Minecraft.getInstance();
            client.execute(() -> {
                if (client.level == null) return;
                var registry = SpellRegistry.from(client.level);
                var spell = registry.get(packet.spellId());
                if (spell.isEmpty()) return;
                ((SpellCasterEntity) client.player).getCooldownManager().set(spell.get(), packet.duration());
            });
        };

        SpellEngineClientHandler.SPELL_MESSAGE = (packet, context) -> {
            var client = Minecraft.getInstance();
            client.execute(() -> {
                var translation = Language.getInstance().getOrDefault(packet.translationKey());
                HudMessages.INSTANCE.error(translation);
            });
        };

        SpellEngineClientHandler.SPELL_COOLDOWN_SYNC = (packet, context) -> {
            var client = Minecraft.getInstance();
            client.execute(() -> {
                var cooldownManager = ((SpellCasterEntity) client.player).getCooldownManager();
                var cooldownsBefore = cooldownManager.spellsOnCooldown();
                cooldownManager.acceptSync(packet.baseTick(), packet.cooldowns());
                var cooldownsAfter = cooldownManager.spellsOnCooldown();
                HudMessages.INSTANCE.onCooldownsChanged(cooldownsBefore, cooldownsAfter);
            });
        };

        SpellEngineClientHandler.SPELL_CONTAINER_SYNC = (packet, context) -> {
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
        };

        SpellEngineClientHandler.ATTACK_AVAILABLE = (packet, context) -> {
            var client = Minecraft.getInstance();
            client.execute(() -> {
                var player = client.player;
                if (player instanceof SpellCasterClient caster) {
                    caster.onAttacksAvailable(packet.attacks());
                }
            });
        };
    }
}
