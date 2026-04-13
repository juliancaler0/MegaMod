package com.ultra.megamod.lib.etf.mixin;

import com.ultra.megamod.lib.etf.ETFApi;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.utils.ETFEntity;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

/**
 * Implements {@link ETFEntity} on vanilla {@link BlockEntity}.
 * <p>
 * Ported 1:1 from upstream ETF, 1.21.11 (MC &gt;= 12106) branch only. The 1.21.11
 * {@code saveMetadata} takes a {@link ValueOutput} rather than {@link CompoundTag}.
 */
@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements ETFEntity {

    @Unique
    private UUID etf$id = null;

    @Shadow public abstract BlockEntityType<?> getType();
    @Shadow public abstract BlockPos getBlockPos();
    @Shadow @Nullable public abstract Level getLevel();
    @Shadow public abstract void saveWithFullMetadata(ValueOutput valueOutput);

    @Override
    public EntityType<?> etf$getType() {
        return null;
    }

    @Override
    public UUID etf$getUuid() {
        if (etf$id == null) etf$id = ETFApi.getUUIDForBlockEntity((BlockEntity) (Object) this);
        return etf$id;
    }

    @Override
    public int etf$getOptifineId() {
        BlockPos pos = etf$getBlockPos();
        int hash = ETFUtils2.optifineHashing(37);
        hash = ETFUtils2.optifineHashing(hash + pos.getX());
        hash = ETFUtils2.optifineHashing(hash + pos.getZ());
        return ETFUtils2.optifineHashing(hash + pos.getY());
    }

    @Override
    public int etf$getOptifineVehicleId() {
        return etf$getOptifineId();
    }

    @Override
    public Level etf$getWorld() {
        return getLevel();
    }

    @Override
    public BlockPos etf$getBlockPos() {
        BlockEntity self = (BlockEntity) (Object) this;
        if (self instanceof BedBlockEntity bed) {
            var state = bed.getBlockState();
            if (state.getValue(BedBlock.PART) == BedPart.HEAD) {
                return getBlockPos().relative(state.getValue(BedBlock.FACING));
            }
        }
        return getBlockPos();
    }

    @Override
    public int etf$getBlockY() {
        return getBlockPos().getY();
    }

    @Override
    public CompoundTag etf$getNbt() {
        return ETFRenderContext.cacheEntityNBTForFrame(etf$getUuid(),
                () -> {
                    TagValueOutput compound = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
                    // upstream calls private saveMetadata; saveWithFullMetadata is the public
                    // wrapper that runs saveWithoutMetadata + saveMetadata — same payload.
                    saveWithFullMetadata(compound);
                    return compound.buildResult();
                });
    }

    @Override
    public boolean etf$hasCustomName() {
        return this instanceof Nameable name && name.hasCustomName();
    }

    @Override
    public Component etf$getCustomName() {
        return this instanceof Nameable name && name.hasCustomName()
                ? name.getCustomName()
                : Component.nullToEmpty("null");
    }

    @Override
    public Team etf$getScoreboardTeam() {
        return null;
    }

    @Override
    public Iterable<ItemStack> etf$getItemsEquipped() {
        return null;
    }

    @Override
    public Iterable<ItemStack> etf$getHandItems() {
        return null;
    }

    @Override
    public Iterable<ItemStack> etf$getArmorItems() {
        return null;
    }

    @Override
    public float etf$distanceTo(Entity entity) {
        BlockPos pos = getBlockPos();
        float f = (float) (pos.getX() - entity.getX());
        float g = (float) (pos.getY() - entity.getY());
        float h = (float) (pos.getZ() - entity.getZ());
        return Mth.sqrt(f * f + g * g + h * h);
    }

    @Override
    public Vec3 etf$getVelocity() {
        return Vec3.ZERO;
    }

    @Override
    public Pose etf$getPose() {
        return Pose.STANDING;
    }

    @Override
    public boolean etf$canBeBright() {
        return false;
    }

    @Override
    public boolean etf$isBlockEntity() {
        return true;
    }

    @Override
    public String etf$getEntityKey() {
        return ETFApi.getBlockEntityTypeToTranslationKey(getType());
    }
}
