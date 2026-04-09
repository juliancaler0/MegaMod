package com.ultra.megamod.feature.citizen.colonyblocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Compost barrel — accepts organic items, progresses through 10 stages (0-9),
 * and produces compost/bone meal when complete.
 */
public class BlockBarrel extends Block implements EntityBlock {
    public static final MapCodec<BlockBarrel> CODEC = BlockBarrel.simpleCodec(BlockBarrel::new);
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 9);

    public BlockBarrel(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected MapCodec<? extends BlockBarrel> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{STAGE});
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityBarrel(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        if (type == ColonyBlockRegistry.BARREL_BE.get()) {
            return (lvl, pos, st, be) -> TileEntityBarrel.serverTick(lvl, pos, st, (TileEntityBarrel) be);
        }
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEntityBarrel barrel) {
            if (barrel.isDone()) {
                // Collect output
                ItemStack output = barrel.collectOutput();
                if (!output.isEmpty()) {
                    if (!player.getInventory().add(output)) {
                        player.drop(output, false);
                    }
                    ((ServerPlayer) player).displayClientMessage(
                        Component.literal("\u00a7aCompost collected!"), true);
                }
            } else {
                int stage = barrel.getStage();
                ((ServerPlayer) player).displayClientMessage(
                    Component.literal("\u00a7eComposting... Stage " + stage + "/9"), true);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEntityBarrel barrel) {
            if (!barrel.isComposting() && !barrel.isDone()) {
                // Accept organic items to start composting
                if (isCompostable(stack)) {
                    barrel.addInput(stack.copy());
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    ((ServerPlayer) player).displayClientMessage(
                        Component.literal("\u00a7dItem added to barrel. Composting started."), true);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    private boolean isCompostable(ItemStack stack) {
        // Accept common organic items
        return stack.is(Items.WHEAT) || stack.is(Items.WHEAT_SEEDS) || stack.is(Items.BEETROOT_SEEDS)
            || stack.is(Items.MELON_SEEDS) || stack.is(Items.PUMPKIN_SEEDS) || stack.is(Items.POTATO)
            || stack.is(Items.CARROT) || stack.is(Items.BEETROOT) || stack.is(Items.APPLE)
            || stack.is(Items.SWEET_BERRIES) || stack.is(Items.KELP) || stack.is(Items.BAMBOO)
            || stack.is(Items.SUGAR_CANE) || stack.is(Items.CACTUS) || stack.is(Items.VINE)
            || stack.is(Items.TALL_GRASS) || stack.is(Items.FERN) || stack.is(Items.DEAD_BUSH)
            || stack.is(Items.MELON_SLICE) || stack.is(Items.PUMPKIN) || stack.is(Items.ROTTEN_FLESH)
            || stack.is(Items.BONE_MEAL) || stack.is(Items.BREAD);
    }
}
