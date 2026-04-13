package com.ultra.megamod.mixin.rangedweapon.client;

import com.ultra.megamod.lib.rangedweapon.api.AttributeModifierIDs;
import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

/**
 * In 1.21.11, the tooltip system was completely refactored.
 * The original appendAttributeModifierTooltip method no longer exists on ItemStack.
 * Attribute tooltips are now handled through the Display system
 * (ItemAttributeModifiers.Display.apply) and NeoForge's AttributeUtil.addAttributeTooltips.
 *
 * The custom green-text formatting for ranged weapon attributes (damage, pull time)
 * would need to be implemented through the NeoForge attribute tooltip event system
 * or by hooking into the new Display apply mechanism.
 *
 * This mixin is retained as a placeholder for API compatibility.
 * The formatting logic is preserved as static utility methods for future integration.
 */
@Mixin(ItemStack.class)
public class RangedWeaponItemStackMixin {

    // Utility method preserved from original API for formatting ranged weapon attributes
    private static void addGreenText(Consumer<Component> textConsumer, Holder<Attribute> attribute, AttributeModifier modifier, double decimalValue) {
        textConsumer.accept(
                CommonComponents.space()
                        .append(
                                Component.translatable(
                                        "attribute.modifier.equals." + modifier.operation().id(),
                                        ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(decimalValue),
                                        Component.translatable(attribute.value().getDescriptionId())
                                )
                        )
                        .withStyle(ChatFormatting.DARK_GREEN)
        );
    }

    /**
     * Check if a given attribute modifier should use custom green formatting.
     * Can be used by external tooltip hooks to apply the correct style.
     */
    private static boolean shouldFormatAsRangedAttribute(Holder<Attribute> attribute, AttributeModifier modifier) {
        if (attribute == EntityAttributes_RangedWeapon.DAMAGE.entry
                && modifier.is(AttributeModifierIDs.WEAPON_DAMAGE_ID)
                && modifier.operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
            return true;
        }
        if (attribute == EntityAttributes_RangedWeapon.PULL_TIME.entry
                && modifier.is(AttributeModifierIDs.WEAPON_PULL_TIME_ID)
                && modifier.operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
            return true;
        }
        return false;
    }
}
