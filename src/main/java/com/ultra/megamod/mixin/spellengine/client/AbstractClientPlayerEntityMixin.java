package com.ultra.megamod.mixin.spellengine.client;

// Note: this mixin's <init> inject (postInit_SpellEngine, line 45) no longer registers
// animation layers directly — that responsibility moved to
// {@link com.ultra.megamod.feature.combat.animation.client.SpellAnimationManager#registerFactories}
// which subscribes to PlayerAnimationAccess.REGISTER_ANIMATION_EVENT and attaches the CASTING /
// RELEASE / MISC controllers at the right priority once the AvatarAnimManager is ready.
// The <init> inject is retained empty so any future per-player SpellEngine setup has a hook.

import com.mojang.authlib.GameProfile;
import com.ultra.megamod.lib.playeranim.core.api.firstPerson.FirstPersonMode;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.AdjustmentModifier;
import com.ultra.megamod.lib.playeranim.core.math.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import com.ultra.megamod.lib.spellengine.api.spell.fx.Sound;
import com.ultra.megamod.lib.spellengine.client.animation.*;
import com.ultra.megamod.lib.spellengine.client.sound.SpellCastingSound;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import com.ultra.megamod.mixin.spellengine.entity.LivingEntityAccessor;
import com.ultra.megamod.lib.spellengine.fx.ParticleHelper;
import com.ultra.megamod.lib.spellengine.utils.AnimationHelper;
import com.ultra.megamod.lib.spellengine.utils.StringUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerEntityMixin extends Player implements AnimatablePlayer, SpellCastingSound.Listener {
    public AbstractClientPlayerEntityMixin(Level world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    private boolean castingAnimationPitching = true;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void postInit_SpellEngine(ClientLevel world, GameProfile profile, CallbackInfo ci) {
        // Animation layer registration is handled lazily by SpellAnimationManager.getState()
        // the first time a cast/release animation is requested — safer than doing it here
        // because AvatarAnimManager may not yet exist on the AbstractClientPlayer subclass.
    }

    @Override
    public void updateSpellCastAnimationsOnTick() {
        var instance = (Object) this;
        var player = (Player) instance;

        String castAnimationName = null;
        Sound castSound = null;
        float speed = 1F;
        var spell = ((SpellCasterEntity)player).getCurrentSpell();
        if (spell != null && spell.active != null) {
            var cast = spell.active.cast;
            castAnimationName = AnimationHelper.getAnimationId(player, cast.animation);
            castSound = cast.sound;
            // turnHead was removed in 1.21.11 - body rotation is now handled differently
            for (var batch: cast.particles) {
                ParticleHelper.play(player.level(), player, player.getYRot(), getXRot(), batch);
            }
            speed = ((SpellCasterEntity)player).getCurrentCastingSpeed() * cast.animation.speed;
            castingAnimationPitching = cast.animation_pitch;
        } else {
            castingAnimationPitching = true;
        }
        updateCastingAnimation(castAnimationName, speed);
        updateCastingSound(castSound);
    }

    private String lastCastAnimationName;
    private void updateCastingAnimation(String animationName, float speed) {
        if (!StringUtil.matching(animationName, lastCastAnimationName)) {
            playSpellAnimation(SpellCast.Animation.CASTING, animationName, speed);
        }
        lastCastAnimationName = animationName;
    }

    private String lastCastSoundId;
    private SpellCastingSound lastCastSound;
    private void updateCastingSound(Sound castSound) {
        String soundId = null;
        if (castSound != null) {
            soundId = castSound.id();
        }
        if (!StringUtil.matching(soundId, lastCastSoundId)) {
            if (lastCastSound != null) {
                Minecraft.getInstance().getSoundManager().stop(lastCastSound);
                lastCastSound = null;
            }
            if (castSound != null && soundId != null && !soundId.isEmpty()) {
                var id = Identifier.parse(soundId);
                var sound = new SpellCastingSound(this, id, castSound.volume(), castSound.randomizedPitch());
                sound.listener = this;
                Minecraft.getInstance().getSoundManager().play(sound);
                lastCastSound = sound;
            }
        }
        lastCastSoundId = soundId;
    }

    public void onSpellCastingSoundDone() {
        lastCastSound = null;
        lastCastSoundId = null;
    }

    public void playSpellAnimation(SpellCast.Animation type, String name, float speed) {
        // Bridge SpellEngine animation packets/ticker into the MegaMod combat
        // SpellAnimationManager (which owns the PlayerAnimator integration).
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        com.ultra.megamod.feature.combat.animation.client.SpellAnimationManager.AnimationType target = switch (type) {
            case CASTING -> com.ultra.megamod.feature.combat.animation.client.SpellAnimationManager.AnimationType.CASTING;
            case RELEASE -> com.ultra.megamod.feature.combat.animation.client.SpellAnimationManager.AnimationType.RELEASE;
            case MISC -> com.ultra.megamod.feature.combat.animation.client.SpellAnimationManager.AnimationType.MISC;
        };

        // Empty / null name = stop currently running animation of that channel
        if (name == null || name.isEmpty()) {
            com.ultra.megamod.feature.combat.animation.client.SpellAnimationManager.stopAnimation(self, target);
            return;
        }

        // Resolve animation id. SpellEngine supplies either a bare path
        // (e.g. "one_handed_projectile_charge") or a full "namespace:path".
        Identifier animId;
        try {
            animId = name.contains(":") ? Identifier.parse(name)
                    : Identifier.fromNamespaceAndPath("megamod", name);
        } catch (Exception e) {
            return;
        }

        boolean mirror = isLeftHanded_SpellEngine();
        com.ultra.megamod.feature.combat.animation.client.SpellAnimationManager.playAnimation(
                self, target, animId, Math.max(0.01f, speed), mirror);
    }

    private boolean isMounting_SpellEngine() {
        return this.getVehicle() != null;
    }

    public boolean isLeftHanded_SpellEngine() {
        return this.getMainArm() == HumanoidArm.LEFT;
    }
}
