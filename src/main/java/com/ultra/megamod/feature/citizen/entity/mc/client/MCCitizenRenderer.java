package com.ultra.megamod.feature.citizen.entity.mc.client;

import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

/**
 * Renderer for MCEntityCitizen entities (MineColonies-ported).
 * <p>
 * Uses the standard humanoid player model with 128x64 texture UV mapping
 * (matching MineColonies citizen textures). Texture is selected based on
 * the citizen's gender, texture ID, and skin tone suffix.
 */
public class MCCitizenRenderer extends HumanoidMobRenderer<MCEntityCitizen, MCCitizenRenderState, HumanoidModel<MCCitizenRenderState>> {

    /**
     * Custom model layer location for citizen entities.
     * Uses 128x64 UV mapping to match the MineColonies citizen texture format.
     */
    public static final ModelLayerLocation CITIZEN_LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath("megamod", "mc_citizen"), "main");

    private static final Identifier DEFAULT_TEXTURE =
            Identifier.fromNamespaceAndPath("megamod", "textures/entity/citizen/default/citizenmale1_a.png");

    /** Number of texture variants per gender (citizenmale1, citizenmale2, citizenmale3) */
    private static final int NUM_TEXTURES = 3;

    /**
     * Creates the citizen model layer definition.
     * Uses 128x64 texture dimensions to match MineColonies citizen textures,
     * which are 2x resolution versions of the legacy 64x32 player skin layout.
     */
    public static LayerDefinition createCitizenLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(mesh, 128, 64);
    }

    @SuppressWarnings("unchecked")
    public MCCitizenRenderer(EntityRendererProvider.Context ctx) {
        super(ctx,
                (HumanoidModel<MCCitizenRenderState>) (HumanoidModel<?>) new HumanoidModel<>(ctx.bakeLayer(CITIZEN_LAYER)),
                0.5f);
    }

    @Override
    public Identifier getTextureLocation(MCCitizenRenderState state) {
        // Build texture path: textures/entity/citizen/default/citizen{male|female}{1-N}{suffix}.png
        String gender = state.isFemale ? "female" : "male";
        int textureNum = (Math.abs(state.textureId) % NUM_TEXTURES) + 1;
        String suffix = state.textureSuffix;
        if (suffix == null || suffix.isEmpty()) {
            suffix = "_a";
        }

        String path = "textures/entity/citizen/default/citizen" + gender + textureNum + suffix + ".png";
        return Identifier.fromNamespaceAndPath("megamod", path);
    }

    @Override
    public MCCitizenRenderState createRenderState() {
        return new MCCitizenRenderState();
    }

    @Override
    public void extractRenderState(MCEntityCitizen entity, MCCitizenRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isFemale = entity.isFemale();
        state.textureId = entity.getTextureId();
        state.textureSuffix = entity.getTextureSuffix();
        state.isChild = entity.isBaby();
        state.citizenName = entity.getCitizenName();

        var job = entity.getCitizenJobHandler().getColonyJob();
        state.jobName = job != null ? job.getDisplayName() : "";
    }
}
