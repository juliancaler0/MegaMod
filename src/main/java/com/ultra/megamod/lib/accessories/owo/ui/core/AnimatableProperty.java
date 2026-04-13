package com.ultra.megamod.lib.accessories.owo.ui.core;

import java.util.function.BiConsumer;

/**
 * Adapter stub for io.wispforest.owo.ui.core.AnimatableProperty.
 */
public class AnimatableProperty<T> {
    private T value;

    private AnimatableProperty(T value) {
        this.value = value;
    }

    public static <T> AnimatableProperty<T> of(T value) {
        return new AnimatableProperty<>(value);
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public void update(float delta) {
        // No-op in stub - animations are not processed
    }

    public AnimationBuilder<T> animate(int durationMs, Easing easing, T target) {
        this.value = target;
        return new AnimationBuilder<>(this);
    }

    public static class AnimationBuilder<T> {
        private final AnimatableProperty<T> property;

        AnimationBuilder(AnimatableProperty<T> property) {
            this.property = property;
        }

        public AnimationBuilder<T> forwards() { return this; }
        public AnimationBuilder<T> backwards() { return this; }

        public FinishedBuilder<T> finished() { return new FinishedBuilder<>(this); }
    }

    public static class FinishedBuilder<T> {
        private final AnimationBuilder<T> builder;

        FinishedBuilder(AnimationBuilder<T> builder) {
            this.builder = builder;
        }

        public void subscribe(BiConsumer<Object, Boolean> callback) {
            // Immediately invoke as completed in stub
            callback.accept(null, false);
        }
    }
}
