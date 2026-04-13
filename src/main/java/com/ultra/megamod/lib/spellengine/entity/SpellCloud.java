package com.ultra.megamod.lib.spellengine.entity;

import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.fx.ParticleHelper;
import com.ultra.megamod.lib.spellengine.utils.SoundPlayerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SpellCloud extends Entity implements OwnableEntity {
    public static EntityType<SpellCloud> ENTITY_TYPE;
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUuid;
    private int timeToLive;
    private int impactsPerformed = 0;
    private int impactCap = 0;
    private Identifier spellId;
    private int dataIndex = 0;
    private SpellHelper.ImpactContext context;

    public SpellCloud(EntityType<? extends SpellCloud> entityType, Level world) {
        super(entityType, world);
    }

    public SpellCloud(Level world) {
        super(ENTITY_TYPE, world);
        this.noPhysics = true;
    }

    public void onCreatedFromSpell(Identifier spellId, Spell.Delivery.Cloud cloudData, SpellHelper.ImpactContext context, float time_to_live_seconds) {
        this.spellId = spellId;
        this.context = context;

        var spellEntry = getSpellEntry();
        if (spellEntry != null) {
            var spell = spellEntry.value();
            var index = 0;
            var dataList = spell.deliver.clouds;
            if (!dataList.isEmpty()) {
                index = dataList.indexOf(cloudData);
            }
            this.dataIndex = index;
        }
        this.getEntityData().set(SPELL_ID_TRACKER, this.spellId.toString());
        this.getEntityData().set(DATA_INDEX_TRACKER, this.dataIndex);
        this.getEntityData().set(RADIUS_TRACKER, calculateRadius());

        this.timeToLive = (int) (time_to_live_seconds * 20);
        this.impactCap = cloudData.impact_cap;
    }

    private float calculateRadius() {
        var cloudData = getCloudData();
        if (cloudData != null) {
            var radius = cloudData.volume.radius;
            if (context != null) {
                radius = cloudData.volume.combinedRadius(context.power().baseValue());
            }
            return radius;
        } else {
            return 0F;
        }
    }

    public EntityDimensions getDimensions(Pose pose) {
        var cloudData = getCloudData();
        if (cloudData != null) {
            var radius = this.getEntityData().get(RADIUS_TRACKER);
            var heightMultiplier = cloudData.volume.area.vertical_range_multiplier;
            return EntityDimensions.scalable(radius * 2, radius * heightMultiplier);
        } else {
            return super.getDimensions(pose);
        }
    }

    // MARK: Owner

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
        this.ownerUuid = owner == null ? null : owner.getUUID();
    }

    @Nullable
    @Override
    public EntityReference<LivingEntity> getOwnerReference() {
        return this.ownerUuid != null ? EntityReference.of(this.ownerUuid) : null;
    }

    @Nullable
    public LivingEntity getOwnerEntity() {
        if (this.owner == null && this.ownerUuid != null && this.level() instanceof ServerLevel) {
            Entity entity = ((ServerLevel)this.level()).getEntity(this.ownerUuid);
            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity)entity;
            }
        }
        return this.owner;
    }

    // MARK: Sync

    private static final EntityDataAccessor<String> SPELL_ID_TRACKER  = SynchedEntityData.defineId(SpellCloud.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_INDEX_TRACKER = SynchedEntityData.defineId(SpellCloud.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RADIUS_TRACKER = SynchedEntityData.defineId(SpellCloud.class, EntityDataSerializers.FLOAT);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPELL_ID_TRACKER, "");
        builder.define(DATA_INDEX_TRACKER, this.dataIndex);
        builder.define(RADIUS_TRACKER, 0F);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if (this.level().isClientSide()) {
            var rawSpellId = this.getEntityData().get(SPELL_ID_TRACKER);
            if (rawSpellId != null && !rawSpellId.isEmpty()) {
                this.spellId = Identifier.parse(rawSpellId);
            }
            this.dataIndex = this.getEntityData().get(DATA_INDEX_TRACKER);
            this.refreshDimensions();
        }
    }

    // MARK: Persistence

    private enum NBTKey {
        AGE("Age"),
        TIME_TO_LIVE("TTL"),
        SPELL_ID("SpellId"),
        DATA_INDEX("DataIndex")
        ;

        public final String key;
        NBTKey(String key) {
            this.key = key;
        }
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        this.tickCount = input.getIntOr(NBTKey.AGE.key, 0);
        this.timeToLive = input.getIntOr(NBTKey.TIME_TO_LIVE.key, 0);
        this.spellId = Identifier.parse(input.getStringOr(NBTKey.SPELL_ID.key, ""));
        this.dataIndex = input.getIntOr(NBTKey.DATA_INDEX.key, 0);
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        output.putInt(NBTKey.AGE.key, this.tickCount);
        output.putInt(NBTKey.TIME_TO_LIVE.key, this.timeToLive);
        output.putString(NBTKey.SPELL_ID.key, this.spellId != null ? this.spellId.toString() : "");
        output.putInt(NBTKey.DATA_INDEX.key, this.dataIndex);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        return false; // SpellCloud is not damageable
    }

    // MARK: Behavior

    @Override
    public boolean isSilent() {
        return false;
    }
    private boolean presenceSoundFired = false;

    public void tick() {
        super.tick();
        var cloudData = this.getCloudData();
        if (cloudData == null) {
            return;
        }
        var world = this.level();
        if (world.isClientSide()) {
            // Client side tick
            var clientData = cloudData.client_data;
            var spawnParticles = clientData.particle_spawn_interval <= 1 || (this.tickCount % clientData.particle_spawn_interval) == 0;
            if (spawnParticles) {
                for (var particleBatch : clientData.interval_particles) {
                    ParticleHelper.play(world, this, particleBatch);
                }
            }
            for (var particleBatch : clientData.particles) {
                ParticleHelper.play(world, this, particleBatch);
            }

            var presence_sound = cloudData.presence_sound;
            if (!presenceSoundFired && presence_sound != null) {
                var soundEventOpt = BuiltInRegistries.SOUND_EVENT.get(Identifier.parse(presence_sound.id()));
                if (soundEventOpt.isPresent()) {
                    ((SoundPlayerWorld) world).playSoundFromEntity(this, soundEventOpt.get().value(), SoundSource.PLAYERS,
                            presence_sound.volume(),
                            presence_sound.randomizedPitch());
                    presenceSoundFired = true;
                } else {
                    System.out.println("SpellCloud: Failed to find presence sound " + presence_sound.id());
                }
            }

        } else {
            // Server side tick
            if (this.tickCount >= this.timeToLive
                    || (this.impactCap > 0 && this.impactsPerformed >= this.impactCap)) {
                this.discard();
                return;
            }
            if ((this.tickCount % cloudData.impact_tick_interval) == 0) {
                // Impact tick due
                var area_impact = cloudData.volume;
                var owner = this.getOwnerEntity();
                var spellEntry = getSpellEntry();
                if (area_impact != null && owner != null && spellEntry != null) {
                    var spell = spellEntry.value();
                    var context = this.context;
                    if (context == null) {
                        context = new SpellHelper.ImpactContext();
                    }
                    var performed = SpellHelper.lookupAndPerformAreaImpact(area_impact, spellEntry, owner,null,
                            this, spell.impacts, context.position(this.position()), true);
                    if (performed) {
                        ParticleHelper.play(world, this, cloudData.impact_particles);
                        this.impactsPerformed++;
                    }
                }
            }
        }
    }

    @Nullable public Spell.Delivery.Cloud getCloudData() {
        var spellEntry = getSpellEntry();
        if (spellEntry != null) {
            var spell = spellEntry.value();
            return spell.deliver.clouds.get(dataIndex);
        }
        return null;
    }

    @Nullable public Holder<Spell> getSpellEntry() {
        return SpellRegistry.from(this.level()).get(this.spellId).orElse(null);
    }
}
