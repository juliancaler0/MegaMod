package com.ultra.megamod.lib.emf.animation.math.methods.simple;

/**
 * Three-argument function type. Mirrors Apache Commons {@code TriFunction} so we
 * don't pull in the dependency; behaviour is identical.
 */
@FunctionalInterface
public interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
}
