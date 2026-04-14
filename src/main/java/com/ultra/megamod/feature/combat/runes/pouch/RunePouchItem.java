package com.ultra.megamod.feature.combat.runes.pouch;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * A rune pouch — right-click to open a GUI showing its contents. Only rune
 * items can be placed inside. Persisted via CUSTOM_DATA on the stack.
 */
public class RunePouchItem extends Item {

    private final RunePouchType type;

    public RunePouchItem(Properties props, RunePouchType type) {
        super(props);
        this.type = type;
    }

    public RunePouchType type() { return type; }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;

        int size = type.slots();
        sp.openMenu(new MenuProvider() {
            @Override public Component getDisplayName() { return Component.literal(type.displayName()); }
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player player1) {
                return RunePouchMenu.create(id, inv, size, stack);
            }
        }, buf -> {
            buf.writeVarInt(size);
            buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
        });
        return InteractionResult.CONSUME;
    }
}
