package me.jadenp.notregionkills;

import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RegionStatAdapter extends TypeAdapter<RegionStat> {
    @Override
    public void write(JsonWriter out, RegionStat value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        Type playerStatToken = new TypeToken<PlayerStat>(){}.getType();
        out.beginObject();
        out.name("regionKills");
        out.beginObject();
        // iterate through each region kill
        for (Map.Entry<RegionType, PlayerStat> entry : value.getRegionKills().entrySet()) {
            out.name(entry.getKey().getName() + "<:>" + entry.getKey().getType().toString());
            StatManager.gson.toJson(entry.getValue(), playerStatToken, out);
        }
        out.endObject();
        out.endObject();
    }

    @Override
    public RegionStat read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        Map<RegionType, PlayerStat> regionKills = new HashMap<>();
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("regionKills")) {
                regionKills = readRegionKills(in);
            } else {
                in.skipValue();
            }
        }
        in.endObject();

        return new RegionStat(regionKills);
    }

    private Map<RegionType, PlayerStat> readRegionKills(JsonReader in) throws IOException {
        Map<RegionType, PlayerStat> regionKills = new HashMap<>();
        Type playerStatToken = new TypeToken<PlayerStat>(){}.getType();
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName(); // region<:>type
            String regionName;
            RegionType.Type regionType;
            if (name.contains("<:>")) {
                regionName = name.substring(0, name.indexOf("<:>"));
                regionType = RegionType.Type.valueOf(name.substring(name.indexOf("<:>") + 3));
            } else {
                regionName = name;
                regionType = RegionType.Type.WORLDGUARD;
            }
            PlayerStat playerStat = StatManager.gson.fromJson(in, playerStatToken);
            regionKills.put(new RegionType(regionName, regionType), playerStat);
        }
        in.endObject();
        return regionKills;
    }
}
