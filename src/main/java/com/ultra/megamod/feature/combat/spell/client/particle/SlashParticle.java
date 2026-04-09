package com.ultra.megamod.feature.combat.spell.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.feature.combat.spell.client.util.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Full 3D rotation slash particle with dual-sided rendering.
 * Ported 1:1 from BetterCombat's SlashParticle.
 *
 * Supports pitch, yaw, roll, and localYaw rotation for positioning
 * weapon trails in 3D space relative to the player's look direction.
 * Renders both front and back faces for visibility from all angles.
 */
public class SlashParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final float pitch;
    private final float yaw;
    private final float localYaw;
    private final float roll;
    private final boolean light;

    public SlashParticle(ClientLevel level, double x, double y, double z,
                         float scale, float pitch, float yaw, float localYaw, float roll,
                         boolean light, long colorRGBA, SpriteSet sprites) {
        super(level, x, y, z, sprites.get(level.random));
        this.sprites = sprites;
        this.light = light;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.localYaw = localYaw;

        // Set color from RGBA
        Color color = Color.fromRGBA(colorRGBA);
        this.setColor(color.red(), color.green(), color.blue());
        this.alpha = color.alpha();

        this.lifetime = 6;
        this.quadSize = scale;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    public int getLightColor(float tint) {
        if (this.light) return 0xF000F0;
        BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
        return this.level.hasChunkAt(pos)
                ? (this.level.getBrightness(LightLayer.BLOCK, pos) << 4) | (this.level.getBrightness(LightLayer.SKY, pos) << 20)
                : 0;
    }

    @Override
    public net.minecraft.client.particle.SingleQuadParticle.Layer getLayer() {
        return net.minecraft.client.particle.SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 camPos = camera.position();
        float fx = (float) (this.xo + (this.x - this.xo) * partialTick - camPos.x);
        float fy = (float) (this.yo + (this.y - this.yo) * partialTick - camPos.y);
        float fz = (float) (this.zo + (this.z - this.zo) * partialTick - camPos.z);

        float size = this.getQuadSize(partialTick);

        // Build rotation quaternion from pitch/yaw/roll/localYaw
        Matrix4f rotMatrix = new Matrix4f();
        rotMatrix.identity();
        rotMatrix.rotateY((float) Math.toRadians(-this.yaw));
        rotMatrix.rotateX((float) Math.toRadians(this.pitch + 90));
        rotMatrix.rotateY((float) Math.toRadians(this.roll));
        rotMatrix.rotateZ((float) Math.toRadians(this.localYaw));

        Quaternionf rotation = new Quaternionf().setFromNormalized(rotMatrix);

        float minU = this.getU0();
        float maxU = this.getU1();
        float minV = this.getV0();
        float maxV = this.getV1();
        int brightness = this.getLightColor(partialTick);

        // Front face
        renderFace(buffer, fx, fy, fz, rotation, size, maxU, minU, minV, maxV, brightness);

        // Back face (rotated 180 degrees around Y)
        Quaternionf backRot = new Quaternionf(rotation);
        backRot.mul(new Quaternionf().rotateY((float) Math.PI));
        renderFace(buffer, fx, fy, fz, backRot, size, minU, maxU, minV, maxV, brightness);
    }

    private void renderFace(VertexConsumer buffer, float x, float y, float z,
                            Quaternionf rotation, float size,
                            float u0, float u1, float v0, float v1, int light) {
        Vector3f[] verts = new Vector3f[]{
                new Vector3f(-1, -1, 0),
                new Vector3f(-1, 1, 0),
                new Vector3f(1, 1, 0),
                new Vector3f(1, -1, 0)
        };

        for (Vector3f v : verts) {
            v.rotate(rotation);
            v.mul(size);
            v.add(x, y, z);
        }

        buffer.addVertex(verts[0].x(), verts[0].y(), verts[0].z())
                .setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
        buffer.addVertex(verts[1].x(), verts[1].y(), verts[1].z())
                .setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
        buffer.addVertex(verts[2].x(), verts[2].y(), verts[2].z())
                .setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
        buffer.addVertex(verts[3].x(), verts[3].y(), verts[3].z())
                .setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
    }

    // ═══════════════════════════════════════════
    // Provider — spawns SlashParticle from SimpleParticleType
    // Uses ThreadLocal context for slash parameters
    // ═══════════════════════════════════════════

    /** ThreadLocal context for passing slash parameters to the factory. */
    private static final ThreadLocal<SlashParams> CURRENT_PARAMS = new ThreadLocal<>();

    public record SlashParams(float scale, float pitch, float yaw, float localYaw,
                              float roll, boolean light, long colorRGBA) {}

    public static void setParams(SlashParams params) { CURRENT_PARAMS.set(params); }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                        double x, double y, double z,
                                        double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            SlashParams params = CURRENT_PARAMS.get();
            CURRENT_PARAMS.remove();
            if (params == null) {
                // Default params if none set
                params = new SlashParams(1.0f, 0, 0, 0, 0, false, Color.WHITE.alpha(0.6f).toRGBA());
            }
            return new SlashParticle(level, x, y, z,
                    params.scale, params.pitch, params.yaw, params.localYaw,
                    params.roll, params.light, params.colorRGBA, this.sprites);
        }
    }
}
