package com.ultra.megamod.feature.citizen.job;

import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.job.ai.AbstractEntityAIBasic;
import com.ultra.megamod.feature.citizen.job.ai.EntityAIWorkFarmer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Farmer job. Assigns the farmer AI which hoes ground, plants seeds,
 * waits for crop growth, harvests, and deposits crops.
 * <p>
 * Ported from MineColonies' JobFarmer. Manages a list of assigned farm fields.
 */
public class JobFarmer extends AbstractJob {

    private static final String TAG_FIELDS = "FarmFields";
    private static final String TAG_CURRENT_FIELD = "CurrentField";

    /** List of assigned farm field positions (scarecrow/marker positions). */
    private final List<BlockPos> assignedFields = new ArrayList<>();

    /** Index of the field currently being worked. */
    private int currentFieldIndex = -1;

    public JobFarmer(@NotNull MCEntityCitizen citizen) {
        super(citizen);
    }

    @Override
    @NotNull
    public CitizenJob getJobType() {
        return CitizenJob.FARMER;
    }

    @Override
    @NotNull
    public AbstractEntityAIBasic createAI() {
        return new EntityAIWorkFarmer(this);
    }

    // ---- Farm field management ----

    public List<BlockPos> getAssignedFields() {
        return assignedFields;
    }

    public void addField(@NotNull BlockPos fieldPos) {
        if (!assignedFields.contains(fieldPos)) {
            assignedFields.add(fieldPos);
        }
    }

    public void removeField(@NotNull BlockPos fieldPos) {
        assignedFields.remove(fieldPos);
    }

    public int getCurrentFieldIndex() { return currentFieldIndex; }
    public void setCurrentFieldIndex(int index) { this.currentFieldIndex = index; }

    /**
     * Get the position of the field currently being worked.
     *
     * @return the current field pos, or null if none
     */
    @Nullable
    public BlockPos getCurrentFieldPos() {
        if (currentFieldIndex >= 0 && currentFieldIndex < assignedFields.size()) {
            return assignedFields.get(currentFieldIndex);
        }
        return null;
    }

    /**
     * Cycle to the next field.
     */
    public void nextField() {
        if (assignedFields.isEmpty()) {
            currentFieldIndex = -1;
        } else {
            currentFieldIndex = (currentFieldIndex + 1) % assignedFields.size();
        }
    }

    // ---- Persistence ----

    @Override
    protected void saveJobData(@NotNull CompoundTag tag) {
        ListTag fieldList = new ListTag();
        for (BlockPos pos : assignedFields) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", pos.getX());
            posTag.putInt("Y", pos.getY());
            posTag.putInt("Z", pos.getZ());
            fieldList.add(posTag);
        }
        tag.put(TAG_FIELDS, fieldList);
        tag.putInt(TAG_CURRENT_FIELD, currentFieldIndex);
    }

    @Override
    protected void loadJobData(@NotNull CompoundTag tag) {
        assignedFields.clear();
        if (tag.contains(TAG_FIELDS)) {
            ListTag fieldList = tag.getListOrEmpty(TAG_FIELDS);
            for (int i = 0; i < fieldList.size(); i++) {
                Tag entry = fieldList.get(i);
                if (entry instanceof CompoundTag posTag) {
                    assignedFields.add(new BlockPos(
                            posTag.getIntOr("X", 0),
                            posTag.getIntOr("Y", 0),
                            posTag.getIntOr("Z", 0)
                    ));
                }
            }
        }
        currentFieldIndex = tag.getIntOr(TAG_CURRENT_FIELD, -1);
    }
}
