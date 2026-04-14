/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.material.MapColor
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredBlock
 *  net.neoforged.neoforge.registries.DeferredItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Blocks
 *  net.neoforged.neoforge.registries.DeferredRegister$Items
 */
package com.ultra.megamod.feature.relics;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.ability.back.ArrowQuiverAbility;
import com.ultra.megamod.feature.relics.ability.back.ElytraBoosterAbility;
import com.ultra.megamod.feature.relics.ability.back.MidnightRobeAbility;
import com.ultra.megamod.feature.relics.ability.belt.DrownedBeltAbility;
import com.ultra.megamod.feature.relics.ability.belt.HunterBeltAbility;
import com.ultra.megamod.feature.relics.ability.belt.LeatherBeltAbility;
import com.ultra.megamod.feature.relics.ability.feet.AmphibianBootAbility;
import com.ultra.megamod.feature.relics.ability.feet.AquaWalkerAbility;
import com.ultra.megamod.feature.relics.ability.feet.IceBreakerAbility;
import com.ultra.megamod.feature.relics.ability.feet.IceSkatesAbility;
import com.ultra.megamod.feature.relics.ability.feet.MagmaWalkerAbility;
import com.ultra.megamod.feature.relics.ability.feet.RollerSkatesAbility;
import com.ultra.megamod.feature.relics.ability.hands.EnderHandAbility;
import com.ultra.megamod.feature.relics.ability.hands.RageGloveAbility;
import com.ultra.megamod.feature.relics.ability.hands.WoolMittenAbility;
import com.ultra.megamod.feature.relics.ability.necklace.HolyLocketAbility;
import com.ultra.megamod.feature.relics.ability.necklace.JellyfishNecklaceAbility;
import com.ultra.megamod.feature.relics.ability.necklace.ReflectionNecklaceAbility;
import com.ultra.megamod.feature.relics.ability.ring.BastionRingAbility;
import com.ultra.megamod.feature.relics.ability.ring.ChorusInhibitorAbility;
import com.ultra.megamod.feature.relics.ability.usable.BlazingFlaskAbility;
import com.ultra.megamod.feature.relics.ability.usable.HorseFluteAbility;
import com.ultra.megamod.feature.relics.ability.usable.InfinityHamAbility;
import com.ultra.megamod.feature.relics.ability.usable.MagicMirrorAbility;
import com.ultra.megamod.feature.relics.ability.usable.ShadowGlaiveAbility;
import com.ultra.megamod.feature.relics.ability.usable.SpaceDissectorAbility;
import com.ultra.megamod.feature.relics.ability.usable.SporeSackAbility;
import com.ultra.megamod.feature.relics.ability.head.LunarCrownAbility;
import com.ultra.megamod.feature.relics.ability.head.SolarCrownAbility;
import com.ultra.megamod.feature.relics.ability.face.WardensVisorAbility;
import com.ultra.megamod.feature.relics.ability.face.VerdantMaskAbility;
import com.ultra.megamod.feature.relics.ability.face.FrostweaveVeilAbility;
import com.ultra.megamod.feature.relics.ability.head.StormcallerCircletAbility;
import com.ultra.megamod.feature.relics.ability.head.AshenDiademAbility;
import com.ultra.megamod.feature.relics.ability.head.WraithCrownAbility;
import com.ultra.megamod.feature.relics.ability.hands_right.ArcaneGauntletAbility;
import com.ultra.megamod.feature.relics.ability.hands_right.IronFistAbility;
import com.ultra.megamod.feature.relics.ability.hands_right.PlagueGraspAbility;
import com.ultra.megamod.feature.relics.ability.hands_right.SunforgedBracerAbility;
import com.ultra.megamod.feature.relics.ability.ring_right.StormbandAbility;
import com.ultra.megamod.feature.relics.ability.ring_right.GravestoneRingAbility;
import com.ultra.megamod.feature.relics.ability.ring_right.VerdantSignetAbility;
import com.ultra.megamod.feature.relics.ability.back.PhoenixMantleAbility;
import com.ultra.megamod.feature.relics.ability.back.WindrunnerCloakAbility;
import com.ultra.megamod.feature.relics.ability.back.AbyssalCapeAbility;
import com.ultra.megamod.feature.relics.ability.belt.AlchemistsSashAbility;
import com.ultra.megamod.feature.relics.ability.belt.GuardiansGirdleAbility;
import com.ultra.megamod.feature.relics.ability.belt.LodestoneMagnetAbility;
import com.ultra.megamod.feature.relics.ability.belt.SerpentBeltAbility;
import com.ultra.megamod.feature.relics.ability.necklace.FrostfirePendantAbility;
import com.ultra.megamod.feature.relics.ability.necklace.TidekeeperAmuletAbility;
import com.ultra.megamod.feature.relics.ability.necklace.BloodstoneChokerAbility;
import com.ultra.megamod.feature.relics.ability.hands.ThornweaveGloveAbility;
import com.ultra.megamod.feature.relics.ability.hands.ChronoGloveAbility;
import com.ultra.megamod.feature.relics.ability.feet.StormstriderBootsAbility;
import com.ultra.megamod.feature.relics.ability.feet.SandwalkerTreadsAbility;
import com.ultra.megamod.feature.relics.ability.ring.EmberstoneBandAbility;
import com.ultra.megamod.feature.relics.ability.usable.VoidLanternAbility;
import com.ultra.megamod.feature.relics.ability.usable.ThunderhornAbility;
import com.ultra.megamod.feature.relics.ability.usable.MendingChaliceAbility;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.relics.research.ResearchingTableBlock;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponItem;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelicRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks((String)"megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String)"megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"megamod");
    public static final DeferredItem<RelicItem> ARROW_QUIVER = ITEMS.registerItem("arrow_quiver", props -> new RelicItem("Arrow Quiver", AccessorySlotType.BACK, ArrowQuiverAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:arrow_quiver"));
    public static final DeferredItem<RelicItem> ELYTRA_BOOSTER = ITEMS.registerItem("elytra_booster", props -> new RelicItem("Elytra Booster", AccessorySlotType.BACK, ElytraBoosterAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:elytra_booster"));
    public static final DeferredItem<RelicItem> MIDNIGHT_ROBE = ITEMS.registerItem("midnight_robe", props -> new RelicItem("Midnight Robe", AccessorySlotType.BACK, MidnightRobeAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:midnight_robe"));
    public static final DeferredItem<RelicItem> LEATHER_BELT = ITEMS.registerItem("leather_belt", props -> new RelicItem("Leather Belt", AccessorySlotType.BELT, LeatherBeltAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:leather_belt"));
    public static final DeferredItem<RelicItem> DROWNED_BELT = ITEMS.registerItem("drowned_belt", props -> new RelicItem("Drowned Belt", AccessorySlotType.BELT, DrownedBeltAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:drowned_belt"));
    public static final DeferredItem<RelicItem> HUNTER_BELT = ITEMS.registerItem("hunter_belt", props -> new RelicItem("Hunter Belt", AccessorySlotType.BELT, HunterBeltAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:hunter_belt"));
    public static final DeferredItem<RelicItem> ENDER_HAND = ITEMS.registerItem("ender_hand", props -> new RelicItem("Ender Hand", AccessorySlotType.HANDS_LEFT, EnderHandAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:ender_hand"));
    public static final DeferredItem<RelicItem> RAGE_GLOVE = ITEMS.registerItem("rage_glove", props -> new RelicItem("Rage Glove", AccessorySlotType.HANDS_LEFT, RageGloveAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:rage_glove"));
    public static final DeferredItem<RelicItem> WOOL_MITTEN = ITEMS.registerItem("wool_mitten", props -> new RelicItem("Wool Mitten", AccessorySlotType.HANDS_LEFT, WoolMittenAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:wool_mitten"));
    public static final DeferredItem<RelicItem> MAGMA_WALKER = ITEMS.registerItem("magma_walker", props -> new RelicItem("Magma Walker", AccessorySlotType.FEET, MagmaWalkerAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:magma_walker"));
    public static final DeferredItem<RelicItem> AQUA_WALKER = ITEMS.registerItem("aqua_walker", props -> new RelicItem("Aqua Walker", AccessorySlotType.FEET, AquaWalkerAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:aqua_walker"));
    public static final DeferredItem<RelicItem> ICE_SKATES = ITEMS.registerItem("ice_skates", props -> new RelicItem("Ice Skates", AccessorySlotType.FEET, IceSkatesAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:ice_skates"));
    public static final DeferredItem<RelicItem> ICE_BREAKER = ITEMS.registerItem("ice_breaker", props -> new RelicItem("Ice Breaker", AccessorySlotType.FEET, IceBreakerAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:ice_breaker"));
    public static final DeferredItem<RelicItem> ROLLER_SKATES = ITEMS.registerItem("roller_skates", props -> new RelicItem("Roller Skates", AccessorySlotType.FEET, RollerSkatesAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:roller_skates"));
    public static final DeferredItem<RelicItem> AMPHIBIAN_BOOT = ITEMS.registerItem("amphibian_boot", props -> new RelicItem("Amphibian Boot", AccessorySlotType.FEET, AmphibianBootAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:amphibian_boot"));
    public static final DeferredItem<RelicItem> REFLECTION_NECKLACE = ITEMS.registerItem("reflection_necklace", props -> new RelicItem("Reflection Necklace", AccessorySlotType.NECKLACE, ReflectionNecklaceAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:reflection_necklace"));
    public static final DeferredItem<RelicItem> JELLYFISH_NECKLACE = ITEMS.registerItem("jellyfish_necklace", props -> new RelicItem("Jellyfish Necklace", AccessorySlotType.NECKLACE, JellyfishNecklaceAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:jellyfish_necklace"));
    public static final DeferredItem<RelicItem> HOLY_LOCKET = ITEMS.registerItem("holy_locket", props -> new RelicItem("Holy Locket", AccessorySlotType.NECKLACE, HolyLocketAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:holy_locket"));
    public static final DeferredItem<RelicItem> BASTION_RING = ITEMS.registerItem("bastion_ring", props -> new RelicItem("Bastion Ring", AccessorySlotType.RING_LEFT, BastionRingAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:bastion_ring"));
    public static final DeferredItem<RelicItem> CHORUS_INHIBITOR = ITEMS.registerItem("chorus_inhibitor", props -> new RelicItem("Chorus Inhibitor", AccessorySlotType.RING_LEFT, ChorusInhibitorAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:chorus_inhibitor"));
    public static final DeferredItem<RelicItem> SHADOW_GLAIVE = ITEMS.registerItem("shadow_glaive", props -> new RelicItem("Shadow Glaive", AccessorySlotType.NONE, ShadowGlaiveAbility.ABILITIES, 7.0f, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:shadow_glaive"));
    public static final DeferredItem<RelicItem> INFINITY_HAM = ITEMS.registerItem("infinity_ham", props -> new RelicItem("Infinity Ham", AccessorySlotType.NONE, InfinityHamAbility.ABILITIES, 2.0f, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:infinity_ham"));
    public static final DeferredItem<RelicItem> SPACE_DISSECTOR = ITEMS.registerItem("space_dissector", props -> new RelicItem("Space Dissector", AccessorySlotType.NONE, SpaceDissectorAbility.ABILITIES, 2.0f, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:space_dissector"));
    public static final DeferredItem<RelicItem> MAGIC_MIRROR = ITEMS.registerItem("magic_mirror", props -> new RelicItem("Magic Mirror", AccessorySlotType.NONE, MagicMirrorAbility.ABILITIES, 2.0f, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:magic_mirror"));
    public static final DeferredItem<RelicItem> HORSE_FLUTE = ITEMS.registerItem("horse_flute", props -> new RelicItem("Horse Flute", AccessorySlotType.NONE, HorseFluteAbility.ABILITIES, 2.0f, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:horse_flute"));
    public static final DeferredItem<RelicItem> SPORE_SACK = ITEMS.registerItem("spore_sack", props -> new RelicItem("Spore Sack", AccessorySlotType.NONE, SporeSackAbility.ABILITIES, 3.0f, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:spore_sack"));
    public static final DeferredItem<RelicItem> BLAZING_FLASK = ITEMS.registerItem("blazing_flask", props -> new RelicItem("Blazing Flask", AccessorySlotType.NONE, BlazingFlaskAbility.ABILITIES, 4.0f, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:blazing_flask"));
    public static final DeferredItem<RelicItem> LUNAR_CROWN = ITEMS.registerItem("lunar_crown", props -> new RelicItem("Lunar Crown", AccessorySlotType.HEAD, LunarCrownAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:lunar_crown"));
    public static final DeferredItem<RelicItem> SOLAR_CROWN = ITEMS.registerItem("solar_crown", props -> new RelicItem("Solar Crown", AccessorySlotType.HEAD, SolarCrownAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:solar_crown"));
    // ===== NEW RELICS =====
    // Face Slot
    public static final DeferredItem<RelicItem> WARDENS_VISOR = ITEMS.registerItem("wardens_visor", props -> new RelicItem("Warden's Visor", AccessorySlotType.FACE, WardensVisorAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:wardens_visor"));
    public static final DeferredItem<RelicItem> VERDANT_MASK = ITEMS.registerItem("verdant_mask", props -> new RelicItem("Verdant Mask", AccessorySlotType.FACE, VerdantMaskAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:verdant_mask"));
    public static final DeferredItem<RelicItem> FROSTWEAVE_VEIL = ITEMS.registerItem("frostweave_veil", props -> new RelicItem("Frostweave Veil", AccessorySlotType.FACE, FrostweaveVeilAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:frostweave_veil"));
    // Head Slot
    public static final DeferredItem<RelicItem> STORMCALLER_CIRCLET = ITEMS.registerItem("stormcaller_circlet", props -> new RelicItem("Stormcaller Circlet", AccessorySlotType.HEAD, StormcallerCircletAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:stormcaller_circlet"));
    public static final DeferredItem<RelicItem> ASHEN_DIADEM = ITEMS.registerItem("ashen_diadem", props -> new RelicItem("Ashen Diadem", AccessorySlotType.HEAD, AshenDiademAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:ashen_diadem"));
    public static final DeferredItem<RelicItem> WRAITH_CROWN = ITEMS.registerItem("wraith_crown", props -> new RelicItem("Wraith Crown", AccessorySlotType.HEAD, WraithCrownAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:wraith_crown"));
    // Hands Right Slot
    public static final DeferredItem<RelicItem> ARCANE_GAUNTLET = ITEMS.registerItem("arcane_gauntlet", props -> new RelicItem("Arcane Gauntlet", AccessorySlotType.HANDS_RIGHT, ArcaneGauntletAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:arcane_gauntlet"));
    public static final DeferredItem<RelicItem> IRON_FIST = ITEMS.registerItem("iron_fist", props -> new RelicItem("Iron Fist", AccessorySlotType.HANDS_RIGHT, IronFistAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:iron_fist"));
    public static final DeferredItem<RelicItem> PLAGUE_GRASP = ITEMS.registerItem("plague_grasp", props -> new RelicItem("Plague Grasp", AccessorySlotType.HANDS_RIGHT, PlagueGraspAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:plague_grasp"));
    public static final DeferredItem<RelicItem> SUNFORGED_BRACER = ITEMS.registerItem("sunforged_bracer", props -> new RelicItem("Sunforged Bracer", AccessorySlotType.HANDS_RIGHT, SunforgedBracerAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:sunforged_bracer"));
    // Ring Right Slot
    public static final DeferredItem<RelicItem> STORMBAND = ITEMS.registerItem("stormband", props -> new RelicItem("Stormband", AccessorySlotType.RING_RIGHT, StormbandAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:stormband"));
    public static final DeferredItem<RelicItem> GRAVESTONE_RING = ITEMS.registerItem("gravestone_ring", props -> new RelicItem("Gravestone Ring", AccessorySlotType.RING_RIGHT, GravestoneRingAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:gravestone_ring"));
    public static final DeferredItem<RelicItem> VERDANT_SIGNET = ITEMS.registerItem("verdant_signet", props -> new RelicItem("Verdant Signet", AccessorySlotType.RING_RIGHT, VerdantSignetAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:verdant_signet"));
    // Back Slot
    public static final DeferredItem<RelicItem> PHOENIX_MANTLE = ITEMS.registerItem("phoenix_mantle", props -> new RelicItem("Phoenix Mantle", AccessorySlotType.BACK, PhoenixMantleAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:phoenix_mantle"));
    public static final DeferredItem<RelicItem> WINDRUNNER_CLOAK = ITEMS.registerItem("windrunner_cloak", props -> new RelicItem("Windrunner Cloak", AccessorySlotType.BACK, WindrunnerCloakAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:windrunner_cloak"));
    public static final DeferredItem<RelicItem> ABYSSAL_CAPE = ITEMS.registerItem("abyssal_cape", props -> new RelicItem("Abyssal Cape", AccessorySlotType.BACK, AbyssalCapeAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:abyssal_cape"));
    // Belt Slot
    public static final DeferredItem<RelicItem> ALCHEMISTS_SASH = ITEMS.registerItem("alchemists_sash", props -> new RelicItem("Alchemist's Sash", AccessorySlotType.BELT, AlchemistsSashAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:alchemists_sash"));
    public static final DeferredItem<RelicItem> GUARDIANS_GIRDLE = ITEMS.registerItem("guardians_girdle", props -> new RelicItem("Guardian's Girdle", AccessorySlotType.BELT, GuardiansGirdleAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:guardians_girdle"));
    public static final DeferredItem<RelicItem> SERPENT_BELT = ITEMS.registerItem("serpent_belt", props -> new RelicItem("Serpent Belt", AccessorySlotType.BELT, SerpentBeltAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:serpent_belt"));
    public static final DeferredItem<RelicItem> LODESTONE_MAGNET = ITEMS.registerItem("lodestone_magnet", props -> new RelicItem("Lodestone Magnet", AccessorySlotType.BELT, LodestoneMagnetAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:lodestone_magnet"));
    // Necklace Slot
    public static final DeferredItem<RelicItem> FROSTFIRE_PENDANT = ITEMS.registerItem("frostfire_pendant", props -> new RelicItem("Frostfire Pendant", AccessorySlotType.NECKLACE, FrostfirePendantAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:frostfire_pendant"));
    public static final DeferredItem<RelicItem> TIDEKEEPER_AMULET = ITEMS.registerItem("tidekeeper_amulet", props -> new RelicItem("Tidekeeper Amulet", AccessorySlotType.NECKLACE, TidekeeperAmuletAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:tidekeeper_amulet"));
    public static final DeferredItem<RelicItem> BLOODSTONE_CHOKER = ITEMS.registerItem("bloodstone_choker", props -> new RelicItem("Bloodstone Choker", AccessorySlotType.NECKLACE, BloodstoneChokerAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:bloodstone_choker"));
    // Hands Left Slot
    public static final DeferredItem<RelicItem> THORNWEAVE_GLOVE = ITEMS.registerItem("thornweave_glove", props -> new RelicItem("Thornweave Glove", AccessorySlotType.HANDS_LEFT, ThornweaveGloveAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:thornweave_glove"));
    public static final DeferredItem<RelicItem> CHRONO_GLOVE = ITEMS.registerItem("chrono_glove", props -> new RelicItem("Chrono Glove", AccessorySlotType.HANDS_LEFT, ChronoGloveAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:chrono_glove"));
    // Feet Slot
    public static final DeferredItem<RelicItem> STORMSTRIDER_BOOTS = ITEMS.registerItem("stormstrider_boots", props -> new RelicItem("Stormstrider Boots", AccessorySlotType.FEET, StormstriderBootsAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:stormstrider_boots"));
    public static final DeferredItem<RelicItem> SANDWALKER_TREADS = ITEMS.registerItem("sandwalker_treads", props -> new RelicItem("Sandwalker Treads", AccessorySlotType.FEET, SandwalkerTreadsAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:sandwalker_treads"));
    // Ring Left Slot
    public static final DeferredItem<RelicItem> EMBERSTONE_BAND = ITEMS.registerItem("emberstone_band", props -> new RelicItem("Emberstone Band", AccessorySlotType.RING_LEFT, EmberstoneBandAbility.ABILITIES, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:emberstone_band"));
    // Usable (NONE slot)
    public static final DeferredItem<RelicItem> VOID_LANTERN = ITEMS.registerItem("void_lantern", props -> new RelicItem("Void Lantern", AccessorySlotType.NONE, VoidLanternAbility.ABILITIES, 2.0f, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:void_lantern"));
    public static final DeferredItem<RelicItem> THUNDERHORN = ITEMS.registerItem("thunderhorn", props -> new RelicItem("Thunderhorn", AccessorySlotType.NONE, ThunderhornAbility.ABILITIES, 2.0f, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:thunderhorn"));
    public static final DeferredItem<RelicItem> MENDING_CHALICE = ITEMS.registerItem("mending_chalice", props -> new RelicItem("Mending Chalice", AccessorySlotType.NONE, MendingChaliceAbility.ABILITIES, 2.0f, (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:mending_chalice"));
    public static final DeferredItem<RpgWeaponItem> VAMPIRIC_TOME = ITEMS.registerItem("vampiric_tome", props -> new RpgWeaponItem("Vampiric Tome", 5.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:vampiric_tome"));
    public static final DeferredItem<RpgWeaponItem> STATIC_SEEKER = ITEMS.registerItem("static_seeker", props -> new RpgWeaponItem("Static Seeker", 7.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:static_seeker"));
    public static final DeferredItem<RpgWeaponItem> BATTLEDANCER = ITEMS.registerItem("battledancer", props -> new RpgWeaponItem("Battledancer", 6.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:battledancer"));
    public static final DeferredItem<RpgWeaponItem> EBONCHILL = ITEMS.registerItem("ebonchill", props -> new RpgWeaponItem("Ebonchill", 8.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:ebonchill"));
    public static final DeferredItem<RpgWeaponItem> LIGHTBINDER = ITEMS.registerItem("lightbinder", props -> new RpgWeaponItem("Lightbinder", 7.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:lightbinder"));
    public static final DeferredItem<RpgWeaponItem> CRESCENT_BLADE = ITEMS.registerItem("crescent_blade", props -> new RpgWeaponItem("Crescent Blade", 9.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:crescent_blade"));
    public static final DeferredItem<RpgWeaponItem> GHOST_FANG = ITEMS.registerItem("ghost_fang", props -> new RpgWeaponItem("Ghost Fang", 8.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:ghost_fang"));
    public static final DeferredItem<RpgWeaponItem> TERRA_WARHAMMER = ITEMS.registerItem("terra_warhammer", props -> new RpgWeaponItem("Terra Warhammer", 10.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:terra_warhammer"));
    public static final DeferredItem<RpgWeaponItem> SOKA_SINGING_BLADE = ITEMS.registerItem("soka_singing_blade", props -> new RpgWeaponItem("Soka Singing Blade", 12.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC), "megamod:soka_singing_blade"));
    // Arsenal Claymores
    public static final DeferredItem<RpgWeaponItem> UNIQUE_CLAYMORE_1 = ITEMS.registerItem("unique_claymore_1", props -> new RpgWeaponItem("Cataclysm's Edge", 11.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_CLAYMORE_2 = ITEMS.registerItem("unique_claymore_2", props -> new RpgWeaponItem("Champion's Greatsword", 11.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_CLAYMORE_SW = ITEMS.registerItem("unique_claymore_sw", props -> new RpgWeaponItem("Apolyon, the Soul-Render", 12.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Daggers
    public static final DeferredItem<RpgWeaponItem> UNIQUE_DAGGER_1 = ITEMS.registerItem("unique_dagger_1", props -> new RpgWeaponItem("Frost Fang", 5.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_DAGGER_2 = ITEMS.registerItem("unique_dagger_2", props -> new RpgWeaponItem("Demonic Shiv", 5.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_DAGGER_SW = ITEMS.registerItem("unique_dagger_sw", props -> new RpgWeaponItem("Crux of the Apocalypse", 6.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Double Axes
    public static final DeferredItem<RpgWeaponItem> UNIQUE_DOUBLE_AXE_1 = ITEMS.registerItem("unique_double_axe_1", props -> new RpgWeaponItem("Dual-blade Butcher", 10.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_DOUBLE_AXE_2 = ITEMS.registerItem("unique_double_axe_2", props -> new RpgWeaponItem("Arcanite Reaper", 10.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_DOUBLE_AXE_SW = ITEMS.registerItem("unique_double_axe_sw", props -> new RpgWeaponItem("Sunreaver War Axe", 11.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Glaives
    public static final DeferredItem<RpgWeaponItem> UNIQUE_GLAIVE_1 = ITEMS.registerItem("unique_glaive_1", props -> new RpgWeaponItem("Hellreaver", 9.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_GLAIVE_2 = ITEMS.registerItem("unique_glaive_2", props -> new RpgWeaponItem("Crystalforge Glaive", 9.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_GLAIVE_SW = ITEMS.registerItem("unique_glaive_sw", props -> new RpgWeaponItem("Shivering Felspine", 10.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Hammers
    public static final DeferredItem<RpgWeaponItem> UNIQUE_HAMMER_1 = ITEMS.registerItem("unique_hammer_1", props -> new RpgWeaponItem("Hammer of Destiny", 12.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_HAMMER_2 = ITEMS.registerItem("unique_hammer_2", props -> new RpgWeaponItem("Blackhand", 12.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_HAMMER_SW = ITEMS.registerItem("unique_hammer_sw", props -> new RpgWeaponItem("Hammer of Sanctification", 13.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Maces
    public static final DeferredItem<RpgWeaponItem> UNIQUE_MACE_1 = ITEMS.registerItem("unique_mace_1", props -> new RpgWeaponItem("Bonecracker", 8.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_MACE_2 = ITEMS.registerItem("unique_mace_2", props -> new RpgWeaponItem("Stormherald", 8.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_MACE_SW = ITEMS.registerItem("unique_mace_sw", props -> new RpgWeaponItem("Archon's Scepter", 9.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Sickles
    public static final DeferredItem<RpgWeaponItem> UNIQUE_SICKLE_1 = ITEMS.registerItem("unique_sickle_1", props -> new RpgWeaponItem("Toxic Sickle", 6.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_SICKLE_2 = ITEMS.registerItem("unique_sickle_2", props -> new RpgWeaponItem("Infernal Harvester", 6.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_SICKLE_SW = ITEMS.registerItem("unique_sickle_sw", props -> new RpgWeaponItem("Thalassian Sickle", 7.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Spears
    public static final DeferredItem<RpgWeaponItem> UNIQUE_SPEAR_1 = ITEMS.registerItem("unique_spear_1", props -> new RpgWeaponItem("Sonic Spear", 8.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_SPEAR_2 = ITEMS.registerItem("unique_spear_2", props -> new RpgWeaponItem("Spear of the Damned", 8.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_SPEAR_SW = ITEMS.registerItem("unique_spear_sw", props -> new RpgWeaponItem("Mounting Vengeance", 9.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Longsword
    public static final DeferredItem<RpgWeaponItem> UNIQUE_LONGSWORD_SW = ITEMS.registerItem("unique_longsword_sw", props -> new RpgWeaponItem("Dragonscale-Encrusted Longblade", 9.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Longbows
    public static final DeferredItem<RpgWeaponItem> UNIQUE_LONGBOW_1 = ITEMS.registerItem("unique_longbow_1", props -> new RpgWeaponItem("Sunfury Hawk-Bow", 7.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_LONGBOW_2 = ITEMS.registerItem("unique_longbow_2", props -> new RpgWeaponItem("Black Bow of the Betrayer", 7.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_LONGBOW_SW = ITEMS.registerItem("unique_longbow_sw", props -> new RpgWeaponItem("Golden Bow of Quel'Thalas", 8.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Heavy Crossbows
    public static final DeferredItem<RpgWeaponItem> UNIQUE_HEAVY_CROSSBOW_1 = ITEMS.registerItem("unique_heavy_crossbow_1", props -> new RpgWeaponItem("Heavy Crossbow of the Phoenix", 9.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_HEAVY_CROSSBOW_2 = ITEMS.registerItem("unique_heavy_crossbow_2", props -> new RpgWeaponItem("Necropolis Ballista", 9.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_HEAVY_CROSSBOW_SW = ITEMS.registerItem("unique_heavy_crossbow_sw", props -> new RpgWeaponItem("Crossbow of Relentless Strikes", 10.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Damage Staves
    public static final DeferredItem<RpgWeaponItem> UNIQUE_STAFF_DAMAGE_1 = ITEMS.registerItem("unique_staff_damage_1", props -> new RpgWeaponItem("Nexus Key", 4.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_STAFF_DAMAGE_2 = ITEMS.registerItem("unique_staff_damage_2", props -> new RpgWeaponItem("Antonidas's Staff", 4.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_STAFF_DAMAGE_3 = ITEMS.registerItem("unique_staff_damage_3", props -> new RpgWeaponItem("Draconic Battle Staff", 4.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_STAFF_DAMAGE_4 = ITEMS.registerItem("unique_staff_damage_4", props -> new RpgWeaponItem("Gargoyle's Bite", 4.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_STAFF_DAMAGE_5 = ITEMS.registerItem("unique_staff_damage_5", props -> new RpgWeaponItem("Mage Lord Cane", 4.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_STAFF_DAMAGE_6 = ITEMS.registerItem("unique_staff_damage_6", props -> new RpgWeaponItem("Endless Winter", 4.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_STAFF_DAMAGE_SW = ITEMS.registerItem("unique_staff_damage_sw", props -> new RpgWeaponItem("Grand Magister's Staff", 5.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Healing Staves
    public static final DeferredItem<RpgWeaponItem> UNIQUE_STAFF_HEAL_1 = ITEMS.registerItem("unique_staff_heal_1", props -> new RpgWeaponItem("Crystalline Life-Staff", 3.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_STAFF_HEAL_2 = ITEMS.registerItem("unique_staff_heal_2", props -> new RpgWeaponItem("Staff of Immaculate Recovery", 3.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_STAFF_HEAL_SW = ITEMS.registerItem("unique_staff_heal_sw", props -> new RpgWeaponItem("Golden Staff of the Sin'dorei", 4.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Arsenal Shields
    public static final DeferredItem<RpgWeaponItem> UNIQUE_SHIELD_1 = ITEMS.registerItem("unique_shield_1", props -> new RpgWeaponItem("Bulwark of Azzinoth", 2.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_SHIELD_2 = ITEMS.registerItem("unique_shield_2", props -> new RpgWeaponItem("Bastion of Light", 2.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_SHIELD_SW = ITEMS.registerItem("unique_shield_sw", props -> new RpgWeaponItem("Sword Breaker's Bulwark", 3.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // ===== NEW CORE RPG WEAPONS =====
    public static final DeferredItem<RpgWeaponItem> VOIDREAVER = ITEMS.registerItem("voidreaver", props -> new RpgWeaponItem("Voidreaver", 9.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:voidreaver"));
    public static final DeferredItem<RpgWeaponItem> SOLARIS = ITEMS.registerItem("solaris", props -> new RpgWeaponItem("Solaris", 8.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:solaris"));
    public static final DeferredItem<RpgWeaponItem> STORMFURY = ITEMS.registerItem("stormfury", props -> new RpgWeaponItem("Stormfury", 7.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:stormfury"));
    public static final DeferredItem<RpgWeaponItem> BRIARTHORN = ITEMS.registerItem("briarthorn", props -> new RpgWeaponItem("Briarthorn", 4.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:briarthorn"));
    public static final DeferredItem<RpgWeaponItem> ABYSSAL_TRIDENT = ITEMS.registerItem("abyssal_trident", props -> new RpgWeaponItem("Abyssal Trident", 8.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:abyssal_trident"));
    public static final DeferredItem<RpgWeaponItem> PYROCLAST = ITEMS.registerItem("pyroclast", props -> new RpgWeaponItem("Pyroclast", 11.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:pyroclast"));
    public static final DeferredItem<RpgWeaponItem> WHISPERWIND = ITEMS.registerItem("whisperwind", props -> new RpgWeaponItem("Whisperwind", 6.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:whisperwind"));
    public static final DeferredItem<RpgWeaponItem> SOULCHAIN = ITEMS.registerItem("soulchain", props -> new RpgWeaponItem("Soulchain", 7.0f, (Item.Properties)props, java.util.List.of()), () -> com.ultra.megamod.feature.relics.weapons.TomeSpellAssignments.props(new Item.Properties().stacksTo(1), "megamod:soulchain"));
    // ===== NEW ARSENAL WEAPONS =====
    // Whips
    public static final DeferredItem<RpgWeaponItem> UNIQUE_WHIP_1 = ITEMS.registerItem("unique_whip_1", props -> new RpgWeaponItem("Serpent's Lash", 6.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_WHIP_2 = ITEMS.registerItem("unique_whip_2", props -> new RpgWeaponItem("Flamelash", 6.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_WHIP_SW = ITEMS.registerItem("unique_whip_sw", props -> new RpgWeaponItem("Thornwhip of the Verdant Warden", 7.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Wands
    public static final DeferredItem<RpgWeaponItem> UNIQUE_WAND_1 = ITEMS.registerItem("unique_wand_1", props -> new RpgWeaponItem("Arcane Focus", 3.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_WAND_2 = ITEMS.registerItem("unique_wand_2", props -> new RpgWeaponItem("Frostfinger", 3.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_WAND_SW = ITEMS.registerItem("unique_wand_sw", props -> new RpgWeaponItem("Star Conduit", 4.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Katanas
    public static final DeferredItem<RpgWeaponItem> UNIQUE_KATANA_1 = ITEMS.registerItem("unique_katana_1", props -> new RpgWeaponItem("Windcutter", 7.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_KATANA_2 = ITEMS.registerItem("unique_katana_2", props -> new RpgWeaponItem("Shadowmoon Blade", 7.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_KATANA_SW = ITEMS.registerItem("unique_katana_sw", props -> new RpgWeaponItem("Ashenblade of the Eternal Dusk", 8.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Greatshields
    public static final DeferredItem<RpgWeaponItem> UNIQUE_GREATSHIELD_1 = ITEMS.registerItem("unique_greatshield_1", props -> new RpgWeaponItem("Ironwall", 3.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_GREATSHIELD_2 = ITEMS.registerItem("unique_greatshield_2", props -> new RpgWeaponItem("Aegis of Dawn", 3.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_GREATSHIELD_SW = ITEMS.registerItem("unique_greatshield_sw", props -> new RpgWeaponItem("Titan's Bulwark", 4.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Throwing Axes
    public static final DeferredItem<RpgWeaponItem> UNIQUE_THROWING_AXE_1 = ITEMS.registerItem("unique_throwing_axe_1", props -> new RpgWeaponItem("Stormhatchet", 7.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_THROWING_AXE_2 = ITEMS.registerItem("unique_throwing_axe_2", props -> new RpgWeaponItem("Frostbite Hatchet", 7.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_THROWING_AXE_SW = ITEMS.registerItem("unique_throwing_axe_sw", props -> new RpgWeaponItem("Galeforce Tomahawk", 8.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Rapiers
    public static final DeferredItem<RpgWeaponItem> UNIQUE_RAPIER_1 = ITEMS.registerItem("unique_rapier_1", props -> new RpgWeaponItem("Duelist's Sting", 6.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_RAPIER_2 = ITEMS.registerItem("unique_rapier_2", props -> new RpgWeaponItem("Venomfang Foil", 6.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_RAPIER_SW = ITEMS.registerItem("unique_rapier_sw", props -> new RpgWeaponItem("Silvered Estoc of the Court", 7.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Fill-in _3 variants + new longswords
    public static final DeferredItem<RpgWeaponItem> UNIQUE_LONGSWORD_1 = ITEMS.registerItem("unique_longsword_1", props -> new RpgWeaponItem("Hearthguard Blade", 8.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_LONGSWORD_2 = ITEMS.registerItem("unique_longsword_2", props -> new RpgWeaponItem("Nightfall Edge", 8.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_CLAYMORE_3 = ITEMS.registerItem("unique_claymore_3", props -> new RpgWeaponItem("Northwind Greatsword", 11.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_DAGGER_3 = ITEMS.registerItem("unique_dagger_3", props -> new RpgWeaponItem("Whispersting", 5.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_DOUBLE_AXE_3 = ITEMS.registerItem("unique_double_axe_3", props -> new RpgWeaponItem("Cindercleaver", 10.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_GLAIVE_3 = ITEMS.registerItem("unique_glaive_3", props -> new RpgWeaponItem("Moonreaver Glaive", 9.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_HAMMER_3 = ITEMS.registerItem("unique_hammer_3", props -> new RpgWeaponItem("Frostfall Maul", 12.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_MACE_3 = ITEMS.registerItem("unique_mace_3", props -> new RpgWeaponItem("Radiant Morningstar", 8.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_SICKLE_3 = ITEMS.registerItem("unique_sickle_3", props -> new RpgWeaponItem("Frostbite Reaper", 6.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_SPEAR_3 = ITEMS.registerItem("unique_spear_3", props -> new RpgWeaponItem("Thunderlance", 8.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_LONGBOW_3 = ITEMS.registerItem("unique_longbow_3", props -> new RpgWeaponItem("Galewind Bow", 7.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_HEAVY_CROSSBOW_3 = ITEMS.registerItem("unique_heavy_crossbow_3", props -> new RpgWeaponItem("Arcane Arbalest", 9.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_STAFF_DAMAGE_8 = ITEMS.registerItem("unique_staff_damage_8", props -> new RpgWeaponItem("Stormcaller's Rod", 4.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_STAFF_HEAL_3 = ITEMS.registerItem("unique_staff_heal_3", props -> new RpgWeaponItem("Living Wood Staff", 3.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_SHIELD_3 = ITEMS.registerItem("unique_shield_3", props -> new RpgWeaponItem("Dreadnought Ward", 2.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    // Nightmare-tier _3 variants for remaining weapon types
    public static final DeferredItem<RpgWeaponItem> UNIQUE_LONGSWORD_3 = ITEMS.registerItem("unique_longsword_3", props -> new RpgWeaponItem("Duskblade", 8.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_WHIP_3 = ITEMS.registerItem("unique_whip_3", props -> new RpgWeaponItem("Thunderlash", 6.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_WAND_3 = ITEMS.registerItem("unique_wand_3", props -> new RpgWeaponItem("Voidwand", 3.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_KATANA_3 = ITEMS.registerItem("unique_katana_3", props -> new RpgWeaponItem("Crimson Edge", 7.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_GREATSHIELD_3 = ITEMS.registerItem("unique_greatshield_3", props -> new RpgWeaponItem("Stoneguard Aegis", 3.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_THROWING_AXE_3 = ITEMS.registerItem("unique_throwing_axe_3", props -> new RpgWeaponItem("Tempest Hatchet", 7.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> UNIQUE_RAPIER_3 = ITEMS.registerItem("unique_rapier_3", props -> new RpgWeaponItem("Mithril Foil", 6.0f, (Item.Properties)props, java.util.List.of()), () -> new Item.Properties().stacksTo(1));
    public static final DeferredBlock<Block> RESEARCHING_TABLE_BLOCK = BLOCKS.registerBlock("researching_table", ResearchingTableBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f).noOcclusion());
    public static final DeferredItem<BlockItem> RESEARCHING_TABLE_ITEM = ITEMS.registerItem("researching_table", props -> new com.ultra.megamod.feature.furniture.OffsetBlockItem(RESEARCHING_TABLE_BLOCK.get(), props), () -> new Item.Properties());
    public static final Supplier<CreativeModeTab> RELICS_TAB = CREATIVE_MODE_TABS.register("megamod_relics_tab", () -> CreativeModeTab.builder().title((Component)Component.literal((String)"MegaMod - Relics")).icon(() -> new ItemStack((ItemLike)Items.ENDER_EYE)).displayItems((parameters, output) -> {
        output.accept((ItemLike)ARROW_QUIVER.get());
        output.accept((ItemLike)ELYTRA_BOOSTER.get());
        output.accept((ItemLike)MIDNIGHT_ROBE.get());
        output.accept((ItemLike)LEATHER_BELT.get());
        output.accept((ItemLike)DROWNED_BELT.get());
        output.accept((ItemLike)HUNTER_BELT.get());
        output.accept((ItemLike)ENDER_HAND.get());
        output.accept((ItemLike)RAGE_GLOVE.get());
        output.accept((ItemLike)WOOL_MITTEN.get());
        output.accept((ItemLike)MAGMA_WALKER.get());
        output.accept((ItemLike)AQUA_WALKER.get());
        output.accept((ItemLike)ICE_SKATES.get());
        output.accept((ItemLike)ICE_BREAKER.get());
        output.accept((ItemLike)ROLLER_SKATES.get());
        output.accept((ItemLike)AMPHIBIAN_BOOT.get());
        output.accept((ItemLike)REFLECTION_NECKLACE.get());
        output.accept((ItemLike)JELLYFISH_NECKLACE.get());
        output.accept((ItemLike)HOLY_LOCKET.get());
        output.accept((ItemLike)BASTION_RING.get());
        output.accept((ItemLike)CHORUS_INHIBITOR.get());
        output.accept((ItemLike)SHADOW_GLAIVE.get());
        output.accept((ItemLike)INFINITY_HAM.get());
        output.accept((ItemLike)SPACE_DISSECTOR.get());
        output.accept((ItemLike)MAGIC_MIRROR.get());
        output.accept((ItemLike)HORSE_FLUTE.get());
        output.accept((ItemLike)SPORE_SACK.get());
        output.accept((ItemLike)BLAZING_FLASK.get());
        output.accept((ItemLike)LUNAR_CROWN.get());
        output.accept((ItemLike)SOLAR_CROWN.get());
        // New Face Relics
        output.accept((ItemLike)WARDENS_VISOR.get());
        output.accept((ItemLike)VERDANT_MASK.get());
        output.accept((ItemLike)FROSTWEAVE_VEIL.get());
        // New Head Relics
        output.accept((ItemLike)STORMCALLER_CIRCLET.get());
        output.accept((ItemLike)ASHEN_DIADEM.get());
        output.accept((ItemLike)WRAITH_CROWN.get());
        // New Hands Right Relics
        output.accept((ItemLike)ARCANE_GAUNTLET.get());
        output.accept((ItemLike)IRON_FIST.get());
        output.accept((ItemLike)PLAGUE_GRASP.get());
        output.accept((ItemLike)SUNFORGED_BRACER.get());
        // New Ring Right Relics
        output.accept((ItemLike)STORMBAND.get());
        output.accept((ItemLike)GRAVESTONE_RING.get());
        output.accept((ItemLike)VERDANT_SIGNET.get());
        // New Back Relics
        output.accept((ItemLike)PHOENIX_MANTLE.get());
        output.accept((ItemLike)WINDRUNNER_CLOAK.get());
        output.accept((ItemLike)ABYSSAL_CAPE.get());
        // New Belt Relics
        output.accept((ItemLike)ALCHEMISTS_SASH.get());
        output.accept((ItemLike)GUARDIANS_GIRDLE.get());
        output.accept((ItemLike)SERPENT_BELT.get());
        output.accept((ItemLike)LODESTONE_MAGNET.get());
        // New Necklace Relics
        output.accept((ItemLike)FROSTFIRE_PENDANT.get());
        output.accept((ItemLike)TIDEKEEPER_AMULET.get());
        output.accept((ItemLike)BLOODSTONE_CHOKER.get());
        // New Hands Left Relics
        output.accept((ItemLike)THORNWEAVE_GLOVE.get());
        output.accept((ItemLike)CHRONO_GLOVE.get());
        // New Feet Relics
        output.accept((ItemLike)STORMSTRIDER_BOOTS.get());
        output.accept((ItemLike)SANDWALKER_TREADS.get());
        // New Ring Left Relic
        output.accept((ItemLike)EMBERSTONE_BAND.get());
        // New Usable Relics
        output.accept((ItemLike)VOID_LANTERN.get());
        output.accept((ItemLike)THUNDERHORN.get());
        output.accept((ItemLike)MENDING_CHALICE.get());
        output.accept((ItemLike)RESEARCHING_TABLE_ITEM.get());
        // Dungeon Masks (Face slot relics)
        output.accept((ItemLike)DungeonEntityRegistry.MASK_OF_FEAR.get());
        output.accept((ItemLike)DungeonEntityRegistry.MASK_OF_FURY.get());
        output.accept((ItemLike)DungeonEntityRegistry.MASK_OF_FAITH.get());
        output.accept((ItemLike)DungeonEntityRegistry.MASK_OF_RAGE.get());
        output.accept((ItemLike)DungeonEntityRegistry.MASK_OF_MISERY.get());
        output.accept((ItemLike)DungeonEntityRegistry.MASK_OF_BLISS.get());
    }).build());
    public static final Supplier<CreativeModeTab> WEAPONS_TAB = CREATIVE_MODE_TABS.register("megamod_weapons_tab", () -> CreativeModeTab.builder().title((Component)Component.literal((String)"MegaMod - Weapons")).icon(() -> new ItemStack((ItemLike)Items.NETHERITE_SWORD)).displayItems((parameters, output) -> {
        // Original RPG Weapons
        output.accept((ItemLike)VAMPIRIC_TOME.get());
        output.accept((ItemLike)STATIC_SEEKER.get());
        output.accept((ItemLike)BATTLEDANCER.get());
        output.accept((ItemLike)EBONCHILL.get());
        output.accept((ItemLike)LIGHTBINDER.get());
        output.accept((ItemLike)CRESCENT_BLADE.get());
        output.accept((ItemLike)GHOST_FANG.get());
        output.accept((ItemLike)TERRA_WARHAMMER.get());
        output.accept((ItemLike)SOKA_SINGING_BLADE.get());
        // Arsenal Claymores
        output.accept((ItemLike)UNIQUE_CLAYMORE_1.get());
        output.accept((ItemLike)UNIQUE_CLAYMORE_2.get());
        output.accept((ItemLike)UNIQUE_CLAYMORE_SW.get());
        // Arsenal Daggers
        output.accept((ItemLike)UNIQUE_DAGGER_1.get());
        output.accept((ItemLike)UNIQUE_DAGGER_2.get());
        output.accept((ItemLike)UNIQUE_DAGGER_SW.get());
        // Arsenal Double Axes
        output.accept((ItemLike)UNIQUE_DOUBLE_AXE_1.get());
        output.accept((ItemLike)UNIQUE_DOUBLE_AXE_2.get());
        output.accept((ItemLike)UNIQUE_DOUBLE_AXE_SW.get());
        // Arsenal Glaives
        output.accept((ItemLike)UNIQUE_GLAIVE_1.get());
        output.accept((ItemLike)UNIQUE_GLAIVE_2.get());
        output.accept((ItemLike)UNIQUE_GLAIVE_SW.get());
        // Arsenal Hammers
        output.accept((ItemLike)UNIQUE_HAMMER_1.get());
        output.accept((ItemLike)UNIQUE_HAMMER_2.get());
        output.accept((ItemLike)UNIQUE_HAMMER_SW.get());
        // Arsenal Maces
        output.accept((ItemLike)UNIQUE_MACE_1.get());
        output.accept((ItemLike)UNIQUE_MACE_2.get());
        output.accept((ItemLike)UNIQUE_MACE_SW.get());
        // Arsenal Sickles
        output.accept((ItemLike)UNIQUE_SICKLE_1.get());
        output.accept((ItemLike)UNIQUE_SICKLE_2.get());
        output.accept((ItemLike)UNIQUE_SICKLE_SW.get());
        // Arsenal Spears
        output.accept((ItemLike)UNIQUE_SPEAR_1.get());
        output.accept((ItemLike)UNIQUE_SPEAR_2.get());
        output.accept((ItemLike)UNIQUE_SPEAR_SW.get());
        // Arsenal Longsword
        output.accept((ItemLike)UNIQUE_LONGSWORD_SW.get());
        // Arsenal Longbows
        output.accept((ItemLike)UNIQUE_LONGBOW_1.get());
        output.accept((ItemLike)UNIQUE_LONGBOW_2.get());
        output.accept((ItemLike)UNIQUE_LONGBOW_SW.get());
        // Arsenal Heavy Crossbows
        output.accept((ItemLike)UNIQUE_HEAVY_CROSSBOW_1.get());
        output.accept((ItemLike)UNIQUE_HEAVY_CROSSBOW_2.get());
        output.accept((ItemLike)UNIQUE_HEAVY_CROSSBOW_SW.get());
        // Arsenal Damage Staves
        output.accept((ItemLike)UNIQUE_STAFF_DAMAGE_1.get());
        output.accept((ItemLike)UNIQUE_STAFF_DAMAGE_2.get());
        output.accept((ItemLike)UNIQUE_STAFF_DAMAGE_3.get());
        output.accept((ItemLike)UNIQUE_STAFF_DAMAGE_4.get());
        output.accept((ItemLike)UNIQUE_STAFF_DAMAGE_5.get());
        output.accept((ItemLike)UNIQUE_STAFF_DAMAGE_6.get());
        output.accept((ItemLike)UNIQUE_STAFF_DAMAGE_SW.get());
        // Arsenal Healing Staves
        output.accept((ItemLike)UNIQUE_STAFF_HEAL_1.get());
        output.accept((ItemLike)UNIQUE_STAFF_HEAL_2.get());
        output.accept((ItemLike)UNIQUE_STAFF_HEAL_SW.get());
        // Arsenal Shields
        output.accept((ItemLike)UNIQUE_SHIELD_1.get());
        output.accept((ItemLike)UNIQUE_SHIELD_2.get());
        output.accept((ItemLike)UNIQUE_SHIELD_SW.get());
        // New Core RPG Weapons
        output.accept((ItemLike)VOIDREAVER.get());
        output.accept((ItemLike)SOLARIS.get());
        output.accept((ItemLike)STORMFURY.get());
        output.accept((ItemLike)BRIARTHORN.get());
        output.accept((ItemLike)ABYSSAL_TRIDENT.get());
        output.accept((ItemLike)PYROCLAST.get());
        output.accept((ItemLike)WHISPERWIND.get());
        output.accept((ItemLike)SOULCHAIN.get());
        // New Arsenal Whips
        output.accept((ItemLike)UNIQUE_WHIP_1.get());
        output.accept((ItemLike)UNIQUE_WHIP_2.get());
        output.accept((ItemLike)UNIQUE_WHIP_SW.get());
        // New Arsenal Wands
        output.accept((ItemLike)UNIQUE_WAND_1.get());
        output.accept((ItemLike)UNIQUE_WAND_2.get());
        output.accept((ItemLike)UNIQUE_WAND_SW.get());
        // New Arsenal Katanas
        output.accept((ItemLike)UNIQUE_KATANA_1.get());
        output.accept((ItemLike)UNIQUE_KATANA_2.get());
        output.accept((ItemLike)UNIQUE_KATANA_SW.get());
        // New Arsenal Greatshields
        output.accept((ItemLike)UNIQUE_GREATSHIELD_1.get());
        output.accept((ItemLike)UNIQUE_GREATSHIELD_2.get());
        output.accept((ItemLike)UNIQUE_GREATSHIELD_SW.get());
        // New Arsenal Throwing Axes
        output.accept((ItemLike)UNIQUE_THROWING_AXE_1.get());
        output.accept((ItemLike)UNIQUE_THROWING_AXE_2.get());
        output.accept((ItemLike)UNIQUE_THROWING_AXE_SW.get());
        // New Arsenal Rapiers
        output.accept((ItemLike)UNIQUE_RAPIER_1.get());
        output.accept((ItemLike)UNIQUE_RAPIER_2.get());
        output.accept((ItemLike)UNIQUE_RAPIER_SW.get());
        // Fill-in Variants + New Longswords
        output.accept((ItemLike)UNIQUE_LONGSWORD_1.get());
        output.accept((ItemLike)UNIQUE_LONGSWORD_2.get());
        output.accept((ItemLike)UNIQUE_CLAYMORE_3.get());
        output.accept((ItemLike)UNIQUE_DAGGER_3.get());
        output.accept((ItemLike)UNIQUE_DOUBLE_AXE_3.get());
        output.accept((ItemLike)UNIQUE_GLAIVE_3.get());
        output.accept((ItemLike)UNIQUE_HAMMER_3.get());
        output.accept((ItemLike)UNIQUE_MACE_3.get());
        output.accept((ItemLike)UNIQUE_SICKLE_3.get());
        output.accept((ItemLike)UNIQUE_SPEAR_3.get());
        output.accept((ItemLike)UNIQUE_LONGBOW_3.get());
        output.accept((ItemLike)UNIQUE_HEAVY_CROSSBOW_3.get());
        output.accept((ItemLike)UNIQUE_STAFF_DAMAGE_8.get());
        output.accept((ItemLike)UNIQUE_STAFF_HEAL_3.get());
        output.accept((ItemLike)UNIQUE_SHIELD_3.get());
        // Nightmare-tier _3 variants
        output.accept((ItemLike)UNIQUE_LONGSWORD_3.get());
        output.accept((ItemLike)UNIQUE_WHIP_3.get());
        output.accept((ItemLike)UNIQUE_WAND_3.get());
        output.accept((ItemLike)UNIQUE_KATANA_3.get());
        output.accept((ItemLike)UNIQUE_GREATSHIELD_3.get());
        output.accept((ItemLike)UNIQUE_THROWING_AXE_3.get());
        output.accept((ItemLike)UNIQUE_RAPIER_3.get());
        // Dungeon Weapons
        output.accept((ItemLike)DungeonEntityRegistry.OSSUKAGE_SWORD.get());
        output.accept((ItemLike)DungeonEntityRegistry.NAGA_FANG_DAGGER.get());
        output.accept((ItemLike)DungeonEntityRegistry.LIFE_STEALER.get());
        output.accept((ItemLike)DungeonEntityRegistry.WROUGHT_AXE.get());
        output.accept((ItemLike)DungeonEntityRegistry.SPEAR.get());
        output.accept((ItemLike)DungeonEntityRegistry.FANG_ON_A_STICK.get());
        output.accept((ItemLike)DungeonEntityRegistry.SCEPTER_OF_CHAOS.get());
        output.accept((ItemLike)DungeonEntityRegistry.SOL_VISAGE.get());
        output.accept((ItemLike)DungeonEntityRegistry.EARTHREND_GAUNTLET.get());
        output.accept((ItemLike)DungeonEntityRegistry.BLOWGUN.get());
        // Mythic Netherite Weapons
        output.accept((ItemLike)com.ultra.megamod.feature.dungeons.loot.DungeonExclusiveItems.MYTHIC_NETHERITE_SWORD.get());
        output.accept((ItemLike)com.ultra.megamod.feature.dungeons.loot.DungeonExclusiveItems.MYTHIC_NETHERITE_AXE.get());
    }).build());

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        ArrowQuiverAbility.register();
        ElytraBoosterAbility.register();
        MidnightRobeAbility.register();
        LeatherBeltAbility.register();
        DrownedBeltAbility.register();
        HunterBeltAbility.register();
        ReflectionNecklaceAbility.register();
        JellyfishNecklaceAbility.register();
        HolyLocketAbility.register();
        MagmaWalkerAbility.register();
        AquaWalkerAbility.register();
        IceSkatesAbility.register();
        IceBreakerAbility.register();
        RollerSkatesAbility.register();
        AmphibianBootAbility.register();
        EnderHandAbility.register();
        RageGloveAbility.register();
        WoolMittenAbility.register();
        BastionRingAbility.register();
        ChorusInhibitorAbility.register();
        ShadowGlaiveAbility.register();
        InfinityHamAbility.register();
        SpaceDissectorAbility.register();
        MagicMirrorAbility.register();
        HorseFluteAbility.register();
        SporeSackAbility.register();
        BlazingFlaskAbility.register();
        LunarCrownAbility.register();
        SolarCrownAbility.register();
        WardensVisorAbility.register();
        VerdantMaskAbility.register();
        FrostweaveVeilAbility.register();
        StormcallerCircletAbility.register();
        AshenDiademAbility.register();
        WraithCrownAbility.register();
        ArcaneGauntletAbility.register();
        IronFistAbility.register();
        PlagueGraspAbility.register();
        SunforgedBracerAbility.register();
        StormbandAbility.register();
        GravestoneRingAbility.register();
        VerdantSignetAbility.register();
        PhoenixMantleAbility.register();
        WindrunnerCloakAbility.register();
        AbyssalCapeAbility.register();
        AlchemistsSashAbility.register();
        GuardiansGirdleAbility.register();
        SerpentBeltAbility.register();
        FrostfirePendantAbility.register();
        TidekeeperAmuletAbility.register();
        BloodstoneChokerAbility.register();
        ThornweaveGloveAbility.register();
        ChronoGloveAbility.register();
        StormstriderBootsAbility.register();
        SandwalkerTreadsAbility.register();
        EmberstoneBandAbility.register();
        VoidLanternAbility.register();
        ThunderhornAbility.register();
        MendingChaliceAbility.register();
        LodestoneMagnetAbility.register();
    }
}

