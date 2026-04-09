package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.building.huts.HutBlockRegistration;
import com.ultra.megamod.feature.citizen.multipiston.MultiPistonRegistry;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for all colony utility blocks, gate blocks, and their block entities.
 */
public class ColonyBlockRegistry {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create((ResourceKey) Registries.CREATIVE_MODE_TAB, "megamod");

    // ==================== Utility Blocks ====================

    public static final DeferredBlock<BlockRack> RACK = BLOCKS.registerBlock(
        "colony_rack", BlockRack::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f).sound(SoundType.WOOD));

    public static final DeferredBlock<BlockBarrel> BARREL = BLOCKS.registerBlock(
        "colony_barrel", BlockBarrel::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD));

    public static final DeferredBlock<BlockScarecrow> SCARECROW = BLOCKS.registerBlock(
        "colony_scarecrow", BlockScarecrow::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).sound(SoundType.WOOD).noOcclusion());

    public static final DeferredBlock<BlockConstructionTape> CONSTRUCTION_TAPE = BLOCKS.registerBlock(
        "construction_tape", BlockConstructionTape::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.0f).noCollision().noOcclusion().sound(SoundType.WOOL));

    public static final DeferredBlock<BlockWaypoint> WAYPOINT = BLOCKS.registerBlock(
        "colony_waypoint", BlockWaypoint::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.0f).noCollision().noOcclusion().sound(SoundType.STONE));

    // ==================== Colony Banner ====================

    public static final DeferredBlock<BlockColonyBanner> COLONY_BANNER = BLOCKS.registerBlock(
        "colony_banner", BlockColonyBanner::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(1.0f).sound(SoundType.WOOL).noOcclusion());

    // ==================== Stash ====================

    public static final DeferredBlock<BlockStash> STASH = BLOCKS.registerBlock(
        "colony_stash", BlockStash::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f).sound(SoundType.WOOD));

    // ==================== Decoration Controller ====================

    public static final DeferredBlock<BlockDecorationController> DECORATION_CONTROLLER = BLOCKS.registerBlock(
        "decoration_controller", BlockDecorationController::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f).sound(SoundType.STONE));

    // ==================== Tag Anchor ====================

    public static final DeferredBlock<BlockTagAnchor> TAG_ANCHOR = BLOCKS.registerBlock(
        "tag_anchor", BlockTagAnchor::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(2.0f).sound(SoundType.STONE));

    // ==================== Placeholder Blocks ====================

    public static final DeferredBlock<BlockPlaceholder> PLACEHOLDER_BLOCK = BLOCKS.registerBlock(
        "placeholder_block",
        props -> new BlockPlaceholder(props, BlockPlaceholder.PlaceholderType.SUBSTITUTION),
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.5f).sound(SoundType.GRAVEL));

    public static final DeferredBlock<BlockPlaceholder> SOLID_PLACEHOLDER_BLOCK = BLOCKS.registerBlock(
        "solid_placeholder_block",
        props -> new BlockPlaceholder(props, BlockPlaceholder.PlaceholderType.SOLID_SUBSTITUTION),
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5f).sound(SoundType.GRAVEL));

    public static final DeferredBlock<BlockPlaceholder> FLUID_PLACEHOLDER_BLOCK = BLOCKS.registerBlock(
        "fluid_placeholder_block",
        props -> new BlockPlaceholder(props, BlockPlaceholder.PlaceholderType.FLUID_SUBSTITUTION),
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WATER).strength(0.5f).sound(SoundType.GRAVEL));

    // ==================== Gate Blocks ====================

    public static final DeferredBlock<BlockGate> IRON_GATE = BLOCKS.registerBlock(
        "colony_iron_gate", BlockGate::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(GateType.IRON.getHardness()).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<BlockGate> WOODEN_GATE = BLOCKS.registerBlock(
        "colony_wooden_gate", BlockGate::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(GateType.WOODEN.getHardness()).sound(SoundType.WOOD).noOcclusion());

    // ==================== Block Items ====================

    public static final DeferredItem<BlockItem> RACK_ITEM = ITEMS.registerSimpleBlockItem("colony_rack", RACK);
    public static final DeferredItem<BlockItem> BARREL_ITEM = ITEMS.registerSimpleBlockItem("colony_barrel", BARREL);
    public static final DeferredItem<BlockItem> SCARECROW_ITEM = ITEMS.registerSimpleBlockItem("colony_scarecrow", SCARECROW);
    public static final DeferredItem<BlockItem> CONSTRUCTION_TAPE_ITEM = ITEMS.registerSimpleBlockItem("construction_tape", CONSTRUCTION_TAPE);
    public static final DeferredItem<BlockItem> WAYPOINT_ITEM = ITEMS.registerSimpleBlockItem("colony_waypoint", WAYPOINT);
    public static final DeferredItem<BlockItem> IRON_GATE_ITEM = ITEMS.registerSimpleBlockItem("colony_iron_gate", IRON_GATE);
    public static final DeferredItem<BlockItem> WOODEN_GATE_ITEM = ITEMS.registerSimpleBlockItem("colony_wooden_gate", WOODEN_GATE);
    public static final DeferredItem<BlockItem> COLONY_BANNER_ITEM = ITEMS.registerSimpleBlockItem("colony_banner", COLONY_BANNER);
    public static final DeferredItem<BlockItem> STASH_ITEM = ITEMS.registerSimpleBlockItem("colony_stash", STASH);
    public static final DeferredItem<BlockItem> DECORATION_CONTROLLER_ITEM = ITEMS.registerSimpleBlockItem("decoration_controller", DECORATION_CONTROLLER);
    public static final DeferredItem<BlockItem> TAG_ANCHOR_ITEM = ITEMS.registerSimpleBlockItem("tag_anchor", TAG_ANCHOR);
    public static final DeferredItem<BlockItem> PLACEHOLDER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("placeholder_block", PLACEHOLDER_BLOCK);
    public static final DeferredItem<BlockItem> SOLID_PLACEHOLDER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("solid_placeholder_block", SOLID_PLACEHOLDER_BLOCK);
    public static final DeferredItem<BlockItem> FLUID_PLACEHOLDER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("fluid_placeholder_block", FLUID_PLACEHOLDER_BLOCK);

    // ==================== Compost Item (produced by barrel) ====================

    public static final DeferredItem<Item> COMPOST_ITEM = ITEMS.registerSimpleItem("colony_compost",
        () -> new Item.Properties().stacksTo(64));

    // ==================== Block Entities ====================

    public static final Supplier<BlockEntityType<TileEntityRack>> RACK_BE =
        BLOCK_ENTITIES.register("colony_rack_be",
            () -> new BlockEntityType<>(TileEntityRack::new, RACK.get()));

    public static final Supplier<BlockEntityType<TileEntityBarrel>> BARREL_BE =
        BLOCK_ENTITIES.register("colony_barrel_be",
            () -> new BlockEntityType<>(TileEntityBarrel::new, BARREL.get()));

    public static final Supplier<BlockEntityType<TileEntityScarecrow>> SCARECROW_BE =
        BLOCK_ENTITIES.register("colony_scarecrow_be",
            () -> new BlockEntityType<>(TileEntityScarecrow::new, SCARECROW.get()));

    public static final Supplier<BlockEntityType<TileEntityStash>> STASH_BE =
        BLOCK_ENTITIES.register("colony_stash_be",
            () -> new BlockEntityType<>(TileEntityStash::new, STASH.get()));

    public static final Supplier<BlockEntityType<TileEntityDecorationController>> DECORATION_CONTROLLER_BE =
        BLOCK_ENTITIES.register("decoration_controller_be",
            () -> new BlockEntityType<>(TileEntityDecorationController::new, DECORATION_CONTROLLER.get()));

    public static final Supplier<BlockEntityType<TileEntityTagAnchor>> TAG_ANCHOR_BE =
        BLOCK_ENTITIES.register("tag_anchor_be",
            () -> new BlockEntityType<>(TileEntityTagAnchor::new, TAG_ANCHOR.get()));

    // ==================== Creative Tab ====================

    public static final Supplier<CreativeModeTab> COLONY_BLOCKS_TAB = CREATIVE_MODE_TABS.register("megamod_colony_blocks_tab",
        () -> CreativeModeTab.builder()
            .title((Component) Component.literal((String) "MegaMod - Colony Blocks"))
            .icon(() -> new ItemStack((ItemLike) RACK_ITEM.get()))
            .displayItems((parameters, output) -> {
                output.accept((ItemLike) RACK_ITEM.get());
                output.accept((ItemLike) BARREL_ITEM.get());
                output.accept((ItemLike) SCARECROW_ITEM.get());
                output.accept((ItemLike) CONSTRUCTION_TAPE_ITEM.get());
                output.accept((ItemLike) WAYPOINT_ITEM.get());
                output.accept((ItemLike) IRON_GATE_ITEM.get());
                output.accept((ItemLike) WOODEN_GATE_ITEM.get());
                output.accept((ItemLike) COMPOST_ITEM.get());
                output.accept((ItemLike) COLONY_BANNER_ITEM.get());
                output.accept((ItemLike) STASH_ITEM.get());
                output.accept((ItemLike) DECORATION_CONTROLLER_ITEM.get());
                output.accept((ItemLike) TAG_ANCHOR_ITEM.get());
                output.accept((ItemLike) PLACEHOLDER_BLOCK_ITEM.get());
                output.accept((ItemLike) SOLID_PLACEHOLDER_BLOCK_ITEM.get());
                output.accept((ItemLike) FLUID_PLACEHOLDER_BLOCK_ITEM.get());
                // Multi-Piston
                output.accept((ItemLike) MultiPistonRegistry.MULTI_PISTON_ITEM.get());
                // Hut Blocks - Resource Production
                output.accept((ItemLike) HutBlockRegistration.HUT_FARMER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_MINER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_LUMBERJACK_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_FISHERMAN_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_PLANTATION_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_NETHER_WORKER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_QUARRY_ITEM.get());
                // Hut Blocks - Animal Husbandry
                output.accept((ItemLike) HutBlockRegistration.HUT_SHEPHERD_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_COWBOY_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_CHICKEN_HERDER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_SWINE_HERDER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_RABBIT_HUTCH_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_BEEKEEPER_ITEM.get());
                // Hut Blocks - Crafting
                output.accept((ItemLike) HutBlockRegistration.HUT_BAKER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_BLACKSMITH_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_STONEMASON_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_SAWMILL_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_SMELTERY_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_STONE_SMELTERY_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_CRUSHER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_SIFTER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_COOK_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_KITCHEN_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_DYER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_FLETCHER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_GLASSBLOWER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_CONCRETE_MIXER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_COMPOSTER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_FLORIST_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_MECHANIC_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_ALCHEMIST_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_ENCHANTER_ITEM.get());
                // Hut Blocks - Military
                output.accept((ItemLike) HutBlockRegistration.HUT_BARRACKS_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_BARRACKS_TOWER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_GUARD_TOWER_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_ARCHERY_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_COMBAT_ACADEMY_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_GATE_HOUSE_ITEM.get());
                // Hut Blocks - Education
                output.accept((ItemLike) HutBlockRegistration.HUT_LIBRARY_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_SCHOOL_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_UNIVERSITY_ITEM.get());
                // Hut Blocks - Services
                output.accept((ItemLike) HutBlockRegistration.HUT_HOSPITAL_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_GRAVEYARD_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_TAVERN_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_DELIVERYMAN_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_WAREHOUSE_ITEM.get());
                // Hut Blocks - Core
                output.accept((ItemLike) HutBlockRegistration.HUT_TOWN_HALL_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_RESIDENCE_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.HUT_MYSTICAL_SITE_ITEM.get());
                output.accept((ItemLike) HutBlockRegistration.POST_BOX_ITEM.get());
            }).build());

    // ==================== Init ====================

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
    }
}
