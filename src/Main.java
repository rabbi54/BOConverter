import models.Food;
import models.ZoneType;
import utils.serializers.ObjectSerializer;

import java.util.ArrayList;
import java.util.UUID;


public class Main {
    public static void main(String[] args) throws Exception {
        Food food = new Food();
        food.setFoodType(42);
        food.setFoodName("Alu vorta");
        food.setFoodUUID(new UUID(12, 12));
        food.setFoodAmount(19.89123d);
        ArrayList<Double> list = new ArrayList<>();
        list.add(1.1);
        list.add(2.2);
        list.add(3.3);
        food.setArrayList(list);

        ObjectSerializer objectSerializer = new ObjectSerializer();
        byte[] serializedFood = objectSerializer.serialize(food);
        System.out.println("Serialized Food: " + java.util.Arrays.toString(serializedFood));

        Food deserializedFood = (Food) objectSerializer.deserialize(serializedFood, Food.class);
        System.out.println("Deserialized Food: " + deserializedFood.getFoodType()
                    + " " + deserializedFood.getFoodName() + " " + deserializedFood.getFoodUUID()
                    + " " + deserializedFood.getFoodAmount()
        );

        for (int i = 0; i < deserializedFood.getArrayList().size(); i++) {
            System.out.println(deserializedFood.getArrayList().get(i));
        }

        ZoneType zoneType = new ZoneType();
        zoneType.setMaxValue(20);
        zoneType.setMinValue(10);
        zoneType.setLength(1723214160000L);
        zoneType.setUuid(new UUID(12631, 21782));
        ArrayList<String> zones = new ArrayList<>();
        zones.add("Max");
        zones.add("Min");
        zones.add("Sem");
        zoneType.setZones(zones);


        objectSerializer = new ObjectSerializer();
        byte[] serializedZone = objectSerializer.serialize(zoneType);
        System.out.println("Serialized Zone: " + java.util.Arrays.toString(serializedZone));

        ZoneType deserializedZone = (ZoneType) objectSerializer.deserialize(serializedZone, ZoneType.class);
        System.out.println("Deserialized Zone: " + deserializedZone.getMaxValue()
                + " " + deserializedZone.getMinValue() + " " + deserializedZone.getUuid() + " " + deserializedZone.getLength()
        );

        for (int i = 0; i < deserializedZone.getZones().size(); i++) {
            System.out.println(deserializedZone.getZones().get(i));
        }
    }
}