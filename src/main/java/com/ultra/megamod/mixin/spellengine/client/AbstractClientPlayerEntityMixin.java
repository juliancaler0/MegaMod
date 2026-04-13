package com.ultra.megamod.mixin.spellengine.client;

// TODO: 1.21.11 - PlayerAnimator library was refactored
// IAnimatedPlayer, KeyframeAnimationPlayer, KeyframeAnimation, PlayerAnimationRegistry
// are no longer available. The playeranim lib now uses IAnimatedAvatar / AvatarAnimManager.
// This mixin needs to be rewritten to use the new playeranim API.
// For now, the animation parts are stubbed out.

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
        // TODO: 1.21.11 - Animation stack registration disabled until playeranim API is updated
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
        // TODO: 1.21.11 - Animation playback disabled until playeranim API is updated
    }

    private boolean isMounting_SpellEngine() {
        return this.getVehicle() != null;
    }

    public boolean isLeftHanded_SpellEngine() {
        return this.getMainArm() == HumanoidArm.LEFT;
    }
}
