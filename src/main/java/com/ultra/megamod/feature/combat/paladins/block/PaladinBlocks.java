package com.ultra.megamod.feature.combat.paladins.block;

import com.ultra.megamod.MegaMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PaladinBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MegaMod.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);

    public static final DeferredBlock<MonkWorkbenchBlock> MONK_WORKBENCH = BLOCKS.register(
            MonkWorkbenchBlock.NAME,
            () -> new MonkWorkbenchBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .noOcclusion())
    );

    public static final DeferredItem<BlockItem> MONK_WORKBENCH_ITEM = ITEMS.register(
            MonkWorkbenchBlock.NAME,
            () -> new BlockItem(MONK_WORKBENCH.get(), new Item.Properties())
    );

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
