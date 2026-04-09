package com.ultra.megamod.feature.casino.slots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SlotReels {
    private static final int REEL_SIZE = 256;
    private static final SlotSymbol[][] STRIPS = new SlotSymbol[3][REEL_SIZE];

    // Base weights per 256 symbols
    private static final int BASE_SKULL = 90;
    private static final int BASE_COAL = 38;
    private static final int BASE_CHERRY = 30;
    private static final int BASE_LAPIS = 26;
    private static final int BASE_REDSTONE = 26;
    private static final int BASE_IRON = 20;
    private static final int BASE_GOLD = 13;
    private static final int BASE_EMERALD = 8;
    private static final int BASE_DIAMOND = 5;

    static {
        // Per-reel weight offsets for variety: reel 0 = base, reel 1 = shift A, reel 2 = shift B
        int[][] offsets = {
                {0, 0, 0, 0, 0, 0, 0, 0, 0},       // reel 0: base
                {2, -1, 1, -2, 1, -1, 1, -1, 0},     // reel 1: shifted
                {-2, 1, -1, 2, -1, 1, -1, 1, 0},     // reel 2: shifted opposite
        };

        for (int reel = 0; reel < 3; reel++) {
            int[] weights = {
                    BASE_SKULL + offsets[reel][0],
                    BASE_COAL + offsets[reel][1],
                    BASE_CHERRY + offsets[reel][2],
                    BASE_LAPIS + offsets[reel][3],
                    BASE_REDSTONE + offsets[reel][4],
                    BASE_IRON + offsets[reel][5],
                    BASE_GOLD + offsets[reel][6],
                    BASE_EMERALD + offsets[reel][7],
                    BASE_DIAMOND + offsets[reel][8],
            };

            SlotSymbol[] symbolOrder = {
                    SlotSymbol.SKULL,
                    SlotSymbol.COAL,
                    SlotSymbol.CHERRY,
                    SlotSymbol.LAPIS,
                    SlotSymbol.REDSTONE,
                    SlotSymbol.IRON,
                    SlotSymbol.GOLD,
                    SlotSymbol.EMERALD,
                    SlotSymbol.DIAMOND,
            };

            // Fill the strip based on weights
            List<SlotSymbol> strip = new ArrayList<>(REEL_SIZE);
            int filled = 0;
            for (int s = 0; s < symbolOrder.length; s++) {
                int count = weights[s];
                // For the last symbol type, fill remaining to hit exactly REEL_SIZE
                if (s == symbolOrder.length - 1) {
                    count = REEL_SIZE - filled;
                }
                for (int j = 0; j < count && filled < REEL_SIZE; j++) {
                    strip.add(symbolOrder[s]);
                    filled++;
                }
            }

            // Pad with SKULL if still short (shouldn't happen with correct weights)
            while (strip.size() < REEL_SIZE) {
                strip.add(SlotSymbol.SKULL);
            }

            // Shuffle with a deterministic seed per reel for reproducibility
            Collections.shuffle(strip, new Random(0xCAFEBEEFL + reel * 7919L));

            for (int i = 0; i < REEL_SIZE; i++) {
                STRIPS[reel][i] = strip.get(i);
            }
        }
    }

    /**
     * Gets the symbol at the given position on the given reel. Position wraps with modulo.
     */
    public static SlotSymbol getSymbol(int reel, int position) {
        int idx = ((position % REEL_SIZE) + REEL_SIZE) % REEL_SIZE;
        return STRIPS[reel][idx];
    }

    /**
     * Returns the reel size (256).
     */
    public static int getReelSize() {
        return REEL_SIZE;
    }

    /**
     * Returns a copy of the specified reel strip.
     */
    public static SlotSymbol[] getStrip(int reel) {
        SlotSymbol[] copy = new SlotSymbol[REEL_SIZE];
        System.arraycopy(STRIPS[reel], 0, copy, 0, REEL_SIZE);
        return copy;
    }
}
