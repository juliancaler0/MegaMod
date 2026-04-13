package com.ultra.megamod.lib.spellengine.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.core.registries.BuiltInRegistries;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.effect.CustomParticleStatusEffect;
import com.ultra.megamod.lib.spellengine.api.effect.SpellEngineEffects;
import com.ultra.megamod.lib.spellengine.api.item.set.EquipmentSetTooltip;
import com.ultra.megamod.lib.spellengine.api.render.BuffParticleSpawner;
import com.ultra.megamod.lib.spellengine.api.render.StunParticleSpawner;
import com.ultra.megamod.lib.spellengine.api.spell.fx.ParticleBatch;
import com.ultra.megamod.lib.spellengine.client.compatibility.CompatFeatures;
import com.ultra.megamod.lib.spellengine.client.gui.SpellTooltip;
import com.ultra.megamod.lib.spellengine.client.particle.*;
import com.ultra.megamod.lib.spellengine.client.render.*;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellengine.config.ClientConfig;
import com.ultra.megamod.lib.spellengine.config.HudConfig;
import com.ultra.megamod.lib.spellengine.entity.SpellCloud;
import com.ultra.megamod.lib.spellengine.entity.SpellProjectile;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineParticles;
import com.ultra.megamod.lib.spellengine.rpg_series.client.RPGSeriesCoreClient;
import com.ultra.megamod.lib.spellengine.spellbinding.SpellBindingBlockEntity;
import com.ultra.megamod.lib.spellengine.spellbinding.SpellBindingScreen;
import com.ultra.megamod.lib.spellengine.spellbinding.SpellBindingScreenHandler;
import com.ultra.megamod.lib.spellengine.spellbinding.spellchoice.SpellChoiceScreen;
import com.ultra.megamod.lib.spellengine.spellbinding.spellchoice.SpellChoiceScreenHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;


public class SpellEngineClient {
    public static ClientConfig config = new ClientConfig();

    /**
     * Simple config holder with value, save, and refresh.
     */
    public static class ConfigHolder<T> {
        public T value;
        private final String filename;
        private final Class<T> type;
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

        public ConfigHolder(String filename, T defaultValue, Class<T> type) {
            this.filename = filename;
            this.value = defaultValue;
            this.type = type;
        }

        public void refresh() {
            try {
                var configDir = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
                var file = configDir.resolve("megamod_" + filename + ".json");
                if (Files.exists(file)) {
                    var json = Files.readString(file);
                    var loaded = GSON.fromJson(json, type);
                    if (loaded != null) {
                        value = loaded;
                    }
                }
            } catch (Exception e) {
                System.err.println("SpellEngine: Failed to load config " + filename + ": " + e.getMessage());
            }
        }

        public void save() {
            try {
                var configDir = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
                Files.createDirectories(configDir);
                var file = configDir.resolve("megamod_" + filename + ".json");
                var json = GSON.toJson(value);
                Files.writeString(file, json);
            } catch (Exception e) {
                System.err.println("SpellEngine: Failed to save config " + filename + ": " + e.getMessage());
            }
        }
    }

    public static ConfigHolder<HudConfig> hudConfig =
            new ConfigHolder<>("hud_config", HudConfig.createDefault(), HudConfig.class);

    public static void init() {
        hudConfig.refresh();

        ClientNetwork.initializeHandlers();

        BlockEntityRenderers.register(SpellBindingBlockEntity.ENTITY_TYPE, SpellBindingBlockEntityRenderer::new);
        CompatFeatures.initialize();
        BeamRenderer.setup();
        registerEffectParticles();

        RPGSeriesCoreClient.init();
    }

    /**
     * Register menu screens. Call from MegaModClient via modEventBus.addListener.
     */
    public static void onRegisterMenuScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(SpellBindingScreenHandler.HANDLER_TYPE, SpellBindingScreen::new);
        event.register(SpellChoiceScreenHandler.HANDLER_TYPE, SpellChoiceScreen::new);
    }

    /**
     * Register entity renderers. Call from MegaModClient via modEventBus.addListener.
     */
    public static void onRegisterEntityRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SpellProjectile.ENTITY_TYPE, SpellProjectileRenderer::new);
        event.registerEntityRenderer(SpellCloud.ENTITY_TYPE, SpellCloudRenderer::new);
    }

    public static void injectRangedWeaponModelPredicates() {
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            var item = entry.getValue();
            if (item instanceof BowItem) {
                ModelPredicateHelper.injectBowSkillUsePredicate(item);
            } else if (item instanceof CrossbowItem) {
                ModelPredicateHelper.injectCrossBowSkillUsePredicate(item);
            }
        }
    }

    private static void registerEffectParticles() {
        CustomParticleStatusEffect.register(
                SpellEngineEffects.STUN.effect,
                new StunParticleSpawner()
        );
        final var magicSnareParticles = new ParticleBatch(
                SpellEngineParticles.MagicParticles.get(
                        SpellEngineParticles.MagicParticles.Shape.SPARK,
                        SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.FEET,
                2F, 0.15F, 0.15F)
                .preSpawnTravel(5)
                .invert();
        CustomParticleStatusEffect.register(
                SpellEngineEffects.IMMOBILIZE.effect,
                new BuffParticleSpawner(new ParticleBatch[]{ magicSnareParticles
                        .copy().color(Color.PHYSICAL_BLUE.toRGBA()) })
        );
    }

    /**
     * Register particle providers. Call from MegaModClient via modEventBus.addListener.
     */
    public static void registerParticleProviders(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) {
        // Elemental
        event.registerSpriteSet(SpellEngineParticles.flame.particleType(), SpellFlameParticle.FlameFactory::new);
        event.registerSpriteSet(SpellEngineParticles.flame_spark.particleType(), SpellFlameParticle.AnimatedFlameFactory::new);
        event.registerSpriteSet(SpellEngineParticles.flame_ground.particleType(), SpellFlameParticle.AnimatedFlameFactory::new);
        event.registerSpriteSet(SpellEngineParticles.flame_medium_a.particleType(), SpellFlameParticle.MediumFlameFactory::new);
        event.registerSpriteSet(SpellEngineParticles.flame_medium_b.particleType(), SpellFlameParticle.MediumFlameFactory::new);
        event.registerSpriteSet(SpellEngineParticles.snowflake.particleType(), SpellSnowflakeParticle.FrostFactory::new);
        event.registerSpriteSet(SpellEngineParticles.frost_shard.particleType(), SpellFlameParticle.FrostShard::new);

        event.registerSpriteSet(SpellEngineParticles.electric_arc_A.particleType(), SpellFlameParticle.ElectricSparkFactory::new);
        event.registerSpriteSet(SpellEngineParticles.electric_arc_B.particleType(), SpellFlameParticle.ElectricSparkFactory::new);

        // Physical
        event.registerSpriteSet(SpellEngineParticles.smoke_medium.particleType(), SpellFlameParticle.SmokeFactory::new);

        // Misc
        event.registerSpriteSet(SpellEngineParticles.weakness_smoke.particleType(), SpellFlameParticle.WeaknessSmokeFactory::new);

        event.registerSpriteSet(
                SpellEngineParticles.shield_small.particleType(), (provider) -> new UniversalSpellParticle.Opaque(provider, SpellEngineParticles.MagicParticleFamily.Motion.DECELERATE)
        );

        event.registerSpriteSet(SpellEngineParticles.dripping_blood.particleType(), SpellSnowflakeParticle.DrippingBloodFactory::new);
        event.registerSpriteSet(SpellEngineParticles.roots.particleType(), ShiftedParticle.RootsFactory::new);

        // Macro, billboard, whatever
        event.registerSpriteSet(SpellEngineParticles.fire_explosion.particleType(), SpellExplosionParticle.TemplateFactory::new);
        event.registerSpriteSet(SpellEngineParticles.smoke_large.particleType(), SpellSmokeParticle.CosySmokeFactory::new);

        for (var entry: SpellEngineParticles.areaEffects()) {
            event.registerSpriteSet(
                    entry.particleType(), (provider) -> new SpellAreaParticle.Factory(provider, entry.texture(), entry.fading(), entry.orientation())
            );
        }
        for (var entry: SpellEngineParticles.signEffects()) {
            event.registerSpriteSet(
                    entry.particleType(), (provider) -> new SpellFlameParticle.SignFactory(provider, entry.texture())
            );
        }
        for (var variant: SpellEngineParticles.MagicParticles.all) {
            event.registerSpriteSet(
                    variant.entry().particleType(), (provider) -> new SpellUniversalParticle.MagicVariant(provider, variant)
            );
        }
    }
}
