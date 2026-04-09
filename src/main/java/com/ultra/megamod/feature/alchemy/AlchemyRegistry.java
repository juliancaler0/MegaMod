package com.ultra.megamod.feature.alchemy;

import com.ultra.megamod.feature.alchemy.block.AlchemyCauldronBlock;
import com.ultra.megamod.feature.alchemy.block.AlchemyCauldronBlockEntity;
import com.ultra.megamod.feature.alchemy.block.AlchemyGrindstoneBlock;
import com.ultra.megamod.feature.alchemy.block.AlchemyGrindstoneBlockEntity;
import com.ultra.megamod.feature.alchemy.block.AlchemyShelfBlock;
import com.ultra.megamod.feature.alchemy.block.AlchemyShelfBlockEntity;
import com.ultra.megamod.feature.alchemy.block.AlchemyShelfMenu;
import com.ultra.megamod.feature.alchemy.effect.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AlchemyRegistry {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "megamod");
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, "megamod");
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create((ResourceKey) Registries.MENU, "megamod");

    // ==================== Blocks ====================

    public static final DeferredBlock<AlchemyCauldronBlock> ALCHEMY_CAULDRON = BLOCKS.registerBlock(
            "alchemy_cauldron", AlchemyCauldronBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops().noOcclusion().lightLevel(state -> state.getValue(AlchemyCauldronBlock.BREWING) ? 7 : 0));

    public static final DeferredBlock<AlchemyGrindstoneBlock> ALCHEMY_GRINDSTONE = BLOCKS.registerBlock(
            "alchemy_grindstone", AlchemyGrindstoneBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<AlchemyShelfBlock> ALCHEMY_SHELF = BLOCKS.registerBlock(
            "alchemy_shelf", AlchemyShelfBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());

    // ==================== Block Items ====================

    public static final DeferredItem<BlockItem> ALCHEMY_CAULDRON_ITEM = ITEMS.registerSimpleBlockItem("alchemy_cauldron", ALCHEMY_CAULDRON);
    public static final DeferredItem<BlockItem> ALCHEMY_GRINDSTONE_ITEM = ITEMS.registerSimpleBlockItem("alchemy_grindstone", ALCHEMY_GRINDSTONE);
    public static final DeferredItem<BlockItem> ALCHEMY_SHELF_ITEM = ITEMS.registerSimpleBlockItem("alchemy_shelf", ALCHEMY_SHELF);

    // ==================== Reagent Items ====================

    public static final DeferredItem<Item> REAGENT_EMBER_DUST = ITEMS.registerSimpleItem("reagent_ember_dust", () -> new Item.Properties().stacksTo(64));
    public static final DeferredItem<Item> REAGENT_FROST_CRYSTAL = ITEMS.registerSimpleItem("reagent_frost_crystal", () -> new Item.Properties().stacksTo(64));
    public static final DeferredItem<Item> REAGENT_SHADOW_ESSENCE = ITEMS.registerSimpleItem("reagent_shadow_essence", () -> new Item.Properties().stacksTo(64));
    public static final DeferredItem<Item> REAGENT_LIFE_BLOOM = ITEMS.registerSimpleItem("reagent_life_bloom", () -> new Item.Properties().stacksTo(64));
    public static final DeferredItem<Item> REAGENT_VOID_SALT = ITEMS.registerSimpleItem("reagent_void_salt", () -> new Item.Properties().stacksTo(64));
    public static final DeferredItem<Item> REAGENT_STORM_CHARGE = ITEMS.registerSimpleItem("reagent_storm_charge", () -> new Item.Properties().stacksTo(64));
    public static final DeferredItem<Item> REAGENT_BLOOD_MOSS = ITEMS.registerSimpleItem("reagent_blood_moss", () -> new Item.Properties().stacksTo(64));
    public static final DeferredItem<Item> REAGENT_STARLIGHT_DEW = ITEMS.registerSimpleItem("reagent_starlight_dew", () -> new Item.Properties().stacksTo(64));
    public static final DeferredItem<Item> REAGENT_EARTH_ROOT = ITEMS.registerSimpleItem("reagent_earth_root", () -> new Item.Properties().stacksTo(64));
    public static final DeferredItem<Item> REAGENT_ARCANE_FLUX = ITEMS.registerSimpleItem("reagent_arcane_flux", () -> new Item.Properties().stacksTo(64));

    // ==================== Potion Items ====================

    public static final DeferredItem<Item> POTION_INFERNO = ITEMS.registerSimpleItem("potion_inferno", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_GLACIER = ITEMS.registerSimpleItem("potion_glacier", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_SHADOW_STEP = ITEMS.registerSimpleItem("potion_shadow_step", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_VITALITY = ITEMS.registerSimpleItem("potion_vitality", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_VOID_WALK = ITEMS.registerSimpleItem("potion_void_walk", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_TEMPEST = ITEMS.registerSimpleItem("potion_tempest", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_BERSERKER = ITEMS.registerSimpleItem("potion_berserker", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_STARLIGHT = ITEMS.registerSimpleItem("potion_starlight", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_STONE_SKIN = ITEMS.registerSimpleItem("potion_stone_skin", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_ARCANE_SURGE = ITEMS.registerSimpleItem("potion_arcane_surge", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_SWIFTBREW = ITEMS.registerSimpleItem("potion_swiftbrew", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_IRON_GUT = ITEMS.registerSimpleItem("potion_iron_gut", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_MIDAS_TOUCH = ITEMS.registerSimpleItem("potion_midas_touch", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_EAGLE_EYE = ITEMS.registerSimpleItem("potion_eagle_eye", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_UNDYING = ITEMS.registerSimpleItem("potion_undying", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_PHANTOM = ITEMS.registerSimpleItem("potion_phantom", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_TITAN = ITEMS.registerSimpleItem("potion_titan", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_TIDAL_WAVE = ITEMS.registerSimpleItem("potion_tidal_wave", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_CHRONOS = ITEMS.registerSimpleItem("potion_chronos", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_BLOOD_RAGE = ITEMS.registerSimpleItem("potion_blood_rage", () -> new Item.Properties().stacksTo(16));

    // ==================== Spell Power Potion Items ====================

    public static final DeferredItem<Item> POTION_SPELL_ARCANE_SURGE = ITEMS.registerSimpleItem("potion_spell_arcane_surge", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_FIRE_ATTUNEMENT = ITEMS.registerSimpleItem("potion_fire_attunement", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_FROST_ATTUNEMENT = ITEMS.registerSimpleItem("potion_frost_attunement", () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> POTION_HEALING_GRACE = ITEMS.registerSimpleItem("potion_healing_grace", () -> new Item.Properties().stacksTo(16));

    // ==================== Block Entities ====================

    public static final Supplier<BlockEntityType<AlchemyCauldronBlockEntity>> ALCHEMY_CAULDRON_BE =
            BLOCK_ENTITIES.register("alchemy_cauldron",
                    () -> new BlockEntityType<>(AlchemyCauldronBlockEntity::new, new Block[]{ALCHEMY_CAULDRON.get()}));

    public static final Supplier<BlockEntityType<AlchemyGrindstoneBlockEntity>> ALCHEMY_GRINDSTONE_BE =
            BLOCK_ENTITIES.register("alchemy_grindstone",
                    () -> new BlockEntityType<>(AlchemyGrindstoneBlockEntity::new, new Block[]{ALCHEMY_GRINDSTONE.get()}));

    public static final Supplier<BlockEntityType<AlchemyShelfBlockEntity>> ALCHEMY_SHELF_BE =
            BLOCK_ENTITIES.register("alchemy_shelf",
                    () -> new BlockEntityType<>(AlchemyShelfBlockEntity::new, new Block[]{ALCHEMY_SHELF.get()}));

    // ==================== Menus ====================

    public static final Supplier<MenuType<AlchemyShelfMenu>> ALCHEMY_SHELF_MENU = MENUS.register("alchemy_shelf",
            () -> IMenuTypeExtension.create(AlchemyShelfMenu::new));

    // ==================== Mob Effects ====================

    public static final Supplier<MobEffect> INFERNO_BOOST = MOB_EFFECTS.register("inferno_boost", InfernoBoostEffect::new);
    public static final Supplier<MobEffect> FROST_AURA = MOB_EFFECTS.register("frost_aura", FrostAuraEffect::new);
    public static final Supplier<MobEffect> SHADOW_STEP = MOB_EFFECTS.register("shadow_step", ShadowStepEffect::new);
    public static final Supplier<MobEffect> VOID_WALK = MOB_EFFECTS.register("void_walk", VoidWalkEffect::new);
    public static final Supplier<MobEffect> TEMPEST = MOB_EFFECTS.register("tempest", TempestEffect::new);
    public static final Supplier<MobEffect> BERSERKER_RAGE = MOB_EFFECTS.register("berserker_rage", BerserkerRageEffect::new);
    public static final Supplier<MobEffect> STARLIGHT = MOB_EFFECTS.register("starlight", StarlightEffect::new);
    public static final Supplier<MobEffect> STONE_SKIN = MOB_EFFECTS.register("stone_skin", StoneSkinEffect::new);
    public static final Supplier<MobEffect> ARCANE_SURGE = MOB_EFFECTS.register("arcane_surge", ArcaneSurgeEffect::new);
    public static final Supplier<MobEffect> EAGLE_EYE = MOB_EFFECTS.register("eagle_eye", EagleEyeEffect::new);
    public static final Supplier<MobEffect> UNDYING_GRACE = MOB_EFFECTS.register("undying_grace", UndyingGraceEffect::new);
    public static final Supplier<MobEffect> MIDAS_TOUCH = MOB_EFFECTS.register("midas_touch", MidasTouchEffect::new);
    public static final Supplier<MobEffect> PHANTOM_PHASE = MOB_EFFECTS.register("phantom_phase", PhantomPhaseEffect::new);
    public static final Supplier<MobEffect> TITAN = MOB_EFFECTS.register("titan", TitanEffect::new);
    public static final Supplier<MobEffect> CHRONOS = MOB_EFFECTS.register("chronos", ChronosEffect::new);
    public static final Supplier<MobEffect> BLOOD_RAGE = MOB_EFFECTS.register("blood_rage", BloodRageEffect::new);

    // ── Spell Power Effects ──
    public static final Supplier<MobEffect> SPELL_ARCANE_SURGE = MOB_EFFECTS.register("spell_arcane_surge", SpellArcaneSurgeEffect::new);
    public static final Supplier<MobEffect> FIRE_ATTUNEMENT = MOB_EFFECTS.register("fire_attunement", FireAttunementEffect::new);
    public static final Supplier<MobEffect> FROST_ATTUNEMENT = MOB_EFFECTS.register("frost_attunement", FrostAttunementEffect::new);
    public static final Supplier<MobEffect> HEALING_GRACE = MOB_EFFECTS.register("healing_grace", HealingGraceEffect::new);

    // ==================== Helper to get Holder from Supplier ====================
    // DeferredRegister.register() returns DeferredHolder which IS a Holder

    @SuppressWarnings("unchecked")
    public static Holder<MobEffect> holderOf(Supplier<MobEffect> supplier) {
        // DeferredHolder<MobEffect, MobEffect> implements both Supplier and Holder
        if (supplier instanceof Holder) {
            return (Holder<MobEffect>) supplier;
        }
        // Fallback: wrap via registry
        MobEffect effect = supplier.get();
        return (Holder<MobEffect>) (Object) net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
    }

    // ==================== Init ====================

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MOB_EFFECTS.register(modBus);
        MENUS.register(modBus);
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == ResourceKey.create(
                    (ResourceKey) Registries.CREATIVE_MODE_TAB,
                    (Identifier) Identifier.fromNamespaceAndPath("megamod", "megamod_tab"))) {
                // Blocks
                event.accept((ItemLike) ALCHEMY_CAULDRON_ITEM.get());
                event.accept((ItemLike) ALCHEMY_GRINDSTONE_ITEM.get());
                event.accept((ItemLike) ALCHEMY_SHELF_ITEM.get());
                // Reagents
                event.accept((ItemLike) REAGENT_EMBER_DUST.get());
                event.accept((ItemLike) REAGENT_FROST_CRYSTAL.get());
                event.accept((ItemLike) REAGENT_SHADOW_ESSENCE.get());
                event.accept((ItemLike) REAGENT_LIFE_BLOOM.get());
                event.accept((ItemLike) REAGENT_VOID_SALT.get());
                event.accept((ItemLike) REAGENT_STORM_CHARGE.get());
                event.accept((ItemLike) REAGENT_BLOOD_MOSS.get());
                event.accept((ItemLike) REAGENT_STARLIGHT_DEW.get());
                event.accept((ItemLike) REAGENT_EARTH_ROOT.get());
                event.accept((ItemLike) REAGENT_ARCANE_FLUX.get());
                // Potions
                event.accept((ItemLike) POTION_INFERNO.get());
                event.accept((ItemLike) POTION_GLACIER.get());
                event.accept((ItemLike) POTION_SHADOW_STEP.get());
                event.accept((ItemLike) POTION_VITALITY.get());
                event.accept((ItemLike) POTION_VOID_WALK.get());
                event.accept((ItemLike) POTION_TEMPEST.get());
                event.accept((ItemLike) POTION_BERSERKER.get());
                event.accept((ItemLike) POTION_STARLIGHT.get());
                event.accept((ItemLike) POTION_STONE_SKIN.get());
                event.accept((ItemLike) POTION_ARCANE_SURGE.get());
                event.accept((ItemLike) POTION_SWIFTBREW.get());
                event.accept((ItemLike) POTION_IRON_GUT.get());
                event.accept((ItemLike) POTION_MIDAS_TOUCH.get());
                event.accept((ItemLike) POTION_EAGLE_EYE.get());
                event.accept((ItemLike) POTION_UNDYING.get());
                event.accept((ItemLike) POTION_PHANTOM.get());
                event.accept((ItemLike) POTION_TITAN.get());
                event.accept((ItemLike) POTION_TIDAL_WAVE.get());
                event.accept((ItemLike) POTION_CHRONOS.get());
                event.accept((ItemLike) POTION_BLOOD_RAGE.get());
                // Spell Power Potions
                event.accept((ItemLike) POTION_SPELL_ARCANE_SURGE.get());
                event.accept((ItemLike) POTION_FIRE_ATTUNEMENT.get());
                event.accept((ItemLike) POTION_FROST_ATTUNEMENT.get());
                event.accept((ItemLike) POTION_HEALING_GRACE.get());
            }
        });
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ALCHEMY_SHELF_MENU.get(), com.ultra.megamod.feature.alchemy.block.AlchemyShelfScreen::new);
    }
}
