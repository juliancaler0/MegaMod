package com.ultra.megamod.feature.adminmodules.modules.movement;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.AdminModuleManager;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class SpeedModule extends AdminModule {
    private ModuleSetting.IntSetting amplifier;
    private ModuleSetting.EnumSetting mode;
    private ModuleSetting.BoolSetting inLiquids;
    private ModuleSetting.BoolSetting whenSneaking;
    private ModuleSetting.DoubleSetting speedLimit;
    private ModuleSetting.BoolSetting jumpBoost;

    public SpeedModule() {
        super("speed", "Speed", "Increases movement speed", ModuleCategory.MOVEMENT);
    }

    @Override
    protected void initSettings() {
        amplifier = integer("Amplifier", 2, 0, 10, "Speed effect level (0-10)");
        mode = enumVal("Mode", "Potion", List.of("Potion", "Velocity"), "Speed mode");
        inLiquids = bool("InLiquids", false, "Apply speed while in water/lava");
        whenSneaking = bool("WhenSneaking", false, "Apply speed while sneaking");
        speedLimit = decimal("SpeedLimit", 10.0, 0.5, 20.0, "Max horizontal speed cap (velocity mode)");
        jumpBoost = bool("JumpBoost", false, "Also apply Jump Boost I for bhop feel");
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        // Don't stack with flight module
        if (AdminModuleManager.get().isModuleEnabled("flight")) return;

        // Check liquid condition
        if (!inLiquids.getValue() && (player.isInWater() || player.isInLava())) return;

        // Check sneaking condition
        if (!whenSneaking.getValue() && player.isShiftKeyDown()) return;

        if ("Potion".equals(mode.getValue())) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, amplifier.getValue(), false, false));
        } else {
            // Velocity mode: multiply horizontal movement directly
            Vec3 vel = player.getDeltaMovement();
            double horizSpeedSq = vel.x * vel.x + vel.z * vel.z;
            if (player.onGround() && horizSpeedSq > 0.0001) {
                double mult = 1.0 + amplifier.getValue() * 0.2;
                double newX = vel.x * mult;
                double newZ = vel.z * mult;

                // Apply speed cap
                double limit = speedLimit.getValue();
                double newHorizSpeed = Math.sqrt(newX * newX + newZ * newZ);
                if (newHorizSpeed > limit) {
                    double scale = limit / newHorizSpeed;
                    newX *= scale;
                    newZ *= scale;
                }

                player.setDeltaMovement(newX, vel.y, newZ);
                player.hurtMarked = true;
            }
        }

        // Apply Jump Boost I if enabled
        if (jumpBoost.getValue()) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 40, 0, false, false));
        }
    }

    @Override
    public void onDisable(ServerPlayer player) {
        player.removeEffect(MobEffects.SPEED);
        player.removeEffect(MobEffects.JUMP_BOOST);
    }
}
