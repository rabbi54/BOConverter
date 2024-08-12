import models.Food;
import models.ZoneType;
import org.jetbrains.annotations.NotNull;
import utils.serializers.ObjectSerializer;

import java.util.ArrayList;
import java.util.UUID;


public class Main {
    public static void main(String[] args) throws Exception {
        Food food = getFood();
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

        ZoneType deserializedZone = deserializedFood.getZoneType();

        System.out.println("Deserialized Zone: " + deserializedZone.getMaxValue()
                + " " + deserializedZone.getMinValue()
                + " " + deserializedZone.getUuid()
                + " " + deserializedZone.getLength()
                + " " + deserializedZone.getLatitude()
                + " " + deserializedZone.getLongitude()
                + " " + deserializedZone.getAltitude()
                + " " + deserializedZone.getBearing()
                + " " + deserializedZone.getAccuracy()
                + " " + deserializedZone.getSafe()
        );

        for (int i = 0; i < deserializedZone.getZones().size(); i++) {
            System.out.println(deserializedZone.getZones().get(i));
        }

    }

    private static @NotNull Food getFood() {
        Food food = new Food();
        food.setFoodType(42);
        food.setFoodName("Alu vorta");
        food.setFoodUUID("0000018d-f7a9-9575-a405-eede1217e657");
        ArrayList<Double> list = new ArrayList<>();
        list.add(1.1);
        list.add(2.2);
        list.add(3.3);
        food.setArrayList(list);

        ZoneType zoneType = new ZoneType();
        zoneType.setMaxValue(20);
        zoneType.setMinValue(10);
        zoneType.setLength(1723214160000L);
        zoneType.setUuid("0000018d-070e-5705-a405-eede1217e657");
        ArrayList<String> zones = new ArrayList<>();
        zones.add("Max");
        zones.add("Min");
        zones.add("Sem");
        zoneType.setZones(zones);
        zoneType.setAccuracy(16.219F);
        zoneType.setSafe(Boolean.TRUE);
        zoneType.setLatitude(37.7749);   // Latitude for San Francisco, CA
        zoneType.setLongitude(-122.4194); // Longitude for San Francisco, CA
        zoneType.setAltitude(15L);       // Altitude in meters (example: 15 meters above sea level)
        zoneType.setBearing((short) 120);       // Bearing in degrees (example: 120.5 degrees)

        food.setZoneType(zoneType);
        return food;
    }
}