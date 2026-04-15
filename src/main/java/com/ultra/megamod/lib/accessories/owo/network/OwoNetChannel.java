package com.ultra.megamod.lib.accessories.owo.network;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * NeoForge-backed implementation of the owo network channel pattern the
 * Accessories library uses. Each registered record type is wrapped in an
 * {@link EndecPayload} and ferried through NeoForge's {@link CustomPacketPayload}
 * system. The Endec's underlying {@link com.mojang.serialization.Codec} is
 * adapted to a {@link StreamCodec} via {@link ByteBufCodecs#fromCodecWithRegistries}
 * so registry-aware types (ItemStack etc.) deserialize correctly.
 *
 * <p>Registrations are queued at mod construction and applied during
 * {@link RegisterPayloadHandlersEvent} (see {@code AccessoriesForge}).</p>
 */
public class OwoNetChannel {

    private final Identifier id;
    private final List<Registration<?>> registrations = new ArrayList<>();
    private final Map<Class<?>, Registration<?>> byClass = new HashMap<>();

    private OwoNetChannel(Identifier id) {
        this.id = id;
    }

    public static OwoNetChannel create(Identifier id) {
        return new OwoNetChannel(id);
    }

    public Identifier id() {
        return id;
    }

    // ─────────────────────────────────────────────────────────────
    // Registration (called from AccessoriesNetworking.init / initClient)
    // ─────────────────────────────────────────────────────────────

    public <R extends Record> void registerServerbound(Class<R> type, Endec<R> endec, ChannelHandler<R, ServerAccess> handler) {
        add(new Registration<>(type, endec, handler, null, Side.SERVERBOUND, payloadType(type)));
    }

    public <R extends Record> void registerClientbound(Class<R> type, Endec<R> endec, ChannelHandler<R, ClientAccess> handler) {
        // Upgrade a previously deferred (no-handler) registration in place so the
        // client handler lands on the same payload type the server already knows about.
        @SuppressWarnings("unchecked")
        Registration<R> existing = (Registration<R>) byClass.get(type);
        if (existing != null) {
            existing.clientHandler = handler;
            existing.side = Side.CLIENTBOUND;
            return;
        }
        add(new Registration<>(type, endec, null, handler, Side.CLIENTBOUND, payloadType(type)));
    }

    public <R extends Record> void registerClientboundDeferred(Class<R> type, Endec<R> endec) {
        add(new Registration<>(type, endec, null, null, Side.CLIENTBOUND, payloadType(type)));
    }

    private <R extends Record> void add(Registration<R> reg) {
        registrations.add(reg);
        byClass.put(reg.type, reg);
    }

    private <R extends Record> CustomPacketPayload.Type<EndecPayload<R>> payloadType(Class<R> recordType) {
        String path = id.getPath() + "/" + recordType.getSimpleName().toLowerCase(java.util.Locale.ROOT);
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(id.getNamespace(), path));
    }

    // ─────────────────────────────────────────────────────────────
    // NeoForge event wiring — called from AccessoriesForge
    // ─────────────────────────────────────────────────────────────

    public void applyRegistrations(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(id.getNamespace())
                .optional(); // accessories clients + servers can coexist without this channel
        for (Registration<?> reg : registrations) {
            applyOne(registrar, reg);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <R extends Record> void applyOne(PayloadRegistrar registrar, Registration<R> reg) {
        CustomPacketPayload.Type<EndecPayload<R>> type = reg.payloadType;
        StreamCodec<RegistryFriendlyByteBuf, EndecPayload<R>> codec = EndecPayload.streamCodec(type, reg.endec);

        switch (reg.side) {
            case SERVERBOUND -> registrar.playToServer(type, codec, (payload, ctx) -> {
                ChannelHandler<R, ServerAccess> h = reg.serverHandler;
                if (h == null) return;
                ctx.enqueueWork(() -> h.handle(payload.record(), () -> ctx.player()));
            });
            case CLIENTBOUND -> {
                // bidirectional option is rare; register both to be safe for deferred+clientbound pairs
                if (reg.clientHandler != null) {
                    registrar.playToClient(type, codec, (payload, ctx) -> {
                        ChannelHandler<R, ClientAccess> h = reg.clientHandler;
                        ctx.enqueueWork(() -> h.handle(payload.record(), () -> ctx.player()));
                    });
                } else {
                    // Deferred: register with a no-op handler so the type exists on both sides.
                    registrar.playToClient(type, codec, (payload, ctx) -> {});
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Send API (consumed by the rest of the lib)
    // ─────────────────────────────────────────────────────────────

    public ClientHandle clientHandle() {
        return new ClientHandle(this);
    }

    public ServerHandle serverHandle(net.minecraft.world.entity.player.Player player) {
        return new ServerHandle(this, player);
    }

    public ServerHandle serverHandle(ServerPlayer player) {
        return new ServerHandle(this, player);
    }

    public ServerHandle serverHandle(Collection<ServerPlayer> players) {
        return new ServerHandle(this, players);
    }

    @SuppressWarnings("unchecked")
    private <R extends Record> EndecPayload<R> wrap(R packet) {
        Registration<R> reg = (Registration<R>) byClass.get(packet.getClass());
        if (reg == null) {
            throw new IllegalStateException("OwoNetChannel: record type " + packet.getClass().getName()
                    + " was never registered on channel " + id);
        }
        return new EndecPayload<>(packet, reg.payloadType);
    }

    // ─────────────────────────────────────────────────────────────
    // Support types
    // ─────────────────────────────────────────────────────────────

    @FunctionalInterface
    public interface ChannelHandler<R, A> {
        void handle(R record, A access);
    }

    private enum Side { SERVERBOUND, CLIENTBOUND }

    private static final class Registration<R extends Record> {
        final Class<R> type;
        final Endec<R> endec;
        ChannelHandler<R, ServerAccess> serverHandler;
        ChannelHandler<R, ClientAccess> clientHandler;
        Side side;
        final CustomPacketPayload.Type<EndecPayload<R>> payloadType;

        Registration(Class<R> type, Endec<R> endec,
                     ChannelHandler<R, ServerAccess> serverHandler,
                     ChannelHandler<R, ClientAccess> clientHandler,
                     Side side,
                     CustomPacketPayload.Type<EndecPayload<R>> payloadType) {
            this.type = type;
            this.endec = endec;
            this.serverHandler = serverHandler;
            this.clientHandler = clientHandler;
            this.side = side;
            this.payloadType = payloadType;
        }
    }

    public static final class ClientHandle {
        private final OwoNetChannel channel;

        private ClientHandle(OwoNetChannel channel) {
            this.channel = channel;
        }

        public <R extends Record> void send(R packet) {
            ClientPacketDistributor.sendToServer(channel.wrap(packet));
        }
    }

    public static final class ServerHandle {
        private final OwoNetChannel channel;
        private final Player singlePlayer;
        private final Collection<ServerPlayer> players;

        private ServerHandle(OwoNetChannel channel, Player player) {
            this.channel = channel;
            this.singlePlayer = player;
            this.players = null;
        }

        private ServerHandle(OwoNetChannel channel, Collection<ServerPlayer> players) {
            this.channel = channel;
            this.singlePlayer = null;
            this.players = players;
        }

        public <R extends Record> void send(R packet) {
            EndecPayload<R> payload = channel.wrap(packet);
            if (singlePlayer instanceof ServerPlayer sp) {
                PacketDistributor.sendToPlayer(sp, payload);
            } else if (players != null) {
                for (ServerPlayer sp : players) {
                    PacketDistributor.sendToPlayer(sp, payload);
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Generic payload wrapper
    // ─────────────────────────────────────────────────────────────

    public record EndecPayload<R extends Record>(R record, CustomPacketPayload.Type<EndecPayload<R>> typeRef) implements CustomPacketPayload {
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return typeRef;
        }

        public static <R extends Record> StreamCodec<RegistryFriendlyByteBuf, EndecPayload<R>> streamCodec(
                CustomPacketPayload.Type<EndecPayload<R>> type, Endec<R> endec) {
            StreamCodec<RegistryFriendlyByteBuf, R> inner = ByteBufCodecs.fromCodecWithRegistries(endec.codec());
            return inner.map(
                    r -> new EndecPayload<>(r, type),
                    EndecPayload::record
            );
        }
    }
}
