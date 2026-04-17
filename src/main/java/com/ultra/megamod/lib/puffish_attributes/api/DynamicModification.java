package com.ultra.megamod.lib.puffish_attributes.api;

import com.ultra.megamod.lib.puffish_attributes.util.DynamicModificationImpl;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

public interface DynamicModification {
	static DynamicModification create() {
		return new DynamicModificationImpl();
	}

	DynamicModification withPositive(Holder<Attribute> attribute, LivingEntity entity);
	DynamicModification withNegative(Holder<Attribute> attribute, LivingEntity entity);

	double applyTo(double value);
	float applyTo(float value);

	double applyToReciprocal(double value);
	float applyToReciprocal(float value);

	double relativeTo(double value);
	float relativeTo(float value);
}
