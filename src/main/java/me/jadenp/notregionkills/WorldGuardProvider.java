package me.jadenp.notregionkills;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collection;
final class WorldGuardProvider implements RegionProvider {
    private static StateFlag trackKills;
    static void registerFlag() {
        com.sk89q.worldguard.protection.flags.registry.FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            trackKills = new StateFlag("track-kills", true);
            registry.register(trackKills);
        } catch (com.sk89q.worldguard.protection.flags.registry.FlagConflictException e) {
            if (!(registry.get("track-kills") instanceof StateFlag))
                throw new IllegalStateException("track-kills is already registered with a different type", e);
            trackKills = (StateFlag) registry.get("track-kills");
        }
    }
    private final RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
    public Collection<RegionType> regionsAt(Location location, Player killer) {
        Collection<RegionType> result = new ArrayList<>();
        query.getApplicableRegions(BukkitAdapter.adapt(location)).getRegions().forEach(region -> {
            if (region.getFlag(trackKills) != StateFlag.State.DENY)
                result.add(new RegionType(region.getId(), RegionType.Type.WORLDGUARD));
        });
        return result;
    }
}
