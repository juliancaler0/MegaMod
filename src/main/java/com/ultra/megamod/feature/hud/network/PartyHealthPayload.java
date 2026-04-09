package com.ultra.megamod.feature.hud.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PartyHealthPayload(List<MemberInfo> members) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PartyHealthPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "party_health"));

    public record MemberInfo(String name, float health, float maxHealth, boolean online) {}

    public static volatile List<MemberInfo> clientMembers = List.of();

    public static final StreamCodec<FriendlyByteBuf, PartyHealthPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PartyHealthPayload decode(FriendlyByteBuf buf) {
            int count = buf.readVarInt();
            List<MemberInfo> list = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                list.add(new MemberInfo(buf.readUtf(), buf.readFloat(), buf.readFloat(), buf.readBoolean()));
            }
            return new PartyHealthPayload(list);
        }

        @Override
        public void encode(FriendlyByteBuf buf, PartyHealthPayload payload) {
            buf.writeVarInt(payload.members().size());
            for (MemberInfo m : payload.members()) {
                buf.writeUtf(m.name());
                buf.writeFloat(m.health());
                buf.writeFloat(m.maxHealth());
                buf.writeBoolean(m.online());
            }
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleOnClient(PartyHealthPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> clientMembers = List.copyOf(payload.members()));
    }
}
