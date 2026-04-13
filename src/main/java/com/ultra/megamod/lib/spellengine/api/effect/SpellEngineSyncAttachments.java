package com.ultra.megamod.lib.spellengine.api.effect;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Supplier;

/**
 * Registers NeoForge data attachments for SpellEngine effect synchronization.
 * Replaces the SynchedEntityData approach (which is blocked by NeoForge 21.11 for mixin-injected data).
 */
public class SpellEngineSyncAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "megamod");

    /**
     * Synced string attachment that holds encoded status effect data.
     * Format: "id:amplifier-id:amplifier-..." for effects that are marked as Synchronized.
     * Empty string means no synced effects.
     */
    public static final Supplier<AttachmentType<String>> SYNCED_EFFECTS = ATTACHMENT_TYPES.register(
            "spellengine_synced_effects",
            () -> AttachmentType.builder(() -> "")
                    .sync(StreamCodec.of(
                            (buf, value) -> buf.writeUtf(value),
                            buf -> buf.readUtf()
                    ))
                    .build()
    );

    /**
     * Synced string attachment for spell cast progress (JSON).
     * Replaces SynchedEntityData SPELL_ENGINE_SPELL_PROGRESS.
     */
    public static final Supplier<AttachmentType<String>> SPELL_PROGRESS = ATTACHMENT_TYPES.register(
            "spellengine_spell_progress",
            () -> AttachmentType.builder(() -> "")
                    .sync(StreamCodec.of(
                            (buf, value) -> buf.writeUtf(value),
                            buf -> buf.readUtf()
                    ))
                    .build()
    );

    /**
     * Synced float attachment for extra slipperiness during melee attacks.
     * Replaces SynchedEntityData SPELL_ENGINE_EXTRA_SLIPPERINESS.
     */
    public static final Supplier<AttachmentType<Float>> EXTRA_SLIPPERINESS = ATTACHMENT_TYPES.register(
            "spellengine_extra_slipperiness",
            () -> AttachmentType.builder(() -> 0F)
                    .sync(StreamCodec.of(
                            (buf, value) -> buf.writeFloat(value),
                            buf -> buf.readFloat()
                    ))
                    .build()
    );

    public static void init(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
