package com.ultra.megamod.lib.skilltree.attributes;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

public record ConditionalAttributeModifier(
        Identifier id,
        Holder<Attribute> attribute,
        AttributeModifier modifier,
        ModifierCondition condition
) {}
