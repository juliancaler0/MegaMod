package com.ultra.megamod.reliquary.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * Adapter helpers that bridge the Reliquary port's legacy {@link IItemHandler}/{@link IFluidHandler}
 * implementations to the new {@link ResourceHandler} API introduced in 1.21.9+.
 *
 * <p>The Reliquary codebase still implements the pre-1.21.11 {@code IItemHandler}/{@code IFluidHandler}
 * interfaces. Rather than rewriting every handler against {@link ResourceHandler} we provide small
 * adapter classes that expose the legacy handlers as {@code ResourceHandler<ItemResource>} or
 * {@code ResourceHandler<FluidResource>}. This is the inverse of the deprecated
 * {@link IItemHandler#of(ResourceHandler)} helper that NeoForge ships for the other direction.
 *
 * <p>The adapters are not transaction-aware: simulated operations honour the transaction boundary, but
 * committed state changes happen immediately via the underlying legacy handlers. This matches the
 * behaviour of the legacy handlers (which had no transaction concept) and is sufficient for Reliquary's
 * usage patterns.
 */
public final class LegacyCapabilityAdapters {
	private LegacyCapabilityAdapters() {}

	// ------------------------------------------------------------------
	//  Item side
	// ------------------------------------------------------------------

	/**
	 * Wraps a legacy {@link IItemHandler} as a {@link ResourceHandler} of {@link ItemResource}s.
	 */
	public static ResourceHandler<ItemResource> asItemResourceHandler(IItemHandler handler) {
		return new ItemHandlerToResourceHandler(handler);
	}

	/**
	 * Attempts to fetch a legacy {@link IItemHandler} view of the block-entity inventory at the given
	 * position. Returns {@code null} if no capability is available.
	 */
	@Nullable
	public static IItemHandler getBlockItemHandler(Level level, BlockPos pos, @Nullable Direction side) {
		ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
		return handler == null ? null : IItemHandler.of(handler);
	}

	// ------------------------------------------------------------------
	//  Fluid side
	// ------------------------------------------------------------------

	/**
	 * Wraps a legacy {@link IFluidHandler} as a {@link ResourceHandler} of {@link FluidResource}s.
	 */
	public static ResourceHandler<FluidResource> asFluidResourceHandler(IFluidHandler handler) {
		return new FluidHandlerToResourceHandler(handler);
	}

	/**
	 * Wraps a legacy {@link IFluidHandlerItem} as a {@link ResourceHandler} of {@link FluidResource}s,
	 * writing the resulting container back to the given {@link ItemAccess} after every mutation.
	 */
	public static ResourceHandler<FluidResource> asFluidResourceHandlerItem(IFluidHandlerItem handler, ItemAccess access) {
		return new FluidHandlerItemToResourceHandler(handler, access);
	}

	/**
	 * Attempts to fetch a legacy {@link IFluidHandler} view of the block-entity tank at the given
	 * position. Returns {@code null} if no capability is available.
	 */
	@Nullable
	public static IFluidHandler getBlockFluidHandler(Level level, BlockPos pos, @Nullable Direction side) {
		ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
		return handler == null ? null : IFluidHandler.of(handler);
	}

	/**
	 * Attempts to fetch a legacy {@link IFluidHandlerItem} view of the given item stack. Uses the
	 * in-hand / mutable access provided by the caller (for example the held item on a pedestal) so
	 * the underlying container may be swapped when the fluid capability mutates it.
	 */
	@Nullable
	public static IFluidHandlerItem getItemFluidHandler(ItemStack stack, ItemAccess access) {
		if (stack.isEmpty()) {
			return null;
		}
		ResourceHandler<FluidResource> handler = stack.getCapability(Capabilities.Fluid.ITEM, access);
		if (handler == null) {
			return null;
		}
		return new ResourceHandlerToFluidHandlerItem(handler, access);
	}

	// ------------------------------------------------------------------
	//  Adapter implementations
	// ------------------------------------------------------------------

	private static final class ItemHandlerToResourceHandler implements ResourceHandler<ItemResource> {
		private final IItemHandler delegate;

		ItemHandlerToResourceHandler(IItemHandler delegate) {
			this.delegate = delegate;
		}

		@Override
		public int size() {
			return delegate.getSlots();
		}

		@Override
		public ItemResource getResource(int index) {
			return ItemResource.of(delegate.getStackInSlot(index));
		}

		@Override
		public long getAmountAsLong(int index) {
			return delegate.getStackInSlot(index).getCount();
		}

		@Override
		public long getCapacityAsLong(int index, ItemResource resource) {
			return delegate.getSlotLimit(index);
		}

		@Override
		public boolean isValid(int index, ItemResource resource) {
			return delegate.isItemValid(index, resource.toStack(1));
		}

		@Override
		public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
			if (resource.isEmpty() || amount <= 0) {
				return 0;
			}
			ItemStack stack = resource.toStack(Math.min(amount, resource.getMaxStackSize()));
			ItemStack remaining = delegate.insertItem(index, stack, true);
			int inserted = stack.getCount() - remaining.getCount();
			if (inserted <= 0) {
				return 0;
			}
			if (transaction.depth() == 0) {
				ItemStack executed = delegate.insertItem(index, stack, false);
				return stack.getCount() - executed.getCount();
			}
			// Inside a nested transaction we can only report the simulated outcome; committing is
			// deferred to the root transaction which the caller is expected to drive manually.
			return inserted;
		}

		@Override
		public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
			if (resource.isEmpty() || amount <= 0) {
				return 0;
			}
			ItemStack simulated = delegate.extractItem(index, amount, true);
			if (simulated.isEmpty() || !resource.matches(simulated)) {
				return 0;
			}
			if (transaction.depth() == 0) {
				ItemStack executed = delegate.extractItem(index, amount, false);
				if (!resource.matches(executed)) {
					return 0;
				}
				return executed.getCount();
			}
			return simulated.getCount();
		}
	}

	private static class FluidHandlerToResourceHandler implements ResourceHandler<FluidResource> {
		protected final IFluidHandler delegate;

		FluidHandlerToResourceHandler(IFluidHandler delegate) {
			this.delegate = delegate;
		}

		@Override
		public int size() {
			return delegate.getTanks();
		}

		@Override
		public FluidResource getResource(int index) {
			return FluidResource.of(delegate.getFluidInTank(index));
		}

		@Override
		public long getAmountAsLong(int index) {
			return delegate.getFluidInTank(index).getAmount();
		}

		@Override
		public long getCapacityAsLong(int index, FluidResource resource) {
			return delegate.getTankCapacity(index);
		}

		@Override
		public boolean isValid(int index, FluidResource resource) {
			return delegate.isFluidValid(index, resource.toStack(1));
		}

		@Override
		public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
			if (resource.isEmpty() || amount <= 0) {
				return 0;
			}
			FluidStack stack = resource.toStack(amount);
			int filled = delegate.fill(stack, IFluidHandler.FluidAction.SIMULATE);
			if (filled <= 0) {
				return 0;
			}
			if (transaction.depth() == 0) {
				return delegate.fill(stack, IFluidHandler.FluidAction.EXECUTE);
			}
			return filled;
		}

		@Override
		public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
			if (resource.isEmpty() || amount <= 0) {
				return 0;
			}
			FluidStack requested = resource.toStack(amount);
			FluidStack simulated = delegate.drain(requested, IFluidHandler.FluidAction.SIMULATE);
			if (simulated.isEmpty()) {
				return 0;
			}
			if (transaction.depth() == 0) {
				FluidStack executed = delegate.drain(requested, IFluidHandler.FluidAction.EXECUTE);
				onAfterMutation();
				return executed.getAmount();
			}
			return simulated.getAmount();
		}

		protected void onAfterMutation() {
			// Subclasses (e.g. item handlers) may want to flush the modified container back.
		}
	}

	private static final class FluidHandlerItemToResourceHandler extends FluidHandlerToResourceHandler {
		private final ItemAccess access;

		FluidHandlerItemToResourceHandler(IFluidHandlerItem delegate, ItemAccess access) {
			super(delegate);
			this.access = access;
		}

		@Override
		public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
			int inserted = super.insert(index, resource, amount, transaction);
			if (inserted > 0 && transaction.depth() == 0) {
				flushContainer();
			}
			return inserted;
		}

		@Override
		protected void onAfterMutation() {
			flushContainer();
		}

		private void flushContainer() {
			IFluidHandlerItem item = (IFluidHandlerItem) delegate;
			ItemStack container = item.getContainer();
			ItemResource currentResource = access.getResource();
			ItemResource newResource = ItemResource.of(container);
			if (currentResource.equals(newResource) && access.getAmount() == container.getCount()) {
				return;
			}
			int amount = access.getAmount();
			if (amount <= 0 || currentResource.isEmpty() || newResource.isEmpty()) {
				return;
			}
			try (net.neoforged.neoforge.transfer.transaction.Transaction tx =
					net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
				access.exchange(newResource, Math.min(amount, container.getCount()), tx);
				tx.commit();
			}
		}
	}

	/**
	 * Presents a {@link ResourceHandler} returned from {@link Capabilities.Fluid#ITEM} as a legacy
	 * {@link IFluidHandlerItem} for callers that still consume the old API.
	 */
	private static final class ResourceHandlerToFluidHandlerItem implements IFluidHandlerItem {
		private final ResourceHandler<FluidResource> delegate;
		private final ItemAccess access;
		private final IFluidHandler legacyView;

		ResourceHandlerToFluidHandlerItem(ResourceHandler<FluidResource> delegate, ItemAccess access) {
			this.delegate = delegate;
			this.access = access;
			this.legacyView = IFluidHandler.of(delegate);
		}

		@Override
		public ItemStack getContainer() {
			return access.getResource().toStack(access.getAmount());
		}

		@Override
		public int getTanks() {
			return legacyView.getTanks();
		}

		@Override
		public FluidStack getFluidInTank(int tank) {
			return legacyView.getFluidInTank(tank);
		}

		@Override
		public int getTankCapacity(int tank) {
			return legacyView.getTankCapacity(tank);
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return legacyView.isFluidValid(tank, stack);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			return legacyView.fill(resource, action);
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			return legacyView.drain(resource, action);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			return legacyView.drain(maxDrain, action);
		}
	}
}
