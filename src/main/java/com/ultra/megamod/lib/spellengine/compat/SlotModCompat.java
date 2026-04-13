package com.ultra.megamod.lib.spellengine.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class SlotModCompat {
    @Nullable
    public static Function<Player, ItemStack> spellBookResolver = (player) -> ItemStack.EMPTY;
    public static ItemStack getEquippedSpellBook(Player player) {
        return spellBookResolver.apply(player);
    }

    public record SpellScrollArs(Item.Properties settings) { }
    @Nullable public static Function<SpellScrollArs, Item> spellScrollFactory = null;
    public static void setSpellScrollFactory(Function<SpellScrollArs, Item> factory) {
        if (spellScrollFactory != null) { return; }
        spellScrollFactory = factory;
    }

    public record SpellBookArgs(Item.Properties settings) { }
    @Nullable public static Function<SpellBookArgs, Item> spellBookFactory = null;
    public static void setSpellBookFactory(Function<SpellBookArgs, Item> factory) {
        if (spellBookFactory != null) { return; }
        spellBookFactory = factory;
    }
}
