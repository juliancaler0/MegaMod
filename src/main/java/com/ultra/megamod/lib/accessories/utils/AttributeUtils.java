package com.ultra.megamod.lib.accessories.utils;

import com.mojang.logging.LogUtils;
import com.ultra.megamod.lib.accessories.api.attributes.AccessoryAttributeBuilder;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import com.ultra.megamod.lib.accessories.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.slf4j.Logger;

public class AttributeUtils {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void addTransientAttributeModifiers(LivingEntity livingEntity, AccessoryAttributeBuilder attributes) {
        if(attributes.isEmpty()) return;

        var attributeMap = livingEntity.getAttributes();
        var capability = ((com.ultra.megamod.lib.accessories.pond.AccessoriesAPIAccess) livingEntity).accessoriesCapability();

        var containers = capability.getContainers();

        attributes.getSlotModifiers().asMap().forEach((s, modifiers) -> {
            var container = containers.get(s);

            if(container == null) return;

            modifiers.stream()
                    .filter(modifier -> !container.hasModifier(modifier.id()))
                    .forEach(container::addTransientModifier);
        });

        attributes.getAttributeModifiers(true).asMap().forEach((holder, modifiers) -> {
            var instance = attributeMap.getInstance(holder);

            if(instance == null) return;

            modifiers.stream()
                    .filter(modifier -> !instance.hasModifier(modifier.id()))
                    .forEach(instance::addTransientModifier);
        });
    }

    public static void removeTransientAttributeModifiers(LivingEntity livingEntity, AccessoryAttributeBuilder attributes) {
        if(attributes.isEmpty()) return;

        var attributeMap = livingEntity.getAttributes();
        var capability = ((com.ultra.megamod.lib.accessories.pond.AccessoriesAPIAccess) livingEntity).accessoriesCapability();

        var containers = capability.getContainers();

        attributes.getSlotModifiers().asMap().forEach((s, modifiers) -> {
            var container = containers.get(s);

            if(container == null) return;

            modifiers.stream()
                    .map(AttributeModifier::id)
                    .forEach(container::removeModifier);
        });

        attributes.getAttributeModifiers(true).asMap().forEach((holder, modifiers) -> {
            var instance = attributeMap.getInstance(holder);

            if(instance == null) return;

            modifiers.stream()
                    .map(AttributeModifier::id)
                    .forEach(instance::removeModifier);
        });
    }

    public static final StructEndec<AttributeModifier> ATTRIBUTE_MODIFIER_ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.IDENTIFIER.fieldOf("id", AttributeModifier::id),
            Endec.DOUBLE.fieldOf("amount", AttributeModifier::amount),
            EndecUtils.forEnumStringRepresentable(AttributeModifier.Operation.class).fieldOf("operation", AttributeModifier::operation),
            AttributeModifier::new
    );
}
