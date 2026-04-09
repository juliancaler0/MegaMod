/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.EnumProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.ultra.megamod.feature.dungeons.boss;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.dungeons.DungeonManager;
import com.ultra.megamod.feature.dungeons.DungeonTheme;
import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import com.ultra.megamod.feature.dungeons.boss.DungeonKeeperBoss;
import com.ultra.megamod.feature.dungeons.boss.FogWallBlock;
import com.ultra.megamod.feature.dungeons.boss.OssukageBoss;
import com.ultra.megamod.feature.dungeons.boss.WraithBoss;
import com.ultra.megamod.feature.dungeons.boss.FrostmawBoss;
import com.ultra.megamod.feature.dungeons.boss.WroughtnautBoss;
import com.ultra.megamod.feature.dungeons.boss.UmvuthiBoss;
import com.ultra.megamod.feature.dungeons.boss.ChaosSpawnerBoss;
import com.ultra.megamod.feature.dungeons.boss.SculptorBoss;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DungeonAltarBlock
extends Block {
    public static final MapCodec<DungeonAltarBlock> CODEC = DungeonAltarBlock.simpleCodec(DungeonAltarBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box((double)1.0, (double)0.0, (double)1.0, (double)15.0, (double)14.0, (double)15.0);

    public DungeonAltarBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
    }

    protected MapCodec<? extends DungeonAltarBlock> codec() {
        return CODEC;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        ServerPlayer serverPlayer = (ServerPlayer)player;
        DungeonManager manager = DungeonManager.get(serverLevel.getServer().overworld());
        DungeonManager.DungeonInstance instance = manager.getDungeonForPlayer(player.getUUID());
        if (instance == null) {
            serverPlayer.sendSystemMessage((Component)Component.literal((String)"The altar remains dormant...").withStyle(ChatFormatting.GRAY));
            return InteractionResult.CONSUME;
        }
        if (!instance.bossAlive) {
            serverPlayer.sendSystemMessage((Component)Component.literal((String)"The altar's power has been spent.").withStyle(ChatFormatting.GRAY));
            return InteractionResult.CONSUME;
        }
        DungeonBossEntity boss = selectBoss(serverLevel, instance.theme);
        boss.setPos((double)pos.getX() + 0.5, pos.getY() + 1, (double)pos.getZ() + 0.5);
        boss.setDungeonInstanceId(instance.instanceId);
        boss.setDifficultyMultiplier(instance.tier.getDifficultyMultiplier());
        boss.setBossRoomCenter(pos);
        serverLevel.addFreshEntity((Entity)boss);
        manager.bossSpawned(instance.instanceId);
        this.placeBossRoomFogWalls(serverLevel, pos);
        serverLevel.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5f, 0.8f);
        serverPlayer.sendSystemMessage((Component)Component.literal((String)("The altar awakens " + boss.getBossDisplayName().getString() + "!")).withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD}));
        serverLevel.removeBlock(pos, false);
        return InteractionResult.CONSUME;
    }

    private DungeonBossEntity selectBoss(ServerLevel level, DungeonTheme theme) {
        DungeonTheme.BossPreference pref = theme.getBossPreference();
        float roll = level.getRandom().nextFloat();
        return switch (pref) {
            case WRAITH -> {
                if (roll < 0.75f) yield new WraithBoss(DungeonEntityRegistry.WRAITH_BOSS.get(), (Level)level);
                else if (roll < 0.875f) yield new OssukageBoss(DungeonEntityRegistry.OSSUKAGE_BOSS.get(), (Level)level);
                else yield new DungeonKeeperBoss(DungeonEntityRegistry.DUNGEON_KEEPER.get(), (Level)level);
            }
            case OSSUKAGE -> {
                if (roll < 0.75f) yield new OssukageBoss(DungeonEntityRegistry.OSSUKAGE_BOSS.get(), (Level)level);
                else if (roll < 0.875f) yield new WraithBoss(DungeonEntityRegistry.WRAITH_BOSS.get(), (Level)level);
                else yield new DungeonKeeperBoss(DungeonEntityRegistry.DUNGEON_KEEPER.get(), (Level)level);
            }
            case DUNGEON_KEEPER -> {
                if (roll < 0.75f) yield new DungeonKeeperBoss(DungeonEntityRegistry.DUNGEON_KEEPER.get(), (Level)level);
                else if (roll < 0.875f) yield new WraithBoss(DungeonEntityRegistry.WRAITH_BOSS.get(), (Level)level);
                else yield new OssukageBoss(DungeonEntityRegistry.OSSUKAGE_BOSS.get(), (Level)level);
            }
            case WRAITH_OR_KEEPER -> {
                if (roll < 0.5f) yield new WraithBoss(DungeonEntityRegistry.WRAITH_BOSS.get(), (Level)level);
                else yield new DungeonKeeperBoss(DungeonEntityRegistry.DUNGEON_KEEPER.get(), (Level)level);
            }
            case FROSTMAW -> {
                if (roll < 0.75f) yield new FrostmawBoss(DungeonEntityRegistry.FROSTMAW_BOSS.get(), (Level)level);
                else if (roll < 0.875f) yield new WraithBoss(DungeonEntityRegistry.WRAITH_BOSS.get(), (Level)level);
                else yield new OssukageBoss(DungeonEntityRegistry.OSSUKAGE_BOSS.get(), (Level)level);
            }
            case WROUGHTNAUT -> {
                if (roll < 0.75f) yield new WroughtnautBoss(DungeonEntityRegistry.WROUGHTNAUT_BOSS.get(), (Level)level);
                else if (roll < 0.875f) yield new OssukageBoss(DungeonEntityRegistry.OSSUKAGE_BOSS.get(), (Level)level);
                else yield new DungeonKeeperBoss(DungeonEntityRegistry.DUNGEON_KEEPER.get(), (Level)level);
            }
            case UMVUTHI -> {
                if (roll < 0.75f) yield new UmvuthiBoss(DungeonEntityRegistry.UMVUTHI_BOSS.get(), (Level)level);
                else if (roll < 0.875f) yield new DungeonKeeperBoss(DungeonEntityRegistry.DUNGEON_KEEPER.get(), (Level)level);
                else yield new WraithBoss(DungeonEntityRegistry.WRAITH_BOSS.get(), (Level)level);
            }
            case CHAOS_SPAWNER -> {
                // Every dungeon has a ChaosSpawner gatekeeper, so remap real boss to DungeonKeeper
                if (roll < 0.75f) yield new DungeonKeeperBoss(DungeonEntityRegistry.DUNGEON_KEEPER.get(), (Level)level);
                else if (roll < 0.875f) yield new WraithBoss(DungeonEntityRegistry.WRAITH_BOSS.get(), (Level)level);
                else yield new UmvuthiBoss(DungeonEntityRegistry.UMVUTHI_BOSS.get(), (Level)level);
            }
            case SCULPTOR -> {
                if (roll < 0.75f) yield new SculptorBoss(DungeonEntityRegistry.SCULPTOR_BOSS.get(), (Level)level);
                else if (roll < 0.875f) yield new UmvuthiBoss(DungeonEntityRegistry.UMVUTHI_BOSS.get(), (Level)level);
                else yield new WroughtnautBoss(DungeonEntityRegistry.WROUGHTNAUT_BOSS.get(), (Level)level);
            }
        };
    }

    /**
     * Place fog walls at the boss room entrances (doorways).
     * Scans outward from the altar to find the room walls, then locates openings
     * (sequences of air blocks in the wall) which are the doorway entrances.
     * Fog walls are placed across these openings to seal the boss arena.
     */
    private void placeBossRoomFogWalls(ServerLevel level, BlockPos altarPos) {
        int searchRadius = 20;
        int floorY = altarPos.getY();

        // For each cardinal direction, scan outward from center to find the wall,
        // then place fog walls across any air openings in that wall line.
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            Direction perpendicular = dir.getClockWise();
            int wallDist = findWallDistance(level, altarPos, dir, searchRadius, floorY, perpendicular);
            if (wallDist <= 0) continue;

            // Fog walls face inward (opposite of wall direction) so they repel back into the room
            Direction fogFacing = dir.getOpposite();
            BlockState fogState = ((FogWallBlock)((Object)DungeonEntityRegistry.FOG_WALL_BLOCK.get()))
                    .defaultBlockState().setValue(FogWallBlock.FACING, fogFacing);

            // The wall is at wallDist blocks from the altar in this direction.
            // Scan along the wall perpendicular to this direction to find air openings (doorways).
            for (int p = -searchRadius; p <= searchRadius; p++) {
                BlockPos wallBase = altarPos.relative(dir, wallDist).relative(perpendicular, p);
                // Check if this position is an air opening at floor+1 height (doorway)
                BlockPos doorCheck = new BlockPos(wallBase.getX(), floorY + 1, wallBase.getZ());
                if (level.getBlockState(doorCheck).isAir() && level.getBlockState(doorCheck.above()).isAir()) {
                    // Verify there's solid wall on either side (real doorway, not open air)
                    BlockPos sideA = doorCheck.relative(perpendicular);
                    BlockPos sideB = doorCheck.relative(perpendicular.getOpposite());
                    boolean hasSolidNeighbor = !level.getBlockState(sideA).isAir()
                            || !level.getBlockState(sideB).isAir();
                    // Also check that the block one step further in this direction is air (corridor beyond)
                    BlockPos beyond = doorCheck.relative(dir);
                    boolean corridorBeyond = level.getBlockState(beyond).isAir();
                    if (hasSolidNeighbor || corridorBeyond) {
                        // This is a doorway opening — place fog wall here
                        for (int wy = 1; wy <= 4; wy++) {
                            BlockPos fogPos = new BlockPos(wallBase.getX(), floorY + wy, wallBase.getZ());
                            if (level.getBlockState(fogPos).isAir()) {
                                level.setBlock(fogPos, fogState, 3);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Scans outward from the altar in the given direction to find the distance to the first
     * solid wall. Checks multiple parallel lines to avoid missing walls when the altar
     * aligns with a doorway opening.
     * Returns the distance, or -1 if no wall found within maxDist.
     */
    private int findWallDistance(ServerLevel level, BlockPos altarPos, Direction dir, int maxDist, int floorY, Direction perpendicular) {
        for (int d = 1; d <= maxDist; d++) {
            int solidCount = 0;
            // Scan 9 parallel lines (-4 to +4 perpendicular offset)
            for (int p = -4; p <= 4; p++) {
                BlockPos check = altarPos.relative(dir, d).relative(perpendicular, p);
                BlockPos atDoor = new BlockPos(check.getX(), floorY + 1, check.getZ());
                BlockPos aboveDoor = new BlockPos(check.getX(), floorY + 2, check.getZ());
                if (!level.getBlockState(atDoor).isAir() && !level.getBlockState(aboveDoor).isAir()) {
                    solidCount++;
                }
            }
            // If at least 3 of 9 parallel positions are solid, we've found the wall line
            if (solidCount >= 3) {
                return d;
            }
        }
        return -1;
    }

    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }
}

