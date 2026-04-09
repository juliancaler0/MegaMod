package com.ultra.megamod.feature.citizen.job;

import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.job.ai.AbstractEntityAIBasic;
import com.ultra.megamod.feature.citizen.job.ai.EntityAIStructureBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builder job. Assigns the builder AI which picks up work orders,
 * goes to build sites, places blocks from blueprints, and requests
 * missing materials.
 * <p>
 * Ported from MineColonies' JobBuilder.
 */
public class JobBuilder extends AbstractJob {

    private static final String TAG_WORK_ORDER_POS = "WorkOrderPos";
    private static final String TAG_WORK_ORDER_TYPE = "WorkOrderType";

    /** Position of the current build work order. */
    @Nullable
    private BlockPos workOrderPos = null;

    /** Type of the current work order (build, repair, upgrade, remove). */
    private String workOrderType = "";

    public JobBuilder(@NotNull MCEntityCitizen citizen) {
        super(citizen);
    }

    @Override
    @NotNull
    public CitizenJob getJobType() {
        return CitizenJob.BUILDER;
    }

    @Override
    @NotNull
    public AbstractEntityAIBasic createAI() {
        return new EntityAIStructureBuilder(this);
    }

    // ---- Builder-specific accessors ----

    @Nullable
    public BlockPos getWorkOrderPos() { return workOrderPos; }
    public void setWorkOrderPos(@Nullable BlockPos pos) { this.workOrderPos = pos; }

    public String getWorkOrderType() { return workOrderType; }
    public void setWorkOrderType(String type) { this.workOrderType = type; }

    public boolean hasWorkOrder() { return workOrderPos != null; }

    public void clearWorkOrder() {
        workOrderPos = null;
        workOrderType = "";
    }

    // ---- Persistence ----

    @Override
    protected void saveJobData(@NotNull CompoundTag tag) {
        if (workOrderPos != null) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", workOrderPos.getX());
            posTag.putInt("Y", workOrderPos.getY());
            posTag.putInt("Z", workOrderPos.getZ());
            tag.put(TAG_WORK_ORDER_POS, posTag);
        }
        tag.putString(TAG_WORK_ORDER_TYPE, workOrderType);
    }

    @Override
    protected void loadJobData(@NotNull CompoundTag tag) {
        if (tag.contains(TAG_WORK_ORDER_POS)) {
            CompoundTag posTag = tag.getCompoundOrEmpty(TAG_WORK_ORDER_POS);
            workOrderPos = new BlockPos(
                    posTag.getIntOr("X", 0),
                    posTag.getIntOr("Y", 0),
                    posTag.getIntOr("Z", 0)
            );
        }
        workOrderType = tag.getStringOr(TAG_WORK_ORDER_TYPE, "");
    }
}
