package com.ultra.megamod.feature.citizen.data;

import java.util.EnumMap;
import java.util.Map;

/**
 * Maps every {@link CitizenJob} to its primary and secondary {@link CitizenSkill}.
 * Based on MegaColonies job-skill assignments. Each job's primary skill receives 100%
 * of earned XP, and the secondary receives 50%.
 *
 * <p>When a citizen works, XP is distributed through {@link CitizenSkillData#addExperience}
 * using these mappings.</p>
 */
public final class JobSkillMap {

    private JobSkillMap() {}

    private static final Map<CitizenJob, CitizenSkill[]> JOB_SKILLS = new EnumMap<>(CitizenJob.class);

    static {
        // =====================================================================
        //  Resource Production Workers
        // =====================================================================
        map(CitizenJob.FARMER,          CitizenSkill.STAMINA,      CitizenSkill.ATHLETICS);
        map(CitizenJob.MINER,           CitizenSkill.STRENGTH,     CitizenSkill.STAMINA);
        map(CitizenJob.LUMBERJACK,      CitizenSkill.STRENGTH,     CitizenSkill.FOCUS);
        map(CitizenJob.FISHERMAN,       CitizenSkill.FOCUS,        CitizenSkill.AGILITY);
        map(CitizenJob.PLANTER,         CitizenSkill.AGILITY,      CitizenSkill.DEXTERITY);
        map(CitizenJob.NETHER_MINER,    CitizenSkill.ADAPTABILITY, CitizenSkill.STRENGTH);
        map(CitizenJob.QUARRIER,        CitizenSkill.STRENGTH,     CitizenSkill.ATHLETICS);

        // =====================================================================
        //  Animal Husbandry Workers
        // =====================================================================
        map(CitizenJob.SHEPHERD,        CitizenSkill.FOCUS,        CitizenSkill.STRENGTH);
        map(CitizenJob.CATTLE_FARMER,   CitizenSkill.ATHLETICS,    CitizenSkill.STAMINA);
        map(CitizenJob.CHICKEN_FARMER,  CitizenSkill.ADAPTABILITY, CitizenSkill.AGILITY);
        map(CitizenJob.SWINEHERD,       CitizenSkill.STRENGTH,     CitizenSkill.ATHLETICS);
        map(CitizenJob.RABBIT_FARMER,   CitizenSkill.AGILITY,      CitizenSkill.ATHLETICS);
        map(CitizenJob.BEEKEEPER,       CitizenSkill.DEXTERITY,    CitizenSkill.ADAPTABILITY);
        map(CitizenJob.GOAT_FARMER,     CitizenSkill.ATHLETICS,    CitizenSkill.STRENGTH);

        // =====================================================================
        //  Crafting Workers
        // =====================================================================
        map(CitizenJob.BAKER,           CitizenSkill.KNOWLEDGE,    CitizenSkill.DEXTERITY);
        map(CitizenJob.BLACKSMITH,      CitizenSkill.STRENGTH,     CitizenSkill.FOCUS);
        map(CitizenJob.STONEMASON,      CitizenSkill.CREATIVITY,   CitizenSkill.DEXTERITY);
        map(CitizenJob.SAWMILL,         CitizenSkill.KNOWLEDGE,    CitizenSkill.DEXTERITY);
        map(CitizenJob.SMELTER,         CitizenSkill.ATHLETICS,    CitizenSkill.STRENGTH);
        map(CitizenJob.STONE_SMELTER,   CitizenSkill.ATHLETICS,    CitizenSkill.DEXTERITY);
        map(CitizenJob.CRUSHER,         CitizenSkill.STAMINA,      CitizenSkill.STRENGTH);
        map(CitizenJob.SIFTER,          CitizenSkill.FOCUS,        CitizenSkill.STRENGTH);
        map(CitizenJob.COOK,            CitizenSkill.ADAPTABILITY, CitizenSkill.KNOWLEDGE);
        map(CitizenJob.CHEF,            CitizenSkill.CREATIVITY,   CitizenSkill.KNOWLEDGE);
        map(CitizenJob.DYE_WORKER,      CitizenSkill.CREATIVITY,   CitizenSkill.DEXTERITY);
        map(CitizenJob.FLETCHER,        CitizenSkill.DEXTERITY,    CitizenSkill.CREATIVITY);
        map(CitizenJob.GLASSBLOWER,     CitizenSkill.CREATIVITY,   CitizenSkill.FOCUS);
        map(CitizenJob.CONCRETE_MIXER,  CitizenSkill.STAMINA,      CitizenSkill.DEXTERITY);
        map(CitizenJob.COMPOSTER,       CitizenSkill.STAMINA,      CitizenSkill.ATHLETICS);
        map(CitizenJob.FLORIST,         CitizenSkill.DEXTERITY,    CitizenSkill.CREATIVITY);
        map(CitizenJob.MECHANIC,        CitizenSkill.KNOWLEDGE,    CitizenSkill.AGILITY);
        map(CitizenJob.ALCHEMIST_CITIZEN, CitizenSkill.MANA,       CitizenSkill.KNOWLEDGE);
        map(CitizenJob.ENCHANTER,       CitizenSkill.MANA,         CitizenSkill.KNOWLEDGE);

        // =====================================================================
        //  Military Workers
        // =====================================================================
        map(CitizenJob.KNIGHT,          CitizenSkill.ADAPTABILITY, CitizenSkill.STAMINA);
        map(CitizenJob.ARCHER_TRAINING, CitizenSkill.AGILITY,      CitizenSkill.ADAPTABILITY);
        map(CitizenJob.COMBAT_TRAINING, CitizenSkill.ATHLETICS,    CitizenSkill.STAMINA);
        map(CitizenJob.DRUID,           CitizenSkill.MANA,         CitizenSkill.FOCUS);

        // =====================================================================
        //  Education Workers
        // =====================================================================
        map(CitizenJob.PUPIL,           CitizenSkill.INTELLIGENCE, CitizenSkill.KNOWLEDGE);
        map(CitizenJob.TEACHER,         CitizenSkill.KNOWLEDGE,    CitizenSkill.MANA);
        map(CitizenJob.RESEARCHER,      CitizenSkill.KNOWLEDGE,    CitizenSkill.MANA);

        // =====================================================================
        //  Service Workers
        // =====================================================================
        map(CitizenJob.HEALER,          CitizenSkill.MANA,         CitizenSkill.KNOWLEDGE);
        map(CitizenJob.UNDERTAKER,      CitizenSkill.STRENGTH,     CitizenSkill.STAMINA);
        map(CitizenJob.DELIVERYMAN,     CitizenSkill.AGILITY,      CitizenSkill.ADAPTABILITY);
        map(CitizenJob.MERCHANT,        CitizenSkill.ADAPTABILITY, CitizenSkill.INTELLIGENCE);
        map(CitizenJob.WAREHOUSE_WORKER, CitizenSkill.STAMINA,     CitizenSkill.DEXTERITY);
        map(CitizenJob.BUILDER,         CitizenSkill.ADAPTABILITY, CitizenSkill.ATHLETICS);

        // =====================================================================
        //  Recruits (Military Units)
        // =====================================================================
        map(CitizenJob.RECRUIT,         CitizenSkill.ATHLETICS,    CitizenSkill.STAMINA);
        map(CitizenJob.SHIELDMAN,       CitizenSkill.STAMINA,      CitizenSkill.ADAPTABILITY);
        map(CitizenJob.BOWMAN,          CitizenSkill.AGILITY,      CitizenSkill.FOCUS);
        map(CitizenJob.CROSSBOWMAN,     CitizenSkill.FOCUS,        CitizenSkill.AGILITY);
        map(CitizenJob.NOMAD,           CitizenSkill.ADAPTABILITY, CitizenSkill.AGILITY);
        map(CitizenJob.HORSEMAN,        CitizenSkill.ATHLETICS,    CitizenSkill.AGILITY);
        map(CitizenJob.COMMANDER,       CitizenSkill.INTELLIGENCE, CitizenSkill.ADAPTABILITY);
        map(CitizenJob.CAPTAIN,         CitizenSkill.INTELLIGENCE, CitizenSkill.STRENGTH);
        map(CitizenJob.MESSENGER,       CitizenSkill.AGILITY,      CitizenSkill.ADAPTABILITY);
        map(CitizenJob.SCOUT,           CitizenSkill.FOCUS,        CitizenSkill.AGILITY);
        map(CitizenJob.ASSASSIN,        CitizenSkill.DEXTERITY,    CitizenSkill.AGILITY);
    }

    private static void map(CitizenJob job, CitizenSkill primary, CitizenSkill secondary) {
        JOB_SKILLS.put(job, new CitizenSkill[]{primary, secondary});
    }

    /**
     * Returns the primary skill for the given job.
     * Falls back to {@link CitizenSkill#ATHLETICS} if unmapped.
     */
    public static CitizenSkill getPrimary(CitizenJob job) {
        CitizenSkill[] skills = JOB_SKILLS.get(job);
        return skills != null ? skills[0] : CitizenSkill.ATHLETICS;
    }

    /**
     * Returns the secondary skill for the given job.
     * Falls back to {@link CitizenSkill#STAMINA} if unmapped.
     */
    public static CitizenSkill getSecondary(CitizenJob job) {
        CitizenSkill[] skills = JOB_SKILLS.get(job);
        return skills != null ? skills[1] : CitizenSkill.STAMINA;
    }

    /**
     * Returns both skills as a 2-element array [primary, secondary].
     */
    public static CitizenSkill[] getSkills(CitizenJob job) {
        CitizenSkill[] skills = JOB_SKILLS.get(job);
        if (skills != null) {
            return new CitizenSkill[]{skills[0], skills[1]};
        }
        return new CitizenSkill[]{CitizenSkill.ATHLETICS, CitizenSkill.STAMINA};
    }

    /**
     * Returns true if the given job has skill mappings defined.
     */
    public static boolean hasMapping(CitizenJob job) {
        return JOB_SKILLS.containsKey(job);
    }
}
