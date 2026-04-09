package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Handles the inventory of the MC citizen.
 * Ported from MineColonies' CitizenInventoryHandler.
 */
public class CitizenInventoryHandler implements ICitizenInventoryHandler {

    private final MCEntityCitizen citizen;

    public CitizenInventoryHandler(MCEntityCitizen citizen) {
        this.citizen = citizen;
    }

    @Override
    public int findFirstSlotInInventoryWith(Item targetItem) {
        SimpleContainer inv = citizen.getCitizenInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(targetItem)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int findFirstSlotInInventoryWith(Block block) {
        SimpleContainer inv = citizen.getCitizenInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() == block) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCountInInventory(Block block) {
        SimpleContainer inv = citizen.getCitizenInventory();
        int count = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() == block) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public int getItemCountInInventory(Item targetItem) {
        SimpleContainer inv = citizen.getCitizenInventory();
        int count = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(targetItem)) {
                count += inv.getItem(i).getCount();
            }
        }
        return count;
    }

    @Override
    public boolean hasItemInInventory(Block block) {
        return findFirstSlotInInventoryWith(block) >= 0;
    }

    @Override
    public boolean hasItemInInventory(Item item) {
        return findFirstSlotInInventoryWith(item) >= 0;
    }

    @Override
    public boolean isInventoryFull() {
        SimpleContainer inv = citizen.getCitizenInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }
}
