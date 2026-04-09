package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.feature.combat.PlayerClassManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * A consumable item that permanently teaches the player a spell.
 * With the class-based spell system, scrolls bypass class/level requirements
 * for the specific spell they teach. The spell ID is stored in the player's
 * "granted spells" set managed by SpellGrantManager.
 */
public class SpellScrollItem extends Item {

    private final String spellId;

    public SpellScrollItem(Properties props, String spellId) {
        super(props);
        this.spellId = spellId;
    }

    public String getSpellId() {
        return spellId;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;

        SpellDefinition def = SpellRegistry.get(spellId);
        String spellName = def != null ? def.name() : spellId;

        // Check if the player already knows this spell (via class+level or previous scroll)
        if (SpellUnlockManager.canCastSpell(player.getUUID(), spellId, serverLevel)) {
            player.displayClientMessage(
                Component.literal("You already know this spell!").withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.FAIL;
        }

        // Permanently learn the spell — bypasses class/level requirements
        PlayerClassManager pcm = PlayerClassManager.get(serverLevel);
        pcm.learnSpell(player.getUUID(), spellId);
        pcm.saveToDisk(serverLevel);

        // Visual/audio feedback
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.displayClientMessage(
            Component.literal("Learned spell: " + spellName + "!").withStyle(ChatFormatting.GREEN), true);

        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        SpellDefinition def = SpellRegistry.get(spellId);
        if (def != null) {
            tooltip.accept(Component.literal("Teaches: " + def.name()).withStyle(s -> s.withColor(def.school().color)));
            tooltip.accept(Component.literal("School: " + def.school().displayName).withStyle(ChatFormatting.GRAY));
            if (def.classRequirement() != null && !def.classRequirement().isEmpty()) {
                tooltip.accept(Component.literal("Originally: " + def.classRequirement() + " class spell")
                    .withStyle(ChatFormatting.GRAY));
            }
            tooltip.accept(Component.literal("Use to permanently learn this spell").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
