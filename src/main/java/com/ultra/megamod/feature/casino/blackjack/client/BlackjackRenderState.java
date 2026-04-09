package com.ultra.megamod.feature.casino.blackjack.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/**
 * Render state for the blackjack table BER.
 * Populated from BlackjackSyncPayload data on the client side.
 */
public class BlackjackRenderState extends BlockEntityRenderState {
    /** Whether there is an active game with cards to render. */
    public boolean hasGame = false;
    /** Number of dealer cards (face-up). 0 means no game. */
    public int dealerCardCount = 0;
    /** Whether the dealer's hole card (index 1) is revealed. */
    public boolean dealerHoleRevealed = false;
    /** Number of occupied seats (0-4). */
    public int seatCount = 0;
    /** Number of cards in each seat's primary hand (max 4 seats). */
    public int[] seatCardCounts = new int[4];
    /** Whether each seat is occupied. */
    public boolean[] seatOccupied = new boolean[4];
    /** Card color hints per seat: true = at least one red suit card. */
    public boolean[] seatHasRed = new boolean[4];
}
