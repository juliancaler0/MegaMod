package com.ultra.megamod.reliquary.client.model;

/**
 * Placeholder — the Reliquary 1.21.x reference implementation wrapped a
 * {@code BakedModel} to make the void-tear icon swap to the stored item's
 * icon while Shift was held. In 1.21.4+ the whole BakedModel + ItemOverrides
 * pipeline was removed: multi-state item visuals are now driven exclusively
 * by data-driven Select Models in {@code assets/<ns>/items/<name>.json}.
 *
 * <p>The replacement JSON lives at
 * {@code src/main/resources/assets/reliquary/items/void_tear.json} and keys
 * off the {@code minecraft:custom_data} component flag that
 * {@link com.ultra.megamod.reliquary.item.VoidTearItem} stamps onto the stack.
 * Keeping this class as an empty marker preserves the package layout from the
 * ref source; there is no longer any code-side model wrapping to do.
 */
public final class VoidTearModel {
	private VoidTearModel() {}
}
