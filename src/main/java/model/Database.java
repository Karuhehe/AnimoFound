package model;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import model.User;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String FILE_PATH = "users.json"; // Saved in root directory of project

    public static List<User> loadUsers() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type userListType = new TypeToken<List<User>>() {}.getType();
            return new Gson().fromJson(reader, userListType);
        } catch (FileNotFoundException e) {
            return new ArrayList<>(); // No users yet
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveUsers(List<User> users) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            new Gson().toJson(users, writer);
            System.out.println("✅ Saved " + users.size() + " user(s) to " + new File(FILE_PATH).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Find a user by their ID number
     */
    public static User findUserByIdNumber(String idNumber) {
        List<User> users = loadUsers();
        return users.stream()
                .filter(user -> user.getIdNumber().equals(idNumber))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find a user by their email
     */
    public static User findUserByEmail(String email) {
        List<User> users = loadUsers();
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
}