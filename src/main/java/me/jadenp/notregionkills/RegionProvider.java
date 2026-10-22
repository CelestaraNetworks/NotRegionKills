package me.jadenp.notregionkills;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.Collection;
interface RegionProvider {
    Collection<RegionType> regionsAt(Location location, Player killer);
}
