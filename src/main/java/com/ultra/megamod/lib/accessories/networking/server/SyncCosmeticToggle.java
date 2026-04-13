package com.ultra.megamod.lib.accessories.networking.server;

import com.ultra.megamod.lib.accessories.api.events.AllowEntityModificationCallback;
import com.ultra.megamod.lib.accessories.api.slot.SlotType;
import com.ultra.megamod.lib.accessories.data.SlotTypeLoader;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record SyncCosmeticToggle(@Nullable Integer entityId, String slotName, int slotIndex)  {

    public static final StructEndec<SyncCosmeticToggle> ENDEC = StructEndecBuilder.of(
            Endec.VAR_INT.nullableOf().fieldOf("entityId", SyncCosmeticToggle::entityId),
            Endec.STRING.fieldOf("slotName", SyncCosmeticToggle::slotName),
            Endec.VAR_INT.fieldOf("slotIndex", SyncCosmeticToggle::slotIndex),
            SyncCosmeticToggle::new
    );

    public static SyncCosmeticToggle of(@Nullable LivingEntity livingEntity, SlotType slotType, int slotIndex){
        return new SyncCosmeticToggle(livingEntity != null ? livingEntity.getId() : null, slotType.name(), slotIndex);
    }

    public static void handlePacket(SyncCosmeticToggle packet, Player player) {
        if(player.level().isClientSide()) return;

        LivingEntity targetEntity = player;

        if(packet.entityId() != null) {
            if(!(player.level().getEntity(packet.entityId()) instanceof LivingEntity livingEntity)) {
                return;
            }

            targetEntity = livingEntity;

            var result = AllowEntityModificationCallback.EVENT.invoker().allowModifications(targetEntity, player, null);

            if(!result.orElse(false)) return;
        }

        var capability = ((com.ultra.megamod.lib.accessories.pond.AccessoriesAPIAccess) targetEntity).accessoriesCapability();

        if(capability == null) return;

        var slotType = SlotTypeLoader.getSlotType(player.level(), packet.slotName());

        if(slotType == null) return;

        var container = capability.getContainer(slotType);

        container.setShouldRender(packet.slotIndex(), !container.shouldRender(packet.slotIndex()));

        container.markChanged(false);
    }
}
