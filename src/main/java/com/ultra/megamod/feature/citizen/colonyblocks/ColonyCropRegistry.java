package com.ultra.megamod.feature.citizen.colonyblocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for all colony crops (15 types), 2 farmland types, seed items, and creative tab.
 */
public class ColonyCropRegistry {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create((ResourceKey) Registries.CREATIVE_MODE_TAB, "megamod");

    // ==================== Crop Block Properties ====================

    private static BlockBehaviour.Properties cropProps() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .noCollision()
            .randomTicks()
            .instabreak()
            .sound(SoundType.CROP)
            .pushReaction(PushReaction.DESTROY);
    }

    // ==================== Crop Blocks (15 crops) ====================

    public static final DeferredBlock<ColonyCropBlock> DURUM_CROP = BLOCKS.registerBlock(
        "crop_durum", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> EGGPLANT_CROP = BLOCKS.registerBlock(
        "crop_eggplant", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> GARLIC_CROP = BLOCKS.registerBlock(
        "crop_garlic", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> ONION_CROP = BLOCKS.registerBlock(
        "crop_onion", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> MINT_CROP = BLOCKS.registerBlock(
        "crop_mint", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> BELL_PEPPER_CROP = BLOCKS.registerBlock(
        "crop_bell_pepper", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> TOMATO_CROP = BLOCKS.registerBlock(
        "crop_tomato", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> CORN_CROP = BLOCKS.registerBlock(
        "crop_corn", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> CABBAGE_CROP = BLOCKS.registerBlock(
        "crop_cabbage", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> BUTTERNUT_SQUASH_CROP = BLOCKS.registerBlock(
        "crop_butternut_squash", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> CHICKPEA_CROP = BLOCKS.registerBlock(
        "crop_chickpea", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> SOY_BEAN_CROP = BLOCKS.registerBlock(
        "crop_soy_bean", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> PEAS_CROP = BLOCKS.registerBlock(
        "crop_peas", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> RICE_CROP = BLOCKS.registerBlock(
        "crop_rice", ColonyCropBlock::new, ColonyCropRegistry::cropProps);
    public static final DeferredBlock<ColonyCropBlock> NETHER_PEPPER_CROP = BLOCKS.registerBlock(
        "crop_nether_pepper", ColonyCropBlock::new, ColonyCropRegistry::cropProps);

    // ==================== Farmland Blocks ====================

    public static final DeferredBlock<Block> COLONY_FARMLAND = BLOCKS.registerBlock(
        "colony_farmland", Block::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.6f).sound(SoundType.GRAVEL)
            .isViewBlocking((s, g, p) -> true).isSuffocating((s, g, p) -> true));

    public static final DeferredBlock<Block> FLOODED_FARMLAND = BLOCKS.registerBlock(
        "flooded_farmland", Block::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.6f).sound(SoundType.GRAVEL)
            .isViewBlocking((s, g, p) -> true).isSuffocating((s, g, p) -> true));

    // ==================== Seed Items (place crops) ====================

    public static final DeferredItem<Item> DURUM_SEEDS = ITEMS.registerItem("seeds_durum",
        p -> new BlockItem(DURUM_CROP.get(), p));
    public static final DeferredItem<Item> EGGPLANT_SEEDS = ITEMS.registerItem("seeds_eggplant",
        p -> new BlockItem(EGGPLANT_CROP.get(), p));
    public static final DeferredItem<Item> GARLIC_SEEDS = ITEMS.registerItem("seeds_garlic",
        p -> new BlockItem(GARLIC_CROP.get(), p));
    public static final DeferredItem<Item> ONION_SEEDS = ITEMS.registerItem("seeds_onion",
        p -> new BlockItem(ONION_CROP.get(), p));
    public static final DeferredItem<Item> MINT_SEEDS = ITEMS.registerItem("seeds_mint",
        p -> new BlockItem(MINT_CROP.get(), p));
    public static final DeferredItem<Item> BELL_PEPPER_SEEDS = ITEMS.registerItem("seeds_bell_pepper",
        p -> new BlockItem(BELL_PEPPER_CROP.get(), p));
    public static final DeferredItem<Item> TOMATO_SEEDS = ITEMS.registerItem("seeds_tomato",
        p -> new BlockItem(TOMATO_CROP.get(), p));
    public static final DeferredItem<Item> CORN_SEEDS = ITEMS.registerItem("seeds_corn",
        p -> new BlockItem(CORN_CROP.get(), p));
    public static final DeferredItem<Item> CABBAGE_SEEDS = ITEMS.registerItem("seeds_cabbage",
        p -> new BlockItem(CABBAGE_CROP.get(), p));
    public static final DeferredItem<Item> BUTTERNUT_SQUASH_SEEDS = ITEMS.registerItem("seeds_butternut_squash",
        p -> new BlockItem(BUTTERNUT_SQUASH_CROP.get(), p));
    public static final DeferredItem<Item> CHICKPEA_SEEDS = ITEMS.registerItem("seeds_chickpea",
        p -> new BlockItem(CHICKPEA_CROP.get(), p));
    public static final DeferredItem<Item> SOY_BEAN_SEEDS = ITEMS.registerItem("seeds_soy_bean",
        p -> new BlockItem(SOY_BEAN_CROP.get(), p));
    public static final DeferredItem<Item> PEAS_SEEDS = ITEMS.registerItem("seeds_peas",
        p -> new BlockItem(PEAS_CROP.get(), p));
    public static final DeferredItem<Item> RICE_SEEDS = ITEMS.registerItem("seeds_rice",
        p -> new BlockItem(RICE_CROP.get(), p));
    public static final DeferredItem<Item> NETHER_PEPPER_SEEDS = ITEMS.registerItem("seeds_nether_pepper",
        p -> new BlockItem(NETHER_PEPPER_CROP.get(), p));

    // ==================== Produce Items (harvest drops) ====================

    public static final DeferredItem<Item> DURUM_ITEM = ITEMS.registerSimpleItem("durum", () -> new Item.Properties());
    public static final DeferredItem<Item> EGGPLANT_ITEM = ITEMS.registerSimpleItem("eggplant", () -> new Item.Properties());
    public static final DeferredItem<Item> GARLIC_ITEM = ITEMS.registerSimpleItem("garlic", () -> new Item.Properties());
    public static final DeferredItem<Item> ONION_ITEM = ITEMS.registerSimpleItem("onion", () -> new Item.Properties());
    public static final DeferredItem<Item> MINT_ITEM = ITEMS.registerSimpleItem("mint", () -> new Item.Properties());
    public static final DeferredItem<Item> BELL_PEPPER_ITEM = ITEMS.registerSimpleItem("bell_pepper", () -> new Item.Properties());
    public static final DeferredItem<Item> TOMATO_ITEM = ITEMS.registerSimpleItem("tomato", () -> new Item.Properties());
    public static final DeferredItem<Item> CORN_ITEM = ITEMS.registerSimpleItem("corn", () -> new Item.Properties());
    public static final DeferredItem<Item> CABBAGE_ITEM = ITEMS.registerSimpleItem("cabbage", () -> new Item.Properties());
    public static final DeferredItem<Item> BUTTERNUT_SQUASH_ITEM = ITEMS.registerSimpleItem("butternut_squash", () -> new Item.Properties());
    public static final DeferredItem<Item> CHICKPEA_ITEM = ITEMS.registerSimpleItem("chickpea", () -> new Item.Properties());
    public static final DeferredItem<Item> SOY_BEAN_ITEM = ITEMS.registerSimpleItem("soy_bean", () -> new Item.Properties());
    public static final DeferredItem<Item> PEAS_ITEM = ITEMS.registerSimpleItem("peas", () -> new Item.Properties());
    public static final DeferredItem<Item> RICE_ITEM = ITEMS.registerSimpleItem("rice", () -> new Item.Properties());
    public static final DeferredItem<Item> NETHER_PEPPER_ITEM = ITEMS.registerSimpleItem("nether_pepper", () -> new Item.Properties());

    // ==================== Farmland Block Items ====================

    public static final DeferredItem<BlockItem> COLONY_FARMLAND_ITEM = ITEMS.registerSimpleBlockItem("colony_farmland", COLONY_FARMLAND);
    public static final DeferredItem<BlockItem> FLOODED_FARMLAND_ITEM = ITEMS.registerSimpleBlockItem("flooded_farmland", FLOODED_FARMLAND);

    // ==================== Creative Tab ====================

    public static final Supplier<CreativeModeTab> COLONY_FARMING_TAB = CREATIVE_MODE_TABS.register("megamod_colony_farming_tab",
        () -> CreativeModeTab.builder()
            .title((Component) Component.literal((String) "MegaMod - Colony Farming"))
            .icon(() -> new ItemStack((ItemLike) Items.WHEAT_SEEDS))
            .displayItems((parameters, output) -> {
                // Farmland
                output.accept((ItemLike) COLONY_FARMLAND_ITEM.get());
                output.accept((ItemLike) FLOODED_FARMLAND_ITEM.get());
                // Seeds
                output.accept((ItemLike) DURUM_SEEDS.get());
                output.accept((ItemLike) EGGPLANT_SEEDS.get());
                output.accept((ItemLike) GARLIC_SEEDS.get());
                output.accept((ItemLike) ONION_SEEDS.get());
                output.accept((ItemLike) MINT_SEEDS.get());
                output.accept((ItemLike) BELL_PEPPER_SEEDS.get());
                output.accept((ItemLike) TOMATO_SEEDS.get());
                output.accept((ItemLike) CORN_SEEDS.get());
                output.accept((ItemLike) CABBAGE_SEEDS.get());
                output.accept((ItemLike) BUTTERNUT_SQUASH_SEEDS.get());
                output.accept((ItemLike) CHICKPEA_SEEDS.get());
                output.accept((ItemLike) SOY_BEAN_SEEDS.get());
                output.accept((ItemLike) PEAS_SEEDS.get());
                output.accept((ItemLike) RICE_SEEDS.get());
                output.accept((ItemLike) NETHER_PEPPER_SEEDS.get());
                // Produce
                output.accept((ItemLike) DURUM_ITEM.get());
                output.accept((ItemLike) EGGPLANT_ITEM.get());
                output.accept((ItemLike) GARLIC_ITEM.get());
                output.accept((ItemLike) ONION_ITEM.get());
                output.accept((ItemLike) MINT_ITEM.get());
                output.accept((ItemLike) BELL_PEPPER_ITEM.get());
                output.accept((ItemLike) TOMATO_ITEM.get());
                output.accept((ItemLike) CORN_ITEM.get());
                output.accept((ItemLike) CABBAGE_ITEM.get());
                output.accept((ItemLike) BUTTERNUT_SQUASH_ITEM.get());
                output.accept((ItemLike) CHICKPEA_ITEM.get());
                output.accept((ItemLike) SOY_BEAN_ITEM.get());
                output.accept((ItemLike) PEAS_ITEM.get());
                output.accept((ItemLike) RICE_ITEM.get());
                output.accept((ItemLike) NETHER_PEPPER_ITEM.get());
            }).build());

    // ==================== Init ====================

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
    }
}
