package com.ultra.megamod.feature.loot;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.loot.DungeonLootGenerator;
import com.ultra.megamod.feature.dungeons.loot.LootQuality;
import com.ultra.megamod.feature.relics.RelicRegistry;
import com.ultra.megamod.feature.skills.synergy.SynergyEffects;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

import java.util.List;

/**
 * Integrates rolled loot (relics, weapons, armor) into the wider game world
 * beyond dungeons. All sources are rare by default, but Luck attribute and
 * loot_fortune increase both drop chance and quality.
 *
 * Sources:
 *   1. Vanilla mob drops (bosses/mini-bosses) -> rare relic/weapon
 *   2. Fishing treasure -> rare aquatic relic
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class WorldLootIntegration {

    // ======================== MOB DROPS ========================

    // Base chances (out of 1.0) before luck scaling
    private static final double BOSS_DROP_CHANCE = 0.12;       // Wither, Dragon, Warden
    private static final double MINIBOSS_DROP_CHANCE = 0.05;   // Elder Guardian, Evoker, Ravager
    private static final double RARE_MOB_DROP_CHANCE = 0.015;  // Enderman, Blaze, Witch, etc.

    // Luck scaling: each point of Luck adds this to drop chance
    private static final double LUCK_CHANCE_BONUS = 0.02;
    // Luck -> fortune: each point of Luck adds this to quality fortune roll
    private static final double LUCK_FORTUNE_BONUS = 5.0;

    @SubscribeEvent
    public static void onMobDeath(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity mob = event.getEntity();
        if (mob.level().isClientSide()) return;
        if (mob instanceof Player) return;

        ServerLevel level = (ServerLevel) mob.level();
        if (!FeatureToggleManager.get(level.getServer().overworld()).isEnabled("world_loot")) return;

        String mobType = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();
        double baseChance = getMobDropChance(mobType);
        if (baseChance <= 0) return;

        DungeonTier tier = getMobDropTier(mobType);
        double luck = getLuck(player);
        double finalChance = Math.min(0.50, baseChance + luck * LUCK_CHANCE_BONUS);
        double fortune = luck * LUCK_FORTUNE_BONUS;

        RandomSource random = level.random;
        if (random.nextDouble() >= finalChance) return;

        ItemStack rolledItem = generateWorldLootItem(tier, random, fortune);
        ItemEntity drop = new ItemEntity(level, mob.getX(), mob.getY() + 0.5, mob.getZ(), rolledItem);
        drop.setDefaultPickUpDelay();
        event.getDrops().add(drop);

        player.sendSystemMessage(Component.literal(
                "\u00A76\u00A7l\u2605 \u00A7eA rare item dropped from " + mob.getName().getString() + "!"));
        level.playSound(null, mob.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundSource.PLAYERS, 0.8f, 1.2f);

        // Fortune's Favor synergy: chance to duplicate the rare drop
        if (SynergyEffects.shouldDoubleRareDrop(player)) {
            ItemStack duplicate = rolledItem.copy();
            ItemEntity bonusDrop = new ItemEntity(level, mob.getX(), mob.getY() + 0.5, mob.getZ(), duplicate);
            bonusDrop.setDefaultPickUpDelay();
            event.getDrops().add(bonusDrop);
            player.sendSystemMessage(Component.literal(
                    "\u00A76\u00A7l\u2605 \u00A7eFortune's Favor! Double drop!"));
        }
    }

    private static double getMobDropChance(String mobType) {
        return switch (mobType) {
            // True bosses
            case "ender_dragon", "wither" -> BOSS_DROP_CHANCE;
            case "warden" -> BOSS_DROP_CHANCE;
            // Mini-bosses
            case "elder_guardian", "evoker", "ravager" -> MINIBOSS_DROP_CHANCE;
            // Rare mobs
            case "enderman", "blaze", "wither_skeleton", "ghast", "shulker" -> RARE_MOB_DROP_CHANCE;
            case "piglin_brute", "vindicator", "pillager" -> RARE_MOB_DROP_CHANCE;
            case "witch", "guardian" -> RARE_MOB_DROP_CHANCE * 0.5;
            default -> 0.0;
        };
    }

    private static DungeonTier getMobDropTier(String mobType) {
        return switch (mobType) {
            case "ender_dragon" -> DungeonTier.NIGHTMARE;
            case "wither" -> DungeonTier.HARD;
            case "warden" -> DungeonTier.NIGHTMARE;
            case "elder_guardian", "evoker", "ravager" -> DungeonTier.HARD;
            default -> DungeonTier.NORMAL;
        };
    }

    // ======================== FISHING RELICS ========================

    // Base chance for a relic to appear as fishing treasure
    private static final double FISHING_RELIC_CHANCE = 0.025; // 2.5%

    // Aquatic-themed relics that can be fished up
    private static volatile List<Item> AQUATIC_RELICS;

    private static List<Item> getAquaticRelics() {
        if (AQUATIC_RELICS == null) {
            AQUATIC_RELICS = List.of(
                    RelicRegistry.AQUA_WALKER.get(),
                    RelicRegistry.JELLYFISH_NECKLACE.get(),
                    RelicRegistry.AMPHIBIAN_BOOT.get(),
                    RelicRegistry.DROWNED_BELT.get(),
                    RelicRegistry.TIDEKEEPER_AMULET.get(),
                    RelicRegistry.STORMBAND.get(),
                    RelicRegistry.STORMSTRIDER_BOOTS.get(),
                    RelicRegistry.ICE_SKATES.get(),
                    RelicRegistry.ICE_BREAKER.get()
            );
        }
        return AQUATIC_RELICS;
    }

    @SubscribeEvent
    public static void onFishing(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = (ServerLevel) player.level();
        if (!FeatureToggleManager.get(level.getServer().overworld()).isEnabled("world_loot")) return;

        double luck = getLuck(player);
        double finalChance = Math.min(0.20, FISHING_RELIC_CHANCE + luck * LUCK_CHANCE_BONUS);

        RandomSource random = level.random;
        if (random.nextDouble() >= finalChance) return;

        // Pick a random aquatic relic and generate it as a rolled item
        List<Item> pool = getAquaticRelics();
        Item baseItem = pool.get(random.nextInt(pool.size()));
        double fortune = luck * LUCK_FORTUNE_BONUS;
        ItemStack rolledItem = generateRolledItem(baseItem, DungeonTier.NORMAL, random, fortune);

        // Drop near the player
        ItemEntity drop = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), rolledItem);
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);

        player.sendSystemMessage(Component.literal(
                "\u00A7b\u00A7l\u2605 \u00A73You fished up a rare relic!"));
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RETURN,
                SoundSource.PLAYERS, 0.8f, 1.0f);
    }

    // ======================== VANILLA CHEST INJECTION ========================

    // Relics that can appear in vanilla structure chests, grouped by structure theme
    private static volatile List<Item> STRUCTURE_COMMON_RELICS;
    private static volatile List<Item> STRUCTURE_UNCOMMON_RELICS;

    private static List<Item> getStructureCommonRelics() {
        if (STRUCTURE_COMMON_RELICS == null) {
            STRUCTURE_COMMON_RELICS = List.of(
                    RelicRegistry.LEATHER_BELT.get(),
                    RelicRegistry.WOOL_MITTEN.get(),
                    RelicRegistry.ARROW_QUIVER.get(),
                    RelicRegistry.ICE_SKATES.get(),
                    RelicRegistry.ROLLER_SKATES.get(),
                    RelicRegistry.IRON_FIST.get(),
                    RelicRegistry.CHORUS_INHIBITOR.get(),
                    RelicRegistry.BASTION_RING.get(),
                    RelicRegistry.LODESTONE_MAGNET.get(),
                    // Usable relics (common tier)
                    RelicRegistry.HORSE_FLUTE.get(),
                    RelicRegistry.INFINITY_HAM.get(),
                    RelicRegistry.SPORE_SACK.get(),
                    // Common-tier weapons
                    RelicRegistry.BRIARTHORN.get(),
                    RelicRegistry.WHISPERWIND.get(),
                    RelicRegistry.BATTLEDANCER.get(),
                    RelicRegistry.VAMPIRIC_TOME.get()
            );
        }
        return STRUCTURE_COMMON_RELICS;
    }

    private static List<Item> getStructureUncommonRelics() {
        if (STRUCTURE_UNCOMMON_RELICS == null) {
            STRUCTURE_UNCOMMON_RELICS = List.of(
                    RelicRegistry.ELYTRA_BOOSTER.get(),
                    RelicRegistry.DROWNED_BELT.get(),
                    RelicRegistry.HUNTER_BELT.get(),
                    RelicRegistry.ENDER_HAND.get(),
                    RelicRegistry.RAGE_GLOVE.get(),
                    RelicRegistry.AQUA_WALKER.get(),
                    RelicRegistry.AMPHIBIAN_BOOT.get(),
                    RelicRegistry.JELLYFISH_NECKLACE.get(),
                    RelicRegistry.REFLECTION_NECKLACE.get(),
                    RelicRegistry.STORMBAND.get(),
                    RelicRegistry.VERDANT_SIGNET.get(),
                    RelicRegistry.VERDANT_MASK.get(),
                    RelicRegistry.SANDWALKER_TREADS.get(),
                    RelicRegistry.EMBERSTONE_BAND.get(),
                    // Usable relics (uncommon tier)
                    RelicRegistry.SHADOW_GLAIVE.get(),
                    RelicRegistry.MAGIC_MIRROR.get(),
                    RelicRegistry.BLAZING_FLASK.get(),
                    RelicRegistry.SPACE_DISSECTOR.get(),
                    // Uncommon-tier weapons
                    RelicRegistry.STATIC_SEEKER.get(),
                    RelicRegistry.EBONCHILL.get(),
                    RelicRegistry.LIGHTBINDER.get(),
                    RelicRegistry.STORMFURY.get()
            );
        }
        return STRUCTURE_UNCOMMON_RELICS;
    }

    // ======================== JEWELRY POOLS ========================

    private static List<Item> JEWELRY_BASIC;    // copper/iron/gold rings (tier 0)
    private static List<Item> JEWELRY_GEM;      // ruby/topaz/citrine/jade/sapphire/tanzanite + diamond/emerald (tier 1-2)
    private static List<Item> JEWELRY_NETHERITE; // netherite-framed variants (tier 3)

    private static List<Item> getBasicJewelry() {
        if (JEWELRY_BASIC == null) {
            JEWELRY_BASIC = List.of(
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.COPPER_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.IRON_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.GOLD_RING.get()
            );
        }
        return JEWELRY_BASIC;
    }

    private static List<Item> getGemJewelry() {
        if (JEWELRY_GEM == null) {
            var R = com.ultra.megamod.feature.combat.items.JewelryRegistry.class;
            JEWELRY_GEM = List.of(
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.RUBY_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.TOPAZ_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.CITRINE_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.JADE_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.SAPPHIRE_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.TANZANITE_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.RUBY_NECKLACE.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.TOPAZ_NECKLACE.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.CITRINE_NECKLACE.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.JADE_NECKLACE.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.SAPPHIRE_NECKLACE.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.TANZANITE_NECKLACE.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.DIAMOND_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.DIAMOND_NECKLACE.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.EMERALD_NECKLACE.get()
            );
        }
        return JEWELRY_GEM;
    }

    private static List<Item> getNetheriteJewelry() {
        if (JEWELRY_NETHERITE == null) {
            JEWELRY_NETHERITE = List.of(
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.NETHERITE_RUBY_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.NETHERITE_TOPAZ_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.NETHERITE_CITRINE_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.NETHERITE_JADE_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.NETHERITE_SAPPHIRE_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.NETHERITE_TANZANITE_RING.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.NETHERITE_RUBY_NECKLACE.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.NETHERITE_TOPAZ_NECKLACE.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.NETHERITE_CITRINE_NECKLACE.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.NETHERITE_JADE_NECKLACE.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.NETHERITE_SAPPHIRE_NECKLACE.get(),
                    com.ultra.megamod.feature.combat.items.JewelryRegistry.NETHERITE_TANZANITE_NECKLACE.get()
            );
        }
        return JEWELRY_NETHERITE;
    }

    // ======================== RUNE POOLS ========================

    private static List<Item> RUNE_POOL;
    private static List<Item> getRunePool() {
        if (RUNE_POOL == null) {
            RUNE_POOL = List.of(
                    com.ultra.megamod.feature.combat.runes.RuneRegistry.ARCANE_RUNE.get(),
                    com.ultra.megamod.feature.combat.runes.RuneRegistry.FIRE_RUNE.get(),
                    com.ultra.megamod.feature.combat.runes.RuneRegistry.FROST_RUNE.get(),
                    com.ultra.megamod.feature.combat.runes.RuneRegistry.HEALING_RUNE.get(),
                    com.ultra.megamod.feature.combat.runes.RuneRegistry.LIGHTNING_RUNE.get(),
                    com.ultra.megamod.feature.combat.runes.RuneRegistry.SOUL_RUNE.get()
            );
        }
        return RUNE_POOL;
    }

    /**
     * Injects spell runes into structure chests. Runes are consumed as spell reagents
     * for tier 2+ spells so they should drop at modest rates across all structures.
     */
    public static ItemStack tryGenerateStructureChestRune(String lootTablePath, RandomSource random, double luck) {
        double baseChance;
        int min, max;

        if (lootTablePath.contains("end_city") || lootTablePath.contains("bastion")
                || lootTablePath.contains("stronghold") || lootTablePath.contains("ancient_city")) {
            baseChance = 0.35;
            min = 2; max = 5;
        } else if (lootTablePath.contains("desert_pyramid") || lootTablePath.contains("jungle_temple")
                || lootTablePath.contains("woodland_mansion") || lootTablePath.contains("nether_bridge")
                || lootTablePath.contains("ruined_portal")) {
            baseChance = 0.28;
            min = 1; max = 3;
        } else if (lootTablePath.contains("village") || lootTablePath.contains("pillager_outpost")
                || lootTablePath.contains("shipwreck") || lootTablePath.contains("igloo")
                || lootTablePath.contains("buried_treasure") || lootTablePath.contains("underwater_ruin")
                || lootTablePath.contains("abandoned_mineshaft")) {
            baseChance = 0.20;
            min = 1; max = 2;
        } else {
            return null;
        }

        double finalChance = Math.min(0.60, baseChance + luck * LUCK_CHANCE_BONUS);
        if (random.nextDouble() >= finalChance) return null;

        var pool = getRunePool();
        Item runeItem = pool.get(random.nextInt(pool.size()));
        int count = min + random.nextInt(max - min + 1);
        return new ItemStack(runeItem, count);
    }

    /**
     * Independent jewelry drop roll for vanilla structure chests. Called alongside
     * the relic roll from {@link StructureChestLootModifier}. Jewelry is obtainable
     * ONLY from loot drops and villager trades — crafting recipes have been removed.
     */
    public static ItemStack tryGenerateStructureChestJewelry(String lootTablePath, RandomSource random, double luck) {
        double baseChance;
        List<Item> pool;

        if (lootTablePath.contains("end_city") || lootTablePath.contains("bastion")
                || lootTablePath.contains("stronghold") || lootTablePath.contains("ancient_city")) {
            baseChance = 0.18;
            // High-tier structures favour netherite jewelry
            pool = random.nextFloat() < 0.55f ? getNetheriteJewelry() : getGemJewelry();
        } else if (lootTablePath.contains("desert_pyramid") || lootTablePath.contains("jungle_temple")
                || lootTablePath.contains("woodland_mansion") || lootTablePath.contains("nether_bridge")) {
            baseChance = 0.14;
            pool = getGemJewelry();
        } else if (lootTablePath.contains("village") || lootTablePath.contains("pillager_outpost")
                || lootTablePath.contains("shipwreck") || lootTablePath.contains("igloo")
                || lootTablePath.contains("buried_treasure") || lootTablePath.contains("underwater_ruin")
                || lootTablePath.contains("abandoned_mineshaft") || lootTablePath.contains("ruined_portal")) {
            baseChance = 0.10;
            // Low-tier — mostly basic, occasional gem
            pool = random.nextFloat() < 0.75f ? getBasicJewelry() : getGemJewelry();
        } else {
            return null;
        }

        double finalChance = Math.min(0.40, baseChance + luck * LUCK_CHANCE_BONUS);
        if (random.nextDouble() >= finalChance) return null;

        Item baseItem = pool.get(random.nextInt(pool.size()));
        return new ItemStack(baseItem);
    }

    /**
     * Picks a relic for vanilla structure chest injection based on the loot table ID.
     * Called from StructureChestLootModifier.
     * Returns null if no item should be added (failed chance roll).
     */
    public static ItemStack tryGenerateStructureChestRelic(String lootTablePath, RandomSource random, double luck) {
        double baseChance;
        DungeonTier tier;
        List<Item> pool;

        // High-value structures: end cities, bastions, strongholds
        if (lootTablePath.contains("end_city") || lootTablePath.contains("bastion")
                || lootTablePath.contains("stronghold")) {
            baseChance = 0.12;
            tier = DungeonTier.HARD;
            pool = getStructureUncommonRelics();
        }
        // Mid-value: temples, mansions, mineshafts
        else if (lootTablePath.contains("desert_pyramid") || lootTablePath.contains("jungle_temple")
                || lootTablePath.contains("woodland_mansion") || lootTablePath.contains("abandoned_mineshaft")
                || lootTablePath.contains("ancient_city")) {
            baseChance = 0.08;
            tier = DungeonTier.NORMAL;
            // Mix of common and uncommon
            pool = random.nextBoolean() ? getStructureCommonRelics() : getStructureUncommonRelics();
        }
        // Low-value: villages, shipwrecks, igloos, pillager outposts
        else if (lootTablePath.contains("village") || lootTablePath.contains("shipwreck")
                || lootTablePath.contains("igloo") || lootTablePath.contains("pillager_outpost")
                || lootTablePath.contains("buried_treasure") || lootTablePath.contains("underwater_ruin")) {
            baseChance = 0.04;
            tier = DungeonTier.NORMAL;
            pool = getStructureCommonRelics();
        }
        // Nether structures
        else if (lootTablePath.contains("nether_bridge") || lootTablePath.contains("ruined_portal")) {
            baseChance = 0.06;
            tier = DungeonTier.NORMAL;
            pool = getStructureCommonRelics();
        }
        else {
            return null; // Not a structure we care about
        }

        double finalChance = Math.min(0.35, baseChance + luck * LUCK_CHANCE_BONUS);
        if (random.nextDouble() >= finalChance) return null;

        double fortune = luck * LUCK_FORTUNE_BONUS;
        Item baseItem = pool.get(random.nextInt(pool.size()));
        return generateRolledItem(baseItem, tier, random, fortune);
    }

    // ======================== SHARED UTILITIES ========================

    /**
     * Gets the player's Luck attribute value.
     */
    public static double getLuck(Player player) {
        try {
            return player.getAttributeValue(Attributes.LUCK);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Generates a rolled world loot item using the dungeon loot generator.
     * Uses the tier's item pool and rolls quality with fortune bonus.
     */
    public static ItemStack generateWorldLootItem(DungeonTier tier, RandomSource random, double fortune) {
        // Temporarily set the fortune bonus for the generator
        double prevFortune = com.ultra.megamod.feature.dungeons.loot.DungeonChestLoot.activeFortuneBonus;
        com.ultra.megamod.feature.dungeons.loot.DungeonChestLoot.activeFortuneBonus = fortune;
        try {
            ItemStack item = DungeonLootGenerator.generateSingleItem(tier, random);
            // Replace "Dungeon Loot" lore with "World Drop"
            replaceSourceLore(item, "World Drop");
            return item;
        } finally {
            com.ultra.megamod.feature.dungeons.loot.DungeonChestLoot.activeFortuneBonus = prevFortune;
        }
    }

    /**
     * Generates a rolled item from a specific base item (for fishing/structure relics).
     */
    public static ItemStack generateRolledItem(Item baseItem, DungeonTier tier, RandomSource random, double fortune) {
        double prevFortune = com.ultra.megamod.feature.dungeons.loot.DungeonChestLoot.activeFortuneBonus;
        com.ultra.megamod.feature.dungeons.loot.DungeonChestLoot.activeFortuneBonus = fortune;
        try {
            ItemStack item = DungeonLootGenerator.generateFromBase(baseItem, tier, random);
            replaceSourceLore(item, "World Drop");
            return item;
        } finally {
            com.ultra.megamod.feature.dungeons.loot.DungeonChestLoot.activeFortuneBonus = prevFortune;
        }
    }

    /**
     * Generates a rolled item specifically for prestige rewards.
     */
    public static ItemStack generatePrestigeReward(DungeonTier tier, RandomSource random, String treeName) {
        ItemStack item = DungeonLootGenerator.generateSingleItem(tier, random);
        replaceSourceLore(item, "Prestige Reward (" + treeName + ")");
        return item;
    }

    /**
     * Generates a rolled item for herald quest rewards.
     */
    public static ItemStack generateQuestReward(DungeonTier tier, RandomSource random) {
        ItemStack item = DungeonLootGenerator.generateSingleItem(tier, random);
        replaceSourceLore(item, "Quest Reward");
        return item;
    }

    /**
     * Generates a rolled item for the MegaShop relic slot.
     * Capped at Common-Uncommon quality (Normal tier range).
     */
    public static ItemStack generateShopRelic(RandomSource random) {
        DungeonLootGenerator.maxQualityCap = LootQuality.UNCOMMON;
        try {
            ItemStack item = DungeonLootGenerator.generateSingleItem(DungeonTier.NORMAL, random);
            replaceSourceLore(item, "MegaShop");
            return item;
        } finally {
            DungeonLootGenerator.maxQualityCap = null;
        }
    }

    /**
     * Replaces the "Dungeon Loot" lore line with a custom source tag.
     */
    private static void replaceSourceLore(ItemStack stack, String source) {
        try {
            net.minecraft.world.item.component.ItemLore lore = stack.get(net.minecraft.core.component.DataComponents.LORE);
            if (lore == null) return;
            List<Component> oldLines = lore.lines();
            java.util.ArrayList<net.minecraft.network.chat.MutableComponent> newLines = new java.util.ArrayList<>();
            for (Component line : oldLines) {
                String text = line.getString();
                if (text.contains("Dungeon Loot")) {
                    newLines.add(Component.literal(source).withStyle(
                            net.minecraft.network.chat.Style.EMPTY.withColor(0xFF8844).withItalic(false)));
                } else {
                    newLines.add(Component.literal(text).withStyle(line.getStyle()));
                }
            }
            stack.set(net.minecraft.core.component.DataComponents.LORE,
                    new net.minecraft.world.item.component.ItemLore((List<Component>)(List<?>) newLines));
        } catch (Exception ignored) {}
    }
}
