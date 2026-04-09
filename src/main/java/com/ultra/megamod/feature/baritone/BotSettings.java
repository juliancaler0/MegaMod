package com.ultra.megamod.feature.baritone;

/**
 * Configuration for a bot instance — ~60 settings organized by category.
 */
public class BotSettings {
    // Movement
    public boolean allowBreak = true;
    public boolean allowPlace = true;
    public boolean allowSprint = true;
    public boolean allowParkour = true;
    public boolean allowSwim = true;
    public boolean allowClimb = true;
    public boolean allowWaterBucket = false;
    public double movementSpeed = 0.2;

    // Elytra
    public boolean allowElytra = true;
    public int elytraCruiseAltitude = 200;
    public boolean elytraAutoFirework = true;
    public int elytraFireworkInterval = 60;

    // Pathfinding
    public int primaryTimeoutMS = 2000;
    public int failureTimeoutMS = 4000;
    public int maxNodes = 300_000;
    public int snapshotRadius = 64;
    public int pathSegmentLength = 50;

    // Advanced pathfinding
    public boolean allowDiagonal = true;
    public int maxPathLength = 200;
    public boolean preferHighways = false;

    // Safety
    public boolean avoidDanger = true;
    public boolean avoidMobs = false;
    public int mobAvoidRadius = 8;
    public int maxFallHeight = 3;
    public boolean avoidWater = false;
    public boolean avoidLava = true;
    public int minFoodLevel = 6;
    public boolean autoEatWhilePathing = true;
    public boolean retreatOnLowHealth = true;
    public int retreatHealthThreshold = 6;

    // Mining
    public int mineRadius = 32;
    public int mineScanInterval = 40;
    public boolean preferSilkTouch = false;

    // Building
    public boolean buildInLayers = true;
    public boolean buildBottomUp = true;
    public boolean skipMatchingBlocks = true;
    public int maxBuildRadius = 64;

    // Backfill
    public boolean autoBackfill = false;
    public int backfillMaxQueue = 200;

    // Farming
    public int farmRadius = 16;
    public boolean autoReplant = true;
    public boolean useBoneMeal = false;
    public int farmScanInterval = 60;

    // Following
    public int followDistance = 3;
    public boolean followSprint = true;

    // Patrol
    public int waypointPauseTicks = 40;
    public boolean loopPatrol = true;

    // Exploration
    public int exploreChunkRadius = 5;
    public boolean spiralExpansion = true;

    // Cache
    public int cacheRadius = 4;
    public int cacheRefreshRate = 20;

    // Combat
    public boolean engageHostiles = false;
    public int combatRange = 16;
    public boolean autoEquipArmor = true;

    // Logging
    public boolean logActions = true;
    public boolean logPathfinding = false;
    public int maxLogEntries = 100;

    public void set(String key, String value) {
        try {
            switch (key) {
                // Movement
                case "allowBreak" -> allowBreak = Boolean.parseBoolean(value);
                case "allowPlace" -> allowPlace = Boolean.parseBoolean(value);
                case "allowSprint" -> allowSprint = Boolean.parseBoolean(value);
                case "allowParkour" -> allowParkour = Boolean.parseBoolean(value);
                case "allowSwim" -> allowSwim = Boolean.parseBoolean(value);
                case "allowClimb" -> allowClimb = Boolean.parseBoolean(value);
                case "allowWaterBucket" -> allowWaterBucket = Boolean.parseBoolean(value);
                case "movementSpeed" -> movementSpeed = Math.max(0.05, Math.min(Double.parseDouble(value), 1.0));
                // Elytra
                case "allowElytra" -> allowElytra = Boolean.parseBoolean(value);
                case "elytraCruiseAltitude" -> elytraCruiseAltitude = Math.max(64, Math.min(Integer.parseInt(value), 320));
                case "elytraAutoFirework" -> elytraAutoFirework = Boolean.parseBoolean(value);
                case "elytraFireworkInterval" -> elytraFireworkInterval = Math.max(10, Math.min(Integer.parseInt(value), 200));
                // Pathfinding
                case "primaryTimeoutMS" -> primaryTimeoutMS = Math.max(500, Math.min(Integer.parseInt(value), 10000));
                case "failureTimeoutMS" -> failureTimeoutMS = Math.max(1000, Math.min(Integer.parseInt(value), 20000));
                case "maxNodes" -> maxNodes = Math.max(10000, Math.min(Integer.parseInt(value), 1000000));
                case "snapshotRadius" -> snapshotRadius = Math.max(16, Math.min(Integer.parseInt(value), 128));
                case "pathSegmentLength" -> pathSegmentLength = Math.max(10, Math.min(Integer.parseInt(value), 200));
                // Advanced pathfinding
                case "allowDiagonal" -> allowDiagonal = Boolean.parseBoolean(value);
                case "maxPathLength" -> maxPathLength = Math.max(20, Math.min(Integer.parseInt(value), 1000));
                case "preferHighways" -> preferHighways = Boolean.parseBoolean(value);
                // Safety
                case "avoidDanger" -> avoidDanger = Boolean.parseBoolean(value);
                case "avoidMobs" -> avoidMobs = Boolean.parseBoolean(value);
                case "mobAvoidRadius" -> mobAvoidRadius = Math.max(2, Math.min(Integer.parseInt(value), 32));
                case "maxFallHeight" -> maxFallHeight = Math.max(0, Math.min(Integer.parseInt(value), 256));
                case "avoidWater" -> avoidWater = Boolean.parseBoolean(value);
                case "avoidLava" -> avoidLava = Boolean.parseBoolean(value);
                case "minFoodLevel" -> minFoodLevel = Math.max(0, Math.min(Integer.parseInt(value), 20));
                case "autoEatWhilePathing" -> autoEatWhilePathing = Boolean.parseBoolean(value);
                case "retreatOnLowHealth" -> retreatOnLowHealth = Boolean.parseBoolean(value);
                case "retreatHealthThreshold" -> retreatHealthThreshold = Math.max(1, Math.min(Integer.parseInt(value), 20));
                // Mining
                case "mineRadius" -> mineRadius = Math.max(8, Math.min(Integer.parseInt(value), 128));
                case "mineScanInterval" -> mineScanInterval = Math.max(10, Math.min(Integer.parseInt(value), 200));
                case "preferSilkTouch" -> preferSilkTouch = Boolean.parseBoolean(value);
                // Building
                case "buildInLayers" -> buildInLayers = Boolean.parseBoolean(value);
                case "buildBottomUp" -> buildBottomUp = Boolean.parseBoolean(value);
                case "skipMatchingBlocks" -> skipMatchingBlocks = Boolean.parseBoolean(value);
                case "maxBuildRadius" -> maxBuildRadius = Math.max(8, Math.min(Integer.parseInt(value), 128));
                // Backfill
                case "autoBackfill" -> autoBackfill = Boolean.parseBoolean(value);
                case "backfillMaxQueue" -> backfillMaxQueue = Math.max(10, Math.min(Integer.parseInt(value), 1000));
                // Farming
                case "farmRadius" -> farmRadius = Math.max(4, Math.min(Integer.parseInt(value), 64));
                case "autoReplant" -> autoReplant = Boolean.parseBoolean(value);
                case "useBoneMeal" -> useBoneMeal = Boolean.parseBoolean(value);
                case "farmScanInterval" -> farmScanInterval = Math.max(10, Math.min(Integer.parseInt(value), 200));
                // Following
                case "followDistance" -> followDistance = Math.max(1, Math.min(Integer.parseInt(value), 32));
                case "followSprint" -> followSprint = Boolean.parseBoolean(value);
                // Patrol
                case "waypointPauseTicks" -> waypointPauseTicks = Math.max(0, Math.min(Integer.parseInt(value), 200));
                case "loopPatrol" -> loopPatrol = Boolean.parseBoolean(value);
                // Exploration
                case "exploreChunkRadius" -> exploreChunkRadius = Math.max(1, Math.min(Integer.parseInt(value), 32));
                case "spiralExpansion" -> spiralExpansion = Boolean.parseBoolean(value);
                // Cache
                case "cacheRadius" -> cacheRadius = Math.max(1, Math.min(Integer.parseInt(value), 16));
                case "cacheRefreshRate" -> cacheRefreshRate = Math.max(1, Math.min(Integer.parseInt(value), 200));
                // Combat
                case "engageHostiles" -> engageHostiles = Boolean.parseBoolean(value);
                case "combatRange" -> combatRange = Math.max(4, Math.min(Integer.parseInt(value), 64));
                case "autoEquipArmor" -> autoEquipArmor = Boolean.parseBoolean(value);
                // Logging
                case "logActions" -> logActions = Boolean.parseBoolean(value);
                case "logPathfinding" -> logPathfinding = Boolean.parseBoolean(value);
                case "maxLogEntries" -> maxLogEntries = Math.max(10, Math.min(Integer.parseInt(value), 1000));
            }
        } catch (NumberFormatException ignored) {
            // Invalid numeric value — keep existing setting
        }
    }

    public String toJson() {
        return "{\"allowBreak\":" + allowBreak
             + ",\"allowPlace\":" + allowPlace
             + ",\"allowSprint\":" + allowSprint
             + ",\"allowParkour\":" + allowParkour
             + ",\"allowSwim\":" + allowSwim
             + ",\"allowClimb\":" + allowClimb
             + ",\"allowWaterBucket\":" + allowWaterBucket
             + ",\"movementSpeed\":" + movementSpeed
             + ",\"allowElytra\":" + allowElytra
             + ",\"elytraCruiseAltitude\":" + elytraCruiseAltitude
             + ",\"elytraAutoFirework\":" + elytraAutoFirework
             + ",\"elytraFireworkInterval\":" + elytraFireworkInterval
             + ",\"primaryTimeoutMS\":" + primaryTimeoutMS
             + ",\"failureTimeoutMS\":" + failureTimeoutMS
             + ",\"maxNodes\":" + maxNodes
             + ",\"snapshotRadius\":" + snapshotRadius
             + ",\"pathSegmentLength\":" + pathSegmentLength
             + ",\"allowDiagonal\":" + allowDiagonal
             + ",\"maxPathLength\":" + maxPathLength
             + ",\"preferHighways\":" + preferHighways
             + ",\"avoidDanger\":" + avoidDanger
             + ",\"avoidMobs\":" + avoidMobs
             + ",\"mobAvoidRadius\":" + mobAvoidRadius
             + ",\"maxFallHeight\":" + maxFallHeight
             + ",\"avoidWater\":" + avoidWater
             + ",\"avoidLava\":" + avoidLava
             + ",\"minFoodLevel\":" + minFoodLevel
             + ",\"autoEatWhilePathing\":" + autoEatWhilePathing
             + ",\"retreatOnLowHealth\":" + retreatOnLowHealth
             + ",\"retreatHealthThreshold\":" + retreatHealthThreshold
             + ",\"mineRadius\":" + mineRadius
             + ",\"mineScanInterval\":" + mineScanInterval
             + ",\"preferSilkTouch\":" + preferSilkTouch
             + ",\"buildInLayers\":" + buildInLayers
             + ",\"buildBottomUp\":" + buildBottomUp
             + ",\"skipMatchingBlocks\":" + skipMatchingBlocks
             + ",\"maxBuildRadius\":" + maxBuildRadius
             + ",\"autoBackfill\":" + autoBackfill
             + ",\"backfillMaxQueue\":" + backfillMaxQueue
             + ",\"farmRadius\":" + farmRadius
             + ",\"autoReplant\":" + autoReplant
             + ",\"useBoneMeal\":" + useBoneMeal
             + ",\"farmScanInterval\":" + farmScanInterval
             + ",\"followDistance\":" + followDistance
             + ",\"followSprint\":" + followSprint
             + ",\"waypointPauseTicks\":" + waypointPauseTicks
             + ",\"loopPatrol\":" + loopPatrol
             + ",\"exploreChunkRadius\":" + exploreChunkRadius
             + ",\"spiralExpansion\":" + spiralExpansion
             + ",\"cacheRadius\":" + cacheRadius
             + ",\"cacheRefreshRate\":" + cacheRefreshRate
             + ",\"engageHostiles\":" + engageHostiles
             + ",\"combatRange\":" + combatRange
             + ",\"autoEquipArmor\":" + autoEquipArmor
             + ",\"logActions\":" + logActions
             + ",\"logPathfinding\":" + logPathfinding
             + ",\"maxLogEntries\":" + maxLogEntries + "}";
    }

    /** Get all setting keys for autocomplete */
    public static String[] getAllKeys() {
        return new String[]{
            // Movement
            "allowBreak", "allowPlace", "allowSprint", "allowParkour", "allowSwim", "allowClimb",
            "allowWaterBucket", "movementSpeed",
            // Elytra
            "allowElytra", "elytraCruiseAltitude", "elytraAutoFirework", "elytraFireworkInterval",
            // Pathfinding
            "primaryTimeoutMS", "failureTimeoutMS", "maxNodes", "snapshotRadius", "pathSegmentLength",
            // Advanced pathfinding
            "allowDiagonal", "maxPathLength", "preferHighways",
            // Safety
            "avoidDanger", "avoidMobs", "mobAvoidRadius", "maxFallHeight", "avoidWater", "avoidLava",
            "minFoodLevel", "autoEatWhilePathing", "retreatOnLowHealth", "retreatHealthThreshold",
            // Mining
            "mineRadius", "mineScanInterval", "preferSilkTouch",
            // Building
            "buildInLayers", "buildBottomUp", "skipMatchingBlocks", "maxBuildRadius",
            // Backfill
            "autoBackfill", "backfillMaxQueue",
            // Farming
            "farmRadius", "autoReplant", "useBoneMeal", "farmScanInterval",
            // Following
            "followDistance", "followSprint",
            // Patrol
            "waypointPauseTicks", "loopPatrol",
            // Exploration
            "exploreChunkRadius", "spiralExpansion",
            // Cache
            "cacheRadius", "cacheRefreshRate",
            // Combat
            "engageHostiles", "combatRange", "autoEquipArmor",
            // Logging
            "logActions", "logPathfinding", "maxLogEntries"
        };
    }
}
