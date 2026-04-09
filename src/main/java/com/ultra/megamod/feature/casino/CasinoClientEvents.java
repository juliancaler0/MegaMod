package com.ultra.megamod.feature.casino;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.casino.network.BlackjackActionPayload;
import com.ultra.megamod.feature.casino.screen.WheelScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class CasinoClientEvents {

    private static final KeyMapping.Category MEGAMOD_CAT = new KeyMapping.Category(
            net.minecraft.resources.Identifier.fromNamespaceAndPath("megamod", "megamod"));

    public static KeyMapping CASINO_MENU_KEY;
    private static int hintTimer = 0;

    // Track whether the player has dismissed each game screen this sitting session.
    // Reset when the player stops being a passenger (dismounts).
    private static boolean dismissedRoulette = false;
    private static boolean dismissedCraps = false;
    private static boolean dismissedBaccarat = false;
    private static boolean dismissedWheel = false;
    private static boolean wasPassenger = false;

    // Tracks which game the player is currently seated at.
    // Prevents cross-game interference (e.g. roulette auto-opening while at wheel).
    private static String activeGameType = null; // "wheel", "roulette", "craps", "baccarat", "blackjack"

    // Stored table positions detected when the player sat down — used for
    // screen creation and F-key reopening so we reference the correct game.
    private static BlockPos storedRoulettePos = null;
    private static BlockPos storedCrapsPos = null;
    private static BlockPos storedBaccaratPos = null;
    private static BlockPos storedWheelPos = null;
    private static BlockPos storedBlackjackPos = null;

    public static void clearCasinoState() {
        dismissedRoulette = false;
        dismissedCraps = false;
        dismissedBaccarat = false;
        dismissedWheel = false;
        activeGameType = null;
        storedRoulettePos = null;
        storedCrapsPos = null;
        storedBaccaratPos = null;
        storedWheelPos = null;
        storedBlackjackPos = null;
        com.ultra.megamod.feature.casino.network.RouletteGameSyncPayload.lastSync = null;
        com.ultra.megamod.feature.casino.network.CrapsGameSyncPayload.lastSync = null;
        com.ultra.megamod.feature.casino.network.BaccaratGameSyncPayload.lastSync = null;
        com.ultra.megamod.feature.casino.network.WheelSyncPayload.lastSync = null;
        com.ultra.megamod.feature.casino.network.OpenWheelPayload.lastPayload = null;
        com.ultra.megamod.feature.casino.network.OpenBlackjackPayload.lastPayload = null;
    }

    /**
     * Set the active game type. Clears syncs for all OTHER games to prevent
     * cross-contamination (e.g. roulette sync arriving while at wheel).
     */
    public static void setActiveGame(String type) {
        activeGameType = type;
        if (!"roulette".equals(type)) com.ultra.megamod.feature.casino.network.RouletteGameSyncPayload.lastSync = null;
        if (!"craps".equals(type)) com.ultra.megamod.feature.casino.network.CrapsGameSyncPayload.lastSync = null;
        if (!"baccarat".equals(type)) com.ultra.megamod.feature.casino.network.BaccaratGameSyncPayload.lastSync = null;
        if (!"wheel".equals(type)) com.ultra.megamod.feature.casino.network.WheelSyncPayload.lastSync = null;
    }

    /** Called from RouletteScreen/CrapsScreen/BaccaratScreen/WheelScreen.onClose() */
    public static void onRouletteDismissed() { dismissedRoulette = true; }
    public static void onCrapsDismissed() { dismissedCraps = true; }
    public static void onBaccaratDismissed() { dismissedBaccarat = true; }
    public static void onWheelDismissed() { dismissedWheel = true; }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        CASINO_MENU_KEY = new KeyMapping("key.megamod.casino_menu", 70, MEGAMOD_CAT);
        event.register(CASINO_MENU_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        // Clear casino state when player leaves the casino dimension
        if (player != null && !com.ultra.megamod.feature.dimensions.network.DimensionSyncPayload.clientDimensionId.contains("casino")) {
            if (dismissedRoulette || dismissedCraps || dismissedBaccarat
                    || com.ultra.megamod.feature.casino.network.RouletteGameSyncPayload.lastSync != null
                    || com.ultra.megamod.feature.casino.network.CrapsGameSyncPayload.lastSync != null
                    || com.ultra.megamod.feature.casino.network.BaccaratGameSyncPayload.lastSync != null) {
                clearCasinoState();
            }
        }

        // On dismount: clear ALL state so stale syncs don't re-open wrong screens
        if (player != null) {
            boolean isPassenger = player.isPassenger();
            if (wasPassenger && !isPassenger) {
                clearCasinoState();
            }
            // On fresh sit-down: clear stale syncs (e.g. from roulette broadcasts
            // while walking around casino) then request the correct game's sync.
            // Preserve OpenWheelPayload — it races with the client detecting the
            // sit-down because the server sends it during onPlayerSat (same tick as
            // startRiding). Clearing it here would lose it permanently since the
            // server won't resend.
            if (!wasPassenger && isPassenger && mc.screen == null) {
                var savedWheelPayload = com.ultra.megamod.feature.casino.network.OpenWheelPayload.lastPayload;
                clearCasinoState();
                autoRequestGameSync(player);
                if (savedWheelPayload != null) {
                    com.ultra.megamod.feature.casino.network.OpenWheelPayload.lastPayload = savedWheelPayload;
                }
            }
            wasPassenger = isPassenger;
        }

        // Auto-open game screens when sync arrives — ONLY while sitting (isPassenger)
        // Respects activeGameType to prevent cross-game interference
        if (player != null && player.isPassenger() && mc.screen == null) {
            // Wheel: auto-open when OpenWheelPayload arrives from WheelChairBlock.
            // Check regardless of activeGameType because the WheelChairBlock searches
            // 12 blocks (larger than autoRequestGameSync's 4-block scan), so
            // activeGameType may not have been set to "wheel" by the scan.
            if (!dismissedWheel
                    && com.ultra.megamod.feature.casino.network.OpenWheelPayload.lastPayload != null) {
                setActiveGame("wheel");
                storedWheelPos = com.ultra.megamod.feature.casino.network.OpenWheelPayload.lastPayload.pos();
                com.ultra.megamod.feature.casino.network.OpenWheelPayload.lastPayload = null;
                mc.setScreen(new WheelScreen(storedWheelPos));
            }
            if ((activeGameType == null || "roulette".equals(activeGameType))
                    && !dismissedRoulette
                    && com.ultra.megamod.feature.casino.network.RouletteGameSyncPayload.lastSync != null) {
                if (activeGameType == null) setActiveGame("roulette");
                mc.setScreen(new com.ultra.megamod.feature.casino.screen.RouletteScreen(
                        storedRoulettePos != null ? storedRoulettePos : player.blockPosition()));
            }
            if ((activeGameType == null || "craps".equals(activeGameType))
                    && !dismissedCraps
                    && com.ultra.megamod.feature.casino.network.CrapsGameSyncPayload.lastSync != null) {
                if (activeGameType == null) setActiveGame("craps");
                mc.setScreen(new com.ultra.megamod.feature.casino.screen.CrapsScreen(
                        storedCrapsPos != null ? storedCrapsPos : player.blockPosition()));
            }
            if ((activeGameType == null || "baccarat".equals(activeGameType))
                    && !dismissedBaccarat
                    && com.ultra.megamod.feature.casino.network.BaccaratGameSyncPayload.lastSync != null) {
                if (activeGameType == null) setActiveGame("baccarat");
                mc.setScreen(new com.ultra.megamod.feature.casino.screen.BaccaratScreen(
                        storedBaccaratPos != null ? storedBaccaratPos : player.blockPosition()));
            }
            // Blackjack: auto-open when OpenBlackjackPayload arrives from BlackjackChairBlock
            if ((activeGameType == null || "blackjack".equals(activeGameType))
                    && com.ultra.megamod.feature.casino.network.OpenBlackjackPayload.lastPayload != null) {
                setActiveGame("blackjack");
                storedBlackjackPos = com.ultra.megamod.feature.casino.network.OpenBlackjackPayload.lastPayload.tablePos();
                com.ultra.megamod.feature.casino.network.OpenBlackjackPayload.lastPayload = null;
                mc.setScreen(new com.ultra.megamod.feature.casino.screen.BlackjackScreen(storedBlackjackPos));
            }
        }

        if (player == null || !player.isPassenger() || mc.screen != null) {
            hintTimer = 0;
            return;
        }

        // Check if near a blackjack table, wheel, or other game tables (4-block scan)
        BlockPos pos = player.blockPosition();
        BlockPos tablePos = null;
        BlockPos wheelPos = null;
        BlockPos roulettePos = null;
        BlockPos crapsPos = null;
        BlockPos baccaratPos = null;

        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos check = pos.offset(dx, dy, dz);
                    Block block = player.level().getBlockState(check).getBlock();
                    if (block instanceof com.ultra.megamod.feature.casino.blackjack.BlackjackTableBlock && tablePos == null) {
                        tablePos = check;
                    }
                    if (block instanceof com.ultra.megamod.feature.casino.wheel.WheelBlock && wheelPos == null) {
                        wheelPos = check;
                    }
                    if (block == com.ultra.megamod.feature.furniture.FurnitureRegistry.CASINO_TABLE_ROULETTE.get() && roulettePos == null) {
                        roulettePos = check;
                    }
                    if (block == com.ultra.megamod.feature.furniture.FurnitureRegistry.CASINO_TABLE_CRAPS.get() && crapsPos == null) {
                        crapsPos = check;
                    }
                    if (block == com.ultra.megamod.feature.furniture.FurnitureRegistry.CASINO_TABLE_BLANK.get() && baccaratPos == null) {
                        baccaratPos = check;
                    }
                }
            }
        }

        // Use storedWheelPos if the 4-block scan couldn't find the wheel
        // (the wheel is often further away in the casino layout)
        if (wheelPos == null && storedWheelPos != null) {
            wheelPos = storedWheelPos;
        }

        if (tablePos == null && wheelPos == null && roulettePos == null && crapsPos == null && baccaratPos == null) return;

        // Show hint on action bar
        hintTimer++;
        if (hintTimer % 40 == 1) {
            StringBuilder games = new StringBuilder();
            if (tablePos != null) games.append("Blackjack");
            if (roulettePos != null) { if (games.length() > 0) games.append(", "); games.append("Roulette"); }
            if (crapsPos != null) { if (games.length() > 0) games.append(", "); games.append("Craps"); }
            if (baccaratPos != null) { if (games.length() > 0) games.append(", "); games.append("Baccarat"); }
            if (wheelPos != null) { if (games.length() > 0) games.append(", "); games.append("Wheel"); }
            player.displayClientMessage(
                    Component.literal("Press [F] to open " + games + " menu")
                            .withStyle(net.minecraft.ChatFormatting.GOLD), true);
        }

        // Handle F key press — reopen the active game using stored positions,
        // or fall back to scanning nearby blocks if no active game is set.
        if (CASINO_MENU_KEY != null && CASINO_MENU_KEY.consumeClick()) {
            // Use stored positions (set during autoRequestGameSync) with scanned positions as fallback
            BlockPos bjPos = storedBlackjackPos != null ? storedBlackjackPos : tablePos;
            BlockPos wPos = storedWheelPos != null ? storedWheelPos : wheelPos;
            BlockPos rPos = storedRoulettePos != null ? storedRoulettePos : roulettePos;
            BlockPos crPos = storedCrapsPos != null ? storedCrapsPos : crapsPos;
            BlockPos baPos = storedBaccaratPos != null ? storedBaccaratPos : baccaratPos;

            if (activeGameType != null) {
                switch (activeGameType) {
                    case "blackjack" -> {
                        if (bjPos != null) ClientPacketDistributor.sendToServer(
                                new BlackjackActionPayload(bjPos, "REOPEN", ""));
                    }
                    case "wheel" -> {
                        if (wPos != null) { dismissedWheel = false; mc.setScreen(new WheelScreen(wPos)); }
                    }
                    case "roulette" -> {
                        if (rPos != null) { dismissedRoulette = false; ClientPacketDistributor.sendToServer(
                                new com.ultra.megamod.feature.casino.network.RouletteActionPayload("", 0, rPos)); }
                    }
                    case "craps" -> {
                        if (crPos != null) { dismissedCraps = false; ClientPacketDistributor.sendToServer(
                                new com.ultra.megamod.feature.casino.network.CrapsActionPayload("sync", 0, crPos)); }
                    }
                    case "baccarat" -> {
                        if (baPos != null) { dismissedBaccarat = false; ClientPacketDistributor.sendToServer(
                                new com.ultra.megamod.feature.casino.network.BaccaratActionPayload("sync", "", 0, baPos)); }
                    }
                }
            } else {
                // No active game — fallback scan (first sit-down before server responds)
                if (bjPos != null) {
                    setActiveGame("blackjack");
                    storedBlackjackPos = bjPos;
                    ClientPacketDistributor.sendToServer(new BlackjackActionPayload(bjPos, "REOPEN", ""));
                } else if (wPos != null) {
                    setActiveGame("wheel");
                    storedWheelPos = wPos;
                    mc.setScreen(new WheelScreen(wPos));
                } else if (rPos != null) {
                    setActiveGame("roulette");
                    storedRoulettePos = rPos;
                    dismissedRoulette = false;
                    ClientPacketDistributor.sendToServer(
                            new com.ultra.megamod.feature.casino.network.RouletteActionPayload("", 0, rPos));
                } else if (crPos != null) {
                    setActiveGame("craps");
                    storedCrapsPos = crPos;
                    dismissedCraps = false;
                    ClientPacketDistributor.sendToServer(
                            new com.ultra.megamod.feature.casino.network.CrapsActionPayload("sync", 0, crPos));
                } else if (baPos != null) {
                    setActiveGame("baccarat");
                    storedBaccaratPos = baPos;
                    dismissedBaccarat = false;
                    ClientPacketDistributor.sendToServer(
                            new com.ultra.megamod.feature.casino.network.BaccaratActionPayload("sync", "", 0, baPos));
                }
            }
        }
    }

    /**
     * When the player sits in any chair (including furniture chairs), scan nearby
     * for game tables and request a sync from the server so the GUI auto-opens.
     * Sets activeGameType and stores the table position to prevent cross-game
     * interference (e.g. roulette broadcast opening while at baccarat table).
     */
    private static void autoRequestGameSync(LocalPlayer player) {
        BlockPos pos = player.blockPosition();
        BlockPos foundRoulette = null, foundCraps = null, foundBaccarat = null;

        // Check the chair block type directly — dedicated chair blocks know their
        // game type. The wheel is often further than 4 blocks away, so the scan
        // below won't find it. Detect the chair type and set the game accordingly.
        Block seatBlock = player.level().getBlockState(pos).getBlock();
        if (seatBlock instanceof com.ultra.megamod.feature.casino.WheelChairBlock) {
            setActiveGame("wheel");
            return;
        }
        if (seatBlock instanceof com.ultra.megamod.feature.casino.BlackjackChairBlock) {
            setActiveGame("blackjack");
            return;
        }

        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos check = pos.offset(dx, dy, dz);
                    Block block = player.level().getBlockState(check).getBlock();

                    // If near a wheel or blackjack table, their dedicated chairs handle
                    // the screen opening. Set activeGameType so roulette broadcasts
                    // don't interfere, but don't send table game sync requests.
                    if (block instanceof com.ultra.megamod.feature.casino.wheel.WheelBlock) {
                        setActiveGame("wheel");
                        storedWheelPos = check;
                        return;
                    }
                    if (block instanceof com.ultra.megamod.feature.casino.blackjack.BlackjackTableBlock) {
                        setActiveGame("blackjack");
                        storedBlackjackPos = check;
                        return;
                    }

                    if (block == com.ultra.megamod.feature.furniture.FurnitureRegistry.CASINO_TABLE_ROULETTE.get() && foundRoulette == null) {
                        foundRoulette = check;
                    }
                    if (block == com.ultra.megamod.feature.furniture.FurnitureRegistry.CASINO_TABLE_CRAPS.get() && foundCraps == null) {
                        foundCraps = check;
                    }
                    if (block == com.ultra.megamod.feature.furniture.FurnitureRegistry.CASINO_TABLE_BLANK.get() && foundBaccarat == null) {
                        foundBaccarat = check;
                    }
                }
            }
        }

        // Send sync for the first table game found. Set activeGameType and store
        // the table position so auto-open creates the correct screen.
        if (foundRoulette != null) {
            setActiveGame("roulette");
            storedRoulettePos = foundRoulette;
            ClientPacketDistributor.sendToServer(
                    new com.ultra.megamod.feature.casino.network.RouletteActionPayload("", 0, foundRoulette));
        } else if (foundCraps != null) {
            setActiveGame("craps");
            storedCrapsPos = foundCraps;
            ClientPacketDistributor.sendToServer(
                    new com.ultra.megamod.feature.casino.network.CrapsActionPayload("sync", 0, foundCraps));
        } else if (foundBaccarat != null) {
            setActiveGame("baccarat");
            storedBaccaratPos = foundBaccarat;
            ClientPacketDistributor.sendToServer(
                    new com.ultra.megamod.feature.casino.network.BaccaratActionPayload("sync", "", 0, foundBaccarat));
        }
    }
}
