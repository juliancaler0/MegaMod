package com.ultra.megamod.mixin.archers.screen;

import com.ultra.megamod.feature.combat.archers.item.misc.AutoFireHook;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class ArchersAnvilMenuMixin extends ItemCombinerMenu {
    @Shadow @Final private DataSlot cost;

    public ArchersAnvilMenuMixin(@Nullable MenuType<?> type, int syncId, Inventory playerInventory, ContainerLevelAccess context, ItemCombinerMenuSlotDefinition slotDefinition) {
        super(type, syncId, playerInventory, context, slotDefinition);
    }

    // Apply AutoFireHook in anvil

    @Inject(method = "createResult", at = @At(value = "RETURN"))
    private void createResult_Inject(CallbackInfo ci) {
        var input = this.inputSlots;
        ItemStack input1 = input.getItem(0);
        ItemStack input2 = input.getItem(1);
        if (input1.isEmpty() || input2.isEmpty()) {
            return;
        }

        ItemStack afh = null;
        ItemStack crossbow = null;
        if (input1.is(AutoFireHook.item()) && AutoFireHook.isApplicable(input2)) {
            afh = input1;
            crossbow = input2;
        } else if (AutoFireHook.isApplicable(input1) && input2.is(AutoFireHook.item())) {
            afh = input2;
            crossbow = input1;
        }
        if (afh == null || crossbow == null || !AutoFireHook.isApplicable(crossbow)) {
            return;
        }

        var afhCrossbow = crossbow.copy();
        AutoFireHook.apply(afhCrossbow);

        this.resultSlots.setItem(0, afhCrossbow);
        cost.set(1);
        broadcastChanges();
    }
}
