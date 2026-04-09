package com.ultra.megamod.feature.ambientsounds.environment;

import java.util.Comparator;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import com.ultra.megamod.feature.ambientsounds.condition.AmbientVolume;
import com.ultra.megamod.feature.ambientsounds.condition.BiomeCondition;
import com.ultra.megamod.feature.ambientsounds.engine.AmbientEngine;
import com.ultra.megamod.feature.ambientsounds.environment.BiomeEnvironment.BiomeArea;
import com.ultra.megamod.feature.ambientsounds.util.AmbientDebugRenderer;
import com.ultra.megamod.feature.ambientsounds.util.SimplePair;
import com.ultra.megamod.feature.ambientsounds.util.SimplePairList;

public class BiomeEnvironment implements Iterable<SimplePair<BiomeArea, AmbientVolume>> {

    private final SimplePairList<BiomeArea, AmbientVolume> biomes = new SimplePairList<>();
    private double highestRainVolume;

    public BiomeEnvironment() {}

    public BiomeEnvironment(AmbientEngine engine, Player player, Level level, AmbientVolume volume) {
        highestRainVolume = 0;
        if (volume.volume() > 0.0) {
            BlockPos center = BlockPos.containing(player.getEyePosition(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)));
            MutableBlockPos pos = new MutableBlockPos();
            for (int x = -engine.biomeScanCount; x <= engine.biomeScanCount; x++) {
                for (int z = -engine.biomeScanCount; z <= engine.biomeScanCount; z++) {
                    pos.set(center.getX() + x * engine.biomeScanDistance, center.getY(), center.getZ() + z * engine.biomeScanDistance);
                    Holder<Biome> holder = level.getBiome(pos);

                    float biomeConditionVolume = (float) ((1 - center.distSqr(pos) / engine.squaredBiomeDistance) * volume.conditionVolume());

                    if (level.isRaining() && holder.value().getPrecipitationAt(pos, level.getSeaLevel()) == Precipitation.RAIN)
                        highestRainVolume = Math.max(highestRainVolume, biomeConditionVolume * volume.settingVolume());

                    BiomeArea area = new BiomeArea(holder, pos);
                    SimplePair<BiomeArea, AmbientVolume> before = biomes.getPair(area);
                    if (before == null)
                        biomes.add(area, new AmbientVolume(biomeConditionVolume, volume.settingVolume()));
                    else if (before.value().conditionVolume() < biomeConditionVolume)
                        before.value().setConditionVolumeDirect(biomeConditionVolume);
                }
            }

            biomes.sort(Comparator.comparingDouble((SimplePair<BiomeArea, AmbientVolume> x) -> x.value().volume()).reversed());
        }
    }

    @Override
    public Iterator<SimplePair<BiomeArea, AmbientVolume>> iterator() {
        return biomes.iterator();
    }

    public double rainVolume() {
        return highestRainVolume;
    }

    public void collectDetails(AmbientDebugRenderer text) {
        for (SimplePair<BiomeArea, AmbientVolume> pair : this)
            text.detail(pair.key().location.toString(), pair.value());
    }

    public static class BiomeArea {

        public final Holder<Biome> biome;
        public final Identifier location;
        public final BlockPos pos;

        public BiomeArea(Holder<Biome> biome, BlockPos pos) {
            this.biome = biome;
            this.location = biome.unwrapKey().get().identifier();
            this.pos = pos;
        }

        public boolean checkBiome(BiomeCondition[] conditions) {
            for (BiomeCondition condition : conditions) {
                if (condition.tag()) {
                    if (biome.tags().anyMatch(x -> condition.pattern().matcher(x.location().toString()).matches()))
                        return true;
                } else if (condition.pattern().matcher(location.toString()).matches())
                    return true;
            }
            return false;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof BiomeArea a)
                return a.biome.equals(biome);
            return false;
        }

        @Override
        public int hashCode() {
            return biome.hashCode();
        }

    }

}
