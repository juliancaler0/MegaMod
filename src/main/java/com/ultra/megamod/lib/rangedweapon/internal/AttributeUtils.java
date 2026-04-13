package com.ultra.megamod.lib.rangedweapon.internal;

import com.ultra.megamod.lib.rangedweapon.api.AttributeModifierIDs;
import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import com.ultra.megamod.lib.rangedweapon.api.RangedConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.List;

public class AttributeUtils {
    public static ItemAttributeModifiers mergeComponents(ItemAttributeModifiers target, ItemAttributeModifiers source) {
        if (source == null && target == null) {
            return null;
        } else if (source == null) {
            return target;
        } else if (target == null) {
            return source;
        }
        var builder = ItemAttributeModifiers.builder();
        for (var entry: source.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        for (var entry: target.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        return builder.build();
    }

    // Matching the interface of `add(...)` in ItemAttributeModifiers.Builder
    public record ComponentEntry(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) { }

    public static ItemAttributeModifiers fromRangedConfig(RangedConfig config) {
        var slot = EquipmentSlotGroup.HAND;

        var damage = new AttributeModifier(
                AttributeModifierIDs.WEAPON_DAMAGE_ID,
                config.damage(),
                AttributeModifier.Operation.ADD_VALUE);
        var pullTime = new AttributeModifier(
                AttributeModifierIDs.WEAPON_PULL_TIME_ID,
                config.pull_time_bonus(),
                AttributeModifier.Operation.ADD_VALUE);
        var builder = ItemAttributeModifiers.builder()
                .add(EntityAttributes_RangedWeapon.DAMAGE.entry, damage, slot)
                .add(EntityAttributes_RangedWeapon.PULL_TIME.entry, pullTime, slot);
        if (config.velocity_bonus() > 0) {
            var velocity = new AttributeModifier(
                    AttributeModifierIDs.WEAPON_VELOCITY_ID,
                    config.velocity_bonus(),
                    AttributeModifier.Operation.ADD_VALUE);
            builder.add(EntityAttributes_RangedWeapon.VELOCITY.entry, velocity, slot);
        }

        for (var entry: componentEntriesFrom(config.attributes(), slot)) {
            builder.add(entry.attribute, entry.modifier, entry.slot);
        }

        return builder.build();
    }


    public static List<ComponentEntry> componentEntriesFrom(List<RangedConfig.Attribute> attributes, EquipmentSlotGroup slot) {
        var list = new ArrayList<ComponentEntry>();
        if (attributes == null || attributes.isEmpty()) {
            return list;
        }
        for (var attr: attributes) {
            Identifier entityAttributeId;
            try {
                entityAttributeId = Identifier.tryParse(attr.attributeId());
            } catch (Exception e) {
                continue;
            }
            var entityAttribute = BuiltInRegistries.ATTRIBUTE.get(entityAttributeId);
            if (entityAttribute.isEmpty() || attr.modifier() == null) { continue; }
            Identifier modifierId;
            try {
                modifierId = Identifier.tryParse(attr.modifier().modifierId());
            } catch (Exception e) {
                continue;
            }
            list.add(new ComponentEntry(
                    entityAttribute.get(),
                    new AttributeModifier(
                            modifierId,
                            attr.modifier().value(),
                            attr.modifier().operation()
                    ),
                    slot
            ));
        }
        return list;
    }
}
