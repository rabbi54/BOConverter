package models;

import utils.interfaces.ByteSerialize;

import java.util.ArrayList;
import java.util.UUID;

public class ZoneType {

    @ByteSerialize(type=Integer.class, identifier = 0x10, length = 4)
    Integer maxValue;

    @ByteSerialize(type=Integer.class, identifier = 0x11, length = 4)
    Integer minValue;

    @ByteSerialize(type= UUID.class, identifier = 0x75, length = 16)
    UUID uuid;

    @ByteSerialize(type = ArrayList.class, identifier = 0x12, length = 3, innerType = String.class)
    ArrayList<String> zones;

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ArrayList<String> getZones() {
        return zones;
    }

    public void setZones(ArrayList<String> zones) {
        this.zones = zones;
    }
}
