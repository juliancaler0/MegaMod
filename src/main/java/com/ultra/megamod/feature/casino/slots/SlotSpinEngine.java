package com.ultra.megamod.feature.casino.slots;

import java.util.List;
import java.util.Random;

public class SlotSpinEngine {
    private static final Random RNG = new Random();

    /**
     * Performs a spin and returns the result.
     *
     * @param betIndex     index into SlotBetConfig.BET_VALUES
     * @param lineMode     index into SlotBetConfig.LINE_MULTIPLIERS (0=1 line, 1=3 lines, 2=5 lines)
     * @param forceJackpot if true, forces all reels to land on DIAMOND
     * @return the spin result with stops, matrix, wins, and total payout
     */
    public static SpinResult doSpin(int betIndex, int lineMode, boolean forceJackpot) {
        int reelSize = SlotReels.getReelSize();
        int[] stops = new int[3];

        if (forceJackpot) {
            // Find a Diamond position on each reel
            for (int r = 0; r < 3; r++) {
                for (int i = 0; i < reelSize; i++) {
                    if (SlotReels.getSymbol(r, i) == SlotSymbol.DIAMOND) {
                        stops[r] = i;
                        break;
                    }
                }
            }
        } else {
            for (int r = 0; r < 3; r++) {
                stops[r] = RNG.nextInt(reelSize);
            }
        }

        // Build 3x3 matrix (rows: top/center/bottom, cols: left/mid/right)
        SlotSymbol[][] matrix = new SlotSymbol[3][3];
        for (int col = 0; col < 3; col++) {
            matrix[0][col] = SlotReels.getSymbol(col, stops[col] - 1); // top
            matrix[1][col] = SlotReels.getSymbol(col, stops[col]);     // center
            matrix[2][col] = SlotReels.getSymbol(col, stops[col] + 1); // bottom
        }

        int lineCount = SlotBetConfig.getLineMultiplier(lineMode); // 1, 3, or 5
        List<SlotLines.LineResult> wins = SlotLines.evaluateLines(matrix, lineCount);

        int baseBet = SlotBetConfig.getBetValue(betIndex);
        int totalWin = 0;
        for (SlotLines.LineResult win : wins) {
            totalWin += baseBet * win.multiplier();
        }

        return new SpinResult(stops, matrix, wins, totalWin);
    }

    public record SpinResult(int[] stops, SlotSymbol[][] matrix, List<SlotLines.LineResult> wins, int totalWin) {
    }
}
