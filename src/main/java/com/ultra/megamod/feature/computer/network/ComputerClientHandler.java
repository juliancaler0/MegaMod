package com.ultra.megamod.feature.computer.network;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.casino.CasinoClientEvents;
import com.ultra.megamod.feature.casino.chips.ChipRenderer;
import com.ultra.megamod.feature.casino.chips.ChipSyncPayload;
import com.ultra.megamod.feature.casino.chips.CashierScreen;
import com.ultra.megamod.feature.casino.network.OpenBlackjackPayload;
import com.ultra.megamod.feature.casino.network.OpenSlotMachinePayload;
import com.ultra.megamod.feature.casino.network.OpenWheelPayload;
import com.ultra.megamod.feature.casino.screen.BlackjackScreen;
import com.ultra.megamod.feature.casino.screen.SlotMachineScreen;
import com.ultra.megamod.feature.casino.screen.WheelScreen;
import com.ultra.megamod.feature.computer.screen.ComputerScreen;
import com.ultra.megamod.feature.dungeons.network.BossMusicPayload;
import com.ultra.megamod.feature.economy.network.OpenAtmPayload;
import com.ultra.megamod.feature.economy.screen.BankScreen;
import com.ultra.megamod.feature.marketplace.screen.TradingTerminalScreen;
import com.ultra.megamod.feature.museum.network.OpenMuseumPayload;
import com.ultra.megamod.feature.museum.screen.CuratorScreen;
import com.ultra.megamod.feature.relics.network.OpenRelicScreenPayload;
import com.ultra.megamod.feature.relics.research.RerollPayload;
import com.ultra.megamod.feature.relics.research.RerollScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Client-only tick handler that processes ALL deferred payloads.
 * Payload handleOnClient methods store data in static fields (server-safe),
 * and this handler picks them up each tick to open screens / play sounds / etc.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class ComputerClientHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // --- Computer ---
        OpenComputerPayload openComputer = OpenComputerPayload.lastPayload;
        if (openComputer != null) {
            OpenComputerPayload.lastPayload = null;
            mc.setScreen(new ComputerScreen(openComputer.isAdmin(), openComputer.wallet(), openComputer.bank()));
        }

        ComputerDataPayload dataPayload = ComputerDataPayload.lastResponse;
        if (dataPayload != null) {
            String type = dataPayload.dataType();
            if (("terminal_state".equals(type) || "terminal_open".equals(type))
                    && !(mc.screen instanceof TradingTerminalScreen)) {
                mc.setScreen(new TradingTerminalScreen());
            }
        }

        // --- Economy (ATM) ---
        OpenAtmPayload openAtm = OpenAtmPayload.lastPayload;
        if (openAtm != null) {
            OpenAtmPayload.lastPayload = null;
            mc.setScreen(new BankScreen(null, openAtm.wallet(), openAtm.bank()));
        }

        // --- Casino: Slot Machine ---
        OpenSlotMachinePayload openSlot = OpenSlotMachinePayload.lastPayload;
        if (openSlot != null) {
            OpenSlotMachinePayload.lastPayload = null;
            mc.setScreen(new SlotMachineScreen(openSlot.pos(), openSlot.betIndex(), openSlot.lineMode(), openSlot.wallet()));
        }

        // --- Casino: Wheel & Blackjack ---
        // When seated (isPassenger), CasinoClientEvents handles these payloads
        // with proper game-type guards to prevent cross-game interference.
        // Only handle here for direct block interaction (right-clicking wheel/table).
        if (!mc.player.isPassenger()) {
            OpenWheelPayload openWheel = OpenWheelPayload.lastPayload;
            if (openWheel != null) {
                OpenWheelPayload.lastPayload = null;
                CasinoClientEvents.setActiveGame("wheel");
                mc.setScreen(new WheelScreen(openWheel.pos()));
            }

            OpenBlackjackPayload openBj = OpenBlackjackPayload.lastPayload;
            if (openBj != null) {
                OpenBlackjackPayload.lastPayload = null;
                mc.setScreen(new BlackjackScreen(openBj.tablePos()));
            }
        }

        // --- Casino: Cashier ---
        if (ChipSyncPayload.shouldOpenCashier) {
            ChipSyncPayload.shouldOpenCashier = false;
            if (mc.screen == null) {
                mc.setScreen(new CashierScreen());
            }
        }

        // --- Museum ---
        OpenMuseumPayload openMuseum = OpenMuseumPayload.lastPayload;
        if (openMuseum != null) {
            OpenMuseumPayload.lastPayload = null;
            mc.setScreen(new CuratorScreen(openMuseum.museumJson()));
        }

        // --- Relics ---
        OpenRelicScreenPayload openRelic = OpenRelicScreenPayload.lastPayload;
        if (openRelic != null) {
            OpenRelicScreenPayload.lastPayload = null;
            com.ultra.megamod.feature.relics.client.RelicScreenOpener.openFromPayload(openRelic);
        }

        // --- Research Reroll ---
        RerollPayload.OpenRerollPayload openReroll = RerollPayload.OpenRerollPayload.lastPayload;
        if (openReroll != null) {
            RerollPayload.OpenRerollPayload.lastPayload = null;
            mc.setScreen(new RerollScreen());
        }

        // --- Boss Music ---
        BossMusicPayload bossMusic = BossMusicPayload.lastPayload;
        if (bossMusic != null) {
            BossMusicPayload.lastPayload = null;
            com.ultra.megamod.feature.dungeons.client.BossMusicClientHandler.handle(bossMusic);
        }
    }
}
