package com.ultra.megamod.lib.spellengine.network;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.config.ServerConfig;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCastSyncHelper;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import com.ultra.megamod.lib.spellengine.internals.container.SpellAssignments;
import com.ultra.megamod.lib.spellengine.internals.container.SpellContainerSource;
import com.ultra.megamod.lib.spellengine.internals.melee.Melee;
import com.ultra.megamod.lib.spellengine.internals.target.SpellTarget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerNetwork {

    /**
     * Register payload handlers. Call from MegaMod constructor via:
     *   modEventBus.addListener(ServerNetwork::registerPayloadHandlers);
     */
    public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("spellengine_1");

        // Configuration stage
        registrar.configurationToClient(Packets.ConfigSync.PACKET_ID, Packets.ConfigSync.CODEC, (packet, context) -> {
            SpellEngineMod.config = packet.config();
            context.reply(new Packets.Ack(ConfigurationTask.name));
        });
        registrar.configurationToClient(Packets.SpellRegistrySync.PACKET_ID, Packets.SpellRegistrySync.CODEC, (packet, context) -> {
            SpellAssignments.decodeContent(packet.chunks());
            context.reply(new Packets.Ack(SpellRegistrySyncTask.name));
        });
        registrar.configurationToServer(Packets.Ack.PACKET_ID, Packets.Ack.CODEC, (packet, context) -> {
            if (packet.code().equals(ConfigurationTask.name)) {
                context.finishCurrentTask(ConfigurationTask.KEY);
            }
            if (packet.code().equals(SpellRegistrySyncTask.name)) {
                context.finishCurrentTask(SpellRegistrySyncTask.KEY);
            }
        });

        // Play stage - client to server
        registrar.playToServer(Packets.SpellCastSync.PACKET_ID, Packets.SpellCastSync.CODEC, (packet, context) -> {
            var player = (ServerPlayer) context.player();
            var server = player.level().getServer();
            ServerLevel world = (ServerLevel) player.level();

            server.execute(() -> {
                if (packet.spellId() == null) {
                    SpellCastSyncHelper.clearCasting(player);
                } else {
                    SpellHelper.startCasting(player, packet.spellId(), packet.speed(), packet.length());
                }
            });
        });

        registrar.playToServer(Packets.SpellRequest.PACKET_ID, Packets.SpellRequest.CODEC, (packet, context) -> {
            var player = (ServerPlayer) context.player();
            var server = player.level().getServer();
            ServerLevel world = (ServerLevel) player.level();

            server.execute(() -> {
                var spellEntry = SpellRegistry.from(world).get(packet.spellId());
                if (spellEntry.isEmpty()) {
                    return;
                }
                List<Entity> targets = new ArrayList<>();
                for (var targetId : packet.targets()) {
                    var entity = world.getEntity(targetId);
                    if (entity != null) {
                        targets.add(entity);
                    } else {
                        System.err.println("Spell Engine: Trying to perform spell " + packet.spellId().toString() + " Entity not found: " + targetId);
                    }
                }
                var target = new SpellTarget.SearchResult(targets, packet.location());
                SpellHelper.performSpell(world, player, spellEntry.get(), target, packet.action(), packet.progress());
            });
        });

        registrar.playToServer(Packets.AttackFxBroadcast.PACKET_ID, Packets.AttackFxBroadcast.CODEC, (packet, context) -> {
            var player = (ServerPlayer) context.player();
            var server = player.level().getServer();

            server.execute(() -> {
                Melee.broadcastAttackFx(player, packet.attackContext());
            });
        });

        registrar.playToServer(Packets.AttackPerform.PACKET_ID, Packets.AttackPerform.CODEC, (packet, context) -> {
            var player = (ServerPlayer) context.player();
            var server = player.level().getServer();

            server.execute(() -> {
                Melee.performAttackAgainstTargets(player, packet.attackContext(), packet.targetIds());
            });
        });

        // Play stage - server to client
        registrar.playToClient(Packets.SpellCooldown.PACKET_ID, Packets.SpellCooldown.CODEC, SpellEngineClientHandler::handleSpellCooldown);
        registrar.playToClient(Packets.SpellCooldownSync.PACKET_ID, Packets.SpellCooldownSync.CODEC, SpellEngineClientHandler::handleSpellCooldownSync);
        registrar.playToClient(Packets.SpellMessage.PACKET_ID, Packets.SpellMessage.CODEC, SpellEngineClientHandler::handleSpellMessage);
        registrar.playToClient(Packets.ParticleBatches.PACKET_ID, Packets.ParticleBatches.CODEC, SpellEngineClientHandler::handleParticleBatches);
        registrar.playToClient(Packets.SpellAnimation.PACKET_ID, Packets.SpellAnimation.CODEC, SpellEngineClientHandler::handleSpellAnimation);
        registrar.playToClient(Packets.SpellContainerSync.PACKET_ID, Packets.SpellContainerSync.CODEC, SpellEngineClientHandler::handleSpellContainerSync);
        registrar.playToClient(Packets.AttackAvailable.PACKET_ID, Packets.AttackAvailable.CODEC, SpellEngineClientHandler::handleAttackAvailable);
    }

    /**
     * Register configuration tasks. Call from MegaMod constructor via:
     *   modEventBus.addListener(ServerNetwork::registerConfigurationTasks);
     */
    public static void registerConfigurationTasks(final RegisterConfigurationTasksEvent event) {
        event.register(new ConfigurationTask(event.getListener(), SpellEngineMod.config));
        if (SpellAssignments.encoded.isEmpty()) {
            System.err.println("Spell Engine: Spell registry is empty at configuration time!");
        } else {
            event.register(new SpellRegistrySyncTask(event.getListener(), SpellAssignments.encoded));
        }
    }

    /**
     * Called when a player joins or changes world. Syncs cooldowns and spell containers.
     */
    public static void onPlayerJoin(ServerPlayer player) {
        ((SpellCasterEntity) player).getCooldownManager().pushSync();
        SpellContainerSource.syncServerSideContainers(player);
    }

    public record ConfigurationTask(
            net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener listener,
            ServerConfig config
    ) implements ICustomConfigurationTask {
        public static final String name = "megamod:spell_config";
        public static final net.minecraft.server.network.ConfigurationTask.Type KEY =
                new net.minecraft.server.network.ConfigurationTask.Type(Identifier.fromNamespaceAndPath(MegaMod.MODID, "spell_config"));

        @Override
        public void run(Consumer<CustomPacketPayload> sender) {
            sender.accept(new Packets.ConfigSync(this.config));
        }

        @Override
        public net.minecraft.server.network.ConfigurationTask.Type type() {
            return KEY;
        }
    }

    public record SpellRegistrySyncTask(
            net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener listener,
            List<String> encodedChunks
    ) implements ICustomConfigurationTask {
        public static final String name = "megamod:spell_registry";
        public static final net.minecraft.server.network.ConfigurationTask.Type KEY =
                new net.minecraft.server.network.ConfigurationTask.Type(Identifier.fromNamespaceAndPath(MegaMod.MODID, "spell_registry"));

        @Override
        public void run(Consumer<CustomPacketPayload> sender) {
            sender.accept(new Packets.SpellRegistrySync(encodedChunks));
        }

        @Override
        public net.minecraft.server.network.ConfigurationTask.Type type() {
            return KEY;
        }
    }
}
