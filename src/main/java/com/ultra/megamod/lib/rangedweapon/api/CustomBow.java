package com.ultra.megamod.lib.rangedweapon.api;

import com.ultra.megamod.lib.rangedweapon.internal.RangedItemSettings;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashSet;
import java.util.function.Supplier;

public class CustomBow extends BowItem {
    // Instances are kept a list of, so model predicates can be automatically registered
    public final static HashSet<CustomBow> instances = new HashSet<>();
    public CustomBow(Properties settings, RangedConfig config, Supplier<Ingredient> repairIngredientSupplier) {
        super(
                ((RangedItemSettings)settings).rangedAttributes(config)
        );
        this.repairIngredientSupplier = repairIngredientSupplier;
        instances.add(this);
    }

    private final Supplier<Ingredient> repairIngredientSupplier;

    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return this.repairIngredientSupplier.get().test(ingredient);
    }

    public Supplier<Ingredient> getRepairIngredientSupplier() {
        return repairIngredientSupplier;
    }
}
