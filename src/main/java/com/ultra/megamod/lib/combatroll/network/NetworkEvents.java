package com.ultra.megamod.lib.combatroll.network;

import com.google.gson.Gson;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.combatroll.CombatRollMod;
import com.ultra.megamod.lib.combatroll.client.ClientNetwork;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Consumer;

/**
 * Network events for CombatRoll.
 * These are MOD bus events - register them via modEventBus.addListener() from MegaMod.
 *
 * Example registration in MegaMod.java:
 *   modEventBus.addListener(NetworkEvents::registerConfigurationTasks);
 *   modEventBus.addListener(NetworkEvents::registerPayloadHandlers);
 */
public class NetworkEvents {
    public static void registerConfigurationTasks(final RegisterConfigurationTasksEvent event) {
        event.register(new ConfigurationTask(event.getListener()));
    }

    public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("combatroll_1");

        // Server

        registrar.configurationToServer(Packets.Ack.PACKET_ID, Packets.Ack.CODEC, (payload, context) -> {
            if (payload.code().equals(ConfigurationTask.ID.toString())) {
                context.finishCurrentTask(ConfigurationTask.KEY);
            }
        });

        registrar.playToServer(Packets.RollPublish.PACKET_ID, Packets.RollPublish.CODEC, (packet, context) -> {
            var player = (ServerPlayer)context.player();
            var server = player.level().getServer();
            ServerNetwork.handleRollPublish(packet, server, player);
        });

        // Client

        registrar.configurationToClient(Packets.ConfigSync.PACKET_ID, Packets.ConfigSync.CODEC, (packet, context) -> {
            ClientNetwork.handleConfigSync(packet);
            context.reply(new Packets.Ack(ConfigurationTask.ID.toString()));
        });

        registrar.playToClient(Packets.RollAnimation.PACKET_ID, Packets.RollAnimation.CODEC, (packet, context) -> {
            ClientNetwork.handleRollAnimation(packet);
        });
    }

    public record ConfigurationTask(net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
        public static final Identifier ID = Identifier.fromNamespaceAndPath(MegaMod.MODID, "combatroll_config");
        public static final net.minecraft.server.network.ConfigurationTask.Type KEY = new net.minecraft.server.network.ConfigurationTask.Type(Identifier.fromNamespaceAndPath(MegaMod.MODID, "combatroll_config"));
        @Override
        public void run(Consumer<CustomPacketPayload> sender) {
            var gson = new Gson();
            var configString = gson.toJson(CombatRollMod.config);
            var configPayload = new Packets.ConfigSync(configString);
            sender.accept(configPayload);
        }

        @Override
        public net.minecraft.server.network.ConfigurationTask.Type type() {
            return KEY;
        }
    }
}
