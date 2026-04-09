package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

import static com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen.DATA_BED_POS;
import static com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen.DATA_IS_ASLEEP;

/**
 * Handles the sleep of the MC citizen.
 * Ported from MineColonies' CitizenSleepHandler.
 */
public class CitizenSleepHandler implements ICitizenSleepHandler {

    private static final double HALF_BLOCK = 0.5D;
    private static final long NIGHT = 12500L;

    private final MCEntityCitizen citizen;

    public CitizenSleepHandler(MCEntityCitizen citizen) {
        this.citizen = citizen;
    }

    @Override
    public boolean isAsleep() {
        return citizen.getEntityData().get(DATA_IS_ASLEEP);
    }

    private void setIsAsleep(boolean isAsleep) {
        citizen.getEntityData().set(DATA_IS_ASLEEP, isAsleep);
    }

    @Override
    public boolean trySleep(BlockPos bedLocation) {
        BlockState state = citizen.level().getBlockState(bedLocation);
        boolean isBed = state.is(BlockTags.BEDS);

        if (!isBed) {
            return false;
        }

        citizen.setPose(Pose.SLEEPING);
        citizen.getNavigation().stop();

        double zOffset = state.getValue(BedBlock.FACING).getAxis() == Direction.Axis.Z ? 0 : HALF_BLOCK;
        double xOffset = state.getValue(BedBlock.FACING).getAxis() == Direction.Axis.X ? 0 : HALF_BLOCK;

        citizen.setPos(
                bedLocation.getX() + xOffset,
                bedLocation.getY() + 0.6875D,
                bedLocation.getZ() + zOffset);
        citizen.setSleepingPos(bedLocation);

        citizen.setDeltaMovement(Vec3.ZERO);

        setIsAsleep(true);
        citizen.getEntityData().set(DATA_BED_POS, bedLocation);

        return true;
    }

    @Override
    public void onWakeUp() {
        if (isAsleep()) {
            spawnCitizenFromBed();
        }

        citizen.setPose(Pose.STANDING);
        citizen.clearSleepingPos();
        setIsAsleep(false);
    }

    private void spawnCitizenFromBed() {
        BlockPos bedLoc = getBedLocation();
        BlockPos spawn;

        if (!bedLoc.equals(BlockPos.ZERO)) {
            BlockState bedState = citizen.level().getBlockState(bedLoc);
            if (bedState.is(BlockTags.BEDS)) {
                if (bedState.getValue(BedBlock.PART) == BedPart.HEAD) {
                    BlockPos relPos = bedLoc.relative(bedState.getValue(BedBlock.FACING).getOpposite());
                    spawn = relPos.above();
                } else {
                    spawn = bedLoc.above();
                }
            } else {
                spawn = citizen.blockPosition();
            }
        } else {
            spawn = citizen.blockPosition();
        }

        if (!spawn.equals(BlockPos.ZERO)) {
            citizen.setPos(spawn.getX() + HALF_BLOCK, spawn.getY(), spawn.getZ() + HALF_BLOCK);
        }

        setIsAsleep(false);
        citizen.getEntityData().set(DATA_BED_POS, BlockPos.ZERO);
    }

    @Override
    public BlockPos getBedLocation() {
        return citizen.getEntityData().get(DATA_BED_POS);
    }

    @Override
    public boolean shouldGoSleep() {
        // Sleep at night time
        long dayTime = citizen.level().getDayTime() % 24000;
        return dayTime >= NIGHT;
    }
}
