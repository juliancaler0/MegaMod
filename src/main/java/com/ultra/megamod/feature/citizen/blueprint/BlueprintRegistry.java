package com.ultra.megamod.feature.citizen.blueprint;

import com.ultra.megamod.feature.citizen.blueprint.network.BlueprintNetwork;
import com.ultra.megamod.feature.citizen.blueprint.tools.BuildToolItem;
import com.ultra.megamod.feature.citizen.blueprint.tools.ScanToolItem;
import com.ultra.megamod.feature.citizen.blueprint.tools.ShapeToolItem;
import com.ultra.megamod.feature.citizen.blueprint.tools.TagToolItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registration hub for the blueprint/build tool system.
 * Registers tool items (scan_tool, build_tool, shape_tool, tag_tool),
 * a creative tab, and the blueprint network payloads.
 */
public class BlueprintRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String) "megamod");

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
            (ResourceKey) Registries.CREATIVE_MODE_TAB, (String) "megamod");

    // === Tool Items ===

    /** Scan Tool: right-click to set corners, then scan a world region into a .blueprint file. */
    public static final DeferredItem<ScanToolItem> SCAN_TOOL = ITEMS.registerItem("scan_tool",
            props -> new ScanToolItem(props.stacksTo(1)));

    /** Build Tool: right-click to open the blueprint browser and place blueprints from packs. */
    public static final DeferredItem<BuildToolItem> BUILD_TOOL = ITEMS.registerItem("build_tool",
            props -> new BuildToolItem(props.stacksTo(1)));

    /** Shape Tool: right-click to open the shape generator and create procedural shapes. */
    public static final DeferredItem<ShapeToolItem> SHAPE_TOOL = ITEMS.registerItem("shape_tool",
            props -> new ShapeToolItem(props.stacksTo(1)));

    /** Tag Tool: right-click to tag/label positions in blueprints for substitution anchors. */
    public static final DeferredItem<TagToolItem> TAG_TOOL = ITEMS.registerItem("tag_tool",
            props -> new TagToolItem(props.stacksTo(1)));

    // === Creative Tab ===

    public static final Supplier<CreativeModeTab> BLUEPRINT_TAB = CREATIVE_MODE_TABS.register("megamod_blueprint_tab",
            () -> CreativeModeTab.builder()
                    .title((Component) Component.literal((String) "MegaMod - Blueprints"))
                    .icon(() -> new ItemStack((ItemLike) Items.PAPER))
                    .displayItems((parameters, output) -> {
                        output.accept((ItemLike) SCAN_TOOL.get());
                        output.accept((ItemLike) BUILD_TOOL.get());
                        output.accept((ItemLike) SHAPE_TOOL.get());
                        output.accept((ItemLike) TAG_TOOL.get());
                    }).build());

    /**
     * Initialize all deferred registers and network registration.
     * Call from the MegaMod main constructor with the mod event bus.
     *
     * @param modEventBus the mod event bus from the mod constructor
     */
    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(BlueprintNetwork::registerPayloads);
    }
}
