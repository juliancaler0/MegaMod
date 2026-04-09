package com.ultra.megamod.feature.adminmodules.modules.combat;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.AABB;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class BowAimbotModule extends AdminModule {
    private ModuleSetting.DoubleSetting range;
    private ModuleSetting.BoolSetting onlyHostile;
    private int tickCounter = 0;

    public BowAimbotModule() {
        super("bow_aimbot", "BowAimbot", "Redirects arrows toward nearest target", ModuleCategory.COMBAT);
    }

    @Override
    protected void initSettings() {
        range = decimal("Range", 32.0, 8.0, 64.0, "Aimbot range");
        onlyHostile = bool("Only Hostile", true, "Only target hostile mobs");
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        // Rate limit: run every 2 ticks instead of every tick to reduce entity scanning overhead
        if (++tickCounter % 2 != 0) return;

        double r = range.getValue();
        AABB box = player.getBoundingBox().inflate(r);
        List<AbstractArrow> arrows = level.getEntitiesOfClass(AbstractArrow.class, box, a -> a.getOwner() == player && !a.onGround());
        if (arrows.isEmpty()) return;

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, e -> {
            if (e == player) return false;
            if (!e.isAlive()) return false;
            if (e instanceof Player p) {
                if (onlyHostile.getValue()) return false;
                // Never target admins
                return !AdminSystem.ADMIN_USERNAMES.contains(p.getGameProfile().name());
            }
            if (onlyHostile.getValue() && !(e instanceof Monster)) return false;
            return true;
        });
        if (targets.isEmpty()) return;

        for (AbstractArrow arrow : arrows) {
            // For each arrow, find the closest target to THAT ARROW (not to the player)
            // This gives much better accuracy since the arrow redirects toward the nearest entity to it
            LivingEntity closest = null;
            double closestDist = Double.MAX_VALUE;
            for (LivingEntity target : targets) {
                double dist = arrow.distanceTo(target);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = target;
                }
            }
            if (closest == null) continue;

            double dx = closest.getX() - arrow.getX();
            double dy = closest.getEyeY() - arrow.getY();
            double dz = closest.getZ() - arrow.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 0) {
                double speed = arrow.getDeltaMovement().length();
                arrow.setDeltaMovement(dx / dist * speed, dy / dist * speed, dz / dist * speed);
            }
        }
    }
}
