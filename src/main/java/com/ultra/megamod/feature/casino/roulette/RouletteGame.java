package com.ultra.megamod.feature.casino.roulette;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.casino.network.RouletteGameSyncPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Server-side roulette game state machine.
 * Phases: BETTING (15s) -> SPINNING (3s) -> RESULT (3s) -> loop.
 */
public class RouletteGame {

    public enum Phase {
        BETTING, SPINNING, RESULT
    }

    private static final int BETTING_TICKS = 300;   // 15 seconds
    private static final int SPINNING_TICKS = 60;   // 3 seconds
    private static final int RESULT_TICKS = 60;     // 3 seconds

    /** Standard European roulette red numbers. */
    public static final Set<Integer> REDS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18,
            19, 21, 23, 25, 27, 30, 32, 34, 36
    );

    private Phase phase = Phase.BETTING;
    private int timer = BETTING_TICKS;
    private boolean resultMessageSent = false;

    /**
     * Each bet placed by a player.
     * betType is one of: "straight:N" (0-36), "red", "black", "odd", "even",
     * "low" (1-18), "high" (19-36), "dozen1" (1-12), "dozen2" (13-24), "dozen3" (25-36),
     * "column1" (1,4,7..34), "column2" (2,5,8..35), "column3" (3,6,9..36),
     * "split:A:B" (two adjacent numbers), "street:N" (row starting at N),
     * "corner:N" (top-left of 4-number block).
     */
    public record Bet(UUID playerId, String betType, int amount) {}

    private final List<Bet> bets = new ArrayList<>();
    private int resultNumber = -1;
    private final Random random = new Random();

    /**
     * Main tick loop, called every server tick.
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
                        phase = Phase.SPINNING;
                        timer = SPINNING_TICKS;
                        spin(level);
                    }
                }
            }
            case SPINNING -> {
                if (timer <= 0) {
                    phase = Phase.RESULT;
                    timer = RESULT_TICKS;
                    resultMessageSent = false;
                    // Pay out immediately but don't send chat messages yet
                    resolvePayouts(level, EconomyManager.get(level), CasinoManager.get(level));
                }
            }
            case RESULT -> {
                // Send chat messages halfway through RESULT phase (after animation settles)
                if (!resultMessageSent && timer <= RESULT_TICKS / 2) {
                    resultMessageSent = true;
                    sendResultMessages(level);
                }
                if (timer <= 0) {
                    // Reset for next round
                    bets.clear();
                    resultNumber = -1;
                    phase = Phase.BETTING;
                    timer = BETTING_TICKS;
                }
            }
        }

        // Broadcast state every 10 ticks (2x/sec) or on phase transitions
        if (timer % 10 == 0 || timer <= 0) {
            broadcastToPlayers(level);
        }
    }

    /**
     * Places a bet for a player.
     *
     * @return true if the bet was placed successfully
     */
    public boolean placeBet(UUID playerId, String betType, int amount, EconomyManager eco, ServerLevel level) {
        if (phase != Phase.BETTING) {
            return false;
        }
        if (amount <= 0) {
            return false;
        }
        if (!isValidBetType(betType)) {
            return false;
        }
        if (!com.ultra.megamod.feature.casino.chips.ChipManager.get(level).spendChips(playerId, amount)) {
            return false;
        }

        bets.add(new Bet(playerId, betType, amount));

        // Record the wager in casino stats
        CasinoManager.get(level).recordWager(playerId, amount, "roulette");

        return true;
    }

    /**
     * Picks the winning number. Checks for admin alwaysWin cheat.
     */
    private void spin(ServerLevel level) {
        CasinoManager casinoMgr = CasinoManager.get(level);

        // Check if any betting admin has alwaysWin enabled
        Integer cheatedNumber = null;
        for (Bet bet : bets) {
            if (casinoMgr.isAlwaysWinRoulette(bet.playerId())) {
                // Find a number that matches this admin's bet
                cheatedNumber = findWinningNumberForBet(bet.betType());
                if (cheatedNumber != null) {
                    break;
                }
            }
        }

        if (cheatedNumber != null) {
            resultNumber = cheatedNumber;
        } else {
            resultNumber = random.nextInt(37); // 0-36
        }
    }

    /**
     * Finds a number that would make the given bet type win.
     */
    private Integer findWinningNumberForBet(String betType) {
        if (betType.startsWith("straight:")) {
            try {
                return Integer.parseInt(betType.substring(9));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (betType.startsWith("split:")) {
            try {
                String[] parts = betType.split(":");
                return Integer.parseInt(parts[1]);
            } catch (Exception e) {
                return null;
            }
        }
        if (betType.startsWith("street:")) {
            try {
                return Integer.parseInt(betType.substring(7));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (betType.startsWith("corner:")) {
            try {
                return Integer.parseInt(betType.substring(7));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return switch (betType) {
            case "red" -> 1;   // 1 is red
            case "black" -> 2; // 2 is black
            case "odd" -> 1;
            case "even" -> 2;
            case "low" -> 1;
            case "high" -> 19;
            case "dozen1" -> 1;
            case "dozen2" -> 13;
            case "dozen3" -> 25;
            case "column1" -> 1;
            case "column2" -> 2;
            case "column3" -> 3;
            default -> null;
        };
    }

    /** Stored payout results for delayed chat messages. */
    private final Map<UUID, Integer> pendingWagered = new HashMap<>();
    private final Map<UUID, Integer> pendingWon = new HashMap<>();

    /**
     * Resolves payouts immediately (chips credited) and records stats. Chat messages delayed.
     */
    private void resolvePayouts(ServerLevel level, EconomyManager eco, CasinoManager casinoMgr) {
        if (resultNumber < 0) return;

        pendingWagered.clear();
        pendingWon.clear();

        for (Bet bet : bets) {
            pendingWagered.merge(bet.playerId(), bet.amount(), Integer::sum);
            int payout = calculatePayout(bet.betType(), bet.amount(), resultNumber);
            if (payout > 0) {
                com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(bet.playerId(), payout);
                pendingWon.merge(bet.playerId(), payout, Integer::sum);
            }
        }

        // Record stats immediately
        for (UUID playerId : pendingWagered.keySet()) {
            int totalWon = pendingWon.getOrDefault(playerId, 0);
            int totalWagered = pendingWagered.get(playerId);
            if (totalWon > 0) {
                casinoMgr.recordWin(playerId, totalWon, "roulette");
            } else {
                casinoMgr.recordLoss(playerId, totalWagered, "roulette");
            }
        }

        casinoMgr.saveToDisk(level);
        eco.saveToDisk(level);
    }

    /**
     * Sends chat messages after the animation has had time to play.
     */
    private void sendResultMessages(ServerLevel level) {
        if (resultNumber < 0) return;

        for (UUID playerId : pendingWagered.keySet()) {
            int totalWon = pendingWon.getOrDefault(playerId, 0);
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
            if (player == null) continue;

            if (totalWon > 0) {
                player.sendSystemMessage(Component.literal(
                        "Roulette landed on " + resultNumber + getColorLabel(resultNumber)
                                + "! You won " + totalWon + " MegaCoins!")
                        .withStyle(ChatFormatting.GOLD));
            } else {
                player.sendSystemMessage(Component.literal(
                        "Roulette landed on " + resultNumber + getColorLabel(resultNumber)
                                + ". Better luck next time!")
                        .withStyle(ChatFormatting.RED));
            }
        }

        // Notify spectators
        Set<UUID> bettors = pendingWagered.keySet();
        for (ServerPlayer p : level.players()) {
            if (!bettors.contains(p.getUUID())) {
                p.sendSystemMessage(Component.literal(
                        "Roulette landed on " + resultNumber + getColorLabel(resultNumber) + "!")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }

        pendingWagered.clear();
        pendingWon.clear();
    }

    /**
     * Calculates the payout for a bet given the winning number.
     * Returns total payout (bet + winnings) or 0 if the bet lost.
     */
    public static int calculatePayout(String betType, int betAmount, int winningNumber) {
        if (doesBetWin(betType, winningNumber)) {
            int multiplier = getPayoutMultiplier(betType);
            return betAmount + (betAmount * multiplier);
        }
        return 0;
    }

    /**
     * Checks if a bet type wins for the given number.
     */
    public static boolean doesBetWin(String betType, int number) {
        if (betType.startsWith("straight:")) {
            try {
                int target = Integer.parseInt(betType.substring(9));
                return target == number;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // Split bet: "split:A:B" — wins if number matches either A or B
        if (betType.startsWith("split:")) {
            try {
                String[] parts = betType.split(":");
                int a = Integer.parseInt(parts[1]);
                int b = Integer.parseInt(parts[2]);
                return number == a || number == b;
            } catch (Exception e) {
                return false;
            }
        }

        // Street bet: "street:N" — wins if number is N, N+1, or N+2
        if (betType.startsWith("street:")) {
            try {
                int start = Integer.parseInt(betType.substring(7));
                return number >= start && number <= start + 2;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // Corner bet: "corner:N" — wins if number is one of N, N+1, N+3, N+4
        // (top-left of a 2x2 block on a 3-column layout)
        if (betType.startsWith("corner:")) {
            try {
                int tl = Integer.parseInt(betType.substring(7));
                return number == tl || number == tl + 1 || number == tl + 3 || number == tl + 4;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // 0 loses all outside bets
        if (number == 0) {
            return false;
        }

        return switch (betType) {
            case "red" -> REDS.contains(number);
            case "black" -> !REDS.contains(number);
            case "odd" -> number % 2 != 0;
            case "even" -> number % 2 == 0;
            case "low" -> number >= 1 && number <= 18;
            case "high" -> number >= 19 && number <= 36;
            case "dozen1" -> number >= 1 && number <= 12;
            case "dozen2" -> number >= 13 && number <= 24;
            case "dozen3" -> number >= 25 && number <= 36;
            case "column1" -> number >= 1 && number <= 36 && (number - 1) % 3 == 0;
            case "column2" -> number >= 1 && number <= 36 && (number - 2) % 3 == 0;
            case "column3" -> number >= 1 && number <= 36 && (number - 3) % 3 == 0;
            default -> false;
        };
    }

    /**
     * Returns the payout multiplier for a bet type (not including the original bet).
     */
    public static int getPayoutMultiplier(String betType) {
        if (betType.startsWith("straight:")) {
            return 35;
        }
        if (betType.startsWith("split:")) {
            return 17;
        }
        if (betType.startsWith("street:")) {
            return 11;
        }
        if (betType.startsWith("corner:")) {
            return 8;
        }
        return switch (betType) {
            case "red", "black", "odd", "even", "low", "high" -> 1;
            case "dozen1", "dozen2", "dozen3" -> 2;
            case "column1", "column2", "column3" -> 2;
            default -> 0;
        };
    }

    /**
     * Validates that a bet type string is recognized.
     */
    public static boolean isValidBetType(String betType) {
        if (betType.startsWith("straight:")) {
            try {
                int n = Integer.parseInt(betType.substring(9));
                return n >= 0 && n <= 36;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (betType.startsWith("split:")) {
            try {
                String[] parts = betType.split(":");
                if (parts.length != 3) return false;
                int a = Integer.parseInt(parts[1]);
                int b = Integer.parseInt(parts[2]);
                return a >= 0 && a <= 36 && b >= 0 && b <= 36 && a != b;
            } catch (Exception e) {
                return false;
            }
        }
        if (betType.startsWith("street:")) {
            try {
                int start = Integer.parseInt(betType.substring(7));
                // Streets start at 1, 4, 7, ... 34
                return start >= 1 && start <= 34 && (start - 1) % 3 == 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (betType.startsWith("corner:")) {
            try {
                int tl = Integer.parseInt(betType.substring(7));
                // Top-left of a 2x2 block: must be in columns 1 or 2 (not 3) and row <= 11
                int col = ((tl - 1) % 3); // 0=col1, 1=col2, 2=col3
                return tl >= 1 && tl <= 32 && col <= 1;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return switch (betType) {
            case "red", "black", "odd", "even", "low", "high",
                 "dozen1", "dozen2", "dozen3",
                 "column1", "column2", "column3" -> true;
            default -> false;
        };
    }

    /**
     * Returns a color label string for display, e.g. " (Red)" or " (Black)".
     */
    private static String getColorLabel(int number) {
        if (number == 0) return " (Green)";
        return REDS.contains(number) ? " (Red)" : " (Black)";
    }

    /**
     * Returns the color name for a number: "green", "red", or "black".
     */
    public static String getColor(int number) {
        if (number == 0) return "green";
        return REDS.contains(number) ? "red" : "black";
    }

    /**
     * Broadcasts the current game state to all players in the level.
     */
    public void broadcastToPlayers(ServerLevel level) {
        String json = toJsonWithPlayers(level);
        RouletteGameSyncPayload payload = new RouletteGameSyncPayload(json);
        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    /**
     * Serializes full game state to JSON, including player names.
     */
    public String toJsonWithPlayers(ServerLevel level) {
        JsonObject json = new JsonObject();
        json.addProperty("phase", phase.name());
        json.addProperty("timer", timer);

        int maxTimer = switch (phase) {
            case BETTING -> BETTING_TICKS;
            case SPINNING -> SPINNING_TICKS;
            case RESULT -> RESULT_TICKS;
        };
        json.addProperty("maxTimer", maxTimer);

        if (resultNumber >= 0) {
            json.addProperty("resultNumber", resultNumber);
            json.addProperty("resultColor", getColor(resultNumber));
        }

        // Active bets with player names
        JsonArray betsArray = new JsonArray();
        Set<UUID> seenPlayers = new HashSet<>();
        for (Bet bet : bets) {
            JsonObject betObj = new JsonObject();

            String playerName = "Unknown";
            ServerPlayer sp = level.getServer().getPlayerList().getPlayer(bet.playerId());
            if (sp != null) playerName = sp.getGameProfile().name();

            betObj.addProperty("player", playerName);
            betObj.addProperty("betType", bet.betType());
            betObj.addProperty("amount", bet.amount());
            betsArray.add(betObj);
            seenPlayers.add(bet.playerId());
        }
        json.add("activeBets", betsArray);
        json.addProperty("totalPlayers", seenPlayers.size());

        // Total pot
        int totalPot = 0;
        for (Bet bet : bets) {
            totalPot += bet.amount();
        }
        json.addProperty("totalPot", totalPot);

        return json.toString();
    }

    /**
     * Returns true if the game is currently accepting bets.
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

    public int getResultNumber() {
        return resultNumber;
    }
}
