package com.ultra.megamod.feature.combat.village;

import com.google.common.collect.ImmutableSet;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.items.ClassArmorRegistry;
import com.ultra.megamod.feature.combat.items.ClassWeaponRegistry;
import com.ultra.megamod.feature.combat.items.JewelryRegistry;
import com.ultra.megamod.feature.combat.runes.RuneRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Registers 5 combat villager professions ported from Paladins, Rogues, Archers, Wizards, and Jewelry mods.
 * Each profession has a village structure (Sanctuary, Barracks, Archery Range, Wizard Tower, Jewelry Shop)
 * and 5 levels of trades selling MegaMod combat items.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class CombatVillagerRegistry {

    // ─── POI Types ───
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MegaMod.MODID);

    // Use custom workbench blocks as POI workstations (avoids conflicts with vanilla professions)
    public static final DeferredHolder<PoiType, PoiType> MONK_POI = POI_TYPES.register("monk",
            () -> new PoiType(ImmutableSet.copyOf(
                    com.ultra.megamod.feature.combat.runes.RuneWorkbenchRegistry.MONK_WORKBENCH.get()
                            .getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<PoiType, PoiType> ARMS_MERCHANT_POI = POI_TYPES.register("arms_merchant",
            () -> new PoiType(ImmutableSet.copyOf(
                    com.ultra.megamod.feature.combat.runes.RuneWorkbenchRegistry.ARMS_WORKBENCH.get()
                            .getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<PoiType, PoiType> ARCHERY_ARTISAN_POI = POI_TYPES.register("archery_artisan",
            () -> new PoiType(ImmutableSet.copyOf(
                    com.ultra.megamod.feature.combat.runes.RuneWorkbenchRegistry.ARCHERS_WORKBENCH.get()
                            .getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<PoiType, PoiType> WIZARD_MERCHANT_POI = POI_TYPES.register("wizard_merchant",
            () -> new PoiType(ImmutableSet.copyOf(
                    com.ultra.megamod.feature.combat.spell.SpellItemRegistry.SPELL_BINDING_TABLE_BLOCK.get()
                            .getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<PoiType, PoiType> JEWELER_POI = POI_TYPES.register("jeweler",
            () -> new PoiType(ImmutableSet.copyOf(
                    com.ultra.megamod.feature.combat.runes.RuneWorkbenchRegistry.JEWELERS_KIT.get()
                            .getStateDefinition().getPossibleStates()), 1, 1));

    // ─── Villager Professions ───
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, MegaMod.MODID);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> MONK = PROFESSIONS.register("monk",
            () -> new VillagerProfession(
                    net.minecraft.network.chat.Component.translatable("entity.minecraft.villager.megamod.monk"),
                    holder -> holder.is(MONK_POI.getKey()),
                    holder -> holder.is(MONK_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_CLERIC));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> ARMS_MERCHANT = PROFESSIONS.register("arms_merchant",
            () -> new VillagerProfession(
                    net.minecraft.network.chat.Component.translatable("entity.minecraft.villager.megamod.arms_merchant"),
                    holder -> holder.is(ARMS_MERCHANT_POI.getKey()),
                    holder -> holder.is(ARMS_MERCHANT_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_WEAPONSMITH));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> ARCHERY_ARTISAN = PROFESSIONS.register("archery_artisan",
            () -> new VillagerProfession(
                    net.minecraft.network.chat.Component.translatable("entity.minecraft.villager.megamod.archery_artisan"),
                    holder -> holder.is(ARCHERY_ARTISAN_POI.getKey()),
                    holder -> holder.is(ARCHERY_ARTISAN_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_FLETCHER));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> WIZARD_MERCHANT = PROFESSIONS.register("wizard_merchant",
            () -> new VillagerProfession(
                    net.minecraft.network.chat.Component.translatable("entity.minecraft.villager.megamod.wizard_merchant"),
                    holder -> holder.is(WIZARD_MERCHANT_POI.getKey()),
                    holder -> holder.is(WIZARD_MERCHANT_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_LIBRARIAN));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> JEWELER = PROFESSIONS.register("jeweler",
            () -> new VillagerProfession(
                    net.minecraft.network.chat.Component.translatable("entity.minecraft.villager.megamod.jeweler"),
                    holder -> holder.is(JEWELER_POI.getKey()),
                    holder -> holder.is(JEWELER_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(),
                    com.ultra.megamod.feature.combat.jewelry.JewelrySounds.JEWELRY_WORKBENCH.get()));

    public static void init(IEventBus modBus) {
        POI_TYPES.register(modBus);
        PROFESSIONS.register(modBus);
    }

    // ─── Trade Registration (Game Bus Event) ───

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        var type = event.getType();
        if (type == MONK.getKey()) {
            registerMonkTrades(event);
        } else if (type == ARMS_MERCHANT.getKey()) {
            registerArmsMerchantTrades(event);
        } else if (type == ARCHERY_ARTISAN.getKey()) {
            registerArcheryArtisanTrades(event);
        } else if (type == WIZARD_MERCHANT.getKey()) {
            registerWizardMerchantTrades(event);
        } else if (type == JEWELER.getKey()) {
            registerJewelerTrades(event);
        }
    }

    // ─── Trade Definitions ───

    private static void registerMonkTrades(VillagerTradesEvent event) {
        // Level 1: Runes and basic weapons
        event.getTrades().get(1).add(sellItem(RuneRegistry.HEALING_RUNE.get(), 8, 2, 128, 1));
        event.getTrades().get(1).add(sellItem(ClassWeaponRegistry.ACOLYTE_WAND.get(), 1, 4, 12, 5));
        event.getTrades().get(1).add(sellItem(ClassWeaponRegistry.WOODEN_GREAT_HAMMER.get(), 1, 8, 12, 8));

        // Level 2: Buy materials
        event.getTrades().get(2).add(buyItem(Items.WHITE_WOOL, 5, 12, 12, 8));
        event.getTrades().get(2).add(buyItem(Items.IRON_INGOT, 6, 12, 12, 8));
        event.getTrades().get(2).add(buyItem(Items.GOLD_INGOT, 6, 12, 12, 8));

        // Level 3: Paladin & Priest armor (head, boots)
        event.getTrades().get(3).add(sellItem(ClassArmorRegistry.PALADIN_ARMOR_HEAD.get(), 1, 15, 12, 13));
        event.getTrades().get(3).add(sellItem(ClassArmorRegistry.PALADIN_ARMOR_FEET.get(), 1, 15, 12, 13));
        event.getTrades().get(3).add(sellItem(ClassArmorRegistry.PRIEST_ROBE_HEAD.get(), 1, 15, 12, 13));
        event.getTrades().get(3).add(sellItem(ClassArmorRegistry.PRIEST_ROBE_FEET.get(), 1, 15, 12, 13));

        // Level 4: Paladin & Priest armor (chest, legs)
        event.getTrades().get(4).add(sellItem(ClassArmorRegistry.PALADIN_ARMOR_CHEST.get(), 1, 20, 12, 15));
        event.getTrades().get(4).add(sellItem(ClassArmorRegistry.PALADIN_ARMOR_LEGS.get(), 1, 20, 12, 15));
        event.getTrades().get(4).add(sellItem(ClassArmorRegistry.PRIEST_ROBE_CHEST.get(), 1, 20, 12, 15));
        event.getTrades().get(4).add(sellItem(ClassArmorRegistry.PRIEST_ROBE_LEGS.get(), 1, 20, 12, 15));

        // Level 5: Diamond-tier holy weapons
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.DIAMOND_HOLY_STAFF.get(), 1, 40, 3, 30));
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.DIAMOND_CLAYMORE.get(), 1, 40, 3, 30));
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.DIAMOND_GREAT_HAMMER.get(), 1, 40, 3, 30));
    }

    private static void registerArmsMerchantTrades(VillagerTradesEvent event) {
        // Level 1
        event.getTrades().get(1).add(buyItem(Items.LEATHER, 8, 12, 12, 4));
        event.getTrades().get(1).add(sellItem(ClassWeaponRegistry.FLINT_DAGGER.get(), 1, 6, 12, 3));
        event.getTrades().get(1).add(sellItem(ClassWeaponRegistry.STONE_DOUBLE_AXE.get(), 1, 8, 12, 4));

        // Level 2
        event.getTrades().get(2).add(buyItem(Items.IRON_INGOT, 12, 12, 12, 8));
        event.getTrades().get(2).add(sellItem(ClassWeaponRegistry.IRON_SICKLE.get(), 1, 12, 12, 10));
        event.getTrades().get(2).add(sellItem(ClassWeaponRegistry.IRON_GLAIVE.get(), 1, 18, 12, 10));
        event.getTrades().get(2).add(sellItem(ClassArmorRegistry.ROGUE_ARMOR_HEAD.get(), 1, 15, 12, 13));
        event.getTrades().get(2).add(sellItem(ClassArmorRegistry.WARRIOR_ARMOR_HEAD.get(), 1, 15, 12, 13));

        // Level 3
        event.getTrades().get(3).add(sellItem(ClassWeaponRegistry.IRON_DAGGER.get(), 1, 14, 12, 15));
        event.getTrades().get(3).add(sellItem(ClassWeaponRegistry.IRON_DOUBLE_AXE.get(), 1, 18, 12, 15));
        event.getTrades().get(3).add(sellItem(ClassArmorRegistry.ROGUE_ARMOR_LEGS.get(), 1, 15, 12, 15));
        event.getTrades().get(3).add(sellItem(ClassArmorRegistry.WARRIOR_ARMOR_LEGS.get(), 1, 15, 12, 15));

        // Level 4
        event.getTrades().get(4).add(sellItem(ClassArmorRegistry.ROGUE_ARMOR_CHEST.get(), 1, 15, 12, 15));
        event.getTrades().get(4).add(sellItem(ClassArmorRegistry.WARRIOR_ARMOR_CHEST.get(), 1, 15, 12, 15));

        // Level 5: Diamond tier weapons
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.DIAMOND_DAGGER.get(), 1, 30, 3, 30));
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.DIAMOND_SICKLE.get(), 1, 30, 3, 30));
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.DIAMOND_DOUBLE_AXE.get(), 1, 40, 3, 30));
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.DIAMOND_GLAIVE.get(), 1, 40, 3, 30));
    }

    private static void registerArcheryArtisanTrades(VillagerTradesEvent event) {
        // Level 1
        event.getTrades().get(1).add(sellItem(Items.ARROW, 8, 2, 128, 3));
        event.getTrades().get(1).add(buyItem(Items.LEATHER, 8, 12, 12, 5));

        // Level 2
        event.getTrades().get(2).add(sellItem(ClassWeaponRegistry.COMPOSITE_LONGBOW.get(), 1, 6, 16, 8));
        event.getTrades().get(2).add(sellItem(ClassArmorRegistry.ARCHER_ARMOR_HEAD.get(), 1, 15, 18, 8));
        event.getTrades().get(2).add(buyItem(Items.STRING, 6, 12, 12, 3));

        // Level 3
        event.getTrades().get(3).add(sellItem(ClassArmorRegistry.ARCHER_ARMOR_FEET.get(), 1, 15, 18, 10));
        event.getTrades().get(3).add(sellItem(ClassArmorRegistry.ARCHER_ARMOR_LEGS.get(), 1, 15, 18, 10));
        event.getTrades().get(3).add(buyItem(Items.REDSTONE, 12, 12, 12, 8));

        // Level 4
        event.getTrades().get(4).add(sellItem(ClassArmorRegistry.ARCHER_ARMOR_CHEST.get(), 1, 15, 18, 13));
        event.getTrades().get(4).add(sellItem(Items.TURTLE_SCUTE, 1, 20, 10, 10));

        // Level 5: Diamond-tier ranged weapons
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.ROYAL_LONGBOW.get(), 1, 40, 3, 30));
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.MECHANIC_SHORTBOW.get(), 1, 40, 3, 30));
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.RAPID_CROSSBOW.get(), 1, 40, 3, 30));
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.HEAVY_CROSSBOW.get(), 1, 40, 3, 30));
    }

    private static void registerWizardMerchantTrades(VillagerTradesEvent event) {
        // Level 1: Runes
        event.getTrades().get(1).add(sellItem(RuneRegistry.ARCANE_RUNE.get(), 8, 2, 128, 3));
        event.getTrades().get(1).add(sellItem(RuneRegistry.FIRE_RUNE.get(), 8, 2, 128, 3));
        event.getTrades().get(1).add(sellItem(RuneRegistry.FROST_RUNE.get(), 8, 2, 128, 3));

        // Level 2: Wands and staves
        event.getTrades().get(2).add(sellItem(ClassWeaponRegistry.STAFF_WIZARD.get(), 1, 4, 12, 18));
        event.getTrades().get(2).add(sellItem(ClassWeaponRegistry.WAND_NOVICE.get(), 1, 4, 12, 18));
        event.getTrades().get(2).add(sellItem(ClassWeaponRegistry.WAND_ARCANE.get(), 1, 18, 12, 18));
        event.getTrades().get(2).add(sellItem(ClassWeaponRegistry.WAND_FIRE.get(), 1, 18, 12, 18));
        event.getTrades().get(2).add(sellItem(ClassWeaponRegistry.WAND_FROST.get(), 1, 18, 12, 18));
        event.getTrades().get(2).add(buyItem(Items.WHITE_WOOL, 10, 12, 12, 6));
        event.getTrades().get(2).add(buyItem(Items.LAPIS_LAZULI, 6, 3, 12, 12));

        // Level 3: Wizard robes (head, boots)
        event.getTrades().get(3).add(sellItem(ClassArmorRegistry.WIZARD_ROBE_HEAD.get(), 1, 15, 12, 16));
        event.getTrades().get(3).add(sellItem(ClassArmorRegistry.WIZARD_ROBE_FEET.get(), 1, 15, 12, 16));

        // Level 4: Wizard robes (chest, legs)
        event.getTrades().get(4).add(sellItem(ClassArmorRegistry.WIZARD_ROBE_CHEST.get(), 1, 20, 12, 16));
        event.getTrades().get(4).add(sellItem(ClassArmorRegistry.WIZARD_ROBE_LEGS.get(), 1, 20, 12, 16));

        // Level 5: Diamond-tier staves
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.STAFF_ARCANE.get(), 1, 40, 3, 30));
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.STAFF_FIRE.get(), 1, 40, 3, 30));
        event.getTrades().get(5).add(sellItem(ClassWeaponRegistry.STAFF_FROST.get(), 1, 40, 3, 30));
    }

    private static void registerJewelerTrades(VillagerTradesEvent event) {
        // Level 1: Buy materials
        event.getTrades().get(1).add(buyItem(Items.COPPER_INGOT, 8, 8, 8, 2));
        event.getTrades().get(1).add(buyItem(Items.STRING, 7, 6, 8, 2));
        event.getTrades().get(1).add(sellItem(JewelryRegistry.COPPER_RING.get(), 1, 4, 12, 4));

        // Level 2: Basic jewelry
        event.getTrades().get(2).add(buyItem(Items.GOLD_INGOT, 7, 8, 8, 8));
        event.getTrades().get(2).add(sellItem(JewelryRegistry.IRON_RING.get(), 1, 4, 6, 5));
        event.getTrades().get(2).add(sellItem(JewelryRegistry.GOLD_RING.get(), 1, 18, 6, 5));

        // Level 3: Necklaces
        event.getTrades().get(3).add(buyItem(Items.DIAMOND, 1, 12, 12, 10));
        event.getTrades().get(3).add(sellItem(JewelryRegistry.EMERALD_NECKLACE.get(), 1, 20, 12, 10));
        event.getTrades().get(3).add(sellItem(JewelryRegistry.DIAMOND_NECKLACE.get(), 1, 25, 12, 10));

        // Level 4: Gem rings
        event.getTrades().get(4).add(sellItem(JewelryRegistry.RUBY_RING.get(), 1, 35, 5, 15));
        event.getTrades().get(4).add(sellItem(JewelryRegistry.TOPAZ_RING.get(), 1, 35, 5, 15));
        event.getTrades().get(4).add(sellItem(JewelryRegistry.CITRINE_RING.get(), 1, 35, 5, 15));
        event.getTrades().get(4).add(sellItem(JewelryRegistry.JADE_RING.get(), 1, 35, 5, 15));
        event.getTrades().get(4).add(sellItem(JewelryRegistry.SAPPHIRE_RING.get(), 1, 35, 5, 15));
        event.getTrades().get(4).add(sellItem(JewelryRegistry.TANZANITE_RING.get(), 1, 35, 5, 13));

        // Level 4: Gem rings (continued) — diamond ring available mid-tier
        event.getTrades().get(4).add(sellItem(JewelryRegistry.DIAMOND_RING.get(), 1, 30, 5, 15));

        // Level 5: Gem necklaces
        event.getTrades().get(5).add(sellItem(JewelryRegistry.RUBY_NECKLACE.get(), 1, 45, 3, 15));
        event.getTrades().get(5).add(sellItem(JewelryRegistry.TOPAZ_NECKLACE.get(), 1, 45, 3, 15));
        event.getTrades().get(5).add(sellItem(JewelryRegistry.CITRINE_NECKLACE.get(), 1, 45, 3, 15));
        event.getTrades().get(5).add(sellItem(JewelryRegistry.JADE_NECKLACE.get(), 1, 45, 3, 15));
        event.getTrades().get(5).add(sellItem(JewelryRegistry.SAPPHIRE_NECKLACE.get(), 1, 45, 3, 15));
        event.getTrades().get(5).add(sellItem(JewelryRegistry.TANZANITE_NECKLACE.get(), 1, 45, 3, 15));

        // Level 5: Rare netherite-tier jewelry (high cost, very limited stock)
        // These are the master jeweler's endgame offerings — players can also find these in high-tier chests.
        event.getTrades().get(5).add(sellItem(JewelryRegistry.NETHERITE_RUBY_RING.get(), 1, 62, 1, 30));
        event.getTrades().get(5).add(sellItem(JewelryRegistry.NETHERITE_SAPPHIRE_RING.get(), 1, 62, 1, 30));
        event.getTrades().get(5).add(sellItem(JewelryRegistry.NETHERITE_JADE_RING.get(), 1, 62, 1, 30));
        event.getTrades().get(5).add(sellItem(JewelryRegistry.NETHERITE_RUBY_NECKLACE.get(), 1, 64, 1, 30));
        event.getTrades().get(5).add(sellItem(JewelryRegistry.NETHERITE_SAPPHIRE_NECKLACE.get(), 1, 64, 1, 30));
        event.getTrades().get(5).add(sellItem(JewelryRegistry.NETHERITE_TOPAZ_NECKLACE.get(), 1, 64, 1, 30));
    }

    // ─── Trade Helper Methods ───

    /**
     * Creates a trade that SELLS an item to the player for emeralds.
     */
    private static VillagerTrades.ItemListing sellItem(net.minecraft.world.item.Item item, int count,
                                                        int emeraldCost, int maxUses, int xp) {
        return (level, trader, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, emeraldCost),
                new ItemStack(item, count),
                maxUses, xp, 0.05f);
    }

    /**
     * Creates a trade that BUYS items from the player for emeralds.
     */
    private static VillagerTrades.ItemListing buyItem(net.minecraft.world.item.Item item, int count,
                                                       int emeraldReward, int maxUses, int xp) {
        return (level, trader, random) -> new MerchantOffer(
                new ItemCost(item, count),
                new ItemStack(Items.EMERALD, emeraldReward),
                maxUses, xp, 0.05f);
    }
}
