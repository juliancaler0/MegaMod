package net.relics_rpgs.compat;

import io.wispforest.accessories.api.AccessoryItem;
import io.wispforest.accessories.api.SoundEventData;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class RelicAccessoriesItem extends AccessoryItem {
    public RelicAccessoriesItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean canUnequip(ItemStack stack, SlotReference reference) {
        var entity = reference.entity();
        var isOnCooldown = false;
        if (entity instanceof PlayerEntity player) {
            isOnCooldown = !player.isCreative() && player.getItemCooldownManager().isCoolingDown(stack.getItem());
        }
        return super.canUnequip(stack, reference) && !isOnCooldown;
    }

//    @Override
//    public SoundEventData getEquipSound(ItemStack stack, SlotReference reference) {
//        // Return null to use default sound, can be customized later if needed
//        return null;
//    }
}