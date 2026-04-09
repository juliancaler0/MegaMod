package com.ultra.megamod.feature.citizen.enchantment;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.raid.AbstractRaiderEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Registry and event handler for colony enchantments.
 * <p>
 * In 1.21.x enchantments are data-driven; the JSON definition should be placed at:
 * {@code data/megamod/enchantment/raider_damage.json}
 * <p>
 * This class handles:
 * <ul>
 *   <li>Initialization (placeholder for future DeferredRegister needs)</li>
 *   <li>A {@link LivingDamageEvent.Pre} handler that applies raider damage bonus</li>
 * </ul>
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class ColonyEnchantmentRegistry {

    /**
     * Initialize colony enchantments.
     * Call from the main mod constructor.
     */
    public static void init(IEventBus modEventBus) {
        // In 1.21.x, enchantments are fully data-driven (JSON).
        // No DeferredRegister is needed for the enchantment itself.
        // The JSON definition at data/megamod/enchantment/raider_damage.json
        // handles registration. This init method is a hook for any future
        // mod-bus listeners or codec registrations.
        MegaMod.LOGGER.debug("Colony enchantments initialized");
    }

    /**
     * Applies bonus damage from the Raider Damage enchantment when
     * a player attacks an AbstractRaiderEntity.
     */
    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;

        // Only apply to raider entities
        if (!(target instanceof AbstractRaiderEntity)) return;

        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof Player player)) return;

        // Get the weapon in main hand
        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return;

        // Check enchantment level
        RegistryAccess registryAccess = player.level().registryAccess();
        int level = RaiderDamageEnchantment.getLevel(weapon, registryAccess);
        if (level <= 0) return;

        // Apply bonus damage
        float bonus = RaiderDamageEnchantment.getBonusDamage(level);
        float newDamage = event.getNewDamage() + bonus;
        event.setNewDamage(newDamage);
    }
}
