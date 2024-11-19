package models;

import utils.interfaces.ByteSerialize;
import utils.serializers.*;

import java.util.ArrayList;
import java.util.UUID;

public class ZoneType {

    @ByteSerialize(type= IntegerSerializer.class, identifier = 0x10, length = 4)
    Integer maxValue;

    @ByteSerialize(type = IntegerSerializer.class, identifier = 0x11, length = 4)
    Integer minValue;

    @ByteSerialize(type= UUIDSerializer.class, identifier = 0x75, length = 16)
    String uuid;

    @ByteSerialize(type = TimeSerializer.class, identifier = 0x13, length = 4)
    Long length;

    @ByteSerialize(type = ArraySerializer.class, identifier = 0x12, length = 0, innerType = StringSerializer.class)
    ArrayList<String> zones;

    @ByteSerialize(type = LocationDataSerializer.class, identifier = 0x14, length = 8)
    Double latitude;

    @ByteSerialize(type = LocationDataSerializer.class, identifier = 0x15, length = 8)
    Double longitude;

    @ByteSerialize(type = BooleanSerializer.class, identifier = 0x16, length = 1)
    Boolean isSafe;

    @ByteSerialize(type = FloatSerializer.class, identifier = 0x17, length = 4)
    Float accuracy;

    @ByteSerialize(type = ShortSerializer.class, identifier = 0x18, length = 2)
    Short bearing;

    @ByteSerialize(type = LongSerializer.class, identifier = 0x19, length = 8)
    Long altitude;

    @ByteSerialize(type=ArraySerializer.class, identifier = 0x1A)
    ArrayList<Area>areas;

    @ByteSerialize(type=ArraySerializer.class, identifier = 0x1B, length = 8, innerType = SleepBinningSerializer.class)
    ArrayList<SleepBinning>sleepBinnings;

    public ArrayList<SleepBinning> getSleepBinnings() {
        return sleepBinnings;
    }

    public void setSleepBinnings(ArrayList<SleepBinning> sleepBinnings) {
        this.sleepBinnings = sleepBinnings;
    }

    public ArrayList<Area> getAreas() {
        return areas;
    }

    public void setAreas(ArrayList<Area> areas) {
        this.areas = areas;
    }

    public Boolean getSafe() {
        return isSafe;
    }

    public void setSafe(Boolean safe) {
        isSafe = safe;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public Short getBearing() {
        return bearing;
    }

    public void setBearing(Short bearing) {
        this.bearing = bearing;
    }

    public Long getAltitude() {
        return altitude;
    }

    public void setAltitude(Long altitude) {
        this.altitude = altitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

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
