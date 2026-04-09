package com.ultra.megamod.feature.adminmodules.modules.combat;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class CrystalAuraModule extends AdminModule {
    private ModuleSetting.DoubleSetting breakRange;
    private ModuleSetting.DoubleSetting placeRange;
    private ModuleSetting.BoolSetting autoBreak;
    private ModuleSetting.BoolSetting placeCrystals;
    private int tickCounter = 0;

    public CrystalAuraModule() {
        super("crystal_aura", "CrystalAura", "Auto-places and detonates end crystals", ModuleCategory.COMBAT);
    }

    @Override
    protected void initSettings() {
        breakRange = decimal("Break Range", 4.0, 1.0, 6.0, "Crystal break/detonate range");
        placeRange = decimal("Place Range", 4.0, 1.0, 6.0, "Crystal placement range");
        autoBreak = bool("Auto Break", true, "Automatically detonate nearby crystals");
        placeCrystals = bool("Place Crystals", false, "Place end crystals near enemies");
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        tickCounter++;
        if (tickCounter % 4 != 0) return;

        // Auto-break: detonate crystals near enemies for damage
        if (autoBreak.getValue()) {
            double br = breakRange.getValue();
            AABB box = player.getBoundingBox().inflate(br);
            List<EndCrystal> crystals = level.getEntitiesOfClass(EndCrystal.class, box);
            for (EndCrystal crystal : crystals) {
                crystal.hurt(level.damageSources().playerAttack(player), 1.0f);
            }
        }

        // Auto-place: place end crystals on obsidian/bedrock surfaces near enemies
        if (placeCrystals.getValue()) {
            double pr = placeRange.getValue();
            AABB scanBox = player.getBoundingBox().inflate(pr + 4);
            // Find nearest non-admin player or hostile mob to target
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, scanBox, e -> {
                if (e == player || !e.isAlive()) return false;
                if (e instanceof Player p) {
                    return !AdminSystem.ADMIN_USERNAMES.contains(p.getGameProfile().name());
                }
                return e instanceof Monster;
            });
            if (targets.isEmpty()) return;
            targets.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));
            LivingEntity target = targets.get(0);

            // Search for valid crystal placement spots near the target
            BlockPos targetPos = target.blockPosition();
            int range = (int) Math.ceil(pr);
            for (int dx = -range; dx <= range; dx++) {
                for (int dz = -range; dz <= range; dz++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        BlockPos base = targetPos.offset(dx, dy, dz);
                        if (player.distanceToSqr(base.getX() + 0.5, base.getY() + 0.5, base.getZ() + 0.5) > pr * pr) continue;
                        BlockState state = level.getBlockState(base);
                        // Crystals can only be placed on obsidian or bedrock
                        if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) continue;
                        BlockPos above = base.above();
                        // Need 2 blocks of air above
                        if (!level.getBlockState(above).isAir()) continue;
                        if (!level.getBlockState(above.above()).isAir()) continue;
                        // Check no existing crystal at this position
                        AABB crystalCheck = new AABB(above).inflate(0.5);
                        if (!level.getEntitiesOfClass(EndCrystal.class, crystalCheck).isEmpty()) continue;
                        // Place crystal
                        EndCrystal crystal = new EndCrystal(level, above.getX() + 0.5, above.getY(), above.getZ() + 0.5);
                        crystal.setShowBottom(false);
                        level.addFreshEntity(crystal);
                        return; // Place one crystal per cycle
                    }
                }
            }
        }
    }
}
