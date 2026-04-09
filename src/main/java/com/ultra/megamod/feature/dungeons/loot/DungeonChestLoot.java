/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.alchemy.PotionContents
 *  net.minecraft.world.item.alchemy.Potions
 *  net.minecraft.world.level.ItemLike
 */
package com.ultra.megamod.feature.dungeons.loot;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.generation.RoomTemplate;
import com.ultra.megamod.feature.dungeons.item.DungeonArmorItem;
import com.ultra.megamod.feature.dungeons.loot.DungeonExclusiveItems;
import com.ultra.megamod.feature.dungeons.loot.DungeonLootGenerator;
import com.ultra.megamod.feature.museum.paintings.MasterpieceRegistry;
import com.ultra.megamod.feature.relics.data.ArmorStatRoller;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;

public class DungeonChestLoot {
    /** Combined fortune+luck bonus applied to quality rolls during loot generation. */
    public static double activeFortuneBonus = 0.0;
    /** Raw loot_fortune attribute value (without luck) — used for quantity bonuses. */
    public static double activeLootFortune = 0.0;

    /**
     * Generate loot with a player's luck/fortune bonus affecting quality rolls.
     */
    public static List<ItemStack> generateChestLootWithBonus(RoomTemplate.RoomType roomType, DungeonTier tier, RandomSource random, double fortuneBonus) {
        activeFortuneBonus = fortuneBonus;
        try {
            return generateChestLoot(roomType, tier, random);
        } finally {
            activeFortuneBonus = 0.0;
        }
    }

    /**
     * Calculate bonus item count from loot_fortune only (not luck).
     * Each 10 points of loot_fortune = +1 guaranteed bonus item,
     * remainder gives a fractional chance for one more.
     */
    private static int lootFortuneBonusItems(RandomSource random) {
        if (activeLootFortune <= 0) return 0;
        int guaranteed = (int) (activeLootFortune / 10.0);
        double remainder = (activeLootFortune % 10.0) / 10.0;
        return guaranteed + (random.nextDouble() < remainder ? 1 : 0);
    }

    public static List<ItemStack> generateChestLoot(RoomTemplate.RoomType roomType, DungeonTier tier, RandomSource random) {
        return switch (roomType) {
            case RoomTemplate.RoomType.ENTRANCE -> DungeonChestLoot.generateEntranceLoot(tier, random);
            case RoomTemplate.RoomType.COMBAT -> DungeonChestLoot.generateCombatLoot(tier, random);
            case RoomTemplate.RoomType.TREASURE -> DungeonChestLoot.generateTreasureLoot(tier, random);
            case RoomTemplate.RoomType.BOSS -> DungeonChestLoot.generateBossLoot(tier, random);
            case RoomTemplate.RoomType.CORRIDOR -> DungeonChestLoot.generateCorridorLoot(tier, random);
            case RoomTemplate.RoomType.PUZZLE -> DungeonChestLoot.generatePuzzleLoot(tier, random);
            case RoomTemplate.RoomType.ORE_DEPOSIT -> DungeonChestLoot.generateOreDepositLoot(tier, random);
            case RoomTemplate.RoomType.CORRIDOR_REWARD -> DungeonChestLoot.generateCorridorRewardLoot(tier, random);
            case RoomTemplate.RoomType.GRAND_HALL -> DungeonChestLoot.generateGrandHallLoot(tier, random);
            case RoomTemplate.RoomType.PRISON -> DungeonChestLoot.generatePrisonLoot(tier, random);
            case RoomTemplate.RoomType.LIBRARY -> DungeonChestLoot.generatePuzzleLoot(tier, random);
            default -> DungeonChestLoot.generateCorridorLoot(tier, random);
        };
    }

    private static List<ItemStack> generateEntranceLoot(DungeonTier tier, RandomSource random) {
        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        loot.add(new ItemStack((ItemLike)Items.TORCH, 16 + random.nextInt(16)));
        loot.add(new ItemStack((ItemLike)Items.COOKED_BEEF, 4 + random.nextInt(4)));
        if (random.nextFloat() < 0.5f) {
            loot.add(new ItemStack((ItemLike)Items.BREAD, 6 + random.nextInt(6)));
        }
        if (tier.getLevel() >= 2 && random.nextFloat() < 0.4f) {
            loot.add(new ItemStack((ItemLike)Items.GOLDEN_APPLE, 1));
        }
        loot.add(new ItemStack((ItemLike)Items.ARROW, 16 + random.nextInt(16)));
        return loot;
    }

    private static List<ItemStack> generateCombatLoot(DungeonTier tier, RandomSource random) {
        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        int itemCount = 1 + random.nextInt(2) + lootFortuneBonusItems(random);
        block8: for (int i = 0; i < itemCount; ++i) {
            int roll = random.nextInt(6);
            switch (roll) {
                case 0: {
                    ItemStack potion = new ItemStack((ItemLike)Items.SPLASH_POTION);
                    potion.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.STRONG_HEALING));
                    loot.add(potion);
                    continue block8;
                }
                case 1: {
                    ItemStack potion = new ItemStack((ItemLike)Items.POTION);
                    potion.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.STRENGTH));
                    loot.add(potion);
                    continue block8;
                }
                case 2: {
                    loot.add(new ItemStack((ItemLike)Items.ARROW, 8 + random.nextInt(16)));
                    continue block8;
                }
                case 3: {
                    loot.add(new ItemStack((ItemLike)Items.GOLDEN_APPLE, 1));
                    continue block8;
                }
                case 4: {
                    loot.add(new ItemStack((ItemLike)Items.COOKED_BEEF, 4 + random.nextInt(4)));
                    continue block8;
                }
                case 5: {
                    ItemStack potion = new ItemStack((ItemLike)Items.POTION);
                    potion.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.REGENERATION));
                    loot.add(potion);
                }
            }
        }
        if (tier.getLevel() >= 3 && random.nextFloat() < 0.3f) {
            loot.add(new ItemStack((ItemLike)Items.SPECTRAL_ARROW, 8 + random.nextInt(8)));
        }
        if (random.nextFloat() < 0.4f) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.DUNGEON_MINI_KEY.get(), 1));
        }
        return loot;
    }

    private static List<ItemStack> generateTreasureLoot(DungeonTier tier, RandomSource random) {
        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        // Side room loot is deliberately weaker than main-path rewards
        int itemCount = 1 + random.nextInt(2) + lootFortuneBonusItems(random);
        for (int i = 0; i < itemCount; ++i) {
            loot.add(DungeonLootGenerator.generateSingleItem(tier, random));
        }
        if (random.nextFloat() < 0.25f) {
            loot.add(new ItemStack((ItemLike)Items.GOLDEN_APPLE, 1));
        }
        if (random.nextFloat() < 0.4f) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.DUNGEON_MINI_KEY.get(), 1));
        }
        if (random.nextFloat() < 0.15f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.DUNGEON_MAP.get()));
        }
        if (random.nextFloat() < 0.2f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.WARP_STONE.get()));
        }
        if (random.nextFloat() < 0.2f) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.CERULEAN_ARROW_ITEM.get(), 2 + random.nextInt(4)));
        }
        if (random.nextFloat() < 0.2f) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.CRYSTAL_ARROW_ITEM.get(), 2 + random.nextInt(4)));
        }
        loot.add(new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE, 2 + random.nextInt(4)));
        // Geomancer armor pieces in treasure rooms — pre-initialize stats so they show in chests
        if (tier.getLevel() >= 2 && random.nextFloat() < 0.06f) {
            int piece = random.nextInt(4);
            switch (piece) {
                case 0 -> loot.add(initArmor(DungeonEntityRegistry.GEOMANCER_HELM.get(), 3.0, 1.0, EquipmentSlot.HEAD, random));
                case 1 -> loot.add(initArmor(DungeonEntityRegistry.GEOMANCER_CHEST.get(), 7.0, 2.0, EquipmentSlot.CHEST, random));
                case 2 -> loot.add(initArmor(DungeonEntityRegistry.GEOMANCER_LEGS.get(), 5.0, 1.5, EquipmentSlot.LEGS, random));
                default -> loot.add(initArmor(DungeonEntityRegistry.GEOMANCER_BOOTS.get(), 2.0, 1.0, EquipmentSlot.FEET, random));
            }
        }
        // Dungeon armor pieces in treasure rooms — tier-gated material
        if (random.nextFloat() < 0.10f) {
            loot.add(pickRandomDungeonArmor(tier, random));
        }
        // Darts in treasure rooms
        if (random.nextFloat() < 0.3f) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.DART_ITEM.get(), 8 + random.nextInt(16)));
        }
        // Dungeon-exclusive materials (tier-gated)
        if (tier.getLevel() >= 2 && random.nextFloat() < 0.12f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.CERULEAN_INGOT.get(), 1 + random.nextInt(2)));
        }
        if (tier.getLevel() >= 2 && random.nextFloat() < 0.10f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.CRYSTALLINE_SHARD.get(), 1));
        }
        if (tier.getLevel() >= 3 && random.nextFloat() < 0.08f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.SPECTRAL_SILK.get(), 1));
        }
        if (tier.getLevel() >= 3 && random.nextFloat() < 0.06f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.UMBRA_INGOT.get(), 1));
        }
        // Dungeon-exclusive consumables and utility items
        if (random.nextFloat() < 0.20f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.STRANGE_MEAT.get(), 1 + random.nextInt(3)));
        }
        if (random.nextFloat() < 0.08f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.LIVING_DIVINING_ROD.get()));
        }
        if (random.nextFloat() < 0.08f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.ABSORPTION_ORB.get()));
        }
        if (random.nextFloat() < 0.15f) {
            loot.add(initWeapon(DungeonEntityRegistry.SPEAR.get(), 7.0f, random));
        }
        if (random.nextFloat() < 0.15f) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.GLOWING_JELLY.get(), 1 + random.nextInt(2)));
        }
        if (random.nextFloat() < 0.10f) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.FOLIAATH_SEED.get(), 1 + random.nextInt(2)));
        }
        if (random.nextFloat() < 0.05f) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.SKELETON_HEAD.get()));
        }
        // Paintings removed from treasure — boss-only at Nightmare+
        return loot;
    }

    private static List<ItemStack> generateBossLoot(DungeonTier tier, RandomSource random) {
        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        // Gear count and quality scale with tier — boss loot uses the dungeon's own tier
        // Fortune/luck adds bonus gear items
        int gearCount = lootFortuneBonusItems(random) + switch (tier) {
            case NORMAL -> 2;
            case HARD -> 2 + random.nextInt(2);
            case NIGHTMARE -> 3 + random.nextInt(2);
            case INFERNAL -> 4 + random.nextInt(2);
            case MYTHIC -> 5 + random.nextInt(3);
            case ETERNAL -> 7 + random.nextInt(3);
        };
        for (int i = 0; i < gearCount; ++i) {
            loot.add(DungeonLootGenerator.generateSingleItem(tier, random));
        }
        // Boss-specific trophies are handled in DungeonBossEntity.die() as rare drops
        // Ossukage Sword: rare drop, chance scales with tier
        float swordChance = switch (tier) {
            case NORMAL -> 0.05f;
            case HARD -> 0.10f;
            case NIGHTMARE -> 0.20f;
            case INFERNAL -> 0.30f;
            case MYTHIC -> 0.45f;
            case ETERNAL -> 0.60f;
        };
        if (random.nextFloat() < swordChance) {
            loot.add(initWeapon(DungeonEntityRegistry.OSSUKAGE_SWORD.get(), 9.0f, random));
        }
        // FangOnAStick: small chance, any tier
        if (random.nextFloat() < 0.15f) {
            loot.add(initWeapon(DungeonEntityRegistry.FANG_ON_A_STICK.get(), 6.0f, random));
        }
        // New items: Earthrend Gauntlet, Blowgun, Captured Grottol
        if (random.nextFloat() < 0.08f) {
            loot.add(initWeapon(DungeonEntityRegistry.EARTHREND_GAUNTLET.get(), 8.0f, random));
        }
        if (random.nextFloat() < 0.12f) {
            loot.add(initWeapon(DungeonEntityRegistry.BLOWGUN.get(), 3.0f, random));
        }
        if (random.nextFloat() < 0.10f) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.CAPTURED_GROTTOL.get()));
        }
        if (tier.getLevel() >= 2 && random.nextFloat() < 0.10f) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.GREAT_EXPERIENCE_BOTTLE.get(), 2 + random.nextInt(3)));
        }
        // Umvuthana masks: rare drop from boss
        if (random.nextFloat() < 0.08f) {
            int maskRoll = random.nextInt(6);
            ItemLike mask = switch (maskRoll) {
                case 0 -> DungeonEntityRegistry.MASK_OF_FEAR.get();
                case 1 -> DungeonEntityRegistry.MASK_OF_FURY.get();
                case 2 -> DungeonEntityRegistry.MASK_OF_FAITH.get();
                case 3 -> DungeonEntityRegistry.MASK_OF_RAGE.get();
                case 4 -> DungeonEntityRegistry.MASK_OF_MISERY.get();
                default -> DungeonEntityRegistry.MASK_OF_BLISS.get();
            };
            loot.add(new ItemStack(mask));
        }
        // Dungeon-exclusive weapon drops (boss-themed) — initialize stats immediately
        // so items show correct damage in chests and when dropped on ground
        if (random.nextFloat() < 0.10f) {
            loot.add(initWeapon(DungeonEntityRegistry.NAGA_FANG_DAGGER.get(), 5.0f, random));
        }
        if (random.nextFloat() < 0.08f) {
            loot.add(initWeapon(DungeonEntityRegistry.WROUGHT_AXE.get(), 11.0f, random));
        }
        if (random.nextFloat() < 0.08f) {
            loot.add(initArmor(DungeonEntityRegistry.WROUGHT_HELM.get(), 3.0, 1.0, EquipmentSlot.HEAD, random));
        }
        if (random.nextFloat() < 0.06f) {
            loot.add(initWeapon(DungeonEntityRegistry.LIFE_STEALER.get(), 7.0f, random));
        }
        if (random.nextFloat() < 0.06f) {
            loot.add(initWeapon(DungeonEntityRegistry.SCEPTER_OF_CHAOS.get(), 4.0f, random));
        }
        if (random.nextFloat() < 0.08f) {
            loot.add(initWeapon(DungeonEntityRegistry.SOL_VISAGE.get(), 5.0f, random));
        }
        if (random.nextFloat() < 0.10f) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.BLUFF_ROD.get()));
        }
        if (random.nextFloat() < 0.12f) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.ICE_CRYSTAL.get()));
        }
        // Dungeon armor — boss guaranteed drop at higher tiers, chance at lower
        float armorDropChance = switch (tier) {
            case NORMAL -> 0.08f;
            case HARD -> 0.12f;
            case NIGHTMARE -> 0.18f;
            case INFERNAL -> 0.25f;
            case MYTHIC -> 0.35f;
            case ETERNAL -> 0.50f;
        };
        if (random.nextFloat() < armorDropChance) {
            loot.add(pickRandomDungeonArmor(tier, random));
        }
        // Flavor drops in Hard+
        if (tier.getLevel() >= 2) {
            loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.SKELETON_BONE.get(), 1 + random.nextInt(3)));
            if (random.nextFloat() < 0.3f) {
                loot.add(new ItemStack((ItemLike)DungeonEntityRegistry.RAT_FANG.get(), 1 + random.nextInt(2)));
            }
        }
        // Dungeon-exclusive materials scale with tier
        int shardCount = switch (tier) {
            case NORMAL -> 1;
            case HARD -> 1 + random.nextInt(2);
            case NIGHTMARE -> 2 + random.nextInt(2);
            case INFERNAL -> 3 + random.nextInt(3);
            case MYTHIC -> 5 + random.nextInt(3);
            case ETERNAL -> 8 + random.nextInt(4);
        };
        loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.VOID_SHARD.get(), shardCount));
        // Dungeon-exclusive materials always drop from bosses at higher tiers
        if (tier.getLevel() >= 2) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.CERULEAN_INGOT.get(), 1 + random.nextInt(2)));
        }
        if (tier.getLevel() >= 2 && random.nextFloat() < 0.5f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.CRYSTALLINE_SHARD.get(), 1 + random.nextInt(2)));
        }
        if (tier.getLevel() >= 3) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.SPECTRAL_SILK.get(), 1 + random.nextInt(2)));
        }
        if (tier.getLevel() >= 3 && random.nextFloat() < 0.4f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.UMBRA_INGOT.get(), 1 + random.nextInt(2)));
        }
        if (tier.getLevel() >= DungeonTier.INFERNAL.getLevel()) {
            int essenceCount = switch (tier) {
                case INFERNAL -> 1 + random.nextInt(2);
                case MYTHIC -> 2 + random.nextInt(3);
                case ETERNAL -> 4 + random.nextInt(4);
                default -> 1;
            };
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.INFERNAL_ESSENCE.get(), essenceCount));
        }
        // Food/consumables scale with tier
        switch (tier) {
            case NORMAL -> loot.add(new ItemStack((ItemLike)Items.GOLDEN_APPLE, 1));
            case HARD -> loot.add(new ItemStack((ItemLike)Items.GOLDEN_APPLE, 1 + random.nextInt(2)));
            case NIGHTMARE -> loot.add(new ItemStack((ItemLike)Items.ENCHANTED_GOLDEN_APPLE, 1));
            case INFERNAL -> {
                loot.add(new ItemStack((ItemLike)Items.ENCHANTED_GOLDEN_APPLE, 1));
                loot.add(new ItemStack((ItemLike)Items.GOLDEN_APPLE, 2));
            }
            case MYTHIC -> {
                loot.add(new ItemStack((ItemLike)Items.ENCHANTED_GOLDEN_APPLE, 1 + random.nextInt(2)));
                loot.add(new ItemStack((ItemLike)Items.GOLDEN_APPLE, 3));
            }
            case ETERNAL -> {
                loot.add(new ItemStack((ItemLike)Items.ENCHANTED_GOLDEN_APPLE, 2 + random.nextInt(2)));
                loot.add(new ItemStack((ItemLike)Items.GOLDEN_APPLE, 4));
                loot.add(new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING, 1));
            }
        }
        loot.add(new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE, 4 + tier.getLevel() * 4 + random.nextInt(8)));
        // Raw materials: tier-gated
        switch (tier) {
            case NORMAL -> loot.add(new ItemStack((ItemLike)Items.IRON_INGOT, 4 + random.nextInt(4)));
            case HARD -> loot.add(new ItemStack((ItemLike)Items.DIAMOND, 1 + random.nextInt(2)));
            case NIGHTMARE -> loot.add(new ItemStack((ItemLike)Items.DIAMOND, 2 + random.nextInt(3)));
            case INFERNAL -> {
                loot.add(new ItemStack((ItemLike)Items.DIAMOND, 3 + random.nextInt(4)));
                loot.add(new ItemStack((ItemLike)Items.NETHERITE_SCRAP, 1 + random.nextInt(2)));
            }
            case MYTHIC -> {
                loot.add(new ItemStack((ItemLike)Items.DIAMOND, 5 + random.nextInt(5)));
                loot.add(new ItemStack((ItemLike)Items.NETHERITE_SCRAP, 2 + random.nextInt(3)));
                loot.add(new ItemStack((ItemLike)Items.EMERALD, 4 + random.nextInt(4)));
            }
            case ETERNAL -> {
                loot.add(new ItemStack((ItemLike)Items.DIAMOND, 8 + random.nextInt(8)));
                loot.add(new ItemStack((ItemLike)Items.NETHERITE_INGOT, 1 + random.nextInt(2)));
                loot.add(new ItemStack((ItemLike)Items.NETHERITE_SCRAP, 3 + random.nextInt(4)));
                loot.add(new ItemStack((ItemLike)Items.NETHER_STAR, 1));
            }
        }
        // Paintings: Nightmare+ only, boss chest exclusive
        if (tier.getLevel() >= 3 && random.nextFloat() < 0.15f) {
            loot.add(MasterpieceRegistry.getRandomPainting(random));
        }
        return loot;
    }

    private static List<ItemStack> generateCorridorLoot(DungeonTier tier, RandomSource random) {
        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        int roll = random.nextInt(3);
        switch (roll) {
            case 0: {
                loot.add(new ItemStack((ItemLike)Items.ARROW, 4 + random.nextInt(8)));
                break;
            }
            case 1: {
                loot.add(new ItemStack((ItemLike)Items.BREAD, 2 + random.nextInt(4)));
                break;
            }
            case 2: {
                loot.add(new ItemStack((ItemLike)Items.TORCH, 8 + random.nextInt(8)));
            }
        }
        return loot;
    }

    public static List<List<ItemStack>> generateDistributedLoot(RoomTemplate.RoomType roomType, DungeonTier tier, int chestCount, RandomSource random) {
        int i;
        List<ItemStack> totalLoot = DungeonChestLoot.generateChestLoot(roomType, tier, random);
        ArrayList<List<ItemStack>> distributed = new ArrayList<List<ItemStack>>();
        for (i = 0; i < chestCount; ++i) {
            distributed.add(new ArrayList());
        }
        for (i = 0; i < totalLoot.size(); ++i) {
            ((List)distributed.get(i % chestCount)).add(totalLoot.get(i));
        }
        for (List list : distributed) {
            if (!list.isEmpty()) continue;
            list.add(new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE, 2 + random.nextInt(4)));
        }
        return distributed;
    }

    private static List<ItemStack> generatePuzzleLoot(DungeonTier tier, RandomSource random) {
        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        loot.add(new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE, 2 + random.nextInt(4)));
        if (random.nextFloat() < 0.5f) {
            loot.add(DungeonLootGenerator.generateSingleItem(tier, random));
        }
        // Fortune bonus items
        int bonus = lootFortuneBonusItems(random);
        for (int i = 0; i < bonus; i++) {
            loot.add(DungeonLootGenerator.generateSingleItem(tier, random));
        }
        if (random.nextFloat() < 0.15f) {
            loot.add(new ItemStack((ItemLike)Items.GOLDEN_APPLE, 1));
        }
        if (random.nextFloat() < 0.25f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.WARP_STONE.get()));
        }
        return loot;
    }

    private static List<ItemStack> generateOreDepositLoot(DungeonTier tier, RandomSource random) {
        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        // Ore-themed loot scaled by tier
        switch (tier) {
            case NORMAL -> {
                loot.add(new ItemStack((ItemLike)Items.RAW_IRON, 4 + random.nextInt(6)));
                loot.add(new ItemStack((ItemLike)Items.RAW_GOLD, 2 + random.nextInt(4)));
            }
            case HARD -> {
                loot.add(new ItemStack((ItemLike)Items.RAW_IRON, 6 + random.nextInt(8)));
                loot.add(new ItemStack((ItemLike)Items.RAW_GOLD, 4 + random.nextInt(4)));
                loot.add(new ItemStack((ItemLike)Items.DIAMOND, 1));
            }
            case NIGHTMARE -> {
                loot.add(new ItemStack((ItemLike)Items.RAW_GOLD, 6 + random.nextInt(6)));
                loot.add(new ItemStack((ItemLike)Items.DIAMOND, 1 + random.nextInt(2)));
                loot.add(new ItemStack((ItemLike)Items.EMERALD, 2 + random.nextInt(3)));
            }
            case INFERNAL -> {
                loot.add(new ItemStack((ItemLike)Items.DIAMOND, 2 + random.nextInt(3)));
                loot.add(new ItemStack((ItemLike)Items.EMERALD, 3 + random.nextInt(4)));
                loot.add(new ItemStack((ItemLike)Items.RAW_GOLD, 8 + random.nextInt(8)));
            }
            case MYTHIC -> {
                loot.add(new ItemStack((ItemLike)Items.DIAMOND, 4 + random.nextInt(4)));
                loot.add(new ItemStack((ItemLike)Items.EMERALD, 5 + random.nextInt(5)));
                loot.add(new ItemStack((ItemLike)Items.RAW_GOLD, 10 + random.nextInt(10)));
                loot.add(new ItemStack((ItemLike)Items.NETHERITE_SCRAP, 1));
            }
            case ETERNAL -> {
                loot.add(new ItemStack((ItemLike)Items.DIAMOND, 6 + random.nextInt(6)));
                loot.add(new ItemStack((ItemLike)Items.EMERALD, 8 + random.nextInt(8)));
                loot.add(new ItemStack((ItemLike)Items.RAW_GOLD, 16 + random.nextInt(16)));
                loot.add(new ItemStack((ItemLike)Items.NETHERITE_SCRAP, 2 + random.nextInt(2)));
                loot.add(new ItemStack((ItemLike)Items.NETHER_STAR, 1));
            }
        }
        // Dungeon materials
        loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.VOID_SHARD.get(), 1));
        if (random.nextFloat() < 0.3f) {
            loot.add(new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE, 2 + random.nextInt(3)));
        }
        return loot;
    }

    private static List<ItemStack> generateCorridorRewardLoot(DungeonTier tier, RandomSource random) {
        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        // 1-2 modest rolled items, fortune can add more
        int itemCount = 1 + (random.nextFloat() < 0.4f ? 1 : 0) + lootFortuneBonusItems(random);
        for (int i = 0; i < itemCount; ++i) {
            loot.add(DungeonLootGenerator.generateSingleItem(tier, random));
        }
        // Consumables
        if (random.nextFloat() < 0.5f) {
            loot.add(new ItemStack((ItemLike)Items.COOKED_BEEF, 3 + random.nextInt(3)));
        }
        if (random.nextFloat() < 0.4f) {
            ItemStack potion = new ItemStack((ItemLike)Items.POTION);
            potion.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.STRONG_HEALING));
            loot.add(potion);
        }
        loot.add(new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE, 1 + random.nextInt(3)));
        return loot;
    }

    private static List<ItemStack> generateGrandHallLoot(DungeonTier tier, RandomSource random) {
        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        loot.add(DungeonLootGenerator.generateSingleItem(tier, random));
        if (random.nextFloat() < 0.3f) {
            loot.add(new ItemStack((ItemLike)Items.GOLDEN_APPLE, 1));
        }
        loot.add(new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE, 2 + random.nextInt(4)));
        if (random.nextFloat() < 0.2f) {
            loot.add(new ItemStack((ItemLike)DungeonExclusiveItems.WARP_STONE.get()));
        }
        return loot;
    }

    private static List<ItemStack> generatePrisonLoot(DungeonTier tier, RandomSource random) {
        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        // Prison cells have modest loot
        if (random.nextFloat() < 0.6f) {
            loot.add(DungeonLootGenerator.generateSingleItem(tier, random));
        }
        loot.add(new ItemStack((ItemLike)Items.BONE, 2 + random.nextInt(3)));
        if (random.nextFloat() < 0.3f) {
            loot.add(new ItemStack((ItemLike)Items.IRON_INGOT, 1 + random.nextInt(3)));
        }
        loot.add(new ItemStack((ItemLike)Items.EXPERIENCE_BOTTLE, 1 + random.nextInt(2)));
        return loot;
    }

    /**
     * Create a dungeon weapon ItemStack with stats initialized immediately.
     * Dungeon-exclusive weapons extend plain Item and normally rely on inventoryTick()
     * to call WeaponStatRoller — but that only fires when in a player inventory,
     * leaving items in chests/on ground with 0 damage. This initializes stats at creation.
     */
    private static ItemStack initWeapon(ItemLike item, float baseDamage, RandomSource random) {
        ItemStack stack = new ItemStack(item);
        if (!WeaponStatRoller.isWeaponInitialized(stack)) {
            boolean isShield = item.asItem() instanceof com.ultra.megamod.feature.relics.weapons.RpgWeaponItem rpg && rpg.isShield();
            WeaponStatRoller.rollAndApply(stack, baseDamage, random, isShield);
        }
        return stack;
    }

    /**
     * Create a dungeon armor ItemStack with stats initialized immediately.
     */
    private static ItemStack initArmor(ItemLike item, double baseArmor, double baseToughness, EquipmentSlot slot, RandomSource random) {
        ItemStack stack = new ItemStack(item);
        if (!ArmorStatRoller.isArmorInitialized(stack)) {
            ArmorStatRoller.rollAndApply(stack, baseArmor, baseToughness, slot, random);
        }
        return stack;
    }

    /**
     * Pick a random dungeon armor piece appropriate for the dungeon tier.
     * Material scales with tier: Chainmail (Normal), Iron (Hard), Diamond (Nightmare), Netherite (Infernal+).
     * Lower tier materials can still appear at higher tiers with reduced probability.
     */
    private static ItemStack pickRandomDungeonArmor(DungeonTier tier, RandomSource random) {
        DungeonArmorItem.Material material = switch (tier) {
            case NORMAL -> DungeonArmorItem.Material.CHAINMAIL;
            case HARD -> random.nextFloat() < 0.65f ? DungeonArmorItem.Material.IRON : DungeonArmorItem.Material.CHAINMAIL;
            case NIGHTMARE -> random.nextFloat() < 0.65f ? DungeonArmorItem.Material.DIAMOND : DungeonArmorItem.Material.IRON;
            case INFERNAL -> random.nextFloat() < 0.65f ? DungeonArmorItem.Material.NETHERITE : DungeonArmorItem.Material.DIAMOND;
            case MYTHIC, ETERNAL -> DungeonArmorItem.Material.NETHERITE;
        };
        EquipmentSlot slot = switch (random.nextInt(4)) {
            case 0 -> EquipmentSlot.HEAD;
            case 1 -> EquipmentSlot.CHEST;
            case 2 -> EquipmentSlot.LEGS;
            default -> EquipmentSlot.FEET;
        };
        Item item = DungeonExclusiveItems.getDungeonArmor(material, slot);
        return initArmor(item, material.getArmor(slot), material.getToughness(slot), slot, random);
    }
}

