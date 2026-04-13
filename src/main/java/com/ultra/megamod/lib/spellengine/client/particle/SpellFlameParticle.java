package com.ultra.megamod.lib.spellengine.client.particle;



import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineParticles;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;
import org.jetbrains.annotations.Nullable;

public class SpellFlameParticle extends SingleQuadParticle {
    boolean glow = true;
    boolean translucent = false;
    private SpriteSet spriteProviderForAnimation = null;
    @Nullable Entity followEntity;
    private Vec3 ownerPositionDiff = Vec3.ZERO;

    private SpriteSet sprites;
    public SpellFlameParticle(ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, SpriteSet sprites) {
        super(clientWorld, d, e, f, g, h, i, sprites.get(clientWorld.random));
        this.sprites = sprites;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return translucent
                ? SingleQuadParticle.Layer.TRANSLUCENT
                : SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        if (followEntity != null && !followEntity.isRemoved()) {

            // Updating diff with velocity, otherwise the movement would be cancelled, due to force following
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

    public int getLightColor(float tint) {
        return glow ? 255 : super.getLightColor(tint);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.spriteProviderForAnimation != null) {
            this.setSpriteFromAge(this.spriteProviderForAnimation);
        }
//        moveWithFollowed();
    }

    private void moveWithFollowed() {
        if (followEntity != null && !followEntity.isRemoved()) {
            this.x += followEntity.getX() - followEntity.xOld;
            this.y += followEntity.getY() - followEntity.yOld;
            this.z += followEntity.getZ() - followEntity.zOld;
        }
    }

    public static class FlameFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public FlameFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellFlameParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            // sprite set in constructor
            return particle;
        }
    }

    public static class AnimatedFlameFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public AnimatedFlameFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellFlameParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            // sprite set in constructor
            particle.spriteProviderForAnimation = this.spriteProvider;
            return particle;
        }
    }

    public static class ColoredAnimatedFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;
        private final Color color;
        private final float scale;
        protected float randomColorFloor = 0.5F;
        protected float randomColorRange = 0.35F;

        public ColoredAnimatedFactory(Color color, float scale, SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
            this.color = color;
            this.scale = scale;
        }

        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellFlameParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            // sprite set in constructor
            particle.spriteProviderForAnimation = this.spriteProvider;

            var red = color.red();
            var green = color.green();
            var blue = color.blue();
            if (randomColorRange > 0) {
                red = (clientWorld.random.nextFloat() * randomColorFloor + randomColorRange) * red;
                green = (clientWorld.random.nextFloat() * randomColorFloor + randomColorRange) * green;
                blue = (clientWorld.random.nextFloat() * randomColorFloor + randomColorRange) * blue;
            }
            float j = clientWorld.random.nextFloat() * 0.5F + 0.35F;
            particle.setColor(red, green, blue);
            particle.quadSize = this.scale;
            particle.setAlpha(1F);
            return particle;
        }
    }

    public static class ElectricSparkFactory extends ColoredAnimatedFactory {
        public ElectricSparkFactory(SpriteSet spriteProvider) {
            super(Color.ELECTRIC, 0.75F, spriteProvider);
            randomColorRange = 0F;
        }
    }

    public static class SmokeFactory implements ParticleProvider<TemplateParticleType> {
        private final SpriteSet spriteProvider;

        public SmokeFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(TemplateParticleType templateParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellFlameParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            // sprite set in constructor

            particle.setColor(1F, 1F, 1F);
            TemplateParticleType.apply(templateParticleType, particle);
            float j = clientWorld.random.nextFloat() * 0.5F + 0.35F;
            particle.setColor(particle.rCol * j, particle.gCol * j, particle.bCol * j);

            particle.spriteProviderForAnimation = this.spriteProvider;
            particle.friction = 0.8F;
            particle.alpha *= 0.8F;
            particle.glow = false;
            particle.gravity = -0.01F;
            return particle;
        }
    }

    public static class WeaknessSmokeFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;
        public Color color = Color.from(0x993333);
        public WeaknessSmokeFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellFlameParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            // sprite set in constructor
            float j = clientWorld.random.nextFloat() * 0.5F + 0.35F;
            particle.setColor(color.red() * j, color.green() * j, color.blue() * j);
            particle.spriteProviderForAnimation = this.spriteProvider;
            particle.friction = 0.8F;
            particle.setAlpha(0.7F);
            particle.glow = false;
            particle.gravity = 0.01F;
            return particle;
        }
    }

    public static class MediumFlameFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public MediumFlameFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellFlameParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            // sprite set in constructor
            particle.spriteProviderForAnimation = this.spriteProvider;
            particle.quadSize = 0.5F;
            particle.lifetime *= 0.5;
            return particle;
        }
    }

    public static class FrostShard implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public FrostShard(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public static Color color = Color.FROST;

        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellFlameParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            // sprite set in constructor
            float j = clientWorld.random.nextFloat() * 0.5F + 0.35F;
            particle.setColor(color.red() * j, color.green() * j, color.blue() * j);
            particle.yd *= clientWorld.random.nextFloat() * 0.2F + 0.9F;
            particle.lifetime = Math.round(clientWorld.random.nextFloat() * 3) + 5;
            return particle;
        }
    }

    public static class ColorableFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;
        public Color color = Color.from(0xffffff);

        public ColorableFactory(SpriteSet spriteProvider, Color color) {
            this.spriteProvider = spriteProvider;
            this.color = color;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellFlameParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            // sprite set in constructor
            float j = clientWorld.random.nextFloat() * 0.5F + 0.35F;
            particle.setColor(color.red() * j, color.green() * j, color.blue() * j);
            return particle;
        }
    }


    public static class HealingFactory extends ColorableFactory {
        public HealingFactory(SpriteSet spriteProvider) {
            super(spriteProvider, Color.from(SpellSchools.HEALING.color));
        }
    }

    public static class HolyFactory extends ColorableFactory {
        public HolyFactory(SpriteSet spriteProvider) {
            super(spriteProvider, Color.HOLY);
        }
    }

    public static class NatureFactory extends ColorableFactory {
        public NatureFactory(SpriteSet spriteProvider) {
            super(spriteProvider, Color.NATURE);
        }
    }

    public static class WhiteFactory extends ColorableFactory {
        public WhiteFactory(SpriteSet spriteProvider) {
            super(spriteProvider, Color.WHITE);
        }
    }

    public static class NatureSlowingFactory extends NatureFactory {
        public NatureSlowingFactory(SpriteSet spriteProvider) {
            super(spriteProvider);
        }
        @Override
        public @Nullable Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = (SpellFlameParticle)super.createParticle(SimpleParticleType, clientWorld, d, e, f, g, h, i, random);
            particle.friction = 0.8F;
            return particle;
        }
    }

    public static class BuffFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;
        public Color color = Color.from(0xffffff);

        public BuffFactory(SpriteSet spriteProvider, Color color) {
            this.spriteProvider = spriteProvider;
            this.color = color;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellFlameParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            // sprite set in constructor
            float j = clientWorld.random.nextFloat() * 0.5F + 0.35F;
            particle.setColor(color.red() * j, color.green() * j, color.blue() * j);
            particle.lifetime = 16;
            particle.translucent = true;
            return particle;
        }
    }

    public static class BuffRageFactory extends BuffFactory {
        public BuffRageFactory(SpriteSet spriteProvider) {
            super(spriteProvider, Color.RAGE);
        }
    }

    public static class SignFactory implements ParticleProvider<TemplateParticleType> {
        private final SpriteSet spriteProvider;
        private final SpellEngineParticles.Texture texture;

        public SignFactory(SpriteSet spriteProvider, SpellEngineParticles.Texture texture) {
            this.spriteProvider = spriteProvider;
            this.texture = texture;
        }

        @Override
        public @Nullable Particle createParticle(TemplateParticleType particleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellFlameParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            // sprite set in constructor
            particle.friction = 0.7F;
            particle.quadSize = 0.4F;
            particle.alpha = 0.9F;
            particle.translucent = true;

            particle.rCol = 1F;
            particle.gCol = 1F;
            particle.bCol = 1F;

            particle.lifetime = texture.frames() > 1 ? texture.frames() : 40;

            TemplateParticleType.apply(particleType, particle);
            var appearance = particleType.getAppearance();
            if (appearance != null) {
                var color = appearance.color;
                if (color != null) {
                    particle.alpha *= appearance.color.alpha();
                }
                particle.quadSize *= appearance.scale;
                particle.followEntity = appearance.entityFollowed;
            }

            return particle;
        }
    }

}