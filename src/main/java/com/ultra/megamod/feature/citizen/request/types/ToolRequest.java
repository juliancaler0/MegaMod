package com.ultra.megamod.feature.citizen.request.types;

import com.ultra.megamod.feature.citizen.request.IRequestable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

/**
 * Request for a tool of a specific type and minimum tier level.
 * Matches pickaxes, axes, shovels, hoes, and swords by item tag.
 * Tier comparison uses a simple heuristic based on the item's registry path
 * (e.g., "diamond_pickaxe" contains "diamond").
 */
public class ToolRequest implements IRequestable {

    private static final String[] TIER_NAMES = {"wooden", "stone", "iron", "diamond", "netherite"};

    private final String toolType;
    private final int minLevel;

    /**
     * Creates a tool request.
     *
     * @param toolType the tool type string: "pickaxe", "axe", "shovel", "hoe", "sword"
     * @param minLevel the minimum tier level (0=wood, 1=stone, 2=iron, 3=diamond, 4=netherite)
     */
    public ToolRequest(String toolType, int minLevel) {
        this.toolType = toolType.toLowerCase();
        this.minLevel = minLevel;
    }

    public String getToolType() {
        return toolType;
    }

    public int getMinLevel() {
        return minLevel;
    }

    @Override
    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) return false;

        boolean typeMatch = switch (toolType) {
            case "pickaxe" -> stack.is(ItemTags.PICKAXES);
            case "axe" -> stack.is(ItemTags.AXES);
            case "shovel" -> stack.is(ItemTags.SHOVELS);
            case "hoe" -> stack.is(ItemTags.HOES);
            case "sword" -> stack.is(ItemTags.SWORDS);
            default -> false;
        };

        if (!typeMatch) return false;

        // Determine the tool's tier from its registry name
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = itemId.getPath();
        int toolTier = getTierFromPath(path);
        return toolTier >= minLevel;
    }

    /**
     * Estimates the tier level from the item registry path.
     * Returns the index into TIER_NAMES (0-4), or 0 if unknown.
     */
    private static int getTierFromPath(String path) {
        for (int i = TIER_NAMES.length - 1; i >= 0; i--) {
            if (path.contains(TIER_NAMES[i])) {
                return i;
            }
        }
        // Default to 0 for modded or unknown tools
        return 0;
    }

    @Override
    public String getDescription() {
        String levelName = switch (minLevel) {
            case 0 -> "Wood";
            case 1 -> "Stone";
            case 2 -> "Iron";
            case 3 -> "Diamond";
            case 4 -> "Netherite";
            default -> "Level " + minLevel;
        };
        return levelName + "+ " + toolType;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "tool");
        tag.putString("toolType", toolType);
        tag.putInt("minLevel", minLevel);
        return tag;
    }

    public static ToolRequest fromNbt(CompoundTag tag) {
        String toolType = tag.getStringOr("toolType", "pickaxe");
        int minLevel = tag.getIntOr("minLevel", 0);
        return new ToolRequest(toolType, minLevel);
    }
}
