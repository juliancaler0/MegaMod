package com.ultra.megamod.feature.economy;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.ArrayList;
import java.util.List;

public class GuideBookHelper {

    public static ItemStack createGuideBook() {
        List<Filterable<Component>> pages = new ArrayList<>();

        // ==========================================
        // Page 1 - Welcome
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Welcome to MegaMod"))
            .append(text("\n\nThis mod transforms\nMinecraft with:\n\n"))
            .append(gold("Economy"))
            .append(text(" & Banking\n"))
            .append(gold("Relics"))
            .append(text(" & Accessories\n"))
            .append(gold("Dungeons"))
            .append(text(" (6 tiers)\n"))
            .append(gold("Skill Trees"))
            .append(text(" (5 paths)\n"))
            .append(gold("Colonies"))
            .append(text(" & Citizens\n"))
            .append(gold("Museum"))
            .append(text(" Collection\n"))
            .append(gold("Alchemy"))
            .append(text(", "))
            .append(gold("Casino"))
            .append(text(",\n"))
            .append(gold("Arena"))
            .append(text(", & 30+ QoL\nimprovements."))
        ));

        // ==========================================
        // Page 2 - Getting Started
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Getting Started"))
            .append(text("\n\n"))
            .append(gold("Step 1: "))
            .append(text("Craft a "))
            .append(gold("Computer"))
            .append(text("\n(4 iron + 1 redstone +\n1 glass pane)\n\n"))
            .append(gold("Step 2: "))
            .append(text("Earn coins by\nkilling mobs and\nmining ores.\n\n"))
            .append(gold("Step 3: "))
            .append(text("Open the Computer\nfor 22+ apps: Shop,\nBank, Wiki, Party,\nSkills, and more.\n\n"))
            .append(gold("Step 4: "))
            .append(text("Press "))
            .append(key("K"))
            .append(text(" to open\nyour Skill Tree!"))
        ));

        // ==========================================
        // Page 3 - Keybinds
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Keybinds"))
            .append(text("\n\n"))
            .append(key("V"))
            .append(text(" "))
            .append(gold("Accessories"))
            .append(text("\n  Equip relics to\n  10 unique slots\n\n"))
            .append(key("K"))
            .append(text(" "))
            .append(gold("Skill Tree"))
            .append(text("\n  Spend XP on 5\n  skill paths\n\n"))
            .append(key("R"))
            .append(text(" "))
            .append(gold("Primary Ability"))
            .append(text("\n  Cast relic power\n\n"))
            .append(key("G"))
            .append(text(" "))
            .append(gold("Secondary Ability"))
            .append(text("\n  Cycle with R+Scroll"))
        ));

        // ==========================================
        // Page 4 - More Keybinds
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("More Keybinds"))
            .append(text("\n\n"))
            .append(key("O"))
            .append(text(" "))
            .append(gold("Sort Inventory"))
            .append(text("\n  Sorts any open\n  container\n\n"))
            .append(key("B"))
            .append(text(" "))
            .append(gold("Open Backpack"))
            .append(text("\n  Portable storage\n  with upgrades\n\n"))
            .append(text("Hold "))
            .append(key("R"))
            .append(text(" + "))
            .append(gold("Scroll"))
            .append(text("\n  Cycle weapon\n  abilities\n\n"))
            .append(gold("Right-Click"))
            .append(text(" weapon\n  Cast weapon skill"))
        ));

        // ==========================================
        // Page 5 - Economy
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Economy & Coins"))
            .append(text("\n\n"))
            .append(gold("MegaCoins (MC)"))
            .append(text(" are\nthe universal currency.\nYou start with 250 MC.\n\n"))
            .append(gold("Earn by:\n"))
            .append(text("  Kill mobs (1-50 MC)\n  Mine ores (1-10 MC)\n  Dungeons (big payout)\n  Sell at Shop\n  Bounty hunts\n\n"))
            .append(gold("Wallet"))
            .append(text(" = on you,\n  "))
            .append(accent("LOST on death!"))
            .append(text("\n"))
            .append(gold("Bank"))
            .append(text(" = always safe.\n  Use an ATM block or\n  the Computer Bank app."))
        ));

        // ==========================================
        // Page 6 - Relics & Accessories
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Relics & Accessories"))
            .append(text("\n\nPress "))
            .append(key("V"))
            .append(text(" to open your\naccessory slots:\n\n"))
            .append(gold("Head"))
            .append(text(", "))
            .append(gold("Face"))
            .append(text(", "))
            .append(gold("Neck"))
            .append(text(", "))
            .append(gold("Back"))
            .append(text(",\n"))
            .append(gold("Hands"))
            .append(text(", "))
            .append(gold("Belt"))
            .append(text(", "))
            .append(gold("Feet"))
            .append(text(",\nand 2x "))
            .append(gold("Ring"))
            .append(text(" slots.\n\n"))
            .append(text("Each relic gives a\n"))
            .append(gold("passive bonus"))
            .append(text(" and an\n"))
            .append(gold("active ability"))
            .append(text(" ("))
            .append(key("R"))
            .append(text("/"))
            .append(key("G"))
            .append(text(").\n\n"))
            .append(text("Chain 2 abilities\nwithin 8s for combos!"))
        ));

        // ==========================================
        // Page 7 - RPG Weapons
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("RPG Weapons"))
            .append(text("\n\n20+ weapon types with\nbuilt-in castable\nskills:\n\n"))
            .append(gold("Katana"))
            .append(text(" - Dash strike\n"))
            .append(gold("Hammer"))
            .append(text(" - Ground slam\n"))
            .append(gold("Staff"))
            .append(text(" - Magic bolt\n"))
            .append(gold("Longbow"))
            .append(text(" - Power shot\n"))
            .append(gold("Rapier"))
            .append(text(" - Flurry attack\n"))
            .append(gold("Spear"))
            .append(text(" - Throw + recall\n\n"))
            .append(text("Right-click to cast.\nFound in dungeons &\nthe MegaShop."))
        ));

        // ==========================================
        // Page 8 - Skill Trees
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Skill Trees"))
            .append(text("\n\nPress "))
            .append(key("K"))
            .append(text(" to open. 5 trees\nwith 100+ nodes:\n\n"))
            .append(gold("Combat"))
            .append(text(" - damage, crit,\n  lifesteal, combos\n"))
            .append(gold("Mining"))
            .append(text(" - speed, fortune,\n  vein mine, auto-smelt\n"))
            .append(gold("Farming"))
            .append(text(" - growth, yield,\n  animal breeding\n"))
            .append(gold("Arcane"))
            .append(text(" - magic damage,\n  summoning, mana\n"))
            .append(gold("Survival"))
            .append(text(" - health, speed,\n  luck, exploration\n\n"))
            .append(text("Max a tree then\nprestige for perma-\nbuffs!"))
        ));

        // ==========================================
        // Page 9 - Dungeons
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Dungeons"))
            .append(text("\n\nCraft a "))
            .append(gold("Dungeon Key"))
            .append(text("\nand right-click to\nenter a procedurally\ngenerated dungeon.\n\n"))
            .append(gold("6 Tiers:"))
            .append(text("\n  Normal > Hard >\n  Nightmare > Infernal\n  > Mythic > Eternal\n\n"))
            .append(gold("Features:\n"))
            .append(text("  8 unique bosses\n  13+ mob types\n  Party co-op (4 max)\n  Dungeon insurance\n  Leaderboard timers"))
        ));

        // ==========================================
        // Page 10 - Citizens & Colony
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Citizens & Colonies"))
            .append(text("\n\n"))
            .append(gold("Workers"))
            .append(text(" (13 types):\n  Farmer, Miner, Lumber-\n  jack, Fisherman,\n  Shepherd, Beekeeper,\n  Warehouse, & more.\n\n"))
            .append(gold("Recruits"))
            .append(text(" (11 types):\n  Bowman, Shieldman,\n  Captain, Assassin,\n  Commander, & more.\n\n"))
            .append(gold("Setup:"))
            .append(text(" Set a bed, chest,\nand work position for\neach citizen. The\nWarehouse Worker auto-\ndelivers supplies!"))
        ));

        // ==========================================
        // Page 11 - Museum
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Museum"))
            .append(text("\n\nBuild your personal\ncollection museum!\n\n"))
            .append(gold("1."))
            .append(text(" Craft a "))
            .append(gold("Mob Net"))
            .append(text("\n"))
            .append(gold("2."))
            .append(text(" Throw at mobs to\n   capture them\n"))
            .append(gold("3."))
            .append(text(" Display on pedestals\n   in your Museum\n\n"))
            .append(gold("5 Wings:\n"))
            .append(text("  Wildlife, Art, Item,\n  Achievement, Aquarium\n\n"))
            .append(text("Talk to the "))
            .append(gold("Curator"))
            .append(text("\nNPC to donate and\ntrack progress."))
        ));

        // ==========================================
        // Page 12 - Alchemy
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Alchemy"))
            .append(text("\n\nBrew custom potions\nat an "))
            .append(gold("Alchemy Cauldron"))
            .append(text(".\n\n"))
            .append(gold("Steps:\n"))
            .append(text("  1. Grind reagents\n     at the Grindstone\n  2. Add to Cauldron\n  3. Stir & collect!\n\n"))
            .append(gold("20+ effects:"))
            .append(text("\n  Berserker Rage,\n  Inferno, Void Walk,\n  Stone Skin, & more.\n\n"))
            .append(text("Higher tiers require\nAlchemy skill nodes."))
        ));

        // ==========================================
        // Page 13 - Backpacks
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Backpacks"))
            .append(text("\n\nPortable inventory you\ncan wear! Press "))
            .append(key("B"))
            .append(text(".\n\n"))
            .append(gold("42 variants"))
            .append(text(" with\npassive abilities like\nMagnet, Night Vision,\nWater Breathing.\n\n"))
            .append(gold("8 Upgrades:\n"))
            .append(text("  Magnet, AutoPickup,\n  Crafting, Feeding,\n  Jukebox, Refill,\n  Smelting, Void\n\n"))
            .append(text("Upgrade tiers from\nLeather to Netherite."))
        ));

        // ==========================================
        // Page 14 - Casino & Arena
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Casino & Arena"))
            .append(text("\n\n"))
            .append(gold("Casino"))
            .append(text(" (6 games):\n  Slots, Blackjack,\n  Roulette, Craps,\n  Baccarat, Big Wheel.\n  Buy chips from the\n  Cashier NPC.\n\n"))
            .append(gold("Arena"))
            .append(text(" (3 modes):\n  PvE Waves, PvP 1v1,\n  Boss Rush.\n  Access via Computer.\n  Leaderboard ranked!"))
        ));

        // ==========================================
        // Page 15 - Marketplace & Corruption
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("More Systems"))
            .append(text("\n\n"))
            .append(gold("Marketplace"))
            .append(text("\n  Player-to-player\n  trading via Terminal\n  block. Escrow system\n  for safe trades.\n\n"))
            .append(gold("Corruption"))
            .append(text("\n  Dark zones spread\n  across the world.\n  Defend with recruits\n  or launch a Purge\n  event to destroy\n  them!\n\n"))
            .append(gold("Bounty Board"))
            .append(text("\n  Hunt named mobs for\n  coin rewards daily."))
        ));

        // ==========================================
        // Page 16 - QoL Features
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Quality of Life"))
            .append(text("\n\n30+ improvements:\n\n"))
            .append(gold("- "))
            .append(text("Sit on stairs\n"))
            .append(gold("- "))
            .append(text("Sprint on paths\n"))
            .append(gold("- "))
            .append(text("Totem saves in void\n"))
            .append(gold("- "))
            .append(text("Gravestone on death\n"))
            .append(gold("- "))
            .append(text("Homing XP orbs\n"))
            .append(gold("- "))
            .append(text("Mob health display\n"))
            .append(gold("- "))
            .append(text("Low HP vignette\n"))
            .append(gold("- "))
            .append(text("Tree felling\n"))
            .append(gold("- "))
            .append(text("Better armor stands\n"))
            .append(gold("- "))
            .append(text("Invisible item frames\n"))
            .append(gold("- "))
            .append(text("Day counter HUD\n"))
            .append(gold("- "))
            .append(text("Compass HUD\n"))
            .append(gold("- "))
            .append(text("450+ furniture blocks"))
        ));

        // ==========================================
        // Page 17 - Prestige & Mastery
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Prestige & Mastery"))
            .append(text("\n\nMax out a skill tree\nto "))
            .append(gold("Prestige"))
            .append(text(" it:\n  Reset the tree for\n  permanent bonuses\n  and cosmetics.\n\n"))
            .append(gold("Mastery Marks"))
            .append(text(" are\nearned from major\nmilestones. Spend in\nthe "))
            .append(gold("Customize App"))
            .append(text(":\n  - Chat badges\n  - Name colors\n  - Stat boosts\n\n"))
            .append(gold("New Game+"))
            .append(text("\n  Clear Infernal tier\n  to unlock Mythic &\n  Eternal dungeons."))
        ));

        // ==========================================
        // Page 18 - Elite Mobs & Combos
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Combat Tips"))
            .append(text("\n\n"))
            .append(gold("Elite Mobs"))
            .append(text(" (5% spawn)\n  2x HP, bonus loot,\n  yellow name tag.\n\n"))
            .append(gold("Champion Mobs"))
            .append(text(" (1%)\n  3x HP, glowing,\n  rare drops.\n\n"))
            .append(gold("Relic Combos"))
            .append(text("\n  Chain 2 abilities\n  within 8 seconds:\n  Fire+Ice = Shatter\n  Heal+Attack = Drain\n  Shadow+Atk = Execute\n  Shield+Shield = Fort"))
        ));

        // ==========================================
        // Page 19 - Tips
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("Pro Tips"))
            .append(text("\n\n"))
            .append(gold("1."))
            .append(text(" "))
            .append(accent("Bank your coins!"))
            .append(text("\n   Wallet is lost on\n   death. Bank is not.\n\n"))
            .append(gold("2."))
            .append(text(" Luck stat improves\n   loot drops and relic\n   quality everywhere.\n\n"))
            .append(gold("3."))
            .append(text(" Party up before\n   dungeons (Computer\n   Party app).\n\n"))
            .append(gold("4."))
            .append(text(" Set up a Warehouse\n   Worker early - they\n   auto-deliver food &\n   tools to workers."))
        ));

        // ==========================================
        // Page 20 - The Computer & Wiki
        // ==========================================
        pages.add(page(Component.empty()
            .append(header("The Computer"))
            .append(text("\n\nYour hub for\n"))
            .append(gold("everything"))
            .append(text(". 22+ apps:\n\n"))
            .append(text("Shop, Bank, Stats,\nSkills, Wiki, Party,\nFriends, Colony,\nCustomize, Arena,\nCasino, Marketplace,\nMail, Bounty, Map,\nRecipes, Music,\nand more.\n\n"))
            .append(text("Open the "))
            .append(gold("Wiki"))
            .append(text(" app\nfor the full in-depth\nguide to every system.\n\n"))
            .append(accent("Craft one now!"))
        ));

        WrittenBookContent content = new WrittenBookContent(
            Filterable.passThrough("MegaMod Guide"),
            "Drek",
            0,
            pages,
            true
        );

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
        return book;
    }

    private static Filterable<Component> page(Component component) {
        return Filterable.passThrough(component);
    }

    private static Component header(String text) {
        return Component.literal(text).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
    }

    private static Component text(String text) {
        return Component.literal(text).withStyle(ChatFormatting.BLACK);
    }

    private static Component gold(String text) {
        return Component.literal(text).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD);
    }

    private static Component key(String text) {
        return Component.literal(text).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD);
    }

    private static Component accent(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }
}
