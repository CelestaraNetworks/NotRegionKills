package me.jadenp.notregionkills;

import java.util.HashMap;
import java.util.Map;

public class PlayerStat {
    /**
     * Entity type, number of kills
     */
    private final Map<String, Long> entityKills;
    public PlayerStat() {
        entityKills = new HashMap<>();
    }
    public PlayerStat(Map<String, Long> entityKills) {
        this.entityKills = entityKills;
    }

    public Map<String, Long> getEntityKills() {
        return entityKills;
    }
    public void killEntity(String entityType) {
        entityKills.merge(entityType, 1L, Long::sum);
    }

    public long totalKills() {
        long total = 0;
        for (long count : entityKills.values()) total += count;
        return total;
    }

    @Override
    public String toString() {
        return "PlayerStat{" +
                "entityKills=" + entityKills +
                '}';
    }
}
