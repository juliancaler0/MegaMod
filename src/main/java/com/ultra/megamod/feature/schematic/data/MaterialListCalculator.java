package com.ultra.megamod.feature.schematic.data;

import com.ultra.megamod.feature.schematic.placement.SchematicPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Calculates the material list for a schematic placement.
 */
public class MaterialListCalculator {

    /**
     * Calculates the material list for a placement.
     * Optionally checks a container (town chest) for available materials.
     */
    public static List<MaterialListEntry> calculate(SchematicPlacement placement,
                                                     Container availableInventory) {
        Map<BlockPos, BlockState> worldBlocks = placement.getWorldBlocks();
        return calculate(worldBlocks, availableInventory);
    }

    /**
     * Calculates the material list from a resolved block map.
     */
    public static List<MaterialListEntry> calculate(Map<BlockPos, BlockState> blocks,
                                                     Container availableInventory) {
        // Count required blocks
        Map<Item, Integer> required = new LinkedHashMap<>();
        for (BlockState state : blocks.values()) {
            Item item = state.getBlock().asItem();
            if (item == Items.AIR) continue; // Blocks with no item form (e.g., fire, water)
            required.merge(item, 1, Integer::sum);
        }

        // Build entries
        List<MaterialListEntry> entries = new ArrayList<>(required.size());
        for (Map.Entry<Item, Integer> entry : required.entrySet()) {
            entries.add(new MaterialListEntry(entry.getKey(), entry.getValue()));
        }

        // Count available items if a container is provided
        if (availableInventory != null) {
            Map<Item, Integer> available = new HashMap<>();
            for (int i = 0; i < availableInventory.getContainerSize(); i++) {
                var stack = availableInventory.getItem(i);
                if (!stack.isEmpty()) {
                    available.merge(stack.getItem(), stack.getCount(), Integer::sum);
                }
            }
            for (MaterialListEntry entry : entries) {
                entry.setCountAvailable(available.getOrDefault(entry.getItem(), 0));
            }
        }

        Collections.sort(entries);
        return entries;
    }

    /**
     * Calculates the material list directly from a SchematicData (no placement transforms).
     */
    public static List<MaterialListEntry> calculateFromSchematic(SchematicData schematic) {
        Map<Item, Integer> required = new LinkedHashMap<>();
        for (BlockState state : schematic.getBlocks().values()) {
            Item item = state.getBlock().asItem();
            if (item == Items.AIR) continue;
            required.merge(item, 1, Integer::sum);
        }

        List<MaterialListEntry> entries = new ArrayList<>(required.size());
        for (Map.Entry<Item, Integer> entry : required.entrySet()) {
            entries.add(new MaterialListEntry(entry.getKey(), entry.getValue()));
        }

        Collections.sort(entries);
        return entries;
    }

    /**
     * Returns the total number of blocks needed (excluding air/non-item blocks).
     */
    public static int getTotalBlocksNeeded(List<MaterialListEntry> entries) {
        int total = 0;
        for (MaterialListEntry entry : entries) {
            total += entry.getCountTotal();
        }
        return total;
    }

    /**
     * Returns the total number of blocks still missing.
     */
    public static int getTotalBlocksMissing(List<MaterialListEntry> entries) {
        int missing = 0;
        for (MaterialListEntry entry : entries) {
            missing += entry.getCountMissing();
        }
        return missing;
    }
}
