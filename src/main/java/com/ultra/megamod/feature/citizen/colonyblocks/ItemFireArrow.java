package com.ultra.megamod.feature.citizen.colonyblocks;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Fire Arrow item that creates arrows which set targets on fire.
 * The arrow spawns already burning, igniting anything it hits for 5 seconds.
 */
public class ItemFireArrow extends ArrowItem {

    public ItemFireArrow(Properties properties) {
        super(properties);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        Projectile projectile = super.asProjectile(level, pos, stack, direction);
        if (projectile instanceof Arrow arrow) {
            arrow.setRemainingFireTicks(100); // 5 seconds of fire
        }
        return projectile;
    }
}
