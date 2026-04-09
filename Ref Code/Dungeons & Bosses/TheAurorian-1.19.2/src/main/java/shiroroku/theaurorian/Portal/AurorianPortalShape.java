package shiroroku.theaurorian.Portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import shiroroku.theaurorian.Blocks.AurorianPortal;
import shiroroku.theaurorian.Registry.BlockRegistry;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class AurorianPortalShape {

    private static final BlockBehaviour.StatePredicate FRAME = (blockState, blockGetter, pos) -> blockState.is(BlockRegistry.aurorian_portal_frame.get());
    private final LevelAccessor level;
    private final Direction.Axis axis;
    private final Direction rightDir;
    private int numPortalBlocks;
    private BlockPos bottomLeft;
    private int height;
    private final int width;

    public static Optional<AurorianPortalShape> findEmptyPortalShape(LevelAccessor pLevel, BlockPos pBottomLeft, Direction.Axis pAxis) {
        return findPortalShape(pLevel, pBottomLeft, (portalShape) -> portalShape.isValid() && portalShape.numPortalBlocks == 0, pAxis);
    }

    public static Optional<AurorianPortalShape> findPortalShape(LevelAccessor pLevel, BlockPos pBottomLeft, Predicate<AurorianPortalShape> pPredicate, Direction.Axis pAxis) {
        Optional<AurorianPortalShape> optional = Optional.of(new AurorianPortalShape(pLevel, pBottomLeft, pAxis)).filter(pPredicate);
        if (optional.isPresent()) {
            return optional;
        }

        Direction.Axis axis = pAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        return Optional.of(new AurorianPortalShape(pLevel, pBottomLeft, axis)).filter(pPredicate);
    }

    public AurorianPortalShape(LevelAccessor pLevel, BlockPos pBottomLeft, Direction.Axis pAxis) {
        this.level = pLevel;
        this.axis = pAxis;
        this.rightDir = pAxis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = this.calculateBottomLeft(pBottomLeft);
        if (this.bottomLeft == null) {
            this.bottomLeft = pBottomLeft;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = this.calculateWidth();
            if (this.width > 0) {
                this.height = this.calculateHeight();
            }
        }

    }

    @Nullable
    private BlockPos calculateBottomLeft(BlockPos pos) {
        int i = Math.max(this.level.getMinBuildHeight(), pos.getY() - 21);
        while (pos.getY() > i && isEmpty(this.level.getBlockState(pos.below()))) {
            pos = pos.below();
        }

        Direction direction = this.rightDir.getOpposite();
        int j = this.getDistanceUntilEdgeAboveFrame(pos, direction) - 1;
        return j < 0 ? null : pos.relative(direction, j);
    }

    private int calculateWidth() {
        int i = this.getDistanceUntilEdgeAboveFrame(this.bottomLeft, this.rightDir);
        return i >= 2 && i <= 21 ? i : 0;
    }

    private int getDistanceUntilEdgeAboveFrame(BlockPos pPos, Direction pDirection) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= 21; ++i) {
            mut.set(pPos).move(pDirection, i);
            BlockState blockstate = this.level.getBlockState(mut);
            if (!isEmpty(blockstate)) {
                if (FRAME.test(blockstate, this.level, mut)) {
                    return i;
                }
                break;
            }

            if (!FRAME.test(this.level.getBlockState(mut.move(Direction.DOWN)), this.level, mut)) {
                break;
            }
        }
        return 0;
    }

    private int calculateHeight() {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int i = this.getDistanceUntilTop(mut);
        return i >= 3 && i <= 21 && this.hasTopFrame(mut, i) ? i : 0;
    }

    private boolean hasTopFrame(BlockPos.MutableBlockPos mut, int y) {
        for (int i = 0; i < this.width; ++i) {
            BlockPos.MutableBlockPos mut1 = mut.set(this.bottomLeft).move(Direction.UP, y).move(this.rightDir, i);
            if (!FRAME.test(this.level.getBlockState(mut1), this.level, mut1)) {
                return false;
            }
        }
        return true;
    }

    private int getDistanceUntilTop(BlockPos.MutableBlockPos pPos) {
        for (int i = 0; i < 21; ++i) {
            pPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, -1);
            if (!FRAME.test(this.level.getBlockState(pPos), this.level, pPos)) {
                return i;
            }
            pPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, this.width);
            if (!FRAME.test(this.level.getBlockState(pPos), this.level, pPos)) {
                return i;
            }
            for (int j = 0; j < this.width; ++j) {
                pPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, j);
                BlockState blockstate = this.level.getBlockState(pPos);
                if (!isEmpty(blockstate)) {
                    return i;
                }

                if (blockstate.is(BlockRegistry.aurorian_portal.get())) {
                    ++this.numPortalBlocks;
                }
            }
        }
        return 21;
    }

    private static boolean isEmpty(BlockState pState) {
        return pState.isAir() || pState.is(BlockTags.FIRE) || pState.is(BlockRegistry.aurorian_portal.get());
    }

    public boolean isValid() {
        return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
    }

    public void createPortalBlocks() {
        BlockState blockstate = BlockRegistry.aurorian_portal.get().defaultBlockState().setValue(AurorianPortal.FACING, this.axis == Direction.Axis.Z ? Direction.WEST : Direction.NORTH);
        BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1)).forEach((pos) -> this.level.setBlock(pos, blockstate, 18));
    }

}
