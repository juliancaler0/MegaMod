package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.blueprint.BlueprintTagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Block entity for the Tag Anchor block.
 * Stores a tag position map ({@code Map<BlockPos, List<String>>}) and an optional
 * "absorbed" block state string that replaces this block when the schematic is built.
 */
public class TileEntityTagAnchor extends BlockEntity {

    private static final String TAG_MAP_KEY = "TagPosMap";
    private static final String ABSORBED_KEY = "AbsorbedBlock";

    private Map<BlockPos, List<String>> tagPosMap = new HashMap<>();
    @Nullable
    private String absorbedBlock = null;

    public TileEntityTagAnchor(BlockPos pos, BlockState state) {
        super(ColonyBlockRegistry.TAG_ANCHOR_BE.get(), pos, state);
    }

    // ==================== Tag Position Map ====================

    public Map<BlockPos, List<String>> getTagPosMap() {
        return tagPosMap;
    }

    public void setTagPosMap(Map<BlockPos, List<String>> map) {
        this.tagPosMap = map;
        setChanged();
    }

    public void addTag(BlockPos pos, String tag) {
        tagPosMap.computeIfAbsent(pos, k -> new ArrayList<>()).add(tag);
        setChanged();
    }

    public void removeTag(BlockPos pos, String tag) {
        List<String> tags = tagPosMap.get(pos);
        if (tags != null) {
            tags.remove(tag);
            if (tags.isEmpty()) {
                tagPosMap.remove(pos);
            }
            setChanged();
        }
    }

    // ==================== Absorbed Block ====================

    @Nullable
    public String getAbsorbedBlock() {
        return absorbedBlock;
    }

    public void setAbsorbedBlock(@Nullable String blockStateString) {
        this.absorbedBlock = blockStateString;
        setChanged();
    }

    // ==================== NBT ====================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        // Save tag position map using BlueprintTagUtils format
        CompoundTag mapTag = new CompoundTag();
        BlueprintTagUtils.writeTagPosMapTo(mapTag, tagPosMap);
        output.store(TAG_MAP_KEY, CompoundTag.CODEC, mapTag);

        if (absorbedBlock != null && !absorbedBlock.isEmpty()) {
            output.putString(ABSORBED_KEY, absorbedBlock);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        // Load tag position map
        tagPosMap = input.read(TAG_MAP_KEY, CompoundTag.CODEC)
            .map(BlueprintTagUtils::readTagPosMapFrom)
            .orElseGet(HashMap::new);

        absorbedBlock = input.getStringOr(ABSORBED_KEY, null);
    }
}
