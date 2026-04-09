package com.ultra.megamod.feature.citizen.research;

import net.minecraft.nbt.CompoundTag;

/**
 * Standard implementation of {@link IResearchEffect}.
 * Supports three effect types:
 * <ul>
 *   <li>{@link EffectType#MULTIPLIER} — multiplies a colony stat (e.g., worker_speed * 1.15)</li>
 *   <li>{@link EffectType#UNLOCK} — unlocks a feature or recipe (e.g., "plate_armor")</li>
 *   <li>{@link EffectType#MODIFIER} — adds/subtracts a flat amount to a stat</li>
 * </ul>
 */
public class ResearchEffect implements IResearchEffect {

    public enum EffectType {
        MULTIPLIER,
        UNLOCK,
        MODIFIER;

        public static EffectType fromString(String s) {
            try {
                return valueOf(s);
            } catch (Exception e) {
                return MODIFIER;
            }
        }
    }

    private final String id;
    private final EffectType type;
    private final String targetStat;
    private final double value;
    private final String description;

    public ResearchEffect(String id, EffectType type, String targetStat, double value, String description) {
        this.id = id;
        this.type = type;
        this.targetStat = targetStat;
        this.value = value;
        this.description = description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public EffectType getType() {
        return type;
    }

    public String getTargetStat() {
        return targetStat;
    }

    public double getValue() {
        return value;
    }

    @Override
    public void apply(ResearchEffectManager manager) {
        switch (type) {
            case MULTIPLIER -> manager.applyMultiplier(targetStat, value);
            case UNLOCK -> manager.unlock(targetStat);
            case MODIFIER -> manager.applyModifier(targetStat, value);
        }
    }

    @Override
    public void remove(ResearchEffectManager manager) {
        switch (type) {
            case MULTIPLIER -> manager.removeMultiplier(targetStat, value);
            case UNLOCK -> manager.revoke(targetStat);
            case MODIFIER -> manager.removeModifier(targetStat, value);
        }
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id);
        tag.putString("Type", type.name());
        tag.putString("TargetStat", targetStat);
        tag.putDouble("Value", value);
        tag.putString("Description", description);
        return tag;
    }

    public static ResearchEffect fromNbt(CompoundTag tag) {
        return new ResearchEffect(
                tag.getStringOr("Id", ""),
                EffectType.fromString(tag.getStringOr("Type", "MODIFIER")),
                tag.getStringOr("TargetStat", ""),
                tag.getDoubleOr("Value", 0.0),
                tag.getStringOr("Description", "")
        );
    }
}
