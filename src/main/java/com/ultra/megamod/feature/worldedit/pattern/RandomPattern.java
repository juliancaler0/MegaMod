package com.ultra.megamod.feature.worldedit.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Weighted random selection between multiple patterns. */
public class RandomPattern implements Pattern {
    private final List<Entry> entries = new ArrayList<>();
    private double totalWeight = 0;
    private final Random random = new Random();

    public record Entry(Pattern pattern, double weight) {}

    public RandomPattern add(Pattern p, double weight) {
        entries.add(new Entry(p, weight));
        totalWeight += weight;
        return this;
    }

    public boolean isEmpty() { return entries.isEmpty(); }

    public List<Entry> entries() { return entries; }

    @Override
    public BlockState apply(BlockPos pos) {
        if (entries.isEmpty()) return null;
        double r = random.nextDouble() * totalWeight;
        double accum = 0;
        for (Entry e : entries) {
            accum += e.weight;
            if (r <= accum) return e.pattern.apply(pos);
        }
        return entries.get(entries.size() - 1).pattern.apply(pos);
    }
}
