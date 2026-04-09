package com.ultra.megamod.feature.economy;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server-side economy analytics engine. Tracks snapshots, daily/weekly aggregates,
 * and transaction metrics. Static singleton with NbtIo persistence.
 */
public class EconomyAnalytics {

    private static EconomyAnalytics INSTANCE;
    private static final String FILE_NAME = "megamod_economy_analytics.dat";
    private static final int MAX_SNAPSHOTS = 288; // 24 hours at 5-min intervals
    private static final int MAX_DAILY_AGGREGATES = 30;
    private static final int MAX_WEEKLY_AGGREGATES = 12;
    private static final int SNAPSHOT_INTERVAL_TICKS = 6000; // 5 minutes

    // Snapshots
    private final List<EconomySnapshot> snapshots = new ArrayList<>();
    private final List<DailyAggregate> dailyAggregates = new ArrayList<>();
    private final List<WeeklyAggregate> weeklyAggregates = new ArrayList<>();

    // Transaction tracking between snapshots
    private final AtomicInteger transactionCount = new AtomicInteger(0);
    private final AtomicInteger transactionVolume = new AtomicInteger(0);
    private final AtomicInteger shopPurchaseCount = new AtomicInteger(0);
    private final AtomicInteger shopRevenueTotal = new AtomicInteger(0);

    // Per-type transaction tracking (capped to prevent unbounded memory growth)
    private static final int MAX_TRANSACTION_TYPES = 50;
    private final ConcurrentHashMap<String, AtomicInteger> transactionsByType = new ConcurrentHashMap<>();

    // Tick counter
    private int tickCounter = 0;
    private boolean dirty = false;
    private String lastDailyAggregateDate = "";
    private String lastWeeklyAggregateWeek = "";

    // Transaction type constants
    public static final String SHOP_BUY = "SHOP_BUY";
    public static final String SHOP_SELL = "SHOP_SELL";
    public static final String TRADE = "TRADE";
    public static final String BOUNTY = "BOUNTY";
    public static final String HIRE_CITIZEN = "HIRE_CITIZEN";
    public static final String UPKEEP = "UPKEEP";
    public static final String ADMIN = "ADMIN";
    public static final String PURGE_REWARD = "PURGE_REWARD";
    public static final String DUNGEON_REWARD = "DUNGEON_REWARD";
    public static final String CASINO = "CASINO";
    public static final String TRANSFER = "TRANSFER";
    public static final String EARN = "EARN";
    public static final String SPEND = "SPEND";
    public static final String MARKET = "MARKET";

    public static class EconomySnapshot {
        public long timestamp;
        public int totalCoinsInCirculation;
        public int totalWalletCoins;
        public int totalBankCoins;
        public int playerCount;
        public int activePlayerCount;
        public int transactionCount;
        public int totalTransactionVolume;
        public int shopPurchases;
        public int shopRevenue;
        public double giniCoefficient;
        public int medianWealth;
        public int averageWealth;
        public int topTenPercentWealth;
    }

    public static class DailyAggregate {
        public String date;
        public int peakCirculation;
        public int minCirculation;
        public int avgCirculation;
        public int totalTransactions;
        public int totalVolume;
        public int newPlayers;
        public int activeCount;
        public int shopRevenue;
        public double avgGini;
        public int snapshotCount;
        // Running accumulators for computing averages
        public long circulationSum;
        public double giniSum;
    }

    public static class WeeklyAggregate {
        public String weekKey; // e.g., "2026-W12"
        public int peakCirculation;
        public int minCirculation;
        public int avgCirculation;
        public int totalTransactions;
        public int totalVolume;
        public int activeCount;
        public int shopRevenue;
        public double avgGini;
        public int dayCount;
        public long circulationSum;
        public double giniSum;
    }

    public static EconomyAnalytics get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new EconomyAnalytics();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static EconomyAnalytics getIfLoaded() {
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    // ---- Transaction recording (called by other systems) ----

    public static void recordTransaction(String type, int amount) {
        EconomyAnalytics inst = INSTANCE;
        if (inst == null) return;
        inst.transactionCount.incrementAndGet();
        inst.transactionVolume.addAndGet(Math.abs(amount));
        // Only track the type if it already exists or we haven't hit the cap
        AtomicInteger existing = inst.transactionsByType.get(type);
        if (existing != null) {
            existing.addAndGet(Math.abs(amount));
        } else if (inst.transactionsByType.size() < MAX_TRANSACTION_TYPES) {
            inst.transactionsByType.computeIfAbsent(type, k -> new AtomicInteger(0)).addAndGet(Math.abs(amount));
        }
        if (SHOP_BUY.equals(type)) {
            inst.shopPurchaseCount.incrementAndGet();
            inst.shopRevenueTotal.addAndGet(Math.abs(amount));
        }
        inst.dirty = true;
    }

    public static void recordShopPurchase(int amount) {
        recordTransaction(SHOP_BUY, amount);
    }

    public static void recordShopSale(int amount) {
        recordTransaction(SHOP_SELL, amount);
    }

    // ---- Tick and snapshot ----

    public void onServerTick(ServerLevel level, EconomyManager eco) {
        tickCounter++;
        if (tickCounter >= SNAPSHOT_INTERVAL_TICKS) {
            tickCounter = 0;
            takeSnapshot(eco, level);
        }
    }

    public void takeSnapshot(EconomyManager eco, ServerLevel level) {
        EconomySnapshot snapshot = new EconomySnapshot();
        snapshot.timestamp = System.currentTimeMillis();

        Map<UUID, int[]> allData = eco.getAllPlayerData();
        snapshot.playerCount = allData.size();

        int totalWallets = 0;
        int totalBanks = 0;
        List<Integer> wealthList = new ArrayList<>();

        for (int[] data : allData.values()) {
            totalWallets += data[0];
            totalBanks += data[1];
            wealthList.add(data[0] + data[1]);
        }

        snapshot.totalWalletCoins = totalWallets;
        snapshot.totalBankCoins = totalBanks;
        snapshot.totalCoinsInCirculation = totalWallets + totalBanks;

        // Active players (online)
        int activeCount = 0;
        if (level.getServer() != null) {
            activeCount = level.getServer().getPlayerList().getPlayerCount();
        }
        snapshot.activePlayerCount = activeCount;

        // Transaction data since last snapshot
        snapshot.transactionCount = transactionCount.getAndSet(0);
        snapshot.totalTransactionVolume = transactionVolume.getAndSet(0);
        snapshot.shopPurchases = shopPurchaseCount.getAndSet(0);
        snapshot.shopRevenue = shopRevenueTotal.getAndSet(0);

        // Wealth distribution
        Collections.sort(wealthList);
        snapshot.giniCoefficient = calculateGini(wealthList);
        snapshot.medianWealth = wealthList.isEmpty() ? 0 : wealthList.get(wealthList.size() / 2);
        snapshot.averageWealth = wealthList.isEmpty() ? 0 : snapshot.totalCoinsInCirculation / wealthList.size();

        // Top 10% wealth
        if (!wealthList.isEmpty()) {
            int topTenIdx = (int) (wealthList.size() * 0.9);
            int topTenWealth = 0;
            for (int i = topTenIdx; i < wealthList.size(); i++) {
                topTenWealth += wealthList.get(i);
            }
            snapshot.topTenPercentWealth = topTenWealth;
        }

        // Add snapshot
        snapshots.add(0, snapshot); // newest first
        while (snapshots.size() > MAX_SNAPSHOTS) {
            snapshots.remove(snapshots.size() - 1);
        }

        // Update daily aggregate
        updateDailyAggregate(snapshot);
        updateWeeklyAggregate();

        dirty = true;
        saveToDisk(level);
    }

    private void updateDailyAggregate(EconomySnapshot snapshot) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        DailyAggregate agg = null;
        if (!dailyAggregates.isEmpty() && dailyAggregates.get(0).date.equals(today)) {
            agg = dailyAggregates.get(0);
        } else {
            agg = new DailyAggregate();
            agg.date = today;
            agg.peakCirculation = 0;
            agg.minCirculation = Integer.MAX_VALUE;
            agg.circulationSum = 0;
            agg.giniSum = 0;
            agg.snapshotCount = 0;
            dailyAggregates.add(0, agg);
            while (dailyAggregates.size() > MAX_DAILY_AGGREGATES) {
                dailyAggregates.remove(dailyAggregates.size() - 1);
            }
        }

        agg.peakCirculation = Math.max(agg.peakCirculation, snapshot.totalCoinsInCirculation);
        agg.minCirculation = Math.min(agg.minCirculation, snapshot.totalCoinsInCirculation);
        agg.circulationSum += snapshot.totalCoinsInCirculation;
        agg.giniSum += snapshot.giniCoefficient;
        agg.snapshotCount++;
        agg.avgCirculation = (int) (agg.circulationSum / agg.snapshotCount);
        agg.avgGini = agg.giniSum / agg.snapshotCount;
        agg.totalTransactions += snapshot.transactionCount;
        agg.totalVolume += snapshot.totalTransactionVolume;
        agg.activeCount = Math.max(agg.activeCount, snapshot.activePlayerCount);
        agg.shopRevenue += snapshot.shopRevenue;
    }

    private void updateWeeklyAggregate() {
        if (dailyAggregates.isEmpty()) return;

        // Determine current week key
        LocalDate now = LocalDate.now();
        int weekNum = now.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
        String weekKey = now.getYear() + "-W" + String.format("%02d", weekNum);

        WeeklyAggregate wAgg = null;
        if (!weeklyAggregates.isEmpty() && weeklyAggregates.get(0).weekKey.equals(weekKey)) {
            wAgg = weeklyAggregates.get(0);
        } else {
            wAgg = new WeeklyAggregate();
            wAgg.weekKey = weekKey;
            wAgg.peakCirculation = 0;
            wAgg.minCirculation = Integer.MAX_VALUE;
            wAgg.circulationSum = 0;
            wAgg.giniSum = 0;
            wAgg.dayCount = 0;
            weeklyAggregates.add(0, wAgg);
            while (weeklyAggregates.size() > MAX_WEEKLY_AGGREGATES) {
                weeklyAggregates.remove(weeklyAggregates.size() - 1);
            }
        }

        // Rebuild from daily aggregates for this week
        wAgg.peakCirculation = 0;
        wAgg.minCirculation = Integer.MAX_VALUE;
        wAgg.circulationSum = 0;
        wAgg.giniSum = 0;
        wAgg.dayCount = 0;
        wAgg.totalTransactions = 0;
        wAgg.totalVolume = 0;
        wAgg.activeCount = 0;
        wAgg.shopRevenue = 0;

        for (DailyAggregate da : dailyAggregates) {
            try {
                LocalDate date = LocalDate.parse(da.date);
                int dWeekNum = date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
                String dWeekKey = date.getYear() + "-W" + String.format("%02d", dWeekNum);
                if (dWeekKey.equals(weekKey)) {
                    wAgg.peakCirculation = Math.max(wAgg.peakCirculation, da.peakCirculation);
                    if (da.minCirculation != Integer.MAX_VALUE) {
                        wAgg.minCirculation = Math.min(wAgg.minCirculation, da.minCirculation);
                    }
                    wAgg.circulationSum += da.avgCirculation;
                    wAgg.giniSum += da.avgGini;
                    wAgg.dayCount++;
                    wAgg.totalTransactions += da.totalTransactions;
                    wAgg.totalVolume += da.totalVolume;
                    wAgg.activeCount = Math.max(wAgg.activeCount, da.activeCount);
                    wAgg.shopRevenue += da.shopRevenue;
                }
            } catch (Exception ignored) {}
        }

        if (wAgg.dayCount > 0) {
            wAgg.avgCirculation = (int) (wAgg.circulationSum / wAgg.dayCount);
            wAgg.avgGini = wAgg.giniSum / wAgg.dayCount;
        }
        if (wAgg.minCirculation == Integer.MAX_VALUE) wAgg.minCirculation = 0;
    }

    // ---- Query Methods ----

    public List<EconomySnapshot> getRecentSnapshots(int count) {
        int n = Math.min(count, snapshots.size());
        return new ArrayList<>(snapshots.subList(0, n));
    }

    public List<DailyAggregate> getDailyAggregates(int days) {
        int n = Math.min(days, dailyAggregates.size());
        return new ArrayList<>(dailyAggregates.subList(0, n));
    }

    public List<WeeklyAggregate> getWeeklyAggregates(int weeks) {
        int n = Math.min(weeks, weeklyAggregates.size());
        return new ArrayList<>(weeklyAggregates.subList(0, n));
    }

    /** % change in total circulation over last 24 hours */
    public double getInflationRate() {
        if (snapshots.size() < 2) return 0.0;
        EconomySnapshot latest = snapshots.get(0);
        // Find snapshot closest to 24h ago
        long target = latest.timestamp - 86400000L;
        EconomySnapshot oldest = snapshots.get(snapshots.size() - 1);
        for (EconomySnapshot s : snapshots) {
            if (s.timestamp <= target) {
                oldest = s;
                break;
            }
        }
        if (oldest.totalCoinsInCirculation == 0) return 0.0;
        return ((double)(latest.totalCoinsInCirculation - oldest.totalCoinsInCirculation) / oldest.totalCoinsInCirculation) * 100.0;
    }

    /** Transaction volume / total circulation ratio (velocity of money) */
    public double getCoinVelocity() {
        if (snapshots.isEmpty()) return 0.0;
        EconomySnapshot latest = snapshots.get(0);
        if (latest.totalCoinsInCirculation == 0) return 0.0;

        // Sum transaction volumes from recent snapshots (last 24h)
        long cutoff = System.currentTimeMillis() - 86400000L;
        int totalVolume = 0;
        for (EconomySnapshot s : snapshots) {
            if (s.timestamp < cutoff) break;
            totalVolume += s.totalTransactionVolume;
        }
        return (double) totalVolume / latest.totalCoinsInCirculation;
    }

    /** Breakdown of wealth by percentile: returns 10 buckets (0-10%, 10-20%, etc.) */
    public int[] getWealthDistribution(EconomyManager eco) {
        int[] buckets = new int[10];
        Map<UUID, int[]> allData = eco.getAllPlayerData();
        if (allData.isEmpty()) return buckets;

        List<Integer> wealthList = new ArrayList<>();
        for (int[] data : allData.values()) {
            wealthList.add(data[0] + data[1]);
        }
        Collections.sort(wealthList);

        int n = wealthList.size();
        for (int i = 0; i < n; i++) {
            int bucket = Math.min(9, (int)((double) i / n * 10));
            buckets[bucket] += wealthList.get(i);
        }
        return buckets;
    }

    /** Top N players by total wealth, returns list of [name, uuid, wallet, bank, total] */
    public List<String[]> getTopEarners(int count, EconomyManager eco, ServerLevel level) {
        Map<UUID, int[]> allData = eco.getAllPlayerData();
        List<Map.Entry<UUID, int[]>> sorted = new ArrayList<>(allData.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue()[0] + b.getValue()[1], a.getValue()[0] + a.getValue()[1]));

        List<String[]> result = new ArrayList<>();
        int n = Math.min(count, sorted.size());
        for (int i = 0; i < n; i++) {
            Map.Entry<UUID, int[]> entry = sorted.get(i);
            String name = resolveName(entry.getKey(), level);
            int wallet = entry.getValue()[0];
            int bank = entry.getValue()[1];
            int total = wallet + bank;
            result.add(new String[]{name, entry.getKey().toString(), String.valueOf(wallet), String.valueOf(bank), String.valueOf(total)});
        }
        return result;
    }

    /** Get transaction volume by type for the last 24h */
    public Map<String, Integer> getTransactionBreakdown() {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, AtomicInteger> entry : transactionsByType.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get());
        }
        return result;
    }

    // ---- Gini Coefficient Calculation ----

    private double calculateGini(List<Integer> sortedWealth) {
        if (sortedWealth.isEmpty()) return 0.0;
        int n = sortedWealth.size();
        if (n == 1) return 0.0;

        long totalWealth = 0;
        for (int w : sortedWealth) totalWealth += w;
        if (totalWealth == 0) return 0.0;

        long numerator = 0;
        for (int i = 0; i < n; i++) {
            numerator += (2L * (i + 1) - n - 1) * sortedWealth.get(i);
        }
        return (double) numerator / (n * totalWealth);
    }

    private String resolveName(UUID uuid, ServerLevel level) {
        if (level.getServer() != null) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) return player.getGameProfile().name();
        }
        // Fallback: shortened UUID
        return uuid.toString().substring(0, 8);
    }

    // ---- Persistence ----

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (!dataFile.exists()) return;

            CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());

            // Load snapshots
            CompoundTag snapshotsTag = root.getCompoundOrEmpty("snapshots");
            int snapCount = snapshotsTag.getIntOr("count", 0);
            for (int i = 0; i < snapCount; i++) {
                CompoundTag sTag = snapshotsTag.getCompoundOrEmpty("s" + i);
                EconomySnapshot s = new EconomySnapshot();
                s.timestamp = sTag.getLongOr("ts", 0L);
                s.totalCoinsInCirculation = sTag.getIntOr("circ", 0);
                s.totalWalletCoins = sTag.getIntOr("wallet", 0);
                s.totalBankCoins = sTag.getIntOr("bank", 0);
                s.playerCount = sTag.getIntOr("players", 0);
                s.activePlayerCount = sTag.getIntOr("active", 0);
                s.transactionCount = sTag.getIntOr("txCount", 0);
                s.totalTransactionVolume = sTag.getIntOr("txVol", 0);
                s.shopPurchases = sTag.getIntOr("shopBuys", 0);
                s.shopRevenue = sTag.getIntOr("shopRev", 0);
                s.giniCoefficient = sTag.getDoubleOr("gini", 0.0);
                s.medianWealth = sTag.getIntOr("median", 0);
                s.averageWealth = sTag.getIntOr("avg", 0);
                s.topTenPercentWealth = sTag.getIntOr("top10", 0);
                snapshots.add(s);
            }

            // Load daily aggregates
            CompoundTag dailyTag = root.getCompoundOrEmpty("daily");
            int dailyCount = dailyTag.getIntOr("count", 0);
            for (int i = 0; i < dailyCount; i++) {
                CompoundTag dTag = dailyTag.getCompoundOrEmpty("d" + i);
                DailyAggregate d = new DailyAggregate();
                d.date = dTag.getStringOr("date", "");
                d.peakCirculation = dTag.getIntOr("peak", 0);
                d.minCirculation = dTag.getIntOr("min", 0);
                d.avgCirculation = dTag.getIntOr("avg", 0);
                d.totalTransactions = dTag.getIntOr("txCount", 0);
                d.totalVolume = dTag.getIntOr("txVol", 0);
                d.newPlayers = dTag.getIntOr("newPlayers", 0);
                d.activeCount = dTag.getIntOr("active", 0);
                d.shopRevenue = dTag.getIntOr("shopRev", 0);
                d.avgGini = dTag.getDoubleOr("gini", 0.0);
                d.snapshotCount = dTag.getIntOr("snapCount", 1);
                d.circulationSum = dTag.getLongOr("circSum", 0L);
                d.giniSum = dTag.getDoubleOr("giniSum", 0.0);
                dailyAggregates.add(d);
            }

            // Load weekly aggregates
            CompoundTag weeklyTag = root.getCompoundOrEmpty("weekly");
            int weeklyCount = weeklyTag.getIntOr("count", 0);
            for (int i = 0; i < weeklyCount; i++) {
                CompoundTag wTag = weeklyTag.getCompoundOrEmpty("w" + i);
                WeeklyAggregate w = new WeeklyAggregate();
                w.weekKey = wTag.getStringOr("weekKey", "");
                w.peakCirculation = wTag.getIntOr("peak", 0);
                w.minCirculation = wTag.getIntOr("min", 0);
                w.avgCirculation = wTag.getIntOr("avg", 0);
                w.totalTransactions = wTag.getIntOr("txCount", 0);
                w.totalVolume = wTag.getIntOr("txVol", 0);
                w.activeCount = wTag.getIntOr("active", 0);
                w.shopRevenue = wTag.getIntOr("shopRev", 0);
                w.avgGini = wTag.getDoubleOr("gini", 0.0);
                w.dayCount = wTag.getIntOr("dayCount", 1);
                w.circulationSum = wTag.getLongOr("circSum", 0L);
                w.giniSum = wTag.getDoubleOr("giniSum", 0.0);
                weeklyAggregates.add(w);
            }

            // Load transaction type tracking (capped to MAX_TRANSACTION_TYPES)
            CompoundTag txTypes = root.getCompoundOrEmpty("txTypes");
            int loadedTypes = 0;
            for (String key : txTypes.keySet()) {
                if (loadedTypes >= MAX_TRANSACTION_TYPES) break;
                transactionsByType.put(key, new AtomicInteger(txTypes.getIntOr(key, 0)));
                loadedTypes++;
            }

        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load economy analytics data", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();

            // Save snapshots
            CompoundTag snapshotsTag = new CompoundTag();
            snapshotsTag.putInt("count", snapshots.size());
            for (int i = 0; i < snapshots.size(); i++) {
                EconomySnapshot s = snapshots.get(i);
                CompoundTag sTag = new CompoundTag();
                sTag.putLong("ts", s.timestamp);
                sTag.putInt("circ", s.totalCoinsInCirculation);
                sTag.putInt("wallet", s.totalWalletCoins);
                sTag.putInt("bank", s.totalBankCoins);
                sTag.putInt("players", s.playerCount);
                sTag.putInt("active", s.activePlayerCount);
                sTag.putInt("txCount", s.transactionCount);
                sTag.putInt("txVol", s.totalTransactionVolume);
                sTag.putInt("shopBuys", s.shopPurchases);
                sTag.putInt("shopRev", s.shopRevenue);
                sTag.putDouble("gini", s.giniCoefficient);
                sTag.putInt("median", s.medianWealth);
                sTag.putInt("avg", s.averageWealth);
                sTag.putInt("top10", s.topTenPercentWealth);
                snapshotsTag.put("s" + i, (Tag) sTag);
            }
            root.put("snapshots", (Tag) snapshotsTag);

            // Save daily aggregates
            CompoundTag dailyTag = new CompoundTag();
            dailyTag.putInt("count", dailyAggregates.size());
            for (int i = 0; i < dailyAggregates.size(); i++) {
                DailyAggregate d = dailyAggregates.get(i);
                CompoundTag dTag = new CompoundTag();
                dTag.putString("date", d.date);
                dTag.putInt("peak", d.peakCirculation);
                dTag.putInt("min", d.minCirculation == Integer.MAX_VALUE ? 0 : d.minCirculation);
                dTag.putInt("avg", d.avgCirculation);
                dTag.putInt("txCount", d.totalTransactions);
                dTag.putInt("txVol", d.totalVolume);
                dTag.putInt("newPlayers", d.newPlayers);
                dTag.putInt("active", d.activeCount);
                dTag.putInt("shopRev", d.shopRevenue);
                dTag.putDouble("gini", d.avgGini);
                dTag.putInt("snapCount", d.snapshotCount);
                dTag.putLong("circSum", d.circulationSum);
                dTag.putDouble("giniSum", d.giniSum);
                dailyTag.put("d" + i, (Tag) dTag);
            }
            root.put("daily", (Tag) dailyTag);

            // Save weekly aggregates
            CompoundTag weeklyTag = new CompoundTag();
            weeklyTag.putInt("count", weeklyAggregates.size());
            for (int i = 0; i < weeklyAggregates.size(); i++) {
                WeeklyAggregate w = weeklyAggregates.get(i);
                CompoundTag wTag = new CompoundTag();
                wTag.putString("weekKey", w.weekKey);
                wTag.putInt("peak", w.peakCirculation);
                wTag.putInt("min", w.minCirculation == Integer.MAX_VALUE ? 0 : w.minCirculation);
                wTag.putInt("avg", w.avgCirculation);
                wTag.putInt("txCount", w.totalTransactions);
                wTag.putInt("txVol", w.totalVolume);
                wTag.putInt("active", w.activeCount);
                wTag.putInt("shopRev", w.shopRevenue);
                wTag.putDouble("gini", w.avgGini);
                wTag.putInt("dayCount", w.dayCount);
                wTag.putLong("circSum", w.circulationSum);
                wTag.putDouble("giniSum", w.giniSum);
                weeklyTag.put("w" + i, (Tag) wTag);
            }
            root.put("weekly", (Tag) weeklyTag);

            // Save transaction type tracking
            CompoundTag txTypes = new CompoundTag();
            for (Map.Entry<String, AtomicInteger> entry : transactionsByType.entrySet()) {
                txTypes.putInt(entry.getKey(), entry.getValue().get());
            }
            root.put("txTypes", (Tag) txTypes);

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save economy analytics data", e);
        }
    }
}
