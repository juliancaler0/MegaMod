package com.ultra.megamod.feature.relics;

import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicAttributeBonus;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.data.WeaponRarity;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class RelicItem
extends Item {
    private final String relicName;
    private final AccessorySlotType slotType;
    private final List<RelicAbility> abilities;
    private final float baseDamage;

    public RelicItem(String relicName, AccessorySlotType slotType, List<RelicAbility> abilities, float baseDamage, Item.Properties props) {
        super(props);
        this.relicName = relicName;
        this.slotType = slotType;
        this.abilities = abilities;
        this.baseDamage = baseDamage;
    }

    public RelicItem(String relicName, AccessorySlotType slotType, List<RelicAbility> abilities, Item.Properties props) {
        this(relicName, slotType, abilities, 0.0f, props);
    }

    public RelicItem(String relicName, AccessorySlotType slotType, List<RelicAbility> abilities) {
        this(relicName, slotType, abilities, 0.0f, new Item.Properties().stacksTo(1));
    }

    public String getRelicName() {
        return this.relicName;
    }

    public AccessorySlotType getSlotType() {
        return this.slotType;
    }

    public List<RelicAbility> getAbilities() {
        return this.abilities;
    }

    public float getBaseDamage() {
        return this.baseDamage;
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!RelicData.isInitialized(stack)) {
            RelicData.initialize(stack, this.abilities, level.random);
        }
        if (this.slotType == AccessorySlotType.NONE && this.baseDamage > 0.0f && !WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponStatRoller.rollAndApply(stack, this.baseDamage, level.random);
        }
    }

    /**
     * Right-click no longer casts abilities -- all ability casting goes through the R keybind.
     * This prevents conflicts with the unified ability bar system.
     */
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept((Component)Component.literal((String)("Slot: " + this.slotType.getDisplayName())).withStyle(ChatFormatting.GRAY));
        if (this.slotType == AccessorySlotType.NONE && this.baseDamage > 0.0f) {
            WeaponStatRoller.appendWeaponTooltip(stack, tooltip);
            tooltip.accept((Component)Component.empty());
        }
        if (RelicData.isInitialized(stack)) {
            int level = RelicData.getLevel(stack);
            int quality = RelicData.getQuality(stack);
            int xp = RelicData.getXp(stack);
            int xpNeeded = 100 + level * 50;
            tooltip.accept((Component)Component.literal((String)("Level: " + level + "/10")).withStyle(ChatFormatting.GREEN));
            tooltip.accept((Component)Component.literal((String)("XP: " + xp + "/" + xpNeeded)).withStyle(ChatFormatting.AQUA));
            tooltip.accept((Component)Component.literal((String)("Quality: " + quality + "/10")).withStyle(this.getQualityColor(quality)));
            int unspent = RelicData.getUnspentPoints(stack, this.abilities);
            if (unspent > 0) {
                tooltip.accept((Component)Component.literal((String)(unspent + " unspent points!")).withStyle(ChatFormatting.YELLOW));
            }
            // Attribute bonuses
            List<RelicAttributeBonus> bonuses = RelicData.getAttributeBonuses(stack);
            if (!bonuses.isEmpty()) {
                tooltip.accept((Component)Component.empty());
                for (RelicAttributeBonus bonus : bonuses) {
                    tooltip.accept((Component)Component.literal((String)("  " + bonus.getDisplayText())).withStyle(ChatFormatting.BLUE));
                }
            }
            tooltip.accept((Component)Component.empty());
            for (RelicAbility ability : this.abilities) {
                boolean unlocked = RelicData.isAbilityUnlocked(level, ability, this.abilities);
                ChatFormatting color = unlocked ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY;
                String lockText = unlocked ? "" : " [Lv" + ability.requiredLevel() + "]";
                tooltip.accept((Component)Component.literal((String)("  " + ability.name() + lockText)).withStyle(color));
                if (ability.description() != null && !ability.description().isEmpty()) {
                    tooltip.accept((Component)Component.literal((String)("    " + ability.description())).withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
                }
                if (!unlocked) continue;
                for (RelicStat stat : ability.stats()) {
                    double value = RelicData.getComputedStatValue(stack, ability.name(), stat);
                    tooltip.accept((Component)Component.literal((String)("    " + stat.name() + ": " + String.format("%.1f", value))).withStyle(ChatFormatting.GRAY));
                }
            }
        } else {
            tooltip.accept((Component)Component.literal((String)"Unidentified - use to initialize").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    private ChatFormatting getQualityColor(int quality) {
        if (quality >= 9) {
            return ChatFormatting.GOLD;
        }
        if (quality >= 7) {
            return ChatFormatting.LIGHT_PURPLE;
        }
        if (quality >= 5) {
            return ChatFormatting.AQUA;
        }
        if (quality >= 3) {
            return ChatFormatting.GREEN;
        }
        return ChatFormatting.GRAY;
    }

    public boolean isFoil(ItemStack stack) {
        if (RelicData.isInitialized(stack) && RelicData.getQuality(stack) >= 8) {
            return true;
        }
        if (WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponRarity rarity = WeaponStatRoller.getRarity(stack);
            return rarity == WeaponRarity.MYTHIC || rarity == WeaponRarity.LEGENDARY;
        }
        return false;
    }
}
