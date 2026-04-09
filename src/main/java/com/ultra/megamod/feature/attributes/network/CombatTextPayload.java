package com.ultra.megamod.feature.attributes.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.ArrayList;
import java.util.List;

public record CombatTextPayload(List<CombatTextEntry> entries) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CombatTextPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "combat_text"));

    public static final StreamCodec<FriendlyByteBuf, CombatTextPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CombatTextPayload decode(FriendlyByteBuf buf) {
            int count = buf.readVarInt();
            List<CombatTextEntry> entries = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                entries.add(new CombatTextEntry(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readUtf(), buf.readInt(), buf.readFloat()
                ));
            }
            return new CombatTextPayload(entries);
        }

        @Override
        public void encode(FriendlyByteBuf buf, CombatTextPayload payload) {
            buf.writeVarInt(payload.entries().size());
            for (CombatTextEntry e : payload.entries()) {
                buf.writeDouble(e.x());
                buf.writeDouble(e.y());
                buf.writeDouble(e.z());
                buf.writeUtf(e.text());
                buf.writeInt(e.color());
                buf.writeFloat(e.scale());
            }
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleOnClient(CombatTextPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            for (CombatTextEntry entry : payload.entries()) {
                CombatTextRenderer.addText(entry);
            }
        });
    }

    public record CombatTextEntry(double x, double y, double z, String text, int color, float scale) {}
}
