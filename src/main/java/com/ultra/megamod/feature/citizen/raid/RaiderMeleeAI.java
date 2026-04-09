package com.ultra.megamod.feature.citizen.raid;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * Melee combat goal for raiders.
 * Targets players and citizen entities with adjustable attack speed based on culture.
 */
public class RaiderMeleeAI extends MeleeAttackGoal {

    private final double cultureSpeedMult;

    /**
     * @param mob               the raider entity
     * @param speedModifier     base movement speed toward target
     * @param followIfNotSeen   whether to follow even without line of sight
     * @param cultureSpeedMult  multiplier applied to attack intervals (lower = faster)
     */
    public RaiderMeleeAI(PathfinderMob mob, double speedModifier, boolean followIfNotSeen, double cultureSpeedMult) {
        super(mob, speedModifier, followIfNotSeen);
        this.cultureSpeedMult = cultureSpeedMult;
    }

    public RaiderMeleeAI(PathfinderMob mob, double speedModifier, boolean followIfNotSeen) {
        this(mob, speedModifier, followIfNotSeen, 1.0);
    }

    @Override
    protected int getAttackInterval() {
        // Base interval is 20 ticks. Culture multiplier adjusts it.
        return Math.max(10, (int) (20 * cultureSpeedMult));
    }
}
