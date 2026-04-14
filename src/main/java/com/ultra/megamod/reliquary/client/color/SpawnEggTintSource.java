package com.ultra.megamod.reliquary.client.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.item.MobCharmFragmentItem;
import com.ultra.megamod.reliquary.item.MobCharmItem;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Tint source that colours a charm/fragment overlay layer from the target
 * mob's spawn-egg palette. The {@code layer} field selects which palette
 * index to sample — 0 for the base (background) colour, 1 for the spots
 * (highlight) colour — matching the pre-1.21.11 {@code SpawnEggItem#getColor}.
 * <p>
 * {@code SpawnEggItem#getColor} was removed in 1.21.11 (vanilla now ships
 * per-mob spawn-egg textures instead of programmatic tinting). We keep the
 * legacy behaviour for Reliquary charms by baking the palette for every mob
 * that can currently hold a charm. Unknown entity types fall back to a
 * deterministic hash so novel mod mobs still show a stable colour instead
 * of white.
 */
public record SpawnEggTintSource(int layer, int fallback) implements ItemTintSource {

    public static final Identifier ID = Identifier.fromNamespaceAndPath("reliquary", "spawn_egg");

    public static final MapCodec<SpawnEggTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    Codec.INT.fieldOf("layer").forGetter(SpawnEggTintSource::layer),
                    ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("default", -1).forGetter(SpawnEggTintSource::fallback)
            ).apply(inst, SpawnEggTintSource::new));

    // (backgroundColor, highlightColor) for each mob charmable via MobCharmDefinition.
    // Values taken from vanilla MC 1.21.1 SpawnEggItem registrations so the port matches
    // what players saw before the charm renderer moved to the data-driven tint source.
    private static final Map<Identifier, int[]> PALETTE = new HashMap<>();
    static {
        put("zombie",            0x00AFAF, 0x799C65);
        put("husk",              0x797061, 0xE0C68A);
        put("drowned",           0x8FA8A1, 0x799C65);
        put("zombie_villager",   0x00AFAF, 0x51A03E);
        put("skeleton",          0xC1C1C1, 0x494949);
        put("stray",             0x6184A1, 0xDDE0E0);
        put("wither_skeleton",   0x141414, 0x4D5850);
        put("creeper",           0x0DA70B, 0x000000);
        put("witch",             0x340000, 0x51A03E);
        put("zombified_piglin",  0xEA9393, 0x4C7129);
        put("cave_spider",       0x0C424F, 0xA80E0E);
        put("spider",            0x342D27, 0xA80E0E);
        put("enderman",          0x161616, 0x000000);
        put("ghast",             0xF9F9F9, 0xBCBCBC);
        put("slime",             0x51A03E, 0x7EBF6E);
        put("magma_cube",        0x340000, 0xFCFC00);
        put("blaze",             0xF6B201, 0xFFD147);
        put("guardian",          0x5A8272, 0xF17D30);
        put("elder_guardian",    0xCECAA3, 0x747693);
        put("phantom",           0x43518A, 0x88FF00);
        put("pillager",          0x532F36, 0x959B9B);
        put("vindicator",        0x959B9B, 0x275E61);
        put("evoker",            0x959B9B, 0x1E1C1A);
        put("piglin",            0xEA9393, 0x4C7129);
        put("piglin_brute",      0x562C16, 0xEA9393);
        put("hoglin",            0xC66E55, 0x5F1F0B);
        put("zoglin",            0xC66E55, 0xE6E6E6);
        put("wither",            0x141414, 0x474D4D);
        put("ender_dragon",      0x161616, 0xE079FB);
        put("warden",            0x0F3A3C, 0x39D6E0);
        put("silverfish",        0x6E6E6E, 0x303030);
        put("endermite",         0x161616, 0x6E6E6E);
        put("shulker",           0x946286, 0x4D3A4D);
        put("vex",               0x7A90A4, 0xE8EDF1);
        put("ravager",           0x757470, 0x514A44);
        put("illusioner",        0x959B9B, 0x334CB2);
        put("breeze",            0xA2A9FF, 0xD8DCFF);
        put("bogged",            0x87897F, 0x324637);
        put("creaking",          0x323447, 0xDF6E29);
    }

    private static void put(String path, int bg, int hi) {
        PALETTE.put(Identifier.fromNamespaceAndPath("minecraft", path), new int[] { bg, hi });
    }

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        Identifier entityName;
        if (stack.getItem() == ModItems.MOB_CHARM.get()) {
            entityName = MobCharmItem.getEntityEggRegistryName(stack);
        } else if (stack.getItem() == ModItems.MOB_CHARM_FRAGMENT.get()) {
            entityName = MobCharmFragmentItem.getEntityRegistryName(stack);
        } else {
            return ARGB.opaque(fallback);
        }
        if (entityName == null) return ARGB.opaque(fallback);

        int[] palette = PALETTE.get(entityName);
        if (palette != null) {
            int idx = Math.max(0, Math.min(1, layer));
            return ARGB.opaque(palette[idx]);
        }
        // Fallback: deterministic colour derived from the registry name hash so the
        // two overlay layers differ and novel mobs still get a stable, distinguishable
        // charm palette even without an entry in the baked table.
        int hash = entityName.toString().hashCode();
        int r = ((hash >> 16) & 0xFF);
        int g = ((hash >> 8) & 0xFF);
        int b = (hash & 0xFF);
        if (layer == 1) {
            r = (r + 0x55) & 0xFF;
            g = (g + 0x55) & 0xFF;
            b = (b + 0x55) & 0xFF;
        }
        return ARGB.opaque((r << 16) | (g << 8) | b);
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
