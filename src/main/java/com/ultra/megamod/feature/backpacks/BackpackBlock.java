package com.ultra.megamod.feature.backpacks;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.backpacks.menu.BackpackMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Placeable backpack block. When a player sneak+right-clicks with a BackpackItem,
 * the backpack is placed as this block with contents preserved in the block entity.
 * Right-clicking the placed block opens the backpack menu.
 * Breaking the block drops the backpack item with all contents intact.
 */
public class BackpackBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<BackpackBlock> CODEC = BackpackBlock.simpleCodec(BackpackBlock::new);

    // Slightly smaller than a full block to look like a placed backpack
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public BackpackBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState((BlockState) this.stateDefinition.any()
                .setValue((Property) FACING, (Comparable) Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BackpackBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState) this.defaultBlockState()
                .setValue((Property) FACING, (Comparable) ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BackpackBlockEntity(pos, state);
    }

    /**
     * Right-click the placed backpack to open its inventory.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BackpackBlockEntity backpackBE && player instanceof ServerPlayer serverPlayer) {
            BackpackTier tier = backpackBE.getTier();
            SimpleContainer container = backpackBE.toContainer();
            SimpleContainer toolContainer = backpackBE.toToolContainer();
            com.ultra.megamod.feature.backpacks.upgrade.UpgradeManager upgradeMgr = new com.ultra.megamod.feature.backpacks.upgrade.UpgradeManager(tier);
            // For block entities, load upgrade data from the original item data stored in BE
            ItemStack tempStack = backpackBE.toItemStack();
            upgradeMgr.initializeFromStack(tempStack);

            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> {
                        BackpackMenu menu = new BackpackMenu(id, inv, container, toolContainer, upgradeMgr, tier.ordinal(), -2);
                        menu.setBlockEntityPos(pos);
                        return menu;
                    },
                    Component.literal(backpackBE.getVariant().getDisplayName() + " Backpack")
            ), buf -> buf.writeInt(tier.ordinal()));
        }
        return InteractionResult.CONSUME;
    }

    /**
     * When the block is broken, drop the backpack as an item with all contents preserved.
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BackpackBlockEntity backpackBE) {
                ItemStack drop = backpackBE.toItemStack();
                if (!drop.isEmpty()) {
                    Block.popResource(level, pos, drop);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Override to prevent default block drops since we handle drops in playerWillDestroy.
     */
    // Note: Block entity removal handled automatically by Minecraft.
    // Item drops are handled in playerWillDestroy above.
}
