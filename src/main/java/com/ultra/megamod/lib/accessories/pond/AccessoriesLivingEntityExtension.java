package com.ultra.megamod.lib.accessories.pond;

import com.ultra.megamod.lib.accessories.api.AccessoriesCapability;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface AccessoriesLivingEntityExtension {
    AccessoriesCapability getOrCreateAccessoriesCapability();

    void onEquipItem(SlotReference slotReference, ItemStack oldItem, ItemStack newItem);

    void pushEnchantmentContext(ItemStack stack, SlotReference reference);

    @Nullable
    SlotReference popEnchantmentContext(ItemStack stack);

    Map<Enchantment, Set<EnchantmentLocationBasedEffect>> activeLocationDependentEnchantmentsFromSlotReference(SlotReference slotReference);
}
