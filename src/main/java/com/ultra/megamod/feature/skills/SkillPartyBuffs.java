package com.ultra.megamod.feature.skills;

import com.ultra.megamod.feature.computer.network.handlers.PartyHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = "megamod")
public class SkillPartyBuffs {

    // Track which parties already received their first-application message
    private static final Map<UUID, String> lastComboApplied = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 200 != 0) return;

        Set<UUID> processedPlayers = new HashSet<>();
        ServerLevel overworld = event.getServer().overworld();
        SkillManager manager = SkillManager.get(overworld);

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();
            if (processedPlayers.contains(playerId)) continue;

            Set<UUID> partyMembers = PartyHandler.getPartyMembers(playerId);
            if (partyMembers.isEmpty() || partyMembers.size() < 2) continue;

            processedPlayers.addAll(partyMembers);

            // Gather all party members' specialized branches
            Set<SkillBranch> partySpecs = new HashSet<>();
            for (UUID memberId : partyMembers) {
                Set<String> nodes = manager.getUnlockedNodes(memberId);
                for (SkillBranch branch : SkillBranch.values()) {
                    String t3Id = branch.name().toLowerCase() + "_3";
                    if (nodes.contains(t3Id)) {
                        partySpecs.add(branch);
                    }
                }
            }

            // Check combos in priority order
            ComboMatch combo = findCombo(partySpecs);
            if (combo == null) continue;

            // Apply buffs to all online party members
            for (UUID memberId : partyMembers) {
                ServerPlayer member = event.getServer().getPlayerList().getPlayer(memberId);
                if (member == null) continue;

                for (MobEffectInstance effect : combo.effects) {
                    member.addEffect(new MobEffectInstance(effect.getEffect(), 220, effect.getAmplifier(), true, false, true));
                }

                // Actionbar notification on first application
                String lastCombo = lastComboApplied.get(memberId);
                if (!combo.name.equals(lastCombo)) {
                    lastComboApplied.put(memberId, combo.name);
                    member.sendSystemMessage(Component.literal("\u00a76\u00a7lParty Buff: \u00a7e" + combo.name + "!"), true);
                }
            }
        }
    }

    private static ComboMatch findCombo(Set<SkillBranch> specs) {
        // Priority order — first match wins

        // War Party: Berserker + Shield Wall -> Resistance I + Strength I
        if (specs.contains(SkillBranch.BERSERKER) && specs.contains(SkillBranch.SHIELD_WALL)) {
            return new ComboMatch("War Party", new MobEffectInstance[]{
                new MobEffectInstance(MobEffects.RESISTANCE, 220, 0),
                new MobEffectInstance(MobEffects.STRENGTH, 220, 0)
            });
        }

        // Vanguard: Shield Wall + Blade Mastery -> Resistance I + Strength I
        if (specs.contains(SkillBranch.SHIELD_WALL) && specs.contains(SkillBranch.BLADE_MASTERY)) {
            return new ComboMatch("Vanguard", new MobEffectInstance[]{
                new MobEffectInstance(MobEffects.RESISTANCE, 220, 0),
                new MobEffectInstance(MobEffects.STRENGTH, 220, 0)
            });
        }

        // Healer's Guard: Mana Weaver + Endurance -> Regeneration I
        if (specs.contains(SkillBranch.MANA_WEAVER) && specs.contains(SkillBranch.ENDURANCE)) {
            return new ComboMatch("Healer's Guard", new MobEffectInstance[]{
                new MobEffectInstance(MobEffects.REGENERATION, 220, 0)
            });
        }

        // Sniper Duo: Ranged Precision + Tactician -> Strength I
        if (specs.contains(SkillBranch.RANGED_PRECISION) && specs.contains(SkillBranch.TACTICIAN)) {
            return new ComboMatch("Sniper Duo", new MobEffectInstance[]{
                new MobEffectInstance(MobEffects.STRENGTH, 220, 0)
            });
        }

        // Arcane Artillery: Spell Blade + Ranged Precision -> Strength I
        if (specs.contains(SkillBranch.SPELL_BLADE) && specs.contains(SkillBranch.RANGED_PRECISION)) {
            return new ComboMatch("Arcane Artillery", new MobEffectInstance[]{
                new MobEffectInstance(MobEffects.STRENGTH, 220, 0)
            });
        }

        // Nature's Alliance: Crop Master + Animal Handler -> Saturation I
        if (specs.contains(SkillBranch.CROP_MASTER) && specs.contains(SkillBranch.ANIMAL_HANDLER)) {
            return new ComboMatch("Nature's Alliance", new MobEffectInstance[]{
                new MobEffectInstance(MobEffects.SATURATION, 220, 0)
            });
        }

        // Dungeon Delvers: Dungeoneer + Explorer -> Speed I
        if (specs.contains(SkillBranch.DUNGEONEER) && specs.contains(SkillBranch.EXPLORER)) {
            return new ComboMatch("Dungeon Delvers", new MobEffectInstance[]{
                new MobEffectInstance(MobEffects.SPEED, 220, 0)
            });
        }

        // Mining Expedition: Efficient Mining + Ore Finder -> Haste I
        if (specs.contains(SkillBranch.EFFICIENT_MINING) && specs.contains(SkillBranch.ORE_FINDER)) {
            return new ComboMatch("Mining Expedition", new MobEffectInstance[]{
                new MobEffectInstance(MobEffects.HASTE, 220, 0)
            });
        }

        return null;
    }

    private record ComboMatch(String name, MobEffectInstance[] effects) {}

    public static void onPlayerDisconnect(UUID playerId) {
        lastComboApplied.remove(playerId);
    }
}
