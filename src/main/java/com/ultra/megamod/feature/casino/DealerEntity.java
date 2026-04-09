package com.ultra.megamod.feature.casino;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Casino dealer NPC. Uses villager model visually but has no trading menu.
 * Stands in place, looks at nearby players, and does nothing on interaction.
 */
public class DealerEntity extends PathfinderMob {
    private static final EntityDataAccessor<VillagerData> DATA_VILLAGER =
            SynchedEntityData.defineId(DealerEntity.class, EntityDataSerializers.VILLAGER_DATA);

    public DealerEntity(EntityType<? extends DealerEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
        this.setPersistenceRequired();
        this.setCustomNameVisible(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VILLAGER, new VillagerData(
                (Holder) BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS),
                (Holder) BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.LIBRARIAN),
                5
        ));
    }

    public VillagerData getVillagerData() {
        return this.entityData.get(DATA_VILLAGER);
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 12.0f));
    }

    @Override
    public void travel(Vec3 movementInput) {
        super.travel(Vec3.ZERO);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Do nothing on interaction - no trading menu
        return InteractionResult.PASS;
    }

    public static AttributeSupplier.Builder createDealerAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }
}
