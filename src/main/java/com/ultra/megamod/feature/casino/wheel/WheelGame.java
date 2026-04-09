package com.ultra.megamod.feature.casino.wheel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.casino.network.WheelSyncPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class WheelGame {

    public enum Phase {
        BETTING, SPINNING, RESULT, COOLDOWN
    }

    private static final int BETTING_TICKS = 200;    // 10 seconds
    private static final int SPINNING_TICKS = 100;   // 5 seconds
    private static final int RESULT_TICKS = 100;     // 5 seconds
    private static final int COOLDOWN_TICKS = 60;    // 3 seconds

    private Phase phase = Phase.BETTING;
    private int timer = BETTING_TICKS;
    private boolean resultMessageSent = false;

    // player UUID -> (segment -> bet amount)
    private final Map<UUID, Map<WheelSegment, Integer>> bets = new HashMap<>();
    private WheelSegment result = null;
    private float spinAngle = 0f;

    private final Random random = new Random();

    /**
     * Main tick loop called every server tick from the block entity.
     */
    public void tick(ServerLevel level) {
        timer--;

        switch (phase) {
            case BETTING -> {
                if (timer <= 0) {
                    if (bets.isEmpty()) {
                        // No bets placed, restart betting phase
                        timer = BETTING_TICKS;
                    } else {
                        // Transition to spinning
                        phase = Phase.SPINNING;
                        timer = SPINNING_TICKS;
                        pickResult(level);
                    }
                }
            }
            case SPINNING -> {
                if (timer <= 0) {
                    phase = Phase.RESULT;
                    timer = RESULT_TICKS;
                    resultMessageSent = false;
                    resolveBets(level, EconomyManager.get(level), CasinoManager.get(level));
                }
            }
            case RESULT -> {
                // Delay chat messages until halfway through RESULT phase
                if (!resultMessageSent && timer <= RESULT_TICKS / 2) {
                    resultMessageSent = true;
                    sendResultMessages(level);
                }
                if (timer <= 0) {
                    phase = Phase.COOLDOWN;
                    timer = COOLDOWN_TICKS;
                }
            }
            case COOLDOWN -> {
                if (timer <= 0) {
                    // Reset for next round
                    bets.clear();
                    result = null;
                    spinAngle = 0f;
                    phase = Phase.BETTING;
                    timer = BETTING_TICKS;
                }
            }
        }

        // Broadcast state to all players in the level every 10 ticks (2x/sec) or on phase transitions
        if (timer % 10 == 0 || timer <= 0) {
            broadcastToPlayers(level);
        }
    }

    /**
     * Picks the winning segment. Checks for admin alwaysWin cheat.
     */
    private void pickResult(ServerLevel level) {
        CasinoManager casinoMgr = CasinoManager.get(level);

        // Check if any betting admin has alwaysWin enabled
        WheelSegment cheatedResult = null;
        for (Map.Entry<UUID, Map<WheelSegment, Integer>> entry : bets.entrySet()) {
            UUID playerId = entry.getKey();
            if (casinoMgr.isAlwaysWinWheel(playerId)) {
                // Find the segment this admin bet the most on
                Map<WheelSegment, Integer> playerBets = entry.getValue();
                WheelSegment bestSeg = null;
                int bestAmount = 0;
                for (Map.Entry<WheelSegment, Integer> segEntry : playerBets.entrySet()) {
                    if (segEntry.getValue() > bestAmount) {
                        bestAmount = segEntry.getValue();
                        bestSeg = segEntry.getKey();
                    }
                }
                if (bestSeg != null) {
                    cheatedResult = bestSeg;
                    break;
                }
            }
        }

        if (cheatedResult != null) {
            result = cheatedResult;
        } else {
            result = WheelSegment.pickRandom(random);
        }

        // The texture has 30 slices in this order (clockwise from top):
        // 1x=13, 2x=4, 3x=6, 5x=4, 10x=2, 20x(JACKPOT)=1
        int[] sliceMultipliers = {
            1, 2, 1, 3, 1, 1, 5, 1, 3, 2,
            1, 1, 10, 1, 3, 5, 1, 2, 1, 3,
            1, 5, 1, 3, 10, 1, 2, 5, 3, 20
        };
        int totalSlices = 30;
        float sliceAngle = 360f / totalSlices; // 18 degrees each

        // Find a slice that matches the result multiplier, pick randomly among matching slices
        java.util.List<Integer> matchingSlices = new java.util.ArrayList<>();
        for (int i = 0; i < totalSlices; i++) {
            if (sliceMultipliers[i] == result.multiplier) {
                matchingSlices.add(i);
            }
        }
        int targetSlice = matchingSlices.get(random.nextInt(matchingSlices.size()));

        // Calculate angle to land pointer on the target slice.
        // Texture: slice 0 at top (12 o'clock), slices go clockwise.
        // Slice N center is at (N * 12 + 6) degrees CW from top.
        // Renderer uses Axis.ZP positive = CCW rotation.
        // Rotating CCW by X degrees brings content at X degrees CW from top TO the top.
        // So to land slice N at the pointer: rotate by sliceCenterCW degrees.
        float sliceCenterCW = targetSlice * sliceAngle + sliceAngle / 2f;
        float extraRotations = (float) (Math.floor(3 + random.nextFloat() * 3) * 360); // 3-6 full rotations
        spinAngle = extraRotations + sliceCenterCW;
    }

    /**
     * Places a bet for a player on a specific segment.
     *
     * @return true if the bet was placed successfully
     */
    public boolean placeBet(UUID playerId, WheelSegment segment, int amount, EconomyManager eco, ServerLevel level) {
        if (phase != Phase.BETTING) {
            return false;
        }
        if (amount <= 0) {
            return false;
        }
        if (!com.ultra.megamod.feature.casino.chips.ChipManager.get(level).spendChips(playerId, amount)) {
            return false;
        }

        bets.computeIfAbsent(playerId, id -> new HashMap<>())
                .merge(segment, amount, Integer::sum);

        // Record the wager in casino stats
        CasinoManager.get(level).recordWager(playerId, amount, "wheel");

        return true;
    }

    /**
     * Resolves all bets after the spin. Pays winners and records stats.
     */
    private void resolveBets(ServerLevel level, EconomyManager eco, CasinoManager casinoMgr) {
        if (result == null) {
            return;
        }

        for (Map.Entry<UUID, Map<WheelSegment, Integer>> entry : bets.entrySet()) {
            UUID playerId = entry.getKey();
            Map<WheelSegment, Integer> playerBets = entry.getValue();

            int totalWageredThisRound = 0;
            for (int amt : playerBets.values()) {
                totalWageredThisRound += amt;
            }

            // Check if they bet on the winning segment
            Integer winningBet = playerBets.get(result);
            if (winningBet != null && winningBet > 0) {
                int payout = winningBet + (winningBet * result.multiplier);
                com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(playerId, payout);
                casinoMgr.recordWin(playerId, payout, "wheel");
            } else {
                casinoMgr.recordLoss(playerId, totalWageredThisRound, "wheel");
            }
        }

        // Save stats (payouts immediate, chat delayed)
        casinoMgr.saveToDisk(level);
        eco.saveToDisk(level);
    }

    /**
     * Sends chat messages after the wheel animation has settled.
     */
    private void sendResultMessages(ServerLevel level) {
        if (result == null) return;

        for (Map.Entry<UUID, Map<WheelSegment, Integer>> entry : bets.entrySet()) {
            UUID playerId = entry.getKey();
            Map<WheelSegment, Integer> playerBets = entry.getValue();
            Integer winningBet = playerBets.get(result);
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
            if (player == null) continue;

            if (winningBet != null && winningBet > 0) {
                int payout = winningBet + (winningBet * result.multiplier);
                player.sendSystemMessage(Component.literal("You won " + payout + " MegaCoins on the wheel!")
                        .withStyle(ChatFormatting.GOLD));
            } else {
                player.sendSystemMessage(Component.literal("The wheel landed on " + result.displayName + ". Better luck next time!")
                        .withStyle(ChatFormatting.RED));
            }
        }

        for (ServerPlayer p : level.players()) {
            if (!bets.containsKey(p.getUUID())) {
                p.sendSystemMessage(Component.literal("Wheel landed on " + result.displayName + " (" + result.multiplier + "x)!")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    /**
     * Broadcasts the current wheel state to all players in the level.
     */
    public void broadcastToPlayers(ServerLevel level) {
        String json = toJsonWithPlayers(level);
        WheelSyncPayload payload = new WheelSyncPayload(json);
        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    /**
     * Like toJson() but includes per-player bet details with names.
     */
    public String toJsonWithPlayers(ServerLevel level) {
        JsonObject json = com.google.gson.JsonParser.parseString(toJson()).getAsJsonObject();

        // Add individual player bets with names
        JsonArray playerBetsArray = new JsonArray();
        for (Map.Entry<UUID, Map<WheelSegment, Integer>> entry : bets.entrySet()) {
            UUID pid = entry.getKey();
            Map<WheelSegment, Integer> pBets = entry.getValue();

            String playerName = "Unknown";
            ServerPlayer sp = level.getServer().getPlayerList().getPlayer(pid);
            if (sp != null) playerName = sp.getGameProfile().name();

            for (Map.Entry<WheelSegment, Integer> betEntry : pBets.entrySet()) {
                if (betEntry.getValue() > 0) {
                    JsonObject betObj = new JsonObject();
                    betObj.addProperty("player", playerName);
                    betObj.addProperty("segment", betEntry.getKey().displayName);
                    betObj.addProperty("amount", betEntry.getValue());
                    betObj.addProperty("color", betEntry.getKey().color);
                    playerBetsArray.add(betObj);
                }
            }
        }
        json.add("activeBets", playerBetsArray);

        return json.toString();
    }

    /**
     * Serializes the full game state to JSON.
     */
    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("phase", phase.name());
        json.addProperty("timer", timer);
        json.addProperty("spinAngle", spinAngle);

        if (result != null) {
            json.addProperty("result", result.name());
            json.addProperty("resultDisplay", result.displayName);
            json.addProperty("resultMultiplier", result.multiplier);
        }

        // Segments info
        JsonArray segments = new JsonArray();
        for (WheelSegment seg : WheelSegment.values()) {
            JsonObject segObj = new JsonObject();
            segObj.addProperty("name", seg.name());
            segObj.addProperty("display", seg.displayName);
            segObj.addProperty("multiplier", seg.multiplier);
            segObj.addProperty("color", seg.color);
            segments.add(segObj);
        }
        json.add("segments", segments);

        // Bets summary per segment (total amounts)
        JsonObject betSummary = new JsonObject();
        for (WheelSegment seg : WheelSegment.values()) {
            int total = 0;
            for (Map<WheelSegment, Integer> playerBets : bets.values()) {
                total += playerBets.getOrDefault(seg, 0);
            }
            betSummary.addProperty(seg.name(), total);
        }
        json.add("bets", betSummary);

        json.addProperty("totalPlayers", bets.size());

        // Max timer for each phase so client can show progress
        int maxTimer = switch (phase) {
            case BETTING -> BETTING_TICKS;
            case SPINNING -> SPINNING_TICKS;
            case RESULT -> RESULT_TICKS;
            case COOLDOWN -> COOLDOWN_TICKS;
        };
        json.addProperty("maxTimer", maxTimer);

        return json.toString();
    }

    /**
     * Returns true if the wheel is currently accepting bets.
     */
    public boolean isAcceptingBets() {
        return phase == Phase.BETTING;
    }

    public Phase getPhase() {
        return phase;
    }

    public int getTimer() {
        return timer;
    }

    public WheelSegment getResult() {
        return result;
    }

    public float getSpinAngle() {
        return spinAngle;
    }

    /**
     * Gets the total amount a specific player has bet on a specific segment this round.
     */
    public int getPlayerBetOnSegment(UUID playerId, WheelSegment segment) {
        Map<WheelSegment, Integer> playerBets = bets.get(playerId);
        if (playerBets == null) {
            return 0;
        }
        return playerBets.getOrDefault(segment, 0);
    }

    /**
     * Gets the total amount a specific player has bet across all segments this round.
     */
    public int getPlayerTotalBet(UUID playerId) {
        Map<WheelSegment, Integer> playerBets = bets.get(playerId);
        if (playerBets == null) {
            return 0;
        }
        int total = 0;
        for (int amt : playerBets.values()) {
            total += amt;
        }
        return total;
    }
}
