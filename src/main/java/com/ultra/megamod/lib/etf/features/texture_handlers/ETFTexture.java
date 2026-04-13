package com.ultra.megamod.lib.etf.features.texture_handlers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.config.ETFConfig;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Properties;

/**
 * Phase C ETFTexture.
 * <p>
 * Holds the variant identifier plus its emissive / enchant / blink companion ids. The
 * base renderer just calls {@link #getTextureIdentifier(ETFEntityRenderState)} which
 * returns either the base id, a blink variant, or the emissive/enchant overlay when the
 * caller is in a "special render overlay phase".
 * <p>
 * Feature-parity with upstream: {@code _e.png}, {@code _enchanted.png}, {@code _blink.png},
 * {@code _blink2.png}, with companion emissive/enchant flavours for each blink.
 */
public class ETFTexture {
    public static final String PATCH_NAMESPACE_PREFIX = "etf_patched_";

    public final @NotNull Identifier thisIdentifier;
    public TextureReturnState currentTextureState = TextureReturnState.NORMAL;
    public @Nullable String eSuffix = null;

    private @Nullable Identifier emissiveIdentifier = null;
    private @Nullable Identifier emissiveBlinkIdentifier = null;
    private @Nullable Identifier emissiveBlink2Identifier = null;
    private @Nullable Identifier enchantIdentifier = null;
    private @Nullable Identifier enchantBlinkIdentifier = null;
    private @Nullable Identifier enchantBlink2Identifier = null;
    private @Nullable Identifier blinkIdentifier = null;
    private @Nullable Identifier blink2Identifier = null;

    private int blinkLength;
    private int blinkFrequency;

    public ETFTexture(@NotNull Identifier variantIdentifier) {
        this.thisIdentifier = variantIdentifier;
        this.blinkLength = ETF.config().getConfig().blinkLength;
        this.blinkFrequency = ETF.config().getConfig().blinkFrequency;
        setupBlinking();
        setupEmissives();
        setupEnchants();
    }

    public static ETFTexture manual(@NotNull Identifier modifiedSkinIdentifier,
                                    @Nullable Identifier emissiveIdentifier,
                                    @Nullable Identifier enchantIdentifier) {
        ETFTexture tex = new ETFTexture(modifiedSkinIdentifier);
        if (emissiveIdentifier != null) tex.emissiveIdentifier = emissiveIdentifier;
        if (enchantIdentifier != null) tex.enchantIdentifier = enchantIdentifier;
        tex.eSuffix = emissiveIdentifier != null ? "_e" : tex.eSuffix;
        return tex;
    }

    private void setupBlinking() {
        ETFConfig cfg = ETF.config().getConfig();
        if (!cfg.enableBlinking) return;
        var mc = Minecraft.getInstance();
        if (mc == null) return;
        var resMan = mc.getResourceManager();
        Optional<Resource> base = resMan.getResource(thisIdentifier);
        if (base.isEmpty()) return;

        Identifier blinkId = ETFUtils2.replaceIdentifier(thisIdentifier, "\\.png$", "_blink.png");
        if (blinkId != null && resMan.getResource(blinkId).isPresent()) {
            blinkIdentifier = blinkId;
            Identifier blink2 = ETFUtils2.replaceIdentifier(thisIdentifier, "\\.png$", "_blink2.png");
            if (blink2 != null && resMan.getResource(blink2).isPresent()) {
                blink2Identifier = blink2;
            }
            Identifier propId = ETFUtils2.replaceIdentifier(blinkId, "\\.png$", ".properties");
            if (propId != null) {
                Properties p = ETFUtils2.readAndReturnPropertiesElseNull(propId);
                if (p != null) {
                    try {
                        if (p.containsKey("blinkLength"))
                            blinkLength = Integer.parseInt(p.getProperty("blinkLength").replaceAll("\\D", ""));
                        if (p.containsKey("blinkFrequency"))
                            blinkFrequency = Integer.parseInt(p.getProperty("blinkFrequency").replaceAll("\\D", ""));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    private void setupEmissives() {
        ETFConfig cfg = ETF.config().getConfig();
        if (!cfg.enableEmissiveTextures) return;
        var mc = Minecraft.getInstance();
        if (mc == null) return;
        var resMan = mc.getResourceManager();

        // 1. Try the explicit suffix registered by the resource pack (optifine.properties).
        // For now we use the default "_e" and always-check vanilla emissive suffix path.
        String[] suffixesToTry = cfg.alwaysCheckVanillaEmissiveSuffix ? new String[]{"_e"} : new String[]{"_e"};

        for (String suffix : suffixesToTry) {
            Identifier eId = ETFUtils2.replaceIdentifier(thisIdentifier, "\\.png$", suffix + ".png");
            if (eId != null && resMan.getResource(eId).isPresent()) {
                emissiveIdentifier = eId;
                eSuffix = suffix;

                if (blinkIdentifier != null) {
                    Identifier eb = ETFUtils2.replaceIdentifier(blinkIdentifier, "\\.png$", suffix + ".png");
                    if (eb != null && resMan.getResource(eb).isPresent()) emissiveBlinkIdentifier = eb;
                }
                if (blink2Identifier != null) {
                    Identifier eb2 = ETFUtils2.replaceIdentifier(blink2Identifier, "\\.png$", suffix + ".png");
                    if (eb2 != null && resMan.getResource(eb2).isPresent()) emissiveBlink2Identifier = eb2;
                }
                break;
            }
        }
    }

    private void setupEnchants() {
        ETFConfig cfg = ETF.config().getConfig();
        if (!cfg.enableEnchantedTextures) return;
        var mc = Minecraft.getInstance();
        if (mc == null) return;
        var resMan = mc.getResourceManager();

        Identifier id = ETFUtils2.replaceIdentifier(thisIdentifier, "\\.png$", "_enchanted.png");
        if (id != null && resMan.getResource(id).isPresent()) {
            enchantIdentifier = id;

            if (blinkIdentifier != null) {
                Identifier b = ETFUtils2.replaceIdentifier(blinkIdentifier, "\\.png$", "_enchanted.png");
                if (b != null && resMan.getResource(b).isPresent()) enchantBlinkIdentifier = b;
            }
            if (blink2Identifier != null) {
                Identifier b2 = ETFUtils2.replaceIdentifier(blink2Identifier, "\\.png$", "_enchanted.png");
                if (b2 != null && resMan.getResource(b2).isPresent()) enchantBlink2Identifier = b2;
            }
        }
    }

    @NotNull
    public Identifier getTextureIdentifier(@Nullable ETFEntityRenderState entity) {
        // Blink handling: pick blink1/blink2 periodically when present.
        if ((blinkIdentifier != null) && entity != null) {
            int frame = (int) ((entity.world() != null ? entity.world().getGameTime() : 0) % Math.max(1, blinkFrequency));
            if (frame < blinkLength) {
                if (blink2Identifier != null && (frame % 2 == 0)) {
                    currentTextureState = TextureReturnState.BLINK2;
                    return blink2Identifier;
                }
                currentTextureState = TextureReturnState.BLINK;
                return blinkIdentifier;
            }
        }
        currentTextureState = TextureReturnState.NORMAL;
        return thisIdentifier;
    }

    @Nullable
    public Identifier getEmissiveIdentifierOfCurrentState() {
        return switch (currentTextureState) {
            case BLINK -> emissiveBlinkIdentifier != null ? emissiveBlinkIdentifier : emissiveIdentifier;
            case BLINK2 -> emissiveBlink2Identifier != null ? emissiveBlink2Identifier : emissiveIdentifier;
            default -> emissiveIdentifier;
        };
    }

    @Nullable
    public Identifier getEnchantIdentifierOfCurrentState() {
        return switch (currentTextureState) {
            case BLINK -> enchantBlinkIdentifier != null ? enchantBlinkIdentifier : enchantIdentifier;
            case BLINK2 -> enchantBlink2Identifier != null ? enchantBlink2Identifier : enchantIdentifier;
            default -> enchantIdentifier;
        };
    }

    public boolean isEmissive() {
        return emissiveIdentifier != null;
    }

    public boolean isEnchanted() {
        return enchantIdentifier != null;
    }

    public boolean exists() {
        var mc = Minecraft.getInstance();
        if (mc == null) return false;
        return mc.getResourceManager().getResource(thisIdentifier).isPresent();
    }

    // ========================================================================
    // Emissive rendering helpers — port of upstream's renderEmissive methods.
    // ========================================================================

    public void renderEmissive(PoseStack poseStack, MultiBufferSource buffers, ModelPart part) {
        renderEmissive(poseStack, buffers, part, ETF.config().getConfig().getEmissiveRenderMode());
    }

    public void renderEmissive(PoseStack poseStack, MultiBufferSource buffers, ModelPart part,
                               ETFConfig.EmissiveRenderModes mode) {
        VertexConsumer vc = getEmissiveVertexConsumer(buffers, null, mode);
        if (vc != null) {
            ETFRenderContext.startSpecialRenderOverlayPhase();
            part.render(poseStack, vc, ETF.EMISSIVE_FEATURE_LIGHT_VALUE, OverlayTexture.NO_OVERLAY);
            ETFRenderContext.endSpecialRenderOverlayPhase();
        }
    }

    public void renderEmissive(PoseStack poseStack, MultiBufferSource buffers, Model model) {
        renderEmissive(poseStack, buffers, model, ETF.config().getConfig().getEmissiveRenderMode());
    }

    public void renderEmissive(PoseStack poseStack, MultiBufferSource buffers, Model model,
                               ETFConfig.EmissiveRenderModes mode) {
        VertexConsumer vc = getEmissiveVertexConsumer(buffers, model, mode);
        if (vc != null) {
            ETFRenderContext.startSpecialRenderOverlayPhase();
            model.renderToBuffer(poseStack, vc, ETF.EMISSIVE_FEATURE_LIGHT_VALUE, OverlayTexture.NO_OVERLAY);
            ETFRenderContext.endSpecialRenderOverlayPhase();
        }
    }

    @Nullable
    public VertexConsumer getEmissiveVertexConsumer(MultiBufferSource buffers, @Nullable Model model,
                                                    ETFConfig.EmissiveRenderModes mode) {
        RenderType type = getEmissiveRenderLayer(model, mode);
        if (type == null) return null;
        return buffers.getBuffer(type);
    }

    @Nullable
    public RenderType getEmissiveRenderLayer(@Nullable Model model) {
        return getEmissiveRenderLayer(model, ETF.config().getConfig().getEmissiveRenderMode());
    }

    @Nullable
    public RenderType getEmissiveRenderLayer(@Nullable Model model, ETFConfig.EmissiveRenderModes mode) {
        ETFRenderContext.preventRenderLayerTextureModify();
        try {
            if (!isEmissive()) return null;
            Identifier id = getEmissiveIdentifierOfCurrentState();
            if (id == null) return null;
            if (mode == ETFConfig.EmissiveRenderModes.BRIGHT) {
                return RenderTypes.beaconBeam(id, true);
            }
            if (model == null) {
                return RenderTypes.entityCutoutNoCull(id);
            }
            return model.renderType(id);
        } finally {
            ETFRenderContext.allowRenderLayerTextureModify();
        }
    }

    // ========================================================================
    // Enchant rendering helpers.
    // ========================================================================

    public void renderEnchanted(PoseStack poseStack, MultiBufferSource buffers, Model model) {
        if (!isEnchanted()) return;
        Identifier id = getEnchantIdentifierOfCurrentState();
        if (id == null) return;
        ETFRenderContext.preventRenderLayerTextureModify();
        try {
            // Use the entity-cutout render type on the enchant texture itself, then
            // overlay the vanilla glint as an extra pass via getBuffer(glint()).
            RenderType baseType = RenderTypes.entityCutoutNoCull(id);
            VertexConsumer vc = com.mojang.blaze3d.vertex.VertexMultiConsumer.create(
                    buffers.getBuffer(RenderTypes.glint()),
                    buffers.getBuffer(baseType));
            ETFRenderContext.startSpecialRenderOverlayPhase();
            model.renderToBuffer(poseStack, vc, ETF.EMISSIVE_FEATURE_LIGHT_VALUE, OverlayTexture.NO_OVERLAY);
            ETFRenderContext.endSpecialRenderOverlayPhase();
        } finally {
            ETFRenderContext.allowRenderLayerTextureModify();
        }
    }

    @Override
    public String toString() {
        return "ETFTexture{" + thisIdentifier + ", emissive=" + isEmissive() + ", enchant=" + isEnchanted() + "}";
    }

    public enum TextureReturnState {
        NORMAL,
        BLINK,
        BLINK2
    }
}
