package com.ultra.megamod.feature.combat.runes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Rune Crafting Altar — opens a smithing-table-style UI for combining items with runes.
 * Ported 1:1 from the Runes mod ref code.
 */
public class RuneCraftingBlock extends HorizontalDirectionalBlock {

    private static final MapCodec<RuneCraftingBlock> CODEC = simpleCodec(RuneCraftingBlock::new);
    private static final Component SCREEN_TITLE = Component.translatable("gui.megamod.rune_crafting");

    // Custom VoxelShape: base pedestal + middle pillar + top platform
    private static final VoxelShape TOP_SHAPE = Block.box(1, 12, 1, 15, 16, 15);
    private static final VoxelShape MIDDLE_SHAPE = Block.box(4, 3, 4, 12, 12, 12);
    private static final VoxelShape BOTTOM_SHAPE = Block.box(1, 0, 1, 15, 3, 15);
    private static final VoxelShape SHAPE = Shapes.or(TOP_SHAPE, MIDDLE_SHAPE, BOTTOM_SHAPE);

    public RuneCraftingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue((Property) FACING, (Comparable) Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (syncId, inventory, player) -> new RuneCraftingMenu(syncId, inventory, ContainerLevelAccess.create(level, pos)),
                SCREEN_TITLE
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(level, pos));
            player.awardStat(Stats.INTERACT_WITH_SMITHING_TABLE);
            return InteractionResult.CONSUME;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
}
