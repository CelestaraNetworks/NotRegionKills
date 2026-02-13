package me.jadenp.notregionkills;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class StatManager {
    private StatManager(){}
    public static final Gson gson;
    static {
        // create a Gson object
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(RegionStat.class, new RegionStatAdapter());
        gson = builder.create();
    }

    /**
     * UUID of the player, Region kill stats
     */
    private static Map<UUID, RegionStat> allPlayerStats = new HashMap<>();

    /**
     * Loads saved kills from stats.json
     * @throws IOException if there was an error creating or reading from the file
     */
    public static void loadSavedKills() throws IOException {
        File save = getSaveFile();

        // read json from stats file & save to map
        Type mapType = new TypeToken<Map<UUID, RegionStat>>(){}.getType();
        try (JsonReader reader = new JsonReader(new FileReader(save))) {
            allPlayerStats = gson.fromJson(reader, mapType);
        }

        // check to see if the file was empty or had a null value in it
        if (allPlayerStats == null)
            allPlayerStats = new HashMap<>();
    }

    /**
     * Saves stats.json with the current mob kill stats
     * @throws IOException if there was an error creating or writing to the file
     */
    public static void save() throws IOException {
        File save = getSaveFile();

        Type mapType = new TypeToken<Map<UUID, RegionStat>>(){}.getType();
        JsonWriter writer = new JsonWriter(new FileWriter(save));
        gson.toJson(allPlayerStats, mapType, writer);
        writer.close();
    }

    public static boolean isMob(String type) {
        type = type.toUpperCase();
        if (NotRegionKills.isMythicMobsEnabled() && MythicMobsClass.isMythicMob(type))
            return true;
        try {
            if (EntityType.valueOf(type).isAlive())
                return true;
        } catch (IllegalArgumentException ignored) {
            // unknown entity type
        }
        return false;
    }

    /**
     * Add a player's kill to the statistics
     * @param killer player who killed the mob
     * @param region region the mob was killed in
     * @param mobType the type of mob
     */
    public static void killMob(Player killer, RegionType region, String mobType) {
        mobType = mobType.toUpperCase();
        if (allPlayerStats.containsKey(killer.getUniqueId())) {
            allPlayerStats.get(killer.getUniqueId()).killEntity(region, mobType);
        } else {
            RegionStat stat = new RegionStat();
            stat.killEntity(region, mobType);
            allPlayerStats.put(killer.getUniqueId(), stat);
        }
    }

    /**
     * Get the stats.json file from the data folder
     * @return the stats.json save file
     */
    private static File getSaveFile() throws IOException {
        File save = new File(NotRegionKills.getInstance().getDataFolder() + File.separator + "stats.json");
        // check if the file exists/create a new file
        if (NotRegionKills.getInstance().getDataFolder().mkdir()) {
            Bukkit.getLogger().info("[NotRegionKills] Created new directory folder.");
        }
        if (save.createNewFile()) {
            Bukkit.getLogger().info("[NotRegionKills] Created new save file.");
        }
        return save;
    }

    /**
     * Get the total mob kills a player has
     * @param uuid UUID of the player
     * @return The total recorded mob kills
     */
    public static long getTotalMobKills(UUID uuid) {
        // check if uuid exists in stats
        if (!allPlayerStats.containsKey(uuid))
            return 0;
        // get region stats
        RegionStat regionStat = allPlayerStats.get(uuid);
        // go through every region stat and add up all the recorded kills
        AtomicLong total = new AtomicLong();
        regionStat.getRegionKills().entrySet().stream()
                .filter(entry -> entry.getKey().getType() == RegionType.Type.WORLD).forEach(entry -> entry.getValue()
                        .getEntityKills().values().forEach(total::addAndGet));
        return total.get();
    }

    /**
     * Get the mob kills of a specific mob type in a specific region for a player
     * @param uuid UUID of the player
     * @param mobType Type of mob
     * @param region Name of the region to get kills for
     * @return the number of killed mobs of mobType in the region for the player
     */
    public static long getRegionMobKills(UUID uuid, String mobType, String region) {
        mobType = mobType.toUpperCase();
        // check if uuid exists in stats
        if (!allPlayerStats.containsKey(uuid))
            return 0;
        // get region stats
        RegionStat regionStat = allPlayerStats.get(uuid);
        // get player stats
        PlayerStat playerStat = null;
        if (regionStat.getRegionKills().containsKey(new RegionType(region, RegionType.Type.WORLDGUARD))) {
            playerStat = regionStat.getRegionKills().get(new RegionType(region, RegionType.Type.WORLDGUARD));
        } else if (regionStat.getRegionKills().containsKey(new RegionType(region, RegionType.Type.WORLD))) {
            playerStat = regionStat.getRegionKills().get(new RegionType(region, RegionType.Type.WORLD));
        }
        // check if mob has been recorded for this player
        if (playerStat == null || !playerStat.getEntityKills().containsKey(mobType))
            return 0;
        return playerStat.getEntityKills().get(mobType);
    }

    /**
     * Get the mob kills in a specific region for a player
     * @param uuid UUID of the player
     * @param region region to get the kills for
     * @return the amount of mobs the player has killed in this region
     */
    public static long getRegionKills(UUID uuid, RegionType region) {
        // check if uuid exists in stats
        if (!allPlayerStats.containsKey(uuid))
            return 0;
        // get region stats
        RegionStat regionStat = allPlayerStats.get(uuid);
        // check if the specified region has been recorded for this player
        if (!regionStat.getRegionKills().containsKey(region))
            return 0;
        // get player stats
        PlayerStat playerStat = regionStat.getRegionKills().get(region);
        AtomicLong total = new AtomicLong();
        // count up all the stored values in playerStat
        playerStat.getEntityKills().values().forEach(total::addAndGet);
        return total.get();
    }

    /**
     * Get the mob kills of a certain type for a player
     * @param uuid UUID of the player
     * @param mobType type of mob
     * @return the amount of mobs the player has killed that are mobType
     */
    public static long getMobKills(UUID uuid, String mobType) {
        mobType = mobType.toUpperCase();
        // check if uuid exists in stats
        if (!allPlayerStats.containsKey(uuid))
            return 0;
        // get region stats
        RegionStat regionStat = allPlayerStats.get(uuid);
        // iterate through all the regions in regionStat and get the sum of the matching mobTypes
        String finalMobType = mobType;
        // using world regions for this
        return regionStat.getRegionKills().entrySet().stream()
                .filter(entry -> entry.getKey().getType() == RegionType.Type.WORLD && entry.getValue().getEntityKills()
                        .containsKey(finalMobType)).mapToLong(entry -> entry.getValue().getEntityKills()
                        .get(finalMobType)).sum();
    }

}
