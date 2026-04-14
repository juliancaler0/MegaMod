package com.ultra.megamod.reliquary.init;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.ultra.megamod.reliquary.Reliquary;
import com.ultra.megamod.reliquary.block.*;
import com.ultra.megamod.reliquary.block.tile.*;
import com.ultra.megamod.reliquary.item.block.BlockItemBase;
import com.ultra.megamod.reliquary.item.block.FertileLilyPadItem;
import com.ultra.megamod.reliquary.item.block.InterdictionTorchItem;
import com.ultra.megamod.reliquary.util.LegacyCapabilityAdapters;

import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {
	private ModBlocks() {}

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Reliquary.MOD_ID);
	private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Reliquary.MOD_ID);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Reliquary.MOD_ID);

	private static final String ALKAHESTRY_ALTAR_REGISTRY_NAME = "alkahestry_altar";
	private static final String INTERDICTION_TORCH_REGISTRY_NAME = "interdiction_torch";
	private static final String APOTHECARY_CAULDRON_REGISTRY_NAME = "apothecary_cauldron";
	private static final String APOTHECARY_MORTAR_REGISTRY_NAME = "apothecary_mortar";
	private static final String FERTILE_LILY_PAD_REGISTRY_NAME = "fertile_lily_pad";
	private static final String WRAITH_NODE_REGISTRY_NAME = "wraith_node";

	public static final Supplier<AlkahestryAltarBlock> ALKAHESTRY_ALTAR = BLOCKS.registerBlock(ALKAHESTRY_ALTAR_REGISTRY_NAME,
			AlkahestryAltarBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 5.0F));
	public static final Supplier<ApothecaryCauldronBlock> APOTHECARY_CAULDRON = BLOCKS.registerBlock(APOTHECARY_CAULDRON_REGISTRY_NAME,
			ApothecaryCauldronBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F, 5.0F).noOcclusion());
	public static final Supplier<ApothecaryMortarBlock> APOTHECARY_MORTAR = BLOCKS.registerBlock(APOTHECARY_MORTAR_REGISTRY_NAME,
			ApothecaryMortarBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 2.0F));
	public static final Supplier<FertileLilyPadBlock> FERTILE_LILY_PAD = BLOCKS.registerBlock(FERTILE_LILY_PAD_REGISTRY_NAME,
			FertileLilyPadBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT));
	public static final Supplier<InterdictionTorchBlock> INTERDICTION_TORCH = BLOCKS.registerBlock(INTERDICTION_TORCH_REGISTRY_NAME,
			InterdictionTorchBlock::new,
			BlockBehaviour.Properties.of().strength(0).lightLevel(value -> 15).randomTicks().sound(SoundType.WOOD).noCollision());
	public static final Supplier<WallInterdictionTorchBlock> WALL_INTERDICTION_TORCH = BLOCKS.registerBlock("wall_interdiction_torch",
			WallInterdictionTorchBlock::new,
			BlockBehaviour.Properties.of().strength(0).lightLevel(value -> 15).randomTicks().sound(SoundType.WOOD).noCollision());
	public static final Supplier<WraithNodeBlock> WRAITH_NODE = BLOCKS.registerBlock(WRAITH_NODE_REGISTRY_NAME,
			WraithNodeBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 5.0F).noOcclusion());

	public static final Map<DyeColor, Supplier<PassivePedestalBlock>> PASSIVE_PEDESTALS;
	public static final Map<DyeColor, Supplier<PedestalBlock>> PEDESTALS;

	static {
		ImmutableMap.Builder<DyeColor, Supplier<PassivePedestalBlock>> passiveBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<DyeColor, Supplier<PedestalBlock>> activeBuilder = ImmutableMap.builder();
		for (DyeColor color : DyeColor.values()) {
			passiveBuilder.put(color, BLOCKS.registerBlock("pedestals/passive/" + color.getName() + "_passive_pedestal",
					PassivePedestalBlock::new,
					BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 2.0F).forceSolidOn()));
			activeBuilder.put(color, BLOCKS.registerBlock("pedestals/" + color.getName() + "_pedestal",
					PedestalBlock::new,
					BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 2.0F).forceSolidOn()));
		}
		PASSIVE_PEDESTALS = passiveBuilder.build();
		PEDESTALS = activeBuilder.build();
	}

	public static final Supplier<BlockEntityType<AlkahestryAltarBlockEntity>> ALKAHESTRY_ALTAR_TILE_TYPE = BLOCK_ENTITY_TYPES.register(ALKAHESTRY_ALTAR_REGISTRY_NAME,
			() -> getTileEntityType(AlkahestryAltarBlockEntity::new, ALKAHESTRY_ALTAR.get()));
	public static final Supplier<BlockEntityType<PedestalBlockEntity>> PEDESTAL_TILE_TYPE = BLOCK_ENTITY_TYPES.register("pedestal",
			() -> getTileEntityType(PedestalBlockEntity::new, PEDESTALS.values().stream().map(Supplier::get).toArray(PedestalBlock[]::new)));
	public static final Supplier<BlockEntityType<PassivePedestalBlockEntity>> PASSIVE_PEDESTAL_TILE_TYPE = BLOCK_ENTITY_TYPES.register("passive_pedestal",
			() -> getTileEntityType(PassivePedestalBlockEntity::new, PASSIVE_PEDESTALS.values().stream().map(Supplier::get).toArray(PassivePedestalBlock[]::new)));
	public static final Supplier<BlockEntityType<ApothecaryCauldronBlockEntity>> APOTHECARY_CAULDRON_TILE_TYPE = BLOCK_ENTITY_TYPES.register(APOTHECARY_CAULDRON_REGISTRY_NAME,
			() -> getTileEntityType(ApothecaryCauldronBlockEntity::new, APOTHECARY_CAULDRON.get()));
	public static final Supplier<BlockEntityType<ApothecaryMortarBlockEntity>> APOTHECARY_MORTAR_TILE_TYPE = BLOCK_ENTITY_TYPES.register(APOTHECARY_MORTAR_REGISTRY_NAME,
			() -> getTileEntityType(ApothecaryMortarBlockEntity::new, APOTHECARY_MORTAR.get()));

	public static final Supplier<BlockItemBase> ALKAHESTRY_ALTAR_ITEM = ITEMS.registerItem(ALKAHESTRY_ALTAR_REGISTRY_NAME, props -> new BlockItemBase(ALKAHESTRY_ALTAR.get(), props));
	public static final Supplier<BlockItemBase> APOTHECARY_CAULDRON_ITEM = ITEMS.registerItem(APOTHECARY_CAULDRON_REGISTRY_NAME, props -> new BlockItemBase(APOTHECARY_CAULDRON.get(), props));
	public static final Supplier<BlockItemBase> APOTHECARY_MORTAR_ITEM = ITEMS.registerItem(APOTHECARY_MORTAR_REGISTRY_NAME, props -> new BlockItemBase(APOTHECARY_MORTAR.get(), props));
	public static final Supplier<FertileLilyPadItem> FERTILE_LILY_PAD_ITEM = ITEMS.registerItem(FERTILE_LILY_PAD_REGISTRY_NAME, FertileLilyPadItem::new);
	public static final Supplier<BlockItemBase> WRAITH_NODE_ITEM = ITEMS.registerItem(WRAITH_NODE_REGISTRY_NAME, props -> new BlockItemBase(WRAITH_NODE.get(), props));
	public static final Supplier<InterdictionTorchItem> INTERDICTION_TORCH_ITEM = ITEMS.registerItem(INTERDICTION_TORCH_REGISTRY_NAME, InterdictionTorchItem::new);
	public static final Map<DyeColor, Supplier<BlockItem>> PEDESTAL_ITEMS;
	public static final Map<DyeColor, Supplier<BlockItem>> PASSIVE_PEDESTAL_ITEMS;

	private static final String BLOCK_PREFIX = "block.";

	static {
		ImmutableMap.Builder<DyeColor, Supplier<BlockItem>> passiveBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<DyeColor, Supplier<BlockItem>> activeBuilder = ImmutableMap.builder();
		for (DyeColor color : DyeColor.values()) {
			// Port note (1.21.11): Item#getDescriptionId() is now final, so per-DyeColor
			// description ids are no longer possible. We collapse every colour variant's display
			// name onto the shared "block.reliquary.passive_pedestal" / ".pedestal" keys via the
			// getName override below — same aggregated tooltip behaviour as the original.
			passiveBuilder.put(color, ITEMS.registerItem("pedestals/passive/" + color.getName() + "_passive_pedestal", props -> new BlockItemBase(PASSIVE_PEDESTALS.get(color).get(), props) {
				@Override
				public Component getName(ItemStack stack) {
					return Component.translatable(BLOCK_PREFIX + Reliquary.MOD_ID + ".passive_pedestal");
				}
			}));
			activeBuilder.put(color, ITEMS.registerItem("pedestals/" + color.getName() + "_pedestal", props -> new BlockItemBase(PEDESTALS.get(color).get(), props) {
				@Override
				public Component getName(ItemStack stack) {
					return Component.translatable(BLOCK_PREFIX + Reliquary.MOD_ID + ".pedestal");
				}
			}));
		}
		PASSIVE_PEDESTAL_ITEMS = passiveBuilder.build();
		PEDESTAL_ITEMS = activeBuilder.build();
	}

	public static void registerListeners(IEventBus modBus) {
		ITEMS.register(modBus);
		BLOCKS.register(modBus);
		BLOCK_ENTITY_TYPES.register(modBus);
		modBus.addListener(ModBlocks::registerCapabilities);
	}

	@SuppressWarnings({"squid:S4449", "ConstantConditions"}) // no datafixer is defined for any of the tile entities so this is moot
	private static <T extends BlockEntity> BlockEntityType<T> getTileEntityType(BlockEntityType.BlockEntitySupplier<T> tileFactory, Block... validBlocks) {
		// Port note (1.21.11): BlockEntityType.Builder#of(...).build(...) was removed; the
		// BlockEntityType public constructor now takes (supplier, validBlocks...) directly and
		// the DataFixer argument is no longer required (vanilla handles that internally).
		return new BlockEntityType<>(tileFactory, validBlocks);
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		// Passive pedestal: item inventory capability (single-slot item holder).
		event.registerBlockEntity(
				Capabilities.Item.BLOCK,
				PASSIVE_PEDESTAL_TILE_TYPE.get(),
				(be, side) -> LegacyCapabilityAdapters.asItemResourceHandler(be.getItemHandler()));

		// Active pedestal: item inventory (own + optional sub-inventory from held item)
		// and pass-through fluid handler.
		event.registerBlockEntity(
				Capabilities.Item.BLOCK,
				PEDESTAL_TILE_TYPE.get(),
				(be, side) -> LegacyCapabilityAdapters.asItemResourceHandler(be.getItemHandler()));
		event.registerBlockEntity(
				Capabilities.Fluid.BLOCK,
				PEDESTAL_TILE_TYPE.get(),
				(be, side) -> LegacyCapabilityAdapters.asFluidResourceHandler(be.getFluidHandler()));

		// Apothecary mortar: item inventory (grinding slots).
		event.registerBlockEntity(
				Capabilities.Item.BLOCK,
				APOTHECARY_MORTAR_TILE_TYPE.get(),
				(be, side) -> LegacyCapabilityAdapters.asItemResourceHandler(be.getItems()));
	}
}
