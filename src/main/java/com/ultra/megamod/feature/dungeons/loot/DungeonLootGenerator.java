/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.EquipmentSlotGroup
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.ItemAttributeModifiers
 *  net.minecraft.world.item.component.ItemAttributeModifiers$Builder
 *  net.minecraft.world.item.component.ItemLore
 *  net.minecraft.world.level.ItemLike
 */
package com.ultra.megamod.feature.dungeons.loot;

import com.ultra.megamod.feature.combat.items.ClassArmorRegistry;
import com.ultra.megamod.feature.dungeons.DungeonTheme;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.loot.DungeonChestLoot;
import com.ultra.megamod.feature.dungeons.loot.DungeonExclusiveItems;
import com.ultra.megamod.feature.dungeons.loot.LootModifier;
import com.ultra.megamod.feature.dungeons.loot.LootQuality;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.ItemLike;

public class DungeonLootGenerator {
    /** Optional quality cap — when set, rolled quality is clamped to this maximum. */
    public static LootQuality maxQualityCap = null;

    /** Set true before generating loot for admin players — boosts ultra-rare drop rates. */
    public static boolean adminLootBoost = false;

    /**
     * Lazily-built set of all class armor items (from ClassArmorRegistry).
     * Used to detect class armor in generateFromBase() for quality rolling.
     */
    private static volatile Set<Item> CLASS_ARMOR_ITEMS;
    private static Set<Item> getClassArmorItems() {
        if (CLASS_ARMOR_ITEMS == null) {
            try {
                Set<Item> set = new HashSet<>();
                set.addAll(ClassArmorRegistry.getTier1Items());
                set.addAll(ClassArmorRegistry.getTier2Items());
                set.addAll(ClassArmorRegistry.getTier3Items());
                CLASS_ARMOR_ITEMS = set;
            } catch (Exception e) {
                return Set.of();
            }
        }
        return CLASS_ARMOR_ITEMS;
    }

    /** Returns true if the item is a class armor piece (Equippable + Attribute Modifiers). */
    private static boolean isClassArmor(Item item) {
        return getClassArmorItems().contains(item);
    }

    /** Safely adds items from combat registries (catches errors if registries not loaded). */
    private static void addClassItemsSafe(List<Item> list, List<Item> items) {
        try { if (items != null) list.addAll(items); } catch (Exception ignored) {}
    }

    // All tiers include mod weapons — higher tiers add more powerful variants + better vanilla gear.
    // Pools are built lazily because DeferredItem .get() requires registries to be loaded.
    private static volatile List<Item> NORMAL_ITEMS;
    private static volatile List<Item> HARD_ITEMS;
    private static volatile List<Item> NIGHTMARE_ITEMS;
    private static volatile List<Item> INFERNAL_ITEMS;
    private static volatile List<Item> MYTHIC_ITEMS;
    private static volatile List<Item> ETERNAL_ITEMS;

    private static List<Item> getNormalItems() {
        if (NORMAL_ITEMS == null) {
            List<Item> list = new ArrayList<>();
            // Vanilla: iron gear
            list.addAll(List.of(Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_SHOVEL,
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
                Items.BOW, Items.CROSSBOW, Items.SHIELD));
            // Dungeon Chainmail armor (Normal tier)
            list.addAll(List.of(
                DungeonExclusiveItems.DUNGEON_CHAINMAIL_HELMET.get(),
                DungeonExclusiveItems.DUNGEON_CHAINMAIL_CHESTPLATE.get(),
                DungeonExclusiveItems.DUNGEON_CHAINMAIL_LEGGINGS.get(),
                DungeonExclusiveItems.DUNGEON_CHAINMAIL_BOOTS.get()));
            // Class tier weapons (T0-T1)
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.ClassWeaponRegistry.getNormalTierItems());
            // T1 class armor
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.ClassArmorRegistry.getTier1Items());
            // Basic jewelry
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.JewelryRegistry.getBasicItems());
            // Raw gem crafting materials
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.JewelryRegistry.getRawGemItems());
            // Spell scrolls (teach individual spells)
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.spell.SpellItemRegistry.getNormalTierScrolls());
            // Archer utility items (Normal tier)
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.ArcherItemRegistry.getNormalTierItems());
            NORMAL_ITEMS = Collections.unmodifiableList(list);
        }
        return NORMAL_ITEMS;
    }

    private static List<Item> getHardItems() {
        if (HARD_ITEMS == null) {
            List<Item> list = new ArrayList<>(getNormalItems());
            // Vanilla: add diamond
            list.addAll(List.of(Items.DIAMOND_SWORD, Items.DIAMOND_AXE,
                Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS));
            // Dungeon Iron armor (Hard tier)
            list.addAll(List.of(
                DungeonExclusiveItems.DUNGEON_IRON_HELMET.get(),
                DungeonExclusiveItems.DUNGEON_IRON_CHESTPLATE.get(),
                DungeonExclusiveItems.DUNGEON_IRON_LEGGINGS.get(),
                DungeonExclusiveItems.DUNGEON_IRON_BOOTS.get()));
            // Class tier weapons (T2 diamond)
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.ClassWeaponRegistry.getHardTierItems());
            // T2 class armor
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.ClassArmorRegistry.getTier2Items());
            // Gem jewelry (T2)
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.JewelryRegistry.getGemItems());
            // Spell books (grant school-wide spell access in offhand)
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.spell.SpellItemRegistry.getHardTierBooks());
            // Archer utility items (Hard tier)
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.ArcherItemRegistry.getHardTierItems());
            HARD_ITEMS = Collections.unmodifiableList(list);
        }
        return HARD_ITEMS;
    }

    private static List<Item> getNightmareItems() {
        if (NIGHTMARE_ITEMS == null) {
            List<Item> list = new ArrayList<>(getHardItems());
            // Vanilla: add trident
            list.add(Items.TRIDENT);
            // Dungeon Diamond armor (Nightmare tier)
            list.addAll(List.of(
                DungeonExclusiveItems.DUNGEON_DIAMOND_HELMET.get(),
                DungeonExclusiveItems.DUNGEON_DIAMOND_CHESTPLATE.get(),
                DungeonExclusiveItems.DUNGEON_DIAMOND_LEGGINGS.get(),
                DungeonExclusiveItems.DUNGEON_DIAMOND_BOOTS.get()));
            // Class tier weapons (T3 netherite)
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.ClassWeaponRegistry.getNightmareTierItems());
            // T3 class armor
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.ClassArmorRegistry.getTier3Items());
            // Netherite jewelry (T3)
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.JewelryRegistry.getNetheriteItems());
            // Archer utility items (Nightmare tier)
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.ArcherItemRegistry.getNightmareTierItems());
            NIGHTMARE_ITEMS = Collections.unmodifiableList(list);
        }
        return NIGHTMARE_ITEMS;
    }

    private static List<Item> getInfernalItems() {
        if (INFERNAL_ITEMS == null) {
            List<Item> list = new ArrayList<>(getNightmareItems());
            // Vanilla: add netherite
            list.addAll(List.of(Items.NETHERITE_SWORD, Items.NETHERITE_AXE,
                Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS));
            // Dungeon Netherite armor (Infernal tier)
            list.addAll(List.of(
                DungeonExclusiveItems.DUNGEON_NETHERITE_HELMET.get(),
                DungeonExclusiveItems.DUNGEON_NETHERITE_CHESTPLATE.get(),
                DungeonExclusiveItems.DUNGEON_NETHERITE_LEGGINGS.get(),
                DungeonExclusiveItems.DUNGEON_NETHERITE_BOOTS.get()));
            // Unique jewelry (T4)
            addClassItemsSafe(list, com.ultra.megamod.feature.combat.items.JewelryRegistry.getUniqueItems());
            INFERNAL_ITEMS = Collections.unmodifiableList(list);
        }
        return INFERNAL_ITEMS;
    }
    private static List<Item> getMythicItems() {
        if (MYTHIC_ITEMS == null) {
            List<Item> list = new ArrayList<>(getInfernalItems());
            // Mythic: Mythic Netherite gear + extra netherite weight + top consumables
            list.addAll(List.of(
                DungeonExclusiveItems.MYTHIC_NETHERITE_SWORD.get(),
                DungeonExclusiveItems.MYTHIC_NETHERITE_AXE.get(),
                Items.NETHERITE_SWORD, Items.NETHERITE_AXE,
                Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
                Items.ENCHANTED_GOLDEN_APPLE, Items.TOTEM_OF_UNDYING));
            MYTHIC_ITEMS = Collections.unmodifiableList(list);
        }
        return MYTHIC_ITEMS;
    }

    private static List<Item> getEternalItems() {
        if (ETERNAL_ITEMS == null) {
            List<Item> list = new ArrayList<>(getMythicItems());
            // Eternal: heavy Mythic Netherite + Nether Star + extra top consumables
            list.addAll(List.of(
                DungeonExclusiveItems.MYTHIC_NETHERITE_SWORD.get(),
                DungeonExclusiveItems.MYTHIC_NETHERITE_AXE.get(),
                DungeonExclusiveItems.MYTHIC_NETHERITE_SWORD.get(),
                DungeonExclusiveItems.MYTHIC_NETHERITE_AXE.get(),
                Items.ENCHANTED_GOLDEN_APPLE, Items.TOTEM_OF_UNDYING, Items.NETHER_STAR));
            ETERNAL_ITEMS = Collections.unmodifiableList(list);
        }
        return ETERNAL_ITEMS;
    }

    // Legacy combined list (kept for slot group lookups)
    private static final List<Item> ALL_BASE_ITEMS;

    public static List<ItemStack> generateLoot(DungeonTier tier, DungeonTheme theme, RandomSource random) {
        return generateLoot(tier, theme, random, null);
    }

    /**
     * Generate loot with optional class bias. When a player is provided and has a class,
     * ~60% of drops are biased toward items matching their class.
     */
    public static List<ItemStack> generateLoot(DungeonTier tier, DungeonTheme theme, RandomSource random,
                                                net.minecraft.server.level.ServerPlayer player) {
        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        int itemCount = switch (tier) {
            default -> throw new MatchException(null, null);
            case DungeonTier.NORMAL -> 1 + random.nextInt(2);
            case DungeonTier.HARD -> 2 + random.nextInt(2);
            case DungeonTier.NIGHTMARE -> 3 + random.nextInt(2);
            case DungeonTier.INFERNAL -> 4 + random.nextInt(2);
            case DungeonTier.MYTHIC -> 5 + random.nextInt(3);
            case DungeonTier.ETERNAL -> 7 + random.nextInt(3);
        };
        for (int i = 0; i < itemCount; ++i) {
            loot.add(DungeonLootGenerator.generateSingleItemForPlayer(tier, random, player));
        }

        // Bonus: Class-biased rune drops (scale with tier)
        float runeChance = switch (tier) {
            case NORMAL -> 0.20f;
            case HARD -> 0.30f;
            case NIGHTMARE -> 0.40f;
            case INFERNAL -> 0.55f;
            case MYTHIC -> 0.70f;
            case ETERNAL -> 0.90f;
        };
        if (random.nextFloat() < runeChance) {
            Item chosenRune = pickClassBiasedRune(player, random);
            int runeCount = 1 + random.nextInt(tier.ordinal() + 1);
            loot.add(new ItemStack(chosenRune, Math.min(runeCount, 8)));
        }

        // Bonus: Guaranteed class spell book at HARD+ (15% chance, only if player has class)
        if (tier.ordinal() >= DungeonTier.HARD.ordinal() && player != null && random.nextFloat() < 0.15f) {
            Item classBook = pickClassSpellBook(player);
            if (classBook != null) {
                loot.add(new ItemStack(classBook));
            }
        }

        return loot;
    }

    public static ItemStack generateSingleItem(DungeonTier tier, RandomSource random) {
        Item baseItem = DungeonLootGenerator.pickBaseItem(tier, random);
        return generateFromBase(baseItem, tier, random);
    }

    /**
     * Generate a single loot item with class bias for the given player.
     * 60% chance to reroll non-class items toward the player's class.
     */
    public static ItemStack generateSingleItemForPlayer(DungeonTier tier, RandomSource random,
                                                         net.minecraft.server.level.ServerPlayer player) {
        Item baseItem = DungeonLootGenerator.pickBaseItem(tier, random);

        // Class-bias loot rerolling retired with the class-selection system —
        // every player now gets unbiased tier-appropriate drops.

        return generateFromBase(baseItem, tier, random);
    }

    /**
     * Generate a rolled loot item from a specific base item (for fishing, structure chests, shop).
     */
    public static ItemStack generateFromBase(Item baseItem, DungeonTier tier, RandomSource random) {
        ItemStack stack = new ItemStack((ItemLike)baseItem);
        LootQuality quality = LootQuality.rollForTier(tier, random, DungeonChestLoot.activeFortuneBonus);
        if (maxQualityCap != null && quality.ordinal() > maxQualityCap.ordinal()) {
            quality = maxQualityCap;
        }
        // Record loot drop for admin Dungeon Analytics tab
        com.ultra.megamod.feature.computer.network.handlers.DungeonAnalyticsHandler.recordLootDrop(quality.ordinal());
        int modCount = quality.getModifierCount();
        // Soka Singing Blade gets +2 bonus attribute slots
        ArrayList<LootModifier> available = new ArrayList<LootModifier>(LootModifier.ALL_MODIFIERS);
        Collections.shuffle(available, new Random(random.nextLong()));
        EquipmentSlotGroup slotGroup = DungeonLootGenerator.getSlotGroupForItem(baseItem);
        // Start with the item's default attribute modifiers (base damage, attack speed, etc.)
        // so we append loot bonuses on top rather than replacing them
        ItemAttributeModifiers existingMods = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder modBuilder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : existingMods.modifiers()) {
            modBuilder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        // RpgWeaponItem doesn't have default attribute modifiers — add base damage scaled by quality
        if (baseItem instanceof com.ultra.megamod.feature.relics.weapons.RpgWeaponItem rpgWeapon) {
            float baseDmg = rpgWeapon.getBaseDamage();
            float qualityMult = switch (quality) {
                case COMMON -> 1.0f;
                case UNCOMMON -> 1.15f;
                case RARE -> 1.3f;
                case EPIC -> 1.5f;
                case LEGENDARY -> 2.0f;
            };
            float finalDmg = baseDmg * qualityMult;
            // Minecraft adds 1.0 base damage, so subtract 1 for the modifier value
            modBuilder.add(
                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Identifier.fromNamespaceAndPath("megamod", "weapon_base_damage"), (double)(finalDmg - 1.0f), AttributeModifier.Operation.ADD_VALUE),
                slotGroup
            );
            // Also add attack speed (slightly faster than default sword)
            modBuilder.add(
                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                new AttributeModifier(Identifier.fromNamespaceAndPath("megamod", "weapon_attack_speed"), -2.2, AttributeModifier.Operation.ADD_VALUE),
                slotGroup
            );
        }
        // (Mythic Netherite armor was removed; only weapons remain — handled below.)
        // DungeonArmorItem — add base armor/toughness scaled by quality
        if (baseItem instanceof com.ultra.megamod.feature.dungeons.item.DungeonArmorItem dungeonArmor) {
            float qualityMult = switch (quality) {
                case COMMON -> 1.0f;
                case UNCOMMON -> 1.15f;
                case RARE -> 1.3f;
                case EPIC -> 1.5f;
                case LEGENDARY -> 2.0f;
            };
            double finalArmor = dungeonArmor.getMaterial().getArmor(dungeonArmor.getArmorSlot()) * qualityMult;
            double finalToughness = dungeonArmor.getMaterial().getToughness(dungeonArmor.getArmorSlot()) * qualityMult;
            modBuilder.add(
                net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,
                new AttributeModifier(Identifier.fromNamespaceAndPath("megamod", "armor_base"), finalArmor, AttributeModifier.Operation.ADD_VALUE),
                slotGroup
            );
            if (finalToughness > 0) {
                modBuilder.add(
                    net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(Identifier.fromNamespaceAndPath("megamod", "armor_toughness_base"), finalToughness, AttributeModifier.Operation.ADD_VALUE),
                    slotGroup
                );
            }
        }
        // Class armor (plain Item with Equippable + ATTRIBUTE_MODIFIERS) — scale existing armor values by quality
        if (isClassArmor(baseItem)) {
            float qualityMult = switch (quality) {
                case COMMON -> 1.0f;
                case UNCOMMON -> 1.1f;
                case RARE -> 1.2f;
                case EPIC -> 1.4f;
                case LEGENDARY -> 1.6f;
            };
            // Rebuild modBuilder with scaled armor/toughness values from the existing modifiers
            ItemAttributeModifiers.Builder scaledBuilder = ItemAttributeModifiers.builder();
            for (ItemAttributeModifiers.Entry entry : existingMods.modifiers()) {
                Holder<Attribute> attr = entry.attribute();
                AttributeModifier mod = entry.modifier();
                // Scale ARMOR and ARMOR_TOUGHNESS modifiers by quality multiplier
                if (attr.equals(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)
                    || attr.equals(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS)) {
                    double scaledValue = mod.amount() * qualityMult;
                    AttributeModifier scaledMod = new AttributeModifier(mod.id(), scaledValue, mod.operation());
                    scaledBuilder.add(attr, scaledMod, entry.slot());
                } else {
                    scaledBuilder.add(attr, mod, entry.slot());
                }
            }
            // Replace modBuilder contents: clear and rebuild from scaled values + any previously added mods
            modBuilder = scaledBuilder;
        }
        ArrayList<String> modDescriptions = new ArrayList<String>();
        int applied = 0;
        for (int i = 0; i < available.size() && applied < modCount; ++i) {
            LootModifier lootMod = (LootModifier)available.get(i);
            Optional<Holder<Attribute>> attrHolder = DungeonLootGenerator.resolveAttribute(lootMod.attributeId());
            if (attrHolder.isEmpty()) continue;
            double value = lootMod.roll(random, tier.getDifficultyMultiplier(), DungeonChestLoot.activeFortuneBonus);
            AttributeModifier modifier = new AttributeModifier(Identifier.fromNamespaceAndPath((String)"megamod", (String)("dungeon_loot_" + applied + "_" + random.nextInt(10000))), value, lootMod.operation());
            modBuilder.add(attrHolder.get(), modifier, slotGroup);
            modDescriptions.add(DungeonLootGenerator.formatModifier(lootMod, value));
            ++applied;
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modBuilder.build());

        // Mark mod weapons/relics/armor as initialized so their inventoryTick doesn't re-roll stats.
        if (baseItem instanceof com.ultra.megamod.feature.relics.weapons.RpgWeaponItem) {
            net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            tag.putBoolean("weapon_stats_initialized", true);
            tag.putInt("weapon_rarity", quality.ordinal());
            stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        } else if (baseItem instanceof com.ultra.megamod.feature.relics.RelicItem) {
            net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            tag.putBoolean("weapon_stats_initialized", true);
            tag.putInt("weapon_rarity", quality.ordinal());
            tag.putBoolean("relic_initialized", true);
            tag.putInt("relic_level", 0);
            tag.putInt("relic_xp", 0);
            stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        } else if (baseItem instanceof com.ultra.megamod.feature.dungeons.item.MythicNetheriteItem) {
            net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            tag.putBoolean("weapon_stats_initialized", true);
            tag.putInt("weapon_rarity", quality.ordinal());
            stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        } else if (baseItem instanceof com.ultra.megamod.feature.dungeons.item.DungeonArmorItem dungeonArmorItem) {
            net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            tag.putBoolean("armor_stats_initialized", true);
            tag.putInt("armor_rarity", quality.ordinal());
            float qualityMult = switch (quality) {
                case COMMON -> 1.0f;
                case UNCOMMON -> 1.15f;
                case RARE -> 1.3f;
                case EPIC -> 1.5f;
                case LEGENDARY -> 2.0f;
            };
            tag.putDouble("armor_base_value", dungeonArmorItem.getMaterial().getArmor(dungeonArmorItem.getArmorSlot()) * qualityMult);
            tag.putDouble("armor_toughness_value", dungeonArmorItem.getMaterial().getToughness(dungeonArmorItem.getArmorSlot()) * qualityMult);
            stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        } else if (isClassArmor(baseItem)) {
            net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            tag.putBoolean("armor_stats_initialized", true);
            tag.putInt("armor_rarity", quality.ordinal());
            stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        }

        String baseName = stack.getHoverName().getString();
        String prefix = quality.getPrefix();
        String displayName = prefix.isEmpty() ? baseName : prefix + " " + baseName;
        ChatFormatting nameColor = switch (quality) {
            default -> throw new MatchException(null, null);
            case LootQuality.COMMON -> ChatFormatting.GRAY;
            case LootQuality.UNCOMMON -> ChatFormatting.GREEN;
            case LootQuality.RARE -> ChatFormatting.BLUE;
            case LootQuality.EPIC -> ChatFormatting.LIGHT_PURPLE;
            case LootQuality.LEGENDARY -> ChatFormatting.GOLD;
        };
        stack.set(DataComponents.CUSTOM_NAME, Component.literal((String)displayName).withStyle(nameColor));
        ArrayList<MutableComponent> loreLines = new ArrayList<MutableComponent>();
        loreLines.add(Component.literal((String)"Dungeon Loot").withStyle(Style.EMPTY.withColor(0xFF8844).withItalic(Boolean.valueOf(false))));
        loreLines.add(Component.literal((String)("Quality: " + quality.name())).withStyle(Style.EMPTY.withColor(quality.getNameColor()).withItalic(Boolean.valueOf(false))));
        loreLines.add(Component.empty());
        for (String desc : modDescriptions) {
            loreLines.add(Component.literal((String)desc).withStyle(Style.EMPTY.withColor(0x55FFFF).withItalic(Boolean.valueOf(false))));
        }
        stack.set(DataComponents.LORE, new ItemLore((List<Component>)(List<?>)loreLines));
        return stack;
    }

    public static int generateCoinReward(DungeonTier tier, RandomSource random) {
        int base = 50 + random.nextInt(151);
        return (int)((float)base * tier.getDifficultyMultiplier());
    }

    /**
     * Picks a rune biased toward the player's class school.
     * Wizards get arcane/fire/frost runes, Paladins get healing, etc.
     */
    private static Item pickClassBiasedRune(net.minecraft.server.level.ServerPlayer player, RandomSource random) {
        Item[] allRunes = {
            com.ultra.megamod.feature.combat.runes.RuneRegistry.ARCANE_RUNE.get(),
            com.ultra.megamod.feature.combat.runes.RuneRegistry.FIRE_RUNE.get(),
            com.ultra.megamod.feature.combat.runes.RuneRegistry.FROST_RUNE.get(),
            com.ultra.megamod.feature.combat.runes.RuneRegistry.HEALING_RUNE.get(),
            com.ultra.megamod.feature.combat.runes.RuneRegistry.LIGHTNING_RUNE.get(),
            com.ultra.megamod.feature.combat.runes.RuneRegistry.SOUL_RUNE.get()
        };
        // Class-biased rune selection retired — pick any rune uniformly.
        return allRunes[random.nextInt(allRunes.length)];
    }

    /**
     * Picks a random school spell book. Class-specific books + class-matched
     * picking retired with the class-selection system.
     */
    private static Item pickClassSpellBook(net.minecraft.server.level.ServerPlayer player) {
        Item[] books = new Item[]{
            com.ultra.megamod.feature.combat.spell.SpellItemRegistry.ARCANE_SPELL_BOOK.get(),
            com.ultra.megamod.feature.combat.spell.SpellItemRegistry.FIRE_SPELL_BOOK.get(),
            com.ultra.megamod.feature.combat.spell.SpellItemRegistry.FROST_SPELL_BOOK.get(),
            com.ultra.megamod.feature.combat.spell.SpellItemRegistry.HEALING_SPELL_BOOK.get()
        };
        return books[(int)(Math.random() * books.length)];
    }

    private static Item pickBaseItem(DungeonTier tier, RandomSource random) {
        // Ultra-rare: Soka Singing Blade — 0.21% for normal players, 50% for admins, Eternal only
        if (tier == DungeonTier.ETERNAL) {
            float sokaChance = adminLootBoost ? 0.50f : 0.0021f;
            if (random.nextFloat() < sokaChance) {
            }
        }
        // Weighted selection: favor current tier's exclusive items
        // 60% current tier, 25% one tier below, 10% two tiers below, 5% any lower (fallback)
        float roll = random.nextFloat();
        if (roll < 0.60f) {
            List<Item> exclusive = getCurrentTierExclusive(tier);
            if (!exclusive.isEmpty()) return exclusive.get(random.nextInt(exclusive.size()));
        } else if (roll < 0.85f) {
            DungeonTier lower = getLowerTier(tier);
            if (lower != null) {
                List<Item> lowerExclusive = getCurrentTierExclusive(lower);
                if (!lowerExclusive.isEmpty()) return lowerExclusive.get(random.nextInt(lowerExclusive.size()));
            }
        } else if (roll < 0.95f) {
            DungeonTier lower2 = getLowerTier(getLowerTier(tier));
            if (lower2 != null) {
                List<Item> lower2Exclusive = getCurrentTierExclusive(lower2);
                if (!lower2Exclusive.isEmpty()) return lower2Exclusive.get(random.nextInt(lower2Exclusive.size()));
            }
        }
        // Fallback: full pool uniform random (5% or when exclusive list is empty)
        List<Item> pool = getPoolForTier(tier);
        return pool.get(random.nextInt(pool.size()));
    }

    /**
     * Returns ONLY the items added at a specific tier (not inherited from lower tiers).
     */
    private static List<Item> getCurrentTierExclusive(DungeonTier tier) {
        return switch (tier) {
            case NORMAL -> getNormalItems();
            case HARD -> {
                List<Item> full = getHardItems();
                List<Item> lower = getNormalItems();
                List<Item> exclusive = new ArrayList<>(full);
                exclusive.removeAll(lower);
                yield exclusive;
            }
            case NIGHTMARE -> {
                List<Item> full = getNightmareItems();
                List<Item> lower = getHardItems();
                List<Item> exclusive = new ArrayList<>(full);
                exclusive.removeAll(lower);
                yield exclusive;
            }
            case INFERNAL -> {
                List<Item> full = getInfernalItems();
                List<Item> lower = getNightmareItems();
                List<Item> exclusive = new ArrayList<>(full);
                exclusive.removeAll(lower);
                yield exclusive;
            }
            case MYTHIC -> {
                List<Item> full = getMythicItems();
                List<Item> lower = getInfernalItems();
                List<Item> exclusive = new ArrayList<>(full);
                exclusive.removeAll(lower);
                yield exclusive;
            }
            case ETERNAL -> {
                List<Item> full = getEternalItems();
                List<Item> lower = getMythicItems();
                List<Item> exclusive = new ArrayList<>(full);
                exclusive.removeAll(lower);
                yield exclusive;
            }
        };
    }

    /**
     * Returns the tier one level below, or null for NORMAL.
     */
    private static DungeonTier getLowerTier(DungeonTier tier) {
        if (tier == null) return null;
        return switch (tier) {
            case NORMAL -> null;
            case HARD -> DungeonTier.NORMAL;
            case NIGHTMARE -> DungeonTier.HARD;
            case INFERNAL -> DungeonTier.NIGHTMARE;
            case MYTHIC -> DungeonTier.INFERNAL;
            case ETERNAL -> DungeonTier.MYTHIC;
        };
    }

    /**
     * Returns the full item pool for a tier (including all inherited items).
     */
    private static List<Item> getPoolForTier(DungeonTier tier) {
        return switch (tier) {
            case NORMAL -> getNormalItems();
            case HARD -> getHardItems();
            case NIGHTMARE -> getNightmareItems();
            case INFERNAL -> getInfernalItems();
            case MYTHIC -> getMythicItems();
            case ETERNAL -> getEternalItems();
        };
    }

    private static EquipmentSlotGroup getSlotGroupForItem(Item item) {
        // Dungeon armor (Chainmail/Iron/Diamond/Netherite rollable variants)
        if (item instanceof com.ultra.megamod.feature.dungeons.item.DungeonArmorItem dungeonArmor) {
            return switch (dungeonArmor.getArmorSlot()) {
                case HEAD -> EquipmentSlotGroup.HEAD;
                case CHEST -> EquipmentSlotGroup.CHEST;
                case LEGS -> EquipmentSlotGroup.LEGS;
                case FEET -> EquipmentSlotGroup.FEET;
                default -> EquipmentSlotGroup.MAINHAND;
            };
        }
        if (item == Items.IRON_HELMET || item == Items.DIAMOND_HELMET || item == Items.NETHERITE_HELMET) {
            return EquipmentSlotGroup.HEAD;
        }
        if (item == Items.IRON_CHESTPLATE || item == Items.DIAMOND_CHESTPLATE || item == Items.NETHERITE_CHESTPLATE) {
            return EquipmentSlotGroup.CHEST;
        }
        if (item == Items.IRON_LEGGINGS || item == Items.DIAMOND_LEGGINGS || item == Items.NETHERITE_LEGGINGS) {
            return EquipmentSlotGroup.LEGS;
        }
        if (item == Items.IRON_BOOTS || item == Items.DIAMOND_BOOTS || item == Items.NETHERITE_BOOTS) {
            return EquipmentSlotGroup.FEET;
        }
        // Class armor (plain Item with Equippable component) — read slot from the Equippable component
        if (isClassArmor(item)) {
            ItemStack probe = new ItemStack((ItemLike) item);
            Equippable equippable = probe.get(DataComponents.EQUIPPABLE);
            if (equippable != null) {
                return switch (equippable.slot()) {
                    case HEAD -> EquipmentSlotGroup.HEAD;
                    case CHEST -> EquipmentSlotGroup.CHEST;
                    case LEGS -> EquipmentSlotGroup.LEGS;
                    case FEET -> EquipmentSlotGroup.FEET;
                    default -> EquipmentSlotGroup.MAINHAND;
                };
            }
        }
        return EquipmentSlotGroup.MAINHAND;
    }

    private static Optional<Holder<Attribute>> resolveAttribute(String attributeId) {
        try {
            Identifier id = Identifier.parse((String)attributeId);
            Optional ref = BuiltInRegistries.ATTRIBUTE.get(id);
            if (ref.isPresent()) {
                return Optional.of((Holder)ref.get());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return Optional.empty();
    }

    private static String formatModifier(LootModifier mod, double value) {
        String sign;
        String attrName = mod.attributeId();
        int colonIdx = attrName.indexOf(58);
        if (colonIdx >= 0) {
            attrName = attrName.substring(colonIdx + 1);
        }
        String[] parts = attrName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        String string = sign = value >= 0.0 ? "+" : "";
        if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            return String.valueOf(sb) + ": " + sign + String.format("%.0f%%", value * 100.0);
        }
        return String.valueOf(sb) + ": " + sign + String.format("%.1f", value);
    }

    // ─── Class-aware loot helpers retired with the class-selection system. ───

    static {
        // ALL_BASE_ITEMS includes vanilla items known at class-load time.
        // Mod weapons are in lazy pools and fall back to MAINHAND slot group.
        ALL_BASE_ITEMS = List.of(
            Items.IRON_SWORD, Items.IRON_AXE, Items.IRON_SHOVEL,
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
            Items.DIAMOND_SWORD, Items.DIAMOND_AXE,
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
            Items.NETHERITE_SWORD, Items.NETHERITE_AXE,
            Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
            Items.BOW, Items.CROSSBOW, Items.TRIDENT, Items.SHIELD);
    }
}

