package me.jadenp.notregionkills;

import net.celestara.zones.api.CelestaraZonesApi;
import net.celestara.zones.zone.ZoneDefinition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collection;

final class CelestaraZonesProvider implements RegionProvider {
    private final CelestaraZonesApi api;
    CelestaraZonesProvider() {
        api = Bukkit.getServicesManager().load(CelestaraZonesApi.class);
        if (api == null) throw new IllegalStateException("CelestaraZones API service is unavailable");
    }
    public Collection<RegionType> regionsAt(Location location, Player killer) {
        Collection<RegionType> result = new ArrayList<>();
        for (ZoneDefinition zone : api.zonesAt(location))
            result.add(new RegionType(zone.id(), RegionType.Type.CELESTARAZONES));
        return result;
    }
}
