package com.ultra.megamod.feature.combat.spell.client.sound;

import com.ultra.megamod.feature.combat.spell.SpellCastManager;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * Looping positional sound that plays while a player is casting a spell.
 * Follows the caster's position and auto-stops when casting ends.
 * Ported 1:1 from SpellEngine (net.spell_engine.client.sound.SpellCastingSound).
 */
public class SpellCastingSound extends AbstractSoundInstance implements TickableSoundInstance {

    public interface Listener {
        void onSpellCastingSoundDone();
    }

    private final LivingEntity emitter;
    private boolean done;
    @Nullable
    public Listener listener;

    public SpellCastingSound(LivingEntity emitter, Identifier soundId, float volume, float pitch) {
        super(soundId, SoundSource.PLAYERS, emitter.getRandom());
        this.emitter = emitter;
        this.volume = volume;
        this.pitch = pitch;
        this.looping = true;
        this.delay = 0;
        this.attenuation = Attenuation.LINEAR;
        this.x = emitter.getX();
        this.y = emitter.getY();
        this.z = emitter.getZ();
        this.relative = false;
    }

    private boolean isEmitterCasting() {
        return emitter != null && emitter.isAlive()
                && SpellCastManager.isCasting(emitter.getUUID());
    }

    @Override
    public boolean isStopped() {
        return done;
    }

    protected final void setDone() {
        this.done = true;
        this.looping = false;
        this.volume = 0;
        if (listener != null) {
            listener.onSpellCastingSoundDone();
        }
    }

    @Override
    public void tick() {
        if (!isEmitterCasting()) {
            setDone();
            return;
        }
        // Follow emitter position
        this.x = emitter.getX();
        this.y = emitter.getY();
        this.z = emitter.getZ();
    }
}
