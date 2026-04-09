package com.ultra.megamod.feature.casino.craps;

import com.google.gson.JsonObject;
import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.casino.network.CrapsGameSyncPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Server-side craps game logic. One instance per table (single player per table).
 *
 * Phases:
 *   BETTING       - Player places bets (Pass Line, Don't Pass, Field)
 *   COME_OUT_ROLL - Player rolls; 7/11 = pass win, 2/3/12 = craps, else sets Point
 *   POINT_PHASE   - Player rolls; additional bets available (Come, Place, Hardways)
 *   RESULT        - Short display phase before resetting to BETTING
 *
 * Supported bet types:
 *   "pass"       - Pass Line (1:1)
 *   "dontpass"   - Don't Pass (1:1, 12 = push)
 *   "field"      - Field bet, one-roll (1:1, 2 pays 2:1, 12 pays 3:1)
 *   "come"       - Come bet (like pass but placed after point is set)
 *   "place_4/5/6/8/9/10" - Place bets (4/10=9:5, 5/9=7:5, 6/8=7:6)
 *   "hard_4/6/8/10" - Hardway bets (4/10=7:1, 6/8=9:1)
 */
public class CrapsGame {

    public enum Phase {
        BETTING, COME_OUT_ROLL, POINT_PHASE, RESULT
    }

    private Phase phase = Phase.BETTING;
    private UUID playerId = null;
    private int point = 0;
    private int die1 = 0;
    private int die2 = 0;
    private String resultMessage = "";
    private int resultTimer = 0;
    private int pendingWinnings = 0;
    private int pendingLosses = 0;
    private boolean resultChatSent = false;

    /** All active bets keyed by bet type. */
    private final Map<String, Integer> bets = new LinkedHashMap<>();

    /** Come bet's personal point (0 = not yet established). */
    private int comePoint = 0;

    private static final int RESULT_DISPLAY_TICKS = 60; // 3 seconds

    /** All valid bet type keys. */
    public static final Set<String> VALID_BET_TYPES = Set.of(
            "pass", "dontpass", "field", "come",
            "place_4", "place_5", "place_6", "place_8", "place_9", "place_10",
            "hard_4", "hard_6", "hard_8", "hard_10",
            // Proposition bets (one-roll)
            "any_seven", "any_craps", "yo_eleven", "aces", "boxcars", "horn", "hi_lo"
    );

    /** Bet types only allowed during BETTING phase (come-out bets). */
    /** Proposition bets (one-roll) allowed in any phase. */
    private static final Set<String> PROP_BETS = Set.of(
            "any_seven", "any_craps", "yo_eleven", "aces", "boxcars", "horn", "hi_lo", "field");

    private static final Set<String> COME_OUT_BETS = Set.of("pass", "dontpass",
            "field", "any_seven", "any_craps", "yo_eleven", "aces", "boxcars", "horn", "hi_lo");

    /** Bet types allowed during POINT_PHASE. */
    private static final Set<String> POINT_PHASE_BETS = Set.of(
            "come", "place_4", "place_5", "place_6", "place_8", "place_9", "place_10",
            "hard_4", "hard_6", "hard_8", "hard_10",
            "field", "any_seven", "any_craps", "yo_eleven", "aces", "boxcars", "horn", "hi_lo"
    );

    private static final Random random = new Random();

    // ---- Getters ----

    public Phase getPhase() { return phase; }
    public UUID getPlayerId() { return playerId; }
    public int getPoint() { return point; }
    public int getDie1() { return die1; }
    public int getDie2() { return die2; }
    public String getResultMessage() { return resultMessage; }
    public Map<String, Integer> getBets() { return Collections.unmodifiableMap(bets); }
    public int getComePoint() { return comePoint; }

    /** Total amount wagered across all active bets. */
    public int getTotalBetAmount() {
        return bets.values().stream().mapToInt(Integer::intValue).sum();
    }

    // ---- Tick (called from block entity or handler) ----

    public void tick(ServerLevel level) {
        if (phase == Phase.RESULT) {
            resultTimer--;
            // Send chat messages after dice animation settles (~30 ticks in)
            if (!resultChatSent && resultTimer <= RESULT_DISPLAY_TICKS - 30) {
                resultChatSent = true;
                sendPendingChat(level);
            }
            if (resultTimer <= 0) {
                // Reset for next round
                phase = Phase.BETTING;
                bets.clear();
                comePoint = 0;
                point = 0;
                die1 = 0;
                die2 = 0;
                resultMessage = "";
                syncToPlayer(level);
            }
        }
    }

    private void sendPendingChat(ServerLevel level) {
        if (playerId == null) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
        if (player == null) return;
        if (pendingWinnings > 0) {
            player.sendSystemMessage(Component.literal("Craps: You won " + pendingWinnings + " MegaCoins!")
                    .withStyle(ChatFormatting.GOLD));
        } else if (pendingLosses > 0) {
            player.sendSystemMessage(Component.literal("Craps: You lost " + pendingLosses + " MegaCoins.")
                    .withStyle(ChatFormatting.RED));
        }
        pendingWinnings = 0;
        pendingLosses = 0;
    }

    // ---- Place Bet ----

    /**
     * Places a bet of the given type. Validates phase restrictions.
     * @param betType One of the VALID_BET_TYPES keys
     * @return true if the bet was placed successfully
     */
    public boolean placeBet(UUID player, String betType, int amount, EconomyManager eco, ServerLevel level) {
        if (!VALID_BET_TYPES.contains(betType)) return false;
        if (amount <= 0) return false;
        if (phase == Phase.RESULT) return false;

        // If a different player is already at the table, reject
        if (playerId != null && !playerId.equals(player)) return false;

        // Validate bet type is allowed in current phase
        if (phase == Phase.BETTING) {
            if (!COME_OUT_BETS.contains(betType)) return false;
        } else if (phase == Phase.POINT_PHASE) {
            if (!POINT_PHASE_BETS.contains(betType)) return false;
        } else if (phase == Phase.COME_OUT_ROLL) {
            // Only one-roll (proposition) bets during come-out roll phase
            if (!PROP_BETS.contains(betType)) return false;
        }

        // Deduct chips
        if (!com.ultra.megamod.feature.casino.chips.ChipManager.get(level).spendChips(player, amount)) return false;

        this.playerId = player;

        // Add to existing bet of same type (stack bets)
        bets.merge(betType, amount, Integer::sum);

        CasinoManager.get(level).recordWager(player, amount, "craps");

        // Transition to COME_OUT_ROLL once any bet is placed during BETTING phase
        // This allows standalone prop bets (horn, any_craps, etc.) without requiring pass/dontpass
        if (phase == Phase.BETTING && !bets.isEmpty()) {
            phase = Phase.COME_OUT_ROLL;
        }

        syncToPlayer(level);
        return true;
    }

    // ---- Roll ----

    /**
     * Rolls the dice. Valid during COME_OUT_ROLL and POINT_PHASE.
     */
    public void roll(ServerLevel level, CasinoManager casino) {
        if (playerId == null) return;
        if (phase != Phase.COME_OUT_ROLL && phase != Phase.POINT_PHASE) return;

        boolean adminWin = casino.isAlwaysWinCraps(playerId);

        if (adminWin) {
            rollAdminRigged();
        } else {
            die1 = 1 + random.nextInt(6);
            die2 = 1 + random.nextInt(6);
        }

        int total = die1 + die2;
        boolean isHardway = (die1 == die2);

        if (phase == Phase.COME_OUT_ROLL) {
            resolveComeOut(total, isHardway);
        } else {
            resolvePointPhase(total, isHardway);
        }

        syncToPlayer(level);
    }

    /**
     * Admin rigged roll: pick dice that satisfy the most bets possible.
     */
    private void rollAdminRigged() {
        int bestScore = Integer.MIN_VALUE;
        int bestD1 = 3, bestD2 = 4; // default 7

        for (int d1 = 1; d1 <= 6; d1++) {
            for (int d2 = 1; d2 <= 6; d2++) {
                int score = scoreRollForAdmin(d1, d2);
                if (score > bestScore) {
                    bestScore = score;
                    bestD1 = d1;
                    bestD2 = d2;
                }
            }
        }
        die1 = bestD1;
        die2 = bestD2;
    }

    /**
     * Score a hypothetical roll for the admin (higher = more bets won).
     */
    private int scoreRollForAdmin(int d1, int d2) {
        int total = d1 + d2;
        boolean hard = (d1 == d2);
        int score = 0;

        if (phase == Phase.COME_OUT_ROLL) {
            // Pass wins on 7/11
            if (bets.containsKey("pass")) {
                if (total == 7 || total == 11) score += bets.get("pass") * 2;
                else if (total == 2 || total == 3 || total == 12) score -= bets.get("pass");
            }
            // Don't Pass wins on 2/3, pushes 12, loses 7/11
            if (bets.containsKey("dontpass")) {
                if (total == 2 || total == 3) score += bets.get("dontpass") * 2;
                else if (total == 12) score += 0; // push is neutral
                else if (total == 7 || total == 11) score -= bets.get("dontpass");
            }
        } else {
            // Point phase
            if (bets.containsKey("pass")) {
                if (total == point) score += bets.get("pass") * 2;
                else if (total == 7) score -= bets.get("pass");
            }
            if (bets.containsKey("dontpass")) {
                if (total == 7) score += bets.get("dontpass") * 2;
                else if (total == point) score -= bets.get("dontpass");
            }
            // Come bet
            if (bets.containsKey("come")) {
                if (comePoint == 0) {
                    if (total == 7 || total == 11) score += bets.get("come") * 2;
                    else if (total == 2 || total == 3 || total == 12) score -= bets.get("come");
                } else {
                    if (total == comePoint) score += bets.get("come") * 2;
                    else if (total == 7) score -= bets.get("come");
                }
            }
            // Place bets
            for (int num : new int[]{4, 5, 6, 8, 9, 10}) {
                String key = "place_" + num;
                if (bets.containsKey(key)) {
                    if (total == num) score += bets.get(key) * 2; // approximate
                    else if (total == 7) score -= bets.get(key);
                }
            }
            // Hardways
            for (int num : new int[]{4, 6, 8, 10}) {
                String key = "hard_" + num;
                if (bets.containsKey(key)) {
                    if (total == num && hard) score += bets.get(key) * 8; // approximate
                    else if (total == 7 || (total == num && !hard)) score -= bets.get(key);
                }
            }
        }

        // Field
        if (bets.containsKey("field")) {
            if (total == 2) score += bets.get("field") * 3;
            else if (total == 12) score += bets.get("field") * 4;
            else if (total == 3 || total == 4 || total == 9 || total == 10 || total == 11) score += bets.get("field") * 2;
            else score -= bets.get("field");
        }

        // Proposition bets (one-roll, always resolve)
        if (bets.containsKey("any_seven")) {
            if (total == 7) score += bets.get("any_seven") * 5;
            else score -= bets.get("any_seven");
        }
        if (bets.containsKey("any_craps")) {
            if (total == 2 || total == 3 || total == 12) score += bets.get("any_craps") * 8;
            else score -= bets.get("any_craps");
        }
        if (bets.containsKey("yo_eleven")) {
            if (total == 11) score += bets.get("yo_eleven") * 16;
            else score -= bets.get("yo_eleven");
        }
        if (bets.containsKey("aces")) {
            if (total == 2) score += bets.get("aces") * 31;
            else score -= bets.get("aces");
        }
        if (bets.containsKey("boxcars")) {
            if (total == 12) score += bets.get("boxcars") * 31;
            else score -= bets.get("boxcars");
        }
        if (bets.containsKey("horn")) {
            if (total == 2 || total == 12) score += bets.get("horn") * 8;
            else if (total == 3 || total == 11) score += bets.get("horn") * 4;
            else score -= bets.get("horn");
        }
        if (bets.containsKey("hi_lo")) {
            if (total == 2 || total == 12) score += bets.get("hi_lo") * 16;
            else score -= bets.get("hi_lo");
        }

        return score;
    }

    // ---- Resolution per phase ----

    private void resolveComeOut(int total, boolean isHardway) {
        StringBuilder msg = new StringBuilder();
        boolean roundEnds = false;

        // One-roll bets always resolve
        resolveFieldBet(total, msg);
        resolvePropositionBets(total, msg);

        if (total == 7 || total == 11) {
            // Pass wins, Don't Pass loses
            if (bets.containsKey("pass")) msg.append("Pass Line wins! ");
            if (bets.containsKey("dontpass")) msg.append("Don't Pass loses. ");
            roundEnds = true;
        } else if (total == 2 || total == 3) {
            // Pass loses, Don't Pass wins
            if (bets.containsKey("pass")) msg.append("Craps! Pass Line loses. ");
            if (bets.containsKey("dontpass")) msg.append("Don't Pass wins! ");
            roundEnds = true;
        } else if (total == 12) {
            // Pass loses, Don't Pass pushes
            if (bets.containsKey("pass")) msg.append("Craps 12! Pass Line loses. ");
            if (bets.containsKey("dontpass")) msg.append("Don't Pass pushes. ");
            roundEnds = true;
        } else {
            // Establish the point
            point = total;
            phase = Phase.POINT_PHASE;
            msg.append("Point set: ").append(total).append(". ");
        }

        resultMessage = msg.toString().trim();

        if (roundEnds) {
            enterResult();
        }
    }

    private void resolvePointPhase(int total, boolean isHardway) {
        StringBuilder msg = new StringBuilder();
        boolean roundEnds = false;

        // One-roll bets always resolve
        resolveFieldBet(total, msg);
        resolvePropositionBets(total, msg);

        // Come bet resolution
        resolveComeBet(total, msg);

        // Hardways resolution (before main resolution since 7 kills them)
        resolveHardwayBets(total, isHardway, msg);

        // Place bet resolution
        resolvePlaceBets(total, msg);

        if (total == point) {
            // Pass wins, Don't Pass loses
            if (bets.containsKey("pass")) msg.append("Point ").append(point).append(" hit! Pass Line wins! ");
            if (bets.containsKey("dontpass")) msg.append("Don't Pass loses. ");
            roundEnds = true;
        } else if (total == 7) {
            // Seven-out: Pass loses, Don't Pass wins
            if (bets.containsKey("pass")) msg.append("Seven out! Pass Line loses. ");
            if (bets.containsKey("dontpass")) msg.append("Don't Pass wins! ");
            // Place bets and come bets lost on 7 are handled in their resolve methods
            roundEnds = true;
        } else {
            msg.append("Rolled ").append(total).append(". Point is ").append(point).append(". Roll again!");
        }

        resultMessage = msg.toString().trim();

        if (roundEnds) {
            enterResult();
        }
    }

    private void resolveFieldBet(int total, StringBuilder msg) {
        if (!bets.containsKey("field")) return;
        int fieldBet = bets.get("field");

        if (total == 2) {
            // 2:1 payout
            msg.append("Field 2 pays double! ");
        } else if (total == 12) {
            // 3:1 payout
            msg.append("Field 12 pays triple! ");
        } else if (total == 3 || total == 4 || total == 9 || total == 10 || total == 11) {
            // 1:1 payout
            msg.append("Field wins! ");
        } else {
            // Loses on 5,6,7,8
            msg.append("Field loses. ");
        }
        // Field is one-roll, remove after resolution (payout handled in resolve())
    }

    private void resolveComeBet(int total, StringBuilder msg) {
        if (!bets.containsKey("come")) return;

        if (comePoint == 0) {
            // Come bet's "come-out" roll
            if (total == 7 || total == 11) {
                msg.append("Come bet wins! ");
            } else if (total == 2 || total == 3 || total == 12) {
                msg.append("Come bet loses. ");
            } else {
                comePoint = total;
                msg.append("Come point set: ").append(total).append(". ");
            }
        } else {
            // Come point established
            if (total == comePoint) {
                msg.append("Come point ").append(comePoint).append(" hit! Come wins! ");
            } else if (total == 7) {
                msg.append("Come bet loses on 7. ");
            }
            // Other numbers: come bet stays
        }
    }

    private void resolvePlaceBets(int total, StringBuilder msg) {
        for (int num : new int[]{4, 5, 6, 8, 9, 10}) {
            String key = "place_" + num;
            if (!bets.containsKey(key)) continue;

            if (total == num) {
                msg.append("Place ").append(num).append(" wins! ");
            } else if (total == 7) {
                msg.append("Place ").append(num).append(" loses. ");
            }
        }
    }

    private void resolveHardwayBets(int total, boolean isHardway, StringBuilder msg) {
        for (int num : new int[]{4, 6, 8, 10}) {
            String key = "hard_" + num;
            if (!bets.containsKey(key)) continue;

            if (total == num && isHardway) {
                msg.append("Hard ").append(num).append(" hits! ");
            } else if (total == 7 || (total == num && !isHardway)) {
                msg.append("Hard ").append(num).append(" loses. ");
            }
        }
    }

    /**
     * Resolve all proposition (one-roll) bets. These always resolve on every roll.
     */
    private void resolvePropositionBets(int total, StringBuilder msg) {
        // Any Seven: wins on 7 (4:1)
        if (bets.containsKey("any_seven")) {
            if (total == 7) msg.append("Any Seven wins! ");
            else msg.append("Any Seven loses. ");
        }
        // Any Craps: wins on 2, 3, 12 (7:1)
        if (bets.containsKey("any_craps")) {
            if (total == 2 || total == 3 || total == 12) msg.append("Any Craps wins! ");
            else msg.append("Any Craps loses. ");
        }
        // Yo Eleven: wins on 11 (15:1)
        if (bets.containsKey("yo_eleven")) {
            if (total == 11) msg.append("Yo Eleven wins! ");
            else msg.append("Yo loses. ");
        }
        // Aces/Snake Eyes: wins on 2 (30:1)
        if (bets.containsKey("aces")) {
            if (total == 2) msg.append("Aces wins! ");
            else msg.append("Aces loses. ");
        }
        // Boxcars/Midnight: wins on 12 (30:1)
        if (bets.containsKey("boxcars")) {
            if (total == 12) msg.append("Boxcars wins! ");
            else msg.append("Boxcars loses. ");
        }
        // Horn: covers 2/3/11/12 — pays based on which hits
        if (bets.containsKey("horn")) {
            if (total == 2 || total == 12) msg.append("Horn hits (").append(total).append(")! ");
            else if (total == 3 || total == 11) msg.append("Horn hits (").append(total).append(")! ");
            else msg.append("Horn loses. ");
        }
        // Hi-Lo: wins on 2 or 12 (15:1)
        if (bets.containsKey("hi_lo")) {
            if (total == 2 || total == 12) msg.append("Hi-Lo wins! ");
            else msg.append("Hi-Lo loses. ");
        }
    }

    // ---- Resolve (payout) ----

    private void enterResult() {
        phase = Phase.RESULT;
        resultTimer = RESULT_DISPLAY_TICKS;
        resultChatSent = false;
    }

    /**
     * Called after roll to pay out. Should be called when phase just transitioned to RESULT,
     * or after a one-roll bet resolves during POINT_PHASE.
     */
    public void resolve(EconomyManager eco, CasinoManager casino, ServerLevel level) {
        if (playerId == null) return;

        int total = die1 + die2;
        boolean isHardway = (die1 == die2);

        int totalWinnings = 0;
        int totalLosses = 0;
        Set<String> betsToRemove = new HashSet<>();

        // --- Pass Line ---
        if (bets.containsKey("pass")) {
            int bet = bets.get("pass");
            if (phase == Phase.RESULT) {
                if (resultMessage.contains("Pass Line wins")) {
                    totalWinnings += bet * 2; // bet + 1:1 payout
                    betsToRemove.add("pass");
                } else if (resultMessage.contains("Pass Line loses")) {
                    totalLosses += bet;
                    betsToRemove.add("pass");
                }
            }
        }

        // --- Don't Pass ---
        if (bets.containsKey("dontpass")) {
            int bet = bets.get("dontpass");
            if (phase == Phase.RESULT) {
                if (resultMessage.contains("Don't Pass wins")) {
                    totalWinnings += bet * 2; // bet + 1:1 payout
                    betsToRemove.add("dontpass");
                } else if (resultMessage.contains("Don't Pass loses")) {
                    totalLosses += bet;
                    betsToRemove.add("dontpass");
                } else if (resultMessage.contains("Don't Pass pushes")) {
                    // Return the bet
                    totalWinnings += bet;
                    betsToRemove.add("dontpass");
                }
            }
        }

        // --- Field (one-roll, always resolves) ---
        if (bets.containsKey("field")) {
            int bet = bets.get("field");
            if (total == 2) {
                totalWinnings += bet + bet * 2; // bet + 2:1 payout
            } else if (total == 12) {
                totalWinnings += bet + bet * 3; // bet + 3:1 payout
            } else if (total == 3 || total == 4 || total == 9 || total == 10 || total == 11) {
                totalWinnings += bet * 2; // bet + 1:1 payout
            } else {
                totalLosses += bet;
            }
            betsToRemove.add("field");
        }

        // --- Come ---
        if (bets.containsKey("come")) {
            int bet = bets.get("come");
            if (resultMessage.contains("Come bet wins")) {
                totalWinnings += bet * 2;
                betsToRemove.add("come");
                comePoint = 0;
            } else if (resultMessage.contains("Come bet loses")) {
                totalLosses += bet;
                betsToRemove.add("come");
                comePoint = 0;
            } else if (resultMessage.contains("Come point") && resultMessage.contains("Come wins")) {
                totalWinnings += bet * 2;
                betsToRemove.add("come");
                comePoint = 0;
            }
            // If seven-out kills come bet during round end
            if (phase == Phase.RESULT && total == 7 && comePoint > 0) {
                totalLosses += bet;
                betsToRemove.add("come");
                comePoint = 0;
            }
        }

        // --- Place Bets ---
        for (int num : new int[]{4, 5, 6, 8, 9, 10}) {
            String key = "place_" + num;
            if (!bets.containsKey(key)) continue;
            int bet = bets.get(key);

            if (total == num) {
                // Win! Calculate payout based on number
                int payout = calculatePlacePayout(bet, num);
                totalWinnings += bet + payout; // return bet + winnings
                betsToRemove.add(key);
            } else if (total == 7) {
                // Lose on seven-out
                totalLosses += bet;
                betsToRemove.add(key);
            }
            // Other numbers: bet stays active (no removal)
        }

        // --- Hardways ---
        for (int num : new int[]{4, 6, 8, 10}) {
            String key = "hard_" + num;
            if (!bets.containsKey(key)) continue;
            int bet = bets.get(key);

            if (total == num && isHardway) {
                // Hardway hit!
                int multiplier = (num == 4 || num == 10) ? 7 : 9; // 7:1 or 9:1
                totalWinnings += bet + bet * multiplier;
                betsToRemove.add(key);
            } else if (total == 7 || (total == num && !isHardway)) {
                // Lost: seven or easy way
                totalLosses += bet;
                betsToRemove.add(key);
            }
            // Other numbers: bet stays active
        }

        // --- Proposition bets (all one-roll, always resolve) ---
        // Any Seven (4:1)
        if (bets.containsKey("any_seven")) {
            int bet = bets.get("any_seven");
            if (total == 7) totalWinnings += bet + bet * 4;
            else totalLosses += bet;
            betsToRemove.add("any_seven");
        }
        // Any Craps (7:1)
        if (bets.containsKey("any_craps")) {
            int bet = bets.get("any_craps");
            if (total == 2 || total == 3 || total == 12) totalWinnings += bet + bet * 7;
            else totalLosses += bet;
            betsToRemove.add("any_craps");
        }
        // Yo Eleven (15:1)
        if (bets.containsKey("yo_eleven")) {
            int bet = bets.get("yo_eleven");
            if (total == 11) totalWinnings += bet + bet * 15;
            else totalLosses += bet;
            betsToRemove.add("yo_eleven");
        }
        // Aces / Snake Eyes (30:1)
        if (bets.containsKey("aces")) {
            int bet = bets.get("aces");
            if (total == 2) totalWinnings += bet + bet * 30;
            else totalLosses += bet;
            betsToRemove.add("aces");
        }
        // Boxcars / Midnight (30:1)
        if (bets.containsKey("boxcars")) {
            int bet = bets.get("boxcars");
            if (total == 12) totalWinnings += bet + bet * 30;
            else totalLosses += bet;
            betsToRemove.add("boxcars");
        }
        // Horn (2/12 pays 27:4, 3/11 pays 3:1 on quarter of bet)
        if (bets.containsKey("horn")) {
            int bet = bets.get("horn");
            if (total == 2 || total == 12) totalWinnings += bet + bet * 7; // simplified: 7:1 on 2 or 12
            else if (total == 3 || total == 11) totalWinnings += bet + bet * 3; // 3:1 on 3 or 11
            else totalLosses += bet;
            betsToRemove.add("horn");
        }
        // Hi-Lo (15:1)
        if (bets.containsKey("hi_lo")) {
            int bet = bets.get("hi_lo");
            if (total == 2 || total == 12) totalWinnings += bet + bet * 15;
            else totalLosses += bet;
            betsToRemove.add("hi_lo");
        }

        // Apply payouts (chips credited immediately, chat delayed)
        if (totalWinnings > 0) {
            com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(playerId, totalWinnings);
            casino.recordWin(playerId, totalWinnings, "craps");
        }

        if (totalLosses > 0) {
            casino.recordLoss(playerId, totalLosses, "craps");
        }

        // Store pending for delayed chat
        this.pendingWinnings += totalWinnings;
        this.pendingLosses += totalLosses;

        // Remove resolved bets
        for (String key : betsToRemove) {
            bets.remove(key);
        }

        eco.saveToDisk(level);
        casino.saveToDisk(level);
    }

    /** Calculate place bet payout: 4/10=9:5, 5/9=7:5, 6/8=7:6. */
    private int calculatePlacePayout(int bet, int number) {
        return switch (number) {
            case 4, 10 -> (bet * 9) / 5;  // 9:5
            case 5, 9 -> (bet * 7) / 5;   // 7:5
            case 6, 8 -> (bet * 7) / 6;   // 7:6
            default -> bet;
        };
    }

    /** Reset state for next round after payout. Keeps the player at the table. */
    public void resetForNextRound() {
        phase = Phase.BETTING;
        bets.clear();
        comePoint = 0;
        point = 0;
        // Keep die1/die2 and resultMessage so client can display last roll result
    }

    // ---- Leave ----

    public void leaveTable(UUID player) {
        if (playerId != null && playerId.equals(player)) {
            // If mid-game, they forfeit all bets
            playerId = null;
            phase = Phase.BETTING;
            bets.clear();
            comePoint = 0;
            point = 0;
            die1 = 0;
            die2 = 0;
            resultMessage = "";
        }
    }

    // ---- Sync to client ----

    public void syncToPlayer(ServerLevel level) {
        if (playerId == null) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
        if (player == null) return;

        PacketDistributor.sendToPlayer(player, new CrapsGameSyncPayload(toJson()));
    }

    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("phase", phase.name());
        json.addProperty("die1", die1);
        json.addProperty("die2", die2);
        json.addProperty("point", point);
        json.addProperty("comePoint", comePoint);
        json.addProperty("resultMessage", resultMessage);

        // All bets
        JsonObject betsJson = new JsonObject();
        for (Map.Entry<String, Integer> entry : bets.entrySet()) {
            betsJson.addProperty(entry.getKey(), entry.getValue());
        }
        json.add("bets", betsJson);

        // Total for backward compat / quick display
        json.addProperty("betAmount", getTotalBetAmount());

        return json.toString();
    }
}
