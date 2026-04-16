package com.ultra.megamod.mixin.pufferfish_skills;

import net.minecraft.world.level.chunk.LevelChunk;
import com.ultra.megamod.lib.pufferfish_skills.access.WorldChunkAccess;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.util.AntiFarmingPerChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelChunk.class)
public abstract class WorldChunkMixin implements WorldChunkAccess {
	@Unique
	private final AntiFarmingPerChunk.State antiFarmingState = new AntiFarmingPerChunk.State();

	@Override
	public AntiFarmingPerChunk.State getAntiFarmingPerChunkState() {
		return antiFarmingState;
	}
}
