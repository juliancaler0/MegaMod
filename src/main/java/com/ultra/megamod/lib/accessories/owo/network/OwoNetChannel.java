package com.ultra.megamod.lib.accessories.owo.network;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * Adapter for io.wispforest.owo.network.OwoNetChannel.
 * Replaced with NeoForge networking in the actual port.
 * This stub allows code to compile - actual networking is handled by AccessoriesNetworking.
 */
public class OwoNetChannel {

    private final Identifier id;

    private OwoNetChannel(Identifier id) {
        this.id = id;
    }

    public static OwoNetChannel create(Identifier id) {
        return new OwoNetChannel(id);
    }

    // Registration methods - these are stubs. Real networking uses NeoForge CustomPacketPayload.
    public <R extends Record> void registerServerbound(Class<R> type, Endec<R> endec, ChannelHandler<R, ServerAccess> handler) {
        // Registration handled by NeoForge networking system
    }

    public <R extends Record> void registerClientbound(Class<R> type, Endec<R> endec, ChannelHandler<R, ClientAccess> handler) {
        // Registration handled by NeoForge networking system
    }

    public <R extends Record> void registerClientboundDeferred(Class<R> type, Endec<R> endec) {
        // Registration handled by NeoForge networking system
    }

    public ClientHandle clientHandle() {
        return new ClientHandle();
    }

    public ServerHandle serverHandle(net.minecraft.world.entity.player.Player player) {
        return new ServerHandle(player);
    }

    public ServerHandle serverHandle(ServerPlayer player) {
        return new ServerHandle((net.minecraft.world.entity.player.Player) player);
    }

    public ServerHandle serverHandle(Collection<ServerPlayer> players) {
        return new ServerHandle(players);
    }

    @FunctionalInterface
    public interface ChannelHandler<R, A> {
        void handle(R record, A access);
    }

    public static class ClientHandle {
        public <R extends Record> void send(R packet) {
            // Handled by NeoForge networking
        }
    }

    public static class ServerHandle {
        private final net.minecraft.world.entity.player.Player player;
        private final Collection<ServerPlayer> players;

        ServerHandle(net.minecraft.world.entity.player.Player player) {
            this.player = player;
            this.players = null;
        }

        ServerHandle(Collection<ServerPlayer> players) {
            this.player = null;
            this.players = players;
        }

        public <R extends Record> void send(R packet) {
            // Handled by NeoForge networking
        }
    }
}
