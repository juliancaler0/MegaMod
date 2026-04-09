package com.ultra.megamod.feature.combat.spell;

/**
 * Data-driven spell definition. Simplified from SpellEngine's Spell class.
 * All spells are defined as static constants in SpellRegistry.
 */
public record SpellDefinition(
    String id,
    String name,
    SpellSchool school,
    int tier,
    CastMode castMode,
    float castDuration,         // seconds (0 for instant)
    DeliveryType delivery,
    TargetType target,
    float range,
    float damageCoefficient,    // multiplied by school spell power
    float healCoefficient,      // multiplied by healing power
    float cooldownSeconds,
    float knockback,
    StatusEffectDef[] effects,  // effects applied on impact
    AreaConfig area,            // null if not AOE
    ProjectileConfig projectile,// null if not projectile
    CloudConfig cloud,          // null if not cloud
    String animationId,         // animation key (for future use)
    String classRequirement,    // skill branch required (e.g., "WIZARD", "PALADIN", null for any)
    float exhaust,              // food exhaustion cost (0.0-1.0 range, multiplied by 40 for MC units)
    SpellVisuals visuals        // visual/audio configuration (animations, sounds, models)
) {

    /**
     * Backwards-compatible constructor — defaults exhaust to 0 and visuals to null.
     */
    public SpellDefinition(String id, String name, SpellSchool school, int tier,
                           CastMode castMode, float castDuration, DeliveryType delivery,
                           TargetType target, float range, float damageCoefficient,
                           float healCoefficient, float cooldownSeconds, float knockback,
                           StatusEffectDef[] effects, AreaConfig area,
                           ProjectileConfig projectile, CloudConfig cloud,
                           String animationId, String classRequirement) {
        this(id, name, school, tier, castMode, castDuration, delivery, target, range,
            damageCoefficient, healCoefficient, cooldownSeconds, knockback, effects,
            area, projectile, cloud, animationId, classRequirement, 0f, null);
    }

    /**
     * Constructor with exhaust but no visuals.
     */
    public SpellDefinition(String id, String name, SpellSchool school, int tier,
                           CastMode castMode, float castDuration, DeliveryType delivery,
                           TargetType target, float range, float damageCoefficient,
                           float healCoefficient, float cooldownSeconds, float knockback,
                           StatusEffectDef[] effects, AreaConfig area,
                           ProjectileConfig projectile, CloudConfig cloud,
                           String animationId, String classRequirement, float exhaust) {
        this(id, name, school, tier, castMode, castDuration, delivery, target, range,
            damageCoefficient, healCoefficient, cooldownSeconds, knockback, effects,
            area, projectile, cloud, animationId, classRequirement, exhaust, null);
    }

    // ─── Enums ��──

    public enum CastMode {
        INSTANT,    // cast immediately, no wind-up
        CHARGED,    // hold to charge, release to cast
        CHANNELED   // continuous effect while holding
    }

    public enum DeliveryType {
        DIRECT,     // instant effect at target location
        PROJECTILE, // spawn projectile entity
        BEAM,       // continuous raycast
        AREA,       // AOE around point
        CLOUD,      // persistent AOE zone entity
        TELEPORT,   // move caster to target
        MELEE,      // enhanced melee strike
        ARROW,      // enhance next arrow shot (stash effect)
        SPAWN       // spawn entity (barrier, summon)
    }

    public enum TargetType {
        SELF,           // target = caster
        AIM,            // target = what caster is looking at
        AREA,           // target = area around a point
        FROM_TRIGGER    // determined by trigger context (melee hit, arrow impact)
    }

    // ─── Sub-configs ───

    public record StatusEffectDef(String effectId, int durationTicks, int amplifier, boolean harmful) {}

    public record AreaConfig(float horizontalRange, float verticalRange, float angleDegrees, boolean includeCaster) {}

    public record ProjectileConfig(float velocity, float homingAngle, int pierce, int bounce) {}

    public record CloudConfig(float radius, float timeToLiveSeconds, int impactIntervalTicks) {}

    // ─── Builder helpers ───

    public static SpellDefinition damage(String id, String name, SpellSchool school, int tier,
                                          CastMode cast, float castDur, DeliveryType delivery,
                                          TargetType target, float range, float dmgCoeff,
                                          float cooldown, float knockback, String classReq) {
        return new SpellDefinition(id, name, school, tier, cast, castDur, delivery, target, range,
            dmgCoeff, 0, cooldown, knockback, new StatusEffectDef[0], null, null, null, null, classReq, 0f, null);
    }

    public static SpellDefinition heal(String id, String name, int tier,
                                        CastMode cast, float castDur, TargetType target,
                                        float range, float healCoeff, float cooldown, String classReq) {
        return new SpellDefinition(id, name, SpellSchool.HEALING, tier, cast, castDur,
            DeliveryType.DIRECT, target, range, 0, healCoeff, cooldown, 0,
            new StatusEffectDef[0], null, null, null, null, classReq, 0f, null);
    }

    public static SpellDefinition buff(String id, String name, SpellSchool school, int tier,
                                        float cooldown, StatusEffectDef[] effects, String classReq) {
        return new SpellDefinition(id, name, school, tier, CastMode.INSTANT, 0,
            DeliveryType.DIRECT, TargetType.SELF, 0, 0, 0, cooldown, 0,
            effects, null, null, null, null, classReq, 0f, null);
    }

    /** Create a spell with a specific exhaust cost. */
    public SpellDefinition withExhaust(float exhaustCost) {
        return new SpellDefinition(id, name, school, tier, castMode, castDuration, delivery, target,
            range, damageCoefficient, healCoefficient, cooldownSeconds, knockback, effects,
            area, projectile, cloud, animationId, classRequirement, exhaustCost, visuals);
    }

    /** Create a spell with visual/audio configuration attached. */
    public SpellDefinition withVisuals(SpellVisuals v) {
        return new SpellDefinition(id, name, school, tier, castMode, castDuration, delivery, target,
            range, damageCoefficient, healCoefficient, cooldownSeconds, knockback, effects,
            area, projectile, cloud, animationId, classRequirement, exhaust, v);
    }

    // ─── Visual/Audio Configuration ───

    /**
     * Complete visual and audio configuration for a spell.
     * Ported 1:1 from SpellEngine's Spell.Active.Cast/Release/Impact visual configs.
     *
     * @param castAnimation       PlayerAnimator animation ID for casting (e.g., "one_handed_projectile_charge")
     * @param releaseAnimation    PlayerAnimator animation ID for release (e.g., "one_handed_projectile_release")
     * @param castSound           Sound event path for casting loop (e.g., "combat.generic_arcane_casting")
     * @param releaseSound        Sound event path for release (e.g., "combat.arcane_beam_release")
     * @param impactSound         Sound event path for impact (e.g., "combat.arcane_blast_impact")
     * @param projectileModelId   Custom model ID for projectile rendering (e.g., "spell_projectile/arcane_bolt")
     * @param projectileModelScale Scale multiplier for projectile model
     * @param projectileOrientation Orientation mode: TOWARDS_CAMERA, TOWARDS_MOTION, ALONG_MOTION
     * @param projectileRotateDeg Degrees per tick spin for projectile
     * @param lightEmission       Light emission level: NONE, GLOW, RADIATE
     * @param beamColorRGBA       Beam color as 0xRRGGBBAA (0 = use school color)
     * @param beamFlow            Beam texture scroll speed
     * @param cloudModelId        Custom model ID for cloud rendering
     */
    public record SpellVisuals(
        String castAnimation,
        String releaseAnimation,
        String castSound,
        String releaseSound,
        String impactSound,
        String projectileModelId,
        float projectileModelScale,
        String projectileOrientation,
        float projectileRotateDeg,
        String lightEmission,
        long beamColorRGBA,
        float beamFlow,
        String cloudModelId
    ) {
        /** Minimal visuals with just animations and sounds. */
        public static SpellVisuals of(String castAnim, String releaseAnim,
                                       String castSound, String releaseSound, String impactSound) {
            return new SpellVisuals(castAnim, releaseAnim, castSound, releaseSound, impactSound,
                    null, 1.0f, "TOWARDS_CAMERA", 0, "NONE", 0, 0, null);
        }

        /** Visuals for projectile spells. */
        public static SpellVisuals projectile(String castAnim, String releaseAnim,
                                               String castSound, String releaseSound, String impactSound,
                                               String modelId, float scale, float rotateDeg) {
            return new SpellVisuals(castAnim, releaseAnim, castSound, releaseSound, impactSound,
                    modelId, scale, "TOWARDS_MOTION", rotateDeg, "GLOW", 0, 0, null);
        }

        /** Visuals for beam spells. */
        public static SpellVisuals beam(String castAnim, String castSound, String releaseSound,
                                         String impactSound, long beamColor, float flow) {
            return new SpellVisuals(castAnim, null, castSound, releaseSound, impactSound,
                    null, 1.0f, null, 0, "RADIATE", beamColor, flow, null);
        }

        /** Visuals for cloud spells. */
        public static SpellVisuals cloud(String releaseAnim, String releaseSound, String impactSound,
                                          String cloudModel) {
            return new SpellVisuals(null, releaseAnim, null, releaseSound, impactSound,
                    null, 1.0f, null, 0, "NONE", 0, 0, cloudModel);
        }

        /** Visuals with only release animation and sound (instant spells). */
        public static SpellVisuals instant(String releaseAnim, String releaseSound) {
            return new SpellVisuals(null, releaseAnim, null, releaseSound, null,
                    null, 1.0f, null, 0, "NONE", 0, 0, null);
        }
    }
}
