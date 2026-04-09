package com.ultra.megamod.feature.adminmodules.modules.movement;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Freecam - Meteor-like approach using invisibility + flight instead of spectator mode.
 * The player becomes invisible and gains creative flight so they can freely explore
 * without being seen by other players. On disable, they teleport back to the saved
 * position with all effects restored.
 */
public class FreecamModule extends AdminModule {
    private double savedX, savedY, savedZ;
    private float savedYRot, savedXRot;
    private ResourceKey<Level> savedDimension;
    private boolean hadFlight;

    public FreecamModule() {
        super("freecam", "Freecam", "Invisible free camera - fly around unseen, return on disable", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onEnable(ServerPlayer player) {
        savedX = player.getX();
        savedY = player.getY();
        savedZ = player.getZ();
        savedYRot = player.getYRot();
        savedXRot = player.getXRot();
        savedDimension = player.level().dimension();
        hadFlight = player.getAbilities().mayfly;

        // Make invisible (no particles) so other players can't see you
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));

        // Grant flight ability without changing gamemode
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();

        // Disable collision so you can fly through blocks
        player.noPhysics = true;
    }

    @Override
    public void onDisable(ServerPlayer player) {
        // Remove invisibility
        player.removeEffect(MobEffects.INVISIBILITY);

        // Restore flight to previous state
        player.getAbilities().mayfly = hadFlight;
        player.getAbilities().flying = hadFlight && player.getAbilities().flying;
        player.onUpdateAbilities();

        // Re-enable collision
        player.noPhysics = false;

        // Teleport back to saved position
        ServerLevel targetLevel = player.level().getServer().getLevel(savedDimension);
        if (targetLevel != null) {
            player.teleportTo(targetLevel, savedX, savedY, savedZ, java.util.Set.of(), savedYRot, savedXRot, false);
        } else {
            player.teleportTo((ServerLevel) player.level(), savedX, savedY, savedZ, java.util.Set.of(), savedYRot, savedXRot, false);
        }
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        // Keep noPhysics active in case something resets it
        player.noPhysics = true;
    }
}
