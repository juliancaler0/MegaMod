package com.ultra.megamod.lib.emf.animation;

/**
 * Tiny per-thread runtime state for EMF evaluation.
 * <p>
 * Upstream's {@code EMFManager} is a fat singleton coupled to the render thread. We
 * only need two pieces of global-ish state for Phase D:
 * <ul>
 *     <li>{@link #isValidationPhase()} — consulted by {@code /} and {@code %} operators and
 *     a handful of methods so that division-by-zero during parse-time validation
 *     doesn't invalidate the expression.</li>
 *     <li>The active {@link EmfVariableContext} — so leaf variable/method nodes can ask
 *     for values without every node carrying an explicit context reference.</li>
 * </ul>
 * Phase E may swap this for a fuller runtime; the method surface stays stable.
 */
public final class EmfRuntime {

    private static final ThreadLocal<EmfRuntime> TL = ThreadLocal.withInitial(EmfRuntime::new);

    private EmfVariableContext context = EmfVariableContext.NO_OP;
    private boolean validationPhase = false;

    public static EmfRuntime current() {
        return TL.get();
    }

    public EmfVariableContext context() {
        return context;
    }

    public boolean isValidationPhase() {
        return validationPhase;
    }

    /**
     * Scoped setter: installs {@code ctx} as the current variable lookup, runs the
     * body, and restores the previous context regardless of how the body exits. Usable
     * in a try-with-resources style.
     */
    public AutoCloseable push(EmfVariableContext ctx) {
        EmfVariableContext previous = this.context;
        this.context = ctx == null ? EmfVariableContext.NO_OP : ctx;
        return () -> this.context = previous;
    }

    /** Sets the validation flag for the duration of {@code runnable}. */
    public void runValidating(Runnable runnable) {
        boolean prev = this.validationPhase;
        this.validationPhase = true;
        try {
            runnable.run();
        } finally {
            this.validationPhase = prev;
        }
    }
}
