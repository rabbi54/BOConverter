package models;

import utils.interfaces.ByteSerialize;

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

    public Double getFoodAmount() {
        return foodAmount;
    }

    public void setFoodAmount(Double foodAmount) {
        this.foodAmount = foodAmount;
    }

    @ByteSerialize(type = String.class, identifier = 0x10)
    String foodName;

    @ByteSerialize(type = UUID.class, identifier = 0x75, length = 16)
    UUID foodUUID;

    @ByteSerialize(type = Integer.class, identifier = 0x11, length = 4)
    Integer foodType;

    @ByteSerialize(type = Double.class, identifier = 0x12, length = 8)
    Double foodAmount;
}
