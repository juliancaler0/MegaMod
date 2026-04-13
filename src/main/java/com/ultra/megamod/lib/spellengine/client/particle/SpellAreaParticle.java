package com.ultra.megamod.lib.spellengine.client.particle;



import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineParticles;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SpellAreaParticle extends SingleQuadParticle {
    @Nullable Entity followEntity;
    public SpellEngineParticles.Fading fading = SpellEngineParticles.Fading.NONE;
    public SpellEngineParticles.Orientation orientation = SpellEngineParticles.Orientation.HORIZONTAL;
    private final SpriteSet spriteProvider;
    private float initialAlpha = 1F;
    private float initialQuadSize = 1F;
    private Vec3 ownerPositionDiff = Vec3.ZERO;
    private boolean skipRender = false;
    private boolean scaleWithEntity = false;

    protected SpellAreaParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.get(world.random));
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;

        this.spriteProvider = spriteProvider;
        this.setSpriteFromAge(spriteProvider);
    }

    private void postInit() {
        initialAlpha = this.alpha;
        initialQuadSize = this.quadSize;
        if (followEntity != null) {
            ownerPositionDiff = new Vec3(
                    this.x - followEntity.getX(),
                    this.y - followEntity.getY(),
                    this.z - followEntity.getZ()
            );
            checkSkip();
        }
    }

    public int getLightColor(float tint) {
        return 255;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        if (followEntity != null && !followEntity.isRemoved()) {
            this.ownerPositionDiff = ownerPositionDiff.add(dx, dy, dz);
            var newPos = followEntity.position().add(ownerPositionDiff);
            this.setPos(newPos.x, newPos.y, newPos.z);
        } else {
            super.move(dx, dy, dz);
        }
    }


    @Override
    public void tick() {
        super.tick();
        checkSkip();
        if (this.age >= this.lifetime) {
            // this.markDead();
        } else {
            this.setSpriteFromAge(this.spriteProvider);
            this.updateAlpha(fading, initialAlpha);
            this.updateScale();
        }
    }

    private void checkSkip() {
        this.skipRender = this.orientation == SpellEngineParticles.Orientation.VERTICAL
                && (this.followEntity == Minecraft.getInstance().getCameraEntity())
                && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    private void updateScale() {
        if (this.scaleWithEntity && this.followEntity != null && !this.followEntity.isRemoved() && this.followEntity instanceof LivingEntity livingEntity) {
            this.quadSize = initialQuadSize * livingEntity.getScale();
        } else {
            this.quadSize = initialQuadSize;
        }
    }

    private static final float EASE_DURATION = 0.3F;
    private static final float EASE_DURATION_INVERSE = 1 / EASE_DURATION;
    private void updateAlpha(SpellEngineParticles.Fading fading, float initialAlpha) {
        switch (fading) {
            case NONE -> {
                return;
            }
            case IN -> {
                var progress = this.age / ((float)this.lifetime);
                this.alpha = initialAlpha * Math.min(1, progress * EASE_DURATION_INVERSE);
            }
            case OUT -> {
                var progress = this.age / ((float)this.lifetime);
                this.alpha = initialAlpha * Math.min(1, (1 - progress) * EASE_DURATION_INVERSE);
            }
            case IN_OUT -> {
                var progress = this.age / ((float)this.lifetime);
                this.alpha = initialAlpha * Math.min(1, Math.min(progress, 1 - progress) * EASE_DURATION_INVERSE);
            }
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    // Rendering uses the default SingleQuadParticle render path
    // Horizontal orientation rendering is deferred to a future update

    public static class Factory implements ParticleProvider<TemplateParticleType> {

        private final SpriteSet spriteProvider;
        private final SpellEngineParticles.Texture texture;
        private final SpellEngineParticles.Fading fading;
        private final SpellEngineParticles.Orientation orientation;
        private final boolean isAura;

        public Factory(SpriteSet spriteProvider, SpellEngineParticles.Texture texture, SpellEngineParticles.Fading fading, SpellEngineParticles.Orientation orientation) {
            this.spriteProvider = spriteProvider;
            this.texture = texture;
            this.fading = fading;
            this.orientation = orientation;
            this.isAura = texture.id().toString().contains("aura");
        }

        public Particle createParticle(TemplateParticleType particleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellAreaParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            particle.xd = g;
            particle.yd = h;
            particle.zd = i;
            particle.gravity = 0;
            particle.orientation = this.orientation;

            particle.rCol = 1F;
            particle.gCol = 1F;
            particle.bCol = 1F;

            if (texture.frames() > 1) {
                particle.lifetime = texture.frames();
            } else {
                particle.lifetime = 16;
            }
            particle.quadSize = 1F;

            TemplateParticleType.apply(particleType, particle);
            var appearance = particleType.getAppearance();
            if (appearance != null) {
                var color = appearance.color;
                if (color != null) {
                    particle.alpha *= appearance.color.alpha();
                }
                particle.quadSize *= appearance.scale;
                particle.lifetime = (int) (particle.lifetime * appearance.max_age);
                particle.followEntity = appearance.entityFollowed;
            }

            particle.fading = fading;
            particle.scaleWithEntity = isAura;
            particle.postInit();
            return particle;
        }
    }
}
