package com.ultra.megamod.feature.combat.relics.spell;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.event.SpellHandlers;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellpower.api.SpellPower;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.jetbrains.annotations.Nullable;

/**
 * Ported 1:1 from Relics-1.21.1's RelicMechanics.
 * Registers the "shield_reset" custom impact handler used by Etienne's Enigma.
 */
public class RelicMechanics {
    public static final Identifier SHIELD_RESET = Identifier.fromNamespaceAndPath(MegaMod.MODID, "shield_reset");

    public static void init() {
        SpellHandlers.registerCustomImpact(SHIELD_RESET, new SpellHandlers.CustomImpact() {
            @Override
            public SpellHandlers.ImpactResult onSpellImpact(Holder<Spell> spellEntry, SpellPower.Result spellPower,
                                                             LivingEntity caster, @Nullable Entity target,
                                                             SpellHelper.ImpactContext context) {
                if (caster instanceof Player player) {
                    var success = tryResetShield(player, player.getMainHandItem()) || tryResetShield(player, player.getOffhandItem());
                    return new SpellHandlers.ImpactResult(success, false);
                }
                return new SpellHandlers.ImpactResult(false, false);
            }

            private boolean tryResetShield(Player player, ItemStack itemStack) {
                if (itemStack != null && itemStack.getItem() instanceof ShieldItem
                        && player.getCooldowns().isOnCooldown(itemStack)) {
                    player.getCooldowns().removeCooldown(BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
                    return true;
                }
                return false;
            }
        });
    }
}
