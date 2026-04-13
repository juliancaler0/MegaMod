package com.ultra.megamod.mixin.spellengine.arrow;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.EntityHitResult;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.entity.ConfigurableKnockback;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.SpellTriggers;
import com.ultra.megamod.lib.spellengine.internals.arrow.ArrowExtension;
import com.ultra.megamod.lib.spellengine.fx.ParticleHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractArrow.class)
public abstract class PersistentProjectileEntityMixin implements ArrowExtension {
    @Shadow protected int inGroundTime;

    @Shadow public abstract byte getPierceLevel();

    @Shadow public abstract void setPierceLevel(byte level);

    @Shadow public abstract void setBaseDamage(double damage);

    @Shadow private double baseDamage;

    private AbstractArrow arrow() {
         return (AbstractArrow)(Object)this;
    }

    private final List<Identifier> spellIds = new ArrayList<>();
    private void addSpellId(Identifier id) {
        if (!spellIds.contains(id)) {
            spellIds.add(id);
        }
        var stringList = this.spellIds.stream().map(Identifier::toString).toList();
        var json = gson.toJson(stringList);
        arrow().getEntityData().set(SPELL_ID_TRACKER, json);
    }

    private List<Holder<Spell>> cachedSpellEntry = List.of();
    @Nullable List<Holder<Spell>> spellEntries() {
        if (cachedSpellEntry == null || cachedSpellEntry.size() != spellIds.size()) {
            var entries = spellIds.stream()
                    .map(id -> {
                        var reference = SpellRegistry.from(arrow().level()).get(id).orElse(null);
                        return (Holder<Spell>)reference;
                    })
                    .toList();
            cachedSpellEntry = entries;
        }
        return cachedSpellEntry;
    }

    private boolean arrowPerksAlreadyApplied(Holder<Spell> spell) {
        var id = spell.unwrapKey().get().identifier().toString();
        return spellIds.contains(id);
    }

    // MARK: Persist extra data

    private static final Gson gson = new Gson();
    private static final Type stringListType = new TypeToken<ArrayList<String>>(){}.getType();
    private static final String NBT_KEY_SPELL_ID = "spell_id";

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void writeCustomDataToNbt_TAIL_SpellEngine(ValueOutput output, CallbackInfo ci) {
        var stringList = this.spellIds.stream().map(Identifier::toString).toList();
        var json = gson.toJson(stringList);
        output.putString(NBT_KEY_SPELL_ID, json);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readCustomDataFromNbt_TAIL_SpellEngine(ValueInput input, CallbackInfo ci) {
        var string = input.getStringOr(NBT_KEY_SPELL_ID, "");
        {
            if (string != null && !string.isEmpty()) {
                List<String> stringList = new Gson().fromJson(string, stringListType);
                this.spellIds.clear();
                for (var idString : stringList) {
                    var id = Identifier.tryParse(idString);
                    if (id != null) {
                        addSpellId(Identifier.parse(idString));
                    }
                }
            }
        }
    }

    // MARK: Sync data to client

    private static final EntityDataAccessor<String> SPELL_ID_TRACKER = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.STRING);
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void initDataTracker_TAIL_SpellEngine(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(SPELL_ID_TRACKER, "");
    }

    // MARK: Tick

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick_HEAD_SpellEngine(CallbackInfo ci) {
        var arrow = arrow();
        var world = arrow.level();
        if (world.isClientSide() && this.spellIds.isEmpty()) {
            var json = arrow().getEntityData().get(SPELL_ID_TRACKER);
            if (json.isEmpty()) {
                return;
            }
            try {
                List<String> stringList = new Gson().fromJson(json, stringListType);
                this.spellIds.clear();
                this.spellIds.addAll(stringList.stream().map(Identifier::parse).toList());
                this.spellEntries();
            } catch (Exception e) {
                System.err.println("Spell Engine: Failed to parse spell id from arrow data tracker: " + json);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick_TAIL_SpellEngine(CallbackInfo ci) {
        for (var spellEntry : spellEntries()) {
            var spell = spellEntry.value();
            var perks = spell.arrow_perks;
            if (perks != null && perks.travel_particles != null) {
                var arrow = arrow();
                for (var travel_particles : perks.travel_particles) {
                    ParticleHelper.play(arrow.level(), arrow, arrow.getYRot(), arrow.getXRot(), travel_particles);
                }
            }
        }
    }

    // MARK: ArrowExtension

    @Override
    public boolean isInGround_SpellEngine() {
        return inGroundTime > 0;
    }

    @Nullable public List<Holder<Spell>> getCarriedSpells() {
        return spellEntries();
    }

    @Override
    public void applyArrowPerks(Holder<Spell> spellEntry) {
        if(arrowPerksAlreadyApplied(spellEntry)) {
            return;
        }
        var arrow = arrow();
        var perks = spellEntry.value().arrow_perks;
        if (perks != null) {
            if (perks.velocity_multiplier != 1.0F) {
                arrow.setDeltaMovement(arrow.getDeltaMovement().scale(perks.velocity_multiplier));
            }
            if (perks.pierce > 0) {
                var newPierce = (byte)(getPierceLevel() + perks.pierce);
                setPierceLevel(newPierce);
            }
            this.setBaseDamage(this.baseDamage * perks.damage_multiplier);
        }
        var spellId = spellEntry.unwrapKey().get().identifier();
        this.addSpellId(spellId);
    }

    // MARK: Apply impact effects

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), cancellable = true)
    private void onHitEntity_BeforeDamage_SpellEngine(EntityHitResult entityHitResult, CallbackInfo ci) {
        for (var spellEntry : spellEntries()) {
            var spell = spellEntry.value();
            var arrowPerks = spell.arrow_perks;
            if (arrowPerks != null) {
                if (arrowPerks.skip_arrow_damage) {
                    ci.cancel();
                    arrow().discard();
                    var entity = entityHitResult.getEntity();
                    if (entity != null) {
                        performImpacts(spellEntry, entity, entityHitResult);
                    }
                }
            }
        }
    }

    @WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean wrapDamageEntity(
            // Mixin Parameters
            Entity entity, DamageSource damageSource, float amount, Operation<Boolean> original,
            // Context Parameters
            EntityHitResult entityHitResult) {
        var spellEntries = spellEntries();
        var arrow = arrow();
        var owner = arrow.getOwner();
        if (entity.level().isClientSide() || spellEntries.isEmpty()) {

            var result = original.call(entity, damageSource, amount);
            if (owner instanceof Player shooter) {
                SpellTriggers.onArrowImpact((ArrowExtension) arrow, shooter, entity, damageSource, amount);
            }
            return result;
        } else {
            int iFrameToRestore = 0;
            var originalIFrame = entity.invulnerableTime;
            float knockbackMultiplier = 1.0F;

            for (var spellEnrty : spellEntries) {
                var spell = spellEnrty.value();
                var arrowPerks = spell.arrow_perks;
                if (arrowPerks != null) {
                    if (arrowPerks.knockback != 1.0F) {
                        knockbackMultiplier *= arrowPerks.knockback;
                    }
                    if (arrowPerks.bypass_iframes) {
                        if (entity.invulnerableTime == originalIFrame) {
                            iFrameToRestore = entity.invulnerableTime;
                        }
                        entity.invulnerableTime = 0;
                    }
                    if (arrowPerks.iframe_to_set > 0) {
                        iFrameToRestore = arrowPerks.iframe_to_set;
                    }
                }
            }

            var pushedKnockback = false;
            if (entity instanceof LivingEntity livingEntity) {
                if (knockbackMultiplier != 1.0F) {
                    ((ConfigurableKnockback) livingEntity).pushKnockbackMultiplier_SpellEngine(knockbackMultiplier);
                    pushedKnockback = true;
                }
            }

            var result = original.call(entity, damageSource, amount);
            for (var spellEntry : spellEntries) {
                performImpacts(spellEntry, entity, entityHitResult);
            }
            if (owner instanceof Player shooter) {
                SpellTriggers.onArrowImpact((ArrowExtension) arrow, shooter, entity, damageSource, amount);
            }

            if (pushedKnockback) {
                ((ConfigurableKnockback) entity).popKnockbackMultiplier_SpellEngine();
            }
            if (iFrameToRestore != 0) {
                entity.invulnerableTime = iFrameToRestore;
            }
            return result;
        }
    }

    private void performImpacts(Holder<Spell> spellEntry, Entity target, EntityHitResult entityHitResult) {
        var arrow = arrow();
        var owner = arrow.getOwner();
        if (spellEntry != null
                && spellEntry.value().impacts != null
                && owner instanceof LivingEntity shooter) {
            SpellHelper.arrowImpact(shooter, arrow, target, spellEntry,
                    new SpellHelper.ImpactContext().position(entityHitResult.getLocation()));
        }
    }
}
