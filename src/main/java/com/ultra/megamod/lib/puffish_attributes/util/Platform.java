package com.ultra.megamod.lib.puffish_attributes.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.ServiceLoader;

public interface Platform {
	Platform INSTANCE = ServiceLoader.load(Platform.class).findFirst().orElseThrow();

	<T> Holder<T> registerReference(Registry<T> registry, Identifier id, T entry);
}
