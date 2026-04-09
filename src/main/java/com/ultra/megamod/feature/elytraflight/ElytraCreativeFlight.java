/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.ultra.megamod.feature.elytraflight;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="megamod")
public class ElytraCreativeFlight {
    private static final double FLY_SPEED = 0.5;
    private static final double VERTICAL_SPEED = 0.3;
    private static final Map<UUID, FlightState> FLYING_PLAYERS = new HashMap<UUID, FlightState>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        boolean jumpPressed;
        boolean jumpRising;
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        UUID uuid = player2.getUUID();
        boolean hasElytra = player2.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
        if (!hasElytra) {
            if (FLYING_PLAYERS.containsKey(uuid)) {
                ElytraCreativeFlight.disableFlight(player2);
            }
            return;
        }
        if (!player2.isAlive() || player2.isCreative() || player2.isSpectator()) {
            FLYING_PLAYERS.remove(uuid);
            return;
        }
        Input input = player2.getLastClientInput();
        boolean isFlying = FLYING_PLAYERS.containsKey(uuid);
        FlightState state = FLYING_PLAYERS.computeIfAbsent(uuid, k -> new FlightState());
        if (state.jumpCooldown > 0) {
            --state.jumpCooldown;
        }
        boolean bl = jumpRising = (jumpPressed = input.jump()) && !state.wasJumping;
        if (isFlying) {
            if (jumpRising && state.jumpCooldown <= 0) {
                ElytraCreativeFlight.disableFlight(player2);
                state.wasJumping = jumpPressed;
                return;
            }
            if (player2.onGround() && !jumpPressed) {
                ElytraCreativeFlight.disableFlight(player2);
                state.wasJumping = jumpPressed;
                return;
            }
            ElytraCreativeFlight.applyFlightMovement(player2, input);
        } else if (jumpRising && !player2.onGround() && state.jumpCooldown <= 0) {
            ElytraCreativeFlight.enableFlight(player2);
            state.jumpCooldown = 5;
        }
        state.wasOnGround = player2.onGround();
        state.wasJumping = jumpPressed;
    }

    private static void enableFlight(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!FLYING_PLAYERS.containsKey(uuid)) {
            FLYING_PLAYERS.put(uuid, new FlightState());
        }
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        if (player.isFallFlying()) {
            player.stopFallFlying();
        }
        player.fallDistance = 0.0;
    }

    private static void disableFlight(ServerPlayer player) {
        FLYING_PLAYERS.remove(player.getUUID());
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    private static void applyFlightMovement(ServerPlayer player, Input input) {
        player.fallDistance = 0.0;
        if (player.isFallFlying()) {
            player.stopFallFlying();
        }
    }

    private static class FlightState {
        boolean wasOnGround = true;
        boolean wasJumping = false;
        int jumpCooldown = 0;

        FlightState() {
        }
    }
}

