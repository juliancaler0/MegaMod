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
package com.ultra.megamod.feature.villagerrefresh;

import com.ultra.megamod.feature.villagerrefresh.VillagerTradeRefresh;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RefreshTradesPayload(int villagerEntityId) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<RefreshTradesPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"refresh_trades"));
    public static final StreamCodec<FriendlyByteBuf, RefreshTradesPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, RefreshTradesPayload>(){

        public RefreshTradesPayload decode(FriendlyByteBuf buf) {
            return new RefreshTradesPayload(buf.readVarInt());
        }

        public void encode(FriendlyByteBuf buf, RefreshTradesPayload payload) {
            buf.writeVarInt(payload.villagerEntityId());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(RefreshTradesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player patt0$temp = context.player();
            if (patt0$temp instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)patt0$temp;
                VillagerTradeRefresh.handleRefreshRequest(serverPlayer, payload.villagerEntityId());
            }
        });
    }
}

