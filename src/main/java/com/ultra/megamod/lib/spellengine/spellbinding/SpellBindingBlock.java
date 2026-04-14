package com.ultra.megamod.lib.spellengine.spellbinding;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpellBindingBlock extends BaseEntityBlock {
    // Port note (1.21.11): Block.<init> now hard-requires Properties#id (set via setId). We attach
    // the registry key to both the block Properties and the BlockItem Properties up front so these
    // static instances can be created before DeferredRegister processes the mod event bus. The IDs
    // must match what gets registered via RPGItemRegistry/BLOCK deferred-register in SpellEngineItems.
    public static SpellBindingBlock INSTANCE = new SpellBindingBlock(BlockBehaviour.Properties.of()
            .setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, SpellBinding.ID))
            .destroyTime(4F).noOcclusion());
    public static final BlockItem ITEM = new BlockItem(INSTANCE, new Item.Properties()
            .setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, SpellBinding.ID)));

    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    public static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2).filter(pos -> Math.abs(pos.getX()) == 2 || Math.abs(pos.getZ()) == 2).map(BlockPos::immutable).toList();

    public static final MapCodec<SpellBindingBlock> CODEC = simpleCodec(SpellBindingBlock::new);
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public SpellBindingBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    public static boolean canAccessBookshelf(Level world, BlockPos tablePos, BlockPos bookshelfOffset) {
        return world.getBlockState(tablePos.offset(bookshelfOffset)).is(Blocks.BOOKSHELF) && world.isEmptyBlock(tablePos.offset(bookshelfOffset.getX() / 2, bookshelfOffset.getY(), bookshelfOffset.getZ() / 2));
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        super.animateTick(state, world, pos, random);
        for (BlockPos blockPos : BOOKSHELF_OFFSETS) {
            if (random.nextInt(16) != 0 || !SpellBindingBlock.canAccessBookshelf(world, pos, blockPos)) continue;
            world.addParticle(ParticleTypes.ENCHANT, (double)pos.getX() + 0.5, (double)pos.getY() + 2.0, (double)pos.getZ() + 0.5, (double)((float)blockPos.getX() + random.nextFloat()) - 0.5, (float)blockPos.getY() - random.nextFloat() - 1.0f, (double)((float)blockPos.getZ() + random.nextFloat()) - 0.5);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpellBindingBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide() ? createTickerHelper(type, SpellBindingBlockEntity.ENTITY_TYPE, SpellBindingBlockEntity::tick) : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        player.openMenu(state.getMenuProvider(world, pos));
        return InteractionResult.CONSUME;
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SpellBindingBlockEntity) {
            // Text text = ((Nameable)((Object)blockEntity)).getDisplayName();
            var text = Component.translatable("gui.spell_engine.spell_binding.title");
            // return new SimpleMenuProvider((syncId, inventory, player) -> new EnchantmentScreenHandler(syncId, inventory, ContainerLevelAccess.create(world, pos)), text);
            return new SimpleMenuProvider((syncId, inventory, player) ->
                    new SpellBindingScreenHandler(syncId, inventory, ContainerLevelAccess.create(world, pos)), text);
        }
        return null;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag options) {
        tooltip.add(Component
                .translatable("block.spell_engine.spell_binding.description")
                .withStyle(ChatFormatting.GRAY)
        );
    }
}