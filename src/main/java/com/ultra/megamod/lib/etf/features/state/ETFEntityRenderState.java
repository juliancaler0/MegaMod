package com.ultra.megamod.lib.etf.features.state;

import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.utils.ETFEntity;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Per-frame snapshot of an entity's state, as consumed by random-property predicates.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben). The snapshot is typically a thin
 * wrapper over an {@link ETFEntity}, but downstream consumers (EMF) may replace the
 * factory to cache values more aggressively.
 */
public interface ETFEntityRenderState {

    UUID uuid();
    boolean canRenderBright();
    boolean isBlockEntity();
    EntityType<?> entityType();
    Level world();
    BlockPos blockPos();
    int optifineId();
    int optifineVehicleId();
    int blockY();
    CompoundTag nbt();
    boolean hasCustomName();
    Component customName();
    Team scoreboardTeam();
    Iterable<ItemStack> itemsEquipped();
    Iterable<ItemStack> handItems();
    Iterable<ItemStack> armorItems();
    Vec3 velocity();

    @Deprecated
    Pose pose();

    String entityKey();

    @Deprecated // upstream deprecation — use ETFEntityRenderState accessors
    ETFEntity entity();

    @Nullable EntityRenderState vanillaState();
    void setVanillaState(EntityRenderState vanillaState);

    @Nullable BlockEntityRenderState vanillaBlockState();
    void setVanillaBlockState(BlockEntityRenderState vanillaState);

    float distanceTo(Entity entity);


    interface ETFRenderStateInit {
        ETFEntityRenderState make(ETFEntity entity);
    }

    static void setEtfRenderStateConstructor(String reason, ETFRenderStateInit init) {
        ETFUtils2.logMessage("Modifying ETF Render State constructor because: " + reason);
        ETF.etfRenderStateConstructor = init;
    }

    static ETFEntityRenderState forEntity(ETFEntity entity) {
        return ETF.etfRenderStateConstructor.make(entity);
    }
}
