package de.cadentem.pufferfish_unofficial_additions.mixin;

import de.cadentem.pufferfish_unofficial_additions.rewards.EffectReward;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Events don't provide a proper way to modify the instance (this way the immune check also happens after modifications) */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique private MobEffectInstance pufferfish_unofficial_additions$modifiedInstance;

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void pufferfish_unofficial_additions$storeModifiedEffect(final MobEffectInstance instance, final Entity entity, final CallbackInfoReturnable<Boolean> callback) {
        if ((Object) this instanceof ServerPlayer player) {
            MobEffectInstance modifiedInstance = EffectReward.modifyEffect(player, instance);

            if (modifiedInstance == null) {
                callback.setReturnValue(false);
            } else {
                pufferfish_unofficial_additions$modifiedInstance = modifiedInstance;
            }
        }
    }

    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "HEAD"), argsOnly = true)
    private MobEffectInstance pufferfish_unofficial_additions$modifyEffect(final MobEffectInstance instance) {
        if (pufferfish_unofficial_additions$modifiedInstance != null) {
            MobEffectInstance modifiedInstance = pufferfish_unofficial_additions$modifiedInstance;
            pufferfish_unofficial_additions$modifiedInstance = null;
            return modifiedInstance;
        }

        return instance;
    }
}
