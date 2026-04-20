package com.ultra.megamod.lib.accessories.endec.adapter.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Adapter for io.wispforest.endec.impl.StructEndecBuilder.
 * Builds struct endecs from field definitions, backed by RecordCodecBuilder.
 */
public final class StructEndecBuilder {

    @FunctionalInterface
    public interface Function3<A, B, C, R> { R apply(A a, B b, C c); }

    @FunctionalInterface
    public interface Function4<A, B, C, D, R> { R apply(A a, B b, C c, D d); }

    @FunctionalInterface
    public interface Function5<A, B, C, D, E, R> { R apply(A a, B b, C c, D d, E e); }

    @FunctionalInterface
    public interface Function6<A, B, C, D, E, F, R> { R apply(A a, B b, C c, D d, E e, F f); }

    @FunctionalInterface
    public interface Function7<A, B, C, D, E, F, G, R> { R apply(A a, B b, C c, D d, E e, F f, G g); }

    @FunctionalInterface
    public interface Function8<A, B, C, D, E, F, G, H, R> { R apply(A a, B b, C c, D d, E e, F f, G g, H h); }

    @SuppressWarnings("unchecked")
    private static <S, T> MapCodec<T> fieldCodec(StructField<S, T> field) {
        // Empty name means a flat/inline field (from flatFieldOf): merge the sub-struct's keys
        // into the parent's map instead of nesting under a named field.
        if (field.name().isEmpty()) {
            Codec<T> inner = field.endec().codec();
            MapCodec<T> asMapCodec;
            if (inner instanceof MapCodec.MapCodecCodec<T> wrapper) {
                asMapCodec = wrapper.codec();
            } else {
                // Fallback: assume the codec was built via RecordCodecBuilder.create, which
                // returns a MapCodec.MapCodecCodec. If it is not, we can't inline; fall back
                // to an error MapCodec so the caller sees a clear reason.
                asMapCodec = MapCodec.assumeMapUnsafe(inner);
            }
            return asMapCodec;
        }
        if (field.defaultValue() != null) {
            T def = field.defaultValue().get();
            if (def != null) {
                return field.endec().codec().optionalFieldOf(field.name(), def);
            }
            // Null default: create a MapCodec that never puts null into DataResult.
            // DFU 9.x's DataResult$Success.result() calls Optional.of(value) which NPEs on null.
            // We use a sentinel-based approach where the MapCodec always decodes to a non-null
            // Holder, and the forGetter wraps the nullable value in a Holder.
            return nullSafeOptionalFieldOf(field.endec().codec(), field.name());
        }
        return field.endec().codec().fieldOf(field.name());
    }

    /**
     * Creates a MapCodec for an optional field where absent/failed = null.
     * Uses a custom MapCodec that returns a sentinel empty string marker to avoid
     * DataResult.success(null), then uses the RecordCodecBuilder's apply function
     * to unwrap.
     *
     * The trick: We decode to Optional<T> internally and never let null into a DataResult.
     * The forGetter wraps values in Optional. The constructor receives Optional and unwraps.
     * But since the StructField expects T, not Optional<T>, we need a different approach:
     * We use a custom MapCodec that decodes directly to T, handling null via custom decode.
     */
    /**
     * Optional field where absent = null. DFU 9.x's {@code DataResult.Success.result()} calls
     * {@code Optional.of(value)} which NPEs on null, and {@code RecordCodecBuilder#ap3} calls
     * {@code result()} internally. So we can never let a {@code DataResult.success(null)}
     * reach ap3 — we must route the null through an Optional wrapper whose value is a
     * non-null holder object, and only unwrap to null via a plain xmap lambda that DFU
     * never inspects with {@code .result()}.
     */
    @SuppressWarnings("unchecked")
    private static <T> MapCodec<T> nullSafeOptionalFieldOf(Codec<T> codec, String name) {
        // Use DFU's own optional-field machinery: Optional<T> is always non-null, so the
        // intermediate DataResult<Optional<T>> never holds null. The xmap unwrap to the
        // caller's T (possibly null) happens as a plain function application — DFU's map
        // doesn't invoke result() on the wrapped DataResult.
        return codec.optionalFieldOf(name).xmap(
            (java.util.Optional<T> opt) -> opt.orElse(null),
            (T value) -> java.util.Optional.ofNullable(value)
        );
    }

    public static <S, T> StructEndec<S> of(StructField<S, T> field, Function<T, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(fieldCodec(field).forGetter(s -> field.getter().apply(s)))
                .apply(inst, constructor)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            BiFunction<A, B, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s))
            ).apply(inst, constructor)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B, C> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            StructField<S, C> f3,
            Function3<A, B, C, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s)),
                fieldCodec(f3).forGetter(s -> f3.getter().apply(s))
            ).apply(inst, constructor::apply)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B, C, D> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            StructField<S, C> f3,
            StructField<S, D> f4,
            Function4<A, B, C, D, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s)),
                fieldCodec(f3).forGetter(s -> f3.getter().apply(s)),
                fieldCodec(f4).forGetter(s -> f4.getter().apply(s))
            ).apply(inst, constructor::apply)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B, C, D, E> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            StructField<S, C> f3,
            StructField<S, D> f4,
            StructField<S, E> f5,
            Function5<A, B, C, D, E, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s)),
                fieldCodec(f3).forGetter(s -> f3.getter().apply(s)),
                fieldCodec(f4).forGetter(s -> f4.getter().apply(s)),
                fieldCodec(f5).forGetter(s -> f5.getter().apply(s))
            ).apply(inst, constructor::apply)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B, C, D, E, F> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            StructField<S, C> f3,
            StructField<S, D> f4,
            StructField<S, E> f5,
            StructField<S, F> f6,
            Function6<A, B, C, D, E, F, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s)),
                fieldCodec(f3).forGetter(s -> f3.getter().apply(s)),
                fieldCodec(f4).forGetter(s -> f4.getter().apply(s)),
                fieldCodec(f5).forGetter(s -> f5.getter().apply(s)),
                fieldCodec(f6).forGetter(s -> f6.getter().apply(s))
            ).apply(inst, constructor::apply)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B, C, D, E, F, G> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            StructField<S, C> f3,
            StructField<S, D> f4,
            StructField<S, E> f5,
            StructField<S, F> f6,
            StructField<S, G> f7,
            Function7<A, B, C, D, E, F, G, S> constructor) {
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s)),
                fieldCodec(f3).forGetter(s -> f3.getter().apply(s)),
                fieldCodec(f4).forGetter(s -> f4.getter().apply(s)),
                fieldCodec(f5).forGetter(s -> f5.getter().apply(s)),
                fieldCodec(f6).forGetter(s -> f6.getter().apply(s)),
                fieldCodec(f7).forGetter(s -> f7.getter().apply(s))
            ).apply(inst, constructor::apply)
        );
        return wrapCodec(codec);
    }

    public static <S, A, B, C, D, E, F, G, H> StructEndec<S> of(
            StructField<S, A> f1,
            StructField<S, B> f2,
            StructField<S, C> f3,
            StructField<S, D> f4,
            StructField<S, E> f5,
            StructField<S, F> f6,
            StructField<S, G> f7,
            StructField<S, H> f8,
            Function8<A, B, C, D, E, F, G, H, S> constructor) {
        // RecordCodecBuilder only supports up to 16 fields, but we need manual composition for 8+
        Codec<S> codec = RecordCodecBuilder.create(inst ->
            inst.group(
                fieldCodec(f1).forGetter(s -> f1.getter().apply(s)),
                fieldCodec(f2).forGetter(s -> f2.getter().apply(s)),
                fieldCodec(f3).forGetter(s -> f3.getter().apply(s)),
                fieldCodec(f4).forGetter(s -> f4.getter().apply(s)),
                fieldCodec(f5).forGetter(s -> f5.getter().apply(s)),
                fieldCodec(f6).forGetter(s -> f6.getter().apply(s)),
                fieldCodec(f7).forGetter(s -> f7.getter().apply(s)),
                fieldCodec(f8).forGetter(s -> f8.getter().apply(s))
            ).apply(inst, constructor::apply)
        );
        return wrapCodec(codec);
    }

    private static <S> StructEndec<S> wrapCodec(Codec<S> codec) {
        return new StructEndec<>() {
            @Override
            public Codec<S> codec() {
                return codec;
            }
        };
    }
}
