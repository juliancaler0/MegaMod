package com.ultra.megamod.feature.casino.network;

import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.casino.baccarat.BaccaratGame;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client -> Server payload for baccarat actions.
 * Actions: "bet", "deal", "reset"
 */
public record BaccaratActionPayload(String action, String side, int amount, BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BaccaratActionPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "baccarat_action"));

    public static final StreamCodec<FriendlyByteBuf, BaccaratActionPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BaccaratActionPayload decode(FriendlyByteBuf buf) {
                    String action = buf.readUtf(256);
                    String side = buf.readUtf(256);
                    int amount = buf.readInt();
                    BlockPos pos = buf.readBlockPos();
                    return new BaccaratActionPayload(action, side, amount, pos);
                }

                @Override
                public void encode(FriendlyByteBuf buf, BaccaratActionPayload payload) {
                    buf.writeUtf(payload.action(), 256);
                    buf.writeUtf(payload.side(), 256);
                    buf.writeInt(payload.amount());
                    buf.writeBlockPos(payload.pos());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // In-memory game instances keyed by player UUID (one game per player)
    private static final Map<UUID, BaccaratGame> GAMES = new HashMap<>();

    public static BaccaratGame getOrCreateGame(UUID playerId) {
        return GAMES.computeIfAbsent(playerId, id -> new BaccaratGame());
    }

    public static void removeGame(UUID playerId) {
        GAMES.remove(playerId);
    }

    public static java.util.Collection<BaccaratGame> allGames() {
        return GAMES.values();
    }

    public static void handleOnServer(BaccaratActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            ServerLevel level = (ServerLevel) serverPlayer.level();
            UUID playerId = serverPlayer.getUUID();
            EconomyManager eco = EconomyManager.get(level);
            CasinoManager casinoMgr = CasinoManager.get(level);

            BaccaratGame game = getOrCreateGame(playerId);

            switch (payload.action().toLowerCase()) {
                case "bet" -> {
                    if (game.getPhase() != BaccaratGame.Phase.BETTING) {
                        // Auto-reset if trying to bet while in RESULT phase
                        if (game.getPhase() == BaccaratGame.Phase.RESULT) {
                            game.reset();
                        } else {
                            syncToPlayer(serverPlayer, game);
                            return;
                        }
                    }
                    boolean success = game.placeBet(playerId, payload.side(), payload.amount(), eco, level);
                    if (success) {
                        // Immediately deal after betting
                        game.deal(casinoMgr);
                        game.resolve(eco, casinoMgr, level);
                    }
                    syncToPlayer(serverPlayer, game);
                }
                case "deal" -> {
                    // Manual deal if needed (bet already placed)
                    if (game.getPhase() == BaccaratGame.Phase.BETTING && game.getBetAmount() > 0) {
                        game.deal(casinoMgr);
                        game.resolve(eco, casinoMgr, level);
                    }
                    syncToPlayer(serverPlayer, game);
                }
                case "reset" -> {
                    game.reset();
                    syncToPlayer(serverPlayer, game);
                }
                case "sync" -> {
                    // Client requesting current state
                    syncToPlayer(serverPlayer, game);
                }
            }
        });
    }

    private static void syncToPlayer(ServerPlayer player, BaccaratGame game) {
        PacketDistributor.sendToPlayer(player, new BaccaratGameSyncPayload(game.toJson()));
    }
}
