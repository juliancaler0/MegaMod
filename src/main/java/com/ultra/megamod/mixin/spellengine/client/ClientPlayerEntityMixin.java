package com.ultra.megamod.mixin.spellengine.client;


import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.effect.EntityActionsAllowed;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.PlatformClient;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;
import com.ultra.megamod.lib.spellengine.client.animation.AnimatablePlayer;
import com.ultra.megamod.lib.spellengine.client.input.SpellHotbar;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterClient;
import com.ultra.megamod.lib.spellengine.internals.melee.Melee;
import com.ultra.megamod.lib.spellengine.internals.melee.OrientedBoundingBox;
import com.ultra.megamod.lib.spellengine.internals.target.SpellTarget;
import com.ultra.megamod.lib.spellengine.network.Packets;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin implements SpellCasterClient {
    @Shadow @Final public ClientPacketListener connection;
    private SpellTarget.SearchResult spellTarget = SpellTarget.SearchResult.empty();

    private LocalPlayer player() {
        return (LocalPlayer) ((Object) this);
    }

    private Entity firstTarget() {
        return spellTarget.entities().stream().findFirst().orElse(null);
    }

    @Override
    @Nullable public SpellCast.Process getSpellCastProcess() {
        return spellCastProcess;
    }

    @Override
    public Spell getCurrentSpell() {
        if (spellCastProcess != null) {
            return spellCastProcess.spell().value();
        }
        return null;
    }

    @Override
    public float getCurrentCastingSpeed() {
        if (spellCastProcess != null) {
            return spellCastProcess.speed();
        }
        return 1F;
    }

    public boolean isCastingSpell() {
        return spellCastProcess != null;
    }

    @Nullable private SpellCast.Process spellCastProcess;

    private void setSpellCastProcess(SpellCast.Process newValue, boolean sync) {
        var oldValue = spellCastProcess;
        spellCastProcess = newValue;
        if (sync && !Objects.equals(oldValue, newValue)) {
            Identifier id = null;
            float speed = 0;
            int length = 0;
            if (newValue != null) {
                id = newValue.spell().unwrapKey().get().identifier();
                speed = newValue.speed();
                length = newValue.length();
            }
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Packets.SpellCastSync(id, speed, length));
        }
    }

    public SpellCast.Attempt startSpellCast(ItemStack itemStack, Holder<Spell> spellEntry) {
        var caster = player();
        if (caster.isSpectator()) {
            return SpellCast.Attempt.none();
        }
        if (spellEntry == null) {
            this.cancelSpellCast();
            return SpellCast.Attempt.none();
        }
        var spell = spellEntry.value();
        var spellId = spellEntry.unwrapKey().get().identifier();
        if ((spellCastProcess != null && spellCastProcess.id().equals(spellId))
                || spell == null) {
            return SpellCast.Attempt.none();
        }
        if (EntityActionsAllowed.isImpaired(caster, EntityActionsAllowed.Player.CAST_SPELL, true)) {
            return SpellCast.Attempt.none();
        }
        var attempt = SpellHelper.attemptCasting(caster, itemStack, spellId);
        if (attempt.isSuccess()) {
            if (spellCastProcess != null) {
                // Cancel previous spell
                cancelSpellCast(false);
            }
            var instant = SpellHelper.isInstantCast(spellEntry, caster);
            if (instant) {
                // Release instant spell
                var process = new SpellCast.Process(caster, spellEntry, itemStack.getItem(), 1, 0, caster.level().getGameTime());
                this.setSpellCastProcess(process, false);
                this.updateSpellCast();
                applyInstantGlobalCooldown();
            } else {
                // Start casting
                var details = SpellHelper.getCastTimeDetails(caster, spell);
                setSpellCastProcess(new SpellCast.Process(caster, spellEntry, itemStack.getItem(), details.speed(), details.length(), caster.level().getGameTime()), true);
            }
        }
        return attempt;
    }

    private void applyInstantGlobalCooldown() {
        var duration = SpellEngineMod.config.spell_instant_cast_global_cooldown;
        if (duration > 0) {
            for (var slot: SpellHotbar.INSTANCE.slots) {
                var spellEntry = slot.spell();
                if (spellEntry == null) {
                    // Some slots may not have spells (such as item usage bypass slot)
                    continue;
                }
                var spell = spellEntry.value();
                if (spell.active != null && spell.active.cast != null && spell.active.cast.duration <= 0) {
                    getCooldownManager().set(spellEntry, duration, false);
                }
            }
        }
    }

    @Nullable public SpellCast.Progress getSpellCastProgress() {
        if (spellCastProcess != null) {
            var player = player();
            return spellCastProcess.progress(player.level().getGameTime());
        }
        return null;
    }

    public void cancelSpellCast() {
        cancelSpellCast(true);
    }
    public void cancelSpellCast(boolean syncProcess) {
        var process = spellCastProcess;
        if (process != null) {
            if (SpellHelper.isChanneled(process.spell().value())) {
                var player = player();
                var progress = process.progress(player.level().getGameTime());
                net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Packets.SpellRequest(SpellCast.Action.RELEASE, process.id(), progress.ratio(), new int[]{}, null));
            }
        }

        setSpellCastProcess(null, syncProcess);
        spellTarget = SpellTarget.SearchResult.empty();
    }

    private void updateSpellCast() {
        var process = spellCastProcess;
        if (process != null) {
            var player = player();
            if (!player().isAlive()
                    || player.getMainHandItem().getItem() != process.item()
                    || getCooldownManager().isCoolingDown(process.spell())
                    || EntityActionsAllowed.isImpaired(player, EntityActionsAllowed.Player.CAST_SPELL, true)
            ) {
                cancelSpellCast();
                return;
            }
            var spell = process.spell().value();
            var cast = spell.active.cast;
            spellTarget = SpellTarget.findTargets(player, process.spell(), spellTarget, SpellEngineClient.config.filterInvalidTargets);

            if (SpellHelper.isChanneled(spell)) {
                // System.out.println("Channeling tick: " + process.spellCastTicksSoFar(player.level().getGameTime()) + " ticks, isDue: " + process.isDue(player.level().getGameTime()));
                if (process.isDue(player.level().getGameTime())) {
                    process.markDue();
                    releaseSpellCast(process, SpellCast.Action.CHANNEL);
                }
                var progress = process.progress(player.level().getGameTime());
                if (progress.ratio() >= 1) {
                    cancelSpellCast();
                }
            } else {
                var spellCastTicks = process.spellCastTicksSoFar(player.level().getGameTime());
                var isFinished = spellCastTicks >= process.length();
                if (isFinished) {
                    // Release spell
                    releaseSpellCast(process, SpellCast.Action.RELEASE);
                }
            }
        } else {
            spellTarget = SpellTarget.SearchResult.empty();
        }
    }

    private void releaseSpellCast(SpellCast.Process process, SpellCast.Action action) {
        var spellId = process.id();
        var player = player();
        var progress = process.progress(player.level().getGameTime());
        var targets = spellTarget.entities();
        var location = spellTarget.location();
        int[] targetIDs = new int[targets.size()];
        int i = 0;
        for (var target : targets) {
            targetIDs[i] = target.getId();
            i += 1;
        }

        net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Packets.SpellRequest(action, spellId, progress.ratio(), targetIDs, location));
        switch (action) {
            case CHANNEL -> {
                if (progress.ratio() >= 1) {
                    cancelSpellCast();
                }
            }
            case RELEASE -> {
                cancelSpellCast();
            }
        }
    }

    public List<Entity> getCurrentTargets() {
        var targets = spellTarget.entities();
        if (targets == null) {
            return List.of();
        }
        return targets;
    }

    public Entity getCurrentFirstTarget() {
        return firstTarget();
    }


    private Melee.ActiveAttack currentAttack = null;
    private List<Melee.Attack> scheduledAttacks = new ArrayList<>();
    public void onAttacksAvailable(List<Melee.Attack> attacks) {
        scheduledAttacks.addAll(attacks);
    }
    public Melee.ActiveAttack getCurrentSkillAttack() {
        return currentAttack;
    }
    @Unique
    private void onTick_ScheduledAttacks(LocalPlayer player) {
        var time = player.tickCount;
        if (EntityActionsAllowed.isImpaired(player, EntityActionsAllowed.Player.ATTACK)) {
            currentAttack = null;
            return;
        }
        checkForNextAttack(player, time);
        if (currentAttack != null) {
            if (currentAttack.weapon != player.getMainHandItem().getItem()) {
                // Weapon changed, cancel attack
                currentAttack = null;
                return;
            }
            if (currentAttack.isDue(time)) {
                onAttackHit(currentAttack);
            }
            if (currentAttack.isFinished(time)) {
                currentAttack = null;
                checkForNextAttack(player, time);
            }
        }
    }
    private void checkForNextAttack(LocalPlayer player, int time) {
        if (currentAttack == null) {
            if (!scheduledAttacks.isEmpty()) {
                var attack = scheduledAttacks.remove(0);
                currentAttack = new Melee.ActiveAttack(attack, time, player.getMainHandItem().getItem());
                onAttackActivated(attack);
            }
        }
    }

    @Unique
    private void onAttackActivated(Melee.Attack attack) {
        // On attack started

        var player = player();
        var momentum = attack.forward_momentum();
        if (momentum > 0
                && (attack.allow_momentum_airborne() || player.onGround()) ) {
            var direction = new Vec3(0, 0, 1)
                    .yRot((float) Math.toRadians((-1.0) * player.getYRot()))
                    .scale(attack.forward_momentum());
            player.push(direction.x, direction.y, direction.z);
        }

        if (attack.context() != null) {
            var animationSpeed = attack.speed() * attack.animation().speed;
            ((AnimatablePlayer)this).playSpellAnimation(SpellCast.Animation.RELEASE, attack.animation().id, animationSpeed);
            var packet = new Packets.AttackFxBroadcast(attack.context());
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(packet);
        }
    }
    @Unique
    private void onAttackHit(Melee.ActiveAttack activeAttack) {
        var player = player();
        var attack = activeAttack.attack;
        var targets = Melee.detectTargets(player, attack);
        if (!attack.additional_hits_on_same_target()) {
            targets = targets.stream().filter(id -> !activeAttack.hitEntityIds.contains(id)).toList();
        }
        activeAttack.hitEntityIds.addAll(targets);
        if (!targets.isEmpty()) {
            var targetIds = targets.stream().mapToInt(Integer::intValue).toArray();
            var context = attack.context() != null ? attack.context() : Melee.AttackContext.EMPTY;
            var packet = new Packets.AttackPerform(context, targetIds);
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(packet);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick_TAIL_SpellEngine(CallbackInfo ci) {
        updateSpellCast();
        var player = player();
        if (isBeaming()) {
            PlatformClient.util().sendVanillaPacket_C2S(player, new ServerboundMovePlayerPacket.PosRot(
                    player.getX(), player.getY(), player.getZ(),
                    player.getYRot(), player.getXRot(),
                    player.onGround(), player.horizontalCollision)
            );
        }
        onTick_ScheduledAttacks(player);
    }
}