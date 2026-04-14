package com.ultra.megamod.reliquary.block.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.ultra.megamod.reliquary.block.AlkahestryAltarBlock;
import com.ultra.megamod.reliquary.init.ModBlocks;
import com.ultra.megamod.reliquary.reference.Config;
import com.ultra.megamod.reliquary.util.WorldHelper;

public class AlkahestryAltarBlockEntity extends BlockEntityBase {
	private int cycleTime;
	private boolean isActive;
	private int redstoneCount;

	public AlkahestryAltarBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.ALKAHESTRY_ALTAR_TILE_TYPE.get(), pos, state);
		cycleTime = 0;
		redstoneCount = 0;
	}

	public void serverTick(Level level, BlockPos pos) {
		// TODO: 1.21.11 port - Level#isNight() removed; derive from day time (13000..23000 = night).
		long tod = level.getDayTime() % 24000L;
		boolean isNight = tod >= 13000L && tod <= 23000L;
		if (level.isClientSide() || !isActive || isNight || !level.canSeeSky(pos.above())) {
			return;
		}
		if (cycleTime > 0) {
			cycleTime--;
		} else {
			isActive = false;
			level.setBlockAndUpdate(pos.above(), Blocks.GLOWSTONE.defaultBlockState());
			AlkahestryAltarBlock.updateAltarBlockState(isActive(), level, pos);
		}
	}

	public void startCycle(Level level) {
		//grabs the cycle time from the configs
		int defaultCycleTime = Config.COMMON.blocks.altar.timeInMinutes.get() * 60 * 20;
		int maximumVariance = Config.COMMON.blocks.altar.maximumTimeVarianceInMinutes.get() * 60 * 20;
		cycleTime = (int) (defaultCycleTime + maximumVariance * level.random.nextGaussian());
		redstoneCount = 0;
		isActive = true;
	}

	public void stopCycle() {
		isActive = false;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		cycleTime = input.getShortOr("cycleTime", (short) 0);
		redstoneCount = input.getShortOr("redstoneCount", (short) 0);
		isActive = input.getBooleanOr("isActive", false);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putShort("cycleTime", (short) cycleTime);
		output.putShort("redstoneCount", (short) redstoneCount);
		output.putBoolean("isActive", isActive);
	}

	public void addRedstone(Level level, BlockPos pos) {
		redstoneCount++;
		if (redstoneCount >= getRedstoneCost()) {
			AlkahestryAltarBlock.updateAltarBlockState(true, level, pos);
		}
		WorldHelper.notifyBlockUpdate(this);
	}

	private static int getRedstoneCost() {
		return Config.COMMON.blocks.altar.redstoneCost.get();
	}

	public int getRedstoneCount() {
		return redstoneCount;
	}

	public boolean isActive() {
		return isActive;
	}

	public int getCycleTime() {
		return cycleTime;
	}
}
