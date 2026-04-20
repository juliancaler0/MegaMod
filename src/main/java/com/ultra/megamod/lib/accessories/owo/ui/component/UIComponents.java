package com.ultra.megamod.lib.accessories.owo.ui.component;

import com.ultra.megamod.lib.accessories.owo.ui.container.UIContainers;
import com.ultra.megamod.lib.accessories.owo.ui.container.FlowLayout;
import com.ultra.megamod.lib.accessories.owo.ui.core.Sizing;
import com.ultra.megamod.lib.accessories.owo.ui.core.UIComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO paginated and tabbed containers

/**
 * Utility methods for creating UI components
 */
public final class UIComponents {

    private UIComponents() {}

    // -----------------------
    // Wrapped Vanilla Widgets
    // -----------------------

    public static ButtonComponent button(Component message, Consumer<ButtonComponent> onPress) {
        return new ButtonComponent(message, onPress);
    }

    // ------------------
    // Default UIComponents
    // ------------------

    public static <E extends Entity> EntityComponent<E> entity(Sizing sizing, EntityType<E> type, @Nullable CompoundTag nbt) {
        return new EntityComponent<>(sizing, type, nbt);
    }

    public static <E extends Entity> EntityComponent<E> entity(Sizing sizing, E entity) {
        return new EntityComponent<>(sizing, entity);
    }

    public static ItemComponent item(ItemStack item) {
        return new ItemComponent(item);
    }

    public static LabelComponent label(net.minecraft.network.chat.Component text) {
        return new LabelComponent(text);
    }

    public static TextureComponent texture(Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        return new TextureComponent(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    public static TextureComponent texture(Identifier texture, int u, int v, int regionWidth, int regionHeight) {
        return new TextureComponent(texture, u, v, regionWidth, regionHeight, 256, 256);
    }

    public static BoxComponent box(Sizing horizontalSizing, Sizing verticalSizing) {
        return new BoxComponent(horizontalSizing, verticalSizing);
    }

    public static SpacerComponent spacer(int percent) {
        return new SpacerComponent(percent);
    }

    public static SpacerComponent spacer() {
        return spacer(100);
    }

    // -------
    // Utility
    // -------

    public static <T, C extends UIComponent> FlowLayout list(List<T> data, Consumer<FlowLayout> layoutConfigurator, Function<T, C> componentMaker, boolean vertical) {
        var layout = vertical ? UIContainers.verticalFlow(Sizing.content(), Sizing.content()) : UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        layoutConfigurator.accept(layout);

        for (var value : data) {
            layout.child(componentMaker.apply(value));
        }

        return layout;
    }

    public static VanillaWidgetComponent wrapVanillaWidget(AbstractWidget widget) {
        return new VanillaWidgetComponent(widget);
    }

    public static <T extends UIComponent> T createWithSizing(Supplier<T> componentMaker, Sizing horizontalSizing, Sizing verticalSizing) {
        var component = componentMaker.get();
        component.sizing(horizontalSizing, verticalSizing);
        return component;
    }


    public static DiscreteSliderComponent discreteSlider(Sizing horizontalSizing, double min, double max) {
        return new DiscreteSliderComponent(horizontalSizing, min, max);
    }
}
