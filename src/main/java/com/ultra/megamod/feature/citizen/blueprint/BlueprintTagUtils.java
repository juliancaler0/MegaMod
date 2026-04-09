package com.ultra.megamod.feature.citizen.blueprint;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

/**
 * Tag and substitution utilities for blueprints in the colony system.
 * Provides constants for known tag keys, substitution block detection,
 * and utilities for reading tagged positions from blueprint anchor data.
 */
public final class BlueprintTagUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    private BlueprintTagUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ========== NBT Tag key constants ==========

    /** Key for the blueprint data compound stored in anchor block tile entities. */
    public static final String TAG_BLUEPRINTDATA = "BlueprintData";

    /** Key for the schematic name within blueprint data. */
    public static final String TAG_SCHEMATIC_NAME = "SchematicName";

    /** Key for the blueprint file path. */
    public static final String TAG_BLUEPRINT_PATH = "BlueprintPath";

    /** Key for corner 1 of the blueprint bounds (relative to anchor). */
    public static final String TAG_CORNER_ONE = "Corner1";

    /** Key for corner 2 of the blueprint bounds (relative to anchor). */
    public static final String TAG_CORNER_TWO = "Corner2";

    /** Key for the tag position map within blueprint data. */
    public static final String TAG_POS_MAP = "TagPosMap";

    /** Tag name marking a position as the ground level reference. */
    public static final String GROUNDLEVEL_TAG = "groundlevel";

    /** Tag name marking a blueprint as invisible in build tool lists. */
    public static final String INVISIBLE_TAG = "invisible";

    // ========== Substitution block names ==========

    /** The substitution block placeholder (replaced with the ground block during placement). */
    public static final String SUBSTITUTION_BLOCK = "megamod:placeholder_block";

    /** The solid substitution block (like substitution but must be a solid block). */
    public static final String SOLID_SUBSTITUTION_BLOCK = "megamod:solid_placeholder_block";

    /** The fluid substitution block (for water/lava placeholders). */
    public static final String FLUID_SUBSTITUTION_BLOCK = "megamod:fluid_placeholder_block";

    // ========== Substitution detection ==========

    /**
     * Checks if the given block state represents a substitution placeholder.
     * Substitution blocks are replaced with appropriate blocks during placement
     * (e.g., the dominant ground block for a SUBSTITUTION, water for FLUID_SUBSTITUTION).
     *
     * @param state the block state to check
     * @return true if this is a substitution placeholder
     */
    public static boolean isSubstitutionBlock(BlockState state) {
        if (state == null) return false;
        String blockName = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        return SUBSTITUTION_BLOCK.equals(blockName)
                || SOLID_SUBSTITUTION_BLOCK.equals(blockName)
                || FLUID_SUBSTITUTION_BLOCK.equals(blockName);
    }

    /**
     * Checks if the given block state is a solid substitution placeholder.
     */
    public static boolean isSolidSubstitution(BlockState state) {
        if (state == null) return false;
        String blockName = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        return SOLID_SUBSTITUTION_BLOCK.equals(blockName);
    }

    /**
     * Checks if the given block state is a fluid substitution placeholder.
     */
    public static boolean isFluidSubstitution(BlockState state) {
        if (state == null) return false;
        String blockName = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        return FLUID_SUBSTITUTION_BLOCK.equals(blockName);
    }

    // ========== Tag map reading ==========

    /**
     * Reads the tag position map from a blueprint's anchor block.
     * The tag map associates block positions (relative to the anchor) with lists of tag strings.
     *
     * @param blueprint the blueprint to read tags from
     * @return the tag map, keyed by position relative to the anchor
     */
    public static Map<BlockPos, List<String>> getBlueprintTags(Blueprint blueprint) {
        BlockPos anchorPos = blueprint.getPrimaryBlockOffset();
        Map<BlockPos, BlockInfo> infoMap = blueprint.getBlockInfoAsMap();
        BlockInfo anchorInfo = infoMap.get(anchorPos);

        if (anchorInfo == null || !anchorInfo.hasTileEntityData()) {
            return new HashMap<>();
        }

        CompoundTag nbt = anchorInfo.tileEntityData();
        CompoundTag blueprintData = nbt.getCompoundOrEmpty(TAG_BLUEPRINTDATA);
        return readTagPosMapFrom(blueprintData);
    }

    /**
     * Reads a tag position map from a CompoundTag.
     * Format: the compound contains keys which are serialized BlockPos ("x,y,z"),
     * each mapping to a ListTag of StringTags.
     *
     * @param compound the compound to read from
     * @return the deserialized map
     */
    public static Map<BlockPos, List<String>> readTagPosMapFrom(CompoundTag compound) {
        Map<BlockPos, List<String>> result = new HashMap<>();

        CompoundTag posMapTag = compound.getCompoundOrEmpty(TAG_POS_MAP);
        for (String key : posMapTag.keySet()) {
            BlockPos pos = parsePosKey(key);
            if (pos != null) {
                ListTag tagList = posMapTag.getListOrEmpty(key);
                List<String> tags = new ArrayList<>();
                for (int i = 0; i < tagList.size(); i++) {
                    tags.add(tagList.getString(i).orElse(""));
                }
                if (!tags.isEmpty()) {
                    result.put(pos, tags);
                }
            }
        }

        return result;
    }

    /**
     * Writes a tag position map to a CompoundTag.
     *
     * @param compound  the compound to write into
     * @param tagPosMap the tag map to serialize
     */
    public static void writeTagPosMapTo(CompoundTag compound, Map<BlockPos, List<String>> tagPosMap) {
        CompoundTag posMapTag = new CompoundTag();
        for (Map.Entry<BlockPos, List<String>> entry : tagPosMap.entrySet()) {
            String key = posToKey(entry.getKey());
            ListTag tagList = new ListTag();
            for (String tag : entry.getValue()) {
                tagList.add(net.minecraft.nbt.StringTag.valueOf(tag));
            }
            posMapTag.put(key, tagList);
        }
        compound.put(TAG_POS_MAP, posMapTag);
    }

    /**
     * Finds the first position in the blueprint that has the specified tag.
     *
     * @param blueprint the blueprint to search
     * @param tagName   the tag to find
     * @return the position, or null if not found
     */
    @Nullable
    public static BlockPos getFirstPosForTag(Blueprint blueprint, String tagName) {
        Map<BlockPos, List<String>> tagPosMap = getBlueprintTags(blueprint);
        for (Map.Entry<BlockPos, List<String>> entry : tagPosMap.entrySet()) {
            if (entry.getValue().contains(tagName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Returns the number of ground levels in the blueprint.
     * Ground levels are layers at the bottom that are considered terrain.
     *
     * @param blueprint          the blueprint to query
     * @param defaultGroundLevels the default if no ground tag is found
     * @return the number of ground levels
     */
    public static int getNumberOfGroundLevels(Blueprint blueprint, int defaultGroundLevels) {
        BlockPos groundLevelPos = getFirstPosForTag(blueprint, GROUNDLEVEL_TAG);
        if (groundLevelPos != null) {
            return blueprint.getPrimaryBlockOffset().getY() + groundLevelPos.getY() + 1;
        }
        return defaultGroundLevels;
    }

    /**
     * Returns the vertical offset from the anchor to the ground level.
     *
     * @param blueprint           the blueprint to query
     * @param defaultGroundOffset the default if no ground tag is found
     * @return the height offset (positive means ground is below anchor)
     */
    public static int getGroundAnchorOffset(Blueprint blueprint, int defaultGroundOffset) {
        BlockPos groundLevelPos = getFirstPosForTag(blueprint, GROUNDLEVEL_TAG);
        if (groundLevelPos != null) {
            return -groundLevelPos.getY();
        }
        return defaultGroundOffset;
    }

    /**
     * Checks if the blueprint should be hidden from normal build tool lists.
     */
    public static boolean isInvisible(Blueprint blueprint) {
        List<String> anchorTags = getBlueprintTags(blueprint)
                .getOrDefault(BlockPos.ZERO, Collections.emptyList());
        return anchorTags.contains(INVISIBLE_TAG);
    }

    // ========== Internal helpers ==========

    /**
     * Parses a position key in "x,y,z" format.
     */
    @Nullable
    private static BlockPos parsePosKey(String key) {
        try {
            String[] parts = key.split(",");
            if (parts.length == 3) {
                return new BlockPos(
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim()));
            }
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid position key in tag map: {}", key);
        }
        return null;
    }

    /**
     * Converts a BlockPos to a "x,y,z" string key.
     */
    private static String posToKey(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
