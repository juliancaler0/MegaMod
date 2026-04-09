package com.ultra.megamod.feature.combat.spell;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Spell projectile entity. Travels in a direction and applies spell impact on hit.
 * Spawned by SpellExecutor for PROJECTILE delivery type spells.
 */
public class SpellProjectileEntity extends ThrowableProjectile {

    /** Synched spell school ordinal so the client renderer can color/particle the projectile. */
    private static final EntityDataAccessor<Integer> DATA_SCHOOL =
            SynchedEntityData.defineId(SpellProjectileEntity.class, EntityDataSerializers.INT);
    /** Synched scale multiplier (1-3) derived from spell tier. */
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(SpellProjectileEntity.class, EntityDataSerializers.FLOAT);

    private SpellDefinition spell;
    private int pierceCount = 0;
    private int maxPierce = 0;
    private int lifetimeTicks = 0;
    private static final int MAX_LIFETIME = 200; // 10 seconds

    /** Previous frame velocity for smooth motion interpolation in renderer. */
    public Vec3 previousVelocity;

    /**
     * Render data from spell visuals — cached at spawn for client access.
     * Contains modelId, scale, orientation, rotation speed, light emission.
     */
    public SpellDefinition.SpellVisuals renderVisuals;

    public SpellProjectileEntity(EntityType<? extends SpellProjectileEntity> type, Level level) {
        super(type, level);
    }

    public SpellProjectileEntity(Level level, ServerPlayer owner, SpellDefinition spell) {
        super(CombatEntityRegistry.SPELL_PROJECTILE.get(), level);
        this.setOwner(owner);
        this.spell = spell;
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        if (spell.projectile() != null) {
            this.maxPierce = spell.projectile().pierce();
        }
        // Sync spell school and scale to clients for rendering
        this.entityData.set(DATA_SCHOOL, spell.school().ordinal());
        this.entityData.set(DATA_SCALE, Math.max(0.5f, 0.5f + spell.tier() * 0.25f));
        this.renderVisuals = spell.visuals();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SCHOOL, 0);
        builder.define(DATA_SCALE, 1.0f);
    }

    /** Client-side accessor: returns the spell school ordinal synched from the server. */
    public int getSchoolOrdinal() {
        return this.entityData.get(DATA_SCHOOL);
    }

    /** Client-side accessor: returns the render scale synched from the server. */
    public float getRenderScale() {
        return this.entityData.get(DATA_SCALE);
    }

    @Override
    public void tick() {
        // Capture velocity before physics for smooth rendering interpolation
        previousVelocity = getDeltaMovement();
        super.tick();
        lifetimeTicks++;
        if (lifetimeTicks > MAX_LIFETIME) {
            discard();
            return;
        }

        // Trail particles (client-side handled by renderer, but server can send particles too)
        if (level() instanceof ServerLevel serverLevel && spell != null) {
            int color = spell.school().color;
            // Use colored particles based on school
            var particleType = switch (spell.school()) {
                case FIRE -> ParticleTypes.FLAME;
                case FROST -> ParticleTypes.SNOWFLAKE;
                case ARCANE -> ParticleTypes.ENCHANT;
                case HEALING -> ParticleTypes.HAPPY_VILLAGER;
                case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
                case SOUL -> ParticleTypes.SOUL;
                default -> ParticleTypes.END_ROD;
            };
            serverLevel.sendParticles(particleType, getX(), getY(), getZ(), 2, 0.1, 0.1, 0.1, 0);
        }

        // Homing behavior
        if (spell != null && spell.projectile() != null && spell.projectile().homingAngle() > 0) {
            applyHoming();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide() || spell == null) return;
        if (!(result.getEntity() instanceof LivingEntity target)) return;
        if (target == getOwner()) return;

        if (getOwner() instanceof ServerPlayer caster) {
            SpellExecutor.applyImpact(caster, target, spell, (ServerLevel) level());
        }

        pierceCount++;
        if (pierceCount >= maxPierce && maxPierce < 99000) {
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (spell != null && spell.projectile() != null && spell.projectile().bounce() > 0) {
            // Bounce off the block
            Vec3 motion = getDeltaMovement();
            Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getUnitVec3i());
            Vec3 reflected = motion.subtract(normal.scale(2 * motion.dot(normal)));
            setDeltaMovement(reflected.scale(0.8));
            return;
        }

        // Area impact on block hit (for meteor-style spells)
        if (spell != null && spell.area() != null && getOwner() instanceof ServerPlayer caster) {
            SpellDefinition areaSpell = new SpellDefinition(
                spell.id() + "_impact", spell.name(), spell.school(), spell.tier(),
                SpellDefinition.CastMode.INSTANT, 0, SpellDefinition.DeliveryType.AREA,
                SpellDefinition.TargetType.AREA, spell.area().horizontalRange(),
                spell.damageCoefficient(), spell.healCoefficient(), 0, spell.knockback(),
                spell.effects(), spell.area(), null, null, null, null
            );
            // Execute area effect at impact point
            Vec3 hitPos = result.getLocation();
            ServerLevel serverLevel = (ServerLevel) level();
            for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class,
                    new net.minecraft.world.phys.AABB(hitPos.x - spell.area().horizontalRange(),
                        hitPos.y - spell.area().verticalRange(), hitPos.z - spell.area().horizontalRange(),
                        hitPos.x + spell.area().horizontalRange(), hitPos.y + spell.area().verticalRange(),
                        hitPos.z + spell.area().horizontalRange()),
                    e -> e.isAlive() && e != caster)) {
                SpellExecutor.applyImpact(caster, entity, spell, serverLevel);
            }
        }

        discard();
    }

    private void applyHoming() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        float homingAngle = spell.projectile().homingAngle();
        Vec3 motion = getDeltaMovement();
        double speed = motion.length();
        if (speed < 0.01) return;

        // Find nearest target in front
        LivingEntity nearest = null;
        double nearestDist = spell.range() * spell.range();
        Vec3 pos = position();

        for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(spell.range()), e -> e != getOwner() && e.isAlive())) {
            double dist = pos.distanceToSqr(entity.position());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = entity;
            }
        }

        if (nearest != null) {
            Vec3 toTarget = nearest.position().add(0, nearest.getBbHeight() * 0.5, 0).subtract(pos).normalize();
            Vec3 currentDir = motion.normalize();
            double dot = currentDir.dot(toTarget);
            double angleRad = Math.toRadians(homingAngle);
            if (dot > Math.cos(angleRad)) {
                Vec3 newDir = currentDir.lerp(toTarget, 0.1).normalize();
                setDeltaMovement(newDir.scale(speed));
            }
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01; // Minimal gravity for spell projectiles
    }
}
