package com.ultra.megamod.feature.combat.animation.logic;

import com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entity targeting logic with relation system.
 * Ported 1:1 from BetterCombat (net.bettercombat.logic.TargetHelper).
 */
public class TargetHelper {

    public enum Relation {
        FRIENDLY,
        NEUTRAL,
        HOSTILE;

        public static Relation coalesce(Relation value, Relation fallback) {
            return value != null ? value : fallback;
        }
    }

    public static Relation getRelation(Player attacker, Entity target) {
        if (attacker == target) {
            return Relation.NEUTRAL; // Allow direct hits on self (for pets)
        }
        if (target instanceof TamableAnimal tameable) {
            var owner = tameable.getOwner();
            if (owner != null) {
                return getRelation(attacker, owner);
            }
        }
        if (target instanceof HangingEntity) {
            return Relation.NEUTRAL;
        }

        // Check vanilla team system
        for (var matcher : TEAM_MATCHERS.values()) {
            var relation = matcher.getRelation(attacker, target);
            if (relation != null) {
                return relation.areTeammates()
                        ? (relation.friendlyFireAllowed() ? Relation.NEUTRAL : Relation.FRIENDLY)
                        : Relation.HOSTILE;
            }
        }

        // Check entity type
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (id != null) {
            String idStr = id.toString();
            // Known neutral entities
            if (idStr.equals("minecraft:player") || idStr.equals("minecraft:villager")
                    || idStr.equals("minecraft:iron_golem")) {
                return Relation.NEUTRAL;
            }
        }

        if (target instanceof Animal) {
            return Relation.coalesce(null, Relation.HOSTILE);
        }
        if (target instanceof Monster) {
            return Relation.coalesce(null, Relation.HOSTILE);
        }
        return Relation.HOSTILE;
    }

    public record TeamRelation(boolean areTeammates, boolean friendlyFireAllowed) {}

    public interface TeamMatcher {
        @Nullable
        TeamRelation getRelation(Entity attacker, Entity target);
    }

    private static final Map<String, TeamMatcher> TEAM_MATCHERS = new LinkedHashMap<>();

    public static void registerTeamMatcher(String name, TeamMatcher matcher) {
        TEAM_MATCHERS.put(name, matcher);
    }

    static {
        registerTeamMatcher("vanilla", (entity1, entity2) -> {
            var team1 = entity1.getTeam();
            var team2 = entity2.getTeam();
            if (team1 == null || team2 == null) return null;
            boolean friendlyFire = team1.isAllowFriendlyFire();
            return new TeamRelation(entity1.isAlliedTo(entity2), friendlyFire);
        });
    }

    public static boolean isAttackableMount(Entity entity) {
        if (entity instanceof Monster) return true;
        return BetterCombatConfig.allow_attacking_mount;
    }

    public static boolean isHitAllowed(boolean isDirect, Relation relation) {
        if (isDirect) {
            return relation != Relation.FRIENDLY;
        } else {
            return relation == Relation.HOSTILE;
        }
    }
}
