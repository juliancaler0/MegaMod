package com.ultra.megamod.feature.relics;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.lib.spellengine.api.spell.event.SpellEvents;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Phase G.1 level-gate for relic spells. Hooked into SpellEngine's
 * {@link SpellEvents#CASTING_ATTEMPT} PRE stage: before a cast starts, inspect
 * the held item stack. If it's a {@link RelicItem} and the spell ID is one of
 * its relic abilities, compare {@link RelicData#getLevel(net.minecraft.world.item.ItemStack)}
 * with the ability's required level (from {@link RelicSpellAssignments}).
 * When the relic is under-leveled the attempt is blocked with a chat message.
 */
public final class RelicLevelGate {
    private RelicLevelGate() {}

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        SpellEvents.CASTING_ATTEMPT.PRE.register(args -> {
            try {
                var spellKey = args.spell().unwrapKey().orElse(null);
                if (spellKey == null) return null;
                String spellId = spellKey.identifier().toString();
                RelicSpellAssignments.SpellMeta meta = RelicSpellAssignments.metaFor(spellId);
                if (meta == null) return null; // not a relic spell
                var stack = args.itemStack();
                if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof RelicItem)) {
                    // Host item may be an accessory — no level info reachable cheaply.
                    // For accessory-equipped relics the player container merge still resolves
                    // the spell even though itemStack here is the mainhand; gate only when
                    // we can actually inspect the relic stack.
                    return null;
                }
                int relicLevel = RelicData.isInitialized(stack) ? RelicData.getLevel(stack) : 1;
                if (relicLevel < meta.requiredLevel()) {
                    if (args.caster() instanceof ServerPlayer sp) {
                        sp.displayClientMessage(
                                Component.literal("Need relic level " + meta.requiredLevel()
                                        + " to use " + meta.abilityName() + ".")
                                        .withStyle(ChatFormatting.RED),
                                true);
                    }
                    return SpellCast.Attempt.none();
                }
            } catch (Throwable t) {
                MegaMod.LOGGER.debug("RelicLevelGate check failed", t);
            }
            return null;
        });
        MegaMod.LOGGER.info("[RelicLevelGate] registered CASTING_ATTEMPT predicate");
    }
}
