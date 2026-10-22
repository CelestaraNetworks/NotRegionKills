package me.jadenp.notregionkills;

import com.google.gson.reflect.TypeToken;
import java.util.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KillTrackingTest {
    private final UUID id = UUID.randomUUID();
    private Player player() {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(id);
        return player;
    }
    @Test void totalsCountWorldsOnceDespiteOverlappingProviders() {
        Player player = player();
        for (RegionType.Type type : RegionType.Type.values())
            StatManager.killMob(player, new RegionType("arena", type), "zombie");
        assertEquals(1, StatManager.getTotalMobKills(id));
        assertEquals(1, StatManager.getMobKills(id, "zombie"));
        for (RegionType.Type type : RegionType.Type.values())
            assertEquals(1, StatManager.getRegionKills(id, new RegionType("arena", type)));
    }
    @Test void serializationPreservesOldAndNewProviderKeys() {
        RegionStat stat = new RegionStat();
        for (RegionType.Type type : RegionType.Type.values()) stat.killEntity(new RegionType("arena", type), "ZOMBIE");
        String json = StatManager.gson.toJson(stat);
        RegionStat restored = StatManager.gson.fromJson(json, RegionStat.class);
        assertEquals(3, restored.getRegionKills().size());
        for (PlayerStat kills : restored.getRegionKills().values()) assertEquals(1, kills.totalKills());
    }
    @Test void legacyKeysAndUnknownPropertiesLoad() {
        RegionStat restored = StatManager.gson.fromJson("{\"future\":{},\"regionKills\":{\"arena\":{\"entityKills\":{\"ZOMBIE\":7}}}}", RegionStat.class);
        assertEquals(7, restored.getRegionKills().get(new RegionType("arena", RegionType.Type.WORLDGUARD)).totalKills());
    }
    @Test void missingPlayerReturnsZero() {
        assertEquals(0, StatManager.getTotalMobKills(id));
        assertEquals(0, StatManager.getTypedRegionMobKills(id, "zombie", new RegionType("arena", RegionType.Type.CELESTARAZONES)));
    }
    @Test void mobNormalizationIsLocaleIndependent() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            StatManager.killMob(player(), new RegionType("world", RegionType.Type.WORLD), "silverfish");
            assertEquals(1, StatManager.getMobKills(id, "SILVERFISH"));
        } finally { Locale.setDefault(original); }
    }
    @Test void deathUsesEntityWorldAndRecordsBothProviders() {
        Player player = player();
        LivingEntity entity = mock(LivingEntity.class);
        World world = mock(World.class);
        when(world.getName()).thenReturn("death_world");
        when(entity.getKiller()).thenReturn(player);
        when(entity.getLocation()).thenReturn(new Location(world, 1, 2, 3));
        when(entity.getType()).thenReturn(EntityType.ZOMBIE);
        EntityDeathEvent event = mock(EntityDeathEvent.class);
        when(event.getEntity()).thenReturn(entity);
        when(event.getEntityType()).thenReturn(EntityType.ZOMBIE);
        RegionType zone = new RegionType("test_zone", RegionType.Type.CELESTARAZONES);
        new Events(List.of((location, killer) -> List.of(zone))).onEntityDeath(event);
        assertEquals(1, StatManager.getRegionKills(id, zone));
        assertEquals(1, StatManager.getRegionKills(id, new RegionType("death_world", RegionType.Type.WORLD)));
        verify(player, never()).getWorld();
    }
    @Test void zonePlaceholderSupportsUnderscores() {
        Player player = player();
        StatManager.killMob(player, new RegionType("my_test_zone", RegionType.Type.CELESTARAZONES), "ZOMBIE");
        RegionKillsExpansion expansion = new RegionKillsExpansion();
        assertEquals("1", expansion.onRequest(player, "zone_my_test_zone"));
        assertEquals("1", expansion.onRequest(player, "zonemob_zombie_my_test_zone"));
        assertNull(expansion.onRequest(null, "total"));
    }
}
