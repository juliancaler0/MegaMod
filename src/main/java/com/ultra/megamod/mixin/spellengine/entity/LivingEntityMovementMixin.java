package com.ultra.megamod.mixin.spellengine.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.LivingEntity;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMovementMixin {
    // NeoForge completely rewrites slipperiness handling, hence this is optional
    @WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"), require = 0)
    private float getSlipperiness_Wrapped(Block instance, Operation<Float> original) {
        var result = original.call(instance);
        var entity = (LivingEntity) (Object) this;
        if (entity instanceof SpellCasterEntity caster) {
            result = Math.min(result + caster.getExtraSlipperiness(), 1F);
        }
        return result;
    }
}
