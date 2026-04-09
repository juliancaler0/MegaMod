package com.ultra.megamod.feature.ambientsounds.engine;

import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.ultra.megamod.feature.ambientsounds.AmbientSoundsFeature;
import com.ultra.megamod.feature.ambientsounds.dimension.AmbientDimension;
import com.ultra.megamod.feature.ambientsounds.environment.AmbientEnvironment;
import com.ultra.megamod.feature.ambientsounds.region.AmbientRegion;
import com.ultra.megamod.feature.ambientsounds.sound.AmbientSound;
import com.ultra.megamod.feature.ambientsounds.sound.AmbientSoundCategory;
import com.ultra.megamod.feature.ambientsounds.sound.AmbientSoundEngine;
import com.ultra.megamod.feature.ambientsounds.util.AmbientDebugRenderer;

public class AmbientTickHandler {

    private static final Minecraft mc = Minecraft.getInstance();

    private static AmbientSoundEngine soundEngine;
    private static AmbientEnvironment environment = null;
    private static AmbientEngine engine;
    private static int timer = 0;

    private static boolean showDebugInfo = false;
    private static boolean shouldReload = false;

    public static void scheduleReload() {
        shouldReload = true;
    }

    public static void toggleDebug() {
        showDebugInfo = !showDebugInfo;
    }

    public static boolean isDebugEnabled() {
        return showDebugInfo;
    }

    public static AmbientEngine getEngine() {
        return engine;
    }

    public static AmbientSoundEngine getSoundEngine() {
        return soundEngine;
    }

    public static AmbientEnvironment getEnvironment() {
        return environment;
    }

    public static void setEngine(AmbientEngine newEngine) {
        engine = newEngine;
    }

    public static void reload() {
        CompletableFuture.runAsync(() -> {
            synchronized (AmbientTickHandler.class) {
                if (engine != null)
                    engine.stopEngine();
                if (environment != null)
                    environment.reload();
                AmbientEngine loaded = AmbientEngine.loadAmbientEngine(soundEngine);
                setEngine(loaded);
            }
        }, Util.backgroundExecutor());
    }

    /** Called from AmbientSoundsFeature on GUI layer registration. Takes (GuiGraphics, DeltaTracker) for 1.21.11. */
    public static void renderOverlay(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (showDebugInfo && engine != null && !mc.isPaused() && environment != null && mc.level != null) {
            AmbientDebugRenderer text = new AmbientDebugRenderer();

            engine.collectDetails(text);

            text.detail("playing", engine.soundEngine.playingCount());
            text.detail("dim-name", mc.level.dimension().identifier());
            text.newLine();

            environment.collectLevelDetails(text);
            text.newLine();

            environment.collectPlayerDetails(text, mc.player);
            text.newLine();

            environment.collectTerrainDetails(text);
            text.newLine();

            environment.collectBiomeDetails(text);
            text.newLine();

            for (AmbientSoundCategory cat : engine.sortedSoundCategories)
                cat.collectDetails(text);
            text.newLine();

            for (AmbientRegion region : engine.activeRegions) {
                text.detail("region", ChatFormatting.DARK_GREEN + region.name + ChatFormatting.RESET);
                text.detail("playing", region.playing.size());
                text.newLine();

                for (AmbientSound sound : region.playing) {
                    if (!sound.isPlaying())
                        continue;

                    if (sound.stream1 != null)
                        sound.stream1.collectDetails(text);
                    if (sound.stream2 != null)
                        sound.stream2.collectDetails(text);
                    text.newLine();
                }
            }

            text.render(mc.font, graphics);
        }
    }

    /** Called every client tick from AmbientSoundsFeature. Drives the main ambient sound loop. */
    public static void onTick() {
        if (soundEngine == null) {
            soundEngine = new AmbientSoundEngine();
            if (engine == null)
                setEngine(AmbientEngine.loadAmbientEngine(soundEngine));
            if (engine != null)
                engine.soundEngine = soundEngine;
        }

        if (shouldReload) {
            reload();
            shouldReload = false;
        }

        if (engine == null)
            return;

        Level level = mc.level;
        Player player = mc.player;

        if (level != null && player != null && mc.options.getSoundSourceVolume(SoundSource.AMBIENT) > 0) {

            if (mc.isPaused())
                return;

            if (environment == null)
                environment = new AmbientEnvironment();

            AmbientDimension newDimension = engine.getDimension(level);
            if (environment.dimension != newDimension) {
                engine.changeDimension(environment, newDimension);
                environment.dimension = newDimension;
            }

            if (timer % engine.environmentTickTime == 0)
                environment.analyzeSlow(newDimension, engine, player, level, timer);

            if (timer % engine.soundTickTime == 0) {
                environment.analyzeFast(newDimension, player, level, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
                environment.dimension.manipulateEnviroment(environment);

                engine.tick(environment);
            }

            engine.fastTick(environment);

            timer++;
        } else if (engine.activeRegions != null && !engine.activeRegions.isEmpty())
            engine.stopEngine();
    }

}
