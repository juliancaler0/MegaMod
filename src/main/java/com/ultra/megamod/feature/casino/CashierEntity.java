package com.ultra.megamod.feature.casino;

import com.ultra.megamod.feature.casino.chips.ChipActionPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

/**
 * Casino Cashier NPC — extends PathfinderMob (NOT Villager) to avoid trade GUI.
 * Uses villager model via DealerRenderer. Right-click sends chip sync to client which opens CashierScreen.
 */
public class CashierEntity extends PathfinderMob {
    private static final EntityDataAccessor<VillagerData> DATA_VILLAGER =
            SynchedEntityData.defineId(CashierEntity.class, EntityDataSerializers.VILLAGER_DATA);

    public CashierEntity(EntityType<? extends CashierEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
        this.setPersistenceRequired();
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createCashierAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0); // doesn't move
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VILLAGER, new VillagerData(
                (Holder) BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS),
                (Holder) BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.LIBRARIAN),
                5));
    }

    public VillagerData getVillagerData() {
        return this.entityData.get(DATA_VILLAGER);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, 8.0f));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        // Server side: send chip sync — client opens CashierScreen when it receives the sync
        if (player instanceof ServerPlayer sp) {
            ServerLevel overworld = sp.level().getServer().overworld();
            EconomyManager eco = EconomyManager.get(overworld);
            ChipActionPayload.sendChipSync(sp, eco, true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false; // invulnerable
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }
    @Override
    public boolean canBeLeashed() { return false; }
    @Override
    public boolean isPushable() { return false; }
    @Override
    public void push(double x, double y, double z) {} // can't be pushed
}
