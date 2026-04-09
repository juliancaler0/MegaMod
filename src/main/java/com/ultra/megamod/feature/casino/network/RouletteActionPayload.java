package com.ultra.megamod.feature.casino.network;

import com.ultra.megamod.feature.casino.roulette.RouletteGame;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client->Server: player places a roulette bet.
 * Fields: betType (String), amount (int), tablePos (BlockPos).
 */
public record RouletteActionPayload(String betType, int amount, BlockPos tablePos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RouletteActionPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "roulette_action"));

    public static final StreamCodec<FriendlyByteBuf, RouletteActionPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public RouletteActionPayload decode(FriendlyByteBuf buf) {
                    String betType = buf.readUtf(256);
                    int amount = buf.readInt();
                    BlockPos pos = buf.readBlockPos();
                    return new RouletteActionPayload(betType, amount, pos);
                }

                @Override
                public void encode(FriendlyByteBuf buf, RouletteActionPayload payload) {
                    buf.writeUtf(payload.betType(), 256);
                    buf.writeInt(payload.amount());
                    buf.writeBlockPos(payload.tablePos());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Server-side handler. Looks up the roulette game associated with the table
     * position and forwards the bet.
     *
     * The roulette game instance is managed per-table. For now we use a simple
     * static map keyed by BlockPos, similar to how WheelBlockEntity holds a WheelGame.
     * If a RouletteTableBlockEntity is added later, this can be refactored to use it.
     */
    public static void handleOnServer(RouletteActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            ServerLevel level = (ServerLevel) serverPlayer.level();

            // Get or create the roulette game for this table position
            RouletteGame game = RouletteTableRegistry.getOrCreate(payload.tablePos());

            // Sync request (empty bet type or zero amount) — just send current state
            if (payload.amount() <= 0 || payload.betType().isEmpty()) {
                game.broadcastToPlayers(level);
                return;
            }

            // Validate bet type
            if (!RouletteGame.isValidBetType(payload.betType())) {
                return;
            }

            // Validate that the game is accepting bets
            if (!game.isAcceptingBets()) {
                return;
            }

            EconomyManager eco = EconomyManager.get(level);
            game.placeBet(serverPlayer.getUUID(), payload.betType(), payload.amount(), eco, level);
        });
    }

    /**
     * Simple static registry for roulette game instances keyed by table BlockPos.
     * This allows multiple roulette tables to run independently.
     */
    public static class RouletteTableRegistry {
        private static final java.util.Map<BlockPos, RouletteGame> GAMES = new java.util.concurrent.ConcurrentHashMap<>();

        public static RouletteGame getOrCreate(BlockPos pos) {
            return GAMES.computeIfAbsent(pos, p -> new RouletteGame());
        }

        public static RouletteGame get(BlockPos pos) {
            return GAMES.get(pos);
        }

        public static java.util.Collection<java.util.Map.Entry<BlockPos, RouletteGame>> allGames() {
            return GAMES.entrySet();
        }

        public static void remove(BlockPos pos) {
            GAMES.remove(pos);
        }

        public static void clear() {
            GAMES.clear();
        }
    }
}
