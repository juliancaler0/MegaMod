package com.ultra.megamod.feature.relics.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelicEffectRegistry
{
	public static final DeferredRegister<MobEffect> MOB_EFFECTS =
		DeferredRegister.create(Registries.MOB_EFFECT, "megamod");

	public static final DeferredHolder<MobEffect, AntiHealEffect> ANTI_HEAL =
		MOB_EFFECTS.register("anti_heal", () -> new AntiHealEffect(MobEffectCategory.HARMFUL, 0x6836AA));

	public static final DeferredHolder<MobEffect, BleedingEffect> BLEEDING =
		MOB_EFFECTS.register("bleeding", () -> new BleedingEffect(MobEffectCategory.HARMFUL, 0xB21F1F));

	public static final DeferredHolder<MobEffect, ConfusionEffect> CONFUSION =
		MOB_EFFECTS.register("confusion", () -> new ConfusionEffect(MobEffectCategory.HARMFUL, 0x9B30FF));

	public static final DeferredHolder<MobEffect, ParalysisEffect> PARALYSIS =
		MOB_EFFECTS.register("paralysis", () -> new ParalysisEffect(MobEffectCategory.HARMFUL, 0xFFD700));

	public static final DeferredHolder<MobEffect, StunEffect> STUN =
		MOB_EFFECTS.register("stun", () -> new StunEffect(MobEffectCategory.HARMFUL, 0xFFFF55));

	public static final DeferredHolder<MobEffect, VanishingEffect> VANISHING =
		MOB_EFFECTS.register("vanishing", () -> new VanishingEffect(MobEffectCategory.BENEFICIAL, 0x333333));

	public static void init(IEventBus modEventBus)
	{
		MOB_EFFECTS.register(modEventBus);
	}
}
