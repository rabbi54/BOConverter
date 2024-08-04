package models;

import utils.interfaces.ByteSerialize;

import java.util.UUID;

public class ZoneType {

    @ByteSerialize(type=Integer.class, identifier = 0x10, length = 4)
    Integer maxValue;

    @ByteSerialize(type=Integer.class, identifier = 0x11, length = 4)
    Integer minValue;

    @ByteSerialize(type= UUID.class, identifier = 0x75, length = 16)
    UUID uuid;

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
}
