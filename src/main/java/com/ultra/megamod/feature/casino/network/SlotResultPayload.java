package com.ultra.megamod.feature.casino.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SlotResultPayload(int stop0, int stop1, int stop2, int totalWin, int newWallet, String winsJson) implements CustomPacketPayload {
    public static volatile SlotResultPayload lastResult = null;

    public static final CustomPacketPayload.Type<SlotResultPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "slot_result"));

    public static final StreamCodec<FriendlyByteBuf, SlotResultPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SlotResultPayload decode(FriendlyByteBuf buf) {
                    int s0 = buf.readInt();
                    int s1 = buf.readInt();
                    int s2 = buf.readInt();
                    int totalWin = buf.readInt();
                    int newWallet = buf.readInt();
                    String winsJson = buf.readUtf(524288);
                    return new SlotResultPayload(s0, s1, s2, totalWin, newWallet, winsJson);
                }

                @Override
                public void encode(FriendlyByteBuf buf, SlotResultPayload payload) {
                    buf.writeInt(payload.stop0());
                    buf.writeInt(payload.stop1());
                    buf.writeInt(payload.stop2());
                    buf.writeInt(payload.totalWin());
                    buf.writeInt(payload.newWallet());
                    buf.writeUtf(payload.winsJson(), 524288);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(SlotResultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            lastResult = payload;
        });
    }
}
