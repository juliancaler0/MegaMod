package com.ultra.megamod.feature.hud.network;

import com.ultra.megamod.feature.hud.LootPickupLog;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LootPickupPayload(String itemName, int count) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LootPickupPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "loot_pickup"));

    public static final StreamCodec<FriendlyByteBuf, LootPickupPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public LootPickupPayload decode(FriendlyByteBuf buf) { return new LootPickupPayload(buf.readUtf(), buf.readVarInt()); }
        @Override
        public void encode(FriendlyByteBuf buf, LootPickupPayload p) { buf.writeUtf(p.itemName()); buf.writeVarInt(p.count()); }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleOnClient(LootPickupPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> LootPickupLog.addEntry(payload.itemName(), payload.count()));
    }
}
