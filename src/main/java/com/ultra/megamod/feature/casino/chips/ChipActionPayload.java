package com.ultra.megamod.feature.casino.chips;

import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: buy/sell/cashout chips.
 */
public record ChipActionPayload(String action, int denomination, int count) implements CustomPacketPayload {

    public static final Type<ChipActionPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("megamod", "chip_action"));

    public static final StreamCodec<FriendlyByteBuf, ChipActionPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ChipActionPayload decode(FriendlyByteBuf buf) {
            return new ChipActionPayload(buf.readUtf(), buf.readVarInt(), buf.readVarInt());
        }
        @Override
        public void encode(FriendlyByteBuf buf, ChipActionPayload p) {
            buf.writeUtf(p.action());
            buf.writeVarInt(p.denomination());
            buf.writeVarInt(p.count());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleServer(ChipActionPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            ServerLevel level = (ServerLevel) player.level();
            EconomyManager eco = EconomyManager.get(level.getServer().overworld());
            ChipManager chips = ChipManager.get(level.getServer().overworld());

            switch (payload.action()) {
                case "buy" -> {
                    ChipDenomination denom = ChipDenomination.fromValue(payload.denomination());
                    if (denom != null && payload.count() > 0) {
                        chips.buyChips(player.getUUID(), denom, payload.count(), eco);
                    }
                }
                case "sell" -> {
                    ChipDenomination denom = ChipDenomination.fromValue(payload.denomination());
                    if (denom != null && payload.count() > 0) {
                        chips.sellChips(player.getUUID(), denom, payload.count(), eco);
                    }
                }
                case "cashout" -> {
                    chips.cashOutAll(player.getUUID(), eco);
                }
                case "sync" -> {} // just sync
            }

            // Always send updated chip state back (data refresh only, don't reopen screen)
            sendChipSync(player, eco, false);
        });
    }

    public static void sendChipSync(ServerPlayer player, EconomyManager eco) {
        sendChipSync(player, eco, false);
    }

    public static void sendChipSync(ServerPlayer player, EconomyManager eco, boolean openScreen) {
        ChipManager chips = ChipManager.get(player.level().getServer().overworld());
        String json = chips.toJson(player.getUUID());
        int wallet = eco.getWallet(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ChipSyncPayload(json, wallet, openScreen));
    }
}
