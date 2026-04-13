package com.ultra.megamod.feature.worldedit.mask;

import com.ultra.megamod.feature.worldedit.util.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parses WorldEdit-style mask strings.
 *
 * Grammar:
 *   "#existing" or "#solid" or "#air"  - built-in masks
 *   "!<mask>"                          - negation
 *   "<mask>&<mask>"                    - intersection
 *   "<mask>|<mask>"                    - union
 *   "stone,dirt"                       - any of the listed block types
 *   "$biome:<id>"                      - biome mask
 */
public final class MaskParser {
    private MaskParser() {}

    public static Mask parse(String input) {
        if (input == null || input.isBlank()) return null;
        input = input.trim();

        // union (lowest precedence)
        if (input.contains("|")) {
            List<Mask> parts = new ArrayList<>();
            for (String s : input.split("\\|")) {
                Mask m = parse(s);
                if (m != null) parts.add(m);
            }
            return parts.isEmpty() ? null : new UnionMask(parts);
        }

        // intersection
        if (input.contains("&")) {
            List<Mask> parts = new ArrayList<>();
            for (String s : input.split("&")) {
                Mask m = parse(s);
                if (m != null) parts.add(m);
            }
            return parts.isEmpty() ? null : new IntersectMask(parts);
        }

        // negation
        if (input.startsWith("!")) {
            Mask inner = parse(input.substring(1).trim());
            return inner == null ? null : new NegateMask(inner);
        }

        // special tokens
        if (input.equalsIgnoreCase("#existing")) return new ExistingBlockMask();
        if (input.equalsIgnoreCase("#solid")) return new SolidBlockMask();
        if (input.equalsIgnoreCase("#air")) return new AirMask();
        if (input.equalsIgnoreCase("*")) return AlwaysTrueMask.INSTANCE;

        // biome
        if (input.startsWith("$")) {
            String idStr = input.substring(1).trim();
            if (!idStr.contains(":")) idStr = "minecraft:" + idStr;
            Identifier id = Identifier.tryParse(idStr);
            if (id == null) return null;
            return new BiomeMask(id);
        }

        // list of blocks
        if (input.contains(",")) {
            List<BlockState> states = new ArrayList<>();
            for (String part : input.split(",")) {
                BlockState st = BlockStateParser.parse(part.trim());
                if (st != null) states.add(st);
            }
            return states.isEmpty() ? null : new BlockMask(states);
        }

        BlockState st = BlockStateParser.parse(input);
        if (st == null) return null;
        return new BlockMask(List.of(st));
    }
}
