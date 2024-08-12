package models;

import utils.interfaces.ByteSerialize;
import utils.serializers.*;

import java.util.ArrayList;
import java.util.UUID;

public class Food {
    public Integer getFoodType() {
        return foodType;
    }

    public void setFoodType(Integer foodType) {
        this.foodType = foodType;
    }

    public String getFoodUUID() {
        return foodUUID;
    }

    public void setFoodUUID(String foodUUID) {
        this.foodUUID = foodUUID;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Double getFoodAmount() {
        return foodAmount;
    }

    public void setFoodAmount(Double foodAmount) {
        this.foodAmount = foodAmount;
    }

    public ArrayList<Double> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<Double> arrayList) {
        this.arrayList = arrayList;
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public void setZoneType(ZoneType zoneType) {
        this.zoneType = zoneType;
    }

    @ByteSerialize(type = StringSerializer.class, identifier = 0x10)
    String foodName;

    @ByteSerialize(type = UUIDSerializer.class, identifier = 0x75, length = 16)
    String foodUUID;

    @ByteSerialize(type = IntegerSerializer.class, identifier = 0x11, length = 4)
    Integer foodType;

    @ByteSerialize(type = DoubleSerializer.class, identifier = 0x12, length = 8)
    Double foodAmount;

    @ByteSerialize(type = ArraySerializer.class, identifier = 0x13, length = 8, innerType = DoubleSerializer.class)
    ArrayList<Double> arrayList;

    @ByteSerialize(identifier = 0x14)
    ZoneType zoneType;
}
