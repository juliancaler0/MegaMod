package com.ultra.megamod.feature.citizen;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

public class CitizenConfig {

    // ===== Citizen Settings =====
    public static int MAX_CITIZENS_PER_PLAYER = 50;
    public static int MAX_CITIZENS_INITIAL = 4; // initial citizens when Town Hall placed
    public static int CITIZEN_RESPAWN_INTERVAL = 6000; // ticks between natural citizen spawns
    public static int MAX_CITIZEN_SATURATION = 100; // max food level
    public static int CITIZEN_MOURNING_DURATION = 24000; // 1 MC day mourning on citizen death
    public static int CHILDREN_GROW_UP_TIME = 72000; // 3 MC days for children to become adults
    public static float BABY_SPAWN_CHANCE = 0.1f; // 10% chance per day when housing available
    public static float CITIZEN_WALK_SPEED = 1.0f; // base movement speed multiplier
    public static float HUNGER_RATE = 1.0f;
    public static int CITIZEN_MAX_INVENTORY = 18;
    public static float CITIZEN_PICKUP_RADIUS = 5.5f;
    public static int MAX_HUNGER = 100;
    public static int HUNGER_THRESHOLD_EAT = 60;
    public static int HIRE_RANGE = 4;
    public static boolean HORSEMAN_AUTO_HORSE = true;
    public static boolean WORKERS_ALWAYS_WORK_IN_RAIN = false;
    public static boolean CHUNK_LOADING_ENABLED = false;

    // ===== Upkeep / Economy Settings =====
    public static int UPKEEP_INTERVAL_TICKS = 24000; // 1 Minecraft day
    public static String UPKEEP_FAILURE_MODE = "idle"; // idle, dismiss, reduced
    public static float RECRUIT_XP_MULTIPLIER = 1.0f;

    // ===== Building Settings =====
    public static int MAX_BUILDING_LEVEL = 5;
    public static boolean BUILDER_INFINITE_RESOURCES = false; // admin bypass for resources
    public static boolean BUILDER_BUILD_EVERY_TICK = false; // speed hack for testing
    public static int MAX_FIELDS_PER_FARMER = 5; // overridable from building level
    public static int[] RECIPE_COUNT_PER_LEVEL = new int[]{10, 20, 40, 80, 160}; // recipes per level for crafters

    // ===== Guard / Military Settings =====
    public static float GUARD_RETREAT_HEALTH_PCT = 0.2f; // retreat at 20% health
    public static double GUARD_FOLLOW_RANGE_LOOSE = 12.0; // loose grouping distance
    public static double GUARD_FOLLOW_RANGE_TIGHT = 4.0; // tight grouping distance
    public static int[] PATROL_RANGE_PER_LEVEL = new int[]{80, 110, 140, 170, 200};
    public static int[] BARRACKS_MAX_TOWERS_PER_LEVEL = new int[]{1, 2, 3, 4, 4};
    public static boolean HIRE_TRAINEE_ENABLED = true;
    public static int MERCENARY_COST = 5; // gold cost to hire mercenaries at barracks L3+

    // ===== Patrol Spawning =====
    public static boolean PATROL_SPAWN_ENABLED = true;
    public static int PATROL_SPAWN_INTERVAL = 6000; // Every 5 minutes
    public static float PATROL_SPAWN_CHANCE = 0.5f; // 50% chance per interval

    // ===== Raid Settings =====
    public static boolean RAID_ENABLED = true;
    public static int MIN_CITIZENS_FOR_RAID = 7;
    public static boolean RAID_DIFFICULTY_SCALING = true;
    public static int DOOR_BREAKING_AT_DIFFICULTY = 5; // raiders break doors at difficulty 5+
    public static int RAID_MAX_WAVE_SIZE = 20;
    public static float NIGHT_RAID_PROBABILITY = 0.5f;

    // ===== Siege Settings =====
    public static int SIEGE_DURATION_TICKS = 72000; // 3 Minecraft days
    public static int SIEGE_HEALTH_DEFAULT = 100;
    public static int SIEGE_MIN_RECRUITS = 3;
    public static boolean SIEGE_REQUIRES_OWNER_ONLINE = false;

    // ===== Economy / Production Settings =====
    public static int[] CRUSHER_DAILY_MAX_PER_LEVEL = new int[]{16, 64, 144, 256, 999};
    public static int[] SIFTER_DAILY_MAX_PER_LEVEL = new int[]{64, 256, 576, 1024, Integer.MAX_VALUE};
    public static int[] BEEKEEPER_MAX_HIVES_PER_LEVEL = new int[]{1, 2, 4, 8, 16};
    public static int[] ANIMAL_MAX_PER_LEVEL = new int[]{2, 4, 6, 8, 10};
    public static float MINER_LUCKY_ORE_CHANCE = 0.02f; // 2% per building level
    public static int[] MINER_MAX_Y_PER_LEVEL = new int[]{48, 16, -16, -64, -64};

    // ===== Disease Settings =====
    public static boolean DISEASE_ENABLED = true;
    public static float DISEASE_SPREAD_CHANCE = 0.01f;
    public static boolean FOOD_DIVERSITY_BONUS = true; // eating diverse food reduces disease chance

    // ===== Happiness Settings =====
    public static float HAPPINESS_GUARD_NEARBY_BONUS = 1.0f;
    public static float HAPPINESS_FOOD_PENALTY = -2.0f;
    public static float HAPPINESS_HOUSING_PENALTY = -3.0f;
    public static float HAPPINESS_DEATH_PENALTY = -5.0f;
    public static float MYSTICAL_SITE_HAPPINESS_PER_LEVEL = 0.5f;

    // ===== Research Settings =====
    public static float RESEARCH_TIME_MULTIPLIER = 1.0f;
    public static boolean OFFLINE_RESEARCH_ENABLED = true; // University L3+
    public static int[] MAX_RESEARCHERS_PER_LEVEL = new int[]{1, 2, 3, 4, 5};

    // ===== Claim / Territory Settings =====
    public static int COLONY_INITIAL_CLAIM_RADIUS = 4; // chunks
    public static int MAX_COLONY_CLAIM_RADIUS = 20; // chunks
    public static int COLONY_MIN_DISTANCE = 272; // blocks between colonies
    public static int CLAIM_BASE_COST = 25;
    public static boolean CASCADE_CLAIM_COST = true;
    public static boolean EXPLOSION_PROTECTION_IN_CLAIMS = true;

    // ===== Faction Settings =====
    public static int FACTION_CREATION_COST = 150;

    // ===== Misc Settings =====
    public static boolean CONSTRUCTION_TAPE_ENABLED = true;
    public static boolean AUTO_WORKER_HIRING = true;
    public static boolean AUTO_CITIZEN_HOUSING = true;
    public static boolean ENTER_LEAVE_MESSAGES = true;
    public static boolean SUPPLY_CAMP_PER_WORLD = true; // one supply per world
    public static int[] WAREHOUSE_MAX_COURIERS_PER_LEVEL = new int[]{2, 4, 6, 8, 10};

    // ===== Helper Methods =====

    public static int getHireCost(CitizenJob job) {
        return job.getCost();
    }

    public static int getUpkeepCost(CitizenJob job) {
        return job.getUpkeepCost();
    }
}
