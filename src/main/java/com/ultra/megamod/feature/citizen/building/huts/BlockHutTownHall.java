package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;
import com.ultra.megamod.feature.citizen.screen.townhall.WindowMainPage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Hut block for the town_hall building.
 * Opens the WindowMainPage (7-tab colony management hub) when right-clicked.
 */
public class BlockHutTownHall extends AbstractBlockHut<BlockHutTownHall> {

    public static final MapCodec<BlockHutTownHall> CODEC = simpleCodec(BlockHutTownHall::new);

    public BlockHutTownHall(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutTownHall> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "town_hall";
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            openTownHallScreen(pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    private static void openTownHallScreen(BlockPos pos) {
        // Open the Town Hall management screen (7 tabs: Actions, Info, Permissions, Citizens, Stats, Settings, Alliance)
        new WindowMainPage<>(pos).open();
    }
}
