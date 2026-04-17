package com.ultra.megamod.lib.puffish_attributes.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class DynamicEntityAttribute extends Attribute {

	public static DynamicEntityAttribute create(Identifier id) {
		return new DynamicEntityAttribute(
				id.toLanguageKey("attribute")
		);
	}

	public DynamicEntityAttribute(String translationKey) {
		super(translationKey, Double.NaN);
	}

}
