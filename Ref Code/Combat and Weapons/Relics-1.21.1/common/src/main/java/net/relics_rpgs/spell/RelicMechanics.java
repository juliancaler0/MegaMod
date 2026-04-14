package net.relics_rpgs.spell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.relics_rpgs.RelicsMod;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellPower;
import org.jetbrains.annotations.Nullable;

public class RelicMechanics {
    public static final Identifier SHIELD_RESET = Identifier.of(RelicsMod.NAMESPACE, "shield_reset");

    public static void init() {
        SpellHandlers.registerCustomImpact(SHIELD_RESET, new SpellHandlers.CustomImpact() {
            @Override
            public SpellHandlers.ImpactResult onSpellImpact(RegistryEntry<Spell> registryEntry, SpellPower.Result result, LivingEntity caster, @Nullable Entity entity, SpellHelper.ImpactContext impactContext) {
                if (caster instanceof PlayerEntity player) {
                    var success = tryResetShield(player, player.getMainHandStack()) || tryResetShield(player, player.getOffHandStack());
                    return new SpellHandlers.ImpactResult(success, false);
                }
                return new SpellHandlers.ImpactResult(false, false);
            }

            private boolean tryResetShield(PlayerEntity player, ItemStack itemStack) {
                if (itemStack != null && itemStack.getItem() instanceof ShieldItem shield
                        && player.getItemCooldownManager().isCoolingDown(shield)) {
                    player.getItemCooldownManager().set(shield, 0);
                    return true;
                }
                return false;
            }
        });

    }
}
