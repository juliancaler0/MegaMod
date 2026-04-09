package com.ultra.megamod.feature.furniture;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A BlockItem that offsets the placement position so multi-block furniture
 * models render correctly. Moves placement 1 block up and 1 block in the
 * facing direction (away from the wall the player clicked on).
 */
public class OffsetBlockItem extends BlockItem {

    public OffsetBlockItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        if (context.getPlayer() == null) return context;

        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos originalPos = context.getClickedPos();

        // Offset: 1 block up (model extends 1 block below origin)
        // and 1 block in facing direction (away from clicked wall, so model back is against wall)
        BlockPos offsetPos = originalPos.above(1).relative(facing, 1);

        // Check if the offset position is free
        BlockState stateAt = context.getLevel().getBlockState(offsetPos);
        if (!stateAt.canBeReplaced(context)) {
            return null; // Can't place — offset position blocked
        }

        BlockHitResult offsetHit = new BlockHitResult(
                Vec3.atCenterOf(offsetPos),
                context.getClickedFace(),
                offsetPos,
                context.isInside()
        );

        return new BlockPlaceContext(
                context.getLevel(), context.getPlayer(),
                context.getHand(), context.getItemInHand(),
                offsetHit
        );
    }
}
