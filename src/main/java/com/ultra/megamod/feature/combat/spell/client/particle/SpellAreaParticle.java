package com.ultra.megamod.feature.combat.spell.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * Advanced area effect particle with horizontal/vertical orientation, entity following,
 * fading modes, and first-person skip.
 * Ported 1:1 from SpellEngine (net.spell_engine.client.particle.SpellAreaParticle).
 */
public class SpellAreaParticle extends SingleQuadParticle {
    @Nullable Entity followEntity;
    public SpellParticleEnums.Fading fading = SpellParticleEnums.Fading.NONE;
    public SpellParticleEnums.Orientation orientation = SpellParticleEnums.Orientation.HORIZONTAL;
    private final SpriteSet sprites;
    private float initialAlpha = 1F;
    private float initialScale = 1F;
    private Vec3 ownerPositionDiff = Vec3.ZERO;
    private boolean skipRender = false;
    private boolean scaleWithEntity = false;

    protected SpellAreaParticle(ClientLevel level, double x, double y, double z,
                                double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd, sprites.get(level.random));
        this.xd = xd; this.yd = yd; this.zd = zd;
        this.sprites = sprites;
        this.setSpriteFromAge(sprites);
    }

    private void postInit() {
        initialAlpha = this.alpha;
        initialScale = this.quadSize;
        if (followEntity != null) {
            ownerPositionDiff = new Vec3(
                    this.x - followEntity.getX(),
                    this.y - followEntity.getY(),
                    this.z - followEntity.getZ());
            checkSkip();
        }
    }

    @Override
    public int getLightColor(float tint) { return 0xF000F0; }

    @Override
    public void move(double dx, double dy, double dz) {
        if (followEntity != null && !followEntity.isRemoved()) {
            this.ownerPositionDiff = ownerPositionDiff.add(dx, dy, dz);
            var newPos = followEntity.position().add(ownerPositionDiff);
            this.setPos(newPos.x, newPos.y, newPos.z);
        } else {
            this.setBoundingBox(this.getBoundingBox().move(dx, dy, dz));
            this.setLocationFromBoundingbox();
        }
    }

    @Override
    public void tick() {
        super.tick();
        checkSkip();
        if (this.age < this.lifetime) {
            this.setSpriteFromAge(this.sprites);
            updateAlpha();
            updateScale();
        }
    }

    private void checkSkip() {
        this.skipRender = this.orientation == SpellParticleEnums.Orientation.VERTICAL
                && (this.followEntity == Minecraft.getInstance().getCameraEntity())
                && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    private void updateScale() {
        if (scaleWithEntity && followEntity != null && !followEntity.isRemoved()
                && followEntity instanceof LivingEntity living) {
            this.quadSize = initialScale * living.getScale();
        } else {
            this.quadSize = initialScale;
        }
    }

    private static final float EASE_DURATION = 0.3F;
    private static final float EASE_INV = 1 / EASE_DURATION;

    private void updateAlpha() {
        float progress = this.age / (float) this.lifetime;
        this.alpha = switch (fading) {
            case NONE -> initialAlpha;
            case IN -> initialAlpha * Math.min(1, progress * EASE_INV);
            case OUT -> initialAlpha * Math.min(1, (1 - progress) * EASE_INV);
            case IN_OUT -> initialAlpha * Math.min(1, Math.min(progress, 1 - progress) * EASE_INV);
        };
    }

    @Override
    public net.minecraft.client.particle.SingleQuadParticle.Layer getLayer() {
        return net.minecraft.client.particle.SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        if (this.skipRender) return;
        if (orientation == SpellParticleEnums.Orientation.HORIZONTAL) {
            renderHorizontal(buffer, camera, partialTick);
        }
    }

    private void renderHorizontal(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 camPos = camera.position();
        float f = (float) (Mth.lerp(partialTick, this.xo, this.x) - camPos.x);
        float g = (float) (Mth.lerp(partialTick, this.yo, this.y) - camPos.y);
        float h = (float) (Mth.lerp(partialTick, this.zo, this.z) - camPos.z);

        Vector3f[] verts = new Vector3f[]{
                new Vector3f(-1, -1, 0), new Vector3f(-1, 1, 0),
                new Vector3f(1, 1, 0), new Vector3f(1, -1, 0)};
        float size = this.getQuadSize(partialTick);

        Quaternionf rot = new Quaternionf().rotateXYZ((float) Math.toRadians(90f), 0, 0);
        for (int k = 0; k < 4; ++k) {
            verts[k].rotate(rot);
            verts[k].mul(size);
            verts[k].add(f, g, h);
        }

        float minU = this.getU0();
        float maxU = this.getU1();
        float minV = this.getV0();
        float maxV = this.getV1();
        int light = this.getLightColor(partialTick);

        buffer.addVertex(verts[0].x(), verts[0].y(), verts[0].z())
                .setUv(maxU, maxV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
        buffer.addVertex(verts[1].x(), verts[1].y(), verts[1].z())
                .setUv(maxU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
        buffer.addVertex(verts[2].x(), verts[2].y(), verts[2].z())
                .setUv(minU, minV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
        buffer.addVertex(verts[3].x(), verts[3].y(), verts[3].z())
                .setUv(minU, maxV).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
    }

    // ═══════════════════════════════════════════
    // Factory
    // ═══════════════════════════════════════════

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final SpellParticleEnums.Fading fading;
        private final SpellParticleEnums.Orientation orientation;
        private final boolean isAura;

        public Factory(SpriteSet sprites, SpellParticleEnums.Fading fading,
                       SpellParticleEnums.Orientation orientation, boolean isAura) {
            this.sprites = sprites;
            this.fading = fading;
            this.orientation = orientation;
            this.isAura = isAura;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellAreaParticle(level, x, y, z, xd, yd, zd, this.sprites);
            p.xd = xd; p.yd = yd; p.zd = zd;
            p.orientation = this.orientation;
            p.rCol = 1F; p.gCol = 1F; p.bCol = 1F;
            p.lifetime = 16;
            p.quadSize = 1F;

            TemplateParticleType.Appearance appearance = TemplateParticleType.consumeAppearance();
            if (appearance != null) {
                TemplateParticleType.apply(appearance, p);
                if (appearance.color != null) p.alpha *= appearance.color.alpha();
                p.quadSize *= appearance.scale;
                p.lifetime = (int) (p.lifetime * appearance.max_age);
                p.followEntity = appearance.entityFollowed;
            }

            p.fading = fading;
            p.scaleWithEntity = isAura;
            p.postInit();
            return p;
        }
    }
}
