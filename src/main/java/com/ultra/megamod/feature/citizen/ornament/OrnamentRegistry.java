package com.ultra.megamod.feature.citizen.ornament;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Central registry for the Domum Ornamentum decorative block system.
 * Registers:
 * - Architects Cutter block + item + block entity + menu
 * - All OrnamentBlockType variants (140+ blocks with proper shapes)
 * - 16 FloatingCarpet color variants
 * - Ornament block entity type (shared by all ornament blocks)
 * - Creative tab
 */
public class OrnamentRegistry {

    // ==================== Deferred Registers ====================

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "megamod");
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create((ResourceKey) Registries.MENU, "megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create((ResourceKey) Registries.CREATIVE_MODE_TAB, "megamod");

    // ==================== Architects Cutter ====================

    public static final DeferredBlock<ArchitectsCutterBlock> ARCHITECTS_CUTTER = BLOCKS.registerBlock(
            "architects_cutter", ArchitectsCutterBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5f).requiresCorrectToolForDrops().noOcclusion()
    );
    public static final DeferredItem<BlockItem> ARCHITECTS_CUTTER_ITEM =
            ITEMS.registerSimpleBlockItem("architects_cutter", ARCHITECTS_CUTTER);

    // ==================== Ornament Blocks ====================

    /**
     * Maps each OrnamentBlockType to its registered DeferredBlock.
     * Populated at class load time. The block type varies based on the shape.
     */
    private static final Map<OrnamentBlockType, DeferredBlock<? extends Block>> ORNAMENT_BLOCKS = new EnumMap<>(OrnamentBlockType.class);
    private static final Map<OrnamentBlockType, DeferredItem<BlockItem>> ORNAMENT_ITEMS = new EnumMap<>(OrnamentBlockType.class);

    /** All ornament blocks as a flat list for block entity registration. */
    private static final List<DeferredBlock<? extends Block>> ALL_ORNAMENT_BLOCK_LIST = new ArrayList<>();

    static {
        for (OrnamentBlockType type : OrnamentBlockType.values()) {
            String regName = type.getRegistryName();
            final OrnamentBlockType capturedType = type;

            DeferredBlock<? extends Block> block = registerOrnamentBlock(regName, capturedType);
            DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(regName, (DeferredBlock) block);

            ORNAMENT_BLOCKS.put(type, block);
            ORNAMENT_ITEMS.put(type, item);
            ALL_ORNAMENT_BLOCK_LIST.add(block);
        }
    }

    /**
     * Registers an ornament block with the proper Block subclass based on its shape.
     */
    private static DeferredBlock<? extends Block> registerOrnamentBlock(String regName, OrnamentBlockType type) {
        OrnamentBlockType.BlockShape shape = type.getShape();

        switch (shape) {
            case SLAB:
                return BLOCKS.registerBlock(regName,
                        props -> new OrnamentSlabBlock(type, props),
                        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());

            case STAIR:
                return BLOCKS.registerBlock(regName,
                        props -> new OrnamentStairBlock(type, props),
                        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());

            case FENCE:
                return BLOCKS.registerBlock(regName,
                        props -> new OrnamentFenceBlock(type, props),
                        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());

            case FENCE_GATE:
                return BLOCKS.registerBlock(regName,
                        props -> new OrnamentFenceGateBlock(type, props),
                        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());

            case WALL:
                return BLOCKS.registerBlock(regName,
                        props -> new OrnamentWallBlock(type, props),
                        () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());

            case DOOR:
                return BLOCKS.registerBlock(regName,
                        props -> new OrnamentDoorBlock(type, props),
                        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());

            case TRAPDOOR:
                return BLOCKS.registerBlock(regName,
                        props -> new OrnamentTrapdoorBlock(type, props),
                        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());

            case FULL:
            default:
                return BLOCKS.registerBlock(regName,
                        props -> new OrnamentBlock(type, props),
                        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion());
        }
    }

    // ==================== Floating Carpets ====================

    private static final Map<DyeColor, DeferredBlock<FloatingCarpetBlock>> CARPET_BLOCKS = new HashMap<>();
    private static final Map<DyeColor, DeferredItem<BlockItem>> CARPET_ITEMS = new HashMap<>();

    static {
        for (DyeColor color : DyeColor.values()) {
            String regName = "floating_carpet_" + color.getName();
            final DyeColor capturedColor = color;
            DeferredBlock<FloatingCarpetBlock> block = BLOCKS.registerBlock(
                    regName,
                    props -> new FloatingCarpetBlock(capturedColor, props),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(color.getMapColor())
                            .strength(0.1f)
                            .noOcclusion()
            );
            DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(regName, block);
            CARPET_BLOCKS.put(color, block);
            CARPET_ITEMS.put(color, item);
        }
    }

    // ==================== Block Entities ====================

    /**
     * Shared block entity type for all ornament blocks.
     * Must include all ornament blocks as valid blocks.
     */
    public static final Supplier<BlockEntityType<MateriallyTexturedBlockEntity>> ORNAMENT_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("ornament_block_entity", () -> {
                Block[] validBlocks = ALL_ORNAMENT_BLOCK_LIST.stream()
                        .map(db -> (Block) db.get())
                        .toArray(Block[]::new);
                return new BlockEntityType<>(MateriallyTexturedBlockEntity::new, validBlocks);
            });

    /**
     * Block entity for the Architects Cutter (minimal — holds no data).
     */
    public static final Supplier<BlockEntityType<ArchitectsCutterBlockEntity>> CUTTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("architects_cutter",
                    () -> new BlockEntityType<>(ArchitectsCutterBlockEntity::new, new Block[]{ARCHITECTS_CUTTER.get()}));

    // ==================== Menu ====================

    public static final Supplier<MenuType<ArchitectsCutterMenu>> CUTTER_MENU =
            MENUS.register("architects_cutter",
                    () -> IMenuTypeExtension.create(ArchitectsCutterMenu::new));

    // ==================== Creative Tab ====================

    public static final Supplier<CreativeModeTab> ORNAMENTS_TAB = CREATIVE_MODE_TABS.register("ornaments_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("MegaMod - Ornaments"))
                    .icon(() -> new ItemStack(ARCHITECTS_CUTTER.get()))
                    .displayItems((params, output) -> {
                        // Architects Cutter
                        output.accept((ItemLike) ARCHITECTS_CUTTER_ITEM.get());

                        // All ornament blocks
                        for (OrnamentBlockType type : OrnamentBlockType.values()) {
                            DeferredItem<BlockItem> item = ORNAMENT_ITEMS.get(type);
                            if (item != null) {
                                output.accept((ItemLike) item.get());
                            }
                        }

                        // All floating carpets
                        for (DyeColor color : DyeColor.values()) {
                            DeferredItem<BlockItem> item = CARPET_ITEMS.get(color);
                            if (item != null) {
                                output.accept((ItemLike) item.get());
                            }
                        }
                    })
                    .build()
    );

    // ==================== Lookup Helpers ====================

    /**
     * Returns the registered Block instance for a given OrnamentBlockType.
     */
    public static Block getOrnamentBlock(OrnamentBlockType type) {
        DeferredBlock<? extends Block> deferred = ORNAMENT_BLOCKS.get(type);
        return deferred != null ? deferred.get() : Blocks.AIR;
    }

    /**
     * Returns the registered FloatingCarpetBlock for a given DyeColor.
     */
    public static Block getFloatingCarpet(DyeColor color) {
        DeferredBlock<FloatingCarpetBlock> deferred = CARPET_BLOCKS.get(color);
        return deferred != null ? deferred.get() : Blocks.AIR;
    }

    /**
     * Returns all ornament block suppliers (for external iteration).
     */
    public static Map<OrnamentBlockType, DeferredBlock<? extends Block>> getOrnamentBlocks() {
        return ORNAMENT_BLOCKS;
    }

    // ==================== Menu Screen Registration (Client) ====================

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(CUTTER_MENU.get(), ArchitectsCutterScreen::new);
    }

    // ==================== Init ====================

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
    }
}
