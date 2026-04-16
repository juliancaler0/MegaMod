package com.ultra.megamod.lib.skilltree.utils;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.skilltree.skills.NodeTypes;
import net.minecraft.server.level.ServerPlayer;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;

import java.util.ArrayList;
import java.util.List;

public class SkillHelper {
    public static final List<Identifier> RESET_CATEGORIES = new ArrayList<>();
    static {
        RESET_CATEGORIES.add(NodeTypes.CATEGORY_ID);
        RESET_CATEGORIES.add(NodeTypes.WEAPON_CATEGORY_ID);
    }

    public static boolean respec(ServerPlayer player) {
        boolean respeced = false;
        for (var categoryId : RESET_CATEGORIES) {
            respeced |= respecCategory(player, categoryId);
        }
        return respeced;
    }

    public static boolean respecCategory(ServerPlayer player, Identifier categoryId) {
        var category = SkillsAPI.getCategory(categoryId);
        if (category.isEmpty()) {
            return false;
        }
        var skillCategory = category.get();
        if (skillCategory.getSpentPoints(player) == 0) {
            return false;
        }
        skillCategory.resetSkills(player);

        return true;
    }
}
