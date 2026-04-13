package com.ultra.megamod.mixin.spellengine.arrow;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import com.ultra.megamod.lib.spellengine.internals.SpellTriggers;
import com.ultra.megamod.lib.spellengine.internals.arrow.ArrowExtension;
import com.ultra.megamod.lib.spellengine.internals.arrow.ArrowHelper;
import com.ultra.megamod.lib.spellengine.internals.arrow.ArrowShootContext;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Mixin(ProjectileWeaponItem.class)
public class RangedWeaponItemMixin {

    @WrapOperation(method = "shoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;createProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/projectile/Projectile;"))
    private Projectile shoot_wrap_createProjectile(
            ProjectileWeaponItem instance, Level world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical,
            Operation<Projectile> original) {
        var projectile = original.call(instance, world, shooter, weaponStack, projectileStack, critical);
        if (shooter instanceof Player player
                && projectile instanceof ArrowExtension arrow) {
            var caster = (SpellCasterEntity) player;
            var shotContext = caster.getArrowShootContext();

            // First run triggers to enable modifying the arrow by passive spells
            // (by appending the arrow shot context)

            final var firedBySpell = shotContext.firedBySpell;
            SpellTriggers.onArrowShot(arrow, player, firedBySpell);

            // Apply arrow modification

            Supplier<Collection<ServerPlayer>> trackers = () -> {
                if (shooter.level() instanceof ServerLevel sl) {
                    return sl.getChunkSource().chunkMap.getPlayers(shooter.chunkPosition(), false);
                }
                return List.of();
            };
            for (var spellEntry: shotContext.activeSpells) {
                ArrowHelper.onArrowShot(arrow, shooter, spellEntry, trackers);
            }

            // Clear arrow shoot context
            caster.setArrowShootContext(ArrowShootContext.empty());
        }
        return projectile;
    }
}
