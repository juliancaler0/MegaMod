package com.ultra.megamod.mixin.spellengine.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterClient;
import com.ultra.megamod.lib.spellengine.utils.TargetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void getTeamColor_HEAD_SpellEngine(CallbackInfoReturnable<Integer> cir) {
        var entity = (Entity) ((Object)this);
        if (entity.level().isClientSide() /* && SpellEngineClient.config.useMagicColorForHighlight */) {
            var clientPlayer = Minecraft.getInstance().player;
            if (TargetHelper.isTargetedByPlayer(entity, clientPlayer)) {
                var spell = ((SpellCasterClient) clientPlayer).getCurrentSpell();
                if (spell != null) {
                    cir.setReturnValue(spell.school.color);
                    cir.cancel();
                }
            }
        }
    }
}
