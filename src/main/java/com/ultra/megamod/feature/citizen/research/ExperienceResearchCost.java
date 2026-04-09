package com.ultra.megamod.feature.citizen.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Research cost requiring the player to spend experience levels.
 */
public class ExperienceResearchCost implements IResearchCost {

    private final int levels;

    public ExperienceResearchCost(int levels) {
        this.levels = Math.max(1, levels);
    }

    public int getLevels() {
        return levels;
    }

    @Override
    public String getType() {
        return "experience";
    }

    @Override
    public boolean canAfford(Player player) {
        return player.experienceLevel >= levels;
    }

    @Override
    public void deduct(Player player) {
        player.giveExperienceLevels(-levels);
    }

    @Override
    public Component getDisplayText() {
        return Component.literal(levels + " XP Levels");
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("CostType", "experience");
        tag.putInt("Levels", levels);
        return tag;
    }

    public static ExperienceResearchCost fromNbt(CompoundTag tag) {
        return new ExperienceResearchCost(tag.getIntOr("Levels", 1));
    }
}
