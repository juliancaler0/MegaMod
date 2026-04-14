package com.ultra.megamod.reliquary.client.init;

/**
 * Placeholder — the Reliquary 1.21.x ref source hooked
 * {@code ModelEvent.ModifyBakingResult} to wrap the void-tear's BakedModel
 * with {@link com.ultra.megamod.reliquary.client.model.VoidTearModel}. Both
 * {@code ModelEvent.ModifyBakingResult} and {@code BakedModel} were removed
 * from the public client API in 1.21.4+: multi-state item visuals are now
 * driven exclusively by data-driven Select Models in
 * {@code assets/reliquary/items/<name>.json}.
 *
 * <p>See:
 * <ul>
 *   <li>{@code src/main/resources/assets/reliquary/items/void_tear.json}
 *       — swaps to the stored item model while Shift is held.</li>
 *   <li>{@code src/main/resources/assets/reliquary/items/handgun.json}
 *       — swaps loaded/empty based on the handgun's magazine state.</li>
 *   <li>{@code src/main/resources/assets/reliquary/items/rod_of_lyssa.json}
 *       — toggles the cast frame while a hook entity exists.</li>
 *   <li>{@code src/main/resources/assets/reliquary/items/infernal_tear.json}
 *       — empty/filled swap driven by the contained item component.</li>
 * </ul>
 * If more items need multi-state rendering, drop the JSON in the same folder;
 * no Java registration call is needed in 1.21.11.
 */
public final class ItemModels {
	private ItemModels() {}
}
