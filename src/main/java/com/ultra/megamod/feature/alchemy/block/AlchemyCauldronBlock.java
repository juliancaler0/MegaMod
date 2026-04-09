package com.ultra.megamod.feature.alchemy.block;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.alchemy.AlchemyRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AlchemyCauldronBlock extends BaseEntityBlock {
    public static final MapCodec<AlchemyCauldronBlock> CODEC = AlchemyCauldronBlock.simpleCodec(AlchemyCauldronBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty BREWING = BooleanProperty.create("brewing");

    // Alchemy station shape
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 16)    // Full block bounding box for alchemy station
    );

    public AlchemyCauldronBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(BREWING, false));
    }

    @Override
    protected MapCodec<? extends AlchemyCauldronBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, BREWING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(BREWING, false);
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
        return new AlchemyCauldronBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, (BlockEntityType<AlchemyCauldronBlockEntity>) AlchemyRegistry.ALCHEMY_CAULDRON_BE.get(),
                AlchemyCauldronBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AlchemyCauldronBlockEntity cauldron)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;

        // Water bucket: fill cauldron
        if (stack.is(Items.WATER_BUCKET)) {
            if (cauldron.getWaterLevel() < 1) {
                cauldron.fillWater();
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                serverPlayer.displayClientMessage(Component.literal("\u00a7bCauldron filled with water."), true);
                return InteractionResult.CONSUME;
            } else {
                serverPlayer.displayClientMessage(Component.literal("\u00a7eCauldron already has water."), true);
                return InteractionResult.CONSUME;
            }
        }

        // Glass bottle: collect brewed potion
        if (stack.is(Items.GLASS_BOTTLE)) {
            if (cauldron.hasResult()) {
                ItemStack potion = cauldron.collectResult(serverPlayer);
                if (potion != null && !potion.isEmpty()) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    if (!player.getInventory().add(potion)) {
                        player.drop(potion, false);
                    }
                    level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                    serverPlayer.displayClientMessage(Component.literal("\u00a7aPotion collected!"), true);
                } else {
                    serverPlayer.displayClientMessage(Component.literal("\u00a7cBrewing failed - unknown recipe."), true);
                }
                return InteractionResult.CONSUME;
            } else {
                serverPlayer.displayClientMessage(Component.literal("\u00a7eNothing ready to collect."), true);
                return InteractionResult.CONSUME;
            }
        }

        // Reagent item: add ingredient
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (itemId.startsWith("megamod:reagent_")) {
            if (cauldron.getWaterLevel() <= 0) {
                serverPlayer.displayClientMessage(Component.literal("\u00a7cAdd water to the cauldron first!"), true);
                return InteractionResult.CONSUME;
            }
            if (cauldron.isBrewing()) {
                serverPlayer.displayClientMessage(Component.literal("\u00a7cCauldron is currently brewing!"), true);
                return InteractionResult.CONSUME;
            }
            if (cauldron.hasResult()) {
                serverPlayer.displayClientMessage(Component.literal("\u00a7cCollect the current potion first!"), true);
                return InteractionResult.CONSUME;
            }
            if (cauldron.addIngredient(itemId)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.5f, 1.2f);
                int count = cauldron.getIngredientCount();
                serverPlayer.displayClientMessage(Component.literal("\u00a7dReagent added (" + count + "/3)."), true);
                return InteractionResult.CONSUME;
            } else {
                serverPlayer.displayClientMessage(Component.literal("\u00a7cCauldron is full (3/3 ingredients)."), true);
                return InteractionResult.CONSUME;
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
        if (be instanceof AlchemyCauldronBlockEntity cauldron) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            // Show status
            StringBuilder msg = new StringBuilder("\u00a7d[Cauldron] ");
            if (cauldron.getWaterLevel() <= 0) {
                msg.append("\u00a77Empty - add water with a bucket.");
            } else if (cauldron.hasResult()) {
                msg.append("\u00a7aPotion ready! Use glass bottle to collect.");
            } else if (cauldron.isBrewing()) {
                int pct = (int) ((cauldron.getBrewingProgress() / 200.0) * 100);
                msg.append("\u00a7eBrewing... ").append(pct).append("%");
            } else {
                msg.append("\u00a7bWater: Full | Ingredients: ").append(cauldron.getIngredientCount()).append("/3");
                if (cauldron.getIngredientCount() > 0) {
                    msg.append(" | \u00a77Add reagents or wait for heat.");
                }
            }
            serverPlayer.displayClientMessage(Component.literal(msg.toString()), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(BREWING)) {
            // Purple bubbling particles when brewing
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            double y = pos.getY() + 0.9 + random.nextDouble() * 0.2;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            level.addParticle(ParticleTypes.WITCH, x, y, z, 0.0, 0.05, 0.0);
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0, 0.03, 0.0);
            }
            if (random.nextInt(5) == 0) {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, y + 0.1, pos.getZ() + 0.5, 0.0, 0.04, 0.0);
            }
        }
    }
}
