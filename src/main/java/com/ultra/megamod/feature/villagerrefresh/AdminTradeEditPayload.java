package com.ultra.megamod.feature.villagerrefresh;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Payload for admin trade editing actions.
 * Actions: 0=REROLL_SINGLE, 1=REROLL_ALL, 2=SET_LEVEL, 3=ADD_TRADE, 4=SEEK_TRADE
 */
public record AdminTradeEditPayload(int villagerEntityId, int action, int tradeIndex, int data, long lockedMask, String searchTerm) implements CustomPacketPayload {

    public static final int ACTION_REROLL_SINGLE = 0;
    public static final int ACTION_REROLL_ALL = 1;
    public static final int ACTION_SET_LEVEL = 2;
    public static final int ACTION_ADD_TRADE = 3;
    public static final int ACTION_SEEK_TRADE = 4;
    public static final int ACTION_CREATE_CUSTOM = 5;

    public static final CustomPacketPayload.Type<AdminTradeEditPayload> TYPE =
            new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("megamod", "admin_trade_edit"));

    public static final StreamCodec<FriendlyByteBuf, AdminTradeEditPayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, AdminTradeEditPayload>() {
                @Override
                public AdminTradeEditPayload decode(FriendlyByteBuf buf) {
                    return new AdminTradeEditPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readLong(), buf.readUtf(256));
                }

                @Override
                public void encode(FriendlyByteBuf buf, AdminTradeEditPayload payload) {
                    buf.writeVarInt(payload.villagerEntityId());
                    buf.writeVarInt(payload.action());
                    buf.writeVarInt(payload.tradeIndex());
                    buf.writeVarInt(payload.data());
                    buf.writeLong(payload.lockedMask());
                    buf.writeUtf(payload.searchTerm(), 256);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(AdminTradeEditPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                AdminTradeEditorHandler.handle(serverPlayer, payload);
            }
        });
    }
}
