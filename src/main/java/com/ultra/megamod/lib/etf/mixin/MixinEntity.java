package com.ultra.megamod.lib.etf.mixin;

import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.utils.ETFEntity;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Implements {@link ETFEntity} on vanilla {@link Entity} so the predicate layer can
 * read snapshot state without a per-frame allocation.
 * <p>
 * Ported 1:1 from upstream ETF. Only the 1.21.11 (MC &gt;= 12105 / 12103) branches are kept.
 */
@Mixin(Entity.class)
public abstract class MixinEntity implements ETFEntity {

    @Shadow public abstract EntityType<?> getType();
    @Shadow public abstract int getBlockY();
    @Shadow public abstract boolean hasCustomName();
    @Shadow @Nullable public abstract Component getCustomName();
    @Shadow public abstract float distanceTo(Entity entity);
    @Shadow public abstract Pose getPose();
    @Shadow public abstract UUID getUUID();
    @Shadow public abstract Level level();
    @Shadow public abstract BlockPos blockPosition();
    @Shadow @Nullable public abstract PlayerTeam getTeam();
    @Shadow public abstract Vec3 getDeltaMovement();
    @Shadow @Nullable public abstract Entity getVehicle();

    @Override
    public EntityType<?> etf$getType() {
        return getType();
    }

    @Override
    public UUID etf$getUuid() {
        return getUUID();
    }

    @Override
    public int etf$getOptifineId() {
        return (int) (etf$getUuid().getLeastSignificantBits() & 0x7FFFFFFFL);
    }

    @Override
    public int etf$getOptifineVehicleId() {
        return getVehicle() == null
                ? etf$getOptifineId()
                : ((ETFEntity) getVehicle()).etf$getOptifineId();
    }

    @Override
    public Level etf$getWorld() {
        return level();
    }

    @Override
    public BlockPos etf$getBlockPos() {
        return blockPosition();
    }

    @Override
    public int etf$getBlockY() {
        return getBlockY();
    }

    @Override
    public CompoundTag etf$getNbt() {
        return ETFRenderContext.cacheEntityNBTForFrame(etf$getUuid(),
                () -> NbtPredicate.getEntityTagToCompare((Entity) (Object) this));
    }

    @Override
    public boolean etf$hasCustomName() {
        return hasCustomName();
    }

    @Override
    public Component etf$getCustomName() {
        return getCustomName();
    }

    @Override
    public Team etf$getScoreboardTeam() {
        return getTeam();
    }

    @Override
    public Iterable<ItemStack> etf$getItemsEquipped() {
        LivingEntity alive = etf$getLivingOrNull();
        if (alive != null) {
            return alive.lastEquipmentItems.values();
        }
        return null;
    }

    @Override
    public Iterable<ItemStack> etf$getHandItems() {
        LivingEntity alive = etf$getLivingOrNull();
        if (alive != null) {
            var equipment = alive.lastEquipmentItems;
            var list = new ArrayList<ItemStack>();
            for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
                if (entry.getKey().getType() == EquipmentSlot.Type.HAND) {
                    list.add(entry.getValue());
                }
            }
            return list;
        }
        return null;
    }

    @Override
    public Iterable<ItemStack> etf$getArmorItems() {
        LivingEntity alive = etf$getLivingOrNull();
        if (alive != null) {
            var equipment = alive.lastEquipmentItems;
            var list = new ArrayList<ItemStack>();
            for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
                if (entry.getValue() != null
                        && (entry.getKey().getType() == EquipmentSlot.Type.HUMANOID_ARMOR
                        || entry.getKey().getType() == EquipmentSlot.Type.ANIMAL_ARMOR)) {
                    list.add(entry.getValue());
                }
            }
            return list;
        }
        return null;
    }

    @Unique
    private LivingEntity etf$getLivingOrNull() {
        Object self = this;
        if (self instanceof LivingEntity alive) {
            return alive;
        }
        return null;
    }

    @Override
    public float etf$distanceTo(Entity entity) {
        return distanceTo(entity);
    }

    @Override
    public Vec3 etf$getVelocity() {
        return getDeltaMovement();
    }

    @Override
    public Pose etf$getPose() {
        return getPose();
    }

    @Override
    public boolean etf$canBeBright() {
        Object self = this;
        return self instanceof Player;
    }

    @Override
    public boolean etf$isBlockEntity() {
        return false;
    }

    @Override
    public String etf$getEntityKey() {
        return getType().getDescriptionId();
    }
}
