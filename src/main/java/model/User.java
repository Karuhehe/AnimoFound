package model;

import java.util.Objects;

public class User {
    private String fullName;
    private String idNumber;
    private String email;
    private String password;

    // Required for GSON deserialization
    public User() {
    }

    public User(String fullName, String idNumber, String email, String password) {
        this.fullName = fullName;
        this.idNumber = idNumber;
        this.email = email;
        this.password = password;
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Optional: for easier debugging or display
    @Override
    public String toString() {
        return fullName + " (" + idNumber + ")";
    }

    // Optional: for comparing users
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return Objects.equals(idNumber, other.idNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNumber);
    }
}
