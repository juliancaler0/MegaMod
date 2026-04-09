package com.ultra.megamod.feature.ambientsounds.environment;

import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.ultra.megamod.feature.ambientsounds.condition.AmbientTime;
import com.ultra.megamod.feature.ambientsounds.condition.AmbientVolume;
import com.ultra.megamod.feature.ambientsounds.dimension.AmbientDimension;
import com.ultra.megamod.feature.ambientsounds.engine.AmbientEngine;
import com.ultra.megamod.feature.ambientsounds.util.AmbientDebugRenderer;

public class AmbientEnvironment {

    public AmbientDimension dimension;

    public boolean muted = false;

    public boolean night;
    public double sunAngle;
    public double dayTimeHour;

    public double rainSurfaceVolume;
    public boolean raining;
    public boolean snowing;
    public boolean thundering;

    public BiomeEnvironment biome = new BiomeEnvironment();
    public TerrainEnvironment terrain = new TerrainEnvironment();
    public EntityEnvironment entity = new EntityEnvironment();

    public AmbientVolume biomeVolume = AmbientVolume.SILENT;

    public HashMap<String, AmbientVolume> biomeTypeVolumes = new HashMap<>();

    public double absoluteHeight;
    public double relativeHeight;
    public double relativeMinHeight;
    public double relativeMaxHeight;
    public double underwater;

    public double temperature;

    public AmbientEnvironment() {}

    public boolean isRainAudibleAtSurface() {
        return rainSurfaceVolume > 0;
    }

    public void analyzeFast(AmbientDimension dimension, Player player, Level level, float deltaTime) {
        this.dimension = dimension;
        this.raining = level.isRainingAt(player.blockPosition().above());
        this.snowing = level.getBiome(player.blockPosition()).value().coldEnoughToSnow(player.blockPosition(), level.getSeaLevel()) && level.isRaining();
        this.thundering = level.isThundering() && !snowing;

        this.absoluteHeight = player.getEyeY();
        this.relativeHeight = absoluteHeight - terrain.averageHeight;
        this.relativeMinHeight = absoluteHeight - terrain.maxHeight;
        this.relativeMaxHeight = absoluteHeight - terrain.minHeight;

        this.temperature = level.getBiome(player.blockPosition()).value().getBaseTemperature();

        analyzeUnderwater(player, level);
        analyzeTime(level, deltaTime);
        entity.analyzeFast(dimension, player, level, deltaTime);
    }

    public void analyzeTime(Level level, float deltaTime) {
        // In 1.21.11 Level.getSunAngle(float) was removed. Compute from getDayTime() directly.
        // Old formula: timeOfDay = (dayTime % 24000) / 24000.0f, sunAngleRad = timeOfDay * 2*PI
        float timeOfDay = (level.getDayTime() % 24000L) / 24000.0f;
        float sunAngleRad = timeOfDay * (float) (Math.PI * 2);
        this.sunAngle = (Math.toDegrees(sunAngleRad) - 180) % 360;
        if (this.sunAngle < 0)
            this.sunAngle += 360;
        this.night = sunAngle < 90 || sunAngle > 270;
        this.dayTimeHour = sunAngle * AmbientTime.ANGLE_TO_TIME;
    }

    public void analyzeUnderwater(Player player, Level level) {
        int depth = 0;
        if (player.isEyeInFluid(FluidTags.WATER)) {
            BlockPos blockpos = BlockPos.containing(player.getEyePosition());
            while (level.getFluidState(blockpos).is(FluidTags.WATER)) {
                depth++;
                blockpos = blockpos.above();
            }
        }
        this.underwater = depth;
    }

    public void analyzeSlow(AmbientDimension dimension, AmbientEngine engine, Player player, Level level, float deltaTime) {
        terrain.analyze(engine, dimension, player, level);
        biome = new BiomeEnvironment(engine, player, level, biomeVolume);
        rainSurfaceVolume = biome.rainVolume();
    }

    public void collectLevelDetails(AmbientDebugRenderer text) {
        text.detail("dimension", dimension);
        text.detail("night", night);
        text.detail("rain", raining);
        text.detail("rainSurfaceVolume", rainSurfaceVolume);
        text.detail("snow", snowing);
        text.detail("storm", thundering);
        text.detail("sun", sunAngle);
        text.detail("time", dayTimeHour);

    }

    public void collectPlayerDetails(AmbientDebugRenderer text, Player player) {
        text.detail("underwater", underwater);
        text.detail("temp", temperature);
        text.detail("height", "r:" + AmbientDebugRenderer.DECIMAL_FORMAT.format(relativeHeight) + ",a:" + AmbientDebugRenderer.DECIMAL_FORMAT.format(
            terrain.averageHeight) + " (" + AmbientDebugRenderer.DECIMAL_FORMAT.format(relativeMinHeight) + "," + AmbientDebugRenderer.DECIMAL_FORMAT.format(relativeMaxHeight) + ")");

    }

    public void collectTerrainDetails(AmbientDebugRenderer text) {
        terrain.collectDetails(text);
    }

    public void collectBiomeDetails(AmbientDebugRenderer text) {
        text.detail("b-volume", biomeVolume);
        biome.collectDetails(text);
    }

    public void reload() {
        terrain.scanner = null;
    }

}
