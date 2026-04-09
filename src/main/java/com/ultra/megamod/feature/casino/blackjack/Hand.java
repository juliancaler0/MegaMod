package com.ultra.megamod.feature.casino.blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hand {
    private final List<Card> cards = new ArrayList<>();

    public void addCard(Card card) {
        cards.add(card);
    }

    public int getValue() {
        int total = 0;
        int aceCount = 0;
        for (Card card : cards) {
            total += card.value();
            if (card.rank() == Card.Rank.ACE) {
                aceCount++;
            }
        }
        while (total > 21 && aceCount > 0) {
            total -= 10;
            aceCount--;
        }
        return total;
    }

    public boolean isBust() {
        return getValue() > 21;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && getValue() == 21;
    }

    /**
     * Returns true if the hand has at least one ace still counting as 11.
     */
    public boolean isSoft() {
        int total = 0;
        int aceCount = 0;
        for (Card card : cards) {
            total += card.value();
            if (card.rank() == Card.Rank.ACE) {
                aceCount++;
            }
        }
        // Reduce aces from 11 to 1 as needed, but check if at least one remains at 11
        while (total > 21 && aceCount > 0) {
            total -= 10;
            aceCount--;
        }
        return aceCount > 0 && total <= 21;
    }

    public boolean canSplit() {
        return cards.size() == 2 && cards.get(0).rank() == cards.get(1).rank();
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public void clear() {
        cards.clear();
    }

    public int size() {
        return cards.size();
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(cards.get(i).toShortString()).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
}
