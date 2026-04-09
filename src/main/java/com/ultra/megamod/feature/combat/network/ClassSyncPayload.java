package com.ultra.megamod.feature.combat.network;

import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import com.ultra.megamod.feature.combat.client.ClientClassCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-to-client payload that syncs the player's class selection.
 * Sent on login (if class is already chosen) and after class choice.
 */
public record ClassSyncPayload(String className) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClassSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "class_sync"));

    public static final StreamCodec<FriendlyByteBuf, ClassSyncPayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, ClassSyncPayload>() {
                @Override
                public ClassSyncPayload decode(FriendlyByteBuf buf) {
                    return new ClassSyncPayload(buf.readUtf(64));
                }

                @Override
                public void encode(FriendlyByteBuf buf, ClassSyncPayload payload) {
                    buf.writeUtf(payload.className(), 64);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(ClassSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                PlayerClass cls = PlayerClass.valueOf(payload.className().toUpperCase());
                ClientClassCache.setPlayerClass(cls);
            } catch (IllegalArgumentException e) {
                ClientClassCache.setPlayerClass(PlayerClass.NONE);
            }
        });
    }
}
