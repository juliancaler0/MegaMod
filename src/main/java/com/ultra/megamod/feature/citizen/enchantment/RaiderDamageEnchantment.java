package com.ultra.megamod.feature.citizen.enchantment;

import com.ultra.megamod.feature.citizen.raid.AbstractRaiderEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Optional;

/**
 * Custom enchantment that deals bonus damage to raider entities.
 * <ul>
 *   <li>Checks if target is {@link AbstractRaiderEntity}</li>
 *   <li>Adds +2.5 damage per level (max level 5)</li>
 *   <li>Compatible with swords</li>
 * </ul>
 *
 * In 1.21.x, enchantments are data-driven (JSON). This class provides
 * the resource key for the enchantment and utility methods for checking
 * and computing the bonus damage.
 *
 * The actual enchantment definition should be placed at:
 * {@code data/megamod/enchantment/raider_damage.json}
 */
public class RaiderDamageEnchantment {

    /** Resource key for the raider damage enchantment */
    public static final ResourceKey<Enchantment> RAIDER_DAMAGE_KEY = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("megamod", "raider_damage")
    );

    /** Damage bonus per enchantment level */
    public static final float DAMAGE_PER_LEVEL = 2.5f;

    /** Maximum enchantment level */
    public static final int MAX_LEVEL = 5;

    /**
     * Get the level of the raider damage enchantment on the given item.
     * Returns 0 if not present or if the enchantment is not registered.
     */
    public static int getLevel(ItemStack stack, RegistryAccess registryAccess) {
        try {
            Registry<Enchantment> enchRegistry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
            Optional<Holder.Reference<Enchantment>> holder = enchRegistry.get(RAIDER_DAMAGE_KEY);
            if (holder.isPresent()) {
                return stack.getEnchantments().getLevel(holder.get());
            }
        } catch (Exception ignored) {}
        return 0;
    }

    /**
     * Calculate bonus damage for the given enchantment level.
     */
    public static float getBonusDamage(int level) {
        if (level <= 0) return 0.0f;
        return Math.min(level, MAX_LEVEL) * DAMAGE_PER_LEVEL;
    }

    /**
     * Check if the target entity is an AbstractRaiderEntity (eligible for bonus damage).
     */
    public static boolean isRaider(LivingEntity target) {
        return target instanceof AbstractRaiderEntity;
    }
}
