package com.ultra.megamod.feature.casino.network;

import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.casino.craps.CrapsGame;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client -> Server payload for craps actions.
 *
 * Action format:
 *   "bet:pass"       - Place a Pass Line bet
 *   "bet:dontpass"   - Place a Don't Pass bet
 *   "bet:field"      - Place a Field bet
 *   "bet:come"       - Place a Come bet
 *   "bet:place_6"    - Place bet on 6
 *   "bet:hard_8"     - Hardway bet on 8
 *   "roll"           - Roll the dice
 *   "leave"          - Leave the table
 *   "bet"            - Legacy: treated as "bet:pass"
 */
public record CrapsActionPayload(String action, int amount, BlockPos tablePos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CrapsActionPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "craps_action"));

    public static final StreamCodec<FriendlyByteBuf, CrapsActionPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public CrapsActionPayload decode(FriendlyByteBuf buf) {
                    String action = buf.readUtf(256);
                    int amount = buf.readInt();
                    BlockPos pos = buf.readBlockPos();
                    return new CrapsActionPayload(action, amount, pos);
                }

                @Override
                public void encode(FriendlyByteBuf buf, CrapsActionPayload payload) {
                    buf.writeUtf(payload.action(), 256);
                    buf.writeInt(payload.amount());
                    buf.writeBlockPos(payload.tablePos());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // ---- In-memory game instances keyed by table BlockPos ----
    private static final Map<BlockPos, CrapsGame> GAMES = new HashMap<>();

    public static CrapsGame getOrCreateGame(BlockPos pos) {
        return GAMES.computeIfAbsent(pos, p -> new CrapsGame());
    }

    public static void removeGame(BlockPos pos) {
        GAMES.remove(pos);
    }

    public static int gameCount() {
        return GAMES.size();
    }

    // ---- Server handler ----

    public static void handleOnServer(CrapsActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            ServerLevel level = (ServerLevel) serverPlayer.level();
            UUID playerId = serverPlayer.getUUID();
            EconomyManager eco = EconomyManager.get(level);
            CasinoManager casino = CasinoManager.get(level);
            CrapsGame game = getOrCreateGame(payload.tablePos());

            String action = payload.action();

            if (action.startsWith("bet:") || "bet".equals(action)) {
                // Parse bet type from action string
                String betType;
                if (action.startsWith("bet:")) {
                    betType = action.substring(4); // e.g., "bet:pass" -> "pass"
                } else {
                    betType = "pass"; // Legacy fallback
                }

                if (payload.amount() > 0 && CrapsGame.VALID_BET_TYPES.contains(betType)) {
                    game.placeBet(playerId, betType, payload.amount(), eco, level);
                }
            } else if ("roll".equals(action)) {
                game.roll(level, casino);
                // Pay out all resolved bets (one-roll bets resolve even if round continues)
                game.resolve(eco, casino, level);
                // If round ended, immediately reset for next round after payout
                if (game.getPhase() == CrapsGame.Phase.RESULT) {
                    game.resetForNextRound();
                }
                game.syncToPlayer(level);
            } else if ("sync".equals(action)) {
                // Send current state directly to the requesting player — don't use
                // game.syncToPlayer() which only sends to the game's active player
                // (would fail if no one has bet yet, i.e. playerId is null).
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer,
                        new CrapsGameSyncPayload(game.toJson()));
            } else if ("leave".equals(action)) {
                game.leaveTable(playerId);
            }
        });
    }
}
