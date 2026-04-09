package com.ultra.megamod.feature.casino.blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public class Deck {
    private final ArrayList<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>(52);
        fill();
        shuffle();
    }

    private void fill() {
        cards.clear();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards, ThreadLocalRandom.current());
    }

    public Card dealOne() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty, cannot deal");
        }
        return cards.removeLast();
    }

    public int remaining() {
        return cards.size();
    }

    public void reset() {
        fill();
        shuffle();
    }
}
