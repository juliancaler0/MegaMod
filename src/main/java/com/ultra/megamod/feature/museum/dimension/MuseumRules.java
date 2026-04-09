package com.ultra.megamod.feature.museum.dimension;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/**
 * Prevents all mob griefing, explosions, and non-player block breaking in the museum dimension.
 */
@EventBusSubscriber(modid = "megamod")
public class MuseumRules {

    @SubscribeEvent
    public static void onMobGriefing(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        if (entity != null && entity.level() instanceof ServerLevel level) {
            if (level.dimension().equals(MegaModDimensions.MUSEUM)) {
                event.setCanGrief(false);
            }
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Start event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (level.dimension().equals(MegaModDimensions.MUSEUM)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (level.dimension().equals(MegaModDimensions.MUSEUM)) {
                Player player = event.getPlayer();
                if (player instanceof ServerPlayer sp && AdminSystem.isAdmin(sp)) {
                    return;
                }
                event.setCanceled(true);
            }
        }
    }
}
