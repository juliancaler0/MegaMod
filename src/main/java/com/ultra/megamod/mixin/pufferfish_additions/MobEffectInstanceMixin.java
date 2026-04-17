package com.ultra.megamod.mixin.pufferfish_additions;

import com.ultra.megamod.lib.pufferfish_additions.misc.ModificationHandler;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of {@code de.cadentem.pufferfish_unofficial_additions.mixin.MobEffectInstanceMixin}.
 *
 * <p>Upstream implementation injected into {@code MobEffectInstance#save} and
 * {@code MobEffectInstance#load} to persist the {@code pufferfish_unofficial_additions$modified}
 * flag across NBT serialization. In Minecraft 1.21.11 those methods no longer exist —
 * {@link MobEffectInstance} uses {@code CODEC} / {@code STREAM_CODEC} for serialization and
 * doesn't expose any {@code save(CompoundTag)} / {@code load(CompoundTag)} entry point to inject
 * into. Persisting a bespoke flag through the codec would force a format change to vanilla NBT
 * which we don't want.</p>
 *
 * <p>We therefore keep the {@link ModificationHandler} interface as a runtime-only marker: the
 * flag lives only as long as the instance does. On reload ({@code /effect clear} round-trip,
 * logout/relogin, server restart) {@link MobEffectInstance} objects are reconstructed via the
 * codec and start with {@code modified == false}. This matches the observable behaviour of the
 * upstream mod for fresh instances. The flag is still set by {@code EffectReward.modifyEffect}
 * and checked by {@code LivingEntityMixin} so the in-session re-entrancy guard still works.</p>
 */
@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements ModificationHandler {
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
}
