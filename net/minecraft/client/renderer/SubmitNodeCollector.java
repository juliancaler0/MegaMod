package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public interface SubmitNodeCollector extends OrderedSubmitNodeCollector {
    OrderedSubmitNodeCollector order(int p_438904_);

    @OnlyIn(Dist.CLIENT)
    public interface CustomGeometryRenderer {
        void render(PoseStack.Pose p_434723_, VertexConsumer p_434248_);
    }

    @OnlyIn(Dist.CLIENT)
    public interface ParticleGroupRenderer {
        QuadParticleRenderState.@Nullable PreparedBuffers prepare(ParticleFeatureRenderer.ParticleBufferCache p_449331_);

        void render(
            QuadParticleRenderState.PreparedBuffers p_451118_,
            ParticleFeatureRenderer.ParticleBufferCache p_449984_,
            RenderPass p_449361_,
            TextureManager p_449778_,
            boolean p_449072_
        );
    }
}
