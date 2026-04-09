package com.ultra.megamod.feature.casino.blackjack;

public record Card(Suit suit, Rank rank) {
    public enum Suit {
        HEARTS("H", 0xFFFF0000), DIAMONDS("D", 0xFFFF0000),
        CLUBS("C", 0xFF000000), SPADES("S", 0xFF000000);
        public final String symbol;
        public final int color;
        Suit(String symbol, int color) { this.symbol = symbol; this.color = color; }
    }
    public enum Rank {
        TWO("2", 2), THREE("3", 3), FOUR("4", 4), FIVE("5", 5), SIX("6", 6),
        SEVEN("7", 7), EIGHT("8", 8), NINE("9", 9), TEN("10", 10),
        JACK("J", 10), QUEEN("Q", 10), KING("K", 10), ACE("A", 11);
        public final String display;
        public final int value;
        Rank(String display, int value) { this.display = display; this.value = value; }
    }
    public int value() { return rank.value; }
    public String toShortString() { return rank.display + suit.symbol; }
}
