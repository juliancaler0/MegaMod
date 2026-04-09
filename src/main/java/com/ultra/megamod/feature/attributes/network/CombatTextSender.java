package com.ultra.megamod.feature.attributes.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.List;

public class CombatTextSender {
    // Color constants
    public static final int WHITE = 0xFFFFFFFF;
    public static final int YELLOW = 0xFFFFFF00;
    public static final int GOLD = 0xFFFFAA00;
    public static final int RED = 0xFFFF4444;
    public static final int GREEN = 0xFF44FF44;
    public static final int CYAN = 0xFF44FFFF;
    public static final int ORANGE = 0xFFFF8800;
    public static final int PURPLE = 0xFFAA44FF;
    public static final int FIRE_RED = 0xFFFF6622;
    public static final int ICE_BLUE = 0xFF88CCFF;
    public static final int LIGHTNING_YELLOW = 0xFFFFFF44;
    public static final int POISON_GREEN = 0xFF44CC44;
    public static final int HOLY_WHITE = 0xFFFFFFCC;
    public static final int SHADOW_PURPLE = 0xFF8844AA;
    public static final int ARCANE_PURPLE = 0xFF7E3BFF;

    public static void sendDamage(LivingEntity target, float amount) {
        send(target, formatDamage(amount), WHITE, 1.3f);
    }

    public static void sendCrit(LivingEntity target, float amount) {
        send(target, "\u2726 " + formatDamage(amount) + " \u2726", GOLD, 2.0f);
    }

    public static void sendDodge(LivingEntity target) {
        send(target, "DODGED!", CYAN, 1.6f);
    }

    public static void sendLifesteal(LivingEntity healer, float amount) {
        send(healer, "+" + formatDamage(amount), GREEN, 1.1f);
    }

    public static void sendStun(LivingEntity target) {
        send(target, "STUNNED!", ORANGE, 1.5f);
    }

    public static void sendThorns(LivingEntity target, float amount) {
        send(target, "\u26A1 " + formatDamage(amount), PURPLE, 1.1f);
    }

    public static void sendElemental(LivingEntity target, float amount, String element) {
        int color = switch (element) {
            case "fire" -> FIRE_RED;
            case "ice" -> ICE_BLUE;
            case "lightning" -> LIGHTNING_YELLOW;
            case "poison" -> POISON_GREEN;
            case "holy" -> HOLY_WHITE;
            case "shadow" -> SHADOW_PURPLE;
            case "arcane" -> ARCANE_PURPLE;
            default -> WHITE;
        };
        send(target, formatDamage(amount) + " " + element, color, 1.1f);
    }

    public static void sendBlock(LivingEntity target) {
        send(target, "BLOCKED!", CYAN, 1.5f);
    }

    public static void sendAbility(LivingEntity target, String abilityName) {
        send(target, abilityName, GOLD, 1.3f);
    }

    public static void sendEffect(LivingEntity target, String effectName, int color) {
        send(target, effectName, color, 1.2f);
    }

    private static String formatDamage(float amount) {
        if (amount == (int) amount) return String.valueOf((int) amount);
        return String.format("%.1f", amount);
    }

    private static void send(LivingEntity entity, String text, int color, float scale) {
        if (entity.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) entity.level();
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() + 0.5;
        double z = entity.getZ();
        // Add slight random offset so numbers don't stack
        x += (level.getRandom().nextDouble() - 0.5) * 0.5;
        y += level.getRandom().nextDouble() * 0.3;
        z += (level.getRandom().nextDouble() - 0.5) * 0.5;

        CombatTextPayload payload = new CombatTextPayload(List.of(
            new CombatTextPayload.CombatTextEntry(x, y, z, text, color, scale)
        ));

        // Send to all players within 32 blocks
        for (ServerPlayer player : level.getPlayers(p -> p.distanceTo(entity) < 32)) {
            PacketDistributor.sendToPlayer(player, (CustomPacketPayload) payload);
        }
    }
}
