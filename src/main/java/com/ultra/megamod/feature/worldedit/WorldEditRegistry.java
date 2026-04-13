package com.ultra.megamod.feature.worldedit;

import com.ultra.megamod.feature.worldedit.tool.BrushToolItem;
import com.ultra.megamod.feature.worldedit.tool.FarWandItem;
import com.ultra.megamod.feature.worldedit.tool.InfoToolItem;
import com.ultra.megamod.feature.worldedit.tool.SuperPickaxeItem;
import com.ultra.megamod.feature.worldedit.tool.TreePlanterItem;
import com.ultra.megamod.feature.worldedit.tool.WandItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers all WorldEdit-related items (tools, wands). Items are NOT
 * placed in a creative tab — they are only obtainable via admin commands
 * or the admin panel toggle.
 */
public final class WorldEditRegistry {
    public static final String MODID = "megamod";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<WandItem> WAND = ITEMS.registerItem("we_wand", WandItem::new);
    public static final DeferredItem<BrushToolItem> BRUSH = ITEMS.registerItem("we_brush", BrushToolItem::new);
    public static final DeferredItem<SuperPickaxeItem> SUPER_PICKAXE = ITEMS.registerItem("we_super_pickaxe", SuperPickaxeItem::new);
    public static final DeferredItem<InfoToolItem> INFO_TOOL = ITEMS.registerItem("we_info_tool", InfoToolItem::new);
    public static final DeferredItem<TreePlanterItem> TREE_PLANTER = ITEMS.registerItem("we_tree_planter", TreePlanterItem::new);
    public static final DeferredItem<FarWandItem> FAR_WAND = ITEMS.registerItem("we_far_wand", FarWandItem::new);

    private WorldEditRegistry() {}

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
