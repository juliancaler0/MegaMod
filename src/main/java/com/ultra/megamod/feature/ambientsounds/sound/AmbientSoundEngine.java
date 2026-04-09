package com.ultra.megamod.feature.ambientsounds.sound;

import com.ultra.megamod.MegaMod;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import it.unimi.dsi.fastutil.doubles.Double2DoubleMap.Entry;
import it.unimi.dsi.fastutil.doubles.Double2DoubleRBTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2DoubleSortedMap;
import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import com.ultra.megamod.feature.ambientsounds.environment.AmbientEnvironment;
import com.ultra.megamod.feature.ambientsounds.sound.AmbientSound.SoundStream;

public class AmbientSoundEngine {

    private static final Minecraft mc = Minecraft.getInstance();

    private final List<SoundStream> sounds = new CopyOnWriteArrayList<>();

    public int playingCount() {
        return sounds.size();
    }

    public SoundManager getManager() {
        return mc.getSoundManager();
    }

    public void tick(AmbientEnvironment env) {
        // Is still playing
        Double2DoubleSortedMap mutes = new Double2DoubleRBTreeMap(DoubleComparators.OPPOSITE_COMPARATOR);
        for (SoundStream sound : sounds) {
            double soundMute = sound.mute();
            if (soundMute > 0)
                mutes.mergeDouble(sound.mutePriority(), soundMute, (x, y) -> Math.max(x, y));
        }

        List<SoundStream> toRemove = new ArrayList<>();
        for (SoundStream sound : sounds) {

            boolean playing;
            if (!getManager().isActive(sound))
                if (sound.hasPlayedOnce())
                    playing = false;
                else
                    continue;
            else
                playing = true;

            if (sound.hasPlayedOnce() && !playing) {
                sound.onFinished();
                getManager().stop(sound);
                toRemove.add(sound);
                continue;
            } else if (!sound.hasPlayedOnce() && playing)
                sound.setPlayedOnce();

            if (mutes.isEmpty())
                sound.effectiveVolume = (float) sound.combinedVolume();
            else {
                double mute = 0;
                for (Entry muteEntry : mutes.double2DoubleEntrySet()) {
                    if (sound.mutePriority() < muteEntry.getDoubleKey() || sound.mute() == 0)
                        mute = Math.max(muteEntry.getDoubleValue(), mute);
                    else
                        break;
                }
                sound.effectiveVolume = (float) (sound.combinedVolume() * (1 - mute));
            }

        }
        sounds.removeAll(toRemove);
    }

    public void stop(SoundStream sound) {
        getManager().stop(sound);
        sounds.remove(sound);
    }

    public void play(SoundStream stream) {
        getManager().play(stream);
        stream.onStart();
        sounds.add(stream);
    }

    public void stopAll() {
        List<SoundStream> snapshot = new ArrayList<>(sounds);
        for (SoundStream sound : snapshot) {
            getManager().stop(sound);
            sound.onFinished();
        }
        sounds.removeAll(snapshot);
    }

}
