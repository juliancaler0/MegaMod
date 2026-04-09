package com.ultra.megamod.feature.furniture;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record QuestBoardDataPayload(String dataType, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QuestBoardDataPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "quest_board_data"));

    public static final StreamCodec<FriendlyByteBuf, QuestBoardDataPayload> STREAM_CODEC =
        StreamCodec.of(QuestBoardDataPayload::write, QuestBoardDataPayload::read);

    // Client-side last response — polled by QuestBoardScreen in tick()
    public static volatile QuestBoardDataPayload lastResponse;

    private static void write(FriendlyByteBuf buf, QuestBoardDataPayload payload) {
        buf.writeUtf(payload.dataType, 256);
        buf.writeUtf(payload.jsonData, 32767);
    }

    private static QuestBoardDataPayload read(FriendlyByteBuf buf) {
        return new QuestBoardDataPayload(buf.readUtf(256), buf.readUtf(32767));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
