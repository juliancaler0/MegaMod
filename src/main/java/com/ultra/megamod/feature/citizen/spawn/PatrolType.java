package com.ultra.megamod.feature.citizen.spawn;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Patrol composition types for natural spawning.
 */
public enum PatrolType {
    // Combat patrols
    TINY(2, new CitizenJob[]{CitizenJob.RECRUIT, CitizenJob.RECRUIT}),
    SMALL(4, new CitizenJob[]{CitizenJob.RECRUIT, CitizenJob.RECRUIT, CitizenJob.BOWMAN, CitizenJob.SHIELDMAN}),
    MEDIUM(6, new CitizenJob[]{CitizenJob.RECRUIT, CitizenJob.RECRUIT, CitizenJob.BOWMAN, CitizenJob.SHIELDMAN, CitizenJob.CROSSBOWMAN, CitizenJob.SCOUT}),
    LARGE(8, new CitizenJob[]{CitizenJob.RECRUIT, CitizenJob.RECRUIT, CitizenJob.BOWMAN, CitizenJob.BOWMAN, CitizenJob.SHIELDMAN, CitizenJob.SHIELDMAN, CitizenJob.CROSSBOWMAN, CitizenJob.COMMANDER}),
    HUGE(10, new CitizenJob[]{CitizenJob.RECRUIT, CitizenJob.RECRUIT, CitizenJob.RECRUIT, CitizenJob.BOWMAN, CitizenJob.BOWMAN, CitizenJob.SHIELDMAN, CitizenJob.SHIELDMAN, CitizenJob.CROSSBOWMAN, CitizenJob.COMMANDER, CitizenJob.CAPTAIN}),
    ROAD(3, new CitizenJob[]{CitizenJob.MERCHANT, CitizenJob.RECRUIT, CitizenJob.RECRUIT}),
    CARAVAN(5, new CitizenJob[]{CitizenJob.MERCHANT, CitizenJob.RECRUIT, CitizenJob.RECRUIT, CitizenJob.BOWMAN, CitizenJob.SHIELDMAN}),
    HERALD(3, new CitizenJob[]{CitizenJob.RECRUIT, CitizenJob.RECRUIT}), // Herald + 2 recruit escorts; Herald spawned separately

    // Worker patrols - civilians looking for work
    WANDERER(1, new CitizenJob[]{CitizenJob.FARMER}), // Single wandering worker (job randomized at spawn)
    SETTLERS(3, new CitizenJob[]{CitizenJob.FARMER, CitizenJob.MINER, CitizenJob.LUMBERJACK}),
    FARMHANDS(3, new CitizenJob[]{CitizenJob.FARMER, CitizenJob.SHEPHERD, CitizenJob.CHICKEN_FARMER}),
    MINERS(2, new CitizenJob[]{CitizenJob.MINER, CitizenJob.MINER}),
    RANCHERS(3, new CitizenJob[]{CitizenJob.CATTLE_FARMER, CitizenJob.SWINEHERD, CitizenJob.SHEPHERD}),
    LABORERS(4, new CitizenJob[]{CitizenJob.FARMER, CitizenJob.LUMBERJACK, CitizenJob.FISHERMAN, CitizenJob.MINER}),
    VILLAGERS(5, new CitizenJob[]{CitizenJob.FARMER, CitizenJob.MINER, CitizenJob.LUMBERJACK, CitizenJob.FISHERMAN, CitizenJob.SHEPHERD});

    private final int size;
    private final CitizenJob[] composition;

    PatrolType(int size, CitizenJob[] composition) {
        this.size = size;
        this.composition = composition;
    }

    public int getSize() { return size; }
    public CitizenJob[] getComposition() { return composition; }
}
