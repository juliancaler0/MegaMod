/*
 * Decompiled with CFR 0.152.
 */
package com.ultra.megamod.feature.dungeons.client;

import com.ultra.megamod.feature.dungeons.boss.DungeonKeeperBoss;
import com.ultra.megamod.feature.dungeons.boss.OssukageBoss;
import com.ultra.megamod.feature.dungeons.boss.WraithBoss;
import com.ultra.megamod.feature.dungeons.client.model.ChaosSpawnerModel;
import com.ultra.megamod.feature.dungeons.client.model.FoliaathModel;
import com.ultra.megamod.feature.dungeons.client.model.FrostmawModel;
import com.ultra.megamod.feature.dungeons.client.model.GrottolModel;
import com.ultra.megamod.feature.dungeons.client.model.HollowModel;
import com.ultra.megamod.feature.dungeons.client.model.KunaiModel;
import com.ultra.megamod.feature.dungeons.client.model.LanternModel;
import com.ultra.megamod.feature.dungeons.client.model.NagaModel;
import com.ultra.megamod.feature.dungeons.client.model.RatModel;
import com.ultra.megamod.feature.dungeons.client.model.SkeletonMinionModel;
import com.ultra.megamod.feature.dungeons.client.model.SkeletonNinjaModel;
import com.ultra.megamod.feature.dungeons.client.model.SpawnerCarrierModel;
import com.ultra.megamod.feature.dungeons.client.model.UmvuthiModel;
import com.ultra.megamod.feature.dungeons.client.model.WraithModel;
import com.ultra.megamod.feature.dungeons.client.model.WroughtnautModel;
import com.ultra.megamod.feature.dungeons.client.model.SculptorModel;
import com.ultra.megamod.feature.dungeons.client.model.BluffModel;
import com.ultra.megamod.feature.dungeons.client.model.BabyFoliaathModel;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.entity.DungeonMobEntity;
import com.ultra.megamod.feature.dungeons.entity.HollowEntity;
import com.ultra.megamod.feature.dungeons.entity.MinionEntity;
import com.ultra.megamod.feature.dungeons.entity.RatEntity;
import com.ultra.megamod.feature.dungeons.entity.UndeadKnightEntity;
import com.ultra.megamod.feature.dungeons.entity.NagaEntity;
import com.ultra.megamod.feature.dungeons.entity.GrottolEntity;
import com.ultra.megamod.feature.dungeons.entity.LanternEntity;
import com.ultra.megamod.feature.dungeons.entity.FoliaathEntity;
import com.ultra.megamod.feature.dungeons.entity.UmvuthanaEntity;
import com.ultra.megamod.feature.dungeons.entity.SpawnerCarrierEntity;
import com.ultra.megamod.feature.dungeons.entity.BluffEntity;
import com.ultra.megamod.feature.dungeons.entity.BabyFoliaathEntity;
import com.ultra.megamod.feature.dungeons.entity.ChaosSpawnerEntity;
import com.ultra.megamod.feature.dungeons.entity.UmvuthanaRaptorEntity;
import com.ultra.megamod.feature.dungeons.entity.UmvuthanaFollowerEntity;
import com.ultra.megamod.feature.dungeons.entity.UmvuthanaCraneEntity;
import com.ultra.megamod.feature.dungeons.boss.FrostmawBoss;
import com.ultra.megamod.feature.dungeons.boss.SculptorBoss;
import com.ultra.megamod.feature.dungeons.boss.WroughtnautBoss;
import com.ultra.megamod.feature.dungeons.boss.UmvuthiBoss;
import com.ultra.megamod.feature.dungeons.boss.ChaosSpawnerBoss;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.AnimationState;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class DungeonEntityRenderers {
    private static final Identifier WRAITH_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/wraith.png");
    private static final Identifier OSSUKAGE_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/skeleton_ninja.png");
    private static final Identifier[] RAT_TEXTURES = new Identifier[]{
            Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/rat.png"),
            Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/rat_blue.png"),
            Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/rat_grey.png"),
            Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/rat_yellow.png")
    };
    private static final Identifier MINION_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/skeleton_minion.png");
    private static final Identifier UNDEAD_KNIGHT_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/undead_knight.png");
    private static final Identifier HOLLOW_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/hollow.png");
    private static final Identifier DUNGEON_KEEPER_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/dungeon_keeper.png");
    // New mob textures
    private static final Identifier NAGA_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/naga.png");
    private static final Identifier GROTTOL_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/grottol.png");
    private static final Identifier LANTERN_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/lantern.png");
    private static final Identifier FOLIAATH_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/foliaath.png");
    private static final Identifier UMVUTHANA_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/umvuthana.png");
    private static final Identifier SPAWNER_CARRIER_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/spawner_carrier.png");
    // New boss textures
    private static final Identifier FROSTMAW_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/frostmaw.png");
    private static final Identifier WROUGHTNAUT_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/wroughtnaut.png");
    private static final Identifier UMVUTHI_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/umvuthi.png");
    private static final Identifier CHAOS_SPAWNER_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/chaos_spawner.png");
    private static final Identifier SCULPTOR_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/sculptor.png");
    private static final Identifier BLUFF_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/bluff.png");
    private static final Identifier BABY_FOLIAATH_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/baby_foliaath.png");
    private static final Identifier WROUGHTNAUT_EYES_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/wroughtnaut_eyes.png");
    private static final Identifier CHAOS_SPAWNER_GLOW_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/chaos_spawner_glow.png");
    private static final Identifier LANTERN_GEL_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/dungeon/lantern_gel.png");

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(DungeonEntityRegistry.WRAITH_BOSS.get(), WraithRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.OSSUKAGE_BOSS.get(), OssukageRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.DUNGEON_MOB.get(), DungeonMobRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.DUNGEON_RAT.get(), DungeonRatRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.MINION.get(), MinionRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.KUNAI.get(), KunaiRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.UNDEAD_KNIGHT.get(), UndeadKnightRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.DUNGEON_SLIME.get(), DungeonSlimeRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.HOLLOW.get(), HollowRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.DUNGEON_KEEPER.get(), DungeonKeeperRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.CRYSTALLINE_BEAM.get(), NoopRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.CERULEAN_ARROW.get(), NoopRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.CRYSTAL_ARROW.get(), NoopRenderer::new);
        // New mob renderers
        event.registerEntityRenderer(DungeonEntityRegistry.NAGA.get(), NagaRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.GROTTOL.get(), GrottolRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.LANTERN.get(), LanternRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.FOLIAATH.get(), FoliaathRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.UMVUTHANA.get(), UmvuthanaRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.SPAWNER_CARRIER.get(), SpawnerCarrierRenderer::new);
        // New boss renderers
        event.registerEntityRenderer(DungeonEntityRegistry.FROSTMAW_BOSS.get(), FrostmawRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.WROUGHTNAUT_BOSS.get(), WroughtnautRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.UMVUTHI_BOSS.get(), UmvuthiRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.CHAOS_SPAWNER_BOSS.get(), ChaosSpawnerRenderer::new);
        // New entity renderers
        event.registerEntityRenderer(DungeonEntityRegistry.SCULPTOR_BOSS.get(), SculptorRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.BLUFF.get(), BluffRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.BABY_FOLIAATH.get(), BabyFoliaathRenderer::new);
        // Umvuthana variants
        event.registerEntityRenderer(DungeonEntityRegistry.UMVUTHANA_RAPTOR.get(), UmvuthanaRaptorRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.UMVUTHANA_FOLLOWER.get(), UmvuthanaFollowerRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.UMVUTHANA_CRANE.get(), UmvuthanaCraneRenderer::new);
        // Projectile renderers (all use NoopRenderer — particle-only)
        event.registerEntityRenderer(DungeonEntityRegistry.POISON_BALL.get(), NoopRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.ICE_BALL.get(), NoopRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.GHOST_BULLET.get(), NoopRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.SOLAR_BEAM.get(), NoopRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.AXE_SWING.get(), NoopRenderer::new);
        event.registerEntityRenderer(DungeonEntityRegistry.DART.get(), NoopRenderer::new);
        // Chaos Spawner gatekeeper
        event.registerEntityRenderer(DungeonEntityRegistry.CHAOS_SPAWNER_GATEKEEPER.get(), ChaosSpawnerGatekeeperRenderer::new);
    }

    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WraithModel.LAYER_LOCATION, WraithModel::createBodyLayer);
        event.registerLayerDefinition(SkeletonNinjaModel.LAYER_LOCATION, SkeletonNinjaModel::createBodyLayer);
        event.registerLayerDefinition(RatModel.LAYER_LOCATION, RatModel::createBodyLayer);
        event.registerLayerDefinition(SkeletonMinionModel.LAYER_LOCATION, SkeletonMinionModel::createBodyLayer);
        event.registerLayerDefinition(KunaiModel.LAYER_LOCATION, KunaiModel::createBodyLayer);
        // New mob models
        event.registerLayerDefinition(NagaModel.LAYER_LOCATION, NagaModel::createBodyLayer);
        event.registerLayerDefinition(GrottolModel.LAYER_LOCATION, GrottolModel::createBodyLayer);
        event.registerLayerDefinition(LanternModel.LAYER_LOCATION, LanternModel::createBodyLayer);
        event.registerLayerDefinition(FoliaathModel.LAYER_LOCATION, FoliaathModel::createBodyLayer);
        event.registerLayerDefinition(SpawnerCarrierModel.LAYER_LOCATION, SpawnerCarrierModel::createBodyLayer);
        event.registerLayerDefinition(HollowModel.LAYER_LOCATION, HollowModel::createBodyLayer);
        // New boss models
        event.registerLayerDefinition(ChaosSpawnerModel.LAYER_LOCATION, ChaosSpawnerModel::createBodyLayer);
        event.registerLayerDefinition(FrostmawModel.LAYER_LOCATION, FrostmawModel::createBodyLayer);
        event.registerLayerDefinition(WroughtnautModel.LAYER_LOCATION, WroughtnautModel::createBodyLayer);
        event.registerLayerDefinition(UmvuthiModel.LAYER_LOCATION, UmvuthiModel::createBodyLayer);
        // New models
        event.registerLayerDefinition(SculptorModel.LAYER_LOCATION, SculptorModel::createBodyLayer);
        event.registerLayerDefinition(BluffModel.LAYER_LOCATION, BluffModel::createBodyLayer);
        event.registerLayerDefinition(BabyFoliaathModel.LAYER_LOCATION, BabyFoliaathModel::createBodyLayer);
    }

    static class MinionRenderer
    extends MobRenderer<MinionEntity, MinionRenderState, SkeletonMinionModel> {
        public MinionRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new SkeletonMinionModel(ctx.bakeLayer(SkeletonMinionModel.LAYER_LOCATION)), 0.4f);
        }
        public Identifier getTextureLocation(MinionRenderState state) {
            return MINION_TEXTURE;
        }
        public MinionRenderState createRenderState() {
            return new MinionRenderState();
        }
        public void extractRenderState(MinionEntity entity, MinionRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    static class DungeonMobRenderer
    extends MobRenderer<DungeonMobEntity, DungeonMobRenderState, RatModel> {
        public DungeonMobRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new RatModel(ctx.bakeLayer(RatModel.LAYER_LOCATION)), 0.5f);
        }
        public Identifier getTextureLocation(DungeonMobRenderState state) {
            int variant = state.skinVariant % RAT_TEXTURES.length;
            return RAT_TEXTURES[variant];
        }
        public DungeonMobRenderState createRenderState() {
            return new DungeonMobRenderState();
        }
        public void extractRenderState(DungeonMobEntity entity, DungeonMobRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
            state.skinVariant = entity.getSkinVariant();
        }
    }

    static class DungeonRatRenderer
    extends MobRenderer<RatEntity, DungeonRatRenderState, RatModel> {
        public DungeonRatRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new RatModel(ctx.bakeLayer(RatModel.LAYER_LOCATION)), 0.3f);
        }
        public Identifier getTextureLocation(DungeonRatRenderState state) {
            int variant = state.skinVariant % RAT_TEXTURES.length;
            return RAT_TEXTURES[variant];
        }
        public DungeonRatRenderState createRenderState() {
            return new DungeonRatRenderState();
        }
        public void extractRenderState(RatEntity entity, DungeonRatRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
            state.skinVariant = entity.getSkinVariant();
        }
    }

    static class OssukageRenderer
    extends MobRenderer<OssukageBoss, DungeonBossRenderState, SkeletonNinjaModel> {
        public OssukageRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new SkeletonNinjaModel(ctx.bakeLayer(SkeletonNinjaModel.LAYER_LOCATION)), 0.7f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return OSSUKAGE_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(OssukageBoss entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
            state.deathAnimationState.copyFrom(entity.deathAnimationState);
        }
    }

    static class WraithRenderer
    extends MobRenderer<WraithBoss, DungeonBossRenderState, WraithModel> {
        public WraithRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new WraithModel(ctx.bakeLayer(WraithModel.LAYER_LOCATION)), 0.5f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return WRAITH_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(WraithBoss entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
            state.deathAnimationState.copyFrom(entity.deathAnimationState);
        }
    }

    // Undead Knight — reuse SkeletonNinja model (humanoid, already works with DungeonBossRenderState)
    static class UndeadKnightRenderer
    extends MobRenderer<UndeadKnightEntity, DungeonBossRenderState, SkeletonNinjaModel> {
        public UndeadKnightRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new SkeletonNinjaModel(ctx.bakeLayer(SkeletonNinjaModel.LAYER_LOCATION)), 0.5f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return UNDEAD_KNIGHT_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(UndeadKnightEntity entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    // Dungeon Slime — extends vanilla SlimeRenderer
    static class DungeonSlimeRenderer extends SlimeRenderer {
        public DungeonSlimeRenderer(EntityRendererProvider.Context ctx) {
            super(ctx);
        }
    }

    // Hollow — fast spectral mob with teleportation
    static class HollowRenderer
    extends MobRenderer<HollowEntity, DungeonBossRenderState, HollowModel> {
        public HollowRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new HollowModel(ctx.bakeLayer(HollowModel.LAYER_LOCATION)), 0.3f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return HOLLOW_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(HollowEntity entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    // Dungeon Keeper Boss — uses SkeletonNinja model
    static class DungeonKeeperRenderer
    extends MobRenderer<DungeonKeeperBoss, DungeonBossRenderState, SkeletonNinjaModel> {
        public DungeonKeeperRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new SkeletonNinjaModel(ctx.bakeLayer(SkeletonNinjaModel.LAYER_LOCATION)), 0.7f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return DUNGEON_KEEPER_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(DungeonKeeperBoss entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
            state.deathAnimationState.copyFrom(entity.deathAnimationState);
            state.scale = 1.3f; // Reference: DungeonKeeper is 1.3x scale
        }
    }

    // === NEW MOB RENDERERS ===

    // Naga — serpent creature with wings and segmented tail
    static class NagaRenderer
    extends MobRenderer<NagaEntity, DungeonMobRenderState, NagaModel> {
        public NagaRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new NagaModel(ctx.bakeLayer(NagaModel.LAYER_LOCATION)), 0.4f);
        }
        public Identifier getTextureLocation(DungeonMobRenderState state) {
            return NAGA_TEXTURE;
        }
        public DungeonMobRenderState createRenderState() {
            return new DungeonMobRenderState();
        }
        public void extractRenderState(NagaEntity entity, DungeonMobRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    // Grottol — rock creature that flees when hurt
    static class GrottolRenderer
    extends MobRenderer<GrottolEntity, DungeonMobRenderState, GrottolModel> {
        public GrottolRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new GrottolModel(ctx.bakeLayer(GrottolModel.LAYER_LOCATION)), 0.3f);
        }
        public Identifier getTextureLocation(DungeonMobRenderState state) {
            return GROTTOL_TEXTURE;
        }
        public DungeonMobRenderState createRenderState() {
            return new DungeonMobRenderState();
        }
        public void extractRenderState(GrottolEntity entity, DungeonMobRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    // Lantern — flying fire mob that shoots fireballs
    static class LanternRenderer
    extends MobRenderer<LanternEntity, DungeonMobRenderState, LanternModel> {
        public LanternRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new LanternModel(ctx.bakeLayer(LanternModel.LAYER_LOCATION)), 0.3f);
            this.addLayer(new RenderLayer<DungeonMobRenderState, LanternModel>(this) {
                @Override
                public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                                   DungeonMobRenderState state, float yRot, float xRot) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack,
                        RenderTypes.eyes(LANTERN_GEL_TEXTURE), packedLight, OverlayTexture.NO_OVERLAY,
                        -1, null, state.outlineColor, null);
                }
            });
        }
        public Identifier getTextureLocation(DungeonMobRenderState state) {
            return LANTERN_TEXTURE;
        }
        public DungeonMobRenderState createRenderState() {
            return new DungeonMobRenderState();
        }
        public void extractRenderState(LanternEntity entity, DungeonMobRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    // Foliaath — plant ambush mob, dormant until player approaches
    static class FoliaathRenderer
    extends MobRenderer<FoliaathEntity, DungeonMobRenderState, FoliaathModel> {
        public FoliaathRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new FoliaathModel(ctx.bakeLayer(FoliaathModel.LAYER_LOCATION)), 0.4f);
        }
        public Identifier getTextureLocation(DungeonMobRenderState state) {
            return FOLIAATH_TEXTURE;
        }
        public DungeonMobRenderState createRenderState() {
            return new DungeonMobRenderState();
        }
        public void extractRenderState(FoliaathEntity entity, DungeonMobRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    // Umvuthana — uses SkeletonNinjaModel (humanoid)
    static class UmvuthanaRenderer
    extends MobRenderer<UmvuthanaEntity, DungeonBossRenderState, SkeletonNinjaModel> {
        public UmvuthanaRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new SkeletonNinjaModel(ctx.bakeLayer(SkeletonNinjaModel.LAYER_LOCATION)), 0.5f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return UMVUTHANA_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(UmvuthanaEntity entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    // SpawnerCarrier — tanky mob that spawns other mobs until killed
    static class SpawnerCarrierRenderer
    extends MobRenderer<SpawnerCarrierEntity, DungeonBossRenderState, SpawnerCarrierModel> {
        public SpawnerCarrierRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new SpawnerCarrierModel(ctx.bakeLayer(SpawnerCarrierModel.LAYER_LOCATION)), 0.5f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return SPAWNER_CARRIER_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(SpawnerCarrierEntity entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    // === NEW BOSS RENDERERS ===

    // Frostmaw — massive ice titan boss with horns and tusks
    static class FrostmawRenderer
    extends MobRenderer<FrostmawBoss, DungeonBossRenderState, FrostmawModel> {
        public FrostmawRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new FrostmawModel(ctx.bakeLayer(FrostmawModel.LAYER_LOCATION)), 0.8f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return FROSTMAW_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(FrostmawBoss entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
            state.deathAnimationState.copyFrom(entity.deathAnimationState);
        }
    }

    // Wroughtnaut — hulking iron warden boss with massive axe
    static class WroughtnautRenderer
    extends MobRenderer<WroughtnautBoss, DungeonBossRenderState, WroughtnautModel> {
        public WroughtnautRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new WroughtnautModel(ctx.bakeLayer(WroughtnautModel.LAYER_LOCATION)), 0.8f);
            this.addLayer(new RenderLayer<DungeonBossRenderState, WroughtnautModel>(this) {
                @Override
                public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                                   DungeonBossRenderState state, float yRot, float xRot) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack,
                        RenderTypes.eyes(WROUGHTNAUT_EYES_TEXTURE), packedLight, OverlayTexture.NO_OVERLAY,
                        -1, null, state.outlineColor, null);
                }
            });
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return WROUGHTNAUT_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(WroughtnautBoss entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
            state.deathAnimationState.copyFrom(entity.deathAnimationState);
        }
    }

    // Umvuthi — large tribal mask lord summoner boss
    static class UmvuthiRenderer
    extends MobRenderer<UmvuthiBoss, DungeonBossRenderState, UmvuthiModel> {
        public UmvuthiRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new UmvuthiModel(ctx.bakeLayer(UmvuthiModel.LAYER_LOCATION)), 1.0f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return UMVUTHI_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(UmvuthiBoss entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
            state.deathAnimationState.copyFrom(entity.deathAnimationState);
        }
    }

    // ChaosSpawner — floating chaos entity with hexahedron core
    static class ChaosSpawnerRenderer
    extends MobRenderer<ChaosSpawnerBoss, DungeonBossRenderState, ChaosSpawnerModel> {
        public ChaosSpawnerRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new ChaosSpawnerModel(ctx.bakeLayer(ChaosSpawnerModel.LAYER_LOCATION)), 0.5f);
            this.addLayer(new RenderLayer<DungeonBossRenderState, ChaosSpawnerModel>(this) {
                @Override
                public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                                   DungeonBossRenderState state, float yRot, float xRot) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack,
                        RenderTypes.eyes(CHAOS_SPAWNER_GLOW_TEXTURE), packedLight, OverlayTexture.NO_OVERLAY,
                        -1, null, state.outlineColor, null);
                }
            });
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return CHAOS_SPAWNER_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(ChaosSpawnerBoss entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
            state.deathAnimationState.copyFrom(entity.deathAnimationState);
        }
    }

    // === SCULPTOR BOSS RENDERER ===
    static class SculptorRenderer
    extends MobRenderer<SculptorBoss, DungeonBossRenderState, SculptorModel> {
        public SculptorRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new SculptorModel(ctx.bakeLayer(SculptorModel.LAYER_LOCATION)), 0.8f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return SCULPTOR_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(SculptorBoss entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
            state.deathAnimationState.copyFrom(entity.deathAnimationState);
        }
    }

    // === BLUFF RENDERER ===
    static class BluffRenderer
    extends MobRenderer<BluffEntity, DungeonMobRenderState, BluffModel> {
        public BluffRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new BluffModel(ctx.bakeLayer(BluffModel.LAYER_LOCATION)), 0.3f);
        }
        public Identifier getTextureLocation(DungeonMobRenderState state) {
            return BLUFF_TEXTURE;
        }
        public DungeonMobRenderState createRenderState() {
            return new DungeonMobRenderState();
        }
        public void extractRenderState(BluffEntity entity, DungeonMobRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    // === BABY FOLIAATH RENDERER ===
    static class BabyFoliaathRenderer
    extends MobRenderer<BabyFoliaathEntity, DungeonMobRenderState, BabyFoliaathModel> {
        public BabyFoliaathRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new BabyFoliaathModel(ctx.bakeLayer(BabyFoliaathModel.LAYER_LOCATION)), 0.2f);
        }
        public Identifier getTextureLocation(DungeonMobRenderState state) {
            return BABY_FOLIAATH_TEXTURE;
        }
        public DungeonMobRenderState createRenderState() {
            return new DungeonMobRenderState();
        }
        public void extractRenderState(BabyFoliaathEntity entity, DungeonMobRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    public static class MinionRenderState
    extends LivingEntityRenderState {
        public AnimationState idleAnimationState = new AnimationState();
        public AnimationState attackAnimationState = new AnimationState();
    }

    public static class DungeonMobRenderState
    extends LivingEntityRenderState {
        public AnimationState idleAnimationState = new AnimationState();
        public AnimationState attackAnimationState = new AnimationState();
        public int skinVariant = 0;
    }

    public static class DungeonRatRenderState
    extends DungeonMobRenderState {
    }

    public static class DungeonBossRenderState
    extends LivingEntityRenderState {
        public AnimationState idleAnimationState = new AnimationState();
        public AnimationState attackAnimationState = new AnimationState();
        public AnimationState deathAnimationState = new AnimationState();
    }

    // === UMVUTHANA RAPTOR RENDERER (pack leader, FURY mask) ===
    static class UmvuthanaRaptorRenderer
    extends MobRenderer<UmvuthanaRaptorEntity, DungeonBossRenderState, SkeletonNinjaModel> {
        public UmvuthanaRaptorRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new SkeletonNinjaModel(ctx.bakeLayer(SkeletonNinjaModel.LAYER_LOCATION)), 0.5f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return UMVUTHANA_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(UmvuthanaRaptorEntity entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
            state.scale = 1.1f; // Raptor is slightly larger
        }
    }

    // === UMVUTHANA FOLLOWER RENDERER (pack member, FEAR mask) ===
    static class UmvuthanaFollowerRenderer
    extends MobRenderer<UmvuthanaFollowerEntity, DungeonBossRenderState, SkeletonNinjaModel> {
        public UmvuthanaFollowerRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new SkeletonNinjaModel(ctx.bakeLayer(SkeletonNinjaModel.LAYER_LOCATION)), 0.4f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return UMVUTHANA_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(UmvuthanaFollowerEntity entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    // === UMVUTHANA CRANE RENDERER (healer, FAITH mask) ===
    static class UmvuthanaCraneRenderer
    extends MobRenderer<UmvuthanaCraneEntity, DungeonBossRenderState, SkeletonNinjaModel> {
        public UmvuthanaCraneRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new SkeletonNinjaModel(ctx.bakeLayer(SkeletonNinjaModel.LAYER_LOCATION)), 0.4f);
        }
        public Identifier getTextureLocation(DungeonBossRenderState state) {
            return UMVUTHANA_TEXTURE;
        }
        public DungeonBossRenderState createRenderState() {
            return new DungeonBossRenderState();
        }
        public void extractRenderState(UmvuthanaCraneEntity entity, DungeonBossRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.idleAnimationState.copyFrom(entity.idleAnimationState);
            state.attackAnimationState.copyFrom(entity.attackAnimationState);
        }
    }

    static class ChaosSpawnerGatekeeperRenderer
    extends MobRenderer<ChaosSpawnerEntity, LivingEntityRenderState, ChaosSpawnerModel> {
        public ChaosSpawnerGatekeeperRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new ChaosSpawnerModel(ctx.bakeLayer(ChaosSpawnerModel.LAYER_LOCATION)), 0.5f);
        }
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return CHAOS_SPAWNER_TEXTURE;
        }
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }
        public void extractRenderState(ChaosSpawnerEntity entity, LivingEntityRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
        }
    }
}
