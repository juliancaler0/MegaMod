package de.cadentem.pufferfish_unofficial_additions.mixin;

import de.cadentem.pufferfish_unofficial_additions.misc.ModificationHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements ModificationHandler {
    @Unique
    private static final String pufferfish_unofficial_additions$TAG = "pufferfish_unofficial_additions.modified";

    @Unique
    private boolean pufferfish_unofficial_additions$modified;

    @Override
    public void pufferfish_unofficial_additions$setModified(boolean modified) {
        this.pufferfish_unofficial_additions$modified = modified;
    }

    @Override
    public boolean pufferfish_unofficial_additions$wasModified() {
        return pufferfish_unofficial_additions$modified;
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void pufferfish_unofficial_additions$saveModified(final CallbackInfoReturnable<Tag> callback) {
        if (callback.getReturnValue() instanceof CompoundTag tag) {
            tag.putBoolean(pufferfish_unofficial_additions$TAG, pufferfish_unofficial_additions$modified);
        }
    }

    @Inject(method = "load", at = @At("TAIL"))
    private static void pufferfish_unofficial_additions$loadModified(final CompoundTag nbt, final CallbackInfoReturnable<MobEffectInstance> callback) {
        ((ModificationHandler) callback.getReturnValue()).pufferfish_unofficial_additions$setModified(nbt.getBoolean(pufferfish_unofficial_additions$TAG));
    }
}
