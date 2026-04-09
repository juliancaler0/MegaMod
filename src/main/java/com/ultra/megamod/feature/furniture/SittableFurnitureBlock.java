package com.ultra.megamod.feature.furniture;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = "megamod")
public class SittableFurnitureBlock extends FurnitureBlock {
    public static final MapCodec<SittableFurnitureBlock> CODEC = SittableFurnitureBlock.simpleCodec(SittableFurnitureBlock::new);

    private static final Map<Block, SeatConfig> SEAT_CONFIGS = new HashMap<>();
    private static final Map<UUID, UUID> SEAT_MAP = new HashMap<>();
    private static final List<Entity> PENDING_SEAT_CLEANUP = new ArrayList<>();

    public record SeatConfig(double seatHeight, int seatCount, double seatSpread) {}

    public SittableFurnitureBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    public static void registerSeating(Block block, double seatHeight, int seatCount) {
        SEAT_CONFIGS.put(block, new SeatConfig(seatHeight, seatCount, 0.0));
    }

    public static void registerSeating(Block block, double seatHeight, int seatCount, double seatSpread) {
        SEAT_CONFIGS.put(block, new SeatConfig(seatHeight, seatCount, seatSpread));
    }

    protected MapCodec<? extends SittableFurnitureBlock> codec() {
        return CODEC;
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        if (serverPlayer.isPassenger()) return InteractionResult.PASS;

        SeatConfig config = SEAT_CONFIGS.getOrDefault(this, new SeatConfig(0.3, 1, 0.0));
        ServerLevel serverLevel = (ServerLevel) level;

        // Count existing occupied seats at this block position
        List<ArmorStand> existingSeats = serverLevel.getEntitiesOfClass(ArmorStand.class,
                new AABB(pos).inflate(1.0),
                e -> e.getTags().contains("megamod_seat") && !e.getPassengers().isEmpty()
        );

        if (existingSeats.size() >= config.seatCount()) return InteractionResult.PASS;

        int seatIndex = existingSeats.size();
        Direction facing = state.getValue(FACING);

        double x = pos.getX() + 0.5;
        double y = pos.getY() + config.seatHeight();
        double z = pos.getZ() + 0.5;

        // Multi-seat: spread seats along the axis perpendicular to facing direction
        if (config.seatCount() > 1 && config.seatSpread() > 0) {
            double offset;
            if (config.seatCount() == 2) {
                offset = seatIndex == 0 ? -config.seatSpread() : config.seatSpread();
            } else {
                offset = -config.seatSpread() + (seatIndex * (2.0 * config.seatSpread() / (config.seatCount() - 1)));
            }
            switch (facing) {
                case NORTH, SOUTH -> x += offset;
                case EAST, WEST -> z += offset;
            }
        }

        ArmorStand seat = new ArmorStand(EntityType.ARMOR_STAND, serverLevel);
        // Set small to lower passenger riding height (bit 0x01 of DATA_CLIENT_FLAGS)
        byte flags = seat.getEntityData().get(ArmorStand.DATA_CLIENT_FLAGS);
        seat.getEntityData().set(ArmorStand.DATA_CLIENT_FLAGS, (byte)(flags | 0x01));
        seat.setPos(x, y, z);
        seat.setInvisible(true);
        seat.setNoGravity(true);
        seat.setInvulnerable(true);
        seat.setNoBasePlate(true);
        seat.setSilent(true);
        seat.addTag("megamod_seat");
        serverLevel.addFreshEntity(seat);
        serverPlayer.startRiding(seat);
        SEAT_MAP.put(serverPlayer.getUUID(), seat.getUUID());

        return InteractionResult.SUCCESS;
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
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos pos = event.getPos();
            if (event.getState().getBlock() instanceof SittableFurnitureBlock) {
                List<ArmorStand> seats = serverLevel.getEntitiesOfClass(ArmorStand.class,
                        new AABB(pos).inflate(1.0),
                        e -> e.getTags().contains("megamod_seat")
                );
                for (ArmorStand seat : seats) {
                    seat.ejectPassengers();
                    PENDING_SEAT_CLEANUP.add(seat);
                }
            }
        }
    }
}
