package com.ultra.megamod.feature.hud.network;

import com.ultra.megamod.feature.hud.DeathRecapOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record DeathRecapPayload(List<DamageEntry> entries, String killerName, String killerType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DeathRecapPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "death_recap"));

    public record DamageEntry(String sourceName, String damageType, float amount, int ticksAgo) {}

    public static final StreamCodec<FriendlyByteBuf, DeathRecapPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public DeathRecapPayload decode(FriendlyByteBuf buf) {
            int count = buf.readVarInt();
            List<DamageEntry> entries = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                entries.add(new DamageEntry(buf.readUtf(), buf.readUtf(), buf.readFloat(), buf.readVarInt()));
            }
            return new DeathRecapPayload(entries, buf.readUtf(), buf.readUtf());
        }

        @Override
        public void encode(FriendlyByteBuf buf, DeathRecapPayload p) {
            buf.writeVarInt(p.entries().size());
            for (DamageEntry e : p.entries()) {
                buf.writeUtf(e.sourceName()); buf.writeUtf(e.damageType());
                buf.writeFloat(e.amount()); buf.writeVarInt(e.ticksAgo());
            }
            buf.writeUtf(p.killerName()); buf.writeUtf(p.killerType());
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleOnClient(DeathRecapPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> DeathRecapOverlay.setRecap(payload));
    }
}
