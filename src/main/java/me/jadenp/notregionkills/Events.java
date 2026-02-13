package me.jadenp.notregionkills;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Events implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (killer == null) {
            return;
        }

        Location deathLocation = event.getEntity().getLocation();
        // get mob type
        String mythicMobType = NotRegionKills.isMythicMobsEnabled() ? MythicMobsClass.getMythicMobType(event.getEntity()) : null;
        String entityType = event.getEntity().getType().toString();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getEntity().isValid() && event.getEntity().getHealth() > 0) {
                    // entity is still alive
                    return;
                }

                // get region info for death location
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionQuery query = container.createQuery();
                ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(deathLocation));
                // iterate through all regions in location
                for (ProtectedRegion region : set.getRegions()) {
                    // check if custom flag is set to allow or null (no value set)
                    if (region.getFlag(NotRegionKills.trackKills) == null || region.getFlag(NotRegionKills.trackKills) == StateFlag.State.ALLOW) {
                        // check if it is a mythic mob
                        if (NotRegionKills.isMythicMobsEnabled() && mythicMobType != null) {
                            StatManager.killMob(killer, new RegionType(region.getId(), RegionType.Type.WORLDGUARD), mythicMobType);
                            continue;
                        }
                        // record statistic if it is a regular entity
                        StatManager.killMob(killer, new RegionType(region.getId(), RegionType.Type.WORLDGUARD), entityType);
                    }
                }
                // record statistic for world
                if (NotRegionKills.isMythicMobsEnabled() && mythicMobType != null) {
                    StatManager.killMob(killer, new RegionType(killer.getWorld().getName(), RegionType.Type.WORLD), mythicMobType);
                } else {
                    StatManager.killMob(killer, new RegionType(killer.getWorld().getName(), RegionType.Type.WORLD), entityType);
                }
            }
        }.runTaskLater(NotRegionKills.getInstance(), 5L);

    }
}
