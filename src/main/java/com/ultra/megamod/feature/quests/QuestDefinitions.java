package com.ultra.megamod.feature.quests;


import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuestDefinitions {

    // ─── Quest Categories (tabs in the GUI) ───

    public enum QuestCategory {
        GETTING_STARTED("Getting Started", "Learn the basics of MegaMod", 0xFF4CAF50),
        COMBAT("Combat & Equipment", "Master weapons, relics, and skills", 0xFFE53935),
        CLASS_QUESTS("Class Quests", "Master your chosen class", 0xFF00BCD4),
        DUNGEONS("Dungeons", "Conquer procedural dungeons", 0xFF9C27B0),
        COLONY("Colony & Territory", "Build your settlement", 0xFF2E7D32),
        EXPLORATION("Exploration & Collection", "Discover the world", 0xFF1565C0),
        ECONOMY("Economy & Trading", "Become a trade mogul", 0xFFE8A838),
        MASTERY("Mastery", "Endgame prestige challenges", 0xFFFF6F00);

        public final String displayName;
        public final String description;
        public final int color;

        QuestCategory(String displayName, String description, int color) {
            this.displayName = displayName;
            this.description = description;
            this.color = color;
        }
    }

    // ─── Task types — each maps to an existing mod system ───

    public enum QuestTaskType {
        HAVE_ITEM,           // inventory contains item (targetId = item registry name)
        REACH_BALANCE,       // wallet + bank >= targetAmount
        SKILL_LEVEL,         // SkillTreeType level >= targetAmount (targetId = tree name or "ANY"/"ALL")
        UNLOCK_SKILL_NODE,   // specific node unlocked (targetId = node ID)
        DUNGEON_CLEAR,       // DungeonTier clear count >= targetAmount (targetId = tier name or "ANY")
        MOB_KILLS,           // PlayerStatistics mobKills >= targetAmount
        BLOCKS_BROKEN,       // PlayerStatistics blocksBroken >= targetAmount
        STAT_CHECK,          // generic stat >= targetAmount (targetId = stat key)
        MUSEUM_DONATIONS,    // total museum donations >= targetAmount
        CITIZEN_COUNT,       // owned citizens >= targetAmount
        CLAIM_CHUNKS,        // claimed chunks >= targetAmount
        EQUIP_ACCESSORY,     // equipped accessories count >= targetAmount
        PRESTIGE_TREE,       // prestiged tree count >= targetAmount (targetId = tree name or "ANY")
        CHECKMARK,           // manual flag — auto-complete or via quest_checkmark action
        BOUNTY_COMPLETE,     // cumulative bounty completions >= targetAmount
        CASINO_PLAY,         // cumulative casino games played >= targetAmount
        TRADE_MARKETPLACE,   // cumulative marketplace trades >= targetAmount
        VISIT_DIMENSION,     // visited dimensions count >= targetAmount
        BUILDING_PLACED,     // colony building placed (targetId = building ID, e.g. "builder")
        BUILDING_COUNT,      // total colony buildings >= targetAmount
        BUILDING_LEVEL,      // building at level >= targetAmount (targetId = building ID)
        SURVIVE_RAID,        // survived raids >= targetAmount
    }

    // ─── Reward types ───

    public enum QuestRewardType {
        COINS,       // amount = coin count
        SKILL_XP,    // amount = XP, targetId = tree name
        ITEM,        // targetId = item registry name, amount = count
    }

    // ─── Records ───

    public record QuestTask(QuestTaskType type, String targetId, int targetAmount, String description) {}
    public record QuestReward(QuestRewardType type, int amount, String targetId, String description) {}

    public record QuestDef(
        String id,
        QuestCategory category,
        String title,
        String[] description,
        int sortOrder,
        String[] prerequisites,
        QuestTask[] tasks,
        QuestReward[] rewards,
        boolean partyShared
    ) {}

    // ─── Registry ───

    public static final Map<String, QuestDef> ALL_QUESTS = new LinkedHashMap<>();
    public static final Map<QuestCategory, List<QuestDef>> BY_CATEGORY = new EnumMap<>(QuestCategory.class);

    /**
     * Maps quest IDs to the PlayerClass required to complete them.
     * Quests not in this map have no class restriction.
     */
    public static final Map<String, String> CLASS_REQUIREMENTS = new LinkedHashMap<>();

    private static void register(QuestDef def) {
        ALL_QUESTS.put(def.id(), def);
        BY_CATEGORY.computeIfAbsent(def.category(), k -> new ArrayList<>()).add(def);
    }

    public static QuestDef get(String id) { return ALL_QUESTS.get(id); }

    // ─── Shorthand helpers ───

    private static String[] prereqs(String... ids) { return ids; }
    private static String[] noPrereqs() { return new String[0]; }
    private static String[] desc(String... lines) { return lines; }

    private static QuestTask task(QuestTaskType type, String targetId, int amount, String description) {
        return new QuestTask(type, targetId, amount, description);
    }
    private static QuestTask task(QuestTaskType type, int amount, String description) {
        return new QuestTask(type, "", amount, description);
    }

    private static QuestReward coins(int amount) {
        return new QuestReward(QuestRewardType.COINS, amount, "", amount + " MegaCoins");
    }
    private static QuestReward skillXp(String tree, int amount) {
        return new QuestReward(QuestRewardType.SKILL_XP, amount, tree, amount + " " + tree + " XP");
    }
    private static QuestReward item(String itemId, int count, String displayName) {
        return new QuestReward(QuestRewardType.ITEM, count, itemId, count + "x " + displayName);
    }

    /** Register a quest that requires a specific player class to complete. */
    private static void registerClassQuest(QuestDef def, String requiredClass) {
        register(def);
        CLASS_REQUIREMENTS.put(def.id(), requiredClass);
    }

    /**
     * Returns the required PlayerClass for a quest, or null if unrestricted.
     */
    public static String getClassRequirement(String questId) {
        return CLASS_REQUIREMENTS.get(questId);
    }

    // ─── Quest content registration ───

    static {
        registerGettingStarted();
        registerCombat();
        registerClassQuests();
        registerDungeons();
        registerColony();
        registerExploration();
        registerEconomy();
        registerMastery();
    }

    // ═══════════════════════════════════════════
    // GETTING STARTED — 19 quests
    // ═══════════════════════════════════════════

    private static void registerGettingStarted() {
        register(new QuestDef("gs_01", QuestCategory.GETTING_STARTED,
            "Welcome to MegaMod",
            desc("You've opened the Quest app!", "This system will guide you through", "everything MegaMod has to offer."),
            1, noPrereqs(),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Open the Quests app") },
            new QuestReward[]{ coins(50) },
            false
        ));

        register(new QuestDef("gs_02", QuestCategory.GETTING_STARTED,
            "Your First Computer",
            desc("Craft a MegaMod Computer.", "It's the hub for everything —", "banking, skills, quests, and more."),
            2, prereqs("gs_01"),
            new QuestTask[]{ task(QuestTaskType.HAVE_ITEM, "megamod:computer", 1, "Craft a Computer") },
            new QuestReward[]{ coins(100) },
            false
        ));

        register(new QuestDef("gs_03", QuestCategory.GETTING_STARTED,
            "Making Money",
            desc("MegaCoins are the universal currency.", "Earn them through quests, bounties,", "selling items, and more."),
            3, prereqs("gs_02"),
            new QuestTask[]{ task(QuestTaskType.REACH_BALANCE, 100, "Have 100 MegaCoins") },
            new QuestReward[]{ coins(50) },
            false
        ));

        register(new QuestDef("gs_04", QuestCategory.GETTING_STARTED,
            "Window Shopping",
            desc("The Shop has a daily rotating", "selection of items for sale.", "Open it from the Computer."),
            4, prereqs("gs_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Open the Shop app") },
            new QuestReward[]{ coins(75) },
            false
        ));

        register(new QuestDef("gs_05", QuestCategory.GETTING_STARTED,
            "Safety Deposit",
            desc("Your wallet drops on death!", "Deposit coins in the Bank to", "keep them safe."),
            5, prereqs("gs_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Deposit coins into the Bank") },
            new QuestReward[]{ coins(100) },
            false
        ));

        register(new QuestDef("gs_06", QuestCategory.GETTING_STARTED,
            "Slayer",
            desc("Combat is a core part of MegaMod.", "Kill some hostile mobs to get started."),
            6, prereqs("gs_01"),
            new QuestTask[]{ task(QuestTaskType.MOB_KILLS, 5, "Kill 5 hostile mobs") },
            new QuestReward[]{ coins(100), skillXp("COMBAT", 10) },
            false
        ));

        register(new QuestDef("gs_07", QuestCategory.GETTING_STARTED,
            "Skill Apprentice",
            desc("MegaMod has 5 skill trees.", "Level them up by performing", "related activities."),
            7, prereqs("gs_06"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "ANY", 2, "Reach level 2 in any skill tree") },
            new QuestReward[]{ coins(200) },
            false
        ));

        register(new QuestDef("gs_08", QuestCategory.GETTING_STARTED,
            "Reading Up",
            desc("The Wiki contains info on every", "mob, item, biome, and system.", "Use it as a reference!"),
            8, prereqs("gs_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Open the Wiki app") },
            new QuestReward[]{ coins(50) },
            false
        ));

        register(new QuestDef("gs_09", QuestCategory.GETTING_STARTED,
            "Note to Self",
            desc("The Notes app lets you save", "reminders, coordinates, and plans."),
            9, prereqs("gs_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Create a note in the Notes app") },
            new QuestReward[]{ coins(50) },
            false
        ));

        register(new QuestDef("gs_10", QuestCategory.GETTING_STARTED,
            "Party Up",
            desc("Team up with friends!", "Party members can share quest", "progress and enter dungeons together."),
            10, prereqs("gs_07"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Create or join a party") },
            new QuestReward[]{ coins(150) },
            true
        ));

        register(new QuestDef("gs_11", QuestCategory.GETTING_STARTED,
            "Friend Request",
            desc("Add another player as a friend.", "Friends can teleport to each other", "and see online status."),
            11, prereqs("gs_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Add a friend via the Friends app") },
            new QuestReward[]{ coins(75) },
            false
        ));

        register(new QuestDef("gs_12", QuestCategory.GETTING_STARTED,
            "Mark the Spot",
            desc("Create a waypoint on the Map app.", "Waypoints help you navigate back", "to important locations."),
            12, prereqs("gs_08"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Create a map waypoint") },
            new QuestReward[]{ coins(75) },
            false
        ));

        register(new QuestDef("gs_13", QuestCategory.GETTING_STARTED,
            "Pack It Up",
            desc("Craft a backpack for portable", "inventory. Upgrade it with modules", "like Magnet, AutoPickup, and more."),
            13, prereqs("gs_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Craft a backpack") },
            new QuestReward[]{ coins(100) },
            false
        ));

        register(new QuestDef("gs_14", QuestCategory.GETTING_STARTED,
            "Stay Organized",
            desc("Use the sort keybind while a", "container is open. It sorts", "items by type automatically."),
            14, prereqs("gs_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Sort a container") },
            new QuestReward[]{ coins(50) },
            false
        ));

        register(new QuestDef("gs_15", QuestCategory.GETTING_STARTED,
            "Harvest Season",
            desc("Harvesting crops grants Farming XP!", "Plant and harvest to level your", "Farming skill tree."),
            15, prereqs("gs_01"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "FARMING", 2, "Reach Farming level 2") },
            new QuestReward[]{ coins(100), skillXp("FARMING", 15) },
            false
        ));

        register(new QuestDef("gs_16", QuestCategory.GETTING_STARTED,
            "Tough as Nails",
            desc("Survival XP comes from endurance —", "taking damage, eating, exploring.", "Level it to become more resilient."),
            16, prereqs("gs_06"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "SURVIVAL", 2, "Reach Survival level 2") },
            new QuestReward[]{ coins(100), skillXp("SURVIVAL", 15) },
            false
        ));

        register(new QuestDef("gs_17", QuestCategory.GETTING_STARTED,
            "On the Go",
            desc("The Phone lets you access the", "computer from anywhere — no need to", "be at a desk."),
            17, prereqs("gs_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Craft and use a Phone") },
            new QuestReward[]{ coins(100) },
            false
        ));

        register(new QuestDef("gs_18", QuestCategory.GETTING_STARTED,
            "Your Preferences",
            desc("Open Settings on the computer to", "toggle features on/off.", "Customize your experience."),
            18, prereqs("gs_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Open the Settings app") },
            new QuestReward[]{ coins(50) },
            false
        ));

        register(new QuestDef("gs_19", QuestCategory.GETTING_STARTED,
            "Choose Your Path",
            desc("Unlock your first class branch in", "the skill tree. Choose Paladin,", "Wizard, Rogue, or Ranger."),
            19, prereqs("gs_07"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Unlock a class branch node") },
            new QuestReward[]{ coins(200) },
            false
        ));
    }

    // ═══════════════════════════════════════════
    // COMBAT & EQUIPMENT — 39 quests
    // ═══════════════════════════════════════════

    private static void registerCombat() {
        register(new QuestDef("cb_01", QuestCategory.COMBAT,
            "First Blood",
            desc("Sharpen your combat skills.", "Every kill earns Combat XP."),
            1, prereqs("gs_06"),
            new QuestTask[]{ task(QuestTaskType.MOB_KILLS, 10, "Kill 10 hostile mobs") },
            new QuestReward[]{ coins(100), skillXp("COMBAT", 15) },
            false
        ));

        register(new QuestDef("cb_02", QuestCategory.COMBAT,
            "Warrior's Path",
            desc("Level your Combat skill tree", "to unlock powerful bonuses."),
            2, prereqs("cb_01"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "COMBAT", 5, "Reach Combat level 5") },
            new QuestReward[]{ coins(200), skillXp("COMBAT", 25) },
            false
        ));

        register(new QuestDef("cb_03", QuestCategory.COMBAT,
            "Relic Hunter",
            desc("Relics are powerful themed weapons", "with unique abilities.", "Find one from dungeon loot or mob drops."),
            3, prereqs("cb_01"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Obtain any Relic weapon") },
            new QuestReward[]{ coins(150), skillXp("ARCANE", 20) },
            false
        ));

        register(new QuestDef("cb_04", QuestCategory.COMBAT,
            "Accessorize",
            desc("Accessories provide passive bonuses.", "Equip one in any slot via the", "Accessories menu (V key)."),
            4, prereqs("cb_01"),
            new QuestTask[]{ task(QuestTaskType.EQUIP_ACCESSORY, 1, "Equip any accessory") },
            new QuestReward[]{ coins(200) },
            false
        ));

        register(new QuestDef("cb_05", QuestCategory.COMBAT,
            "Monster Slayer",
            desc("Prove your combat prowess.", "The more you fight, the stronger you become."),
            5, prereqs("cb_02"),
            new QuestTask[]{ task(QuestTaskType.MOB_KILLS, 50, "Kill 50 hostile mobs") },
            new QuestReward[]{ coins(300), skillXp("COMBAT", 30) },
            false
        ));

        register(new QuestDef("cb_06", QuestCategory.COMBAT,
            "Combat Specialist",
            desc("Reaching level 10 unlocks", "the second branch of the Combat tree."),
            6, prereqs("cb_05"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "COMBAT", 10, "Reach Combat level 10") },
            new QuestReward[]{ coins(500) },
            false
        ));

        register(new QuestDef("cb_07", QuestCategory.COMBAT,
            "Arcane Student",
            desc("The Arcane tree governs magic,", "enchantments, and potion effects."),
            7, prereqs("cb_02"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "ARCANE", 5, "Reach Arcane level 5") },
            new QuestReward[]{ coins(200), skillXp("ARCANE", 25) },
            false
        ));

        register(new QuestDef("cb_08", QuestCategory.COMBAT,
            "Relic Researcher",
            desc("Research relics at the Research Table", "to unlock their hidden abilities."),
            8, prereqs("cb_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Research a Relic") },
            new QuestReward[]{ coins(300) },
            false
        ));

        register(new QuestDef("cb_09", QuestCategory.COMBAT,
            "Fully Equipped",
            desc("Fill multiple accessory slots", "for stacking bonuses."),
            9, prereqs("cb_04"),
            new QuestTask[]{ task(QuestTaskType.EQUIP_ACCESSORY, 4, "Equip 4 accessories") },
            new QuestReward[]{ coins(400), skillXp("COMBAT", 30) },
            false
        ));

        register(new QuestDef("cb_10", QuestCategory.COMBAT,
            "Centurion",
            desc("A true warrior has slain hundreds.", "Keep fighting!"),
            10, prereqs("cb_05"),
            new QuestTask[]{ task(QuestTaskType.MOB_KILLS, 200, "Kill 200 hostile mobs") },
            new QuestReward[]{ coins(500), skillXp("COMBAT", 50) },
            false
        ));

        register(new QuestDef("cb_11", QuestCategory.COMBAT,
            "Master Combatant",
            desc("Reach the pinnacle of Combat skill.", "Capstone nodes await."),
            11, prereqs("cb_06"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "COMBAT", 25, "Reach Combat level 25") },
            new QuestReward[]{ coins(1000) },
            false
        ));

        register(new QuestDef("cb_12", QuestCategory.COMBAT,
            "Arcane Master",
            desc("Harness the full power of the Arcane.", "Capstone abilities unlock at high levels."),
            12, prereqs("cb_07"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "ARCANE", 25, "Reach Arcane level 25") },
            new QuestReward[]{ coins(1000) },
            false
        ));

        register(new QuestDef("cb_13", QuestCategory.COMBAT,
            "Power Strike",
            desc("Relics have active abilities!", "Press R to activate your relic's", "primary ability in combat."),
            13, prereqs("cb_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use a relic's primary ability (R)") },
            new QuestReward[]{ coins(200), skillXp("COMBAT", 20) },
            false
        ));

        register(new QuestDef("cb_14", QuestCategory.COMBAT,
            "Dual Wielder",
            desc("Press G to toggle your relic's", "secondary passive ability.", "Combine both for maximum power."),
            14, prereqs("cb_13"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use a relic's secondary ability (G)") },
            new QuestReward[]{ coins(200), skillXp("ARCANE", 20) },
            false
        ));

        register(new QuestDef("cb_15", QuestCategory.COMBAT,
            "Alchemist's Apprentice",
            desc("The Alchemy Grindstone turns raw", "materials into magical reagents.", "Grind your first ingredient."),
            15, prereqs("cb_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Grind a reagent at an Alchemy Grindstone") },
            new QuestReward[]{ coins(150), skillXp("ARCANE", 15) },
            false
        ));

        register(new QuestDef("cb_16", QuestCategory.COMBAT,
            "Potion Brewer",
            desc("Combine reagents in the Alchemy", "Cauldron to brew custom potions.", "Effects like Inferno, Shadow Step, and more."),
            16, prereqs("cb_15"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Brew a potion in the Alchemy Cauldron") },
            new QuestReward[]{ coins(250), skillXp("ARCANE", 25) },
            false
        ));

        register(new QuestDef("cb_17", QuestCategory.COMBAT,
            "Arena Challenger",
            desc("The Arena pits you against waves", "of mobs. Survive as long as", "you can for rewards."),
            17, prereqs("cb_05"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Complete an Arena PvE run") },
            new QuestReward[]{ coins(300), skillXp("COMBAT", 30) },
            false
        ));

        register(new QuestDef("cb_18", QuestCategory.COMBAT,
            "Arena Veteran",
            desc("Consistency in the Arena proves", "true combat mastery."),
            18, prereqs("cb_17"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Complete 5 Arena runs") },
            new QuestReward[]{ coins(500), skillXp("COMBAT", 50) },
            false
        ));

        register(new QuestDef("cb_19", QuestCategory.COMBAT,
            "Advanced Alchemy",
            desc("Brew a powerful potion like", "Berserker Rage, Chronos, or Shadow Step.", "Higher-tier potions turn the tide."),
            19, prereqs("cb_16"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Brew an advanced potion (Tier 3+)") },
            new QuestReward[]{ coins(400), skillXp("ARCANE", 40) },
            false
        ));

        register(new QuestDef("cb_20", QuestCategory.COMBAT,
            "Banner Bearer",
            desc("Equip a banner on your back as", "a cosmetic — show your colors", "on the battlefield."),
            20, prereqs("cb_05"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip a banner") },
            new QuestReward[]{ coins(150) },
            false
        ));

        register(new QuestDef("cb_21", QuestCategory.COMBAT,
            "Paladin's Oath",
            desc("Equip a full Paladin or Crusader", "armor set. Set bonuses activate", "with 2+ pieces."),
            21, prereqs("gs_19"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip a Paladin armor set") },
            new QuestReward[]{ coins(300), skillXp("COMBAT", 30) },
            false
        ));

        register(new QuestDef("cb_22", QuestCategory.COMBAT,
            "Wizard's First Spell",
            desc("Cast your first spell. Unlock spell", "nodes in the Arcane tree, then", "use R/G to cast."),
            22, prereqs("gs_19"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Cast any spell") },
            new QuestReward[]{ coins(300), skillXp("ARCANE", 30) },
            false
        ));

        register(new QuestDef("cb_23", QuestCategory.COMBAT,
            "Rogue's Cunning",
            desc("Use Shadow Step or Vanish in", "combat. The shadows are your ally."),
            23, prereqs("gs_19"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use a Rogue spell") },
            new QuestReward[]{ coins(300), skillXp("SURVIVAL", 30) },
            false
        ));

        register(new QuestDef("cb_24", QuestCategory.COMBAT,
            "Ranger's Precision",
            desc("Use Power Shot or Barrage to", "enhance your ranged attacks."),
            24, prereqs("gs_19"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use a Ranger spell") },
            new QuestReward[]{ coins(300), skillXp("SURVIVAL", 30) },
            false
        ));

        register(new QuestDef("cb_25", QuestCategory.COMBAT,
            "Set Bonus",
            desc("Equip 4 pieces of the same armor", "set to activate the full set bonus."),
            25, prereqs("cb_21"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Activate a 4-piece set bonus") },
            new QuestReward[]{ coins(500) },
            false
        ));

        register(new QuestDef("cb_26", QuestCategory.COMBAT,
            "Spell Combo",
            desc("Cast 3 different spells in a", "single combat encounter."),
            26, prereqs("cb_22"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Cast 3 different spells in combat") },
            new QuestReward[]{ coins(400), skillXp("ARCANE", 40) },
            false
        ));

        register(new QuestDef("cb_27", QuestCategory.COMBAT,
            "Shield Wall",
            desc("Kite Shields provide extra protection", "and knockback resistance. Equip one", "in your off-hand."),
            27, prereqs("cb_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip a Kite Shield") },
            new QuestReward[]{ coins(200), skillXp("COMBAT", 20) },
            false
        ));

        register(new QuestDef("cb_28", QuestCategory.COMBAT,
            "Partial Set",
            desc("Wearing 2 pieces of the same armor", "set activates its first bonus tier.", "Check your stats to see the effect."),
            28, prereqs("cb_21"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Activate a 2-piece set bonus") },
            new QuestReward[]{ coins(300) },
            false
        ));

        register(new QuestDef("cb_29", QuestCategory.COMBAT,
            "Combo Strike",
            desc("Weapons have directional attack", "combos. Chain consecutive hits to", "cycle through the combo sequence."),
            29, prereqs("cb_05"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Complete a full weapon combo chain") },
            new QuestReward[]{ coins(250), skillXp("COMBAT", 25) },
            false
        ));

        register(new QuestDef("cb_30", QuestCategory.COMBAT,
            "Both Blades",
            desc("Equip one-handed weapons in both", "hands to dual-wield. Attacks", "alternate between main and off-hand."),
            30, prereqs("cb_29"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Kill a mob while dual-wielding") },
            new QuestReward[]{ coins(350), skillXp("COMBAT", 30) },
            false
        ));

        register(new QuestDef("cb_31", QuestCategory.COMBAT,
            "Warrior's Fury",
            desc("Warriors wield raw power. Use", "Shattering Throw, Shout, or Charge", "to dominate the battlefield."),
            31, prereqs("gs_19"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use a Warrior spell") },
            new QuestReward[]{ coins(300), skillXp("COMBAT", 30) },
            false
        ));

        register(new QuestDef("cb_32", QuestCategory.COMBAT,
            "School of Magic",
            desc("Spells belong to schools: Arcane,", "Fire, Frost, Healing, Lightning,", "and more. Cast from 3 different schools."),
            32, prereqs("cb_22"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Cast spells from 3 spell schools") },
            new QuestReward[]{ coins(400), skillXp("ARCANE", 35) },
            false
        ));

        register(new QuestDef("cb_33", QuestCategory.COMBAT,
            "Field Medic",
            desc("Paladin healing spells can target", "allies. Use Heal or Flash Heal", "on a party member or nearby player."),
            33, prereqs("cb_21"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Heal another player with a spell") },
            new QuestReward[]{ coins(350), skillXp("ARCANE", 30) },
            true
        ));

        register(new QuestDef("cb_34", QuestCategory.COMBAT,
            "Arena Spellcaster",
            desc("Put your class build to the test.", "Use at least 2 class spells during", "a single Arena run."),
            34, prereqs("cb_17", "cb_22"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use 2 class spells in an Arena run") },
            new QuestReward[]{ coins(500), skillXp("COMBAT", 40) },
            false
        ));

        register(new QuestDef("cb_35", QuestCategory.COMBAT,
            "Enchanted Spellcaster",
            desc("Equip a weapon with a spell enchantment.", "Spell Power, Sunfire, Soulfrost, Energize,", "Spell Crit, Spell Haste, or Magic Protection."),
            35, prereqs("cb_22"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip a weapon with a spell enchantment") },
            new QuestReward[]{ coins(400), skillXp("ARCANE", 35) },
            false
        ));

        register(new QuestDef("cb_36", QuestCategory.COMBAT,
            "Shadow Strike",
            desc("Use the Vanish spell to enter stealth,", "then attack an enemy to break it.", "The element of surprise is deadly."),
            36, prereqs("cb_23"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Break stealth by attacking an enemy") },
            new QuestReward[]{ coins(350), skillXp("SURVIVAL", 30) },
            false
        ));

        register(new QuestDef("cb_37", QuestCategory.COMBAT,
            "Nimble Feet",
            desc("Dodge an incoming attack! The DODGE_CHANCE", "attribute gives a % chance to avoid", "all damage from a hit."),
            37, prereqs("cb_04"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Dodge an attack via DODGE_CHANCE") },
            new QuestReward[]{ coins(200), skillXp("COMBAT", 20) },
            false
        ));

        register(new QuestDef("cb_38", QuestCategory.COMBAT,
            "Scroll Scholar",
            desc("Spell scrolls are consumable items that", "permanently teach a spell. Find them", "in dungeon loot."),
            38, prereqs("cb_22"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use a spell scroll to permanently learn a spell") },
            new QuestReward[]{ coins(300), skillXp("ARCANE", 25) },
            false
        ));

        register(new QuestDef("cb_39", QuestCategory.COMBAT,
            "Tome of Power",
            desc("Spell books grant access to all spells", "of a school while held in your offhand."),
            39, prereqs("cb_22"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip a spell book in your offhand to access its spells") },
            new QuestReward[]{ coins(400), skillXp("ARCANE", 30) },
            false
        ));
    }

    // ═══════════════════════════════════════════
    // CLASS QUESTS — 25 quests (5 per class)
    // ═══════════════════════════════════════════

    private static void registerClassQuests() {

        // ─── PALADIN (cp_01 through cp_05) ───

        registerClassQuest(new QuestDef("cp_01", QuestCategory.CLASS_QUESTS,
            "The Paladin's Oath",
            desc("Begin your journey as a Paladin.", "Equip a claymore or great hammer", "to take your first oath.", "[Requires Paladin]"),
            1, prereqs("gs_19"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip a claymore or great hammer") },
            new QuestReward[]{ coins(300), skillXp("COMBAT", 30) },
            false
        ), "PALADIN");

        registerClassQuest(new QuestDef("cp_02", QuestCategory.CLASS_QUESTS,
            "Shield Bearer",
            desc("A Paladin stands between danger", "and their allies. Equip a kite", "shield to complete your defense.", "[Requires Paladin]"),
            2, prereqs("cp_01"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip a kite shield") },
            new QuestReward[]{ coins(300), skillXp("COMBAT", 25) },
            false
        ), "PALADIN");

        registerClassQuest(new QuestDef("cp_03", QuestCategory.CLASS_QUESTS,
            "First Aid",
            desc("Paladins channel holy light to mend", "wounds. Cast Heal on yourself", "to prove your faith.", "[Requires Paladin]"),
            3, prereqs("cp_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Cast Heal on yourself") },
            new QuestReward[]{ coins(400), skillXp("ARCANE", 30) },
            false
        ), "PALADIN");

        registerClassQuest(new QuestDef("cp_04", QuestCategory.CLASS_QUESTS,
            "Holy Crusade",
            desc("Lead your party through darkness.", "Complete a dungeon as a Paladin", "to prove your resolve.", "[Requires Paladin]"),
            4, prereqs("cp_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Complete a dungeon as Paladin") },
            new QuestReward[]{ coins(500), skillXp("COMBAT", 40) },
            true
        ), "PALADIN");

        registerClassQuest(new QuestDef("cp_05", QuestCategory.CLASS_QUESTS,
            "Beacon of Light",
            desc("The ultimate expression of a Paladin's", "power. Cast Circle of Healing while", "in a party to shield all allies.", "[Requires Paladin]"),
            5, prereqs("cp_04"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Cast Circle of Healing in a party") },
            new QuestReward[]{ coins(800), skillXp("ARCANE", 50) },
            true
        ), "PALADIN");

        // ─── WARRIOR (cw_01 through cw_05) ───

        registerClassQuest(new QuestDef("cw_01", QuestCategory.CLASS_QUESTS,
            "Battle Ready",
            desc("Warriors are the frontline. Equip", "warrior armor to steel yourself", "for the battles ahead.", "[Requires Warrior]"),
            6, prereqs("gs_19"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip warrior armor") },
            new QuestReward[]{ coins(300), skillXp("COMBAT", 30) },
            false
        ), "WARRIOR");

        registerClassQuest(new QuestDef("cw_02", QuestCategory.CLASS_QUESTS,
            "Berserker's Fury",
            desc("Raw aggression wins fights. Use", "Charge and Shout in combat to", "overwhelm your enemies.", "[Requires Warrior]"),
            7, prereqs("cw_01"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use Charge and Shout in combat") },
            new QuestReward[]{ coins(400), skillXp("COMBAT", 35) },
            false
        ), "WARRIOR");

        registerClassQuest(new QuestDef("cw_03", QuestCategory.CLASS_QUESTS,
            "Cleave Master",
            desc("Warriors excel at crowd control.", "Hit 3 or more enemies with a", "single sweeping attack.", "[Requires Warrior]"),
            8, prereqs("cw_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Hit 3+ enemies with one sweeping attack") },
            new QuestReward[]{ coins(400), skillXp("COMBAT", 35) },
            false
        ), "WARRIOR");

        registerClassQuest(new QuestDef("cw_04", QuestCategory.CLASS_QUESTS,
            "Arena Champion",
            desc("The Arena is where Warriors shine.", "Win an Arena bout to prove", "you are the strongest.", "[Requires Warrior]"),
            9, prereqs("cw_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Win an Arena bout") },
            new QuestReward[]{ coins(500), skillXp("COMBAT", 40) },
            false
        ), "WARRIOR");

        registerClassQuest(new QuestDef("cw_05", QuestCategory.CLASS_QUESTS,
            "Warlord",
            desc("Only the most dedicated Warriors", "earn this title. Push your Combat", "skill to its peak.", "[Requires Warrior]"),
            10, prereqs("cw_04"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "COMBAT", 25, "Reach Combat level 25") },
            new QuestReward[]{ coins(1000), skillXp("COMBAT", 60) },
            false
        ), "WARRIOR");

        // ─── WIZARD (cm_01 through cm_05) ───

        registerClassQuest(new QuestDef("cm_01", QuestCategory.CLASS_QUESTS,
            "Apprentice Mage",
            desc("Every great Wizard starts here.", "Cast Arcane Bolt to begin your", "journey into the arcane arts.", "[Requires Wizard]"),
            11, prereqs("gs_19"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Cast Arcane Bolt") },
            new QuestReward[]{ coins(300), skillXp("ARCANE", 30) },
            false
        ), "WIZARD");

        registerClassQuest(new QuestDef("cm_02", QuestCategory.CLASS_QUESTS,
            "Elemental Student",
            desc("Master multiple elements. Cast both", "a Fire spell and a Frost spell", "to expand your repertoire.", "[Requires Wizard]"),
            12, prereqs("cm_01"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Cast a Fire and Frost spell") },
            new QuestReward[]{ coins(400), skillXp("ARCANE", 35) },
            false
        ), "WIZARD");

        registerClassQuest(new QuestDef("cm_03", QuestCategory.CLASS_QUESTS,
            "Spell Combo",
            desc("True Wizards chain spells in rapid", "succession. Cast 3 different spells", "in a single fight.", "[Requires Wizard]"),
            13, prereqs("cm_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Cast 3 different spells in one fight") },
            new QuestReward[]{ coins(400), skillXp("ARCANE", 35) },
            false
        ), "WIZARD");

        registerClassQuest(new QuestDef("cm_04", QuestCategory.CLASS_QUESTS,
            "Arcane Scholar",
            desc("Spell Books contain entire schools", "of magic. Acquire one to unlock", "a world of spells.", "[Requires Wizard]"),
            14, prereqs("cm_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Acquire a Spell Book") },
            new QuestReward[]{ coins(500), skillXp("ARCANE", 40) },
            false
        ), "WIZARD");

        registerClassQuest(new QuestDef("cm_05", QuestCategory.CLASS_QUESTS,
            "Archmage",
            desc("The pinnacle of magical mastery.", "Push your Arcane skill to its", "absolute peak.", "[Requires Wizard]"),
            15, prereqs("cm_04"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "ARCANE", 25, "Reach Arcane level 25") },
            new QuestReward[]{ coins(1000), skillXp("ARCANE", 60) },
            false
        ), "WIZARD");

        // ─── ROGUE (cr_01 through cr_05) ───

        registerClassQuest(new QuestDef("cr_01", QuestCategory.CLASS_QUESTS,
            "Shadow's Edge",
            desc("Rogues live by the blade. Equip", "a dagger or sickle to begin", "your life in the shadows.", "[Requires Rogue]"),
            16, prereqs("gs_19"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip a dagger or sickle") },
            new QuestReward[]{ coins(300), skillXp("SURVIVAL", 30) },
            false
        ), "ROGUE");

        registerClassQuest(new QuestDef("cr_02", QuestCategory.CLASS_QUESTS,
            "First Strike",
            desc("Disappear from sight. Use Vanish", "in combat to slip into the", "shadows unseen.", "[Requires Rogue]"),
            17, prereqs("cr_01"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use Vanish in combat") },
            new QuestReward[]{ coins(400), skillXp("SURVIVAL", 35) },
            false
        ), "ROGUE");

        registerClassQuest(new QuestDef("cr_03", QuestCategory.CLASS_QUESTS,
            "Backstab",
            desc("The deadliest strike comes from", "behind. Attack an enemy while", "in stealth for massive damage.", "[Requires Rogue]"),
            18, prereqs("cr_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Attack from stealth") },
            new QuestReward[]{ coins(400), skillXp("SURVIVAL", 35) },
            false
        ), "ROGUE");

        registerClassQuest(new QuestDef("cr_04", QuestCategory.CLASS_QUESTS,
            "Shadow Dancer",
            desc("Master the art of teleportation.", "Use Shadow Step 5 times to", "become untouchable.", "[Requires Rogue]"),
            19, prereqs("cr_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use Shadow Step 5 times") },
            new QuestReward[]{ coins(500), skillXp("SURVIVAL", 40) },
            false
        ), "ROGUE");

        registerClassQuest(new QuestDef("cr_05", QuestCategory.CLASS_QUESTS,
            "Master Assassin",
            desc("A true Rogue hunts the most", "dangerous prey. Complete a bounty", "hunt to earn this title.", "[Requires Rogue]"),
            20, prereqs("cr_04"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Complete a bounty hunt") },
            new QuestReward[]{ coins(1000), skillXp("SURVIVAL", 60) },
            false
        ), "ROGUE");

        // ─── RANGER (ca_01 through ca_05) ───

        registerClassQuest(new QuestDef("ca_01", QuestCategory.CLASS_QUESTS,
            "First Arrow",
            desc("Rangers command the battlefield from", "afar. Equip a bow or crossbow", "and a quiver to begin.", "[Requires Ranger]"),
            21, prereqs("gs_19"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip a bow or crossbow and a quiver") },
            new QuestReward[]{ coins(300), skillXp("SURVIVAL", 30) },
            false
        ), "RANGER");

        registerClassQuest(new QuestDef("ca_02", QuestCategory.CLASS_QUESTS,
            "Marked Prey",
            desc("Precision over power. Use Power", "Shot on an enemy to deliver", "a devastating focused strike.", "[Requires Ranger]"),
            22, prereqs("ca_01"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use Power Shot on an enemy") },
            new QuestReward[]{ coins(400), skillXp("SURVIVAL", 35) },
            false
        ), "RANGER");

        registerClassQuest(new QuestDef("ca_03", QuestCategory.CLASS_QUESTS,
            "Rain of Arrows",
            desc("Unleash a volley. Use Barrage", "to blanket an area with arrows,", "hitting everything in range.", "[Requires Ranger]"),
            23, prereqs("ca_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use Barrage") },
            new QuestReward[]{ coins(400), skillXp("SURVIVAL", 35) },
            false
        ), "RANGER");

        registerClassQuest(new QuestDef("ca_04", QuestCategory.CLASS_QUESTS,
            "Nature's Trap",
            desc("Rangers bend nature to their will.", "Use Entangling Roots in a dungeon", "to lock enemies in place.", "[Requires Ranger]"),
            24, prereqs("ca_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use Entangling Roots in a dungeon") },
            new QuestReward[]{ coins(500), skillXp("SURVIVAL", 40) },
            true
        ), "RANGER");

        registerClassQuest(new QuestDef("ca_05", QuestCategory.CLASS_QUESTS,
            "Grandmaster Archer",
            desc("The ultimate Ranger has honed their", "survival instincts to perfection.", "Reach the peak of Survival.", "[Requires Ranger]"),
            25, prereqs("ca_04"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "SURVIVAL", 25, "Reach Survival level 25") },
            new QuestReward[]{ coins(1000), skillXp("SURVIVAL", 60) },
            false
        ), "RANGER");
    }

    // ═══════════════════════════════════════════
    // DUNGEONS — 13 quests
    // ═══════════════════════════════════════════

    private static void registerDungeons() {
        register(new QuestDef("dg_01", QuestCategory.DUNGEONS,
            "Into the Depths",
            desc("Dungeons are procedural pocket", "dimensions with bosses and loot.", "Enter one to begin."),
            1, prereqs("cb_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Enter a dungeon") },
            new QuestReward[]{ coins(200) },
            true
        ));

        register(new QuestDef("dg_02", QuestCategory.DUNGEONS,
            "First Clear",
            desc("Defeat the dungeon boss to", "complete a Normal clear."),
            2, prereqs("dg_01"),
            new QuestTask[]{ task(QuestTaskType.DUNGEON_CLEAR, "NORMAL", 1, "Clear a Normal dungeon") },
            new QuestReward[]{ coins(300), skillXp("COMBAT", 30) },
            true
        ));

        register(new QuestDef("dg_03", QuestCategory.DUNGEONS,
            "Herald's Call",
            desc("The Herald NPC offers special", "dungeon quests with coin rewards."),
            3, prereqs("dg_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Accept a Herald quest") },
            new QuestReward[]{ coins(150) },
            false
        ));

        register(new QuestDef("dg_04", QuestCategory.DUNGEONS,
            "Hardened",
            desc("Hard dungeons have tougher mobs", "and better loot. Step up."),
            4, prereqs("dg_02"),
            new QuestTask[]{ task(QuestTaskType.DUNGEON_CLEAR, "HARD", 1, "Clear a Hard dungeon") },
            new QuestReward[]{ coins(500), skillXp("COMBAT", 50) },
            true
        ));

        register(new QuestDef("dg_05", QuestCategory.DUNGEONS,
            "Dungeon Veteran",
            desc("Consistency is key.", "Run dungeons regularly for best loot."),
            5, prereqs("dg_02"),
            new QuestTask[]{ task(QuestTaskType.DUNGEON_CLEAR, "NORMAL", 5, "Clear 5 Normal dungeons") },
            new QuestReward[]{ coins(400), skillXp("COMBAT", 40) },
            true
        ));

        register(new QuestDef("dg_06", QuestCategory.DUNGEONS,
            "Nightmare Survivor",
            desc("Nightmare dungeons push your limits.", "Prepare your best gear."),
            6, prereqs("dg_04"),
            new QuestTask[]{ task(QuestTaskType.DUNGEON_CLEAR, "NIGHTMARE", 1, "Clear a Nightmare dungeon") },
            new QuestReward[]{ coins(750), skillXp("COMBAT", 75) },
            true
        ));

        register(new QuestDef("dg_07", QuestCategory.DUNGEONS,
            "Infernal Conqueror",
            desc("The hardest standard difficulty.", "Only the elite can survive."),
            7, prereqs("dg_06"),
            new QuestTask[]{ task(QuestTaskType.DUNGEON_CLEAR, "INFERNAL", 1, "Clear an Infernal dungeon") },
            new QuestReward[]{ coins(1000), skillXp("COMBAT", 100) },
            true
        ));

        register(new QuestDef("dg_08", QuestCategory.DUNGEONS,
            "Dungeon Master",
            desc("Prove mastery through volume.", "Each run sharpens your skills."),
            8, prereqs("dg_05"),
            new QuestTask[]{ task(QuestTaskType.DUNGEON_CLEAR, "ANY", 20, "Clear 20 dungeons total") },
            new QuestReward[]{ coins(1500) },
            true
        ));

        register(new QuestDef("dg_09", QuestCategory.DUNGEONS,
            "Streak Runner",
            desc("Clear dungeons back to back", "without failing. Consecutive clears", "grant bonus loot."),
            9, prereqs("dg_08"),
            new QuestTask[]{ task(QuestTaskType.STAT_CHECK, "consecutiveClears", 5, "Reach a 5-clear streak") },
            new QuestReward[]{ coins(2000) },
            true
        ));

        register(new QuestDef("dg_10", QuestCategory.DUNGEONS,
            "Absolute Legend",
            desc("Clear the highest tier.", "This is the ultimate test."),
            10, prereqs("dg_07"),
            new QuestTask[]{ task(QuestTaskType.DUNGEON_CLEAR, "ANY", 50, "Clear 50 dungeons total") },
            new QuestReward[]{ coins(5000), skillXp("COMBAT", 200) },
            true
        ));

        register(new QuestDef("dg_11", QuestCategory.DUNGEONS,
            "Safety Net",
            desc("Dungeon Insurance protects your items", "if you fail a run. Purchase it", "before entering."),
            11, prereqs("dg_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Purchase Dungeon Insurance") },
            new QuestReward[]{ coins(200) },
            false
        ));

        register(new QuestDef("dg_12", QuestCategory.DUNGEONS,
            "Party Dungeon",
            desc("Enter a dungeon with your party.", "Coordination is key — bosses scale", "with party size."),
            12, prereqs("dg_02", "gs_10"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Clear a dungeon with a party") },
            new QuestReward[]{ coins(400), skillXp("COMBAT", 40) },
            true
        ));

        register(new QuestDef("dg_13", QuestCategory.DUNGEONS,
            "New Game+",
            desc("After prestiging, New Game+ dungeons", "unlock with extreme difficulty", "and legendary loot."),
            13, prereqs("dg_07", "ms_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Clear a New Game+ dungeon") },
            new QuestReward[]{ coins(3000), skillXp("COMBAT", 150) },
            true
        ));
    }

    // ═══════════════════════════════════════════
    // COLONY & TERRITORY — 14 quests
    // ═══════════════════════════════════════════

    private static void registerColony() {
        register(new QuestDef("cl_01", QuestCategory.COLONY,
            "First Citizen",
            desc("Recruit your first citizen!", "Citizens can be workers,", "soldiers, or merchants."),
            1, prereqs("gs_07"),
            new QuestTask[]{ task(QuestTaskType.CITIZEN_COUNT, 1, "Recruit 1 citizen") },
            new QuestReward[]{ coins(200), skillXp("SURVIVAL", 20) },
            false
        ));

        register(new QuestDef("cl_02", QuestCategory.COLONY,
            "Town Founder",
            desc("Claim your first chunk of land.", "This establishes your territory."),
            2, prereqs("cl_01"),
            new QuestTask[]{ task(QuestTaskType.CLAIM_CHUNKS, 1, "Claim 1 chunk") },
            new QuestReward[]{ coins(300) },
            false
        ));

        register(new QuestDef("cl_03", QuestCategory.COLONY,
            "Growing Settlement",
            desc("A handful of citizens can start", "making a real difference."),
            3, prereqs("cl_01"),
            new QuestTask[]{ task(QuestTaskType.CITIZEN_COUNT, 3, "Have 3 citizens") },
            new QuestReward[]{ coins(300), skillXp("SURVIVAL", 30) },
            false
        ));

        register(new QuestDef("cl_04", QuestCategory.COLONY,
            "Worker Management",
            desc("Assign a citizen to a job.", "Workers gather resources automatically."),
            4, prereqs("cl_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Assign a citizen to a job") },
            new QuestReward[]{ coins(200), skillXp("FARMING", 25) },
            false
        ));

        register(new QuestDef("cl_05", QuestCategory.COLONY,
            "Military Might",
            desc("Recruit soldiers to defend", "your colony from raids and sieges."),
            5, prereqs("cl_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Recruit 2 military citizens") },
            new QuestReward[]{ coins(400), skillXp("COMBAT", 30) },
            false
        ));

        register(new QuestDef("cl_06", QuestCategory.COLONY,
            "Territory Expansion",
            desc("Expand your territory to protect", "more of your builds."),
            6, prereqs("cl_02"),
            new QuestTask[]{ task(QuestTaskType.CLAIM_CHUNKS, 5, "Claim 5 chunks") },
            new QuestReward[]{ coins(500) },
            false
        ));

        register(new QuestDef("cl_07", QuestCategory.COLONY,
            "Thriving Colony",
            desc("A colony of 10 citizens is", "a real achievement."),
            7, prereqs("cl_03"),
            new QuestTask[]{ task(QuestTaskType.CITIZEN_COUNT, 10, "Have 10 citizens") },
            new QuestReward[]{ coins(750), skillXp("SURVIVAL", 50) },
            false
        ));

        register(new QuestDef("cl_08", QuestCategory.COLONY,
            "Trade Network",
            desc("Merchant citizens sell items", "to other players automatically."),
            8, prereqs("cl_04"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Have a merchant citizen") },
            new QuestReward[]{ coins(500) },
            false
        ));

        register(new QuestDef("cl_09", QuestCategory.COLONY,
            "Army Commander",
            desc("Build a formidable fighting force", "to handle any siege."),
            9, prereqs("cl_05"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Have 8 military citizens") },
            new QuestReward[]{ coins(1000), skillXp("COMBAT", 50) },
            false
        ));

        register(new QuestDef("cl_10", QuestCategory.COLONY,
            "Empire Builder",
            desc("A massive colony with vast", "territory. You are a true leader."),
            10, prereqs("cl_07", "cl_06"),
            new QuestTask[]{
                task(QuestTaskType.CITIZEN_COUNT, 25, "Have 25 citizens"),
                task(QuestTaskType.CLAIM_CHUNKS, 15, "Claim 15 chunks")
            },
            new QuestReward[]{ coins(2000), skillXp("SURVIVAL", 100) },
            false
        ));

        register(new QuestDef("cl_11", QuestCategory.COLONY,
            "Interior Design",
            desc("Furniture adds character to builds.", "Place chairs, desks, lamps, and more", "from the furniture crafting system."),
            11, prereqs("cl_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Place 5 furniture pieces") },
            new QuestReward[]{ coins(200), skillXp("SURVIVAL", 20) },
            false
        ));

        register(new QuestDef("cl_12", QuestCategory.COLONY,
            "Blueprint Builder",
            desc("Schematics let you preview and", "build structures block by block.", "Load one and start placing."),
            12, prereqs("cl_06"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use a schematic to place a structure") },
            new QuestReward[]{ coins(400), skillXp("SURVIVAL", 30) },
            false
        ));

        register(new QuestDef("cl_13", QuestCategory.COLONY,
            "Diverse Workforce",
            desc("Assign citizens to 3 different jobs.", "Lumberjack, Fisherman, Farmer, Miner,", "Merchant — each has unique value."),
            13, prereqs("cl_04"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Have 3 different worker types") },
            new QuestReward[]{ coins(300), skillXp("SURVIVAL", 25) },
            false
        ));

        register(new QuestDef("cl_14", QuestCategory.COLONY,
            "Elite Guard",
            desc("Recruit specialized military citizens.", "Bowmen, Horsemen, and Captains each", "bring unique combat strengths."),
            14, prereqs("cl_05"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Have 3 different recruit types") },
            new QuestReward[]{ coins(500), skillXp("COMBAT", 30) },
            false
        ));

        // ── MegaColonies Walkthrough Progression ──

        register(new QuestDef("cl_15", QuestCategory.COLONY,
            "Supply Camp",
            desc("Place a Supply Camp or Ship", "to begin your colony journey.", "Find the Town Hall and Build Tool inside."),
            15, noPrereqs(),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Place a Supply Camp or Ship") },
            new QuestReward[]{ coins(100) },
            false
        ));

        register(new QuestDef("cl_16", QuestCategory.COLONY,
            "Found a Colony",
            desc("Place the Town Hall block to", "establish your colony. 4 citizens", "will arrive immediately."),
            16, prereqs("cl_15"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "town_hall", 1, "Place a Town Hall") },
            new QuestReward[]{ coins(200) },
            false
        ));

        register(new QuestDef("cl_17", QuestCategory.COLONY,
            "Bob the Builder",
            desc("Build a Builder's Hut so your", "Builder can construct buildings.", "They must build their own hut first!"),
            17, prereqs("cl_16"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "builder", 1, "Place a Builder's Hut") },
            new QuestReward[]{ coins(200), item("minecraft:oak_planks", 64, "Oak Planks") },
            false
        ));

        register(new QuestDef("cl_18", QuestCategory.COLONY,
            "Come One, Come All",
            desc("Build a Tavern for housing and", "to attract visitors who can be", "recruited into your colony."),
            18, prereqs("cl_17"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "tavern", 1, "Place a Tavern") },
            new QuestReward[]{ coins(300) },
            false
        ));

        register(new QuestDef("cl_19", QuestCategory.COLONY,
            "Gone Fishing",
            desc("Build a Fisher's Hut for a", "quick and reliable food source."),
            19, prereqs("cl_18"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "fisherman", 1, "Place a Fisher's Hut") },
            new QuestReward[]{ coins(200), item("minecraft:fishing_rod", 1, "Fishing Rod") },
            false
        ));

        register(new QuestDef("cl_20", QuestCategory.COLONY,
            "Timber!",
            desc("Build a Forester's Hut so your", "Forester can gather wood for", "construction and crafting."),
            20, prereqs("cl_19"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "lumberjack", 1, "Place a Forester's Hut") },
            new QuestReward[]{ coins(200) },
            false
        ));

        register(new QuestDef("cl_21", QuestCategory.COLONY,
            "Diggy Diggy Hole",
            desc("Build a Mine so your Miner", "can extract stone and ores."),
            21, prereqs("cl_20"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "miner", 1, "Place a Mine") },
            new QuestReward[]{ coins(300), item("minecraft:iron_pickaxe", 1, "Iron Pickaxe") },
            false
        ));

        register(new QuestDef("cl_22", QuestCategory.COLONY,
            "Automate Deliveries",
            desc("Build a Warehouse and Courier's", "Hut for automatic item transport."),
            22, prereqs("cl_21"),
            new QuestTask[]{
                task(QuestTaskType.BUILDING_PLACED, "warehouse", 1, "Place a Warehouse"),
                task(QuestTaskType.BUILDING_PLACED, "deliveryman", 1, "Place a Courier's Hut")
            },
            new QuestReward[]{ coins(400) },
            false
        ));

        register(new QuestDef("cl_23", QuestCategory.COLONY,
            "Standing Guard",
            desc("Build a Guard Tower to protect", "your colony from raiders.", "Raids begin at 7 citizens!"),
            23, prereqs("cl_22"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "guard_tower", 1, "Place a Guard Tower") },
            new QuestReward[]{ coins(300), item("minecraft:iron_sword", 1, "Iron Sword") },
            false
        ));

        register(new QuestDef("cl_24", QuestCategory.COLONY,
            "Higher Learning",
            desc("Build a University to research", "upgrades for your entire colony."),
            24, prereqs("cl_22"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "university", 1, "Place a University") },
            new QuestReward[]{ coins(500), item("minecraft:book", 5, "Books") },
            false
        ));

        register(new QuestDef("cl_25", QuestCategory.COLONY,
            "Iron Will",
            desc("Build a Blacksmith's Hut to craft", "tools and armor for your workers."),
            25, prereqs("cl_22"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "blacksmith", 1, "Place a Blacksmith's Hut") },
            new QuestReward[]{ coins(300), item("minecraft:iron_ingot", 8, "Iron Ingots") },
            false
        ));

        register(new QuestDef("cl_26", QuestCategory.COLONY,
            "Hot Stuff",
            desc("Build a Smeltery to smelt ores", "into ingots with fortune bonus."),
            26, prereqs("cl_25"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "smeltery", 1, "Place a Smeltery") },
            new QuestReward[]{ coins(300) },
            false
        ));

        register(new QuestDef("cl_27", QuestCategory.COLONY,
            "Fortified",
            desc("Build a Barracks to house up to", "20 guards across 4 towers."),
            27, prereqs("cl_23"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "barracks", 1, "Place a Barracks") },
            new QuestReward[]{ coins(500), item("minecraft:iron_block", 2, "Iron Blocks") },
            false
        ));

        register(new QuestDef("cl_28", QuestCategory.COLONY,
            "Ancient Knowledge",
            desc("Build an Enchanter's Tower to", "create enchanted books using", "Ancient Tomes from raids."),
            28, prereqs("cl_26"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "enchanter", 1, "Place an Enchanter's Tower") },
            new QuestReward[]{ coins(400), item("megamod:ancient_tome", 1, "Ancient Tome") },
            false
        ));

        register(new QuestDef("cl_29", QuestCategory.COLONY,
            "Into the Nether",
            desc("Build a Nether Mine to gather", "exotic resources from the Nether."),
            29, prereqs("cl_24"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_PLACED, "nether_worker", 1, "Place a Nether Mine") },
            new QuestReward[]{ coins(600), item("minecraft:obsidian", 4, "Obsidian") },
            false
        ));

        register(new QuestDef("cl_30", QuestCategory.COLONY,
            "Fully Upgraded",
            desc("Upgrade any building to level 5.", "This requires a level 5 Builder."),
            30, prereqs("cl_22"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_LEVEL, "builder", 5, "Upgrade Builder to level 5") },
            new QuestReward[]{ coins(1000), item("minecraft:diamond", 3, "Diamonds") },
            false
        ));

        // Population milestones
        register(new QuestDef("cl_31", QuestCategory.COLONY,
            "Outpost",
            desc("Your colony has grown to 25", "citizens. A true outpost!"),
            31, prereqs("cl_10"),
            new QuestTask[]{ task(QuestTaskType.CITIZEN_COUNT, 25, "Have 25 citizens") },
            new QuestReward[]{ coins(1500), item("minecraft:emerald", 10, "Emeralds") },
            false
        ));

        register(new QuestDef("cl_32", QuestCategory.COLONY,
            "Hamlet",
            desc("50 citizens! Your settlement", "is becoming a proper hamlet."),
            32, prereqs("cl_31"),
            new QuestTask[]{ task(QuestTaskType.CITIZEN_COUNT, 50, "Have 50 citizens") },
            new QuestReward[]{ coins(3000), item("minecraft:emerald", 25, "Emeralds") },
            false
        ));

        register(new QuestDef("cl_33", QuestCategory.COLONY,
            "Village",
            desc("100 citizens under your rule.", "A thriving village!"),
            33, prereqs("cl_32"),
            new QuestTask[]{ task(QuestTaskType.CITIZEN_COUNT, 100, "Have 100 citizens") },
            new QuestReward[]{ coins(5000), item("minecraft:emerald", 50, "Emeralds") },
            false
        ));

        register(new QuestDef("cl_34", QuestCategory.COLONY,
            "City",
            desc("150 citizens! Your city is a", "marvel of organization."),
            34, prereqs("cl_33"),
            new QuestTask[]{ task(QuestTaskType.CITIZEN_COUNT, 150, "Have 150 citizens") },
            new QuestReward[]{ coins(10000), item("minecraft:diamond_block", 1, "Diamond Block") },
            false
        ));

        register(new QuestDef("cl_35", QuestCategory.COLONY,
            "Metropolis",
            desc("200 citizens! You've built a", "grand metropolis. Legendary!"),
            35, prereqs("cl_34"),
            new QuestTask[]{ task(QuestTaskType.CITIZEN_COUNT, 200, "Have 200 citizens") },
            new QuestReward[]{ coins(25000), item("minecraft:netherite_ingot", 1, "Netherite Ingot") },
            false
        ));

        register(new QuestDef("cl_36", QuestCategory.COLONY,
            "Raid Survivor",
            desc("Defend your colony from a raid.", "Raiders attack at 7+ citizens."),
            36, prereqs("cl_23"),
            new QuestTask[]{ task(QuestTaskType.SURVIVE_RAID, 1, "Survive a raid") },
            new QuestReward[]{ coins(500), skillXp("COMBAT", 50) },
            false
        ));

        register(new QuestDef("cl_37", QuestCategory.COLONY,
            "Master Craftsmen",
            desc("Build every crafter building type.", "Baker, Blacksmith, Sawmill, Stonemason,", "and all the rest!"),
            37, prereqs("cl_25"),
            new QuestTask[]{ task(QuestTaskType.BUILDING_COUNT, 15, "Have 15 different building types") },
            new QuestReward[]{ coins(3000), item("minecraft:diamond", 5, "Diamonds") },
            false
        ));
    }

    // ═══════════════════════════════════════════
    // EXPLORATION & COLLECTION — 24 quests
    // ═══════════════════════════════════════════

    private static void registerExploration() {
        register(new QuestDef("ex_01", QuestCategory.EXPLORATION,
            "Curious Explorer",
            desc("Break blocks, explore caves,", "and discover the world."),
            1, prereqs("gs_03"),
            new QuestTask[]{ task(QuestTaskType.BLOCKS_BROKEN, 500, "Break 500 blocks") },
            new QuestReward[]{ coins(100), skillXp("MINING", 15) },
            false
        ));

        register(new QuestDef("ex_02", QuestCategory.EXPLORATION,
            "Museum Patron",
            desc("The Museum collects specimens from", "around the world. Donate your first item."),
            2, prereqs("gs_03"),
            new QuestTask[]{ task(QuestTaskType.MUSEUM_DONATIONS, 1, "Donate 1 item to the Museum") },
            new QuestReward[]{ coins(200) },
            false
        ));

        register(new QuestDef("ex_03", QuestCategory.EXPLORATION,
            "Deep Miner",
            desc("The Mining skill tree unlocks", "ore bonuses and vein mining."),
            3, prereqs("ex_01"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "MINING", 5, "Reach Mining level 5") },
            new QuestReward[]{ coins(200), skillXp("MINING", 25) },
            false
        ));

        register(new QuestDef("ex_04", QuestCategory.EXPLORATION,
            "Collector's Eye",
            desc("The more you donate, the more", "the Museum rewards you."),
            4, prereqs("ex_02"),
            new QuestTask[]{ task(QuestTaskType.MUSEUM_DONATIONS, 10, "Donate 10 items to the Museum") },
            new QuestReward[]{ coins(400), skillXp("SURVIVAL", 30) },
            false
        ));

        register(new QuestDef("ex_05", QuestCategory.EXPLORATION,
            "Dimension Hopper",
            desc("Travel to a new dimension.", "The Nether, End, or a MegaMod", "pocket dimension all count."),
            5, prereqs("gs_03"),
            new QuestTask[]{ task(QuestTaskType.VISIT_DIMENSION, 1, "Visit a non-Overworld dimension") },
            new QuestReward[]{ coins(300) },
            false
        ));

        register(new QuestDef("ex_06", QuestCategory.EXPLORATION,
            "Bounty Hunter",
            desc("Complete bounty hunts for", "extra coins and combat experience."),
            6, prereqs("gs_06"),
            new QuestTask[]{ task(QuestTaskType.BOUNTY_COMPLETE, 3, "Complete 3 bounty hunts") },
            new QuestReward[]{ coins(400), skillXp("COMBAT", 30) },
            false
        ));

        register(new QuestDef("ex_07", QuestCategory.EXPLORATION,
            "Master Miner",
            desc("A seasoned miner with deep", "knowledge of the underground."),
            7, prereqs("ex_03"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "MINING", 25, "Reach Mining level 25") },
            new QuestReward[]{ coins(1000) },
            false
        ));

        register(new QuestDef("ex_08", QuestCategory.EXPLORATION,
            "Museum Curator",
            desc("An impressive collection.", "Keep donating to fill all wings."),
            8, prereqs("ex_04"),
            new QuestTask[]{ task(QuestTaskType.MUSEUM_DONATIONS, 30, "Donate 30 items to the Museum") },
            new QuestReward[]{ coins(1000), skillXp("SURVIVAL", 50) },
            false
        ));

        register(new QuestDef("ex_09", QuestCategory.EXPLORATION,
            "World Explorer",
            desc("Visit multiple dimensions.", "Each offers unique challenges."),
            9, prereqs("ex_05"),
            new QuestTask[]{ task(QuestTaskType.VISIT_DIMENSION, 3, "Visit 3 different dimensions") },
            new QuestReward[]{ coins(1500) },
            false
        ));

        register(new QuestDef("ex_10", QuestCategory.EXPLORATION,
            "Veteran Bounty Hunter",
            desc("A seasoned hunter who has", "completed many contracts."),
            10, prereqs("ex_06"),
            new QuestTask[]{ task(QuestTaskType.BOUNTY_COMPLETE, 15, "Complete 15 bounty hunts") },
            new QuestReward[]{ coins(1500), skillXp("COMBAT", 75) },
            false
        ));

        register(new QuestDef("ex_11", QuestCategory.EXPLORATION,
            "Lorekeeper",
            desc("The Encyclopedia tracks everything", "you discover — mobs, relics,", "potions, bosses, and more."),
            11, prereqs("gs_08"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Discover 10 encyclopedia entries") },
            new QuestReward[]{ coins(200), skillXp("SURVIVAL", 20) },
            false
        ));

        register(new QuestDef("ex_12", QuestCategory.EXPLORATION,
            "Encyclopedia Expert",
            desc("A seasoned explorer with knowledge", "of many creatures and artifacts."),
            12, prereqs("ex_11"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Discover 30 encyclopedia entries") },
            new QuestReward[]{ coins(500), skillXp("SURVIVAL", 40) },
            false
        ));

        register(new QuestDef("ex_15", QuestCategory.EXPLORATION,
            "Backpack Upgrade",
            desc("Install a module in your backpack.", "Options: Magnet, AutoPickup, Crafting,", "Feeding, Void Filter, Smelting, and more."),
            15, prereqs("gs_13"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Install an upgrade in a backpack") },
            new QuestReward[]{ coins(200), skillXp("SURVIVAL", 20) },
            false
        ));

        register(new QuestDef("ex_16", QuestCategory.EXPLORATION,
            "Timber!",
            desc("Chop the base of a tree to fell", "the whole thing at once.", "A huge time saver."),
            16, prereqs("gs_01"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Fell a tree using Tree Felling") },
            new QuestReward[]{ coins(75), skillXp("SURVIVAL", 10) },
            false
        ));

        register(new QuestDef("ex_17", QuestCategory.EXPLORATION,
            "Green Thumb",
            desc("The Farming tree unlocks crop yield", "bonuses, auto-replant, and more.", "Keep harvesting to level it up."),
            17, prereqs("gs_15"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "FARMING", 5, "Reach Farming level 5") },
            new QuestReward[]{ coins(200), skillXp("FARMING", 25) },
            false
        ));

        register(new QuestDef("ex_18", QuestCategory.EXPLORATION,
            "Survivor's Instinct",
            desc("The Survival tree grants health regen,", "hunger efficiency, and environmental", "resistance bonuses."),
            18, prereqs("gs_16"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "SURVIVAL", 5, "Reach Survival level 5") },
            new QuestReward[]{ coins(200), skillXp("SURVIVAL", 25) },
            false
        ));

        register(new QuestDef("ex_19", QuestCategory.EXPLORATION,
            "Resource Expedition",
            desc("The Resource Dimension is a special", "world full of rare ores and materials.", "Enter it through a portal."),
            19, prereqs("ex_05"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Enter the Resource Dimension") },
            new QuestReward[]{ coins(300), skillXp("MINING", 30) },
            false
        ));

        register(new QuestDef("ex_20", QuestCategory.EXPLORATION,
            "Trophy Hunter",
            desc("Defeating mobs has a chance to", "drop their head as a trophy.", "Collect them!"),
            20, prereqs("gs_06"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Collect 5 mob heads") },
            new QuestReward[]{ coins(200), skillXp("COMBAT", 20) },
            false
        ));

        register(new QuestDef("ex_21", QuestCategory.EXPLORATION,
            "Gem Collector",
            desc("Equip a piece of jewelry. Rings", "and necklaces provide stat bonuses", "without abilities."),
            21, prereqs("gs_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip any jewelry piece") },
            new QuestReward[]{ coins(200) },
            false
        ));

        register(new QuestDef("ex_22", QuestCategory.EXPLORATION,
            "Dual Specialization",
            desc("Unlock branches in 2 different", "class trees. Be versatile!"),
            22, prereqs("gs_19"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Unlock nodes in 2 class branches") },
            new QuestReward[]{ coins(500) },
            false
        ));

        register(new QuestDef("ex_23", QuestCategory.EXPLORATION,
            "Gem Hoarder",
            desc("Collect all 6 raw gem types:", "Ruby, Topaz, Citrine, Jade,", "Sapphire, and Tanzanite."),
            23, prereqs("gs_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Collect all 6 gem types") },
            new QuestReward[]{ coins(500) },
            false
        ));

        register(new QuestDef("ex_24", QuestCategory.EXPLORATION,
            "Armed to the Quiver",
            desc("Quivers boost your ranged damage", "when held in the offhand."),
            24, prereqs("cb_24"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip a quiver in your offhand for ranged damage bonus") },
            new QuestReward[]{ coins(250), skillXp("SURVIVAL", 20) },
            false
        ));
    }

    // ═══════════════════════════════════════════
    // ECONOMY & TRADING — 13 quests
    // ═══════════════════════════════════════════

    private static void registerEconomy() {
        register(new QuestDef("ec_01", QuestCategory.ECONOMY,
            "First Purchase",
            desc("Buy an item from the Shop.", "The selection rotates daily."),
            1, prereqs("gs_05"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Buy an item from the Shop") },
            new QuestReward[]{ coins(100) },
            false
        ));

        register(new QuestDef("ec_02", QuestCategory.ECONOMY,
            "Market Seller",
            desc("List an item on the Marketplace", "for other players to buy."),
            2, prereqs("gs_05"),
            new QuestTask[]{ task(QuestTaskType.TRADE_MARKETPLACE, 1, "Complete 1 marketplace trade") },
            new QuestReward[]{ coins(200) },
            false
        ));

        register(new QuestDef("ec_03", QuestCategory.ECONOMY,
            "Savings Account",
            desc("Keep your coins safe in the Bank.", "Building savings pays off."),
            3, prereqs("gs_05"),
            new QuestTask[]{ task(QuestTaskType.REACH_BALANCE, 1000, "Have 1,000 MegaCoins total") },
            new QuestReward[]{ coins(200) },
            false
        ));

        register(new QuestDef("ec_04", QuestCategory.ECONOMY,
            "Lucky Day",
            desc("Visit the Casino dimension", "and try your luck at a game."),
            4, prereqs("gs_05"),
            new QuestTask[]{ task(QuestTaskType.CASINO_PLAY, 1, "Play 1 casino game") },
            new QuestReward[]{ coins(150) },
            false
        ));

        register(new QuestDef("ec_05", QuestCategory.ECONOMY,
            "Bounty Poster",
            desc("Post a bounty on the Bounty Board", "to request items from other players."),
            5, prereqs("ec_01"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Post a bounty on the board") },
            new QuestReward[]{ coins(200) },
            false
        ));

        register(new QuestDef("ec_06", QuestCategory.ECONOMY,
            "Active Trader",
            desc("Regular trading builds wealth.", "Use the Marketplace consistently."),
            6, prereqs("ec_02"),
            new QuestTask[]{ task(QuestTaskType.TRADE_MARKETPLACE, 10, "Complete 10 marketplace trades") },
            new QuestReward[]{ coins(500) },
            false
        ));

        register(new QuestDef("ec_07", QuestCategory.ECONOMY,
            "High Roller",
            desc("A regular at the Casino.", "Remember: the house always wins... mostly."),
            7, prereqs("ec_04"),
            new QuestTask[]{ task(QuestTaskType.CASINO_PLAY, 10, "Play 10 casino games") },
            new QuestReward[]{ coins(500) },
            false
        ));

        register(new QuestDef("ec_08", QuestCategory.ECONOMY,
            "Wealthy",
            desc("A solid fortune.", "You're well on your way to the top."),
            8, prereqs("ec_03"),
            new QuestTask[]{ task(QuestTaskType.REACH_BALANCE, 10000, "Have 10,000 MegaCoins total") },
            new QuestReward[]{ coins(1000) },
            false
        ));

        register(new QuestDef("ec_09", QuestCategory.ECONOMY,
            "Trade Mogul",
            desc("A marketplace empire.", "You drive the server economy."),
            9, prereqs("ec_06"),
            new QuestTask[]{ task(QuestTaskType.TRADE_MARKETPLACE, 50, "Complete 50 marketplace trades") },
            new QuestReward[]{ coins(1500) },
            false
        ));

        register(new QuestDef("ec_10", QuestCategory.ECONOMY,
            "Megamod Millionaire",
            desc("The richest player around.", "An unimaginable fortune."),
            10, prereqs("ec_08"),
            new QuestTask[]{ task(QuestTaskType.REACH_BALANCE, 100000, "Have 100,000 MegaCoins total") },
            new QuestReward[]{ coins(5000) },
            false
        ));

        register(new QuestDef("ec_11", QuestCategory.ECONOMY,
            "Express Delivery",
            desc("The Mail system lets you send", "messages and item attachments", "to other players."),
            11, prereqs("gs_02"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Send a mail with an item attached") },
            new QuestReward[]{ coins(150) },
            false
        ));

        register(new QuestDef("ec_12", QuestCategory.ECONOMY,
            "Personal Brand",
            desc("The Customize app lets you set", "a badge title, name color, and", "mastery marks."),
            12, prereqs("ec_03"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Customize your badge in the Customize app") },
            new QuestReward[]{ coins(200) },
            false
        ));

        register(new QuestDef("ec_13", QuestCategory.ECONOMY,
            "Quick Withdrawal",
            desc("ATM blocks give you bank access", "anywhere in the world — no computer", "needed."),
            13, prereqs("gs_05"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Use an ATM block") },
            new QuestReward[]{ coins(100) },
            false
        ));
    }

    // ═══════════════════════════════════════════
    // MASTERY — 23 quests
    // ═══════════════════════════════════════════

    private static void registerMastery() {
        register(new QuestDef("ms_01", QuestCategory.MASTERY,
            "Jack of All Trades",
            desc("Reach level 10 in all 5 skill trees.", "A well-rounded adventurer."),
            1, prereqs("cb_06", "ex_03"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "ALL", 10, "All 5 skill trees at level 10") },
            new QuestReward[]{ coins(2000) },
            false
        ));

        register(new QuestDef("ms_02", QuestCategory.MASTERY,
            "First Prestige",
            desc("Prestige a maxed skill tree", "to reset it and gain permanent bonuses."),
            2, prereqs("ms_01"),
            new QuestTask[]{ task(QuestTaskType.PRESTIGE_TREE, "ANY", 1, "Prestige any skill tree") },
            new QuestReward[]{ coins(3000) },
            false
        ));

        register(new QuestDef("ms_03", QuestCategory.MASTERY,
            "Dungeon Speedrunner",
            desc("Maintain a 5-clear consecutive", "dungeon streak without failing."),
            3, prereqs("dg_08"),
            new QuestTask[]{ task(QuestTaskType.STAT_CHECK, "consecutiveClears", 5, "Reach a 5-clear streak") },
            new QuestReward[]{ coins(2000) },
            true
        ));

        register(new QuestDef("ms_04", QuestCategory.MASTERY,
            "Completionist",
            desc("A true collector and hunter.", "Excel at multiple activities."),
            4, prereqs("ex_08", "ex_10"),
            new QuestTask[]{
                task(QuestTaskType.MUSEUM_DONATIONS, 50, "Donate 50 items to the Museum"),
                task(QuestTaskType.BOUNTY_COMPLETE, 20, "Complete 20 bounty hunts")
            },
            new QuestReward[]{ coins(5000) },
            false
        ));

        register(new QuestDef("ms_05", QuestCategory.MASTERY,
            "Master of All",
            desc("Reach level 40 in all 5 skill trees.", "The pinnacle of growth."),
            5, prereqs("ms_01"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "ALL", 40, "All 5 skill trees at level 40") },
            new QuestReward[]{ coins(10000) },
            false
        ));

        register(new QuestDef("ms_06", QuestCategory.MASTERY,
            "Legend",
            desc("Complete 50 quests total.", "A seasoned veteran of MegaMod."),
            6, prereqs("ms_01"),
            new QuestTask[]{ task(QuestTaskType.STAT_CHECK, "questsCompleted", 50, "Complete 50 quests") },
            new QuestReward[]{ coins(10000) },
            false
        ));

        register(new QuestDef("ms_07", QuestCategory.MASTERY,
            "Grand Master",
            desc("Prestige all 5 skill trees.", "The ultimate achievement."),
            7, prereqs("ms_02"),
            new QuestTask[]{ task(QuestTaskType.PRESTIGE_TREE, "ALL", 1, "Prestige all 5 skill trees") },
            new QuestReward[]{ coins(25000) },
            false
        ));

        register(new QuestDef("ms_08", QuestCategory.MASTERY,
            "Eternal Champion",
            desc("An absolute legend.", "Clear the highest dungeons repeatedly."),
            8, prereqs("dg_10"),
            new QuestTask[]{ task(QuestTaskType.DUNGEON_CLEAR, "ANY", 100, "Clear 100 dungeons total") },
            new QuestReward[]{ coins(50000) },
            true
        ));

        register(new QuestDef("ms_09", QuestCategory.MASTERY,
            "Alchemy Master",
            desc("Master the full range of alchemy.", "Brew many different potion types."),
            9, prereqs("cb_16"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Brew 10 different potion types") },
            new QuestReward[]{ coins(3000), skillXp("ARCANE", 100) },
            false
        ));

        register(new QuestDef("ms_10", QuestCategory.MASTERY,
            "Arena Champion",
            desc("Complete a Challenge mode run.", "No Armor or No Damage — only the", "best survive."),
            10, prereqs("cb_18"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Complete an Arena Challenge mode") },
            new QuestReward[]{ coins(5000), skillXp("COMBAT", 100) },
            false
        ));

        register(new QuestDef("ms_11", QuestCategory.MASTERY,
            "Ultimate Backpack",
            desc("Reach the highest backpack tier.", "Netherite backpacks offer maximum", "storage and upgrade slots."),
            11, prereqs("ex_15"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Craft a Netherite backpack") },
            new QuestReward[]{ coins(3000), skillXp("SURVIVAL", 75) },
            false
        ));

        register(new QuestDef("ms_12", QuestCategory.MASTERY,
            "Walking Encyclopedia",
            desc("Discover an enormous number of", "entries. You know almost everything."),
            12, prereqs("ex_12"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Discover 100 encyclopedia entries") },
            new QuestReward[]{ coins(5000), skillXp("SURVIVAL", 100) },
            false
        ));

        register(new QuestDef("ms_14", QuestCategory.MASTERY,
            "Farming Legend",
            desc("Reach the pinnacle of agriculture.", "Capstone Farming nodes grant", "incredible harvest bonuses."),
            14, prereqs("ex_17"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "FARMING", 25, "Reach Farming level 25") },
            new QuestReward[]{ coins(1000), skillXp("FARMING", 100) },
            false
        ));

        register(new QuestDef("ms_15", QuestCategory.MASTERY,
            "Survival Expert",
            desc("Master the art of endurance.", "High Survival levels grant health", "regen and damage resistance."),
            15, prereqs("ex_18"),
            new QuestTask[]{ task(QuestTaskType.SKILL_LEVEL, "SURVIVAL", 25, "Reach Survival level 25") },
            new QuestReward[]{ coins(1000), skillXp("SURVIVAL", 100) },
            false
        ));

        register(new QuestDef("ms_16", QuestCategory.MASTERY,
            "Challenge Accepted",
            desc("Weekly Challenges reset every week", "with fresh objectives. Complete one", "for bonus XP and coins."),
            16, prereqs("gs_07"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Complete a weekly skill challenge") },
            new QuestReward[]{ coins(500) },
            false
        ));

        register(new QuestDef("ms_17", QuestCategory.MASTERY,
            "Leaderboard Contender",
            desc("Check the Ranks app to see where", "you stand. Compete for the top", "spot in kills, clears, or wealth."),
            17, prereqs("ms_01"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Appear on any leaderboard") },
            new QuestReward[]{ coins(2000) },
            false
        ));

        register(new QuestDef("ms_18", QuestCategory.MASTERY,
            "Trophy Collector",
            desc("Collect mob heads from battle.", "Display them as trophies or trade", "them for prestige."),
            18, prereqs("ex_20"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Collect 20 mob heads") },
            new QuestReward[]{ coins(3000), skillXp("COMBAT", 75) },
            false
        ));

        register(new QuestDef("ms_19", QuestCategory.MASTERY,
            "True Completionist",
            desc("Complete every quest in every", "category. The ultimate achievement."),
            19, prereqs("ms_05", "ms_07", "ms_08"),
            new QuestTask[]{ task(QuestTaskType.STAT_CHECK, "questsCompleted", 100, "Complete 100 quests") },
            new QuestReward[]{ coins(100000) },
            false
        ));

        register(new QuestDef("ms_20", QuestCategory.MASTERY,
            "Legendary Find",
            desc("Obtain an Arsenal legendary weapon.", "These drop from dungeon bosses."),
            20, prereqs("dg_04"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Obtain an Arsenal legendary weapon") },
            new QuestReward[]{ coins(3000) },
            false
        ));

        register(new QuestDef("ms_21", QuestCategory.MASTERY,
            "Class Master",
            desc("Reach the capstone node in any", "class branch (Tier 4)."),
            21, prereqs("ms_01"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Unlock a T4 class capstone") },
            new QuestReward[]{ coins(5000) },
            false
        ));

        register(new QuestDef("ms_22", QuestCategory.MASTERY,
            "Spell Arsenal",
            desc("Unlock 10 different spells across", "all classes."),
            22, prereqs("cb_22"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Unlock 10 spells") },
            new QuestReward[]{ coins(5000) },
            false
        ));

        register(new QuestDef("ms_23", QuestCategory.MASTERY,
            "Jeweled Crown",
            desc("Equip 3 jewelry pieces", "simultaneously."),
            23, prereqs("ex_21"),
            new QuestTask[]{ task(QuestTaskType.CHECKMARK, 1, "Equip 3 jewelry pieces") },
            new QuestReward[]{ coins(2000) },
            false
        ));
    }
}
