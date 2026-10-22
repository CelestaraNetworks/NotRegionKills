package me.jadenp.notregionkills;

import java.util.*;
import net.celestara.zones.api.CelestaraZonesApi;
import net.celestara.zones.zone.ZoneDefinition;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicesManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CelestaraZonesProviderTest {
    @Test void tracksOverlappingAndGlobalZones() {
        CelestaraZonesApi api = mock(CelestaraZonesApi.class);
        ServicesManager services = mock(ServicesManager.class);
        Location location = new Location(mock(World.class), 0, 64, 0);
        when(services.load(CelestaraZonesApi.class)).thenReturn(api);
        ZoneDefinition first = mock(ZoneDefinition.class), second = mock(ZoneDefinition.class);
        when(first.id()).thenReturn("arena");
        when(second.id()).thenReturn("__global__");
        when(api.zonesAt(location)).thenReturn(List.of(first, second));
        try (var bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getServicesManager).thenReturn(services);
            var result = new CelestaraZonesProvider().regionsAt(location, mock(Player.class));
            assertEquals(Set.of(new RegionType("arena", RegionType.Type.CELESTARAZONES),
                    new RegionType("__global__", RegionType.Type.CELESTARAZONES)), new HashSet<>(result));
            verify(api).zonesAt(location);
        }
    }
    @Test void missingApiFailsClearly() {
        ServicesManager services = mock(ServicesManager.class);
        try (var bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getServicesManager).thenReturn(services);
            assertThrows(IllegalStateException.class, CelestaraZonesProvider::new);
        }
    }
}
