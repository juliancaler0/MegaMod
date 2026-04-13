package com.ultra.megamod.mixin.spellengine.entity;

import com.google.gson.Gson;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Holder;
import com.ultra.megamod.lib.spellengine.api.effect.SpellEngineSyncAttachments;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.client.animation.AnimatablePlayer;
import com.ultra.megamod.lib.spellengine.internals.*;
import com.ultra.megamod.lib.spellengine.internals.arrow.ArrowShootContext;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCastSyncHelper;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import com.ultra.megamod.lib.spellengine.internals.melee.Melee;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.ultra.megamod.lib.spellengine.api.spell.Spell.Target.Type.BEAM;

@Mixin(value = Player.class, priority = 555)
public class PlayerEntityMixin implements SpellCasterEntity {

    private Player player() {
        return (Player) ((Object) this);
    }

    private final SpellCooldownManager spellCooldownManager = new SpellCooldownManager(player());

    // Uses NeoForge data attachments instead of SynchedEntityData.defineId()
    // (NeoForge 21.11 blocks defineId on foreign entities)
    private static final Gson syncGson = new Gson();

    private SpellCast.Process synchronizedSpellCastProcess = null;
    public void setSpellCastProcess(@Nullable SpellCast.Process process) {
        if (process != null && process.spell().value().active == null) { return; }
        synchronizedSpellCastProcess = process;
        var json = process != null ? process.fastSyncJSON() : "";
        player().setData(SpellEngineSyncAttachments.SPELL_PROGRESS.get(), json);
    }

    private int channelTickIndex = 0;
    @Override
    public void setChannelTickIndex(int channelTickIndex) {
        this.channelTickIndex = channelTickIndex;
    }
    @Override
    public int getChannelTickIndex() {
        return channelTickIndex;
    }

    @Nullable public SpellCast.Process getSpellCastProcess() {
        return synchronizedSpellCastProcess;
    }

    @Override
    public Spell getCurrentSpell() {
        var process = getSpellCastProcess();
        if (process != null) {
            return process.spell().value();
        }
        return null;
    }

    @Override
    public float getCurrentCastingSpeed() {
        var process = getSpellCastProcess();
        if (process != null) {
            return process.speed();
        }
        return 1F; // Fallback value
    }

    private ArrowShootContext arrowShotContext = ArrowShootContext.empty();
    @Override
    public void setArrowShootContext(ArrowShootContext shotContext) {
        arrowShotContext = shotContext;
    }
    @Override
    public ArrowShootContext getArrowShootContext() {
        return arrowShotContext;
    }

    @Override
    public SpellCooldownManager getCooldownManager() {
        return spellCooldownManager;
    }

    private String lastHandledSyncData = "";

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick_TAIL_SpellEngine(CallbackInfo ci) {
        var player = player();
        if (player.level().isClientSide()) {
            ((AnimatablePlayer)player()).updateSpellCastAnimationsOnTick();

            // Check changes in tracked data (via NeoForge attachment)
            var progressString = player.getData(SpellEngineSyncAttachments.SPELL_PROGRESS.get());
            if (!progressString.equals(lastHandledSyncData)) {
                if (progressString.isEmpty()) {
                    this.synchronizedSpellCastProcess = null;
                } else {
                    var syncFormat = syncGson.fromJson(progressString, SpellCast.Process.SyncFormat.class);
                    this.synchronizedSpellCastProcess = SpellCast.Process.fromSync(player, player.level(), syncFormat, player.getMainHandItem().getItem(), player.level().getGameTime());
                }
                lastHandledSyncData = progressString;
            }

        } else {
            // Server side
            if (synchronizedSpellCastProcess != null) {
                var castTicks = synchronizedSpellCastProcess.spellCastTicksSoFar(player.level().getGameTime());
                if (castTicks >= (synchronizedSpellCastProcess.length() * 1.5)) {
                    SpellCastSyncHelper.clearCasting(player);
                }
            }
            if (activeAttack_serverSide != null
                    // Offsetting time by 1 tick, to compensate sync delays
                    && activeAttack_serverSide.isFinished(player.tickCount + 1)) {
                setMeleeSkillAttack(null);
            }
        }
        spellCooldownManager.tickUpdate();
    }

    public boolean isBeaming() {
        return getBeam() != null;
    }

    @Nullable
    public Spell.Target.Beam getBeam() {
        var spell = getCurrentSpell();
        if (spell != null && spell.target != null && spell.target.type == BEAM) {
            return spell.target.beam;
        }
        return null;
    }

    private Melee.ActiveAttack activeAttack_serverSide = null;
    @Override
    public void setMeleeSkillAttack(Melee.ActiveAttack attack) {
        activeAttack_serverSide = attack;
        float slip = 0;
        if (attack != null) {
            slip = attack.attack.movement_slip();
        }
        player().setData(SpellEngineSyncAttachments.EXTRA_SLIPPERINESS.get(), slip);
    }
    @Override
    public float getExtraSlipperiness() {
        return player().getData(SpellEngineSyncAttachments.EXTRA_SLIPPERINESS.get());
    }

    @Nullable private Holder<Spell> activeMeleeSpell = null;
    public void setActiveMeleeSkill(Holder<Spell> spell) {
        activeMeleeSpell = spell;
    }
    public Holder<Spell> getActiveMeleeSkill() {
        return activeMeleeSpell;
    }

    // MARK: Persistence

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void writeCustomDataToNbt_TAIL_SpellEngine(net.minecraft.world.level.storage.ValueOutput output, CallbackInfo ci) {
        var nbt = new CompoundTag();
        spellCooldownManager.writeCustomDataToNbt(nbt);
        output.store("SpellEngineCooldowns", CompoundTag.CODEC, nbt);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readCustomDataFromNbt_TAIL_SpellEngine(net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
        var nbt = input.read("SpellEngineCooldowns", CompoundTag.CODEC).orElse(new CompoundTag());
        spellCooldownManager.readCustomDataFromNbt(nbt);
    }
}
