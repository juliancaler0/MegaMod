package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Handles citizen disease tracking for the MC citizen.
 * Ported from MineColonies' ICitizenDiseaseHandler concept.
 */
public class CitizenDiseaseHandler implements ICitizenDiseaseHandler {

    private static final String TAG_DISEASE = "Disease";
    private static final String TAG_DISEASE_TICKS = "DiseaseTicks";
    private static final int DISEASE_DURATION = 24000; // 20 minutes

    @Nullable
    private String currentDisease;
    private int diseaseTicksRemaining = 0;

    @Override
    public void update(int tickRate) {
        if (diseaseTicksRemaining > 0) {
            diseaseTicksRemaining -= tickRate;
            if (diseaseTicksRemaining <= 0) {
                cure();
            }
        }
    }

    @Override
    public boolean isSick() {
        return currentDisease != null && diseaseTicksRemaining > 0;
    }

    @Override
    public void write(CompoundTag compound) {
        if (currentDisease != null) {
            compound.putString(TAG_DISEASE, currentDisease);
            compound.putInt(TAG_DISEASE_TICKS, diseaseTicksRemaining);
        }
    }

    @Override
    public void read(CompoundTag compound) {
        if (compound.contains(TAG_DISEASE)) {
            currentDisease = compound.getStringOr(TAG_DISEASE, null);
            diseaseTicksRemaining = compound.getIntOr(TAG_DISEASE_TICKS, 0);
        }
    }

    @Override
    @Nullable
    public String getDiseaseName() {
        return currentDisease;
    }

    @Override
    public void cure() {
        currentDisease = null;
        diseaseTicksRemaining = 0;
    }

    @Override
    public boolean setDisease(String diseaseName) {
        if (diseaseName == null || isSick()) {
            return false;
        }
        this.currentDisease = diseaseName;
        this.diseaseTicksRemaining = DISEASE_DURATION;
        return true;
    }
}
