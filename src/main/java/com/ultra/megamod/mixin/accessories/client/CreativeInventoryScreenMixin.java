package com.ultra.megamod.mixin.accessories.client;

import com.ultra.megamod.lib.accessories.client.gui.components.ComponentUtils;
import com.ultra.megamod.lib.accessories.networking.AccessoriesNetworking;
import com.ultra.megamod.lib.accessories.networking.server.NukeAccessories;
import com.ultra.megamod.lib.accessories.fabric.event.Event;
import com.ultra.megamod.lib.accessories.fabric.event.EventFactory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> implements ComponentUtils.CreativeScreenExtension {

    @Accessor("selectedTab")
    private static CreativeModeTab selectedTab() {
        throw new IllegalStateException("How");
    }

    public CreativeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Unique private int nukeCoolDown = 0;

    @Inject(method = "containerTick", at = @At("HEAD"))
    private void nukeCooldown(CallbackInfo ci){
        if(this.nukeCoolDown > 0) this.nukeCoolDown--;
    }

    @Inject(method = "selectTab", at = @At(value = "TAIL"))
    private void onCreativeTagChange(CreativeModeTab tab, CallbackInfo ci){
        getEvent().invoker().onTabChange(tab);
    }

    @Inject(method = "slotClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;size()I", ordinal = 0, shift = At.Shift.BEFORE))
    private void clearAccessoriesWithClearSlot(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        if(this.nukeCoolDown <= 0) {
            AccessoriesNetworking.sendToServer(new NukeAccessories());

            this.nukeCoolDown = 10;
        }
    }

    //--

    @Unique
    private final Event<ComponentUtils.OnCreativeTabChange> onTabChangeEvent = EventFactory.createArrayBacked(ComponentUtils.OnCreativeTabChange.class, invokers -> (tab) -> {
        for (var invoker : invokers) invoker.onTabChange(tab);
    });

    @Override
    public Event<ComponentUtils.OnCreativeTabChange> getEvent() {
        return this.onTabChangeEvent;
    }

    @Override
    public CreativeModeTab getTab() {
        return selectedTab();
    }
}
