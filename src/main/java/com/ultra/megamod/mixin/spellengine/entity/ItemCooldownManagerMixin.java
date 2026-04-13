package com.ultra.megamod.mixin.spellengine.entity;

import com.google.common.collect.Maps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.Item;
import com.ultra.megamod.lib.spellengine.utils.ItemCooldownManagerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ItemCooldowns.class)
public class ItemCooldownManagerMixin implements ItemCooldownManagerExtension {

    /**
     * The goal of this mixin is to provide a way to get the last cooldown duration of an item.
     * In 1.21.11, ItemCooldowns uses Identifier-based cooldown groups instead of Item keys.
     * We track durations by cooldown group Identifier.
     */

    public int SE_getLastCooldownDuration(Item item) {
        // In 1.21.11, get the cooldown group for this item's default stack
        var self = (ItemCooldowns) (Object) this;
        var group = self.getCooldownGroup(item.getDefaultInstance());
        return durations.getOrDefault(group, 0);
    }

    @Unique
    private final Map<Identifier, Integer> durations = Maps.newHashMap();

    @Inject(method = "addCooldown(Lnet/minecraft/resources/Identifier;I)V", at = @At("HEAD"))
    private void addCooldown_HEAD(Identifier group, int duration, CallbackInfo ci) {
        durations.put(group, duration);
    }

    @Inject(method = "removeCooldown", at = @At("RETURN"))
    private void removeCooldown_RETURN(Identifier group, CallbackInfo ci) {
        durations.remove(group);
    }

}
