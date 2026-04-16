package com.ultra.megamod.lib.skilltree.attributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;

import java.util.List;

public interface ConditionalAttributeHolder {
    List<ConditionalAttributeModifier> getConditionalModifiers();

    default void addConditionalModifier(ConditionalAttributeModifier modifier) {
        getConditionalModifiers().add(modifier);
    }

    default void removeConditionalModifier(Identifier id) {
        getConditionalModifiers().removeIf(m -> m.id().equals(id));
    }

    default void reapplyConditionalModifiers(LivingEntity entity) {
        for (var conditional : getConditionalModifiers()) {
            var instance = entity.getAttribute(conditional.attribute());
            if (instance == null) continue;
            boolean shouldApply = conditional.condition().equipment().test(entity);
            boolean isApplied = instance.getModifier(conditional.modifier().id()) != null;
            if (shouldApply && !isApplied) {
                instance.addTransientModifier(conditional.modifier());
            } else if (!shouldApply && isApplied) {
                instance.removeModifier(conditional.modifier().id());
            }
        }
    }
}
