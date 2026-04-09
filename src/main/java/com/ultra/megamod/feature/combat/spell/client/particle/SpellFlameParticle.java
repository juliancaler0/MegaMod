package com.ultra.megamod.feature.combat.spell.client.particle;

import com.ultra.megamod.feature.combat.spell.client.util.Color;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

/**
 * Animated flame/spark particle with many factory variants.
 * Ported 1:1 from SpellEngine (net.spell_engine.client.particle.SpellFlameParticle).
 */
public class SpellFlameParticle extends SingleQuadParticle {
    boolean glow = true;
    boolean translucent = false;
    private SpriteSet spriteSetForAnimation = null;
    @Nullable Entity followEntity;
    private Vec3 ownerPositionDiff = Vec3.ZERO;

    public SpellFlameParticle(ClientLevel level, SpriteSet sprites, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd, sprites.get(level.random));
        this.friction = 0.96F;
        this.xd = this.xd * 0.01 + xd;
        this.yd = this.yd * 0.01 + yd;
        this.zd = this.zd * 0.01 + zd;
        this.lifetime = (int) (8.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public net.minecraft.client.particle.SingleQuadParticle.Layer getLayer() {
        return translucent ? net.minecraft.client.particle.SingleQuadParticle.Layer.TRANSLUCENT : net.minecraft.client.particle.SingleQuadParticle.Layer.TRANSLUCENT;
    }

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
    public float getQuadSize(float tickDelta) {
        float f = ((float) this.age + tickDelta) / (float) this.lifetime;
        return this.quadSize * (1.0f - f * f * 0.5f);
    }

    @Override
    public int getLightColor(float tint) {
        return glow ? 0xF000F0 : super.getLightColor(tint);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.spriteSetForAnimation != null) {
            this.setSpriteFromAge(this.spriteSetForAnimation);
        }
    }

    // ═══════════════════════════════════════════
    // Factories
    // ═══════════════════════════════════════════

    public static class FlameFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public FlameFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellFlameParticle(level, this.sprites, x, y, z, xd, yd, zd);
            
            return p;
        }
    }

    public static class AnimatedFlameFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public AnimatedFlameFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellFlameParticle(level, this.sprites, x, y, z, xd, yd, zd);
            
            p.spriteSetForAnimation = this.sprites;
            return p;
        }
    }

    public static class ColoredAnimatedFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Color color;
        private final float scale;

        public ColoredAnimatedFactory(Color color, float scale, SpriteSet sprites) {
            this.sprites = sprites;
            this.color = color;
            this.scale = scale;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellFlameParticle(level, this.sprites, x, y, z, xd, yd, zd);
            
            p.spriteSetForAnimation = this.sprites;
            float j = level.random.nextFloat() * 0.5F + 0.35F;
            p.setColor(color.red() * j, color.green() * j, color.blue() * j);
            p.quadSize = this.scale;
            p.setAlpha(1F);
            return p;
        }
    }

    public static class ElectricSparkFactory extends ColoredAnimatedFactory {
        public ElectricSparkFactory(SpriteSet sprites) {
            super(Color.ELECTRIC, 0.75F, sprites);
        }
    }

    public static class SmokeFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public SmokeFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellFlameParticle(level, this.sprites, x, y, z, xd, yd, zd);
            
            p.setColor(1F, 1F, 1F);

            // Apply appearance from context if available
            TemplateParticleType.Appearance appearance = TemplateParticleType.consumeAppearance();
            TemplateParticleType.apply(appearance, p);
            float j = level.random.nextFloat() * 0.5F + 0.35F;
            p.setColor(p.rCol * j, p.gCol * j, p.bCol * j);

            p.spriteSetForAnimation = this.sprites;
            p.friction = 0.8F;
            p.alpha *= 0.8F;
            p.glow = false;
            p.gravity = -0.01F;
            return p;
        }
    }

    public static class WeaknessSmokeFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Color color = Color.from(0x993333);

        public WeaknessSmokeFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellFlameParticle(level, this.sprites, x, y, z, xd, yd, zd);
            
            float j = level.random.nextFloat() * 0.5F + 0.35F;
            p.setColor(color.red() * j, color.green() * j, color.blue() * j);
            p.spriteSetForAnimation = this.sprites;
            p.friction = 0.8F;
            p.setAlpha(0.7F);
            p.glow = false;
            p.gravity = 0.01F;
            return p;
        }
    }

    public static class MediumFlameFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public MediumFlameFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellFlameParticle(level, this.sprites, x, y, z, xd, yd, zd);
            
            p.spriteSetForAnimation = this.sprites;
            p.quadSize = 0.5F;
            p.lifetime = (int) (p.lifetime * 0.5);
            return p;
        }
    }

    public static class FrostShardFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public FrostShardFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellFlameParticle(level, this.sprites, x, y, z, xd, yd, zd);
            
            float j = level.random.nextFloat() * 0.5F + 0.35F;
            p.setColor(Color.FROST.red() * j, Color.FROST.green() * j, Color.FROST.blue() * j);
            p.yd *= level.random.nextFloat() * 0.2F + 0.9F;
            p.lifetime = Math.round(level.random.nextFloat() * 3) + 5;
            return p;
        }
    }

    public static class ColorableFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Color color;

        public ColorableFactory(SpriteSet sprites, Color color) {
            this.sprites = sprites;
            this.color = color;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellFlameParticle(level, this.sprites, x, y, z, xd, yd, zd);
            
            float j = level.random.nextFloat() * 0.5F + 0.35F;
            p.setColor(color.red() * j, color.green() * j, color.blue() * j);
            return p;
        }
    }

    public static class HealingFactory extends ColorableFactory {
        public HealingFactory(SpriteSet sprites) { super(sprites, Color.NATURE); }
    }

    public static class HolyFactory extends ColorableFactory {
        public HolyFactory(SpriteSet sprites) { super(sprites, Color.HOLY); }
    }

    public static class NatureFactory extends ColorableFactory {
        public NatureFactory(SpriteSet sprites) { super(sprites, Color.NATURE); }
    }

    public static class BuffFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Color color;

        public BuffFactory(SpriteSet sprites, Color color) {
            this.sprites = sprites;
            this.color = color;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellFlameParticle(level, this.sprites, x, y, z, xd, yd, zd);
            
            float j = level.random.nextFloat() * 0.5F + 0.35F;
            p.setColor(color.red() * j, color.green() * j, color.blue() * j);
            p.lifetime = 16;
            p.translucent = true;
            return p;
        }
    }

    public static class BuffRageFactory extends BuffFactory {
        public BuffRageFactory(SpriteSet sprites) { super(sprites, Color.RAGE); }
    }

    public static class SignFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SignFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellFlameParticle(level, this.sprites, x, y, z, xd, yd, zd);
            
            p.friction = 0.7F;
            p.quadSize = 0.4F;
            p.alpha = 0.9F;
            p.translucent = true;
            p.rCol = 1F; p.gCol = 1F; p.bCol = 1F;
            p.lifetime = 40;

            TemplateParticleType.Appearance appearance = TemplateParticleType.consumeAppearance();
            if (appearance != null) {
                if (appearance.color != null) {
                    p.setColor(appearance.color.red(), appearance.color.green(), appearance.color.blue());
                    p.alpha *= appearance.color.alpha();
                }
                p.quadSize *= appearance.scale;
                p.followEntity = appearance.entityFollowed;
            }
            return p;
        }
    }
}
