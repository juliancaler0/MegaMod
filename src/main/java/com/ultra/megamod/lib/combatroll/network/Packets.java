package com.ultra.megamod.lib.combatroll.network;

import com.google.gson.Gson;
import com.ultra.megamod.lib.combatroll.CombatRollMod;
import com.ultra.megamod.lib.combatroll.client.RollEffect;
import com.ultra.megamod.lib.combatroll.config.ServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class Packets {
    public record RollPublish(int playerId, RollEffect.Visuals visuals, Vec3 velocity) implements CustomPacketPayload {
        public static final Identifier ID = Identifier.fromNamespaceAndPath(CombatRollMod.ID, "combatroll_publish");
        public static final CustomPacketPayload.Type<RollPublish> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, RollPublish> CODEC = StreamCodec.of(RollPublish::write, RollPublish::read);

        public static RollPublish read(RegistryFriendlyByteBuf buffer) {
            int playerId = buffer.readInt();
            var visuals = new RollEffect.Visuals(
                    buffer.readUtf(),
                    RollEffect.Particles.valueOf(buffer.readUtf()));
            Vec3 velocity = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            return new RollPublish(playerId, visuals, velocity);
        }

        public static void write(RegistryFriendlyByteBuf buffer, RollPublish packet) {
            buffer.writeInt(packet.playerId);
            buffer.writeUtf(packet.visuals.animationName());
            buffer.writeUtf(packet.visuals.particles().toString());
            buffer.writeDouble(packet.velocity.x);
            buffer.writeDouble(packet.velocity.y);
            buffer.writeDouble(packet.velocity.z);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }
    }

    public record RollAnimation(int playerId, RollEffect.Visuals visuals, Vec3 velocity) implements CustomPacketPayload {
        public static final Identifier ID = Identifier.fromNamespaceAndPath(CombatRollMod.ID, "combatroll_animation");
        public static final CustomPacketPayload.Type<RollAnimation> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, RollAnimation> CODEC = StreamCodec.of(RollAnimation::write, RollAnimation::read);

        public static RollAnimation read(RegistryFriendlyByteBuf buffer) {
            int playerId = buffer.readInt();
            var visuals = new RollEffect.Visuals(
                    buffer.readUtf(),
                    RollEffect.Particles.valueOf(buffer.readUtf()));
            Vec3 velocity = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            return new RollAnimation(playerId, visuals, velocity);
        }

        public static void write(RegistryFriendlyByteBuf buffer, RollAnimation packet) {
            buffer.writeInt(packet.playerId);
            buffer.writeUtf(packet.visuals.animationName());
            buffer.writeUtf(packet.visuals.particles().toString());
            buffer.writeDouble(packet.velocity.x);
            buffer.writeDouble(packet.velocity.y);
            buffer.writeDouble(packet.velocity.z);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }
    }

    public record ConfigSync(String json) implements CustomPacketPayload {
        public static final Identifier ID = Identifier.fromNamespaceAndPath(CombatRollMod.ID, "combatroll_config_sync");
        public static final CustomPacketPayload.Type<ConfigSync> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<FriendlyByteBuf, ConfigSync> CODEC = StreamCodec.of(ConfigSync::write, ConfigSync::read);

        private static final Gson gson = new Gson();
        public static String serialize(ServerConfig config) {
            return gson.toJson(config);
        }

        public static void write(FriendlyByteBuf buffer, ConfigSync packet) {
            buffer.writeUtf(packet.json);
        }

        public static ConfigSync read(FriendlyByteBuf buffer) {
            var json = buffer.readUtf();
            return new ConfigSync(json);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }
    }

    public record Ack(String code) implements CustomPacketPayload {
        public static final Identifier ID = Identifier.fromNamespaceAndPath(CombatRollMod.ID, "combatroll_ack");
        public static final CustomPacketPayload.Type<Ack> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<FriendlyByteBuf, Ack> CODEC = StreamCodec.of(Ack::write, Ack::read);

        public static void write(FriendlyByteBuf buffer, Ack packet) {
            buffer.writeUtf(packet.code);
        }

        public static Ack read(FriendlyByteBuf buffer) {
            var code = buffer.readUtf();
            return new Ack(code);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }
    }
}
