package com.ultra.megamod.reliquary.block.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;

abstract class BlockEntityBase extends BlockEntity {
	protected BlockEntityBase(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state) {
		super(tileEntityType, pos, state);
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
