package com.ultra.megamod.lib.combatroll.internals;

import com.ultra.megamod.lib.combatroll.CombatRollMod;
import com.ultra.megamod.lib.combatroll.api.CombatRoll;
import com.ultra.megamod.lib.combatroll.client.CombatRollClient;
import com.ultra.megamod.mixin.combatroll.CombatRollPlayerAccessor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class RollManager {
    public boolean isEnabled = true;
    public static int rollDuration() {
        return CombatRollMod.config.roll_duration;
    }
    private int timeSinceLastRoll = 10;
    private float currentCooldownProgress = 0;
    private int currentCooldownLength = 0;
    private int maxRolls = 1;
    private int availableRolls = 0;

    public RollManager() { }

    public record CooldownInfo(int elapsed, int total, int availableRolls, int maxRolls) { }

    public CooldownInfo getCooldown() {
        return new CooldownInfo((int)currentCooldownProgress, currentCooldownLength, availableRolls, maxRolls);
    }

    public boolean isRollAvailable(Player player) {
        return isEnabled
                && !isRolling()
                && availableRolls > 0
                && !((CombatRollPlayerAccessor)player).combatroll$invokeIsImmobile()
                && (!player.isPassenger() && player.isAlive())
                && player.getAttributeValue(Attributes.MOVEMENT_SPEED) > 0;
    }

    public boolean isRolling() {
        return timeSinceLastRoll <= rollDuration();
    }

    public void onRoll(LocalPlayer player) {
        availableRolls -= 1;
        timeSinceLastRoll = 0;
        updateCooldownLength(player);
    }

    public void tick(LocalPlayer player) {
        maxRolls = (int) player.getAttributeValue(CombatRoll.Attributes.COUNT.entry);
        timeSinceLastRoll += 1;
        if (availableRolls < maxRolls) {
            currentCooldownProgress += increment(player);
            if (currentCooldownProgress >= currentCooldownLength) {
                rechargeRoll(player);
            }
        }
        if (availableRolls == maxRolls) {
            currentCooldownProgress = 0;
        }
        if (availableRolls > maxRolls) {
            availableRolls = maxRolls;
        }
    }

    private float increment(Player player) {
        return (float) (player.getAttributeValue(CombatRoll.Attributes.RECHARGE.entry) / 20F);
    }

    private void rechargeRoll(LocalPlayer player) {
        availableRolls += 1;
        currentCooldownProgress = Math.max(currentCooldownProgress - currentCooldownLength, 0);
        updateCooldownLength(player);
        if (CombatRollClient.config.playCooldownSound) {
            var cooldownReady = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.fromNamespaceAndPath("megamod", "roll_cooldown_ready"));
            if (cooldownReady != null) {
                player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), cooldownReady, SoundSource.PLAYERS, 1, 1, false);
            }
        }
    }

    private void updateCooldownLength(LocalPlayer player) {
        var duration = CombatRollMod.config.roll_cooldown;
        currentCooldownLength = Math.round(duration * 20F);
    }
}
