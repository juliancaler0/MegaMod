package team.creative.ambientsounds.sound;

import java.io.IOException;

import net.minecraft.resources.ResourceLocation;

public interface OggAudioStreamExtended {
    
    public boolean setPositionRandomly(long length, ResourceLocation id) throws IOException;
}
