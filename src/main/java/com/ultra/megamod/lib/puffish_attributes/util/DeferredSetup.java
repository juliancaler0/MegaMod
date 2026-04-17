package com.ultra.megamod.lib.puffish_attributes.util;

import com.ultra.megamod.lib.puffish_attributes.api.DynamicEntityAttribute;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class DeferredSetup {
	public static final String MOD_ID = "puffish_attributes";

	public static Identifier createIdentifier(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static Attribute createClampedAttribute(Identifier id, double fallback, double min, double max) {
		return new RangedAttribute(
				id.toLanguageKey("attribute"),
				fallback,
				min,
				max
		);
	}

	public static Attribute createDynamicAttribute(Identifier id) {
		return DynamicEntityAttribute.create(id);
	}

	public static Holder<Attribute> registerAttribute(Identifier id, Attribute attribute) {
		return Platform.INSTANCE.registerReference(BuiltInRegistries.ATTRIBUTE, id, attribute);
	}
}
