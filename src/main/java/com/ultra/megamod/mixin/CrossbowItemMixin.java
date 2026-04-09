package com.ultra.megamod.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows the Infinity enchantment to work on crossbows.
 * When a crossbow with Infinity loads a projectile, the consumed arrow is restored.
 *
 * Hook into releaseUsing — right after the crossbow finishes loading (which consumes the arrow),
 * we check if Infinity is on the weapon and if so, restore the arrow that was consumed.
 */
@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    /**
     * After the crossbow releases (finishes loading), check if it has Infinity.
     * If so, scan the player's inventory for the arrow stack that was decremented
     * and restore one arrow. The crossbow already stored the projectile in its
     * ChargedProjectiles component, so we just need to undo the consumption.
     */
    @Inject(method = "releaseUsing", at = @At("TAIL"))
    private void megamod$restoreArrowIfInfinity(ItemStack stack, net.minecraft.world.level.Level level,
                                                 LivingEntity entity, int timeLeft, CallbackInfoReturnable<Boolean> cir) {
        if (level.isClientSide()) return;
        if (!(entity instanceof Player player)) return;

        // Check if the crossbow has the Infinity enchantment
        int infinityLevel = megamod$getInfinityLevel(stack, entity);
        if (infinityLevel <= 0) return;

        // The crossbow just loaded — check if it actually has charged projectiles now
        var charged = stack.get(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES);
        if (charged == null || charged.isEmpty()) return;

        // Get the first projectile that was loaded
        java.util.List<ItemStack> projectiles = charged.getItems();
        if (projectiles.isEmpty()) return;

        ItemStack loadedProjectile = projectiles.getFirst();
        if (loadedProjectile.isEmpty()) return;

        // Don't restore creative-mode arrow (tipped/spectral/etc.) — only standard arrows
        // Actually, restore any arrow type for consistency
        // Give back one of whatever arrow was consumed
        ItemStack restored = loadedProjectile.copy();
        restored.setCount(1);

        // Try to find the arrow stack in inventory that was decremented and add back
        if (!player.getInventory().add(restored)) {
            // If inventory is full, drop it
            player.drop(restored, false);
        }
    }

    @Unique
    private static int megamod$getInfinityLevel(ItemStack stack, LivingEntity entity) {
        // In 1.21.x, enchantment levels are retrieved via the enchantment helper
        // which checks the item's enchantment component
        try {
            var registryAccess = entity.registryAccess();
            var infinityHolder = registryAccess.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.INFINITY);
            return EnchantmentHelper.getItemEnchantmentLevel(infinityHolder, stack);
        } catch (Exception e) {
            return 0;
        }
    }
}
