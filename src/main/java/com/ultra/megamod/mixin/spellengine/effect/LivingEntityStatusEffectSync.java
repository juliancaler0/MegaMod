package com.ultra.megamod.mixin.spellengine.effect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import com.ultra.megamod.lib.spellengine.api.effect.Synchronized;
import com.ultra.megamod.lib.spellengine.api.effect.SpellEngineSyncAttachments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityStatusEffectSync extends Entity implements Synchronized.Provider {
    @Shadow @Final private Map<Holder<MobEffect>, MobEffectInstance> activeEffects;

    @Unique
    private final ArrayList<Synchronized.Effect> SpellEngine_syncedStatusEffects = new ArrayList<>();

    @Unique
    private String megamod$lastSyncedValue = "";

    public LivingEntityStatusEffectSync(EntityType<?> type, Level world) {
        super(type, world);
    }

    /**
     * `updateInvisibilityStatus` is called upon effects of the entity being changed.
     * We use it to update the synced attachment value.
     */
    @Inject(method = "updateInvisibilityStatus", at = @At("HEAD"))
    private void updatePotionVisibility_HEAD_SpellEngine_SyncEffects(CallbackInfo ci) {
        if (!this.level().isClientSide()) {
            String encoded;
            if (activeEffects.isEmpty()) {
                encoded = "";
            } else {
                encoded = megamod$encodedStatusEffects();
            }
            this.setData(SpellEngineSyncAttachments.SYNCED_EFFECTS.get(), encoded);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick_HEAD_SpellEngine_SyncEffectsCheck(CallbackInfo ci) {
        if (this.level().isClientSide()) {
            String current = this.getData(SpellEngineSyncAttachments.SYNCED_EFFECTS.get());
            if (!current.equals(megamod$lastSyncedValue)) {
                megamod$lastSyncedValue = current;
                SpellEngine_syncedStatusEffects.clear();
                SpellEngine_syncedStatusEffects.addAll(megamod$decodeStatusEffects(current));
            }
        }
    }

    @Unique
    private String megamod$encodedStatusEffects() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (var entry : activeEffects.entrySet()) {
            var effect = entry.getKey().value();
            if (((Synchronized)effect).shouldSynchronize()) {
                int id = BuiltInRegistries.MOB_EFFECT.getId(effect);
                int amplifier = entry.getValue().getAmplifier();
                if (i > 0) {
                    builder.append("-");
                }
                builder.append(id).append(":").append(amplifier);
                i += 1;
            }
        }
        return builder.toString();
    }

    @Unique
    private static List<Synchronized.Effect> megamod$decodeStatusEffects(String string) {
        var effects = new ArrayList<Synchronized.Effect>();
        if (string == null || string.isEmpty()) {
            return effects;
        }
        for (var effect : string.split("-")) {
            var components = effect.split(":");
            if (components.length != 2) {
                continue;
            }
            try {
                int rawId = Integer.parseInt(components[0]);
                int amplifier = Integer.parseInt(components[1]);
                var statusEffect = BuiltInRegistries.MOB_EFFECT.byId(rawId);
                if (statusEffect != null) {
                    effects.add(new Synchronized.Effect(statusEffect, amplifier));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return effects;
    }

    public List<Synchronized.Effect> SpellEngine_syncedStatusEffects() {
        return SpellEngine_syncedStatusEffects;
    }
}
