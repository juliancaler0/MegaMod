package com.ultra.megamod.mixin.spellengine.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import com.ultra.megamod.lib.spellengine.utils.SoundPlayerWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientLevel.class)
public class ClientWorldMixin implements SoundPlayerWorld {
    public void playSoundFromEntity(Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch) {
        var clientWorld = (ClientLevel) (Object) this;
        var clientPlayer = Minecraft.getInstance().player;
        clientWorld.playSeededSound(clientPlayer, entity.getX(), entity.getY(), entity.getZ(), sound, category, volume, pitch, clientWorld.random.nextLong());
    }
}
