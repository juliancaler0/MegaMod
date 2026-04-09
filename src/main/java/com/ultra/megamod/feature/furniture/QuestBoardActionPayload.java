package com.ultra.megamod.feature.furniture;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record QuestBoardActionPayload(String action, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QuestBoardActionPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "quest_board_action"));

    public static final StreamCodec<FriendlyByteBuf, QuestBoardActionPayload> STREAM_CODEC =
        StreamCodec.of(QuestBoardActionPayload::write, QuestBoardActionPayload::read);

    private static void write(FriendlyByteBuf buf, QuestBoardActionPayload payload) {
        buf.writeUtf(payload.action, 256);
        buf.writeUtf(payload.jsonData, 32767);
    }

    private static QuestBoardActionPayload read(FriendlyByteBuf buf) {
        return new QuestBoardActionPayload(buf.readUtf(256), buf.readUtf(32767));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
