package com.ultra.megamod.feature.alchemy;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side tooltips for alchemy potions showing effects and duration.
 */
@EventBusSubscriber(modid = "megamod", value = Dist.CLIENT)
public class AlchemyPotionTooltips {

    private record PotionInfo(String name, List<String> effects, String duration, int tier) {}

    private static final Map<String, PotionInfo> POTION_INFO = new LinkedHashMap<>();

    static {
        POTION_INFO.put("megamod:potion_inferno", new PotionInfo("Potion of Inferno",
                List.of("Fire damage boost", "Fire Resistance"), "30s", 2));
        POTION_INFO.put("megamod:potion_glacier", new PotionInfo("Potion of Glacier",
                List.of("Frost aura slows nearby mobs", "Fire Resistance"), "45s", 2));
        POTION_INFO.put("megamod:potion_shadow_step", new PotionInfo("Potion of Shadow Step",
                List.of("Invisibility", "Speed II", "Silent footsteps"), "20s", 3));
        POTION_INFO.put("megamod:potion_vitality", new PotionInfo("Potion of Vitality",
                List.of("Regeneration III", "Absorption II"), "30s", 2));
        POTION_INFO.put("megamod:potion_void_walk", new PotionInfo("Potion of Void Walk",
                List.of("No fall damage", "Slow Falling"), "25s", 4));
        POTION_INFO.put("megamod:potion_tempest", new PotionInfo("Potion of Tempest",
                List.of("Lightning strikes nearby mobs every 3s"), "30s", 3));
        POTION_INFO.put("megamod:potion_berserker", new PotionInfo("Potion of Berserker",
                List.of("Strength III", "-50% defense", "Attacks heal 10%"), "20s", 3));
        POTION_INFO.put("megamod:potion_starlight", new PotionInfo("Potion of Starlight",
                List.of("Night Vision", "Mobs glow within 32 blocks", "Luck"), "60s", 3));
        POTION_INFO.put("megamod:potion_stone_skin", new PotionInfo("Potion of Stone Skin",
                List.of("Resistance III", "Slowness I", "Knockback immunity"), "30s", 1));
        POTION_INFO.put("megamod:potion_arcane_surge", new PotionInfo("Potion of Arcane Surge",
                List.of("+50% ability power", "+30% cooldown reduction", "Mana regeneration"), "45s", 4));
        POTION_INFO.put("megamod:potion_swiftbrew", new PotionInfo("Potion of Swiftbrew",
                List.of("Speed III", "Jump Boost II"), "30s", 1));
        POTION_INFO.put("megamod:potion_iron_gut", new PotionInfo("Potion of Iron Gut",
                List.of("Hunger immunity", "Saturation"), "120s", 1));
        POTION_INFO.put("megamod:potion_midas_touch", new PotionInfo("Potion of Midas Touch",
                List.of("+100% MegaCoin drops from mobs"), "60s", 4));
        POTION_INFO.put("megamod:potion_eagle_eye", new PotionInfo("Potion of Eagle Eye",
                List.of("+50% ranged damage", "No-gravity projectiles"), "30s", 4));
        POTION_INFO.put("megamod:potion_undying", new PotionInfo("Potion of Undying",
                List.of("Totem of Undying effect (one use)"), "120s", 5));
        POTION_INFO.put("megamod:potion_phantom", new PotionInfo("Potion of Phantom",
                List.of("Invisibility", "Speed II", "50% damage reduction"), "25s", 4));
        POTION_INFO.put("megamod:potion_titan", new PotionInfo("Potion of Titan",
                List.of("Strength II", "Resistance I", "+50% melee damage"), "30s", 3));
        POTION_INFO.put("megamod:potion_tidal_wave", new PotionInfo("Potion of Tidal Wave",
                List.of("Water Breathing", "Dolphin's Grace", "Conduit Power"), "90s", 2));
        POTION_INFO.put("megamod:potion_chronos", new PotionInfo("Potion of Chronos",
                List.of("Haste II", "Nearby mobs slowed (12-block radius)"), "30s", 5));
        POTION_INFO.put("megamod:potion_blood_rage", new PotionInfo("Potion of Blood Rage",
                List.of("+5% damage per hit (stacks to 50%)", "Slowly drains HP"), "20s", 4));
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        PotionInfo info = POTION_INFO.get(itemId);
        if (info == null) return;

        List<Component> tooltip = event.getToolTip();
        int insertIdx = Math.min(1, tooltip.size());

        // Effects
        for (String effect : info.effects) {
            MutableComponent line = Component.literal("  " + effect).withStyle(ChatFormatting.LIGHT_PURPLE);
            tooltip.add(insertIdx++, line);
        }

        // Duration
        MutableComponent durationLine = Component.literal("  Duration: " + info.duration).withStyle(ChatFormatting.GRAY);
        tooltip.add(insertIdx++, durationLine);

        // Tier requirement
        String req = AlchemyRecipeRegistry.getTierRequirement(info.tier);
        MutableComponent tierLine = Component.literal("  Requires: " + req).withStyle(ChatFormatting.DARK_GRAY);
        tooltip.add(insertIdx, tierLine);
    }
}
