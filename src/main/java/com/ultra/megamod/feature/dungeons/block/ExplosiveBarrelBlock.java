package com.ultra.megamod.feature.dungeons.block;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Set;

public class ExplosiveBarrelBlock extends Block {
    public static final MapCodec<ExplosiveBarrelBlock> CODEC = ExplosiveBarrelBlock.simpleCodec(ExplosiveBarrelBlock::new);

    /** Blocks that should never be destroyed by barrel explosions */
    private static final Set<Block> PROTECTED_BLOCKS = Set.of(
        Blocks.BEDROCK, Blocks.BARRIER, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME,
        Blocks.SPAWNER, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST
    );

    private static final int RADIUS = 3;

    public ExplosiveBarrelBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends ExplosiveBarrelBlock> codec() {
        return CODEC;
    }

    /** Triggered when a player punches the barrel */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        detonate(state, level, pos);
    }

    /** Triggered when an arrow or other projectile hits the barrel */
    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        detonate(state, level, hit.getBlockPos());
    }

    private void detonate(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;
        // Prevent double-detonation if block is already gone
        if (!level.getBlockState(pos).is(this)) return;

        level.removeBlock(pos, false);

        // Visual + damage explosion (no block breaking — we handle that manually)
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            4.0f, Level.ExplosionInteraction.NONE);

        // Manually break blocks in radius — works in dungeons
        boolean inDungeon = level.dimension().equals(MegaModDimensions.DUNGEON);
        if (inDungeon) {
            breakBlocksInRadius(serverLevel, pos);
        } else {
            // Outside dungeons, use normal explosion block breaking
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                4.0f, Level.ExplosionInteraction.BLOCK);
        }

        // Extra particles for visual impact
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            1, 0.0, 0.0, 0.0, 0.0);
        serverLevel.sendParticles(ParticleTypes.FLAME,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            30, 1.5, 1.5, 1.5, 0.05);
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
            20, 1.5, 1.5, 1.5, 0.02);
        serverLevel.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.5f, 0.8f);
    }

    /**
     * Breaks blocks in a sphere around the explosion center.
     * Bypasses dungeon block-break protection but respects protected blocks.
     * Uses distance falloff so outer blocks have a chance to survive.
     */
    private static void breakBlocksInRadius(ServerLevel level, BlockPos center) {
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > RADIUS) continue;

                    // Outer blocks have a chance to survive (falloff)
                    if (dist > 1.5 && level.random.nextFloat() > (1.0f - (float)(dist / RADIUS) * 0.6f)) {
                        continue;
                    }

                    BlockPos target = center.offset(dx, dy, dz);
                    BlockState targetState = level.getBlockState(target);
                    if (targetState.isAir()) continue;

                    Block block = targetState.getBlock();
                    // Don't break protected blocks or other explosive barrels
                    if (PROTECTED_BLOCKS.contains(block)) continue;
                    if (block instanceof ExplosiveBarrelBlock) continue;
                    // Don't break custom dungeon blocks (chaos barriers, portal blocks, etc.)
                    String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();
                    if (blockId.startsWith("megamod:") || blockId.startsWith("dungeonnowloading:")) {
                        if (blockId.contains("barrier") || blockId.contains("portal") || blockId.contains("spawner")) {
                            continue;
                        }
                    }

                    level.destroyBlock(target, false);
                }
            }
        }
    }
}
