package com.ultra.megamod.mixin.bettercombat.player;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ultra.megamod.feature.combat.animation.AttackHand;
import com.ultra.megamod.feature.combat.animation.api.EntityPlayer_BetterCombat;
import com.ultra.megamod.feature.combat.animation.api.PlayerAttackProperties;
import com.ultra.megamod.feature.combat.animation.client.PlayerAttackAnimatable;
import com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig;
import com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Player mixin for BetterCombat combo tracking, dual-wield speed boosts, and sweeping control.
 * Ported 1:1 from BetterCombat (net.bettercombat.mixin.player.PlayerEntityMixin).
 */
@Mixin(value = Player.class, priority = 899)
public abstract class PlayerEntityMixin implements PlayerAttackProperties, EntityPlayer_BetterCombat {

    @Unique
    private int bettercombat$comboCount = 0;

    @Override
    public int getComboCount() {
        return bettercombat$comboCount;
    }

    @Override
    public void setComboCount(int comboCount) {
        this.bettercombat$comboCount = comboCount;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void bettercombat$post_Tick(CallbackInfo ci) {
        var player = ((Player) (Object) this);

        if (player.level().isClientSide()) {
            ((PlayerAttackAnimatable) this).updateAnimationsOnTick();
        }
        bettercombat$updateDualWieldingSpeedBoost();
    }

    // FEATURE: Disable sweeping for attributed weapons

    @ModifyVariable(method = "attack", at = @At("STORE"), ordinal = 3)
    private boolean bettercombat$disableSweeping(boolean value) {
        if (BetterCombatConfig.allow_vanilla_sweeping) {
            return value;
        }

        var player = ((Player) (Object) this);
        var currentHand = PlayerAttackHelper.getCurrentAttack(player, bettercombat$comboCount);
        if (currentHand != null) {
            return false;
        }
        return value;
    }

    // FEATURE: Dual wielding speed boost

    @Unique
    private Multimap<Holder<Attribute>, AttributeModifier> bettercombat$dualWieldingAttributeMap;

    @Unique
    private static final Identifier bettercombat$dualWieldingSpeedModifierId =
            Identifier.fromNamespaceAndPath("megamod", "dual_wield");

    @Unique
    private void bettercombat$updateDualWieldingSpeedBoost() {
        var player = ((Player) (Object) this);
        var newState = PlayerAttackHelper.isDualWielding(player);
        var currentState = bettercombat$dualWieldingAttributeMap != null;
        if (newState != currentState) {
            if (newState) {
                this.bettercombat$dualWieldingAttributeMap = HashMultimap.create();
                double multiplier = com.ultra.megamod.feature.combat.animation.config.ScopedCombatConfig.dualWieldingAttackSpeedMultiplier(player) - 1;
                bettercombat$dualWieldingAttributeMap.put(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                bettercombat$dualWieldingSpeedModifierId,
                                multiplier,
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                player.getAttributes().addTransientAttributeModifiers(bettercombat$dualWieldingAttributeMap);
            } else {
                if (bettercombat$dualWieldingAttributeMap != null) {
                    player.getAttributes().removeAttributeModifiers(bettercombat$dualWieldingAttributeMap);
                    bettercombat$dualWieldingAttributeMap = null;
                }
            }
        }
    }

    // canSweepAttack was removed in MC 1.21.11 — sweep mechanics changed
    // Sweep control is handled differently in the new combat system

    // SECTION: EntityPlayer_BetterCombat

    @Nullable
    @Override
    public AttackHand getCurrentAttack() {
        if (bettercombat$comboCount < 0) {
            return null;
        }
        var player = ((Player) (Object) this);
        return PlayerAttackHelper.getCurrentAttack(player, bettercombat$comboCount);
    }

    @Override
    public String getMainHandIdleAnimation() {
        // Idle animation data is stored via NeoForge data attachments or simple fields
        // For MegaMod we use a simple field approach
        return "";
    }

    @Override
    public String getOffHandIdleAnimation() {
        return "";
    }
}
