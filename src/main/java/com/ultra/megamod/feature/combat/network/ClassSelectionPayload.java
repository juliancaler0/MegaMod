package com.ultra.megamod.feature.combat.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.BiConsumer;

/**
 * Server-to-client payload sent when a player joins without having chosen a class.
 * Triggers the ClassSelectionScreen on the client.
 *
 * <p>Client-side screen opening is delegated through {@link #CLIENT_HANDLER} so the
 * common-side payload registration never links against {@code Minecraft}/{@code Screen}
 * (which would fail on the dedicated server's classloader).
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

    /**
     * Populated by the client-side proxy (MegaModClient) to open the ClassSelectionScreen.
     * Defaults to a no-op so the dedicated server never loads client classes.
     */
    public static BiConsumer<ClassSelectionPayload, IPayloadContext> CLIENT_HANDLER = (payload, ctx) -> {};

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(ClassSelectionPayload payload, IPayloadContext context) {
        CLIENT_HANDLER.accept(payload, context);
    }
}
