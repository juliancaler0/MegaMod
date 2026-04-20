package com.ultra.megamod.feature.combat.animation.particle;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.MegaMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Registry of all BetterCombat slash/stab particle types.
 * 1:1 port of {@code net.bettercombat.particle.BetterCombatParticles}.
 *
 * <p>Registers 12 particle types via NeoForge {@link DeferredRegister} —
 * {@code botslash45/90/180/270/360/stab} and {@code topslash45/90/180/270/360/stab}.
 * Each spawns a {@link SlashParticleEffect} instance with its own rotation params.</p>
 */
public class BetterCombatParticles {
    public record StaticParams(boolean animated, int lifetime, float scale) {}

    private static final DeferredRegister<ParticleType<?>> REGISTER =
            DeferredRegister.create(Registries.PARTICLE_TYPE, MegaMod.MODID);

    public static final class Entry {
        public final Identifier id;
        public final int textureFrames;
        public final StaticParams params;
        private final DeferredHolder<ParticleType<?>, ParticleType<SlashParticleEffect>> holder;

        Entry(String name, int textureFrames, StaticParams params) {
            this.id = Identifier.fromNamespaceAndPath(MegaMod.MODID, name);
            this.textureFrames = textureFrames;
            this.params = params;
            @SuppressWarnings({"unchecked", "rawtypes"})
            DeferredHolder<ParticleType<?>, ParticleType<SlashParticleEffect>> h =
                    (DeferredHolder) REGISTER.register(name, BetterCombatParticles::createParticle);
            this.holder = h;
        }

        public ParticleType<SlashParticleEffect> particleType() {
            return holder.get();
        }
    }

    private static ParticleType<SlashParticleEffect> createParticle() {
        return new ParticleType<SlashParticleEffect>(false) {
            @Override
            public MapCodec<SlashParticleEffect> codec() {
                return SlashParticleEffect.createCodec(this);
            }
            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, SlashParticleEffect> streamCodec() {
                return SlashParticleEffect.createStreamCodec(this);
            }
        };
    }

    public static final List<Entry> ENTRIES = new ArrayList<>();

    private static Entry add(Entry entry) {
        ENTRIES.add(entry);
        return entry;
    }

    private static final int default_age = 20;
    private static final int default_frames = 8;
    private static final StaticParams default_params = new StaticParams(true, default_age, 1F);

    public static final Entry botslash45  = add(new Entry("botslash45",  default_frames, default_params));
    public static final Entry botslash90  = add(new Entry("botslash90",  default_frames, default_params));
    public static final Entry botslash180 = add(new Entry("botslash180", default_frames, default_params));
    public static final Entry botslash270 = add(new Entry("botslash270", default_frames, default_params));
    public static final Entry botslash360 = add(new Entry("botslash360", default_frames, default_params));
    public static final Entry botstab     = add(new Entry("botstab",     default_frames, default_params));
    public static final Entry topslash45  = add(new Entry("topslash45",  default_frames, default_params));
    public static final Entry topslash90  = add(new Entry("topslash90",  default_frames, default_params));
    public static final Entry topslash180 = add(new Entry("topslash180", default_frames, default_params));
    public static final Entry topslash270 = add(new Entry("topslash270", default_frames, default_params));
    public static final Entry topslash360 = add(new Entry("topslash360", default_frames, default_params));
    public static final Entry topstab     = add(new Entry("topstab",     default_frames, default_params));

    /** Call from MegaMod constructor with the mod event bus. */
    public static void init(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
