/*
 * Decompiled with CFR 0.152.
 */
package com.ultra.megamod.feature.skills;

import com.ultra.megamod.feature.skills.SkillBranch;
import java.util.List;
import java.util.Map;

public record SkillNode(String id, SkillBranch branch, int tier, int cost, String displayName, String description, List<String> prerequisites, Map<String, Double> bonuses) {
}

