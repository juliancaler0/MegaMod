package com.ultra.megamod.lib.accessories.networking.server;

import com.ultra.megamod.lib.accessories.Accessories;
import com.ultra.megamod.lib.accessories.menu.AccessoriesMenuVariant;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import com.ultra.megamod.lib.accessories.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ScreenOpen(int entityId, boolean targetLookEntity, AccessoriesMenuVariant variant, @Nullable ItemStack creativeCarriedStack) {

    public static final StructEndec<ScreenOpen> ENDEC = StructEndecBuilder.of(
            Endec.VAR_INT.fieldOf("entityId", ScreenOpen::entityId),
            Endec.BOOLEAN.fieldOf("targetLookEntity", ScreenOpen::targetLookEntity),
            Endec.forEnum(AccessoriesMenuVariant.class).fieldOf("screenType", ScreenOpen::variant),
            MinecraftEndecs.ITEM_STACK.nullableOf().fieldOf("creativeCarriedStack", ScreenOpen::creativeCarriedStack),
            ScreenOpen::new
    );

    public static ScreenOpen of(@Nullable LivingEntity livingEntity, AccessoriesMenuVariant variant){
        return of(livingEntity, variant, null);
    }

    public static ScreenOpen of(@Nullable LivingEntity livingEntity, AccessoriesMenuVariant variant, @Nullable ItemStack creativeCarriedStack){
        return new ScreenOpen(livingEntity != null ? livingEntity.getId() : -1, false, variant, creativeCarriedStack);
    }

    public static void handlePacket(ScreenOpen packet, Player player) {
        LivingEntity livingEntity = null;

        if(packet.entityId() != -1) {
            var entity = player.level().getEntity(packet.entityId());

            if(entity instanceof LivingEntity living) {
                livingEntity = living;

                // Simple permission check - ops can open from any distance
                var isOp = (player instanceof ServerPlayer sp) && Commands.LEVEL_GAMEMASTERS.check(sp.createCommandSourceStack().permissions());
                var bl = !player.equals(livingEntity)
                        && !isOp
                        && player.distanceTo(livingEntity) > 6.0;

                // Prevent people without op perms to have the ability to open inv from any distance
                if(bl) return;
            }
        } else if(packet.targetLookEntity()) {
            Accessories.attemptOpenScreenPlayer((ServerPlayer) player, packet.variant());

            return;
        }

        ItemStack carriedStack = null;

        if (packet.creativeCarriedStack != null && player.isCreative()) {
            carriedStack = packet.creativeCarriedStack;
        } else if(player.containerMenu instanceof AbstractContainerMenu oldMenu) {
            var currentCarriedStack = oldMenu.getCarried();

            if(!currentCarriedStack.isEmpty()) {
                carriedStack = currentCarriedStack;

                oldMenu.setCarried(ItemStack.EMPTY);
            }
        }

        Accessories.openAccessoriesMenu(player, packet.variant(), livingEntity, carriedStack);
    }
}