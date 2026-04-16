package com.ultra.megamod.lib.pufferfish_skills.access;

import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.util.AntiFarmingPerChunk;

public interface WorldChunkAccess {
	AntiFarmingPerChunk.State getAntiFarmingPerChunkState();
}
