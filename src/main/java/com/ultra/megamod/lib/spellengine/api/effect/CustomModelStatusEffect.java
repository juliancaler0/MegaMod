package com.ultra.megamod.lib.spellengine.api.effect;

import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;

import java.util.HashMap;
import java.util.Map;

public final class CustomModelStatusEffect {
    public interface Renderer {
        void renderEffect(int amplifier, LivingEntity livingEntity, float delta, PoseStack matrixStack,
                          MultiBufferSource vertexConsumers, int light);
    }
    public record Args(boolean scaleWithEntity) {
        public static final Args DEFAULT = new Args(true);
    }
    public record Entry(Renderer renderer, Args args) { }

    private static final Map<MobEffect, Entry> renderers = new HashMap<>();

    public static void register(MobEffect statusEffect, Renderer renderer) {
        register(statusEffect, renderer, Args.DEFAULT);
    }

    public static void register(MobEffect statusEffect, Renderer renderer, Args args) {
        renderers.put(statusEffect, new Entry(renderer, args));
    }

    public static Entry entryOf(MobEffect statusEffect) {
        return renderers.get(statusEffect);
    }

    public static Renderer rendererOf(MobEffect statusEffect) {
        return renderers.get(statusEffect).renderer();
    }
}
