package com.ultra.megamod.feature.alchemy.block;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.alchemy.AlchemyRecipeRegistry;
import com.ultra.megamod.feature.alchemy.AlchemyRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AlchemyGrindstoneBlock extends BaseEntityBlock {
    public static final MapCodec<AlchemyGrindstoneBlock> CODEC = AlchemyGrindstoneBlock.simpleCodec(AlchemyGrindstoneBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Brewing table shape
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16)    // Full block bounding box for brewing table
    );

    public AlchemyGrindstoneBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends AlchemyGrindstoneBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemyGrindstoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, (BlockEntityType<AlchemyGrindstoneBlockEntity>) AlchemyRegistry.ALCHEMY_GRINDSTONE_BE.get(),
                AlchemyGrindstoneBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AlchemyGrindstoneBlockEntity grindstone)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;

        // Try inserting the held item
        if (!stack.isEmpty()) {
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            AlchemyRecipeRegistry.GrindingRecipe recipe = AlchemyRecipeRegistry.findGrindingRecipe(itemId);
            if (recipe != null) {
                if (grindstone.insertItem(itemId, stack.copy())) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.7f, 1.0f);
                    serverPlayer.displayClientMessage(Component.literal("\u00a7eGrinding " + stack.getHoverName().getString() + "..."), true);
                    return InteractionResult.CONSUME;
                } else {
                    serverPlayer.displayClientMessage(Component.literal("\u00a7cGrindstone is busy."), true);
                    return InteractionResult.CONSUME;
                }
            }
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AlchemyGrindstoneBlockEntity grindstone) {
            ServerPlayer serverPlayer = (ServerPlayer) player;

            // Collect output if available
            if (grindstone.hasOutput()) {
                ItemStack output = grindstone.collectOutput();
                if (!output.isEmpty()) {
                    if (!player.getInventory().add(output)) {
                        player.drop(output, false);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.2f);
                    serverPlayer.displayClientMessage(Component.literal("\u00a7aCollected " + output.getHoverName().getString() + " x" + output.getCount()), true);
                }
                return InteractionResult.SUCCESS;
            }

            // Show status
            if (grindstone.isGrinding()) {
                int pct = (int) ((grindstone.getGrindingProgress() / (float) grindstone.getGrindingTotal()) * 100);
                serverPlayer.displayClientMessage(Component.literal("\u00a7e[Grindstone] Grinding... " + pct + "%"), true);
            } else {
                serverPlayer.displayClientMessage(Component.literal("\u00a77[Grindstone] Insert an ingredient to grind."), true);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
