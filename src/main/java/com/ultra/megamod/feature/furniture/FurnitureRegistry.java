package com.ultra.megamod.feature.furniture;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class FurnitureRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create((ResourceKey) Registries.CREATIVE_MODE_TAB, "megamod");

    // =============================================
    // --- Office Furniture Blocks ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> OFFICE_BOARD_SMALL = BLOCKS.registerBlock("office_board_small", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_BOARD_SMALL_ITEM = ITEMS.registerSimpleBlockItem("office_board_small", OFFICE_BOARD_SMALL);

    public static final DeferredBlock<FurnitureBlock> OFFICE_BOARD_LARGE = BLOCKS.registerBlock("office_board_large", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_BOARD_LARGE_ITEM = ITEMS.registerSimpleBlockItem("office_board_large", OFFICE_BOARD_LARGE);

    public static final DeferredBlock<FurnitureBlock> OFFICE_CHAIR = BLOCKS.registerBlock("office_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("office_chair", OFFICE_CHAIR);

    public static final DeferredBlock<FurnitureBlock> OFFICE_CEO_CHAIR = BLOCKS.registerBlock("office_ceo_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_CEO_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("office_ceo_chair", OFFICE_CEO_CHAIR);

    public static final DeferredBlock<FurnitureBlock> OFFICE_TABLE = BLOCKS.registerBlock("office_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_TABLE_ITEM = ITEMS.registerSimpleBlockItem("office_table", OFFICE_TABLE);

    public static final DeferredBlock<FurnitureBlock> OFFICE_CEO_DESK = BLOCKS.registerBlock("office_ceo_desk", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_CEO_DESK_ITEM = ITEMS.registerSimpleBlockItem("office_ceo_desk", OFFICE_CEO_DESK);

    public static final DeferredBlock<FurnitureBlock> OFFICE_COMPUTER = BLOCKS.registerBlock("office_computer", ComputerFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_COMPUTER_ITEM = ITEMS.registerSimpleBlockItem("office_computer", OFFICE_COMPUTER);

    public static final DeferredBlock<FurnitureBlock> OFFICE_CUPBOARD = BLOCKS.registerBlock("office_cupboard", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_CUPBOARD_ITEM = ITEMS.registerSimpleBlockItem("office_cupboard", OFFICE_CUPBOARD);

    public static final DeferredBlock<FurnitureBlock> OFFICE_BOOKSHELF = BLOCKS.registerBlock("office_bookshelf", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_BOOKSHELF_ITEM = ITEMS.registerSimpleBlockItem("office_bookshelf", OFFICE_BOOKSHELF);

    public static final DeferredBlock<FurnitureBlock> OFFICE_BOOKSHELF_TALL = BLOCKS.registerBlock("office_bookshelf_tall", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_BOOKSHELF_TALL_ITEM = ITEMS.registerSimpleBlockItem("office_bookshelf_tall", OFFICE_BOOKSHELF_TALL);

    public static final DeferredBlock<FurnitureBlock> OFFICE_FILING_CABINET = BLOCKS.registerBlock("office_filing_cabinet", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_FILING_CABINET_ITEM = ITEMS.registerSimpleBlockItem("office_filing_cabinet", OFFICE_FILING_CABINET);

    public static final DeferredBlock<FurnitureBlock> OFFICE_FILE_RACK = BLOCKS.registerBlock("office_file_rack", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_FILE_RACK_ITEM = ITEMS.registerSimpleBlockItem("office_file_rack", OFFICE_FILE_RACK);

    public static final DeferredBlock<FurnitureBlock> OFFICE_LAMP = BLOCKS.registerBlock("office_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion().lightLevel(state -> 12));
    public static final DeferredItem<BlockItem> OFFICE_LAMP_ITEM = ITEMS.registerSimpleBlockItem("office_lamp", OFFICE_LAMP);

    public static final DeferredBlock<FurnitureBlock> OFFICE_POTTED_PLANT = BLOCKS.registerBlock("office_potted_plant", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_POTTED_PLANT_ITEM = ITEMS.registerSimpleBlockItem("office_potted_plant", OFFICE_POTTED_PLANT);

    public static final DeferredBlock<FurnitureBlock> OFFICE_PRINTER = BLOCKS.registerBlock("office_printer", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_PRINTER_ITEM = ITEMS.registerSimpleBlockItem("office_printer", OFFICE_PRINTER);

    public static final DeferredBlock<FurnitureBlock> OFFICE_PROJECTOR_SCREEN = BLOCKS.registerBlock("office_projector_screen", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_PROJECTOR_SCREEN_ITEM = ITEMS.registerSimpleBlockItem("office_projector_screen", OFFICE_PROJECTOR_SCREEN);

    public static final DeferredBlock<FurnitureBlock> OFFICE_PROJECTOR = BLOCKS.registerBlock("office_projector", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_PROJECTOR_ITEM = ITEMS.registerSimpleBlockItem("office_projector", OFFICE_PROJECTOR);

    public static final DeferredBlock<TrashBinBlock> OFFICE_RUBBISH_BIN = BLOCKS.registerBlock("office_rubbish_bin", TrashBinBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_RUBBISH_BIN_ITEM = ITEMS.registerSimpleBlockItem("office_rubbish_bin", OFFICE_RUBBISH_BIN);

    public static final DeferredBlock<FurnitureBlock> OFFICE_SOFA = BLOCKS.registerBlock("office_sofa", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_SOFA_ITEM = ITEMS.registerSimpleBlockItem("office_sofa", OFFICE_SOFA);

    public static final DeferredBlock<FurnitureBlock> OFFICE_SOFA_LARGE = BLOCKS.registerBlock("office_sofa_large", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_SOFA_LARGE_ITEM = ITEMS.registerSimpleBlockItem("office_sofa_large", OFFICE_SOFA_LARGE);

    public static final DeferredBlock<FurnitureBlock> OFFICE_CONFERENCE_TABLE = BLOCKS.registerBlock("office_conference_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> OFFICE_CONFERENCE_TABLE_ITEM = ITEMS.registerSimpleBlockItem("office_conference_table", OFFICE_CONFERENCE_TABLE);

    // =============================================
    // --- Vintage Furniture (20th Century V1) ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> VINTAGE_BED = BLOCKS.registerBlock("vintage_bed", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_BED_ITEM = ITEMS.registerSimpleBlockItem("vintage_bed", VINTAGE_BED);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_BIG_CUPBOARD = BLOCKS.registerBlock("vintage_big_cupboard", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_BIG_CUPBOARD_ITEM = ITEMS.registerSimpleBlockItem("vintage_big_cupboard", VINTAGE_BIG_CUPBOARD);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_BOOK_SHELF = BLOCKS.registerBlock("vintage_book_shelf", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_BOOK_SHELF_ITEM = ITEMS.registerSimpleBlockItem("vintage_book_shelf", VINTAGE_BOOK_SHELF);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_CARPET = BLOCKS.registerBlock("vintage_carpet", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_CARPET_ITEM = ITEMS.registerSimpleBlockItem("vintage_carpet", VINTAGE_CARPET);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_CARPET_ALT = BLOCKS.registerBlock("vintage_carpet_alt", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_CARPET_ALT_ITEM = ITEMS.registerSimpleBlockItem("vintage_carpet_alt", VINTAGE_CARPET_ALT);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_CHAIR = BLOCKS.registerBlock("vintage_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("vintage_chair", VINTAGE_CHAIR);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_CLOCK = BLOCKS.registerBlock("vintage_clock", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_CLOCK_ITEM = ITEMS.registerSimpleBlockItem("vintage_clock", VINTAGE_CLOCK);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_CUPBOARD = BLOCKS.registerBlock("vintage_cupboard", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_CUPBOARD_ITEM = ITEMS.registerSimpleBlockItem("vintage_cupboard", VINTAGE_CUPBOARD);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_DESK_LAMP = BLOCKS.registerBlock("vintage_desk_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> VINTAGE_DESK_LAMP_ITEM = ITEMS.registerSimpleBlockItem("vintage_desk_lamp", VINTAGE_DESK_LAMP);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_FIREPLACE = BLOCKS.registerBlock("vintage_fireplace", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f).noOcclusion().lightLevel(state -> 13));
    public static final DeferredItem<BlockItem> VINTAGE_FIREPLACE_ITEM = ITEMS.registerSimpleBlockItem("vintage_fireplace", VINTAGE_FIREPLACE);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_LEATHER_SOFA = BLOCKS.registerBlock("vintage_leather_sofa", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_GREEN).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_LEATHER_SOFA_ITEM = ITEMS.registerSimpleBlockItem("vintage_leather_sofa", VINTAGE_LEATHER_SOFA);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_MIRROR = BLOCKS.registerBlock("vintage_mirror", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_MIRROR_ITEM = ITEMS.registerSimpleBlockItem("vintage_mirror", VINTAGE_MIRROR);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_NIGHTSTAND = BLOCKS.registerBlock("vintage_nightstand", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_NIGHTSTAND_ITEM = ITEMS.registerSimpleBlockItem("vintage_nightstand", VINTAGE_NIGHTSTAND);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_PAINTING = BLOCKS.registerBlock("vintage_painting", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_PAINTING_ITEM = ITEMS.registerSimpleBlockItem("vintage_painting", VINTAGE_PAINTING);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_PIANO = BLOCKS.registerBlock("vintage_piano", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2.5f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_PIANO_ITEM = ITEMS.registerSimpleBlockItem("vintage_piano", VINTAGE_PIANO);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_RADIO = BLOCKS.registerBlock("vintage_radio", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_RADIO_ITEM = ITEMS.registerSimpleBlockItem("vintage_radio", VINTAGE_RADIO);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_SHOWCASE = BLOCKS.registerBlock("vintage_showcase", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_SHOWCASE_ITEM = ITEMS.registerSimpleBlockItem("vintage_showcase", VINTAGE_SHOWCASE);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_STANDING_LAMP = BLOCKS.registerBlock("vintage_standing_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion().lightLevel(state -> 12));
    public static final DeferredItem<BlockItem> VINTAGE_STANDING_LAMP_ITEM = ITEMS.registerSimpleBlockItem("vintage_standing_lamp", VINTAGE_STANDING_LAMP);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_TABLE = BLOCKS.registerBlock("vintage_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VINTAGE_TABLE_ITEM = ITEMS.registerSimpleBlockItem("vintage_table", VINTAGE_TABLE);

    public static final DeferredBlock<FurnitureBlock> VINTAGE_TABLETOP_LAMP = BLOCKS.registerBlock("vintage_tabletop_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> VINTAGE_TABLETOP_LAMP_ITEM = ITEMS.registerSimpleBlockItem("vintage_tabletop_lamp", VINTAGE_TABLETOP_LAMP);

    // =============================================
    // --- Classic Furniture (20th Century V2) ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> CLASSIC_CANDLE = BLOCKS.registerBlock("classic_candle", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.5f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> CLASSIC_CANDLE_ITEM = ITEMS.registerSimpleBlockItem("classic_candle", CLASSIC_CANDLE);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_CEILING_FAN = BLOCKS.registerBlock("classic_ceiling_fan", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_CEILING_FAN_ITEM = ITEMS.registerSimpleBlockItem("classic_ceiling_fan", CLASSIC_CEILING_FAN);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_CHAIR = BLOCKS.registerBlock("classic_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("classic_chair", CLASSIC_CHAIR);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_CLEANING_SET = BLOCKS.registerBlock("classic_cleaning_set", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_CLEANING_SET_ITEM = ITEMS.registerSimpleBlockItem("classic_cleaning_set", CLASSIC_CLEANING_SET);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_CURTAIN = BLOCKS.registerBlock("classic_curtain", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_CURTAIN_ITEM = ITEMS.registerSimpleBlockItem("classic_curtain", CLASSIC_CURTAIN);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_CURTAIN_RED = BLOCKS.registerBlock("classic_curtain_red", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_CURTAIN_RED_ITEM = ITEMS.registerSimpleBlockItem("classic_curtain_red", CLASSIC_CURTAIN_RED);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_DOOR = BLOCKS.registerBlock("classic_door", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_DOOR_ITEM = ITEMS.registerSimpleBlockItem("classic_door", CLASSIC_DOOR);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_FLOWER = BLOCKS.registerBlock("classic_flower", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_FLOWER_ITEM = ITEMS.registerSimpleBlockItem("classic_flower", CLASSIC_FLOWER);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_GOLDEN_TREE = BLOCKS.registerBlock("classic_golden_tree", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_GOLDEN_TREE_ITEM = ITEMS.registerSimpleBlockItem("classic_golden_tree", CLASSIC_GOLDEN_TREE);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_HARP = BLOCKS.registerBlock("classic_harp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_HARP_ITEM = ITEMS.registerSimpleBlockItem("classic_harp", CLASSIC_HARP);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_HAT_HANGER = BLOCKS.registerBlock("classic_hat_hanger", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_HAT_HANGER_ITEM = ITEMS.registerSimpleBlockItem("classic_hat_hanger", CLASSIC_HAT_HANGER);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_JAR = BLOCKS.registerBlock("classic_jar", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_JAR_ITEM = ITEMS.registerSimpleBlockItem("classic_jar", CLASSIC_JAR);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_LONG_TABLE = BLOCKS.registerBlock("classic_long_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_LONG_TABLE_ITEM = ITEMS.registerSimpleBlockItem("classic_long_table", CLASSIC_LONG_TABLE);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_PAINTING = BLOCKS.registerBlock("classic_painting", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_PAINTING_ITEM = ITEMS.registerSimpleBlockItem("classic_painting", CLASSIC_PAINTING);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_PHONE = BLOCKS.registerBlock("classic_phone", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_PHONE_ITEM = ITEMS.registerSimpleBlockItem("classic_phone", CLASSIC_PHONE);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_SHOWCASE_CORNER = BLOCKS.registerBlock("classic_showcase_corner", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_SHOWCASE_CORNER_ITEM = ITEMS.registerSimpleBlockItem("classic_showcase_corner", CLASSIC_SHOWCASE_CORNER);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_TABLE = BLOCKS.registerBlock("classic_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CLASSIC_TABLE_ITEM = ITEMS.registerSimpleBlockItem("classic_table", CLASSIC_TABLE);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_TABLE_LAMP = BLOCKS.registerBlock("classic_table_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> CLASSIC_TABLE_LAMP_ITEM = ITEMS.registerSimpleBlockItem("classic_table_lamp", CLASSIC_TABLE_LAMP);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_WALL_LAMP = BLOCKS.registerBlock("classic_wall_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion().lightLevel(state -> 12));
    public static final DeferredItem<BlockItem> CLASSIC_WALL_LAMP_ITEM = ITEMS.registerSimpleBlockItem("classic_wall_lamp", CLASSIC_WALL_LAMP);

    public static final DeferredBlock<FurnitureBlock> CLASSIC_WALL_LAMP_DOUBLE = BLOCKS.registerBlock("classic_wall_lamp_double", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion().lightLevel(state -> 14));
    public static final DeferredItem<BlockItem> CLASSIC_WALL_LAMP_DOUBLE_ITEM = ITEMS.registerSimpleBlockItem("classic_wall_lamp_double", CLASSIC_WALL_LAMP_DOUBLE);

    // =============================================
    // --- Dungeon Decor ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> DUNGEON_CAGE = BLOCKS.registerBlock("dungeon_cage", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_CAGE_ITEM = ITEMS.registerSimpleBlockItem("dungeon_cage", DUNGEON_CAGE);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_CAGE_WITH_BONE = BLOCKS.registerBlock("dungeon_cage_with_bone", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_CAGE_WITH_BONE_ITEM = ITEMS.registerSimpleBlockItem("dungeon_cage_with_bone", DUNGEON_CAGE_WITH_BONE);

    public static final DeferredBlock<DungeonChestBlock> DUNGEON_CHEST_DECOR = BLOCKS.registerBlock("dungeon_chest_decor", DungeonChestBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_CHEST_DECOR_ITEM = ITEMS.registerSimpleBlockItem("dungeon_chest_decor", DUNGEON_CHEST_DECOR);
    public static final Supplier<BlockEntityType<DungeonChestBlockEntity>> DUNGEON_CHEST_BE = BLOCK_ENTITIES.register("dungeon_chest_be",
        () -> new BlockEntityType<>(DungeonChestBlockEntity::new, DUNGEON_CHEST_DECOR.get()));

    public static final DeferredBlock<FurnitureBlock> DUNGEON_CHEST_OPEN = BLOCKS.registerBlock("dungeon_chest_open", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_CHEST_OPEN_ITEM = ITEMS.registerSimpleBlockItem("dungeon_chest_open", DUNGEON_CHEST_OPEN);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_GOLDBAR = BLOCKS.registerBlock("dungeon_goldbar", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_GOLDBAR_ITEM = ITEMS.registerSimpleBlockItem("dungeon_goldbar", DUNGEON_GOLDBAR);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_GOLDBAR_COIN = BLOCKS.registerBlock("dungeon_goldbar_coin", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_GOLDBAR_COIN_ITEM = ITEMS.registerSimpleBlockItem("dungeon_goldbar_coin", DUNGEON_GOLDBAR_COIN);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_GOLDBARS = BLOCKS.registerBlock("dungeon_goldbars", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_GOLDBARS_ITEM = ITEMS.registerSimpleBlockItem("dungeon_goldbars", DUNGEON_GOLDBARS);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_HEADS = BLOCKS.registerBlock("dungeon_heads", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_HEADS_ITEM = ITEMS.registerSimpleBlockItem("dungeon_heads", DUNGEON_HEADS);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_SWORD_BONE = BLOCKS.registerBlock("dungeon_sword_bone", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_SWORD_BONE_ITEM = ITEMS.registerSimpleBlockItem("dungeon_sword_bone", DUNGEON_SWORD_BONE);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_VASE = BLOCKS.registerBlock("dungeon_vase", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_VASE_ITEM = ITEMS.registerSimpleBlockItem("dungeon_vase", DUNGEON_VASE);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_HANG_FLAG = BLOCKS.registerBlock("dungeon_hang_flag", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_HANG_FLAG_ITEM = ITEMS.registerSimpleBlockItem("dungeon_hang_flag", DUNGEON_HANG_FLAG);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_FLAG_BONE = BLOCKS.registerBlock("dungeon_flag_bone", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_FLAG_BONE_ITEM = ITEMS.registerSimpleBlockItem("dungeon_flag_bone", DUNGEON_FLAG_BONE);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_SKELETON = BLOCKS.registerBlock("dungeon_skeleton", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_SKELETON_ITEM = ITEMS.registerSimpleBlockItem("dungeon_skeleton", DUNGEON_SKELETON);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_SKELETON_HEAD = BLOCKS.registerBlock("dungeon_skeleton_head", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_SKELETON_HEAD_ITEM = ITEMS.registerSimpleBlockItem("dungeon_skeleton_head", DUNGEON_SKELETON_HEAD);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_SKELETON_SLEEP = BLOCKS.registerBlock("dungeon_skeleton_sleep", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_SKELETON_SLEEP_ITEM = ITEMS.registerSimpleBlockItem("dungeon_skeleton_sleep", DUNGEON_SKELETON_SLEEP);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_TABLE_DECOR = BLOCKS.registerBlock("dungeon_table_decor", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_TABLE_DECOR_ITEM = ITEMS.registerSimpleBlockItem("dungeon_table_decor", DUNGEON_TABLE_DECOR);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_TABLE_LONG = BLOCKS.registerBlock("dungeon_table_long", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_TABLE_LONG_ITEM = ITEMS.registerSimpleBlockItem("dungeon_table_long", DUNGEON_TABLE_LONG);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_TORCH_DECOR = BLOCKS.registerBlock("dungeon_torch_decor", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f).noOcclusion().lightLevel(state -> 14));
    public static final DeferredItem<BlockItem> DUNGEON_TORCH_DECOR_ITEM = ITEMS.registerSimpleBlockItem("dungeon_torch_decor", DUNGEON_TORCH_DECOR);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_WEAPONSTAND = BLOCKS.registerBlock("dungeon_weaponstand", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_WEAPONSTAND_ITEM = ITEMS.registerSimpleBlockItem("dungeon_weaponstand", DUNGEON_WEAPONSTAND);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_WOOD_BARREL = BLOCKS.registerBlock("dungeon_wood_barrel", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_WOOD_BARREL_ITEM = ITEMS.registerSimpleBlockItem("dungeon_wood_barrel", DUNGEON_WOOD_BARREL);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_WOOD_BOX = BLOCKS.registerBlock("dungeon_wood_box", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_WOOD_BOX_ITEM = ITEMS.registerSimpleBlockItem("dungeon_wood_box", DUNGEON_WOOD_BOX);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_WOOD_CHAIR = BLOCKS.registerBlock("dungeon_wood_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_WOOD_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("dungeon_wood_chair", DUNGEON_WOOD_CHAIR);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_CHAIR_BONE = BLOCKS.registerBlock("dungeon_chair_bone", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_CHAIR_BONE_ITEM = ITEMS.registerSimpleBlockItem("dungeon_chair_bone", DUNGEON_CHAIR_BONE);

    public static final DeferredBlock<FurnitureBlock> DUNGEON_CHAIR_REST = BLOCKS.registerBlock("dungeon_chair_rest", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_CHAIR_REST_ITEM = ITEMS.registerSimpleBlockItem("dungeon_chair_rest", DUNGEON_CHAIR_REST);

    // =============================================
    // --- Coffee Shop Furniture ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> COFFEE_BLENDER = BLOCKS.registerBlock("coffee_blender", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_BLENDER_ITEM = ITEMS.registerSimpleBlockItem("coffee_blender", COFFEE_BLENDER);

    public static final DeferredBlock<FurnitureBlock> COFFEE_BOARD_1 = BLOCKS.registerBlock("coffee_board_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_BOARD_1_ITEM = ITEMS.registerSimpleBlockItem("coffee_board_1", COFFEE_BOARD_1);

    public static final DeferredBlock<FurnitureBlock> COFFEE_BOARD_2 = BLOCKS.registerBlock("coffee_board_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_BOARD_2_ITEM = ITEMS.registerSimpleBlockItem("coffee_board_2", COFFEE_BOARD_2);

    public static final DeferredBlock<FurnitureBlock> COFFEE_BREAD_SHOWCASE = BLOCKS.registerBlock("coffee_bread_showcase", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_BREAD_SHOWCASE_ITEM = ITEMS.registerSimpleBlockItem("coffee_bread_showcase", COFFEE_BREAD_SHOWCASE);

    public static final DeferredBlock<FurnitureBlock> COFFEE_BREADCOFFEE_1 = BLOCKS.registerBlock("coffee_breadcoffee_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_BREADCOFFEE_1_ITEM = ITEMS.registerSimpleBlockItem("coffee_breadcoffee_1", COFFEE_BREADCOFFEE_1);

    public static final DeferredBlock<FurnitureBlock> COFFEE_BREADCOFFEE_2 = BLOCKS.registerBlock("coffee_breadcoffee_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_BREADCOFFEE_2_ITEM = ITEMS.registerSimpleBlockItem("coffee_breadcoffee_2", COFFEE_BREADCOFFEE_2);

    public static final DeferredBlock<FurnitureBlock> COFFEE_CASHIER_TABLE = BLOCKS.registerBlock("coffee_cashier_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_CASHIER_TABLE_ITEM = ITEMS.registerSimpleBlockItem("coffee_cashier_table", COFFEE_CASHIER_TABLE);

    public static final DeferredBlock<FurnitureBlock> COFFEE_CHAIR = BLOCKS.registerBlock("coffee_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("coffee_chair", COFFEE_CHAIR);

    public static final DeferredBlock<FurnitureBlock> COFFEE_COFFEE_MACHINE = BLOCKS.registerBlock("coffee_coffee_machine", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_COFFEE_MACHINE_ITEM = ITEMS.registerSimpleBlockItem("coffee_coffee_machine", COFFEE_COFFEE_MACHINE);

    public static final DeferredBlock<FurnitureBlock> COFFEE_COUNTER_TABLE = BLOCKS.registerBlock("coffee_counter_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_COUNTER_TABLE_ITEM = ITEMS.registerSimpleBlockItem("coffee_counter_table", COFFEE_COUNTER_TABLE);

    public static final DeferredBlock<FurnitureBlock> COFFEE_GLASS_HANGER = BLOCKS.registerBlock("coffee_glass_hanger", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_GLASS_HANGER_ITEM = ITEMS.registerSimpleBlockItem("coffee_glass_hanger", COFFEE_GLASS_HANGER);

    public static final DeferredBlock<FurnitureBlock> COFFEE_HANGING_LAMP = BLOCKS.registerBlock("coffee_hanging_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion().lightLevel(state -> 12));
    public static final DeferredItem<BlockItem> COFFEE_HANGING_LAMP_ITEM = ITEMS.registerSimpleBlockItem("coffee_hanging_lamp", COFFEE_HANGING_LAMP);

    public static final DeferredBlock<FurnitureBlock> COFFEE_PICTURE_WALLPAPER = BLOCKS.registerBlock("coffee_picture_wallpaper", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_PICTURE_WALLPAPER_ITEM = ITEMS.registerSimpleBlockItem("coffee_picture_wallpaper", COFFEE_PICTURE_WALLPAPER);

    public static final DeferredBlock<FurnitureBlock> COFFEE_PLANT_POT = BLOCKS.registerBlock("coffee_plant_pot", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_PLANT_POT_ITEM = ITEMS.registerSimpleBlockItem("coffee_plant_pot", COFFEE_PLANT_POT);

    public static final DeferredBlock<FurnitureBlock> COFFEE_SHELF_1 = BLOCKS.registerBlock("coffee_shelf_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_SHELF_1_ITEM = ITEMS.registerSimpleBlockItem("coffee_shelf_1", COFFEE_SHELF_1);

    public static final DeferredBlock<FurnitureBlock> COFFEE_SHELF_2 = BLOCKS.registerBlock("coffee_shelf_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_SHELF_2_ITEM = ITEMS.registerSimpleBlockItem("coffee_shelf_2", COFFEE_SHELF_2);

    public static final DeferredBlock<FurnitureBlock> COFFEE_SHOP_SIGN = BLOCKS.registerBlock("coffee_shop_sign", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_SHOP_SIGN_ITEM = ITEMS.registerSimpleBlockItem("coffee_shop_sign", COFFEE_SHOP_SIGN);

    public static final DeferredBlock<FurnitureBlock> COFFEE_SIGN = BLOCKS.registerBlock("coffee_sign", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_SIGN_ITEM = ITEMS.registerSimpleBlockItem("coffee_sign", COFFEE_SIGN);

    public static final DeferredBlock<FurnitureBlock> COFFEE_SOFA_1 = BLOCKS.registerBlock("coffee_sofa_1", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_SOFA_1_ITEM = ITEMS.registerSimpleBlockItem("coffee_sofa_1", COFFEE_SOFA_1);

    public static final DeferredBlock<FurnitureBlock> COFFEE_SOFA_2 = BLOCKS.registerBlock("coffee_sofa_2", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_SOFA_2_ITEM = ITEMS.registerSimpleBlockItem("coffee_sofa_2", COFFEE_SOFA_2);

    public static final DeferredBlock<FurnitureBlock> COFFEE_TABLE_1 = BLOCKS.registerBlock("coffee_table_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_TABLE_1_ITEM = ITEMS.registerSimpleBlockItem("coffee_table_1", COFFEE_TABLE_1);

    public static final DeferredBlock<FurnitureBlock> COFFEE_TABLE_2 = BLOCKS.registerBlock("coffee_table_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> COFFEE_TABLE_2_ITEM = ITEMS.registerSimpleBlockItem("coffee_table_2", COFFEE_TABLE_2);

    // =============================================
    // --- Medieval Market V1 Furniture ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> MARKET_BARREL = BLOCKS.registerBlock("market_barrel", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_BARREL_ITEM = ITEMS.registerSimpleBlockItem("market_barrel", MARKET_BARREL);

    public static final DeferredBlock<FurnitureBlock> MARKET_BARRELBERRY = BLOCKS.registerBlock("market_barrelberry", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_BARRELBERRY_ITEM = ITEMS.registerSimpleBlockItem("market_barrelberry", MARKET_BARRELBERRY);

    public static final DeferredBlock<FurnitureBlock> MARKET_BARRELSWORD = BLOCKS.registerBlock("market_barrelsword", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_BARRELSWORD_ITEM = ITEMS.registerSimpleBlockItem("market_barrelsword", MARKET_BARRELSWORD);

    public static final DeferredBlock<FurnitureBlock> MARKET_BOARDSIGN = BLOCKS.registerBlock("market_boardsign", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_BOARDSIGN_ITEM = ITEMS.registerSimpleBlockItem("market_boardsign", MARKET_BOARDSIGN);

    public static final DeferredBlock<FurnitureBlock> MARKET_CAMPFIRE_1 = BLOCKS.registerBlock("market_campfire_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion().lightLevel(state -> 13));
    public static final DeferredItem<BlockItem> MARKET_CAMPFIRE_1_ITEM = ITEMS.registerSimpleBlockItem("market_campfire_1", MARKET_CAMPFIRE_1);

    public static final DeferredBlock<FurnitureBlock> MARKET_CAMPFIRE_2 = BLOCKS.registerBlock("market_campfire_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion().lightLevel(state -> 13));
    public static final DeferredItem<BlockItem> MARKET_CAMPFIRE_2_ITEM = ITEMS.registerSimpleBlockItem("market_campfire_2", MARKET_CAMPFIRE_2);

    public static final DeferredBlock<FurnitureBlock> MARKET_CARGO_1 = BLOCKS.registerBlock("market_cargo_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_CARGO_1_ITEM = ITEMS.registerSimpleBlockItem("market_cargo_1", MARKET_CARGO_1);

    public static final DeferredBlock<FurnitureBlock> MARKET_CARGO_2 = BLOCKS.registerBlock("market_cargo_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_CARGO_2_ITEM = ITEMS.registerSimpleBlockItem("market_cargo_2", MARKET_CARGO_2);

    public static final DeferredBlock<FurnitureBlock> MARKET_CART_1 = BLOCKS.registerBlock("market_cart_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_CART_1_ITEM = ITEMS.registerSimpleBlockItem("market_cart_1", MARKET_CART_1);

    public static final DeferredBlock<FurnitureBlock> MARKET_CART_2 = BLOCKS.registerBlock("market_cart_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_CART_2_ITEM = ITEMS.registerSimpleBlockItem("market_cart_2", MARKET_CART_2);

    public static final DeferredBlock<FurnitureBlock> MARKET_CHAIR = BLOCKS.registerBlock("market_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("market_chair", MARKET_CHAIR);

    public static final DeferredBlock<FurnitureBlock> MARKET_CRATE = BLOCKS.registerBlock("market_crate", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_CRATE_ITEM = ITEMS.registerSimpleBlockItem("market_crate", MARKET_CRATE);

    public static final DeferredBlock<FurnitureBlock> MARKET_EARTHENWARE = BLOCKS.registerBlock("market_earthenware", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_EARTHENWARE_ITEM = ITEMS.registerSimpleBlockItem("market_earthenware", MARKET_EARTHENWARE);

    public static final DeferredBlock<FurnitureBlock> MARKET_FISHTUB = BLOCKS.registerBlock("market_fishtub", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_FISHTUB_ITEM = ITEMS.registerSimpleBlockItem("market_fishtub", MARKET_FISHTUB);

    public static final DeferredBlock<FurnitureBlock> MARKET_MARKETSTALL_1 = BLOCKS.registerBlock("market_marketstall_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_MARKETSTALL_1_ITEM = ITEMS.registerSimpleBlockItem("market_marketstall_1", MARKET_MARKETSTALL_1);

    public static final DeferredBlock<FurnitureBlock> MARKET_MARKETSTALL_2 = BLOCKS.registerBlock("market_marketstall_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_MARKETSTALL_2_ITEM = ITEMS.registerSimpleBlockItem("market_marketstall_2", MARKET_MARKETSTALL_2);

    public static final DeferredBlock<FurnitureBlock> MARKET_MARKETSTALL_3 = BLOCKS.registerBlock("market_marketstall_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_MARKETSTALL_3_ITEM = ITEMS.registerSimpleBlockItem("market_marketstall_3", MARKET_MARKETSTALL_3);

    public static final DeferredBlock<FurnitureBlock> MARKET_MARKETSTALL_4 = BLOCKS.registerBlock("market_marketstall_4", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_MARKETSTALL_4_ITEM = ITEMS.registerSimpleBlockItem("market_marketstall_4", MARKET_MARKETSTALL_4);

    public static final DeferredBlock<FurnitureBlock> MARKET_POLE = BLOCKS.registerBlock("market_pole", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_POLE_ITEM = ITEMS.registerSimpleBlockItem("market_pole", MARKET_POLE);

    public static final DeferredBlock<FurnitureBlock> MARKET_SHARK = BLOCKS.registerBlock("market_shark", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_SHARK_ITEM = ITEMS.registerSimpleBlockItem("market_shark", MARKET_SHARK);

    public static final DeferredBlock<FurnitureBlock> MARKET_SHELF_1 = BLOCKS.registerBlock("market_shelf_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_SHELF_1_ITEM = ITEMS.registerSimpleBlockItem("market_shelf_1", MARKET_SHELF_1);

    public static final DeferredBlock<FurnitureBlock> MARKET_SHELF_2 = BLOCKS.registerBlock("market_shelf_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_SHELF_2_ITEM = ITEMS.registerSimpleBlockItem("market_shelf_2", MARKET_SHELF_2);

    public static final DeferredBlock<FurnitureBlock> MARKET_TABLE_1 = BLOCKS.registerBlock("market_table_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_TABLE_1_ITEM = ITEMS.registerSimpleBlockItem("market_table_1", MARKET_TABLE_1);

    public static final DeferredBlock<FurnitureBlock> MARKET_TABLE_2 = BLOCKS.registerBlock("market_table_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_TABLE_2_ITEM = ITEMS.registerSimpleBlockItem("market_table_2", MARKET_TABLE_2);

    public static final DeferredBlock<FurnitureBlock> MARKET_WATERWELL = BLOCKS.registerBlock("market_waterwell", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET_WATERWELL_ITEM = ITEMS.registerSimpleBlockItem("market_waterwell", MARKET_WATERWELL);

    // =============================================
    // --- Medieval Market V2 Furniture ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> MARKET2_BARREL_1 = BLOCKS.registerBlock("market2_barrel_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_BARREL_1_ITEM = ITEMS.registerSimpleBlockItem("market2_barrel_1", MARKET2_BARREL_1);

    public static final DeferredBlock<FurnitureBlock> MARKET2_BARREL_2 = BLOCKS.registerBlock("market2_barrel_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_BARREL_2_ITEM = ITEMS.registerSimpleBlockItem("market2_barrel_2", MARKET2_BARREL_2);

    public static final DeferredBlock<FurnitureBlock> MARKET2_BOARD_1 = BLOCKS.registerBlock("market2_board_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_BOARD_1_ITEM = ITEMS.registerSimpleBlockItem("market2_board_1", MARKET2_BOARD_1);

    public static final DeferredBlock<FurnitureBlock> MARKET2_BOARDSIGN_1 = BLOCKS.registerBlock("market2_boardsign_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_BOARDSIGN_1_ITEM = ITEMS.registerSimpleBlockItem("market2_boardsign_1", MARKET2_BOARDSIGN_1);

    public static final DeferredBlock<FurnitureBlock> MARKET2_BOX_1 = BLOCKS.registerBlock("market2_box_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_BOX_1_ITEM = ITEMS.registerSimpleBlockItem("market2_box_1", MARKET2_BOX_1);

    public static final DeferredBlock<FurnitureBlock> MARKET2_CARGO_1 = BLOCKS.registerBlock("market2_cargo_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_CARGO_1_ITEM = ITEMS.registerSimpleBlockItem("market2_cargo_1", MARKET2_CARGO_1);

    public static final DeferredBlock<FurnitureBlock> MARKET2_CARGO_2 = BLOCKS.registerBlock("market2_cargo_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_CARGO_2_ITEM = ITEMS.registerSimpleBlockItem("market2_cargo_2", MARKET2_CARGO_2);

    public static final DeferredBlock<FurnitureBlock> MARKET2_CHAIR_1 = BLOCKS.registerBlock("market2_chair_1", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_CHAIR_1_ITEM = ITEMS.registerSimpleBlockItem("market2_chair_1", MARKET2_CHAIR_1);

    public static final DeferredBlock<FurnitureBlock> MARKET2_CHAIR_2 = BLOCKS.registerBlock("market2_chair_2", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_CHAIR_2_ITEM = ITEMS.registerSimpleBlockItem("market2_chair_2", MARKET2_CHAIR_2);

    public static final DeferredBlock<FurnitureBlock> MARKET2_CRATE_1 = BLOCKS.registerBlock("market2_crate_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_CRATE_1_ITEM = ITEMS.registerSimpleBlockItem("market2_crate_1", MARKET2_CRATE_1);

    public static final DeferredBlock<FurnitureBlock> MARKET2_CRATE_2 = BLOCKS.registerBlock("market2_crate_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_CRATE_2_ITEM = ITEMS.registerSimpleBlockItem("market2_crate_2", MARKET2_CRATE_2);

    public static final DeferredBlock<FurnitureBlock> MARKET2_CRATE_3 = BLOCKS.registerBlock("market2_crate_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_CRATE_3_ITEM = ITEMS.registerSimpleBlockItem("market2_crate_3", MARKET2_CRATE_3);

    public static final DeferredBlock<FurnitureBlock> MARKET2_CRATE_4 = BLOCKS.registerBlock("market2_crate_4", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_CRATE_4_ITEM = ITEMS.registerSimpleBlockItem("market2_crate_4", MARKET2_CRATE_4);

    public static final DeferredBlock<FurnitureBlock> MARKET2_MARKETSTALL_1 = BLOCKS.registerBlock("market2_marketstall_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_MARKETSTALL_1_ITEM = ITEMS.registerSimpleBlockItem("market2_marketstall_1", MARKET2_MARKETSTALL_1);

    public static final DeferredBlock<FurnitureBlock> MARKET2_MARKETSTALL_2 = BLOCKS.registerBlock("market2_marketstall_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_MARKETSTALL_2_ITEM = ITEMS.registerSimpleBlockItem("market2_marketstall_2", MARKET2_MARKETSTALL_2);

    public static final DeferredBlock<FurnitureBlock> MARKET2_MARKETSTALL_3 = BLOCKS.registerBlock("market2_marketstall_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_MARKETSTALL_3_ITEM = ITEMS.registerSimpleBlockItem("market2_marketstall_3", MARKET2_MARKETSTALL_3);

    public static final DeferredBlock<FurnitureBlock> MARKET2_MARKETSTALL_4 = BLOCKS.registerBlock("market2_marketstall_4", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_MARKETSTALL_4_ITEM = ITEMS.registerSimpleBlockItem("market2_marketstall_4", MARKET2_MARKETSTALL_4);

    public static final DeferredBlock<FurnitureBlock> MARKET2_SHELF_1 = BLOCKS.registerBlock("market2_shelf_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_SHELF_1_ITEM = ITEMS.registerSimpleBlockItem("market2_shelf_1", MARKET2_SHELF_1);

    public static final DeferredBlock<FurnitureBlock> MARKET2_TABLE_1 = BLOCKS.registerBlock("market2_table_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_TABLE_1_ITEM = ITEMS.registerSimpleBlockItem("market2_table_1", MARKET2_TABLE_1);

    public static final DeferredBlock<FurnitureBlock> MARKET2_TABLE_2 = BLOCKS.registerBlock("market2_table_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MARKET2_TABLE_2_ITEM = ITEMS.registerSimpleBlockItem("market2_table_2", MARKET2_TABLE_2);

    // =============================================
    // --- Casino Decoration V1 Furniture ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> CASINO_BARRIER = BLOCKS.registerBlock("casino_barrier", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_BARRIER_ITEM = ITEMS.registerSimpleBlockItem("casino_barrier", CASINO_BARRIER);

    public static final DeferredBlock<FurnitureBlock> CASINO_BUSH = BLOCKS.registerBlock("casino_bush", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_BUSH_ITEM = ITEMS.registerSimpleBlockItem("casino_bush", CASINO_BUSH);

    public static final DeferredBlock<FurnitureBlock> CASINO_CARD_AND_TOKEN = BLOCKS.registerBlock("casino_card_and_token", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_CARD_AND_TOKEN_ITEM = ITEMS.registerSimpleBlockItem("casino_card_and_token", CASINO_CARD_AND_TOKEN);

    public static final DeferredBlock<FurnitureBlock> CASINO_CHAIR_BAR_BROWN = BLOCKS.registerBlock("casino_chair_bar_brown", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_CHAIR_BAR_BROWN_ITEM = ITEMS.registerSimpleBlockItem("casino_chair_bar_brown", CASINO_CHAIR_BAR_BROWN);

    public static final DeferredBlock<FurnitureBlock> CASINO_CHAIR_BAR_RED = BLOCKS.registerBlock("casino_chair_bar_red", props -> new com.ultra.megamod.feature.casino.CasinoChairBlock(props), () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_CHAIR_BAR_RED_ITEM = ITEMS.registerSimpleBlockItem("casino_chair_bar_red", CASINO_CHAIR_BAR_RED);

    public static final DeferredBlock<FurnitureBlock> CASINO_CHAIR_BROWN = BLOCKS.registerBlock("casino_chair_brown", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_CHAIR_BROWN_ITEM = ITEMS.registerSimpleBlockItem("casino_chair_brown", CASINO_CHAIR_BROWN);

    public static final DeferredBlock<FurnitureBlock> CASINO_CHIPS = BLOCKS.registerBlock("casino_chips", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion().noCollision());
    public static final DeferredItem<BlockItem> CASINO_CHIPS_ITEM = ITEMS.registerSimpleBlockItem("casino_chips", CASINO_CHIPS);

    public static final DeferredBlock<FurnitureBlock> CASINO_GAME_BIGWIN = BLOCKS.registerBlock("casino_game_bigwin", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_GAME_BIGWIN_ITEM = ITEMS.registerSimpleBlockItem("casino_game_bigwin", CASINO_GAME_BIGWIN);

    public static final DeferredBlock<FurnitureBlock> CASINO_GAME_SLOT = BLOCKS.registerBlock("casino_game_slot", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_GAME_SLOT_ITEM = ITEMS.registerSimpleBlockItem("casino_game_slot", CASINO_GAME_SLOT);

    public static final DeferredBlock<FurnitureBlock> CASINO_MONITOR = BLOCKS.registerBlock("casino_monitor", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_MONITOR_ITEM = ITEMS.registerSimpleBlockItem("casino_monitor", CASINO_MONITOR);

    public static final DeferredBlock<FurnitureBlock> CASINO_POT_TREE = BLOCKS.registerBlock("casino_pot_tree", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_POT_TREE_ITEM = ITEMS.registerSimpleBlockItem("casino_pot_tree", CASINO_POT_TREE);

    public static final DeferredBlock<FurnitureBlock> CASINO_SOFA_RED = BLOCKS.registerBlock("casino_sofa_red", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_SOFA_RED_ITEM = ITEMS.registerSimpleBlockItem("casino_sofa_red", CASINO_SOFA_RED);

    public static final DeferredBlock<FurnitureBlock> CASINO_SOFA_RED_NO_REST = BLOCKS.registerBlock("casino_sofa_red_no_rest", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_SOFA_RED_NO_REST_ITEM = ITEMS.registerSimpleBlockItem("casino_sofa_red_no_rest", CASINO_SOFA_RED_NO_REST);

    public static final DeferredBlock<FurnitureBlock> CASINO_SOFA_RED_SINGLE = BLOCKS.registerBlock("casino_sofa_red_single", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_SOFA_RED_SINGLE_ITEM = ITEMS.registerSimpleBlockItem("casino_sofa_red_single", CASINO_SOFA_RED_SINGLE);

    public static final DeferredBlock<FurnitureBlock> CASINO_TABLE_BILLIARDS = BLOCKS.registerBlock("casino_table_billiards", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_TABLE_BILLIARDS_ITEM = ITEMS.registerSimpleBlockItem("casino_table_billiards", CASINO_TABLE_BILLIARDS);

    public static final DeferredBlock<FurnitureBlock> CASINO_TABLE_BILLIARDS_STICK_STAND = BLOCKS.registerBlock("casino_table_billiards_stick_stand", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_TABLE_BILLIARDS_STICK_STAND_ITEM = ITEMS.registerSimpleBlockItem("casino_table_billiards_stick_stand", CASINO_TABLE_BILLIARDS_STICK_STAND);

    public static final DeferredBlock<FurnitureBlock> CASINO_TABLE_BLACKJACK = BLOCKS.registerBlock("casino_table_blackjack", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_TABLE_BLACKJACK_ITEM = ITEMS.registerSimpleBlockItem("casino_table_blackjack", CASINO_TABLE_BLACKJACK);

    public static final DeferredBlock<FurnitureBlock> CASINO_TABLE_BLANK = BLOCKS.registerBlock("casino_table_blank", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_TABLE_BLANK_ITEM = ITEMS.registerSimpleBlockItem("casino_table_blank", CASINO_TABLE_BLANK);

    public static final DeferredBlock<FurnitureBlock> CASINO_TABLE_CRAPS = BLOCKS.registerBlock("casino_table_craps", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_TABLE_CRAPS_ITEM = ITEMS.registerSimpleBlockItem("casino_table_craps", CASINO_TABLE_CRAPS);

    public static final DeferredBlock<FurnitureBlock> CASINO_TABLE_ROULETTE = BLOCKS.registerBlock("casino_table_roulette", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_TABLE_ROULETTE_ITEM = ITEMS.registerSimpleBlockItem("casino_table_roulette", CASINO_TABLE_ROULETTE);

    public static final DeferredBlock<FurnitureBlock> CASINO_TABLE_WOOD = BLOCKS.registerBlock("casino_table_wood", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_TABLE_WOOD_ITEM = ITEMS.registerSimpleBlockItem("casino_table_wood", CASINO_TABLE_WOOD);

    public static final DeferredBlock<FurnitureBlock> CASINO_VENDING_MACHINE = BLOCKS.registerBlock("casino_vending_machine", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO_VENDING_MACHINE_ITEM = ITEMS.registerSimpleBlockItem("casino_vending_machine", CASINO_VENDING_MACHINE);

    // =============================================
    // --- Casino Decoration V2 Furniture ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> CASINO2_ASHTRAY = BLOCKS.registerBlock("casino2_ashtray", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_ASHTRAY_ITEM = ITEMS.registerSimpleBlockItem("casino2_ashtray", CASINO2_ASHTRAY);

    public static final DeferredBlock<FurnitureBlock> CASINO2_BACCARAT_MACHINE = BLOCKS.registerBlock("casino2_baccarat_machine", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_BACCARAT_MACHINE_ITEM = ITEMS.registerSimpleBlockItem("casino2_baccarat_machine", CASINO2_BACCARAT_MACHINE);

    public static final DeferredBlock<FurnitureBlock> CASINO2_BUFFALO_SLOT_MACHINE = BLOCKS.registerBlock("casino2_buffalo_slot_machine", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_BUFFALO_SLOT_MACHINE_ITEM = ITEMS.registerSimpleBlockItem("casino2_buffalo_slot_machine", CASINO2_BUFFALO_SLOT_MACHINE);

    public static final DeferredBlock<FurnitureBlock> CASINO2_CHAIR_RED = BLOCKS.registerBlock("casino2_chair_red", props -> new com.ultra.megamod.feature.casino.CasinoChairBlock(props), () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_CHAIR_RED_ITEM = ITEMS.registerSimpleBlockItem("casino2_chair_red", CASINO2_CHAIR_RED);

    public static final DeferredBlock<FurnitureBlock> CASINO2_CHAIR_YELLOW = BLOCKS.registerBlock("casino2_chair_yellow", props -> new com.ultra.megamod.feature.casino.CasinoChairBlock(props), () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_CHAIR_YELLOW_ITEM = ITEMS.registerSimpleBlockItem("casino2_chair_yellow", CASINO2_CHAIR_YELLOW);

    public static final DeferredBlock<FurnitureBlock> CASINO2_CHIP_SET = BLOCKS.registerBlock("casino2_chip_set", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion().noCollision());
    public static final DeferredItem<BlockItem> CASINO2_CHIP_SET_ITEM = ITEMS.registerSimpleBlockItem("casino2_chip_set", CASINO2_CHIP_SET);

    public static final DeferredBlock<FurnitureBlock> CASINO2_DOGGIE_CASH = BLOCKS.registerBlock("casino2_doggie_cash", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_DOGGIE_CASH_ITEM = ITEMS.registerSimpleBlockItem("casino2_doggie_cash", CASINO2_DOGGIE_CASH);

    public static final DeferredBlock<FurnitureBlock> CASINO2_GAMBLING_GAME_MACHINE = BLOCKS.registerBlock("casino2_gambling_game_machine", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_GAMBLING_GAME_MACHINE_ITEM = ITEMS.registerSimpleBlockItem("casino2_gambling_game_machine", CASINO2_GAMBLING_GAME_MACHINE);

    public static final DeferredBlock<FurnitureBlock> CASINO2_LADY_LED_SIGN = BLOCKS.registerBlock("casino2_lady_led_sign", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> CASINO2_LADY_LED_SIGN_ITEM = ITEMS.registerSimpleBlockItem("casino2_lady_led_sign", CASINO2_LADY_LED_SIGN);

    public static final DeferredBlock<FurnitureBlock> CASINO2_LED_SIGN = BLOCKS.registerBlock("casino2_led_sign", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> CASINO2_LED_SIGN_ITEM = ITEMS.registerSimpleBlockItem("casino2_led_sign", CASINO2_LED_SIGN);

    public static final DeferredBlock<FurnitureBlock> CASINO2_MADONNA_GAMBLING_MACHINE = BLOCKS.registerBlock("casino2_madonna_gambling_machine", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_MADONNA_GAMBLING_MACHINE_ITEM = ITEMS.registerSimpleBlockItem("casino2_madonna_gambling_machine", CASINO2_MADONNA_GAMBLING_MACHINE);

    public static final DeferredBlock<FurnitureBlock> CASINO2_POKER_SIGN = BLOCKS.registerBlock("casino2_poker_sign", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_POKER_SIGN_ITEM = ITEMS.registerSimpleBlockItem("casino2_poker_sign", CASINO2_POKER_SIGN);

    public static final DeferredBlock<FurnitureBlock> CASINO2_RED_ARMREST_CHAIR = BLOCKS.registerBlock("casino2_red_armrest_chair", props -> new com.ultra.megamod.feature.casino.CasinoChairBlock(props), () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_RED_ARMREST_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("casino2_red_armrest_chair", CASINO2_RED_ARMREST_CHAIR);

    public static final DeferredBlock<FurnitureBlock> CASINO2_ROULETTE = BLOCKS.registerBlock("casino2_roulette", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_ROULETTE_ITEM = ITEMS.registerSimpleBlockItem("casino2_roulette", CASINO2_ROULETTE);

    public static final DeferredBlock<FurnitureBlock> CASINO2_STACK_CARD = BLOCKS.registerBlock("casino2_stack_card", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_STACK_CARD_ITEM = ITEMS.registerSimpleBlockItem("casino2_stack_card", CASINO2_STACK_CARD);

    public static final DeferredBlock<FurnitureBlock> CASINO2_TABLE = BLOCKS.registerBlock("casino2_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_TABLE_ITEM = ITEMS.registerSimpleBlockItem("casino2_table", CASINO2_TABLE);

    public static final DeferredBlock<FurnitureBlock> CASINO2_VENEZIA_SLOT = BLOCKS.registerBlock("casino2_venezia_slot", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_VENEZIA_SLOT_ITEM = ITEMS.registerSimpleBlockItem("casino2_venezia_slot", CASINO2_VENEZIA_SLOT);

    public static final DeferredBlock<FurnitureBlock> CASINO2_WHEEL_MACHINE = BLOCKS.registerBlock("casino2_wheel_machine", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_WHEEL_MACHINE_ITEM = ITEMS.registerSimpleBlockItem("casino2_wheel_machine", CASINO2_WHEEL_MACHINE);

    public static final DeferredBlock<FurnitureBlock> CASINO2_WOMAN_PAINTING = BLOCKS.registerBlock("casino2_woman_painting", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_WOMAN_PAINTING_ITEM = ITEMS.registerSimpleBlockItem("casino2_woman_painting", CASINO2_WOMAN_PAINTING);

    public static final DeferredBlock<FurnitureBlock> CASINO2_YELLOW_ARMREST_CHAIR = BLOCKS.registerBlock("casino2_yellow_armrest_chair", props -> new com.ultra.megamod.feature.casino.CasinoChairBlock(props), () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CASINO2_YELLOW_ARMREST_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("casino2_yellow_armrest_chair", CASINO2_YELLOW_ARMREST_CHAIR);

    // =============================================
    // --- Farmer Decorations Furniture ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> FARMER_BEER = BLOCKS.registerBlock("farmer_beer", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_BEER_ITEM = ITEMS.registerSimpleBlockItem("farmer_beer", FARMER_BEER);

    public static final DeferredBlock<FurnitureBlock> FARMER_CORN = BLOCKS.registerBlock("farmer_corn", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_CORN_ITEM = ITEMS.registerSimpleBlockItem("farmer_corn", FARMER_CORN);

    public static final DeferredBlock<FurnitureBlock> FARMER_CORN_BASKET = BLOCKS.registerBlock("farmer_corn_basket", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_CORN_BASKET_ITEM = ITEMS.registerSimpleBlockItem("farmer_corn_basket", FARMER_CORN_BASKET);

    public static final DeferredBlock<FurnitureBlock> FARMER_DOOR = BLOCKS.registerBlock("farmer_door", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_DOOR_ITEM = ITEMS.registerSimpleBlockItem("farmer_door", FARMER_DOOR);

    public static final DeferredBlock<FurnitureBlock> FARMER_FENCE = BLOCKS.registerBlock("farmer_fence", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_FENCE_ITEM = ITEMS.registerSimpleBlockItem("farmer_fence", FARMER_FENCE);

    public static final DeferredBlock<FurnitureBlock> FARMER_FENCE_CORNER = BLOCKS.registerBlock("farmer_fence_corner", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_FENCE_CORNER_ITEM = ITEMS.registerSimpleBlockItem("farmer_fence_corner", FARMER_FENCE_CORNER);

    public static final DeferredBlock<FurnitureBlock> FARMER_FIREWOOD = BLOCKS.registerBlock("farmer_firewood", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_FIREWOOD_ITEM = ITEMS.registerSimpleBlockItem("farmer_firewood", FARMER_FIREWOOD);

    public static final DeferredBlock<FurnitureBlock> FARMER_FLOWER = BLOCKS.registerBlock("farmer_flower", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_FLOWER_ITEM = ITEMS.registerSimpleBlockItem("farmer_flower", FARMER_FLOWER);

    public static final DeferredBlock<FurnitureBlock> FARMER_HAY_WITH_SPADE = BLOCKS.registerBlock("farmer_hay_with_spade", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_HAY_WITH_SPADE_ITEM = ITEMS.registerSimpleBlockItem("farmer_hay_with_spade", FARMER_HAY_WITH_SPADE);

    public static final DeferredBlock<FurnitureBlock> FARMER_LOG_AXE = BLOCKS.registerBlock("farmer_log_axe", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_LOG_AXE_ITEM = ITEMS.registerSimpleBlockItem("farmer_log_axe", FARMER_LOG_AXE);

    public static final DeferredBlock<FurnitureBlock> FARMER_MILK_TANK = BLOCKS.registerBlock("farmer_milk_tank", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_MILK_TANK_ITEM = ITEMS.registerSimpleBlockItem("farmer_milk_tank", FARMER_MILK_TANK);

    public static final DeferredBlock<FurnitureBlock> FARMER_PLATE_RICE = BLOCKS.registerBlock("farmer_plate_rice", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_PLATE_RICE_ITEM = ITEMS.registerSimpleBlockItem("farmer_plate_rice", FARMER_PLATE_RICE);

    public static final DeferredBlock<FurnitureBlock> FARMER_POND = BLOCKS.registerBlock("farmer_pond", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WATER).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_POND_ITEM = ITEMS.registerSimpleBlockItem("farmer_pond", FARMER_POND);

    public static final DeferredBlock<FurnitureBlock> FARMER_RICE_BASKET = BLOCKS.registerBlock("farmer_rice_basket", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_RICE_BASKET_ITEM = ITEMS.registerSimpleBlockItem("farmer_rice_basket", FARMER_RICE_BASKET);

    public static final DeferredBlock<FurnitureBlock> FARMER_SCARE_CROW = BLOCKS.registerBlock("farmer_scare_crow", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_SCARE_CROW_ITEM = ITEMS.registerSimpleBlockItem("farmer_scare_crow", FARMER_SCARE_CROW);

    public static final DeferredBlock<FurnitureBlock> FARMER_STONE = BLOCKS.registerBlock("farmer_stone", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_STONE_ITEM = ITEMS.registerSimpleBlockItem("farmer_stone", FARMER_STONE);

    public static final DeferredBlock<FurnitureBlock> FARMER_TABLE = BLOCKS.registerBlock("farmer_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_TABLE_ITEM = ITEMS.registerSimpleBlockItem("farmer_table", FARMER_TABLE);

    public static final DeferredBlock<FurnitureBlock> FARMER_TABLE_SAWING = BLOCKS.registerBlock("farmer_table_sawing", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_TABLE_SAWING_ITEM = ITEMS.registerSimpleBlockItem("farmer_table_sawing", FARMER_TABLE_SAWING);

    public static final DeferredBlock<FurnitureBlock> FARMER_TOOLS_STAND = BLOCKS.registerBlock("farmer_tools_stand", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_TOOLS_STAND_ITEM = ITEMS.registerSimpleBlockItem("farmer_tools_stand", FARMER_TOOLS_STAND);

    public static final DeferredBlock<FurnitureBlock> FARMER_WHEELBARROW = BLOCKS.registerBlock("farmer_wheelbarrow", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> FARMER_WHEELBARROW_ITEM = ITEMS.registerSimpleBlockItem("farmer_wheelbarrow", FARMER_WHEELBARROW);

    // === Caribbean Vacation Furniture ===
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_AQUATRIKES = BLOCKS.registerBlock("caribbean_aquatrikes", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_AQUATRIKES_ITEM = ITEMS.registerSimpleBlockItem("caribbean_aquatrikes", CARIBBEAN_AQUATRIKES);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_ARM_CHAIR = BLOCKS.registerBlock("caribbean_arm_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_ARM_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("caribbean_arm_chair", CARIBBEAN_ARM_CHAIR);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_BEACH_HANGING_CHAIR = BLOCKS.registerBlock("caribbean_beach_hanging_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_BEACH_HANGING_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("caribbean_beach_hanging_chair", CARIBBEAN_BEACH_HANGING_CHAIR);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_BEACH_SIGN = BLOCKS.registerBlock("caribbean_beach_sign", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_BEACH_SIGN_ITEM = ITEMS.registerSimpleBlockItem("caribbean_beach_sign", CARIBBEAN_BEACH_SIGN);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_BED = BLOCKS.registerBlock("caribbean_bed", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_BED_ITEM = ITEMS.registerSimpleBlockItem("caribbean_bed", CARIBBEAN_BED);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_CARPET = BLOCKS.registerBlock("caribbean_carpet", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_CARPET_ITEM = ITEMS.registerSimpleBlockItem("caribbean_carpet", CARIBBEAN_CARPET);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_COCONUT_SET = BLOCKS.registerBlock("caribbean_coconut_set", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_COCONUT_SET_ITEM = ITEMS.registerSimpleBlockItem("caribbean_coconut_set", CARIBBEAN_COCONUT_SET);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_COUNTER_CHAIR = BLOCKS.registerBlock("caribbean_counter_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_COUNTER_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("caribbean_counter_chair", CARIBBEAN_COUNTER_CHAIR);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_HAND_WASHING = BLOCKS.registerBlock("caribbean_hand_washing", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_HAND_WASHING_ITEM = ITEMS.registerSimpleBlockItem("caribbean_hand_washing", CARIBBEAN_HAND_WASHING);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_HANGING_LAMP = BLOCKS.registerBlock("caribbean_hanging_lamp_carribean_style", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion().lightLevel(state -> 12));
    public static final DeferredItem<BlockItem> CARIBBEAN_HANGING_LAMP_ITEM = ITEMS.registerSimpleBlockItem("caribbean_hanging_lamp_carribean_style", CARIBBEAN_HANGING_LAMP);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_HANGING_SEAT = BLOCKS.registerBlock("caribbean_hanging_seat", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_HANGING_SEAT_ITEM = ITEMS.registerSimpleBlockItem("caribbean_hanging_seat", CARIBBEAN_HANGING_SEAT);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_ICECREAMBOX_BIKE = BLOCKS.registerBlock("caribbean_icecreambox_bike", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_ICECREAMBOX_BIKE_ITEM = ITEMS.registerSimpleBlockItem("caribbean_icecreambox_bike", CARIBBEAN_ICECREAMBOX_BIKE);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_PALM_TREE = BLOCKS.registerBlock("caribbean_palm_tree", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_PALM_TREE_ITEM = ITEMS.registerSimpleBlockItem("caribbean_palm_tree", CARIBBEAN_PALM_TREE);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_PLANT_DESK = BLOCKS.registerBlock("caribbean_plant_desk", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_PLANT_DESK_ITEM = ITEMS.registerSimpleBlockItem("caribbean_plant_desk", CARIBBEAN_PLANT_DESK);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_SHIP_WHEEL_CLOCK = BLOCKS.registerBlock("caribbean_ship_wheel_clock", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_SHIP_WHEEL_CLOCK_ITEM = ITEMS.registerSimpleBlockItem("caribbean_ship_wheel_clock", CARIBBEAN_SHIP_WHEEL_CLOCK);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_SHIP_WHEEL_TABLE = BLOCKS.registerBlock("caribbean_ship_wheel_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_SHIP_WHEEL_TABLE_ITEM = ITEMS.registerSimpleBlockItem("caribbean_ship_wheel_table", CARIBBEAN_SHIP_WHEEL_TABLE);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_SUNSET_PAINTING = BLOCKS.registerBlock("caribbean_sunset_painting", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_SUNSET_PAINTING_ITEM = ITEMS.registerSimpleBlockItem("caribbean_sunset_painting", CARIBBEAN_SUNSET_PAINTING);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_SWIM_RING = BLOCKS.registerBlock("caribbean_swim_ring_decoration", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_SWIM_RING_ITEM = ITEMS.registerSimpleBlockItem("caribbean_swim_ring_decoration", CARIBBEAN_SWIM_RING);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_UMBRELLA = BLOCKS.registerBlock("caribbean_umbrella", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_UMBRELLA_ITEM = ITEMS.registerSimpleBlockItem("caribbean_umbrella", CARIBBEAN_UMBRELLA);
    public static final DeferredBlock<FurnitureBlock> CARIBBEAN_WELCOME_FLOWER = BLOCKS.registerBlock("caribbean_welcome_flower", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CARIBBEAN_WELCOME_FLOWER_ITEM = ITEMS.registerSimpleBlockItem("caribbean_welcome_flower", CARIBBEAN_WELCOME_FLOWER);

    // === Master Bedroom Furniture ===
    public static final DeferredBlock<FurnitureBlock> BEDROOM_BASKET = BLOCKS.registerBlock("bedroom_basket", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_BASKET_ITEM = ITEMS.registerSimpleBlockItem("bedroom_basket", BEDROOM_BASKET);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_BED = BLOCKS.registerBlock("bedroom_bed", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_BED_ITEM = ITEMS.registerSimpleBlockItem("bedroom_bed", BEDROOM_BED);
    public static final DeferredBlock<TrashBinBlock> BEDROOM_BIN = BLOCKS.registerBlock("bedroom_bin", TrashBinBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_BIN_ITEM = ITEMS.registerSimpleBlockItem("bedroom_bin", BEDROOM_BIN);

    public static final Supplier<BlockEntityType<TrashBinBlockEntity>> TRASH_BIN_BE = BLOCK_ENTITIES.register("trash_bin_be",
        () -> new BlockEntityType<>(TrashBinBlockEntity::new, OFFICE_RUBBISH_BIN.get(), BEDROOM_BIN.get()));

    public static final DeferredBlock<FurnitureBlock> BEDROOM_BOOKSHELF = BLOCKS.registerBlock("bedroom_bookshelf", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_BOOKSHELF_ITEM = ITEMS.registerSimpleBlockItem("bedroom_bookshelf", BEDROOM_BOOKSHELF);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_CARPET = BLOCKS.registerBlock("bedroom_carpet", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_CARPET_ITEM = ITEMS.registerSimpleBlockItem("bedroom_carpet", BEDROOM_CARPET);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_CHAIR_DRESSING_TABLE = BLOCKS.registerBlock("bedroom_chair_dressing_table", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_CHAIR_DRESSING_TABLE_ITEM = ITEMS.registerSimpleBlockItem("bedroom_chair_dressing_table", BEDROOM_CHAIR_DRESSING_TABLE);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_DESKTOP_PHOTO_FRAME = BLOCKS.registerBlock("bedroom_desktop_photo_frame", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_DESKTOP_PHOTO_FRAME_ITEM = ITEMS.registerSimpleBlockItem("bedroom_desktop_photo_frame", BEDROOM_DESKTOP_PHOTO_FRAME);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_DRAWER_CABINET = BLOCKS.registerBlock("bedroom_drawer_cabinet", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_DRAWER_CABINET_ITEM = ITEMS.registerSimpleBlockItem("bedroom_drawer_cabinet", BEDROOM_DRAWER_CABINET);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_DRAWER_LAMP = BLOCKS.registerBlock("bedroom_drawer_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> BEDROOM_DRAWER_LAMP_ITEM = ITEMS.registerSimpleBlockItem("bedroom_drawer_lamp", BEDROOM_DRAWER_LAMP);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_DRESSING_TABLE = BLOCKS.registerBlock("bedroom_dressing_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_DRESSING_TABLE_ITEM = ITEMS.registerSimpleBlockItem("bedroom_dressing_table", BEDROOM_DRESSING_TABLE);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_LAMP = BLOCKS.registerBlock("bedroom_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion().lightLevel(state -> 12));
    public static final DeferredItem<BlockItem> BEDROOM_LAMP_ITEM = ITEMS.registerSimpleBlockItem("bedroom_lamp", BEDROOM_LAMP);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_MIRROR = BLOCKS.registerBlock("bedroom_mirror", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_MIRROR_ITEM = ITEMS.registerSimpleBlockItem("bedroom_mirror", BEDROOM_MIRROR);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_PICTURE_FRAME = BLOCKS.registerBlock("bedroom_picture_frame", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_PICTURE_FRAME_ITEM = ITEMS.registerSimpleBlockItem("bedroom_picture_frame", BEDROOM_PICTURE_FRAME);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_SHELF = BLOCKS.registerBlock("bedroom_shelf", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_SHELF_ITEM = ITEMS.registerSimpleBlockItem("bedroom_shelf", BEDROOM_SHELF);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_SMALL_TABLE = BLOCKS.registerBlock("bedroom_small_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_SMALL_TABLE_ITEM = ITEMS.registerSimpleBlockItem("bedroom_small_table", BEDROOM_SMALL_TABLE);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_SOFA_BENCH = BLOCKS.registerBlock("bedroom_sofa_bench", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_SOFA_BENCH_ITEM = ITEMS.registerSimpleBlockItem("bedroom_sofa_bench", BEDROOM_SOFA_BENCH);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_SOFA_CHAIR = BLOCKS.registerBlock("bedroom_sofa_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_SOFA_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("bedroom_sofa_chair", BEDROOM_SOFA_CHAIR);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_TV_STAND = BLOCKS.registerBlock("bedroom_tv_stand", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_TV_STAND_ITEM = ITEMS.registerSimpleBlockItem("bedroom_tv_stand", BEDROOM_TV_STAND);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_VASE = BLOCKS.registerBlock("bedroom_vase", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_VASE_ITEM = ITEMS.registerSimpleBlockItem("bedroom_vase", BEDROOM_VASE);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_WARDROBE = BLOCKS.registerBlock("bedroom_wardrobe", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_WARDROBE_ITEM = ITEMS.registerSimpleBlockItem("bedroom_wardrobe", BEDROOM_WARDROBE);
    public static final DeferredBlock<FurnitureBlock> BEDROOM_WORK_TABLE = BLOCKS.registerBlock("bedroom_work_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BEDROOM_WORK_TABLE_ITEM = ITEMS.registerSimpleBlockItem("bedroom_work_table", BEDROOM_WORK_TABLE);

    // === Modern Furniture ===
    public static final DeferredBlock<FurnitureBlock> MODERN_BED = BLOCKS.registerBlock("modern_bed", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_BED_ITEM = ITEMS.registerSimpleBlockItem("modern_bed", MODERN_BED);
    public static final DeferredBlock<FurnitureBlock> MODERN_BOARD = BLOCKS.registerBlock("modern_board", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_BOARD_ITEM = ITEMS.registerSimpleBlockItem("modern_board", MODERN_BOARD);
    public static final DeferredBlock<FurnitureBlock> MODERN_CABINET = BLOCKS.registerBlock("modern_cabinet", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_CABINET_ITEM = ITEMS.registerSimpleBlockItem("modern_cabinet", MODERN_CABINET);
    public static final DeferredBlock<FurnitureBlock> MODERN_CARPET = BLOCKS.registerBlock("modern_carpet", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_CARPET_ITEM = ITEMS.registerSimpleBlockItem("modern_carpet", MODERN_CARPET);
    public static final DeferredBlock<FurnitureBlock> MODERN_CHAIR = BLOCKS.registerBlock("modern_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("modern_chair", MODERN_CHAIR);
    public static final DeferredBlock<FurnitureBlock> MODERN_COMPUTER_CHAIR = BLOCKS.registerBlock("modern_computerchair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_COMPUTER_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("modern_computerchair", MODERN_COMPUTER_CHAIR);
    public static final DeferredBlock<FurnitureBlock> MODERN_COMPUTER_DESK = BLOCKS.registerBlock("modern_computerdesk", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_COMPUTER_DESK_ITEM = ITEMS.registerSimpleBlockItem("modern_computerdesk", MODERN_COMPUTER_DESK);
    public static final DeferredBlock<FurnitureBlock> MODERN_LAMP = BLOCKS.registerBlock("modern_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion().lightLevel(state -> 12));
    public static final DeferredItem<BlockItem> MODERN_LAMP_ITEM = ITEMS.registerSimpleBlockItem("modern_lamp", MODERN_LAMP);
    public static final DeferredBlock<FurnitureBlock> MODERN_MACBOOK = BLOCKS.registerBlock("modern_macbook", ComputerFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion().lightLevel(state -> 8));
    public static final DeferredItem<BlockItem> MODERN_MACBOOK_ITEM = ITEMS.registerSimpleBlockItem("modern_macbook", MODERN_MACBOOK);
    public static final DeferredBlock<FurnitureBlock> MODERN_PICTURE = BLOCKS.registerBlock("modern_picture", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_PICTURE_ITEM = ITEMS.registerSimpleBlockItem("modern_picture", MODERN_PICTURE);
    public static final DeferredBlock<FurnitureBlock> MODERN_PLANTPOT = BLOCKS.registerBlock("modern_plantpot", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_PLANTPOT_ITEM = ITEMS.registerSimpleBlockItem("modern_plantpot", MODERN_PLANTPOT);
    public static final DeferredBlock<FurnitureBlock> MODERN_SHELF = BLOCKS.registerBlock("modern_shelf", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_SHELF_ITEM = ITEMS.registerSimpleBlockItem("modern_shelf", MODERN_SHELF);
    public static final DeferredBlock<FurnitureBlock> MODERN_SOFA_01 = BLOCKS.registerBlock("modern_sofa_01", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_SOFA_01_ITEM = ITEMS.registerSimpleBlockItem("modern_sofa_01", MODERN_SOFA_01);
    public static final DeferredBlock<FurnitureBlock> MODERN_SOFA_02 = BLOCKS.registerBlock("modern_sofa_02", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_SOFA_02_ITEM = ITEMS.registerSimpleBlockItem("modern_sofa_02", MODERN_SOFA_02);
    public static final DeferredBlock<FurnitureBlock> MODERN_TABLE_01 = BLOCKS.registerBlock("modern_table_01", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_TABLE_01_ITEM = ITEMS.registerSimpleBlockItem("modern_table_01", MODERN_TABLE_01);
    public static final DeferredBlock<FurnitureBlock> MODERN_TABLE_02 = BLOCKS.registerBlock("modern_table_02", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_TABLE_02_ITEM = ITEMS.registerSimpleBlockItem("modern_table_02", MODERN_TABLE_02);
    public static final DeferredBlock<FurnitureBlock> MODERN_TABLE_03 = BLOCKS.registerBlock("modern_table_03", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_TABLE_03_ITEM = ITEMS.registerSimpleBlockItem("modern_table_03", MODERN_TABLE_03);
    public static final DeferredBlock<FurnitureBlock> MODERN_TV = BLOCKS.registerBlock("modern_tv", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5f).noOcclusion().lightLevel(state -> 8));
    public static final DeferredItem<BlockItem> MODERN_TV_ITEM = ITEMS.registerSimpleBlockItem("modern_tv", MODERN_TV);
    public static final DeferredBlock<FurnitureBlock> MODERN_WARDROBE_01 = BLOCKS.registerBlock("modern_wardrobe_01", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_WARDROBE_01_ITEM = ITEMS.registerSimpleBlockItem("modern_wardrobe_01", MODERN_WARDROBE_01);
    public static final DeferredBlock<FurnitureBlock> MODERN_WARDROBE_02 = BLOCKS.registerBlock("modern_wardrobe_02", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> MODERN_WARDROBE_02_ITEM = ITEMS.registerSimpleBlockItem("modern_wardrobe_02", MODERN_WARDROBE_02);

    // =============================================
    // --- Dungeon Tavern Furniture ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> BL_BAR_STOOL = BLOCKS.registerBlock("bl_bar_stool", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_BAR_STOOL_ITEM = ITEMS.registerSimpleBlockItem("bl_bar_stool", BL_BAR_STOOL);
    public static final DeferredBlock<FurnitureBlock> BL_BARREL_TABLE = BLOCKS.registerBlock("bl_barrel_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_BARREL_TABLE_ITEM = ITEMS.registerSimpleBlockItem("bl_barrel_table", BL_BARREL_TABLE);
    public static final DeferredBlock<FurnitureBlock> BL_BARREL_TABLE_2 = BLOCKS.registerBlock("bl_barrel_table_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_BARREL_TABLE_2_ITEM = ITEMS.registerSimpleBlockItem("bl_barrel_table_2", BL_BARREL_TABLE_2);
    public static final DeferredBlock<FurnitureBlock> BL_BOTTLE_WINE_1 = BLOCKS.registerBlock("bl_bottle_wine_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_BOTTLE_WINE_1_ITEM = ITEMS.registerSimpleBlockItem("bl_bottle_wine_1", BL_BOTTLE_WINE_1);
    public static final DeferredBlock<FurnitureBlock> BL_BOTTLE_WINE_2 = BLOCKS.registerBlock("bl_bottle_wine_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_BOTTLE_WINE_2_ITEM = ITEMS.registerSimpleBlockItem("bl_bottle_wine_2", BL_BOTTLE_WINE_2);
    public static final DeferredBlock<FurnitureBlock> BL_BOTTLE_WINE_3 = BLOCKS.registerBlock("bl_bottle_wine_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_BOTTLE_WINE_3_ITEM = ITEMS.registerSimpleBlockItem("bl_bottle_wine_3", BL_BOTTLE_WINE_3);
    public static final DeferredBlock<FurnitureBlock> BL_BOTTLE_WINE_STACK = BLOCKS.registerBlock("bl_bottle_wine_stack", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_BOTTLE_WINE_STACK_ITEM = ITEMS.registerSimpleBlockItem("bl_bottle_wine_stack", BL_BOTTLE_WINE_STACK);
    public static final DeferredBlock<FurnitureBlock> BL_GLASS_BEER = BLOCKS.registerBlock("bl_glass_beer", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_GLASS_BEER_ITEM = ITEMS.registerSimpleBlockItem("bl_glass_beer", BL_GLASS_BEER);
    public static final DeferredBlock<FurnitureBlock> BL_GLASS_WINE = BLOCKS.registerBlock("bl_glass_wine", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_GLASS_WINE_ITEM = ITEMS.registerSimpleBlockItem("bl_glass_wine", BL_GLASS_WINE);
    public static final DeferredBlock<FurnitureBlock> BL_TABLE_LARGE = BLOCKS.registerBlock("bl_table_large", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_TABLE_LARGE_ITEM = ITEMS.registerSimpleBlockItem("bl_table_large", BL_TABLE_LARGE);
    public static final DeferredBlock<FurnitureBlock> BL_TABLE_LARGE_2 = BLOCKS.registerBlock("bl_table_large_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_TABLE_LARGE_2_ITEM = ITEMS.registerSimpleBlockItem("bl_table_large_2", BL_TABLE_LARGE_2);
    public static final DeferredBlock<FurnitureBlock> BL_TAVERN_BENCH = BLOCKS.registerBlock("bl_tavern_bench", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_TAVERN_BENCH_ITEM = ITEMS.registerSimpleBlockItem("bl_tavern_bench", BL_TAVERN_BENCH);
    public static final DeferredBlock<FurnitureBlock> BL_TAVERN_CABINET_1 = BLOCKS.registerBlock("bl_tavern_cabinet_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_TAVERN_CABINET_1_ITEM = ITEMS.registerSimpleBlockItem("bl_tavern_cabinet_1", BL_TAVERN_CABINET_1);
    public static final DeferredBlock<FurnitureBlock> BL_TAVERN_CABINET_2 = BLOCKS.registerBlock("bl_tavern_cabinet_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_TAVERN_CABINET_2_ITEM = ITEMS.registerSimpleBlockItem("bl_tavern_cabinet_2", BL_TAVERN_CABINET_2);
    public static final DeferredBlock<FurnitureBlock> BL_TAVERN_COUNTER = BLOCKS.registerBlock("bl_tavern_counter", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_TAVERN_COUNTER_ITEM = ITEMS.registerSimpleBlockItem("bl_tavern_counter", BL_TAVERN_COUNTER);
    public static final DeferredBlock<FurnitureBlock> BL_WALL_SHELF = BLOCKS.registerBlock("bl_wall_shelf", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BL_WALL_SHELF_ITEM = ITEMS.registerSimpleBlockItem("bl_wall_shelf", BL_WALL_SHELF);

    // =============================================
    // --- Bank Furniture ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> BANK_ATM = BLOCKS.registerBlock("bank_atm", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_ATM_ITEM = ITEMS.registerSimpleBlockItem("bank_atm", BANK_ATM);
    public static final DeferredBlock<FurnitureBlock> BANK_BARRIER = BLOCKS.registerBlock("bank_barrier", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_BARRIER_ITEM = ITEMS.registerSimpleBlockItem("bank_barrier", BANK_BARRIER);
    public static final DeferredBlock<FurnitureBlock> BANK_CAMERA = BLOCKS.registerBlock("bank_camera", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_CAMERA_ITEM = ITEMS.registerSimpleBlockItem("bank_camera", BANK_CAMERA);
    public static final DeferredBlock<FurnitureBlock> BANK_CASE = BLOCKS.registerBlock("bank_case", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_CASE_ITEM = ITEMS.registerSimpleBlockItem("bank_case", BANK_CASE);
    public static final DeferredBlock<FurnitureBlock> BANK_CHAIR = BLOCKS.registerBlock("bank_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("bank_chair", BANK_CHAIR);
    public static final DeferredBlock<FurnitureBlock> BANK_CHAIR_1 = BLOCKS.registerBlock("bank_chair_1", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_CHAIR_1_ITEM = ITEMS.registerSimpleBlockItem("bank_chair_1", BANK_CHAIR_1);
    public static final DeferredBlock<FurnitureBlock> BANK_COMPUTER = BLOCKS.registerBlock("bank_computer", ComputerFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_COMPUTER_ITEM = ITEMS.registerSimpleBlockItem("bank_computer", BANK_COMPUTER);
    public static final DeferredBlock<FurnitureBlock> BANK_DOCUMENT = BLOCKS.registerBlock("bank_document", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_DOCUMENT_ITEM = ITEMS.registerSimpleBlockItem("bank_document", BANK_DOCUMENT);
    public static final DeferredBlock<FurnitureBlock> BANK_GLASS = BLOCKS.registerBlock("bank_glass", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_GLASS_ITEM = ITEMS.registerSimpleBlockItem("bank_glass", BANK_GLASS);
    public static final DeferredBlock<FurnitureBlock> BANK_GREEK_BALANCE = BLOCKS.registerBlock("bank_greek_balance", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_GREEK_BALANCE_ITEM = ITEMS.registerSimpleBlockItem("bank_greek_balance", BANK_GREEK_BALANCE);
    public static final DeferredBlock<FurnitureBlock> BANK_LOCKER = BLOCKS.registerBlock("bank_locker", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_LOCKER_ITEM = ITEMS.registerSimpleBlockItem("bank_locker", BANK_LOCKER);
    public static final DeferredBlock<FurnitureBlock> BANK_LOCKER_OPEN = BLOCKS.registerBlock("bank_locker_open", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_LOCKER_OPEN_ITEM = ITEMS.registerSimpleBlockItem("bank_locker_open", BANK_LOCKER_OPEN);
    public static final DeferredBlock<FurnitureBlock> BANK_MONEY = BLOCKS.registerBlock("bank_money", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_MONEY_ITEM = ITEMS.registerSimpleBlockItem("bank_money", BANK_MONEY);
    public static final DeferredBlock<FurnitureBlock> BANK_MONEY_1 = BLOCKS.registerBlock("bank_money_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_MONEY_1_ITEM = ITEMS.registerSimpleBlockItem("bank_money_1", BANK_MONEY_1);
    public static final DeferredBlock<FurnitureBlock> BANK_MONEY_CASE = BLOCKS.registerBlock("bank_money_case", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_MONEY_CASE_ITEM = ITEMS.registerSimpleBlockItem("bank_money_case", BANK_MONEY_CASE);
    public static final DeferredBlock<FurnitureBlock> BANK_POT = BLOCKS.registerBlock("bank_pot", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_POT_ITEM = ITEMS.registerSimpleBlockItem("bank_pot", BANK_POT);
    public static final DeferredBlock<FurnitureBlock> BANK_SAFE_DOOR = BLOCKS.registerBlock("bank_safe_door", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_SAFE_DOOR_ITEM = ITEMS.registerSimpleBlockItem("bank_safe_door", BANK_SAFE_DOOR);
    public static final DeferredBlock<FurnitureBlock> BANK_SIGN = BLOCKS.registerBlock("bank_sign", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_SIGN_ITEM = ITEMS.registerSimpleBlockItem("bank_sign", BANK_SIGN);
    public static final DeferredBlock<FurnitureBlock> BANK_TABLE = BLOCKS.registerBlock("bank_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_TABLE_ITEM = ITEMS.registerSimpleBlockItem("bank_table", BANK_TABLE);
    public static final DeferredBlock<FurnitureBlock> BANK_TALL_CHAIR = BLOCKS.registerBlock("bank_tall_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BANK_TALL_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("bank_tall_chair", BANK_TALL_CHAIR);

    // =============================================
    // --- Medieval Interior Furniture ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> MED_ARM_CHAIR = BLOCKS.registerBlock("med_arm_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_ARM_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("med_arm_chair", MED_ARM_CHAIR);
    public static final DeferredBlock<FurnitureBlock> MED_BED_BUNK = BLOCKS.registerBlock("med_bed_bunk", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BED_BUNK_ITEM = ITEMS.registerSimpleBlockItem("med_bed_bunk", MED_BED_BUNK);
    public static final DeferredBlock<FurnitureBlock> MED_BED_DOUBLE = BLOCKS.registerBlock("med_bed_double", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BED_DOUBLE_ITEM = ITEMS.registerSimpleBlockItem("med_bed_double", MED_BED_DOUBLE);
    public static final DeferredBlock<FurnitureBlock> MED_BED_SINGLE = BLOCKS.registerBlock("med_bed_single", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BED_SINGLE_ITEM = ITEMS.registerSimpleBlockItem("med_bed_single", MED_BED_SINGLE);
    public static final DeferredBlock<FurnitureBlock> MED_BOOK_OPEN = BLOCKS.registerBlock("med_book_open", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BOOK_OPEN_ITEM = ITEMS.registerSimpleBlockItem("med_book_open", MED_BOOK_OPEN);
    public static final DeferredBlock<FurnitureBlock> MED_BOOK_STACK_HORIZONTAL_1 = BLOCKS.registerBlock("med_book_stack_horizontal_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BOOK_STACK_HORIZONTAL_1_ITEM = ITEMS.registerSimpleBlockItem("med_book_stack_horizontal_1", MED_BOOK_STACK_HORIZONTAL_1);
    public static final DeferredBlock<FurnitureBlock> MED_BOOK_STACK_HORIZONTAL_2 = BLOCKS.registerBlock("med_book_stack_horizontal_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BOOK_STACK_HORIZONTAL_2_ITEM = ITEMS.registerSimpleBlockItem("med_book_stack_horizontal_2", MED_BOOK_STACK_HORIZONTAL_2);
    public static final DeferredBlock<FurnitureBlock> MED_BOOK_STACK_VERTICAL_1 = BLOCKS.registerBlock("med_book_stack_vertical_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BOOK_STACK_VERTICAL_1_ITEM = ITEMS.registerSimpleBlockItem("med_book_stack_vertical_1", MED_BOOK_STACK_VERTICAL_1);
    public static final DeferredBlock<FurnitureBlock> MED_BOOK_STACK_VERTICAL_2 = BLOCKS.registerBlock("med_book_stack_vertical_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BOOK_STACK_VERTICAL_2_ITEM = ITEMS.registerSimpleBlockItem("med_book_stack_vertical_2", MED_BOOK_STACK_VERTICAL_2);
    public static final DeferredBlock<FurnitureBlock> MED_CANDLE_DISH = BLOCKS.registerBlock("med_candle_dish", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> MED_CANDLE_DISH_ITEM = ITEMS.registerSimpleBlockItem("med_candle_dish", MED_CANDLE_DISH);
    public static final DeferredBlock<FurnitureBlock> MED_CHAIR = BLOCKS.registerBlock("med_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("med_chair", MED_CHAIR);
    public static final DeferredBlock<FurnitureBlock> MED_CHAIR_CUSHION = BLOCKS.registerBlock("med_chair_cushion", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CHAIR_CUSHION_ITEM = ITEMS.registerSimpleBlockItem("med_chair_cushion", MED_CHAIR_CUSHION);
    public static final DeferredBlock<FurnitureBlock> MED_COFFEE_TABLE = BLOCKS.registerBlock("med_coffee_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_COFFEE_TABLE_ITEM = ITEMS.registerSimpleBlockItem("med_coffee_table", MED_COFFEE_TABLE);
    public static final DeferredBlock<FurnitureBlock> MED_COFFEE_TABLE_CLOTH = BLOCKS.registerBlock("med_coffee_table_cloth", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_COFFEE_TABLE_CLOTH_ITEM = ITEMS.registerSimpleBlockItem("med_coffee_table_cloth", MED_COFFEE_TABLE_CLOTH);
    public static final DeferredBlock<FurnitureBlock> MED_DESK = BLOCKS.registerBlock("med_desk", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_DESK_ITEM = ITEMS.registerSimpleBlockItem("med_desk", MED_DESK);
    public static final DeferredBlock<FurnitureBlock> MED_DRESSER_LOW = BLOCKS.registerBlock("med_dresser_low", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_DRESSER_LOW_ITEM = ITEMS.registerSimpleBlockItem("med_dresser_low", MED_DRESSER_LOW);
    public static final DeferredBlock<FurnitureBlock> MED_DRESSER_TALL = BLOCKS.registerBlock("med_dresser_tall", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_DRESSER_TALL_ITEM = ITEMS.registerSimpleBlockItem("med_dresser_tall", MED_DRESSER_TALL);
    public static final DeferredBlock<FurnitureBlock> MED_END_TABLE = BLOCKS.registerBlock("med_end_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_END_TABLE_ITEM = ITEMS.registerSimpleBlockItem("med_end_table", MED_END_TABLE);
    public static final DeferredBlock<FurnitureBlock> MED_FIREPLACE = BLOCKS.registerBlock("med_fireplace", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> MED_FIREPLACE_ITEM = ITEMS.registerSimpleBlockItem("med_fireplace", MED_FIREPLACE);
    public static final DeferredBlock<FurnitureBlock> MED_LAMP = BLOCKS.registerBlock("med_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> MED_LAMP_ITEM = ITEMS.registerSimpleBlockItem("med_lamp", MED_LAMP);
    public static final DeferredBlock<FurnitureBlock> MED_LAMP_HANGING = BLOCKS.registerBlock("med_lamp_hanging", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> MED_LAMP_HANGING_ITEM = ITEMS.registerSimpleBlockItem("med_lamp_hanging", MED_LAMP_HANGING);
    public static final DeferredBlock<FurnitureBlock> MED_LAMP_TALL = BLOCKS.registerBlock("med_lamp_tall", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> MED_LAMP_TALL_ITEM = ITEMS.registerSimpleBlockItem("med_lamp_tall", MED_LAMP_TALL);
    public static final DeferredBlock<FurnitureBlock> MED_POT_1 = BLOCKS.registerBlock("med_pot_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_POT_1_ITEM = ITEMS.registerSimpleBlockItem("med_pot_1", MED_POT_1);
    public static final DeferredBlock<FurnitureBlock> MED_POT_2 = BLOCKS.registerBlock("med_pot_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_POT_2_ITEM = ITEMS.registerSimpleBlockItem("med_pot_2", MED_POT_2);
    public static final DeferredBlock<FurnitureBlock> MED_POT_3 = BLOCKS.registerBlock("med_pot_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_POT_3_ITEM = ITEMS.registerSimpleBlockItem("med_pot_3", MED_POT_3);
    public static final DeferredBlock<FurnitureBlock> MED_SCROLL_OPEN = BLOCKS.registerBlock("med_scroll_open", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_SCROLL_OPEN_ITEM = ITEMS.registerSimpleBlockItem("med_scroll_open", MED_SCROLL_OPEN);
    public static final DeferredBlock<FurnitureBlock> MED_SCROLL_STACK = BLOCKS.registerBlock("med_scroll_stack", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_SCROLL_STACK_ITEM = ITEMS.registerSimpleBlockItem("med_scroll_stack", MED_SCROLL_STACK);
    public static final DeferredBlock<FurnitureBlock> MED_SOFA = BLOCKS.registerBlock("med_sofa", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_SOFA_ITEM = ITEMS.registerSimpleBlockItem("med_sofa", MED_SOFA);
    public static final DeferredBlock<FurnitureBlock> MED_STOOL = BLOCKS.registerBlock("med_stool", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_STOOL_ITEM = ITEMS.registerSimpleBlockItem("med_stool", MED_STOOL);
    public static final DeferredBlock<FurnitureBlock> MED_STOOL_CUSHION = BLOCKS.registerBlock("med_stool_cushion", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_STOOL_CUSHION_ITEM = ITEMS.registerSimpleBlockItem("med_stool_cushion", MED_STOOL_CUSHION);
    public static final DeferredBlock<FurnitureBlock> MED_TABLE_LONG = BLOCKS.registerBlock("med_table_long", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_TABLE_LONG_ITEM = ITEMS.registerSimpleBlockItem("med_table_long", MED_TABLE_LONG);
    public static final DeferredBlock<FurnitureBlock> MED_TABLE_SMALL = BLOCKS.registerBlock("med_table_small", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_TABLE_SMALL_ITEM = ITEMS.registerSimpleBlockItem("med_table_small", MED_TABLE_SMALL);
    public static final DeferredBlock<FurnitureBlock> MED_TABLE_SMALL_CLOTH = BLOCKS.registerBlock("med_table_small_cloth", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_TABLE_SMALL_CLOTH_ITEM = ITEMS.registerSimpleBlockItem("med_table_small_cloth", MED_TABLE_SMALL_CLOTH);

    // =============================================
    // --- Medieval Market Furniture ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> MED_BULLETIN_BOARD = BLOCKS.registerBlock("med_bulletin_board", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BULLETIN_BOARD_ITEM = ITEMS.registerSimpleBlockItem("med_bulletin_board", MED_BULLETIN_BOARD);
    public static final DeferredBlock<FurnitureBlock> MED_BULLETIN_BOARD_SMALL = BLOCKS.registerBlock("med_bulletin_board_small", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BULLETIN_BOARD_SMALL_ITEM = ITEMS.registerSimpleBlockItem("med_bulletin_board_small", MED_BULLETIN_BOARD_SMALL);
    public static final DeferredBlock<FurnitureBlock> MED_BULLETIN_BOARD_WALL = BLOCKS.registerBlock("med_bulletin_board_wall", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BULLETIN_BOARD_WALL_ITEM = ITEMS.registerSimpleBlockItem("med_bulletin_board_wall", MED_BULLETIN_BOARD_WALL);
    public static final DeferredBlock<FurnitureBlock> MED_CANOPY_FLAT = BLOCKS.registerBlock("med_canopy_flat", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CANOPY_FLAT_ITEM = ITEMS.registerSimpleBlockItem("med_canopy_flat", MED_CANOPY_FLAT);
    public static final DeferredBlock<FurnitureBlock> MED_CANOPY_SLOPED = BLOCKS.registerBlock("med_canopy_sloped", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CANOPY_SLOPED_ITEM = ITEMS.registerSimpleBlockItem("med_canopy_sloped", MED_CANOPY_SLOPED);
    public static final DeferredBlock<FurnitureBlock> MED_CHALK_SIGN = BLOCKS.registerBlock("med_chalk_sign", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CHALK_SIGN_ITEM = ITEMS.registerSimpleBlockItem("med_chalk_sign", MED_CHALK_SIGN);
    public static final DeferredBlock<FurnitureBlock> MED_CHALK_SIGN_HANGING = BLOCKS.registerBlock("med_chalk_sign_hanging", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CHALK_SIGN_HANGING_ITEM = ITEMS.registerSimpleBlockItem("med_chalk_sign_hanging", MED_CHALK_SIGN_HANGING);
    public static final DeferredBlock<FurnitureBlock> MED_CHEST = BLOCKS.registerBlock("med_chest", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CHEST_ITEM = ITEMS.registerSimpleBlockItem("med_chest", MED_CHEST);
    public static final DeferredBlock<FurnitureBlock> MED_CHEST_OPEN = BLOCKS.registerBlock("med_chest_open", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CHEST_OPEN_ITEM = ITEMS.registerSimpleBlockItem("med_chest_open", MED_CHEST_OPEN);
    public static final DeferredBlock<FurnitureBlock> MED_COINS_1 = BLOCKS.registerBlock("med_coins_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_COINS_1_ITEM = ITEMS.registerSimpleBlockItem("med_coins_1", MED_COINS_1);
    public static final DeferredBlock<FurnitureBlock> MED_COINS_2 = BLOCKS.registerBlock("med_coins_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_COINS_2_ITEM = ITEMS.registerSimpleBlockItem("med_coins_2", MED_COINS_2);
    public static final DeferredBlock<FurnitureBlock> MED_COINS_3 = BLOCKS.registerBlock("med_coins_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_COINS_3_ITEM = ITEMS.registerSimpleBlockItem("med_coins_3", MED_COINS_3);
    public static final DeferredBlock<FurnitureBlock> MED_CRATE_BREAD = BLOCKS.registerBlock("med_crate_bread", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CRATE_BREAD_ITEM = ITEMS.registerSimpleBlockItem("med_crate_bread", MED_CRATE_BREAD);
    public static final DeferredBlock<FurnitureBlock> MED_CRATE_CARROTS = BLOCKS.registerBlock("med_crate_carrots", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CRATE_CARROTS_ITEM = ITEMS.registerSimpleBlockItem("med_crate_carrots", MED_CRATE_CARROTS);
    public static final DeferredBlock<FurnitureBlock> MED_CRATE_DISPLAY_BREAD = BLOCKS.registerBlock("med_crate_display_bread", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CRATE_DISPLAY_BREAD_ITEM = ITEMS.registerSimpleBlockItem("med_crate_display_bread", MED_CRATE_DISPLAY_BREAD);
    public static final DeferredBlock<FurnitureBlock> MED_CRATE_DISPLAY_CARROTS = BLOCKS.registerBlock("med_crate_display_carrots", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CRATE_DISPLAY_CARROTS_ITEM = ITEMS.registerSimpleBlockItem("med_crate_display_carrots", MED_CRATE_DISPLAY_CARROTS);
    public static final DeferredBlock<FurnitureBlock> MED_CRATE_DISPLAY_EMPTY = BLOCKS.registerBlock("med_crate_display_empty", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CRATE_DISPLAY_EMPTY_ITEM = ITEMS.registerSimpleBlockItem("med_crate_display_empty", MED_CRATE_DISPLAY_EMPTY);
    public static final DeferredBlock<FurnitureBlock> MED_CRATE_EMPTY = BLOCKS.registerBlock("med_crate_empty", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CRATE_EMPTY_ITEM = ITEMS.registerSimpleBlockItem("med_crate_empty", MED_CRATE_EMPTY);
    public static final DeferredBlock<FurnitureBlock> MED_CRATE_STACK = BLOCKS.registerBlock("med_crate_stack", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CRATE_STACK_ITEM = ITEMS.registerSimpleBlockItem("med_crate_stack", MED_CRATE_STACK);
    public static final DeferredBlock<FurnitureBlock> MED_PAPERS_HANGING_1 = BLOCKS.registerBlock("med_papers_hanging_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_PAPERS_HANGING_1_ITEM = ITEMS.registerSimpleBlockItem("med_papers_hanging_1", MED_PAPERS_HANGING_1);
    public static final DeferredBlock<FurnitureBlock> MED_PAPERS_HANGING_2 = BLOCKS.registerBlock("med_papers_hanging_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_PAPERS_HANGING_2_ITEM = ITEMS.registerSimpleBlockItem("med_papers_hanging_2", MED_PAPERS_HANGING_2);
    public static final DeferredBlock<FurnitureBlock> MED_PAPERS_HANGING_3 = BLOCKS.registerBlock("med_papers_hanging_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_PAPERS_HANGING_3_ITEM = ITEMS.registerSimpleBlockItem("med_papers_hanging_3", MED_PAPERS_HANGING_3);
    public static final DeferredBlock<FurnitureBlock> MED_SACK = BLOCKS.registerBlock("med_sack", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_SACK_ITEM = ITEMS.registerSimpleBlockItem("med_sack", MED_SACK);
    public static final DeferredBlock<FurnitureBlock> MED_SHIPPING_CRATE = BLOCKS.registerBlock("med_shipping_crate", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_SHIPPING_CRATE_ITEM = ITEMS.registerSimpleBlockItem("med_shipping_crate", MED_SHIPPING_CRATE);
    public static final DeferredBlock<FurnitureBlock> MED_SHIPPING_CRATE_TRIPLE_STACK = BLOCKS.registerBlock("med_shipping_crate_triple_stack", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_SHIPPING_CRATE_TRIPLE_STACK_ITEM = ITEMS.registerSimpleBlockItem("med_shipping_crate_triple_stack", MED_SHIPPING_CRATE_TRIPLE_STACK);
    public static final DeferredBlock<FurnitureBlock> MED_SHOP_CART = BLOCKS.registerBlock("med_shop_cart", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_SHOP_CART_ITEM = ITEMS.registerSimpleBlockItem("med_shop_cart", MED_SHOP_CART);
    public static final DeferredBlock<FurnitureBlock> MED_STREAMER_POST = BLOCKS.registerBlock("med_streamer_post", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_STREAMER_POST_ITEM = ITEMS.registerSimpleBlockItem("med_streamer_post", MED_STREAMER_POST);
    public static final DeferredBlock<FurnitureBlock> MED_STREAMERS_HANGING = BLOCKS.registerBlock("med_streamers_hanging", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_STREAMERS_HANGING_ITEM = ITEMS.registerSimpleBlockItem("med_streamers_hanging", MED_STREAMERS_HANGING);
    public static final DeferredBlock<FurnitureBlock> MED_WAGON = BLOCKS.registerBlock("med_wagon", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_WAGON_ITEM = ITEMS.registerSimpleBlockItem("med_wagon", MED_WAGON);
    public static final DeferredBlock<FurnitureBlock> MED_WAGON_FULL = BLOCKS.registerBlock("med_wagon_full", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_WAGON_FULL_ITEM = ITEMS.registerSimpleBlockItem("med_wagon_full", MED_WAGON_FULL);

    // =============================================
    // --- Medieval Nature Decor ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> MED_BROWN_MUSHROOM_PATCH = BLOCKS.registerBlock("med_brown_mushroom_patch", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BROWN_MUSHROOM_PATCH_ITEM = ITEMS.registerSimpleBlockItem("med_brown_mushroom_patch", MED_BROWN_MUSHROOM_PATCH);
    public static final DeferredBlock<FurnitureBlock> MED_BROWN_MUSHROOM_PATCH_LARGE = BLOCKS.registerBlock("med_brown_mushroom_patch_large", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_BROWN_MUSHROOM_PATCH_LARGE_ITEM = ITEMS.registerSimpleBlockItem("med_brown_mushroom_patch_large", MED_BROWN_MUSHROOM_PATCH_LARGE);
    public static final DeferredBlock<FurnitureBlock> MED_CATTAILS = BLOCKS.registerBlock("med_cattails", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CATTAILS_ITEM = ITEMS.registerSimpleBlockItem("med_cattails", MED_CATTAILS);
    public static final DeferredBlock<FurnitureBlock> MED_CLOVER_FLOWERS = BLOCKS.registerBlock("med_clover_flowers", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CLOVER_FLOWERS_ITEM = ITEMS.registerSimpleBlockItem("med_clover_flowers", MED_CLOVER_FLOWERS);
    public static final DeferredBlock<FurnitureBlock> MED_CLOVERS = BLOCKS.registerBlock("med_clovers", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_CLOVERS_ITEM = ITEMS.registerSimpleBlockItem("med_clovers", MED_CLOVERS);
    public static final DeferredBlock<FurnitureBlock> MED_CRYSTAL = BLOCKS.registerBlock("med_crystal", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> MED_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem("med_crystal", MED_CRYSTAL);
    public static final DeferredBlock<FurnitureBlock> MED_GLOW_MUSHROOM_PATCH = BLOCKS.registerBlock("med_glow_mushroom_patch", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> MED_GLOW_MUSHROOM_PATCH_ITEM = ITEMS.registerSimpleBlockItem("med_glow_mushroom_patch", MED_GLOW_MUSHROOM_PATCH);
    public static final DeferredBlock<FurnitureBlock> MED_GLOW_MUSHROOM_PATCH_LARGE = BLOCKS.registerBlock("med_glow_mushroom_patch_large", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(1.5f).noOcclusion().lightLevel(state -> 10));
    public static final DeferredItem<BlockItem> MED_GLOW_MUSHROOM_PATCH_LARGE_ITEM = ITEMS.registerSimpleBlockItem("med_glow_mushroom_patch_large", MED_GLOW_MUSHROOM_PATCH_LARGE);
    public static final DeferredBlock<FurnitureBlock> MED_LOG_MUSHROOM = BLOCKS.registerBlock("med_log_mushroom", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_LOG_MUSHROOM_ITEM = ITEMS.registerSimpleBlockItem("med_log_mushroom", MED_LOG_MUSHROOM);
    public static final DeferredBlock<FurnitureBlock> MED_LOG_MUSHROOM_CORNER = BLOCKS.registerBlock("med_log_mushroom_corner", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_LOG_MUSHROOM_CORNER_ITEM = ITEMS.registerSimpleBlockItem("med_log_mushroom_corner", MED_LOG_MUSHROOM_CORNER);
    public static final DeferredBlock<FurnitureBlock> MED_LOG_PILE_LARGE = BLOCKS.registerBlock("med_log_pile_large", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_LOG_PILE_LARGE_ITEM = ITEMS.registerSimpleBlockItem("med_log_pile_large", MED_LOG_PILE_LARGE);
    public static final DeferredBlock<FurnitureBlock> MED_LOG_PILE_LARGE_OVERGROWN = BLOCKS.registerBlock("med_log_pile_large_overgrown", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_LOG_PILE_LARGE_OVERGROWN_ITEM = ITEMS.registerSimpleBlockItem("med_log_pile_large_overgrown", MED_LOG_PILE_LARGE_OVERGROWN);
    public static final DeferredBlock<FurnitureBlock> MED_LOG_PILE_SMALL_1 = BLOCKS.registerBlock("med_log_pile_small_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_LOG_PILE_SMALL_1_ITEM = ITEMS.registerSimpleBlockItem("med_log_pile_small_1", MED_LOG_PILE_SMALL_1);
    public static final DeferredBlock<FurnitureBlock> MED_LOG_PILE_SMALL_2 = BLOCKS.registerBlock("med_log_pile_small_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_LOG_PILE_SMALL_2_ITEM = ITEMS.registerSimpleBlockItem("med_log_pile_small_2", MED_LOG_PILE_SMALL_2);
    public static final DeferredBlock<FurnitureBlock> MED_LOG_PILE_SMALL_3 = BLOCKS.registerBlock("med_log_pile_small_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_LOG_PILE_SMALL_3_ITEM = ITEMS.registerSimpleBlockItem("med_log_pile_small_3", MED_LOG_PILE_SMALL_3);
    public static final DeferredBlock<FurnitureBlock> MED_ORANGE_MUSHROOM_PATCH = BLOCKS.registerBlock("med_orange_mushroom_patch", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_ORANGE_MUSHROOM_PATCH_ITEM = ITEMS.registerSimpleBlockItem("med_orange_mushroom_patch", MED_ORANGE_MUSHROOM_PATCH);
    public static final DeferredBlock<FurnitureBlock> MED_ORANGE_MUSHROOM_PATCH_LARGE = BLOCKS.registerBlock("med_orange_mushroom_patch_large", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_ORANGE_MUSHROOM_PATCH_LARGE_ITEM = ITEMS.registerSimpleBlockItem("med_orange_mushroom_patch_large", MED_ORANGE_MUSHROOM_PATCH_LARGE);
    public static final DeferredBlock<FurnitureBlock> MED_ORE = BLOCKS.registerBlock("med_ore", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_ORE_ITEM = ITEMS.registerSimpleBlockItem("med_ore", MED_ORE);
    public static final DeferredBlock<FurnitureBlock> MED_PEBBLES_1 = BLOCKS.registerBlock("med_pebbles_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_PEBBLES_1_ITEM = ITEMS.registerSimpleBlockItem("med_pebbles_1", MED_PEBBLES_1);
    public static final DeferredBlock<FurnitureBlock> MED_PEBBLES_2 = BLOCKS.registerBlock("med_pebbles_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_PEBBLES_2_ITEM = ITEMS.registerSimpleBlockItem("med_pebbles_2", MED_PEBBLES_2);
    public static final DeferredBlock<FurnitureBlock> MED_PEBBLES_3 = BLOCKS.registerBlock("med_pebbles_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_PEBBLES_3_ITEM = ITEMS.registerSimpleBlockItem("med_pebbles_3", MED_PEBBLES_3);
    public static final DeferredBlock<FurnitureBlock> MED_PLANT = BLOCKS.registerBlock("med_plant", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_PLANT_ITEM = ITEMS.registerSimpleBlockItem("med_plant", MED_PLANT);
    public static final DeferredBlock<FurnitureBlock> MED_RED_MUSHROOM_PATCH = BLOCKS.registerBlock("med_red_mushroom_patch", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_RED_MUSHROOM_PATCH_ITEM = ITEMS.registerSimpleBlockItem("med_red_mushroom_patch", MED_RED_MUSHROOM_PATCH);
    public static final DeferredBlock<FurnitureBlock> MED_RED_MUSHROOM_PATCH_LARGE = BLOCKS.registerBlock("med_red_mushroom_patch_large", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_RED_MUSHROOM_PATCH_LARGE_ITEM = ITEMS.registerSimpleBlockItem("med_red_mushroom_patch_large", MED_RED_MUSHROOM_PATCH_LARGE);
    public static final DeferredBlock<FurnitureBlock> MED_ROCK_1 = BLOCKS.registerBlock("med_rock_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_ROCK_1_ITEM = ITEMS.registerSimpleBlockItem("med_rock_1", MED_ROCK_1);
    public static final DeferredBlock<FurnitureBlock> MED_ROCK_2 = BLOCKS.registerBlock("med_rock_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_ROCK_2_ITEM = ITEMS.registerSimpleBlockItem("med_rock_2", MED_ROCK_2);
    public static final DeferredBlock<FurnitureBlock> MED_ROCK_3 = BLOCKS.registerBlock("med_rock_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_ROCK_3_ITEM = ITEMS.registerSimpleBlockItem("med_rock_3", MED_ROCK_3);
    public static final DeferredBlock<FurnitureBlock> MED_ROCK_4 = BLOCKS.registerBlock("med_rock_4", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MED_ROCK_4_ITEM = ITEMS.registerSimpleBlockItem("med_rock_4", MED_ROCK_4);


    // =============================================
    // --- New Furniture Packs (Auto-Generated) ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> LR_CHAIR_1 = BLOCKS.registerBlock("lr_chair_1", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_CHAIR_1_ITEM = ITEMS.registerSimpleBlockItem("lr_chair_1", LR_CHAIR_1);
    public static final DeferredBlock<FurnitureBlock> LR_CHAIR_2 = BLOCKS.registerBlock("lr_chair_2", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_CHAIR_2_ITEM = ITEMS.registerSimpleBlockItem("lr_chair_2", LR_CHAIR_2);
    public static final DeferredBlock<FurnitureBlock> LR_CUPBOARD = BLOCKS.registerBlock("lr_cupboard", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_CUPBOARD_ITEM = ITEMS.registerSimpleBlockItem("lr_cupboard", LR_CUPBOARD);
    public static final DeferredBlock<FurnitureBlock> LR_FIREPLACE = BLOCKS.registerBlock("lr_fireplace", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_FIREPLACE_ITEM = ITEMS.registerSimpleBlockItem("lr_fireplace", LR_FIREPLACE);
    public static final DeferredBlock<FurnitureBlock> LR_FRAME_1 = BLOCKS.registerBlock("lr_frame_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_FRAME_1_ITEM = ITEMS.registerSimpleBlockItem("lr_frame_1", LR_FRAME_1);
    public static final DeferredBlock<FurnitureBlock> LR_FRAME_2 = BLOCKS.registerBlock("lr_frame_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_FRAME_2_ITEM = ITEMS.registerSimpleBlockItem("lr_frame_2", LR_FRAME_2);
    public static final DeferredBlock<FurnitureBlock> LR_LAMP_1 = BLOCKS.registerBlock("lr_lamp_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_LAMP_1_ITEM = ITEMS.registerSimpleBlockItem("lr_lamp_1", LR_LAMP_1);
    public static final DeferredBlock<FurnitureBlock> LR_LAMP_2 = BLOCKS.registerBlock("lr_lamp_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_LAMP_2_ITEM = ITEMS.registerSimpleBlockItem("lr_lamp_2", LR_LAMP_2);
    public static final DeferredBlock<FurnitureBlock> LR_PIANO = BLOCKS.registerBlock("lr_piano", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_PIANO_ITEM = ITEMS.registerSimpleBlockItem("lr_piano", LR_PIANO);
    public static final DeferredBlock<FurnitureBlock> LR_PLANT_1 = BLOCKS.registerBlock("lr_plant_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_PLANT_1_ITEM = ITEMS.registerSimpleBlockItem("lr_plant_1", LR_PLANT_1);
    public static final DeferredBlock<FurnitureBlock> LR_PLANT_2 = BLOCKS.registerBlock("lr_plant_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_PLANT_2_ITEM = ITEMS.registerSimpleBlockItem("lr_plant_2", LR_PLANT_2);
    public static final DeferredBlock<FurnitureBlock> LR_PLANT_3 = BLOCKS.registerBlock("lr_plant_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_PLANT_3_ITEM = ITEMS.registerSimpleBlockItem("lr_plant_3", LR_PLANT_3);
    public static final DeferredBlock<FurnitureBlock> LR_SHELF_1 = BLOCKS.registerBlock("lr_shelf_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_SHELF_1_ITEM = ITEMS.registerSimpleBlockItem("lr_shelf_1", LR_SHELF_1);
    public static final DeferredBlock<FurnitureBlock> LR_SHELF_2 = BLOCKS.registerBlock("lr_shelf_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_SHELF_2_ITEM = ITEMS.registerSimpleBlockItem("lr_shelf_2", LR_SHELF_2);
    public static final DeferredBlock<FurnitureBlock> LR_SOFA_1 = BLOCKS.registerBlock("lr_sofa_1", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_SOFA_1_ITEM = ITEMS.registerSimpleBlockItem("lr_sofa_1", LR_SOFA_1);
    public static final DeferredBlock<FurnitureBlock> LR_SOFA_2 = BLOCKS.registerBlock("lr_sofa_2", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_SOFA_2_ITEM = ITEMS.registerSimpleBlockItem("lr_sofa_2", LR_SOFA_2);
    public static final DeferredBlock<FurnitureBlock> LR_SOFA_3 = BLOCKS.registerBlock("lr_sofa_3", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_SOFA_3_ITEM = ITEMS.registerSimpleBlockItem("lr_sofa_3", LR_SOFA_3);
    public static final DeferredBlock<FurnitureBlock> LR_SOFA_4 = BLOCKS.registerBlock("lr_sofa_4", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_SOFA_4_ITEM = ITEMS.registerSimpleBlockItem("lr_sofa_4", LR_SOFA_4);
    public static final DeferredBlock<FurnitureBlock> LR_SOUND_SYSTEM = BLOCKS.registerBlock("lr_sound_system", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_SOUND_SYSTEM_ITEM = ITEMS.registerSimpleBlockItem("lr_sound_system", LR_SOUND_SYSTEM);
    public static final DeferredBlock<FurnitureBlock> LR_TABLE_1 = BLOCKS.registerBlock("lr_table_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_TABLE_1_ITEM = ITEMS.registerSimpleBlockItem("lr_table_1", LR_TABLE_1);
    public static final DeferredBlock<FurnitureBlock> LR_TABLE_2 = BLOCKS.registerBlock("lr_table_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_TABLE_2_ITEM = ITEMS.registerSimpleBlockItem("lr_table_2", LR_TABLE_2);
    public static final DeferredBlock<FurnitureBlock> LR_TV = BLOCKS.registerBlock("lr_tv", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> LR_TV_ITEM = ITEMS.registerSimpleBlockItem("lr_tv", LR_TV);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_BED_COLOR = BLOCKS.registerBlock("ms2_medieval_bed_color", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_BED_COLOR_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_bed_color", MS2_MEDIEVAL_BED_COLOR);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_BED_DYE = BLOCKS.registerBlock("ms2_medieval_bed_dye", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_BED_DYE_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_bed_dye", MS2_MEDIEVAL_BED_DYE);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_BOOK_1 = BLOCKS.registerBlock("ms2_medieval_book_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_BOOK_1_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_book_1", MS2_MEDIEVAL_BOOK_1);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_BOOK_2 = BLOCKS.registerBlock("ms2_medieval_book_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_BOOK_2_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_book_2", MS2_MEDIEVAL_BOOK_2);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_BOOK_3 = BLOCKS.registerBlock("ms2_medieval_book_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_BOOK_3_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_book_3", MS2_MEDIEVAL_BOOK_3);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_BOOK_4 = BLOCKS.registerBlock("ms2_medieval_book_4", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_BOOK_4_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_book_4", MS2_MEDIEVAL_BOOK_4);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_BRICK_1 = BLOCKS.registerBlock("ms2_medieval_brick_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_BRICK_1_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_brick_1", MS2_MEDIEVAL_BRICK_1);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_DOUBLE_BED_COLOR = BLOCKS.registerBlock("ms2_medieval_double_bed_color", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_DOUBLE_BED_COLOR_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_double_bed_color", MS2_MEDIEVAL_DOUBLE_BED_COLOR);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_DOUBLE_BED_DYE = BLOCKS.registerBlock("ms2_medieval_double_bed_dye", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_DOUBLE_BED_DYE_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_double_bed_dye", MS2_MEDIEVAL_DOUBLE_BED_DYE);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_DOUBLE_CHAIR_COLOR = BLOCKS.registerBlock("ms2_medieval_double_chair_color", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_DOUBLE_CHAIR_COLOR_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_double_chair_color", MS2_MEDIEVAL_DOUBLE_CHAIR_COLOR);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_DOUBLE_CHAIR_DYE = BLOCKS.registerBlock("ms2_medieval_double_chair_dye", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_DOUBLE_CHAIR_DYE_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_double_chair_dye", MS2_MEDIEVAL_DOUBLE_CHAIR_DYE);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_FAN_COLOR = BLOCKS.registerBlock("ms2_medieval_fan_color", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_FAN_COLOR_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_fan_color", MS2_MEDIEVAL_FAN_COLOR);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_FENCE_1 = BLOCKS.registerBlock("ms2_medieval_fence_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_FENCE_1_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_fence_1", MS2_MEDIEVAL_FENCE_1);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_FIRE_STAND = BLOCKS.registerBlock("ms2_medieval_fire_stand", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_FIRE_STAND_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_fire_stand", MS2_MEDIEVAL_FIRE_STAND);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_FLOWERPOT_1 = BLOCKS.registerBlock("ms2_medieval_flowerpot_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_FLOWERPOT_1_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_flowerpot_1", MS2_MEDIEVAL_FLOWERPOT_1);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_FLOWERPOT_2 = BLOCKS.registerBlock("ms2_medieval_flowerpot_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_FLOWERPOT_2_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_flowerpot_2", MS2_MEDIEVAL_FLOWERPOT_2);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_FLOWERPOT_3 = BLOCKS.registerBlock("ms2_medieval_flowerpot_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_FLOWERPOT_3_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_flowerpot_3", MS2_MEDIEVAL_FLOWERPOT_3);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_FULL_TABLE_COLOR = BLOCKS.registerBlock("ms2_medieval_full_table_color", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_FULL_TABLE_COLOR_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_full_table_color", MS2_MEDIEVAL_FULL_TABLE_COLOR);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_FULL_TABLE_COLOR_1 = BLOCKS.registerBlock("ms2_medieval_full_table_color_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_FULL_TABLE_COLOR_1_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_full_table_color_1", MS2_MEDIEVAL_FULL_TABLE_COLOR_1);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_OAKLOG_1 = BLOCKS.registerBlock("ms2_medieval_oaklog_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_OAKLOG_1_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_oaklog_1", MS2_MEDIEVAL_OAKLOG_1);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_OAKLOG_2 = BLOCKS.registerBlock("ms2_medieval_oaklog_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_OAKLOG_2_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_oaklog_2", MS2_MEDIEVAL_OAKLOG_2);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_OAKLOG_3 = BLOCKS.registerBlock("ms2_medieval_oaklog_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_OAKLOG_3_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_oaklog_3", MS2_MEDIEVAL_OAKLOG_3);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_ONE_CHAIR_1_COLOR = BLOCKS.registerBlock("ms2_medieval_one_chair_1_color", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_ONE_CHAIR_1_COLOR_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_one_chair_1_color", MS2_MEDIEVAL_ONE_CHAIR_1_COLOR);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_ONE_CHAIR_1_DYE = BLOCKS.registerBlock("ms2_medieval_one_chair_1_dye", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_ONE_CHAIR_1_DYE_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_one_chair_1_dye", MS2_MEDIEVAL_ONE_CHAIR_1_DYE);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_ONE_CHAIR_COLOR = BLOCKS.registerBlock("ms2_medieval_one_chair_color", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_ONE_CHAIR_COLOR_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_one_chair_color", MS2_MEDIEVAL_ONE_CHAIR_COLOR);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_ONE_CHAIR_COLOR_1 = BLOCKS.registerBlock("ms2_medieval_one_chair_color_1", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_ONE_CHAIR_COLOR_1_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_one_chair_color_1", MS2_MEDIEVAL_ONE_CHAIR_COLOR_1);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_ONE_CHAIR_COLOR_2 = BLOCKS.registerBlock("ms2_medieval_one_chair_color_2", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_ONE_CHAIR_COLOR_2_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_one_chair_color_2", MS2_MEDIEVAL_ONE_CHAIR_COLOR_2);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_ONE_CHAIR_DYE = BLOCKS.registerBlock("ms2_medieval_one_chair_dye", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_ONE_CHAIR_DYE_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_one_chair_dye", MS2_MEDIEVAL_ONE_CHAIR_DYE);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_ONE_CHAIR_DYE_2 = BLOCKS.registerBlock("ms2_medieval_one_chair_dye_2", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_ONE_CHAIR_DYE_2_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_one_chair_dye_2", MS2_MEDIEVAL_ONE_CHAIR_DYE_2);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_ONE_TABLE_COLOR_1 = BLOCKS.registerBlock("ms2_medieval_one_table_color_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_ONE_TABLE_COLOR_1_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_one_table_color_1", MS2_MEDIEVAL_ONE_TABLE_COLOR_1);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_SCAFFOLDING = BLOCKS.registerBlock("ms2_medieval_scaffolding", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_SCAFFOLDING_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_scaffolding", MS2_MEDIEVAL_SCAFFOLDING);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_SIGN_BANK = BLOCKS.registerBlock("ms2_medieval_sign_bank", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_SIGN_BANK_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_sign_bank", MS2_MEDIEVAL_SIGN_BANK);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_SIGN_BLACKSMITH = BLOCKS.registerBlock("ms2_medieval_sign_blacksmith", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_SIGN_BLACKSMITH_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_sign_blacksmith", MS2_MEDIEVAL_SIGN_BLACKSMITH);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_SIGN_COSMETIC = BLOCKS.registerBlock("ms2_medieval_sign_cosmetic", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_SIGN_COSMETIC_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_sign_cosmetic", MS2_MEDIEVAL_SIGN_COSMETIC);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_SIGN_DEFAULT = BLOCKS.registerBlock("ms2_medieval_sign_default", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_SIGN_DEFAULT_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_sign_default", MS2_MEDIEVAL_SIGN_DEFAULT);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_SIGN_ENCHANTED = BLOCKS.registerBlock("ms2_medieval_sign_enchanted", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_SIGN_ENCHANTED_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_sign_enchanted", MS2_MEDIEVAL_SIGN_ENCHANTED);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_SIGN_FURNITURE = BLOCKS.registerBlock("ms2_medieval_sign_furniture", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_SIGN_FURNITURE_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_sign_furniture", MS2_MEDIEVAL_SIGN_FURNITURE);
    public static final DeferredBlock<FurnitureBlock> MS2_MEDIEVAL_SIGN_POTION = BLOCKS.registerBlock("ms2_medieval_sign_potion", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS2_MEDIEVAL_SIGN_POTION_ITEM = ITEMS.registerSimpleBlockItem("ms2_medieval_sign_potion", MS2_MEDIEVAL_SIGN_POTION);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_BOX_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_box_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_BOX_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_box_1", MS4_MEDIEVAL_PACK_V4_BOX_1);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_BOX_2 = BLOCKS.registerBlock("ms4_medieval_pack_v4_box_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_BOX_2_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_box_2", MS4_MEDIEVAL_PACK_V4_BOX_2);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_BRIDGE = BLOCKS.registerBlock("ms4_medieval_pack_v4_bridge", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_BRIDGE_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_bridge", MS4_MEDIEVAL_PACK_V4_BRIDGE);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_BRIDGE_MID = BLOCKS.registerBlock("ms4_medieval_pack_v4_bridge_mid", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_BRIDGE_MID_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_bridge_mid", MS4_MEDIEVAL_PACK_V4_BRIDGE_MID);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_BRIDGE_PATH = BLOCKS.registerBlock("ms4_medieval_pack_v4_bridge_path", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_BRIDGE_PATH_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_bridge_path", MS4_MEDIEVAL_PACK_V4_BRIDGE_PATH);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND = BLOCKS.registerBlock("ms4_medieval_pack_v4_bridge_stand", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_bridge_stand", MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_bridge_stand_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_bridge_stand_1", MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_1);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_2 = BLOCKS.registerBlock("ms4_medieval_pack_v4_bridge_stand_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_2_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_bridge_stand_2", MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_2);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_MID_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_bridge_stand_mid_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_MID_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_bridge_stand_mid_1", MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_MID_1);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_MID_2 = BLOCKS.registerBlock("ms4_medieval_pack_v4_bridge_stand_mid_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_MID_2_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_bridge_stand_mid_2", MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_MID_2);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_MID_3 = BLOCKS.registerBlock("ms4_medieval_pack_v4_bridge_stand_mid_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_MID_3_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_bridge_stand_mid_3", MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_MID_3);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_CAGE_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_cage_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_CAGE_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_cage_1", MS4_MEDIEVAL_PACK_V4_CAGE_1);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_CAGE_2 = BLOCKS.registerBlock("ms4_medieval_pack_v4_cage_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_CAGE_2_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_cage_2", MS4_MEDIEVAL_PACK_V4_CAGE_2);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_CAMP_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_camp_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_CAMP_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_camp_1", MS4_MEDIEVAL_PACK_V4_CAMP_1);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_CHAIR_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_chair_1", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_CHAIR_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_chair_1", MS4_MEDIEVAL_PACK_V4_CHAIR_1);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_CHAIR_2 = BLOCKS.registerBlock("ms4_medieval_pack_v4_chair_2", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_CHAIR_2_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_chair_2", MS4_MEDIEVAL_PACK_V4_CHAIR_2);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_GATE_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_gate_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_GATE_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_gate_1", MS4_MEDIEVAL_PACK_V4_GATE_1);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_GATE_2 = BLOCKS.registerBlock("ms4_medieval_pack_v4_gate_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_GATE_2_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_gate_2", MS4_MEDIEVAL_PACK_V4_GATE_2);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_LADDER = BLOCKS.registerBlock("ms4_medieval_pack_v4_ladder", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_LADDER_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_ladder", MS4_MEDIEVAL_PACK_V4_LADDER);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_LOG_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_log_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_LOG_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_log_1", MS4_MEDIEVAL_PACK_V4_LOG_1);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_LOG_2 = BLOCKS.registerBlock("ms4_medieval_pack_v4_log_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_LOG_2_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_log_2", MS4_MEDIEVAL_PACK_V4_LOG_2);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_LOG_3 = BLOCKS.registerBlock("ms4_medieval_pack_v4_log_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_LOG_3_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_log_3", MS4_MEDIEVAL_PACK_V4_LOG_3);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_LOG_4 = BLOCKS.registerBlock("ms4_medieval_pack_v4_log_4", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_LOG_4_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_log_4", MS4_MEDIEVAL_PACK_V4_LOG_4);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_LOG_5 = BLOCKS.registerBlock("ms4_medieval_pack_v4_log_5", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_LOG_5_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_log_5", MS4_MEDIEVAL_PACK_V4_LOG_5);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_LOG_6 = BLOCKS.registerBlock("ms4_medieval_pack_v4_log_6", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_LOG_6_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_log_6", MS4_MEDIEVAL_PACK_V4_LOG_6);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_LOG_7 = BLOCKS.registerBlock("ms4_medieval_pack_v4_log_7", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_LOG_7_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_log_7", MS4_MEDIEVAL_PACK_V4_LOG_7);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_LOG_8 = BLOCKS.registerBlock("ms4_medieval_pack_v4_log_8", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_LOG_8_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_log_8", MS4_MEDIEVAL_PACK_V4_LOG_8);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_LOG_9 = BLOCKS.registerBlock("ms4_medieval_pack_v4_log_9", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_LOG_9_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_log_9", MS4_MEDIEVAL_PACK_V4_LOG_9);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_ROCK_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_rock_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_ROCK_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_rock_1", MS4_MEDIEVAL_PACK_V4_ROCK_1);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_STANDLIGHT = BLOCKS.registerBlock("ms4_medieval_pack_v4_standlight", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_STANDLIGHT_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_standlight", MS4_MEDIEVAL_PACK_V4_STANDLIGHT);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_STANDLIGHT_2 = BLOCKS.registerBlock("ms4_medieval_pack_v4_standlight_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_STANDLIGHT_2_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_standlight_2", MS4_MEDIEVAL_PACK_V4_STANDLIGHT_2);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_SUPPORT = BLOCKS.registerBlock("ms4_medieval_pack_v4_support", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_SUPPORT_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_support", MS4_MEDIEVAL_PACK_V4_SUPPORT);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_SUPPORT_H3 = BLOCKS.registerBlock("ms4_medieval_pack_v4_support_h3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_SUPPORT_H3_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_support_h3", MS4_MEDIEVAL_PACK_V4_SUPPORT_H3);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_TABE_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_tabe_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_TABE_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_tabe_1", MS4_MEDIEVAL_PACK_V4_TABE_1);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_TORCH_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_torch_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_TORCH_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_torch_1", MS4_MEDIEVAL_PACK_V4_TORCH_1);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_TORCH_2 = BLOCKS.registerBlock("ms4_medieval_pack_v4_torch_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_TORCH_2_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_torch_2", MS4_MEDIEVAL_PACK_V4_TORCH_2);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_TORCH_3 = BLOCKS.registerBlock("ms4_medieval_pack_v4_torch_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_TORCH_3_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_torch_3", MS4_MEDIEVAL_PACK_V4_TORCH_3);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_TOWER_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_tower_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_TOWER_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_tower_1", MS4_MEDIEVAL_PACK_V4_TOWER_1);
    public static final DeferredBlock<FurnitureBlock> MS4_MEDIEVAL_PACK_V4_WHEEL_1 = BLOCKS.registerBlock("ms4_medieval_pack_v4_wheel_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MS4_MEDIEVAL_PACK_V4_WHEEL_1_ITEM = ITEMS.registerSimpleBlockItem("ms4_medieval_pack_v4_wheel_1", MS4_MEDIEVAL_PACK_V4_WHEEL_1);
    public static final DeferredBlock<FurnitureBlock> MKT_MEDIEVAL_MARKET_BAG = BLOCKS.registerBlock("mkt_medieval_market_bag", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MKT_MEDIEVAL_MARKET_BAG_ITEM = ITEMS.registerSimpleBlockItem("mkt_medieval_market_bag", MKT_MEDIEVAL_MARKET_BAG);
    public static final DeferredBlock<FurnitureBlock> MKT_MEDIEVAL_MARKET_BARELL = BLOCKS.registerBlock("mkt_medieval_market_barell", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MKT_MEDIEVAL_MARKET_BARELL_ITEM = ITEMS.registerSimpleBlockItem("mkt_medieval_market_barell", MKT_MEDIEVAL_MARKET_BARELL);
    public static final DeferredBlock<FurnitureBlock> MKT_MEDIEVAL_MARKET_BATH = BLOCKS.registerBlock("mkt_medieval_market_bath", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MKT_MEDIEVAL_MARKET_BATH_ITEM = ITEMS.registerSimpleBlockItem("mkt_medieval_market_bath", MKT_MEDIEVAL_MARKET_BATH);
    public static final DeferredBlock<FurnitureBlock> MKT_MEDIEVAL_MARKET_BED = BLOCKS.registerBlock("mkt_medieval_market_bed", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MKT_MEDIEVAL_MARKET_BED_ITEM = ITEMS.registerSimpleBlockItem("mkt_medieval_market_bed", MKT_MEDIEVAL_MARKET_BED);
    public static final DeferredBlock<FurnitureBlock> MKT_MEDIEVAL_MARKET_BENCH = BLOCKS.registerBlock("mkt_medieval_market_bench", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MKT_MEDIEVAL_MARKET_BENCH_ITEM = ITEMS.registerSimpleBlockItem("mkt_medieval_market_bench", MKT_MEDIEVAL_MARKET_BENCH);
    public static final DeferredBlock<FurnitureBlock> MKT_MEDIEVAL_MARKET_BOX = BLOCKS.registerBlock("mkt_medieval_market_box", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MKT_MEDIEVAL_MARKET_BOX_ITEM = ITEMS.registerSimpleBlockItem("mkt_medieval_market_box", MKT_MEDIEVAL_MARKET_BOX);
    public static final DeferredBlock<FurnitureBlock> MKT_MEDIEVAL_MARKET_BOX_APPLES = BLOCKS.registerBlock("mkt_medieval_market_box_apples", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MKT_MEDIEVAL_MARKET_BOX_APPLES_ITEM = ITEMS.registerSimpleBlockItem("mkt_medieval_market_box_apples", MKT_MEDIEVAL_MARKET_BOX_APPLES);
    public static final DeferredBlock<FurnitureBlock> MKT_MEDIEVAL_MARKET_BOX_BREAD = BLOCKS.registerBlock("mkt_medieval_market_box_bread", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MKT_MEDIEVAL_MARKET_BOX_BREAD_ITEM = ITEMS.registerSimpleBlockItem("mkt_medieval_market_box_bread", MKT_MEDIEVAL_MARKET_BOX_BREAD);
    public static final DeferredBlock<FurnitureBlock> MKT_MEDIEVAL_MARKET_CAMPFIRE = BLOCKS.registerBlock("mkt_medieval_market_campfire", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MKT_MEDIEVAL_MARKET_CAMPFIRE_ITEM = ITEMS.registerSimpleBlockItem("mkt_medieval_market_campfire", MKT_MEDIEVAL_MARKET_CAMPFIRE);
    public static final DeferredBlock<FurnitureBlock> MKT_MEDIEVAL_MARKET_CHAIR = BLOCKS.registerBlock("mkt_medieval_market_chair", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MKT_MEDIEVAL_MARKET_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("mkt_medieval_market_chair", MKT_MEDIEVAL_MARKET_CHAIR);
    public static final DeferredBlock<FurnitureBlock> MKT_MEDIEVAL_MARKET_CHEST = BLOCKS.registerBlock("mkt_medieval_market_chest", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> MKT_MEDIEVAL_MARKET_CHEST_ITEM = ITEMS.registerSimpleBlockItem("mkt_medieval_market_chest", MKT_MEDIEVAL_MARKET_CHEST);
    public static final DeferredBlock<FurnitureBlock> PARK_BENCH = BLOCKS.registerBlock("park_bench", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_BENCH_ITEM = ITEMS.registerSimpleBlockItem("park_bench", PARK_BENCH);
    public static final DeferredBlock<FurnitureBlock> PARK_BIRD_HOUSE = BLOCKS.registerBlock("park_bird_house", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_BIRD_HOUSE_ITEM = ITEMS.registerSimpleBlockItem("park_bird_house", PARK_BIRD_HOUSE);
    public static final DeferredBlock<FurnitureBlock> PARK_BOX = BLOCKS.registerBlock("park_box", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_BOX_ITEM = ITEMS.registerSimpleBlockItem("park_box", PARK_BOX);
    public static final DeferredBlock<FurnitureBlock> PARK_BOX_CONSTRUCTION = BLOCKS.registerBlock("park_box_construction", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_BOX_CONSTRUCTION_ITEM = ITEMS.registerSimpleBlockItem("park_box_construction", PARK_BOX_CONSTRUCTION);
    public static final DeferredBlock<FurnitureBlock> PARK_CONCRET_POT_PLANT = BLOCKS.registerBlock("park_concret_pot_plant", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_CONCRET_POT_PLANT_ITEM = ITEMS.registerSimpleBlockItem("park_concret_pot_plant", PARK_CONCRET_POT_PLANT);
    public static final DeferredBlock<FurnitureBlock> PARK_FENCE = BLOCKS.registerBlock("park_fence", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_FENCE_ITEM = ITEMS.registerSimpleBlockItem("park_fence", PARK_FENCE);
    public static final DeferredBlock<FurnitureBlock> PARK_LANTERN_BOX = BLOCKS.registerBlock("park_lantern_box", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_LANTERN_BOX_ITEM = ITEMS.registerSimpleBlockItem("park_lantern_box", PARK_LANTERN_BOX);
    public static final DeferredBlock<FurnitureBlock> PARK_LIGHT_WOODEN_PALLET = BLOCKS.registerBlock("park_light_wooden_pallet", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_LIGHT_WOODEN_PALLET_ITEM = ITEMS.registerSimpleBlockItem("park_light_wooden_pallet", PARK_LIGHT_WOODEN_PALLET);
    public static final DeferredBlock<FurnitureBlock> PARK_LITTLE_BOX = BLOCKS.registerBlock("park_little_box", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_LITTLE_BOX_ITEM = ITEMS.registerSimpleBlockItem("park_little_box", PARK_LITTLE_BOX);
    public static final DeferredBlock<FurnitureBlock> PARK_LOG_BENCH = BLOCKS.registerBlock("park_log_bench", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_LOG_BENCH_ITEM = ITEMS.registerSimpleBlockItem("park_log_bench", PARK_LOG_BENCH);
    public static final DeferredBlock<FurnitureBlock> PARK_PICNIC_TABLE = BLOCKS.registerBlock("park_picnic_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_PICNIC_TABLE_ITEM = ITEMS.registerSimpleBlockItem("park_picnic_table", PARK_PICNIC_TABLE);
    public static final DeferredBlock<FurnitureBlock> PARK_PICNIC_TABLE_NAPPE = BLOCKS.registerBlock("park_picnic_table_nappe", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_PICNIC_TABLE_NAPPE_ITEM = ITEMS.registerSimpleBlockItem("park_picnic_table_nappe", PARK_PICNIC_TABLE_NAPPE);
    public static final DeferredBlock<FurnitureBlock> PARK_STELE_1 = BLOCKS.registerBlock("park_stele_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_STELE_1_ITEM = ITEMS.registerSimpleBlockItem("park_stele_1", PARK_STELE_1);
    public static final DeferredBlock<FurnitureBlock> PARK_STELE_2 = BLOCKS.registerBlock("park_stele_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_STELE_2_ITEM = ITEMS.registerSimpleBlockItem("park_stele_2", PARK_STELE_2);
    public static final DeferredBlock<FurnitureBlock> PARK_STELE_3 = BLOCKS.registerBlock("park_stele_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_STELE_3_ITEM = ITEMS.registerSimpleBlockItem("park_stele_3", PARK_STELE_3);
    public static final DeferredBlock<FurnitureBlock> PARK_STELE_4 = BLOCKS.registerBlock("park_stele_4", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_STELE_4_ITEM = ITEMS.registerSimpleBlockItem("park_stele_4", PARK_STELE_4);
    public static final DeferredBlock<FurnitureBlock> PARK_STELE_5 = BLOCKS.registerBlock("park_stele_5", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_STELE_5_ITEM = ITEMS.registerSimpleBlockItem("park_stele_5", PARK_STELE_5);
    public static final DeferredBlock<FurnitureBlock> PARK_STREET_LAMP = BLOCKS.registerBlock("park_street_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_STREET_LAMP_ITEM = ITEMS.registerSimpleBlockItem("park_street_lamp", PARK_STREET_LAMP);
    public static final DeferredBlock<FurnitureBlock> PARK_STREET_LAMP_DOUBLE = BLOCKS.registerBlock("park_street_lamp_double", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_STREET_LAMP_DOUBLE_ITEM = ITEMS.registerSimpleBlockItem("park_street_lamp_double", PARK_STREET_LAMP_DOUBLE);
    public static final DeferredBlock<FurnitureBlock> PARK_STREET_LAMP_QUADRUPLE = BLOCKS.registerBlock("park_street_lamp_quadruple", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_STREET_LAMP_QUADRUPLE_ITEM = ITEMS.registerSimpleBlockItem("park_street_lamp_quadruple", PARK_STREET_LAMP_QUADRUPLE);
    public static final DeferredBlock<FurnitureBlock> PARK_STREET_LAMP_UPPER = BLOCKS.registerBlock("park_street_lamp_upper", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_STREET_LAMP_UPPER_ITEM = ITEMS.registerSimpleBlockItem("park_street_lamp_upper", PARK_STREET_LAMP_UPPER);
    public static final DeferredBlock<FurnitureBlock> PARK_STREET_LAMP_UPPER_DOUBLE = BLOCKS.registerBlock("park_street_lamp_upper_double", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_STREET_LAMP_UPPER_DOUBLE_ITEM = ITEMS.registerSimpleBlockItem("park_street_lamp_upper_double", PARK_STREET_LAMP_UPPER_DOUBLE);
    public static final DeferredBlock<FurnitureBlock> PARK_STREET_LAMP_UPPER_QUADRUPLE = BLOCKS.registerBlock("park_street_lamp_upper_quadruple", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_STREET_LAMP_UPPER_QUADRUPLE_ITEM = ITEMS.registerSimpleBlockItem("park_street_lamp_upper_quadruple", PARK_STREET_LAMP_UPPER_QUADRUPLE);
    public static final DeferredBlock<FurnitureBlock> PARK_STREET_LAMP_UPPER_TRIPLE = BLOCKS.registerBlock("park_street_lamp_upper_triple", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_STREET_LAMP_UPPER_TRIPLE_ITEM = ITEMS.registerSimpleBlockItem("park_street_lamp_upper_triple", PARK_STREET_LAMP_UPPER_TRIPLE);
    public static final DeferredBlock<FurnitureBlock> PARK_SWING = BLOCKS.registerBlock("park_swing", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_SWING_ITEM = ITEMS.registerSimpleBlockItem("park_swing", PARK_SWING);
    public static final DeferredBlock<FurnitureBlock> PARK_UNDER_CONSTRUCTION = BLOCKS.registerBlock("park_under_construction", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_UNDER_CONSTRUCTION_ITEM = ITEMS.registerSimpleBlockItem("park_under_construction", PARK_UNDER_CONSTRUCTION);
    public static final DeferredBlock<FurnitureBlock> PARK_WOODEN_FLOWER_POT = BLOCKS.registerBlock("park_wooden_flower_pot", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_WOODEN_FLOWER_POT_ITEM = ITEMS.registerSimpleBlockItem("park_wooden_flower_pot", PARK_WOODEN_FLOWER_POT);
    public static final DeferredBlock<FurnitureBlock> PARK_WOODEN_GROUND_PLANKS = BLOCKS.registerBlock("park_wooden_ground_planks", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_WOODEN_GROUND_PLANKS_ITEM = ITEMS.registerSimpleBlockItem("park_wooden_ground_planks", PARK_WOODEN_GROUND_PLANKS);
    public static final DeferredBlock<FurnitureBlock> PARK_WOODEN_PALLET = BLOCKS.registerBlock("park_wooden_pallet", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> PARK_WOODEN_PALLET_ITEM = ITEMS.registerSimpleBlockItem("park_wooden_pallet", PARK_WOODEN_PALLET);
    public static final DeferredBlock<FurnitureBlock> CRAFT_ALCHEMY_STATION = BLOCKS.registerBlock("craft_alchemy_station", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_ALCHEMY_STATION_ITEM = ITEMS.registerSimpleBlockItem("craft_alchemy_station", CRAFT_ALCHEMY_STATION);
    public static final DeferredBlock<FurnitureBlock> CRAFT_BREWING_STAND = BLOCKS.registerBlock("craft_brewing_stand", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_BREWING_STAND_ITEM = ITEMS.registerSimpleBlockItem("craft_brewing_stand", CRAFT_BREWING_STAND);
    public static final DeferredBlock<FurnitureBlock> CRAFT_BREWING_TABLE = BLOCKS.registerBlock("craft_brewing_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_BREWING_TABLE_ITEM = ITEMS.registerSimpleBlockItem("craft_brewing_table", CRAFT_BREWING_TABLE);
    public static final DeferredBlock<FurnitureBlock> CRAFT_HERB_WALL_RACK = BLOCKS.registerBlock("craft_herb_wall_rack", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_HERB_WALL_RACK_ITEM = ITEMS.registerSimpleBlockItem("craft_herb_wall_rack", CRAFT_HERB_WALL_RACK);
    public static final DeferredBlock<FurnitureBlock> CRAFT_POTION_SHELF = BLOCKS.registerBlock("craft_potion_shelf", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_POTION_SHELF_ITEM = ITEMS.registerSimpleBlockItem("craft_potion_shelf", CRAFT_POTION_SHELF);
    public static final DeferredBlock<FurnitureBlock> CRAFT_BLUEPRINT = BLOCKS.registerBlock("craft_blueprint", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_BLUEPRINT_ITEM = ITEMS.registerSimpleBlockItem("craft_blueprint", CRAFT_BLUEPRINT);
    public static final DeferredBlock<FurnitureBlock> CRAFT_CARPENTRY_STATION = BLOCKS.registerBlock("craft_carpentry_station", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_CARPENTRY_STATION_ITEM = ITEMS.registerSimpleBlockItem("craft_carpentry_station", CRAFT_CARPENTRY_STATION);
    public static final DeferredBlock<FurnitureBlock> CRAFT_LOG_CUTTING_STAND = BLOCKS.registerBlock("craft_log_cutting_stand", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_LOG_CUTTING_STAND_ITEM = ITEMS.registerSimpleBlockItem("craft_log_cutting_stand", CRAFT_LOG_CUTTING_STAND);
    public static final DeferredBlock<FurnitureBlock> CRAFT_PLANKS_LEANING = BLOCKS.registerBlock("craft_planks_leaning", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_PLANKS_LEANING_ITEM = ITEMS.registerSimpleBlockItem("craft_planks_leaning", CRAFT_PLANKS_LEANING);
    public static final DeferredBlock<FurnitureBlock> CRAFT_PLANKS_PILE = BLOCKS.registerBlock("craft_planks_pile", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_PLANKS_PILE_ITEM = ITEMS.registerSimpleBlockItem("craft_planks_pile", CRAFT_PLANKS_PILE);
    public static final DeferredBlock<FurnitureBlock> CRAFT_COOKING_SHELF = BLOCKS.registerBlock("craft_cooking_shelf", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_COOKING_SHELF_ITEM = ITEMS.registerSimpleBlockItem("craft_cooking_shelf", CRAFT_COOKING_SHELF);
    public static final DeferredBlock<FurnitureBlock> CRAFT_COOKING_STATION = BLOCKS.registerBlock("craft_cooking_station", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_COOKING_STATION_ITEM = ITEMS.registerSimpleBlockItem("craft_cooking_station", CRAFT_COOKING_STATION);
    public static final DeferredBlock<FurnitureBlock> CRAFT_INGREDIENT_SHELF = BLOCKS.registerBlock("craft_ingredient_shelf", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_INGREDIENT_SHELF_ITEM = ITEMS.registerSimpleBlockItem("craft_ingredient_shelf", CRAFT_INGREDIENT_SHELF);
    public static final DeferredBlock<FurnitureBlock> CRAFT_SPICE_JARS = BLOCKS.registerBlock("craft_spice_jars", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_SPICE_JARS_ITEM = ITEMS.registerSimpleBlockItem("craft_spice_jars", CRAFT_SPICE_JARS);
    public static final DeferredBlock<FurnitureBlock> CRAFT_SPICE_WALL_RACK = BLOCKS.registerBlock("craft_spice_wall_rack", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_SPICE_WALL_RACK_ITEM = ITEMS.registerSimpleBlockItem("craft_spice_wall_rack", CRAFT_SPICE_WALL_RACK);
    public static final DeferredBlock<FurnitureBlock> CRAFT_ENCHANTED_BOOK_OPEN = BLOCKS.registerBlock("craft_enchanted_book_open", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_ENCHANTED_BOOK_OPEN_ITEM = ITEMS.registerSimpleBlockItem("craft_enchanted_book_open", CRAFT_ENCHANTED_BOOK_OPEN);
    public static final DeferredBlock<FurnitureBlock> CRAFT_ENCHANTED_BOOK_STACK = BLOCKS.registerBlock("craft_enchanted_book_stack", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_ENCHANTED_BOOK_STACK_ITEM = ITEMS.registerSimpleBlockItem("craft_enchanted_book_stack", CRAFT_ENCHANTED_BOOK_STACK);
    public static final DeferredBlock<FurnitureBlock> CRAFT_ENCHANTED_BOOK_STACK_TALL = BLOCKS.registerBlock("craft_enchanted_book_stack_tall", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_ENCHANTED_BOOK_STACK_TALL_ITEM = ITEMS.registerSimpleBlockItem("craft_enchanted_book_stack_tall", CRAFT_ENCHANTED_BOOK_STACK_TALL);
    public static final DeferredBlock<FurnitureBlock> CRAFT_ENCHANTED_BOOKSHELF = BLOCKS.registerBlock("craft_enchanted_bookshelf", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_ENCHANTED_BOOKSHELF_ITEM = ITEMS.registerSimpleBlockItem("craft_enchanted_bookshelf", CRAFT_ENCHANTED_BOOKSHELF);
    public static final DeferredBlock<FurnitureBlock> CRAFT_ENCHANTING_STATION = BLOCKS.registerBlock("craft_enchanting_station", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_ENCHANTING_STATION_ITEM = ITEMS.registerItem("craft_enchanting_station", props -> new OffsetBlockItem(CRAFT_ENCHANTING_STATION.get(), props), () -> new net.minecraft.world.item.Item.Properties());
    public static final DeferredBlock<FurnitureBlock> CRAFT_ENCHANTING_WALL_SHELF = BLOCKS.registerBlock("craft_enchanting_wall_shelf", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_ENCHANTING_WALL_SHELF_ITEM = ITEMS.registerSimpleBlockItem("craft_enchanting_wall_shelf", CRAFT_ENCHANTING_WALL_SHELF);
    public static final DeferredBlock<FurnitureBlock> CRAFT_EASEL = BLOCKS.registerBlock("craft_easel", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_EASEL_ITEM = ITEMS.registerSimpleBlockItem("craft_easel", CRAFT_EASEL);
    public static final DeferredBlock<FurnitureBlock> CRAFT_PAINT_BUCKETS = BLOCKS.registerBlock("craft_paint_buckets", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_PAINT_BUCKETS_ITEM = ITEMS.registerSimpleBlockItem("craft_paint_buckets", CRAFT_PAINT_BUCKETS);
    public static final DeferredBlock<FurnitureBlock> CRAFT_PAINT_VIALS = BLOCKS.registerBlock("craft_paint_vials", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_PAINT_VIALS_ITEM = ITEMS.registerSimpleBlockItem("craft_paint_vials", CRAFT_PAINT_VIALS);
    public static final DeferredBlock<FurnitureBlock> CRAFT_PAINT_WALL_SHELF = BLOCKS.registerBlock("craft_paint_wall_shelf", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_PAINT_WALL_SHELF_ITEM = ITEMS.registerSimpleBlockItem("craft_paint_wall_shelf", CRAFT_PAINT_WALL_SHELF);
    public static final DeferredBlock<FurnitureBlock> CRAFT_PAINTING_PEGBOARD = BLOCKS.registerBlock("craft_painting_pegboard", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_PAINTING_PEGBOARD_ITEM = ITEMS.registerSimpleBlockItem("craft_painting_pegboard", CRAFT_PAINTING_PEGBOARD);
    public static final DeferredBlock<FurnitureBlock> CRAFT_PAINTING_STATION = BLOCKS.registerBlock("craft_painting_station", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_PAINTING_STATION_ITEM = ITEMS.registerSimpleBlockItem("craft_painting_station", CRAFT_PAINTING_STATION);
    public static final DeferredBlock<FurnitureBlock> CRAFT_FABRIC_SPOOLS = BLOCKS.registerBlock("craft_fabric_spools", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_FABRIC_SPOOLS_ITEM = ITEMS.registerSimpleBlockItem("craft_fabric_spools", CRAFT_FABRIC_SPOOLS);
    public static final DeferredBlock<FurnitureBlock> CRAFT_FABRIC_STACK = BLOCKS.registerBlock("craft_fabric_stack", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_FABRIC_STACK_ITEM = ITEMS.registerSimpleBlockItem("craft_fabric_stack", CRAFT_FABRIC_STACK);
    public static final DeferredBlock<FurnitureBlock> CRAFT_LOOM = BLOCKS.registerBlock("craft_loom", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_LOOM_ITEM = ITEMS.registerSimpleBlockItem("craft_loom", CRAFT_LOOM);
    public static final DeferredBlock<FurnitureBlock> CRAFT_MANNEQUIN = BLOCKS.registerBlock("craft_mannequin", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_MANNEQUIN_ITEM = ITEMS.registerSimpleBlockItem("craft_mannequin", CRAFT_MANNEQUIN);
    public static final DeferredBlock<FurnitureBlock> CRAFT_STANDING_LOOM = BLOCKS.registerBlock("craft_standing_loom", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_STANDING_LOOM_ITEM = ITEMS.registerSimpleBlockItem("craft_standing_loom", CRAFT_STANDING_LOOM);
    public static final DeferredBlock<FurnitureBlock> CRAFT_TAILORING_STATION = BLOCKS.registerBlock("craft_tailoring_station", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> CRAFT_TAILORING_STATION_ITEM = ITEMS.registerSimpleBlockItem("craft_tailoring_station", CRAFT_TAILORING_STATION);

    // =============================================
    // --- Medieval Bathroom Furniture ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> BATH_BAQUET = BLOCKS.registerBlock("bath_baquet", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_BAQUET_ITEM = ITEMS.registerSimpleBlockItem("bath_baquet", BATH_BAQUET);
    public static final DeferredBlock<FurnitureBlock> BATH_BAQUET_FILLED = BLOCKS.registerBlock("bath_baquet_filled", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_BAQUET_FILLED_ITEM = ITEMS.registerSimpleBlockItem("bath_baquet_filled", BATH_BAQUET_FILLED);
    public static final DeferredBlock<FurnitureBlock> BATH_BAQUET_FABRIC = BLOCKS.registerBlock("bath_baquet_fabric", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_BAQUET_FABRIC_ITEM = ITEMS.registerSimpleBlockItem("bath_baquet_fabric", BATH_BAQUET_FABRIC);
    public static final DeferredBlock<FurnitureBlock> BATH_BAQUET_FABRIC_FILLED = BLOCKS.registerBlock("bath_baquet_fabric_filled", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_BAQUET_FABRIC_FILLED_ITEM = ITEMS.registerSimpleBlockItem("bath_baquet_fabric_filled", BATH_BAQUET_FABRIC_FILLED);
    public static final DeferredBlock<FurnitureBlock> BATH_STOOL = BLOCKS.registerBlock("bath_stool", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_STOOL_ITEM = ITEMS.registerSimpleBlockItem("bath_stool", BATH_STOOL);
    public static final DeferredBlock<FurnitureBlock> BATH_BUCKET = BLOCKS.registerBlock("bath_bucket", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_BUCKET_ITEM = ITEMS.registerSimpleBlockItem("bath_bucket", BATH_BUCKET);
    public static final DeferredBlock<FurnitureBlock> BATH_BUCKET_FILLED = BLOCKS.registerBlock("bath_bucket_filled", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_BUCKET_FILLED_ITEM = ITEMS.registerSimpleBlockItem("bath_bucket_filled", BATH_BUCKET_FILLED);
    public static final DeferredBlock<FurnitureBlock> BATH_TOILET = BLOCKS.registerBlock("bath_toilet", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_TOILET_ITEM = ITEMS.registerSimpleBlockItem("bath_toilet", BATH_TOILET);
    public static final DeferredBlock<FurnitureBlock> BATH_TOILET_DOUBLE = BLOCKS.registerBlock("bath_toilet_double", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_TOILET_DOUBLE_ITEM = ITEMS.registerSimpleBlockItem("bath_toilet_double", BATH_TOILET_DOUBLE);
    public static final DeferredBlock<FurnitureBlock> BATH_TABLE = BLOCKS.registerBlock("bath_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_TABLE_ITEM = ITEMS.registerSimpleBlockItem("bath_table", BATH_TABLE);
    public static final DeferredBlock<FurnitureBlock> BATH_BENCH = BLOCKS.registerBlock("bath_bench", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_BENCH_ITEM = ITEMS.registerSimpleBlockItem("bath_bench", BATH_BENCH);
    public static final DeferredBlock<FurnitureBlock> BATH_TOWELS = BLOCKS.registerBlock("bath_towels", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_TOWELS_ITEM = ITEMS.registerSimpleBlockItem("bath_towels", BATH_TOWELS);
    public static final DeferredBlock<FurnitureBlock> BATH_PITCHER = BLOCKS.registerBlock("bath_pitcher", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_PITCHER_ITEM = ITEMS.registerSimpleBlockItem("bath_pitcher", BATH_PITCHER);
    public static final DeferredBlock<FurnitureBlock> BATH_OIL_LAMP = BLOCKS.registerBlock("bath_oil_lamp", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion().lightLevel(state -> 15));
    public static final DeferredItem<BlockItem> BATH_OIL_LAMP_ITEM = ITEMS.registerSimpleBlockItem("bath_oil_lamp", BATH_OIL_LAMP);
    public static final DeferredBlock<FurnitureBlock> BATH_CLOTHESLINE = BLOCKS.registerBlock("bath_clothesline", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_CLOTHESLINE_ITEM = ITEMS.registerSimpleBlockItem("bath_clothesline", BATH_CLOTHESLINE);
    public static final DeferredBlock<FurnitureBlock> BATH_SMALL_TABLE = BLOCKS.registerBlock("bath_small_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_SMALL_TABLE_ITEM = ITEMS.registerSimpleBlockItem("bath_small_table", BATH_SMALL_TABLE);
    public static final DeferredBlock<FurnitureBlock> BATH_BASIN = BLOCKS.registerBlock("bath_basin", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_BASIN_ITEM = ITEMS.registerSimpleBlockItem("bath_basin", BATH_BASIN);
    public static final DeferredBlock<FurnitureBlock> BATH_SOAP = BLOCKS.registerBlock("bath_soap", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_SOAP_ITEM = ITEMS.registerSimpleBlockItem("bath_soap", BATH_SOAP);
    public static final DeferredBlock<FurnitureBlock> BATH_MIRROR = BLOCKS.registerBlock("bath_mirror", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BATH_MIRROR_ITEM = ITEMS.registerSimpleBlockItem("bath_mirror", BATH_MIRROR);

    // =============================================
    // --- Viking Furniture ---
    // =============================================
    public static final DeferredBlock<FurnitureBlock> VIKING_AXE_SHIELD = BLOCKS.registerBlock("viking_axe_shield", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_AXE_SHIELD_ITEM = ITEMS.registerSimpleBlockItem("viking_axe_shield", VIKING_AXE_SHIELD);
    public static final DeferredBlock<FurnitureBlock> VIKING_BUCKET_1 = BLOCKS.registerBlock("viking_bucket_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_BUCKET_1_ITEM = ITEMS.registerSimpleBlockItem("viking_bucket_1", VIKING_BUCKET_1);
    public static final DeferredBlock<FurnitureBlock> VIKING_BUCKET_2 = BLOCKS.registerBlock("viking_bucket_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_BUCKET_2_ITEM = ITEMS.registerSimpleBlockItem("viking_bucket_2", VIKING_BUCKET_2);
    public static final DeferredBlock<FurnitureBlock> VIKING_CABINET_1 = BLOCKS.registerBlock("viking_cabinet_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_CABINET_1_ITEM = ITEMS.registerSimpleBlockItem("viking_cabinet_1", VIKING_CABINET_1);
    public static final DeferredBlock<FurnitureBlock> VIKING_CABINET_2 = BLOCKS.registerBlock("viking_cabinet_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_CABINET_2_ITEM = ITEMS.registerSimpleBlockItem("viking_cabinet_2", VIKING_CABINET_2);
    public static final DeferredBlock<FurnitureBlock> VIKING_STATUE_1 = BLOCKS.registerBlock("viking_statue_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_STATUE_1_ITEM = ITEMS.registerSimpleBlockItem("viking_statue_1", VIKING_STATUE_1);
    public static final DeferredBlock<FurnitureBlock> VIKING_STATUE_2 = BLOCKS.registerBlock("viking_statue_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_STATUE_2_ITEM = ITEMS.registerSimpleBlockItem("viking_statue_2", VIKING_STATUE_2);
    public static final DeferredBlock<FurnitureBlock> VIKING_STATUE_3 = BLOCKS.registerBlock("viking_statue_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_STATUE_3_ITEM = ITEMS.registerSimpleBlockItem("viking_statue_3", VIKING_STATUE_3);
    public static final DeferredBlock<FurnitureBlock> VIKING_STATUE_4 = BLOCKS.registerBlock("viking_statue_4", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_STATUE_4_ITEM = ITEMS.registerSimpleBlockItem("viking_statue_4", VIKING_STATUE_4);
    public static final DeferredBlock<FurnitureBlock> VIKING_STATUE_5 = BLOCKS.registerBlock("viking_statue_5", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_STATUE_5_ITEM = ITEMS.registerSimpleBlockItem("viking_statue_5", VIKING_STATUE_5);
    public static final DeferredBlock<FurnitureBlock> VIKING_STATUE_6 = BLOCKS.registerBlock("viking_statue_6", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_STATUE_6_ITEM = ITEMS.registerSimpleBlockItem("viking_statue_6", VIKING_STATUE_6);
    public static final DeferredBlock<FurnitureBlock> VIKING_STATUE_7 = BLOCKS.registerBlock("viking_statue_7", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_STATUE_7_ITEM = ITEMS.registerSimpleBlockItem("viking_statue_7", VIKING_STATUE_7);
    public static final DeferredBlock<FurnitureBlock> VIKING_STONE_CHAIR_1 = BLOCKS.registerBlock("viking_stone_chair_1", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_STONE_CHAIR_1_ITEM = ITEMS.registerSimpleBlockItem("viking_stone_chair_1", VIKING_STONE_CHAIR_1);
    public static final DeferredBlock<FurnitureBlock> VIKING_STONE_CHAIR_2 = BLOCKS.registerBlock("viking_stone_chair_2", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_STONE_CHAIR_2_ITEM = ITEMS.registerSimpleBlockItem("viking_stone_chair_2", VIKING_STONE_CHAIR_2);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_BED = BLOCKS.registerBlock("viking_wooden_bed", SleepableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_BED_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_bed", VIKING_WOODEN_BED);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_CHAIR_1 = BLOCKS.registerBlock("viking_wooden_chair_1", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_CHAIR_1_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_chair_1", VIKING_WOODEN_CHAIR_1);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_CHAIR_2 = BLOCKS.registerBlock("viking_wooden_chair_2", SittableFurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_CHAIR_2_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_chair_2", VIKING_WOODEN_CHAIR_2);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_HANGING = BLOCKS.registerBlock("viking_wooden_hanging", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_HANGING_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_hanging", VIKING_WOODEN_HANGING);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_PILLAR_1 = BLOCKS.registerBlock("viking_wooden_pillar_1", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_PILLAR_1_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_pillar_1", VIKING_WOODEN_PILLAR_1);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_PILLAR_2 = BLOCKS.registerBlock("viking_wooden_pillar_2", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_PILLAR_2_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_pillar_2", VIKING_WOODEN_PILLAR_2);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_PILLAR_3 = BLOCKS.registerBlock("viking_wooden_pillar_3", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_PILLAR_3_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_pillar_3", VIKING_WOODEN_PILLAR_3);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_PILLAR_4 = BLOCKS.registerBlock("viking_wooden_pillar_4", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_PILLAR_4_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_pillar_4", VIKING_WOODEN_PILLAR_4);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_PILLAR_5 = BLOCKS.registerBlock("viking_wooden_pillar_5", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_PILLAR_5_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_pillar_5", VIKING_WOODEN_PILLAR_5);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_PILLAR_6 = BLOCKS.registerBlock("viking_wooden_pillar_6", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_PILLAR_6_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_pillar_6", VIKING_WOODEN_PILLAR_6);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_PILLAR_7 = BLOCKS.registerBlock("viking_wooden_pillar_7", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_PILLAR_7_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_pillar_7", VIKING_WOODEN_PILLAR_7);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_PILLAR_8 = BLOCKS.registerBlock("viking_wooden_pillar_8", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_PILLAR_8_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_pillar_8", VIKING_WOODEN_PILLAR_8);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_PILLAR_9 = BLOCKS.registerBlock("viking_wooden_pillar_9", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_PILLAR_9_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_pillar_9", VIKING_WOODEN_PILLAR_9);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_PILLAR_10 = BLOCKS.registerBlock("viking_wooden_pillar_10", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_PILLAR_10_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_pillar_10", VIKING_WOODEN_PILLAR_10);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_PILLAR_11 = BLOCKS.registerBlock("viking_wooden_pillar_11", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_PILLAR_11_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_pillar_11", VIKING_WOODEN_PILLAR_11);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_PILLAR_12 = BLOCKS.registerBlock("viking_wooden_pillar_12", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_PILLAR_12_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_pillar_12", VIKING_WOODEN_PILLAR_12);
    public static final DeferredBlock<FurnitureBlock> VIKING_WOODEN_TABLE = BLOCKS.registerBlock("viking_wooden_table", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> VIKING_WOODEN_TABLE_ITEM = ITEMS.registerSimpleBlockItem("viking_wooden_table", VIKING_WOODEN_TABLE);

    // =============================================
    // --- Quest Board ---
    // =============================================
    public static final DeferredBlock<QuestBoardBlock> QUEST_BOARD = BLOCKS.registerBlock("quest_board", QuestBoardBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(3.0f).noOcclusion());
    public static final DeferredItem<BlockItem> QUEST_BOARD_ITEM = ITEMS.registerSimpleBlockItem("quest_board", QUEST_BOARD);

    // --- Creative Tab ---
    public static final Supplier<CreativeModeTab> FURNITURE_TAB = CREATIVE_MODE_TABS.register("furniture_tab", () -> CreativeModeTab.builder()
            .title((Component) Component.literal("MegaMod - Furniture"))
            .icon(() -> new ItemStack((ItemLike) OFFICE_SOFA_ITEM.get()))
            .displayItems((parameters, output) -> {
                // Office Furniture
                output.accept((ItemLike) OFFICE_TABLE_ITEM.get());
                output.accept((ItemLike) OFFICE_CEO_DESK_ITEM.get());
                output.accept((ItemLike) OFFICE_CHAIR_ITEM.get());
                output.accept((ItemLike) OFFICE_CEO_CHAIR_ITEM.get());
                output.accept((ItemLike) OFFICE_CONFERENCE_TABLE_ITEM.get());
                output.accept((ItemLike) OFFICE_PRINTER_ITEM.get());
                output.accept((ItemLike) OFFICE_CUPBOARD_ITEM.get());
                output.accept((ItemLike) OFFICE_BOOKSHELF_ITEM.get());
                output.accept((ItemLike) OFFICE_BOOKSHELF_TALL_ITEM.get());
                output.accept((ItemLike) OFFICE_FILING_CABINET_ITEM.get());
                output.accept((ItemLike) OFFICE_FILE_RACK_ITEM.get());
                output.accept((ItemLike) OFFICE_SOFA_ITEM.get());
                output.accept((ItemLike) OFFICE_SOFA_LARGE_ITEM.get());
                output.accept((ItemLike) OFFICE_LAMP_ITEM.get());
                output.accept((ItemLike) OFFICE_POTTED_PLANT_ITEM.get());
                output.accept((ItemLike) OFFICE_BOARD_SMALL_ITEM.get());
                output.accept((ItemLike) OFFICE_BOARD_LARGE_ITEM.get());
                output.accept((ItemLike) OFFICE_PROJECTOR_SCREEN_ITEM.get());
                output.accept((ItemLike) OFFICE_PROJECTOR_ITEM.get());
                output.accept((ItemLike) OFFICE_RUBBISH_BIN_ITEM.get());
                // Vintage Furniture
                output.accept((ItemLike) VINTAGE_BED_ITEM.get());
                output.accept((ItemLike) VINTAGE_BIG_CUPBOARD_ITEM.get());
                output.accept((ItemLike) VINTAGE_BOOK_SHELF_ITEM.get());
                output.accept((ItemLike) VINTAGE_CARPET_ITEM.get());
                output.accept((ItemLike) VINTAGE_CARPET_ALT_ITEM.get());
                output.accept((ItemLike) VINTAGE_CHAIR_ITEM.get());
                output.accept((ItemLike) VINTAGE_CLOCK_ITEM.get());
                output.accept((ItemLike) VINTAGE_CUPBOARD_ITEM.get());
                output.accept((ItemLike) VINTAGE_DESK_LAMP_ITEM.get());
                output.accept((ItemLike) VINTAGE_FIREPLACE_ITEM.get());
                output.accept((ItemLike) VINTAGE_LEATHER_SOFA_ITEM.get());
                output.accept((ItemLike) VINTAGE_MIRROR_ITEM.get());
                output.accept((ItemLike) VINTAGE_NIGHTSTAND_ITEM.get());
                output.accept((ItemLike) VINTAGE_PAINTING_ITEM.get());
                output.accept((ItemLike) VINTAGE_PIANO_ITEM.get());
                output.accept((ItemLike) VINTAGE_RADIO_ITEM.get());
                output.accept((ItemLike) VINTAGE_SHOWCASE_ITEM.get());
                output.accept((ItemLike) VINTAGE_STANDING_LAMP_ITEM.get());
                output.accept((ItemLike) VINTAGE_TABLE_ITEM.get());
                output.accept((ItemLike) VINTAGE_TABLETOP_LAMP_ITEM.get());
                // Classic Furniture
                output.accept((ItemLike) CLASSIC_CANDLE_ITEM.get());
                output.accept((ItemLike) CLASSIC_CEILING_FAN_ITEM.get());
                output.accept((ItemLike) CLASSIC_CHAIR_ITEM.get());
                output.accept((ItemLike) CLASSIC_CLEANING_SET_ITEM.get());
                output.accept((ItemLike) CLASSIC_CURTAIN_ITEM.get());
                output.accept((ItemLike) CLASSIC_CURTAIN_RED_ITEM.get());
                output.accept((ItemLike) CLASSIC_DOOR_ITEM.get());
                output.accept((ItemLike) CLASSIC_FLOWER_ITEM.get());
                output.accept((ItemLike) CLASSIC_GOLDEN_TREE_ITEM.get());
                output.accept((ItemLike) CLASSIC_HARP_ITEM.get());
                output.accept((ItemLike) CLASSIC_HAT_HANGER_ITEM.get());
                output.accept((ItemLike) CLASSIC_JAR_ITEM.get());
                output.accept((ItemLike) CLASSIC_LONG_TABLE_ITEM.get());
                output.accept((ItemLike) CLASSIC_PAINTING_ITEM.get());
                output.accept((ItemLike) CLASSIC_PHONE_ITEM.get());
                output.accept((ItemLike) CLASSIC_SHOWCASE_CORNER_ITEM.get());
                output.accept((ItemLike) CLASSIC_TABLE_ITEM.get());
                output.accept((ItemLike) CLASSIC_TABLE_LAMP_ITEM.get());
                output.accept((ItemLike) CLASSIC_WALL_LAMP_ITEM.get());
                output.accept((ItemLike) CLASSIC_WALL_LAMP_DOUBLE_ITEM.get());
                // Dungeon Decor
                output.accept((ItemLike) DUNGEON_CAGE_ITEM.get());
                output.accept((ItemLike) DUNGEON_CAGE_WITH_BONE_ITEM.get());
                output.accept((ItemLike) DUNGEON_CHEST_DECOR_ITEM.get());
                output.accept((ItemLike) DUNGEON_CHEST_OPEN_ITEM.get());
                output.accept((ItemLike) DUNGEON_GOLDBAR_ITEM.get());
                output.accept((ItemLike) DUNGEON_GOLDBAR_COIN_ITEM.get());
                output.accept((ItemLike) DUNGEON_GOLDBARS_ITEM.get());
                output.accept((ItemLike) DUNGEON_HEADS_ITEM.get());
                output.accept((ItemLike) DUNGEON_SWORD_BONE_ITEM.get());
                output.accept((ItemLike) DUNGEON_VASE_ITEM.get());
                output.accept((ItemLike) DUNGEON_HANG_FLAG_ITEM.get());
                output.accept((ItemLike) DUNGEON_FLAG_BONE_ITEM.get());
                output.accept((ItemLike) DUNGEON_SKELETON_ITEM.get());
                output.accept((ItemLike) DUNGEON_SKELETON_HEAD_ITEM.get());
                output.accept((ItemLike) DUNGEON_SKELETON_SLEEP_ITEM.get());
                output.accept((ItemLike) DUNGEON_TABLE_DECOR_ITEM.get());
                output.accept((ItemLike) DUNGEON_TABLE_LONG_ITEM.get());
                output.accept((ItemLike) DUNGEON_TORCH_DECOR_ITEM.get());
                output.accept((ItemLike) DUNGEON_WEAPONSTAND_ITEM.get());
                output.accept((ItemLike) DUNGEON_WOOD_BARREL_ITEM.get());
                output.accept((ItemLike) DUNGEON_WOOD_BOX_ITEM.get());
                output.accept((ItemLike) DUNGEON_WOOD_CHAIR_ITEM.get());
                output.accept((ItemLike) DUNGEON_CHAIR_BONE_ITEM.get());
                output.accept((ItemLike) DUNGEON_CHAIR_REST_ITEM.get());
                // Coffee Shop Furniture
                output.accept((ItemLike) COFFEE_BLENDER_ITEM.get());
                output.accept((ItemLike) COFFEE_BOARD_1_ITEM.get());
                output.accept((ItemLike) COFFEE_BOARD_2_ITEM.get());
                output.accept((ItemLike) COFFEE_BREAD_SHOWCASE_ITEM.get());
                output.accept((ItemLike) COFFEE_BREADCOFFEE_1_ITEM.get());
                output.accept((ItemLike) COFFEE_BREADCOFFEE_2_ITEM.get());
                output.accept((ItemLike) COFFEE_CASHIER_TABLE_ITEM.get());
                output.accept((ItemLike) COFFEE_CHAIR_ITEM.get());
                output.accept((ItemLike) COFFEE_COFFEE_MACHINE_ITEM.get());
                output.accept((ItemLike) COFFEE_COUNTER_TABLE_ITEM.get());
                output.accept((ItemLike) COFFEE_GLASS_HANGER_ITEM.get());
                output.accept((ItemLike) COFFEE_HANGING_LAMP_ITEM.get());
                output.accept((ItemLike) COFFEE_PICTURE_WALLPAPER_ITEM.get());
                output.accept((ItemLike) COFFEE_PLANT_POT_ITEM.get());
                output.accept((ItemLike) COFFEE_SHELF_1_ITEM.get());
                output.accept((ItemLike) COFFEE_SHELF_2_ITEM.get());
                output.accept((ItemLike) COFFEE_SHOP_SIGN_ITEM.get());
                output.accept((ItemLike) COFFEE_SIGN_ITEM.get());
                output.accept((ItemLike) COFFEE_SOFA_1_ITEM.get());
                output.accept((ItemLike) COFFEE_SOFA_2_ITEM.get());
                output.accept((ItemLike) COFFEE_TABLE_1_ITEM.get());
                output.accept((ItemLike) COFFEE_TABLE_2_ITEM.get());
                // Medieval Market V1
                output.accept((ItemLike) MARKET_BARREL_ITEM.get());
                output.accept((ItemLike) MARKET_BARRELBERRY_ITEM.get());
                output.accept((ItemLike) MARKET_BARRELSWORD_ITEM.get());
                output.accept((ItemLike) MARKET_BOARDSIGN_ITEM.get());
                output.accept((ItemLike) MARKET_CAMPFIRE_1_ITEM.get());
                output.accept((ItemLike) MARKET_CAMPFIRE_2_ITEM.get());
                output.accept((ItemLike) MARKET_CARGO_1_ITEM.get());
                output.accept((ItemLike) MARKET_CARGO_2_ITEM.get());
                output.accept((ItemLike) MARKET_CART_1_ITEM.get());
                output.accept((ItemLike) MARKET_CART_2_ITEM.get());
                output.accept((ItemLike) MARKET_CHAIR_ITEM.get());
                output.accept((ItemLike) MARKET_CRATE_ITEM.get());
                output.accept((ItemLike) MARKET_EARTHENWARE_ITEM.get());
                output.accept((ItemLike) MARKET_FISHTUB_ITEM.get());
                output.accept((ItemLike) MARKET_MARKETSTALL_1_ITEM.get());
                output.accept((ItemLike) MARKET_MARKETSTALL_2_ITEM.get());
                output.accept((ItemLike) MARKET_MARKETSTALL_3_ITEM.get());
                output.accept((ItemLike) MARKET_MARKETSTALL_4_ITEM.get());
                output.accept((ItemLike) MARKET_POLE_ITEM.get());
                output.accept((ItemLike) MARKET_SHARK_ITEM.get());
                output.accept((ItemLike) MARKET_SHELF_1_ITEM.get());
                output.accept((ItemLike) MARKET_SHELF_2_ITEM.get());
                output.accept((ItemLike) MARKET_TABLE_1_ITEM.get());
                output.accept((ItemLike) MARKET_TABLE_2_ITEM.get());
                output.accept((ItemLike) MARKET_WATERWELL_ITEM.get());
                // Medieval Market V2
                output.accept((ItemLike) MARKET2_BARREL_1_ITEM.get());
                output.accept((ItemLike) MARKET2_BARREL_2_ITEM.get());
                output.accept((ItemLike) MARKET2_BOARD_1_ITEM.get());
                output.accept((ItemLike) MARKET2_BOARDSIGN_1_ITEM.get());
                output.accept((ItemLike) MARKET2_BOX_1_ITEM.get());
                output.accept((ItemLike) MARKET2_CARGO_1_ITEM.get());
                output.accept((ItemLike) MARKET2_CARGO_2_ITEM.get());
                output.accept((ItemLike) MARKET2_CHAIR_1_ITEM.get());
                output.accept((ItemLike) MARKET2_CHAIR_2_ITEM.get());
                output.accept((ItemLike) MARKET2_CRATE_1_ITEM.get());
                output.accept((ItemLike) MARKET2_CRATE_2_ITEM.get());
                output.accept((ItemLike) MARKET2_CRATE_3_ITEM.get());
                output.accept((ItemLike) MARKET2_CRATE_4_ITEM.get());
                output.accept((ItemLike) MARKET2_MARKETSTALL_1_ITEM.get());
                output.accept((ItemLike) MARKET2_MARKETSTALL_2_ITEM.get());
                output.accept((ItemLike) MARKET2_MARKETSTALL_3_ITEM.get());
                output.accept((ItemLike) MARKET2_MARKETSTALL_4_ITEM.get());
                output.accept((ItemLike) MARKET2_SHELF_1_ITEM.get());
                output.accept((ItemLike) MARKET2_TABLE_1_ITEM.get());
                output.accept((ItemLike) MARKET2_TABLE_2_ITEM.get());
                // Casino Decoration V1
                output.accept((ItemLike) CASINO_BARRIER_ITEM.get());
                output.accept((ItemLike) CASINO_BUSH_ITEM.get());
                output.accept((ItemLike) CASINO_CARD_AND_TOKEN_ITEM.get());
                output.accept((ItemLike) CASINO_CHAIR_BAR_BROWN_ITEM.get());
                output.accept((ItemLike) CASINO_CHAIR_BAR_RED_ITEM.get());
                output.accept((ItemLike) CASINO_CHAIR_BROWN_ITEM.get());
                output.accept((ItemLike) CASINO_CHIPS_ITEM.get());
                output.accept((ItemLike) CASINO_GAME_BIGWIN_ITEM.get());
                output.accept((ItemLike) CASINO_GAME_SLOT_ITEM.get());
                output.accept((ItemLike) CASINO_MONITOR_ITEM.get());
                output.accept((ItemLike) CASINO_POT_TREE_ITEM.get());
                output.accept((ItemLike) CASINO_SOFA_RED_ITEM.get());
                output.accept((ItemLike) CASINO_SOFA_RED_NO_REST_ITEM.get());
                output.accept((ItemLike) CASINO_SOFA_RED_SINGLE_ITEM.get());
                output.accept((ItemLike) CASINO_TABLE_BILLIARDS_ITEM.get());
                output.accept((ItemLike) CASINO_TABLE_BILLIARDS_STICK_STAND_ITEM.get());
                output.accept((ItemLike) CASINO_TABLE_BLACKJACK_ITEM.get());
                output.accept((ItemLike) CASINO_TABLE_BLANK_ITEM.get());
                output.accept((ItemLike) CASINO_TABLE_CRAPS_ITEM.get());
                output.accept((ItemLike) CASINO_TABLE_ROULETTE_ITEM.get());
                output.accept((ItemLike) CASINO_TABLE_WOOD_ITEM.get());
                output.accept((ItemLike) CASINO_VENDING_MACHINE_ITEM.get());
                // Casino Decoration V2
                output.accept((ItemLike) CASINO2_ASHTRAY_ITEM.get());
                output.accept((ItemLike) CASINO2_BACCARAT_MACHINE_ITEM.get());
                output.accept((ItemLike) CASINO2_BUFFALO_SLOT_MACHINE_ITEM.get());
                output.accept((ItemLike) CASINO2_CHAIR_RED_ITEM.get());
                output.accept((ItemLike) CASINO2_CHAIR_YELLOW_ITEM.get());
                output.accept((ItemLike) CASINO2_CHIP_SET_ITEM.get());
                output.accept((ItemLike) CASINO2_DOGGIE_CASH_ITEM.get());
                output.accept((ItemLike) CASINO2_GAMBLING_GAME_MACHINE_ITEM.get());
                output.accept((ItemLike) CASINO2_LADY_LED_SIGN_ITEM.get());
                output.accept((ItemLike) CASINO2_LED_SIGN_ITEM.get());
                output.accept((ItemLike) CASINO2_MADONNA_GAMBLING_MACHINE_ITEM.get());
                output.accept((ItemLike) CASINO2_POKER_SIGN_ITEM.get());
                output.accept((ItemLike) CASINO2_RED_ARMREST_CHAIR_ITEM.get());
                output.accept((ItemLike) CASINO2_ROULETTE_ITEM.get());
                output.accept((ItemLike) CASINO2_STACK_CARD_ITEM.get());
                output.accept((ItemLike) CASINO2_TABLE_ITEM.get());
                output.accept((ItemLike) CASINO2_VENEZIA_SLOT_ITEM.get());
                output.accept((ItemLike) CASINO2_WHEEL_MACHINE_ITEM.get());
                output.accept((ItemLike) CASINO2_WOMAN_PAINTING_ITEM.get());
                output.accept((ItemLike) CASINO2_YELLOW_ARMREST_CHAIR_ITEM.get());
                // Farmer Decorations
                output.accept((ItemLike) FARMER_BEER_ITEM.get());
                output.accept((ItemLike) FARMER_CORN_ITEM.get());
                output.accept((ItemLike) FARMER_CORN_BASKET_ITEM.get());
                output.accept((ItemLike) FARMER_DOOR_ITEM.get());
                output.accept((ItemLike) FARMER_FENCE_ITEM.get());
                output.accept((ItemLike) FARMER_FENCE_CORNER_ITEM.get());
                output.accept((ItemLike) FARMER_FIREWOOD_ITEM.get());
                output.accept((ItemLike) FARMER_FLOWER_ITEM.get());
                output.accept((ItemLike) FARMER_HAY_WITH_SPADE_ITEM.get());
                output.accept((ItemLike) FARMER_LOG_AXE_ITEM.get());
                output.accept((ItemLike) FARMER_MILK_TANK_ITEM.get());
                output.accept((ItemLike) FARMER_PLATE_RICE_ITEM.get());
                output.accept((ItemLike) FARMER_POND_ITEM.get());
                output.accept((ItemLike) FARMER_RICE_BASKET_ITEM.get());
                output.accept((ItemLike) FARMER_SCARE_CROW_ITEM.get());
                output.accept((ItemLike) FARMER_STONE_ITEM.get());
                output.accept((ItemLike) FARMER_TABLE_ITEM.get());
                output.accept((ItemLike) FARMER_TABLE_SAWING_ITEM.get());
                output.accept((ItemLike) FARMER_TOOLS_STAND_ITEM.get());
                output.accept((ItemLike) FARMER_WHEELBARROW_ITEM.get());
                // Caribbean Vacation
                output.accept((ItemLike) CARIBBEAN_ARM_CHAIR_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_BED_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_COUNTER_CHAIR_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_HANGING_SEAT_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_BEACH_HANGING_CHAIR_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_SHIP_WHEEL_TABLE_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_PLANT_DESK_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_CARPET_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_HANGING_LAMP_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_UMBRELLA_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_PALM_TREE_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_BEACH_SIGN_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_COCONUT_SET_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_HAND_WASHING_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_ICECREAMBOX_BIKE_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_AQUATRIKES_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_SHIP_WHEEL_CLOCK_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_SUNSET_PAINTING_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_SWIM_RING_ITEM.get());
                output.accept((ItemLike) CARIBBEAN_WELCOME_FLOWER_ITEM.get());
                // Master Bedroom
                output.accept((ItemLike) BEDROOM_BED_ITEM.get());
                output.accept((ItemLike) BEDROOM_WARDROBE_ITEM.get());
                output.accept((ItemLike) BEDROOM_DRESSING_TABLE_ITEM.get());
                output.accept((ItemLike) BEDROOM_CHAIR_DRESSING_TABLE_ITEM.get());
                output.accept((ItemLike) BEDROOM_DRAWER_CABINET_ITEM.get());
                output.accept((ItemLike) BEDROOM_BOOKSHELF_ITEM.get());
                output.accept((ItemLike) BEDROOM_SHELF_ITEM.get());
                output.accept((ItemLike) BEDROOM_WORK_TABLE_ITEM.get());
                output.accept((ItemLike) BEDROOM_SMALL_TABLE_ITEM.get());
                output.accept((ItemLike) BEDROOM_TV_STAND_ITEM.get());
                output.accept((ItemLike) BEDROOM_SOFA_BENCH_ITEM.get());
                output.accept((ItemLike) BEDROOM_SOFA_CHAIR_ITEM.get());
                output.accept((ItemLike) BEDROOM_LAMP_ITEM.get());
                output.accept((ItemLike) BEDROOM_DRAWER_LAMP_ITEM.get());
                output.accept((ItemLike) BEDROOM_MIRROR_ITEM.get());
                output.accept((ItemLike) BEDROOM_CARPET_ITEM.get());
                output.accept((ItemLike) BEDROOM_BASKET_ITEM.get());
                output.accept((ItemLike) BEDROOM_BIN_ITEM.get());
                output.accept((ItemLike) BEDROOM_VASE_ITEM.get());
                output.accept((ItemLike) BEDROOM_PICTURE_FRAME_ITEM.get());
                output.accept((ItemLike) BEDROOM_DESKTOP_PHOTO_FRAME_ITEM.get());
                // Modern Furniture
                output.accept((ItemLike) MODERN_BED_ITEM.get());
                output.accept((ItemLike) MODERN_SOFA_01_ITEM.get());
                output.accept((ItemLike) MODERN_SOFA_02_ITEM.get());
                output.accept((ItemLike) MODERN_CHAIR_ITEM.get());
                output.accept((ItemLike) MODERN_COMPUTER_CHAIR_ITEM.get());
                output.accept((ItemLike) MODERN_TABLE_01_ITEM.get());
                output.accept((ItemLike) MODERN_TABLE_02_ITEM.get());
                output.accept((ItemLike) MODERN_TABLE_03_ITEM.get());
                output.accept((ItemLike) MODERN_COMPUTER_DESK_ITEM.get());
                output.accept((ItemLike) MODERN_CABINET_ITEM.get());
                output.accept((ItemLike) MODERN_WARDROBE_01_ITEM.get());
                output.accept((ItemLike) MODERN_WARDROBE_02_ITEM.get());
                output.accept((ItemLike) MODERN_SHELF_ITEM.get());
                output.accept((ItemLike) MODERN_LAMP_ITEM.get());
                output.accept((ItemLike) MODERN_TV_ITEM.get());
                output.accept((ItemLike) MODERN_MACBOOK_ITEM.get());
                output.accept((ItemLike) MODERN_CARPET_ITEM.get());
                output.accept((ItemLike) MODERN_PICTURE_ITEM.get());
                output.accept((ItemLike) MODERN_BOARD_ITEM.get());
                output.accept((ItemLike) MODERN_PLANTPOT_ITEM.get());
                // Dungeon Tavern
                output.accept((ItemLike) BL_BAR_STOOL_ITEM.get());
                output.accept((ItemLike) BL_BARREL_TABLE_ITEM.get());
                output.accept((ItemLike) BL_BARREL_TABLE_2_ITEM.get());
                output.accept((ItemLike) BL_BOTTLE_WINE_1_ITEM.get());
                output.accept((ItemLike) BL_BOTTLE_WINE_2_ITEM.get());
                output.accept((ItemLike) BL_BOTTLE_WINE_3_ITEM.get());
                output.accept((ItemLike) BL_BOTTLE_WINE_STACK_ITEM.get());
                output.accept((ItemLike) BL_GLASS_BEER_ITEM.get());
                output.accept((ItemLike) BL_GLASS_WINE_ITEM.get());
                output.accept((ItemLike) BL_TABLE_LARGE_ITEM.get());
                output.accept((ItemLike) BL_TABLE_LARGE_2_ITEM.get());
                output.accept((ItemLike) BL_TAVERN_BENCH_ITEM.get());
                output.accept((ItemLike) BL_TAVERN_CABINET_1_ITEM.get());
                output.accept((ItemLike) BL_TAVERN_CABINET_2_ITEM.get());
                output.accept((ItemLike) BL_TAVERN_COUNTER_ITEM.get());
                output.accept((ItemLike) BL_WALL_SHELF_ITEM.get());
                // Bank
                output.accept((ItemLike) BANK_ATM_ITEM.get());
                output.accept((ItemLike) BANK_BARRIER_ITEM.get());
                output.accept((ItemLike) BANK_CAMERA_ITEM.get());
                output.accept((ItemLike) BANK_CASE_ITEM.get());
                output.accept((ItemLike) BANK_CHAIR_ITEM.get());
                output.accept((ItemLike) BANK_CHAIR_1_ITEM.get());
                output.accept((ItemLike) BANK_COMPUTER_ITEM.get());
                output.accept((ItemLike) BANK_DOCUMENT_ITEM.get());
                output.accept((ItemLike) BANK_GLASS_ITEM.get());
                output.accept((ItemLike) BANK_GREEK_BALANCE_ITEM.get());
                output.accept((ItemLike) BANK_LOCKER_ITEM.get());
                output.accept((ItemLike) BANK_LOCKER_OPEN_ITEM.get());
                output.accept((ItemLike) BANK_MONEY_ITEM.get());
                output.accept((ItemLike) BANK_MONEY_1_ITEM.get());
                output.accept((ItemLike) BANK_MONEY_CASE_ITEM.get());
                output.accept((ItemLike) BANK_POT_ITEM.get());
                output.accept((ItemLike) BANK_SAFE_DOOR_ITEM.get());
                output.accept((ItemLike) BANK_SIGN_ITEM.get());
                output.accept((ItemLike) BANK_TABLE_ITEM.get());
                output.accept((ItemLike) BANK_TALL_CHAIR_ITEM.get());
                // Medieval
                output.accept((ItemLike) MED_ARM_CHAIR_ITEM.get());
                output.accept((ItemLike) MED_BED_BUNK_ITEM.get());
                output.accept((ItemLike) MED_BED_DOUBLE_ITEM.get());
                output.accept((ItemLike) MED_BED_SINGLE_ITEM.get());
                output.accept((ItemLike) MED_BOOK_OPEN_ITEM.get());
                output.accept((ItemLike) MED_BOOK_STACK_HORIZONTAL_1_ITEM.get());
                output.accept((ItemLike) MED_BOOK_STACK_HORIZONTAL_2_ITEM.get());
                output.accept((ItemLike) MED_BOOK_STACK_VERTICAL_1_ITEM.get());
                output.accept((ItemLike) MED_BOOK_STACK_VERTICAL_2_ITEM.get());
                output.accept((ItemLike) MED_CANDLE_DISH_ITEM.get());
                output.accept((ItemLike) MED_CHAIR_ITEM.get());
                output.accept((ItemLike) MED_CHAIR_CUSHION_ITEM.get());
                output.accept((ItemLike) MED_COFFEE_TABLE_ITEM.get());
                output.accept((ItemLike) MED_COFFEE_TABLE_CLOTH_ITEM.get());
                output.accept((ItemLike) MED_DESK_ITEM.get());
                output.accept((ItemLike) MED_DRESSER_LOW_ITEM.get());
                output.accept((ItemLike) MED_DRESSER_TALL_ITEM.get());
                output.accept((ItemLike) MED_END_TABLE_ITEM.get());
                output.accept((ItemLike) MED_FIREPLACE_ITEM.get());
                output.accept((ItemLike) MED_LAMP_ITEM.get());
                output.accept((ItemLike) MED_LAMP_HANGING_ITEM.get());
                output.accept((ItemLike) MED_LAMP_TALL_ITEM.get());
                output.accept((ItemLike) MED_POT_1_ITEM.get());
                output.accept((ItemLike) MED_POT_2_ITEM.get());
                output.accept((ItemLike) MED_POT_3_ITEM.get());
                output.accept((ItemLike) MED_SCROLL_OPEN_ITEM.get());
                output.accept((ItemLike) MED_SCROLL_STACK_ITEM.get());
                output.accept((ItemLike) MED_SOFA_ITEM.get());
                output.accept((ItemLike) MED_STOOL_ITEM.get());
                output.accept((ItemLike) MED_STOOL_CUSHION_ITEM.get());
                output.accept((ItemLike) MED_TABLE_LONG_ITEM.get());
                output.accept((ItemLike) MED_TABLE_SMALL_ITEM.get());
                output.accept((ItemLike) MED_TABLE_SMALL_CLOTH_ITEM.get());
                output.accept((ItemLike) MED_BULLETIN_BOARD_ITEM.get());
                output.accept((ItemLike) MED_BULLETIN_BOARD_SMALL_ITEM.get());
                output.accept((ItemLike) MED_BULLETIN_BOARD_WALL_ITEM.get());
                output.accept((ItemLike) MED_CANOPY_FLAT_ITEM.get());
                output.accept((ItemLike) MED_CANOPY_SLOPED_ITEM.get());
                output.accept((ItemLike) MED_CHALK_SIGN_ITEM.get());
                output.accept((ItemLike) MED_CHALK_SIGN_HANGING_ITEM.get());
                output.accept((ItemLike) MED_CHEST_ITEM.get());
                output.accept((ItemLike) MED_CHEST_OPEN_ITEM.get());
                output.accept((ItemLike) MED_COINS_1_ITEM.get());
                output.accept((ItemLike) MED_COINS_2_ITEM.get());
                output.accept((ItemLike) MED_COINS_3_ITEM.get());
                output.accept((ItemLike) MED_CRATE_BREAD_ITEM.get());
                output.accept((ItemLike) MED_CRATE_CARROTS_ITEM.get());
                output.accept((ItemLike) MED_CRATE_DISPLAY_BREAD_ITEM.get());
                output.accept((ItemLike) MED_CRATE_DISPLAY_CARROTS_ITEM.get());
                output.accept((ItemLike) MED_CRATE_DISPLAY_EMPTY_ITEM.get());
                output.accept((ItemLike) MED_CRATE_EMPTY_ITEM.get());
                output.accept((ItemLike) MED_CRATE_STACK_ITEM.get());
                output.accept((ItemLike) MED_PAPERS_HANGING_1_ITEM.get());
                output.accept((ItemLike) MED_PAPERS_HANGING_2_ITEM.get());
                output.accept((ItemLike) MED_PAPERS_HANGING_3_ITEM.get());
                output.accept((ItemLike) MED_SACK_ITEM.get());
                output.accept((ItemLike) MED_SHIPPING_CRATE_ITEM.get());
                output.accept((ItemLike) MED_SHIPPING_CRATE_TRIPLE_STACK_ITEM.get());
                output.accept((ItemLike) MED_SHOP_CART_ITEM.get());
                output.accept((ItemLike) MED_STREAMER_POST_ITEM.get());
                output.accept((ItemLike) MED_STREAMERS_HANGING_ITEM.get());
                output.accept((ItemLike) MED_WAGON_ITEM.get());
                output.accept((ItemLike) MED_WAGON_FULL_ITEM.get());
                output.accept((ItemLike) MED_BROWN_MUSHROOM_PATCH_ITEM.get());
                output.accept((ItemLike) MED_BROWN_MUSHROOM_PATCH_LARGE_ITEM.get());
                output.accept((ItemLike) MED_CATTAILS_ITEM.get());
                output.accept((ItemLike) MED_CLOVER_FLOWERS_ITEM.get());
                output.accept((ItemLike) MED_CLOVERS_ITEM.get());
                output.accept((ItemLike) MED_CRYSTAL_ITEM.get());
                output.accept((ItemLike) MED_GLOW_MUSHROOM_PATCH_ITEM.get());
                output.accept((ItemLike) MED_GLOW_MUSHROOM_PATCH_LARGE_ITEM.get());
                output.accept((ItemLike) MED_LOG_MUSHROOM_ITEM.get());
                output.accept((ItemLike) MED_LOG_MUSHROOM_CORNER_ITEM.get());
                output.accept((ItemLike) MED_LOG_PILE_LARGE_ITEM.get());
                output.accept((ItemLike) MED_LOG_PILE_LARGE_OVERGROWN_ITEM.get());
                output.accept((ItemLike) MED_LOG_PILE_SMALL_1_ITEM.get());
                output.accept((ItemLike) MED_LOG_PILE_SMALL_2_ITEM.get());
                output.accept((ItemLike) MED_LOG_PILE_SMALL_3_ITEM.get());
                output.accept((ItemLike) MED_ORANGE_MUSHROOM_PATCH_ITEM.get());
                output.accept((ItemLike) MED_ORANGE_MUSHROOM_PATCH_LARGE_ITEM.get());
                output.accept((ItemLike) MED_ORE_ITEM.get());
                output.accept((ItemLike) MED_PEBBLES_1_ITEM.get());
                output.accept((ItemLike) MED_PEBBLES_2_ITEM.get());
                output.accept((ItemLike) MED_PEBBLES_3_ITEM.get());
                output.accept((ItemLike) MED_PLANT_ITEM.get());
                output.accept((ItemLike) MED_RED_MUSHROOM_PATCH_ITEM.get());
                output.accept((ItemLike) MED_RED_MUSHROOM_PATCH_LARGE_ITEM.get());
                output.accept((ItemLike) MED_ROCK_1_ITEM.get());
                output.accept((ItemLike) MED_ROCK_2_ITEM.get());
                output.accept((ItemLike) MED_ROCK_3_ITEM.get());
                output.accept((ItemLike) MED_ROCK_4_ITEM.get());
                // --- New Furniture Packs (Auto-Generated) ---
                output.accept((ItemLike) LR_CHAIR_1_ITEM.get());
                output.accept((ItemLike) LR_CHAIR_2_ITEM.get());
                output.accept((ItemLike) LR_CUPBOARD_ITEM.get());
                output.accept((ItemLike) LR_FIREPLACE_ITEM.get());
                output.accept((ItemLike) LR_FRAME_1_ITEM.get());
                output.accept((ItemLike) LR_FRAME_2_ITEM.get());
                output.accept((ItemLike) LR_LAMP_1_ITEM.get());
                output.accept((ItemLike) LR_LAMP_2_ITEM.get());
                output.accept((ItemLike) LR_PIANO_ITEM.get());
                output.accept((ItemLike) LR_PLANT_1_ITEM.get());
                output.accept((ItemLike) LR_PLANT_2_ITEM.get());
                output.accept((ItemLike) LR_PLANT_3_ITEM.get());
                output.accept((ItemLike) LR_SHELF_1_ITEM.get());
                output.accept((ItemLike) LR_SHELF_2_ITEM.get());
                output.accept((ItemLike) LR_SOFA_1_ITEM.get());
                output.accept((ItemLike) LR_SOFA_2_ITEM.get());
                output.accept((ItemLike) LR_SOFA_3_ITEM.get());
                output.accept((ItemLike) LR_SOFA_4_ITEM.get());
                output.accept((ItemLike) LR_SOUND_SYSTEM_ITEM.get());
                output.accept((ItemLike) LR_TABLE_1_ITEM.get());
                output.accept((ItemLike) LR_TABLE_2_ITEM.get());
                output.accept((ItemLike) LR_TV_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_BED_COLOR_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_BED_DYE_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_BOOK_1_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_BOOK_2_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_BOOK_3_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_BOOK_4_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_BRICK_1_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_DOUBLE_BED_COLOR_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_DOUBLE_BED_DYE_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_DOUBLE_CHAIR_COLOR_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_DOUBLE_CHAIR_DYE_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_FAN_COLOR_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_FENCE_1_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_FIRE_STAND_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_FLOWERPOT_1_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_FLOWERPOT_2_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_FLOWERPOT_3_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_FULL_TABLE_COLOR_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_FULL_TABLE_COLOR_1_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_OAKLOG_1_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_OAKLOG_2_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_OAKLOG_3_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_ONE_CHAIR_1_COLOR_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_ONE_CHAIR_1_DYE_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_ONE_CHAIR_COLOR_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_ONE_CHAIR_COLOR_1_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_ONE_CHAIR_COLOR_2_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_ONE_CHAIR_DYE_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_ONE_CHAIR_DYE_2_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_ONE_TABLE_COLOR_1_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_SCAFFOLDING_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_SIGN_BANK_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_SIGN_BLACKSMITH_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_SIGN_COSMETIC_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_SIGN_DEFAULT_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_SIGN_ENCHANTED_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_SIGN_FURNITURE_ITEM.get());
                output.accept((ItemLike) MS2_MEDIEVAL_SIGN_POTION_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_BOX_1_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_BOX_2_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_BRIDGE_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_BRIDGE_MID_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_BRIDGE_PATH_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_1_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_2_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_MID_1_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_MID_2_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_BRIDGE_STAND_MID_3_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_CAGE_1_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_CAGE_2_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_CAMP_1_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_CHAIR_1_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_CHAIR_2_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_GATE_1_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_GATE_2_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_LADDER_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_LOG_1_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_LOG_2_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_LOG_3_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_LOG_4_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_LOG_5_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_LOG_6_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_LOG_7_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_LOG_8_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_LOG_9_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_ROCK_1_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_STANDLIGHT_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_STANDLIGHT_2_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_SUPPORT_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_SUPPORT_H3_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_TABE_1_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_TORCH_1_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_TORCH_2_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_TORCH_3_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_TOWER_1_ITEM.get());
                output.accept((ItemLike) MS4_MEDIEVAL_PACK_V4_WHEEL_1_ITEM.get());
                output.accept((ItemLike) MKT_MEDIEVAL_MARKET_BAG_ITEM.get());
                output.accept((ItemLike) MKT_MEDIEVAL_MARKET_BARELL_ITEM.get());
                output.accept((ItemLike) MKT_MEDIEVAL_MARKET_BATH_ITEM.get());
                output.accept((ItemLike) MKT_MEDIEVAL_MARKET_BED_ITEM.get());
                output.accept((ItemLike) MKT_MEDIEVAL_MARKET_BENCH_ITEM.get());
                output.accept((ItemLike) MKT_MEDIEVAL_MARKET_BOX_ITEM.get());
                output.accept((ItemLike) MKT_MEDIEVAL_MARKET_BOX_APPLES_ITEM.get());
                output.accept((ItemLike) MKT_MEDIEVAL_MARKET_BOX_BREAD_ITEM.get());
                output.accept((ItemLike) MKT_MEDIEVAL_MARKET_CAMPFIRE_ITEM.get());
                output.accept((ItemLike) MKT_MEDIEVAL_MARKET_CHAIR_ITEM.get());
                output.accept((ItemLike) MKT_MEDIEVAL_MARKET_CHEST_ITEM.get());
                output.accept((ItemLike) PARK_BENCH_ITEM.get());
                output.accept((ItemLike) PARK_BIRD_HOUSE_ITEM.get());
                output.accept((ItemLike) PARK_BOX_ITEM.get());
                output.accept((ItemLike) PARK_BOX_CONSTRUCTION_ITEM.get());
                output.accept((ItemLike) PARK_CONCRET_POT_PLANT_ITEM.get());
                output.accept((ItemLike) PARK_FENCE_ITEM.get());
                output.accept((ItemLike) PARK_LANTERN_BOX_ITEM.get());
                output.accept((ItemLike) PARK_LIGHT_WOODEN_PALLET_ITEM.get());
                output.accept((ItemLike) PARK_LITTLE_BOX_ITEM.get());
                output.accept((ItemLike) PARK_LOG_BENCH_ITEM.get());
                output.accept((ItemLike) PARK_PICNIC_TABLE_ITEM.get());
                output.accept((ItemLike) PARK_PICNIC_TABLE_NAPPE_ITEM.get());
                output.accept((ItemLike) PARK_STELE_1_ITEM.get());
                output.accept((ItemLike) PARK_STELE_2_ITEM.get());
                output.accept((ItemLike) PARK_STELE_3_ITEM.get());
                output.accept((ItemLike) PARK_STELE_4_ITEM.get());
                output.accept((ItemLike) PARK_STELE_5_ITEM.get());
                output.accept((ItemLike) PARK_STREET_LAMP_ITEM.get());
                output.accept((ItemLike) PARK_STREET_LAMP_DOUBLE_ITEM.get());
                output.accept((ItemLike) PARK_STREET_LAMP_QUADRUPLE_ITEM.get());
                output.accept((ItemLike) PARK_STREET_LAMP_UPPER_ITEM.get());
                output.accept((ItemLike) PARK_STREET_LAMP_UPPER_DOUBLE_ITEM.get());
                output.accept((ItemLike) PARK_STREET_LAMP_UPPER_QUADRUPLE_ITEM.get());
                output.accept((ItemLike) PARK_STREET_LAMP_UPPER_TRIPLE_ITEM.get());
                output.accept((ItemLike) PARK_SWING_ITEM.get());
                output.accept((ItemLike) PARK_UNDER_CONSTRUCTION_ITEM.get());
                output.accept((ItemLike) PARK_WOODEN_FLOWER_POT_ITEM.get());
                output.accept((ItemLike) PARK_WOODEN_GROUND_PLANKS_ITEM.get());
                output.accept((ItemLike) PARK_WOODEN_PALLET_ITEM.get());
                output.accept((ItemLike) CRAFT_ALCHEMY_STATION_ITEM.get());
                output.accept((ItemLike) CRAFT_BREWING_STAND_ITEM.get());
                output.accept((ItemLike) CRAFT_BREWING_TABLE_ITEM.get());
                output.accept((ItemLike) CRAFT_HERB_WALL_RACK_ITEM.get());
                output.accept((ItemLike) CRAFT_POTION_SHELF_ITEM.get());
                output.accept((ItemLike) CRAFT_BLUEPRINT_ITEM.get());
                output.accept((ItemLike) CRAFT_CARPENTRY_STATION_ITEM.get());
                output.accept((ItemLike) CRAFT_LOG_CUTTING_STAND_ITEM.get());
                output.accept((ItemLike) CRAFT_PLANKS_LEANING_ITEM.get());
                output.accept((ItemLike) CRAFT_PLANKS_PILE_ITEM.get());
                output.accept((ItemLike) CRAFT_COOKING_SHELF_ITEM.get());
                output.accept((ItemLike) CRAFT_COOKING_STATION_ITEM.get());
                output.accept((ItemLike) CRAFT_INGREDIENT_SHELF_ITEM.get());
                output.accept((ItemLike) CRAFT_SPICE_JARS_ITEM.get());
                output.accept((ItemLike) CRAFT_SPICE_WALL_RACK_ITEM.get());
                output.accept((ItemLike) CRAFT_ENCHANTED_BOOK_OPEN_ITEM.get());
                output.accept((ItemLike) CRAFT_ENCHANTED_BOOK_STACK_ITEM.get());
                output.accept((ItemLike) CRAFT_ENCHANTED_BOOK_STACK_TALL_ITEM.get());
                output.accept((ItemLike) CRAFT_ENCHANTED_BOOKSHELF_ITEM.get());
                output.accept((ItemLike) CRAFT_ENCHANTING_STATION_ITEM.get());
                output.accept((ItemLike) CRAFT_ENCHANTING_WALL_SHELF_ITEM.get());
                output.accept((ItemLike) CRAFT_EASEL_ITEM.get());
                output.accept((ItemLike) CRAFT_PAINT_BUCKETS_ITEM.get());
                output.accept((ItemLike) CRAFT_PAINT_VIALS_ITEM.get());
                output.accept((ItemLike) CRAFT_PAINT_WALL_SHELF_ITEM.get());
                output.accept((ItemLike) CRAFT_PAINTING_PEGBOARD_ITEM.get());
                output.accept((ItemLike) CRAFT_PAINTING_STATION_ITEM.get());
                output.accept((ItemLike) CRAFT_FABRIC_SPOOLS_ITEM.get());
                output.accept((ItemLike) CRAFT_FABRIC_STACK_ITEM.get());
                output.accept((ItemLike) CRAFT_LOOM_ITEM.get());
                output.accept((ItemLike) CRAFT_MANNEQUIN_ITEM.get());
                output.accept((ItemLike) CRAFT_STANDING_LOOM_ITEM.get());
                output.accept((ItemLike) CRAFT_TAILORING_STATION_ITEM.get());
                // Medieval Bathroom
                output.accept((ItemLike) BATH_BAQUET_ITEM.get());
                output.accept((ItemLike) BATH_BAQUET_FILLED_ITEM.get());
                output.accept((ItemLike) BATH_BAQUET_FABRIC_ITEM.get());
                output.accept((ItemLike) BATH_BAQUET_FABRIC_FILLED_ITEM.get());
                output.accept((ItemLike) BATH_STOOL_ITEM.get());
                output.accept((ItemLike) BATH_BUCKET_ITEM.get());
                output.accept((ItemLike) BATH_BUCKET_FILLED_ITEM.get());
                output.accept((ItemLike) BATH_TOILET_ITEM.get());
                output.accept((ItemLike) BATH_TOILET_DOUBLE_ITEM.get());
                output.accept((ItemLike) BATH_TABLE_ITEM.get());
                output.accept((ItemLike) BATH_BENCH_ITEM.get());
                output.accept((ItemLike) BATH_TOWELS_ITEM.get());
                output.accept((ItemLike) BATH_PITCHER_ITEM.get());
                output.accept((ItemLike) BATH_OIL_LAMP_ITEM.get());
                output.accept((ItemLike) BATH_CLOTHESLINE_ITEM.get());
                output.accept((ItemLike) BATH_SMALL_TABLE_ITEM.get());
                output.accept((ItemLike) BATH_BASIN_ITEM.get());
                output.accept((ItemLike) BATH_SOAP_ITEM.get());
                output.accept((ItemLike) BATH_MIRROR_ITEM.get());
                // Viking Furniture
                output.accept((ItemLike) VIKING_AXE_SHIELD_ITEM.get());
                output.accept((ItemLike) VIKING_BUCKET_1_ITEM.get());
                output.accept((ItemLike) VIKING_BUCKET_2_ITEM.get());
                output.accept((ItemLike) VIKING_CABINET_1_ITEM.get());
                output.accept((ItemLike) VIKING_CABINET_2_ITEM.get());
                output.accept((ItemLike) VIKING_STATUE_1_ITEM.get());
                output.accept((ItemLike) VIKING_STATUE_2_ITEM.get());
                output.accept((ItemLike) VIKING_STATUE_3_ITEM.get());
                output.accept((ItemLike) VIKING_STATUE_4_ITEM.get());
                output.accept((ItemLike) VIKING_STATUE_5_ITEM.get());
                output.accept((ItemLike) VIKING_STATUE_6_ITEM.get());
                output.accept((ItemLike) VIKING_STATUE_7_ITEM.get());
                output.accept((ItemLike) VIKING_STONE_CHAIR_1_ITEM.get());
                output.accept((ItemLike) VIKING_STONE_CHAIR_2_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_BED_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_CHAIR_1_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_CHAIR_2_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_HANGING_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_PILLAR_1_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_PILLAR_2_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_PILLAR_3_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_PILLAR_4_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_PILLAR_5_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_PILLAR_6_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_PILLAR_7_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_PILLAR_8_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_PILLAR_9_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_PILLAR_10_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_PILLAR_11_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_PILLAR_12_ITEM.get());
                output.accept((ItemLike) VIKING_WOODEN_TABLE_ITEM.get());
                // Quest Board
                output.accept((ItemLike) QUEST_BOARD_ITEM.get());

            }).build());

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        modBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) -> {
            // Office shapes
            FurnitureBlock.registerShape((Block) OFFICE_BOARD_SMALL.get(), Block.box(0.0, 5.0, 14.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) OFFICE_BOARD_LARGE.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) OFFICE_COMPUTER.get(), Block.box(0.0, 0.0, 0.0, 15.0, 10.0, 15.0));
            FurnitureBlock.registerShape((Block) OFFICE_CUPBOARD.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) OFFICE_BOOKSHELF.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) OFFICE_BOOKSHELF_TALL.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) OFFICE_FILING_CABINET.get(), Block.box(0.0, 0.0, 3.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) OFFICE_FILE_RACK.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) OFFICE_LAMP.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) OFFICE_POTTED_PLANT.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) OFFICE_PRINTER.get(), Block.box(4.0, 0.0, 2.0, 12.0, 9.0, 11.0));
            FurnitureBlock.registerShape((Block) OFFICE_PROJECTOR_SCREEN.get(), Block.box(0.0, 2.0, 13.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) OFFICE_PROJECTOR.get(), Block.box(0.0, 0.0, 0.0, 14.0, 5.0, 16.0));
            FurnitureBlock.registerShape((Block) OFFICE_RUBBISH_BIN.get(), Block.box(4.0, 0.0, 3.0, 12.0, 9.0, 11.0));
            FurnitureBlock.registerShape((Block) OFFICE_SOFA.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) OFFICE_SOFA_LARGE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) OFFICE_CONFERENCE_TABLE.get(), Block.box(0.0, 0.0, 3.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) OFFICE_CHAIR.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) OFFICE_CEO_CHAIR.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) OFFICE_TABLE.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) OFFICE_CEO_DESK.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 15.0));
            // Vintage shapes
            FurnitureBlock.registerShape((Block) VINTAGE_BED.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) VINTAGE_BIG_CUPBOARD.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) VINTAGE_BOOK_SHELF.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) VINTAGE_CARPET.get(), Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0));
            FurnitureBlock.registerShape((Block) VINTAGE_CARPET_ALT.get(), Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0));
            FurnitureBlock.registerShape((Block) VINTAGE_CHAIR.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) VINTAGE_CLOCK.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) VINTAGE_CUPBOARD.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) VINTAGE_DESK_LAMP.get(), Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0));
            FurnitureBlock.registerShape((Block) VINTAGE_FIREPLACE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) VINTAGE_LEATHER_SOFA.get(), Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) VINTAGE_MIRROR.get(), Block.box(2.0, 0.0, 14.0, 14.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) VINTAGE_NIGHTSTAND.get(), Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0));
            FurnitureBlock.registerShape((Block) VINTAGE_PAINTING.get(), Block.box(1.0, 2.0, 14.0, 15.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) VINTAGE_PIANO.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) VINTAGE_RADIO.get(), Block.box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0));
            FurnitureBlock.registerShape((Block) VINTAGE_SHOWCASE.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) VINTAGE_STANDING_LAMP.get(), Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0));
            FurnitureBlock.registerShape((Block) VINTAGE_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) VINTAGE_TABLETOP_LAMP.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            // Classic shapes
            FurnitureBlock.registerShape((Block) CLASSIC_CANDLE.get(), Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0));
            FurnitureBlock.registerShape((Block) CLASSIC_CEILING_FAN.get(), Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CLASSIC_CHAIR.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) CLASSIC_CLEANING_SET.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) CLASSIC_CURTAIN.get(), Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CLASSIC_CURTAIN_RED.get(), Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CLASSIC_DOOR.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0));
            FurnitureBlock.registerShape((Block) CLASSIC_FLOWER.get(), Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0));
            FurnitureBlock.registerShape((Block) CLASSIC_GOLDEN_TREE.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) CLASSIC_HARP.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) CLASSIC_HAT_HANGER.get(), Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0));
            FurnitureBlock.registerShape((Block) CLASSIC_JAR.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) CLASSIC_LONG_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) CLASSIC_PAINTING.get(), Block.box(1.0, 2.0, 14.0, 15.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) CLASSIC_PHONE.get(), Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0));
            FurnitureBlock.registerShape((Block) CLASSIC_SHOWCASE_CORNER.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CLASSIC_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) CLASSIC_TABLE_LAMP.get(), Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0));
            FurnitureBlock.registerShape((Block) CLASSIC_WALL_LAMP.get(), Block.box(5.0, 4.0, 13.0, 11.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) CLASSIC_WALL_LAMP_DOUBLE.get(), Block.box(3.0, 4.0, 13.0, 13.0, 14.0, 16.0));
            // Dungeon shapes
            FurnitureBlock.registerShape((Block) DUNGEON_CAGE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) DUNGEON_CAGE_WITH_BONE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) DUNGEON_CHEST_DECOR.get(), Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0));
            FurnitureBlock.registerShape((Block) DUNGEON_CHEST_OPEN.get(), Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0));
            FurnitureBlock.registerShape((Block) DUNGEON_GOLDBAR.get(), Block.box(3.0, 0.0, 3.0, 13.0, 4.0, 13.0));
            FurnitureBlock.registerShape((Block) DUNGEON_GOLDBAR_COIN.get(), Block.box(2.0, 0.0, 2.0, 14.0, 5.0, 14.0));
            FurnitureBlock.registerShape((Block) DUNGEON_GOLDBARS.get(), Block.box(1.0, 0.0, 1.0, 15.0, 6.0, 15.0));
            FurnitureBlock.registerShape((Block) DUNGEON_HEADS.get(), Block.box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0));
            FurnitureBlock.registerShape((Block) DUNGEON_SWORD_BONE.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) DUNGEON_VASE.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) DUNGEON_HANG_FLAG.get(), Block.box(2.0, 0.0, 14.0, 14.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) DUNGEON_FLAG_BONE.get(), Block.box(2.0, 0.0, 14.0, 14.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) DUNGEON_SKELETON.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) DUNGEON_SKELETON_HEAD.get(), Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0));
            FurnitureBlock.registerShape((Block) DUNGEON_SKELETON_SLEEP.get(), Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0));
            FurnitureBlock.registerShape((Block) DUNGEON_TABLE_DECOR.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) DUNGEON_TABLE_LONG.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) DUNGEON_TORCH_DECOR.get(), Block.box(5.0, 0.0, 13.0, 11.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) DUNGEON_WEAPONSTAND.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) DUNGEON_WOOD_BARREL.get(), Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) DUNGEON_WOOD_BOX.get(), Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0));
            FurnitureBlock.registerShape((Block) DUNGEON_WOOD_CHAIR.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) DUNGEON_CHAIR_BONE.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) DUNGEON_CHAIR_REST.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            // Coffee Shop shapes
            FurnitureBlock.registerShape((Block) COFFEE_BLENDER.get(), Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0));
            FurnitureBlock.registerShape((Block) COFFEE_BOARD_1.get(), Block.box(2.0, 4.0, 14.0, 14.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) COFFEE_BOARD_2.get(), Block.box(1.0, 2.0, 14.0, 15.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) COFFEE_BREAD_SHOWCASE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) COFFEE_BREADCOFFEE_1.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) COFFEE_BREADCOFFEE_2.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) COFFEE_CASHIER_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) COFFEE_CHAIR.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) COFFEE_COFFEE_MACHINE.get(), Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0));
            FurnitureBlock.registerShape((Block) COFFEE_COUNTER_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) COFFEE_GLASS_HANGER.get(), Block.box(2.0, 8.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) COFFEE_HANGING_LAMP.get(), Block.box(4.0, 6.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) COFFEE_PICTURE_WALLPAPER.get(), Block.box(1.0, 2.0, 14.0, 15.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) COFFEE_PLANT_POT.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) COFFEE_SHELF_1.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) COFFEE_SHELF_2.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) COFFEE_SHOP_SIGN.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) COFFEE_SIGN.get(), Block.box(3.0, 0.0, 6.0, 13.0, 16.0, 10.0));
            FurnitureBlock.registerShape((Block) COFFEE_SOFA_1.get(), Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) COFFEE_SOFA_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) COFFEE_TABLE_1.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) COFFEE_TABLE_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            // Medieval Market V1 shapes
            FurnitureBlock.registerShape((Block) MARKET_BARREL.get(), Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) MARKET_BARRELBERRY.get(), Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) MARKET_BARRELSWORD.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) MARKET_BOARDSIGN.get(), Block.box(2.0, 0.0, 6.0, 14.0, 16.0, 10.0));
            FurnitureBlock.registerShape((Block) MARKET_CAMPFIRE_1.get(), Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0));
            FurnitureBlock.registerShape((Block) MARKET_CAMPFIRE_2.get(), Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0));
            FurnitureBlock.registerShape((Block) MARKET_CARGO_1.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET_CARGO_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET_CART_1.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET_CART_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET_CHAIR.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) MARKET_CRATE.get(), Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0));
            FurnitureBlock.registerShape((Block) MARKET_EARTHENWARE.get(), Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0));
            FurnitureBlock.registerShape((Block) MARKET_FISHTUB.get(), Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0));
            FurnitureBlock.registerShape((Block) MARKET_MARKETSTALL_1.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET_MARKETSTALL_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET_MARKETSTALL_3.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET_MARKETSTALL_4.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET_POLE.get(), Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0));
            FurnitureBlock.registerShape((Block) MARKET_SHARK.get(), Block.box(0.0, 0.0, 2.0, 16.0, 12.0, 14.0));
            FurnitureBlock.registerShape((Block) MARKET_SHELF_1.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET_SHELF_2.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET_TABLE_1.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET_TABLE_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET_WATERWELL.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            // Medieval Market V2 shapes
            FurnitureBlock.registerShape((Block) MARKET2_BARREL_1.get(), Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) MARKET2_BARREL_2.get(), Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) MARKET2_BOARD_1.get(), Block.box(2.0, 4.0, 14.0, 14.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET2_BOARDSIGN_1.get(), Block.box(2.0, 0.0, 6.0, 14.0, 16.0, 10.0));
            FurnitureBlock.registerShape((Block) MARKET2_BOX_1.get(), Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0));
            FurnitureBlock.registerShape((Block) MARKET2_CARGO_1.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET2_CARGO_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET2_CHAIR_1.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) MARKET2_CHAIR_2.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) MARKET2_CRATE_1.get(), Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0));
            FurnitureBlock.registerShape((Block) MARKET2_CRATE_2.get(), Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0));
            FurnitureBlock.registerShape((Block) MARKET2_CRATE_3.get(), Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0));
            FurnitureBlock.registerShape((Block) MARKET2_CRATE_4.get(), Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0));
            FurnitureBlock.registerShape((Block) MARKET2_MARKETSTALL_1.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET2_MARKETSTALL_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET2_MARKETSTALL_3.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET2_MARKETSTALL_4.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET2_SHELF_1.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET2_TABLE_1.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) MARKET2_TABLE_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            // Casino Decoration V1 shapes
            FurnitureBlock.registerShape((Block) CASINO_BARRIER.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_BUSH.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_CARD_AND_TOKEN.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_CHAIR_BAR_BROWN.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) CASINO_CHAIR_BAR_RED.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) CASINO_CHAIR_BROWN.get(), Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) CASINO_CHIPS.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_GAME_BIGWIN.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_GAME_SLOT.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_MONITOR.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_POT_TREE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_SOFA_RED.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_SOFA_RED_NO_REST.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_SOFA_RED_SINGLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_TABLE_BILLIARDS.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_TABLE_BILLIARDS_STICK_STAND.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_TABLE_BLACKJACK.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_TABLE_BLANK.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_TABLE_CRAPS.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_TABLE_ROULETTE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_TABLE_WOOD.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO_VENDING_MACHINE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            // Casino Decoration V2 shapes
            FurnitureBlock.registerShape((Block) CASINO2_ASHTRAY.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_BACCARAT_MACHINE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_BUFFALO_SLOT_MACHINE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_CHAIR_RED.get(), Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) CASINO2_CHAIR_YELLOW.get(), Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) CASINO2_CHIP_SET.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_DOGGIE_CASH.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_GAMBLING_GAME_MACHINE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_LADY_LED_SIGN.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_LED_SIGN.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_MADONNA_GAMBLING_MACHINE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_POKER_SIGN.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_RED_ARMREST_CHAIR.get(), Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0));
            FurnitureBlock.registerShape((Block) CASINO2_ROULETTE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_STACK_CARD.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_VENEZIA_SLOT.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_WHEEL_MACHINE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_WOMAN_PAINTING.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CASINO2_YELLOW_ARMREST_CHAIR.get(), Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0));
            // Farmer Decorations shapes
            FurnitureBlock.registerShape((Block) FARMER_BEER.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_CORN.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_CORN_BASKET.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_DOOR.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_FENCE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_FENCE_CORNER.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_FIREWOOD.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_FLOWER.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_HAY_WITH_SPADE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_LOG_AXE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_MILK_TANK.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_PLATE_RICE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_POND.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_RICE_BASKET.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_SCARE_CROW.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_STONE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_TABLE_SAWING.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_TOOLS_STAND.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) FARMER_WHEELBARROW.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            // Caribbean shapes
            FurnitureBlock.registerShape((Block) CARIBBEAN_AQUATRIKES.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_ARM_CHAIR.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_BEACH_HANGING_CHAIR.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_BEACH_SIGN.get(), Block.box(2.0, 0.0, 6.0, 14.0, 16.0, 10.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_BED.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_CARPET.get(), Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_COCONUT_SET.get(), Block.box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_COUNTER_CHAIR.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_HAND_WASHING.get(), Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_HANGING_LAMP.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_HANGING_SEAT.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_ICECREAMBOX_BIKE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_PALM_TREE.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_PLANT_DESK.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_SHIP_WHEEL_CLOCK.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_SHIP_WHEEL_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_SUNSET_PAINTING.get(), Block.box(1.0, 2.0, 14.0, 15.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_SWIM_RING.get(), Block.box(3.0, 0.0, 3.0, 13.0, 4.0, 13.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_UMBRELLA.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) CARIBBEAN_WELCOME_FLOWER.get(), Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0));
            // Bedroom shapes
            FurnitureBlock.registerShape((Block) BEDROOM_BASKET.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) BEDROOM_BED.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) BEDROOM_BIN.get(), Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0));
            FurnitureBlock.registerShape((Block) BEDROOM_BOOKSHELF.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) BEDROOM_CARPET.get(), Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0));
            FurnitureBlock.registerShape((Block) BEDROOM_CHAIR_DRESSING_TABLE.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) BEDROOM_DESKTOP_PHOTO_FRAME.get(), Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0));
            FurnitureBlock.registerShape((Block) BEDROOM_DRAWER_CABINET.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) BEDROOM_DRAWER_LAMP.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) BEDROOM_DRESSING_TABLE.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) BEDROOM_LAMP.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) BEDROOM_MIRROR.get(), Block.box(2.0, 0.0, 13.0, 14.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BEDROOM_PICTURE_FRAME.get(), Block.box(1.0, 2.0, 14.0, 15.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) BEDROOM_SHELF.get(), Block.box(0.0, 6.0, 12.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) BEDROOM_SMALL_TABLE.get(), Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0));
            FurnitureBlock.registerShape((Block) BEDROOM_SOFA_BENCH.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) BEDROOM_SOFA_CHAIR.get(), Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0));
            FurnitureBlock.registerShape((Block) BEDROOM_TV_STAND.get(), Block.box(0.0, 0.0, 2.0, 16.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) BEDROOM_VASE.get(), Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0));
            FurnitureBlock.registerShape((Block) BEDROOM_WARDROBE.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) BEDROOM_WORK_TABLE.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            // Modern shapes
            FurnitureBlock.registerShape((Block) MODERN_BED.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) MODERN_BOARD.get(), Block.box(1.0, 2.0, 14.0, 15.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) MODERN_CABINET.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MODERN_CARPET.get(), Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0));
            FurnitureBlock.registerShape((Block) MODERN_CHAIR.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) MODERN_COMPUTER_CHAIR.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) MODERN_COMPUTER_DESK.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MODERN_LAMP.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) MODERN_MACBOOK.get(), Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0));
            FurnitureBlock.registerShape((Block) MODERN_PICTURE.get(), Block.box(1.0, 2.0, 14.0, 15.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) MODERN_PLANTPOT.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) MODERN_SHELF.get(), Block.box(0.0, 6.0, 12.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) MODERN_SOFA_01.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) MODERN_SOFA_02.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) MODERN_TABLE_01.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) MODERN_TABLE_02.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) MODERN_TABLE_03.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) MODERN_TV.get(), Block.box(1.0, 0.0, 6.0, 15.0, 14.0, 10.0));
            FurnitureBlock.registerShape((Block) MODERN_WARDROBE_01.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MODERN_WARDROBE_02.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0));
            // Dungeon Tavern shapes
            FurnitureBlock.registerShape((Block) BL_BAR_STOOL.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) BL_BARREL_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BL_BARREL_TABLE_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BL_BOTTLE_WINE_1.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) BL_BOTTLE_WINE_2.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) BL_BOTTLE_WINE_3.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) BL_BOTTLE_WINE_STACK.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) BL_GLASS_BEER.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) BL_GLASS_WINE.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) BL_TABLE_LARGE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BL_TABLE_LARGE_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BL_TAVERN_BENCH.get(), Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0));
            FurnitureBlock.registerShape((Block) BL_TAVERN_CABINET_1.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BL_TAVERN_CABINET_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BL_TAVERN_COUNTER.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BL_WALL_SHELF.get(), Block.box(0.0, 2.0, 12.0, 16.0, 14.0, 16.0));
            // Bank shapes
            FurnitureBlock.registerShape((Block) BANK_ATM.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) BANK_BARRIER.get(), Block.box(2.0, 0.0, 6.0, 14.0, 16.0, 10.0));
            FurnitureBlock.registerShape((Block) BANK_CAMERA.get(), Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) BANK_CASE.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) BANK_CHAIR.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) BANK_CHAIR_1.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) BANK_COMPUTER.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) BANK_DOCUMENT.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) BANK_GLASS.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) BANK_GREEK_BALANCE.get(), Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) BANK_LOCKER.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BANK_LOCKER_OPEN.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BANK_MONEY.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) BANK_MONEY_1.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) BANK_MONEY_CASE.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) BANK_POT.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) BANK_SAFE_DOOR.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BANK_SIGN.get(), Block.box(0.0, 2.0, 12.0, 16.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) BANK_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BANK_TALL_CHAIR.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            // Medieval shapes
            FurnitureBlock.registerShape((Block) MED_ARM_CHAIR.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_BED_BUNK.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_BED_DOUBLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_BED_SINGLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_BOOK_OPEN.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_BOOK_STACK_HORIZONTAL_1.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_BOOK_STACK_HORIZONTAL_2.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_BOOK_STACK_VERTICAL_1.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_BOOK_STACK_VERTICAL_2.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_CANDLE_DISH.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) MED_CHAIR.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_CHAIR_CUSHION.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_COFFEE_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_COFFEE_TABLE_CLOTH.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_DESK.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_DRESSER_LOW.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_DRESSER_TALL.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_END_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_FIREPLACE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_LAMP.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) MED_LAMP_HANGING.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) MED_LAMP_TALL.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) MED_POT_1.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) MED_POT_2.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) MED_POT_3.get(), Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0));
            FurnitureBlock.registerShape((Block) MED_SCROLL_OPEN.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_SCROLL_STACK.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_SOFA.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_STOOL.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_STOOL_CUSHION.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_TABLE_LONG.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_TABLE_SMALL.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_TABLE_SMALL_CLOTH.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_BULLETIN_BOARD.get(), Block.box(0.0, 2.0, 12.0, 16.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_BULLETIN_BOARD_SMALL.get(), Block.box(0.0, 2.0, 12.0, 16.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_BULLETIN_BOARD_WALL.get(), Block.box(0.0, 2.0, 12.0, 16.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_CANOPY_FLAT.get(), Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_CANOPY_SLOPED.get(), Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_CHALK_SIGN.get(), Block.box(0.0, 2.0, 12.0, 16.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_CHALK_SIGN_HANGING.get(), Block.box(0.0, 2.0, 12.0, 16.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_CHEST.get(), Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_CHEST_OPEN.get(), Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_COINS_1.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_COINS_2.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_COINS_3.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_CRATE_BREAD.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_CRATE_CARROTS.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_CRATE_DISPLAY_BREAD.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_CRATE_DISPLAY_CARROTS.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_CRATE_DISPLAY_EMPTY.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_CRATE_EMPTY.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_CRATE_STACK.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_PAPERS_HANGING_1.get(), Block.box(0.0, 2.0, 12.0, 16.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_PAPERS_HANGING_2.get(), Block.box(0.0, 2.0, 12.0, 16.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_PAPERS_HANGING_3.get(), Block.box(0.0, 2.0, 12.0, 16.0, 14.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_SACK.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_SHIPPING_CRATE.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_SHIPPING_CRATE_TRIPLE_STACK.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) MED_SHOP_CART.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_STREAMER_POST.get(), Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_STREAMERS_HANGING.get(), Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_WAGON.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_WAGON_FULL.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_BROWN_MUSHROOM_PATCH.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_BROWN_MUSHROOM_PATCH_LARGE.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_CATTAILS.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_CLOVER_FLOWERS.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_CLOVERS.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_CRYSTAL.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_GLOW_MUSHROOM_PATCH.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_GLOW_MUSHROOM_PATCH_LARGE.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_LOG_MUSHROOM.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_LOG_MUSHROOM_CORNER.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_LOG_PILE_LARGE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_LOG_PILE_LARGE_OVERGROWN.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_LOG_PILE_SMALL_1.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_LOG_PILE_SMALL_2.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_LOG_PILE_SMALL_3.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) MED_ORANGE_MUSHROOM_PATCH.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_ORANGE_MUSHROOM_PATCH_LARGE.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_ORE.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_PEBBLES_1.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_PEBBLES_2.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_PEBBLES_3.get(), Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_PLANT.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_RED_MUSHROOM_PATCH.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_RED_MUSHROOM_PATCH_LARGE.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_ROCK_1.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_ROCK_2.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_ROCK_3.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
            FurnitureBlock.registerShape((Block) MED_ROCK_4.get(), Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));

            // =============================================
            // --- Seating Configurations ---
            // =============================================
            // Office chairs (standard height 0.3)
            SittableFurnitureBlock.registerSeating((Block) OFFICE_CHAIR.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) OFFICE_CEO_CHAIR.get(), 0.35, 1);
            // Vintage/Classic chairs
            SittableFurnitureBlock.registerSeating((Block) VINTAGE_CHAIR.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) CLASSIC_CHAIR.get(), 0.3, 1);
            // Dungeon chairs (slightly lower)
            SittableFurnitureBlock.registerSeating((Block) DUNGEON_WOOD_CHAIR.get(), 0.25, 1);
            SittableFurnitureBlock.registerSeating((Block) DUNGEON_CHAIR_BONE.get(), 0.25, 1);
            SittableFurnitureBlock.registerSeating((Block) DUNGEON_CHAIR_REST.get(), 0.25, 1);
            // Coffee shop chair
            SittableFurnitureBlock.registerSeating((Block) COFFEE_CHAIR.get(), 0.3, 1);
            // Market chairs
            SittableFurnitureBlock.registerSeating((Block) MARKET_CHAIR.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) MARKET2_CHAIR_1.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) MARKET2_CHAIR_2.get(), 0.3, 1);
            // Casino chairs
            SittableFurnitureBlock.registerSeating((Block) CASINO_CHAIR_BAR_BROWN.get(), 0.45, 1);
            SittableFurnitureBlock.registerSeating((Block) CASINO_CHAIR_BAR_RED.get(), 0.45, 1);
            SittableFurnitureBlock.registerSeating((Block) CASINO_CHAIR_BROWN.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) CASINO2_CHAIR_RED.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) CASINO2_CHAIR_YELLOW.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) CASINO2_RED_ARMREST_CHAIR.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) CASINO2_YELLOW_ARMREST_CHAIR.get(), 0.3, 1);
            // Caribbean chairs
            SittableFurnitureBlock.registerSeating((Block) CARIBBEAN_ARM_CHAIR.get(), 0.2, 1);
            SittableFurnitureBlock.registerSeating((Block) CARIBBEAN_BEACH_HANGING_CHAIR.get(), 0.35, 1);
            SittableFurnitureBlock.registerSeating((Block) CARIBBEAN_COUNTER_CHAIR.get(), 0.45, 1);
            SittableFurnitureBlock.registerSeating((Block) CARIBBEAN_HANGING_SEAT.get(), 0.35, 1);
            // Bedroom chairs
            SittableFurnitureBlock.registerSeating((Block) BEDROOM_CHAIR_DRESSING_TABLE.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) BEDROOM_SOFA_CHAIR.get(), 0.2, 1);
            // Modern chairs
            SittableFurnitureBlock.registerSeating((Block) MODERN_CHAIR.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) MODERN_COMPUTER_CHAIR.get(), 0.3, 1);
            // Bank chairs
            SittableFurnitureBlock.registerSeating((Block) BANK_CHAIR.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) BANK_CHAIR_1.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) BANK_TALL_CHAIR.get(), 0.45, 1);
            // Medieval chairs
            SittableFurnitureBlock.registerSeating((Block) MED_ARM_CHAIR.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) MED_CHAIR.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) MED_CHAIR_CUSHION.get(), 0.3, 1);
            // Stools (higher seat)
            SittableFurnitureBlock.registerSeating((Block) BL_BAR_STOOL.get(), 0.45, 1);
            SittableFurnitureBlock.registerSeating((Block) MED_STOOL.get(), 0.35, 1);
            SittableFurnitureBlock.registerSeating((Block) MED_STOOL_CUSHION.get(), 0.35, 1);

            // --- Sofas (multi-seat) ---
            SittableFurnitureBlock.registerSeating((Block) OFFICE_SOFA.get(), 0.2, 2, 0.3);
            SittableFurnitureBlock.registerSeating((Block) OFFICE_SOFA_LARGE.get(), 0.2, 3, 0.5);
            SittableFurnitureBlock.registerSeating((Block) VINTAGE_LEATHER_SOFA.get(), 0.2, 2, 0.35);
            SittableFurnitureBlock.registerSeating((Block) COFFEE_SOFA_1.get(), 0.2, 1);
            SittableFurnitureBlock.registerSeating((Block) COFFEE_SOFA_2.get(), 0.2, 1);
            SittableFurnitureBlock.registerSeating((Block) CASINO_SOFA_RED.get(), 0.2, 2, 0.35);
            SittableFurnitureBlock.registerSeating((Block) CASINO_SOFA_RED_NO_REST.get(), 0.2, 2, 0.35);
            SittableFurnitureBlock.registerSeating((Block) CASINO_SOFA_RED_SINGLE.get(), 0.2, 1);
            SittableFurnitureBlock.registerSeating((Block) MODERN_SOFA_01.get(), 0.2, 2, 0.3);
            SittableFurnitureBlock.registerSeating((Block) MODERN_SOFA_02.get(), 0.2, 2, 0.3);
            SittableFurnitureBlock.registerSeating((Block) MED_SOFA.get(), 0.2, 3, 0.6);

            // --- Benches (2-seat) ---
            SittableFurnitureBlock.registerSeating((Block) BEDROOM_SOFA_BENCH.get(), 0.2, 2, 0.3);
            SittableFurnitureBlock.registerSeating((Block) BL_TAVERN_BENCH.get(), 0.25, 2, 0.3);

            // --- Livingroom ---
            SittableFurnitureBlock.registerSeating((Block) LR_CHAIR_1.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) LR_CHAIR_2.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) LR_SOFA_1.get(), 0.2, 2, 0.3);
            SittableFurnitureBlock.registerSeating((Block) LR_SOFA_2.get(), 0.2, 2, 0.3);
            SittableFurnitureBlock.registerSeating((Block) LR_SOFA_3.get(), 0.2, 3, 0.5);
            SittableFurnitureBlock.registerSeating((Block) LR_SOFA_4.get(), 0.2, 3, 0.5);

            // --- Medieval Furniture (chairs) ---
            SittableFurnitureBlock.registerSeating((Block) MS2_MEDIEVAL_DOUBLE_CHAIR_COLOR.get(), 0.3, 2, 0.3);
            SittableFurnitureBlock.registerSeating((Block) MS2_MEDIEVAL_DOUBLE_CHAIR_DYE.get(), 0.3, 2, 0.3);
            SittableFurnitureBlock.registerSeating((Block) MS2_MEDIEVAL_ONE_CHAIR_1_COLOR.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) MS2_MEDIEVAL_ONE_CHAIR_1_DYE.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) MS2_MEDIEVAL_ONE_CHAIR_COLOR.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) MS2_MEDIEVAL_ONE_CHAIR_COLOR_1.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) MS2_MEDIEVAL_ONE_CHAIR_COLOR_2.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) MS2_MEDIEVAL_ONE_CHAIR_DYE.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) MS2_MEDIEVAL_ONE_CHAIR_DYE_2.get(), 0.3, 1);

            // --- Medieval Structures (chairs) ---
            SittableFurnitureBlock.registerSeating((Block) MS4_MEDIEVAL_PACK_V4_CHAIR_1.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) MS4_MEDIEVAL_PACK_V4_CHAIR_2.get(), 0.3, 1);

            // --- Medieval Village (bench/chair) ---
            SittableFurnitureBlock.registerSeating((Block) MKT_MEDIEVAL_MARKET_BENCH.get(), 0.25, 2, 0.3);
            SittableFurnitureBlock.registerSeating((Block) MKT_MEDIEVAL_MARKET_CHAIR.get(), 0.3, 1);

            // --- Park ---
            SittableFurnitureBlock.registerSeating((Block) PARK_BENCH.get(), 0.25, 2, 0.3);
            SittableFurnitureBlock.registerSeating((Block) PARK_LOG_BENCH.get(), 0.25, 2, 0.3);
            SittableFurnitureBlock.registerSeating((Block) PARK_SWING.get(), 0.35, 1);

            // =============================================
            // --- Bed Configurations ---
            // =============================================
            // Single beds (1 sleeper)
            SleepableFurnitureBlock.registerBed((Block) VINTAGE_BED.get(), 1);
            SleepableFurnitureBlock.registerBed((Block) CARIBBEAN_BED.get(), 1);
            SleepableFurnitureBlock.registerBed((Block) BEDROOM_BED.get(), 1);
            SleepableFurnitureBlock.registerBed((Block) MED_BED_SINGLE.get(), 1);
            SleepableFurnitureBlock.registerBed((Block) MED_BED_BUNK.get(), 1);
            SleepableFurnitureBlock.registerBed((Block) MS2_MEDIEVAL_BED_COLOR.get(), 1);
            SleepableFurnitureBlock.registerBed((Block) MS2_MEDIEVAL_BED_DYE.get(), 1);
            SleepableFurnitureBlock.registerBed((Block) MKT_MEDIEVAL_MARKET_BED.get(), 1);
            // Double beds (2 sleepers)
            SleepableFurnitureBlock.registerBed((Block) MED_BED_DOUBLE.get(), 2);
            SleepableFurnitureBlock.registerBed((Block) MODERN_BED.get(), 2);
            SleepableFurnitureBlock.registerBed((Block) MS2_MEDIEVAL_DOUBLE_BED_COLOR.get(), 2);
            SleepableFurnitureBlock.registerBed((Block) MS2_MEDIEVAL_DOUBLE_BED_DYE.get(), 2);

            // =============================================
            // --- Medieval Bathroom Shapes ---
            // =============================================
            FurnitureBlock.registerShape((Block) BATH_BAQUET.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) BATH_BAQUET_FILLED.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) BATH_BAQUET_FABRIC.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) BATH_BAQUET_FABRIC_FILLED.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) BATH_STOOL.get(), Block.box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0));
            FurnitureBlock.registerShape((Block) BATH_BUCKET.get(), Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0));
            FurnitureBlock.registerShape((Block) BATH_BUCKET_FILLED.get(), Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0));
            FurnitureBlock.registerShape((Block) BATH_TOILET.get(), Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) BATH_TOILET_DOUBLE.get(), Block.box(0.0, 0.0, 2.0, 16.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) BATH_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));
            FurnitureBlock.registerShape((Block) BATH_BENCH.get(), Block.box(0.0, 0.0, 2.0, 16.0, 8.0, 14.0));
            FurnitureBlock.registerShape((Block) BATH_TOWELS.get(), Block.box(3.0, 0.0, 3.0, 13.0, 4.0, 13.0));
            FurnitureBlock.registerShape((Block) BATH_PITCHER.get(), Block.box(5.0, 0.0, 5.0, 11.0, 12.0, 11.0));
            FurnitureBlock.registerShape((Block) BATH_OIL_LAMP.get(), Block.box(5.0, 0.0, 5.0, 11.0, 14.0, 11.0));
            FurnitureBlock.registerShape((Block) BATH_CLOTHESLINE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) BATH_SMALL_TABLE.get(), Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0));
            FurnitureBlock.registerShape((Block) BATH_BASIN.get(), Block.box(3.0, 0.0, 3.0, 13.0, 6.0, 13.0));
            FurnitureBlock.registerShape((Block) BATH_SOAP.get(), Block.box(4.0, 0.0, 4.0, 12.0, 4.0, 12.0));
            FurnitureBlock.registerShape((Block) BATH_MIRROR.get(), Block.box(2.0, 0.0, 14.0, 14.0, 16.0, 16.0));

            // Medieval Bathroom Seating
            SittableFurnitureBlock.registerSeating((Block) BATH_BAQUET.get(), 0.1, 1);
            SittableFurnitureBlock.registerSeating((Block) BATH_BAQUET_FILLED.get(), 0.1, 1);
            SittableFurnitureBlock.registerSeating((Block) BATH_BAQUET_FABRIC.get(), 0.1, 1);
            SittableFurnitureBlock.registerSeating((Block) BATH_BAQUET_FABRIC_FILLED.get(), 0.1, 1);
            SittableFurnitureBlock.registerSeating((Block) BATH_STOOL.get(), 0.4, 1);
            SittableFurnitureBlock.registerSeating((Block) BATH_TOILET.get(), 0.5, 1);
            SittableFurnitureBlock.registerSeating((Block) BATH_TOILET_DOUBLE.get(), 0.5, 2, 0.3);
            SittableFurnitureBlock.registerSeating((Block) BATH_BENCH.get(), 0.2, 2, 0.3);

            // =============================================
            // --- Viking Furniture Shapes ---
            // =============================================
            FurnitureBlock.registerShape((Block) VIKING_AXE_SHIELD.get(), Block.box(1.0, 2.0, 14.0, 15.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) VIKING_BUCKET_1.get(), Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_BUCKET_2.get(), Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_CABINET_1.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) VIKING_CABINET_2.get(), Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 16.0));
            FurnitureBlock.registerShape((Block) VIKING_STATUE_1.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) VIKING_STATUE_2.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) VIKING_STATUE_3.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) VIKING_STATUE_4.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) VIKING_STATUE_5.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) VIKING_STATUE_6.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) VIKING_STATUE_7.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) VIKING_STONE_CHAIR_1.get(), Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) VIKING_STONE_CHAIR_2.get(), Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_BED.get(), Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_CHAIR_1.get(), Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_CHAIR_2.get(), Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_HANGING.get(), Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_PILLAR_1.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_PILLAR_2.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_PILLAR_3.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_PILLAR_4.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_PILLAR_5.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_PILLAR_6.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_PILLAR_7.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_PILLAR_8.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_PILLAR_9.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_PILLAR_10.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_PILLAR_11.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_PILLAR_12.get(), Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0));
            FurnitureBlock.registerShape((Block) VIKING_WOODEN_TABLE.get(), Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0));

            // Viking Seating
            SittableFurnitureBlock.registerSeating((Block) VIKING_STONE_CHAIR_1.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) VIKING_STONE_CHAIR_2.get(), 0.3, 1);
            SittableFurnitureBlock.registerSeating((Block) VIKING_WOODEN_CHAIR_1.get(), 0.5, 1);
            SittableFurnitureBlock.registerSeating((Block) VIKING_WOODEN_CHAIR_2.get(), 0.5, 1);

            // Viking Bed
            SleepableFurnitureBlock.registerBed((Block) VIKING_WOODEN_BED.get(), 1);

            // =============================================
            // --- Quest Board Shape ---
            // =============================================
            FurnitureBlock.registerShape((Block) QUEST_BOARD.get(), Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
        });
    }
}
