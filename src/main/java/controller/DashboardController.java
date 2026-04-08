package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import model.ItemDatabase;
import model.Database;
import model.User;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import model.Item;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    // Report form fields
    @FXML private TextField itemNameField;
    @FXML private TextArea itemDescriptionArea;
    @FXML private ImageView previewImage;
    @FXML private Label fileNameLabel;
    @FXML private TextField yourNameField;
    @FXML private TextField locationField;
    @FXML private TextField dateTimeField;
    @FXML private TextField contactField;
    @FXML private Button submitButton;
    @FXML private Label feedbackLabel;

    // Search fields
    @FXML private TextField searchNameField;
    @FXML private TextField searchDescField;
    @FXML private TextField searchLocationField;
    @FXML private TextField searchDateField;
    @FXML private Button searchButton;

    // Navigation and content
    @FXML private ScrollPane dashboardScrollPane;
    @FXML private VBox reportSection;
    @FXML private VBox searchSection;
    @FXML private FlowPane recentItemsContainer;

    // Navigation buttons
    @FXML private Button navDashboard;
    @FXML private Button navReportItem;
    @FXML private Button navSearch;
    @FXML private Button navGallery;
    @FXML private Button btnLogout;

    private File selectedImageFile;
    private String currentUserId; // Track current user

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeCurrentUser();
        loadRecentItems();
        setupNavigationButtons();
    }

    /**
     * Initialize current user - in a real app this would come from login
     */
    private void initializeCurrentUser() {
        List<User> users = Database.loadUsers();
        if (!users.isEmpty()) {
            // Use first user as default for demo purposes
            this.currentUserId = users.get(0).getIdNumber();

            // Pre-fill user's name in the form
            if (yourNameField != null) {
                yourNameField.setText(users.get(0).getFullName());
            }

            // Pre-fill contact info with user's email
            if (contactField != null) {
                contactField.setText(users.get(0).getEmail());
            }

            System.out.println("Dashboard initialized with user: " + users.get(0).getFullName());
        }
    }

    private void setupNavigationButtons() {
        if (navReportItem != null) {
            navReportItem.setOnAction(e -> scrollTo(reportSection));
        }
        if (navSearch != null) {
            navSearch.setOnAction(e -> scrollTo(searchSection));
        }
        if (navDashboard != null) {
            navDashboard.setOnAction(e -> scrollTo(dashboardScrollPane.getContent()));
        }
        if (navGallery != null) {
            navGallery.setOnAction(e -> goToGallery());
        }
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Item Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            selectedImageFile = file;
            fileNameLabel.setText(file.getName());

            // Show preview
            try {
                Image image = new Image(file.toURI().toString());
                previewImage.setImage(image);
                previewImage.setVisible(false);
            } catch (Exception e) {
                System.err.println("Error loading image preview: " + e.getMessage());
            }
        } else {
            fileNameLabel.setText("No image selected");
        }
    }

    @FXML
    private void handleSubmitReport() {
        String yourName = yourNameField.getText().trim();
        String itemName = itemNameField.getText().trim();
        String itemDescription = itemDescriptionArea.getText().trim();
        String location = locationField.getText().trim();
        String dateTime = dateTimeField.getText().trim();
        String contact = contactField.getText().trim();
        String imagePath = selectedImageFile != null ? selectedImageFile.toURI().toString() : "";

        // Validation
        if (itemName.isEmpty() || itemDescription.isEmpty()) {
            feedbackLabel.setText("❌ Please fill out Item Name and Description.");
            feedbackLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Validate that we have a current user
        if (currentUserId == null) {
            feedbackLabel.setText("❌ User not found. Please log in again.");
            feedbackLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Create new item with ALL the form data
        Item newItem = new Item();
        newItem.setName(itemName);
        newItem.setDescription(itemDescription);
        newItem.setImagePath(imagePath);

        // Use the location from the form
        newItem.setLocation(location.isEmpty() ? "Location not specified" : location);

        newItem.setCategory("General"); // Default category

        // Use the date/time from the form if provided, otherwise use current time
        String finalDateTime;
        if (!dateTime.isEmpty()) {
            finalDateTime = dateTime;
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
            finalDateTime = LocalDateTime.now().format(formatter);
        }
        newItem.setDatePosted(finalDateTime);

        // Set default status to Unclaimed
        newItem.setStatus("Unclaimed");

        // IMPORTANT: Set the uploader ID to track who reported this item
        newItem.setUploaderIdNumber(currentUserId);

        // NEW: Store the actual form data for finder information
        newItem.setFinderName(yourName.isEmpty() ? "Anonymous" : yourName);
        newItem.setFinderContact(contact.isEmpty() ? "No contact provided" : contact);

        System.out.println("Creating item with:");
        System.out.println("  Uploader ID: " + currentUserId);
        System.out.println("  Finder Name: " + newItem.getFinderName());
        System.out.println("  Finder Contact: " + newItem.getFinderContact());
        System.out.println("  Location: " + newItem.getLocation());
        System.out.println("  Date: " + newItem.getDatePosted());

        // Save to JSON using ItemDatabase
        ItemDatabase.addItem(newItem);

        feedbackLabel.setText("✅ Item reported successfully!");
        feedbackLabel.setStyle("-fx-text-fill: green;");

        // Refresh recent items
        loadRecentItems();

        // Clear form
        clearForm();
    }

    @FXML
    private void handleSearchItems() {
        String searchName = searchNameField != null ? searchNameField.getText().trim().toLowerCase() : "";
        String searchDesc = searchDescField != null ? searchDescField.getText().trim().toLowerCase() : "";
        String searchLocation = searchLocationField != null ? searchLocationField.getText().trim().toLowerCase() : "";
        String searchDate = searchDateField != null ? searchDateField.getText().trim() : "";

        List<Item> allItems = ItemDatabase.readItems();
        List<Item> filteredItems = allItems.stream()
                .filter(item -> {
                    boolean nameMatch = searchName.isEmpty() ||
                            (item.getName() != null && item.getName().toLowerCase().contains(searchName));

                    boolean descMatch = searchDesc.isEmpty() ||
                            (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchDesc));

                    boolean locationMatch = searchLocation.isEmpty() ||
                            (item.getLocation() != null && item.getLocation().toLowerCase().contains(searchLocation));

                    boolean dateMatch = searchDate.isEmpty() ||
                            (item.getDatePosted() != null && item.getDatePosted().contains(searchDate));

                    return nameMatch && descMatch && locationMatch && dateMatch;
                })
                .collect(Collectors.toList());

        // Display filtered results
        displaySearchResults(filteredItems);
    }

    @FXML
    private void clearSearchFilters() {
        if (searchNameField != null) searchNameField.clear();
        if (searchDescField != null) searchDescField.clear();
        if (searchLocationField != null) searchLocationField.clear();
        if (searchDateField != null) searchDateField.clear();

        // Show all items
        loadRecentItems();
    }

    @FXML
    private void showAllItems() {
        loadRecentItems();
    }

    @FXML
    private void refreshItems() {
        loadRecentItems();
    }

    private void displaySearchResults(List<Item> items) {
        recentItemsContainer.getChildren().clear();

        if (items.isEmpty()) {
            Label noResults = new Label("No items found matching your search criteria.");
            noResults.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 20;");
            recentItemsContainer.getChildren().add(noResults);
            return;
        }

        for (Item item : items) {
            VBox card = createItemCard(item);
            recentItemsContainer.getChildren().add(card);
        }
    }

    private void loadRecentItems() {
        List<Item> items = ItemDatabase.readItems();
        recentItemsContainer.getChildren().clear();

        if (items.isEmpty()) {
            Label noItems = new Label("No items reported yet. Be the first to report a found item!");
            noItems.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 20;");
            recentItemsContainer.getChildren().add(noItems);
            return;
        }

        // Sort items by date (newest first)
        items.sort((a, b) -> {
            String dateA = a.getDatePosted() != null ? a.getDatePosted() : "";
            String dateB = b.getDatePosted() != null ? b.getDatePosted() : "";
            return dateB.compareTo(dateA);
        });

        for (Item item : items) {
            VBox card = createItemCard(item);
            recentItemsContainer.getChildren().add(card);
        }
    }

    private VBox createItemCard(Item item) {
        VBox card = new VBox();
        card.setSpacing(12);
        card.getStyleClass().add("item-card");
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Image container
        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPrefSize(250, 180);
        imageContainer.setMaxSize(250, 180);
        imageContainer.setStyle("-fx-border-color: #eee; -fx-border-radius: 4; -fx-background-color: #f9f9f9;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(170);
        imageView.setPreserveRatio(true);

        // Load image if available
        String imagePath = item.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image = new Image(imagePath);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Failed to load image from: " + imagePath);
                imageView.setImage(null);
            }
        }

        imageContainer.getChildren().add(imageView);

        // Item details
        Label nameLabel = new Label(item.getName() != null ? item.getName() : "Unnamed Item");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label descLabel = new Label(item.getDescription() != null ? item.getDescription() : "No description");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(240);
        descLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // Show actual location from the item
        String locationText = item.getLocation() != null ? item.getLocation() : "Location not specified";
        Label locationLabel = new Label("📍 " + locationText);
        locationLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        // Show actual date from the item
        String dateText = item.getDatePosted() != null ? item.getDatePosted() : "Date not specified";
        Label dateLabel = new Label("🕒 Found: " + dateText);
        dateLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        // Show who found it - USE ACTUAL FINDER NAME FROM FORM
        String founderName = "Unknown Finder";
        if (item.getFinderName() != null && !item.getFinderName().isEmpty()) {
            founderName = item.getFinderName();  // Use actual form data
        } else if (item.getUploaderIdNumber() != null) {
            // Fallback to user lookup only if no finder name stored
            User founder = Database.findUserByIdNumber(item.getUploaderIdNumber());
            if (founder != null) {
                founderName = founder.getFullName();
            }
        }
        Label founderLabel = new Label("👤 Found by: " + founderName);
        founderLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        Label statusLabel = new Label();
        String status = item.getStatus() != null ? item.getStatus() : "Unknown";
        switch (status) {
            case "Pending":
                statusLabel.setText("🟡 Pending");
                statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                break;
            case "Claimed":
                statusLabel.setText("🟢 Claimed");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                break;
            default:
                statusLabel.setText("🔴 Unclaimed");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                break;
        }

        Button viewButton = new Button("View Details");
        viewButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                "-fx-border-radius: 4; -fx-background-radius: 4; " +
                "-fx-padding: 8 16; -fx-cursor: hand;");

        viewButton.setOnMouseClicked(event -> openClaimView(item));

        card.getChildren().addAll(imageContainer, nameLabel, descLabel, locationLabel, dateLabel, founderLabel, statusLabel, viewButton);
        return card;
    }

    private void openClaimView(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/claim.fxml"));
            Parent root = loader.load();

            ClaimController claimController = loader.getController();
            // Set the current user so the claim controller knows who is viewing
            claimController.setCurrentUser(currentUserId);
            claimController.setItem(item);

            Stage stage = new Stage();
            stage.setTitle("Item Details - " + item.getName());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Error opening claim view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
        // Don't clear the user's name and contact since they're pre-filled
        // yourNameField.clear();
        // contactField.clear();

        itemNameField.clear();
        itemDescriptionArea.clear();
        locationField.clear();
        dateTimeField.clear();
        selectedImageFile = null;
        if (previewImage != null) {
            previewImage.setImage(null);
            previewImage.setVisible(false);
        }
        fileNameLabel.setText("No image selected");
    }

    // Navigation methods
    @FXML
    private void goToReport(MouseEvent event) {
        scrollTo(reportSection);
    }

    @FXML
    private void goToSearch(MouseEvent event) {
        scrollTo(searchSection);
    }

    @FXML
    private void goToGallery() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/gallery.fxml"));
            Stage stage = null;
            if (btnLogout != null && btnLogout.getScene() != null) {
                stage = (Stage) btnLogout.getScene().getWindow();
            } else if (dashboardScrollPane != null && dashboardScrollPane.getScene() != null) {
                stage = (Stage) dashboardScrollPane.getScene().getWindow();
            } else if (recentItemsContainer != null && recentItemsContainer.getScene() != null) {
                stage = (Stage) recentItemsContainer.getScene().getWindow();
            }

            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setMaximized(true);
            }
        } catch (IOException e) {
            System.err.println("Error loading gallery scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToGallery(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/gallery.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("Error loading gallery scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToClaim(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Claim Items");
        alert.setHeaderText("How to Claim Items");
        alert.setContentText("To claim an item, click 'View Details' on any item card below or browse all items in the Gallery.");
        alert.showAndWait();
    }

    @FXML
    private void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading login scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper methods
    private void scrollTo(Node node) {
        if (node != null && dashboardScrollPane != null) {
            double y = node.getLayoutY();
            double totalHeight = dashboardScrollPane.getContent().getBoundsInLocal().getHeight();
            double scrollValue = y / totalHeight;
            dashboardScrollPane.setVvalue(scrollValue);
        }
    }

    private void loadScene(String fxmlFile, MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/" + fxmlFile));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("Error loading scene: " + fxmlFile);
            e.printStackTrace();
        }
    }
}