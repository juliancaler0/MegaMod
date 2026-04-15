package com.ultra.megamod.lib.emf.models.animation.state;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.ultra.megamod.lib.emf.models.animation.EMFAttachments;
import com.ultra.megamod.lib.emf.utils.EMFEntity;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;

import java.util.function.Function;

public interface EMFEntityRenderState extends ETFEntityRenderState {

    /**
     * Deprecated - replace usages with 1.21+ impl that doesn't smuggle entity
     */
    @Deprecated
    EMFEntity emfEntity();

    double prevX();
    double x();
    double prevY();
    double y();
    double prevZ();
    double z();

    float prevPitch();
    float pitch();

    boolean isTouchingWater();
    boolean isOnFire();
    boolean hasVehicle();
    boolean isOnGround();
    boolean isAlive();
    boolean isGlowing();
    boolean isInLava();
    boolean isInvisible();
    boolean hasPassengers();
    boolean isSneaking();
    boolean isSprinting();
    boolean isWet();

    float age();
    float yaw();

    Vec3 emfVelocity(); // nullable

    String typeString(); // nullable

    Object2FloatOpenHashMap<String> variableMap(); // nullable

    Function<Identifier, RenderType> layerFactory();
    void setLayerFactory(Function<Identifier, RenderType> layerFactory);

    @Nullable EMFAttachments leftArmOverride();
    void setLeftArmOverride(EMFAttachments override);
    @Nullable EMFAttachments rightArmOverride();
    void setRightArmOverride(EMFAttachments override);


    void setBipedPose(EMFBipedPose pose);
    /** Returns the biped pose if it was animated. */
    @Nullable EMFBipedPose getBipedPose();

    @Nullable
    static EMFEntityRenderState from(net.minecraft.client.renderer.entity.state.EntityRenderState state) {
        if (state instanceof HoldsETFRenderState holds && holds.etf$getState() instanceof EMFEntityRenderState emf) {
            return emf;
        }
        return null;
    }
}