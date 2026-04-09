/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.material.MapColor
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredBlock
 *  net.neoforged.neoforge.registries.DeferredItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Blocks
 *  net.neoforged.neoforge.registries.DeferredRegister$Items
 */
package com.ultra.megamod.feature.computer;

import com.ultra.megamod.feature.computer.ComputerBlock;
import com.ultra.megamod.feature.computer.ComputerBlockEntity;
import com.ultra.megamod.feature.computer.DecorationBlock;
import com.ultra.megamod.feature.computer.network.ComputerNetwork;
import com.ultra.megamod.feature.economy.AtmBlock;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ComputerRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks((String)"megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String)"megamod");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create((ResourceKey)Registries.BLOCK_ENTITY_TYPE, (String)"megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"megamod");
    public static final DeferredBlock<ComputerBlock> COMPUTER_BLOCK = BLOCKS.registerBlock("computer", ComputerBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).requiresCorrectToolForDrops().noOcclusion());
    public static final DeferredItem<BlockItem> COMPUTER_ITEM = ITEMS.registerSimpleBlockItem("computer", COMPUTER_BLOCK);
    public static final DeferredBlock<AtmBlock> ATM_BLOCK = BLOCKS.registerBlock("atm", AtmBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).requiresCorrectToolForDrops().noOcclusion());
    public static final DeferredItem<BlockItem> ATM_ITEM = ITEMS.registerSimpleBlockItem("atm", ATM_BLOCK);
    public static final DeferredItem<PhoneItem> PHONE_ITEM = ITEMS.registerItem("phone", props -> new PhoneItem(props.stacksTo(1)));
    public static final Supplier<BlockEntityType<ComputerBlockEntity>> COMPUTER_BLOCK_ENTITY = BLOCK_ENTITIES.register("computer", () -> new BlockEntityType(ComputerBlockEntity::new, new Block[]{(Block)COMPUTER_BLOCK.get()}));
    public static final Supplier<CreativeModeTab> MEGAMOD_TAB = CREATIVE_MODE_TABS.register("megamod_tab", () -> CreativeModeTab.builder().title((Component)Component.literal((String)"MegaMod")).icon(() -> new ItemStack((ItemLike)Items.BEDROCK)).displayItems((parameters, output) -> {
        output.accept((ItemLike)COMPUTER_ITEM.get());
        output.accept((ItemLike)ATM_ITEM.get());
        output.accept((ItemLike)PHONE_ITEM.get());
        output.accept((ItemLike)com.ultra.megamod.feature.marketplace.MarketplaceRegistry.TRADING_TERMINAL_ITEM.get());
    }).build());

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        modBus.addListener(ComputerNetwork::registerPayloads);
        modBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) -> {
        });
    }
}

