package com.ultra.megamod.feature.adminmodules;

/**
 * Static state flags that mixins and NeoForge event handlers can read.
 * Updated by module onEnable/onDisable when modules toggle.
 * This avoids mixins needing to access the full module system.
 *
 * All fields are volatile for safe cross-thread reads between
 * the server tick thread (which toggles modules) and the render thread
 * (which reads these flags in mixins/event handlers).
 */
public class AdminModuleState {

    // ── Render modules ──────────────────────────────────────────────────────

    // FreeLook: decouple camera rotation from player head
    public static volatile boolean freeLookEnabled = false;
    public static volatile boolean freeLookActive = false;
    public static volatile float freeLookYaw = 0;
    public static volatile float freeLookPitch = 0;
    public static volatile float savedPlayerYaw = 0;
    public static volatile float savedPlayerPitch = 0;

    // CameraClip: prevent camera from clipping into blocks in 3rd person
    public static volatile boolean cameraClipEnabled = false;

    // NoBob: cancel view bobbing
    public static volatile boolean noBobEnabled = false;

    // Zoom: FOV divisor for zooming in
    public static volatile boolean zoomEnabled = false;
    public static volatile float zoomFactor = 4.0f;

    // CustomFOV: override FOV to a specific value
    public static volatile boolean customFovEnabled = false;
    public static volatile int customFovValue = 90;

    // ItemPhysics: dropped items render flat on ground
    public static volatile boolean itemPhysicsEnabled = false;

    // AntiOverlay: remove screen overlays (pumpkin, water, fire)
    public static volatile boolean antiOverlayEnabled = false;
    public static volatile boolean antiOverlayPumpkin = true;
    public static volatile boolean antiOverlayWater = true;
    public static volatile boolean antiOverlayFire = true;

    // BlockHighlight: custom block selection highlight color
    public static volatile boolean blockHighlightEnabled = false;
    public static volatile float blockHighlightR = 1.0f;
    public static volatile float blockHighlightG = 1.0f;
    public static volatile float blockHighlightB = 1.0f;
    public static volatile float blockHighlightA = 1.0f;

    // BetterTab: enhanced player tab list
    public static volatile boolean betterTabEnabled = false;

    // HandView: customize hand rendering
    public static volatile boolean handViewEnabled = false;
    public static volatile float handViewScale = 1.0f;
    public static volatile float handViewX = 0.0f;
    public static volatile float handViewY = 0.0f;

    // CameraTweaks: adjust third-person camera distance
    public static volatile boolean cameraTweaksEnabled = false;
    public static volatile float cameraTweaksDistance = 6.0f;

    // ── Movement modules that benefit from client-side hooks ────────────────

    // NoSlow: prevent item use slowdown (client-side component)
    public static volatile boolean noSlowEnabled = false;

    // GUIMove: allow player input while GUIs are open (client-side component)
    public static volatile boolean guiMoveEnabled = false;

    // ── Mixin-backed module flags ────────────────────────────────────────────

    // Velocity: cancel knockback on client (LivingEntityMixin)
    public static volatile boolean velocityEnabled = false;

    // NoBreakDelay: remove break delay between blocks (MultiPlayerGameModeMixin)
    public static volatile boolean noBreakDelayEnabled = false;

    // InstantMine: client-side instant break delay removal (MultiPlayerGameModeMixin)
    public static volatile boolean instantMineEnabled = false;

    // Nametags through walls: force nametag rendering (EntityRendererMixin)
    public static volatile boolean nametagThroughWallsEnabled = false;

    // Xray: triggers chunk rebuild on toggle (LevelRendererMixin)
    public static volatile boolean xrayEnabled = false;

    // Fullbright Gamma: override light texture brightness (LightTextureMixin)
    public static volatile boolean fullbrightGammaEnabled = false;

    // AttackTick / UseTick: track mouse button state (MinecraftMixin)
    public static volatile boolean attackTickEnabled = false;
    public static volatile boolean attackTickActive = false;
    public static volatile boolean useTickEnabled = false;
    public static volatile boolean useTickActive = false;

    // NoFog: disable fog rendering (FogRendererMixin)
    public static volatile boolean noFogEnabled = false;

    // ContainerButtons: steal/dump buttons on container screens (AbstractContainerScreenMixin)
    public static volatile boolean containerButtonsEnabled = false;
}
