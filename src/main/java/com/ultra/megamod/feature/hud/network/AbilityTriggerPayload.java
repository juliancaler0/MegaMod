package com.ultra.megamod.feature.hud.network;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.hud.AbilityTriggerHud;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

/**
 * Server → Client: tells the client to show a brief "ability triggered" HUD popup.
 *
 * @param label the human-readable ability or effect name
 * @param kind  0 = MANUAL cast (right-click), 1 = PASSIVE on-hit
 */
public record AbilityTriggerPayload(String label, int kind) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AbilityTriggerPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MegaMod.MODID, "ability_trigger_hud"));

    public static final StreamCodec<FriendlyByteBuf, AbilityTriggerPayload> STREAM_CODEC =
            StreamCodec.of(AbilityTriggerPayload::write, AbilityTriggerPayload::read);

    private static void write(FriendlyByteBuf buf, AbilityTriggerPayload p) {
        buf.writeUtf(p.label, 64);
        buf.writeByte(p.kind);
    }

    private static AbilityTriggerPayload read(FriendlyByteBuf buf) {
        return new AbilityTriggerPayload(buf.readUtf(64), buf.readByte());
    }

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleOnClient(AbilityTriggerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> AbilityTriggerHud.show(payload.label(), AbilityTriggerHud.Kind.fromOrdinal(payload.kind())));
    }
}
