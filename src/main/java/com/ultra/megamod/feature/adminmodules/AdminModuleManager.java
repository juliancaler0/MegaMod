package com.ultra.megamod.feature.adminmodules;

import com.ultra.megamod.feature.adminmodules.modules.combat.*;
import com.ultra.megamod.feature.adminmodules.modules.movement.*;
import com.ultra.megamod.feature.adminmodules.modules.render.*;
import com.ultra.megamod.feature.adminmodules.modules.player.*;
import com.ultra.megamod.feature.adminmodules.modules.world.*;
import com.ultra.megamod.feature.adminmodules.modules.misc.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.io.File;
import java.util.*;

public class AdminModuleManager {
    private static AdminModuleManager INSTANCE;
    private final Map<String, AdminModule> modules = new LinkedHashMap<>();
    private boolean loaded = false;

    private AdminModuleManager() {
        registerAll();
    }

    public static AdminModuleManager get() {
        if (INSTANCE == null) {
            INSTANCE = new AdminModuleManager();
        }
        return INSTANCE;
    }

    private void register(AdminModule module) {
        modules.put(module.getId(), module);
    }

    private void registerAll() {
        boolean isClient;
        try {
            Class.forName("net.minecraft.client.Minecraft");
            isClient = true;
        } catch (ClassNotFoundException e) {
            isClient = false;
        }

        // Combat (30)
        register(new KillAuraModule());
        register(new CrystalAuraModule());
        register(new BowAimbotModule());
        register(new SurroundModule());
        CombatModules.register(this::register);

        // Movement (29)
        register(new FlightModule());
        register(new SpeedModule());
        register(new ScaffoldModule());
        register(new FreecamModule());
        MovementModules.register(this::register);

        // Render (35) — client only, these classes import client rendering APIs
        if (isClient) {
            register(new ESPModule());
            register(new OreESPModule());
            register(new TracersModule());
            register(new HoleESPModule());
            register(new TrajectoriesModule());
            RenderModules.register(this::register);
        }

        // Player (32)
        register(new AutoEatModule());
        register(new AutoFishModule());
        register(new ChestStealerModule());
        PlayerModules.register(this::register);

        // World (25)
        register(new NukerModule());
        register(new VeinMinerModule());
        WorldModules.register(this::register);

        // Misc (15)
        MiscModules.register(this::register);
    }

    public AdminModule getModule(String id) {
        return modules.get(id);
    }

    public Collection<AdminModule> getAllModules() {
        return modules.values();
    }

    public List<AdminModule> getModulesByCategory(ModuleCategory category) {
        List<AdminModule> result = new ArrayList<>();
        for (AdminModule m : modules.values()) {
            if (m.getCategory() == category) result.add(m);
        }
        return result;
    }

    public int getEnabledCount() {
        int c = 0;
        for (AdminModule m : modules.values()) {
            if (m.isEnabled()) c++;
        }
        return c;
    }

    public int getTotalCount() {
        return modules.size();
    }

    public boolean isModuleEnabled(String moduleId) {
        AdminModule m = modules.get(moduleId);
        return m != null && m.isEnabled();
    }

    // Tick dispatch - called from event handler
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        for (AdminModule m : modules.values()) {
            if (m.isEnabled() && m.isServerSide()) {
                try {
                    m.onServerTick(player, level);
                } catch (Exception e) {
                    com.ultra.megamod.MegaMod.LOGGER.warn("Module tick error in {}", m.getId(), e);
                }
            }
        }
    }

    // Damage dispatch
    public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
        for (AdminModule m : modules.values()) {
            if (m.isEnabled() && m.isServerSide()) {
                try {
                    m.onDamage(player, event);
                } catch (Exception e) {
                    com.ultra.megamod.MegaMod.LOGGER.warn("Module damage error in {}", m.getId(), e);
                }
            }
        }
    }

    // Break speed dispatch
    public void onBreakSpeed(ServerPlayer player, net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event) {
        for (AdminModule m : modules.values()) {
            if (m.isEnabled() && m.isServerSide()) {
                try {
                    m.onBreakSpeed(player, event);
                } catch (Exception e) {
                    com.ultra.megamod.MegaMod.LOGGER.warn("Module break speed error in {}", m.getId(), e);
                }
            }
        }
    }

    // Build full JSON for client
    public String toJson() {
        StringBuilder sb = new StringBuilder("{\"modules\":[");
        boolean first = true;
        for (AdminModule m : modules.values()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(m.toJson());
        }
        sb.append("],\"enabled\":").append(getEnabledCount());
        sb.append(",\"total\":").append(getTotalCount()).append("}");
        return sb.toString();
    }

    // Persistence
    public void saveToDisk(ServerLevel level) {
        try {
            File dir = new File(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "megamod");
            dir.mkdirs();
            File file = new File(dir, "admin_modules.dat");
            CompoundTag tag = new CompoundTag();
            for (AdminModule m : modules.values()) {
                CompoundTag mTag = new CompoundTag();
                mTag.putBoolean("enabled", m.isEnabled());
                mTag.putString("toggleKey", m.getToggleKey());
                for (ModuleSetting<?> s : m.getSettings()) {
                    mTag.putString("s_" + s.getName(), s.serializeValue());
                }
                tag.put(m.getId(), mTag);
            }
            NbtIo.writeCompressed(tag, file.toPath());
        } catch (Exception e) {
            com.ultra.megamod.MegaMod.LOGGER.warn("Failed to save admin modules: {}", e.getMessage());
        }
    }

    public static void reset() {
        if (INSTANCE != null) {
            INSTANCE.loaded = false;
        }
        INSTANCE = null;
    }

    public void loadFromDisk(ServerLevel level) {
        if (loaded) return;
        loaded = true;
        try {
            File dir = new File(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "megamod");
            File file = new File(dir, "admin_modules.dat");
            if (!file.exists()) return;
            CompoundTag tag = NbtIo.readCompressed(file.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            for (String key : tag.keySet()) {
                AdminModule m = modules.get(key);
                if (m == null) continue;
                CompoundTag mTag = tag.getCompoundOrEmpty(key);
                m.setEnabled(mTag.getBooleanOr("enabled", false));
                String savedKey = mTag.getStringOr("toggleKey", "NONE");
                m.setToggleKey(savedKey);
                for (ModuleSetting<?> s : m.getSettings()) {
                    String val = mTag.getStringOr("s_" + s.getName(), "");
                    if (!val.isEmpty()) {
                        try { s.deserializeValue(val); } catch (Exception e) {
                            com.ultra.megamod.MegaMod.LOGGER.warn("Module setting deserialize error for {}.{}", key, s.getName(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            com.ultra.megamod.MegaMod.LOGGER.warn("Failed to load admin modules: {}", e.getMessage());
        }
    }
}
