package com.ultra.megamod.mixin.skilltree;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.skilltree.node.ConditionalAttributeReward;
import com.ultra.megamod.lib.skilltree.node.SpellContainerReward;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init_TAIL_SkillTreeRPGs(MinecraftServer server, ServerLevel world, GameProfile profile, SyncedClientOptions clientOptions, CallbackInfo ci) {
        var serverPlayer = (ServerPlayer) (Object) this;
        SkillsAPI.updateRewards(serverPlayer, SpellContainerReward.ID);
        SkillsAPI.updateRewards(serverPlayer, ConditionalAttributeReward.ID);
    }
}
