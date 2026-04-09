package com.ultra.megamod.feature.skills.locks;

import com.ultra.megamod.feature.skills.SkillBranch;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillNode;
import com.ultra.megamod.feature.skills.SkillTreeDefinitions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Client-side tooltip additions for skill-locked items.
 * Adds a red "LOCKED" line with required branches when hovering locked items.
 */
@EventBusSubscriber(modid = "megamod", value = Dist.CLIENT)
public class SkillLockTooltips {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        // Only process if we have client player data available
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Admin with lock bypass active — never show LOCKED tooltips
        if (com.ultra.megamod.feature.skills.network.SkillSyncPayload.clientAdminLockBypass) return;

        SkillLockDefinitions.UseLock lock = SkillLockManager.getUseLock(stack);
        if (lock == null) return;

        // Check if the local player has the required branch unlocked
        // Use client-side synced skill data if available, otherwise show lock for all
        boolean isLocked = !hasClientBranch(lock.branchA()) && !hasClientBranch(lock.branchB());

        if (isLocked) {
            String branchA = lock.branchA().getDisplayName();
            String branchB = lock.branchB() != null ? lock.branchB().getDisplayName() : null;

            // Insert lock info after the item name (index 1)
            int insertIdx = Math.min(1, event.getToolTip().size());

            MutableComponent lockLine = Component.literal("\u2716 LOCKED").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
            event.getToolTip().add(insertIdx, lockLine);

            MutableComponent reqLine;
            if (branchB != null) {
                reqLine = Component.literal("  Requires: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(branchA).withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" or ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(branchB).withStyle(ChatFormatting.YELLOW));
            } else {
                reqLine = Component.literal("  Requires: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(branchA).withStyle(ChatFormatting.YELLOW));
            }
            event.getToolTip().add(insertIdx + 1, reqLine);

            MutableComponent tierLine = Component.literal("  (Tier 3+ specialization)").withStyle(ChatFormatting.DARK_GRAY);
            event.getToolTip().add(insertIdx + 2, tierLine);
        }
    }

    /**
     * Check if the local client player has a branch specialized (tier 3+ node).
     * Uses the client-synced skill data from SkillSyncPayload.
     */
    private static boolean hasClientBranch(SkillBranch branch) {
        if (branch == null) return false;
        // Access client-side synced data
        var clientNodes = com.ultra.megamod.feature.skills.network.SkillSyncPayload.clientUnlockedNodes;
        if (clientNodes == null || clientNodes.isEmpty()) return false;

        for (String nodeId : clientNodes) {
            SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
            if (node != null && node.branch() == branch && node.tier() >= 3) {
                return true;
            }
        }
        return false;
    }
}
