package com.ultra.megamod.feature.citizen.colonyblocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Creative-only block that acts as an anchor for decoration schematics.
 * Stores a tag position map and an optional "absorbed" block state.
 * When placed by a builder or pasted, this block disappears (replaced by air or the absorbed block).
 * Middle-click (pick block) on another block while holding this stores that block as the absorbed block.
 * Right-click displays current tags and absorbed block info.
 */
public class BlockTagAnchor extends Block implements EntityBlock {
    public static final MapCodec<BlockTagAnchor> CODEC = BlockTagAnchor.simpleCodec(BlockTagAnchor::new);

    public BlockTagAnchor(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BlockTagAnchor> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityTagAnchor(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEntityTagAnchor anchor) {
            // Display tag info
            Map<BlockPos, List<String>> tags = anchor.getTagPosMap();
            if (tags.isEmpty()) {
                player.displayClientMessage(Component.literal("Tag Anchor: No tags set."), false);
            } else {
                player.displayClientMessage(Component.literal("Tag Anchor: " + tags.size() + " tagged position(s)"), false);
                for (Map.Entry<BlockPos, List<String>> entry : tags.entrySet()) {
                    BlockPos tagPos = entry.getKey();
                    player.displayClientMessage(Component.literal(
                        "  [" + tagPos.getX() + ", " + tagPos.getY() + ", " + tagPos.getZ() + "] = " +
                            String.join(", ", entry.getValue())), false);
                }
            }

            // Display absorbed block info
            String absorbed = anchor.getAbsorbedBlock();
            if (absorbed != null && !absorbed.isEmpty()) {
                player.displayClientMessage(Component.literal("Absorbed block: " + absorbed), false);
            } else {
                player.displayClientMessage(Component.literal("No absorbed block."), false);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
