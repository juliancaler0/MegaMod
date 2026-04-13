package com.ultra.megamod.lib.rangedweapon.api;

import com.ultra.megamod.lib.rangedweapon.internal.RangedItemSettings;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashSet;
import java.util.function.Supplier;

public class CustomCrossbow extends CrossbowItem {
    // Instances are kept a list of, so model predicates can be automatically registered
    public final static HashSet<CustomCrossbow> instances = new HashSet<>();

    public CustomCrossbow(Properties settings, RangedConfig config, Supplier<Ingredient> repairIngredientSupplier) {
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
