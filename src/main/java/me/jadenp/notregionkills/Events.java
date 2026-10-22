package me.jadenp.notregionkills;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.Bukkit;

public class Events implements Listener {
    private final List<RegionProvider> providers;
    Events(List<RegionProvider> providers) { this.providers = providers; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        Location location = event.getEntity().getLocation();
        String mythicType = NotRegionKills.isMythicMobsEnabled()
                ? MythicMobsClass.getMythicMobType(event.getEntity()) : null;
        String mobType = mythicType == null ? event.getEntityType().name() : mythicType;
        List<RegionType> regions = new ArrayList<>();
        for (RegionProvider provider : providers) {
            regions.addAll(provider.regionsAt(location, killer));
        }
        regions.add(new RegionType(location.getWorld().getName(), RegionType.Type.WORLD));
        Runnable record = () -> {
            for (RegionType region : regions) StatManager.killMob(killer, region, mobType);
        };
        if (NotRegionKills.isMythicMobsEnabled()) {
            // MythicMobs can revive entities after the death event; preserve the upstream safeguard.
            Bukkit.getScheduler().runTaskLater(NotRegionKills.getInstance(), () -> {
                if (!event.getEntity().isValid() || event.getEntity().getHealth() <= 0) record.run();
            }, 5L);
        } else {
            record.run();
        }
    }
}
