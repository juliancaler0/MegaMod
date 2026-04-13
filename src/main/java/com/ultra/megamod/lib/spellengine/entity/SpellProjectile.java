package com.ultra.megamod.lib.spellengine.entity;

import com.google.gson.Gson;
import net.minecraft.world.entity.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.entity.TwoWayCollisionChecker;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.client.render.FlyingSpellEntity;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.target.EntityRelations;
import com.ultra.megamod.lib.spellengine.internals.target.SpellTarget;
import com.ultra.megamod.lib.spellengine.fx.ParticleHelper;
import com.ultra.megamod.lib.spellengine.utils.SoundHelper;
import com.ultra.megamod.lib.spellengine.utils.VectorHelper;
import com.ultra.megamod.lib.spellpower.api.SpellPower;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import com.ultra.megamod.lib.spellengine.internals.melee.OrientedBoundingBox;

public class SpellProjectile extends Projectile implements FlyingSpellEntity {
    public static EntityType<SpellProjectile> ENTITY_TYPE;
    private static Random random = new Random();

    public float range = 128;
    private Spell.ProjectileData.Perks perks;
    private SpellHelper.ImpactContext context;
    public Vec3 previousVelocity;
    private double distanceTraveled = 0;

    public SpellProjectile(EntityType<? extends Projectile> entityType, Level world) {
        super(entityType, world);
    }

    protected SpellProjectile(Level world, LivingEntity owner) {
        super(ENTITY_TYPE, world);
        this.setOwner(owner);
    }

    public enum Behaviour {
        FLY, FALL
    }

    public SpellProjectile(Level world, LivingEntity caster, double x, double y, double z,
                           Behaviour behaviour, Holder<Spell> spellEntry, SpellHelper.ImpactContext context, Spell.ProjectileData.Perks mutablePerks) {
        this(world, caster);
        this.setPos(x, y, z);

        this.setBehaviour(behaviour);
        this.setSpell(spellEntry);
        this.perks = mutablePerks;
        this.context = context;

        var projectileData = projectileData();
        if (projectileData.client_data != null && projectileData.client_data.model != null) {
            var model = projectileData.client_data.model;
            if (model.use_held_item) {
                setItemStackModel(caster.getMainHandItem());
            }
        }
    }

    /**
     * A copy of the spell projectile perks, can be safely modified
      */
    public Spell.ProjectileData.Perks mutablePerks() {
        return perks;
    }

    public Spell.ProjectileData projectileData() {
        var spellEntry = getSpellEntry();
        if (spellEntry == null) {
            return null;
        }
        var spell = getSpellEntry().value();
        var release = spell.deliver;
        switch (release.type) {
            case PROJECTILE -> {
                return release.projectile.projectile;
            }
            case METEOR -> {
                return release.meteor.projectile;
            }
        }
        assert true;
        return null;
    }

    public void setVelocity(double x, double y, double z, float speed, float spread, float divergence) {
        var rotX = Math.toRadians(divergence * random.nextFloat(spread, 1F));
        var rotY = Math.toRadians(360 * random.nextFloat());
        Vec3 vec3d = (new Vec3(x, y, z))
                .xRot((float) rotX)
                .yRot((float) rotY)
                .scale(speed);
        this.setDeltaMovement(vec3d);
        double d = vec3d.horizontalDistance();
        this.setYRot((float)(Mth.atan2(vec3d.x, vec3d.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2(vec3d.y, d) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    private boolean hasCustomDimensions = false;
    public EntityDimensions getDimensions(Pose pose) {
        var data = projectileData();
        if (data != null && data.hitbox != null) {
            this.hasCustomDimensions = true;
            var width = data.hitbox.width;
            var height = data.hitbox.height;
            return EntityDimensions.scalable(width, height);
        } else {
            return super.getDimensions(pose);
        }
    }

    public Entity getFollowedTarget() {
        Entity entityReference = null;
        if (this.level().isClientSide()) {
            var id = this.getEntityData().get(TRACKER_TARGET_ID);
            if (id != null && id > 0) {
                entityReference = this.level().getEntity(id);
            }
        } else {
            entityReference = followedTarget;
        }
        if (entityReference != null && entityReference.isAttackable() && entityReference.isAlive()) {
            return entityReference;
        }
        return entityReference;
    }

//    @Override
//    public void setVelocityClient(double x, double y, double z) {
//        super.setVelocityClient(x, y, z);
//    }

    public boolean shouldRender(double distance) {
        double d0 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(d0)) {
            d0 = 4.0;
        }

        d0 *= 128.0;
        var result =  distance < d0 * d0;
        return result;
    }

    private boolean skipTravel = false;

    public void tick() {
        skipTravel = false;
        Entity entity = this.getOwner();
        var behaviour = getBehaviour();
        var spellEntry = getSpellEntry();
        if (!this.level().isClientSide()) {
            // Server side
            if (spellEntry == null) {
                System.err.println("Spell Projectile safeguard termination, failed to resolve spell: " + spellId());
                this.kill((ServerLevel) this.level());
                return;
            }
            switch (behaviour) {
                case FLY -> {
                    if (distanceTraveled >= range || tickCount> 1200) { // 1200 ticks = 1 minute
                        this.kill((ServerLevel) this.level());
                        return;
                    }
                }
                case FALL -> {
                    if (distanceTraveled >= (range * 0.98)) {
                        finishFalling();
                        this.kill((ServerLevel) this.level());
                        return;
                    }
                    if (tickCount > 1200) { // 1200 ticks = 1 minute
                        this.kill((ServerLevel) this.level());
                        return;
                    }
                }
            }
            if (distanceTraveled >= range || tickCount> 1200) { // 1200 ticks = 1 minute
                this.kill((ServerLevel) this.level());
                return;
            }
        }
        this.previousVelocity = new Vec3(getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z);
        if (this.level().isClientSide() || (entity == null || !entity.isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
            super.tick();

            if (!this.level().isClientSide()) {
                HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
                var data = projectileData();
                if (data != null && data.hitbox != null) {
                    if (hitResult.getType() == HitResult.Type.BLOCK) {
                        // Block hit: handle normally (preserves bounce/block-impact)
                        handleHitResult(hitResult, behaviour, spellEntry);
                    } else {
                        // Entity/miss from raycast: discard, use swept OBB detection instead
                        performVolumetricEntityCollision(behaviour, spellEntry, data);
                    }
                } else {
                    // RAYCAST mode (default) — original path with intersects bug fixed
                    handleHitResult(hitResult, behaviour, spellEntry);
                    if (hitResult.getType() == HitResult.Type.MISS && hasCustomDimensions) {
                        var boundingBox = this.getBoundingBox();
                        for (Entity areaTarget : this.level().getEntities(entity, this.getBoundingBox().inflate(1), this::canHitEntity)) {
                            if (areaTarget.getBoundingBox().intersects(boundingBox)) {
                                var areaHitResult = new EntityHitResult(areaTarget);
                                handleHitResult(areaHitResult, behaviour, spellEntry);
                            }
                        }
                    }
                }
            }

            // Block collision check handled by entity movement

            // Travel
            if (!skipTravel) {
                this.followTarget();
                Vec3 velocity = this.getDeltaMovement();
                double d = this.getX() + velocity.x;
                double e = this.getY() + velocity.y;
                double f = this.getZ() + velocity.z;
                setRotFromVelocity(this);

                float g = this.getDrag();
                if (this.isInWater()) {
                    for(int i = 0; i < 4; ++i) {
                        float h = 0.25F;
                        this.level().addParticle(ParticleTypes.BUBBLE, d - velocity.x * 0.25, e - velocity.y * 0.25, f - velocity.z * 0.25, velocity.x, velocity.y, velocity.z);
                    }
                    g = 0.8F;
                }

                var data = projectileData();
                if (data != null) {
                    if (this.level().isClientSide()) {
                        for (var travel_particles : data.client_data.travel_particles) {
                            ParticleHelper.play(this.level(), this, getYRot(), getXRot(), travel_particles);
                        }
                    } else {
                        if (data.travel_sound != null && tickCount% data.travel_sound_interval == 0) {
                            SoundHelper.playSound(this.level(), this, data.travel_sound);
                        }
                    }
                }

                this.setPos(d, e, f);
                this.distanceTraveled += velocity.length();
            }
        } else {
            this.discard();
        }
    }

    private void handleHitResult(HitResult hitResult, Behaviour behaviour, Holder<Spell> spellEntry) {
        if (hitResult.getType() != HitResult.Type.MISS) {
            switch (behaviour) {
                case FLY -> {
                    boolean shouldCollideWithEntity = true;
                    if (hitResult.getType() == HitResult.Type.ENTITY) {
                        var target = ((EntityHitResult) hitResult).getEntity();
                        var spell = spellEntry.value();
                        if (SpellEngineMod.config.projectiles_pass_thru_irrelevant_targets
                                && spell != null
                                && !spell.impacts.isEmpty()
                                && !impactHistory.contains(target.getId())
                                && getOwner() instanceof LivingEntity owner) {
                            var intents = SpellHelper.impactIntents(spell);

                            boolean intentAllows = false;
                            for (var intent: intents) {
                                intentAllows = intentAllows || EntityRelations.actionAllowed(SpellTarget.FocusMode.DIRECT, intent, owner, target);
                            }
                            shouldCollideWithEntity = intentAllows;
                        }
                    }
                    if (shouldCollideWithEntity) {
                        this.onHit(hitResult);
                    } else {
                        this.setFollowedTarget(null);
                    }
                }
                case FALL -> {
                    if (hitResult.getType() == HitResult.Type.ENTITY) {
                        var target = ((EntityHitResult) hitResult).getEntity();
                        var reverse = ((TwoWayCollisionChecker) target).getReverseCollisionChecker();
                        if (reverse != null) {
                            var result = reverse.apply(this);
                            if (result == TwoWayCollisionChecker.CollisionResult.COLLIDE) {
                                this.finishFalling();
                            }
                        }
                    }
                }
            }
        }
    }

    private void performVolumetricEntityCollision(
            Behaviour behaviour,
            Holder<Spell> spellEntry,
            Spell.ProjectileData data) {

        // 1. Determine OBB dimensions from hitbox (caller guarantees hitbox is non-null)
        var hitbox = data.hitbox;
        float obbWidth  = hitbox.width;
        float obbHeight = hitbox.height;
        float obbLength = (hitbox.length > 0) ? hitbox.length : hitbox.width;
        Vec3 obbCenter = this.position().add(this.getDeltaMovement().normalize().scale(obbLength));

        // point backward and miss targets that are clearly in the travel path.
        float obbYaw = this.getYRot();
        float obbPitch = this.getXRot();

        // NOTE: OBB constructor param order is (pitch_value, yaw_value) — matches fromPolar(pitch, yaw)
        var obb = new OrientedBoundingBox(
                obbCenter, obbWidth, obbHeight, obbLength,
                obbPitch, obbYaw);
        obb.updateVertex();

        var effectiveLength = Math.max(obbWidth, obbLength);

        // 3. Broad-phase: query candidate entities in a conservative search box
        double searchRadius = effectiveLength / 2.0 + Math.max(obbWidth, obbHeight) / 2.0 + 1.0;
        var broadPhaseBox = this.getBoundingBox().inflate(searchRadius);
        List<Entity> candidates = this.level().getEntities(
                this, broadPhaseBox, this::canHitEntity);

        // === DEBUG LOG ===
        if (tickCount <= 60) {
            System.out.println("[VOBB] t=" + tickCount
                    + " p=" + fmt(position()) + " pp=" + fmtRaw(xOld, yOld, zOld)
                    + " y=" + String.format("%.2f", getYRot()) + "(vy=" + String.format("%.2f", obbYaw) + ")"
                    + " pt=" + String.format("%.2f", getXRot()) + "(vpt=" + String.format("%.2f", obbPitch) + ")");
            System.out.println("[VOBB]  c=" + fmt(obbCenter)
                    + " e(w=" + String.format("%.2f", obbWidth) + ",h=" + String.format("%.2f", obbHeight) + ",l=" + String.format("%.2f", effectiveLength) + ")"
                    + " td=" + String.format("%.2f", 0F));
            var bb = broadPhaseBox;
            System.out.println("[VOBB]  bb=[" + String.format("%.2f,%.2f,%.2f->%.2f,%.2f,%.2f", bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ) + "] n=" + candidates.size());
            for (Entity c : candidates) {
                var cb = c.getBoundingBox().inflate(c.getPickRadius());
                var cc = c.position().add(0, c.getBbHeight() / 2.0, 0);
                boolean ix = obb.intersects(cb);
                boolean cn = obb.contains(cc);
                System.out.println("[VOBB]   e=" + c.getName().getString()
                        + " ep=" + fmt(c.position())
                        + " d=" + String.format("%.2f", c.distanceTo(this))
                        + " h=" + impactHistory.contains(c.getId())
                        + " ix=" + ix + " cn=" + cn);
            }
        }
        // === END DEBUG LOG ===

        if (candidates.isEmpty()) return;

        // 4. Narrow-phase: SAT test (OBB vs entity AABB, plus center-point containment check)
        List<Entity> hits = new ArrayList<>();
        for (Entity candidate : candidates) {
            if (obb.intersects(candidate.getBoundingBox().inflate(candidate.getPickRadius()))
                    || obb.contains(candidate.position().add(0, candidate.getBbHeight() / 2.0, 0))) {
                hits.add(candidate);
            }
        }
        if (hits.isEmpty()) return;

        // 5. Sort by distance to mid-center: nearest processed first (pierce/ricochet consistency)
        hits.sort(Comparator.comparingDouble(e -> e.distanceToSqr(obbCenter)));

        // 6. Process hits sequentially; stop if projectile is killed mid-loop
        for (Entity hitEntity : hits) {
            if (this.isRemoved()) break;
            if (impactHistory.contains(hitEntity.getId())) continue;
            handleHitResult(new EntityHitResult(hitEntity), behaviour, spellEntry);
        }
    }

    private static String fmt(Vec3 v) {
        return String.format("(%.2f, %.2f, %.2f)", v.x, v.y, v.z);
    }
    private static String fmtRaw(double x, double y, double z) {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }

    private void finishFalling() {
        Entity owner = this.getOwner();
        if (owner == null || owner.isRemoved()) {
            return;
        }
        if (owner instanceof LivingEntity livingEntity) {
            SpellHelper.fallImpact(livingEntity, this, this.getSpellEntry(), context.position(this.position()));
        }
    }

    private int followTicks = 0;
    private void followTarget() {
        var target = getFollowedTarget();
        var data = projectileData();
        if (data == null) {
            return;
        }
        var homing_angle = projectileData().homing_angle;
        if (projectileData().homing_angles != null && followTicks < projectileData().homing_angles.length) {
            homing_angle = projectileData().homing_angles[followTicks];
        }
        if (target != null && homing_angle > 0) {
            if (data.homing_after_relative_distance > 0 || data.homing_after_absolute_distance > 0) {
                var shouldFollow = distanceTraveled >= (distanceToFollow * data.homing_after_relative_distance)
                        || distanceTraveled >= data.homing_after_absolute_distance;
                if (!shouldFollow) {
                    return;
                }
            }
//            System.out.println((this.level().isClientSide() ? "Client: " : "Server: ") + "Following target: " + target + " with angle: " + homing_angle);
            var distanceVector = (target.position().add(0, target.getBbHeight() / 2F, 0))
                    .subtract(this.position().add(0, this.getBbHeight() / 2F, 0));
//            System.out.println((world.isClientSide() ? "Client: " : "Server: ") + "Distance: " + distanceVector);
//            System.out.println((world.isClientSide() ? "Client: " : "Server: ") + "Velocity: " + getDeltaMovement());
            var newVelocity = VectorHelper.rotateTowards(getDeltaMovement(), distanceVector, homing_angle);
            if (newVelocity.lengthSqr() > 0) {
//                System.out.println((world.isClientSide() ? "Client: " : "Server: ") + "Rotated to: " + newVelocity);
                this.setDeltaMovement(newVelocity);
                // this.hurtMarked = true;
                followTicks += 1;
            }
        }
    }

    protected float getDrag() {
        return 0.95F;
    }

    private static void setRotFromVelocity(Entity entity) {
        Vec3 vel = entity.getDeltaMovement();
        double d = vel.horizontalDistance();
        entity.setYRot((float)(Mth.atan2(vel.x, vel.z) * (180.0 / Math.PI)));
        entity.setXRot((float)(Mth.atan2(vel.y, d) * (180.0 / Math.PI)));
        entity.yRotO = entity.getYRot();
        entity.xRotO = entity.getXRot();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (!this.level().isClientSide()) {
            var target = entityHitResult.getEntity();
            if (target != null
                    && !impactHistory.contains(target.getId())
                    && this.getOwner() != null
                    && this.getOwner() instanceof LivingEntity caster) {
                setFollowedTarget(null);
                var context = this.context;
                if (context == null) {
                    context = new SpellHelper.ImpactContext();
                    var spell = this.getSpellEntry().value();
                    if (getOwner() instanceof Player player && spell != null)  {
                        context = context.power(SpellPower.getSpellPower(spell.school, player));
                    }
                }
                if (context.power() == null) {
                    this.kill((ServerLevel) this.level());
                    return;
                }

                var prevProjectilePos = new Vec3(this.xOld, this.yOld, this.zOld);
                var hitVector = entityHitResult.getLocation().subtract(prevProjectilePos).normalize().scale(this.getBbWidth() * 0.5F);
                var hitPosition = entityHitResult.getLocation().subtract(hitVector);

                var performed = SpellHelper.projectileImpact(caster, this, target, this.getSpellEntry(), context.position(hitPosition));
                if (performed) {
                    chainReactionFrom(target);
                    if (ricochetFrom(target, caster)) {
                        return;
                    }
                    if (pierced(target)) {
                        return;
                    }
                    this.kill((ServerLevel) this.level());
                }
            }
        }
    }

    // MARK: Perks
    protected Set<Integer> impactHistory = new HashSet<>();

    /**
     * Returns `true` if a new target is found to ricochet to
     */
    protected boolean ricochetFrom(Entity target, LivingEntity caster) {
        if (this.perks == null
                || this.perks.ricochet <= 0) {
            return false;
        }
        impactHistory.add(target.getId());

        // Find next target
        var box = this.getBoundingBox().inflate(
                this.perks.ricochet_range,
                this.perks.ricochet_range,
                this.perks.ricochet_range);
        var spell = this.getSpellEntry().value();
        var intents = SpellHelper.impactIntents(spell);
        Predicate<Entity> intentMatches = (entity) -> {
            boolean intentAllows = false;
            for (var intent: intents) {
                intentAllows = intentAllows || EntityRelations.actionAllowed(SpellTarget.FocusMode.AREA, intent, caster, entity);
            }
            return intentAllows;
        };
        var otherTargets = this.level().getEntities(this, box, (entity) -> {
            return entity.isAttackable()
                    && entity instanceof LivingEntity // Avoid targeting unliving entities like other projectiles
                    && !impactHistory.contains(entity.getId())
                    && intentMatches.test(entity)
                    && !entity.position().equals(target.position());
        });
        if (otherTargets.isEmpty()) {
            this.setFollowedTarget(null);
            return false;
        }

        otherTargets.sort(Comparator.comparingDouble(o -> o.distanceToSqr(target)));

        // Set trajectory
        var newTarget = otherTargets.get(0);
        var newPos = target.position().add(0, target.getBbHeight() * 0.5F, 0);
        this.setPos(newPos.x, newPos.y, newPos.z);
        this.setFollowedTarget(newTarget);

        var distanceVector = (newTarget.position().add(0, newTarget.getBbHeight() / 2F, 0))
                .subtract(this.position().add(0, this.getBbHeight() / 2F, 0));
        var newVelocity = distanceVector.normalize().scale(this.getDeltaMovement().length());
        this.setDeltaMovement(newVelocity);
        this.hurtMarked = true;

        this.perks.ricochet -= 1;
        if (this.perks.bounce_ricochet_sync) {
            this.perks.bounce -= 1;
        }
        return true;
    }

    /**
     * Returns `true` if projectile can continue to travel
     */
    private boolean pierced(Entity target) {
        if (this.perks == null
                || this.perks.pierce <= 0) {
            return false;
        }
        // Save
        impactHistory.add(target.getId());
        setFollowedTarget(null);
        this.perks.pierce -= 1;

        // Modify velocity by a tiny, non zero amount
        // to enforce velocity update on the client.
        // (Otherwise the projectile is going crazy on the client)
        var tiny = 0.01 * ((-1) * (this.perks.pierce % 2));
        this.setDeltaMovement(this.getDeltaMovement().scale(1 + tiny));
        this.hurtMarked = true;

        return true;
    }

    private boolean bounceFrom(BlockHitResult blockHitResult) {
        if (this.perks == null
                || this.perks.bounce <= 0) {
            return false;
        }

        var previousPosition = position();
        var previousDirection = getDeltaMovement();
        var impactPosition = blockHitResult.getLocation();
        var impactSide = blockHitResult.getDirection();
        var speed = getDeltaMovement().length();

        Vec3 surfaceNormal = getSurfaceNormal(impactSide);
        Vec3 newDirection = calculateBounceVector(previousDirection, surfaceNormal);

        // Calculate the remaining distance the projectile should travel after bouncing
        double remainingDistance = previousDirection.length() - (impactPosition.subtract(previousPosition)).length();

        // Calculate the final position after the remaining distance
        Vec3 finalPosition = impactPosition.add(newDirection.normalize().scale(remainingDistance));

        // Set the new position and velocity
        this.setPos(finalPosition.x, finalPosition.y, finalPosition.z);
        this.setDeltaMovement(newDirection.scale(speed));
        setRotFromVelocity(this);

        this.perks.bounce -= 1;
        if (this.perks.bounce_ricochet_sync) {
            this.perks.ricochet -= 1;
        }
        this.hurtMarked = true;
        this.skipTravel = true;
        return true;
    }

    public Vec3 calculateBounceVector(Vec3 previousDirection, Vec3 normal) {
        // Calculate the reflection of the incident vector with respect to the surface normal
        return previousDirection.subtract(normal.scale(2.0 * previousDirection.dot(normal)));
    }

    public Vec3 getSurfaceNormal(Direction blockSide) {
        return switch (blockSide) {
            case DOWN -> new Vec3(0, -1, 0);
            case UP -> new Vec3(0, 1, 0);
            case NORTH -> new Vec3(0, 0, -1);
            case SOUTH -> new Vec3(0, 0, 1);
            case WEST -> new Vec3(-1, 0, 0);
            case EAST -> new Vec3(1, 0, 0);
        };
    }
    
    private void chainReactionFrom(Entity target) {
        if (this.perks == null
                || this.perks.chain_reaction_size <= 0
                || this.perks.chain_reaction_triggers <= 0
                || impactHistory.contains(target.getId())) {
            return;
        }
        if (this.level().isClientSide()) {
            return;
        }
        var spellEntry = this.getSpellEntry();
        if (spellEntry == null) {
            return;
        }
        var position = this.position();
        var spawnCount = this.perks.chain_reaction_size;
        var launchVector = new Vec3(1, 0, 0).scale(this.getDeltaMovement().length());
        var launchAngle = 360 / spawnCount;
        var launchAngleOffset = random.nextFloat() * launchAngle;

        this.impactHistory.add(target.getId());
        this.perks.chain_reaction_triggers -= 1;
        this.perks.chain_reaction_size += this.perks.chain_reaction_increment;

        for (int i = 0; i < spawnCount; i++) {
            var projectile = new SpellProjectile(this.level(), (LivingEntity)this.getOwner(),
                    position.x, position.y, position.z,
                    this.getBehaviour(), spellEntry, context, this.perks.copy());

            var angle = launchAngle * i + launchAngleOffset;
            projectile.setDeltaMovement(launchVector.yRot((float) Math.toRadians(angle)));
            projectile.range = this.range;
            setRotFromVelocity(projectile);
            projectile.impactHistory = new HashSet<>(this.impactHistory);
            this.level().addFreshEntity(projectile);
        }
    }

    // MARK: Helper

    public SpellHelper.ImpactContext getImpactContext() {
        return context;
    }

    public ItemStack getItemStackModel() {
        return itemStackModel;
    }

    // MARK: FlyingSpellEntity

    public Spell.ProjectileModel renderData() {
        var data = projectileData();
        if (data != null && data.client_data != null) {
            return data.client_data.model;
        }
        return null;
    }

    @Override
    public ItemStack getItem() {
        return getStack();
    }

    public ItemStack getStack() {
        var data = projectileData();
        if (data != null && data.client_data != null && data.client_data.model != null) {
            var itemOpt = BuiltInRegistries.ITEM.get(Identifier.parse(data.client_data.model.model_id));
            return itemOpt.map(ref -> ref.value().getDefaultInstance()).orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (bounceFrom(blockHitResult)) {
            return;
        }

        if (this.getOwner() != null
                && this.getOwner() instanceof LivingEntity caster) {
            var hitPosition = blockHitResult.getLocation();
            var performed = SpellHelper.projectileImpact(caster, this, null, this.getSpellEntry(), context.position(hitPosition));
        }
        this.kill((ServerLevel) this.level());
    }

    private Gson gson = new Gson();


    // MARK: Stored data

    public void setBehaviour(Behaviour behaviour) {
        this.getEntityData().set(TRACKER_BEHAVIOUR, behaviour.toString());
    }
    public Behaviour getBehaviour() {
        var string = this.getEntityData().get(TRACKER_BEHAVIOUR);
        if (string == null || string.isEmpty()) {
            return Behaviour.FLY;
        }
        return Behaviour.valueOf(string);
    }

    private Holder<Spell> spellEntry;
    public void setSpell(Holder<Spell> entry) {
        this.spellEntry = entry;
        if (!this.level().isClientSide()) {
            this.getEntityData().set(TRACKER_SPELL_ID, spellId().toString());
        }
        this.refreshDimensions();
    }
    @Nullable public Holder<Spell> getSpellEntry() {
        return spellEntry;
    }
    private Identifier spellId() {
        if (spellEntry != null) {
            return spellEntry.unwrapKey().get().identifier();
        }
        return null;
    }


    private Entity followedTarget;
    private double distanceToFollow = 0;
    public void setFollowedTarget(Entity target) {
        followedTarget = target;
        if (target != null) {
            distanceToFollow = target.distanceTo(this);
        } else {
            distanceToFollow = 0;
        }
        var id = -1;
        if (!this.level().isClientSide()) {
            if (target != null) {
                id = target.getId();
            }
            this.getEntityData().set(TRACKER_TARGET_ID, id);
        }
    }

    private ItemStack itemStackModel;
    public void setItemStackModel(ItemStack itemStack) {
        var modelId = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        this.getEntityData().set(TRACKER_ITEM_MODEL_ID, modelId.toString());
    }
    private void updateItemModel(String idString) {
        if (idString != null && !idString.isEmpty()) {
            var id = Identifier.parse(this.getEntityData().get(TRACKER_ITEM_MODEL_ID));
            itemStackModel = BuiltInRegistries.ITEM.get(id).map(ref -> ref.value().getDefaultInstance()).orElse(ItemStack.EMPTY);
        }
    }

    // MARK: NBT (Persistence)

    private static String NBT_BEHAVIOUR = "Behaviour";
    private static String NBT_SPELL_ID = "Spell.ID";
    private static String NBT_PERKS = "Perks";
    private static String NBT_IMPACT_CONTEXT = "Impact.Context";
    private static String NBT_ITEM_MODEL_ID = "Item.Model.ID";

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putString(NBT_BEHAVIOUR, this.getBehaviour().toString());

        if (this.spellId() != null) {
            output.putString(NBT_SPELL_ID, this.spellId().toString());
        }
        output.putString(NBT_IMPACT_CONTEXT, gson.toJson(this.context));
        output.putString(NBT_PERKS, gson.toJson(this.perks));

        var itemModelId = getEntityData().get(TRACKER_ITEM_MODEL_ID);
        if (!itemModelId.isEmpty()) {
            output.putString(NBT_ITEM_MODEL_ID, itemModelId);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        var spellIdStr = input.getStringOr(NBT_SPELL_ID, "");
        if (!spellIdStr.isEmpty()) {
            try {
                var behaviour = Behaviour.valueOf(input.getStringOr(NBT_BEHAVIOUR, "FLY"));
                this.setBehaviour(behaviour);

                var spellId = Identifier.parse(spellIdStr);
                this.setSpell(SpellRegistry.from(this.level()).get(spellId).orElse(null));

                this.context = gson.fromJson(input.getStringOr(NBT_IMPACT_CONTEXT, "{}"), SpellHelper.ImpactContext.class);
                this.perks = gson.fromJson(input.getStringOr(NBT_PERKS, "{}"), Spell.ProjectileData.Perks.class);

                var itemModelIdStr = input.getStringOr(NBT_ITEM_MODEL_ID, "");
                if (!itemModelIdStr.isEmpty()) {
                    updateItemModel(itemModelIdStr);
                }
            } catch (Exception e) {
                System.err.println("SpellProjectile - Failed to read spell data from NBT " + e.getMessage());
            }
        }
    }

    // MARK: SynchedEntityData (client-server sync)

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TRACKER_SPELL_ID, "");
        builder.define(TRACKER_BEHAVIOUR, Behaviour.FLY.toString());
        builder.define(TRACKER_TARGET_ID, 0);
        builder.define(TRACKER_ITEM_MODEL_ID, "");
    }

    private static final EntityDataAccessor<String> TRACKER_SPELL_ID;
    private static final EntityDataAccessor<String> TRACKER_BEHAVIOUR;
    private static final EntityDataAccessor<Integer> TRACKER_TARGET_ID;
    private static final EntityDataAccessor<String> TRACKER_ITEM_MODEL_ID;

    static {
        TRACKER_SPELL_ID = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.STRING);
        TRACKER_BEHAVIOUR = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.STRING);
        TRACKER_TARGET_ID = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.INT);
        TRACKER_ITEM_MODEL_ID = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.STRING);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if (this.level().isClientSide()) {
            if (data.equals(TRACKER_SPELL_ID)) {
                var spellId = this.getEntityData().get(TRACKER_SPELL_ID);
                var spellEntry = SpellRegistry.from(this.level()).get(Identifier.parse(spellId)).orElse(null);
                this.setSpell(spellEntry);
            }
            if (data.equals(TRACKER_ITEM_MODEL_ID)) {
                updateItemModel(this.getEntityData().get(TRACKER_ITEM_MODEL_ID));
            }
            if (data.equals(TRACKER_TARGET_ID)) {
                var id = this.getEntityData().get(TRACKER_TARGET_ID);
                var target = id > 0 ? this.level().getEntity(id) : null;
                this.setFollowedTarget(target);
            }
        }
    }
}
