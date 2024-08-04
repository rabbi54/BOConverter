import models.Food;
import models.ZoneType;
import utils.serializers.ObjectSerializer;

import java.util.UUID;


public class Main {
    public static void main(String[] args) throws Exception {
        Food food = new Food();
        food.setFoodType(42);
        food.setFoodName("Alu vorta");
        food.setFoodUUID(new UUID(12, 12));
        food.setFoodAmount(19.89);

        ObjectSerializer objectSerializer = new ObjectSerializer(food);
        byte[] serializedFood = objectSerializer.serialize();
        System.out.println("Serialized Food: " + java.util.Arrays.toString(serializedFood));

        Food deserializedFood = (Food) objectSerializer.deserialize(serializedFood, Food.class);
        System.out.println("Deserialized Food: " + deserializedFood.getFoodType()
                    + " " + deserializedFood.getFoodName() + " " + deserializedFood.getFoodUUID()
                    + " " + deserializedFood.getFoodAmount()
        );

        ZoneType zoneType = new ZoneType();
        zoneType.setMaxValue(20);
        zoneType.setMinValue(10);
        zoneType.setUuid(new UUID(12631, 21782));

        objectSerializer = new ObjectSerializer(zoneType);
        byte[] serializedZone = objectSerializer.serialize();
        System.out.println("Serialized Zone: " + java.util.Arrays.toString(serializedZone));

        ZoneType deserializedZone = (ZoneType) objectSerializer.deserialize(serializedZone, ZoneType.class);
        System.out.println("Deserialized Zone: " + deserializedZone.getMaxValue()
                + " " + deserializedZone.getMinValue() + " " + deserializedZone.getUuid()
        );
    }
}