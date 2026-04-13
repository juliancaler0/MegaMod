package com.ultra.megamod.mixin.combatroll;

import com.ultra.megamod.lib.combatroll.CombatRollMod;
import com.ultra.megamod.lib.combatroll.Platform;
import com.ultra.megamod.lib.combatroll.api.CombatRoll;
import com.ultra.megamod.lib.combatroll.client.Keybindings;
import com.ultra.megamod.lib.combatroll.client.RollEffect;
import com.ultra.megamod.lib.combatroll.compatibility.BetterCombatHelper;
import com.ultra.megamod.lib.combatroll.internals.RollingEntity;
import com.ultra.megamod.lib.combatroll.network.Packets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.ultra.megamod.lib.combatroll.client.RollEffect.Particles.PUFF;

@Mixin(value = Minecraft.class, priority = 449)
public abstract class CombatRollMinecraftMixin {
    @Shadow private int missTime;
    @Shadow @Nullable public LocalPlayer player;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void combatroll$startAttack_HEAD(CallbackInfoReturnable<Boolean> info) {
        var rollingPlayer = ((RollingEntity)player);
        if (rollingPlayer != null && rollingPlayer.getRollManager().isRolling()) {
            info.setReturnValue(false);
            info.cancel();
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void combatroll$continueAttack_HEAD(boolean bl, CallbackInfo ci) {
        var rollingPlayer = ((RollingEntity)player);
        if (rollingPlayer != null && rollingPlayer.getRollManager().isRolling()) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void combatroll$startUseItem_HEAD(CallbackInfo ci) {
        var rollingPlayer = ((RollingEntity)player);
        if (rollingPlayer != null && rollingPlayer.getRollManager().isRolling()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void combatroll$handleKeybinds_TAIL(CallbackInfo ci) {
        combatroll$tryRolling();
    }

    @Unique
    private void combatroll$tryRolling() {
        var client = (Minecraft) ((Object)this);
        if (player == null || client.isPaused() || client.screen != null) {
            return;
        }
        var rollingPlayer = ((RollingEntity)player);
        var rollManager = rollingPlayer.getRollManager();
        if (Keybindings.roll.isDown()) {
            com.ultra.megamod.MegaMod.LOGGER.info("[CombatRoll] Roll key pressed. available={} airborn={} food={} swimming={} vehicle={} using={} cooldown={}",
                    rollManager.isRollAvailable(player),
                    !player.onGround(),
                    player.getFoodData().getFoodLevel(),
                    player.isSwimming(),
                    player.getVehicle() != null,
                    player.isUsingItem(),
                    player.getAttackStrengthScale(0));
            if(!rollManager.isRollAvailable(player)) {
                com.ultra.megamod.MegaMod.LOGGER.info("[CombatRoll] BLOCK: roll not available");
                return;
            }
            if(!CombatRollMod.config.allow_rolling_while_airborn && !player.onGround()) {
                com.ultra.megamod.MegaMod.LOGGER.info("[CombatRoll] BLOCK: airborne");
                return;
            }
            if(player.getFoodData().getFoodLevel() <= CombatRollMod.config.food_level_required) {
                com.ultra.megamod.MegaMod.LOGGER.info("[CombatRoll] BLOCK: low food");
                return;
            }
            if(player.isSwimming() || player.isVisuallyCrawling()) {
                com.ultra.megamod.MegaMod.LOGGER.info("[CombatRoll] BLOCK: swimming/crawling");
                return;
            }
            if(player.getVehicle() != null) {
                com.ultra.megamod.MegaMod.LOGGER.info("[CombatRoll] BLOCK: in vehicle");
                return;
            }
            if(player.isUsingItem() || player.isBlocking()) {
                com.ultra.megamod.MegaMod.LOGGER.info("[CombatRoll] BLOCK: using item");
                return;
            }
            if (!CombatRollMod.config.allow_rolling_while_weapon_cooldown && player.getAttackStrengthScale(0) < 0.95) {
                com.ultra.megamod.MegaMod.LOGGER.info("[CombatRoll] BLOCK: weapon cooldown");
                return;
            }
            com.ultra.megamod.MegaMod.LOGGER.info("[CombatRoll] ROLLING!");
            if (BetterCombatHelper.isDoingUpswing()) {
                BetterCombatHelper.cancelUpswing();
            } else {
                if (client.options.keyAttack.isDown()) {
                    return;
                }
            }
            if (missTime > 0) {
                return;
            }

            var forward = player.zza;
            var sideways = player.xxa;
            Vec3 direction;
            if (forward == 0 && sideways == 0) {
                direction = new Vec3(0, 0, 1);
            } else  {
                direction = new Vec3(sideways, 0, forward).normalize();
            }
            direction = direction.yRot((float) Math.toRadians((-1.0) * player.getYRot()));
            var distance = 0.475 *
                    (player.getAttributeValue(CombatRoll.Attributes.DISTANCE.entry)
                    + CombatRollMod.config.additional_roll_distance);
            direction = direction.multiply(distance, distance, distance);

            if (player.isInWater()) {
                var liquidHeight = player.getFluidHeight(FluidTags.WATER);
                liquidHeight = Math.min(liquidHeight, 1F);
                var multiplier = Math.max(1 - (liquidHeight*3), 0.3);
                direction = direction.multiply(multiplier, multiplier, multiplier);
            }

            if (player.isInLava()) {
                var liquidHeight = player.getFluidHeight(FluidTags.LAVA);
                liquidHeight = Math.min(liquidHeight, 1F);
                direction = direction.multiply(0.3, 0.3, 0.3);
            }

            var block = player.level().getBlockState(player.blockPosition().below()).getBlock();
            var slipperiness = block.getFriction();
            var defaultSlipperiness = Blocks.GRASS_BLOCK.getFriction();
            if (slipperiness > defaultSlipperiness) {
                var multiplier = defaultSlipperiness / slipperiness;
                direction = direction.multiply(multiplier * multiplier, multiplier * multiplier, multiplier * multiplier);
            }

            player.push(direction.x, direction.y, direction.z);
            rollManager.onRoll(player);

            var rollVisuals = new RollEffect.Visuals(CombatRollMod.ID + ":roll", PUFF);
            Platform.networkC2S_Send(new Packets.RollPublish(player.getId(), rollVisuals, direction));
            RollEffect.playVisuals(rollVisuals, player, direction);
        }
    }
}
