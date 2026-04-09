package com.ultra.megamod.feature.casino.slots;

import java.util.ArrayList;
import java.util.List;

public class SlotLines {
    // 5 lines: center, top, bottom, diag-down, diag-up
    // Each line is 3 positions as [row][col] (0=top, 1=center, 2=bottom row; 0/1/2 = left/mid/right col)
    private static final int[][][] LINE_COORDS = {
            {{1, 0}, {1, 1}, {1, 2}},  // center horizontal
            {{0, 0}, {0, 1}, {0, 2}},  // top horizontal
            {{2, 0}, {2, 1}, {2, 2}},  // bottom horizontal
            {{0, 0}, {1, 1}, {2, 2}},  // diagonal \
            {{2, 0}, {1, 1}, {0, 2}},  // diagonal /
    };

    /**
     * Evaluates win lines on the given 3x3 symbol matrix.
     *
     * @param matrix    3x3 symbol grid (rows: top/center/bottom, cols: left/mid/right)
     * @param lineCount number of lines to check: 1 = center only, 3 = top+center+bottom, 5 = all
     * @return list of winning line results
     */
    public static List<LineResult> evaluateLines(SlotSymbol[][] matrix, int lineCount) {
        List<LineResult> wins = new ArrayList<>();
        for (int i = 0; i < lineCount; i++) {
            LineResult result = evaluateLine(matrix, LINE_COORDS[i], i);
            if (result != null) {
                wins.add(result);
            }
        }
        return wins;
    }

    private static LineResult evaluateLine(SlotSymbol[][] matrix, int[][] coords, int lineIndex) {
        SlotSymbol a = matrix[coords[0][0]][coords[0][1]];
        SlotSymbol b = matrix[coords[1][0]][coords[1][1]];
        SlotSymbol c = matrix[coords[2][0]][coords[2][1]];

        // Cherry special: match from left
        if (a == SlotSymbol.CHERRY) {
            int count = 1;
            if (b == SlotSymbol.CHERRY) {
                count = 2;
                if (c == SlotSymbol.CHERRY) {
                    count = 3;
                }
            }
            return new LineResult(lineIndex, SlotSymbol.CHERRY, count, SlotSymbol.getCherryMultiplier(count));
        }

        // Normal: all 3 must match and be winnable
        if (a == b && b == c && a.isWinnable()) {
            return new LineResult(lineIndex, a, 3, a.getTripleMultiplier());
        }

        return null;
    }

    public record LineResult(int lineIndex, SlotSymbol symbol, int matchCount, int multiplier) {
    }
}
