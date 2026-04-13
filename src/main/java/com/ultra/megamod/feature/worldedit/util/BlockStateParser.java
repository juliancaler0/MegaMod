package com.ultra.megamod.feature.worldedit.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Parses a block identifier, optionally with bracket-enclosed state
 * properties, into a BlockState. Accepts "stone", "minecraft:stone",
 * "oak_log[axis=y]", etc.
 */
public final class BlockStateParser {
    private static final Logger LOGGER = LogUtils.getLogger();

    private BlockStateParser() {}

    public static BlockState parse(String input) {
        if (input == null || input.isBlank()) return null;
        input = input.trim();

        // strip leading "minecraft:" optional; allow "#cobblestone" (tags) etc silently
        int bracketStart = input.indexOf('[');
        String idPart;
        String propsPart = null;
        if (bracketStart >= 0) {
            int bracketEnd = input.lastIndexOf(']');
            if (bracketEnd > bracketStart) {
                idPart = input.substring(0, bracketStart).trim();
                propsPart = input.substring(bracketStart + 1, bracketEnd).trim();
            } else {
                idPart = input;
            }
        } else {
            idPart = input;
        }

        if (!idPart.contains(":")) idPart = "minecraft:" + idPart;
        Identifier id = Identifier.tryParse(idPart);
        if (id == null) return null;
        Optional<Block> bOpt = BuiltInRegistries.BLOCK.getOptional(id);
        if (bOpt.isEmpty()) return null;
        BlockState state = bOpt.get().defaultBlockState();

        if (propsPart != null && !propsPart.isEmpty()) {
            for (String kv : propsPart.split(",")) {
                int eq = kv.indexOf('=');
                if (eq < 0) continue;
                String k = kv.substring(0, eq).trim();
                String v = kv.substring(eq + 1).trim();
                state = applyProperty(state, k, v);
            }
        }
        return state;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState applyProperty(BlockState state, String name, String value) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty(name);
        if (property == null) return state;
        Optional<?> v = property.getValue(value);
        if (v.isEmpty()) return state;
        return state.setValue((Property) property, (Comparable) v.get());
    }

    /** Returns "minecraft:stone" for a BlockState. */
    public static String idOf(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }
}
