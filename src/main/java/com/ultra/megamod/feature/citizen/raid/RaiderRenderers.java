package com.ultra.megamod.feature.citizen.raid;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.function.Supplier;

/**
 * Registers entity renderers for all 18 raider entity types.
 * Uses a simple HumanoidMobRenderer with culture-specific textures.
 */
public class RaiderRenderers {

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Barbarians
        registerRaider(event, RaiderEntityRegistry.BARBARIAN, "barbarian");
        registerRaider(event, RaiderEntityRegistry.ARCHER_BARBARIAN, "archer_barbarian");
        registerRaider(event, RaiderEntityRegistry.CHIEF_BARBARIAN, "chief_barbarian");
        // Pirates
        registerRaider(event, RaiderEntityRegistry.PIRATE, "pirate");
        registerRaider(event, RaiderEntityRegistry.ARCHER_PIRATE, "archer_pirate");
        registerRaider(event, RaiderEntityRegistry.CAPTAIN_PIRATE, "captain_pirate");
        // Egyptians
        registerRaider(event, RaiderEntityRegistry.MUMMY, "mummy");
        registerRaider(event, RaiderEntityRegistry.ARCHER_MUMMY, "archer_mummy");
        registerRaider(event, RaiderEntityRegistry.PHARAO, "pharao");
        // Norsemen
        registerRaider(event, RaiderEntityRegistry.SHIELDMAIDEN, "shieldmaiden");
        registerRaider(event, RaiderEntityRegistry.NORSEMEN_ARCHER, "norsemen_archer");
        registerRaider(event, RaiderEntityRegistry.NORSEMEN_CHIEF, "norsemen_chief");
        // Amazons
        registerRaider(event, RaiderEntityRegistry.AMAZON_SPEARMAN, "amazon_spearman");
        registerRaider(event, RaiderEntityRegistry.ARCHER_AMAZON, "archer_amazon");
        registerRaider(event, RaiderEntityRegistry.AMAZON_CHIEF, "amazon_chief");
        // Drowned Pirates
        registerRaider(event, RaiderEntityRegistry.DROWNED_PIRATE, "drowned_pirate");
        registerRaider(event, RaiderEntityRegistry.DROWNED_ARCHER_PIRATE, "drowned_archer_pirate");
        registerRaider(event, RaiderEntityRegistry.DROWNED_CAPTAIN_PIRATE, "drowned_captain_pirate");
    }

    @SuppressWarnings("unchecked")
    private static <T extends AbstractRaiderEntity> void registerRaider(
            EntityRenderersEvent.RegisterRenderers event,
            Supplier<EntityType<T>> typeSupplier,
            String textureName) {
        event.registerEntityRenderer(typeSupplier.get(),
                (EntityRendererProvider<T>) ctx -> new RaiderRenderer<>(ctx, textureName));
    }

    public static class RaiderRenderer<T extends AbstractRaiderEntity>
            extends HumanoidMobRenderer<T, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
        private final Identifier texture;

        @SuppressWarnings("unchecked")
        public RaiderRenderer(EntityRendererProvider.Context ctx, String textureName) {
            super(ctx, (HumanoidModel<HumanoidRenderState>) (HumanoidModel<?>) new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
            this.texture = Identifier.fromNamespaceAndPath("megamod", "textures/entity/raider/" + textureName + ".png");
        }

        @Override
        public Identifier getTextureLocation(HumanoidRenderState state) {
            return texture;
        }

        @Override
        public HumanoidRenderState createRenderState() {
            return new HumanoidRenderState();
        }
    }
}
