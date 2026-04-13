package com.ultra.megamod.lib.accessories.networking.client;

import com.ultra.megamod.lib.accessories.client.AccessoriesClient;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ScreenVariantPing(int entityId, boolean targetLookEntity) {

    public static final StructEndec<ScreenVariantPing> ENDEC = StructEndecBuilder.of(
            Endec.VAR_INT.fieldOf("entityId", ScreenVariantPing::entityId),
            Endec.BOOLEAN.fieldOf("targetLookEntity", ScreenVariantPing::targetLookEntity),
            ScreenVariantPing::new
    );

    public static ScreenVariantPing of(@Nullable LivingEntity livingEntity){
        return new ScreenVariantPing(livingEntity != null ? livingEntity.getId() : -1, false);
    }

    public static ScreenVariantPing of(boolean targetLookEntity){
        return new ScreenVariantPing(-1, targetLookEntity);
    }

    //@Environment(EnvType.CLIENT)
    public static void handlePacket(ScreenVariantPing packet, Player player) {
        AccessoriesClient.attemptToOpenSelectionScreen(packet.entityId, packet.targetLookEntity, player);
    }
}
