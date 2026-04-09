package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles citizen mourning tracking for the MC citizen.
 * Ported from MineColonies' ICitizenMournHandler concept.
 */
public class CitizenMournHandler implements ICitizenMournHandler {

    private static final String TAG_DECEASED = "DeceasedCitizens";

    private final Set<String> deceasedCitizens = new HashSet<>();
    private boolean mourning = false;

    @Override
    public void read(CompoundTag compound) {
        deceasedCitizens.clear();
        if (compound.contains(TAG_DECEASED)) {
            ListTag list = compound.getListOrEmpty(TAG_DECEASED);
            for (int i = 0; i < list.size(); i++) {
                Tag tag = list.get(i);
                if (tag instanceof StringTag st) {
                    deceasedCitizens.add(st.value());
                }
            }
        }
        mourning = compound.getBooleanOr("Mourning", false);
    }

    @Override
    public void write(CompoundTag compound) {
        ListTag list = new ListTag();
        for (String name : deceasedCitizens) {
            list.add(StringTag.valueOf(name));
        }
        compound.put(TAG_DECEASED, list);
        compound.putBoolean("Mourning", mourning);
    }

    @Override
    public void addDeceasedCitizen(String name) {
        deceasedCitizens.add(name);
    }

    @Override
    public Set<String> getDeceasedCitizens() {
        return Set.copyOf(deceasedCitizens);
    }

    @Override
    public void removeDeceasedCitizen(String name) {
        deceasedCitizens.remove(name);
    }

    @Override
    public void clearDeceasedCitizen() {
        deceasedCitizens.clear();
        mourning = false;
    }

    @Override
    public boolean shouldMourn() {
        return !deceasedCitizens.isEmpty();
    }

    @Override
    public boolean isMourning() {
        return mourning;
    }

    @Override
    public void setMourning(boolean mourn) {
        this.mourning = mourn;
    }
}
