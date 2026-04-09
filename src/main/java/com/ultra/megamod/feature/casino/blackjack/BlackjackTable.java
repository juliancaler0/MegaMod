package com.ultra.megamod.feature.casino.blackjack;

import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class BlackjackTable {

    public enum Phase {
        WAITING, BETTING, DEALING, PLAYER_TURN, DEALER_TURN, PAYOUT, COMPLETE
    }

    private static final int MAX_SEATS = 4;
    private static final int MAX_SPLIT_HANDS = 4;
    private static final int MIN_DECK_THRESHOLD = 10;

    private Phase phase = Phase.WAITING;
    private final BlockPos tablePos;
    private final PlayerSeat[] seats = new PlayerSeat[MAX_SEATS];
    private final Hand dealerHand = new Hand();
    private boolean dealerHoleCardRevealed = false;
    private Deck deck = new Deck();
    private int currentSeatIndex = -1;
    private CasinoManager lastCasinoManager = null; // cached for dealer turn

    // =========================================================================
    // Player Seat
    // =========================================================================

    public static class PlayerSeat {
        public final UUID playerId;
        public final String playerName;
        public int bet;
        public final List<Hand> hands = new ArrayList<>();
        public int activeHandIndex = 0;
        public boolean hasInsurance = false;
        public int insuranceBet = 0;
        public boolean done = false;

        public PlayerSeat(UUID id, String name) {
            this.playerId = id;
            this.playerName = name;
            this.hands.add(new Hand());
        }

        public Hand activeHand() {
            return hands.get(activeHandIndex);
        }

        public void reset() {
            bet = 0;
            hands.clear();
            hands.add(new Hand());
            activeHandIndex = 0;
            hasInsurance = false;
            insuranceBet = 0;
            done = false;
        }
    }

    // =========================================================================
    // Constructor
    // =========================================================================

    public BlackjackTable(BlockPos tablePos) {
        this.tablePos = tablePos;
    }

    // =========================================================================
    // Join / Leave
    // =========================================================================

    public boolean joinTable(ServerPlayer player) {
        if (isPlayerSeated(player.getUUID())) {
            return false;
        }
        for (int i = 0; i < MAX_SEATS; i++) {
            if (seats[i] == null) {
                seats[i] = new PlayerSeat(player.getUUID(), player.getGameProfile().name());
                BlackjackTableBlockEntity.trackPlayer(player.getUUID(), tablePos);
                // If we're in WAITING and at least one player is now seated, move to BETTING
                if (phase == Phase.WAITING || phase == Phase.COMPLETE) {
                    resetRound();
                    phase = Phase.BETTING;
                }
                return true;
            }
        }
        return false; // table full
    }

    public void leaveTable(UUID playerId) {
        BlackjackTableBlockEntity.untrackPlayer(playerId);
        for (int i = 0; i < MAX_SEATS; i++) {
            if (seats[i] != null && seats[i].playerId.equals(playerId)) {
                seats[i] = null;
                break;
            }
        }
        // If no players remain, reset to WAITING
        if (getSeatedCount() == 0) {
            resetRound();
            phase = Phase.WAITING;
            currentSeatIndex = -1;
        } else if (phase == Phase.PLAYER_TURN) {
            // If the leaving player was the current turn, advance
            if (currentSeatIndex >= 0 && currentSeatIndex < MAX_SEATS
                    && seats[currentSeatIndex] != null
                    && seats[currentSeatIndex].playerId.equals(playerId)) {
                // Seat was already nulled above, advance
                advanceToNextPlayer();
            }
        }
    }

    // =========================================================================
    // Betting
    // =========================================================================

    public boolean placeBet(UUID playerId, int amount, EconomyManager eco, ServerLevel level) {
        if (phase != Phase.BETTING || amount <= 0) {
            return false;
        }
        PlayerSeat seat = getSeat(playerId);
        if (seat == null || seat.bet > 0) {
            return false; // not seated or already bet
        }
        int chipBalance = com.ultra.megamod.feature.casino.chips.ChipManager.get(level).getBalance(playerId);
        if (chipBalance < amount) {
            return false; // insufficient funds
        }
        com.ultra.megamod.feature.casino.chips.ChipManager.get(level).spendChips(playerId, amount);
        seat.bet = amount;

        // Cache references for dealer turn
        this.lastEco = eco;
        this.lastLevel = level;
        this.lastCasinoManager = com.ultra.megamod.feature.casino.CasinoManager.get(level);

        // Check if all seated players have placed their bets
        if (allSeatedPlayersBet()) {
            startDealing();
        }
        return true;
    }

    private boolean allSeatedPlayersBet() {
        for (int i = 0; i < MAX_SEATS; i++) {
            if (seats[i] != null && seats[i].bet <= 0) {
                return false;
            }
        }
        return true;
    }

    // =========================================================================
    // Dealing
    // =========================================================================

    private void startDealing() {
        phase = Phase.DEALING;
        ensureDeckReady();

        // Deal 2 cards to each player
        for (int round = 0; round < 2; round++) {
            for (int i = 0; i < MAX_SEATS; i++) {
                if (seats[i] != null) {
                    seats[i].activeHand().addCard(deck.dealOne());
                }
            }
            dealerHand.addCard(deck.dealOne());
        }
        dealerHoleCardRevealed = false;

        // Check for dealer blackjack (ace showing) -- insurance opportunity handled via actions
        // Check for player natural blackjacks and auto-stand them
        for (int i = 0; i < MAX_SEATS; i++) {
            if (seats[i] != null && seats[i].activeHand().isBlackjack()) {
                seats[i].done = true;
            }
        }

        // Move to player turns
        phase = Phase.PLAYER_TURN;
        currentSeatIndex = findFirstActivePlayer();
        if (currentSeatIndex == -1) {
            // All players have blackjack, go straight to dealer
            beginDealerTurn(lastCasinoManager);
        }
    }

    // =========================================================================
    // Player Actions
    // =========================================================================

    public boolean handleAction(UUID playerId, BlackjackAction action, EconomyManager eco, ServerLevel level, CasinoManager casino) {
        if (phase != Phase.PLAYER_TURN) {
            return false;
        }
        PlayerSeat seat = getSeat(playerId);
        if (seat == null || seat.done) {
            return false;
        }
        // Only the current-turn player can act
        if (currentSeatIndex < 0 || currentSeatIndex >= MAX_SEATS
                || seats[currentSeatIndex] == null
                || !seats[currentSeatIndex].playerId.equals(playerId)) {
            return false;
        }

        this.lastCasinoManager = casino;
        this.lastEco = eco;
        this.lastLevel = level;
        boolean adminAlwaysWin = casino != null && casino.isAlwaysWinBlackjack(playerId);

        switch (action) {
            case HIT -> {
                return doHit(seat, adminAlwaysWin);
            }
            case STAND -> {
                return doStand(seat);
            }
            case DOUBLE -> {
                return doDouble(seat, eco, adminAlwaysWin);
            }
            case SPLIT -> {
                return doSplit(seat, eco);
            }
            case SURRENDER -> {
                return doSurrender(seat, eco, level);
            }
            case INSURANCE -> {
                return doInsurance(seat, eco);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean doHit(PlayerSeat seat, boolean adminAlwaysWin) {
        ensureDeckReady();
        Hand hand = seat.activeHand();

        if (adminAlwaysWin) {
            // Cheat: deal a card that gets closest to 21 without busting
            Card bestCard = findBestCardForTarget(hand, 21);
            if (bestCard != null) {
                hand.addCard(bestCard);
            } else {
                hand.addCard(deck.dealOne());
            }
        } else {
            hand.addCard(deck.dealOne());
        }

        if (hand.isBust()) {
            advanceHand(seat);
        } else if (hand.getValue() == 21) {
            // Auto-stand on 21
            advanceHand(seat);
        }
        return true;
    }

    private boolean doStand(PlayerSeat seat) {
        advanceHand(seat);
        return true;
    }

    private boolean doDouble(PlayerSeat seat, EconomyManager eco, boolean adminAlwaysWin) {
        Hand hand = seat.activeHand();
        // Can only double on first 2 cards of a hand
        if (hand.size() != 2) {
            return false;
        }
        int additionalBet = seat.bet;
        if (com.ultra.megamod.feature.casino.chips.ChipManager.get(lastLevel).getBalance(seat.playerId) < additionalBet) {
            return false; // insufficient funds to double
        }
        com.ultra.megamod.feature.casino.chips.ChipManager.get(lastLevel).spendChips(seat.playerId, additionalBet);
        seat.bet += additionalBet;

        ensureDeckReady();
        if (adminAlwaysWin) {
            Card bestCard = findBestCardForTarget(hand, 21);
            if (bestCard != null) {
                hand.addCard(bestCard);
            } else {
                hand.addCard(deck.dealOne());
            }
        } else {
            hand.addCard(deck.dealOne());
        }

        // After double, player is done with this hand regardless
        advanceHand(seat);
        return true;
    }

    private boolean doSplit(PlayerSeat seat, EconomyManager eco) {
        Hand hand = seat.activeHand();
        if (!hand.canSplit()) {
            return false;
        }
        if (seat.hands.size() >= MAX_SPLIT_HANDS) {
            return false; // max splits reached
        }
        int splitBet = seat.bet / seat.hands.size(); // original bet per hand
        if (com.ultra.megamod.feature.casino.chips.ChipManager.get(lastLevel).getBalance(seat.playerId) < splitBet) {
            return false; // can't afford the split
        }
        com.ultra.megamod.feature.casino.chips.ChipManager.get(lastLevel).spendChips(seat.playerId, splitBet);

        // Create the new hand from the second card
        Card secondCard = hand.getCards().get(1);
        Hand newHand = new Hand();
        newHand.addCard(secondCard);

        // Rebuild current hand with just the first card
        Card firstCard = hand.getCards().get(0);
        hand.clear();
        hand.addCard(firstCard);

        // Deal one new card to each hand
        ensureDeckReady();
        hand.addCard(deck.dealOne());
        newHand.addCard(deck.dealOne());

        // Insert the new hand right after the current active hand
        seat.hands.add(seat.activeHandIndex + 1, newHand);

        // Check if current hand is 21, if so advance to next hand
        if (hand.getValue() == 21) {
            advanceHand(seat);
        }
        return true;
    }

    private boolean doSurrender(PlayerSeat seat, EconomyManager eco, ServerLevel level) {
        Hand hand = seat.activeHand();
        // Can only surrender on initial 2 cards, no splits
        if (hand.size() != 2 || seat.hands.size() > 1) {
            return false;
        }
        // Return half the bet
        int halfBet = seat.bet / 2;
        com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(seat.playerId, halfBet);
        seat.bet = seat.bet - halfBet; // house keeps the other half
        seat.done = true;
        advanceToNextPlayer();
        return true;
    }

    private boolean doInsurance(PlayerSeat seat, EconomyManager eco) {
        // Insurance only available when dealer shows an Ace, during first action
        if (seat.hasInsurance) {
            return false;
        }
        if (dealerHand.getCards().isEmpty()) {
            return false;
        }
        // Dealer's face-up card is the first card dealt
        Card dealerUpCard = dealerHand.getCards().get(0);
        if (dealerUpCard.rank() != Card.Rank.ACE) {
            return false;
        }
        int insuranceAmount = seat.bet / 2;
        if (com.ultra.megamod.feature.casino.chips.ChipManager.get(lastLevel).getBalance(seat.playerId) < insuranceAmount) {
            return false;
        }
        com.ultra.megamod.feature.casino.chips.ChipManager.get(lastLevel).spendChips(seat.playerId, insuranceAmount);
        seat.hasInsurance = true;
        seat.insuranceBet = insuranceAmount;
        return true;
    }

    // =========================================================================
    // Hand / Turn Advancement
    // =========================================================================

    private void advanceHand(PlayerSeat seat) {
        // Move to next hand in this seat's split hands
        if (seat.activeHandIndex < seat.hands.size() - 1) {
            seat.activeHandIndex++;
            // Auto-stand on 21
            if (seat.activeHand().getValue() == 21) {
                advanceHand(seat);
            }
        } else {
            // All hands for this seat are done
            seat.done = true;
            advanceToNextPlayer();
        }
    }

    private void advanceToNextPlayer() {
        int next = findNextActivePlayer(currentSeatIndex);
        if (next == -1) {
            // All players done, dealer's turn
            beginDealerTurn(lastCasinoManager);
        } else {
            currentSeatIndex = next;
        }
    }

    private int findFirstActivePlayer() {
        for (int i = 0; i < MAX_SEATS; i++) {
            if (seats[i] != null && !seats[i].done) {
                return i;
            }
        }
        return -1;
    }

    private int findNextActivePlayer(int after) {
        for (int i = after + 1; i < MAX_SEATS; i++) {
            if (seats[i] != null && !seats[i].done) {
                return i;
            }
        }
        return -1;
    }

    // =========================================================================
    // Dealer Turn
    // =========================================================================

    private EconomyManager lastEco = null;
    private ServerLevel lastLevel = null;
    private int dealerTurnDelay = 0; // ticks until next dealer action
    private boolean dealerNeedsBust = false; // admin cheat flag for this round
    private boolean dealerTurnComplete = false;
    private int payoutDelay = 0; // ticks after payout before reset

    private void beginDealerTurn(CasinoManager casino) {
        phase = Phase.DEALER_TURN;
        dealerHoleCardRevealed = true;
        dealerTurnDelay = 30; // 1.5 second pause to show hole card
        dealerTurnComplete = false;
        dealerNeedsBust = false;

        // Admin auto-win flag is now handled during payout resolution, not dealer card logic.
        // The dealer plays by standard rules (hit on <17/soft 17, stand on 17+).

        // Broadcast the hole card reveal immediately
        broadcastToSeatedPlayers();
    }

    /**
     * Called every server tick. Handles dealer turn step-by-step with delays.
     */
    public void tick() {
        if (phase == Phase.DEALER_TURN && !dealerTurnComplete) {
            if (dealerTurnDelay > 0) {
                dealerTurnDelay--;
                return;
            }

            // Standard dealer rules: hit on <17 or soft 17, stand on hard 17+
            boolean shouldHit = dealerHand.getValue() < 17 || (dealerHand.getValue() == 17 && dealerHand.isSoft());

            if (shouldHit) {
                ensureDeckReady();
                dealerHand.addCard(deck.dealOne());
                dealerTurnDelay = 25; // 1.25 second between each dealer card
                broadcastToSeatedPlayers();
            } else {
                // Dealer is done hitting
                dealerTurnComplete = true;
                phase = Phase.PAYOUT;
                if (lastEco != null && lastLevel != null) {
                    resolveBets(lastEco, lastCasinoManager, lastLevel);
                }
                payoutDelay = 60; // 3 seconds to read results before reset
                broadcastToSeatedPlayers();
            }
        }

        if (phase == Phase.PAYOUT || phase == Phase.COMPLETE) {
            if (payoutDelay > 0) {
                payoutDelay--;
                if (payoutDelay == 0) {
                    if (getSeatedCount() > 0) {
                        resetRound();
                        phase = Phase.BETTING;
                    } else {
                        phase = Phase.COMPLETE;
                    }
                    broadcastToSeatedPlayers();
                }
            }
        }
    }

    private void broadcastToSeatedPlayers() {
        if (lastLevel == null) return;
        for (int i = 0; i < MAX_SEATS; i++) {
            if (seats[i] != null) {
                net.minecraft.server.level.ServerPlayer sp = lastLevel.getServer().getPlayerList().getPlayer(seats[i].playerId);
                if (sp != null) {
                    com.ultra.megamod.feature.casino.network.BlackjackSyncPayload sync =
                            new com.ultra.megamod.feature.casino.network.BlackjackSyncPayload(toJsonForPlayer(seats[i].playerId).toString());
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, sync);
                }
            }
        }
    }

    // playDealer is now handled step-by-step in tick()

    // =========================================================================
    // Payout / Resolution
    // =========================================================================

    public void resolveBets(EconomyManager eco, CasinoManager casino, ServerLevel level) {
        if (phase != Phase.PAYOUT) {
            return;
        }

        int dealerValue = dealerHand.getValue();
        boolean dealerBust = dealerHand.isBust();
        boolean dealerBlackjack = dealerHand.isBlackjack();

        for (int i = 0; i < MAX_SEATS; i++) {
            PlayerSeat seat = seats[i];
            if (seat == null) continue;

            UUID pid = seat.playerId;
            int perHandBet = seat.bet / Math.max(1, seat.hands.size());

            // Handle insurance first
            if (seat.hasInsurance) {
                if (dealerBlackjack) {
                    // Insurance pays 2:1
                    int insurancePayout = seat.insuranceBet * 3; // original bet + 2x winnings
                    com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(pid, insurancePayout);
                }
                // If dealer doesn't have blackjack, insurance is lost (already deducted)
            }

            int totalWon = 0;
            int totalLost = 0;

            // Admin auto-win payout override: only if BOTH blackjack auto-win AND payout override are on
            // Base auto-win just rigs cards (admin still needs to play well or reach 21)
            // Payout override = true always win regardless of hand value
            boolean adminPayoutOverride = false;
            if (casino != null && casino.isAlwaysWinBlackjack(pid) && casino.isAlwaysWinPayoutOverride(pid)) {
                adminPayoutOverride = true;
            }

            for (Hand hand : seat.hands) {
                int handBet = perHandBet;
                int handValue = hand.getValue();
                boolean handBust = hand.isBust();
                boolean handBlackjack = hand.isBlackjack();

                if (adminPayoutOverride && !handBust) {
                    // Payout override: admin always wins any non-bust hand, even if dealer is higher
                    int payout = handBlackjack ? handBet + (handBet * 3 / 2) : handBet * 2;
                    com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(pid, payout);
                    totalWon += payout - handBet;
                    if (casino != null) casino.recordWin(pid, payout - handBet, "blackjack");
                } else if (handBust) {
                    totalLost += handBet;
                    if (casino != null) casino.recordLoss(pid, handBet, "blackjack");
                } else if (handBlackjack && !dealerBlackjack) {
                    int payout = handBet + (handBet * 3 / 2);
                    com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(pid, payout);
                    totalWon += payout - handBet;
                    if (casino != null) casino.recordWin(pid, payout - handBet, "blackjack");
                } else if (dealerBust) {
                    int payout = handBet * 2;
                    com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(pid, payout);
                    totalWon += handBet;
                    if (casino != null) casino.recordWin(pid, handBet, "blackjack");
                } else if (handBlackjack && dealerBlackjack) {
                    com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(pid, handBet); // push
                } else if (handValue > dealerValue) {
                    int payout = handBet * 2;
                    com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(pid, payout);
                    totalWon += handBet;
                    if (casino != null) casino.recordWin(pid, handBet, "blackjack");
                } else if (handValue == dealerValue) {
                    com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(pid, handBet); // push
                } else {
                    totalLost += handBet;
                    if (casino != null) casino.recordLoss(pid, handBet, "blackjack");
                }
            }

            if (casino != null) casino.recordWager(pid, seat.bet, "blackjack");

            // Send chat result to the player
            net.minecraft.server.level.ServerPlayer sp = level.getServer().getPlayerList().getPlayer(pid);
            if (sp != null) {
                String dealerStr = dealerBust ? "Dealer BUSTED (" + dealerValue + ")" : "Dealer: " + dealerValue;
                if (totalWon > 0 && totalLost == 0) {
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            dealerStr + " | " + seat.playerName + " WON +" + totalWon + " MC!")
                            .withStyle(net.minecraft.ChatFormatting.GREEN));
                } else if (totalLost > 0 && totalWon == 0) {
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            dealerStr + " | " + seat.playerName + " LOST -" + totalLost + " MC")
                            .withStyle(net.minecraft.ChatFormatting.RED));
                } else if (totalWon > 0 && totalLost > 0) {
                    int netResult = totalWon - totalLost;
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            dealerStr + " | " + seat.playerName + " net: " + (netResult >= 0 ? "+" : "") + netResult + " MC")
                            .withStyle(netResult >= 0 ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED));
                } else {
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            dealerStr + " | " + seat.playerName + " PUSH (bet returned)")
                            .withStyle(net.minecraft.ChatFormatting.YELLOW));
                }

                // Also broadcast to all other players at the table
                for (int j = 0; j < MAX_SEATS; j++) {
                    if (j != i && seats[j] != null) {
                        net.minecraft.server.level.ServerPlayer other = level.getServer().getPlayerList().getPlayer(seats[j].playerId);
                        if (other != null) {
                            String result = totalWon > totalLost ? "won" : (totalLost > totalWon ? "lost" : "pushed");
                            other.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    seat.playerName + " " + result + " at blackjack")
                                    .withStyle(net.minecraft.ChatFormatting.GRAY));
                        }
                    }
                }
            }
        }

        // Save
        if (casino != null) casino.saveToDisk(level);
        eco.saveToDisk(level);

        phase = Phase.COMPLETE;
    }

    // =========================================================================
    // Available Actions
    // =========================================================================

    public List<BlackjackAction> getAvailableActions(UUID playerId) {
        List<BlackjackAction> actions = new ArrayList<>();

        if (!isPlayerSeated(playerId)) {
            if (phase == Phase.WAITING || phase == Phase.COMPLETE) {
                actions.add(BlackjackAction.JOIN);
            }
            return actions;
        }

        actions.add(BlackjackAction.LEAVE);

        PlayerSeat seat = getSeat(playerId);
        if (seat == null) return actions;

        switch (phase) {
            case BETTING -> {
                if (seat.bet <= 0) {
                    actions.add(BlackjackAction.BET);
                }
            }
            case PLAYER_TURN -> {
                if (!seat.done && currentSeatIndex >= 0 && currentSeatIndex < MAX_SEATS
                        && seats[currentSeatIndex] != null
                        && seats[currentSeatIndex].playerId.equals(playerId)) {
                    Hand hand = seat.activeHand();
                    actions.add(BlackjackAction.HIT);
                    actions.add(BlackjackAction.STAND);

                    // Double: only on first 2 cards
                    if (hand.size() == 2) {
                        actions.add(BlackjackAction.DOUBLE);
                    }

                    // Split: same rank, < max hands, first 2 cards
                    if (hand.canSplit() && seat.hands.size() < MAX_SPLIT_HANDS) {
                        actions.add(BlackjackAction.SPLIT);
                    }

                    // Surrender: only on initial 2 cards, no splits
                    if (hand.size() == 2 && seat.hands.size() == 1) {
                        actions.add(BlackjackAction.SURRENDER);
                    }

                    // Insurance: dealer shows ace, not already taken
                    if (!seat.hasInsurance && !dealerHand.getCards().isEmpty()
                            && dealerHand.getCards().get(0).rank() == Card.Rank.ACE) {
                        actions.add(BlackjackAction.INSURANCE);
                    }
                }
            }
            default -> {
                // No game actions available in other phases
            }
        }

        return actions;
    }

    // =========================================================================
    // Admin Always-Win Card Selection
    // =========================================================================

    /**
     * Finds the best card from the deck that brings the hand closest to the target
     * value without busting. Removes that card from the deck.
     * Returns null if no card can be dealt without busting (falls back to normal deal).
     */
    /**
     * Admin auto-win card fabrication.
     * Picks the perfect card value to reach exactly the target (21) without busting.
     * Fabricates a card rather than searching the deck — consumes one real card to keep count.
     */
    private Card findBestCardForTarget(Hand hand, int target) {
        int currentValue = hand.getValue();
        int needed = target - currentValue;
        if (needed <= 0) return null;

        // Find the rank whose value best matches what we need
        Card.Rank bestRank = null;
        int bestDiff = Integer.MAX_VALUE;

        for (Card.Rank rank : Card.Rank.values()) {
            // Simulate adding this rank to the hand
            Card.Suit dummySuit = Card.Suit.HEARTS;
            int simVal = simulateHandValue(hand, new Card(dummySuit, rank));
            if (simVal <= target) {
                int diff = target - simVal;
                if (diff < bestDiff) {
                    bestDiff = diff;
                    bestRank = rank;
                    if (diff == 0) break; // perfect match
                }
            }
        }

        if (bestRank == null) return null;

        // Consume one real card from deck to keep count consistent
        if (deck.remaining() > 0) deck.dealOne();

        // Random suit for visual variety
        Card.Suit suit = Card.Suit.values()[java.util.concurrent.ThreadLocalRandom.current().nextInt(4)];
        return new Card(suit, bestRank);
    }

    private int simulateHandValue(Hand hand, Card extraCard) {
        int total = 0;
        int aceCount = 0;
        for (Card c : hand.getCards()) {
            total += c.value();
            if (c.rank() == Card.Rank.ACE) aceCount++;
        }
        total += extraCard.value();
        if (extraCard.rank() == Card.Rank.ACE) aceCount++;
        while (total > 21 && aceCount > 0) {
            total -= 10;
            aceCount--;
        }
        return total;
    }

    // =========================================================================
    // Utility
    // =========================================================================

    private void ensureDeckReady() {
        if (deck.remaining() < MIN_DECK_THRESHOLD) {
            deck.reset();
        }
    }

    private void resetRound() {
        dealerHand.clear();
        dealerHoleCardRevealed = false;
        currentSeatIndex = -1;
        for (int i = 0; i < MAX_SEATS; i++) {
            if (seats[i] != null) {
                seats[i].reset();
            }
        }
    }

    private PlayerSeat getSeat(UUID playerId) {
        for (int i = 0; i < MAX_SEATS; i++) {
            if (seats[i] != null && seats[i].playerId.equals(playerId)) {
                return seats[i];
            }
        }
        return null;
    }

    private int getSeatedCount() {
        int count = 0;
        for (int i = 0; i < MAX_SEATS; i++) {
            if (seats[i] != null) count++;
        }
        return count;
    }

    // =========================================================================
    // Public Getters
    // =========================================================================

    public Phase getPhase() {
        return phase;
    }

    public int getSeatCount() {
        return getSeatedCount();
    }

    public boolean isPlayerSeated(UUID playerId) {
        return getSeat(playerId) != null;
    }

    public BlockPos getTablePos() {
        return tablePos;
    }

    public Hand getDealerHand() {
        return dealerHand;
    }

    public boolean isDealerHoleCardRevealed() {
        return dealerHoleCardRevealed;
    }

    public PlayerSeat[] getSeats() {
        return seats;
    }

    public int getCurrentSeatIndex() {
        return currentSeatIndex;
    }

    // =========================================================================
    // JSON Serialization (for network sync)
    // =========================================================================

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("phase", phase.name());
        root.addProperty("currentSeat", currentSeatIndex);

        // Dealer hand
        JsonObject dealer = new JsonObject();
        JsonArray dealerCards = new JsonArray();
        List<Card> dCards = dealerHand.getCards();
        for (int i = 0; i < dCards.size(); i++) {
            if (i == 1 && !dealerHoleCardRevealed) {
                dealerCards.add("??"); // hide hole card
            } else {
                dealerCards.add(dCards.get(i).toShortString());
            }
        }
        dealer.add("cards", dealerCards);
        if (dealerHoleCardRevealed || dCards.isEmpty()) {
            dealer.addProperty("value", dealerHand.getValue());
        } else {
            // Only show value of the face-up card
            dealer.addProperty("value", dCards.isEmpty() ? 0 : dCards.get(0).value());
        }
        dealer.addProperty("holeRevealed", dealerHoleCardRevealed);
        root.add("dealer", dealer);

        // Seats
        JsonArray seatsArray = new JsonArray();
        for (int i = 0; i < MAX_SEATS; i++) {
            JsonObject seatObj = new JsonObject();
            if (seats[i] == null) {
                seatObj.addProperty("occupied", false);
            } else {
                PlayerSeat seat = seats[i];
                seatObj.addProperty("occupied", true);
                seatObj.addProperty("playerId", seat.playerId.toString());
                seatObj.addProperty("playerName", seat.playerName);
                seatObj.addProperty("bet", seat.bet);
                seatObj.addProperty("done", seat.done);
                seatObj.addProperty("activeHand", seat.activeHandIndex);
                seatObj.addProperty("hasInsurance", seat.hasInsurance);
                seatObj.addProperty("insuranceBet", seat.insuranceBet);

                JsonArray handsArray = new JsonArray();
                for (Hand hand : seat.hands) {
                    JsonObject handObj = new JsonObject();
                    JsonArray cardArray = new JsonArray();
                    for (Card c : hand.getCards()) {
                        cardArray.add(c.toShortString());
                    }
                    handObj.add("cards", cardArray);
                    handObj.addProperty("value", hand.getValue());
                    handObj.addProperty("bust", hand.isBust());
                    handObj.addProperty("blackjack", hand.isBlackjack());
                    handObj.addProperty("soft", hand.isSoft());
                    handsArray.add(handObj);
                }
                seatObj.add("hands", handsArray);
            }
            seatsArray.add(seatObj);
        }
        root.add("seats", seatsArray);

        return root;
    }

    /**
     * Creates a JSON state for a specific player, including their seat index,
     * available actions, and flattened dealer info for easy client parsing.
     */
    public JsonObject toJsonForPlayer(java.util.UUID playerId) {
        JsonObject root = toJson();

        // Flatten dealer info to root level for easy parsing
        if (root.has("dealer")) {
            JsonObject dealer = root.getAsJsonObject("dealer");
            if (dealer.has("cards")) root.add("dealerCards", dealer.get("cards"));
            if (dealer.has("value")) root.addProperty("dealerValue", dealer.get("value").getAsInt());
            if (dealer.has("holeRevealed")) root.addProperty("dealerRevealed", dealer.get("holeRevealed").getAsBoolean());
        }

        // Find this player's seat index
        int mySeat = -1;
        for (int i = 0; i < MAX_SEATS; i++) {
            if (seats[i] != null && seats[i].playerId.equals(playerId)) {
                mySeat = i;
                break;
            }
        }
        root.addProperty("mySeatIndex", mySeat);
        root.addProperty("myPlayerId", playerId.toString());

        // Available actions for this player
        List<BlackjackAction> actions = getAvailableActions(playerId);
        JsonArray actionsArray = new JsonArray();
        for (BlackjackAction action : actions) {
            actionsArray.add(action.name());
        }
        root.add("availableActions", actionsArray);

        // Status text
        String status = switch (phase) {
            case WAITING -> "Waiting for players...";
            case BETTING -> "Place your bets!";
            case DEALING -> "Dealing cards...";
            case PLAYER_TURN -> {
                if (currentSeatIndex >= 0 && currentSeatIndex < MAX_SEATS && seats[currentSeatIndex] != null) {
                    if (seats[currentSeatIndex].playerId.equals(playerId)) {
                        yield "Your turn! Hit or Stand?";
                    } else {
                        yield seats[currentSeatIndex].playerName + "'s turn...";
                    }
                }
                yield "Playing...";
            }
            case DEALER_TURN -> "Dealer's turn...";
            case PAYOUT -> "Resolving bets...";
            case COMPLETE -> "Round complete!";
        };
        root.addProperty("statusText", status);

        // Active player name (whose turn it is)
        if (phase == Phase.PLAYER_TURN && currentSeatIndex >= 0 && currentSeatIndex < MAX_SEATS
                && seats[currentSeatIndex] != null) {
            root.addProperty("activePlayerName", seats[currentSeatIndex].playerName);
            root.addProperty("activePlayerSeat", currentSeatIndex);
        }

        // All player bets summary for HUD display
        JsonArray allBets = new JsonArray();
        for (int i = 0; i < MAX_SEATS; i++) {
            if (seats[i] != null) {
                JsonObject betInfo = new JsonObject();
                betInfo.addProperty("name", seats[i].playerName);
                betInfo.addProperty("bet", seats[i].bet);
                betInfo.addProperty("seat", i);
                betInfo.addProperty("done", seats[i].done);
                betInfo.addProperty("isActive", i == currentSeatIndex && phase == Phase.PLAYER_TURN);
                // Current hand value
                if (!seats[i].hands.isEmpty()) {
                    betInfo.addProperty("handValue", seats[i].activeHand().getValue());
                    betInfo.addProperty("bust", seats[i].activeHand().isBust());
                    betInfo.addProperty("blackjack", seats[i].activeHand().isBlackjack());
                }
                allBets.add(betInfo);
            }
        }
        root.add("playerSummary", allBets);

        return root;
    }
}
