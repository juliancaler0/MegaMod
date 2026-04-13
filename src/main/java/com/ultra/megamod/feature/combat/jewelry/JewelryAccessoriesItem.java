package com.ultra.megamod.feature.combat.jewelry;

import com.ultra.megamod.lib.accessories.api.SoundEventData;
import com.ultra.megamod.lib.accessories.api.core.AccessoryItem;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.ChatFormatting;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Accessory-compatible jewelry item, ported 1:1 from the Jewelry mod reference.
 * Extends AccessoryItem to gain full slot compatibility with the Accessories system.
 * Displays optional lore text and plays a custom equip sound.
 */
public class JewelryAccessoriesItem extends AccessoryItem {
    private final Supplier<Holder<SoundEvent>> equipSound;
    private final String lore;

    public JewelryAccessoriesItem(Properties properties, String lore, Supplier<Holder<SoundEvent>> equipSound) {
        super(properties);
        this.lore = lore;
        this.equipSound = equipSound;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, display, tooltip, type);
        if (lore != null && !lore.isEmpty()) {
            tooltip.accept(Component.translatable(lore).withStyle(ChatFormatting.ITALIC, ChatFormatting.GOLD));
        }
    }

    public SoundEventData getEquipSound(ItemStack stack, SlotReference reference) {
        var entry = this.equipSound.get();
        if (entry != null) {
            return new SoundEventData(entry, 1.0F, 1.0F);
        } else {
            return null;
        }
    }
}
