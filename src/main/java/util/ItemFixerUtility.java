package util;

import model.Database;
import model.Item;
import model.ItemDatabase;
import model.User;

import java.util.List;


public class ItemFixerUtility {


    public static void fixExistingItems() {
        System.out.println("=== FIXING EXISTING ITEMS ===");

        List<Item> items = ItemDatabase.readItems();
        List<User> users = Database.loadUsers();

        if (users.isEmpty()) {
            System.out.println("❌ No users found! Please create users first.");
            return;
        }

        if (items.isEmpty()) {
            System.out.println("ℹ️ No items found to fix.");
            return;
        }

        boolean updated = false;
        User defaultUser = users.get(0); // Use first user as default

        System.out.println("📋 Found " + items.size() + " items to check...");
        System.out.println("👤 Default user: " + defaultUser.getFullName() + " (" + defaultUser.getIdNumber() + ")");

        for (Item item : items) {
            boolean itemUpdated = false;

            // Fix missing uploader ID
            if (item.getUploaderIdNumber() == null || item.getUploaderIdNumber().trim().isEmpty()) {
                item.setUploaderIdNumber(defaultUser.getIdNumber());
                itemUpdated = true;
                System.out.println("✅ Fixed uploader for: " + item.getName());
            }

            // Fix missing location
            if (item.getLocation() == null || item.getLocation().trim().isEmpty() || "Unknown".equals(item.getLocation())) {
                item.setLocation("Campus - Location not specified");
                itemUpdated = true;
                System.out.println("📍 Fixed location for: " + item.getName());
            }

            // Fix missing date
            if (item.getDatePosted() == null || item.getDatePosted().trim().isEmpty() || "Unknown".equals(item.getDatePosted())) {
                item.setDatePosted("12/01/2024 10:00 AM");
                itemUpdated = true;
                System.out.println("📅 Fixed date for: " + item.getName());
            }

            // Fix missing status
            if (item.getStatus() == null || item.getStatus().trim().isEmpty()) {
                item.setStatus("Unclaimed");
                itemUpdated = true;
                System.out.println("🔄 Fixed status for: " + item.getName());
            }

            if (itemUpdated) {
                updated = true;
            }
        }

        if (updated) {
            ItemDatabase.saveItems(items);
            System.out.println("\n✅ All items have been updated and saved!");
        } else {
            System.out.println("\nℹ️ All items already have correct information.");
        }

        // Display summary
        System.out.println("\n=== SUMMARY ===");
        for (Item item : items) {
            User uploader = Database.findUserByIdNumber(item.getUploaderIdNumber());
            String uploaderName = uploader != null ? uploader.getFullName() : "Unknown";

            System.out.println("📦 " + item.getName());
            System.out.println("   👤 Found by: " + uploaderName);
            System.out.println("   📍 Location: " + item.getLocation());
            System.out.println("   📅 Date: " + item.getDatePosted());
            System.out.println("   🔄 Status: " + item.getStatus());
            System.out.println();
        }
    }

    /**
     * Main method to run the utility
     */
    public static void main(String[] args) {
        System.out.println("🔧 Starting Item Fixer Utility...\n");
        fixExistingItems();
        System.out.println("\n🎉 Item Fixer Utility completed!");
    }
}