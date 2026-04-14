package net.relics_rpgs.fabric.compat.trinkets;

import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class RelicTrinketItem extends TrinketItem {
    private AttributeModifiersComponent customAttributes = AttributeModifiersComponent.builder().build();

    public RelicTrinketItem(Settings settings, @Nullable AttributeModifiersComponent customAttributes) {
        super(settings);
        if (customAttributes != null) {
            this.customAttributes = customAttributes;
        }
    }

    public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, Identifier slotIdentifier) {
        var modifiers = super.getModifiers(stack, slot, entity, slotIdentifier);
        for (var entry : this.customAttributes.modifiers()) {
            modifiers.put(entry.attribute(),
                    new EntityAttributeModifier(slotIdentifier, entry.modifier().value(), entry.modifier().operation()));
        }
        return modifiers;
    }

    public void setConfigurableModifiers(AttributeModifiersComponent component) {
        this.customAttributes = component;
    }

    @Override
    public boolean canUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        var isOnCooldown = false;
        if (entity instanceof PlayerEntity player) {
            isOnCooldown = !player.isCreative() && player.getItemCooldownManager().isCoolingDown(stack.getItem());
        }
        return super.canUnequip(stack, slot, entity) && !isOnCooldown;
    }

//    @Override
//    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
//        super.onEquip(stack, slot, entity);
//
//        if (entity.getWorld().isClient() // Play sound only on client
//                && entity.age > 100      // Avoid playing sound on entering world / dimension
//        ) {
//            entity.playSound(SoundHelper.JEWELRY_EQUIP, 1.0F, 1.0F);
//        }
//    }
}