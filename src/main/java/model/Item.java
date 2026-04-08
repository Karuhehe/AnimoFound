package model;

public class Item {
    private String name;
    private String description;
    private String imagePath;
    private String dateTime;
    private String datePosted;
    private String status;
    private String title;
    private String category;
    private String location;

    // NEW: Fields to track who uploaded and who claimed the item
    private String uploaderIdNumber;    // ID of user who uploaded/found the item
    private String claimerIdNumber;     // ID of user who claimed the item
    private String claimMessage;        // Message from claimer

    // NEW: Fields to store actual finder information from form
    private String finderName;          // Actual name from "Your Name" field
    private String finderContact;       // Actual contact from "Contact Information" field

    // ✅ Required: No-arg constructor
    public Item() {
    }

    public Item(String name, String description, String imagePath) {
        this(name, description, imagePath, "N/A", "Unclaimed"); // Defaults
    }

    public Item(String name, String description, String imagePath, String datePosted, String status) {
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.datePosted = datePosted;
        this.status = status;
    }

    // Constructor with uploader info
    public Item(String name, String description, String imagePath, String datePosted, String status, String uploaderIdNumber) {
        this(name, description, imagePath, datePosted, status);
        this.uploaderIdNumber = uploaderIdNumber;
    }

    // Existing getters
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public String getDatePosted() { return datePosted; }
    public String getStatus() { return status; }
    public String getDateTime() { return dateTime; }

    // Existing getters for claim info
    public String getUploaderIdNumber() { return uploaderIdNumber; }
    public String getClaimerIdNumber() { return claimerIdNumber; }
    public String getClaimMessage() { return claimMessage; }

    // NEW getters for finder information
    public String getFinderName() { return finderName; }
    public String getFinderContact() { return finderContact; }

    // Existing setters
    public void setName(String name) { this.name = name; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setDatePosted(String datePosted) { this.datePosted = datePosted; }
    public void setStatus(String status) { this.status = status; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    // Existing setters for claim info
    public void setUploaderIdNumber(String uploaderIdNumber) { this.uploaderIdNumber = uploaderIdNumber; }
    public void setClaimerIdNumber(String claimerIdNumber) { this.claimerIdNumber = claimerIdNumber; }
    public void setClaimMessage(String claimMessage) { this.claimMessage = claimMessage; }

    // NEW setters for finder information
    public void setFinderName(String finderName) { this.finderName = finderName; }
    public void setFinderContact(String finderContact) { this.finderContact = finderContact; }
}