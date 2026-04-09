package com.ultra.megamod.feature.casino.baccarat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.casino.network.BaccaratGameSyncPayload;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Baccarat (punto banco) game logic.
 * <p>
 * Card values are simplified: each card is a random int 0-9.
 * Hand value = sum of card values mod 10 (only the ones digit).
 * <p>
 * Third-card drawing rules follow standard punto banco tableau.
 * Admin auto-win rigs the cards so the admin's chosen side always wins.
 */
public class BaccaratGame {

    public enum Phase {
        BETTING, DEALING, RESULT
    }

    private Phase phase = Phase.BETTING;

    // Hands: each entry is a card value 0-9
    private final List<Integer> playerHand = new ArrayList<>();
    private final List<Integer> bankerHand = new ArrayList<>();

    // Current bet info (single-player per game instance)
    private UUID playerId;
    private String betSide = "";   // "player", "banker", or "tie"
    private int betAmount = 0;

    // Result info
    private String result = "";    // "player", "banker", "tie"
    private int payout = 0;
    private String resultMessage = "";

    private final Random random = new Random();
    private String pendingChatMessage = "";
    private ChatFormatting pendingChatColor = ChatFormatting.WHITE;
    private int chatDelayTicks = 0;

    public Phase getPhase() {
        return phase;
    }

    /**
     * Tick for delayed chat messages. Call from server tick.
     */
    public void tick(ServerLevel level) {
        if (chatDelayTicks > 0) {
            chatDelayTicks--;
            if (chatDelayTicks <= 0 && !pendingChatMessage.isEmpty()) {
                ServerPlayer sp = getServerPlayer();
                if (sp != null) {
                    sp.sendSystemMessage(Component.literal(pendingChatMessage).withStyle(pendingChatColor));
                }
                pendingChatMessage = "";
            }
        }
    }

    // ---- Betting ----

    /**
     * Places a bet for the player.
     *
     * @return true if the bet was placed successfully
     */
    public boolean placeBet(UUID playerId, String side, int amount, EconomyManager eco, ServerLevel level) {
        if (phase != Phase.BETTING) {
            return false;
        }
        if (amount <= 0) {
            return false;
        }
        String normalizedSide = side.toLowerCase();
        if (!normalizedSide.equals("player") && !normalizedSide.equals("banker") && !normalizedSide.equals("tie")) {
            return false;
        }
        if (!com.ultra.megamod.feature.casino.chips.ChipManager.get(level).spendChips(playerId, amount)) {
            return false;
        }

        this.playerId = playerId;
        this.betSide = normalizedSide;
        this.betAmount = amount;

        // Record the wager in casino stats
        CasinoManager.get(level).recordWager(playerId, amount, "baccarat");

        return true;
    }

    // ---- Dealing ----

    /**
     * Deals the initial cards and applies third-card rules.
     * If the player is an admin with auto-win, the cards are rigged.
     */
    public void deal(CasinoManager casino) {
        if (phase != Phase.BETTING || playerId == null || betAmount <= 0) {
            return;
        }
        phase = Phase.DEALING;

        playerHand.clear();
        bankerHand.clear();

        // Check admin auto-win
        boolean adminAutoWin = false;
        ServerPlayer sp = getServerPlayer();
        if (sp != null && AdminSystem.isAdmin(sp) && casino.isAlwaysWinBaccarat(playerId)) {
            adminAutoWin = true;
        }

        if (adminAutoWin) {
            dealRigged();
        } else {
            dealNormal();
        }
    }

    /**
     * Normal deal: random cards with standard third-card rules.
     */
    private void dealNormal() {
        // Initial two cards each
        playerHand.add(randomCard());
        bankerHand.add(randomCard());
        playerHand.add(randomCard());
        bankerHand.add(randomCard());

        int playerTotal = handValue(playerHand);
        int bankerTotal = handValue(bankerHand);

        // Natural check (8 or 9): no more cards
        if (playerTotal >= 8 || bankerTotal >= 8) {
            return;
        }

        // Player third card rule: draw on 0-5, stand on 6-7
        int playerThirdCard = -1;
        if (playerTotal <= 5) {
            playerThirdCard = randomCard();
            playerHand.add(playerThirdCard);
        }

        // Banker third card rules (standard tableau)
        bankerTotal = handValue(bankerHand);
        if (playerThirdCard == -1) {
            // Player stood: banker draws on 0-5
            if (bankerTotal <= 5) {
                bankerHand.add(randomCard());
            }
        } else {
            // Banker draws based on player's third card value
            applyBankerThirdCardRule(bankerTotal, playerThirdCard);
        }
    }

    /**
     * Standard banker third-card tableau.
     * bankerTotal is the banker's current hand value (from first two cards).
     * playerThirdCard is the value of the player's third card.
     */
    private void applyBankerThirdCardRule(int bankerTotal, int playerThirdCard) {
        boolean bankerDraws;
        switch (bankerTotal) {
            case 0, 1, 2 -> bankerDraws = true;
            case 3 -> bankerDraws = playerThirdCard != 8;
            case 4 -> bankerDraws = playerThirdCard >= 2 && playerThirdCard <= 7;
            case 5 -> bankerDraws = playerThirdCard >= 4 && playerThirdCard <= 7;
            case 6 -> bankerDraws = playerThirdCard == 6 || playerThirdCard == 7;
            default -> bankerDraws = false; // 7: stand
        }
        if (bankerDraws) {
            bankerHand.add(randomCard());
        }
    }

    /**
     * Rigged deal: ensures the admin's chosen side wins.
     */
    private void dealRigged() {
        switch (betSide) {
            case "player" -> {
                // Give player a natural 9, banker something lower
                playerHand.add(9);
                playerHand.add(0); // total = 9
                bankerHand.add(3);
                bankerHand.add(4); // total = 7
            }
            case "banker" -> {
                // Give banker a natural 9, player something lower
                playerHand.add(2);
                playerHand.add(4); // total = 6
                bankerHand.add(9);
                bankerHand.add(0); // total = 9
            }
            case "tie" -> {
                // Give both the same value
                playerHand.add(4);
                playerHand.add(4); // total = 8
                bankerHand.add(3);
                bankerHand.add(5); // total = 8
            }
        }
    }

    // ---- Resolution ----

    /**
     * Resolves the hand, determines winner, pays out.
     */
    public void resolve(EconomyManager eco, CasinoManager casino, ServerLevel level) {
        if (phase != Phase.DEALING || playerId == null) {
            return;
        }
        phase = Phase.RESULT;

        int pVal = handValue(playerHand);
        int bVal = handValue(bankerHand);

        // Determine winner
        if (pVal > bVal) {
            result = "player";
        } else if (bVal > pVal) {
            result = "banker";
        } else {
            result = "tie";
        }

        // Calculate payout
        payout = 0;
        if (result.equals(betSide)) {
            switch (betSide) {
                case "player" -> {
                    payout = betAmount * 2; // 1:1 return original + winnings
                }
                case "banker" -> {
                    // 0.95:1 (5% commission): return original bet + 95% of bet
                    int winnings = (int) (betAmount * 0.95);
                    payout = betAmount + winnings;
                }
                case "tie" -> {
                    payout = betAmount + betAmount * 8; // 8:1 plus original bet
                }
            }
            com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(playerId, payout);
            casino.recordWin(playerId, payout, "baccarat");
            resultMessage = "You win " + payout + " MegaCoins!";
        } else if (result.equals("tie") && !betSide.equals("tie")) {
            // On a tie, non-tie bets push (return the bet)
            payout = betAmount;
            com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(playerId, payout);
            resultMessage = "Tie! Your bet is returned.";
        } else {
            casino.recordLoss(playerId, betAmount, "baccarat");
            resultMessage = "You lose " + betAmount + " MegaCoins.";
        }

        // Delay chat message so card dealing animation plays first
        String winner = result.substring(0, 1).toUpperCase() + result.substring(1);
        pendingChatMessage = "Baccarat: " + winner + " wins! (P:" + handValue(playerHand)
                + " B:" + handValue(bankerHand) + ") " + resultMessage;
        pendingChatColor = payout > betAmount ? ChatFormatting.GOLD : ChatFormatting.RED;
        chatDelayTicks = 40; // ~2 seconds for card animation

        // Save stats
        casino.saveToDisk(level);
        eco.saveToDisk(level);
    }

    // ---- Reset ----

    /**
     * Resets the game for a new round.
     */
    public void reset() {
        phase = Phase.BETTING;
        playerHand.clear();
        bankerHand.clear();
        betSide = "";
        betAmount = 0;
        playerId = null;
        result = "";
        payout = 0;
        resultMessage = "";
    }

    // ---- Sync ----

    /**
     * Sends the current game state to the given player.
     */
    public void syncToPlayer(ServerPlayer player) {
        String json = toJson();
        PacketDistributor.sendToPlayer(player, new BaccaratGameSyncPayload(json));
    }

    /**
     * Serializes the full game state to JSON for client sync.
     */
    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("phase", phase.name());
        json.addProperty("betSide", betSide);
        json.addProperty("betAmount", betAmount);

        JsonArray pCards = new JsonArray();
        for (int c : playerHand) pCards.add(c);
        json.add("playerCards", pCards);

        JsonArray bCards = new JsonArray();
        for (int c : bankerHand) bCards.add(c);
        json.add("bankerCards", bCards);

        json.addProperty("playerValue", handValue(playerHand));
        json.addProperty("bankerValue", handValue(bankerHand));

        json.addProperty("result", result);
        json.addProperty("resultMessage", resultMessage);
        json.addProperty("payout", payout);

        return json.toString();
    }

    // ---- Helpers ----

    private int randomCard() {
        return random.nextInt(10); // 0-9
    }

    private int handValue(List<Integer> hand) {
        int sum = 0;
        for (int c : hand) {
            sum += c;
        }
        return sum % 10;
    }

    private ServerPlayer getServerPlayer() {
        if (playerId == null) return null;
        net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        return server.getPlayerList().getPlayer(playerId);
    }

    // ---- Getters ----

    public List<Integer> getPlayerHand() {
        return playerHand;
    }

    public List<Integer> getBankerHand() {
        return bankerHand;
    }

    public String getBetSide() {
        return betSide;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getResult() {
        return result;
    }

    public int getPayout() {
        return payout;
    }

    public String getResultMessage() {
        return resultMessage;
    }
}
