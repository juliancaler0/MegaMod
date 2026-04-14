package net.relics_rpgs.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.relics_rpgs.RelicsMod;

import java.util.function.Supplier;

public class Group {
    public static Identifier ID = Identifier.of(RelicsMod.NAMESPACE, "generic");
    public static String translationKey = "itemGroup." + ID.getNamespace() + "." + ID.getPath();
    public static RegistryKey<ItemGroup> KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), ID);
    public static ItemGroup GROUP;
    public static Supplier<ItemStack> ICON = () -> {
        return new ItemStack(RelicItems.MEDIUM_USE_FIRE_POWER.item().get());
    };
}
