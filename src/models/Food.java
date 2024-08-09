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

    public UUID getFoodUUID() {
        return foodUUID;
    }

    public void setFoodUUID(UUID foodUUID) {
        this.foodUUID = foodUUID;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Float getFoodAmount() {
        return foodAmount;
    }

    public void setFoodAmount(Float foodAmount) {
        this.foodAmount = foodAmount;
    }

    public ArrayList<Double> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<Double> arrayList) {
        this.arrayList = arrayList;
    }

    @ByteSerialize(type = StringSerializer.class, identifier = 0x10)
    String foodName;

    @ByteSerialize(type = UUIDSerializer.class, identifier = 0x75, length = 16)
    UUID foodUUID;

    @ByteSerialize(type = IntegerSerializer.class, identifier = 0x11, length = 4)
    Integer foodType;

    @ByteSerialize(type = FloatSerializer.class, identifier = 0x12, length = 4)
    Float foodAmount;

    @ByteSerialize(type = ArraySerializer.class, identifier = 0x13, length = 8, innerType = DoubleSerializer.class)
    ArrayList<Double> arrayList;
}
