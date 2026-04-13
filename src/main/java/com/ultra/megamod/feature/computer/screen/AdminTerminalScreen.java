/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.neoforge.client.network.ClientPacketDistributor
 */
package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.MegaMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.ComputerRegistry;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.dungeons.DungeonRegistry;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.loot.DungeonExclusiveItems;
import com.ultra.megamod.feature.museum.MuseumRegistry;
import com.ultra.megamod.feature.relics.RelicRegistry;

import com.ultra.megamod.feature.computer.screen.panels.MuseumManagerPanel;
import com.ultra.megamod.feature.computer.screen.panels.InventoryViewerPanel;
import com.ultra.megamod.feature.computer.screen.panels.DungeonAnalyticsPanel;
import com.ultra.megamod.feature.computer.screen.panels.FeatureTogglesPanel;
import com.ultra.megamod.feature.computer.screen.panels.ModerationPanel;
import com.ultra.megamod.feature.computer.screen.panels.StructureLocatorPanel;
import com.ultra.megamod.feature.computer.screen.panels.SchedulerPanel;
import com.ultra.megamod.feature.computer.screen.panels.BotControlPanel;
import com.ultra.megamod.feature.computer.screen.panels.MobShowcasePanel;
import com.ultra.megamod.feature.computer.screen.panels.AdminModulesPanel;
import com.ultra.megamod.feature.computer.screen.panels.EconomyDashboardPanel;
import com.ultra.megamod.feature.computer.screen.panels.MarketplaceAdminPanel;
import com.ultra.megamod.feature.computer.screen.panels.AlchemyAdminPanel;
import com.ultra.megamod.feature.computer.screen.panels.SystemHealthPanel;
import com.ultra.megamod.feature.computer.screen.panels.CorruptionAdminPanel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class AdminTerminalScreen
extends Screen {
    private final Screen parent;
    private final Minecraft mc = Minecraft.getInstance();
    private static final String[] TAB_NAMES = new String[]{"Dashboard", "Players", "World", "Items", "MegaMod", "Economy", "Skills", "Audit", "Item Editor", "Entities", "Terminal", "Museum Mgr", "Inv Viewer", "Dungeons", "Toggles", "Moderation", "Structures", "Scheduler", "Bot Control", "Showcase", "Modules", "Warps", "Citizens", "Casino", "Corruption", "Marketplace", "Alchemy", "System", "Deaths", "Cleanup", "Loot Tables", "Aliases", "Undo", "Furniture", "Combat", "World Edit", "\u2315 Search"};
    private int currentTab = 0;
    private int tabScroll = 0; // vertical scroll offset for sidebar tabs (in pixels)
    private static final String WELCOME_TEXT = "Welcome back, NeverNotch...";
    // Session-scoped: welcome animation plays once per Minecraft launch, then
    // is skipped on subsequent admin-screen opens. Resets on game restart.
    private static boolean welcomeShownThisSession = false;
    private int animationTicks = welcomeShownThisSession ? WELCOME_TEXT.length() * 2 : 0;
    private boolean animationComplete = welcomeShownThisSession;
    private static final int SIDEBAR_WIDTH = 76;
    private static final int HEADER_HEIGHT = 22;
    private static final int STATUS_BAR_HEIGHT = 16;
    private static final int CONTENT_PAD = 6;
    private int contentTop;
    private int contentBottom;
    private int contentLeft;
    private int contentRight;
    private int[][] tabBounds;
    private final List<String> terminalOutput = new ArrayList<String>();
    private final List<String> commandHistory = new ArrayList<String>();
    private int historyIndex = -1;
    private EditBox commandInput;
    private int terminalScroll = 0;
    private int playerScroll = 0;
    // Player tracker data: name -> [x, y, z, dim, health, maxHealth]
    private java.util.Map<String, String[]> trackerData = null;
    private int trackerTick = 0;
    private static final int PLAYER_ROW_HEIGHT = 26;
    private int worldScroll = 0;
    private EditBox itemSearchBox;
    private int itemScroll = 0;
    private String currentItemCategory = "All";
    private static final String[] ITEM_CATEGORIES = new String[]{"All", "Minecraft", "MegaMod", "Tools", "Armor", "Food", "Blocks", "Redstone", "Combat", "Brewing"};
    private static final Map<String, Item[]> CATEGORY_ITEMS = new LinkedHashMap<String, Item[]>();
    private int megamodScroll = 0;
    private String currentMegamodCategory = "Relics";
    private int ecoScroll = 0;
    private EditBox ecoAmountBox;
    private final List<EcoPlayerEntry> ecoPlayers = new ArrayList<EcoPlayerEntry>();
    private int ecoTotalWallets = 0;
    private int ecoTotalBanks = 0;
    private int ecoPlayerCount = 0;
    private boolean ecoSortByRichest = false;
    private String ecoSubView = "money";
    private int shopIntervalTicks = 24000;
    private double shopPriceMult = 1.0;
    private double shopSellPct = 0.20;
    private String selectedSkillTree = "COMBAT";
    private String skillSubView = "skills";
    private List<String[]> gameScoresCache = new ArrayList<String[]>();
    private int skillScroll = 0;
    private final List<SkillPlayerEntry> skillPlayers = new ArrayList<SkillPlayerEntry>();
    private double cachedAdminXpMult = 1.0;
    private double cachedAdminOnlyXpBoost = 1.0;
    private final List<CosmeticPlayerEntry> cosmeticPlayers = new ArrayList<CosmeticPlayerEntry>();
    private String cosmeticSection = "badges"; // "badges", "parties", "bounties"
    private boolean godModeActive = false;
    private final List<PartyViewEntry> partyViewEntries = new ArrayList<PartyViewEntry>();
    private final List<BountyViewEntry> bountyViewEntries = new ArrayList<BountyViewEntry>();
    private String selectedCosmeticPrestigeUUID = null;
    private static final String[] SKILL_TREES = new String[]{"COMBAT", "MINING", "FARMING", "ARCANE", "SURVIVAL"};
    private static final String[] SKILL_TREE_NAMES = new String[]{"Combat", "Mining", "Farming", "Arcane", "Survival"};
    private String selectedPlayerUUID = null;
    private String selectedPlayerName = null;
    private int playerDetailScroll = 0;
    private List<String> playerDetailLines = new ArrayList<String>();
    private List<String> playerInvLines = new ArrayList<String>();
    private EditBox broadcastBox;
    private EditBox warpNameBox;
    private int warpScroll = 0;
    private List<String[]> warpsCache = new ArrayList<String[]>();
    private List<String[]> dungeonsCache = new ArrayList<String[]>();
    private List<String[]> questsCache = new ArrayList<String[]>();
    private List<String> questsCompleted = new ArrayList<String>();
    private String questTargetUUID = null;
    private String questTargetName = "";
    private int dungeonScroll = 0;
    private int questScroll = 0;
    private List<String[]> auditCache = new ArrayList<String[]>();
    private List<String[]> playerAuditCache = new ArrayList<String[]>();
    private String auditSubView = "economy";
    private int auditScroll = 0;
    private EditBox auditSearchBox;
    private String auditTypeFilter = "ALL";
    private String auditTimeFilter = "ALL";
    private List<String> adminMessages = new ArrayList<String>();
    private double perfTps = 0, perfMspt = 0;
    private long perfMem = 0, perfMaxMem = 0, perfTotalMem = 0;
    private int perfEntities = 0, perfChunks = 0, perfPlayers = 0;
    private int perfActiveArenas = 0, perfActiveBounties = 0, perfActiveDungeons = 0;
    // TPS history for graph
    private final List<double[]> tpsHistory = new ArrayList<>(); // [tps, memPercent]
    private final List<String[]> perfIssues = new ArrayList<>(); // [level, msg]
    private int perfRefreshCounter = 0;
    private List<EntityEntry> entityList = new ArrayList<>();
    private int entityScroll = 0;
    private int researchScroll = 0;
    private List<ResearchInvEntry> researchInventory = new ArrayList<ResearchInvEntry>();
    private List<String[]> cachedEnchantList = null;
    private EditBox itemEditorInput;
    private int researchSelectedSlot = -1;
    private List<String> suggestions = new ArrayList<String>();
    private int selectedSuggestion = -1;
    private String lastSuggestionQuery = "";
    private long lastSuggestionTime = 0;
    private String terminalMode = "terminal";
    private final List<String> serverLogLines = new ArrayList<String>();
    private int logScroll = 0;
    private int logRefreshTicks = 0;
    private static final java.util.LinkedHashMap<String, String[]> COMMAND_PRESETS = new java.util.LinkedHashMap<String, String[]>();
    private static final java.util.LinkedHashMap<String, String> PRESET_DESCRIPTIONS = new java.util.LinkedHashMap<String, String>();
    static {
        COMMAND_PRESETS.put("Arena Setup", new String[]{"time set day", "weather clear", "difficulty hard", "gamemode survival @s", "effect clear @s"});
        COMMAND_PRESETS.put("Creative Build", new String[]{"gamemode creative @s", "time set noon", "weather clear", "gamerule doDaylightCycle false"});
        COMMAND_PRESETS.put("Testing Mode", new String[]{"gamemode creative @s", "effect give @s minecraft:resistance 999999 255 true", "effect give @s minecraft:saturation 999999 255 true"});
        COMMAND_PRESETS.put("Reset World", new String[]{"time set day", "weather clear", "difficulty normal", "gamerule doDaylightCycle true", "gamerule doWeatherCycle true", "kill @e[type=!player]"});
        COMMAND_PRESETS.put("Dungeon Test", new String[]{"gamemode survival @s", "effect clear @s", "effect give @s minecraft:instant_health 1 255", "effect give @s minecraft:saturation 1 255"});
        PRESET_DESCRIPTIONS.put("Arena Setup", "Sets day, clears weather, hard difficulty, survival mode");
        PRESET_DESCRIPTIONS.put("Creative Build", "Creative mode, noon, no weather/daylight cycle");
        PRESET_DESCRIPTIONS.put("Testing Mode", "Creative + invincibility + infinite food");
        PRESET_DESCRIPTIONS.put("Reset World", "Resets time, weather, difficulty, kills entities");
        PRESET_DESCRIPTIONS.put("Dungeon Test", "Survival + full heal + full food for testing dungeons");
    }
    private static final String[] MEGAMOD_CATEGORIES;
    private static final Map<String, List<MegamodEntry>> MEGAMOD_ITEMS;
    private static final String[] GAMERULES;
    private final boolean[] gameruleStates = new boolean[]{false, true, true, true, true, true, true, true, false, true, true, true, true, true, false, true, false};
    private final List<ActionRect> actionRects = new ArrayList<ActionRect>();
    private final List<GameruleRect> gameruleRects = new ArrayList<GameruleRect>();
    // Combat tab interactive widgets — rebuilt each render in renderCombatConfig
    private final List<CombatSlider> combatSliders = new ArrayList<CombatSlider>();
    private final List<CombatToggle> combatToggles = new ArrayList<CombatToggle>();
    private record CombatSlider(String key, int x, int y, int w, int h, double min, double max) {}
    private record CombatToggle(String key, int x, int y, int w, int h, boolean current) {}
    private int titleBarH;
    private int backBtnW;
    private int backBtnH;
    private int backBtnX;
    private int backBtnY;
    // New panel instances
    private MuseumManagerPanel museumPanel;
    private InventoryViewerPanel inventoryPanel;
    private DungeonAnalyticsPanel dungeonAnalyticsPanel;
    private FeatureTogglesPanel featureTogglesPanel;
    private ModerationPanel moderationPanel;
    private StructureLocatorPanel structureLocatorPanel;
    private SchedulerPanel schedulerPanel;
    private BotControlPanel botControlPanel;
    private MobShowcasePanel mobShowcasePanel;
    private AdminModulesPanel adminModulesPanel;
    private com.ultra.megamod.feature.computer.screen.panels.WarpPanel warpPanel;
    // TODO: AdminCitizensPanel removed during colony system transition - needs port to new system
    // private com.ultra.megamod.feature.citizen.screen.panels.AdminCitizensPanel citizensPanel;
    private com.ultra.megamod.feature.casino.screen.panels.CasinoManagerPanel casinoPanel;
    private EconomyDashboardPanel economyDashboardPanel;
    private MarketplaceAdminPanel marketplaceAdminPanel;
    private AlchemyAdminPanel alchemyAdminPanel;
    private SystemHealthPanel systemHealthPanel;
    private com.ultra.megamod.feature.computer.screen.panels.AdminSearchPanel adminSearchPanel;
    private com.ultra.megamod.feature.computer.screen.panels.WorldEditPanel worldEditPanel;

    // Corruption panel
    private CorruptionAdminPanel corruptionPanel;
    private String corruptionJsonCache = "";
    private int corruptionScrollY = 0;

    public AdminTerminalScreen(Screen parent) {
        super((Component)Component.literal((String)"Admin Panel"));
        this.parent = parent;
    }

    protected void init() {
        super.init();
        this.contentLeft = SIDEBAR_WIDTH + 2;
        this.contentRight = this.width - 2;
        this.contentTop = HEADER_HEIGHT + 2;
        this.contentBottom = this.height - STATUS_BAR_HEIGHT - 2;
        int tabH = 17;
        int tabGap = 1;
        int tabStartY = HEADER_HEIGHT + 4;
        this.tabBounds = new int[TAB_NAMES.length][4];
        for (int i = 0; i < TAB_NAMES.length; ++i) {
            // Search tab (34) at the top, everything else shifts down by one slot
            int displayPos = (i == TAB_NAMES.length - 1) ? 0 : i + 1;
            this.tabBounds[i] = new int[]{2, tabStartY + displayPos * (tabH + tabGap), SIDEBAR_WIDTH - 4, tabH};
        }
        int inputY = this.contentBottom - 22;
        int runWidth = 50;
        int inputWidth = this.contentRight - this.contentLeft - runWidth - 6;
        this.commandInput = new EditBox(this.font, this.contentLeft, inputY, Math.max(60, inputWidth), 20, (Component)Component.literal((String)"Command"));
        this.commandInput.setMaxLength(256);
        this.commandInput.setTextColor(0xFFE6EDF3);
        this.commandInput.visible = false;
        this.addRenderableWidget(this.commandInput);
        this.itemSearchBox = new EditBox(this.font, this.contentLeft + 2, this.contentTop + 2, 160, 16, (Component)Component.literal((String)"Search"));
        this.itemSearchBox.setMaxLength(64);
        this.itemSearchBox.setTextColor(-1);
        this.itemSearchBox.visible = false;
        this.addRenderableWidget(this.itemSearchBox);
        int ecoAmountY = this.contentTop + 6;
        this.ecoAmountBox = new EditBox(this.font, this.contentLeft + 6 + 60 * 2 + 2 + 20 + 50, ecoAmountY, 80, 16, (Component)Component.literal((String)"Amount"));
        this.ecoAmountBox.setMaxLength(10);
        this.ecoAmountBox.setTextColor(-1);
        this.ecoAmountBox.setValue("100");
        this.ecoAmountBox.visible = false;
        this.addRenderableWidget(this.ecoAmountBox);
        this.broadcastBox = new EditBox(this.font, this.contentLeft + 6, this.contentBottom - 42, 200, 16, (Component)Component.literal((String)"Broadcast"));
        this.broadcastBox.setMaxLength(256);
        this.broadcastBox.setTextColor(-1);
        this.broadcastBox.visible = false;
        this.lootInput = new EditBox(this.font, this.contentLeft + 6, this.contentBottom - 42, 300, 16, (Component)Component.literal("Loot"));
        this.lootInput.setMaxLength(256);
        this.lootInput.setTextColor(-1);
        this.lootInput.visible = false;
        this.addRenderableWidget(this.lootInput);
        this.aliasInput = new EditBox(this.font, this.contentLeft + 6, this.contentBottom - 42, 300, 16, (Component)Component.literal("Alias"));
        this.aliasInput.setMaxLength(256);
        this.aliasInput.setTextColor(-1);
        this.aliasInput.visible = false;
        this.addRenderableWidget(this.aliasInput);
        this.addRenderableWidget(this.broadcastBox);
        this.warpNameBox = new EditBox(this.font, this.contentLeft + 6, this.contentBottom - 42, 120, 16, (Component)Component.literal((String)"Warp Name"));
        this.warpNameBox.setMaxLength(32);
        this.warpNameBox.setTextColor(-1);
        this.warpNameBox.visible = false;
        this.addRenderableWidget(this.warpNameBox);
        this.auditSearchBox = new EditBox(this.font, this.contentLeft + 6, this.contentTop + 28, 120, 14, (Component)Component.literal((String)"Search player"));
        this.auditSearchBox.setMaxLength(32);
        this.auditSearchBox.setTextColor(-1);
        this.auditSearchBox.visible = false;
        this.addRenderableWidget(this.auditSearchBox);
        this.itemEditorInput = new EditBox(this.font, this.contentLeft + 6, this.contentTop + 4, 180, 14, (Component)Component.literal((String)"Name/Lore"));
        this.itemEditorInput.setMaxLength(128);
        this.itemEditorInput.setTextColor(-1);
        this.itemEditorInput.visible = false;
        this.addRenderableWidget(this.itemEditorInput);
        if (this.animationComplete) {
            this.applyTabVisibility();
        }
        // Initialize panels
        this.museumPanel = new MuseumManagerPanel(this.font);
        this.inventoryPanel = new InventoryViewerPanel(this.font);
        this.dungeonAnalyticsPanel = new DungeonAnalyticsPanel(this.font);
        this.featureTogglesPanel = new FeatureTogglesPanel(this.font);
        this.moderationPanel = new ModerationPanel(this.font);
        this.structureLocatorPanel = new StructureLocatorPanel(this.font);
        this.schedulerPanel = new SchedulerPanel(this.font);
        this.botControlPanel = new BotControlPanel(this.font);
        this.mobShowcasePanel = new MobShowcasePanel(this.font);
        this.adminModulesPanel = new AdminModulesPanel(this.font);
        this.warpPanel = new com.ultra.megamod.feature.computer.screen.panels.WarpPanel(this.font);
        // TODO: citizensPanel removed during colony system transition
        // this.citizensPanel = new com.ultra.megamod.feature.citizen.screen.panels.AdminCitizensPanel(this.font);
        // this.citizensPanel.init(this.contentLeft, this.contentTop, this.contentRight - this.contentLeft, this.contentBottom - this.contentTop);
        this.casinoPanel = new com.ultra.megamod.feature.casino.screen.panels.CasinoManagerPanel(this.font);
        this.corruptionPanel = new CorruptionAdminPanel(this.font);
        this.economyDashboardPanel = new EconomyDashboardPanel(this.font);
        this.marketplaceAdminPanel = new MarketplaceAdminPanel(this.font);
        this.alchemyAdminPanel = new AlchemyAdminPanel(this.font);
        this.systemHealthPanel = new SystemHealthPanel(this.font);
        this.adminSearchPanel = new com.ultra.megamod.feature.computer.screen.panels.AdminSearchPanel();
        this.worldEditPanel = new com.ultra.megamod.feature.computer.screen.panels.WorldEditPanel(this.font);
    }

    private void switchTab(int tab) {
        this.currentTab = tab;
        this.playerScroll = 0;
        this.itemScroll = 0;
        this.terminalScroll = 0;
        this.worldScroll = 0;
        this.megamodScroll = 0;
        this.ecoScroll = 0;
        this.skillScroll = 0;
        this.playerDetailScroll = 0;
        this.warpScroll = 0;
        this.dungeonScroll = 0;
        this.questScroll = 0;
        this.auditScroll = 0;
        this.researchScroll = 0;
        this.logScroll = 0;
        this.terminalMode = "terminal";
        this.selectedPlayerUUID = null;
        this.suggestions.clear();
        this.selectedSuggestion = -1;
        this.lastSuggestionQuery = "";
        this.applyTabVisibility();
        if (tab == 5 && this.economyDashboardPanel != null) {
            this.economyDashboardPanel.requestData();
        }
        if (tab == 6) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_skills", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
        if (tab == 7) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_audit_log", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
        if (tab == 2) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_warps", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
        if (tab == 8) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_inventory", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
        if (tab == 9) {
            this.entityScroll = 0;
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_entities", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
        // New panel tab data requests
        if (tab == 11 && this.museumPanel != null) this.museumPanel.requestData();
        if (tab == 12 && this.inventoryPanel != null) this.inventoryPanel.requestData();
        if (tab == 13 && this.dungeonAnalyticsPanel != null) this.dungeonAnalyticsPanel.requestData();
        if (tab == 14 && this.featureTogglesPanel != null) this.featureTogglesPanel.requestData();
        if (tab == 15 && this.moderationPanel != null) this.moderationPanel.requestData();
        if (tab == 17 && this.schedulerPanel != null) this.schedulerPanel.requestData();
        if (tab == 18 && this.botControlPanel != null) this.botControlPanel.requestData();
        if (tab == 20 && this.adminModulesPanel != null) this.adminModulesPanel.requestData();
        if (tab == 21 && this.warpPanel != null) this.warpPanel.requestData();
        // TODO: citizensPanel removed - if (tab == 22 && this.citizensPanel != null) this.citizensPanel.requestData();
        if (tab == 23 && this.casinoPanel != null) this.casinoPanel.requestData();
        if (tab == 24 && this.corruptionPanel != null) this.corruptionPanel.requestData();
        if (tab == 25 && this.marketplaceAdminPanel != null) this.marketplaceAdminPanel.requestData();
        if (tab == 26 && this.alchemyAdminPanel != null) this.alchemyAdminPanel.requestData();
        if (tab == 27 && this.systemHealthPanel != null) this.systemHealthPanel.requestData();
    }

    private void applyTabVisibility() {
        boolean isItems;
        boolean isTerminal;
        boolean isEconomy;
        boolean bl = isTerminal = this.currentTab == 10;
        if (this.commandInput != null) {
            this.commandInput.visible = isTerminal && "terminal".equals(this.terminalMode);
        }
        boolean bl2 = isItems = this.currentTab == 3;
        if (this.itemSearchBox != null) {
            this.itemSearchBox.visible = isItems;
        }
        boolean bl3 = isEconomy = this.currentTab == 6;
        if (this.ecoAmountBox != null) {
            this.ecoAmountBox.visible = isEconomy;
        }
        if (this.broadcastBox != null) {
            this.broadcastBox.visible = this.currentTab == 0 && this.animationComplete;
        }
        if (this.warpNameBox != null) {
            this.warpNameBox.visible = this.currentTab == 2 && this.animationComplete;
        }
        if (this.auditSearchBox != null) {
            this.auditSearchBox.visible = this.currentTab == 7 && this.animationComplete;
        }
        if (this.itemEditorInput != null) {
            this.itemEditorInput.visible = this.currentTab == 8 && this.animationComplete;
        }
        if (this.lootInput != null) {
            this.lootInput.visible = this.currentTab == 30 && this.animationComplete;
        }
        if (this.aliasInput != null) {
            this.aliasInput.visible = this.currentTab == 31 && this.animationComplete;
        }
    }

    private void sendCommand(String cmd) {
        ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("execute_command", cmd), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private void executeTerminalCommand() {
        String text = this.commandInput.getValue().trim();
        if (text.isEmpty()) {
            return;
        }
        String timestamp = String.format("[%tT] ", System.currentTimeMillis());
        this.terminalOutput.add(timestamp + "> " + text);
        this.commandHistory.add(text);
        this.historyIndex = this.commandHistory.size();
        this.sendCommand(text);
        this.commandInput.setValue("");
        this.suggestions.clear();
        this.selectedSuggestion = -1;
        this.lastSuggestionQuery = "";
        this.updateTerminalScroll();
    }

    private void updateTerminalScroll() {
        Objects.requireNonNull(this.font);
        int lineH = 9 + 3;
        int visibleH = this.contentBottom - 28 - this.contentTop;
        int totalH = this.terminalOutput.size() * lineH;
        this.terminalScroll = Math.max(0, totalH - visibleH);
    }

    public void tick() {
        ComputerDataPayload response;
        super.tick();
        // Request player tracker data every 40 ticks (2 seconds) when on Players tab
        if (++this.trackerTick % 40 == 0 && this.currentTab == 1) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_player_tracker", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
        if (!this.animationComplete) {
            ++this.animationTicks;
            if (this.animationTicks / 2 >= WELCOME_TEXT.length()) {
                this.animationComplete = true;
                welcomeShownThisSession = true;
                this.applyTabVisibility();
            }
        }
        if ((response = ComputerDataPayload.lastResponse) != null) {
            String responseType = response.dataType();
            if ("command_result".equals(responseType)) {
                String data = response.jsonData();
                String ts = String.format("[%tT] ", System.currentTimeMillis());
                if (data != null && !data.isEmpty() && !data.equals("{\"success\":true}")) {
                    for (String line : data.split("\n")) {
                        this.terminalOutput.add(ts + line);
                    }
                } else {
                    this.terminalOutput.add(ts + "Command executed.");
                }
                ComputerDataPayload.lastResponse = null;
                this.updateTerminalScroll();
            } else if ("economy_data".equals(responseType)) {
                this.parseEconomyData(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("skills_data".equals(responseType)) {
                this.parseSkillData(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("player_detail_data".equals(responseType)) {
                this.parsePlayerDetail(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("dungeons_data".equals(responseType)) {
                this.parseDungeons(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("quests_data".equals(responseType)) {
                this.parseQuests(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("warps_data".equals(responseType)) {
                this.parseWarps(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("audit_log_data".equals(responseType)) {
                this.parseAuditLog(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("player_audit_data".equals(responseType)) {
                this.parsePlayerAuditData(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("game_scores_data".equals(responseType)) {
                this.parseGameScoresData(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("cosmetics_data".equals(responseType)) {
                this.parseCosmeticData(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("party_view_data".equals(responseType)) {
                this.parsePartyViewData(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("bounty_view_data".equals(responseType)) {
                this.parseBountyViewData(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("shop_config_data".equals(responseType)) {
                try {
                    JsonObject sc = JsonParser.parseString(response.jsonData()).getAsJsonObject();
                    this.shopIntervalTicks = sc.get("intervalTicks").getAsInt();
                    this.shopPriceMult = sc.get("priceMult").getAsDouble();
                    this.shopSellPct = sc.get("sellPct").getAsDouble();
                } catch (Exception e) {}
                ComputerDataPayload.lastResponse = null;
            } else if ("suggestions_data".equals(responseType)) {
                this.parseSuggestions(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("inventory_data".equals(responseType)) {
                this.parseInventoryData(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("research_result".equals(responseType)) {
                try {
                    JsonObject obj = JsonParser.parseString(response.jsonData()).getAsJsonObject();
                    if (obj.has("msg")) {
                        this.adminMessages.add(obj.get("msg").getAsString());
                    }
                } catch (Exception e) {}
                // Re-request inventory to refresh display
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_inventory", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
                ComputerDataPayload.lastResponse = null;
            } else if ("player_tracker_data".equals(responseType)) {
                try {
                    this.trackerData = new java.util.HashMap<>();
                    JsonArray pArr = JsonParser.parseString(response.jsonData()).getAsJsonObject().getAsJsonArray("players");
                    for (JsonElement el : pArr) {
                        JsonObject p = el.getAsJsonObject();
                        String pn = p.get("name").getAsString();
                        String dim = p.get("dim").getAsString();
                        // Shorten dimension name
                        if (dim.contains(":")) dim = dim.substring(dim.indexOf(':') + 1);
                        this.trackerData.put(pn, new String[]{
                            String.valueOf(p.get("x").getAsInt()),
                            String.valueOf(p.get("y").getAsInt()),
                            String.valueOf(p.get("z").getAsInt()),
                            dim
                        });
                    }
                } catch (Exception e) {}
                ComputerDataPayload.lastResponse = null;
            } else if ("performance_data".equals(responseType)) {
                try {
                    JsonObject pObj = JsonParser.parseString(response.jsonData()).getAsJsonObject();
                    this.perfTps = pObj.get("tps").getAsDouble();
                    this.perfMspt = pObj.get("mspt").getAsDouble();
                    this.perfMem = pObj.get("mem").getAsLong();
                    this.perfMaxMem = pObj.get("maxMem").getAsLong();
                    this.perfTotalMem = pObj.get("totalMem").getAsLong();
                    this.perfEntities = pObj.get("entities").getAsInt();
                    this.perfChunks = pObj.get("chunks").getAsInt();
                    this.perfPlayers = pObj.get("players").getAsInt();
                    if (pObj.has("activeArenas")) this.perfActiveArenas = pObj.get("activeArenas").getAsInt();
                    if (pObj.has("activeBounties")) this.perfActiveBounties = pObj.get("activeBounties").getAsInt();
                    if (pObj.has("activeDungeons")) this.perfActiveDungeons = pObj.get("activeDungeons").getAsInt();
                    // Parse TPS history for graph
                    if (pObj.has("tpsHistory")) {
                        this.tpsHistory.clear();
                        for (JsonElement h : pObj.getAsJsonArray("tpsHistory")) {
                            JsonObject ho = h.getAsJsonObject();
                            this.tpsHistory.add(new double[]{ho.get("tps").getAsDouble(), ho.get("mem").getAsDouble()});
                        }
                    }
                    // Parse issues/warnings
                    if (pObj.has("issues")) {
                        this.perfIssues.clear();
                        for (JsonElement iss : pObj.getAsJsonArray("issues")) {
                            JsonObject io = iss.getAsJsonObject();
                            this.perfIssues.add(new String[]{io.get("level").getAsString(), io.get("msg").getAsString()});
                        }
                    }
                } catch (Exception e) {}
                ComputerDataPayload.lastResponse = null;
            } else if ("deathlog_data".equals(responseType)) {
                this.deathLogLines = new java.util.ArrayList<>();
                try {
                    JsonArray dArr = JsonParser.parseString(response.jsonData()).getAsJsonObject().getAsJsonArray("deaths");
                    long now = System.currentTimeMillis();
                    for (JsonElement el : dArr) {
                        JsonObject d = el.getAsJsonObject();
                        long elapsed = (now - d.get("time").getAsLong()) / 1000;
                        String timeAgo = elapsed < 60 ? elapsed + "s" : elapsed < 3600 ? (elapsed/60) + "m" : (elapsed/3600) + "h";
                        this.deathLogLines.add(new String[]{
                            d.get("name").getAsString(),
                            String.valueOf(d.get("x").getAsInt()),
                            String.valueOf(d.get("y").getAsInt()),
                            String.valueOf(d.get("z").getAsInt()),
                            d.get("dim").getAsString(),
                            d.get("cause").getAsString(),
                            String.valueOf(d.get("itemCount").getAsInt()),
                            timeAgo
                        });
                    }
                } catch (Exception e) {}
                ComputerDataPayload.lastResponse = null;
            } else if ("loot_data".equals(responseType)) {
                this.lootTableData = new java.util.ArrayList<>();
                try {
                    JsonArray mArr = JsonParser.parseString(response.jsonData()).getAsJsonObject().getAsJsonArray("mobs");
                    for (JsonElement mel : mArr) {
                        JsonObject m = mel.getAsJsonObject();
                        String mobId = m.get("mobId").getAsString();
                        JsonArray drops = m.getAsJsonArray("drops");
                        for (int di = 0; di < drops.size(); di++) {
                            JsonObject dr = drops.get(di).getAsJsonObject();
                            this.lootTableData.add(new String[]{
                                mobId, dr.get("item").getAsString(),
                                String.valueOf(dr.get("min").getAsInt()), String.valueOf(dr.get("max").getAsInt()),
                                String.valueOf(dr.get("chance").getAsDouble()), String.valueOf(di)
                            });
                        }
                    }
                } catch (Exception e) {}
                ComputerDataPayload.lastResponse = null;
            } else if ("alias_data".equals(responseType)) {
                this.aliasData = new java.util.ArrayList<>();
                try {
                    JsonArray aArr = JsonParser.parseString(response.jsonData()).getAsJsonObject().getAsJsonArray("aliases");
                    for (JsonElement el : aArr) {
                        JsonObject a = el.getAsJsonObject();
                        this.aliasData.add(new String[]{a.get("name").getAsString(), a.get("cmd").getAsString()});
                    }
                } catch (Exception e) {}
                ComputerDataPayload.lastResponse = null;
            } else if ("undo_data".equals(responseType)) {
                this.undoData = new java.util.ArrayList<>();
                try {
                    JsonArray uArr = JsonParser.parseString(response.jsonData()).getAsJsonObject().getAsJsonArray("history");
                    for (JsonElement el : uArr) {
                        JsonObject u = el.getAsJsonObject();
                        this.undoData.add(new String[]{
                            u.get("type").getAsString(), u.get("admin").getAsString(),
                            u.get("target").getAsString(), u.get("desc").getAsString()
                        });
                    }
                } catch (Exception e) {}
                ComputerDataPayload.lastResponse = null;
            } else if ("entity_data".equals(responseType)) {
                this.entityList.clear();
                try {
                    JsonArray eArr = JsonParser.parseString(response.jsonData()).getAsJsonArray();
                    for (JsonElement el : eArr) {
                        JsonObject eObj = el.getAsJsonObject();
                        this.entityList.add(new EntityEntry(
                            eObj.get("id").getAsInt(),
                            eObj.get("type").getAsString(),
                            eObj.get("name").getAsString(),
                            eObj.get("x").getAsDouble(),
                            eObj.get("y").getAsDouble(),
                            eObj.get("z").getAsDouble(),
                            eObj.get("health").getAsFloat(),
                            eObj.get("maxHealth").getAsFloat(),
                            eObj.get("distance").getAsFloat()
                        ));
                    }
                } catch (Exception e) {}
                ComputerDataPayload.lastResponse = null;
            } else if ("log_data".equals(responseType)) {
                this.serverLogLines.clear();
                try {
                    JsonArray logArr = JsonParser.parseString(response.jsonData()).getAsJsonArray();
                    for (JsonElement el : logArr) {
                        this.serverLogLines.add(el.getAsString());
                    }
                } catch (Exception e) {
                    this.serverLogLines.add("Failed to parse log data.");
                }
                ComputerDataPayload.lastResponse = null;
            } else if (responseType.startsWith("museum_") && this.museumPanel != null) {
                this.museumPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if (responseType.startsWith("inv_view_") && this.inventoryPanel != null) {
                this.inventoryPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if (responseType.startsWith("dungeon_analytics_") && this.dungeonAnalyticsPanel != null) {
                this.dungeonAnalyticsPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if (responseType.startsWith("feature_toggles_") && this.featureTogglesPanel != null) {
                this.featureTogglesPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if (responseType.startsWith("mod_") && this.moderationPanel != null) {
                this.moderationPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if (responseType.startsWith("locate_") && this.structureLocatorPanel != null) {
                this.structureLocatorPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if (responseType.startsWith("we_") && this.worldEditPanel != null) {
                this.worldEditPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if (responseType.startsWith("scheduler_") && this.schedulerPanel != null) {
                this.schedulerPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if (responseType.startsWith("bot_") && this.botControlPanel != null) {
                this.botControlPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ((responseType.startsWith("mob_showcase_") || responseType.startsWith("furniture_showcase_")) && this.mobShowcasePanel != null) {
                this.mobShowcasePanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if (responseType.startsWith("adminmod_") && this.adminModulesPanel != null) {
                this.adminModulesPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ((responseType.equals("warp_data") || responseType.equals("warp_result")) && this.warpPanel != null) {
                this.warpPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("encyclopedia_result".equals(responseType)) {
                try {
                    JsonObject obj = JsonParser.parseString(response.jsonData()).getAsJsonObject();
                    if (obj.has("msg")) this.adminMessages.add(obj.get("msg").getAsString());
                } catch (Exception ignored) {}
                ComputerDataPayload.lastResponse = null;
            } else if ("corruption_data".equals(responseType)) {
                this.corruptionJsonCache = response.jsonData();
                if (this.corruptionPanel != null) this.corruptionPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("corruption_result".equals(responseType)) {
                try {
                    JsonObject obj = JsonParser.parseString(response.jsonData()).getAsJsonObject();
                    if (obj.has("message")) this.adminMessages.add(obj.get("message").getAsString());
                } catch (Exception ignored) {}
                if (this.corruptionPanel != null) this.corruptionPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("purge_data".equals(responseType)) {
                if (this.corruptionPanel != null) this.corruptionPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if (responseType.startsWith("eco_") && this.economyDashboardPanel != null) {
                this.economyDashboardPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("marketplace_admin_data".equals(responseType) && this.marketplaceAdminPanel != null) {
                this.marketplaceAdminPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("alchemy_admin_data".equals(responseType) && this.alchemyAdminPanel != null) {
                this.alchemyAdminPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("system_health_data".equals(responseType) && this.systemHealthPanel != null) {
                this.systemHealthPanel.handleResponse(responseType, response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("admin_search_results".equals(responseType) && this.adminSearchPanel != null) {
                this.adminSearchPanel.handleResponse(response.jsonData());
                ComputerDataPayload.lastResponse = null;
            } else if ("admin_result".equals(responseType) || "broadcast_result".equals(responseType) || "warp_result".equals(responseType)) {
                try {
                    JsonObject obj = JsonParser.parseString(response.jsonData()).getAsJsonObject();
                    if (obj.has("msg")) {
                        this.adminMessages.add(obj.get("msg").getAsString());
                    } else {
                        this.adminMessages.add("Done.");
                    }
                } catch (Exception e) {}
                ComputerDataPayload.lastResponse = null;
            }
        }
        // Tick panels that need it
        if (this.currentTab == 11 && this.museumPanel != null) this.museumPanel.tick();
        if (this.currentTab == 13 && this.dungeonAnalyticsPanel != null) this.dungeonAnalyticsPanel.tick();
        if (this.currentTab == 18 && this.botControlPanel != null) this.botControlPanel.tick();
        if (this.currentTab == 5 && this.economyDashboardPanel != null) this.economyDashboardPanel.tick();
        if (this.currentTab == 25 && this.marketplaceAdminPanel != null) this.marketplaceAdminPanel.tick();
        if (this.currentTab == 26 && this.alchemyAdminPanel != null) this.alchemyAdminPanel.tick();
        if (this.currentTab == 27 && this.systemHealthPanel != null) this.systemHealthPanel.tick();
        if (this.currentTab == 36 && this.adminSearchPanel != null) this.adminSearchPanel.tick();
        if (this.currentTab == 35 && this.worldEditPanel != null) this.worldEditPanel.tick();
        // Performance auto-refresh when on Dashboard
        if (this.currentTab == 0 && this.animationComplete) {
            this.perfRefreshCounter++;
            if (this.perfRefreshCounter % 40 == 0) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_performance", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
        }
        // Auto-refresh logs when in log mode on terminal tab
        if ("logs".equals(this.terminalMode) && this.animationComplete) {
            this.logRefreshTicks++;
            if (this.logRefreshTicks % 100 == 0) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_server_logs", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
        }
        // Request suggestions while typing in terminal
        if (this.currentTab == 10 && this.commandInput != null && this.commandInput.isFocused()) {
            String currentText = this.commandInput.getValue().trim();
            if (!currentText.isEmpty() && !currentText.equals(this.lastSuggestionQuery)) {
                long now = System.currentTimeMillis();
                if (now - this.lastSuggestionTime > 150) { // debounce 150ms
                    this.lastSuggestionQuery = currentText;
                    this.lastSuggestionTime = now;
                    ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_suggestions", currentText), (CustomPacketPayload[])new CustomPacketPayload[0]);
                }
            } else if (currentText.isEmpty()) {
                this.suggestions.clear();
                this.lastSuggestionQuery = "";
            }
        }
    }

    private int getBackBtnX() {
        String adminName = this.mc.player != null ? this.mc.player.getGameProfile().name() : "Admin";
        int adminW = this.font.width(adminName);
        return this.width - adminW - 58;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.actionRects.clear();
        this.gameruleRects.clear();
        // Dark background
        g.fill(0, 0, this.width, this.height, 0xFF0D1117);
        if (!this.animationComplete) {
            this.renderAnimation(g);
            super.render(g, mouseX, mouseY, partialTick);
            return;
        }
        // Sidebar
        g.fill(0, 0, SIDEBAR_WIDTH, this.height, 0xFF161B22);
        g.fill(SIDEBAR_WIDTH - 1, 0, SIDEBAR_WIDTH, this.height, 0xFF30363D);
        // Sidebar tabs (scrollable)
        int tabAreaTop = HEADER_HEIGHT + 4;
        int tabAreaBottom = this.height - STATUS_BAR_HEIGHT;
        int tabH = 17;
        int tabGap = 1;
        int totalTabHeight = TAB_NAMES.length * (tabH + tabGap);
        int visibleTabHeight = tabAreaBottom - tabAreaTop;
        int maxTabScroll = Math.max(0, totalTabHeight - visibleTabHeight);
        this.tabScroll = Math.max(0, Math.min(this.tabScroll, maxTabScroll));

        // Scroll indicators
        if (this.tabScroll > 0) {
            g.drawCenteredString(this.font, "\u25B2", SIDEBAR_WIDTH / 2, tabAreaTop - 1, 0xFF58A6FF);
        }
        if (this.tabScroll < maxTabScroll) {
            g.drawCenteredString(this.font, "\u25BC", SIDEBAR_WIDTH / 2, tabAreaBottom - 8, 0xFF58A6FF);
        }

        g.enableScissor(0, tabAreaTop, SIDEBAR_WIDTH, tabAreaBottom);
        for (int i = 0; i < this.tabBounds.length; ++i) {
            int[] tb = this.tabBounds[i];
            int drawY = tb[1] - this.tabScroll;
            // Skip if completely outside visible area
            if (drawY + tb[3] < tabAreaTop || drawY > tabAreaBottom) continue;
            boolean selected = i == this.currentTab;
            boolean tabHovered = mouseX >= tb[0] && mouseX < tb[0] + tb[2] && mouseY >= drawY && mouseY < drawY + tb[3]
                && mouseY >= tabAreaTop && mouseY < tabAreaBottom;
            if (selected) {
                g.fill(tb[0], drawY, tb[0] + tb[2], drawY + tb[3], 0xFF21262D);
                g.fill(tb[0], drawY, tb[0] + 2, drawY + tb[3], 0xFF58A6FF);
            } else if (tabHovered) {
                g.fill(tb[0], drawY, tb[0] + tb[2], drawY + tb[3], 0xFF1C2128);
            }
            String shortName = TAB_NAMES[i];
            int labelColor = selected ? 0xFFE6EDF3 : (tabHovered ? 0xFFC9D1D9 : 0xFF8B949E);
            int labelW = this.font.width(shortName);
            g.drawString(this.font, shortName, tb[0] + (tb[2] - labelW) / 2, drawY + 4, labelColor, false);
        }
        g.disableScissor();
        // Header bar
        g.fill(SIDEBAR_WIDTH, 0, this.width, HEADER_HEIGHT, 0xFF161B22);
        g.fill(SIDEBAR_WIDTH, HEADER_HEIGHT - 1, this.width, HEADER_HEIGHT, 0xFF30363D);
        String headerTitle = TAB_NAMES[this.currentTab];
        g.drawString(this.font, headerTitle, SIDEBAR_WIDTH + 8, 7, 0xFF58A6FF, false);
        // Admin name on right
        String adminName = this.mc.player != null ? this.mc.player.getGameProfile().name() : "Admin";
        int adminW = this.font.width(adminName);
        g.drawString(this.font, adminName, this.width - adminW - 8, 7, 0xFF8B949E, false);
        // Back button in header
        int backX = this.getBackBtnX();
        boolean backHover = mouseX >= backX && mouseX < backX + 40 && mouseY >= 4 && mouseY < 18;
        g.fill(backX, 4, backX + 40, 18, backHover ? 0xFF30363D : 0xFF21262D);
        int backTxtW = this.font.width("Back");
        g.drawString(this.font, "Back", backX + (40 - backTxtW) / 2, 7, backHover ? 0xFFE6EDF3 : 0xFF8B949E, false);
        // Status bar
        g.fill(SIDEBAR_WIDTH, this.height - STATUS_BAR_HEIGHT, this.width, this.height, 0xFF161B22);
        g.fill(SIDEBAR_WIDTH, this.height - STATUS_BAR_HEIGHT, this.width, this.height - STATUS_BAR_HEIGHT + 1, 0xFF30363D);
        String statusText = String.format("TPS: %.1f | Mem: %dMB/%dMB | Entities: %d | Chunks: %d | Players: %d",
            this.perfTps, this.perfMem, this.perfMaxMem, this.perfEntities, this.perfChunks, this.perfPlayers);
        int tpsColor = this.perfTps >= 18 ? 0xFF3FB950 : (this.perfTps >= 15 ? 0xFFD29922 : 0xFFF85149);
        g.drawString(this.font, statusText, SIDEBAR_WIDTH + 8, this.height - STATUS_BAR_HEIGHT + 4, tpsColor, false);
        // Content area background
        g.fill(this.contentLeft, this.contentTop, this.contentRight, this.contentBottom, 0xFF0D1117);
        g.fill(this.contentLeft, this.contentTop, this.contentRight, this.contentTop + 1, 0xFF21262D);
        g.fill(this.contentLeft, this.contentTop, this.contentLeft + 1, this.contentBottom, 0xFF21262D);
        switch (this.currentTab) {
            case 0: { this.renderDashboard(g, mouseX, mouseY); break; }
            case 1: { this.renderPlayers(g, mouseX, mouseY); break; }
            case 2: { this.renderWorld(g, mouseX, mouseY); break; }
            case 3: { this.renderItems(g, mouseX, mouseY); break; }
            case 4: { this.renderMegaMod(g, mouseX, mouseY); break; }
            case 5: { if (this.economyDashboardPanel != null) this.economyDashboardPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 6: { this.renderSkills(g, mouseX, mouseY); break; }
            case 7: { this.renderAudit(g, mouseX, mouseY); break; }
            case 8: { this.renderResearch(g, mouseX, mouseY); break; }
            case 9: { this.renderEntities(g, mouseX, mouseY); break; }
            case 10: { this.renderTerminal(g, mouseX, mouseY); break; }
            case 11: { if (this.museumPanel != null) this.museumPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 12: { if (this.inventoryPanel != null) this.inventoryPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 13: { if (this.dungeonAnalyticsPanel != null) this.dungeonAnalyticsPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 14: { if (this.featureTogglesPanel != null) this.featureTogglesPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 15: { if (this.moderationPanel != null) this.moderationPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 16: { if (this.structureLocatorPanel != null) this.structureLocatorPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 17: { if (this.schedulerPanel != null) this.schedulerPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 18: { if (this.botControlPanel != null) this.botControlPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 19: { if (this.mobShowcasePanel != null) this.mobShowcasePanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 20: { if (this.adminModulesPanel != null) this.adminModulesPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 21: { if (this.warpPanel != null) { this.warpPanel.tick(); this.warpPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); } break; }
            case 22: { /* TODO: citizensPanel removed */ break; }
            case 23: { if (this.casinoPanel != null) { this.casinoPanel.tick(); this.casinoPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); } break; }
            case 24: { if (this.corruptionPanel != null) { this.corruptionPanel.tick(); this.corruptionPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); } break; }
            case 25: { if (this.marketplaceAdminPanel != null) this.marketplaceAdminPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 26: { if (this.alchemyAdminPanel != null) this.alchemyAdminPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 27: { if (this.systemHealthPanel != null) this.systemHealthPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); break; }
            case 28: { this.renderDeathLog(g, mouseX, mouseY); break; }
            case 29: { this.renderCleanup(g, mouseX, mouseY); break; }
            case 30: { this.renderLootTables(g, mouseX, mouseY); break; }
            case 31: { this.renderAliases(g, mouseX, mouseY); break; }
            case 32: { this.renderUndo(g, mouseX, mouseY); break; }
            case 33: { if (this.mobShowcasePanel != null) { this.mobShowcasePanel.setActiveView(1); this.mobShowcasePanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); } break; }
            case 34: { this.renderCombatConfig(g, mouseX, mouseY); break; }
            case 35: { if (this.worldEditPanel != null) { this.worldEditPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); } break; }
            case 36: { if (this.adminSearchPanel != null) { this.adminSearchPanel.render(g, mouseX, mouseY, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom); } break; }
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    private int getResearchRowHeight(ResearchInvEntry entry) {
        int h = 28; // base: icon + name + slot line
        h += 14; // custom name line
        h += 14 + entry.loreLines.size() * 11; // lore header + lines
        if (entry.hasWeaponStats) {
            h += 24; // rarity line + base damage line
            h += entry.bonuses.size() * 13; // each bonus line
            h += 70; // bonus picker rows (expanded with all custom attributes)
            if (!entry.weaponSkills.isEmpty()) {
                h += 13; // "Skills:" header
                h += entry.weaponSkills.size() * 22; // name + description per skill
            }
        }
        if (entry.hasRelicData) {
            h += 14; // relic Lv/Q line
            h += 14; // XP line with +/- buttons
            h += 16; // Reroll/Max Stats/Reset buttons
            if (!entry.abilities.isEmpty()) {
                for (ResearchAbility ab : entry.abilities) {
                    h += 13; // ability header line
                    if (!ab.description.isEmpty()) h += 11; // description
                    h += 2; // spacing
                    if (!"PASSIVE".equals(ab.castType)) {
                        h += 12; // cooldown line
                    }
                    h += ab.stats.size() * 12; // each stat line
                }
            }
        }
        if (entry.isRelicItem && !entry.hasRelicData) {
            h += 18; // Init Relic button
        }
        if (!entry.hasWeaponStats && !entry.hasRelicData && !entry.isRelicItem) {
            h += 32; // "Init Weapon" + "Init Armor" buttons
        }
        // Enchantments section
        if (!entry.enchantments.isEmpty()) {
            h += 15 + entry.enchantments.size() * 13; // header + each enchantment line + spacing
        }
        h += 44; // enchant picker (3 rows)
        if (entry.hasWeaponStats) {
            // Dynamic skill picker: divider + S1 header + S1 grid + S2 header + S2 grid
            int skillCount = 0;
            try { skillCount = com.ultra.megamod.feature.relics.weapons.RpgWeaponRegistry.getAllSkillNames().size(); } catch (Exception e) {}
            int rowsPerSlot = Math.max(1, (int) Math.ceil(skillCount / 6.0)); // ~6 skills per row
            h += 4 + 13 + rowsPerSlot * 12 + 14 + 12 + rowsPerSlot * 12 + 14; // divider + S1 header + S1 rows + gap + S2 header + S2 rows + gap
        } else {
            h += 4; // just divider
        }
        return h + 4; // padding
    }

    private void renderResearch(GuiGraphics g, int mouseX, int mouseY) {
        int cx = this.contentLeft + 4;
        int cy = this.contentTop + 4;
        int cw = this.contentRight - this.contentLeft - 8;
        this.drawSectionHeader(g, "Item Editor", cx, cy, cw);

        // Refresh button
        int refreshBtnW = 70;
        int refreshBtnH = 14;
        int refreshBtnX = this.contentRight - refreshBtnW - 8;
        int refreshBtnY = this.contentTop + 4;
        boolean refreshHovered = mouseX >= refreshBtnX && mouseX < refreshBtnX + refreshBtnW && mouseY >= refreshBtnY && mouseY < refreshBtnY + refreshBtnH;
        darkBtn(g, refreshBtnX, refreshBtnY, refreshBtnW, refreshBtnH, refreshHovered);
        g.drawString(this.font, "Refresh", refreshBtnX + 16, refreshBtnY + 3, 0xFFE6EDF3, false);
        this.actionRects.add(new ActionRect(refreshBtnX, refreshBtnY, refreshBtnW, refreshBtnH, "__research_refresh__"));
        cy += 16;

        // Name/Lore input box
        g.drawString(this.font, "Text:", cx, cy + 3, 0xFF8B949E, false);
        if (this.itemEditorInput != null) {
            this.itemEditorInput.setX(cx + 28);
            this.itemEditorInput.setY(cy);
        }
        cy += 18;

        if (this.researchInventory.isEmpty()) {
            g.drawString(this.font, "No items in inventory.", cx, cy, 0xFF8B949E, false);
            g.drawString(this.font, "Pick up items and click Refresh.", cx, cy + 12, 0xFF8B949E, false);
            return;
        }

        // Calculate total content height
        int totalH = 0;
        for (ResearchInvEntry entry : this.researchInventory) {
            totalH += getResearchRowHeight(entry);
        }
        int visibleH = this.contentBottom - cy - 4;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.researchScroll = Math.max(0, Math.min(this.researchScroll, maxScroll));

        g.enableScissor(this.contentLeft, cy, this.contentRight, this.contentBottom);

        int drawY = cy - this.researchScroll;
        for (int i = 0; i < this.researchInventory.size(); i++) {
            ResearchInvEntry entry = this.researchInventory.get(i);
            int rowH = getResearchRowHeight(entry);
            if (drawY + rowH < cy) { drawY += rowH; continue; }
            if (drawY > this.contentBottom) break;

            boolean rowHovered = mouseX >= cx && mouseX < cx + cw && mouseY >= drawY && mouseY < drawY + rowH;
            darkCard(g,cx, drawY, cw, rowH - 2, rowHovered);

            // Item icon
            try {
                Item item = (Item) BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(entry.itemId));
                g.renderItem(new ItemStack((net.minecraft.world.level.ItemLike) item), cx + 4, drawY + 4);
            } catch (Exception e) {}

            // Item name + slot info
            String displayName = entry.displayName;
            if (displayName.length() > 35) displayName = displayName.substring(0, 35) + "...";
            g.drawString(this.font, displayName, cx + 24, drawY + 4, 0xFFE6EDF3, false);
            g.drawString(this.font, "Slot: " + entry.slot + " | " + entry.itemId, cx + 24, drawY + 15, 0xFF8B949E, false);

            int innerY = drawY + 28;

            // Custom name editing
            String nameLabel = entry.customName.isEmpty() ? "(default)" : "\"" + entry.customName + "\"";
            g.drawString(this.font, "Name: " + nameLabel, cx + 24, innerY, entry.customName.isEmpty() ? 0xFF8B949E : 0xFF58A6FF, false);
            int nameBtnX = cx + cw - 120;
            this.drawActionButton(g, nameBtnX, innerY - 1, 50, 13, "Set", mouseX, mouseY, "__research_set_name__:" + entry.slot);
            this.drawActionButton(g, nameBtnX + 54, innerY - 1, 50, 13, "Clear", mouseX, mouseY, "__research_clear_name__:" + entry.slot);
            innerY += 14;

            // Lore editing
            g.drawString(this.font, "Lore:", cx + 24, innerY, 0xFFA371F7, false);
            int loreBtnX = cx + cw - 120;
            this.drawActionButton(g, loreBtnX, innerY - 1, 50, 13, "Add", mouseX, mouseY, "__research_add_lore__:" + entry.slot);
            this.drawActionButton(g, loreBtnX + 54, innerY - 1, 50, 13, "Clear", mouseX, mouseY, "__research_clear_lore__:" + entry.slot);
            innerY += 13;
            if (entry.loreLines.isEmpty()) {
                g.drawString(this.font, "  (none)", cx + 24, innerY, 0xFF8B949E, false);
                innerY += 11;
            } else {
                for (int li = 0; li < entry.loreLines.size(); li++) {
                    String loreLine = entry.loreLines.get(li);
                    if (loreLine.length() > 50) loreLine = loreLine.substring(0, 50) + "...";
                    g.drawString(this.font, "  " + loreLine, cx + 24, innerY, 0xFFC9D1D9, false);
                    this.drawActionButton(g, cx + cw - 22, innerY - 1, 16, 12, "X", mouseX, mouseY, "__research_rm_lore__:" + entry.slot + ":" + li);
                    innerY += 11;
                }
            }
            innerY += 1;

            if (entry.hasWeaponStats) {
                // Rarity line with controls
                g.drawString(this.font, "Rarity: " + entry.rarityName, cx + 24, innerY, entry.rarityColor, false);

                // Rarity buttons on right side
                int rarBtnX = cx + cw - 195;
                boolean rarUpHover = mouseX >= rarBtnX && mouseX < rarBtnX + 14 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                this.darkIconBtn(g,rarBtnX, innerY - 1, 14, "+", 0xFF3FB950, rarUpHover);
                this.actionRects.add(new ActionRect(rarBtnX, innerY - 1, 14, 14, "research_rarity_up:" + entry.slot));

                boolean rarDnHover = mouseX >= rarBtnX + 16 && mouseX < rarBtnX + 30 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                this.darkIconBtn(g,rarBtnX + 16, innerY - 1, 14, "-", 0xFFF85149, rarDnHover);
                this.actionRects.add(new ActionRect(rarBtnX + 16, innerY - 1, 14, 14, "research_rarity_down:" + entry.slot));

                // Reroll button
                int rerollBtnX = cx + cw - 128;
                boolean rerollHover = mouseX >= rerollBtnX && mouseX < rerollBtnX + 55 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                darkBtn(g,rerollBtnX, innerY - 1, 55, 14, rerollHover);
                g.drawString(this.font, "Reroll", rerollBtnX + 12, innerY + 2, 0xFFE6EDF3, false);
                this.actionRects.add(new ActionRect(rerollBtnX, innerY - 1, 55, 14, "research_reroll:" + entry.slot));

                // Max Rarity button
                int legBtnX = cx + cw - 68;
                boolean legHover = mouseX >= legBtnX && mouseX < legBtnX + 64 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                darkBtn(g,legBtnX, innerY - 1, 64, 14, legHover);
                g.drawString(this.font, "Max Rarity", legBtnX + 4, innerY + 2, 0xFFD29922, false);
                this.actionRects.add(new ActionRect(legBtnX, innerY - 1, 64, 14, "research_max_rarity:" + entry.slot));

                innerY += 13;
                g.drawString(this.font, "Base Dmg: " + String.format("%.1f", entry.baseDamage), cx + 24, innerY, 0xFF8B949E, false);
                // Base damage +/- buttons
                int dmgBtnX = cx + cw - 80;
                float dmgUp = entry.baseDamage + 1.0f;
                float dmgDn = Math.max(0, entry.baseDamage - 1.0f);
                boolean dmgUpHover = mouseX >= dmgBtnX && mouseX < dmgBtnX + 13 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                darkIconBtn(g, dmgBtnX, innerY - 1, 13, "+", 0xFF3FB950, dmgUpHover);
                this.actionRects.add(new ActionRect(dmgBtnX, innerY - 1, 13, 13, "__research_set_base_dmg__:" + entry.slot + ":" + String.format("%.1f", dmgUp)));
                boolean dmgDnHover = mouseX >= dmgBtnX + 15 && mouseX < dmgBtnX + 28 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                darkIconBtn(g, dmgBtnX + 15, innerY - 1, 13, "-", 0xFFD29922, dmgDnHover);
                this.actionRects.add(new ActionRect(dmgBtnX + 15, innerY - 1, 13, 13, "__research_set_base_dmg__:" + entry.slot + ":" + String.format("%.1f", dmgDn)));
                // Weapon skills
                if (!entry.weaponSkills.isEmpty()) {
                    innerY += 13;
                    g.drawString(this.font, "Skills:", cx + 24, innerY, 0xFFA371F7, false);
                    innerY += 12;
                    for (ResearchWeaponSkill ws : entry.weaponSkills) {
                        boolean isModified = ws.cooldown != ws.defaultCooldown;
                        String cdLabel = isModified ? " (CD:" + String.format("%.1f", ws.cooldownSec) + "s*)" : " (CD:" + String.format("%.1f", ws.cooldownSec) + "s)";
                        g.drawString(this.font, "  " + ws.name + cdLabel, cx + 24, innerY, 0xFFC9D1D9, false);

                        // Cooldown +/- buttons (step: 20 ticks = 1s)
                        int wcdBtnX = cx + cw - 80;
                        int cdUp = ws.cooldown + 20;
                        int cdDn = Math.max(0, ws.cooldown - 20);

                        boolean wcdUpHover = mouseX >= wcdBtnX && mouseX < wcdBtnX + 13 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                        this.darkIconBtn(g, wcdBtnX, innerY - 1, 13, "+", 0xFF3FB950, wcdUpHover);
                        this.actionRects.add(new ActionRect(wcdBtnX, innerY - 1, 13, 13, "__research_set_weapon_cd__:" + entry.slot + ":" + ws.name + ":" + cdUp));

                        boolean wcdDnHover = mouseX >= wcdBtnX + 15 && mouseX < wcdBtnX + 28 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                        this.darkIconBtn(g, wcdBtnX + 15, innerY - 1, 13, "-", 0xFFF85149, wcdDnHover);
                        this.actionRects.add(new ActionRect(wcdBtnX + 15, innerY - 1, 13, 13, "__research_set_weapon_cd__:" + entry.slot + ":" + ws.name + ":" + cdDn));

                        // Reset button (back to default)
                        if (isModified) {
                            boolean wcdRstHover = mouseX >= wcdBtnX + 32 && mouseX < wcdBtnX + 55 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                            darkBtn(g, wcdBtnX + 32, innerY - 1, 23, 12, wcdRstHover);
                            g.drawString(this.font, "Rst", wcdBtnX + 35, innerY + 1, 0xFFD29922, false);
                            this.actionRects.add(new ActionRect(wcdBtnX + 32, innerY - 1, 23, 12, "__research_set_weapon_cd__:" + entry.slot + ":" + ws.name + ":" + ws.defaultCooldown));
                        }

                        innerY += 10;
                        String descTrunc = ws.desc.length() > 50 ? ws.desc.substring(0, 50) + "..." : ws.desc;
                        g.drawString(this.font, "    " + descTrunc, cx + 24, innerY, 0xFF8B949E, false);
                        innerY += 12;
                    }
                }
                innerY += 11;

                // Individual bonuses
                for (int bi = 0; bi < entry.bonuses.size(); bi++) {
                    ResearchBonus bonus = entry.bonuses.get(bi);
                    String sign = bonus.value >= 0 ? "+" : "";
                    String valStr = bonus.percent ? sign + String.format("%.1f%%", bonus.value) : sign + String.format("%.2f", bonus.value);
                    g.drawString(this.font, "  " + bonus.name + ": " + valStr, cx + 24, innerY, 0xFF8B949E, false);

                    // +/- and +10/-10 buttons for this bonus
                    int bBtnX = cx + cw - 120;
                    double increment = bonus.percent ? 1.0 : 1.0;
                    double newValUp = bonus.value + increment;
                    double newValDn = bonus.value - increment;
                    double newValUp10 = bonus.value + 10.0;
                    double newValDn10 = bonus.value - 10.0;

                    boolean bDnHover = mouseX >= bBtnX && mouseX < bBtnX + 14 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                    this.darkIconBtn(g,bBtnX, innerY - 1, 13, "-", 0xFFF85149, bDnHover);
                    this.actionRects.add(new ActionRect(bBtnX, innerY - 1, 14, 13, "__research_set_bonus__:" + entry.slot + ":" + bi + ":" + String.format("%.4f", newValDn)));

                    boolean bUpHover = mouseX >= bBtnX + 16 && mouseX < bBtnX + 30 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                    this.darkIconBtn(g,bBtnX + 16, innerY - 1, 13, "+", 0xFF3FB950, bUpHover);
                    this.actionRects.add(new ActionRect(bBtnX + 16, innerY - 1, 14, 13, "__research_set_bonus__:" + entry.slot + ":" + bi + ":" + String.format("%.4f", newValUp)));

                    // +10 button
                    int bUp10X = bBtnX + 32;
                    boolean bUp10Hover = mouseX >= bUp10X && mouseX < bUp10X + 22 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                    darkBtn(g, bUp10X, innerY - 1, 22, 13, bUp10Hover);
                    g.drawString(this.font, "+10", bUp10X + 2, innerY + 2, 0xFF3FB950, false);
                    this.actionRects.add(new ActionRect(bUp10X, innerY - 1, 22, 13, "__research_set_bonus__:" + entry.slot + ":" + bi + ":" + String.format("%.4f", newValUp10)));

                    // -10 button
                    int bDn10X = bUp10X + 24;
                    boolean bDn10Hover = mouseX >= bDn10X && mouseX < bDn10X + 22 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                    darkBtn(g, bDn10X, innerY - 1, 22, 13, bDn10Hover);
                    g.drawString(this.font, "-10", bDn10X + 3, innerY + 2, 0xFFF85149, false);
                    this.actionRects.add(new ActionRect(bDn10X, innerY - 1, 22, 13, "__research_set_bonus__:" + entry.slot + ":" + bi + ":" + String.format("%.4f", newValDn10)));

                    // X (remove) button
                    int bRemX = bDn10X + 24;
                    boolean bRemHover = mouseX >= bRemX && mouseX < bRemX + 14 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                    this.darkIconBtn(g,bRemX, innerY - 1, 13, "X", 0xFFF85149, bRemHover);
                    this.actionRects.add(new ActionRect(bRemX, innerY - 1, 14, 13, "__research_remove_bonus__:" + entry.slot + ":" + bi));

                    innerY += 13;
                }

                // Bonus picker - show all available attributes (flowing layout)
                g.drawString(this.font, "  Add:", cx + 24, innerY + 2, 0xFF3FB950, false);
                int pickerX = cx + 56;
                String[] bonusLabels = {
                    "Atk", "ASpd", "HP", "MSpd", "Armor", "Tough", "Luck", "Crit%",
                    "CritD", "Life%", "Fire", "Ice", "Lght", "CDR%", "Dodge", "Regen",
                    "Poison", "Holy", "Shadow", "Thorns", "AShred", "Stun%",
                    "AbilPwr", "Mana%", "SpellRng", "Combo",
                    "FireRes", "IceRes", "LghtRes", "PoisRes", "HolyRes", "ShadRes",
                    "MinSpd", "SwimSpd", "Jump", "FallRed", "Hunger",
                    "Coin%", "Shop%", "Sell%", "Loot%", "XP%",
                    "CbtXP", "MineXP", "FarmXP", "ArcXP", "SurvXP"
                };
                for (int pi = 0; pi < bonusLabels.length; pi++) {
                    String label = bonusLabels[pi];
                    int bpW = this.font.width(label) + 6;
                    if (pickerX + bpW > cx + cw - 4) { pickerX = cx + 56; innerY += 14; }
                    boolean bpHover = mouseX >= pickerX && mouseX < pickerX + bpW && mouseY >= innerY && mouseY < innerY + 13;
                    darkBtn(g, pickerX, innerY, bpW, 13, bpHover);
                    g.drawString(this.font, label, pickerX + 3, innerY + 2, 0xFFE6EDF3, false);
                    this.actionRects.add(new ActionRect(pickerX, innerY, bpW, 13, "__research_add_specific__:" + entry.slot + ":" + pi));
                    pickerX += bpW + 2;
                }
                innerY += 16;
            }

            if (entry.hasRelicData) {
                // Relic Level + Quality line
                g.drawString(this.font, "Relic Lv:" + entry.relicLevel + "/10 Q:" + entry.relicQuality + "/10", cx + 24, innerY, 0xFF58A6FF, false);

                // Level +/- buttons
                int rlBtnX = cx + cw - 170;
                boolean lvUpHover = mouseX >= rlBtnX && mouseX < rlBtnX + 14 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                this.darkIconBtn(g,rlBtnX, innerY - 1, 14, "+", 0xFF3FB950, lvUpHover);
                this.actionRects.add(new ActionRect(rlBtnX, innerY - 1, 14, 14, "__research_set_relic_level__:" + entry.slot + ":" + Math.min(entry.relicLevel + 1, 10)));

                boolean lvDnHover = mouseX >= rlBtnX + 16 && mouseX < rlBtnX + 30 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                this.darkIconBtn(g,rlBtnX + 16, innerY - 1, 14, "-", 0xFFF85149, lvDnHover);
                this.actionRects.add(new ActionRect(rlBtnX + 16, innerY - 1, 14, 14, "__research_set_relic_level__:" + entry.slot + ":" + Math.max(entry.relicLevel - 1, 0)));

                g.drawString(this.font, "Lv", rlBtnX + 34, innerY + 2, 0xFF8B949E, false);

                // Quality +/- buttons
                int rqBtnX = cx + cw - 115;
                boolean qUpHover = mouseX >= rqBtnX && mouseX < rqBtnX + 14 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                this.darkIconBtn(g,rqBtnX, innerY - 1, 14, "+", 0xFF3FB950, qUpHover);
                this.actionRects.add(new ActionRect(rqBtnX, innerY - 1, 14, 14, "__research_set_relic_quality__:" + entry.slot + ":" + Math.min(entry.relicQuality + 1, 10)));

                boolean qDnHover = mouseX >= rqBtnX + 16 && mouseX < rqBtnX + 30 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                this.darkIconBtn(g,rqBtnX + 16, innerY - 1, 14, "-", 0xFFF85149, qDnHover);
                this.actionRects.add(new ActionRect(rqBtnX + 16, innerY - 1, 14, 14, "__research_set_relic_quality__:" + entry.slot + ":" + Math.max(entry.relicQuality - 1, 0)));

                g.drawString(this.font, "Q", rqBtnX + 34, innerY + 2, 0xFF8B949E, false);

                // Max relic button
                int maxRelicX = cx + cw - 68;
                boolean maxRelicHover = mouseX >= maxRelicX && mouseX < maxRelicX + 64 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                darkBtn(g,maxRelicX, innerY - 1, 64, 14, maxRelicHover);
                g.drawString(this.font, "Max Relic", maxRelicX + 6, innerY + 2, 0xFFA371F7, false);
                this.actionRects.add(new ActionRect(maxRelicX, innerY - 1, 64, 14, "research_max_relic:" + entry.slot));
                innerY += 14;

                // XP line with +/- buttons
                int xpNeeded = 100 + entry.relicLevel * 50;
                g.drawString(this.font, "XP: " + entry.relicXp + "/" + xpNeeded, cx + 24, innerY, 0xFF8B949E, false);
                int xpBtnX = cx + cw - 170;
                boolean xpUpHover = mouseX >= xpBtnX && mouseX < xpBtnX + 14 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                this.darkIconBtn(g, xpBtnX, innerY - 1, 14, "+", 0xFF3FB950, xpUpHover);
                this.actionRects.add(new ActionRect(xpBtnX, innerY - 1, 14, 14, "__research_set_relic_xp__:" + entry.slot + ":" + (entry.relicXp + 10)));

                boolean xpDnHover = mouseX >= xpBtnX + 16 && mouseX < xpBtnX + 30 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                this.darkIconBtn(g, xpBtnX + 16, innerY - 1, 14, "-", 0xFFF85149, xpDnHover);
                this.actionRects.add(new ActionRect(xpBtnX + 16, innerY - 1, 14, 14, "__research_set_relic_xp__:" + entry.slot + ":" + Math.max(0, entry.relicXp - 10)));

                // Fill XP button
                int fillXpX = xpBtnX + 34;
                boolean fillXpHover = mouseX >= fillXpX && mouseX < fillXpX + 28 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                darkBtn(g, fillXpX, innerY - 1, 28, 14, fillXpHover);
                g.drawString(this.font, "Fill", fillXpX + 6, innerY + 2, 0xFFD29922, false);
                this.actionRects.add(new ActionRect(fillXpX, innerY - 1, 28, 14, "__research_set_relic_xp__:" + entry.slot + ":" + (xpNeeded - 1)));

                // Clear XP button
                int clearXpX = fillXpX + 30;
                boolean clearXpHover = mouseX >= clearXpX && mouseX < clearXpX + 14 && mouseY >= innerY - 1 && mouseY < innerY + 13;
                this.darkIconBtn(g, clearXpX, innerY - 1, 14, "0", 0xFFF85149, clearXpHover);
                this.actionRects.add(new ActionRect(clearXpX, innerY - 1, 14, 14, "__research_set_relic_xp__:" + entry.slot + ":0"));
                innerY += 14;

                // Action buttons row: Reroll Stats, Max Stats, Reset Relic
                int actionBtnX = cx + 24;
                this.drawActionButton(g, actionBtnX, innerY, 70, 14, "Reroll Stats", mouseX, mouseY, "__research_reroll_relic__:" + entry.slot);
                this.drawActionButton(g, actionBtnX + 74, innerY, 65, 14, "Max Stats", mouseX, mouseY, "__research_max_relic_stats__:" + entry.slot);
                this.drawActionButton(g, actionBtnX + 143, innerY, 70, 14, "Reset Relic", mouseX, mouseY, "__research_reset_relic__:" + entry.slot);
                innerY += 16;

                // Relic abilities
                if (!entry.abilities.isEmpty()) {
                    for (ResearchAbility ab : entry.abilities) {
                        String abLabel = ab.name + " [" + ab.castType + " Lv" + ab.reqLevel + "] Pts:" + ab.points;
                        g.drawString(this.font, abLabel, cx + 28, innerY, 0xFF58A6FF, false);

                        // Description tooltip under ability name
                        if (!ab.description.isEmpty()) {
                            innerY += 11;
                            g.drawString(this.font, "    " + ab.description, cx + 28, innerY, 0xFF6E7681, false);
                        }
                        innerY += 2;

                        // Points +/- buttons
                        int ptBtnX = cx + cw - 60;
                        boolean ptUpHover = mouseX >= ptBtnX && mouseX < ptBtnX + 13 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                        this.darkIconBtn(g,ptBtnX, innerY - 1, 13, "+", 0xFF3FB950, ptUpHover);
                        this.actionRects.add(new ActionRect(ptBtnX, innerY - 1, 13, 13, "__research_set_ability_pts__:" + entry.slot + ":" + ab.name + ":" + (ab.points + 1)));

                        boolean ptDnHover = mouseX >= ptBtnX + 15 && mouseX < ptBtnX + 28 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                        this.darkIconBtn(g,ptBtnX + 15, innerY - 1, 13, "-", 0xFFF85149, ptDnHover);
                        this.actionRects.add(new ActionRect(ptBtnX + 15, innerY - 1, 13, 13, "__research_set_ability_pts__:" + entry.slot + ":" + ab.name + ":" + Math.max(0, ab.points - 1)));

                        innerY += 13;

                        // Cooldown line for non-PASSIVE abilities
                        if (!"PASSIVE".equals(ab.castType)) {
                            boolean cdModified = ab.cooldownTicks != ab.defaultCooldown;
                            String cdStr = cdModified
                                ? "    CD: " + String.format("%.1f", ab.cooldownTicks / 20.0f) + "s (" + ab.cooldownTicks + "t)*"
                                : "    CD: " + String.format("%.1f", ab.cooldownTicks / 20.0f) + "s (" + ab.cooldownTicks + "t)";
                            g.drawString(this.font, cdStr, cx + 28, innerY, cdModified ? 0xFFD29922 : 0xFF8B949E, false);

                            // Cooldown +/- buttons (step: 20 ticks = 1s)
                            int cdBtnX = cx + cw - 80;
                            int cdUp = ab.cooldownTicks + 20;
                            int cdDn = Math.max(0, ab.cooldownTicks - 20);

                            boolean cdUpHover = mouseX >= cdBtnX && mouseX < cdBtnX + 13 && mouseY >= innerY - 1 && mouseY < innerY + 11;
                            this.darkIconBtn(g, cdBtnX, innerY - 1, 12, "+", 0xFF3FB950, cdUpHover);
                            this.actionRects.add(new ActionRect(cdBtnX, innerY - 1, 13, 12, "__research_set_ability_cd__:" + entry.slot + ":" + ab.name + ":" + cdUp));

                            boolean cdDnHover = mouseX >= cdBtnX + 15 && mouseX < cdBtnX + 28 && mouseY >= innerY - 1 && mouseY < innerY + 11;
                            this.darkIconBtn(g, cdBtnX + 15, innerY - 1, 12, "-", 0xFFF85149, cdDnHover);
                            this.actionRects.add(new ActionRect(cdBtnX + 15, innerY - 1, 13, 12, "__research_set_ability_cd__:" + entry.slot + ":" + ab.name + ":" + cdDn));

                            // Reset button
                            if (cdModified) {
                                boolean cdRstHover = mouseX >= cdBtnX + 32 && mouseX < cdBtnX + 55 && mouseY >= innerY - 1 && mouseY < innerY + 11;
                                darkBtn(g, cdBtnX + 32, innerY - 1, 23, 12, cdRstHover);
                                g.drawString(this.font, "Rst", cdBtnX + 35, innerY + 1, 0xFFD29922, false);
                                this.actionRects.add(new ActionRect(cdBtnX + 32, innerY - 1, 23, 12, "__research_set_ability_cd__:" + entry.slot + ":" + ab.name + ":" + ab.defaultCooldown));
                            }

                            innerY += 12;
                        }

                        for (ResearchAbilityStat st : ab.stats) {
                            String statStr = "    " + st.name + ": " + String.format("%.2f", st.base) + " (=" + String.format("%.2f", st.computed) + ")";
                            g.drawString(this.font, statStr, cx + 28, innerY, 0xFF8B949E, false);

                            // Stat +/- buttons
                            int stBtnX = cx + cw - 60;
                            double increment = Math.max(0.01, (st.max - st.min) * 0.1);
                            double newUp = st.base + increment;
                            double newDn = st.base - increment;

                            boolean stUpHover = mouseX >= stBtnX && mouseX < stBtnX + 13 && mouseY >= innerY - 1 && mouseY < innerY + 11;
                            this.darkIconBtn(g,stBtnX, innerY - 1, 12, "+", 0xFF3FB950, stUpHover);
                            this.actionRects.add(new ActionRect(stBtnX, innerY - 1, 13, 12, "__research_set_relic_stat__:" + entry.slot + ":" + ab.name + ":" + st.name + ":" + String.format("%.4f", newUp)));

                            boolean stDnHover = mouseX >= stBtnX + 15 && mouseX < stBtnX + 28 && mouseY >= innerY - 1 && mouseY < innerY + 11;
                            this.darkIconBtn(g,stBtnX + 15, innerY - 1, 12, "-", 0xFFF85149, stDnHover);
                            this.actionRects.add(new ActionRect(stBtnX + 15, innerY - 1, 13, 12, "__research_set_relic_stat__:" + entry.slot + ":" + ab.name + ":" + st.name + ":" + String.format("%.4f", newDn)));

                            // Max button
                            boolean stMaxHover = mouseX >= stBtnX + 32 && mouseX < stBtnX + 55 && mouseY >= innerY - 1 && mouseY < innerY + 11;
                            darkBtn(g,stBtnX + 32, innerY - 1, 23, 12, stMaxHover);
                            g.drawString(this.font, "Max", stBtnX + 35, innerY + 1, 0xFFD29922, false);
                            this.actionRects.add(new ActionRect(stBtnX + 32, innerY - 1, 23, 12, "__research_set_relic_stat__:" + entry.slot + ":" + ab.name + ":" + st.name + ":" + String.format("%.4f", st.max)));

                            innerY += 12;
                        }
                    }
                }
            }

            // Init Relic button for uninitialized relic items
            if (entry.isRelicItem && !entry.hasRelicData) {
                int initRelicBtnX = cx + 24;
                boolean initRelicHover = mouseX >= initRelicBtnX && mouseX < initRelicBtnX + 76 && mouseY >= innerY && mouseY < innerY + 14;
                darkBtn(g, initRelicBtnX, innerY, 76, 14, initRelicHover);
                g.drawString(this.font, "Init Relic", initRelicBtnX + 8, innerY + 3, 0xFFA371F7, false);
                this.actionRects.add(new ActionRect(initRelicBtnX, innerY, 76, 14, "__research_init_relic__:" + entry.slot));
                innerY += 18;
            }

            if (!entry.hasWeaponStats && !entry.hasRelicData && !entry.isRelicItem) {
                // Init Weapon button for plain items
                int initBtnX = cx + 24;
                boolean initWepHover = mouseX >= initBtnX && mouseX < initBtnX + 80 && mouseY >= innerY && mouseY < innerY + 14;
                darkBtn(g, initBtnX, innerY, 80, 14, initWepHover);
                g.drawString(this.font, "Init Weapon", initBtnX + 6, innerY + 3, 0xFFE6EDF3, false);
                this.actionRects.add(new ActionRect(initBtnX, innerY, 80, 14, "__research_init_weapon__:" + entry.slot));

                // Init Armor button
                int initArmBtnX = initBtnX + 84;
                boolean initArmHover = mouseX >= initArmBtnX && mouseX < initArmBtnX + 76 && mouseY >= innerY && mouseY < innerY + 14;
                darkBtn(g, initArmBtnX, innerY, 76, 14, initArmHover);
                g.drawString(this.font, "Init Armor", initArmBtnX + 6, innerY + 3, 0xFF58A6FF, false);
                this.actionRects.add(new ActionRect(initArmBtnX, innerY, 76, 14, "__research_init_armor__:" + entry.slot));
                innerY += 18;
            }

            // Enchantments section
            if (!entry.enchantments.isEmpty()) {
                g.drawString(this.font, "Enchantments:", cx + 24, innerY, 0xFFA371F7, false);

                // Clear all button
                int clearEnchX = cx + cw - 80;
                boolean clearEnchHover = mouseX >= clearEnchX && mouseX < clearEnchX + 75 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                darkBtn(g, clearEnchX, innerY - 1, 75, 13, clearEnchHover);
                g.drawString(this.font, "Clear All", clearEnchX + 12, innerY + 1, 0xFFE6EDF3, false);
                this.actionRects.add(new ActionRect(clearEnchX, innerY - 1, 75, 13, "__research_clear_enchants__:" + entry.slot));
                innerY += 13;

                for (int ei = 0; ei < entry.enchantments.size(); ei++) {
                    ResearchEnchant ench = entry.enchantments.get(ei);
                    String lvlLabel = ench.level > 10 ? String.valueOf(ench.level) : toRoman(ench.level);
                    g.drawString(this.font, "  " + ench.name + " " + lvlLabel, cx + 24, innerY, 0xFFC9D1D9, false);

                    // Level +1 / -1 / +10 / Max / Remove buttons
                    int eBtnX = cx + cw - 130;
                    boolean eUpHover = mouseX >= eBtnX && mouseX < eBtnX + 13 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                    this.darkIconBtn(g, eBtnX, innerY - 1, 13, "-", 0xFFD29922, eUpHover);
                    this.actionRects.add(new ActionRect(eBtnX, innerY - 1, 13, 13, "__research_set_enchant__:" + entry.slot + ":" + ench.id + ":" + Math.max(1, ench.level - 1)));

                    int ePlus1X = eBtnX + 15;
                    boolean ePlus1Hover = mouseX >= ePlus1X && mouseX < ePlus1X + 13 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                    this.darkIconBtn(g, ePlus1X, innerY - 1, 13, "+", 0xFF3FB950, ePlus1Hover);
                    this.actionRects.add(new ActionRect(ePlus1X, innerY - 1, 13, 13, "__research_set_enchant__:" + entry.slot + ":" + ench.id + ":" + Math.min(255, ench.level + 1)));

                    int ePlus10X = ePlus1X + 15;
                    int plus10W = 22;
                    boolean ePlus10Hover = mouseX >= ePlus10X && mouseX < ePlus10X + plus10W && mouseY >= innerY - 1 && mouseY < innerY + 12;
                    darkBtn(g, ePlus10X, innerY - 1, plus10W, 13, ePlus10Hover);
                    g.drawString(this.font, "+10", ePlus10X + 2, innerY + 2, 0xFF3FB950, false);
                    this.actionRects.add(new ActionRect(ePlus10X, innerY - 1, plus10W, 13, "__research_set_enchant__:" + entry.slot + ":" + ench.id + ":" + Math.min(255, ench.level + 10)));

                    int eMaxX = ePlus10X + plus10W + 2;
                    int maxW = 26;
                    boolean eMaxHover = mouseX >= eMaxX && mouseX < eMaxX + maxW && mouseY >= innerY - 1 && mouseY < innerY + 12;
                    darkBtn(g, eMaxX, innerY - 1, maxW, 13, eMaxHover);
                    g.drawString(this.font, "MAX", eMaxX + 3, innerY + 2, 0xFFDA3633, false);
                    this.actionRects.add(new ActionRect(eMaxX, innerY - 1, maxW, 13, "__research_set_enchant__:" + entry.slot + ":" + ench.id + ":255"));

                    // Remove button
                    int eRemX = eMaxX + maxW + 2;
                    boolean eRemHover = mouseX >= eRemX && mouseX < eRemX + 13 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                    this.darkIconBtn(g, eRemX, innerY - 1, 13, "X", 0xFFF85149, eRemHover);
                    this.actionRects.add(new ActionRect(eRemX, innerY - 1, 13, 13, "__research_remove_enchant__:" + entry.slot + ":" + ench.id));

                    innerY += 13;
                }
            }

            // Add Enchant picker - all enchantments from registry
            innerY += 4; // spacing to prevent text overlap with enchant list above
            g.drawString(this.font, "  +Ench:", cx + 24, innerY + 2, 0xFFA371F7, false);
            int epX = cx + 80;
            if (cachedEnchantList == null && this.mc.level != null) {
                cachedEnchantList = new ArrayList<>();
                var enchReg = this.mc.level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                enchReg.listElements().forEach(holder -> {
                    String fullId = holder.key().identifier().toString();
                    String path = holder.key().identifier().getPath();
                    String label = path.replace('_', ' ');
                    if (label.length() > 12) {
                        // Abbreviate: take first letter of each word, capitalize
                        String[] words = label.split(" ");
                        StringBuilder sb = new StringBuilder();
                        for (String w : words) {
                            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0)));
                            if (words.length <= 2 && w.length() > 1) sb.append(w.substring(1, Math.min(w.length(), 4)));
                        }
                        label = sb.toString();
                    } else {
                        // Title case first letter
                        label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
                    }
                    cachedEnchantList.add(new String[]{label, fullId});
                });
                cachedEnchantList.sort((a, b) -> a[0].compareToIgnoreCase(b[0]));
            }
            if (cachedEnchantList != null) {
                for (String[] ep : cachedEnchantList) {
                    int epW = this.font.width(ep[0]) + 6;
                    if (epX + epW > cx + cw - 4) { epX = cx + 80; innerY += 14; }
                    boolean epHover = mouseX >= epX && mouseX < epX + epW && mouseY >= innerY && mouseY < innerY + 13;
                    darkBtn(g, epX, innerY, epW, 13, epHover);
                    g.drawString(this.font, ep[0], epX + 3, innerY + 2, 0xFFE6EDF3, false);
                    this.actionRects.add(new ActionRect(epX, innerY, epW, 13, "__research_add_enchant__:" + entry.slot + ":" + ep[1] + ":1"));
                    epX += epW + 2;
                }
            }
            innerY += 16;

            // Weapon Skill Editor - add/swap/remove combat skills on this item
            if (entry.hasWeaponStats) {
                darkDivider(g, cx + 24, innerY, cw - 48);
                innerY += 4;
                g.drawString(this.font, "Set Skill 1:", cx + 24, innerY, 0xFF58A6FF, false);
                // Reset to default button
                int rstBtnX = cx + cw - 80;
                boolean rstHover = mouseX >= rstBtnX && mouseX < rstBtnX + 75 && mouseY >= innerY - 1 && mouseY < innerY + 12;
                darkBtn(g, rstBtnX, innerY - 1, 75, 13, rstHover);
                g.drawString(this.font, "Reset Def.", rstBtnX + 10, innerY + 1, 0xFFD29922, false);
                this.actionRects.add(new ActionRect(rstBtnX, innerY - 1, 75, 13, "__research_clear_weapon_skills__:" + entry.slot));
                innerY += 13;
                // S1 row - flowing layout with all available skills from registry
                java.util.List<String> allSkills = new java.util.ArrayList<>(com.ultra.megamod.feature.relics.weapons.RpgWeaponRegistry.getAllSkillNames());
                java.util.Collections.sort(allSkills);
                int skx = cx + 24;
                for (String skillName : allSkills) {
                    String shortLabel = skillName.length() > 12 ? skillName.substring(0, 11) + "." : skillName;
                    int skW = this.font.width(shortLabel) + 4;
                    if (skx + skW > cx + cw - 4) { skx = cx + 24; innerY += 12; }
                    boolean skHover = mouseX >= skx && mouseX < skx + skW && mouseY >= innerY && mouseY < innerY + 11;
                    darkBtn(g, skx, innerY, skW, 11, skHover);
                    g.drawString(this.font, shortLabel, skx + 2, innerY + 2, 0xFFE6EDF3, false);
                    this.actionRects.add(new ActionRect(skx, innerY, skW, 11, "research_set_weapon_skill:" + entry.slot + ":0:" + skillName));
                    skx += skW + 1;
                }
                innerY += 14;

                g.drawString(this.font, "Set Skill 2:", cx + 24, innerY, 0xFFD29922, false);
                innerY += 12;
                skx = cx + 24;
                for (String skillName : allSkills) {
                    String shortLabel = skillName.length() > 12 ? skillName.substring(0, 11) + "." : skillName;
                    int skW = this.font.width(shortLabel) + 4;
                    if (skx + skW > cx + cw - 4) { skx = cx + 24; innerY += 12; }
                    boolean skHover = mouseX >= skx && mouseX < skx + skW && mouseY >= innerY && mouseY < innerY + 11;
                    darkBtn(g, skx, innerY, skW, 11, skHover);
                    g.drawString(this.font, shortLabel, skx + 2, innerY + 2, 0xFFE6EDF3, false);
                    this.actionRects.add(new ActionRect(skx, innerY, skW, 11, "research_set_weapon_skill:" + entry.slot + ":1:" + skillName));
                    skx += skW + 1;
                }
                innerY += 14;
            } else {
                darkDivider(g, cx + 24, innerY, cw - 48);
                innerY += 4;
            }

            drawY += rowH;
        }

        g.disableScissor();

        // Scrollbar
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.researchScroll / (float) maxScroll : 0.0f;
            darkScrollbar(g,this.contentRight - 6 - 6, cy, visibleH, progress);
        }
    }

    /**
     * Apply a suggestion to the current input text.
     * If the suggestion completes the last token (starts with it), replace it.
     * If it's a next-argument suggestion, append it after a space.
     */
    private static String applySuggestion(String current, String suggestion) {
        int lastSpace = current.lastIndexOf(' ');
        String lastToken = lastSpace >= 0 ? current.substring(lastSpace + 1) : current;
        String prefix = lastSpace >= 0 ? current.substring(0, lastSpace + 1) : "";

        if (lastToken.isEmpty() || suggestion.toLowerCase().startsWith(lastToken.toLowerCase())) {
            // Completing/replacing the current token
            return prefix + suggestion;
        } else {
            // Next-arg suggestion — append after current text with space
            return current + " " + suggestion;
        }
    }

    private void parseSuggestions(String json) {
        this.suggestions.clear();
        this.selectedSuggestion = -1;
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement el : arr) {
                this.suggestions.add(el.getAsString());
            }
        } catch (Exception e) {}
    }

    private void parseInventoryData(String json) {
        this.researchInventory.clear();
        this.researchSelectedSlot = -1;
        try {
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                List<ResearchBonus> bonusList = new ArrayList<>();
                if (obj.has("bonuses")) {
                    JsonArray bonusArr = obj.getAsJsonArray("bonuses");
                    for (JsonElement bEl : bonusArr) {
                        JsonObject bObj = bEl.getAsJsonObject();
                        bonusList.add(new ResearchBonus(
                            bObj.has("name") ? bObj.get("name").getAsString() : "?",
                            bObj.has("value") ? bObj.get("value").getAsDouble() : 0.0,
                            bObj.has("percent") && bObj.get("percent").getAsBoolean(),
                            bObj.has("attr") ? bObj.get("attr").getAsString() : ""
                        ));
                    }
                }
                List<ResearchAbility> abilityList = new ArrayList<>();
                if (obj.has("abilities")) {
                    JsonArray abArr = obj.getAsJsonArray("abilities");
                    for (JsonElement abEl : abArr) {
                        JsonObject abObj = abEl.getAsJsonObject();
                        List<ResearchAbilityStat> statList = new ArrayList<>();
                        if (abObj.has("stats")) {
                            JsonArray stArr = abObj.getAsJsonArray("stats");
                            for (JsonElement stEl : stArr) {
                                JsonObject stObj = stEl.getAsJsonObject();
                                statList.add(new ResearchAbilityStat(
                                    stObj.has("name") ? stObj.get("name").getAsString() : "?",
                                    stObj.has("base") ? stObj.get("base").getAsDouble() : 0,
                                    stObj.has("computed") ? stObj.get("computed").getAsDouble() : 0,
                                    stObj.has("min") ? stObj.get("min").getAsDouble() : 0,
                                    stObj.has("max") ? stObj.get("max").getAsDouble() : 0
                                ));
                            }
                        }
                        abilityList.add(new ResearchAbility(
                            abObj.has("name") ? abObj.get("name").getAsString() : "?",
                            abObj.has("desc") ? abObj.get("desc").getAsString() : "",
                            abObj.has("reqLevel") ? abObj.get("reqLevel").getAsInt() : 0,
                            abObj.has("castType") ? abObj.get("castType").getAsString() : "?",
                            abObj.has("points") ? abObj.get("points").getAsInt() : 0,
                            abObj.has("cooldownTicks") ? abObj.get("cooldownTicks").getAsInt() : 0,
                            abObj.has("defaultCooldown") ? abObj.get("defaultCooldown").getAsInt() : 0,
                            statList
                        ));
                    }
                }
                List<ResearchEnchant> enchantList = new ArrayList<>();
                if (obj.has("enchantments")) {
                    JsonArray eArr = obj.getAsJsonArray("enchantments");
                    for (JsonElement eEl : eArr) {
                        JsonObject eObj = eEl.getAsJsonObject();
                        enchantList.add(new ResearchEnchant(
                            eObj.has("id") ? eObj.get("id").getAsString() : "",
                            eObj.has("name") ? eObj.get("name").getAsString() : "?",
                            eObj.has("level") ? eObj.get("level").getAsInt() : 0
                        ));
                    }
                }
                List<ResearchWeaponSkill> wSkillList = new ArrayList<>();
                if (obj.has("weaponSkills")) {
                    JsonArray wsArr = obj.getAsJsonArray("weaponSkills");
                    for (JsonElement wsEl : wsArr) {
                        JsonObject wsObj = wsEl.getAsJsonObject();
                        wSkillList.add(new ResearchWeaponSkill(
                            wsObj.has("name") ? wsObj.get("name").getAsString() : "?",
                            wsObj.has("desc") ? wsObj.get("desc").getAsString() : "",
                            wsObj.has("cooldown") ? wsObj.get("cooldown").getAsInt() : 0,
                            wsObj.has("cooldownSec") ? wsObj.get("cooldownSec").getAsFloat() : 0,
                            wsObj.has("defaultCooldown") ? wsObj.get("defaultCooldown").getAsInt() : 0
                        ));
                    }
                }
                String customName = obj.has("customName") ? obj.get("customName").getAsString() : "";
                List<String> loreLines = new ArrayList<>();
                if (obj.has("lore")) {
                    JsonArray loreArr = obj.getAsJsonArray("lore");
                    for (JsonElement loreEl : loreArr) {
                        loreLines.add(loreEl.getAsString());
                    }
                }
                ResearchInvEntry entry = new ResearchInvEntry(
                    obj.has("slot") ? obj.get("slot").getAsInt() : 0,
                    obj.has("itemId") ? obj.get("itemId").getAsString() : "",
                    obj.has("displayName") ? obj.get("displayName").getAsString() : "?",
                    obj.has("hasWeaponStats") && obj.get("hasWeaponStats").getAsBoolean(),
                    obj.has("rarityName") ? obj.get("rarityName").getAsString() : "",
                    obj.has("rarityColor") ? obj.get("rarityColor").getAsInt() : -1,
                    obj.has("baseDamage") ? obj.get("baseDamage").getAsFloat() : 0,
                    obj.has("bonusCount") ? obj.get("bonusCount").getAsInt() : 0,
                    obj.has("hasRelicData") && obj.get("hasRelicData").getAsBoolean(),
                    obj.has("relicLevel") ? obj.get("relicLevel").getAsInt() : 0,
                    obj.has("relicQuality") ? obj.get("relicQuality").getAsInt() : 0,
                    obj.has("relicXp") ? obj.get("relicXp").getAsInt() : 0,
                    bonusList,
                    abilityList,
                    enchantList,
                    wSkillList,
                    customName,
                    loreLines,
                    obj.has("isRelicItem") && obj.get("isRelicItem").getAsBoolean()
                );
                this.researchInventory.add(entry);
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse admin inventory data", e);
        }
    }

    private void drawSectionHeader(GuiGraphics g, String text, int x, int y, int width) {
        g.drawString(this.font, text, x, y, 0xFF58A6FF, false);
        int textW = this.font.width(text);
        g.fill(x, y + 11, x + width, y + 12, 0xFF21262D);
    }

    private void renderAnimation(GuiGraphics g) {
        int chars = Math.min(this.animationTicks / 2, WELCOME_TEXT.length());
        String visible = WELCOME_TEXT.substring(0, chars);
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int cy = centerY - 4;
        int panelW = Math.max(this.font.width(WELCOME_TEXT) + 40, 200);
        int panelH = 29;
        int panelX = centerX - panelW / 2;
        int panelY = cy - 10;
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF161B22);
        g.fill(panelX, panelY, panelX + panelW, panelY + 1, 0xFF30363D);
        g.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, 0xFF30363D);
        g.fill(panelX, panelY, panelX + 1, panelY + panelH, 0xFF30363D);
        g.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, 0xFF30363D);
        g.drawCenteredString(this.font, visible, centerX, cy, 0xFF58A6FF);
        if (this.animationTicks / 10 % 2 == 0) {
            int tw = this.font.width(visible);
            g.drawString(this.font, "_", centerX + tw / 2 + 1, cy, 0xFF58A6FF);
        }
    }

    private void renderDashboard(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        Objects.requireNonNull(this.font);
        int lh = 9 + 4;
        int contentW = this.contentRight - this.contentLeft - 12;
        this.drawSectionHeader(g, "Server Info", x, y, contentW);
        int cardW = Math.min(280, contentW);
        int cardH = 5 * lh + 12;
        darkCard(g,x, y += lh + 4, cardW, cardH);
        int tx = x + 8;
        int ty = y + 6;
        String serverName = this.mc.getCurrentServer() != null ? this.mc.getCurrentServer().name : "Singleplayer";
        g.drawString(this.font, "Server:", tx, ty, 0xFF8B949E, false);
        g.drawString(this.font, serverName, tx + 60, ty, 0xFFE6EDF3, false);
        int playerCount = this.mc.getConnection() != null ? this.mc.getConnection().getOnlinePlayers().size() : 0;
        g.drawString(this.font, "Players:", tx, ty += lh, 0xFF8B949E, false);
        g.drawString(this.font, String.valueOf(playerCount), tx + 60, ty, 0xFF58A6FF, false);
        String playerName = this.mc.player != null ? this.mc.player.getGameProfile().name() : "Unknown";
        g.drawString(this.font, "You:", tx, ty += lh, 0xFF8B949E, false);
        g.drawString(this.font, playerName, tx + 60, ty, 0xFFE6EDF3, false);
        String gamemode = this.mc.gameMode != null ? this.mc.gameMode.getPlayerMode().getName() : "Unknown";
        g.drawString(this.font, "Mode:", tx, ty += lh, 0xFF8B949E, false);
        g.drawString(this.font, gamemode, tx + 60, ty, 0xFFE6EDF3, false);
        ty += lh;
        if (this.mc.level != null) {
            g.drawString(this.font, "World:", tx, ty, 0xFF8B949E, false);
            g.drawString(this.font, "Loaded", tx + 60, ty, 0xFF3FB950, false);
        }
        this.drawSectionHeader(g, "Quick Actions", x, y += cardH + 6 + 4, contentW);
        int btnW = 90;
        int btnH = 20;
        int gap = 4;
        this.drawActionButton(g, x, y += lh + 4, btnW, btnH, "Day", mouseX, mouseY, "time set day");
        this.drawActionButton(g, x + btnW + gap, y, btnW, btnH, "Night", mouseX, mouseY, "time set night");
        this.drawActionButton(g, x + (btnW + gap) * 2, y, btnW + 20, btnH, "Clear Weather", mouseX, mouseY, "weather clear");
        this.drawActionButton(g, x, y += btnH + gap, btnW, btnH, "Survival", mouseX, mouseY, "gamemode survival @s");
        this.drawActionButton(g, x + btnW + gap, y, btnW, btnH, "Creative", mouseX, mouseY, "gamemode creative @s");
        this.drawActionButton(g, x + (btnW + gap) * 2, y, btnW, btnH, "Adventure", mouseX, mouseY, "gamemode adventure @s");
        this.drawActionButton(g, x + (btnW + gap) * 3, y, btnW, btnH, "Spectator", mouseX, mouseY, "gamemode spectator @s");
        this.drawActionButton(g, x, y += btnH + gap, btnW, btnH, "Heal Self", mouseX, mouseY, "__admin_quick__:heal_self");
        this.drawActionButton(g, x + btnW + gap, y, btnW, btnH, "Feed Self", mouseX, mouseY, "__admin_quick__:feed_self");
        this.drawActionButton(g, x + (btnW + gap) * 2, y, btnW + 20, btnH, "Kill Hostiles", mouseX, mouseY, "kill @e[type=!player,type=!item]");
        this.drawActionButton(g, x, y += btnH + gap, btnW + 20, btnH, "Fill Museum", mouseX, mouseY, "__fill_museum__");
        this.drawActionButton(g, x + btnW + 20 + gap, y, btnW + 40, btnH, "Complete Advancements", mouseX, mouseY, "__complete_advancements__");
        this.drawActionButton(g, x, y += btnH + gap, btnW + 30, btnH, "Unlock All Wiki", mouseX, mouseY, "__unlock_wiki__");
        this.drawActionButton(g, x + btnW + 30 + gap, y, btnW + 20, btnH, "Max Skill Tree", mouseX, mouseY, "__max_skill_tree__");
        this.drawActionButton(g, x, y += btnH + gap, btnW, btnH, "TP Spawn", mouseX, mouseY, "tp @s ~ ~ ~ 0 0");
        this.drawActionButton(g, x + btnW + gap, y, btnW, btnH, "TP 0,0", mouseX, mouseY, "tp @s 0 100 0");
        this.drawActionButton(g, x + (btnW + gap) * 2, y, btnW + 20, btnH, "Clear Effects", mouseX, mouseY, "effect clear @s");
        this.drawActionButton(g, x, y += btnH + gap, btnW, btnH, this.godModeActive ? "God OFF" : "God Mode", mouseX, mouseY, "__toggle_god__");
        this.drawActionButton(g, x + btnW + gap, y, btnW, btnH, "Fly Speed", mouseX, mouseY, "__fly_speed__");
        this.drawActionButton(g, x + (btnW + gap) * 2, y, btnW + 20, btnH, "Force Save", mouseX, mouseY, "save-all");
        this.drawActionButton(g, x, y += btnH + gap, btnW + 20, btnH, "Clear Inv", mouseX, mouseY, "clear @s");
        this.drawActionButton(g, x + btnW + 20 + gap, y, btnW, btnH, "XP +30Lv", mouseX, mouseY, "experience add @s 30 levels");
        this.drawActionButton(g, x + (btnW + gap) * 2 + 20, y, btnW + 20, btnH, "Set Spawn", mouseX, mouseY, "setworldspawn ~ ~ ~");
        this.drawActionButton(g, x, y += btnH + gap, btnW + 30, btnH, "Admin Weapon", mouseX, mouseY, "__spawn_admin_weapon__");
        this.drawActionButton(g, x + btnW + 30 + gap, y, btnW + 30, btnH, "Admin Armor", mouseX, mouseY, "__spawn_admin_armor__");
        y += btnH + gap + 10;
        this.drawSectionHeader(g, "Broadcast", x, y, contentW);
        y += lh + 4;
        if (this.broadcastBox != null) {
            this.broadcastBox.setPosition(x, y);
        }
        this.drawActionButton(g, x + 210, y, 70, 18, "Send", mouseX, mouseY, "__broadcast__");
        if (!this.adminMessages.isEmpty()) {
            y += 22;
            String lastMsg = this.adminMessages.get(this.adminMessages.size() - 1);
            g.drawString(this.font, lastMsg, x, y, 0xFF3FB950, false);
            y += lh;
        }
        y += lh + 10;
        this.drawSectionHeader(g, "Performance", x, y, contentW);
        y += lh + 4;
        int pcW = Math.min(340, contentW);
        int pcH = 7 * lh + 16;
        darkCard(g,x, y, pcW, pcH);
        int ppx = x + 8;
        int ppy = y + 6;
        int tCol = this.perfTps > 18.0 ? 0xFF3FB950 : (this.perfTps > 15.0 ? 0xFFD29922 : 0xFFF85149);
        g.drawString(this.font, "TPS:", ppx, ppy, 0xFF8B949E, false);
        g.drawString(this.font, String.format("%.1f / 20.0", this.perfTps), ppx + 50, ppy, tCol, false);
        darkProgressBar(g,ppx + 130, ppy - 1, pcW - 146, 10, (float)(this.perfTps / 20.0), tCol);
        ppy += lh;
        int mCol = this.perfMspt < 50.0 ? 0xFF3FB950 : (this.perfMspt < 100.0 ? 0xFFD29922 : 0xFFF85149);
        g.drawString(this.font, "MSPT:", ppx, ppy, 0xFF8B949E, false);
        g.drawString(this.font, String.format("%.1f ms", this.perfMspt), ppx + 50, ppy, mCol, false);
        ppy += lh;
        float mProg = this.perfMaxMem > 0 ? (float) this.perfMem / (float) this.perfMaxMem : 0;
        int mmCol = mProg < 0.7f ? 0xFF3FB950 : (mProg < 0.9f ? 0xFFD29922 : 0xFFF85149);
        g.drawString(this.font, "Memory:", ppx, ppy, 0xFF8B949E, false);
        g.drawString(this.font, this.perfMem + " / " + this.perfMaxMem + " MB", ppx + 50, ppy, mmCol, false);
        ppy += lh;
        darkProgressBar(g,ppx, ppy - 1, pcW - 24, 10, mProg, mmCol);
        ppy += lh;
        g.drawString(this.font, "Entities:", ppx, ppy, 0xFF8B949E, false);
        g.drawString(this.font, String.valueOf(this.perfEntities), ppx + 60, ppy, 0xFFE6EDF3, false);
        g.drawString(this.font, "Chunks:", ppx + 120, ppy, 0xFF8B949E, false);
        g.drawString(this.font, String.valueOf(this.perfChunks), ppx + 180, ppy, 0xFFE6EDF3, false);
        ppy += lh;
        g.drawString(this.font, "Players:", ppx, ppy, 0xFF8B949E, false);
        g.drawString(this.font, String.valueOf(this.perfPlayers), ppx + 60, ppy, 0xFFE6EDF3, false);
        // --- TPS Graph ---
        y += pcH + 6;
        if (!this.tpsHistory.isEmpty()) {
            this.drawSectionHeader(g, "TPS History (last 3 min)", x, y, contentW);
            y += lh + 4;
            int graphW = Math.min(340, contentW);
            int graphH = 50;
            darkCard(g, x, y, graphW, graphH + 8);
            int gx = x + 4;
            int gy = y + 4;
            // Grid lines at TPS 10 and 20
            int y20 = gy;
            int y10 = gy + graphH / 2;
            int yBot = gy + graphH;
            g.fill(gx, y20, gx + graphW - 8, y20 + 1, 0x20FFFFFF);
            g.fill(gx, y10, gx + graphW - 8, y10 + 1, 0x20FFFFFF);
            g.drawString(this.font, "20", gx - 2, y20 - 1, 0xFF555566, false);
            g.drawString(this.font, "10", gx - 2, y10 - 1, 0xFF555566, false);
            // Draw TPS line
            int dataSize = this.tpsHistory.size();
            float step = (float)(graphW - 8) / Math.max(1, dataSize - 1);
            for (int i = 1; i < dataSize; i++) {
                double tps1 = this.tpsHistory.get(i - 1)[0];
                double tps2 = this.tpsHistory.get(i)[0];
                int x1 = gx + (int)((i - 1) * step);
                int x2 = gx + (int)(i * step);
                int py1 = yBot - (int)(Math.min(tps1, 20) / 20.0 * graphH);
                int py2 = yBot - (int)(Math.min(tps2, 20) / 20.0 * graphH);
                int lineCol = tps2 >= 18 ? 0xFF3FB950 : (tps2 >= 15 ? 0xFFD29922 : 0xFFF85149);
                // Simple line: draw 2px wide
                g.fill(Math.min(x1, x2), Math.min(py1, py2), Math.max(x1, x2) + 1, Math.max(py1, py2) + 2, lineCol);
            }
            // Memory line (dimmer, blue)
            for (int i = 1; i < dataSize; i++) {
                double mem1 = this.tpsHistory.get(i - 1)[1];
                double mem2 = this.tpsHistory.get(i)[1];
                int x1 = gx + (int)((i - 1) * step);
                int x2 = gx + (int)(i * step);
                int py1 = yBot - (int)(mem1 / 100.0 * graphH);
                int py2 = yBot - (int)(mem2 / 100.0 * graphH);
                g.fill(Math.min(x1, x2), Math.min(py1, py2), Math.max(x1, x2) + 1, Math.max(py1, py2) + 1, 0x6058A6FF);
            }
            // Legend
            g.fill(gx + graphW - 80, gy + 2, gx + graphW - 74, gy + 5, 0xFF3FB950);
            g.drawString(this.font, "TPS", gx + graphW - 72, gy, 0xFF8B949E, false);
            g.fill(gx + graphW - 45, gy + 2, gx + graphW - 39, gy + 5, 0x6058A6FF);
            g.drawString(this.font, "MEM", gx + graphW - 37, gy, 0xFF8B949E, false);
            y += graphH + 14;
        }

        // --- System Warnings ---
        if (!this.perfIssues.isEmpty()) {
            this.drawSectionHeader(g, "Warnings", x, y, contentW);
            y += lh + 4;
            for (String[] issue : this.perfIssues) {
                boolean isError = "ERROR".equals(issue[0]);
                int issCol = isError ? 0xFFF85149 : 0xFFD29922;
                String prefix = isError ? "\u26A0 ERROR: " : "\u26A0 WARN: ";
                g.drawString(this.font, prefix + issue[1], x + 4, y, issCol, false);
                y += lh;
            }
            y += 4;
        }

        // Systems Overview card
        this.drawSectionHeader(g, "Active Systems", x, y, contentW);
        y += lh + 4;
        int sysW = Math.min(340, contentW);
        int sysH = 4 * lh + 12;
        darkCard(g, x, y, sysW, sysH);
        int spx = x + 8;
        int spy = y + 6;
        g.drawString(this.font, "Dungeons:", spx, spy, 0xFF8B949E, false);
        g.drawString(this.font, String.valueOf(this.perfActiveDungeons) + " active", spx + 70, spy, this.perfActiveDungeons > 0 ? 0xFF58A6FF : 0xFF8B949E, false);
        g.drawString(this.font, "Arenas:", spx + 160, spy, 0xFF8B949E, false);
        g.drawString(this.font, String.valueOf(this.perfActiveArenas) + " active", spx + 220, spy, this.perfActiveArenas > 0 ? 0xFF3FB950 : 0xFF8B949E, false);
        spy += lh;
        g.drawString(this.font, "Bounties:", spx, spy, 0xFF8B949E, false);
        g.drawString(this.font, String.valueOf(this.perfActiveBounties) + " available", spx + 70, spy, this.perfActiveBounties > 0 ? 0xFFD29922 : 0xFF8B949E, false);
        spy += lh;
        g.drawString(this.font, "Features:", spx, spy, 0xFF8B949E, false);
        g.drawString(this.font, "Arena \u2022 Bounties \u2022 Combos \u2022 NG+ \u2022 Variants", spx + 70, spy, 0xFF58A6FF, false);
    }

    private void renderPlayers(GuiGraphics g, int mouseX, int mouseY) {
        if (this.selectedPlayerUUID != null) {
            this.renderPlayerDetail(g, mouseX, mouseY);
            return;
        }
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        int contentW = this.contentRight - this.contentLeft - 12;
        this.drawSectionHeader(g, "Online Players (click for details)", x, y, contentW);
        Objects.requireNonNull(this.font);
        y += 9 + 6;
        if (this.mc.getConnection() == null) {
            g.drawString(this.font, "No connection.", x, y, 0xFFF85149, false);
            return;
        }
        Collection<PlayerInfo> players = this.mc.getConnection().getOnlinePlayers();
        int startY = y;
        int visibleH = this.contentBottom - startY - 6;
        int maxVisible = visibleH / 26;
        int totalPlayers = players.size();
        int maxScroll = Math.max(0, totalPlayers - maxVisible);
        this.playerScroll = Math.max(0, Math.min(this.playerScroll, maxScroll));
        g.enableScissor(this.contentLeft, startY, this.contentRight, this.contentBottom);
        int idx = 0;
        for (PlayerInfo info : players) {
            if (idx < this.playerScroll) { ++idx; continue; }
            if (idx - this.playerScroll >= maxVisible + 1) break;
            int rowY = startY + (idx - this.playerScroll) * 26;
            String pName = info.getProfile().name();
            darkRowBg(g,x, rowY, contentW, 26, idx % 2 == 0);
            boolean hovered = mouseX >= x && mouseX <= this.contentRight - 6 && mouseY >= rowY && mouseY < rowY + 26;
            if (hovered) {
                g.fill(x, rowY, this.contentRight - 6, rowY + 26, 419419904);
            }
            g.fill(x + 4, rowY + 8, x + 6, rowY + 10, 0xFF58A6FF);
            g.drawString(this.font, pName, x + 10, rowY + 4, 0xFFE6EDF3, false);
            // Show coordinates from tracker data if available
            if (this.trackerData != null && this.trackerData.containsKey(pName)) {
                String[] td = this.trackerData.get(pName);
                g.drawString(this.font, td[0] + ", " + td[1] + ", " + td[2] + " [" + td[3] + "]", x + 10, rowY + 15, 0xFF8B949E, false);
            }
            int btnX = x + 120;
            int btnW2 = 50;
            int btnH2 = 16;
            int btnY2 = rowY + 4;
            int btnGap = 3;
            this.drawActionButton(g, btnX, btnY2, btnW2, btnH2, "TP to", mouseX, mouseY, "tp @s " + pName);
            this.drawActionButton(g, btnX += btnW2 + btnGap, btnY2, btnW2 + 4, btnH2, "TP here", mouseX, mouseY, "tp " + pName + " @s");
            this.drawActionButton(g, btnX += btnW2 + 4 + btnGap, btnY2, 34, btnH2, "Kick", mouseX, mouseY, "__kick__:" + pName);
            this.drawActionButton(g, btnX += 34 + btnGap, btnY2, 34, btnH2, "Mute", mouseX, mouseY, "__mute__:" + pName);
            this.drawActionButton(g, btnX += 34 + btnGap, btnY2, 34, btnH2, "Ban", mouseX, mouseY, "__ban__:" + pName);
            this.drawActionButton(g, btnX += 34 + btnGap, btnY2, 48, btnH2, "Clear Inv", mouseX, mouseY, "clear " + pName);
            this.drawActionButton(g, btnX += 48 + btnGap, btnY2, 42, btnH2, "+30 Lv", mouseX, mouseY, "experience add " + pName + " 30 levels");
            this.actionRects.add(new ActionRect(x, rowY, 115, 26, "__player_detail__:" + info.getProfile().id().toString() + ":" + pName));
            ++idx;
        }
        g.disableScissor();
        if (totalPlayers > maxVisible) {
            String scrollInfo = this.playerScroll + 1 + "-" + Math.min(this.playerScroll + maxVisible, totalPlayers) + " / " + totalPlayers;
            int siW = this.font.width(scrollInfo);
            g.drawString(this.font, scrollInfo, this.contentRight - 6 - siW, this.contentTop + 6, 0xFF8B949E, false);
        }
    }

    private void renderPlayerDetail(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        Objects.requireNonNull(this.font);
        int lh = 9 + 4;
        int contentW = this.contentRight - this.contentLeft - 12;
        this.drawActionButton(g, x, y, 50, 16, "< Back", mouseX, mouseY, "__player_back__");
        g.drawString(this.font, this.selectedPlayerName, x + 56, y + 4, 0xFFE6EDF3, false);
        this.drawActionButton(g, x + contentW - 136, y, 70, 16, "Clear Inv", mouseX, mouseY, "__clear_player_inv__:" + this.selectedPlayerUUID);
        this.drawActionButton(g, x + contentW - 60, y, 60, 16, "Refresh", mouseX, mouseY, "__player_refresh__:" + this.selectedPlayerUUID);
        y += 22;
        int listTop = y;
        int listBottom = this.contentBottom - 6;
        int visibleH = listBottom - listTop;
        int totalH = this.playerDetailLines.size() * lh;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.playerDetailScroll = Math.max(0, Math.min(this.playerDetailScroll, maxScroll));
        g.enableScissor(this.contentLeft, listTop, this.contentRight, listBottom);
        for (int i = 0; i < this.playerDetailLines.size(); ++i) {
            int ly = listTop + i * lh - this.playerDetailScroll;
            if (ly + lh < listTop || ly > listBottom) continue;
            String line = this.playerDetailLines.get(i);
            int color = line.startsWith("---") ? 0xFF58A6FF : (line.startsWith("  ") ? 0xFF8B949E : 0xFFE6EDF3);
            if (line.startsWith("---")) {
                darkDivider(g,x, ly + lh / 2, contentW);
                g.drawString(this.font, line.substring(3), x + 4, ly, 0xFF58A6FF, false);
            } else {
                g.drawString(this.font, line, x, ly, color, false);
            }
        }
        g.disableScissor();
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float)this.playerDetailScroll / (float)maxScroll : 0.0f;
            darkScrollbar(g,this.contentRight - 6 - 6, listTop, listBottom - listTop, progress);
        }
    }

    private int getWorldContentHeight() {
        Objects.requireNonNull(this.font);
        int lh = 9 + 4;
        int btnH = 18;
        int gap = 3;
        int sectionGap = gap * 2;
        int h = 0;
        h += lh + 2 + btnH + sectionGap; // Time
        h += lh + 2 + btnH + sectionGap; // Weather
        h += lh + 2 + btnH + sectionGap; // Difficulty
        h += lh + 2 + GAMERULES.length * (btnH + gap); // Gamerules
        h += btnH + gap * 3 + lh + 2 + 5 * (btnH + gap); // Spawn Rules (header + 5 rows)
        h += btnH + gap * 3 + lh + 2 + btnH + gap; // World Border
        h += btnH + gap * 3 + lh + 2 + btnH + gap; // Tick Speed
        h += btnH + gap * 3 + lh + 2 + btnH + gap; // Warps header + buttons
        h += this.warpsCache.size() * (btnH + gap); // Warp entries
        h += btnH + gap * 3 + lh + 2 + 3 * (btnH + gap); // Quick Fill (header + 3 rows)
        return h;
    }

    private void renderWorld(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        Objects.requireNonNull(this.font);
        int lh = 9 + 4;
        int contentW = this.contentRight - this.contentLeft - 12;
        int btnW = 80;
        int btnH = 18;
        int gap = 3;
        int sectionGap = gap * 3;
        int visibleH = this.contentBottom - this.contentTop - 12;
        int totalH = this.getWorldContentHeight();
        int maxScroll = Math.max(0, totalH - visibleH);
        this.worldScroll = Math.max(0, Math.min(this.worldScroll, maxScroll));
        g.enableScissor(this.contentLeft, this.contentTop, this.contentRight, this.contentBottom);
        int y = this.contentTop + 6 - this.worldScroll;
        this.drawSectionHeader(g, "Time", x, y, contentW);
        this.drawActionButton(g, x, y += lh + 2, btnW, btnH, "Sunrise", mouseX, mouseY, "time set 0");
        this.drawActionButton(g, x + (btnW + gap), y, btnW, btnH, "Noon", mouseX, mouseY, "time set 6000");
        this.drawActionButton(g, x + (btnW + gap) * 2, y, btnW, btnH, "Sunset", mouseX, mouseY, "time set 12000");
        this.drawActionButton(g, x + (btnW + gap) * 3, y, btnW, btnH, "Midnight", mouseX, mouseY, "time set 18000");
        this.drawSectionHeader(g, "Weather", x, y += btnH + sectionGap, contentW);
        this.drawActionButton(g, x, y += lh + 2, btnW, btnH, "Clear", mouseX, mouseY, "weather clear");
        this.drawActionButton(g, x + (btnW + gap), y, btnW, btnH, "Rain", mouseX, mouseY, "weather rain");
        this.drawActionButton(g, x + (btnW + gap) * 2, y, btnW, btnH, "Thunder", mouseX, mouseY, "weather thunder");
        this.drawSectionHeader(g, "Difficulty", x, y += btnH + sectionGap, contentW);
        this.drawActionButton(g, x, y += lh + 2, btnW, btnH, "Peaceful", mouseX, mouseY, "difficulty peaceful");
        this.drawActionButton(g, x + (btnW + gap), y, btnW, btnH, "Easy", mouseX, mouseY, "difficulty easy");
        this.drawActionButton(g, x + (btnW + gap) * 2, y, btnW, btnH, "Normal", mouseX, mouseY, "difficulty normal");
        this.drawActionButton(g, x + (btnW + gap) * 3, y, btnW, btnH, "Hard", mouseX, mouseY, "difficulty hard");
        this.drawSectionHeader(g, "Gamerules", x, y += btnH + sectionGap, contentW);
        y += lh + 2;
        for (int i = 0; i < GAMERULES.length; ++i) {
            String rule = GAMERULES[i];
            boolean state = this.gameruleStates[i];
            int color = state ? 0xFF58A6FF : 0xFFF85149;
            String label = rule + ": " + (state ? "ON" : "OFF");
            int tw = this.font.width(label) + 12;
            this.drawGameruleToggle(g, x, y, Math.max(tw, 140), btnH, label, color, mouseX, mouseY, i);
            y += btnH + gap;
        }
        this.drawSectionHeader(g, "Spawn Rules", x, y += btnH + gap * 3, contentW);
        y += lh + 2;
        int sbW = 80;
        this.drawActionButton(g, x, y, sbW, btnH, "Mobs: ON", mouseX, mouseY, "gamerule doMobSpawning true");
        this.drawActionButton(g, x + sbW + gap, y, sbW, btnH, "Mobs: OFF", mouseX, mouseY, "gamerule doMobSpawning false");
        this.drawActionButton(g, x + (sbW + gap) * 2, y, sbW + 10, btnH, "Phantoms: ON", mouseX, mouseY, "gamerule doInsomnia true");
        this.drawActionButton(g, x + (sbW + gap) * 2 + sbW + 10 + gap, y, sbW + 10, btnH, "Phantoms: OFF", mouseX, mouseY, "gamerule doInsomnia false");
        y += btnH + gap;
        this.drawActionButton(g, x, y, sbW + 10, btnH, "Patrols: ON", mouseX, mouseY, "gamerule doPatrolSpawning true");
        this.drawActionButton(g, x + sbW + 10 + gap, y, sbW + 10, btnH, "Patrols: OFF", mouseX, mouseY, "gamerule doPatrolSpawning false");
        this.drawActionButton(g, x + (sbW + 10 + gap) * 2, y, sbW + 10, btnH, "Traders: ON", mouseX, mouseY, "gamerule doTraderSpawning true");
        this.drawActionButton(g, x + (sbW + 10 + gap) * 2 + sbW + 10 + gap, y, sbW + 10, btnH, "Traders: OFF", mouseX, mouseY, "gamerule doTraderSpawning false");
        y += btnH + gap;
        this.drawActionButton(g, x, y, sbW + 10, btnH, "Warden: ON", mouseX, mouseY, "gamerule doWardenSpawning true");
        this.drawActionButton(g, x + sbW + 10 + gap, y, sbW + 10, btnH, "Warden: OFF", mouseX, mouseY, "gamerule doWardenSpawning false");
        this.drawActionButton(g, x + (sbW + 10 + gap) * 2, y, sbW + 10, btnH, "FireTick: ON", mouseX, mouseY, "gamerule doFireTick true");
        this.drawActionButton(g, x + (sbW + 10 + gap) * 2 + sbW + 10 + gap, y, sbW + 10, btnH, "FireTick: OFF", mouseX, mouseY, "gamerule doFireTick false");
        y += btnH + gap;
        this.drawActionButton(g, x, y, sbW + 20, btnH, "Cramming: 24", mouseX, mouseY, "gamerule maxEntityCramming 24");
        this.drawActionButton(g, x + sbW + 20 + gap, y, sbW + 20, btnH, "Cramming: 0", mouseX, mouseY, "gamerule maxEntityCramming 0");
        this.drawActionButton(g, x + (sbW + 20 + gap) * 2, y, sbW + 20, btnH, "RandTick: 0", mouseX, mouseY, "gamerule randomTickSpeed 0");
        y += btnH + gap;
        this.drawActionButton(g, x, y, sbW + 20, btnH, "RandTick: 3", mouseX, mouseY, "gamerule randomTickSpeed 3");
        this.drawActionButton(g, x + sbW + 20 + gap, y, sbW + 20, btnH, "RandTick: 20", mouseX, mouseY, "gamerule randomTickSpeed 20");
        this.drawActionButton(g, x + (sbW + 20 + gap) * 2, y, sbW + 20, btnH, "MobGrief: ON", mouseX, mouseY, "gamerule mobGriefing true");
        this.drawActionButton(g, x + (sbW + 20 + gap) * 3, y, sbW + 20, btnH, "MobGrief: OFF", mouseX, mouseY, "gamerule mobGriefing false");
        // World Border section
        this.drawSectionHeader(g, "World Border", x, y += btnH + gap * 3, contentW);
        y += lh + 2;
        this.drawActionButton(g, x, y, 70, btnH, "1,000", mouseX, mouseY, "worldborder set 1000");
        this.drawActionButton(g, x + 74, y, 70, btnH, "5,000", mouseX, mouseY, "worldborder set 5000");
        this.drawActionButton(g, x + 148, y, 70, btnH, "10,000", mouseX, mouseY, "worldborder set 10000");
        this.drawActionButton(g, x + 222, y, 86, btnH, "60,000,000", mouseX, mouseY, "worldborder set 60000000");
        // Tick Speed section
        this.drawSectionHeader(g, "Tick Speed", x, y += btnH + gap * 3, contentW);
        y += lh + 2;
        this.drawActionButton(g, x, y, 50, btnH, "0", mouseX, mouseY, "gamerule randomTickSpeed 0");
        this.drawActionButton(g, x + 54, y, 50, btnH, "1", mouseX, mouseY, "gamerule randomTickSpeed 1");
        this.drawActionButton(g, x + 108, y, 50, btnH, "3", mouseX, mouseY, "gamerule randomTickSpeed 3");
        this.drawActionButton(g, x + 162, y, 50, btnH, "10", mouseX, mouseY, "gamerule randomTickSpeed 10");
        this.drawActionButton(g, x + 216, y, 50, btnH, "20", mouseX, mouseY, "gamerule randomTickSpeed 20");
        this.drawActionButton(g, x + 270, y, 50, btnH, "100", mouseX, mouseY, "gamerule randomTickSpeed 100");
        this.drawSectionHeader(g, "Admin Warps", x, y += btnH + gap * 3, contentW);
        y += lh + 2;
        if (this.warpNameBox != null) {
            this.warpNameBox.setPosition(x, y + btnH + gap + 2);
        }
        this.drawActionButton(g, x, y, 80, btnH, "Save Here", mouseX, mouseY, "__warp_save__");
        this.drawActionButton(g, x + 84, y, 60, btnH, "Refresh", mouseX, mouseY, "__warp_refresh__");
        y += btnH + gap;
        for (int wi = 0; wi < this.warpsCache.size(); ++wi) {
            String[] warp = this.warpsCache.get(wi);
            String label = warp[0] + " (" + warp[1] + ", " + warp[2] + ", " + warp[3] + ")";
            g.drawString(this.font, label, x, y + 4, 0xFFE6EDF3, false);
            this.drawActionButton(g, x + contentW - 94, y, 40, btnH, "TP", mouseX, mouseY, "__warp_tp__:" + warp[0]);
            this.drawActionButton(g, x + contentW - 50, y, 46, btnH, "Delete", mouseX, mouseY, "__warp_del__:" + warp[0]);
            y += btnH + gap;
        }
        // Quick Fill section
        this.drawSectionHeader(g, "Quick Fill (centered on you)", x, y += btnH + gap * 3, contentW);
        y += lh + 2;
        // Row 1: Platform materials
        this.drawActionButton(g, x, y, 70, btnH, "Stone 5", mouseX, mouseY, "__fill__:minecraft:stone:5:0");
        this.drawActionButton(g, x + 74, y, 70, btnH, "Stone 10", mouseX, mouseY, "__fill__:minecraft:stone:10:0");
        this.drawActionButton(g, x + 148, y, 70, btnH, "Grass 5", mouseX, mouseY, "__fill__:minecraft:grass_block:5:-1");
        this.drawActionButton(g, x + 222, y, 86, btnH, "Smooth 10", mouseX, mouseY, "__fill__:minecraft:smooth_stone:10:-1");
        y += btnH + gap;
        // Row 2: Clear / special
        this.drawActionButton(g, x, y, 60, btnH, "Air 5", mouseX, mouseY, "__fill__:minecraft:air:5:0");
        this.drawActionButton(g, x + 64, y, 60, btnH, "Air 10", mouseX, mouseY, "__fill__:minecraft:air:10:0");
        this.drawActionButton(g, x + 128, y, 70, btnH, "Glass 5", mouseX, mouseY, "__fill__:minecraft:glass:5:0");
        this.drawActionButton(g, x + 202, y, 76, btnH, "Barrier 5", mouseX, mouseY, "__fill__:minecraft:barrier:5:0");
        y += btnH + gap;
        // Row 3: Walls and floors
        this.drawActionButton(g, x, y, 90, btnH, "Wall Stone 5", mouseX, mouseY, "__fill_wall__:minecraft:stone_bricks:5");
        this.drawActionButton(g, x + 94, y, 86, btnH, "Wall Glass 5", mouseX, mouseY, "__fill_wall__:minecraft:glass:5");
        this.drawActionButton(g, x + 184, y, 96, btnH, "Floor Wood 5", mouseX, mouseY, "__fill__:minecraft:oak_planks:5:-1");
        g.disableScissor();
    }

    private void renderItems(GuiGraphics g, int mouseX, int mouseY) {
        int iy;
        int ix;
        int col;
        int row;
        int i;
        int x = this.contentLeft + 6;
        int baseY = this.contentTop + 6 + 42;
        int catBtnW = 60;
        int catY = this.contentTop + 22;
        for (int i2 = 0; i2 < ITEM_CATEGORIES.length; ++i2) {
            int catBtnX = this.contentLeft + 2 + i2 * (catBtnW + 2);
            boolean isActive = ITEM_CATEGORIES[i2].equals(this.currentItemCategory);
            boolean catHover = mouseX >= catBtnX && mouseX < catBtnX + catBtnW && mouseY >= catY && mouseY < catY + 16;
            darkBtn(g,catBtnX, catY, catBtnW, 16, catHover || isActive, isActive);
            int catTextW = this.font.width(ITEM_CATEGORIES[i2]);
            g.drawString(this.font, ITEM_CATEGORIES[i2], catBtnX + (catBtnW - catTextW) / 2, catY + 4, isActive ? 0xFF58A6FF : 0xFFE6EDF3, false);
            this.actionRects.add(new ActionRect(catBtnX, catY, catBtnW, 16, "cat:" + ITEM_CATEGORIES[i2]));
        }
        darkDivider(g,this.contentLeft, baseY - 2, this.contentRight - this.contentLeft);
        String search = this.itemSearchBox != null ? this.itemSearchBox.getValue().toLowerCase().trim() : "";
        ArrayList<Item> filtered = new ArrayList<Item>();
        String cat = this.currentItemCategory;
        if ("All".equals(cat) || "Minecraft".equals(cat) || "MegaMod".equals(cat)) {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == Items.AIR) continue;
                String ns = BuiltInRegistries.ITEM.getKey(item).getNamespace();
                if ("Minecraft".equals(cat) && !"minecraft".equals(ns)) continue;
                if ("MegaMod".equals(cat) && !"megamod".equals(ns)) continue;
                if (!search.isEmpty()) {
                    String itemId = BuiltInRegistries.ITEM.getKey(item).getPath();
                    String displayName = new ItemStack((ItemLike)item).getHoverName().getString().toLowerCase();
                    if (!itemId.contains(search) && !displayName.contains(search)) continue;
                }
                filtered.add(item);
            }
        } else {
            Item[] catItems = CATEGORY_ITEMS.getOrDefault(cat, new Item[0]);
            for (Item item : catItems) {
                if (!search.isEmpty()) {
                    String itemId = BuiltInRegistries.ITEM.getKey(item).getPath();
                    String displayName = new ItemStack((ItemLike)item).getHoverName().getString().toLowerCase();
                    if (!itemId.contains(search) && !displayName.contains(search)) continue;
                }
                filtered.add(item);
            }
        }
        // Item count display
        String countStr = filtered.size() + " items";
        g.drawString(this.font, countStr, this.contentRight - this.font.width(countStr) - 6, this.contentTop + 8, 0xFF8B949E, false);
        int cellSize = 24;
        int cols = Math.max(1, (this.contentRight - this.contentLeft - 12) / cellSize);
        int visibleH = this.contentBottom - baseY - 6;
        int visibleRows = visibleH / cellSize;
        int totalRows = (filtered.size() + cols - 1) / cols;
        int maxScroll = Math.max(0, totalRows - visibleRows);
        this.itemScroll = Math.max(0, Math.min(this.itemScroll, maxScroll));
        g.enableScissor(this.contentLeft, baseY, this.contentRight, this.contentBottom);
        for (i = 0; i < filtered.size(); ++i) {
            row = i / cols;
            col = i % cols;
            ix = x + col * cellSize;
            iy = baseY + (row - this.itemScroll) * cellSize;
            if (iy + cellSize < baseY || iy > this.contentBottom) continue;
            Item item = (Item)filtered.get(i);
            ItemStack stack = new ItemStack((ItemLike)item);
            boolean hovered = mouseX >= ix && mouseX < ix + cellSize && mouseY >= iy && mouseY < iy + cellSize;
            darkSlot(g, ix, iy, cellSize - 1, hovered);
            g.renderItem(stack, ix + 4, iy + 4);
        }
        g.disableScissor();
        for (i = 0; i < filtered.size(); ++i) {
            row = i / cols;
            col = i % cols;
            ix = x + col * cellSize;
            iy = baseY + (row - this.itemScroll) * cellSize;
            if (iy + cellSize < baseY || iy > this.contentBottom || mouseX < ix || mouseX >= ix + cellSize || mouseY < iy || mouseY >= iy + cellSize) continue;
            String name = new ItemStack((ItemLike)filtered.get(i)).getHoverName().getString();
            String line1 = "Click: Give 64";
            String line2 = "Shift: Give 1";
            int tooltipW = Math.max(this.font.width(name), Math.max(this.font.width(line1), this.font.width(line2))) + 12;
            Objects.requireNonNull(this.font);
            int tooltipH = (9 + 2) * 3 + 8;
            darkTooltip(g, mouseX + 10, mouseY - 4, tooltipW, tooltipH);
            g.drawString(this.font, name, mouseX + 14, mouseY, 0xFFE6EDF3, false);
            Objects.requireNonNull(this.font);
            g.drawString(this.font, line1, mouseX + 14, mouseY + 9 + 2, 0xFF8B949E, false);
            Objects.requireNonNull(this.font);
            g.drawString(this.font, line2, mouseX + 14, mouseY + (9 + 2) * 2, 0xFF8B949E, false);
            break;
        }
    }

    private void renderMegaMod(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int contentW = this.contentRight - this.contentLeft - 12;
        int catBtnW = 65;
        int catY = this.contentTop + 6;
        for (int i = 0; i < MEGAMOD_CATEGORIES.length; ++i) {
            int catBtnX = x + i * (catBtnW + 2);
            boolean isActive = MEGAMOD_CATEGORIES[i].equals(this.currentMegamodCategory);
            boolean catHover = mouseX >= catBtnX && mouseX < catBtnX + catBtnW && mouseY >= catY && mouseY < catY + 16;
            darkBtn(g,catBtnX, catY, catBtnW, 16, catHover || isActive, isActive);
            int catTextW = this.font.width(MEGAMOD_CATEGORIES[i]);
            g.drawString(this.font, MEGAMOD_CATEGORIES[i], catBtnX + (catBtnW - catTextW) / 2, catY + 4, isActive ? 0xFF58A6FF : 0xFFE6EDF3, false);
            this.actionRects.add(new ActionRect(catBtnX, catY, catBtnW, 16, "megacat:" + MEGAMOD_CATEGORIES[i]));
        }
        int qaY = catY + 20;
        int qaBtnW = 90;
        int qaBtnH = 16;
        int qaGap = 4;
        this.drawActionButton(g, x, qaY, qaBtnW + 10, qaBtnH, "Give All Relics", mouseX, mouseY, "give_all_relics");
        this.drawActionButton(g, x + qaBtnW + 10 + qaGap, qaY, qaBtnW + 10, qaBtnH, "Give All Weapons", mouseX, mouseY, "give_all_weapons");
        this.drawActionButton(g, x + (qaBtnW + 10 + qaGap) * 2, qaY, qaBtnW + 10, qaBtnH, "Give All Keys", mouseX, mouseY, "give_all_keys");
        this.drawActionButton(g, x, qaY += qaBtnH + 4, qaBtnW, qaBtnH, "Spawn Wraith", mouseX, mouseY, "summon megamod:wraith_boss ~ ~ ~");
        this.drawActionButton(g, x + qaBtnW + qaGap, qaY, qaBtnW + 10, qaBtnH, "Spawn Ossukage", mouseX, mouseY, "summon megamod:ossukage_boss ~ ~ ~");
        this.drawActionButton(g, x + (qaBtnW + qaGap) + qaBtnW + 10 + qaGap, qaY, qaBtnW + 10, qaBtnH, "Spawn Keeper", mouseX, mouseY, "summon megamod:dungeon_keeper ~ ~ ~");
        darkDivider(g,x, qaY + qaBtnH + 4, contentW);
        int listTop = qaY + qaBtnH + 10;
        int listBottom2 = this.contentBottom - 6;
        if ("Dungeon Mgr".equals(this.currentMegamodCategory)) {
            this.renderDungeonMgr(g, mouseX, mouseY, x, listTop, listBottom2, contentW);
            return;
        }
        if ("Quests".equals(this.currentMegamodCategory)) {
            this.renderQuestMgr(g, mouseX, mouseY, x, listTop, listBottom2, contentW);
            return;
        }
        List entries = MEGAMOD_ITEMS.getOrDefault(this.currentMegamodCategory, List.of());
        int rowH = 24;
        int rowGap = 2;
        int visibleH = listBottom2 - listTop;
        int totalH = entries.size() * (rowH + rowGap) - (entries.isEmpty() ? 0 : rowGap);
        int maxScroll = Math.max(0, totalH - visibleH);
        this.megamodScroll = Math.max(0, Math.min(this.megamodScroll, maxScroll));
        g.enableScissor(this.contentLeft, listTop, this.contentRight, listBottom2);
        int giveBtnW = 44;
        int giveBtnH = 16;
        for (int i = 0; i < entries.size(); ++i) {
            MegamodEntry entry = (MegamodEntry)entries.get(i);
            int rowY = listTop + i * (rowH + rowGap) - this.megamodScroll;
            if (rowY + rowH < listTop || rowY > listBottom2) continue;
            darkRowBg(g,x, rowY, contentW, rowH, i % 2 == 0);
            try {
                ItemStack stack = new ItemStack((ItemLike)entry.itemSupplier.get());
                g.fill(x + 2, rowY + 2, x + 20, rowY + 20, 0xFF0D1117);
                g.renderItem(stack, x + 2, rowY + 2);
            }
            catch (Exception stack) {
                // empty catch block
            }
            String string = entry.displayName;
            Objects.requireNonNull(this.font);
            g.drawString(this.font, string, x + 24, rowY + (rowH - 9) / 2, 0xFFE6EDF3, false);
            int giveX = x + contentW - giveBtnW - 4;
            int giveY = rowY + (rowH - giveBtnH) / 2;
            boolean giveHover = mouseX >= giveX && mouseX < giveX + giveBtnW && mouseY >= giveY && mouseY < giveY + giveBtnH;
            darkBtn(g,giveX, giveY, giveBtnW, giveBtnH, giveHover);
            int giveTextW = this.font.width("Give");
            int n = giveX + (giveBtnW - giveTextW) / 2;
            Objects.requireNonNull(this.font);
            g.drawString(this.font, "Give", n, giveY + (giveBtnH - 9) / 2, 0xFFE6EDF3, false);
            this.actionRects.add(new ActionRect(giveX, giveY, giveBtnW, giveBtnH, "give @s " + entry.registryId));
        }
        g.disableScissor();
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float)this.megamodScroll / (float)maxScroll : 0.0f;
            darkScrollbar(g,this.contentRight - 6 - 6, listTop, listBottom2 - listTop, progress);
        }
    }

    private void renderDungeonMgr(GuiGraphics g, int mouseX, int mouseY, int x, int listTop, int listBottom, int contentW) {
        Objects.requireNonNull(this.font);
        int lh = 9 + 4;
        if (this.dungeonsCache.isEmpty()) {
            g.drawString(this.font, "No active dungeon instances.", x + 4, listTop + 4, 0xFF8B949E, false);
            this.drawActionButton(g, x + 4, listTop + lh + 4, 60, 16, "Refresh", mouseX, mouseY, "__dungeon_refresh__");
            return;
        }
        this.drawActionButton(g, x + 4, listTop - 18, 60, 16, "Refresh", mouseX, mouseY, "__dungeon_refresh__");
        int rowH = 30;
        int rowGap = 2;
        int visibleH = listBottom - listTop;
        int totalH = this.dungeonsCache.size() * (rowH + rowGap);
        int maxScroll = Math.max(0, totalH - visibleH);
        this.dungeonScroll = Math.max(0, Math.min(this.dungeonScroll, maxScroll));
        g.enableScissor(this.contentLeft, listTop, this.contentRight, listBottom);
        for (int i = 0; i < this.dungeonsCache.size(); ++i) {
            String[] d = this.dungeonsCache.get(i);
            int rowY = listTop + i * (rowH + rowGap) - this.dungeonScroll;
            if (rowY + rowH < listTop || rowY > listBottom) continue;
            darkRowBg(g,x, rowY, contentW, rowH, i % 2 == 0);
            g.drawString(this.font, d[1] + " - " + d[3] + " " + d[4], x + 4, rowY + 4, 0xFFE6EDF3, false);
            String status = "true".equals(d[7]) ? "CLEARED" : ("true".equals(d[8]) ? "ABANDONED" : "Rooms: " + d[5] + "/" + d[6]);
            g.drawString(this.font, status, x + 4, rowY + 4 + lh, 0xFF8B949E, false);
            if (!"true".equals(d[7])) {
                this.drawActionButton(g, x + contentW - 62, rowY + 7, 58, 16, "Extract", mouseX, mouseY, "__dungeon_extract__:" + d[2]);
            }
        }
        g.disableScissor();
    }

    private void renderQuestMgr(GuiGraphics g, int mouseX, int mouseY, int x, int listTop, int listBottom, int contentW) {
        Objects.requireNonNull(this.font);
        int lh = 9 + 4;
        g.drawString(this.font, "Player:", x, listTop - 16, 0xFF8B949E, false);
        if (this.mc.getConnection() != null) {
            int pbx = x + 50;
            for (PlayerInfo info : this.mc.getConnection().getOnlinePlayers()) {
                String pn = info.getProfile().name();
                int pw = this.font.width(pn) + 8;
                boolean sel = info.getProfile().id().toString().equals(this.questTargetUUID);
                this.drawActionButton(g, pbx, listTop - 18, pw, 16, pn, mouseX, mouseY, "__quest_player__:" + info.getProfile().id().toString() + ":" + pn);
                pbx += pw + 2;
            }
        }
        if (this.questTargetUUID == null) {
            g.drawString(this.font, "Select a player above.", x + 4, listTop + 4, 0xFF8B949E, false);
            return;
        }
        int rowH = 24;
        int rowGap = 2;
        int visibleH = listBottom - listTop;
        int totalH = (this.questsCache.size() + this.questsCompleted.size() + 2) * (rowH + rowGap);
        int maxScroll = Math.max(0, totalH - visibleH);
        this.questScroll = Math.max(0, Math.min(this.questScroll, maxScroll));
        g.enableScissor(this.contentLeft, listTop, this.contentRight, listBottom);
        int y = listTop - this.questScroll;
        g.drawString(this.font, "Active Quests:", x + 4, y + 4, 0xFF58A6FF, false);
        y += rowH;
        for (int i = 0; i < this.questsCache.size(); ++i) {
            String[] q = this.questsCache.get(i);
            if (y + rowH >= listTop && y <= listBottom) {
                darkRowBg(g,x, y, contentW, rowH, i % 2 == 0);
                g.drawString(this.font, q[1] + " (" + q[2] + ") " + q[3] + " MC", x + 4, y + (rowH - 9) / 2, 0xFFE6EDF3, false);
                this.drawActionButton(g, x + contentW - 62, y + 4, 58, 16, "Complete", mouseX, mouseY, "__quest_complete__:" + this.questTargetUUID + ":" + q[0]);
            }
            y += rowH + rowGap;
        }
        g.drawString(this.font, "Completed (" + this.questsCompleted.size() + "):", x + 4, y + 4, 0xFF58A6FF, false);
        y += rowH;
        for (int i = 0; i < this.questsCompleted.size(); ++i) {
            if (y + rowH >= listTop && y <= listBottom) {
                g.drawString(this.font, this.questsCompleted.get(i), x + 4, y + (rowH - 9) / 2, 0xFF8B949E, false);
                this.drawActionButton(g, x + contentW - 52, y + 4, 48, 16, "Reset", mouseX, mouseY, "__quest_reset__:" + this.questTargetUUID + ":" + this.questsCompleted.get(i));
            }
            y += rowH + rowGap;
        }
        g.disableScissor();
    }

    private void renderEconomy(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        Objects.requireNonNull(this.font);
        int lh = 9 + 4;
        int contentW = this.contentRight - this.contentLeft - 12;
        // Sub-view toggle: Balances / Shop Manager
        int svBtnW = 70;
        boolean isMoney = "money".equals(this.ecoSubView);
        boolean moneyHover = mouseX >= x && mouseX < x + svBtnW && mouseY >= y && mouseY < y + 16;
        darkBtn(g, x, y, svBtnW, 16, moneyHover || isMoney, isMoney);
        g.drawString(this.font, "Balances", x + (svBtnW - this.font.width("Balances")) / 2, y + 4, isMoney ? 0xFF58A6FF : 0xFFE6EDF3, false);
        this.actionRects.add(new ActionRect(x, y, svBtnW, 16, "__eco_subview__:money"));
        boolean isShop = "shop".equals(this.ecoSubView);
        boolean shopHover = mouseX >= x + svBtnW + 4 && mouseX < x + svBtnW * 2 + 14 && mouseY >= y && mouseY < y + 16;
        darkBtn(g, x + svBtnW + 4, y, svBtnW + 10, 16, shopHover || isShop, isShop);
        g.drawString(this.font, "Shop Mgr", x + svBtnW + 4 + ((svBtnW + 10) - this.font.width("Shop Mgr")) / 2, y + 4, isShop ? 0xFFD29922 : 0xFFE6EDF3, false);
        this.actionRects.add(new ActionRect(x + svBtnW + 4, y, svBtnW + 10, 16, "__eco_subview__:shop"));
        g.drawString(this.font, "Amount:", x + svBtnW * 2 + 20, y + 4, 0xFF8B949E, false);
        y += 22;
        darkDivider(g, x, y, contentW);
        y += 4;
        if ("shop".equals(this.ecoSubView)) {
            this.renderShopManager(g, mouseX, mouseY, x, y, lh, contentW);
        } else {
            this.renderMoneyView(g, mouseX, mouseY, x, y, lh, contentW);
        }
    }

    private void renderSkills(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        Objects.requireNonNull(this.font);
        int lh = 9 + 4;
        int contentW = this.contentRight - this.contentLeft - 12;
        // Sub-view toggle: Skills / Game Scores / Cosmetics
        int svBtnW = 70;
        boolean isSkills = "skills".equals(this.skillSubView);
        boolean skillsHover = mouseX >= x && mouseX < x + svBtnW && mouseY >= y && mouseY < y + 16;
        darkBtn(g, x, y, svBtnW, 16, skillsHover || isSkills, isSkills);
        g.drawString(this.font, "Skills", x + (svBtnW - this.font.width("Skills")) / 2, y + 4, isSkills ? 0xFF58A6FF : 0xFFE6EDF3, false);
        this.actionRects.add(new ActionRect(x, y, svBtnW, 16, "__skill_view__:skills"));
        int bx = x + svBtnW + 4;
        boolean isGames = "games".equals(this.skillSubView);
        boolean gamesHover = mouseX >= bx && mouseX < bx + svBtnW + 10 && mouseY >= y && mouseY < y + 16;
        darkBtn(g, bx, y, svBtnW + 10, 16, gamesHover || isGames, isGames);
        g.drawString(this.font, "Game Scores", bx + ((svBtnW + 10) - this.font.width("Game Scores")) / 2, y + 4, isGames ? 0xFFFF9800 : 0xFFE6EDF3, false);
        this.actionRects.add(new ActionRect(bx, y, svBtnW + 10, 16, "__skill_view__:games"));
        int cx = bx + svBtnW + 14;
        boolean isCosm = "cosmetics".equals(this.skillSubView);
        boolean cosmHover = mouseX >= cx && mouseX < cx + svBtnW + 10 && mouseY >= y && mouseY < y + 16;
        darkBtn(g, cx, y, svBtnW + 10, 16, cosmHover || isCosm, isCosm);
        g.drawString(this.font, "Cosmetics", cx + ((svBtnW + 10) - this.font.width("Cosmetics")) / 2, y + 4, isCosm ? 0xFFFFD700 : 0xFFE6EDF3, false);
        this.actionRects.add(new ActionRect(cx, y, svBtnW + 10, 16, "__skill_view__:cosmetics"));
        String amountLabel = isCosm ? "Tag (title:color):" : "Amount:";
        int labelX = cx + svBtnW + 16;
        g.drawString(this.font, amountLabel, labelX, y + 4, 0xFF8B949E, false);
        if (this.ecoAmountBox != null) {
            this.ecoAmountBox.setPosition(labelX + this.font.width(amountLabel) + 4, y);
        }
        y += 22;
        darkDivider(g,x, y, contentW);
        y += 4;
        if ("games".equals(this.skillSubView)) {
            this.renderGameScoresView(g, mouseX, mouseY, x, y, lh, contentW);
        } else if ("cosmetics".equals(this.skillSubView)) {
            this.renderCosmeticsView(g, mouseX, mouseY, x, y, lh, contentW);
        } else {
            this.renderSkillsView(g, mouseX, mouseY, x, y, lh, contentW);
        }
    }

    private void renderCosmeticsView(GuiGraphics g, int mouseX, int mouseY, int x, int y, int lh, int contentW) {
        // Section tabs: Badges | Parties | Bounties
        int secW = 55;
        String[] sections = {"badges", "parties", "bounties"};
        String[] secLabels = {"Badges", "Parties", "Bounties"};
        int[] secColors = {0xFFFFD700, 0xFF58A6FF, 0xFF3FB950};
        for (int s = 0; s < sections.length; s++) {
            int sx = x + s * (secW + 4);
            boolean isSel = sections[s].equals(this.cosmeticSection);
            boolean sHover = mouseX >= sx && mouseX < sx + secW && mouseY >= y && mouseY < y + 14;
            darkBtn(g, sx, y, secW, 14, sHover || isSel, isSel);
            g.drawString(this.font, secLabels[s], sx + (secW - this.font.width(secLabels[s])) / 2, y + 3, isSel ? secColors[s] : 0xFFE6EDF3, false);
            this.actionRects.add(new ActionRect(sx, y, secW, 14, "__cosm_section__:" + sections[s]));
        }
        // Refresh button
        int refreshX = x + sections.length * (secW + 4) + 4;
        int refreshW = 50;
        boolean refreshHover = mouseX >= refreshX && mouseX < refreshX + refreshW && mouseY >= y && mouseY < y + 14;
        darkBtn(g, refreshX, y, refreshW, 14, refreshHover, false);
        g.drawString(this.font, "Refresh", refreshX + (refreshW - this.font.width("Refresh")) / 2, y + 3, 0xFF58A6FF, false);
        this.actionRects.add(new ActionRect(refreshX, y, refreshW, 14, "__cosm_refresh__"));
        y += 18;
        darkDivider(g, x, y - 2, contentW);

        switch (this.cosmeticSection) {
            case "parties" -> renderPartiesSection(g, mouseX, mouseY, x, y, lh, contentW);
            case "bounties" -> renderBountiesSection(g, mouseX, mouseY, x, y, lh, contentW);
            default -> renderBadgesSection(g, mouseX, mouseY, x, y, lh, contentW);
        }
    }

    private void renderBadgesSection(GuiGraphics g, int mouseX, int mouseY, int x, int y, int lh, int contentW) {
        if (this.cosmeticPlayers.isEmpty()) {
            g.drawString(this.font, "No data loaded. Click Refresh.", x, y, 0xFF8B949E, false);
            return;
        }

        // Per-tree prestige detail view
        if (this.selectedCosmeticPrestigeUUID != null) {
            renderPrestigeDetail(g, mouseX, mouseY, x, y, lh, contentW);
            return;
        }

        // Header
        g.drawString(this.font, "Player", x, y, 0xFF8B949E, false);
        g.drawString(this.font, "Badge", x + 80, y, 0xFF8B949E, false);
        g.drawString(this.font, "Prstg", x + 195, y, 0xFF8B949E, false);
        g.drawString(this.font, "Actions", x + 240, y, 0xFF8B949E, false);
        y += lh;
        darkDivider(g, x, y - 2, contentW);

        int visibleH = this.contentBottom - y - 4;
        int rowH = 24;
        int maxVisible = visibleH / rowH;
        int startIdx = this.skillScroll / rowH;
        startIdx = Math.max(0, Math.min(startIdx, Math.max(0, this.cosmeticPlayers.size() - 1)));

        for (int i = startIdx; i < this.cosmeticPlayers.size() && (i - startIdx) < maxVisible; i++) {
            CosmeticPlayerEntry entry = this.cosmeticPlayers.get(i);
            int ry = y + (i - startIdx) * rowH;
            if (ry + rowH > this.contentBottom) break;
            if (i % 2 == 0) g.fill(x - 2, ry, x + contentW, ry + rowH, 0x18FFFFFF);

            // Name
            g.drawString(this.font, entry.name, x, ry + 2, 0xFFE6EDF3, false);

            // Badge
            if (entry.hasCustomBadge) {
                g.drawString(this.font, "[" + entry.customTitle + "]", x + 80, ry + 2, 0xFF55FFFF, false);
                g.drawString(this.font, "Custom:" + entry.customColor, x + 80, ry + 12, 0xFF8B949E, false);
            } else if (entry.badgeTitle != null && !entry.badgeTitle.isEmpty()) {
                int badgeColor = switch (entry.badgeTree) {
                    case "COMBAT" -> 0xFFFF5555; case "MINING" -> 0xFFFFAA00;
                    case "FARMING" -> 0xFF55FF55; case "ARCANE" -> 0xFFFF55FF;
                    case "SURVIVAL" -> 0xFFFFFF55; default -> 0xFFE6EDF3;
                };
                g.drawString(this.font, "[" + entry.badgeTitle + "]", x + 80, ry + 2, badgeColor, false);
                g.drawString(this.font, "T" + entry.badgeTier, x + 80, ry + 12, 0xFF8B949E, false);
            } else {
                g.drawString(this.font, "None", x + 80, ry + 2, 0xFF4A4A50, false);
            }

            // Status
            g.drawString(this.font, entry.badgeEnabled ? "\u00a7aBdg" : "\u00a7cBdg", x + 165, ry + 2, 0xFFFFFFFF, false);
            g.drawString(this.font, entry.particlesEnabled ? "\u00a7aFx" : "\u00a7cFx", x + 165, ry + 12, 0xFFFFFFFF, false);

            // Prestige
            g.drawString(this.font, "\u2605" + entry.totalPrestige, x + 195, ry + 2, entry.totalPrestige > 0 ? 0xFFFFD700 : 0xFF4A4A50, false);
            g.drawString(this.font, "R:" + entry.respecCount, x + 195, ry + 12, 0xFF8B949E, false);

            // Action buttons
            int btnX = x + 240;
            int bw = 32;
            // Badge toggle
            boolean h1 = mouseX >= btnX && mouseX < btnX + bw && mouseY >= ry + 1 && mouseY < ry + 11;
            darkBtn(g, btnX, ry + 1, bw, 10, h1, false);
            g.drawString(this.font, "Bdg", btnX + 2, ry + 2, 0xFF58A6FF, false);
            this.actionRects.add(new ActionRect(btnX, ry + 1, bw, 10, "__cosm_badge__:" + entry.uuid));
            // FX toggle
            boolean h2 = mouseX >= btnX && mouseX < btnX + bw && mouseY >= ry + 12 && mouseY < ry + 22;
            darkBtn(g, btnX, ry + 12, bw, 10, h2, false);
            g.drawString(this.font, "FX", btnX + 2, ry + 13, 0xFFFF9800, false);
            this.actionRects.add(new ActionRect(btnX, ry + 12, bw, 10, "__cosm_particles__:" + entry.uuid));

            // Prestige detail
            int px = btnX + bw + 4;
            boolean h3 = mouseX >= px && mouseX < px + bw && mouseY >= ry + 1 && mouseY < ry + 11;
            darkBtn(g, px, ry + 1, bw, 10, h3, false);
            g.drawString(this.font, "P\u2605", px + 2, ry + 2, 0xFFFFD700, false);
            this.actionRects.add(new ActionRect(px, ry + 1, bw, 10, "__cosm_prestige_detail__:" + entry.uuid));

            // Reset respec
            boolean h4 = mouseX >= px && mouseX < px + bw && mouseY >= ry + 12 && mouseY < ry + 22;
            darkBtn(g, px, ry + 12, bw, 10, h4, false);
            g.drawString(this.font, "R\u21bb", px + 2, ry + 13, 0xFF55FF55, false);
            this.actionRects.add(new ActionRect(px, ry + 12, bw, 10, "__cosm_reset_respec__:" + entry.uuid));

            // Custom badge set/clear
            int cbX = px + bw + 4;
            int cbW = 26;
            if (entry.hasCustomBadge) {
                boolean h5 = mouseX >= cbX && mouseX < cbX + cbW && mouseY >= ry + 1 && mouseY < ry + 11;
                darkBtn(g, cbX, ry + 1, cbW, 10, h5, false);
                g.drawString(this.font, "Clr", cbX + 2, ry + 2, 0xFFFF5555, false);
                this.actionRects.add(new ActionRect(cbX, ry + 1, cbW, 10, "__cosm_clear_badge__:" + entry.uuid));
            }
            boolean h6 = mouseX >= cbX && mouseX < cbX + cbW && mouseY >= ry + 12 && mouseY < ry + 22;
            darkBtn(g, cbX, ry + 12, cbW, 10, h6, false);
            g.drawString(this.font, "Tag", cbX + 2, ry + 13, 0xFF55FFFF, false);
            this.actionRects.add(new ActionRect(cbX, ry + 12, cbW, 10, "__cosm_set_badge__:" + entry.uuid));
        }
    }

    private void renderPrestigeDetail(GuiGraphics g, int mouseX, int mouseY, int x, int y, int lh, int contentW) {
        // Find the player
        CosmeticPlayerEntry target = null;
        for (CosmeticPlayerEntry e : this.cosmeticPlayers) {
            if (e.uuid.equals(this.selectedCosmeticPrestigeUUID)) { target = e; break; }
        }
        if (target == null) { this.selectedCosmeticPrestigeUUID = null; return; }

        // Back button
        int backW = 40;
        boolean backH = mouseX >= x && mouseX < x + backW && mouseY >= y && mouseY < y + 14;
        darkBtn(g, x, y, backW, 14, backH, false);
        g.drawString(this.font, "\u2190 Back", x + 4, y + 3, 0xFF58A6FF, false);
        this.actionRects.add(new ActionRect(x, y, backW, 14, "__cosm_prestige_back__"));
        g.drawString(this.font, "Prestige: " + target.name + " (\u2605" + target.totalPrestige + " total)", x + backW + 8, y + 3, 0xFFFFD700, false);
        y += 20;

        // Per-tree prestige controls
        for (int t = 0; t < SKILL_TREES.length; t++) {
            String tree = SKILL_TREES[t];
            String treeName = SKILL_TREE_NAMES[t];
            int pLvl = target.treePrestige.getOrDefault(tree, 0);
            int ry = y + t * 22;
            int treeColor = switch (tree) {
                case "COMBAT" -> 0xFFFF5555; case "MINING" -> 0xFFFFAA00;
                case "FARMING" -> 0xFF55FF55; case "ARCANE" -> 0xFFFF55FF;
                case "SURVIVAL" -> 0xFFFFFF55; default -> 0xFFE6EDF3;
            };
            g.drawString(this.font, treeName, x, ry + 3, treeColor, false);
            // Stars
            StringBuilder stars = new StringBuilder();
            for (int s = 0; s < 5; s++) stars.append(s < pLvl ? "\u2605" : "\u2606");
            g.drawString(this.font, stars.toString(), x + 65, ry + 3, 0xFFFFD700, false);
            g.drawString(this.font, "P" + pLvl, x + 115, ry + 3, 0xFF8B949E, false);

            // + button
            int bx = x + 140;
            int bw = 20;
            boolean hPlus = mouseX >= bx && mouseX < bx + bw && mouseY >= ry + 1 && mouseY < ry + 13;
            darkBtn(g, bx, ry + 1, bw, 12, hPlus, false);
            g.drawString(this.font, "+", bx + 6, ry + 2, 0xFF55FF55, false);
            this.actionRects.add(new ActionRect(bx, ry + 1, bw, 12, "__cosm_tree_prestige_up__:" + target.uuid + ":" + tree));

            // - button
            int bx2 = bx + bw + 4;
            boolean hMinus = mouseX >= bx2 && mouseX < bx2 + bw && mouseY >= ry + 1 && mouseY < ry + 13;
            darkBtn(g, bx2, ry + 1, bw, 12, hMinus, false);
            g.drawString(this.font, "-", bx2 + 7, ry + 2, 0xFFFF5555, false);
            this.actionRects.add(new ActionRect(bx2, ry + 1, bw, 12, "__cosm_tree_prestige_down__:" + target.uuid + ":" + tree));
        }
    }

    private void renderPartiesSection(GuiGraphics g, int mouseX, int mouseY, int x, int y, int lh, int contentW) {
        if (this.partyViewEntries.isEmpty()) {
            g.drawString(this.font, "No active parties. Click Refresh to load.", x, y, 0xFF8B949E, false);
            return;
        }

        g.drawString(this.font, "Active Parties (" + this.partyViewEntries.size() + ")", x, y, 0xFF58A6FF, false);
        y += lh;

        for (int i = 0; i < this.partyViewEntries.size(); i++) {
            PartyViewEntry party = this.partyViewEntries.get(i);
            int ry = y + i * 44;
            if (ry + 44 > this.contentBottom) break;

            g.fill(x - 2, ry, x + contentW, ry + 42, 0x18FFFFFF);
            g.drawString(this.font, "\u00a7eLeader: \u00a7f" + party.leaderName, x + 2, ry + 2, 0xFFFFFFFF, false);

            // Members
            StringBuilder membStr = new StringBuilder("Members: ");
            for (int m = 0; m < party.members.size(); m++) {
                if (m > 0) membStr.append(", ");
                String[] mem = party.members.get(m);
                String onlineColor = "true".equals(mem[1]) ? "\u00a7a" : "\u00a77";
                membStr.append(onlineColor).append(mem[0]);
            }
            g.drawString(this.font, membStr.toString(), x + 2, ry + 13, 0xFFFFFFFF, false);

            // Combo buff
            String combo = party.comboName != null && !party.comboName.isEmpty() ? "\u00a76Buff: \u00a7e" + party.comboName : "\u00a78No combo buff";
            g.drawString(this.font, combo, x + 2, ry + 24, 0xFFFFFFFF, false);

            // Disband button
            int dbX = x + contentW - 52;
            boolean dbH = mouseX >= dbX && mouseX < dbX + 50 && mouseY >= ry + 2 && mouseY < ry + 14;
            darkBtn(g, dbX, ry + 2, 50, 12, dbH, false);
            g.drawString(this.font, "Disband", dbX + 4, ry + 3, 0xFFFF5555, false);
            this.actionRects.add(new ActionRect(dbX, ry + 2, 50, 12, "__cosm_disband_party__:" + party.leaderUuid));

            darkDivider(g, x, ry + 42, contentW);
        }
    }

    private void renderBountiesSection(GuiGraphics g, int mouseX, int mouseY, int x, int y, int lh, int contentW) {
        if (this.bountyViewEntries.isEmpty()) {
            g.drawString(this.font, "No active bounties. Click Refresh to load.", x, y, 0xFF8B949E, false);
            return;
        }

        // Header
        g.drawString(this.font, "Poster", x, y, 0xFF8B949E, false);
        g.drawString(this.font, "Item", x + 75, y, 0xFF8B949E, false);
        g.drawString(this.font, "Qty", x + 180, y, 0xFF8B949E, false);
        g.drawString(this.font, "Price", x + 210, y, 0xFF8B949E, false);
        g.drawString(this.font, "Status", x + 260, y, 0xFF8B949E, false);
        y += lh;
        darkDivider(g, x, y - 2, contentW);

        int rowH = 16;
        int maxVisible = (this.contentBottom - y - 4) / rowH;
        int startIdx = this.skillScroll / rowH;
        startIdx = Math.max(0, Math.min(startIdx, Math.max(0, this.bountyViewEntries.size() - 1)));

        for (int i = startIdx; i < this.bountyViewEntries.size() && (i - startIdx) < maxVisible; i++) {
            BountyViewEntry b = this.bountyViewEntries.get(i);
            int ry = y + (i - startIdx) * rowH;
            if (ry + rowH > this.contentBottom) break;
            if (i % 2 == 0) g.fill(x - 2, ry, x + contentW, ry + rowH, 0x18FFFFFF);

            g.drawString(this.font, b.posterName, x, ry + 3, 0xFFE6EDF3, false);
            String itemShort = b.itemName.length() > 16 ? b.itemName.substring(0, 15) + ".." : b.itemName;
            g.drawString(this.font, itemShort, x + 75, ry + 3, 0xFF58A6FF, false);
            g.drawString(this.font, String.valueOf(b.quantity), x + 180, ry + 3, 0xFFE6EDF3, false);
            g.drawString(this.font, b.priceOffered + " MC", x + 210, ry + 3, 0xFFFFD700, false);

            if (b.fulfilled) {
                g.drawString(this.font, "\u2714 " + b.fulfillerName, x + 260, ry + 3, 0xFF3FB950, false);
            } else {
                g.drawString(this.font, b.hoursRemaining + "h left", x + 260, ry + 3, 0xFFFF9800, false);
            }

            // Remove button
            int rbX = x + contentW - 30;
            boolean rbH = mouseX >= rbX && mouseX < rbX + 28 && mouseY >= ry + 1 && mouseY < ry + 13;
            darkBtn(g, rbX, ry + 1, 28, 12, rbH, false);
            g.drawString(this.font, "\u2716", rbX + 10, ry + 2, 0xFFFF5555, false);
            this.actionRects.add(new ActionRect(rbX, ry + 1, 28, 12, "__cosm_remove_bounty__:" + b.id));
        }
    }

    private void parseCosmeticData(String json) {
        this.cosmeticPlayers.clear();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray arr = root.getAsJsonArray("players");
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                String uuid = obj.get("uuid").getAsString();
                String name = obj.get("name").getAsString();
                String badgeTitle = obj.has("badgeTitle") ? obj.get("badgeTitle").getAsString() : "";
                int badgeTier = obj.has("badgeTier") ? obj.get("badgeTier").getAsInt() : 0;
                String badgeTree = obj.has("badgeTree") ? obj.get("badgeTree").getAsString() : "";
                boolean badgeEnabled = !obj.has("badgeEnabled") || obj.get("badgeEnabled").getAsBoolean();
                boolean particlesEnabled = !obj.has("particlesEnabled") || obj.get("particlesEnabled").getAsBoolean();
                int totalPrestige = obj.has("totalPrestige") ? obj.get("totalPrestige").getAsInt() : 0;
                int respecCount = obj.has("respecCount") ? obj.get("respecCount").getAsInt() : 0;
                Map<String, Integer> treePrestige = new java.util.LinkedHashMap<>();
                if (obj.has("treePrestige")) {
                    JsonObject tp = obj.getAsJsonObject("treePrestige");
                    for (String key : tp.keySet()) {
                        treePrestige.put(key, tp.get(key).getAsInt());
                    }
                }
                boolean hasCustomBadge = obj.has("hasCustomBadge") && obj.get("hasCustomBadge").getAsBoolean();
                String customTitle = obj.has("customTitle") ? obj.get("customTitle").getAsString() : "";
                String customColor = obj.has("customColor") ? obj.get("customColor").getAsString() : "";
                this.cosmeticPlayers.add(new CosmeticPlayerEntry(uuid, name, badgeTitle, badgeTier, badgeTree,
                    badgeEnabled, particlesEnabled, totalPrestige, treePrestige, respecCount,
                    hasCustomBadge, customTitle, customColor));
            }
        } catch (Exception e) {
            this.cosmeticPlayers.clear();
        }
    }

    private void parsePartyViewData(String json) {
        this.partyViewEntries.clear();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray arr = root.getAsJsonArray("parties");
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                String leaderName = obj.get("leaderName").getAsString();
                String leaderUuid = obj.get("leaderUuid").getAsString();
                String comboName = obj.has("combo") ? obj.get("combo").getAsString() : "";
                List<String[]> members = new ArrayList<>();
                JsonArray memArr = obj.getAsJsonArray("members");
                for (JsonElement me : memArr) {
                    JsonObject mo = me.getAsJsonObject();
                    members.add(new String[]{mo.get("name").getAsString(), mo.has("online") ? String.valueOf(mo.get("online").getAsBoolean()) : "false"});
                }
                this.partyViewEntries.add(new PartyViewEntry(leaderName, leaderUuid, members, comboName));
            }
        } catch (Exception e) {
            this.partyViewEntries.clear();
        }
    }

    private void parseBountyViewData(String json) {
        this.bountyViewEntries.clear();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray arr = root.getAsJsonArray("bounties");
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                this.bountyViewEntries.add(new BountyViewEntry(
                    obj.get("id").getAsInt(),
                    obj.get("poster").getAsString(),
                    obj.get("item").getAsString(),
                    obj.get("qty").getAsInt(),
                    obj.get("price").getAsInt(),
                    obj.has("fulfilled") && obj.get("fulfilled").getAsBoolean(),
                    obj.has("fulfiller") ? obj.get("fulfiller").getAsString() : "",
                    obj.has("hoursLeft") ? obj.get("hoursLeft").getAsLong() : 0
                ));
            }
        } catch (Exception e) {
            this.bountyViewEntries.clear();
        }
    }

    private void renderAudit(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        Objects.requireNonNull(this.font);
        int lh = 9 + 4;
        int contentW = this.contentRight - this.contentLeft - 12;
        // Sub-view toggle buttons
        int svBtnW = 80;
        boolean isEco = "economy".equals(this.auditSubView);
        boolean ecoHover = mouseX >= x && mouseX < x + svBtnW && mouseY >= y && mouseY < y + 16;
        darkBtn(g, x, y, svBtnW, 16, ecoHover || isEco, isEco);
        g.drawString(this.font, "Economy", x + (svBtnW - this.font.width("Economy")) / 2, y + 4, isEco ? 0xFF58A6FF : 0xFFE6EDF3, false);
        this.actionRects.add(new ActionRect(x, y, svBtnW, 16, "__audit_view__:economy"));
        boolean isActivity = "activity".equals(this.auditSubView);
        boolean actHover = mouseX >= x + svBtnW + 4 && mouseX < x + svBtnW * 2 + 4 && mouseY >= y && mouseY < y + 16;
        darkBtn(g, x + svBtnW + 4, y, svBtnW + 10, 16, actHover || isActivity, isActivity);
        g.drawString(this.font, "Player Activity", x + svBtnW + 4 + ((svBtnW + 10) - this.font.width("Player Activity")) / 2, y + 4, isActivity ? 0xFF58A6FF : 0xFFE6EDF3, false);
        this.actionRects.add(new ActionRect(x + svBtnW + 4, y, svBtnW + 10, 16, "__audit_view__:activity"));
        y += 20;
        // Search box label
        g.drawString(this.font, "Player:", x, y + 3, 0xFF8B949E, false);
        if (this.auditSearchBox != null) {
            this.auditSearchBox.setX(x + 40);
            this.auditSearchBox.setY(y);
        }
        // Time filter buttons
        int tfX = x + 170;
        g.drawString(this.font, "Time:", tfX, y + 3, 0xFF8B949E, false);
        tfX += 30;
        String[] timeFilters = {"ALL", "1h", "24h"};
        for (String tf : timeFilters) {
            int tw = this.font.width(tf) + 10;
            boolean sel = tf.equals(this.auditTimeFilter);
            this.drawActionButton(g, tfX, y, tw, 14, tf, mouseX, mouseY, "__audit_time__:" + tf);
            if (sel) g.fill(tfX, y + 13, tfX + tw, y + 14, 0xFF58A6FF);
            tfX += tw + 2;
        }
        y += 18;
        // Type filter buttons
        g.drawString(this.font, "Type:", x, y + 2, 0xFF8B949E, false);
        int ftX = x + 30;
        String[] typeFilters;
        if (isEco) {
            typeFilters = new String[]{"ALL", "ADD", "REMOVE", "ADMIN", "EARN", "SPEND"};
        } else {
            typeFilters = new String[]{"ALL", "COMMAND", "SUSPICIOUS", "DEATH", "LOGIN", "DIMENSION"};
        }
        for (String tf : typeFilters) {
            int tw = this.font.width(tf) + 8;
            boolean sel = tf.equals(this.auditTypeFilter);
            this.drawActionButton(g, ftX, y, tw, 14, tf, mouseX, mouseY, "__audit_type__:" + tf);
            if (sel) g.fill(ftX, y + 13, ftX + tw, y + 14, 0xFF58A6FF);
            ftX += tw + 2;
        }
        y += 18;
        darkDivider(g, x, y, contentW);
        y += 4;
        if ("activity".equals(this.auditSubView)) {
            this.renderPlayerAuditView(g, mouseX, mouseY, x, y, lh, contentW);
        } else {
            this.renderAuditView(g, mouseX, mouseY, x, y, lh, contentW);
        }
    }

    private void renderMoneyView(GuiGraphics g, int mouseX, int mouseY, int x, int y, int lh, int contentW) {
        this.drawSectionHeader(g, "Global Economy", x, y, contentW);
        y += lh + 4;
        int cardW = Math.min(300, contentW);
        int cardH = 6 * lh + 12;
        darkCard(g,x, y, cardW, cardH);
        int tx = x + 8;
        int ty = y + 6;
        int totalCirculation = this.ecoTotalWallets + this.ecoTotalBanks;
        g.drawString(this.font, "Total Circulation:", tx, ty, 0xFF8B949E, false);
        g.drawString(this.font, totalCirculation + " MC", tx + 120, ty, 0xFF58A6FF, false);
        ty += lh;
        g.drawString(this.font, "Total in Wallets:", tx, ty, 0xFF8B949E, false);
        g.drawString(this.font, this.ecoTotalWallets + " MC", tx + 120, ty, 0xFFE6EDF3, false);
        ty += lh;
        g.drawString(this.font, "Total in Banks:", tx, ty, 0xFF8B949E, false);
        g.drawString(this.font, this.ecoTotalBanks + " MC", tx + 120, ty, 0xFFE6EDF3, false);
        ty += lh;
        g.drawString(this.font, "Players Tracked:", tx, ty, 0xFF8B949E, false);
        g.drawString(this.font, String.valueOf(this.ecoPlayerCount), tx + 120, ty, 0xFFE6EDF3, false);
        ty += lh;
        int avgWealth = totalCirculation / Math.max(1, this.ecoPlayerCount);
        g.drawString(this.font, "Average Wealth:", tx, ty, 0xFF8B949E, false);
        g.drawString(this.font, avgWealth + " MC", tx + 120, ty, 0xFFE6EDF3, false);
        ty += lh;
        String richestName = "N/A";
        int richestTotal = 0;
        for (EcoPlayerEntry ep : this.ecoPlayers) {
            int total = ep.wallet + ep.bank;
            if (total > richestTotal) { richestTotal = total; richestName = ep.name; }
        }
        g.drawString(this.font, "Richest Player:", tx, ty, 0xFF8B949E, false);
        g.drawString(this.font, richestName + " (" + richestTotal + " MC)", tx + 120, ty, 0xFFD29922, false);
        int refreshBtnW = 60;
        int refreshBtnH = 16;
        int refreshBtnX = x + cardW + 8;
        int refreshBtnY = y + 4;
        this.drawActionButton(g, refreshBtnX, refreshBtnY, refreshBtnW, refreshBtnH, "Refresh", mouseX, mouseY, "__eco_refresh__");
        // Top 5 Richest leaderboard next to global card
        if (!this.ecoPlayers.isEmpty()) {
            List<EcoPlayerEntry> ranked = new ArrayList<>(this.ecoPlayers);
            ranked.sort((a, b) -> Integer.compare(b.wallet + b.bank, a.wallet + a.bank));
            int lbX = x + cardW + 8;
            int lbY = y + 24;
            g.drawString(this.font, "Top 5 Richest", lbX, lbY, 0xFFD29922, false);
            lbY += lh;
            for (int r = 0; r < Math.min(5, ranked.size()); r++) {
                EcoPlayerEntry rp = ranked.get(r);
                int rpTotal = rp.wallet + rp.bank;
                int pct = totalCirculation > 0 ? (int)(rpTotal * 100L / totalCirculation) : 0;
                g.drawString(this.font, (r + 1) + ". " + rp.name + ": " + rpTotal + " MC (" + pct + "%)", lbX + 2, lbY, 0xFFE6EDF3, false);
                lbY += lh;
            }
            // Wealth distribution stat
            if (ranked.size() >= 2) {
                int topWealth = ranked.get(0).wallet + ranked.get(0).bank;
                int topPct = totalCirculation > 0 ? (int)(topWealth * 100L / totalCirculation) : 0;
                lbY += 2;
                g.drawString(this.font, "Top player holds " + topPct + "% of all MC", lbX + 2, lbY, topPct > 50 ? 0xFFF85149 : 0xFF8B949E, false);
            }
        }
        y += cardH + 8;
        // Bulk actions: Give All / Tax All
        this.drawActionButton(g, x, y, 70, 16, "Give All", mouseX, mouseY, "__eco_bulk__:wallet:add");
        this.drawActionButton(g, x + 74, y, 70, 16, "Tax All", mouseX, mouseY, "__eco_bulk__:wallet:sub");
        // Sort toggle
        String sortLabel = this.ecoSortByRichest ? "Sort: Richest" : "Sort: Default";
        this.drawActionButton(g, x + 148, y, 80, 16, sortLabel, mouseX, mouseY, "__eco_sort_toggle__");
        y += 20;
        this.drawSectionHeader(g, "Player Balances", x, y, contentW);
        y += lh + 2;
        g.drawString(this.font, "Player", x + 4, y, 0xFF58A6FF, false);
        g.drawString(this.font, "Wallet", x + 100, y, 0xFF58A6FF, false);
        g.drawString(this.font, "Bank", x + 160, y, 0xFF58A6FF, false);
        g.drawString(this.font, "Total", x + 210, y, 0xFF58A6FF, false);
        y += lh;
        int listTop = y;
        int listBottom = this.contentBottom - 6;
        int rowH = 22;
        int rowGap = 1;
        int visibleH = listBottom - listTop;
        // Sort players if needed
        List<EcoPlayerEntry> displayPlayers = new ArrayList<>(this.ecoPlayers);
        if (this.ecoSortByRichest) {
            displayPlayers.sort((a, b) -> Integer.compare(b.wallet + b.bank, a.wallet + a.bank));
        }
        int totalH = displayPlayers.size() * (rowH + rowGap);
        int maxScroll = Math.max(0, totalH - visibleH);
        this.ecoScroll = Math.max(0, Math.min(this.ecoScroll, maxScroll));
        g.enableScissor(this.contentLeft, listTop, this.contentRight, listBottom);
        int btnH2 = 16;
        for (int i = 0; i < displayPlayers.size(); ++i) {
            EcoPlayerEntry ep = displayPlayers.get(i);
            int rowY = listTop + i * (rowH + rowGap) - this.ecoScroll;
            if (rowY + rowH < listTop || rowY > listBottom) continue;
            darkRowBg(g,x, rowY, contentW, rowH, i % 2 == 0);
            Objects.requireNonNull(this.font);
            int textY2 = rowY + (rowH - 9) / 2;
            g.drawString(this.font, ep.name, x + 4, textY2, 0xFFE6EDF3, false);
            g.drawString(this.font, String.valueOf(ep.wallet), x + 100, textY2, 0xFF58A6FF, false);
            g.drawString(this.font, String.valueOf(ep.bank), x + 160, textY2, 0xFF58A6FF, false);
            g.drawString(this.font, String.valueOf(ep.wallet + ep.bank), x + 210, textY2, 0xFFE6EDF3, false);
            int abx = x + 270;
            this.drawActionButton(g, abx, rowY + 3, 36, btnH2, "+Wal", mouseX, mouseY, "__eco_mod__:" + ep.uuid + ":wallet:add");
            abx += 38;
            this.drawActionButton(g, abx, rowY + 3, 36, btnH2, "+Bank", mouseX, mouseY, "__eco_mod__:" + ep.uuid + ":bank:add");
            abx += 38;
            this.drawActionButton(g, abx, rowY + 3, 36, btnH2, "-Wal", mouseX, mouseY, "__eco_mod__:" + ep.uuid + ":wallet:sub");
            abx += 38;
            this.drawActionButton(g, abx, rowY + 3, 36, btnH2, "-Bank", mouseX, mouseY, "__eco_mod__:" + ep.uuid + ":bank:sub");
            abx += 38;
            this.drawActionButton(g, abx, rowY + 3, 36, btnH2, "=Wal", mouseX, mouseY, "__eco_mod__:" + ep.uuid + ":wallet:set");
            abx += 38;
            this.drawActionButton(g, abx, rowY + 3, 36, btnH2, "=Bank", mouseX, mouseY, "__eco_mod__:" + ep.uuid + ":bank:set");
        }
        g.disableScissor();
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float)this.ecoScroll / (float)maxScroll : 0.0f;
            darkScrollbar(g,this.contentRight - 6 - 6, listTop, listBottom - listTop, progress);
        }
    }

    private void renderShopManager(GuiGraphics g, int mouseX, int mouseY, int x, int y, int lh, int contentW) {
        // Shop Config Card
        this.drawSectionHeader(g, "Shop Configuration", x, y, contentW);
        y += lh + 2;
        int cardW = Math.min(380, contentW);
        int cardH = 5 * lh + 16;
        darkCard(g, x, y, cardW, cardH);
        int tx = x + 8;
        int ty = y + 6;
        int btnH = 14;
        // Refresh Interval
        g.drawString(this.font, "Refresh Interval:", tx, ty, 0xFF8B949E, false);
        g.drawString(this.font, "Every 24 hours (real-world)", tx + 110, ty, 0xFFE6EDF3, false);
        ty += lh;
        // Price Multiplier
        g.drawString(this.font, "Price Multiplier:", tx, ty, 0xFF8B949E, false);
        g.drawString(this.font, String.format("%.1fx", this.shopPriceMult), tx + 110, ty, this.shopPriceMult != 1.0 ? 0xFFD29922 : 0xFFE6EDF3, false);
        ty += lh;
        // Sell Percentage
        g.drawString(this.font, "Sell Percentage:", tx, ty, 0xFF8B949E, false);
        g.drawString(this.font, (int)(this.shopSellPct * 100) + "%", tx + 110, ty, 0xFFE6EDF3, false);
        ty += lh;
        // Catalog size
        g.drawString(this.font, "Catalog Size:", tx, ty, 0xFF8B949E, false);
        g.drawString(this.font, "60 items (17C + 18U + 15R + 10E)", tx + 110, ty, 0xFFE6EDF3, false);
        ty += lh;
        g.drawString(this.font, "Daily Slots:", tx, ty, 0xFF8B949E, false);
        g.drawString(this.font, "9 items (3C + 3U + 2R + 1E)", tx + 110, ty, 0xFFE6EDF3, false);

        y += cardH + 8;

        // Action buttons
        this.drawSectionHeader(g, "Shop Actions", x, y, contentW);
        y += lh + 2;
        int bw = 90;
        this.drawActionButton(g, x, y, bw, 16, "Force Refresh", mouseX, mouseY, "__shop_refresh__");
        this.drawActionButton(g, x + bw + 4, y, bw, 16, "Get Config", mouseX, mouseY, "__shop_get_config__");
        y += 20;

        y += 0; // Interval is fixed to real-world 24h

        // Price multiplier controls
        g.drawString(this.font, "Price Multiplier:", x, y + 4, 0xFF8B949E, false);
        int pbx = x + 160;
        this.drawActionButton(g, pbx, y, 34, 16, "0.5x", mouseX, mouseY, "__shop_price_mult__:0.5");
        this.drawActionButton(g, pbx + 38, y, 34, 16, "0.75", mouseX, mouseY, "__shop_price_mult__:0.75");
        this.drawActionButton(g, pbx + 76, y, 30, 16, "1x", mouseX, mouseY, "__shop_price_mult__:1.0");
        this.drawActionButton(g, pbx + 110, y, 34, 16, "1.5x", mouseX, mouseY, "__shop_price_mult__:1.5");
        this.drawActionButton(g, pbx + 148, y, 30, 16, "2x", mouseX, mouseY, "__shop_price_mult__:2.0");
        this.drawActionButton(g, pbx + 182, y, 30, 16, "3x", mouseX, mouseY, "__shop_price_mult__:3.0");
        y += 20;

        // Sell percentage controls
        g.drawString(this.font, "Sell Percentage:", x, y + 4, 0xFF8B949E, false);
        int sbx = x + 160;
        this.drawActionButton(g, sbx, y, 34, 16, "10%", mouseX, mouseY, "__shop_sell_pct__:10");
        this.drawActionButton(g, sbx + 38, y, 34, 16, "25%", mouseX, mouseY, "__shop_sell_pct__:25");
        this.drawActionButton(g, sbx + 76, y, 34, 16, "35%", mouseX, mouseY, "__shop_sell_pct__:35");
        this.drawActionButton(g, sbx + 114, y, 34, 16, "50%", mouseX, mouseY, "__shop_sell_pct__:50");
        this.drawActionButton(g, sbx + 152, y, 34, 16, "75%", mouseX, mouseY, "__shop_sell_pct__:75");
        y += 24;

        // Player admin tools section
        this.drawSectionHeader(g, "Player Admin Tools", x, y, contentW);
        y += lh + 2;
        this.drawActionButton(g, x, y, 100, 16, "Discover All", mouseX, mouseY, "__admin_quick__:discover_all");
        this.drawActionButton(g, x + 104, y, 110, 16, "Clear Discoveries", mouseX, mouseY, "__admin_quick__:clear_discoveries");
        this.drawActionButton(g, x + 218, y, 110, 16, "Reset Cooldowns", mouseX, mouseY, "__admin_quick__:clear_cooldowns");
        y += 20;
        this.drawActionButton(g, x, y, 120, 16, "Disband All Parties", mouseX, mouseY, "__admin_quick__:disband_parties");
        g.drawString(this.font, "(Uses your own UUID for player-targeted actions)", x, y + 20, 0xFF8B949E, false);
    }

    private void renderSkillsView(GuiGraphics g, int mouseX, int mouseY, int x, int y, int lh, int contentW) {
        int treeBtnW = 60;
        for (int i = 0; i < SKILL_TREES.length; ++i) {
            int treeBtnX = x + i * (treeBtnW + 2);
            boolean isActive = SKILL_TREES[i].equals(this.selectedSkillTree);
            boolean treeHover = mouseX >= treeBtnX && mouseX < treeBtnX + treeBtnW && mouseY >= y && mouseY < y + 16;
            darkBtn(g,treeBtnX, y, treeBtnW, 16, treeHover || isActive, isActive);
            int treeTextW = this.font.width(SKILL_TREE_NAMES[i]);
            g.drawString(this.font, SKILL_TREE_NAMES[i], treeBtnX + (treeBtnW - treeTextW) / 2, y + 4, isActive ? 0xFF58A6FF : 0xFFE6EDF3, false);
            this.actionRects.add(new ActionRect(treeBtnX, y, treeBtnW, 16, "__skill_tree__:" + SKILL_TREES[i]));
        }
        int refreshBtnX = x + SKILL_TREES.length * (treeBtnW + 2) + 4;
        this.drawActionButton(g, refreshBtnX, y, 60, 16, "Refresh", mouseX, mouseY, "__skill_refresh__");

        // Global XP Event Multiplier control (applies to ALL players)
        int axX = refreshBtnX + 68;
        String axLabel = "XP Event: " + String.format("%.1f", this.cachedAdminXpMult) + "x";
        g.drawString(this.font, axLabel, axX, y + 4, 0xFFD29922, false);
        int axBtnX = axX + this.font.width(axLabel) + 4;
        boolean axDnHover = mouseX >= axBtnX && mouseX < axBtnX + 13 && mouseY >= y && mouseY < y + 14;
        this.darkIconBtn(g, axBtnX, y, 13, "-", 0xFFF85149, axDnHover);
        this.actionRects.add(new ActionRect(axBtnX, y, 13, 14, "skill_set_admin_xp_mult:" + String.format("%.1f", Math.max(1.0, this.cachedAdminXpMult - 0.5))));
        boolean axUpHover = mouseX >= axBtnX + 15 && mouseX < axBtnX + 28 && mouseY >= y && mouseY < y + 14;
        this.darkIconBtn(g, axBtnX + 15, y, 13, "+", 0xFF3FB950, axUpHover);
        this.actionRects.add(new ActionRect(axBtnX + 15, y, 13, 14, "skill_set_admin_xp_mult:" + String.format("%.1f", Math.min(10.0, this.cachedAdminXpMult + 0.5))));

        // Admin-only XP Boost control (only affects NeverNotch & Dev)
        int abX = axBtnX + 34;
        String abLabel = "Admin XP: " + String.format("%.1f", this.cachedAdminOnlyXpBoost) + "x";
        g.drawString(this.font, abLabel, abX, y + 4, 0xFFDA3633, false);
        int abBtnX = abX + this.font.width(abLabel) + 4;
        boolean abDnHover = mouseX >= abBtnX && mouseX < abBtnX + 13 && mouseY >= y && mouseY < y + 14;
        this.darkIconBtn(g, abBtnX, y, 13, "-", 0xFFF85149, abDnHover);
        this.actionRects.add(new ActionRect(abBtnX, y, 13, 14, "skill_set_admin_only_xp_boost:" + String.format("%.1f", Math.max(1.0, this.cachedAdminOnlyXpBoost - 0.5))));
        boolean abUpHover = mouseX >= abBtnX + 15 && mouseX < abBtnX + 28 && mouseY >= y && mouseY < y + 14;
        this.darkIconBtn(g, abBtnX + 15, y, 13, "+", 0xFF3FB950, abUpHover);
        this.actionRects.add(new ActionRect(abBtnX + 15, y, 13, 14, "skill_set_admin_only_xp_boost:" + String.format("%.1f", Math.min(10.0, this.cachedAdminOnlyXpBoost + 0.5))));

        y += 22;
        int selectedIdx = 0;
        for (int i = 0; i < SKILL_TREES.length; ++i) {
            if (SKILL_TREES[i].equals(this.selectedSkillTree)) { selectedIdx = i; break; }
        }
        this.drawSectionHeader(g, SKILL_TREE_NAMES[selectedIdx] + " Skills", x, y, contentW);
        y += lh + 2;
        g.drawString(this.font, "Player", x + 4, y, 0xFF58A6FF, false);
        g.drawString(this.font, "Level", x + 80, y, 0xFF58A6FF, false);
        g.drawString(this.font, "XP", x + 115, y, 0xFF58A6FF, false);
        g.drawString(this.font, "Pts", x + 185, y, 0xFF58A6FF, false);
        g.drawString(this.font, "Actions", x + 210, y, 0xFF58A6FF, false);
        y += lh;
        int listTop = y;
        int listBottom = this.contentBottom - 6;
        int rowH = 28;
        int rowGap = 1;
        int visibleH = listBottom - listTop;
        int totalH = this.skillPlayers.size() * (rowH + rowGap);
        int maxScroll = Math.max(0, totalH - visibleH);
        this.skillScroll = Math.max(0, Math.min(this.skillScroll, maxScroll));
        g.enableScissor(this.contentLeft, listTop, this.contentRight, listBottom);
        int btnH2 = 14;
        for (int i = 0; i < this.skillPlayers.size(); ++i) {
            SkillPlayerEntry sp = this.skillPlayers.get(i);
            int rowY = listTop + i * (rowH + rowGap) - this.skillScroll;
            if (rowY + rowH < listTop || rowY > listBottom) continue;
            darkRowBg(g,x, rowY, contentW, rowH, i % 2 == 0);
            Objects.requireNonNull(this.font);
            int textY2 = rowY + 4;
            int[] treeData = sp.treeData.getOrDefault(this.selectedSkillTree, new int[]{0, 0});
            int level = treeData[0];
            int xp = treeData[1];
            int xpNeeded = level >= 50 ? 0 : 100 + level * 50;
            int treePts = sp.treePoints.getOrDefault(this.selectedSkillTree, 0);
            g.drawString(this.font, sp.name, x + 4, textY2, 0xFFE6EDF3, false);
            g.drawString(this.font, "Lv." + level, x + 80, textY2, 0xFF58A6FF, false);
            // XP progress bar
            if (level < 50 && xpNeeded > 0) {
                int barX = x + 115;
                int barW = 65;
                float progress = (float) xp / (float) xpNeeded;
                darkProgressBar(g, barX, textY2 + 1, barW, 7, progress, 0xFF58A6FF);
                g.drawString(this.font, xp + "/" + xpNeeded, barX, textY2 + 10, 0xFF8B949E, false);
            } else {
                g.drawString(this.font, "MAX", x + 115, textY2, 0xFF3FB950, false);
            }
            g.drawString(this.font, String.valueOf(treePts), x + 185, textY2, 0xFFE6EDF3, false);
            // Action buttons — two rows to fit within content area
            int abx = x + 210;
            int btnH3 = 12;
            this.drawActionButton(g, abx, rowY + 1, 28, btnH3, "+XP", mouseX, mouseY, "__skill_xp__:" + sp.uuid + ":" + this.selectedSkillTree);
            this.drawActionButton(g, abx + 30, rowY + 1, 28, btnH3, "+Pts", mouseX, mouseY, "__skill_pts__:" + sp.uuid + ":" + this.selectedSkillTree);
            this.drawActionButton(g, abx + 60, rowY + 1, 34, btnH3, "SetLv", mouseX, mouseY, "__skill_set__:" + sp.uuid + ":" + this.selectedSkillTree);
            this.drawActionButton(g, abx, rowY + 15, 34, btnH3, "Reset", mouseX, mouseY, "__skill_reset__:" + sp.uuid + ":" + this.selectedSkillTree);
            this.drawActionButton(g, abx + 36, rowY + 15, 28, btnH3, "Max", mouseX, mouseY, "__skill_max__:" + sp.uuid + ":" + this.selectedSkillTree);
        }
        g.disableScissor();
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float)this.skillScroll / (float)maxScroll : 0.0f;
            darkScrollbar(g,this.contentRight - 6 - 6, listTop, listBottom - listTop, progress);
        }
    }

    private void renderGameScoresView(GuiGraphics g, int mouseX, int mouseY, int x, int y, int lh, int contentW) {
        this.drawActionButton(g, x + contentW - 60, y, 60, 16, "Refresh", mouseX, mouseY, "__game_scores_refresh__");
        this.drawSectionHeader(g, "Minigame High Scores", x, y, contentW);
        y += lh + 2;
        // Column headers
        g.drawString(this.font, "Player", x + 4, y, 0xFF58A6FF, false);
        g.drawString(this.font, "Snake", x + 100, y, 0xFF58A6FF, false);
        g.drawString(this.font, "Tetris", x + 160, y, 0xFF58A6FF, false);
        g.drawString(this.font, "Minesweep", x + 220, y, 0xFF58A6FF, false);
        g.drawString(this.font, "Total", x + 300, y, 0xFF58A6FF, false);
        g.drawString(this.font, "Actions", x + 350, y, 0xFF58A6FF, false);
        y += lh;
        int listTop = y;
        int listBottom = this.contentBottom - 6;
        int rowH = 22;
        int rowGap = 1;
        int visibleH = listBottom - listTop;
        int totalH = this.gameScoresCache.size() * (rowH + rowGap);
        int maxScroll = Math.max(0, totalH - visibleH);
        this.skillScroll = Math.max(0, Math.min(this.skillScroll, maxScroll));
        g.enableScissor(this.contentLeft, listTop, this.contentRight, listBottom);
        int btnH2 = 14;
        for (int i = 0; i < this.gameScoresCache.size(); i++) {
            String[] gs = this.gameScoresCache.get(i);
            // gs = [uuid, name, snake, tetris, minesweeper, total]
            int rowY = listTop + i * (rowH + rowGap) - this.skillScroll;
            if (rowY + rowH < listTop || rowY > listBottom) continue;
            darkRowBg(g, x, rowY, contentW, rowH, i % 2 == 0);
            int textY2 = rowY + (rowH - 9) / 2;
            g.drawString(this.font, gs[1], x + 4, textY2, 0xFFE6EDF3, false);
            g.drawString(this.font, gs[2], x + 100, textY2, 0xFF8B949E, false);
            g.drawString(this.font, gs[3], x + 160, textY2, 0xFF8B949E, false);
            g.drawString(this.font, gs[4], x + 220, textY2, 0xFF8B949E, false);
            g.drawString(this.font, gs[5], x + 300, textY2, 0xFFFF9800, false);
            // Action buttons
            int abx = x + 350;
            this.drawActionButton(g, abx, rowY + 3, 30, btnH2, "+Sn", mouseX, mouseY, "__gs_add__:" + gs[0] + ":snake");
            abx += 32;
            this.drawActionButton(g, abx, rowY + 3, 30, btnH2, "+Tet", mouseX, mouseY, "__gs_add__:" + gs[0] + ":tetris");
            abx += 32;
            this.drawActionButton(g, abx, rowY + 3, 30, btnH2, "+Ms", mouseX, mouseY, "__gs_add__:" + gs[0] + ":minesweeper");
            abx += 32;
            this.drawActionButton(g, abx, rowY + 3, 36, btnH2, "Reset", mouseX, mouseY, "__gs_reset__:" + gs[0]);
        }
        g.disableScissor();
        if (this.gameScoresCache.isEmpty()) {
            g.drawString(this.font, "No game scores recorded yet.", x + 4, listTop + 4, 0xFF8B949E, false);
        }
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.skillScroll / (float) maxScroll : 0.0f;
            darkScrollbar(g, this.contentRight - 6 - 6, listTop, listBottom - listTop, progress);
        }
    }

    private void parseEconomyData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            this.ecoTotalWallets = obj.get("totalWallets").getAsInt();
            this.ecoTotalBanks = obj.get("totalBanks").getAsInt();
            this.ecoPlayerCount = obj.get("playerCount").getAsInt();
            this.ecoPlayers.clear();
            JsonArray players = obj.getAsJsonArray("players");
            for (JsonElement el : players) {
                JsonObject p = el.getAsJsonObject();
                this.ecoPlayers.add(new EcoPlayerEntry(
                    p.get("uuid").getAsString(),
                    p.get("name").getAsString(),
                    p.get("wallet").getAsInt(),
                    p.get("bank").getAsInt()
                ));
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse admin entity data", e);
        }
    }

    private void parseSkillData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            this.skillPlayers.clear();
            JsonArray players = obj.getAsJsonArray("players");
            for (JsonElement el : players) {
                JsonObject p = el.getAsJsonObject();
                Map<String, int[]> treeData = new LinkedHashMap<String, int[]>();
                for (String tree : SKILL_TREES) {
                    JsonArray arr = p.getAsJsonArray(tree);
                    if (arr != null && arr.size() >= 2) {
                        treeData.put(tree, new int[]{arr.get(0).getAsInt(), arr.get(1).getAsInt()});
                    } else {
                        treeData.put(tree, new int[]{0, 0});
                    }
                }
                Map<String, Integer> treePoints = new LinkedHashMap<String, Integer>();
                if (p.has("treePoints")) {
                    JsonObject tp = p.getAsJsonObject("treePoints");
                    for (String tree : SKILL_TREES) {
                        treePoints.put(tree, tp.has(tree) ? tp.get(tree).getAsInt() : 0);
                    }
                }
                this.skillPlayers.add(new SkillPlayerEntry(
                    p.get("uuid").getAsString(),
                    p.get("name").getAsString(),
                    treeData,
                    p.get("points").getAsInt(),
                    treePoints
                ));
            }
            if (obj.has("adminXpMult")) {
                this.cachedAdminXpMult = obj.get("adminXpMult").getAsDouble();
            }
            if (obj.has("adminOnlyXpBoost")) {
                this.cachedAdminOnlyXpBoost = obj.get("adminOnlyXpBoost").getAsDouble();
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to parse admin settings data", e);
        }
    }

    private void parsePlayerDetail(String json) {
        this.playerDetailLines.clear();
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            this.playerDetailLines.add("---Player Info");
            this.playerDetailLines.add("  Name: " + obj.get("name").getAsString());
            this.playerDetailLines.add("  Online: " + obj.get("online").getAsBoolean());
            if (obj.get("online").getAsBoolean()) {
                this.playerDetailLines.add("  Position: " + obj.get("x").getAsInt() + ", " + obj.get("y").getAsInt() + ", " + obj.get("z").getAsInt());
                this.playerDetailLines.add("  Dimension: " + obj.get("dim").getAsString());
                this.playerDetailLines.add("  Health: " + obj.get("health").getAsInt() + "/" + obj.get("maxHealth").getAsInt());
                this.playerDetailLines.add("  Food: " + obj.get("food").getAsInt() + "/20");
                this.playerDetailLines.add("  Gamemode: " + obj.get("gamemode").getAsString());
            }
            this.playerDetailLines.add("---Economy");
            this.playerDetailLines.add("  Wallet: " + obj.get("wallet").getAsInt() + " MC");
            this.playerDetailLines.add("  Bank: " + obj.get("bank").getAsInt() + " MC");
            this.playerDetailLines.add("---Skills (Points: " + obj.get("skillPoints").getAsInt() + ")");
            JsonObject skills = obj.getAsJsonObject("skills");
            for (String tree : new String[]{"COMBAT", "MINING", "FARMING", "ARCANE", "SURVIVAL"}) {
                if (!skills.has(tree)) continue;
                JsonArray arr = skills.getAsJsonArray(tree);
                int lvl = arr.get(0).getAsInt();
                int xp = arr.get(1).getAsInt();
                int needed = lvl >= 50 ? 0 : 100 + lvl * 50;
                this.playerDetailLines.add("  " + tree + ": Lv." + lvl + " (" + (lvl >= 50 ? "MAX" : xp + "/" + needed) + ")");
            }
            this.playerDetailLines.add("---Equipped Relics");
            JsonObject relics = obj.getAsJsonObject("relics");
            if (relics.size() == 0) {
                this.playerDetailLines.add("  None equipped");
            } else {
                for (String slot : relics.keySet()) {
                    this.playerDetailLines.add("  " + slot + ": " + relics.get(slot).getAsString());
                }
            }
            this.playerDetailLines.add("---Museum");
            JsonObject museum = obj.getAsJsonObject("museum");
            this.playerDetailLines.add("  Items: " + museum.get("items").getAsInt() + " | Mobs: " + museum.get("mobs").getAsInt() + " | Art: " + museum.get("art").getAsInt() + " | Achievements: " + museum.get("achievements").getAsInt());
            this.playerDetailLines.add("---Statistics");
            JsonObject stats = obj.getAsJsonObject("stats");
            this.playerDetailLines.add("  Kills: " + stats.get("kills").getAsInt() + " | Deaths: " + stats.get("deaths").getAsInt() + " | Mob Kills: " + stats.get("mobKills").getAsInt());
            this.playerDetailLines.add("  Blocks Broken: " + stats.get("blocksBroken").getAsInt() + " | Placed: " + stats.get("blocksPlaced").getAsInt());
            int ticks = stats.get("playTimeTicks").getAsInt();
            this.playerDetailLines.add("  Play Time: " + (ticks / 72000) + "h " + ((ticks % 72000) / 1200) + "m");
            if (obj.has("inDungeon") && obj.get("inDungeon").getAsBoolean()) {
                this.playerDetailLines.add("---Dungeon");
                this.playerDetailLines.add("  In dungeon: " + obj.get("dungeonTier").getAsString() + " " + obj.get("dungeonTheme").getAsString());
            }
            // Prestige & Mastery
            if (obj.has("prestige")) {
                JsonObject prest = obj.getAsJsonObject("prestige");
                int total = prest.has("total") ? prest.get("total").getAsInt() : 0;
                this.playerDetailLines.add("---Prestige (Total: " + total + ")");
                for (String tree : new String[]{"COMBAT", "MINING", "FARMING", "ARCANE", "SURVIVAL"}) {
                    if (prest.has(tree)) {
                        int p = prest.get(tree).getAsInt();
                        String stars = p > 0 ? " " + "\u2605".repeat(p) : "";
                        this.playerDetailLines.add("  " + tree + ": P" + p + stars);
                    }
                }
            }
            if (obj.has("masteryMarks")) {
                this.playerDetailLines.add("  Mastery Marks: " + obj.get("masteryMarks").getAsInt());
            }
            // Arena & PvP
            if (obj.has("arena") && obj.get("arena").isJsonObject()) {
                JsonObject arena = obj.getAsJsonObject("arena");
                this.playerDetailLines.add("---Arena");
                if (arena.has("elo")) this.playerDetailLines.add("  ELO Rating: " + arena.get("elo").getAsInt());
                if (arena.has("pvpWins")) this.playerDetailLines.add("  PvP: " + arena.get("pvpWins").getAsInt() + "W / " + arena.get("pvpLosses").getAsInt() + "L");
                if (arena.has("bestWave")) this.playerDetailLines.add("  Best PvE Wave: " + arena.get("bestWave").getAsInt());
                if (arena.has("bestBossRush")) {
                    long br = arena.get("bestBossRush").getAsLong();
                    this.playerDetailLines.add("  Boss Rush: " + (br > 0 ? (br / 1000) + "s" : "Not completed"));
                }
            }
            // NG+ Progress
            if (obj.has("ngplus") && obj.get("ngplus").isJsonObject()) {
                JsonObject ngp = obj.getAsJsonObject("ngplus");
                this.playerDetailLines.add("---New Game+");
                int infB = ngp.has("infernalBosses") ? ngp.get("infernalBosses").getAsInt() : 0;
                int mytB = ngp.has("mythicBosses") ? ngp.get("mythicBosses").getAsInt() : 0;
                this.playerDetailLines.add("  Infernal Bosses: " + infB + "/8");
                this.playerDetailLines.add("  Mythic Access: " + (ngp.has("mythicAccess") && ngp.get("mythicAccess").getAsBoolean() ? "Unlocked" : "Locked"));
                this.playerDetailLines.add("  Mythic Bosses: " + mytB + "/8");
                this.playerDetailLines.add("  Eternal Access: " + (ngp.has("eternalAccess") && ngp.get("eternalAccess").getAsBoolean() ? "Unlocked" : "Locked"));
            }
            // Inventory
            this.playerInvLines.clear();
            if (obj.has("inventory")) {
                JsonArray inv = obj.getAsJsonArray("inventory");
                this.playerDetailLines.add("---Inventory (" + inv.size() + " items)");
                for (JsonElement el : inv) {
                    JsonObject item = el.getAsJsonObject();
                    int slot = item.get("slot").getAsInt();
                    String itemName = item.get("name").getAsString();
                    int count = item.get("count").getAsInt();
                    String itemId = item.get("id").getAsString();
                    String line = "  [" + slot + "] " + itemName + " x" + count + " (" + itemId + ")";
                    this.playerDetailLines.add(line);
                    this.playerInvLines.add(line);
                }
                if (inv.size() == 0) {
                    this.playerDetailLines.add("  (empty)");
                }
            }
        } catch (Exception e) {
            this.playerDetailLines.add("Error parsing player data.");
        }
    }

    private void parseDungeons(String json) {
        this.dungeonsCache.clear();
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray arr = obj.getAsJsonArray("instances");
            for (JsonElement el : arr) {
                JsonObject d = el.getAsJsonObject();
                this.dungeonsCache.add(new String[]{
                    d.get("id").getAsString(), d.get("player").getAsString(),
                    d.get("uuid").getAsString(), d.get("tier").getAsString(),
                    d.get("theme").getAsString(), String.valueOf(d.get("rooms").getAsInt()),
                    String.valueOf(d.get("totalRooms").getAsInt()), String.valueOf(d.get("bossAlive").getAsBoolean()),
                    String.valueOf(d.get("abandoned").getAsBoolean())
                });
            }
        } catch (Exception e) {}
    }

    private void parseQuests(String json) {
        this.questsCache.clear();
        this.questsCompleted.clear();
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            this.questTargetName = obj.get("playerName").getAsString();
            JsonArray active = obj.getAsJsonArray("active");
            for (JsonElement el : active) {
                JsonObject q = el.getAsJsonObject();
                this.questsCache.add(new String[]{
                    q.get("id").getAsString(), q.get("title").getAsString(),
                    q.get("type").getAsString(), String.valueOf(q.get("reward").getAsInt())
                });
            }
            JsonArray completed = obj.getAsJsonArray("completed");
            for (JsonElement el : completed) {
                this.questsCompleted.add(el.getAsString());
            }
        } catch (Exception e) {}
    }

    private void parseWarps(String json) {
        this.warpsCache.clear();
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray arr = obj.getAsJsonArray("warps");
            for (JsonElement el : arr) {
                JsonObject w = el.getAsJsonObject();
                this.warpsCache.add(new String[]{
                    w.get("name").getAsString(), String.valueOf(w.get("x").getAsInt()),
                    String.valueOf(w.get("y").getAsInt()), String.valueOf(w.get("z").getAsInt()),
                    w.get("dim").getAsString()
                });
            }
        } catch (Exception e) {}
    }

    private void parseAuditLog(String json) {
        this.auditCache.clear();
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray arr = obj.getAsJsonArray("entries");
            for (JsonElement el : arr) {
                JsonObject e = el.getAsJsonObject();
                this.auditCache.add(new String[]{
                    String.valueOf(e.get("time").getAsLong()),
                    e.get("player").getAsString(), e.get("type").getAsString(),
                    String.valueOf(e.get("amount").getAsInt()), e.get("desc").getAsString()
                });
            }
        } catch (Exception e) {}
    }

    private void parsePlayerAuditData(String json) {
        this.playerAuditCache.clear();
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray arr = obj.getAsJsonArray("entries");
            for (JsonElement el : arr) {
                JsonObject e = el.getAsJsonObject();
                this.playerAuditCache.add(new String[]{
                    String.valueOf(e.get("time").getAsLong()),
                    e.get("player").getAsString(),
                    e.get("type").getAsString(),
                    e.get("desc").getAsString()
                });
            }
        } catch (Exception e) {}
    }

    private void parseGameScoresData(String json) {
        this.gameScoresCache.clear();
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray arr = obj.getAsJsonArray("players");
            for (JsonElement el : arr) {
                JsonObject p = el.getAsJsonObject();
                this.gameScoresCache.add(new String[]{
                    p.get("uuid").getAsString(),
                    p.get("name").getAsString(),
                    String.valueOf(p.get("snake").getAsInt()),
                    String.valueOf(p.get("tetris").getAsInt()),
                    String.valueOf(p.get("minesweeper").getAsInt()),
                    String.valueOf(p.get("total").getAsInt())
                });
            }
        } catch (Exception e) {}
    }

    private int getEcoAmount() {
        if (this.ecoAmountBox == null) return 100;
        try {
            return Math.max(1, Integer.parseInt(this.ecoAmountBox.getValue().trim()));
        } catch (NumberFormatException e) {
            return 100;
        }
    }

    private boolean auditEntryMatchesFilters(String[] e, boolean isEconomy) {
        // Player name search filter
        if (this.auditSearchBox != null && !this.auditSearchBox.getValue().isEmpty()) {
            String search = this.auditSearchBox.getValue().toLowerCase();
            if (!e[1].toLowerCase().contains(search)) return false;
        }
        // Type filter
        if (!"ALL".equals(this.auditTypeFilter)) {
            String type = e[2].toUpperCase();
            if (!type.contains(this.auditTypeFilter)) return false;
        }
        // Time filter
        if (!"ALL".equals(this.auditTimeFilter)) {
            try {
                long ts = Long.parseLong(e[0]);
                long now = System.currentTimeMillis();
                long cutoff = "1h".equals(this.auditTimeFilter) ? now - 3600000L : now - 86400000L;
                if (ts < cutoff) return false;
            } catch (NumberFormatException ex) { /* skip filter */ }
        }
        return true;
    }

    private void renderAuditView(GuiGraphics g, int mouseX, int mouseY, int x, int y, int lh, int contentW) {
        this.drawSectionHeader(g, "Transaction Log", x, y, contentW);
        this.drawActionButton(g, x + contentW - 60, y, 60, 16, "Refresh", mouseX, mouseY, "__audit_refresh__");
        y += lh + 2;
        // Build filtered list
        List<String[]> filtered = new ArrayList<>();
        for (int i = this.auditCache.size() - 1; i >= 0; --i) {
            String[] e = this.auditCache.get(i);
            if (auditEntryMatchesFilters(e, true)) filtered.add(e);
        }
        g.drawString(this.font, "Showing " + filtered.size() + " / " + this.auditCache.size(), x + contentW - 60 - this.font.width("Showing " + filtered.size() + " / " + this.auditCache.size()) - 8, y - lh + 2, 0xFF8B949E, false);
        int listTop = y;
        int listBottom = this.contentBottom - 6;
        int rowH = 18;
        int rowGap = 1;
        int visibleH = listBottom - listTop;
        int totalH = filtered.size() * (rowH + rowGap);
        int maxScroll = Math.max(0, totalH - visibleH);
        this.auditScroll = Math.max(0, Math.min(this.auditScroll, maxScroll));
        g.enableScissor(this.contentLeft, listTop, this.contentRight, listBottom);
        for (int ri = 0; ri < filtered.size(); ri++) {
            String[] e = filtered.get(ri);
            int rowY = listTop + ri * (rowH + rowGap) - this.auditScroll;
            if (rowY + rowH < listTop || rowY > listBottom) continue;
            darkRowBg(g, x, rowY, contentW, rowH, ri % 2 == 0);
            int color = e[2].contains("ADD") || e[2].contains("EARN") ? 0xFF3FB950 : (e[2].contains("REMOVE") || e[2].contains("SPEND") ? 0xFFF85149 : 0xFF8B949E);
            g.drawString(this.font, e[1] + " | " + e[2] + " | " + e[3] + " MC | " + e[4], x + 4, rowY + (rowH - 9) / 2, color, false);
        }
        g.disableScissor();
    }

    private void renderPlayerAuditView(GuiGraphics g, int mouseX, int mouseY, int x, int y, int lh, int contentW) {
        this.drawSectionHeader(g, "Player Activity Log", x, y, contentW);
        this.drawActionButton(g, x + contentW - 60, y, 60, 16, "Refresh", mouseX, mouseY, "__player_audit_refresh__");
        y += lh + 2;
        // Build filtered list
        List<String[]> filtered = new ArrayList<>();
        for (int i = this.playerAuditCache.size() - 1; i >= 0; --i) {
            String[] e = this.playerAuditCache.get(i);
            if (auditEntryMatchesFilters(e, false)) filtered.add(e);
        }
        g.drawString(this.font, "Showing " + filtered.size() + " / " + this.playerAuditCache.size(), x + contentW - 60 - this.font.width("Showing " + filtered.size() + " / " + this.playerAuditCache.size()) - 8, y - lh + 2, 0xFF8B949E, false);
        int listTop = y;
        int listBottom = this.contentBottom - 6;
        int rowH = 18;
        int rowGap = 1;
        int visibleH = listBottom - listTop;
        int totalH = filtered.size() * (rowH + rowGap);
        int maxScroll = Math.max(0, totalH - visibleH);
        this.auditScroll = Math.max(0, Math.min(this.auditScroll, maxScroll));
        g.enableScissor(this.contentLeft, listTop, this.contentRight, listBottom);
        for (int ri = 0; ri < filtered.size(); ri++) {
            String[] e = filtered.get(ri);
            int rowY = listTop + ri * (rowH + rowGap) - this.auditScroll;
            if (rowY + rowH < listTop || rowY > listBottom) continue;
            darkRowBg(g, x, rowY, contentW, rowH, ri % 2 == 0);
            int color;
            String type = e[2];
            if ("COMMAND_USED".equals(type)) color = 0xFFD29922;
            else if ("SUSPICIOUS_ITEM".equals(type) || "RAPID_KILLS".equals(type)) color = 0xFFF85149;
            else if ("LOGIN_LOGOUT".equals(type)) color = 0xFF8B949E;
            else if ("DIMENSION_ENTER".equals(type)) color = 0xFF58A6FF;
            else if ("PLAYER_DEATH".equals(type)) color = 0xFFA371F7;
            else color = 0xFFE6EDF3;
            String text = e[1] + " | " + type + " | " + e[3];
            if (text.length() > 80) text = text.substring(0, 80) + "...";
            g.drawString(this.font, text, x + 4, rowY + (rowH - 9) / 2, color, false);
        }
        g.disableScissor();
    }

    private void renderEntities(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        Objects.requireNonNull(this.font);
        int lh = 9 + 4;
        int contentW = this.contentRight - this.contentLeft - 12;
        this.drawSectionHeader(g, "Entity Inspector (50-block radius)", x, y, contentW);
        int scanBtnW = 60;
        int scanBtnX = x + contentW - scanBtnW;
        this.drawActionButton(g, scanBtnX, y - 2, scanBtnW, 16, "Scan", mouseX, mouseY, "__entity_scan__");
        this.drawActionButton(g, scanBtnX - 64, y - 2, 60, 16, "Kill All", mouseX, mouseY, "__kill_all_entities__");
        this.drawActionButton(g, scanBtnX - 64 - 78, y - 2, 74, 16, "Kill Hostile", mouseX, mouseY, "__kill_hostile__");
        g.drawString(this.font, "Entities: " + this.entityList.size(), x, y, 0xFF8B949E, false);
        y += lh + 4;
        // Type breakdown
        if (!this.entityList.isEmpty()) {
            LinkedHashMap<String, Integer> typeCounts = new LinkedHashMap<>();
            for (EntityEntry ent : this.entityList) {
                typeCounts.merge(ent.type, 1, Integer::sum);
            }
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(typeCounts.entrySet());
            sorted.sort((a, b) -> b.getValue() - a.getValue());
            g.drawString(this.font, "By Type:", x, y, 0xFFD29922, false);
            y += lh;
            int typeX = x;
            for (Map.Entry<String, Integer> entry : sorted) {
                String shortName = entry.getKey();
                if (shortName.contains(":")) shortName = shortName.substring(shortName.indexOf(':') + 1);
                String label = shortName + ":" + entry.getValue();
                int labelW = this.font.width(label) + 4;
                int killBtnW = 16;
                int cellW = labelW + killBtnW + 6;
                if (typeX + cellW > x + contentW && typeX > x) {
                    typeX = x;
                    y += 16;
                }
                g.drawString(this.font, label, typeX, y + 2, 0xFFE6EDF3, false);
                this.drawActionButton(g, typeX + labelW, y, killBtnW, 14, "X", mouseX, mouseY, "__kill_type__:" + entry.getKey());
                typeX += cellW;
            }
            y += 20;
        }
        int rowH = 28;
        int rowGap = 2;
        int listTop = y;
        int listBottom = this.contentBottom - 6;
        int visibleH = listBottom - listTop;
        int totalH = this.entityList.size() * (rowH + rowGap);
        int maxScroll = Math.max(0, totalH - visibleH);
        this.entityScroll = Math.max(0, Math.min(this.entityScroll, maxScroll));
        g.enableScissor(this.contentLeft, listTop, this.contentRight, listBottom);
        for (int i = 0; i < this.entityList.size(); i++) {
            EntityEntry ent = this.entityList.get(i);
            int rowY = listTop + i * (rowH + rowGap) - this.entityScroll;
            if (rowY + rowH < listTop || rowY > listBottom) continue;
            darkRowBg(g, x, rowY, contentW, rowH, i % 2 == 0);
            String typeName = ent.type;
            if (typeName.contains(":")) typeName = typeName.substring(typeName.indexOf(':') + 1);
            String dispLine = ent.name + " (" + typeName + ")";
            if (dispLine.length() > 40) dispLine = dispLine.substring(0, 40) + "...";
            g.drawString(this.font, dispLine, x + 4, rowY + 3, 0xFFE6EDF3, false);
            String posStr = String.format("%.0f, %.0f, %.0f  [%.1fm]", ent.x, ent.y, ent.z, ent.distance);
            g.drawString(this.font, posStr, x + 4, rowY + 3 + lh, 0xFF8B949E, false);
            if (ent.maxHealth > 0) {
                int hpBarX = x + 220;
                int hpBarW = 60;
                float hpProg = ent.health / ent.maxHealth;
                int hpCol = hpProg > 0.5f ? 0xFF3FB950 : (hpProg > 0.25f ? 0xFFD29922 : 0xFFF85149);
                darkProgressBar(g, hpBarX, rowY + 4, hpBarW, 8, hpProg, hpCol);
                g.drawString(this.font, String.format("%.0f/%.0f", ent.health, ent.maxHealth), hpBarX, rowY + 15, 0xFF8B949E, false);
            }
            int btnX = x + contentW - 80;
            this.drawActionButton(g, btnX, rowY + 2, 34, 14, "Kill", mouseX, mouseY, "__entity_kill__:" + ent.entityId);
            this.drawActionButton(g, btnX + 38, rowY + 2, 34, 14, "TP", mouseX, mouseY, "__entity_tp__:" + ent.entityId);
        }
        g.disableScissor();
        if (totalH > visibleH) {
            float progress = maxScroll > 0 ? (float) this.entityScroll / (float) maxScroll : 0.0f;
            darkScrollbar(g, this.contentRight - 6 - 6, listTop, listBottom - listTop, progress);
        }
    }

    private void renderTerminal(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        Objects.requireNonNull(this.font);
        int lineH = 9 + 3;
        boolean isTermMode = "terminal".equals(this.terminalMode);
        boolean isLogMode = "logs".equals(this.terminalMode);
        int outputBottom = isTermMode ? this.contentBottom - 28 : this.contentBottom - 6;
        g.fill(this.contentLeft + 1, this.contentTop + 1, this.contentRight - 1, this.contentBottom - 1, 0xFF0D1117);
        Objects.requireNonNull(this.font);
        g.fill(this.contentLeft + 1, y - 2, this.contentRight - 1, y + 9 + 2, 0xFF161B22);
        Objects.requireNonNull(this.font);
        darkDivider(g,this.contentLeft + 1, y + 9 + 2, this.contentRight - this.contentLeft - 2);
        g.drawString(this.font, "> Admin Terminal", x, y, 0xFF58A6FF, false);
        int dotY = y + 1;
        int dotX = this.contentRight - 6 - 30;
        g.fill(dotX, dotY, dotX + 6, dotY + 6, 0xFFF85149);
        g.fill(dotX + 10, dotY, dotX + 16, dotY + 6, 0xFF58A6FF);
        g.fill(dotX + 20, dotY, dotX + 26, dotY + 6, 0xFF3FB950);
        Objects.requireNonNull(this.font);
        // Mode toggle buttons
        int modeY = y + 9 + 4;
        int tmBtnX = x;
        darkBtn(g,tmBtnX, modeY, 60, 14, false, isTermMode);
        g.drawString(this.font, "Terminal", tmBtnX + 8, modeY + 3, isTermMode ? 0xFF58A6FF : 0xFFE6EDF3, false);
        this.actionRects.add(new ActionRect(tmBtnX, modeY, 60, 14, "__term_mode_terminal__"));
        int lgBtnX = tmBtnX + 64;
        darkBtn(g,lgBtnX, modeY, 50, 14, false, isLogMode);
        g.drawString(this.font, "Logs", lgBtnX + 12, modeY + 3, isLogMode ? 0xFF58A6FF : 0xFFE6EDF3, false);
        this.actionRects.add(new ActionRect(lgBtnX, modeY, 50, 14, "__term_mode_logs__"));
        if (isLogMode) {
            int rfBtnX = lgBtnX + 54;
            boolean rfHover = mouseX >= rfBtnX && mouseX < rfBtnX + 55 && mouseY >= modeY && mouseY < modeY + 14;
            darkBtn(g,rfBtnX, modeY, 55, 14, rfHover);
            g.drawString(this.font, "Refresh", rfBtnX + 8, modeY + 3, 0xFFE6EDF3, false);
            this.actionRects.add(new ActionRect(rfBtnX, modeY, 55, 14, "__refresh_logs__"));
        }
        y = modeY + 16;
        if (isTermMode) {
            // Quick command buttons
            int qBtnW = 55;
            int qBtnH = 14;
            int qGap = 3;
            int qx = x;
            int qy = y;
            this.drawActionButton(g, qx, qy, qBtnW, qBtnH, "Players", mouseX, mouseY, "list");
            this.drawActionButton(g, qx += qBtnW + qGap, qy, qBtnW, qBtnH, "TPS", mouseX, mouseY, "neoforge tps");
            this.drawActionButton(g, qx += qBtnW + qGap, qy, qBtnW + 10, qBtnH, "Entities", mouseX, mouseY, "neoforge entity list");
            this.drawActionButton(g, qx += qBtnW + 10 + qGap, qy, qBtnW, qBtnH, "Mods", mouseX, mouseY, "neoforge mods");
            this.drawActionButton(g, qx += qBtnW + qGap, qy, qBtnW + 10, qBtnH, "KeepInv?", mouseX, mouseY, "gamerule keepInventory");
            this.drawActionButton(g, qx += qBtnW + 10 + qGap, qy, qBtnW, qBtnH, "Seed", mouseX, mouseY, "seed");
            y = qy + qBtnH + 2;
            // Presets row
            int presetsY = y;
            g.drawString(this.font, "Presets:", x, presetsY + 3, 0xFF8B949E, false);
            int pX = x + 50;
            String hoveredPreset = null;
            int hoveredPresetX = 0, hoveredPresetY = 0;
            for (Map.Entry<String, String[]> preset : COMMAND_PRESETS.entrySet()) {
                String name = preset.getKey();
                int pW = this.font.width(name) + 8;
                boolean pH = mouseX >= pX && mouseX < pX + pW && mouseY >= presetsY && mouseY < presetsY + 14;
                darkBtn(g,pX, presetsY, pW, 14, pH);
                g.drawString(this.font, name, pX + 4, presetsY + 3, 0xFFE6EDF3, false);
                this.actionRects.add(new ActionRect(pX, presetsY, pW, 14, "__run_preset__:" + name));
                if (pH) { hoveredPreset = name; hoveredPresetX = mouseX; hoveredPresetY = mouseY; }
                pX += pW + 3;
                if (pX > this.contentRight - 60) {
                    pX = x + 50;
                    presetsY += 16;
                }
            }
            if (hoveredPreset != null && PRESET_DESCRIPTIONS.containsKey(hoveredPreset)) {
                String desc = PRESET_DESCRIPTIONS.get(hoveredPreset);
                int tipW = this.font.width(desc) + 8;
                int tipH = 14;
                int tipX = hoveredPresetX + 8;
                int tipY = hoveredPresetY - tipH - 2;
                if (tipX + tipW > this.contentRight) tipX = hoveredPresetX - tipW;
                darkTooltip(g, tipX, tipY, tipW, tipH);
                g.drawString(this.font, desc, tipX + 4, tipY + 3, 0xFFE6EDF3, false);
            }
            y = presetsY + 18;
            // Run button
            int runW = 50;
            int runH = 20;
            int runX = this.contentRight - runW;
            int runY = this.contentBottom - 22;
            boolean runHover = mouseX >= runX && mouseX < runX + runW && mouseY >= runY && mouseY < runY + runH;
            darkBtn(g,runX, runY, runW, runH, runHover);
            int runTextX = runX + (runW - this.font.width("Run")) / 2;
            Objects.requireNonNull(this.font);
            g.drawString(this.font, "Run", runTextX, runY + (runH - 9) / 2, 0xFFE6EDF3, false);
            this.actionRects.add(new ActionRect(runX, runY, runW, runH, "__run_terminal__"));
            // Terminal output
            g.enableScissor(this.contentLeft, y, this.contentRight, outputBottom);
            int startY = y + 2;
            for (int i = 0; i < this.terminalOutput.size(); ++i) {
                int ly = startY + i * lineH - this.terminalScroll;
                if (ly + lineH < startY || ly > outputBottom) continue;
                String line = this.terminalOutput.get(i);
                int color = line.startsWith(">") ? 0xFF58A6FF : (line.contains("Error") || line.contains("error") || line.contains("fail") ? 0xFFF85149 : 0xFF8B949E);
                g.drawString(this.font, line, x, ly, color, false);
            }
            g.disableScissor();
            darkDivider(g,this.contentLeft, outputBottom + 2, this.contentRight - this.contentLeft);
            // Render suggestion dropdown above command input
            if (!this.suggestions.isEmpty() && this.commandInput != null && this.commandInput.visible) {
                int sugX = this.commandInput.getX();
                int sugW = this.commandInput.getWidth();
                int sugLineH = 12;
                int sugCount = Math.min(this.suggestions.size(), 10);
                int sugTotalH = sugCount * sugLineH + 4;
                int sugTopY = this.commandInput.getY() - sugTotalH;
                g.fill(sugX, sugTopY, sugX + sugW, sugTopY + sugTotalH, 0xEE1A1A2E);
                g.fill(sugX, sugTopY, sugX + sugW, sugTopY + 1, 0xFF444444);
                g.fill(sugX, sugTopY, sugX + 1, sugTopY + sugTotalH, 0xFF444444);
                g.fill(sugX + sugW - 1, sugTopY, sugX + sugW, sugTopY + sugTotalH, 0xFF444444);
                for (int si = 0; si < sugCount; si++) {
                    String sug = this.suggestions.get(si);
                    int sLineY = sugTopY + 2 + si * sugLineH;
                    boolean sugHovered = mouseX >= sugX && mouseX < sugX + sugW && mouseY >= sLineY && mouseY < sLineY + sugLineH;
                    boolean sugSelected = si == this.selectedSuggestion;
                    if (sugHovered || sugSelected) {
                        g.fill(sugX + 1, sLineY, sugX + sugW - 1, sLineY + sugLineH, 0x40FFFFFF);
                    }
                    int sugColor = sugHovered ? 0xFF58A6FF : (sugSelected ? 0xFF58A6FF : 0xFF8B949E);
                    String displaySug = sug;
                    if (this.font.width(displaySug) > sugW - 8) {
                        displaySug = displaySug.substring(0, Math.max(1, displaySug.length() - 3)) + "...";
                    }
                    g.drawString(this.font, displaySug, sugX + 4, sLineY + 2, sugColor, false);
                }
            }
        } else {
            // Logs mode
            if (this.serverLogLines.isEmpty()) {
                g.drawString(this.font, "No log data loaded. Click Refresh or switch to Logs mode.", x, y + 4, 0xFF8B949E, false);
            } else {
                g.enableScissor(this.contentLeft, y, this.contentRight, outputBottom);
                int startY = y + 2;
                for (int i = 0; i < this.serverLogLines.size(); ++i) {
                    int ly = startY + i * lineH - this.logScroll;
                    if (ly + lineH < startY || ly > outputBottom) continue;
                    String line = this.serverLogLines.get(i);
                    int color;
                    if (line.contains("ERROR") || line.contains("/ERROR")) {
                        color = 0xFFFF5555;
                    } else if (line.contains("WARN") || line.contains("/WARN")) {
                        color = 0xFFFFAA00;
                    } else {
                        color = 0xFF8B949E;
                    }
                    String displayLine = line;
                    int maxW = this.contentRight - this.contentLeft - 14;
                    if (this.font.width(displayLine) > maxW) {
                        while (displayLine.length() > 1 && this.font.width(displayLine + "...") > maxW) {
                            displayLine = displayLine.substring(0, displayLine.length() - 1);
                        }
                        displayLine = displayLine + "...";
                    }
                    g.drawString(this.font, displayLine, x, ly, color, false);
                }
                g.disableScissor();
            }
        }
    }

    private void drawActionButton(GuiGraphics g, int x, int y, int w, int h, String label, int mx, int my, String command) {
        boolean hovered = mx >= x && mx < x + w && my >= y && my < y + h;
        int bg = hovered ? 0xFF30363D : 0xFF21262D;
        int border = hovered ? 0xFF58A6FF : 0xFF30363D;
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        int textX = x + (w - this.font.width(label)) / 2;
        int textY = y + (h - 9) / 2;
        g.drawString(this.font, label, textX, textY, hovered ? 0xFFE6EDF3 : 0xFFC9D1D9, false);
        this.actionRects.add(new ActionRect(x, y, w, h, command));
    }

    private void drawGameruleToggle(GuiGraphics g, int x, int y, int w, int h, String label, int color, int mx, int my, int index) {
        boolean hovered = mx >= x && mx < x + w && my >= y && my < y + h;
        int bg = hovered ? 0xFF30363D : 0xFF21262D;
        int border = hovered ? 0xFF58A6FF : 0xFF30363D;
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        int dotColor = this.gameruleStates[index] ? 0xFF3FB950 : 0xFFF85149;
        g.fill(x + 5, y + (h - 4) / 2, x + 9, y + (h - 4) / 2 + 4, dotColor);
        int textX = x + 14;
        int textY = y + (h - 9) / 2;
        g.drawString(this.font, label, textX, textY, hovered ? 0xFFE6EDF3 : 0xFFC9D1D9, false);
        this.gameruleRects.add(new GameruleRect(x, y, w, h, index));
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        boolean shiftDown;
        if (!this.animationComplete) {
            this.animationComplete = true;
            welcomeShownThisSession = true;
            this.animationTicks = WELCOME_TEXT.length() * 2;
            this.applyTabVisibility();
            return true;
        }
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int)event.x();
        int my = (int)event.y();
        int backBtnX = this.getBackBtnX();
        if (mx >= backBtnX && mx < backBtnX + 40 && my >= 4 && my < 18) {
            if (this.mc != null) {
                this.mc.setScreen(this.parent);
            }
            return true;
        }
        int tabClipTop = HEADER_HEIGHT + 4;
        int tabClipBottom = this.height - STATUS_BAR_HEIGHT;
        for (int i = 0; i < this.tabBounds.length; ++i) {
            int[] tb = this.tabBounds[i];
            int drawY = tb[1] - this.tabScroll;
            if (drawY + tb[3] < tabClipTop || drawY > tabClipBottom) continue; // off-screen
            if (mx < tb[0] || mx >= tb[0] + tb[2] || my < drawY || my >= drawY + tb[3]) continue;
            if (my < tabClipTop || my >= tabClipBottom) continue; // outside clipped area
            this.switchTab(i);
            return true;
        }
        for (ActionRect ar : this.actionRects) {
            if (mx < ar.x || mx >= ar.x + ar.w || my < ar.y || my >= ar.y + ar.h) continue;
            this.handleActionCommand(ar.command, event);
            return true;
        }
        for (GameruleRect gr : this.gameruleRects) {
            if (mx < gr.x || mx >= gr.x + gr.w || my < gr.y || my >= gr.y + gr.h) continue;
            this.gameruleStates[gr.index] = !this.gameruleStates[gr.index];
            this.sendCommand("gamerule " + GAMERULES[gr.index] + " " + this.gameruleStates[gr.index]);
            return true;
        }
        if (this.currentTab == 34 && this.handleCombatTabClick(mx, my)) {
            return true;
        }
        // Suggestion click handling
        if (this.currentTab == 10 && !this.suggestions.isEmpty() && this.commandInput != null) {
            int sugX = this.commandInput.getX();
            int sugW = this.commandInput.getWidth();
            int sugLineH = 12;
            int sugCount = Math.min(this.suggestions.size(), 10);
            int sugTotalH = sugCount * sugLineH + 4;
            int sugY = this.commandInput.getY() - sugTotalH;

            if (mx >= sugX && mx < sugX + sugW && my >= sugY && my < sugY + sugTotalH) {
                int clickedIdx = (my - sugY - 2) / sugLineH;
                if (clickedIdx >= 0 && clickedIdx < sugCount) {
                    String suggestion = this.suggestions.get(clickedIdx);
                    this.commandInput.setValue(applySuggestion(this.commandInput.getValue(), suggestion));
                    this.commandInput.setCursorPosition(this.commandInput.getValue().length());
                    this.suggestions.clear();
                    this.selectedSuggestion = -1;
                    this.lastSuggestionQuery = this.commandInput.getValue();
                    return true;
                }
            }
        }
        if (this.currentTab == 3 && this.handleItemClick(mx, my, shiftDown = event.buttonInfo().hasShiftDown())) {
            return true;
        }
        // Delegate to new panels
        if (this.currentTab == 11 && this.museumPanel != null && this.museumPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 12 && this.inventoryPanel != null && this.inventoryPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 13 && this.dungeonAnalyticsPanel != null && this.dungeonAnalyticsPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 14 && this.featureTogglesPanel != null && this.featureTogglesPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 15 && this.moderationPanel != null && this.moderationPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 16 && this.structureLocatorPanel != null && this.structureLocatorPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 17 && this.schedulerPanel != null && this.schedulerPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 18 && this.botControlPanel != null && this.botControlPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 19 && this.mobShowcasePanel != null && this.mobShowcasePanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 33 && this.mobShowcasePanel != null && this.mobShowcasePanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 20 && this.adminModulesPanel != null && this.adminModulesPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 21 && this.warpPanel != null && this.warpPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        // TODO: citizensPanel removed - if (this.currentTab == 22 && this.citizensPanel != null && this.citizensPanel.mouseClicked(mx, my, 0)) return true;
        if (this.currentTab == 23 && this.casinoPanel != null && this.casinoPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 24 && this.corruptionPanel != null && this.corruptionPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 24 && this.handleCorruptionClick(mx, my)) return true;
        if (this.currentTab == 5 && this.economyDashboardPanel != null && this.economyDashboardPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 25 && this.marketplaceAdminPanel != null && this.marketplaceAdminPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 26 && this.alchemyAdminPanel != null && this.alchemyAdminPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 27 && this.systemHealthPanel != null && this.systemHealthPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 36 && this.adminSearchPanel != null && this.adminSearchPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        if (this.currentTab == 35 && this.worldEditPanel != null && this.worldEditPanel.mouseClicked(mx, my, 0, this.contentLeft, this.contentTop, this.contentRight, this.contentBottom)) return true;
        return super.mouseClicked(event, consumed);
    }

    private void handleActionCommand(String command, MouseButtonEvent event) {
        if ("__run_terminal__".equals(command)) {
            this.executeTerminalCommand();
            return;
        }
        if ("__term_mode_terminal__".equals(command)) {
            this.terminalMode = "terminal";
            this.applyTabVisibility();
            return;
        }
        if ("__term_mode_logs__".equals(command)) {
            this.terminalMode = "logs";
            this.logScroll = 0;
            this.logRefreshTicks = 0;
            this.applyTabVisibility();
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_server_logs", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__refresh_logs__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_server_logs", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__run_preset__:")) {
            String presetName = command.substring(15);
            String[] cmds = COMMAND_PRESETS.get(presetName);
            if (cmds != null) {
                this.terminalOutput.add("> [Preset: " + presetName + "]");
                for (String cmd : cmds) {
                    this.terminalOutput.add("  > " + cmd);
                    this.sendCommand(cmd);
                }
                this.updateTerminalScroll();
            }
            return;
        }
        if (command.startsWith("cat:")) {
            this.currentItemCategory = command.substring(4);
            this.itemScroll = 0;
            return;
        }
        if (command.startsWith("megacat:")) {
            this.currentMegamodCategory = command.substring(8);
            this.megamodScroll = 0;
            this.dungeonScroll = 0;
            this.questScroll = 0;
            if ("Dungeon Mgr".equals(this.currentMegamodCategory)) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_dungeons", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return;
        }
        if ("give_all_relics".equals(command)) {
            for (MegamodEntry e : MEGAMOD_ITEMS.getOrDefault("Relics", List.of())) {
                this.sendCommand("give @s " + e.registryId);
            }
            return;
        }
        if ("give_all_weapons".equals(command)) {
            for (MegamodEntry e : MEGAMOD_ITEMS.getOrDefault("Weapons", List.of())) {
                this.sendCommand("give @s " + e.registryId);
            }
            return;
        }
        if ("give_all_keys".equals(command)) {
            for (MegamodEntry e : MEGAMOD_ITEMS.getOrDefault("Dungeons", List.of())) {
                if (!e.displayName.startsWith("Key")) continue;
                this.sendCommand("give @s " + e.registryId);
            }
            return;
        }
        if ("__eco_refresh__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_economy", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__eco_mod__:")) {
            String[] parts = command.substring(12).split(":");
            String uuid = parts[0];
            String targetType = parts[1];
            String op = parts[2];
            int amount = this.getEcoAmount();
            if ("set".equals(op)) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("eco_set", uuid + ":" + amount + ":" + targetType), (CustomPacketPayload[])new CustomPacketPayload[0]);
            } else {
                if ("sub".equals(op)) amount = -amount;
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("eco_modify", uuid + ":" + amount + ":" + targetType), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return;
        }
        if (command.startsWith("__eco_bulk__:")) {
            String[] parts = command.substring(13).split(":");
            String targetType = parts[0];
            String op = parts[1];
            int amount = this.getEcoAmount();
            if ("sub".equals(op)) amount = -amount;
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("eco_bulk", amount + ":" + targetType), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__eco_sort_toggle__".equals(command)) {
            this.ecoSortByRichest = !this.ecoSortByRichest;
            this.ecoScroll = 0;
            return;
        }
        if (command.startsWith("__eco_subview__:")) {
            this.ecoSubView = command.substring(16);
            this.ecoScroll = 0;
            this.skillScroll = 0;
            if ("skills".equals(this.ecoSubView)) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_skills", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            } else if ("shop".equals(this.ecoSubView)) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("shop_admin_get_config", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            } else {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_economy", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return;
        }
        if ("__shop_refresh__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("shop_admin_refresh", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__shop_get_config__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("shop_admin_get_config", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__shop_interval__:")) {
            String hours = command.substring(18);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("shop_admin_set_interval", hours), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__shop_price_mult__:")) {
            String mult = command.substring(20);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("shop_admin_set_price_mult", mult), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__shop_sell_pct__:")) {
            String pct = command.substring(18);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("shop_admin_set_sell_pct", pct), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__admin_quick__:")) {
            String action = command.substring(16);
            String myUuid = this.mc.player != null ? this.mc.player.getStringUUID() : "";
            switch (action) {
                case "heal_self":
                    ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_heal_self", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    break;
                case "feed_self":
                    ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_feed_self", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    break;
                case "discover_all":
                    ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_discover_all", myUuid), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    break;
                case "clear_discoveries":
                    ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_clear_discoveries", myUuid), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    break;
                case "clear_cooldowns":
                    ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_clear_cooldowns", myUuid), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    break;
                case "disband_parties":
                    ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_disband_all_parties", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    break;
            }
            return;
        }
        if (command.startsWith("__skill_tree__:")) {
            this.selectedSkillTree = command.substring(15);
            this.skillScroll = 0;
            return;
        }
        if ("__skill_refresh__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_skills", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("skill_set_admin_xp_mult:")) {
            String mult = command.substring(24);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("skill_set_admin_xp_mult", mult), (CustomPacketPayload[])new CustomPacketPayload[0]);
            this.cachedAdminXpMult = Double.parseDouble(mult);
            return;
        }
        if (command.startsWith("skill_set_admin_only_xp_boost:")) {
            String boost = command.substring(30);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("skill_set_admin_only_xp_boost", boost), (CustomPacketPayload[])new CustomPacketPayload[0]);
            this.cachedAdminOnlyXpBoost = Double.parseDouble(boost);
            return;
        }
        if (command.startsWith("__skill_xp__:")) {
            String[] parts = command.substring(13).split(":");
            String uuid = parts[0];
            String tree = parts[1];
            int amount = this.getEcoAmount();
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("skill_add_xp", uuid + ":" + tree + ":" + amount), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__skill_pts__:")) {
            String[] parts = command.substring(14).split(":");
            String uuid = parts[0];
            String tree = parts[1];
            int amount = this.getEcoAmount();
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("skill_add_points", uuid + ":" + tree + ":" + amount), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__skill_set__:")) {
            String[] parts = command.substring(14).split(":");
            String uuid = parts[0];
            String tree = parts[1];
            int lvl = this.getEcoAmount();
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("skill_set_level", uuid + ":" + tree + ":" + lvl), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__skill_reset__:")) {
            String[] parts = command.substring(16).split(":");
            String uuid = parts[0];
            String tree = parts[1];
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("skill_reset_tree", uuid + ":" + tree), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__skill_max__:")) {
            String[] parts = command.substring(14).split(":");
            String uuid = parts[0];
            String tree = parts[1];
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("skill_set_level", uuid + ":" + tree + ":50"), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__skill_view__:")) {
            this.skillSubView = command.substring(15);
            this.skillScroll = 0;
            if ("games".equals(this.skillSubView) && this.gameScoresCache.isEmpty()) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_game_scores", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            if ("cosmetics".equals(this.skillSubView) && this.cosmeticPlayers.isEmpty()) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_cosmetics", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return;
        }
        if ("__cosm_refresh__".equals(command)) {
            if ("parties".equals(this.cosmeticSection)) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_party_view", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            } else if ("bounties".equals(this.cosmeticSection)) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_bounty_view", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            } else {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_cosmetics", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return;
        }
        if (command.startsWith("__cosm_badge__:")) {
            String uuid = command.substring(15);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("cosm_toggle_badge", uuid), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__cosm_particles__:")) {
            String uuid = command.substring(19);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("cosm_toggle_particles", uuid), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__cosm_prestige_up__:")) {
            String uuid = command.substring(21);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("cosm_prestige_up", uuid), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__cosm_prestige_down__:")) {
            String uuid = command.substring(23);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("cosm_prestige_down", uuid), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__cosm_section__:")) {
            this.cosmeticSection = command.substring(17);
            this.skillScroll = 0;
            this.selectedCosmeticPrestigeUUID = null;
            // Auto-refresh on section switch
            if ("parties".equals(this.cosmeticSection)) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_party_view", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            } else if ("bounties".equals(this.cosmeticSection)) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_bounty_view", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            } else {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_cosmetics", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return;
        }
        if (command.startsWith("__cosm_prestige_detail__:")) {
            this.selectedCosmeticPrestigeUUID = command.substring(25);
            return;
        }
        if ("__cosm_prestige_back__".equals(command)) {
            this.selectedCosmeticPrestigeUUID = null;
            return;
        }
        if (command.startsWith("__cosm_tree_prestige_up__:")) {
            String[] parts = command.substring(26).split(":");
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("cosm_tree_prestige_up", parts[0] + ":" + parts[1]), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__cosm_tree_prestige_down__:")) {
            String[] parts = command.substring(28).split(":");
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("cosm_tree_prestige_down", parts[0] + ":" + parts[1]), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__cosm_reset_respec__:")) {
            String uuid = command.substring(22);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("cosm_reset_respec", uuid), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__cosm_disband_party__:")) {
            String uuid = command.substring(23);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("cosm_disband_party", uuid), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__cosm_remove_bounty__:")) {
            String id = command.substring(23);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("cosm_remove_bounty", id), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__cosm_clear_badge__:")) {
            String uuid = command.substring(21);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("cosm_clear_badge", uuid), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__cosm_set_badge__:")) {
            // Use ecoAmountBox text as "title:color" input (e.g. "Champion:gold")
            String uuid = command.substring(19);
            String input = this.ecoAmountBox != null ? this.ecoAmountBox.getValue().trim() : "";
            if (input.isEmpty()) {
                this.adminMessages.add("Type badge as title:color in Amount box (e.g. Champion:gold)");
                return;
            }
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("cosm_set_badge", uuid + "|" + input), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__game_scores_refresh__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_game_scores", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__gs_add__:")) {
            String[] parts = command.substring(11).split(":");
            String uuid = parts[0];
            String game = parts[1];
            int amount = this.getEcoAmount();
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_set_game_score", uuid + ":" + game + ":" + amount), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__gs_reset__:")) {
            String uuid = command.substring(13);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_reset_game_scores", uuid), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__broadcast__".equals(command)) {
            if (this.broadcastBox != null && !this.broadcastBox.getValue().trim().isEmpty()) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_broadcast", this.broadcastBox.getValue().trim()), (CustomPacketPayload[])new CustomPacketPayload[0]);
                this.broadcastBox.setValue("");
            }
            return;
        }
        if (command.startsWith("__player_detail__:")) {
            String[] parts = command.substring(18).split(":", 2);
            this.selectedPlayerUUID = parts[0];
            this.selectedPlayerName = parts.length > 1 ? parts[1] : "Unknown";
            this.playerDetailScroll = 0;
            this.playerDetailLines.clear();
            this.playerDetailLines.add("Loading...");
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_player_detail", this.selectedPlayerUUID), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__player_back__".equals(command)) {
            this.selectedPlayerUUID = null;
            return;
        }
        if (command.startsWith("__player_refresh__:")) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_player_detail", command.substring(19)), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        // Generic send: __send__:action:jsonData — sends action+data directly to server
        if (command.startsWith("__send__:")) {
            String rest = command.substring(9);
            int colonIdx = rest.indexOf(':');
            String act = colonIdx >= 0 ? rest.substring(0, colonIdx) : rest;
            String data = colonIdx >= 0 ? rest.substring(colonIdx + 1) : "";
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload(act, data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__loot_add__".equals(command)) {
            if (this.lootInput != null && !this.lootInput.getValue().isEmpty()) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("loot_add", this.lootInput.getValue()), (CustomPacketPayload[])new CustomPacketPayload[0]);
                this.lootInput.setValue("");
            }
            return;
        }
        if ("__alias_add__".equals(command)) {
            if (this.aliasInput != null && !this.aliasInput.getValue().isEmpty()) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("alias_add", this.aliasInput.getValue()), (CustomPacketPayload[])new CustomPacketPayload[0]);
                this.aliasInput.setValue("");
            }
            return;
        }
        if (command.startsWith("__kick__:")) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_kick", command.substring(9)), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__mute__:")) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_mute", command.substring(9)), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__ban__:")) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_ban", command.substring(8)), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__dungeon_refresh__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_dungeons", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__dungeon_extract__:")) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("dungeon_force_extract", command.substring(20)), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__quest_player__:")) {
            String[] parts = command.substring(17).split(":", 2);
            this.questTargetUUID = parts[0];
            this.questTargetName = parts.length > 1 ? parts[1] : "?";
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_quests", this.questTargetUUID), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__quest_complete__:")) {
            String data = command.substring(19);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_complete_quest", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__quest_reset__:")) {
            String data = command.substring(16);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_reset_quest", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__warp_save__".equals(command)) {
            if (this.warpNameBox != null && !this.warpNameBox.getValue().trim().isEmpty()) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("warp_save", this.warpNameBox.getValue().trim()), (CustomPacketPayload[])new CustomPacketPayload[0]);
                this.warpNameBox.setValue("");
                // Auto-refresh warp list after saving
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_warps", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return;
        }
        if ("__warp_refresh__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_warps", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__warp_tp__:")) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("warp_goto", command.substring(12)), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__warp_del__:")) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("warp_delete", command.substring(13)), (CustomPacketPayload[])new CustomPacketPayload[0]);
            // Auto-refresh warp list after deleting
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_warps", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__audit_refresh__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_audit_log", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__player_audit_refresh__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_player_audit", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__audit_view__:")) {
            this.auditSubView = command.substring(15);
            this.auditScroll = 0;
            this.auditTypeFilter = "ALL";
            if ("activity".equals(this.auditSubView) && this.playerAuditCache.isEmpty()) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_player_audit", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return;
        }
        if (command.startsWith("__audit_type__:")) {
            this.auditTypeFilter = command.substring(15);
            this.auditScroll = 0;
            return;
        }
        if (command.startsWith("__audit_time__:")) {
            this.auditTimeFilter = command.substring(15);
            this.auditScroll = 0;
            return;
        }
        if ("__kill_all_entities__".equals(command)) {
            this.sendCommand("kill @e[type=!player,distance=..50]");
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_entities", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__kill_hostile__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_kill_hostile", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_entities", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__kill_type__:")) {
            String fullType = command.substring(14);
            this.sendCommand("kill @e[type=" + fullType + ",distance=..50]");
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_entities", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__entity_scan__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_entities", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__entity_kill__:")) {
            String eid = command.substring(16);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_kill_entity", eid), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__entity_tp__:")) {
            String eid = command.substring(14);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_tp_to_entity", eid), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__research_refresh__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("request_inventory", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("research_set_weapon_skill:")) {
            String data = command.substring(25);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_set_weapon_skill", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_clear_weapon_skills__:")) {
            String slot = command.substring(32);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_clear_weapon_skills", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("research_reroll:")) {
            String slot = command.substring(16);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_reroll", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("research_max_rarity:")) {
            String slot = command.substring(20);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_max_rarity", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("research_rarity_up:")) {
            String slot = command.substring(19);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_rarity_up", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("research_rarity_down:")) {
            String slot = command.substring(21);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_rarity_down", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("research_max_relic:")) {
            String slot = command.substring(19);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_max_relic", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_set_base_dmg__:")) {
            String data = command.substring(26);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_set_base_damage", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_set_bonus__:")) {
            String data = command.substring(23);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_set_bonus", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_add_bonus__:")) {
            String slot = command.substring(23);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_add_bonus", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_add_specific__:")) {
            String data = command.substring(26);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_add_specific_bonus", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_set_relic_stat__:")) {
            String data = command.substring(28);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_set_relic_stat", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_set_ability_pts__:")) {
            String data = command.substring(29);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_set_ability_points", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_set_ability_cd__:")) {
            String data = command.substring(28);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_set_ability_cooldown", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_set_weapon_cd__:")) {
            String data = command.substring(27);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_set_weapon_cooldown", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_remove_bonus__:")) {
            String data = command.substring(26);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_remove_bonus", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_set_relic_level__:")) {
            String data = command.substring(29);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_set_relic_level", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_set_relic_quality__:")) {
            String data = command.substring(31);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_set_relic_quality", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_init_weapon__:")) {
            String slot = command.substring(25);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_init_weapon", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_init_armor__:")) {
            String slot = command.substring(24);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_init_armor", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_init_relic__:")) {
            String slot = command.substring(24);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_init_relic", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_set_relic_xp__:")) {
            String data = command.substring(26);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_set_relic_xp", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_reroll_relic__:")) {
            String slot = command.substring(25);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_reroll_relic_stats", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_max_relic_stats__:")) {
            String slot = command.substring(28);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_max_relic_stats", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_reset_relic__:")) {
            String slot = command.substring(24);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_reset_relic", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_add_enchant__:")) {
            String data = command.substring(25);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_add_enchant", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_remove_enchant__:")) {
            String data = command.substring(28);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_remove_enchant", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_set_enchant__:")) {
            String data = command.substring(25);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_set_enchant_level", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_clear_enchants__:")) {
            String data = command.substring(28);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_clear_enchants", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_set_name__:")) {
            String slot = command.substring(22);
            String nameText = this.itemEditorInput != null ? this.itemEditorInput.getValue().trim() : "";
            if (!nameText.isEmpty()) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_set_name", slot + ":" + nameText), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return;
        }
        if (command.startsWith("__research_clear_name__:")) {
            String slot = command.substring(24);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_clear_name", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_add_lore__:")) {
            String slot = command.substring(22);
            String loreText = this.itemEditorInput != null ? this.itemEditorInput.getValue().trim() : "";
            if (!loreText.isEmpty()) {
                ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_add_lore", slot + ":" + loreText), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return;
        }
        if (command.startsWith("__research_clear_lore__:")) {
            String slot = command.substring(24);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_clear_lore", slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__research_rm_lore__:")) {
            String data = command.substring(21);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("research_remove_lore", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__toggle_god__".equals(command)) {
            this.godModeActive = !this.godModeActive;
            if (this.godModeActive) {
                this.sendCommand("effect give @s minecraft:resistance 999999 255 true");
                this.sendCommand("effect give @s minecraft:fire_resistance 999999 0 true");
                this.sendCommand("effect give @s minecraft:water_breathing 999999 0 true");
                this.adminMessages.add("God mode ON");
            } else {
                this.sendCommand("effect clear @s minecraft:resistance");
                this.sendCommand("effect clear @s minecraft:fire_resistance");
                this.sendCommand("effect clear @s minecraft:water_breathing");
                this.adminMessages.add("God mode OFF");
            }
            return;
        }
        if ("__fly_speed__".equals(command)) {
            this.sendCommand("attribute @s minecraft:flying_speed base set 0.1");
            this.adminMessages.add("Fly speed set to 0.1 (default is 0.05)");
            return;
        }
        if ("__fill_museum__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_fill_museum", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__complete_advancements__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_complete_advancements", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__spawn_admin_weapon__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_spawn_admin_weapon", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            this.adminMessages.add("Spawning Admin Weapon...");
            return;
        }
        if ("__spawn_admin_armor__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_spawn_admin_armor", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            this.adminMessages.add("Spawning Admin Armor set...");
            return;
        }
        if ("__unlock_wiki__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("encyclopedia_unlock_all", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if ("__max_skill_tree__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("skill_max_all_trees", ""), (CustomPacketPayload[])new CustomPacketPayload[0]);
            this.adminMessages.add("Maxing all skill trees...");
            return;
        }
        if (command.startsWith("__clear_player_inv__:")) {
            String uuid = command.substring(21);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_clear_player_inv", uuid), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__fill__:")) {
            // Format: __fill__:minecraft:block_id:radius:yOffset
            String data = command.substring(9);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)new ComputerActionPayload("admin_fill_area", data), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return;
        }
        if (command.startsWith("__fill_wall__:")) {
            // Format: __fill_wall__:minecraft:block_id:radius
            // Build hollow fill: walls around the player
            String[] parts = command.substring(14).split(":");
            String block = parts[0] + ":" + parts[1];
            int radius = Integer.parseInt(parts[2]);
            // Use hollow fill to create walls around player (y to y+3)
            this.sendCommand("fill ~" + (-radius) + " ~ ~" + (-radius) + " ~" + radius + " ~3 ~" + radius + " " + block + " hollow");
            return;
        }
        this.sendCommand(command);
    }

    private boolean handleItemClick(int mx, int my, boolean shiftDown) {
        int x = this.contentLeft + 6;
        int baseY = this.contentTop + 6 + 42;
        int cellSize = 24;
        int cols = Math.max(1, (this.contentRight - this.contentLeft - 12) / cellSize);
        String search = this.itemSearchBox != null ? this.itemSearchBox.getValue().toLowerCase().trim() : "";
        ArrayList<Item> filtered = new ArrayList<Item>();
        String cat = this.currentItemCategory;
        if ("All".equals(cat) || "Minecraft".equals(cat) || "MegaMod".equals(cat)) {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == Items.AIR) continue;
                String ns = BuiltInRegistries.ITEM.getKey(item).getNamespace();
                if ("Minecraft".equals(cat) && !"minecraft".equals(ns)) continue;
                if ("MegaMod".equals(cat) && !"megamod".equals(ns)) continue;
                if (!search.isEmpty()) {
                    String itemId = BuiltInRegistries.ITEM.getKey(item).getPath();
                    String displayName = new ItemStack((ItemLike)item).getHoverName().getString().toLowerCase();
                    if (!itemId.contains(search) && !displayName.contains(search)) continue;
                }
                filtered.add(item);
            }
        } else {
            Item[] catItems = CATEGORY_ITEMS.getOrDefault(cat, new Item[0]);
            for (Item item : catItems) {
                if (!search.isEmpty()) {
                    String itemId = BuiltInRegistries.ITEM.getKey(item).getPath();
                    String displayName = new ItemStack((ItemLike)item).getHoverName().getString().toLowerCase();
                    if (!itemId.contains(search) && !displayName.contains(search)) continue;
                }
                filtered.add(item);
            }
        }
        for (int i = 0; i < filtered.size(); ++i) {
            int row = i / cols;
            int col = i % cols;
            int ix = x + col * cellSize;
            int iy = baseY + (row - this.itemScroll) * cellSize;
            if (mx < ix || mx >= ix + cellSize || my < iy || my >= iy + cellSize) continue;
            String id = BuiltInRegistries.ITEM.getKey(filtered.get(i)).toString();
            int amount = shiftDown ? 1 : 64;
            this.sendCommand("give @s " + id + " " + amount);
            return true;
        }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        if (!this.animationComplete) {
            return super.keyPressed(event);
        }
        int keyCode = event.key();
        // Tab to accept suggestion
        if (keyCode == 258 && this.currentTab == 10 && this.commandInput != null && this.commandInput.isFocused() && !this.suggestions.isEmpty()) {
            int idx = this.selectedSuggestion >= 0 ? this.selectedSuggestion : 0;
            if (idx < this.suggestions.size()) {
                String suggestion = this.suggestions.get(idx);
                this.commandInput.setValue(applySuggestion(this.commandInput.getValue(), suggestion));
                this.commandInput.setCursorPosition(this.commandInput.getValue().length());
                this.suggestions.clear();
                this.selectedSuggestion = -1;
                this.lastSuggestionQuery = this.commandInput.getValue();
            }
            return true;
        }
        if (keyCode == 257 && this.currentTab == 10 && this.commandInput != null && this.commandInput.isFocused()) {
            this.executeTerminalCommand();
            return true;
        }
        if (this.currentTab == 10 && this.commandInput != null && this.commandInput.isFocused()) {
            if (keyCode == 265) {
                if (!this.suggestions.isEmpty()) {
                    this.selectedSuggestion = Math.max(0, this.selectedSuggestion - 1);
                } else if (!this.commandHistory.isEmpty() && this.historyIndex > 0) {
                    --this.historyIndex;
                    this.commandInput.setValue(this.commandHistory.get(this.historyIndex));
                }
                return true;
            }
            if (keyCode == 264) {
                if (!this.suggestions.isEmpty()) {
                    this.selectedSuggestion = Math.min(this.suggestions.size() - 1, this.selectedSuggestion + 1);
                } else if (!this.commandHistory.isEmpty()) {
                    this.historyIndex = Math.min(this.historyIndex + 1, this.commandHistory.size());
                    if (this.historyIndex < this.commandHistory.size()) {
                        this.commandInput.setValue(this.commandHistory.get(this.historyIndex));
                    } else {
                        this.commandInput.setValue("");
                    }
                }
                return true;
            }
        }
        // Forward key events to panels with text input
        if (this.currentTab == 14 && this.featureTogglesPanel != null && this.featureTogglesPanel.keyPressed(keyCode, 0, 0)) return true;
        if (this.currentTab == 15 && this.moderationPanel != null && this.moderationPanel.keyPressed(keyCode, 0, 0)) return true;
        if (this.currentTab == 17 && this.schedulerPanel != null && this.schedulerPanel.keyPressed(keyCode, 0, 0)) return true;
        if (this.currentTab == 18 && this.botControlPanel != null && this.botControlPanel.keyPressed(keyCode, 0, 0)) return true;
        if (this.currentTab == 20 && this.adminModulesPanel != null && this.adminModulesPanel.keyPressed(keyCode, 0, 0)) return true;
        if (this.currentTab == 21 && this.warpPanel != null && this.warpPanel.keyPressed(keyCode, 0, 0)) return true;
        // TODO: citizensPanel removed - if (this.currentTab == 22 && this.citizensPanel != null && this.citizensPanel.keyPressed(keyCode, 0, 0)) return true;
        if (this.currentTab == 36 && this.adminSearchPanel != null && this.adminSearchPanel.keyPressed(keyCode, 0, 0)) return true;
        if (this.currentTab == 35 && this.worldEditPanel != null && this.worldEditPanel.keyPressed(keyCode, 0, 0)) return true;
        if (keyCode == 256) {
            if (this.mc != null) {
                this.mc.setScreen(this.parent);
            }
            return true;
        }
        return super.keyPressed(event);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.animationComplete) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        // Sidebar tab scrolling
        if (mouseX < SIDEBAR_WIDTH) {
            int tabH = 17, tabGap = 1;
            int totalTabH = TAB_NAMES.length * (tabH + tabGap);
            int visibleH = (this.height - STATUS_BAR_HEIGHT) - (HEADER_HEIGHT + 4);
            int maxScroll = Math.max(0, totalTabH - visibleH);
            this.tabScroll = (int) Math.max(0, Math.min(maxScroll, this.tabScroll - scrollY * (tabH + tabGap) * 2));
            return true;
        }
        int delta = (int)(-scrollY);
        switch (this.currentTab) {
            case 1: {
                if (this.selectedPlayerUUID != null) {
                    this.playerDetailScroll += (int)(-scrollY * 12.0);
                    this.playerDetailScroll = Math.max(0, this.playerDetailScroll);
                } else {
                    this.playerScroll += delta;
                    this.playerScroll = Math.max(0, this.playerScroll);
                }
                return true;
            }
            case 2: {
                this.worldScroll += (int)(-scrollY * 10.0);
                this.worldScroll = Math.max(0, this.worldScroll);
                return true;
            }
            case 3: {
                this.itemScroll += delta;
                this.itemScroll = Math.max(0, this.itemScroll);
                return true;
            }
            case 4: {
                if ("Dungeon Mgr".equals(this.currentMegamodCategory)) {
                    this.dungeonScroll += (int)(-scrollY * 12.0);
                    this.dungeonScroll = Math.max(0, this.dungeonScroll);
                } else if ("Quests".equals(this.currentMegamodCategory)) {
                    this.questScroll += (int)(-scrollY * 12.0);
                    this.questScroll = Math.max(0, this.questScroll);
                } else {
                    this.megamodScroll += (int)(-scrollY * 12.0);
                    this.megamodScroll = Math.max(0, this.megamodScroll);
                }
                return true;
            }
            case 5: {
                if (this.economyDashboardPanel != null) this.economyDashboardPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
                return true;
            }
            case 6: {
                this.skillScroll += (int)(-scrollY * 12.0);
                this.skillScroll = Math.max(0, this.skillScroll);
                return true;
            }
            case 7: {
                this.auditScroll += (int)(-scrollY * 12.0);
                this.auditScroll = Math.max(0, this.auditScroll);
                return true;
            }
            case 8: {
                this.researchScroll += (int)(-scrollY * 12.0);
                this.researchScroll = Math.max(0, this.researchScroll);
                return true;
            }
            case 9: {
                this.entityScroll += (int)(-scrollY * 12.0);
                this.entityScroll = Math.max(0, this.entityScroll);
                return true;
            }
            case 10: {
                Objects.requireNonNull(this.font);
                int lineH = 9 + 3;
                if ("logs".equals(this.terminalMode)) {
                    this.logScroll -= (int)(scrollY * (double)lineH * 3.0);
                    this.logScroll = Math.max(0, this.logScroll);
                } else {
                    this.terminalScroll -= (int)(scrollY * (double)lineH * 3.0);
                    this.terminalScroll = Math.max(0, this.terminalScroll);
                }
                return true;
            }
            case 11: { if (this.museumPanel != null) this.museumPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 12: { if (this.inventoryPanel != null) this.inventoryPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 13: { if (this.dungeonAnalyticsPanel != null) this.dungeonAnalyticsPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 14: { if (this.featureTogglesPanel != null) this.featureTogglesPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 15: { if (this.moderationPanel != null) this.moderationPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 16: { if (this.structureLocatorPanel != null) this.structureLocatorPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 17: { if (this.schedulerPanel != null) this.schedulerPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 18: { if (this.botControlPanel != null) this.botControlPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 19: { if (this.mobShowcasePanel != null) this.mobShowcasePanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 33: { if (this.mobShowcasePanel != null) this.mobShowcasePanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 20: { if (this.adminModulesPanel != null) this.adminModulesPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 21: { if (this.warpPanel != null) this.warpPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 22: { /* TODO: citizensPanel removed */ return true; }
            case 23: { return true; }
            case 24: { if (this.corruptionPanel != null) this.corruptionPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 25: { if (this.marketplaceAdminPanel != null) this.marketplaceAdminPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 26: { if (this.alchemyAdminPanel != null) this.alchemyAdminPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 27: { if (this.systemHealthPanel != null) this.systemHealthPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 35: { if (this.worldEditPanel != null) this.worldEditPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
            case 36: { if (this.adminSearchPanel != null) this.adminSearchPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY); return true; }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        char ch = (char) event.codepoint();
        if (this.currentTab == 18 && this.botControlPanel != null && this.botControlPanel.charTyped(ch, event.modifiers())) return true;
        if (this.currentTab == 20 && this.adminModulesPanel != null && this.adminModulesPanel.charTyped(ch, event.modifiers())) return true;
        if (this.currentTab == 21 && this.warpPanel != null && this.warpPanel.charTyped(ch, event.modifiers())) return true;
        // TODO: citizensPanel removed - if (this.currentTab == 22 && this.citizensPanel != null && this.citizensPanel.charTyped(ch, event.modifiers())) return true;
        if (this.currentTab == 36 && this.adminSearchPanel != null && this.adminSearchPanel.charTyped(ch, event.modifiers())) return true;
        if (this.currentTab == 35 && this.worldEditPanel != null && this.worldEditPanel.charTyped(ch, event.modifiers())) return true;
        return super.charTyped(event);
    }

    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    public boolean isPauseScreen() {
        return false;
    }

    static {
        CATEGORY_ITEMS.put("Tools", new Item[]{Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE, Items.SHEARS, Items.FLINT_AND_STEEL, Items.FISHING_ROD, Items.LEAD, Items.COMPASS, Items.CLOCK, Items.SPYGLASS, Items.BRUSH});
        CATEGORY_ITEMS.put("Armor", new Item[]{Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, Items.ELYTRA, Items.SHIELD, Items.TURTLE_HELMET, Items.LEATHER_HORSE_ARMOR, Items.IRON_HORSE_ARMOR, Items.GOLDEN_HORSE_ARMOR, Items.DIAMOND_HORSE_ARMOR, Items.SADDLE});
        CATEGORY_ITEMS.put("Food", new Item[]{Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.GOLDEN_CARROT, Items.BREAD, Items.CAKE, Items.COOKIE, Items.PUMPKIN_PIE, Items.MUSHROOM_STEW, Items.HONEY_BOTTLE, Items.CHORUS_FRUIT, Items.COOKED_SALMON, Items.COOKED_CHICKEN, Items.DRIED_KELP, Items.SWEET_BERRIES});
        CATEGORY_ITEMS.put("Blocks", new Item[]{Items.DIAMOND_BLOCK, Items.EMERALD_BLOCK, Items.IRON_BLOCK, Items.GOLD_BLOCK, Items.NETHERITE_BLOCK, Items.LAPIS_BLOCK, Items.REDSTONE_BLOCK, Items.COAL_BLOCK, Items.GLASS, Items.GLOWSTONE, Items.SEA_LANTERN, Items.OBSIDIAN, Items.TNT, Items.BEACON, Items.ENDER_CHEST, Items.CRAFTING_TABLE});
        CATEGORY_ITEMS.put("Redstone", new Item[]{Items.REDSTONE, Items.REPEATER, Items.COMPARATOR, Items.PISTON, Items.STICKY_PISTON, Items.OBSERVER, Items.HOPPER, Items.DROPPER, Items.DISPENSER, Items.LEVER, Items.DAYLIGHT_DETECTOR, Items.REDSTONE_LAMP, Items.REDSTONE_TORCH, Items.TRIPWIRE_HOOK, Items.TARGET, Items.NOTE_BLOCK});
        CATEGORY_ITEMS.put("Combat", new Item[]{Items.DIAMOND_SWORD, Items.NETHERITE_SWORD, Items.BOW, Items.CROSSBOW, Items.TRIDENT, Items.MACE, Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW, Items.FIREWORK_ROCKET, Items.ENDER_PEARL, Items.TOTEM_OF_UNDYING, Items.END_CRYSTAL, Items.EXPERIENCE_BOTTLE, Items.TNT, Items.RESPAWN_ANCHOR});
        CATEGORY_ITEMS.put("Brewing", new Item[]{Items.BREWING_STAND, Items.BLAZE_POWDER, Items.NETHER_WART, Items.GHAST_TEAR, Items.MAGMA_CREAM, Items.FERMENTED_SPIDER_EYE, Items.GLISTERING_MELON_SLICE, Items.GOLDEN_CARROT, Items.RABBIT_FOOT, Items.PHANTOM_MEMBRANE, Items.GLASS_BOTTLE, Items.DRAGON_BREATH, Items.REDSTONE, Items.GLOWSTONE_DUST, Items.GUNPOWDER, Items.SUGAR});
        MEGAMOD_CATEGORIES = new String[]{"Relics", "Weapons", "D.Weapons", "D.Items", "Masks", "Trophies", "Museum", "Computer", "Furniture", "Dungeon Mgr", "Quests"};
        MEGAMOD_ITEMS = new LinkedHashMap<String, List<MegamodEntry>>();
        ArrayList<MegamodEntry> relics = new ArrayList<MegamodEntry>();
        relics.add(new MegamodEntry("Arrow Quiver", "megamod:arrow_quiver", (Supplier<? extends Item>)RelicRegistry.ARROW_QUIVER));
        relics.add(new MegamodEntry("Elytra Booster", "megamod:elytra_booster", (Supplier<? extends Item>)RelicRegistry.ELYTRA_BOOSTER));
        relics.add(new MegamodEntry("Midnight Robe", "megamod:midnight_robe", (Supplier<? extends Item>)RelicRegistry.MIDNIGHT_ROBE));
        relics.add(new MegamodEntry("Leather Belt", "megamod:leather_belt", (Supplier<? extends Item>)RelicRegistry.LEATHER_BELT));
        relics.add(new MegamodEntry("Drowned Belt", "megamod:drowned_belt", (Supplier<? extends Item>)RelicRegistry.DROWNED_BELT));
        relics.add(new MegamodEntry("Hunter Belt", "megamod:hunter_belt", (Supplier<? extends Item>)RelicRegistry.HUNTER_BELT));
        relics.add(new MegamodEntry("Ender Hand", "megamod:ender_hand", (Supplier<? extends Item>)RelicRegistry.ENDER_HAND));
        relics.add(new MegamodEntry("Rage Glove", "megamod:rage_glove", (Supplier<? extends Item>)RelicRegistry.RAGE_GLOVE));
        relics.add(new MegamodEntry("Wool Mitten", "megamod:wool_mitten", (Supplier<? extends Item>)RelicRegistry.WOOL_MITTEN));
        relics.add(new MegamodEntry("Magma Walker", "megamod:magma_walker", (Supplier<? extends Item>)RelicRegistry.MAGMA_WALKER));
        relics.add(new MegamodEntry("Aqua Walker", "megamod:aqua_walker", (Supplier<? extends Item>)RelicRegistry.AQUA_WALKER));
        relics.add(new MegamodEntry("Ice Skates", "megamod:ice_skates", (Supplier<? extends Item>)RelicRegistry.ICE_SKATES));
        relics.add(new MegamodEntry("Ice Breaker", "megamod:ice_breaker", (Supplier<? extends Item>)RelicRegistry.ICE_BREAKER));
        relics.add(new MegamodEntry("Roller Skates", "megamod:roller_skates", (Supplier<? extends Item>)RelicRegistry.ROLLER_SKATES));
        relics.add(new MegamodEntry("Amphibian Boot", "megamod:amphibian_boot", (Supplier<? extends Item>)RelicRegistry.AMPHIBIAN_BOOT));
        relics.add(new MegamodEntry("Reflection Necklace", "megamod:reflection_necklace", (Supplier<? extends Item>)RelicRegistry.REFLECTION_NECKLACE));
        relics.add(new MegamodEntry("Jellyfish Necklace", "megamod:jellyfish_necklace", (Supplier<? extends Item>)RelicRegistry.JELLYFISH_NECKLACE));
        relics.add(new MegamodEntry("Holy Locket", "megamod:holy_locket", (Supplier<? extends Item>)RelicRegistry.HOLY_LOCKET));
        relics.add(new MegamodEntry("Bastion Ring", "megamod:bastion_ring", (Supplier<? extends Item>)RelicRegistry.BASTION_RING));
        relics.add(new MegamodEntry("Chorus Inhibitor", "megamod:chorus_inhibitor", (Supplier<? extends Item>)RelicRegistry.CHORUS_INHIBITOR));
        relics.add(new MegamodEntry("Shadow Glaive", "megamod:shadow_glaive", (Supplier<? extends Item>)RelicRegistry.SHADOW_GLAIVE));
        relics.add(new MegamodEntry("Infinity Ham", "megamod:infinity_ham", (Supplier<? extends Item>)RelicRegistry.INFINITY_HAM));
        relics.add(new MegamodEntry("Space Dissector", "megamod:space_dissector", (Supplier<? extends Item>)RelicRegistry.SPACE_DISSECTOR));
        relics.add(new MegamodEntry("Magic Mirror", "megamod:magic_mirror", (Supplier<? extends Item>)RelicRegistry.MAGIC_MIRROR));
        relics.add(new MegamodEntry("Horse Flute", "megamod:horse_flute", (Supplier<? extends Item>)RelicRegistry.HORSE_FLUTE));
        relics.add(new MegamodEntry("Spore Sack", "megamod:spore_sack", (Supplier<? extends Item>)RelicRegistry.SPORE_SACK));
        relics.add(new MegamodEntry("Blazing Flask", "megamod:blazing_flask", (Supplier<? extends Item>)RelicRegistry.BLAZING_FLASK));
        relics.add(new MegamodEntry("Researching Table", "megamod:researching_table", (Supplier<? extends Item>)RelicRegistry.RESEARCHING_TABLE_ITEM));
        MEGAMOD_ITEMS.put("Relics", relics);
        ArrayList<MegamodEntry> weapons = new ArrayList<MegamodEntry>();
        weapons.add(new MegamodEntry("Lunar Crown", "megamod:lunar_crown", (Supplier<? extends Item>)RelicRegistry.LUNAR_CROWN));
        weapons.add(new MegamodEntry("Solar Crown", "megamod:solar_crown", (Supplier<? extends Item>)RelicRegistry.SOLAR_CROWN));
        weapons.add(new MegamodEntry("Vampiric Tome", "megamod:vampiric_tome", (Supplier<? extends Item>)RelicRegistry.VAMPIRIC_TOME));
        weapons.add(new MegamodEntry("Static Seeker", "megamod:static_seeker", (Supplier<? extends Item>)RelicRegistry.STATIC_SEEKER));
        weapons.add(new MegamodEntry("Battledancer", "megamod:battledancer", (Supplier<? extends Item>)RelicRegistry.BATTLEDANCER));
        weapons.add(new MegamodEntry("Ebonchill", "megamod:ebonchill", (Supplier<? extends Item>)RelicRegistry.EBONCHILL));
        weapons.add(new MegamodEntry("Lightbinder", "megamod:lightbinder", (Supplier<? extends Item>)RelicRegistry.LIGHTBINDER));
        weapons.add(new MegamodEntry("Crescent Blade", "megamod:crescent_blade", (Supplier<? extends Item>)RelicRegistry.CRESCENT_BLADE));
        weapons.add(new MegamodEntry("Ghost Fang", "megamod:ghost_fang", (Supplier<? extends Item>)RelicRegistry.GHOST_FANG));
        weapons.add(new MegamodEntry("Terra Warhammer", "megamod:terra_warhammer", (Supplier<? extends Item>)RelicRegistry.TERRA_WARHAMMER));
        MEGAMOD_ITEMS.put("Weapons", weapons);
        // D.Weapons — all dungeon weapons and armor
        ArrayList<MegamodEntry> dWeapons = new ArrayList<MegamodEntry>();
        dWeapons.add(new MegamodEntry("Ossukage Sword", "megamod:ossukage_sword", (Supplier<? extends Item>)DungeonEntityRegistry.OSSUKAGE_SWORD));
        dWeapons.add(new MegamodEntry("Naga Fang Dagger", "megamod:naga_fang_dagger", (Supplier<? extends Item>)DungeonEntityRegistry.NAGA_FANG_DAGGER));
        dWeapons.add(new MegamodEntry("Wrought Axe", "megamod:wrought_axe", (Supplier<? extends Item>)DungeonEntityRegistry.WROUGHT_AXE));
        dWeapons.add(new MegamodEntry("Wrought Helm", "megamod:wrought_helm", (Supplier<? extends Item>)DungeonEntityRegistry.WROUGHT_HELM));
        dWeapons.add(new MegamodEntry("Ice Crystal", "megamod:ice_crystal", (Supplier<? extends Item>)DungeonEntityRegistry.ICE_CRYSTAL));
        dWeapons.add(new MegamodEntry("Spear", "megamod:spear", (Supplier<? extends Item>)DungeonEntityRegistry.SPEAR));
        dWeapons.add(new MegamodEntry("Life Stealer", "megamod:life_stealer", (Supplier<? extends Item>)DungeonEntityRegistry.LIFE_STEALER));
        dWeapons.add(new MegamodEntry("Scepter of Chaos", "megamod:scepter_of_chaos", (Supplier<? extends Item>)DungeonEntityRegistry.SCEPTER_OF_CHAOS));
        dWeapons.add(new MegamodEntry("Sol Visage", "megamod:sol_visage", (Supplier<? extends Item>)DungeonEntityRegistry.SOL_VISAGE));
        dWeapons.add(new MegamodEntry("Earthrend Gauntlet", "megamod:earthrend_gauntlet", (Supplier<? extends Item>)DungeonEntityRegistry.EARTHREND_GAUNTLET));
        dWeapons.add(new MegamodEntry("Blowgun", "megamod:blowgun", (Supplier<? extends Item>)DungeonEntityRegistry.BLOWGUN));
        dWeapons.add(new MegamodEntry("Geomancer Helm", "megamod:geomancer_helm", (Supplier<? extends Item>)DungeonEntityRegistry.GEOMANCER_HELM));
        dWeapons.add(new MegamodEntry("Geomancer Chest", "megamod:geomancer_chest", (Supplier<? extends Item>)DungeonEntityRegistry.GEOMANCER_CHEST));
        dWeapons.add(new MegamodEntry("Geomancer Legs", "megamod:geomancer_legs", (Supplier<? extends Item>)DungeonEntityRegistry.GEOMANCER_LEGS));
        dWeapons.add(new MegamodEntry("Geomancer Boots", "megamod:geomancer_boots", (Supplier<? extends Item>)DungeonEntityRegistry.GEOMANCER_BOOTS));
        MEGAMOD_ITEMS.put("D.Weapons", dWeapons);
        // D.Items — keys, materials, consumables, mob drops, blocks
        ArrayList<MegamodEntry> dItems = new ArrayList<MegamodEntry>();
        dItems.add(new MegamodEntry("Key (Normal)", "megamod:dungeon_key_normal", (Supplier<? extends Item>)DungeonRegistry.DUNGEON_KEY_NORMAL));
        dItems.add(new MegamodEntry("Key (Hard)", "megamod:dungeon_key_hard", (Supplier<? extends Item>)DungeonRegistry.DUNGEON_KEY_HARD));
        dItems.add(new MegamodEntry("Key (Nightmare)", "megamod:dungeon_key_nightmare", (Supplier<? extends Item>)DungeonRegistry.DUNGEON_KEY_NIGHTMARE));
        dItems.add(new MegamodEntry("Key (Infernal)", "megamod:dungeon_key_infernal", (Supplier<? extends Item>)DungeonRegistry.DUNGEON_KEY_INFERNAL));
        dItems.add(new MegamodEntry("Dungeon Mini Key", "megamod:dungeon_mini_key", (Supplier<? extends Item>)DungeonEntityRegistry.DUNGEON_MINI_KEY));
        dItems.add(new MegamodEntry("Soul Anchor", "megamod:soul_anchor", (Supplier<? extends Item>)DungeonRegistry.SOUL_ANCHOR));
        dItems.add(new MegamodEntry("Dungeon Map", "megamod:dungeon_map", (Supplier<? extends Item>)DungeonExclusiveItems.DUNGEON_MAP));
        dItems.add(new MegamodEntry("Warp Stone", "megamod:warp_stone", (Supplier<? extends Item>)DungeonExclusiveItems.WARP_STONE));
        dItems.add(new MegamodEntry("Infernal Essence", "megamod:infernal_essence", (Supplier<? extends Item>)DungeonExclusiveItems.INFERNAL_ESSENCE));
        dItems.add(new MegamodEntry("Void Shard", "megamod:void_shard", (Supplier<? extends Item>)DungeonExclusiveItems.VOID_SHARD));
        dItems.add(new MegamodEntry("Cerulean Ingot", "megamod:cerulean_ingot", (Supplier<? extends Item>)DungeonExclusiveItems.CERULEAN_INGOT));
        dItems.add(new MegamodEntry("Crystalline Shard", "megamod:crystalline_shard", (Supplier<? extends Item>)DungeonExclusiveItems.CRYSTALLINE_SHARD));
        dItems.add(new MegamodEntry("Spectral Silk", "megamod:spectral_silk", (Supplier<? extends Item>)DungeonExclusiveItems.SPECTRAL_SILK));
        dItems.add(new MegamodEntry("Umbra Ingot", "megamod:umbra_ingot", (Supplier<? extends Item>)DungeonExclusiveItems.UMBRA_INGOT));
        dItems.add(new MegamodEntry("Cerulean Arrow", "megamod:cerulean_arrow", (Supplier<? extends Item>)DungeonEntityRegistry.CERULEAN_ARROW_ITEM));
        dItems.add(new MegamodEntry("Crystal Arrow", "megamod:crystal_arrow", (Supplier<? extends Item>)DungeonEntityRegistry.CRYSTAL_ARROW_ITEM));
        dItems.add(new MegamodEntry("Dart Ammo", "megamod:dart_ammo", (Supplier<? extends Item>)DungeonEntityRegistry.DART_ITEM));
        dItems.add(new MegamodEntry("Rat Fang", "megamod:rat_fang", (Supplier<? extends Item>)DungeonEntityRegistry.RAT_FANG));
        dItems.add(new MegamodEntry("Fang on a Stick", "megamod:fang_on_a_stick", (Supplier<? extends Item>)DungeonEntityRegistry.FANG_ON_A_STICK));
        dItems.add(new MegamodEntry("Skeleton Bone", "megamod:skeleton_bone", (Supplier<? extends Item>)DungeonEntityRegistry.SKELETON_BONE));
        dItems.add(new MegamodEntry("Skeleton Head", "megamod:skeleton_head", (Supplier<? extends Item>)DungeonEntityRegistry.SKELETON_HEAD));
        dItems.add(new MegamodEntry("Glowing Jelly", "megamod:glowing_jelly", (Supplier<? extends Item>)DungeonEntityRegistry.GLOWING_JELLY));
        dItems.add(new MegamodEntry("Strange Meat", "megamod:strange_meat", (Supplier<? extends Item>)DungeonExclusiveItems.STRANGE_MEAT));
        dItems.add(new MegamodEntry("Foliaath Seed", "megamod:foliaath_seed", (Supplier<? extends Item>)DungeonEntityRegistry.FOLIAATH_SEED));
        dItems.add(new MegamodEntry("Captured Grottol", "megamod:captured_grottol", (Supplier<? extends Item>)DungeonEntityRegistry.CAPTURED_GROTTOL));
        dItems.add(new MegamodEntry("Bluff Rod", "megamod:bluff_rod", (Supplier<? extends Item>)DungeonEntityRegistry.BLUFF_ROD));
        dItems.add(new MegamodEntry("Great XP Bottle", "megamod:great_experience_bottle", (Supplier<? extends Item>)DungeonEntityRegistry.GREAT_EXPERIENCE_BOTTLE));
        dItems.add(new MegamodEntry("Living Divining Rod", "megamod:living_divining_rod", (Supplier<? extends Item>)DungeonExclusiveItems.LIVING_DIVINING_ROD));
        dItems.add(new MegamodEntry("Absorption Orb", "megamod:absorption_orb", (Supplier<? extends Item>)DungeonExclusiveItems.ABSORPTION_ORB));
        dItems.add(new MegamodEntry("Boss Trophy", "megamod:boss_trophy", (Supplier<? extends Item>)DungeonExclusiveItems.BOSS_TROPHY));
        dItems.add(new MegamodEntry("Rat Spawn Egg", "megamod:dungeon_rat_spawn_egg", (Supplier<? extends Item>)DungeonEntityRegistry.DUNGEON_RAT_SPAWN_EGG));
        dItems.add(new MegamodEntry("Dungeon Altar", "megamod:dungeon_altar", (Supplier<? extends Item>)DungeonEntityRegistry.DUNGEON_ALTAR_ITEM));
        dItems.add(new MegamodEntry("Fog Wall", "megamod:fog_wall", (Supplier<? extends Item>)DungeonEntityRegistry.FOG_WALL_ITEM));
        dItems.add(new MegamodEntry("Spike Block", "megamod:spike_block", (Supplier<? extends Item>)DungeonEntityRegistry.SPIKE_BLOCK_ITEM));
        dItems.add(new MegamodEntry("Explosive Barrel", "megamod:explosive_barrel", (Supplier<? extends Item>)DungeonEntityRegistry.EXPLOSIVE_BARREL_ITEM));
        dItems.add(new MegamodEntry("Wall Rack", "megamod:wall_rack", (Supplier<? extends Item>)DungeonEntityRegistry.WALL_RACK_ITEM));
        dItems.add(new MegamodEntry("Music Disc", "megamod:music_disc_house_money", (Supplier<? extends Item>)DungeonRegistry.MUSIC_DISC_HOUSE_MONEY));
        MEGAMOD_ITEMS.put("D.Items", dItems);
        // Masks
        ArrayList<MegamodEntry> masks = new ArrayList<MegamodEntry>();
        masks.add(new MegamodEntry("Mask of Fear", "megamod:mask_of_fear", (Supplier<? extends Item>)DungeonEntityRegistry.MASK_OF_FEAR));
        masks.add(new MegamodEntry("Mask of Fury", "megamod:mask_of_fury", (Supplier<? extends Item>)DungeonEntityRegistry.MASK_OF_FURY));
        masks.add(new MegamodEntry("Mask of Faith", "megamod:mask_of_faith", (Supplier<? extends Item>)DungeonEntityRegistry.MASK_OF_FAITH));
        masks.add(new MegamodEntry("Mask of Rage", "megamod:mask_of_rage", (Supplier<? extends Item>)DungeonEntityRegistry.MASK_OF_RAGE));
        masks.add(new MegamodEntry("Mask of Misery", "megamod:mask_of_misery", (Supplier<? extends Item>)DungeonEntityRegistry.MASK_OF_MISERY));
        masks.add(new MegamodEntry("Mask of Bliss", "megamod:mask_of_bliss", (Supplier<? extends Item>)DungeonEntityRegistry.MASK_OF_BLISS));
        MEGAMOD_ITEMS.put("Masks", masks);
        // Trophies
        ArrayList<MegamodEntry> trophies = new ArrayList<MegamodEntry>();
        trophies.add(new MegamodEntry("Wraith Trophy", "megamod:wraith_trophy", (Supplier<? extends Item>)DungeonExclusiveItems.WRAITH_TROPHY_ITEM));
        trophies.add(new MegamodEntry("Ossukage Trophy", "megamod:ossukage_trophy", (Supplier<? extends Item>)DungeonExclusiveItems.OSSUKAGE_TROPHY_ITEM));
        trophies.add(new MegamodEntry("Keeper Trophy", "megamod:dungeon_keeper_trophy", (Supplier<? extends Item>)DungeonExclusiveItems.DUNGEON_KEEPER_TROPHY_ITEM));
        trophies.add(new MegamodEntry("Frostmaw Trophy", "megamod:frostmaw_trophy", (Supplier<? extends Item>)DungeonExclusiveItems.FROSTMAW_TROPHY_ITEM));
        trophies.add(new MegamodEntry("Wroughtnaut Trophy", "megamod:wroughtnaut_trophy", (Supplier<? extends Item>)DungeonExclusiveItems.WROUGHTNAUT_TROPHY_ITEM));
        trophies.add(new MegamodEntry("Umvuthi Trophy", "megamod:umvuthi_trophy", (Supplier<? extends Item>)DungeonExclusiveItems.UMVUTHI_TROPHY_ITEM));
        trophies.add(new MegamodEntry("Chaos Trophy", "megamod:chaos_spawner_trophy", (Supplier<? extends Item>)DungeonExclusiveItems.CHAOS_SPAWNER_TROPHY_ITEM));
        trophies.add(new MegamodEntry("Sculptor Trophy", "megamod:sculptor_trophy", (Supplier<? extends Item>)DungeonExclusiveItems.SCULPTOR_TROPHY_ITEM));
        MEGAMOD_ITEMS.put("Trophies", trophies);
        ArrayList<MegamodEntry> museum = new ArrayList<MegamodEntry>();
        museum.add(new MegamodEntry("Museum Block", "megamod:museum", (Supplier<? extends Item>)MuseumRegistry.MUSEUM_BLOCK_ITEM));
        museum.add(new MegamodEntry("Museum Door", "megamod:museum_door", (Supplier<? extends Item>)MuseumRegistry.MUSEUM_DOOR_ITEM));
        museum.add(new MegamodEntry("Mob Net", "megamod:mob_net", (Supplier<? extends Item>)MuseumRegistry.MOB_NET_ITEM));
        museum.add(new MegamodEntry("Captured Mob", "megamod:captured_mob", (Supplier<? extends Item>)MuseumRegistry.CAPTURED_MOB_ITEM));
        MEGAMOD_ITEMS.put("Museum", museum);
        // Computer & ATM
        ArrayList<MegamodEntry> computer = new ArrayList<MegamodEntry>();
        computer.add(new MegamodEntry("Computer", "megamod:computer", (Supplier<? extends Item>)ComputerRegistry.COMPUTER_ITEM));
        computer.add(new MegamodEntry("ATM", "megamod:atm", (Supplier<? extends Item>)ComputerRegistry.ATM_ITEM));
        computer.add(new MegamodEntry("Phone", "megamod:phone", (Supplier<? extends Item>)ComputerRegistry.PHONE_ITEM));
        MEGAMOD_ITEMS.put("Computer", computer);
        // Furniture
        ArrayList<MegamodEntry> furniture = new ArrayList<MegamodEntry>();
        furniture.add(new MegamodEntry("Sofa", "megamod:office_sofa", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_SOFA_ITEM));
        furniture.add(new MegamodEntry("Large Sofa", "megamod:office_sofa_large", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_SOFA_LARGE_ITEM));
        furniture.add(new MegamodEntry("Conf. Table", "megamod:office_conference_table", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_CONFERENCE_TABLE_ITEM));
        furniture.add(new MegamodEntry("Filing Cabinet", "megamod:office_filing_cabinet", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_FILING_CABINET_ITEM));
        furniture.add(new MegamodEntry("Printer", "megamod:office_printer", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_PRINTER_ITEM));
        furniture.add(new MegamodEntry("Potted Plant", "megamod:office_potted_plant", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_POTTED_PLANT_ITEM));
        furniture.add(new MegamodEntry("Lamp", "megamod:office_lamp", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_LAMP_ITEM));
        furniture.add(new MegamodEntry("Rubbish Bin", "megamod:office_rubbish_bin", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_RUBBISH_BIN_ITEM));
        furniture.add(new MegamodEntry("Bookshelf", "megamod:office_bookshelf", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_BOOKSHELF_ITEM));
        furniture.add(new MegamodEntry("Cupboard", "megamod:office_cupboard", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_CUPBOARD_ITEM));
        furniture.add(new MegamodEntry("Projector", "megamod:office_projector", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_PROJECTOR_ITEM));
        furniture.add(new MegamodEntry("Board (Sm)", "megamod:office_board_small", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_BOARD_SMALL_ITEM));
        furniture.add(new MegamodEntry("Board (Lg)", "megamod:office_board_large", (Supplier<? extends Item>)com.ultra.megamod.feature.furniture.FurnitureRegistry.OFFICE_BOARD_LARGE_ITEM));
        MEGAMOD_ITEMS.put("Furniture", furniture);
        GAMERULES = new String[]{"keepInventory", "mobGriefing", "doDaylightCycle", "doWeatherCycle", "naturalRegeneration", "pvp", "showDeathMessages", "announceAdvancements", "doImmediateRespawn", "drowningDamage", "fallDamage", "fireDamage", "doTileDrops", "doEntityDrops", "disableRaids", "sendCommandFeedback", "doLimitedCrafting"};
    }

    // ==================== Dark Theme Helpers ====================

    private static void darkCard(GuiGraphics g, int x, int y, int w, int h, boolean hovered) {
        int bg = hovered ? 0xFF1C2128 : 0xFF161B22;
        int border = hovered ? 0xFF58A6FF : 0xFF30363D;
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
    }

    private static void darkCard(GuiGraphics g, int x, int y, int w, int h) {
        darkCard(g, x, y, w, h, false);
    }

    private static void darkRowBg(GuiGraphics g, int x, int y, int w, int h, boolean even) {
        int bg = even ? 0xFF161B22 : 0xFF0D1117;
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 1, 0xFF21262D);
    }

    private static void darkProgressBar(GuiGraphics g, int x, int y, int w, int h, float progress, int fillColor) {
        g.fill(x, y, x + w, y + h, 0xFF0D1117);
        g.fill(x, y, x + w, y + 1, 0xFF30363D);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF30363D);
        g.fill(x, y, x + 1, y + h, 0xFF30363D);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF30363D);
        int fillW = Math.round((w - 2) * Math.max(0, Math.min(1, progress)));
        if (fillW > 0) {
            g.fill(x + 1, y + 1, x + 1 + fillW, y + h - 1, fillColor);
        }
    }

    private static String toRoman(int n) {
        if (n <= 0) return "0";
        if (n == 1) return "I"; if (n == 2) return "II"; if (n == 3) return "III";
        if (n == 4) return "IV"; if (n == 5) return "V"; if (n == 6) return "VI";
        if (n == 7) return "VII"; if (n == 8) return "VIII"; if (n == 9) return "IX";
        if (n == 10) return "X"; return String.valueOf(n);
    }

    private void darkBtn(GuiGraphics g, int x, int y, int w, int h, boolean hovered) {
        darkBtn(g, x, y, w, h, hovered, false);
    }

    private void darkBtn(GuiGraphics g, int x, int y, int w, int h, boolean hovered, boolean active) {
        int bg = active ? 0xFF21262D : (hovered ? 0xFF30363D : 0xFF21262D);
        int border = active ? 0xFF58A6FF : (hovered ? 0xFF58A6FF : 0xFF30363D);
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
    }

    private void darkIconBtn(GuiGraphics g, int x, int y, int size, String icon, int color, boolean hovered) {
        int bg = hovered ? 0xFF30363D : 0xFF21262D;
        g.fill(x, y, x + size, y + size, 0xFF30363D);
        g.fill(x + 1, y + 1, x + size - 1, y + size - 1, bg);
        int tw = this.font.width(icon);
        g.drawString(this.font, icon, x + (size - tw) / 2, y + (size - 9) / 2, hovered ? 0xFFFFFFFF : color, false);
    }

    private static void darkDivider(GuiGraphics g, int x, int y, int w) {
        g.fill(x, y, x + w, y + 1, 0xFF30363D);
    }

    private static void darkScrollbar(GuiGraphics g, int x, int y, int h, float progress) {
        int trackW = 6;
        g.fill(x, y, x + trackW, y + h, 0xFF0D1117);
        int thumbH = Math.max(16, h / 5);
        int thumbTravel = h - thumbH;
        int thumbY = y + Math.round(thumbTravel * Math.max(0, Math.min(1, progress)));
        g.fill(x, thumbY, x + trackW, thumbY + thumbH, 0xFF30363D);
        g.fill(x + 1, thumbY + 1, x + trackW - 1, thumbY + thumbH - 1, 0xFF58A6FF);
    }

    private static void darkSlot(GuiGraphics g, int x, int y, int size, boolean hovered) {
        int bg = hovered ? 0xFF21262D : 0xFF0D1117;
        int border = hovered ? 0xFF58A6FF : 0xFF30363D;
        g.fill(x, y, x + size, y + size, border);
        g.fill(x + 1, y + 1, x + size - 1, y + size - 1, bg);
    }

    private static void darkTooltip(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF30363D);
        g.fill(x, y, x + w, y + h, 0xF0161B22);
    }

    // ==================== NEW TAB RENDER METHODS ====================

    private void renderDeathLog(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        int contentW = this.contentRight - this.contentLeft - 12;
        this.drawSectionHeader(g, "Death Log (recent deaths)", x, y, contentW);
        this.drawActionButton(g, x + contentW - 60, y - 2, 60, 16, "Refresh", mouseX, mouseY, "__send__:deathlog_request:");
        y += 18;

        if (this.deathLogLines == null || this.deathLogLines.isEmpty()) {
            g.drawString(this.font, "No deaths recorded. Click Refresh.", x, y, 0xFF8B949E, false);
            return;
        }
        g.enableScissor(this.contentLeft, y, this.contentRight, this.contentBottom);
        for (int i = 0; i < this.deathLogLines.size() && y < this.contentBottom; i++) {
            String[] dl = this.deathLogLines.get(i);
            // dl = [name, x, y, z, dim, cause, itemCount, timeAgo]
            darkRowBg(g, x, y, contentW, 24, i % 2 == 0);
            g.drawString(this.font, dl[0], x + 4, y + 2, 0xFFF85149, false);
            g.drawString(this.font, dl[5], x + 80, y + 2, 0xFFC9D1D9, false);
            g.drawString(this.font, dl[1] + ", " + dl[2] + ", " + dl[3] + " [" + dl[4] + "]", x + 4, y + 13, 0xFF8B949E, false);
            g.drawString(this.font, dl[6] + " items", x + contentW - 110, y + 2, 0xFFD29922, false);
            this.drawActionButton(g, x + contentW - 40, y + 4, 36, 14, "TP", mouseX, mouseY, "__send__:deathlog_tp:" + dl[1] + ":" + dl[2] + ":" + dl[3] + ":" + dl[4]);
            y += 24;
        }
        g.disableScissor();
    }

    private void renderCleanup(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        int contentW = this.contentRight - this.contentLeft - 12;
        this.drawSectionHeader(g, "Entity Cleanup Tool", x, y, contentW);
        y += 18;

        // Vanish toggle
        this.drawActionButton(g, x, y, 100, 18, "Toggle Vanish", mouseX, mouseY, "__send__:vanish_toggle:");
        y += 24;
        darkDivider(g, x, y, contentW);
        y += 6;

        g.drawString(this.font, "Quick Cleanup:", x, y, 0xFFA371F7, false);
        y += 14;
        this.drawActionButton(g, x, y, 110, 18, "Kill Hostiles", mouseX, mouseY, "__send__:admin_kill_hostile:");
        this.drawActionButton(g, x + 115, y, 110, 18, "Kill All Mobs", mouseX, mouseY, "kill @e[type=!player,type=!item]");
        this.drawActionButton(g, x + 230, y, 110, 18, "Clear Items", mouseX, mouseY, "__send__:cleanup_items:");
        y += 24;
        this.drawActionButton(g, x, y, 140, 18, "Clear Dim Entities", mouseX, mouseY, "__send__:cleanup_dimension:");
        y += 24;

        darkDivider(g, x, y, contentW);
        y += 6;
        g.drawString(this.font, "By Radius (from you):", x, y, 0xFFA371F7, false);
        y += 14;
        this.drawActionButton(g, x, y, 80, 18, "R=25 All", mouseX, mouseY, "__send__:cleanup_radius:25:all");
        this.drawActionButton(g, x + 85, y, 80, 18, "R=50 All", mouseX, mouseY, "__send__:cleanup_radius:50:all");
        this.drawActionButton(g, x + 170, y, 80, 18, "R=100 All", mouseX, mouseY, "__send__:cleanup_radius:100:all");
        y += 24;

        darkDivider(g, x, y, contentW);
        y += 6;
        g.drawString(this.font, "By Entity Type:", x, y, 0xFFA371F7, false);
        y += 14;
        String[][] types = {
            {"Zombies", "minecraft:zombie"}, {"Skeletons", "minecraft:skeleton"},
            {"Creepers", "minecraft:creeper"}, {"Spiders", "minecraft:spider"},
            {"Endermen", "minecraft:enderman"}, {"Phantoms", "minecraft:phantom"},
            {"Drowned", "minecraft:drowned"}, {"Pillagers", "minecraft:pillager"},
            {"Slimes", "minecraft:slime"}, {"Witches", "minecraft:witch"},
            {"XP Orbs", "minecraft:experience_orb"}, {"Arrows", "minecraft:arrow"},
        };
        int bx = x;
        for (String[] t : types) {
            int bw = this.font.width(t[0]) + 10;
            if (bx + bw > x + contentW) { bx = x; y += 22; }
            this.drawActionButton(g, bx, y, bw, 18, t[0], mouseX, mouseY, "__send__:cleanup_by_type:" + t[1]);
            bx += bw + 4;
        }
    }

    private void renderLootTables(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        int contentW = this.contentRight - this.contentLeft - 12;
        this.drawSectionHeader(g, "Custom Loot Tables (extra mob drops)", x, y, contentW);
        this.drawActionButton(g, x + contentW - 60, y - 2, 60, 16, "Refresh", mouseX, mouseY, "__send__:loot_request:");
        y += 18;

        // Add form
        g.drawString(this.font, "Add: mobId|itemId|min|max|chance (use input below)", x, y, 0xFF8B949E, false);
        y += 12;
        if (this.lootInput != null) {
            this.lootInput.setPosition(x, y);
            this.lootInput.setWidth(contentW - 70);
            this.lootInput.render(g, mouseX, mouseY, 0);
        }
        this.drawActionButton(g, x + contentW - 60, y, 56, 18, "Add", mouseX, mouseY, "__loot_add__");
        y += 24;
        darkDivider(g, x, y, contentW);
        y += 6;

        if (this.lootTableData == null || this.lootTableData.isEmpty()) {
            g.drawString(this.font, "No custom drops. Click Refresh.", x, y, 0xFF8B949E, false);
            return;
        }
        g.enableScissor(this.contentLeft, y, this.contentRight, this.contentBottom);
        for (int i = 0; i < this.lootTableData.size() && y < this.contentBottom; i++) {
            String[] ld = this.lootTableData.get(i);
            // ld = [mobId, itemId, min, max, chance, index]
            darkRowBg(g, x, y, contentW, 16, i % 2 == 0);
            g.drawString(this.font, ld[0] + " -> " + ld[1] + " x" + ld[2] + "-" + ld[3] + " @" + ld[4], x + 4, y + 3, 0xFFC9D1D9, false);
            this.drawActionButton(g, x + contentW - 40, y + 1, 36, 14, "Del", mouseX, mouseY, "__send__:loot_remove:" + ld[0] + "|" + ld[5]);
            y += 16;
        }
        g.disableScissor();
    }

    private void renderAliases(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        int contentW = this.contentRight - this.contentLeft - 12;
        this.drawSectionHeader(g, "Command Aliases", x, y, contentW);
        this.drawActionButton(g, x + contentW - 60, y - 2, 60, 16, "Refresh", mouseX, mouseY, "__send__:alias_request:");
        y += 18;

        g.drawString(this.font, "Add: name:command (use {player} for your name)", x, y, 0xFF8B949E, false);
        y += 12;
        if (this.aliasInput != null) {
            this.aliasInput.setPosition(x, y);
            this.aliasInput.setWidth(contentW - 70);
            this.aliasInput.render(g, mouseX, mouseY, 0);
        }
        this.drawActionButton(g, x + contentW - 60, y, 56, 18, "Add", mouseX, mouseY, "__alias_add__");
        y += 24;
        darkDivider(g, x, y, contentW);
        y += 6;

        if (this.aliasData == null || this.aliasData.isEmpty()) {
            g.drawString(this.font, "No aliases. Click Refresh.", x, y, 0xFF8B949E, false);
            return;
        }
        for (int i = 0; i < this.aliasData.size() && y < this.contentBottom; i++) {
            String[] ad = this.aliasData.get(i);
            // ad = [name, command]
            darkRowBg(g, x, y, contentW, 18, i % 2 == 0);
            g.drawString(this.font, "/" + ad[0], x + 4, y + 4, 0xFF58A6FF, false);
            g.drawString(this.font, "-> " + ad[1], x + 70, y + 4, 0xFFC9D1D9, false);
            this.drawActionButton(g, x + contentW - 70, y + 1, 30, 14, "Run", mouseX, mouseY, "__send__:alias_execute:" + ad[0]);
            this.drawActionButton(g, x + contentW - 36, y + 1, 32, 14, "Del", mouseX, mouseY, "__send__:alias_remove:" + ad[0]);
            y += 18;
        }
    }

    private void renderUndo(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        int contentW = this.contentRight - this.contentLeft - 12;
        this.drawSectionHeader(g, "Undo / Rollback", x, y, contentW);
        this.drawActionButton(g, x + contentW - 130, y - 2, 60, 16, "Refresh", mouseX, mouseY, "__send__:undo_request:");
        this.drawActionButton(g, x + contentW - 65, y - 2, 65, 16, "Undo Last", mouseX, mouseY, "__send__:undo_last:");
        y += 18;

        if (this.undoData == null || this.undoData.isEmpty()) {
            g.drawString(this.font, "No undo history. Click Refresh.", x, y, 0xFF8B949E, false);
            return;
        }
        for (int i = 0; i < this.undoData.size() && y < this.contentBottom; i++) {
            String[] ud = this.undoData.get(i);
            // ud = [type, admin, target, desc, timeAgo]
            darkRowBg(g, x, y, contentW, 18, i % 2 == 0);
            g.drawString(this.font, "[" + ud[0] + "]", x + 4, y + 4, 0xFFD29922, false);
            g.drawString(this.font, ud[3], x + 80, y + 4, 0xFFC9D1D9, false);
            y += 18;
        }
    }

    // Data lists for new tabs
    private java.util.List<String[]> deathLogLines = null;
    private java.util.List<String[]> lootTableData = null;
    private java.util.List<String[]> aliasData = null;
    private java.util.List<String[]> undoData = null;
    private net.minecraft.client.gui.components.EditBox lootInput;
    private net.minecraft.client.gui.components.EditBox aliasInput;

    private record ActionRect(int x, int y, int w, int h, String command) {
    }

    private record MegamodEntry(String displayName, String registryId, Supplier<? extends Item> itemSupplier) {
    }

    private record GameruleRect(int x, int y, int w, int h, int index) {
    }

    private record EcoPlayerEntry(String uuid, String name, int wallet, int bank) {
    }

    private record SkillPlayerEntry(String uuid, String name, Map<String, int[]> treeData, int availablePoints, Map<String, Integer> treePoints) {
    }

    private record CosmeticPlayerEntry(String uuid, String name, String badgeTitle, int badgeTier, String badgeTree,
                                        boolean badgeEnabled, boolean particlesEnabled, int totalPrestige,
                                        Map<String, Integer> treePrestige, int respecCount,
                                        boolean hasCustomBadge, String customTitle, String customColor) {
    }

    private record PartyViewEntry(String leaderName, String leaderUuid, List<String[]> members, String comboName) {
    }

    private record BountyViewEntry(int id, String posterName, String itemName, int quantity, int priceOffered,
                                    boolean fulfilled, String fulfillerName, long hoursRemaining) {
    }

    private record ResearchBonus(String name, double value, boolean percent, String attr) {
    }

    private record ResearchAbility(String name, String description, int reqLevel, String castType, int points, int cooldownTicks, int defaultCooldown, List<ResearchAbilityStat> stats) {
    }

    private record ResearchAbilityStat(String name, double base, double computed, double min, double max) {
    }

    private record ResearchEnchant(String id, String name, int level) {
    }

    private record ResearchWeaponSkill(String name, String desc, int cooldown, float cooldownSec, int defaultCooldown) {
    }

    private record ResearchInvEntry(int slot, String itemId, String displayName,
            boolean hasWeaponStats, String rarityName, int rarityColor, float baseDamage, int bonusCount,
            boolean hasRelicData, int relicLevel, int relicQuality, int relicXp,
            List<ResearchBonus> bonuses, List<ResearchAbility> abilities, List<ResearchEnchant> enchantments,
            List<ResearchWeaponSkill> weaponSkills, String customName, List<String> loreLines,
            boolean isRelicItem) {
    }

    private record EntityEntry(int entityId, String type, String name, double x, double y, double z, float health, float maxHealth, float distance) {
    }

    // ---- Corruption Tab (24) ----

    private void renderCorruptionTab(GuiGraphics g, int mouseX, int mouseY) {
        int x = this.contentLeft + 6;
        int y = this.contentTop + 6 - this.corruptionScrollY;
        Objects.requireNonNull(this.font);
        int lh = 13;
        int contentW = this.contentRight - this.contentLeft - 12;

        // Title
        this.drawSectionHeader(g, "Corruption Management", x, y, contentW);
        y += 16;

        // Action buttons
        int btnW = 80;
        int btnH = 16;
        int gap = 4;
        this.drawActionButton(g, x, y, btnW, btnH, "Refresh", mouseX, mouseY, "__corruption_refresh__");
        this.drawActionButton(g, x + btnW + gap, y, btnW + 20, btnH, "Create Zone", mouseX, mouseY, "__corruption_create__");
        this.drawActionButton(g, x + btnW * 2 + gap * 2 + 20, y, btnW + 10, btnH, "Clear All", mouseX, mouseY, "__corruption_clear__");
        y += btnH + 8;

        if (this.corruptionJsonCache == null || this.corruptionJsonCache.isEmpty()) {
            g.drawString(this.font, "No data loaded. Click Refresh.", x, y, 0xFF8B949E, false);
            return;
        }

        try {
            JsonObject root = JsonParser.parseString(this.corruptionJsonCache).getAsJsonObject();

            // Stats overview
            this.drawSectionHeader(g, "Overview", x, y, contentW);
            y += 14;
            int totalZones = root.has("totalZones") ? root.get("totalZones").getAsInt() : 0;
            int totalChunks = root.has("totalCorruptedChunks") ? root.get("totalCorruptedChunks").getAsInt() : 0;
            g.drawString(this.font, "Active Zones: " + totalZones, x, y, 0xFFE6EDF3, false);
            g.drawString(this.font, "Total Corrupted Chunks: " + totalChunks, x + 160, y, 0xFFE6EDF3, false);
            y += lh;

            // Stats
            if (root.has("stats")) {
                JsonObject stats = root.getAsJsonObject("stats");
                int created = stats.has("zonesCreated") ? stats.get("zonesCreated").getAsInt() : 0;
                int destroyed = stats.has("zonesDestroyed") ? stats.get("zonesDestroyed").getAsInt() : 0;
                int purgesOk = stats.has("purgesCompleted") ? stats.get("purgesCompleted").getAsInt() : 0;
                int purgesFail = stats.has("purgesFailed") ? stats.get("purgesFailed").getAsInt() : 0;
                g.drawString(this.font, "Lifetime: " + created + " created, " + destroyed + " destroyed, " + purgesOk + " purges OK, " + purgesFail + " failed", x, y, 0xFF8B949E, false);
                y += lh;
            }

            // Active purge
            if (root.has("activePurge")) {
                y += 4;
                this.drawSectionHeader(g, "Active Purge", x, y, contentW);
                y += 14;
                JsonObject purge = root.getAsJsonObject("activePurge");
                String progress = purge.has("progress") ? purge.get("progress").getAsString() : "?";
                String timeLeft = purge.has("timeLeft") ? purge.get("timeLeft").getAsString() : "?";
                int participants = purge.has("participants") ? purge.get("participants").getAsInt() : 0;
                String initiator = purge.has("initiator") ? purge.get("initiator").getAsString() : "?";
                int purgeZoneId = purge.has("zoneId") ? purge.get("zoneId").getAsInt() : 0;

                g.drawString(this.font, "Zone #" + purgeZoneId + " | Progress: " + progress + " | Time Left: " + timeLeft, x, y, 0xFFFFD700, false);
                y += lh;
                g.drawString(this.font, "Participants: " + participants + " | Initiator: " + initiator, x, y, 0xFFE6EDF3, false);
                y += lh;

                this.drawActionButton(g, x, y, 90, btnH, "Stop Purge", mouseX, mouseY, "__corruption_purge_stop__");
                y += btnH + 4;
            }

            // Zone list
            y += 4;
            this.drawSectionHeader(g, "Corruption Zones", x, y, contentW);
            y += 14;

            if (root.has("zones")) {
                JsonArray zones = root.getAsJsonArray("zones");
                if (zones.size() == 0) {
                    g.drawString(this.font, "No corruption zones active.", x, y, 0xFF8B949E, false);
                    y += lh;
                } else {
                    // Header
                    g.drawString(this.font, "ID", x, y, 0xFF58A6FF, false);
                    g.drawString(this.font, "Center", x + 30, y, 0xFF58A6FF, false);
                    g.drawString(this.font, "Radius", x + 130, y, 0xFF58A6FF, false);
                    g.drawString(this.font, "Str", x + 180, y, 0xFF58A6FF, false);
                    g.drawString(this.font, "Source", x + 210, y, 0xFF58A6FF, false);
                    g.drawString(this.font, "Age", x + 280, y, 0xFF58A6FF, false);
                    g.drawString(this.font, "Actions", x + 330, y, 0xFF58A6FF, false);
                    y += lh;

                    for (int i = 0; i < zones.size(); i++) {
                        JsonObject zone = zones.get(i).getAsJsonObject();
                        int zoneId = zone.has("id") ? zone.get("id").getAsInt() : 0;
                        long cx = zone.has("centerX") ? zone.get("centerX").getAsLong() : 0;
                        long cz = zone.has("centerZ") ? zone.get("centerZ").getAsLong() : 0;
                        int radius = zone.has("radius") ? zone.get("radius").getAsInt() : 0;
                        int maxRad = zone.has("maxRadius") ? zone.get("maxRadius").getAsInt() : 0;
                        int str = zone.has("strength") ? zone.get("strength").getAsInt() : 0;
                        String source = zone.has("source") ? zone.get("source").getAsString() : "?";
                        String age = zone.has("age") ? zone.get("age").getAsString() : "?";

                        int rowColor = i % 2 == 0 ? 0xFF161B22 : 0xFF0D1117;
                        g.fill(x - 2, y - 1, x + contentW, y + lh - 1, rowColor);

                        g.drawString(this.font, "#" + zoneId, x, y, 0xFFE6EDF3, false);
                        g.drawString(this.font, cx + ", " + cz, x + 30, y, 0xFFC9D1D9, false);
                        g.drawString(this.font, radius + "/" + maxRad, x + 130, y, 0xFFC9D1D9, false);

                        // Color-code strength
                        int strColor = str >= 8 ? 0xFFFF4444 : str >= 5 ? 0xFFFFAA00 : str >= 3 ? 0xFFFFFF00 : 0xFF44FF44;
                        g.drawString(this.font, String.valueOf(str), x + 180, y, strColor, false);

                        g.drawString(this.font, source, x + 210, y, 0xFF8B949E, false);
                        g.drawString(this.font, age, x + 280, y, 0xFF8B949E, false);

                        // Action buttons: Purge | Remove
                        int actX = x + 330;
                        this.drawActionButton(g, actX, y - 1, 40, 12, "Purge", mouseX, mouseY, "__corruption_purge__:" + zoneId);
                        this.drawActionButton(g, actX + 44, y - 1, 30, 12, "Del", mouseX, mouseY, "__corruption_del__:" + zoneId);

                        y += lh + 2;
                    }
                }
            }
        } catch (Exception e) {
            g.drawString(this.font, "Error parsing corruption data.", x, y, 0xFFFF4444, false);
        }
    }

    private boolean handleCorruptionClick(double mx, double my) {
        // Check actionRects for corruption commands (handled in handleActionCommand)
        for (ActionRect rect : this.actionRects) {
            if (mx >= rect.x() && mx < rect.x() + rect.w() && my >= rect.y() && my < rect.y() + rect.h()) {
                String cmd = rect.command();
                if (cmd.startsWith("__corruption_")) {
                    this.processCorruptionCommand(cmd);
                    return true;
                }
            }
        }
        return false;
    }

    private void processCorruptionCommand(String command) {
        if ("__corruption_refresh__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("corruption_request", ""), (CustomPacketPayload[]) new CustomPacketPayload[0]);
        } else if ("__corruption_create__".equals(command)) {
            // Create zone at player's position with default settings
            ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("corruption_create", "{\"strength\":3,\"maxRadius\":8}"), (CustomPacketPayload[]) new CustomPacketPayload[0]);
        } else if ("__corruption_clear__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("corruption_clear_all", ""), (CustomPacketPayload[]) new CustomPacketPayload[0]);
        } else if ("__corruption_purge_stop__".equals(command)) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("corruption_purge_stop", ""), (CustomPacketPayload[]) new CustomPacketPayload[0]);
        } else if (command.startsWith("__corruption_purge__:")) {
            String zoneId = command.substring("__corruption_purge__:".length());
            ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("corruption_purge_start", zoneId), (CustomPacketPayload[]) new CustomPacketPayload[0]);
        } else if (command.startsWith("__corruption_del__:")) {
            String zoneId = command.substring("__corruption_del__:".length());
            ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("corruption_remove", zoneId), (CustomPacketPayload[]) new CustomPacketPayload[0]);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Combat Config Tab (BetterCombat settings)
    // ═══════════════════════════════════════════════════════════════

    private void renderCombatConfig(net.minecraft.client.gui.GuiGraphics g, int mouseX, int mouseY) {
        this.combatSliders.clear();
        this.combatToggles.clear();

        int x = this.contentLeft + 6;
        int y = this.contentTop + 6;
        int contentW = this.contentRight - this.contentLeft - 12;
        int col2X = x + contentW / 2 + 6;

        // Top banner
        this.drawSectionHeader(g, "Combat Configuration", x, y, contentW);
        y += 18;

        // ── LEFT COLUMN: sliders
        int ly = y;
        this.drawSectionHeader(g, "Passive Trigger Rate", x, ly, contentW / 2 - 6);
        ly += 14;
        ly = drawCombatSlider(g, x, ly, "Proc Multiplier",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.passive_proc_multiplier,
                0.0, 10.0, "passive_proc_multiplier", "%.2fx", 1.0);
        g.drawString(this.font, "0 = off  \u00B7  1 = normal  \u00B7  10 = always", x, ly, 0xFF8B949E, false);
        ly += 12;
        // Admin-only direct override (set to -1 to disable, 0-1 to force a fixed % chance)
        float procOverride = com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.admin_proc_override_chance;
        // Slider shows -1..1; at -1 we label it "Off". The percent readout treats < 0 as disabled.
        ly = drawCombatSlider(g, x, ly, "Admin Force Chance",
                procOverride,
                -1.0, 1.0, "admin_proc_override_chance",
                procOverride < 0 ? "OFF" : "%.0f%%", 100.0);
        g.drawString(this.font, "Drag fully left to disable; 100% = always procs", x, ly, 0xFF8B949E, false);
        ly += 12;
        ly = drawCombatSlider(g, x, ly, "Admin Attack Speed",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.admin_attack_speed_multiplier,
                0.1, 5.0, "admin_attack_speed_multiplier", "%.2fx", 1.0);

        this.drawSectionHeader(g, "Server Combat", x, ly + 4, contentW / 2 - 6);
        ly += 18;
        ly = drawCombatSlider(g, x, ly, "Upswing Multiplier",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.upswing_multiplier,
                0.2, 1.0, "upswing_multiplier", "%.2f", 1.0);
        ly = drawCombatSlider(g, x, ly, "Attack Interval Cap",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.attack_interval_cap,
                0, 20, "attack_interval_cap", "%.0f ticks", 1.0);
        ly = drawCombatSlider(g, x, ly, "Combo Reset Rate",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.combo_reset_rate,
                1.0, 10.0, "combo_reset_rate", "%.1fx", 1.0);
        ly = drawCombatSlider(g, x, ly, "Target Search Range",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.target_search_range_multiplier,
                1.0, 5.0, "target_search_range_multiplier", "%.1fx", 1.0);
        ly = drawCombatSlider(g, x, ly, "Move Speed Attacking",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.movement_speed_while_attacking,
                0.0, 1.0, "movement_speed_while_attacking", "%.0f%%", 100.0);

        this.drawSectionHeader(g, "Dual Wielding", x, ly + 4, contentW / 2 - 6);
        ly += 18;
        ly = drawCombatSlider(g, x, ly, "DW Attack Speed",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.dual_wielding_attack_speed_multiplier,
                0.5, 2.0, "dual_wielding_attack_speed_multiplier", "%.2fx", 1.0);
        ly = drawCombatSlider(g, x, ly, "DW Main Hand Damage",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.dual_wielding_main_hand_damage_multiplier,
                0.1, 2.0, "dual_wielding_main_hand_damage_multiplier", "%.0f%%", 100.0);
        ly = drawCombatSlider(g, x, ly, "DW Off Hand Damage",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.dual_wielding_off_hand_damage_multiplier,
                0.1, 2.0, "dual_wielding_off_hand_damage_multiplier", "%.0f%%", 100.0);

        // ── RIGHT COLUMN: toggles (rendered as button-style rows so text is identical to confirmed-working widgets)
        int ry = y;
        this.drawSectionHeader(g, "Server Toggles", col2X, ry, contentW / 2 - 6);
        ry += 18;
        ry = drawCombatToggleRow(g, col2X, ry, mouseX, mouseY, "Admin-Only Effects",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.admin_only_combat_effects,
                "admin_only_combat_effects");
        ry = drawCombatToggleRow(g, col2X, ry, mouseX, mouseY, "Allow Fast Attacks",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.allow_fast_attacks,
                "allow_fast_attacks");
        ry = drawCombatToggleRow(g, col2X, ry, mouseX, mouseY, "Reworked Sweeping",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.allow_reworked_sweeping,
                "allow_reworked_sweeping");
        ry = drawCombatToggleRow(g, col2X, ry, mouseX, mouseY, "Vanilla Sweeping",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.allow_vanilla_sweeping,
                "allow_vanilla_sweeping");
        ry = drawCombatToggleRow(g, col2X, ry, mouseX, mouseY, "Attack Through Walls",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.allow_attacking_thru_walls,
                "allow_attacking_thru_walls");

        this.drawSectionHeader(g, "Client Toggles", col2X, ry + 4, contentW / 2 - 6);
        ry += 18;
        ry = drawCombatToggleRow(g, col2X, ry, mouseX, mouseY, "Hold To Attack",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.isHoldToAttackEnabled,
                "hold_to_attack");
        ry = drawCombatToggleRow(g, col2X, ry, mouseX, mouseY, "Mining With Weapons",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.isMiningWithWeaponsEnabled,
                "mining_with_weapons");
        ry = drawCombatToggleRow(g, col2X, ry, mouseX, mouseY, "Swing Through Grass",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.isSwingThruGrassEnabled,
                "swing_thru_grass");
        ry = drawCombatToggleRow(g, col2X, ry, mouseX, mouseY, "Show Weapon Trails",
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.isShowingWeaponTrails,
                "show_weapon_trails");

        ry += 10;
        g.drawString(this.font, "Click a slider to set its value", col2X, ry,      0xFF8B949E, false);
        g.drawString(this.font, "Click a toggle to flip ON/OFF",   col2X, ry + 11, 0xFF8B949E, false);
    }

    /**
     * Slider row: label → bar → value. Uses the same drawString(this.font, s, x, y, color, false)
     * convention as {@code renderCleanup} so text is always visible.
     */
    private int drawCombatSlider(net.minecraft.client.gui.GuiGraphics g, int x, int y, String label,
                                  double current, double min, double max, String configKey,
                                  String valueFormat, double displayScale) {
        int rowH = 18;
        int labelW = 140;
        int valueW = 60;
        int barX = x + labelW;
        int barW = 120;
        int barY = y + 6;
        int barH = 6;

        // Row background stripe — helps the text pop off the panel
        g.fill(x, y, x + labelW + barW + valueW, y + rowH, 0xFF161B22);
        g.fill(x, y, x + labelW + barW + valueW, y + 1,    0xFF21262D);

        // Label
        g.drawString(this.font, label, x + 4, y + 5, 0xFFC9D1D9, false);

        // Track
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF30363D);
        // Fill
        double t = Math.max(0.0, Math.min(1.0, (current - min) / (max - min)));
        int fillW = (int) Math.round(t * barW);
        if (fillW > 0) g.fill(barX, barY, barX + fillW, barY + barH, 0xFF58A6FF);
        // Handle (bigger + taller so it's obvious where to click)
        int handleX = Math.min(barX + fillW - 1, barX + barW - 3);
        g.fill(handleX, barY - 2, handleX + 3, barY + barH + 2, 0xFFE6EDF3);

        // Value readout on right
        String valStr = String.format(valueFormat, current * displayScale);
        g.drawString(this.font, valStr, barX + barW + 6, y + 5, 0xFFD29922, false);

        this.combatSliders.add(new CombatSlider(configKey, barX, y, barW, rowH, min, max));
        return y + rowH + 2;
    }

    /**
     * Toggle row rendered like a button — identical drawString path as drawActionButton, which is
     * confirmed to render text correctly. The full row is clickable.
     */
    private int drawCombatToggleRow(net.minecraft.client.gui.GuiGraphics g, int x, int y, int mouseX, int mouseY,
                                     String label, boolean current, String configKey) {
        int w = 200;
        int h = 16;
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;

        int border = hovered ? 0xFF58A6FF : 0xFF30363D;
        int bg     = hovered ? 0xFF21262D : 0xFF161B22;
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);

        // On/off indicator box
        int dotColor = current ? 0xFF3FB950 : 0xFF484F58;
        g.fill(x + 3, y + 3, x + 13, y + 13, dotColor);
        if (current) {
            g.drawString(this.font, "\u2713", x + 4, y + 4, 0xFF000000, false);
        }
        // Label + status
        g.drawString(this.font, label, x + 18, y + 4, 0xFFC9D1D9, false);
        String status = current ? "ON" : "OFF";
        int statusColor = current ? 0xFF3FB950 : 0xFFF85149;
        int sw = this.font.width(status);
        g.drawString(this.font, status, x + w - sw - 6, y + 4, statusColor, false);

        this.combatToggles.add(new CombatToggle(configKey, x, y, w, h, current));
        return y + h + 2;
    }

    /** Handle a click in the combat tab. Returns true if the click hit a widget. */
    private boolean handleCombatTabClick(int mx, int my) {
        for (CombatToggle t : this.combatToggles) {
            if (mx >= t.x && mx < t.x + t.w && my >= t.y && my < t.y + t.h) {
                sendCombatConfig(t.key, String.valueOf(!t.current));
                return true;
            }
        }
        for (CombatSlider s : this.combatSliders) {
            if (mx >= s.x && mx < s.x + s.w && my >= s.y && my < s.y + s.h) {
                double t = (mx - s.x) / (double) s.w;
                t = Math.max(0.0, Math.min(1.0, t));
                double value = s.min + t * (s.max - s.min);
                sendCombatConfig(s.key, String.format("%.4f", value));
                return true;
            }
        }
        return false;
    }

    /** Send a set_combat_config action to the server. */
    private void sendCombatConfig(String key, String rawValue) {
        String json = "{\"key\":\"" + key + "\",\"value\":" +
                (rawValue.equalsIgnoreCase("true") || rawValue.equalsIgnoreCase("false")
                        ? rawValue
                        : rawValue) + "}";
        ClientPacketDistributor.sendToServer(
                new com.ultra.megamod.feature.computer.network.ComputerActionPayload(
                        "set_combat_config", json));
    }

    private int drawConfigLine(net.minecraft.client.gui.GuiGraphics g, int x, int y, String label, String value) {
        g.drawString(this.font, label + ":", x, y, 0xCCCCCC);
        g.drawString(this.font, value, x + 180, y, 0xFFFFFF);
        return y + 12;
    }

    private String bool(boolean v) { return v ? "\u00A7aON" : "\u00A7cOFF"; }
}

