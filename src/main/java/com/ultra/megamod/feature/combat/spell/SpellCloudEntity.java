package com.ultra.megamod.feature.combat.spell;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Persistent AOE zone entity. Spawned by SpellExecutor for CLOUD delivery type spells.
 * Ticks every N ticks and applies spell impact to entities within radius.
 * Examples: Fire Wall, Entangling Roots, Battle Banner, Frost Blizzard.
 */
public class SpellCloudEntity extends Entity {

    /** Synched spell school ordinal so the client renderer can color/particle the cloud. */
    private static final EntityDataAccessor<Integer> DATA_SCHOOL =
            SynchedEntityData.defineId(SpellCloudEntity.class, EntityDataSerializers.INT);
    /** Synched radius so the client renderer knows how large to draw the disc. */
    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(SpellCloudEntity.class, EntityDataSerializers.FLOAT);

    private SpellDefinition spell;
    private UUID casterUuid;
    private int lifetimeTicks = 0;
    private int maxLifetimeTicks;
    private int impactInterval;
    private float radius;
    private Vec3 anchorPos;

    public SpellCloudEntity(EntityType<? extends SpellCloudEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SpellCloudEntity(Level level, ServerPlayer caster, SpellDefinition spell, Vec3 position) {
        super(CombatEntityRegistry.SPELL_CLOUD.get(), level);
        this.spell = spell;
        this.casterUuid = caster.getUUID();
        this.anchorPos = position;
        this.setPos(position.x, position.y, position.z);
        this.noPhysics = true;

        SpellDefinition.CloudConfig cloudConfig = spell.cloud();
        if (cloudConfig != null) {
            this.radius = cloudConfig.radius();
            this.maxLifetimeTicks = (int)(cloudConfig.timeToLiveSeconds() * 20);
            this.impactInterval = cloudConfig.impactIntervalTicks();
        } else {
            this.radius = 3;
            this.maxLifetimeTicks = 200;
            this.impactInterval = 20;
        }

        // Sync spell school and radius to clients for rendering
        this.entityData.set(DATA_SCHOOL, spell.school().ordinal());
        this.entityData.set(DATA_RADIUS, this.radius);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SCHOOL, 0);
        builder.define(DATA_RADIUS, 3.0f);
    }

    /** Client-side accessor: returns the spell school ordinal synched from the server. */
    public int getSchoolOrdinal() {
        return this.entityData.get(DATA_SCHOOL);
    }

    /** Client-side accessor: returns the cloud radius synched from the server. */
    public float getCloudRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    @Override
    public void tick() {
        super.tick();
        lifetimeTicks++;

        if (lifetimeTicks > maxLifetimeTicks) {
            discard();
            return;
        }

        if (!(level() instanceof ServerLevel serverLevel)) return;

        // Spawn particles
        if (lifetimeTicks % 5 == 0) {
            var particleType = spell != null ? switch (spell.school()) {
                case FIRE -> ParticleTypes.FLAME;
                case FROST -> ParticleTypes.SNOWFLAKE;
                case HEALING -> ParticleTypes.HAPPY_VILLAGER;
                default -> ParticleTypes.ENCHANT;
            } : ParticleTypes.ENCHANT;

            for (int i = 0; i < 5; i++) {
                double offsetX = (random.nextDouble() - 0.5) * radius * 2;
                double offsetZ = (random.nextDouble() - 0.5) * radius * 2;
                serverLevel.sendParticles(particleType,
                    anchorPos.x + offsetX, anchorPos.y + 0.3, anchorPos.z + offsetZ,
                    1, 0, 0.1, 0, 0);
            }
        }

        // Apply impact on interval
        if (lifetimeTicks % impactInterval == 0 && spell != null) {
            ServerPlayer caster = serverLevel.getServer().getPlayerList().getPlayer(casterUuid);
            if (caster == null) return;

            AABB box = new AABB(
                anchorPos.x - radius, anchorPos.y - 1, anchorPos.z - radius,
                anchorPos.x + radius, anchorPos.y + 3, anchorPos.z + radius
            );

            List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && e.distanceToSqr(anchorPos) <= radius * radius);

            boolean isHarmful = spell.damageCoefficient() > 0;
            for (LivingEntity entity : entities) {
                // Harmful clouds don't hit caster, beneficial ones do
                if (isHarmful && entity == caster) continue;
                if (!isHarmful && entity != caster && spell.healCoefficient() <= 0) continue;
                SpellExecutor.applyImpact(caster, entity, spell, serverLevel);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        // Clouds are transient — don't persist across restarts
        discard();
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        // No persistence
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false; // Clouds are invulnerable
    }
}
