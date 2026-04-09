package com.ultra.megamod.feature.dungeons.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.entity.GrottolEntity;

public class CapturedGrottolItem extends Item {
    public CapturedGrottolItem(Item.Properties props) {
        super(props.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack stack = player.getItemInHand(hand);
        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos spawnPos = player.blockPosition().relative(player.getDirection(), 2);

        GrottolEntity grottol = new GrottolEntity(DungeonEntityRegistry.GROTTOL.get(), level);
        grottol.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        grottol.setCustomName(Component.literal("Friendly Grottol").withStyle(ChatFormatting.GREEN));
        grottol.setCustomNameVisible(true);
        grottol.setNoAi(false);
        grottol.setHealth(grottol.getMaxHealth());
        level.addFreshEntity(grottol);

        serverLevel.playSound(null, spawnPos, SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 1.0f, 1.0f);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Release a friendly Grottol").withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.literal("It will mine nearby ores for 60 seconds").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }
}
