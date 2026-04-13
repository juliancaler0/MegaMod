package com.ultra.megamod.feature.combat.archers.block;

import com.ultra.megamod.feature.combat.archers.ArchersMod;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.function.Supplier;

public class ArcherBlocks {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ArchersMod.ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ArchersMod.ID);

    public record Entry(String name, Supplier<Block> blockSupplier, Supplier<BlockItem> itemSupplier) {
        public Block block() { return blockSupplier.get(); }
        public BlockItem item() { return itemSupplier.get(); }
    }

    public static final ArrayList<Entry> all = new ArrayList<>();

    private static Entry entry(String name, Supplier<Block> blockFactory) {
        var blockHolder = BLOCKS.register(name, blockFactory);
        var itemHolder = ITEMS.register(name, () -> new BlockItem(blockHolder.get(), new Item.Properties()));
        var entry = new Entry(name, blockHolder, itemHolder);
        all.add(entry);
        return entry;
    }

    public static final Entry WORKBENCH = entry(ArcherWorkbenchBlock.BLOCK_ID.getPath(), () -> new ArcherWorkbenchBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    ));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
