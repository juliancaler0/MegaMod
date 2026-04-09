package com.ultra.megamod.feature.hud.network;

import com.ultra.megamod.feature.hud.ScreenShakeEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ScreenShakePayload(float intensity) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ScreenShakePayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "screen_shake"));

    public static final StreamCodec<FriendlyByteBuf, ScreenShakePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ScreenShakePayload decode(FriendlyByteBuf buf) { return new ScreenShakePayload(buf.readFloat()); }
        @Override
        public void encode(FriendlyByteBuf buf, ScreenShakePayload p) { buf.writeFloat(p.intensity()); }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleOnClient(ScreenShakePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ScreenShakeEffect.trigger(payload.intensity()));
    }
}
