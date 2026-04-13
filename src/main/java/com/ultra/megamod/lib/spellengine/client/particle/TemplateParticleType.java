package com.ultra.megamod.lib.spellengine.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public class TemplateParticleType extends ParticleType<TemplateParticleType> implements ParticleOptions, TemplateParticleEffect {
    private final MapCodec<TemplateParticleType> mapCodec = MapCodec.unit(this::getType);
    private final StreamCodec<RegistryFriendlyByteBuf, TemplateParticleType> packetCodec = StreamCodec.unit(this);

    private TemplateParticleType type;
    public TemplateParticleType() {
        this(true);
    }

    public TemplateParticleType(boolean alwaysShow) {
        super(alwaysShow);
        this.type = this;
    }

    public TemplateParticleType getType() {
        return this.type;
    }

    @Override
    public MapCodec<TemplateParticleType> codec() {
        return this.mapCodec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, TemplateParticleType> streamCodec() {
        return packetCodec;
    }


    private TemplateParticleEffect.Appearance appearance = new TemplateParticleEffect.Appearance();

    @Override
    public void setAppearance(Appearance appearance) {
        this.appearance = appearance;
    }
    @Override
    public Appearance getAppearance() {
        return appearance;
    }
    @Override
    public TemplateParticleEffect copy() {
        var copy = new TemplateParticleType(true);
        copy.type = this.type;
        return copy;
    }

    public static void apply(TemplateParticleType templateParticleType, Particle particle) {
        var appearance = templateParticleType.getAppearance();
        if (appearance != null) {
            var color = appearance.color;
            if (color != null) {
                if (particle instanceof SingleQuadParticle sqp) {
                    sqp.setColor(color.red(), color.green(), color.blue());
                }
            }
        }
    }
}

