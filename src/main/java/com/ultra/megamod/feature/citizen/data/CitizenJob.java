package com.ultra.megamod.feature.citizen.data;

public enum CitizenJob {
    // Resource Production Workers
    FARMER("Farmer", true, false, 20, 5),
    MINER("Miner", true, false, 30, 8),
    LUMBERJACK("Lumberjack", true, false, 20, 5),
    FISHERMAN("Fisherman", true, false, 20, 5),
    PLANTER("Planter", true, false, 25, 6),
    NETHER_MINER("Nether Miner", true, false, 50, 12),
    QUARRIER("Quarrier", true, false, 35, 8),
    // Animal Husbandry Workers
    SHEPHERD("Shepherd", true, false, 25, 6),
    CATTLE_FARMER("Cattle Farmer", true, false, 25, 6),
    CHICKEN_FARMER("Chicken Farmer", true, false, 25, 6),
    SWINEHERD("Swineherd", true, false, 25, 6),
    RABBIT_FARMER("Rabbit Farmer", true, false, 25, 6),
    BEEKEEPER("Beekeeper", true, false, 25, 6),
    GOAT_FARMER("Goat Farmer", true, false, 25, 6),
    // Crafting Workers
    BAKER("Baker", true, false, 30, 8),
    BLACKSMITH("Blacksmith", true, false, 40, 10),
    STONEMASON("Stonemason", true, false, 35, 8),
    SAWMILL("Sawmill", true, false, 30, 8),
    SMELTER("Smelter", true, false, 35, 8),
    STONE_SMELTER("Stone Smelter", true, false, 35, 8),
    CRUSHER("Crusher", true, false, 30, 8),
    SIFTER("Sifter", true, false, 25, 6),
    COOK("Cook", true, false, 25, 6),
    CHEF("Chef", true, false, 35, 8),
    DYE_WORKER("Dye Worker", true, false, 25, 6),
    FLETCHER("Fletcher", true, false, 30, 8),
    GLASSBLOWER("Glassblower", true, false, 30, 8),
    CONCRETE_MIXER("Concrete Mixer", true, false, 30, 8),
    COMPOSTER("Composter", true, false, 20, 5),
    FLORIST("Florist", true, false, 25, 6),
    MECHANIC("Mechanic", true, false, 40, 10),
    ALCHEMIST_CITIZEN("Alchemist", true, false, 50, 12),
    ENCHANTER("Enchanter", true, false, 60, 15),
    // Military Workers
    KNIGHT("Knight", true, false, 60, 15),
    ARCHER_TRAINING("Archer Training", true, false, 30, 8),
    COMBAT_TRAINING("Combat Training", true, false, 30, 8),
    DRUID("Druid", true, false, 60, 15),
    // Education Workers
    PUPIL("Pupil", true, false, 10, 3),
    TEACHER("Teacher", true, false, 40, 10),
    RESEARCHER("Researcher", true, false, 50, 12),
    // Service Workers
    HEALER("Healer", true, false, 50, 12),
    UNDERTAKER("Undertaker", true, false, 25, 6),
    DELIVERYMAN("Deliveryman", true, false, 30, 8),
    MERCHANT("Merchant", true, false, 60, 15),
    WAREHOUSE_WORKER("Warehouse Worker", true, false, 45, 10),
    BUILDER("Builder", true, false, 50, 12),
    // Recruits
    RECRUIT("Recruit", false, true, 40, 8),
    SHIELDMAN("Shieldman", false, true, 50, 12),
    BOWMAN("Bowman", false, true, 40, 8),
    CROSSBOWMAN("Crossbowman", false, true, 50, 12),
    NOMAD("Nomad", false, true, 60, 15),
    HORSEMAN("Horseman", false, true, 80, 20),
    COMMANDER("Commander", false, true, 100, 25),
    CAPTAIN("Captain", false, true, 150, 30),
    MESSENGER("Messenger", false, true, 60, 15),
    SCOUT("Scout", false, true, 40, 8),
    ASSASSIN("Assassin", false, true, 80, 20);

    private final String displayName;
    private final boolean worker;
    private final boolean recruit;
    private final int hireCost;
    private final int upkeepCost;

    CitizenJob(String displayName, boolean worker, boolean recruit, int hireCost, int upkeepCost) {
        this.displayName = displayName;
        this.worker = worker;
        this.recruit = recruit;
        this.hireCost = hireCost;
        this.upkeepCost = upkeepCost;
    }

    public String getDisplayName() { return displayName; }
    public boolean isWorker() { return worker; }
    public boolean isRecruit() { return recruit; }
    public int getCost() { return hireCost; }
    public int getUpkeepCost() { return upkeepCost; }

    public static CitizenJob fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FARMER;
        }
    }
}
