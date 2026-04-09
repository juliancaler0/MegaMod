/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 */
package com.ultra.megamod.feature.computer.network;

import com.ultra.megamod.feature.computer.network.ComputerActionHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ComputerActionPayload(String action, String jsonData) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ComputerActionPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"computer_action"));
    public static final StreamCodec<FriendlyByteBuf, ComputerActionPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, ComputerActionPayload>(){

        public ComputerActionPayload decode(FriendlyByteBuf buf) {
            String action = buf.readUtf(256);
            String jsonData = buf.readUtf(32768);
            return new ComputerActionPayload(action, jsonData);
        }

        public void encode(FriendlyByteBuf buf, ComputerActionPayload payload) {
            buf.writeUtf(payload.action(), 256);
            buf.writeUtf(payload.jsonData(), 32768);
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(ComputerActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player patt0$temp = context.player();
            if (patt0$temp instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)patt0$temp;
                ComputerActionHandler.handleAction(serverPlayer, payload.action(), payload.jsonData());
            }
        });
    }
}

