package com.ultra.megamod.lib.spellengine.api.config;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ConfigUtil {
    public record Entry(Holder<Attribute> attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier modifier) { }
    public static ItemAttributeModifiers.Builder attributesComponent(Identifier modifierId, List<AttributeModifier> attributesConfig) {
        var componentBuilder = ItemAttributeModifiers.builder();
        var modifiers = modifiersFrom(modifierId, attributesConfig);
        for (var modifier : modifiers) {
            componentBuilder.add(modifier.attribute(), modifier.modifier(), EquipmentSlotGroup.ANY);
        }
        return componentBuilder;
    }

    public static List<Entry> modifiersFrom(Identifier modifierId, List<AttributeModifier> attributesConfig) {
        var modifiers = new ArrayList<Entry>();
        for (var modifier : attributesConfig) {
            var attributeId = Identifier.parse(modifier.attribute);
            var attribute = BuiltInRegistries.ATTRIBUTE.get(attributeId);
            if (attribute.isPresent()) {
                var id = (modifier.id != null && !modifier.id.isEmpty())
                        ? Identifier.parse(modifier.id)
                        : modifierId;
                modifiers.add(new Entry(
                        attribute.get(),
                        new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                id,
                                modifier.value,
                                modifier.operation
                        )
                ));
            } else {
                System.err.println("Failed to resolve Attribute with id: " + modifier.attribute);
            }
        }
        return modifiers;
    }
}
