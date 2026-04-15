package com.ultra.megamod.feature.combat.spell;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.ultra.megamod.lib.spellengine.spellbinding.SpellBindingScreenHandler;

/**
 * Spell Binding Table — craftable workstation for binding a chosen spell onto
 * a weapon with a pool-based SpellContainer (e.g. Arsenal swirl uniques,
 * Thalassian Sickle). Opens {@link SpellBindingScreenHandler} — the ported
 * SpellEngine binding UI that costs XP + lapis per bind.
 * <p>
 * Also serves as the POI block for Wizard Merchant villagers.
 */
public class SpellBindingTableBlock extends Block {

    public static final MapCodec<SpellBindingTableBlock> CODEC = simpleCodec(SpellBindingTableBlock::new);

    public SpellBindingTableBlock(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends SpellBindingTableBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        // Open the ported SpellEngine Spell Binding UI (pool-picker for magic weapons).
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInv, p) -> new SpellBindingScreenHandler(containerId, playerInv,
                        ContainerLevelAccess.create(level, pos)),
                Component.translatable("block.megamod.spell_binding_table")
        ));

        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.CONSUME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Floating particle effect (like enchanting table)
        for (int i = 0; i < 3; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            double y = pos.getY() + 1.0 + random.nextDouble() * 0.5;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            level.addParticle(ParticleTypes.ENCHANT, x, y, z,
                    (random.nextDouble() - 0.5) * 0.1, random.nextDouble() * 0.1, (random.nextDouble() - 0.5) * 0.1);
        }
    }
}
