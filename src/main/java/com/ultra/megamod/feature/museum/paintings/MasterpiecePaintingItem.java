package com.ultra.megamod.feature.museum.paintings;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class MasterpiecePaintingItem extends Item {
    private final String variantName;
    private final String title;
    private final String artist;

    public MasterpiecePaintingItem(String variantName, String title, String artist, Properties props) {
        super(props);
        this.variantName = variantName;
        this.title = title;
        this.artist = artist;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Direction face = context.getClickedFace();
        if (face.getAxis().isVertical()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos hangPos = context.getClickedPos().relative(face);
        ServerLevel serverLevel = (ServerLevel) level;
        ResourceKey<PaintingVariant> key = ResourceKey.create(
            Registries.PAINTING_VARIANT,
            Identifier.fromNamespaceAndPath("megamod", this.variantName));
        Optional<Holder.Reference<PaintingVariant>> holder = serverLevel.registryAccess()
            .lookupOrThrow(Registries.PAINTING_VARIANT)
            .get(key);
        if (holder.isEmpty()) {
            return InteractionResult.FAIL;
        }
        Painting painting = new Painting(level, hangPos, face, holder.get());
        if (painting.survives()) {
            level.addFreshEntity(painting);
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, painting.position());
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal(this.title).withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.literal("by " + this.artist).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Right-click a wall to hang").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
        tooltip.accept(Component.literal("Museum Collectible").withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public String getVariantName() {
        return this.variantName;
    }

    public String getTitle() {
        return this.title;
    }

    public String getArtist() {
        return this.artist;
    }
}
