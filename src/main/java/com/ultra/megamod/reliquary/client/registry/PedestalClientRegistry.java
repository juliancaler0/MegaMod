package com.ultra.megamod.reliquary.client.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.ultra.megamod.reliquary.api.client.IPedestalItemRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Registry of extra-renderers that draw once-per-pedestal on top of the
 * rotating item. Typed by the pedestal item's Java class — a class lookup
 * makes it cheap to match custom rod subclasses (see {@link
 * com.ultra.megamod.reliquary.client.render.PedestalFishHookRenderer}).
 */
public class PedestalClientRegistry {
	private static final PedestalClientRegistry INSTANCE = new PedestalClientRegistry();

	private final Map<Class<? extends Item>, Supplier<IPedestalItemRenderer>> itemRenderers = new HashMap<>();

	private PedestalClientRegistry() {
	}

	public static void registerItemRenderer(Class<? extends Item> itemClass, Supplier<IPedestalItemRenderer> rendererFactory) {
		INSTANCE.itemRenderers.put(itemClass, rendererFactory);
	}

	public static Optional<IPedestalItemRenderer> getItemRenderer(ItemStack item) {
		for (Class<? extends Item> itemClass : INSTANCE.itemRenderers.keySet()) {
			if (itemClass.isInstance(item.getItem())) {
				return Optional.of(INSTANCE.itemRenderers.get(itemClass).get());
			}
		}
		return Optional.empty();
	}
}
