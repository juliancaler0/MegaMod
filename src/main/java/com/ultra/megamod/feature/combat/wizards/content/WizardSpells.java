package com.ultra.megamod.feature.combat.wizards.content;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.spell.SpellEffects;
import com.ultra.megamod.lib.spellengine.api.datagen.SpellBuilder;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.fx.ParticleBatch;
import com.ultra.megamod.lib.spellengine.api.spell.fx.PlayerAnimation;
import com.ultra.megamod.lib.spellengine.api.spell.fx.Sound;
import com.ultra.megamod.lib.spellengine.client.gui.SpellTooltip;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineParticles;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineSounds;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Wizard spell definitions for the spell engine system.
 * Contains all 16 wizard spells (Arcane, Fire, Frost) ported from the Wizards mod.
 *
 * Spell JSONs are generated under data/megamod/spell/ and these entries
 * provide programmatic access to spell IDs for weapon containers and spell books.
 */
public class WizardSpells {

    public enum WeaponGroup { WIZARD_STAFF, ARCANE_STAFF, FIRE_STAFF, FROST_STAFF }
    public enum Book { ARCANE, FIRE, FROST }

    public record Entry(Identifier id, Spell spell, String title, String description,
                        @Nullable SpellTooltip.DescriptionMutator mutator,
                        @Nullable List<WeaponGroup> weaponGroups,
                        @Nullable Book book) {
        public Entry(Identifier id, Spell spell, String title, String description) {
            this(id, spell, title, description, null, List.of(), null);
        }
        public Entry mutator(SpellTooltip.DescriptionMutator mutator) {
            return new Entry(id, spell, title, description, mutator, weaponGroups, book);
        }
        public Entry weaponGroup(WeaponGroup weaponGroup) {
            var newGroups = new ArrayList<>(weaponGroups != null ? weaponGroups : List.of());
            newGroups.add(weaponGroup);
            return new Entry(id, spell, title, description, mutator, newGroups, book);
        }
        public Entry book(Book book) {
            return new Entry(id, spell, title, description, mutator, weaponGroups, book);
        }
    }

    public static final List<Entry> entries = new ArrayList<>();
    private static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }

    private static final String PRIMARY_GROUP = "primary";
    private static final float BASIC_PROJECTILE_RANGE = 48F;
    private static final Color ARCANE_COLOR = Color.from(SpellSchools.ARCANE.color);
    private static final Color FIRE_COLOR = Color.from(SpellSchools.FIRE.color);
    private static final Color FROST_COLOR = Color.from(SpellSchools.FROST.color);

    // ─── Helper methods ───

    private static Spell activeSpellBase() {
        var spell = new Spell();
        spell.type = Spell.Type.ACTIVE;
        spell.active = new Spell.Active();
        spell.active.cast = new Spell.Active.Cast();
        return spell;
    }

    private static ParticleBatch arcaneCastingParticles() {
        return new ParticleBatch(
                SpellEngineParticles.MagicParticles.get(
                        SpellEngineParticles.MagicParticles.Shape.SPELL,
                        SpellEngineParticles.MagicParticles.Motion.ASCEND
                ).id().toString(),
                ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET,
                1, 0.05F, 0.1F)
                .color(ARCANE_COLOR.toRGBA());
    }

    private static void configureArcaneRuneCost(Spell spell) {
        if (spell.cost == null) spell.cost = new Spell.Cost();
        spell.cost.item = new Spell.Cost.Item();
        spell.cost.item.id = "megamod:arcane_rune";
    }

    private static void configureCooldown(Spell spell, float duration) {
        if (spell.cost == null) spell.cost = new Spell.Cost();
        spell.cost.cooldown = new Spell.Cost.Cooldown();
        spell.cost.cooldown.duration = duration;
    }

    private static ParticleBatch fireCastingParticles() {
        return new ParticleBatch(
                SpellEngineParticles.flame.id().toString(),
                ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET,
                1, 0.05F, 0.1F);
    }

    private static void configureFireRuneCost(Spell spell) {
        if (spell.cost == null) spell.cost = new Spell.Cost();
        spell.cost.item = new Spell.Cost.Item();
        spell.cost.item.id = "megamod:fire_rune";
    }

    private static ParticleBatch[] fireImpactParticles() {
        return new ParticleBatch[] {
                new ParticleBatch("smoke",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        15, 0.01F, 0.1F),
                new ParticleBatch("flame",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        10, 0.01F, 0.1F)
        };
    }

    private static ParticleBatch frostCastingParticles() {
        return new ParticleBatch(
                SpellEngineParticles.snowflake.id().toString(),
                ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.CENTER,
                0.5F, 0.1F, 0.2F);
    }

    private static void configureFrostRuneCost(Spell spell) {
        if (spell.cost == null) spell.cost = new Spell.Cost();
        spell.cost.item = new Spell.Cost.Item();
        spell.cost.item.id = "megamod:frost_rune";
    }

    private static ParticleBatch[] frostImpactParticles() {
        return new ParticleBatch[] {
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.FROST,
                                SpellEngineParticles.MagicParticles.Motion.BURST
                        ).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        50, 0.2F, 0.7F)
                        .color(FROST_COLOR.toRGBA())
        };
    }

    // ═══════════════════════════════════════════
    // ARCANE SPELLS
    // ═══════════════════════════════════════════

    public static Entry arcane_bolt = add(arcane_bolt());
    private static Entry arcane_bolt() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "arcane_bolt");
        var spell = SpellBuilder.createWeaponSpell();
        spell.school = SpellSchools.ARCANE;
        spell.group = PRIMARY_GROUP;
        spell.tier = 0;
        spell.range = BASIC_PROJECTILE_RANGE;
        spell.active.cast.duration = 1;
        spell.active.cast.animation = PlayerAnimation.of("megamod:one_handed_projectile_charge");
        spell.active.cast.sound = new Sound(SpellEngineSounds.GENERIC_ARCANE_CASTING.id(), 0);
        spell.active.cast.particles = new ParticleBatch[] { arcaneCastingParticles() };

        spell.release = new Spell.Release();
        spell.release.animation = PlayerAnimation.of("megamod:one_handed_projectile_release");
        spell.release.sound = new Sound(WizardSounds.ARCANE_MISSILE_RELEASE.id());

        spell.target.type = Spell.Target.Type.AIM;
        spell.target.aim = new Spell.Target.Aim();

        spell.deliver.type = Spell.Delivery.Type.PROJECTILE;
        spell.deliver.projectile = new Spell.Delivery.ShootProjectile();
        var projectile = new Spell.ProjectileData();
        projectile.homing_angle = 1F;
        projectile.client_data = new Spell.ProjectileData.Client();
        projectile.client_data.light_level = 10;
        projectile.client_data.travel_particles = new ParticleBatch[] {
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPELL,
                                SpellEngineParticles.MagicParticles.Motion.ASCEND
                        ).id().toString(),
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                        ParticleBatch.Rotation.LOOK, 1, 0.05F, 0.1F, 0.0F, 0F)
                        .color(ARCANE_COLOR.toRGBA())
        };
        projectile.client_data.model = new Spell.ProjectileModel();
        projectile.client_data.model.model_id = "megamod:spell_projectile/arcane_bolt";
        projectile.client_data.model.scale = 0.5F;
        spell.deliver.projectile.projectile = projectile;

        var damage = SpellBuilder.Impacts.damage(0.7F, 0.6F);
        damage.particles = new ParticleBatch[] {
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.ARCANE,
                                SpellEngineParticles.MagicParticles.Motion.BURST
                        ).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        null, 20, 0.2F, 0.7F, 0.0F, 0F)
                        .color(ARCANE_COLOR.toRGBA())
        };
        damage.sound = new Sound(WizardSounds.ARCANE_MISSILE_IMPACT.id());
        spell.impacts = List.of(damage);

        SpellBuilder.Cost.cooldownGroup(spell, "weapon");
        configureArcaneRuneCost(spell);

        return new Entry(id, spell, "", "");
    }

    public static Entry arcane_blast = add(arcane_blast());
    private static Entry arcane_blast() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "arcane_blast");
        var spell = SpellBuilder.createWeaponSpell();
        spell.school = SpellSchools.ARCANE;
        spell.group = PRIMARY_GROUP;
        spell.tier = 1;
        spell.sub_tier = 2;
        spell.range = 16;
        spell.learn = new Spell.Learn();

        spell.active.cast.duration = 1.5F;
        spell.active.cast.animation = PlayerAnimation.of("megamod:one_handed_projectile_charge");
        spell.active.cast.sound = new Sound(SpellEngineSounds.GENERIC_ARCANE_CASTING.id(), 0);
        spell.active.cast.particles = new ParticleBatch[] { arcaneCastingParticles() };

        spell.release = new Spell.Release();
        spell.release.animation = PlayerAnimation.of("megamod:one_handed_projectile_release");
        spell.release.sound = new Sound(WizardSounds.ARCANE_MISSILE_RELEASE.id());

        spell.target.type = Spell.Target.Type.AIM;
        spell.target.aim = new Spell.Target.Aim();
        spell.target.aim.sticky = true;

        var damage = SpellBuilder.Impacts.damage(0.8F, 0.5F);
        damage.particles = new ParticleBatch[] {
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.ARCANE,
                                SpellEngineParticles.MagicParticles.Motion.BURST
                        ).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        30, 0.2F, 0.7F)
                        .color(ARCANE_COLOR.toRGBA()),
                new ParticleBatch("firework",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        20, 0.05F, 0.2F)
        };
        damage.sound = new Sound(WizardSounds.ARCANE_BLAST_IMPACT.id());

        var arcaneCharge = SpellBuilder.Impacts.effectAdd("megamod:arcane_charge", 10, 1, 2);
        arcaneCharge.action.status_effect.show_particles = false;
        arcaneCharge.action.apply_to_caster = true;

        spell.impacts = List.of(damage, arcaneCharge);
        SpellBuilder.Cost.cooldownGroup(spell, "weapon");
        configureArcaneRuneCost(spell);

        return new Entry(id, spell, "", "").weaponGroup(WeaponGroup.ARCANE_STAFF).weaponGroup(WeaponGroup.WIZARD_STAFF);
    }

    public static Entry arcane_missile = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "arcane_missile"),
            new Spell(), "", "").book(Book.ARCANE));

    public static Entry arcane_beam = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "arcane_beam"),
            new Spell(), "", "").book(Book.ARCANE));

    public static Entry arcane_blink = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "arcane_blink"),
            new Spell(), "", "").book(Book.ARCANE));

    // ═══════════════════════════════════════════
    // FIRE SPELLS
    // ═══════════════════════════════════════════

    public static Entry fire_scorch = add(fire_scorch());
    private static Entry fire_scorch() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "fire_scorch");
        var spell = SpellBuilder.createWeaponSpell();
        spell.school = SpellSchools.FIRE;
        spell.group = PRIMARY_GROUP;
        spell.tier = 0;
        spell.sub_tier = 0;
        spell.range = 16;

        spell.active.cast.duration = 1.2F;
        spell.active.cast.animation = PlayerAnimation.of("megamod:one_handed_projectile_charge");
        spell.active.cast.sound = new Sound(SpellEngineSounds.GENERIC_FIRE_CASTING.id(), 0);
        spell.active.cast.particles = new ParticleBatch[] { fireCastingParticles() };

        spell.release = new Spell.Release();
        spell.release.animation = PlayerAnimation.of("megamod:one_handed_projectile_release");
        spell.release.sound = new Sound(SpellEngineSounds.GENERIC_FIRE_RELEASE.id());

        spell.target.type = Spell.Target.Type.AIM;
        spell.target.aim = new Spell.Target.Aim();
        spell.target.aim.required = true;
        spell.target.aim.sticky = true;

        var damage = SpellBuilder.Impacts.damage(0.6F, 0.6F);
        damage.particles = fireImpactParticles();
        damage.sound = new Sound(WizardSounds.FIRE_SCORCH_IMPACT.id());

        var fire = SpellBuilder.Impacts.fire(3);
        spell.impacts = List.of(damage, fire);

        SpellBuilder.Cost.cooldownGroup(spell, "weapon");
        configureFireRuneCost(spell);

        return new Entry(id, spell, "", "");
    }

    public static Entry fireball = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "fireball"),
            new Spell(), "", ""));

    public static Entry fire_blast = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "fire_blast"),
            new Spell(), "", "").weaponGroup(WeaponGroup.FIRE_STAFF).weaponGroup(WeaponGroup.WIZARD_STAFF));

    public static Entry fire_breath = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "fire_breath"),
            new Spell(), "", "").book(Book.FIRE));

    public static Entry fire_meteor = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "fire_meteor"),
            new Spell(), "", "").book(Book.FIRE));

    public static Entry fire_wall = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "fire_wall"),
            new Spell(), "", "").book(Book.FIRE));

    // ═══════════════════════════════════════════
    // FROST SPELLS
    // ═══════════════════════════════════════════

    public static Entry frost_shard = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "frost_shard"),
            new Spell(), "", ""));

    public static Entry frostbolt = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "frostbolt"),
            new Spell(), "", "").weaponGroup(WeaponGroup.FROST_STAFF).weaponGroup(WeaponGroup.WIZARD_STAFF));

    public static Entry frost_nova = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "frost_nova"),
            new Spell(), "", "").book(Book.FROST));

    public static Entry frost_shield = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "frost_shield"),
            new Spell(), "", "").book(Book.FROST));

    public static Entry frost_blizzard = add(new Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "frost_blizzard"),
            new Spell(), "", "").book(Book.FROST));
}
