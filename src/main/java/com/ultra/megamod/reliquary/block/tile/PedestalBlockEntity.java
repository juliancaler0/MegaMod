package com.ultra.megamod.reliquary.block.tile;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import com.ultra.megamod.reliquary.api.IPedestal;
import com.ultra.megamod.reliquary.api.IPedestalActionItem;
import com.ultra.megamod.reliquary.api.IPedestalRedstoneItem;
import com.ultra.megamod.reliquary.api.IPedestalRedstoneItemWrapper;
import com.ultra.megamod.reliquary.block.PedestalBlock;
import com.ultra.megamod.reliquary.init.ModBlocks;
import com.ultra.megamod.reliquary.pedestal.PedestalRegistry;
import com.ultra.megamod.reliquary.util.CombinedItemHandler;
import com.ultra.megamod.reliquary.util.FakePlayerFactory;
import com.ultra.megamod.reliquary.util.InventoryHelper;
import com.ultra.megamod.reliquary.util.LegacyCapabilityAdapters;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PedestalBlockEntity extends PassivePedestalBlockEntity implements IPedestal {
	private boolean tickable = false;
	private int actionCooldown = 0;
	@Nullable
	private IPedestalActionItem actionItem = null;
	@Nullable
	private IPedestalRedstoneItem redstoneItem = null;
	@Nullable
	private IItemHandler itemHandler = null;
	@Nullable
	private IItemHandler combinedHandler = null;
	private ItemStack fluidContainer = ItemStack.EMPTY;
	private boolean switchedOn = false;
	private final List<Long> onSwitches = new ArrayList<>();
	private boolean enabledInitialized = false;
	private boolean powered = false;
	private PedestalFluidHandler pedestalFluidHandler = null;
	private Object itemData = null;

	public PedestalBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.PEDESTAL_TILE_TYPE.get(), pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		switchedOn = input.getBooleanOr("SwitchedOn", false);
		powered = input.getBooleanOr("Powered", false);

		onSwitches.clear();
		input.listOrEmpty("OnSwitches", Codec.LONG).forEach(onSwitches::add);

		updateSpecialItems();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);

		output.putBoolean("SwitchedOn", switchedOn);
		output.putBoolean("Powered", powered);

		ValueOutput.TypedOutputList<Long> onLocations = output.list("OnSwitches", Codec.LONG);
		for (Long onSwitch : onSwitches) {
			onLocations.add(onSwitch);
		}
	}

	@Override
	public void onChunkUnloaded() {
		if (level != null && !level.isClientSide()) {
			PedestalRegistry.unregisterPosition(level.dimension().registry(), worldPosition);
		}

		super.onChunkUnloaded();
	}

	@Override
	public void onLoad() {
		if (level != null && !level.isClientSide()) {
			PedestalRegistry.registerPosition(level.dimension().registry(), worldPosition);
		}

		super.onLoad();
	}

	@Override
	public IItemHandler getItemHandler() {
		IItemHandler superInventory = super.getItemHandler();
		if (itemHandler == null) {
			return superInventory;
		}
		if (combinedHandler == null) {
			combinedHandler = new CombinedItemHandler(superInventory, itemHandler);
		}
		return combinedHandler;
	}

	public IFluidHandler getFluidHandler() {
		if (pedestalFluidHandler == null) {
			pedestalFluidHandler = new PedestalFluidHandler(this);
		}
		return pedestalFluidHandler;
	}

	public void executeOnActionItem(Consumer<IPedestalActionItem> execute) {
		if (actionItem == null) {
			return;
		}
		execute.accept(actionItem);
	}

	private void executeOnRedstoneItem(Consumer<IPedestalRedstoneItem> execute) {
		if (redstoneItem == null) {
			return;
		}
		execute.accept(redstoneItem);
	}

	private void updateSpecialItems() {
		resetSpecialItems();

		ItemStack item = getItem();
		if (item.isEmpty()) {
			return;
		}

		// Item-based sub-inventory: if the held item exposes an item capability (e.g. a
		// backpack-like container) surface it via the combined handler.
		net.neoforged.neoforge.transfer.access.ItemAccess itemAccess =
				net.neoforged.neoforge.transfer.access.ItemAccess.forStack(item);
		net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> itemCap =
				item.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Item.ITEM, itemAccess);
		if (itemCap != null) {
			itemHandler = IItemHandler.of(itemCap);
		}

		if (item.getItem() instanceof IPedestalActionItem pedestalActionItem) {
			tickable = true;
			actionItem = pedestalActionItem;
		} else if (item.getItem() instanceof IPedestalRedstoneItem pedestalRedstoneItem) {
			redstoneItem = pedestalRedstoneItem;
		} else {
			PedestalRegistry.getItemWrapper(item).ifPresent(wrapper -> {
				if (wrapper instanceof IPedestalActionItem pedestalActionItem) {
					tickable = true;
					actionItem = pedestalActionItem;
				}
				if (wrapper instanceof IPedestalRedstoneItemWrapper) {
					redstoneItem = (IPedestalRedstoneItem) wrapper;
				}
			});
		}


		IFluidHandlerItem itemFluidHandler = LegacyCapabilityAdapters.getItemFluidHandler(item, itemAccess);
		if (itemFluidHandler != null) {
			fluidContainer = item;
		}

		actionCooldown = 0;
	}

	private void resetSpecialItems() {
		tickable = false;
		fluidContainer = ItemStack.EMPTY;
		actionItem = null;
		redstoneItem = null;
		itemHandler = null;
		if (combinedHandler != null) {
			level.invalidateCapabilities(getBlockPos());
		}
		combinedHandler = null;
	}

	public void serverTick(Level level) {
		if (level.isClientSide()) {
			return;
		}

		if (!enabledInitialized) {
			enabledInitialized = true;
			neighborUpdate(level);
		}

		if (tickable && isEnabled()) {
			if (actionCooldown > 0) {
				actionCooldown--;
			} else {
				executeOnActionItem(ai -> ai.update(getItem(), level, this));
			}
		}
	}

	public void neighborUpdate(Level level) {
		if (powered != level.hasNeighborSignal(worldPosition)) {
			powered = level.hasNeighborSignal(worldPosition);

			if (powered) {
				switchOn(level, BlockPos.ZERO);
			} else {
				switchOff(level, BlockPos.ZERO);
			}
		}

		updateRedstone(level);
	}

	public void updateRedstone(Level level) {
		executeOnRedstoneItem(ri -> ri.updateRedstone(getItem(), level, this));
	}

	@Override
	public BlockPos getBlockPosition() {
		return getBlockPos();
	}

	@Override
	public int addToConnectedInventory(Level level, ItemStack stack) {
		int numberAdded = 0;
		for (Direction side : Direction.values()) {
			numberAdded += InventoryHelper.tryToAddToInventoryAtPos(stack, level, worldPosition.relative(side), side.getOpposite(), stack.getCount() - numberAdded);
			if (numberAdded >= stack.getCount()) {
				break;
			}
		}

		return numberAdded;
	}

	@Override
	public int fillConnectedTank(FluidStack fluidStack, IFluidHandler.FluidAction action) {
		List<IFluidHandler> adjacentTanks = getAdjacentTanks();

		int fluidFilled = 0;
		FluidStack copy = fluidStack.copy();

		for (IFluidHandler tank : adjacentTanks) {
			if (tank.fill(copy, IFluidHandler.FluidAction.SIMULATE) == copy.getAmount()) {
				fluidFilled += tank.fill(copy, action);

				if (fluidFilled >= fluidStack.getAmount()) {
					break;
				} else {
					copy.setAmount(fluidStack.getAmount() - fluidFilled);
				}
			}
		}

		return fluidFilled;
	}

	@Override
	public int fillConnectedTank(FluidStack fluidStack) {
		return fillConnectedTank(fluidStack, IFluidHandler.FluidAction.EXECUTE);
	}

	@Override
	public void setActionCoolDown(int coolDownTicks) {
		actionCooldown = coolDownTicks;
	}

	@Override
	public Optional<FakePlayer> getFakePlayer() {
		if (level == null || !(level instanceof ServerLevel serverLevel)) {
			return Optional.empty();
		}
		return Optional.of(FakePlayerFactory.get(serverLevel));
	}

	@Override
	public void destroyItem() {
		setItem(ItemStack.EMPTY);
	}

	@Override
	public List<BlockPos> getPedestalsInRange(Level level, int range) {
		return PedestalRegistry.getPositionsInRange(level.dimension().registry(), worldPosition, range);
	}

	@Override
	public void switchOn(Level level, BlockPos switchedOnFrom) {
		if (switchedOnFrom != BlockPos.ZERO && !onSwitches.contains(switchedOnFrom.asLong())) {
			onSwitches.add(switchedOnFrom.asLong());
		}

		setEnabled(level, true);

		BlockState blockState = level.getBlockState(worldPosition);
		level.sendBlockUpdated(worldPosition, blockState, blockState, 3);
	}

	@Override
	public void switchOff(Level level, BlockPos switchedOffFrom) {
		if (switchedOffFrom != BlockPos.ZERO) {
			onSwitches.remove(switchedOffFrom.asLong());
		}

		if (!switchedOn && !powered && onSwitches.isEmpty()) {
			setEnabled(level, false);
		}
		BlockState blockState = level.getBlockState(worldPosition);
		level.sendBlockUpdated(worldPosition, blockState, blockState, 3);
	}

	@Override
	public Optional<Object> getItemData() {
		return Optional.ofNullable(itemData);
	}

	@Override
	public void setItemData(@Nullable Object data) {
		itemData = data;
	}

	@Override
	public boolean switchedOn() {
		return switchedOn;
	}

	public boolean isPowered() {
		return powered;
	}

	public List<Long> getOnSwitches() {
		return onSwitches;
	}

	private void setEnabled(Level level, boolean switchedOn) {
		if (level.getBlockState(worldPosition).getBlock() instanceof PedestalBlock) {
			level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(PedestalBlock.ENABLED, switchedOn));
			if (!switchedOn) {
				executeOnActionItem(ai -> ai.stop(getItem(), level, this));
			}
		}
		setChanged();
	}

	private List<IFluidHandler> getAdjacentTanks() {
		List<IFluidHandler> adjacentTanks = new ArrayList<>();

		for (Direction side : Direction.values()) {
			BlockPos tankPos = getBlockPos().relative(side);
			Direction tankDirection = side.getOpposite();
			addIfTank(adjacentTanks, tankPos, tankDirection);
		}

		return adjacentTanks;
	}

	private void addIfTank(List<IFluidHandler> adjacentTanks, BlockPos tankPos, Direction tankDirection) {
		if (level == null) {
			return;
		}
		IFluidHandler adjacent = LegacyCapabilityAdapters.getBlockFluidHandler(level, tankPos, tankDirection);
		if (adjacent != null) {
			adjacentTanks.add(adjacent);
		}
	}

	public void removeSpecialItems(Level level) {
		removeSpecialItems(level, getItem());
	}

	public void removeSpecialItems(Level level, ItemStack itemBeingRemoved) {
		executeOnRedstoneItem(ri -> ri.onRemoved(itemBeingRemoved, level, this));
		executeOnActionItem(ai -> ai.onRemoved(itemBeingRemoved, level, this));
	}

	@Override
	public void removeAndSpawnItem(Level level) {
		removeSpecialItems(level);
		resetSpecialItems();
		super.removeAndSpawnItem(level);
	}

	@Override
	protected void onItemRemoved(ItemStack itemBeingRemoved) {
		super.onItemRemoved(itemBeingRemoved);
		if (level != null) {
			removeSpecialItems(level, itemBeingRemoved);
		}
		updateItemsAndBlock();
	}

	@Override
	protected void onItemAdded() {
		super.onItemAdded();
		updateItemsAndBlock();
	}

	private void updateItemsAndBlock() {
		updateSpecialItems();
		if (level == null) {
			return;
		}
		updateRedstone(level);
		BlockState blockState = level.getBlockState(getBlockPos());
		level.sendBlockUpdated(getBlockPos(), blockState, blockState, 3);
	}

	public void toggleSwitch(Level level) {
		switchedOn = !switchedOn;

		if (switchedOn) {
			switchOn(level, BlockPos.ZERO);
		} else {
			switchOff(level, BlockPos.ZERO);
		}

		updateRedstone(level);
	}

	ItemStack getFluidContainer() {
		return fluidContainer;
	}

	public boolean isEnabled() {
		return getBlockState().getValue(PedestalBlock.ENABLED);
	}
}