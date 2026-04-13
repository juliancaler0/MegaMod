package com.ultra.megamod.feature.worldedit.pattern;

import com.ultra.megamod.feature.worldedit.util.BlockStateParser;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Parses WorldEdit-style pattern strings.
 *
 * Examples:
 *   stone
 *   minecraft:stone
 *   50%stone,50%dirt
 *   3%gold_ore,97%stone
 */
public final class PatternParser {
    private PatternParser() {}

    public static Pattern parse(String input) {
        if (input == null || input.isBlank()) return null;
        input = input.trim();

        if (!input.contains(",") && !input.contains("%")) {
            BlockState state = BlockStateParser.parse(input);
            if (state == null) return null;
            return new BlockPattern(state);
        }

        RandomPattern rand = new RandomPattern();
        String[] parts = input.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            double weight = 1.0;
            String blockPart = part;
            int pct = part.indexOf('%');
            if (pct > 0) {
                try {
                    weight = Double.parseDouble(part.substring(0, pct));
                } catch (NumberFormatException ignored) { weight = 1.0; }
                blockPart = part.substring(pct + 1).trim();
            }
            BlockState st = BlockStateParser.parse(blockPart);
            if (st == null) continue;
            rand.add(new BlockPattern(st), weight);
        }
        if (rand.isEmpty()) return null;
        return rand;
    }
}
