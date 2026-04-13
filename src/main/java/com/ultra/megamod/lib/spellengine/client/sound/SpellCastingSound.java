package com.ultra.megamod.lib.spellengine.client.sound;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import org.jetbrains.annotations.Nullable;

public class SpellCastingSound extends AbstractSoundInstance implements TickableSoundInstance {
    public interface Listener {
        void onSpellCastingSoundDone();
    }

    private LivingEntity emitter;
    private boolean done;
    public @Nullable Listener listener;

    public SpellCastingSound(LivingEntity emitter, Identifier id, float volume, float pitch) {
        super(id, SoundSource.PLAYERS, emitter.getRandom());
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
        return emitter != null && emitter.isAlive() && (emitter instanceof SpellCasterEntity caster && caster.isCastingSpell());
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
        this.x = emitter.getX();
        this.y = emitter.getY();
        this.z = emitter.getZ();
    }

    @Override
    public double getX() {
        return emitter.getX();
    }

    @Override
    public double getY() {
        return emitter.getY();
    }

    @Override
    public double getZ() {
        return emitter.getZ();
    }
}
