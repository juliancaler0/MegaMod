package com.ultra.megamod.feature.ambientsounds.region;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import net.minecraft.server.packs.resources.ResourceManager;
import com.ultra.megamod.feature.ambientsounds.condition.AmbientCondition;
import com.ultra.megamod.feature.ambientsounds.condition.AmbientSelection;
import com.ultra.megamod.feature.ambientsounds.dimension.AmbientDimension;
import com.ultra.megamod.feature.ambientsounds.engine.AmbientEngine;
import com.ultra.megamod.feature.ambientsounds.engine.AmbientEngineLoadException;
import com.ultra.megamod.feature.ambientsounds.engine.AmbientStackType;
import com.ultra.megamod.feature.ambientsounds.environment.AmbientEnvironment;
import com.ultra.megamod.feature.ambientsounds.sound.AmbientSound;

public class AmbientRegion extends AmbientCondition {

    public String name;
    public AmbientStackType stack = AmbientStackType.overwrite;
    public AmbientSound[] sounds;
    @SerializedName("sound-collections")
    public String[] soundCollections;

    public transient double volumeSetting = 1;

    protected transient boolean active;
    public transient LinkedHashMap<String, AmbientSound> loadedSounds;
    public transient List<AmbientSound> playing = new ArrayList<>();

    public transient AmbientDimension dimension;

    public AmbientRegion() {}

    public void load(AmbientEngine engine, Gson gson, ResourceManager manager) throws AmbientEngineLoadException {
        if (sounds != null) {
            loadedSounds = new LinkedHashMap<>();
            for (int i = 0; i < sounds.length; i++) {
                AmbientSound sound = sounds[i];
                loadedSounds.put(sound.name, sound);
            }
        }
    }

    @Override
    public String regionName() {
        return name;
    }

    @Override
    public void init(AmbientEngine engine) throws AmbientEngineLoadException {
        super.init(engine);

        if (loadedSounds != null)
            for (AmbientSound sound : loadedSounds.values())
                sound.init(engine);

        if (soundCollections != null) {
            if (loadedSounds == null)
                loadedSounds = new LinkedHashMap<>();
            engine.consumeSoundCollections(soundCollections, x -> loadedSounds.put(x.name, x.copy()));
        }
    }

    @Override
    public AmbientSelection value(AmbientEnvironment env) {
        if (dimension != null && dimension != env.dimension)
            return null;
        if (volumeSetting == 0)
            return null;
        AmbientSelection selection = super.value(env);
        if (selection != null)
            selection.mulSetting(volumeSetting);
        return selection;
    }

    public boolean fastTick(AmbientEnvironment env) {
        if (!playing.isEmpty()) {
            for (Iterator<AmbientSound> iterator = playing.iterator(); iterator.hasNext();) {
                AmbientSound sound = iterator.next();
                if (!sound.fastTick(env)) {
                    sound.deactivate();
                    iterator.remove();
                }
            }
        }

        return !playing.isEmpty();
    }

    public boolean tick(AmbientEnvironment env) {

        if (loadedSounds == null)
            return false;

        AmbientSelection selection = value(env);
        for (AmbientSound sound : loadedSounds.values()) {
            if (sound.tick(env, selection)) {
                if (!sound.isActive()) {
                    sound.activate();
                    playing.add(sound);
                }
            } else if (sound.isActive()) {
                sound.deactivate();
                playing.remove(sound);
            }
        }

        return !playing.isEmpty();
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;

        if (!playing.isEmpty()) {
            for (AmbientSound sound : playing)
                sound.deactivate();
            playing.clear();
        }
    }

    @Override
    public String toString() {
        return name + ", playing: " + playing.size();
    }

}
