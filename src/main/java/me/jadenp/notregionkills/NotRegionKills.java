package me.jadenp.notregionkills;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class NotRegionKills extends JavaPlugin {
    private static NotRegionKills instance;
    private static boolean mythicMobsEnabled;
    private boolean statsLoaded;
    private boolean failedStartup;
    private RegionKillsExpansion expansion;
    public static NotRegionKills getInstance() { return instance; }
    public static boolean isMythicMobsEnabled() { return mythicMobsEnabled; }

    @Override
    public void onLoad() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            try { WorldGuardProvider.registerFlag(); }
            catch (RuntimeException e) {
                getLogger().log(Level.SEVERE, "Cannot register WorldGuard track-kills flag", e);
                failedStartup = true;
            }
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        mythicMobsEnabled = false;
        if (failedStartup) { Bukkit.getPluginManager().disablePlugin(this); return; }
        try {
            StatManager.loadSavedKills();
            statsLoaded = true;
            List<RegionProvider> providers = new ArrayList<>();
            if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) providers.add(new WorldGuardProvider());
            if (Bukkit.getPluginManager().isPluginEnabled("CelestaraZones")) providers.add(new CelestaraZonesProvider());
            if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
                MythicMobsClass.loadMobTypes();
                mythicMobsEnabled = true;
            }
            expansion = new RegionKillsExpansion();
            expansion.register();
            Bukkit.getPluginManager().registerEvents(new Events(providers), this);
            Bukkit.getScheduler().runTaskTimer(this, this::save, 18000L, 18000L);
            getLogger().info("Tracking kills with " + providers.size() + " region provider(s).");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Startup failed; disabling NotRegionKills", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (expansion != null) expansion.unregister();
        if (statsLoaded) save();
        mythicMobsEnabled = false;
    }
    private void save() {
        try { StatManager.save(); }
        catch (Exception e) { getLogger().log(Level.SEVERE, "Failed to save stats", e); }
    }
}
