package com.ultra.megamod.feature.sitting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = "megamod")
public class PlayerSitting {
    private static final Map<UUID, UUID> SEAT_MAP = new HashMap<>();
    private static final List<Entity> PENDING_SEAT_CLEANUP = new ArrayList<>();

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!serverPlayer.isShiftKeyDown()) return;
        if (serverPlayer.getXRot() <= 80.0f) return;
        if (!serverPlayer.getMainHandItem().isEmpty() || !serverPlayer.getOffhandItem().isEmpty()) return;
        if (serverPlayer.isPassenger()) return;

        ServerLevel level = serverPlayer.level();
        ArmorStand seat = new ArmorStand(EntityType.ARMOR_STAND, level);
        seat.setPos(
            event.getPos().getX() + 0.5,
            event.getPos().getY() + 0.3,
            event.getPos().getZ() + 0.5
        );
        seat.setInvisible(true);
        seat.setNoGravity(true);
        seat.setInvulnerable(true);
        seat.setNoBasePlate(true);
        seat.setSilent(true);
        level.addFreshEntity(seat);
        serverPlayer.startRiding(seat);
        SEAT_MAP.put(serverPlayer.getUUID(), seat.getUUID());
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityDismount(EntityMountEvent event) {
        if (event.isMounting()) return;
        Entity rider = event.getEntityMounting();
        if (!(rider instanceof ServerPlayer player)) return;
        UUID seatId = SEAT_MAP.remove(player.getUUID());
        if (seatId == null) return;
        Entity seatEntity = event.getEntityBeingMounted();
        if (seatEntity != null && seatEntity.getUUID().equals(seatId)) {
            // Queue for next tick — discard() during mount event re-enters
            // LivingEntity.remove() and corrupts the passenger list.
            PENDING_SEAT_CLEANUP.add(seatEntity);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!PENDING_SEAT_CLEANUP.isEmpty()) {
            PENDING_SEAT_CLEANUP.forEach(Entity::discard);
            PENDING_SEAT_CLEANUP.clear();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID seatId = SEAT_MAP.remove(player.getUUID());
        if (seatId == null) return;
        ServerLevel level = (ServerLevel) player.level();
        Entity seatEntity = level.getEntity(seatId);
        if (seatEntity != null) {
            seatEntity.discard();
        }
    }
}
