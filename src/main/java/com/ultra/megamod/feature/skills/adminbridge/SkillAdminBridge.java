package com.ultra.megamod.feature.skills.adminbridge;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.lib.pufferfish_skills.api.Category;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillAdminBridge {
	private static final String SKILL_TREE_RPGS = "skill_tree_rpgs";
	private static final Identifier CLASS_SKILLS = Identifier.fromNamespaceAndPath(SKILL_TREE_RPGS, "class_skills");
	private static final Identifier WEAPON_SKILLS = Identifier.fromNamespaceAndPath(SKILL_TREE_RPGS, "weapon_skills");
	private static final Identifier ADMIN_SOURCE = Identifier.fromNamespaceAndPath("megamod", "admin");

	// Global XP multiplier applied to everyone (event boost). Persists for server lifetime only.
	private static volatile double globalXpMultiplier = 1.0;
	// Extra multiplier that stacks only for admin accounts (for testing buildouts quickly).
	private static volatile double adminOnlyXpBoost = 1.0;
	// Per-player bypass — admins with this flag skip node prerequisite checks in the skill tree.
	private static final Set<UUID> bypassUuids = ConcurrentHashMap.newKeySet();

	private SkillAdminBridge() { }

	public static Identifier categoryFor(String legacyTree) {
		if (legacyTree == null) return CLASS_SKILLS;
		if (legacyTree.contains(":")) {
			return Identifier.parse(legacyTree);
		}
		return switch (legacyTree.toUpperCase()) {
			case "COMBAT", "WEAPON", "WEAPONS", "WEAPON_SKILLS" -> WEAPON_SKILLS;
			default -> CLASS_SKILLS;
		};
	}

	public static Identifier[] allCategoryIds() {
		return new Identifier[] { CLASS_SKILLS, WEAPON_SKILLS };
	}

	public static void maxAll(ServerPlayer player) {
		maxCategory(player, CLASS_SKILLS);
		maxCategory(player, WEAPON_SKILLS);
	}

	/// Force-unlocks every skill in the category and then normalizes the admin-source point
	/// bucket so the total earned exactly equals the total spent. This keeps the skill menu's
	/// "points left" display coherent with its Earned/Spent tooltip — previously we blindly
	/// granted 500 points which overflowed spent_points_limit (class=10, weapon=6) and made
	/// the header show "10 left" while the tooltip reported "Earned: 500 / Spent: 0".
	///
	/// tryUnlockSkill(force=true) runs updateSkillRewards → applies the attribute modifiers,
	/// spell containers, and other rewards, so unlocking every node actually takes effect on
	/// the player instead of just inflating a counter.
	private static void maxCategory(ServerPlayer player, Identifier categoryId) {
		SkillsAPI.getCategory(categoryId).ifPresent(cat -> {
			Category c = cat;
			c.streamSkills().forEach(skill -> skill.unlock(player));
			int spent = c.getSpentPoints(player);
			int nonAdmin = c.getPointsTotal(player) - c.getPoints(player, ADMIN_SOURCE);
			c.setPoints(player, ADMIN_SOURCE, Math.max(0, spent - nonAdmin));
		});
	}

	// --- XP multipliers --------------------------------------------------

	public static double getGlobalXpMultiplier() { return globalXpMultiplier; }
	public static double getAdminOnlyXpBoost()   { return adminOnlyXpBoost; }

	public static void setGlobalXpMultiplier(double v) {
		globalXpMultiplier = Math.max(0.0, Math.min(100.0, v));
	}

	public static void setAdminOnlyXpBoost(double v) {
		adminOnlyXpBoost = Math.max(0.0, Math.min(100.0, v));
	}

	/// Returns the multiplier to apply to an XP grant for the given player.
	public static double resolveMultiplier(ServerPlayer player) {
		double mult = globalXpMultiplier;
		if (player != null && AdminSystem.isAdmin(player)) {
			mult *= adminOnlyXpBoost;
		}
		return mult;
	}

	// --- Admin bypass ----------------------------------------------------

	public static boolean toggleBypass(UUID uuid) {
		if (bypassUuids.contains(uuid)) {
			bypassUuids.remove(uuid);
			return false;
		}
		bypassUuids.add(uuid);
		return true;
	}

	public static boolean isBypassEnabled(UUID uuid) {
		return bypassUuids.contains(uuid);
	}

	public static Set<UUID> bypassSnapshot() {
		return new HashSet<>(bypassUuids);
	}
}
