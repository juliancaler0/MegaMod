package com.ultra.megamod.feature.dungeons.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BossMusicPayload(int entityId, boolean play) implements CustomPacketPayload
{
    public static volatile BossMusicPayload lastPayload = null;

    public static final CustomPacketPayload.Type<BossMusicPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"boss_music"));
    public static final StreamCodec<FriendlyByteBuf, BossMusicPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, BossMusicPayload>(){

        public BossMusicPayload decode(FriendlyByteBuf buf) {
            return new BossMusicPayload(buf.readVarInt(), buf.readBoolean());
        }

        public void encode(FriendlyByteBuf buf, BossMusicPayload payload) {
            buf.writeVarInt(payload.entityId());
            buf.writeBoolean(payload.play());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(BossMusicPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> lastPayload = payload);
    }
}

