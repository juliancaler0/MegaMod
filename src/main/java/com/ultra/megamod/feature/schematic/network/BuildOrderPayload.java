package com.ultra.megamod.feature.schematic.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> Server: manages build orders (assign builder, cancel, pause, resume).
 */
public record BuildOrderPayload(
        String action,
        String orderId,
        String jsonData
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BuildOrderPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "build_order"));

    public static final StreamCodec<FriendlyByteBuf, BuildOrderPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BuildOrderPayload decode(FriendlyByteBuf buf) {
                    return new BuildOrderPayload(buf.readUtf(64), buf.readUtf(64), buf.readUtf(4096));
                }

                @Override
                public void encode(FriendlyByteBuf buf, BuildOrderPayload payload) {
                    buf.writeUtf(payload.action(), 64);
                    buf.writeUtf(payload.orderId(), 64);
                    buf.writeUtf(payload.jsonData(), 4096);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(BuildOrderPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                SchematicServerHandler.handleBuildOrder(serverPlayer, payload);
            }
        });
    }
}
