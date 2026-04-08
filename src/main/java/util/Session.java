package util;

import model.User;

public class Session {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }

    public static String getCurrentUserName() {
        return currentUser != null ? currentUser.getFullName() : "Anonymous";
    }

    public static String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "unknown@email.com";
    }

    public static String getCurrentUserId() {
        return currentUser != null ? currentUser.getIdNumber() : "000000";
    }
}