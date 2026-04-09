package com.ultra.megamod.feature.casino.chips;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client: syncs chip inventory and wallet balance.
 */
public record ChipSyncPayload(String chipJson, int wallet, boolean openScreen) implements CustomPacketPayload {

    public static volatile int[] clientChips = new int[ChipDenomination.values().length];
    public static volatile int clientChipTotal = 0;
    public static volatile boolean shouldOpenCashier = false;

    public static final Type<ChipSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("megamod", "chip_sync"));

    public static final StreamCodec<FriendlyByteBuf, ChipSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ChipSyncPayload decode(FriendlyByteBuf buf) {
            return new ChipSyncPayload(buf.readUtf(), buf.readVarInt(), buf.readBoolean());
        }
        @Override
        public void encode(FriendlyByteBuf buf, ChipSyncPayload p) {
            buf.writeUtf(p.chipJson());
            buf.writeVarInt(p.wallet());
            buf.writeBoolean(p.openScreen());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleClient(ChipSyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                JsonObject root = JsonParser.parseString(payload.chipJson()).getAsJsonObject();
                ChipDenomination[] denoms = ChipDenomination.values();
                for (int i = 0; i < denoms.length; i++) {
                    String key = String.valueOf(denoms[i].value);
                    clientChips[i] = root.has(key) ? root.get(key).getAsInt() : 0;
                }
                clientChipTotal = root.has("total") ? root.get("total").getAsInt() : 0;
            } catch (Exception ignored) {}
            // Also update wallet
            com.ultra.megamod.feature.economy.network.PlayerInfoSyncPayload.clientWallet = payload.wallet();
            // Always copy chip data to renderer for live screen updates
            System.arraycopy(clientChips, 0, ChipRenderer.clientChips, 0,
                    Math.min(clientChips.length, ChipRenderer.clientChips.length));
            ChipRenderer.clientChipTotal = clientChipTotal;
            // Only signal screen open when server explicitly requests it (initial interaction)
            if (payload.openScreen()) {
                shouldOpenCashier = true;
            }
        });
    }
}
