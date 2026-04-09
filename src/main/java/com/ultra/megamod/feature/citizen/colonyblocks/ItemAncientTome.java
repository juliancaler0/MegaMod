package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.raid.ColonyRaidManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Ancient Tome — dropped by raiders during raids.
 * Glows (enchantment foil) when a raid is scheduled for tonight.
 * Used by Enchanter to create enchanted books.
 * Cannot be right-clicked (no use action).
 */
public class ItemAncientTome extends Item {

    public ItemAncientTome(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Glow when raid is coming tonight — checked client-side via a flag
        // In practice, the foil check happens on render thread so we use a static flag
        return raidScheduledTonight;
    }

    // Static flag updated from server tick — set by ColonyRaidManager when a raid is scheduled
    private static volatile boolean raidScheduledTonight = false;

    public static void setRaidScheduledTonight(boolean scheduled) {
        raidScheduledTonight = scheduled;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        // Update raid flag every 100 ticks for all players holding ancient tomes
        if (level.getGameTime() % 100 == 0) {
            raidScheduledTonight = ColonyRaidManager.isRaidScheduledTonight();
        }
    }
}
