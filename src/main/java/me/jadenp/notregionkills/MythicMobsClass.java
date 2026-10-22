package me.jadenp.notregionkills;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.Locale;

public class MythicMobsClass {
    private static final Set<String> mobTypes = new HashSet<>();

    private MythicMobsClass(){}

    /**
     * Checks whether the mob type is a mythic mob
     * @param mobType Mob Type - not case-sensitive
     * @return True if the mob is a mythic mob
     */
    public static boolean isMythicMob(String mobType) {
        mobType = mobType.toUpperCase(Locale.ROOT);
        return mobTypes.contains(mobType);
    }

    /**
     * Gets the mythic mob type of the living entity
     * @param entity Entity to check
     * @return The mob type, or null if the mob isn't a mythic mob
     */
    public static String getMythicMobType(LivingEntity entity) {
        ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
        if (mythicMob == null)
            return null;
        return mythicMob.getType().getInternalName();
    }

    public static void loadMobTypes() {
        mobTypes.clear();
        for (MythicMob mob : MythicBukkit.inst().getMobManager().getMobTypes()) {
            mobTypes.add(mob.getInternalName().toUpperCase(Locale.ROOT));
        }
    }
}
