package com.ultra.megamod.feature.museum;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Prevents non-admin players from breaking or placing blocks in the museum dimension.
 * Players can still interact with blocks (open doors, use buttons, etc.).
 * Admins (NeverNotch/Dev) bypass all restrictions.
 * Also grants Speed III while inside the museum for easier navigation.
 */
@EventBusSubscriber(modid = "megamod")
public class MuseumRules {

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (event.getTo().equals(MegaModDimensions.MUSEUM)) {
            // Grant Speed III (amplifier 2) with effectively infinite duration, no particles
            sp.addEffect(new MobEffectInstance(MobEffects.SPEED, MobEffectInstance.INFINITE_DURATION, 2, false, false, true));
        } else if (event.getFrom().equals(MegaModDimensions.MUSEUM)) {
            // Remove speed when leaving the museum
            sp.removeEffect(MobEffects.SPEED);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer sp)) return;
        if (!sp.level().dimension().equals(MegaModDimensions.MUSEUM)) return;
        if (AdminSystem.isAdmin(sp)) return;
        event.setCanceled(true);
        if (sp.level().getGameTime() % 20L == 0L) {
            sp.sendSystemMessage((Component) Component.literal((String) "You cannot break blocks in the museum!").withStyle(ChatFormatting.RED));
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!sp.level().dimension().equals(MegaModDimensions.MUSEUM)) return;
        if (AdminSystem.isAdmin(sp)) return;
        event.setCanceled(true);
        if (sp.level().getGameTime() % 20L == 0L) {
            sp.sendSystemMessage((Component) Component.literal((String) "You cannot place blocks in the museum!").withStyle(ChatFormatting.RED));
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        // Remove museum speed effect if player was in museum dimension
        if (sp.level().dimension().equals(MegaModDimensions.MUSEUM)) {
            sp.removeEffect(MobEffects.SPEED);
        }
    }
}
