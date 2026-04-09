package com.ultra.megamod.feature.combat.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-to-client payload sent when a player joins without having chosen a class.
 * Triggers the ClassSelectionScreen on the client.
 */
public record ClassSelectionPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClassSelectionPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "class_selection"));

    public static final StreamCodec<FriendlyByteBuf, ClassSelectionPayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, ClassSelectionPayload>() {
                @Override
                public ClassSelectionPayload decode(FriendlyByteBuf buf) {
                    return new ClassSelectionPayload();
                }

                @Override
                public void encode(FriendlyByteBuf buf, ClassSelectionPayload payload) {
                    // No data needed — the packet itself is the signal
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(ClassSelectionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new com.ultra.megamod.feature.combat.client.ClassSelectionScreen());
        });
    }
}
