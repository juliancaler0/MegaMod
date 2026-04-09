package com.ultra.megamod.feature.combat.runes;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.items.WorkbenchBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for all 5 RPG class workbench blocks and the rune crafting menu type.
 * Blocks:
 * - archers_workbench (Archers mod) — decorative, POI for Archery Artisan
 * - monk_workbench (Paladins mod) — decorative, POI for Monk
 * - arms_workbench (Rogues mod) — decorative, POI for Arms Merchant
 * - jewelers_kit (Jewelry mod) — decorative, POI for Jeweler
 * - crafting_altar (Runes mod) — functional, opens RuneCraftingMenu
 */
public class RuneWorkbenchRegistry {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MegaMod.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MegaMod.MODID);

    // ── Workbench block properties ──
    private static BlockBehaviour.Properties workbenchProps() {
        return BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    // ── Monk workbench custom shape (base + pillar + top) ──
    private static final VoxelShape MONK_SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 3, 15),   // base
            Block.box(4, 3, 4, 12, 12, 12),   // pillar
            Block.box(1, 12, 1, 15, 16, 15)   // top
    );

    // ══════════════════════════════════════════════
    // Decorative workbenches (4 blocks)
    // ══════════════════════════════════════════════

    public static final DeferredBlock<WorkbenchBlock> ARCHERS_WORKBENCH = BLOCKS.registerBlock("archers_workbench",
            props -> new WorkbenchBlock(props, "block.megamod.archers_workbench.hint", null), RuneWorkbenchRegistry::workbenchProps);

    public static final DeferredBlock<WorkbenchBlock> MONK_WORKBENCH = BLOCKS.registerBlock("monk_workbench",
            props -> new WorkbenchBlock(props, "block.megamod.monk_workbench.hint", MONK_SHAPE), RuneWorkbenchRegistry::workbenchProps);

    public static final DeferredBlock<WorkbenchBlock> ARMS_WORKBENCH = BLOCKS.registerBlock("arms_workbench",
            props -> new WorkbenchBlock(props, "block.megamod.arms_workbench.hint", null), RuneWorkbenchRegistry::workbenchProps);

    public static final DeferredBlock<WorkbenchBlock> JEWELERS_KIT = BLOCKS.registerBlock("jewelers_kit",
            props -> new WorkbenchBlock(props, "block.megamod.jewelers_kit.hint", null), RuneWorkbenchRegistry::workbenchProps);

    // ══════════════════════════════════════════════
    // Functional crafting altar (Runes)
    // ══════════════════════════════════════════════

    public static final DeferredBlock<RuneCraftingBlock> CRAFTING_ALTAR = BLOCKS.registerBlock("crafting_altar",
            RuneCraftingBlock::new, RuneWorkbenchRegistry::workbenchProps);

    // ══════════════════════════════════════════════
    // Block items
    // ══════════════════════════════════════════════

    public static final DeferredItem<BlockItem> ARCHERS_WORKBENCH_ITEM = ITEMS.registerSimpleBlockItem(ARCHERS_WORKBENCH);
    public static final DeferredItem<BlockItem> MONK_WORKBENCH_ITEM = ITEMS.registerSimpleBlockItem(MONK_WORKBENCH);
    public static final DeferredItem<BlockItem> ARMS_WORKBENCH_ITEM = ITEMS.registerSimpleBlockItem(ARMS_WORKBENCH);
    public static final DeferredItem<BlockItem> JEWELERS_KIT_ITEM = ITEMS.registerSimpleBlockItem(JEWELERS_KIT);
    public static final DeferredItem<BlockItem> CRAFTING_ALTAR_ITEM = ITEMS.registerSimpleBlockItem(CRAFTING_ALTAR);

    // ══════════════════════════════════════════════
    // Menu type for rune crafting
    // ══════════════════════════════════════════════

    public static final Supplier<MenuType<RuneCraftingMenu>> RUNE_CRAFTING_MENU = MENUS.register("rune_crafting",
            () -> IMenuTypeExtension.create((syncId, inv, buf) -> new RuneCraftingMenu(syncId, inv)));

    // ══════════════════════════════════════════════
    // Init
    // ══════════════════════════════════════════════

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        MENUS.register(modBus);
    }

    /**
     * Register menu screens (client-side). Called from MegaModClient.
     */
    public static void onRegisterMenuScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(RUNE_CRAFTING_MENU.get(),
                com.ultra.megamod.feature.combat.runes.client.RuneCraftingScreen::new);
    }
}
