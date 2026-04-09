package com.ultra.megamod.feature.casino;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;
import com.ultra.megamod.feature.casino.slots.SlotMachineBlock;
import com.ultra.megamod.feature.casino.slots.SlotMachineBlockEntity;
import com.ultra.megamod.feature.casino.blackjack.BlackjackTableBlock;
import com.ultra.megamod.feature.casino.blackjack.BlackjackTableBlockEntity;
import com.ultra.megamod.feature.casino.wheel.WheelBlock;
import com.ultra.megamod.feature.casino.wheel.WheelBlockEntity;
import com.ultra.megamod.feature.casino.network.CasinoNetwork;

public class CasinoRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "megamod");
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
            (ResourceKey) Registries.ENTITY_TYPE, (String) "megamod");

    // =============================================
    // --- Casino Blocks ---
    // =============================================
    public static final DeferredBlock<SlotMachineBlock> SLOT_MACHINE = BLOCKS.registerBlock("slot_machine",
            SlotMachineBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).noOcclusion());
    public static final DeferredItem<BlockItem> SLOT_MACHINE_ITEM = ITEMS.registerSimpleBlockItem("slot_machine", SLOT_MACHINE);

    public static final DeferredBlock<BlackjackTableBlock> BLACKJACK_TABLE = BLOCKS.registerBlock("blackjack_table",
            BlackjackTableBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(3.0f).noOcclusion());
    public static final DeferredItem<BlockItem> BLACKJACK_TABLE_ITEM = ITEMS.registerSimpleBlockItem("blackjack_table", BLACKJACK_TABLE);

    public static final DeferredBlock<WheelBlock> WHEEL = BLOCKS.registerBlock("wheel",
            WheelBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(3.0f).noOcclusion());
    public static final DeferredItem<BlockItem> WHEEL_ITEM = ITEMS.registerSimpleBlockItem("wheel", WHEEL);

    public static final DeferredBlock<BlackjackChairBlock> BLACKJACK_CHAIR = BLOCKS.registerBlock("blackjack_chair",
            BlackjackChairBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> BLACKJACK_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("blackjack_chair", BLACKJACK_CHAIR);

    public static final DeferredBlock<WheelChairBlock> WHEEL_CHAIR = BLOCKS.registerBlock("wheel_chair",
            WheelChairBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());
    public static final DeferredItem<BlockItem> WHEEL_CHAIR_ITEM = ITEMS.registerSimpleBlockItem("wheel_chair", WHEEL_CHAIR);

    // =============================================
    // --- Block Entities ---
    // =============================================
    public static final Supplier<BlockEntityType<SlotMachineBlockEntity>> SLOT_MACHINE_BE = BLOCK_ENTITIES.register("slot_machine_be",
            () -> new BlockEntityType<>(SlotMachineBlockEntity::new, SLOT_MACHINE.get()));

    public static final Supplier<BlockEntityType<BlackjackTableBlockEntity>> BLACKJACK_TABLE_BE = BLOCK_ENTITIES.register("blackjack_table_be",
            () -> new BlockEntityType<>(BlackjackTableBlockEntity::new, BLACKJACK_TABLE.get()));

    public static final Supplier<BlockEntityType<WheelBlockEntity>> WHEEL_BE = BLOCK_ENTITIES.register("wheel_be",
            () -> new BlockEntityType<>(WheelBlockEntity::new, WHEEL.get()));

    // =============================================
    // --- Entities ---
    // =============================================
    private static ResourceKey<EntityType<?>> entityKey(String name) {
        return ResourceKey.create((ResourceKey) Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath("megamod", name));
    }

    public static final Supplier<EntityType<DealerEntity>> DEALER = ENTITY_TYPES.register("casino_dealer",
            () -> EntityType.Builder.of(DealerEntity::new, (MobCategory) MobCategory.CREATURE)
                    .sized(0.6f, 1.95f).clientTrackingRange(10).build(entityKey("casino_dealer")));

    @SuppressWarnings("unchecked")
    public static final Supplier<EntityType<CashierEntity>> CASHIER = ENTITY_TYPES.register("casino_cashier",
            () -> EntityType.Builder.of((EntityType.EntityFactory<CashierEntity>) CashierEntity::new, (MobCategory) MobCategory.CREATURE)
                    .sized(0.6f, 1.95f).clientTrackingRange(10).build(entityKey("casino_cashier")));

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ENTITY_TYPES.register(modBus);
        modBus.addListener(CasinoNetwork::registerPayloads);
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT) {
            modBus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) -> {
                event.registerEntityRenderer(DEALER.get(), DealerRenderer::new);
                event.registerEntityRenderer(CASHIER.get(), CashierRenderer::new);
            });
        }
        modBus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) -> {
                event.put(DEALER.get(), DealerEntity.createDealerAttributes().build());
                event.put(CASHIER.get(), CashierEntity.createCashierAttributes().build());
        });
    }
}
