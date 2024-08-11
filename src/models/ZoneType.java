package models;

import utils.interfaces.ByteSerialize;
import utils.serializers.*;

import java.util.ArrayList;
import java.util.UUID;

public class ZoneType {

    @ByteSerialize(type= IntegerSerializer.class, identifier = 0x10, length = 4)
    Integer maxValue;

    @ByteSerialize(type=IntegerSerializer.class, identifier = 0x11, length = 4)
    Integer minValue;

    @ByteSerialize(type= UUIDSerializer.class, identifier = 0x75, length = 16)
    String uuid;

    @ByteSerialize(type = TimeSerializer.class, identifier = 0x13, length = 4)
    Long length;

    @ByteSerialize(type = ArraySerializer.class, identifier = 0x12, length = 0, innerType = StringSerializer.class)
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ArrayList<String> getZones() {
        return zones;
    }

    public void setZones(ArrayList<String> zones) {
        this.zones = zones;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }
}
