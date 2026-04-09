package com.ultra.megamod.feature.economy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayerInfoSyncPayload(int wallet, int bank, int totalLevel,
        int totalPrestige, String badgeTitle, String badgeColorCode) implements CustomPacketPayload {

    // Client-side cache — written by network handler, read by HUD overlay
    public static volatile int clientWallet = 0;
    public static volatile int clientBank = 0;
    public static volatile int clientTotalLevel = 0;
    public static volatile int clientTotalPrestige = 0;
    public static volatile String clientBadgeTitle = "";
    public static volatile String clientBadgeColorCode = "";

    public static final CustomPacketPayload.Type<PlayerInfoSyncPayload> TYPE =
            new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("megamod", "player_info_sync"));

    public static final StreamCodec<FriendlyByteBuf, PlayerInfoSyncPayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, PlayerInfoSyncPayload>() {
                public PlayerInfoSyncPayload decode(FriendlyByteBuf buf) {
                    int wallet = buf.readInt();
                    int bank = buf.readInt();
                    int totalLevel = buf.readInt();
                    int totalPrestige = buf.readInt();
                    String badgeTitle = buf.readUtf(256);
                    String badgeColorCode = buf.readUtf(16);
                    return new PlayerInfoSyncPayload(wallet, bank, totalLevel, totalPrestige, badgeTitle, badgeColorCode);
                }

                public void encode(FriendlyByteBuf buf, PlayerInfoSyncPayload payload) {
                    buf.writeInt(payload.wallet());
                    buf.writeInt(payload.bank());
                    buf.writeInt(payload.totalLevel());
                    buf.writeInt(payload.totalPrestige());
                    buf.writeUtf(payload.badgeTitle(), 256);
                    buf.writeUtf(payload.badgeColorCode(), 16);
                }
            };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(PlayerInfoSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            clientWallet = payload.wallet();
            clientBank = payload.bank();
            clientTotalLevel = payload.totalLevel();
            clientTotalPrestige = payload.totalPrestige();
            clientBadgeTitle = payload.badgeTitle();
            clientBadgeColorCode = payload.badgeColorCode();
        });
    }
}
