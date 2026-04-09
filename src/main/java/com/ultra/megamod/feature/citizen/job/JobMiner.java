package com.ultra.megamod.feature.citizen.job;

import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.job.ai.AbstractEntityAIBasic;
import com.ultra.megamod.feature.citizen.job.ai.EntityAIWorkMiner;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * Miner job. Assigns the miner AI which digs shafts and branch mines,
 * collecting ores and depositing them at the mine building.
 * <p>
 * Ported from MineColonies' JobMiner.
 */
public class JobMiner extends AbstractJob {

    private static final String TAG_LADDER_POS = "LadderPos";
    private static final String TAG_SHAFT_START_Y = "ShaftStartY";
    private static final String TAG_CURRENT_LEVEL = "CurrentMineLevel";

    /** Position of the ladder shaft entrance. */
    private BlockPos ladderPos = null;

    /** Y level where the shaft starts (building level). */
    private int shaftStartY = -1;

    /** Current mining level depth (increases as the miner goes deeper). */
    private int currentMineLevel = 0;

    public JobMiner(@NotNull MCEntityCitizen citizen) {
        super(citizen);
    }

    @Override
    @NotNull
    public CitizenJob getJobType() {
        return CitizenJob.MINER;
    }

    @Override
    @NotNull
    public AbstractEntityAIBasic createAI() {
        return new EntityAIWorkMiner(this);
    }

    @Override
    public boolean allowsAvoidance() {
        return true;
    }

    // ---- Miner-specific accessors ----

    public BlockPos getLadderPos() { return ladderPos; }
    public void setLadderPos(BlockPos pos) { this.ladderPos = pos; }

    public int getShaftStartY() { return shaftStartY; }
    public void setShaftStartY(int y) { this.shaftStartY = y; }

    public int getCurrentMineLevel() { return currentMineLevel; }
    public void setCurrentMineLevel(int level) { this.currentMineLevel = level; }

    // ---- Persistence ----

    @Override
    protected void saveJobData(@NotNull CompoundTag tag) {
        if (ladderPos != null) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", ladderPos.getX());
            posTag.putInt("Y", ladderPos.getY());
            posTag.putInt("Z", ladderPos.getZ());
            tag.put(TAG_LADDER_POS, posTag);
        }
        tag.putInt(TAG_SHAFT_START_Y, shaftStartY);
        tag.putInt(TAG_CURRENT_LEVEL, currentMineLevel);
    }

    @Override
    protected void loadJobData(@NotNull CompoundTag tag) {
        if (tag.contains(TAG_LADDER_POS)) {
            CompoundTag posTag = tag.getCompoundOrEmpty(TAG_LADDER_POS);
            ladderPos = new BlockPos(
                    posTag.getIntOr("X", 0),
                    posTag.getIntOr("Y", 0),
                    posTag.getIntOr("Z", 0)
            );
        }
        shaftStartY = tag.getIntOr(TAG_SHAFT_START_Y, -1);
        currentMineLevel = tag.getIntOr(TAG_CURRENT_LEVEL, 0);
    }
}
