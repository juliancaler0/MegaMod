package com.ultra.megamod.reliquary.block.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

abstract class BlockEntityBase extends BlockEntity {
	protected BlockEntityBase(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state) {
		super(tileEntityType, pos, state);
	}

	// Port note (1.21.11): without this override the initial chunk-load packet
	// contains only the BE type and an empty tag, so the Pedestal / Mortar /
	// Altar block-entities arrive on the client with no item data — the renderer
	// then has nothing to draw. Mirror the reference behaviour by re-serialising
	// saveAdditional into a CompoundTag through TagValueOutput.
	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		saveAdditional(output);
		return output.buildResult();
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ValueInput valueInput) {
		if (level == null) {
			super.onDataPacket(net, valueInput);
			return;
		}

		BlockState blockState = level.getBlockState(getBlockPos());
		loadWithComponents(valueInput);

		level.sendBlockUpdated(getBlockPos(), blockState, blockState, 3);
	}
}
