package com.ultra.megamod.feature.corruption.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-to-client payload that syncs nearby corruption zone boundaries.
 * Sent periodically so the client can render particle boundary effects.
 * Each zone entry contains: centerX, centerZ, radius, tier, corruptionLevel.
 */
public record CorruptionZoneSyncPayload(List<ZoneEntry> zones) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CorruptionZoneSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "corruption_zone_sync"));

    public record ZoneEntry(long centerX, long centerZ, int radius, int tier, int corruptionLevel) {}

    public static final StreamCodec<FriendlyByteBuf, CorruptionZoneSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CorruptionZoneSyncPayload decode(FriendlyByteBuf buf) {
            int count = buf.readVarInt();
            List<ZoneEntry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                long cx = buf.readLong();
                long cz = buf.readLong();
                int radius = buf.readVarInt();
                int tier = buf.readVarInt();
                int corruptionLevel = buf.readVarInt();
                entries.add(new ZoneEntry(cx, cz, radius, tier, corruptionLevel));
            }
            return new CorruptionZoneSyncPayload(entries);
        }

        @Override
        public void encode(FriendlyByteBuf buf, CorruptionZoneSyncPayload p) {
            buf.writeVarInt(p.zones().size());
            for (ZoneEntry entry : p.zones()) {
                buf.writeLong(entry.centerX());
                buf.writeLong(entry.centerZ());
                buf.writeVarInt(entry.radius());
                buf.writeVarInt(entry.tier());
                buf.writeVarInt(entry.corruptionLevel());
            }
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(CorruptionZoneSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> com.ultra.megamod.feature.corruption.client.CorruptionClientTracker.setZones(payload.zones()));
    }
}
