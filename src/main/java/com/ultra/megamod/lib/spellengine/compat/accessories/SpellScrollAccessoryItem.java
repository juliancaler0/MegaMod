package com.ultra.megamod.lib.spellengine.compat.accessories;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;

import java.util.List;
import java.util.function.Supplier;

public class SpellScrollAccessoryItem extends SpellHostAccessoryItem {
    public SpellScrollAccessoryItem(Item.Properties settings, Supplier<Holder<SoundEvent>> equipSound) {
        super(settings, equipSound);
    }

    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        if (SpellEngineClient.config.showSpellBindingTooltip) {
            tooltip.add(Component
                    .translatable("item.spell_engine.scroll.table_hint")
                    .withStyle(ChatFormatting.GRAY)
            );
        }
    }
}
