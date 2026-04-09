package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.feature.combat.spell.client.BeamRenderState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-to-client payload that syncs a beam spell visual.
 * Sent when a beam spell fires so nearby clients can render the beam.
 */
public record BeamSyncPayload(
        double startX, double startY, double startZ,
        double endX, double endY, double endZ,
        int schoolOrdinal,
        int durationMs
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BeamSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "beam_sync"));

    public static final StreamCodec<FriendlyByteBuf, BeamSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BeamSyncPayload decode(FriendlyByteBuf buf) {
                    return new BeamSyncPayload(
                            buf.readDouble(), buf.readDouble(), buf.readDouble(),
                            buf.readDouble(), buf.readDouble(), buf.readDouble(),
                            buf.readVarInt(), buf.readVarInt()
                    );
                }

                @Override
                public void encode(FriendlyByteBuf buf, BeamSyncPayload payload) {
                    buf.writeDouble(payload.startX());
                    buf.writeDouble(payload.startY());
                    buf.writeDouble(payload.startZ());
                    buf.writeDouble(payload.endX());
                    buf.writeDouble(payload.endY());
                    buf.writeDouble(payload.endZ());
                    buf.writeVarInt(payload.schoolOrdinal());
                    buf.writeVarInt(payload.durationMs());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(BeamSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            BeamRenderState.addBeam(
                    new Vec3(payload.startX(), payload.startY(), payload.startZ()),
                    new Vec3(payload.endX(), payload.endY(), payload.endZ()),
                    payload.schoolOrdinal(),
                    payload.durationMs()
            );
        });
    }
}
