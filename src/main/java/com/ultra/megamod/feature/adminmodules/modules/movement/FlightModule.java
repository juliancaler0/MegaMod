package com.ultra.megamod.feature.adminmodules.modules.movement;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FlightModule extends AdminModule {
    private ModuleSetting.DoubleSetting speed;
    private ModuleSetting.BoolSetting antiKick;
    private ModuleSetting.EnumSetting mode;
    private final Map<UUID, Integer> antiKickTimers = new HashMap<>();

    public FlightModule() {
        super("flight", "Flight", "Enables creative flight anywhere", ModuleCategory.MOVEMENT);
    }

    @Override
    protected void initSettings() {
        speed = decimal("Speed", 1.0, 0.1, 10.0, "Flight speed multiplier");
        antiKick = bool("Anti-Kick", true, "Prevents being kicked for flying");
        mode = enumVal("Mode", "Creative", List.of("Creative", "Vanilla", "Ability"), "Flight mode");
    }

    @Override
    public void onEnable(ServerPlayer player) {
        String m = mode.getValue();
        if ("Creative".equals(m) || "Ability".equals(m)) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.getAbilities().setFlyingSpeed((float)(0.05f * speed.getValue()));
            player.onUpdateAbilities();
        }
    }

    @Override
    public void onDisable(ServerPlayer player) {
        antiKickTimers.remove(player.getUUID());
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.getAbilities().setFlyingSpeed(0.05f);
            player.onUpdateAbilities();
        }
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        String m = mode.getValue();

        if ("Creative".equals(m) || "Ability".equals(m)) {
            // Creative/Ability mode: grant creative flight
            player.getAbilities().mayfly = true;
            player.getAbilities().setFlyingSpeed((float)(0.05f * speed.getValue()));
            player.onUpdateAbilities();
        } else {
            // Vanilla mode: simulate flight by canceling gravity and applying look-direction movement
            if (!player.onGround()) {
                net.minecraft.world.phys.Vec3 vel = player.getDeltaMovement();
                // Hover in place (cancel gravity), let player steer via look
                player.setDeltaMovement(vel.x * 0.9, vel.y > 0 ? vel.y * 0.8 : 0, vel.z * 0.9);
                player.fallDistance = 0;
                player.hurtMarked = true;
            }
        }

        if (antiKick.getValue()) {
            UUID uid = player.getUUID();
            int timer = antiKickTimers.getOrDefault(uid, 0) + 1;
            if (timer >= 40) {
                timer = 0;
                if (player.getAbilities().flying || !player.onGround()) {
                    // Teleport player to their current position to reset the server's
                    // "flying too long" tracking. resetPosition() may not exist in 1.21.11,
                    // so we use teleportTo which reliably resets the position tracker.
                    player.teleportTo((ServerLevel) player.level(),
                        player.getX(), player.getY(), player.getZ(),
                        java.util.Set.of(), player.getYRot(), player.getXRot(), false);
                }
            }
            antiKickTimers.put(uid, timer);
        }
    }
}
