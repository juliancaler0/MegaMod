package com.ultra.megamod.feature.citizen.job;

import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.job.ai.AbstractEntityAIBasic;
import com.ultra.megamod.feature.citizen.job.ai.EntityAIWorkFisherman;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Fisherman job. Assigns the fisherman AI which finds water, casts a line,
 * waits for a catch, reels in fish, and deposits them.
 * <p>
 * Ported from MineColonies' JobFisherman. Maintains a list of known
 * fishing ponds for the citizen to cycle through.
 */
public class JobFisherman extends AbstractJob {

    private static final String TAG_PONDS = "Ponds";
    private static final String TAG_CURRENT_WATER = "CurrentWater";
    private static final String TAG_CURRENT_STAND = "CurrentStand";

    /**
     * A fishing pond: the water block to fish at + the land block to stand on.
     */
    public static class PondEntry {
        public final BlockPos water;
        public final BlockPos stand;

        public PondEntry(BlockPos water, BlockPos stand) {
            this.water = water;
            this.stand = stand;
        }
    }

    /** Known fishing ponds. */
    private final List<PondEntry> ponds = new ArrayList<>();

    /** Currently selected water position. */
    @Nullable
    private BlockPos currentWater = null;

    /** Currently selected standing position. */
    @Nullable
    private BlockPos currentStand = null;

    public JobFisherman(@NotNull MCEntityCitizen citizen) {
        super(citizen);
    }

    @Override
    @NotNull
    public CitizenJob getJobType() {
        return CitizenJob.FISHERMAN;
    }

    @Override
    @NotNull
    public AbstractEntityAIBasic createAI() {
        return new EntityAIWorkFisherman(this);
    }

    // ---- Pond management ----

    public List<PondEntry> getPonds() { return ponds; }

    public void addPond(BlockPos water, BlockPos stand) {
        ponds.add(new PondEntry(water, stand));
    }

    public void removePond(PondEntry pond) {
        ponds.remove(pond);
    }

    public void clearPonds() {
        ponds.clear();
    }

    @Nullable
    public BlockPos getCurrentWater() { return currentWater; }
    public void setCurrentWater(@Nullable BlockPos pos) { this.currentWater = pos; }

    @Nullable
    public BlockPos getCurrentStand() { return currentStand; }
    public void setCurrentStand(@Nullable BlockPos pos) { this.currentStand = pos; }

    // ---- Persistence ----

    @Override
    protected void saveJobData(@NotNull CompoundTag tag) {
        ListTag pondList = new ListTag();
        for (PondEntry pond : ponds) {
            CompoundTag pondTag = new CompoundTag();
            CompoundTag waterTag = new CompoundTag();
            waterTag.putInt("X", pond.water.getX());
            waterTag.putInt("Y", pond.water.getY());
            waterTag.putInt("Z", pond.water.getZ());
            pondTag.put("Water", waterTag);

            CompoundTag standTag = new CompoundTag();
            standTag.putInt("X", pond.stand.getX());
            standTag.putInt("Y", pond.stand.getY());
            standTag.putInt("Z", pond.stand.getZ());
            pondTag.put("Stand", standTag);

            pondList.add(pondTag);
        }
        tag.put(TAG_PONDS, pondList);

        if (currentWater != null) {
            CompoundTag cwTag = new CompoundTag();
            cwTag.putInt("X", currentWater.getX());
            cwTag.putInt("Y", currentWater.getY());
            cwTag.putInt("Z", currentWater.getZ());
            tag.put(TAG_CURRENT_WATER, cwTag);
        }
        if (currentStand != null) {
            CompoundTag csTag = new CompoundTag();
            csTag.putInt("X", currentStand.getX());
            csTag.putInt("Y", currentStand.getY());
            csTag.putInt("Z", currentStand.getZ());
            tag.put(TAG_CURRENT_STAND, csTag);
        }
    }

    @Override
    protected void loadJobData(@NotNull CompoundTag tag) {
        ponds.clear();
        if (tag.contains(TAG_PONDS)) {
            ListTag pondList = tag.getListOrEmpty(TAG_PONDS);
            for (int i = 0; i < pondList.size(); i++) {
                Tag entry = pondList.get(i);
                if (entry instanceof CompoundTag pondTag) {
                    CompoundTag waterTag = pondTag.getCompoundOrEmpty("Water");
                    CompoundTag standTag = pondTag.getCompoundOrEmpty("Stand");
                    BlockPos water = new BlockPos(
                            waterTag.getIntOr("X", 0),
                            waterTag.getIntOr("Y", 0),
                            waterTag.getIntOr("Z", 0));
                    BlockPos stand = new BlockPos(
                            standTag.getIntOr("X", 0),
                            standTag.getIntOr("Y", 0),
                            standTag.getIntOr("Z", 0));
                    ponds.add(new PondEntry(water, stand));
                }
            }
        }

        if (tag.contains(TAG_CURRENT_WATER)) {
            CompoundTag cwTag = tag.getCompoundOrEmpty(TAG_CURRENT_WATER);
            currentWater = new BlockPos(
                    cwTag.getIntOr("X", 0),
                    cwTag.getIntOr("Y", 0),
                    cwTag.getIntOr("Z", 0));
        }
        if (tag.contains(TAG_CURRENT_STAND)) {
            CompoundTag csTag = tag.getCompoundOrEmpty(TAG_CURRENT_STAND);
            currentStand = new BlockPos(
                    csTag.getIntOr("X", 0),
                    csTag.getIntOr("Y", 0),
                    csTag.getIntOr("Z", 0));
        }
    }
}
