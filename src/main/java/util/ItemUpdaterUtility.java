package util;

import model.Database;
import model.Item;
import model.ItemDatabase;
import model.User;

import java.util.List;


public class ItemUpdaterUtility {


    public static void updateItemsWithUploaderInfo() {
        List<Item> items = ItemDatabase.readItems();
        List<User> users = Database.loadUsers();

        if (users.isEmpty()) {
            System.out.println("No users found in database. Cannot update items.");
            return;
        }

        boolean updated = false;

        for (Item item : items) {
            if (item.getUploaderIdNumber() == null || item.getUploaderIdNumber().trim().isEmpty()) {
                // Assign to first user (or you could randomize this)
                User randomUser = users.get(0);
                item.setUploaderIdNumber(randomUser.getIdNumber());

                System.out.println("Updated item '" + item.getName() + "' with uploader: " + randomUser.getFullName());
                updated = true;
            }
        }

        if (updated) {
            ItemDatabase.saveItems(items);
            System.out.println("All items updated and saved!");
        } else {
            System.out.println("All items already have uploader information.");
        }
    }


    public static void main(String[] args) {
        System.out.println("Starting item update process...");

        // Load and display current users
        List<User> users = Database.loadUsers();
        System.out.println("Found " + users.size() + " users:");
        for (User user : users) {
            System.out.println("  - " + user.getFullName() + " (" + user.getIdNumber() + ")");
        }

        // Update items
        updateItemsWithUploaderInfo();

        // Display updated items
        List<Item> items = ItemDatabase.readItems();
        System.out.println("\nUpdated items:");
        for (Item item : items) {
            String uploaderName = "Unknown";
            if (item.getUploaderIdNumber() != null) {
                User uploader = Database.findUserByIdNumber(item.getUploaderIdNumber());
                if (uploader != null) {
                    uploaderName = uploader.getFullName();
                }
            }
            System.out.println("  - " + item.getName() + " uploaded by: " + uploaderName);
        }
    }
}