package com.ultra.megamod.lib.spellengine.network;

import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainer;
import com.ultra.megamod.lib.spellengine.api.spell.fx.ParticleBatch;
import com.ultra.megamod.lib.spellengine.config.ServerConfig;
import com.ultra.megamod.lib.spellengine.internals.SpellCooldownManager;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import com.ultra.megamod.lib.spellengine.internals.melee.Melee;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Packets {

    public record SpellCastSync(Identifier spellId, float speed, int length) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "cast_sync");
        public static final CustomPacketPayload.Type<SpellCastSync> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, SpellCastSync> CODEC = StreamCodec.of(SpellCastSync::write, SpellCastSync::read);
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }

        public static void write(RegistryFriendlyByteBuf buffer, SpellCastSync packet) {
            if (packet.spellId == null) {
                buffer.writeUtf("");
            } else {
                buffer.writeUtf(packet.spellId.toString());
            }
            buffer.writeFloat(packet.speed);
            buffer.writeInt(packet.length);
        }

        public static SpellCastSync read(RegistryFriendlyByteBuf buffer) {
            var string = buffer.readUtf();
            Identifier spellId = null;
            if (!string.isEmpty()) {
                spellId = Identifier.parse(string);
            }
            var speed = buffer.readFloat();
            var length = buffer.readInt();
            return new SpellCastSync(spellId, speed, length);
        }
    }

    public record SpellRequest(SpellCast.Action action, Identifier spellId, float progress, int[] targets, @Nullable Vec3 location) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "release_request");
        public static final CustomPacketPayload.Type<SpellRequest> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, SpellRequest> CODEC = StreamCodec.of(SpellRequest::write, SpellRequest::read);
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }

        public static void write(RegistryFriendlyByteBuf buffer, SpellRequest packet) {
            buffer.writeEnum(packet.action);
            buffer.writeUtf(packet.spellId.toString());
            buffer.writeFloat(packet.progress);
            buffer.writeVarIntArray(packet.targets);

            if (packet.location != null) {
                buffer.writeBoolean(true);
                buffer.writeDouble(packet.location.x);
                buffer.writeDouble(packet.location.y);
                buffer.writeDouble(packet.location.z);
            } else {
                buffer.writeBoolean(false);
            }
        }

        public static SpellRequest read(RegistryFriendlyByteBuf buffer) {
            var action = buffer.readEnum(SpellCast.Action.class);
            var spellId = Identifier.parse(buffer.readUtf());
            var progress = buffer.readFloat();
            var targets = buffer.readVarIntArray();

            Vec3 location = null;
            var hasLocation = buffer.readBoolean();
            if (hasLocation) {
                var x = buffer.readDouble();
                var y = buffer.readDouble();
                var z = buffer.readDouble();
                location = new Vec3(x, y, z);
            }
            return new SpellRequest(action, spellId, progress, targets, location);
        }
    }

    public record SpellCooldown(Identifier spellId, int duration) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "spell_cooldown");
        public static final CustomPacketPayload.Type<SpellCooldown> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, SpellCooldown> CODEC = StreamCodec.of(SpellCooldown::write, SpellCooldown::read);
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }

        public static void write(RegistryFriendlyByteBuf buffer, SpellCooldown packet) {
            buffer.writeUtf(packet.spellId.toString());
            buffer.writeInt(packet.duration);
        }

        public static SpellCooldown read(RegistryFriendlyByteBuf buffer) {
            var spellId = Identifier.parse(buffer.readUtf());
            int duration = buffer.readInt();
            return new SpellCooldown(spellId, duration);
        }
    }

    public record SpellCooldownSync(int baseTick, Map<Identifier, SpellCooldownManager.Entry> cooldowns) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "cooldown_sync");
        public static final CustomPacketPayload.Type<SpellCooldownSync> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, SpellCooldownSync> CODEC = StreamCodec.of(SpellCooldownSync::write, SpellCooldownSync::read);
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }

        public static void write(RegistryFriendlyByteBuf buffer, SpellCooldownSync packet) {
            buffer.writeInt(packet.baseTick);
            buffer.writeInt(packet.cooldowns.size());
            for (var entry: packet.cooldowns.entrySet()) {
                buffer.writeUtf(entry.getKey().toString());
                buffer.writeInt(entry.getValue().startTick());
                buffer.writeInt(entry.getValue().endTick());
            }
        }

        public static SpellCooldownSync read(RegistryFriendlyByteBuf buffer) {
            int baseTick = buffer.readInt();
            int size = buffer.readInt();
            var cooldowns = new HashMap<Identifier, SpellCooldownManager.Entry>();
            for (int i = 0; i < size; ++i) {
                var spellId = Identifier.parse(buffer.readUtf());
                var startTick = buffer.readInt();
                var endTick = buffer.readInt();
                cooldowns.put(spellId, new SpellCooldownManager.Entry(startTick, endTick));
            }
            return new SpellCooldownSync(baseTick, cooldowns);
        }
    }

    public record SpellAnimation(int playerId, SpellCast.Animation animationType, String name, float speed) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "spell_animation");
        public static final CustomPacketPayload.Type<SpellAnimation> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, SpellAnimation> CODEC = StreamCodec.of(SpellAnimation::write, SpellAnimation::read);
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }

        public static void write(RegistryFriendlyByteBuf buffer, SpellAnimation packet) {
            buffer.writeInt(packet.playerId);
            buffer.writeInt(packet.animationType.ordinal());
            buffer.writeUtf(packet.name);
            buffer.writeFloat(packet.speed);
        }

        public static SpellAnimation read(RegistryFriendlyByteBuf buffer) {
            int playerId = buffer.readInt();
            var animationType = SpellCast.Animation.values()[buffer.readInt()];
            var name = buffer.readUtf();
            var speed = buffer.readFloat();
            return new SpellAnimation(playerId, animationType, name, speed);
        }
    }

    public record SpellMessage(String translationKey, ChatFormatting format) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "spell_message");
        public static final CustomPacketPayload.Type<SpellMessage> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, SpellMessage> CODEC = StreamCodec.of(SpellMessage::write, SpellMessage::read);
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }

        public static void write(RegistryFriendlyByteBuf buffer, SpellMessage packet) {
            buffer.writeUtf(packet.translationKey);
            buffer.writeInt(packet.format.ordinal());
        }

        public static SpellMessage read(RegistryFriendlyByteBuf buffer) {
            var text = buffer.readUtf();
            var format = ChatFormatting.values()[buffer.readInt()];
            return new SpellMessage(text, format);
        }
    }

    public record ParticleBatches(SourceType sourceType, float countMultiplier, List<Spawn> spawns) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "particle_effects");
        public static final CustomPacketPayload.Type<ParticleBatches> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ParticleBatches> CODEC = StreamCodec.of(ParticleBatches::writeStatic, ParticleBatches::read);
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }

        public enum SourceType { ENTITY, COORDINATE }
        public record Spawn(int sourceEntityId, float yaw, float pitch, Vec3 sourceLocation, ParticleBatch batch) { }

        public static void writeStatic(RegistryFriendlyByteBuf buffer, ParticleBatches packet) {
            buffer.writeInt(packet.sourceType.ordinal());
            buffer.writeInt(packet.spawns.size());
            for (var spawn: packet.spawns) {
                buffer.writeInt(spawn.sourceEntityId);
                buffer.writeFloat(spawn.yaw);
                buffer.writeFloat(spawn.pitch);
                buffer.writeDouble(spawn.sourceLocation.x);
                buffer.writeDouble(spawn.sourceLocation.y);
                buffer.writeDouble(spawn.sourceLocation.z);
                writeBatch(spawn.batch, buffer, packet.countMultiplier);
            }
        }

        private static void writeBatch(ParticleBatch batch, FriendlyByteBuf buffer, float countMultiplier) {
            buffer.writeUtf(batch.particle_id);
            buffer.writeInt(batch.shape.ordinal());
            buffer.writeInt(batch.origin.ordinal());
            buffer.writeInt(batch.rotation != null ? batch.rotation.ordinal() : -1);
            buffer.writeFloat(batch.roll);
            buffer.writeFloat(batch.roll_offset);
            buffer.writeFloat(batch.count * countMultiplier);
            buffer.writeFloat(batch.min_speed);
            buffer.writeFloat(batch.max_speed);
            buffer.writeFloat(batch.angle);
            buffer.writeFloat(batch.extent);
            buffer.writeFloat(batch.pre_spawn_travel);
            buffer.writeBoolean(batch.invert);

            buffer.writeLong(batch.color_rgba);
            buffer.writeFloat(batch.scale);
            buffer.writeBoolean(batch.follow_entity);
            buffer.writeFloat(batch.max_age);
        }

        private static ParticleBatch readBatch(RegistryFriendlyByteBuf buffer) {
            return new ParticleBatch(
                    buffer.readUtf(),
                    ParticleBatch.Shape.values()[buffer.readInt()],
                    ParticleBatch.Origin.values()[buffer.readInt()],
                    ParticleBatch.Rotation.from(buffer.readInt()),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readBoolean(),

                    buffer.readLong(),
                    buffer.readFloat(),
                    buffer.readBoolean(),
                    buffer.readFloat()
            );
        }

        public static ParticleBatches read(RegistryFriendlyByteBuf buffer) {
            var sourceType = SourceType.values()[buffer.readInt()];
            var spawnCount = buffer.readInt();
            var spawns = new ArrayList<Spawn>();
            for (int i = 0; i < spawnCount; ++i) {
                spawns.add(new Spawn(
                        buffer.readInt(),
                        buffer.readFloat(),
                        buffer.readFloat(),
                        new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                        readBatch(buffer)
                ));
            }
            return new ParticleBatches(sourceType, 1, spawns);
        }
    }

    public record SpellContainerSync(LinkedHashMap<String, SpellContainer> containers) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "spell_container_sync");
        public static final CustomPacketPayload.Type<SpellContainerSync> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<FriendlyByteBuf, SpellContainerSync> CODEC = StreamCodec.of(SpellContainerSync::write, SpellContainerSync::read);
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }

        private static final Gson gson = new Gson();
        public static void write(FriendlyByteBuf buffer, SpellContainerSync packet) {
            buffer.writeInt(packet.containers.size());
            for (var entry: packet.containers.entrySet()) {
                buffer.writeUtf(entry.getKey());
                var json = gson.toJson(entry.getValue());
                buffer.writeUtf(json);
            }
        }

        public static SpellContainerSync read(FriendlyByteBuf buffer) {
            int size = buffer.readInt();
            var containers = new LinkedHashMap<String, SpellContainer>();
            for (int i = 0; i < size; ++i) {
                var key = buffer.readUtf();
                var json = buffer.readUtf();
                var container = gson.fromJson(json, SpellContainer.class);
                containers.put(key, container);
            }
            return new SpellContainerSync(containers);
        }
    }

    public record ConfigSync(ServerConfig config) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "config_sync");
        public static final CustomPacketPayload.Type<ConfigSync> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<FriendlyByteBuf, ConfigSync> CODEC = StreamCodec.of(ConfigSync::write, ConfigSync::read);
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }

        private static final Gson gson = new Gson();

        public static void write(FriendlyByteBuf buffer, ConfigSync packet) {
            var json = gson.toJson(packet.config);
            buffer.writeUtf(json);
        }

        public static ConfigSync read(FriendlyByteBuf buffer) {
            var gson = new Gson();
            var json = buffer.readUtf();
            var config = gson.fromJson(json, ServerConfig.class);
            return new ConfigSync(config);
        }
    }

    public record SpellRegistrySync(List<String> chunks) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "spell_registry_sync");
        public static final CustomPacketPayload.Type<SpellRegistrySync> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<FriendlyByteBuf, SpellRegistrySync> CODEC = StreamCodec.of(SpellRegistrySync::write, SpellRegistrySync::read);

        private static void write(FriendlyByteBuf buffer, SpellRegistrySync packet) {
            buffer.writeInt(packet.chunks.size());
            for (var chunk: packet.chunks) {
                buffer.writeUtf(chunk);
            }
        }

        private static SpellRegistrySync read(FriendlyByteBuf buffer) {
            var chunkCount = buffer.readInt();
            var chunks = new ArrayList<String>();
            for (int i = 0; i < chunkCount; ++i) {
                chunks.add(buffer.readUtf());
            }
            return new SpellRegistrySync(chunks);
        }

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }
    }

    public record Ack(String code) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "ack");
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
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }
    }

    public record AttackAvailable(Identifier spellId, List<Melee.Attack> attacks) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "attack_available");
        public static final CustomPacketPayload.Type<AttackAvailable> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<FriendlyByteBuf, AttackAvailable> CODEC = StreamCodec.of(AttackAvailable::write, AttackAvailable::read);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }

        private static final Gson gson = new Gson();

        public static void write(FriendlyByteBuf buffer, AttackAvailable packet) {
            buffer.writeUtf(packet.spellId.toString());

            // Serialize MeleeAttack list to JSON
            buffer.writeInt(packet.attacks.size());
            for (var attack : packet.attacks) {
                var attackJson = gson.toJson(attack);
                buffer.writeUtf(attackJson);
            }
        }

        public static AttackAvailable read(FriendlyByteBuf buffer) {
            var spellId = Identifier.parse(buffer.readUtf());

            // Deserialize MeleeAttack list from JSON
            var attackCount = buffer.readInt();
            var attacks = new ArrayList<Melee.Attack>();
            for (int i = 0; i < attackCount; i++) {
                var attackJson = buffer.readUtf();
                var attack = gson.fromJson(attackJson, Melee.Attack.class);
                attacks.add(attack);
            }

            return new AttackAvailable(spellId, attacks);
        }
    }

    public record AttackPerform(Melee.AttackContext attackContext, int[] targetIds) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "attack_perform");
        public static final CustomPacketPayload.Type<AttackPerform> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<FriendlyByteBuf, AttackPerform> CODEC = StreamCodec.of(AttackPerform::write, AttackPerform::read);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }

        public static void write(FriendlyByteBuf buffer, AttackPerform packet) {
            buffer.writeUtf(packet.attackContext.spellId().toString());
            buffer.writeUtf(packet.attackContext.attackId());
            buffer.writeVarIntArray(packet.targetIds);
        }

        public static AttackPerform read(FriendlyByteBuf buffer) {
            var spellId = Identifier.parse(buffer.readUtf());
            var attackId = buffer.readUtf();
            var context = new Melee.AttackContext(spellId, attackId);
            var targetIds = buffer.readVarIntArray();
            return new AttackPerform(context, targetIds);
        }
    }

    public record AttackFxBroadcast(Melee.AttackContext attackContext) implements CustomPacketPayload {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "attack_fx_broadcast");
        public static final CustomPacketPayload.Type<AttackFxBroadcast> PACKET_ID = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<FriendlyByteBuf, AttackFxBroadcast> CODEC = StreamCodec.of(AttackFxBroadcast::write, AttackFxBroadcast::read);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return PACKET_ID;
        }

        public static void write(FriendlyByteBuf buffer, AttackFxBroadcast packet) {
            buffer.writeUtf(packet.attackContext.spellId().toString());
            buffer.writeUtf(packet.attackContext.attackId());
        }

        public static AttackFxBroadcast read(FriendlyByteBuf buffer) {
            var spellId = Identifier.parse(buffer.readUtf());
            var attackId = buffer.readUtf();
            return new AttackFxBroadcast(new Melee.AttackContext(spellId, attackId));
        }
    }
}
