package com.ultra.megamod.lib.spellengine.client.particle;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleOptions;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import org.jetbrains.annotations.Nullable;

public interface TemplateParticleEffect extends ParticleOptions {
    class Appearance {
        public @Nullable Color color;
        public float scale = 1;
        public Entity entityFollowed;
        public boolean grounded = false;
        public float max_age = 1;
    }

    void setAppearance(Appearance appearance);
    Appearance getAppearance();

    default Appearance createOrDefaultAppearance() {
        var appearance = getAppearance() == null ? new Appearance() : getAppearance();
        setAppearance(appearance);
        return appearance;
    }

    TemplateParticleEffect copy();
}
