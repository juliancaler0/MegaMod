package com.ultra.megamod.feature.combat.items;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.util.valueproviders.UniformInt;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Gem ore blocks and gem vein blocks ported from the Jewelry mod.
 * Individual gem ores (deepslate_ruby_ore, etc.) generate at diamond depth.
 * Gem vein blocks (gem_vein, deepslate_gem_vein) drop a random gem when mined.
 * All require iron pickaxe or better to mine.
 */
public class GemOreRegistry {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MegaMod.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);

    // ── Gem Vein blocks (from Jewelry mod — drop random gems) ──

    private static BlockBehaviour.Properties stoneOreProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties deepslateOreProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .sound(SoundType.DEEPSLATE)
                .requiresCorrectToolForDrops();
    }

    // Gem Vein (stone variant) — replaces stone at ore generation
    public static final DeferredBlock<Block> GEM_VEIN = BLOCKS.registerBlock("gem_vein",
            props -> new DropExperienceBlock(UniformInt.of(3, 7), props), GemOreRegistry::stoneOreProps);

    // Deepslate Gem Vein — replaces deepslate at ore generation
    public static final DeferredBlock<Block> DEEPSLATE_GEM_VEIN = BLOCKS.registerBlock("deepslate_gem_vein",
            props -> new DropExperienceBlock(UniformInt.of(3, 7), props), GemOreRegistry::deepslateOreProps);

    // Block items for gem veins
    public static final DeferredItem<BlockItem> GEM_VEIN_ITEM = ITEMS.registerSimpleBlockItem(GEM_VEIN);
    public static final DeferredItem<BlockItem> DEEPSLATE_GEM_VEIN_ITEM = ITEMS.registerSimpleBlockItem(DEEPSLATE_GEM_VEIN);

    // ── Individual deepslate gem ores ──

    // Deepslate gem ores (primary — found at diamond depths)
    public static final DeferredBlock<Block> DEEPSLATE_RUBY_ORE = BLOCKS.registerBlock("deepslate_ruby_ore",
            props -> new DropExperienceBlock(UniformInt.of(3, 7), props), GemOreRegistry::deepslateOreProps);
    public static final DeferredBlock<Block> DEEPSLATE_TOPAZ_ORE = BLOCKS.registerBlock("deepslate_topaz_ore",
            props -> new DropExperienceBlock(UniformInt.of(3, 7), props), GemOreRegistry::deepslateOreProps);
    public static final DeferredBlock<Block> DEEPSLATE_CITRINE_ORE = BLOCKS.registerBlock("deepslate_citrine_ore",
            props -> new DropExperienceBlock(UniformInt.of(3, 7), props), GemOreRegistry::deepslateOreProps);
    public static final DeferredBlock<Block> DEEPSLATE_JADE_ORE = BLOCKS.registerBlock("deepslate_jade_ore",
            props -> new DropExperienceBlock(UniformInt.of(3, 7), props), GemOreRegistry::deepslateOreProps);
    public static final DeferredBlock<Block> DEEPSLATE_SAPPHIRE_ORE = BLOCKS.registerBlock("deepslate_sapphire_ore",
            props -> new DropExperienceBlock(UniformInt.of(3, 7), props), GemOreRegistry::deepslateOreProps);
    public static final DeferredBlock<Block> DEEPSLATE_TANZANITE_ORE = BLOCKS.registerBlock("deepslate_tanzanite_ore",
            props -> new DropExperienceBlock(UniformInt.of(3, 7), props), GemOreRegistry::deepslateOreProps);

    // Block items
    public static final DeferredItem<BlockItem> DEEPSLATE_RUBY_ORE_ITEM = ITEMS.registerSimpleBlockItem(DEEPSLATE_RUBY_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_TOPAZ_ORE_ITEM = ITEMS.registerSimpleBlockItem(DEEPSLATE_TOPAZ_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_CITRINE_ORE_ITEM = ITEMS.registerSimpleBlockItem(DEEPSLATE_CITRINE_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_JADE_ORE_ITEM = ITEMS.registerSimpleBlockItem(DEEPSLATE_JADE_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_SAPPHIRE_ORE_ITEM = ITEMS.registerSimpleBlockItem(DEEPSLATE_SAPPHIRE_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_TANZANITE_ORE_ITEM = ITEMS.registerSimpleBlockItem(DEEPSLATE_TANZANITE_ORE);

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }
}
