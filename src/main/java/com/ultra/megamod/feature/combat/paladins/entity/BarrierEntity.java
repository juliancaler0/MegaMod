package com.ultra.megamod.feature.combat.paladins.entity;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.paladins.PaladinsMod;
import com.ultra.megamod.feature.combat.paladins.content.PaladinSounds;
import com.ultra.megamod.lib.spellengine.api.entity.LivingEntityImmunity;
import com.ultra.megamod.lib.spellengine.api.entity.SpellEntity;
import com.ultra.megamod.lib.spellengine.api.entity.TwoWayCollisionChecker;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.internals.target.EntityRelations;
import com.ultra.megamod.lib.spellengine.utils.SoundPlayerWorld;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BarrierEntity extends Entity implements SpellEntity.Spawned {
    public static EntityType<BarrierEntity> TYPE;

    private Identifier spellId;
    private int ownerId;
    private int timeToLive = 20;

    public BarrierEntity(EntityType<? extends BarrierEntity> entityType, Level level) {
        super(entityType, level);
        ((TwoWayCollisionChecker)this).setReverseCollisionChecker(entity -> {
            return this.canCollideWith(entity)
                    ? TwoWayCollisionChecker.CollisionResult.COLLIDE
                    : TwoWayCollisionChecker.CollisionResult.PASS;
        });
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    @Override
    public void onSpawnedBySpell(Args args) {
        var owner = args.owner();
        var spellId = args.spell().getKey().identifier();
        var spawn = args.spawnData();
        this.spellId = spellId;
        this.getEntityData().set(SPELL_ID_TRACKER, this.spellId.toString());
        this.ownerId = owner.getId();
        this.getEntityData().set(OWNER_ID_TRACKER, this.ownerId);
        this.timeToLive = spawn.time_to_live_seconds * 20;
        this.getEntityData().set(TIME_TO_LIVE_TRACKER, this.timeToLive);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return this.isAlive();
    }

    @Override
    public boolean canCollideWith(Entity other) {
        var owner = this.getOwner();
        if (owner == null) {
            return super.canCollideWith(other);
        }
        if (other instanceof LivingEntity otherLiving) {
            return !isProtected(otherLiving);
        }
        return super.canCollideWith(other);
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        return super.hurtClient(source);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        this.level().playSound(null, this, PaladinSounds.holy_barrier_impact.soundEvent(), SoundSource.PLAYERS, 1F, 1F);
        return true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        var spellEntry = getSpellEntry();
        if (spellEntry != null) {
            var spell = spellEntry.value();
            var width = spell.range * 2;
            var height = spell.range;
            return EntityDimensions.scalable(width, height);
        } else {
            return super.getDimensions(pose);
        }
    }

    private static final EntityDataAccessor<String> SPELL_ID_TRACKER = SynchedEntityData.defineId(BarrierEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> OWNER_ID_TRACKER = SynchedEntityData.defineId(BarrierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TIME_TO_LIVE_TRACKER = SynchedEntityData.defineId(BarrierEntity.class, EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPELL_ID_TRACKER, "");
        builder.define(OWNER_ID_TRACKER, 0);
        builder.define(TIME_TO_LIVE_TRACKER, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        var rawSpellId = this.getEntityData().get(SPELL_ID_TRACKER);
        if (rawSpellId != null && !rawSpellId.isEmpty()) {
            this.spellId = Identifier.parse(rawSpellId);
        }
        this.timeToLive = this.getEntityData().get(TIME_TO_LIVE_TRACKER);
        this.refreshDimensions();
    }

    private enum NBTKey {
        OWNER_ID("OwnerId"),
        SPELL_ID("SpellId"),
        TIME_TO_LIVE("TTL"),
        ;

        public final String key;
        NBTKey(String key) {
            this.key = key;
        }
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        this.spellId = Identifier.parse(input.getStringOr(NBTKey.SPELL_ID.key, ""));
        this.ownerId = input.getIntOr(NBTKey.OWNER_ID.key, 0);
        this.timeToLive = input.getIntOr(NBTKey.TIME_TO_LIVE.key, 0);

        this.getEntityData().set(SPELL_ID_TRACKER, this.spellId.toString());
        this.getEntityData().set(OWNER_ID_TRACKER, this.ownerId);
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        output.putString(NBTKey.SPELL_ID.key, this.spellId != null ? this.spellId.toString() : "");
        output.putInt(NBTKey.OWNER_ID.key, this.ownerId);
        output.putInt(NBTKey.TIME_TO_LIVE.key, this.timeToLive);
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    private static final TagKey<DamageType> BARRIER_PROTECTS = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(MegaMod.MODID, "barrier_protects"));

    private boolean idleSoundFired = false;
    private static final int checkInterval = 4;

    @Override
    public void tick() {
        super.tick();
        var spellEntry = getSpellEntry();
        if (spellEntry == null) {
            return;
        }
        var spell = spellEntry.value();
        var level = this.level();
        if (level.isClientSide()) {
            // Client
            if (!idleSoundFired) {
                ((SoundPlayerWorld)level).playSoundFromEntity(this, PaladinSounds.holy_barrier_idle.soundEvent(), SoundSource.PLAYERS, 1F, 1F);
                idleSoundFired = true;
            }
        } else {
            // Server
            if (this.tickCount > this.timeToLive) {
                this.kill((ServerLevel) this.level());
            }
            if (this.tickCount % checkInterval == 0) {
                var entities = level.getEntities(this, this.getBoundingBox().inflate(0.1F));
                for (var entity : entities) {
                    if (entity instanceof LivingEntity livingEntity) {
                        if (isProtected(livingEntity)) {
                            LivingEntityImmunity.apply(livingEntity, null, BARRIER_PROTECTS, null, true, checkInterval + 1);
                        } else {
                            livingEntity.knockback(PaladinsMod.tweaksConfig.barrier_knockback_strength,
                                    this.getX() - livingEntity.getX(), this.getZ() - livingEntity.getZ());
                            if (livingEntity instanceof ServerPlayer serverPlayer) {
                                serverPlayer.connection.send(
                                        new ClientboundSetEntityMotionPacket(serverPlayer.getId(), serverPlayer.getDeltaMovement())
                                );
                            }
                        }
                    }
                }
            }
            if (this.tickCount == (this.timeToLive - expirationDuration())) {
                this.level().playSound(null, this, PaladinSounds.holy_barrier_deactivate.soundEvent(), SoundSource.PLAYERS, 1F, 1F);
            }
        }
    }

    public int expirationDuration() {
        return 20;
    }

    public boolean isExpiring() {
        return this.tickCount >= (this.timeToLive - expirationDuration());
    }

    public boolean isProtected(Entity other) {
        var owner = this.getOwner();
        if (owner == null) {
            return false;
        }
        var relation = EntityRelations.getRelation(owner, other);
        switch (relation) {
            case ALLY, FRIENDLY -> {
                return true;
            }
            case NEUTRAL, MIXED, HOSTILE -> {
                return false;
            }
        }
        return false;
    }

    @Nullable
    public Holder<Spell> getSpellEntry() {
        return SpellRegistry.from(this.level()).get(this.spellId).orElse(null);
    }

    private LivingEntity cachedOwner = null;
    @Nullable
    public LivingEntity getOwner() {
        if (cachedOwner != null) {
            return cachedOwner;
        }
        var owner = this.level().getEntity(this.ownerId);
        if (owner instanceof LivingEntity livingOwner) {
            cachedOwner = livingOwner;
            return livingOwner;
        }
        return null;
    }
}
