package com.ultra.megamod.lib.spellengine.internals.delivery;

import com.google.common.base.Suppliers;


import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.SpellTriggers;
import com.ultra.megamod.lib.spellengine.internals.arrow.ArrowHelper;
import com.ultra.megamod.lib.spellengine.internals.target.SpellTarget;
import com.ultra.megamod.lib.spellengine.utils.StatusEffectUtil;
import com.ultra.megamod.lib.spellpower.api.SpellPower;

import java.util.*;
import java.util.function.Supplier;

public class SpellStashHelper {
    public static void init() {
        // NeoForge: Server starting hook is called from SpellEngineMod's server lifecycle event
    }

    public static void link(MinecraftServer minecraftServer) {
        var manager = minecraftServer.registryAccess();
        var registry = manager.lookupOrThrow(SpellRegistry.KEY);
        registry.listElements().forEach(entry -> {
            var spell = entry.value();
            var id = entry.unwrapKey().get().identifier();
            if (spell.deliver.type == Spell.Delivery.Type.STASH_EFFECT) {
                if (spell.deliver.stash_effect == null) {
                    System.err.println("Spell Engine: Stash spell linking error! Spell:" + id + " is missing `stash_effect`!");
                    return;
                }
                var stashEffect = spell.deliver.stash_effect;
                if (stashEffect.id == null || stashEffect.id.isEmpty()) {
                    System.err.println("Spell Engine: Stash spell linking error! Spell:" + id + " is missing `stash_effect.id`!");
                    return;
                }
                var trigger = stashEffect.triggers;
                if (trigger == null || trigger.isEmpty()) {
                    System.err.println("Spell Engine: Stash spell linking error! Spell:" + id + " is missing `stash_effect.trigger`!");
                    return;
                }
                var effectId = Identifier.parse(stashEffect.id);
                var statusEffectOpt = BuiltInRegistries.MOB_EFFECT.get(effectId);
                if (statusEffectOpt.isEmpty()) {
                    System.err.println("Spell Engine: Stash spell linking error! Spell:" + id + " found no status effect for `stash_effect.id`: " + stashEffect.id);
                    return;
                }
                var statusEffect = statusEffectOpt.get().value();

                var stashes = SpellStash.getStashedSpells(statusEffect);
                for (var existingStash: stashes) {
                    if (existingStash.spell().equals(entry)) {
                        System.err.println("Spell Engine: Stash spell linking error! Spell:" + id + " already has a stash effect linked to " + stashEffect.id);
                        return;
                    }
                }

                SpellStash.configure(statusEffect, entry, stashEffect.triggers, stashEffect.impact_mode, stashEffect.consume, stashEffect.consumed_next_tick, stashEffect.consume_any_stacks);
            }
        });
    }

    public static void useStashes(SpellTriggers.Event event) {
        var caster = event.player;
        var world = caster.level();
        Map<MobEffectInstance, StatusEffectUtil.Diff> effectChanges = new HashMap<>();
        var activeEffects = List.copyOf(caster.getActiveEffects()); // Create copy to avoid concurrent modification
        for(var stack: activeEffects) {
            var effect = stack.getEffect().value();

            for (var stash: ((SpellStash) effect).getStashedSpells()) {
                var spellEntry = stash.spell();
                for (var trigger: stash.triggers()) {
                    if (spellEntry == null || trigger == null) { continue; }
                    if (!SpellTriggers.evaluateTrigger(spellEntry, trigger, event)) { continue; }

                    var consume = stash.consume();
                    var stacksAvailable = effectChanges.getOrDefault(stack, new StatusEffectUtil.Diff(stack, stack.getAmplifier(), stash.delayConsume() ? 1 : 0)).newAmplifier();
                    if (!stash.consume_any_stacks() && ((stacksAvailable + 1) < consume) ) {
                        continue;
                    }

                    switch (stash.impactMode()) {
                        case PERFORM -> {
                            var target = event.target(trigger);
                            var aoeSource = event.aoeSource(trigger);
                            var spell = stash.spell().value();
                            var power = SpellPower.getSpellPower(spell.school, event.player);
                            var impactContext = new SpellHelper.ImpactContext(1F, 1F, null, power, SpellTarget.FocusMode.DIRECT, 0);
                            if (target != null) {
                                impactContext = impactContext.position(target.position());
                            } else if (aoeSource != null) {
                                impactContext = impactContext.position(aoeSource.position());
                            } else {
                                impactContext = impactContext.position(caster.position());
                            }
                            SpellHelper.performImpacts(world, caster, target, aoeSource, spellEntry, spellEntry.value().impacts, impactContext);
                        }
                        case TRANSFER -> {
                            var arrow = event.arrow;
                            if (arrow != null) {
                                var shooter = event.player;
                                Supplier<Collection<ServerPlayer>> trackers = () -> shooter.level() instanceof ServerLevel sl ? sl.getChunkSource().chunkMap.getPlayers(shooter.chunkPosition(), false) : java.util.List.of();
                                ArrowHelper.onArrowShot(arrow, shooter, spellEntry, trackers);
                            }
                        }
                    }

                    if (consume != 0) {
                        effectChanges.put(stack, new StatusEffectUtil.Diff(stack, stacksAvailable - consume, stash.delayConsume() ? 1 : 0));
                    }
                    break; // Stop processing other triggers for this effect
                }
            }
        }

        var changes = effectChanges.values().stream().toList();
        StatusEffectUtil.applyChanges(caster, changes);
    }
}
