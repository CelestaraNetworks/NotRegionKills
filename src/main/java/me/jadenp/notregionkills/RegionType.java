package me.jadenp.notregionkills;

import java.util.Objects;

public class RegionType {
    private final String name;
    private final Type type;

    public enum Type {
        WORLDGUARD, WORLD, CELESTARAZONES
    }

    public RegionType(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "RegionType{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegionType)) return false;
        RegionType that = (RegionType) o;
        return Objects.equals(name, that.name) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
