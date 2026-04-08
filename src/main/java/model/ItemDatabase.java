package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ItemDatabase {
    private static final String JSON_FILE_PATH = "items.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Reads all items from the JSON file
     */
    public static List<Item> readItems() {
        List<Item> items = new ArrayList<>();
        File file = new File(JSON_FILE_PATH);

        if (!file.exists()) {
            // Create empty JSON file if it doesn't exist
            saveItems(items);
            return items;
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Item>>(){}.getType();
            items = gson.fromJson(reader, listType);

            // Handle null case
            if (items == null) {
                items = new ArrayList<>();
            }

        } catch (IOException e) {
            System.err.println("Error reading items from JSON: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Saves all items to the JSON file
     */
    public static void saveItems(List<Item> items) {
        try (FileWriter writer = new FileWriter(JSON_FILE_PATH)) {
            gson.toJson(items, writer);
        } catch (IOException e) {
            System.err.println("Error saving items to JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adds a new item to the database
     */
    public static void addItem(Item item) {
        List<Item> items = readItems();
        items.add(item);
        saveItems(items);
    }

    /**
     * Updates an existing item in the database
     */
    public static void updateItem(Item updatedItem) {
        List<Item> items = readItems();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.getName().equals(updatedItem.getName()) &&
                    item.getDatePosted().equals(updatedItem.getDatePosted())) {
                items.set(i, updatedItem);
                break;
            }
        }

        saveItems(items);
    }

    /**
     * Deletes an item from the database
     */
    public static void deleteItem(String itemName, String datePosted) {
        List<Item> items = readItems();
        items.removeIf(item ->
                item.getName().equals(itemName) &&
                        item.getDatePosted().equals(datePosted));
        saveItems(items);
    }
}