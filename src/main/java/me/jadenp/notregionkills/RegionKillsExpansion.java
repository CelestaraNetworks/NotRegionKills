package me.jadenp.notregionkills;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * %notregionkills_total% - total mob kills
 * %notregionkills_mob_(mob type)% - mob kills of a certain type
 * %notregionkills_region_(region name)% - mob kills in a region
 * %notregionkills_world_(world name)% - mob kills in a world
 * %notregionkills_(mob type)_(region name)% - mob kills of a certain type in a region
 */
public class RegionKillsExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "notregionkills";
    }

    @Override
    public boolean persist() { return true; }

    @Override
    public @NotNull String getAuthor() {
        return "Not_Jaden";
    }

    @Override
    public @NotNull String getVersion() {
        return NotRegionKills.getInstance().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return null;
        if (params.startsWith("zone_")) {
            return Long.toString(StatManager.getRegionKills(player.getUniqueId(),
                    new RegionType(params.substring(5), RegionType.Type.CELESTARAZONES)));
        }
        if (params.startsWith("zonemob_")) {
            String query = params.substring(8);
            // Zone IDs may contain underscores. Choose the longest valid mob prefix.
            for (int split = query.lastIndexOf('_'); split > 0; split = query.lastIndexOf('_', split - 1)) {
                String mob = query.substring(0, split);
                if (StatManager.isMob(mob)) {
                    return Long.toString(StatManager.getTypedRegionMobKills(player.getUniqueId(), mob,
                            new RegionType(query.substring(split + 1), RegionType.Type.CELESTARAZONES)));
                }
            }
            return "";
        }
        if (params.equalsIgnoreCase("total")) {
            // %notregionkills_total% - total mob kills
            return StatManager.getTotalMobKills(player.getUniqueId()) + "";
        }
        if (params.startsWith("mob_")) {
            // %notregionkills_mob_(mob type)% - mob kills of a certain type
            String type = params.substring(4);
            if (StatManager.isMob(type)) {
                return StatManager.getMobKills(player.getUniqueId(), type) + "";
            }
            return "";
        } else if (params.startsWith("region_")) {
            // %notregionkills_region_(region name)% - mob kills in a region
            String regionName = params.substring(7);
            return StatManager.getRegionKills(player.getUniqueId(), new RegionType(regionName, RegionType.Type.WORLDGUARD)) + "";
        } else if (params.startsWith("world_")) {
            // %notregionkills_world_(world name)% - mob kills in a world
            String worldName = params.substring(6);
            return StatManager.getRegionKills(player.getUniqueId(), new RegionType(worldName, RegionType.Type.WORLD)) + "";
        } else if (params.contains("_")){
            // %notregionkills_(mob type)_(region name)% - mob kills of a certain type in a region
            String mobType = params.substring(0, params.lastIndexOf("_"));
            String regionName = params.substring(params.lastIndexOf("_") + 1);
            if (StatManager.isMob(mobType)) {
                return StatManager.getRegionMobKills(player.getUniqueId(), mobType, regionName) + "";
            }
            return "";
        }

        return null; // Placeholder is unknown by the Expansion
    }
}
