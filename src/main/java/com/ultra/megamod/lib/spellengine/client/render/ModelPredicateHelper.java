package com.ultra.megamod.lib.spellengine.client.render;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;

/**
 * ModelPredicateHelper - adapted for MC 1.21.11.
 *
 * In MC 1.21.4+, the ModelPredicateProvider/ModelPredicateProviderRegistry system
 * was removed. Item models are now handled through the ItemModel system with
 * property-based selection (SelectItemModel, RangeSelectItemModel, etc.).
 *
 * The bow/crossbow animation predicates (pull, pulling, charged) are now handled
 * natively by the item model definition files rather than code-based predicates.
 *
 * These methods are kept as no-ops for API compatibility with callers.
 */
public class ModelPredicateHelper {
    public static void injectBowSkillUsePredicate(Item item) {
        // Model predicates are no longer injectable in MC 1.21.4+
        // Bow animations are now handled through item model definitions
    }

    public static void injectCrossBowSkillUsePredicate(Item item) {
        // Model predicates are no longer injectable in MC 1.21.4+
        // Crossbow animations are now handled through item model definitions
    }

    public static SpellCast.Progress getItemStackRangedSkillProgress(ItemStack itemStack, LivingEntity entity) {
        if (entity instanceof SpellCasterEntity caster && entity.getMainHandItem() == itemStack) {
            var process = caster.getSpellCastProcess();
            if (process != null && process.spell().value().active.cast.animates_ranged_weapon) {
                return process.progress(entity.level().getGameTime());
            }
        }
        return null;
    }

    public static boolean isItemStackUsedForRangedSkill(ItemStack itemStack, LivingEntity entity) {
        if (entity instanceof SpellCasterEntity caster && entity.getMainHandItem() == itemStack) {
            var process = caster.getSpellCastProcess();
            if (process != null && process.spell().value().active.cast.animates_ranged_weapon) {
                return true;
            }
        }
        return false;
    }
}
