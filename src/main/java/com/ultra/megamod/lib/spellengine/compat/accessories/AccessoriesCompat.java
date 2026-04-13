package com.ultra.megamod.lib.spellengine.compat.accessories;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainerHelper;
import com.ultra.megamod.lib.spellengine.compat.container.ContainerCompat;
import com.ultra.megamod.lib.spellengine.internals.container.SpellContainerSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Accessories compatibility layer.
 * The relic/accessories API package is not yet present in this build.
 * This stub allows compilation while the accessories system is pending integration.
 */
public class AccessoriesCompat {
    private static final String MOD_ID = "accessories";
    private static boolean intialized = false;
    private static boolean enabled = false;

    public static boolean init() {
        if (intialized) {
            return enabled;
        }
        intialized = true;
        enabled = net.neoforged.fml.ModList.get().isLoaded(MOD_ID);
        if (!enabled) {
            return enabled;
        }

        ContainerCompat.addProvider(AccessoriesCompat::getAll);

        final var spellSourceName = "accessories";
        SpellContainerSource.addItemSource(
                SpellContainerSource.LootItem.of(
                        spellSourceName,
                        (player, name) -> getEquippedStacks(player)
                ),
                SpellContainerSource.MAIN_HAND.name()
        );

        return enabled;
    }

    private static List<ItemStack> getAll(Player player) {
        // Accessories API not available yet
        return List.of();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static List<SpellContainerSource.SourcedContainer> getSpellContainers(Player player, String sourceName) {
        return List.of();
    }

    public static List<ItemStack> getEquippedStacks(Player player) {
        return List.of();
    }

    public static ItemStack getSpellBookStack(Player player) {
        return ItemStack.EMPTY;
    }
}
