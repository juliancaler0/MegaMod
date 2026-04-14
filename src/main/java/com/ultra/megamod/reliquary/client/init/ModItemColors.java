package com.ultra.megamod.reliquary.client.init;

/**
 * Placeholder — in 1.21.11 {@code RegisterColorHandlersEvent.Item} was
 * removed. Item tint overlays are now sourced exclusively from
 * {@code ItemTintSource} entries baked into the item's data-driven JSON
 * model (see {@code RegisterColorHandlersEvent.ItemTintSources}).
 *
 * <p>The ref source wired five categories of tint logic here:
 * <ul>
 *   <li>Void tear → mirrors the contained item's color while Shift is held.
 *       In the new API this needs a custom {@code ItemTintSource} MapCodec
 *       registered via {@code RegisterColorHandlersEvent.ItemTintSources}
 *       plus a JSON entry in {@code assets/reliquary/items/void_tear.json}.</li>
 *   <li>Potion essence / potion / splash potion / lingering potion / tipped
 *       arrow → can use the vanilla {@code minecraft:potion} ItemTintSource
 *       directly in the item JSON.</li>
 *   <li>Bullets & magazines → need a custom BulletColor tint source keying
 *       off the bullet's per-variant color plus a potion-contents overlay.</li>
 *   <li>Mob charm & mob charm fragment → need a custom SpawnEggColor tint
 *       source that resolves the stored entity registry name to its spawn
 *       egg's two-layer palette.</li>
 * </ul>
 *
 * <p>These custom ItemTintSource ports are follow-up work; disabling the
 * Java-side color handler leaves the items rendering in their base texture
 * colors, which is strictly a visual regression.
 */
public final class ModItemColors {
	private ModItemColors() {
	}

	// No registerItemColors() method here anymore — the old API
	// (RegisterColorHandlersEvent.Item) was removed in 1.21.11.
}
