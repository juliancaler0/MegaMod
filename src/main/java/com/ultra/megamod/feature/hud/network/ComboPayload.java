package com.ultra.megamod.feature.hud.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ComboPayload(int comboCount, int comboTimerTicks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "kill_combo"));

    public static volatile int clientCombo = 0;
    public static volatile int clientTimer = 0;
    public static volatile long lastUpdateMs = 0;

    public static final StreamCodec<FriendlyByteBuf, ComboPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ComboPayload decode(FriendlyByteBuf buf) { return new ComboPayload(buf.readVarInt(), buf.readVarInt()); }
        @Override
        public void encode(FriendlyByteBuf buf, ComboPayload p) { buf.writeVarInt(p.comboCount()); buf.writeVarInt(p.comboTimerTicks()); }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleOnClient(ComboPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            clientCombo = payload.comboCount();
            clientTimer = payload.comboTimerTicks();
            lastUpdateMs = System.currentTimeMillis();
        });
    }
}
