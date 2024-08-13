package models;

import utils.interfaces.ByteSerialize;
import utils.serializers.DoubleSerializer;
import utils.serializers.StringSerializer;

public class Area {
    @ByteSerialize(type = StringSerializer.class, identifier = (byte)0x88)
    private String areaName;

    @ByteSerialize(type = DoubleSerializer.class, identifier = (byte)0x89, length = 8)
    private Double area;

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }
}
