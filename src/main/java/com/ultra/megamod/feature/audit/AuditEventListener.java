package com.ultra.megamod.feature.audit;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "megamod")
public class AuditEventListener {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;
        if (AdminSystem.isAdmin(sp)) return;
        ServerLevel level = (ServerLevel) sp.level();
        AuditLogManager.get(level).log(
            sp.getGameProfile().name(),
            sp.getUUID().toString(),
            AuditLogManager.EventType.LOGIN_LOGOUT,
            "Logged in"
        );
        AuditLogManager.get(level).saveToDisk(level);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;
        if (AdminSystem.isAdmin(sp)) return;
        ServerLevel level = (ServerLevel) sp.level();
        AuditLogManager.get(level).log(
            sp.getGameProfile().name(),
            sp.getUUID().toString(),
            AuditLogManager.EventType.LOGIN_LOGOUT,
            "Logged out"
        );
        AuditLogManager.get(level).saveToDisk(level);
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        Entity entity = ((CommandSourceStack) event.getParseResults().getContext().getSource()).getEntity();
        if (!(entity instanceof ServerPlayer sp)) return;
        if (AdminSystem.isAdmin(sp)) return;
        String cmd = event.getParseResults().getReader().getString();
        ServerLevel level = (ServerLevel) sp.level();
        AuditLogManager.get(level).log(
            sp.getGameProfile().name(),
            sp.getUUID().toString(),
            AuditLogManager.EventType.COMMAND_USED,
            "Command: /" + cmd
        );
        AuditLogManager.get(level).saveToDisk(level);
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (AdminSystem.isAdmin(sp)) return;
        ServerLevel level = (ServerLevel) sp.level();
        String cause = event.getSource().type().msgId();
        AuditLogManager.get(level).log(
            sp.getGameProfile().name(),
            sp.getUUID().toString(),
            AuditLogManager.EventType.PLAYER_DEATH,
            "Died: " + cause
        );
        AuditLogManager.get(level).saveToDisk(level);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;
        if (AdminSystem.isAdmin(sp)) return;
        ServerLevel level = (ServerLevel) sp.level();
        String from = event.getFrom().identifier().toString();
        String to = event.getTo().identifier().toString();
        AuditLogManager.get(level).log(
            sp.getGameProfile().name(),
            sp.getUUID().toString(),
            AuditLogManager.EventType.DIMENSION_ENTER,
            from + " -> " + to
        );
        AuditLogManager.get(level).saveToDisk(level);
    }
}
