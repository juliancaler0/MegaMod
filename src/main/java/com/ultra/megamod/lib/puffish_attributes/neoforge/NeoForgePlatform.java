package com.ultra.megamod.lib.puffish_attributes.neoforge;

import com.ultra.megamod.lib.puffish_attributes.util.Platform;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgePlatform implements Platform {

	@Override
	public <T> Holder<T> registerReference(Registry<T> registry, Identifier id, T entry) {
		var deferredRegister = DeferredRegister.create(registry.key(), id.getNamespace());
		AttributesForge.DEFERRED_REGISTERS.add(deferredRegister);
		return deferredRegister.register(id.getPath(), () -> entry);
	}

}
