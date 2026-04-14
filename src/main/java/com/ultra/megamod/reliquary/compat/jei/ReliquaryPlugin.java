package com.ultra.megamod.reliquary.compat.jei;

import com.ultra.megamod.reliquary.Reliquary;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.minecraft.resources.Identifier;

/**
 * Minimal {@code @JeiPlugin} for Reliquary. Currently registers no categories
 * or recipes — this is a compile-clean shell that satisfies JEI's plugin
 * discovery when both Reliquary and JEI are present. The rich category /
 * recipe-maker ports (alkahestry, cauldron, mortar, infernal tear, magazines,
 * lingering arrows, mob charm belt) are deferred; see {@code package-info.java}.
 */
@SuppressWarnings("unused") // plugin class is used by JEI reflection
@JeiPlugin
public class ReliquaryPlugin implements IModPlugin {

	@Override
	public Identifier getPluginUid() {
		return Reliquary.getRL("default");
	}
}
