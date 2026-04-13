package com.ultra.megamod.lib.accessories.owo.ui.component;

import com.ultra.megamod.lib.accessories.owo.ui.base.BaseComponent;
import com.ultra.megamod.lib.accessories.owo.ui.core.*;
import com.ultra.megamod.lib.accessories.owo.ui.core.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.function.Consumer;

/**
 * Adapter stub for io.wispforest.owo.ui.component.EntityComponent.
 */
public class EntityComponent<E extends Entity> extends BaseComponent {

    protected E entity;
    protected float scale = 1f;
    protected boolean lookAtCursor = false;
    protected float mouseRotation = 0f;
    protected boolean showNametag = false;
    protected Consumer<Matrix4f> transform = m -> {};
    protected EntityRenderDispatcher manager;

    protected EntityComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        this.manager = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    @SuppressWarnings("unchecked")
    protected EntityComponent(Sizing horizontalSizing, E entity) {
        this.entity = entity;
        this.manager = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    @SuppressWarnings("unchecked")
    protected EntityComponent(Sizing horizontalSizing, EntityType<E> type, @Nullable CompoundTag nbt) {
        this.manager = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    public E entity() { return entity; }
    public EntityComponent<E> entity(E entity) { this.entity = entity; return this; }

    public EntityComponent<E> scale(float value) { this.scale = value; return this; }
    public EntityComponent<E> scale(int value) { this.scale = value; return this; }
    public EntityComponent<E> scaleToFit(boolean value) { return this; }
    public EntityComponent<E> allowMouseRotation(boolean value) { return this; }
    public EntityComponent<E> lookAtCursor(boolean value) { this.lookAtCursor = value; return this; }
    public EntityComponent<E> showNametag(boolean value) { this.showNametag = value; return this; }

    @Override
    public Sizing horizontalSizing() { return super.horizontalSizing.get(); }

    @Override
    public Sizing verticalSizing() { return super.verticalSizing.get(); }

    @Override
    public Component horizontalSizing(Sizing sizing) {
        super.horizontalSizing = AnimatableProperty.of(sizing);
        return this;
    }

    @Override
    public Component verticalSizing(Sizing sizing) {
        super.verticalSizing = AnimatableProperty.of(sizing);
        return this;
    }

    public boolean onKeyPress(net.minecraft.client.input.KeyEvent event) { return false; }

    /**
     * Stub for entity element render state.
     */
    public record EntityElementRenderState(
        Object entityState,
        Matrix4f matrix,
        net.minecraft.client.gui.navigation.ScreenRectangle viewport,
        Object scissor
    ) {}
}
