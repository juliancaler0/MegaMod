/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.material.MapColor
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredBlock
 *  net.neoforged.neoforge.registries.DeferredItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Blocks
 *  net.neoforged.neoforge.registries.DeferredRegister$Items
 */
package com.ultra.megamod.feature.museum;

import com.ultra.megamod.feature.museum.CuratorEntity;
import com.ultra.megamod.feature.museum.CuratorRenderer;
import com.ultra.megamod.feature.museum.MobNetItem;
import com.ultra.megamod.feature.museum.MuseumBlock;
import com.ultra.megamod.feature.museum.MuseumDoorBlock;
import com.ultra.megamod.feature.museum.PedestalBlock;
import com.ultra.megamod.feature.museum.network.MuseumNetwork;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MuseumRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks((String)"megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String)"megamod");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "megamod");
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create((ResourceKey)Registries.ENTITY_TYPE, (String)"megamod");
    public static final DeferredBlock<MuseumBlock> MUSEUM_BLOCK = BLOCKS.registerBlock("museum", MuseumBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(5.0f).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> MUSEUM_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("museum", MUSEUM_BLOCK);
    public static final DeferredBlock<MuseumDoorBlock> MUSEUM_DOOR_BLOCK = BLOCKS.registerBlock("museum_door", MuseumDoorBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(5.0f).noOcclusion().requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> MUSEUM_DOOR_ITEM = ITEMS.registerSimpleBlockItem("museum_door", MUSEUM_DOOR_BLOCK);
    public static final Supplier<BlockEntityType<MuseumDoorBlockEntity>> MUSEUM_DOOR_BE = BLOCK_ENTITIES.register("museum_door_be",
        () -> new BlockEntityType<>(MuseumDoorBlockEntity::new, MUSEUM_DOOR_BLOCK.get()));
    public static final DeferredItem<MobNetItem> MOB_NET_ITEM = ITEMS.registerItem("mob_net", props -> new MobNetItem(props.stacksTo(16)));
    public static final DeferredItem<Item> CAPTURED_MOB_ITEM = ITEMS.registerSimpleItem("captured_mob", () -> new Item.Properties().stacksTo(1));
    public static final DeferredBlock<PedestalBlock> PEDESTAL_BLOCK = BLOCKS.registerBlock("pedestal", PedestalBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> PEDESTAL_ITEM = ITEMS.registerSimpleBlockItem("pedestal", PEDESTAL_BLOCK);
    private static final ResourceKey<EntityType<?>> CURATOR_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"curator"));
    public static final Supplier<EntityType<CuratorEntity>> CURATOR_ENTITY = ENTITY_TYPES.register("curator", () -> EntityType.Builder.of(CuratorEntity::new, (MobCategory)MobCategory.CREATURE).sized(0.6f, 1.95f).build(CURATOR_KEY));

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ENTITY_TYPES.register(modBus);
        modBus.addListener(MuseumNetwork::registerPayloads);
        modBus.addListener((EntityAttributeCreationEvent event) -> event.put(CURATOR_ENTITY.get(), Mob.createMobAttributes().build()));
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT) {
            modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerEntityRenderer(CURATOR_ENTITY.get(), CuratorRenderer::new));
        }
    }
}

