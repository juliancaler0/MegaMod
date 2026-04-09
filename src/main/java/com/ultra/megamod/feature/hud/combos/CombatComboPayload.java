package com.ultra.megamod.feature.hud.combos;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> Client payload for displaying a combo name on the HUD.
 */
public record CombatComboPayload(String comboName, int color) implements CustomPacketPayload {

    public static volatile String lastComboName = "";
    public static volatile long lastComboTime = 0;
    public static volatile int lastComboColor = 0xFFFFFFFF;

    public static final CustomPacketPayload.Type<CombatComboPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "combat_combo"));

    public static final StreamCodec<FriendlyByteBuf, CombatComboPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public CombatComboPayload decode(FriendlyByteBuf buf) {
                return new CombatComboPayload(buf.readUtf(), buf.readInt());
            }

            @Override
            public void encode(FriendlyByteBuf buf, CombatComboPayload payload) {
                buf.writeUtf(payload.comboName());
                buf.writeInt(payload.color());
            }
        };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(CombatComboPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            lastComboName = payload.comboName();
            lastComboColor = payload.color();
            lastComboTime = System.currentTimeMillis();
        });
    }
}
