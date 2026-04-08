package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Database;
import model.Item;
import model.ItemDatabase;
import model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GalleryController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> locationFilter;
    @FXML private ComboBox<String> sortFilter;
    @FXML private TilePane galleryPane;
    @FXML private HBox paginationContainer;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button page1;
    @FXML private Button page2;
    @FXML private Button page3;
    @FXML private Button page10;

    private int currentPage = 1;
    private final int itemsPerPage = 9;
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();
    private String currentUserId; // Track current user

    @FXML
    public void initialize() {
        initializeCurrentUser();
        setupFilters();
        loadAllItems();
        applyFiltersAndLoad();
        setupListeners();
    }

    /**
     * Initialize current user - in a real app this would come from login
     */
    private void initializeCurrentUser() {
        List<User> users = Database.loadUsers();
        if (!users.isEmpty()) {
            // Use first user as default for demo purposes
            this.currentUserId = users.get(0).getIdNumber();
            System.out.println("Gallery initialized with user: " + users.get(0).getFullName());
        }
    }

    private void setupFilters() {
        // Setup category filter - get unique categories from items
        categoryFilter.getItems().add("All");

        // Setup location filter - get unique locations from items
        locationFilter.getItems().add("All");

        // Setup sort filter
        sortFilter.getItems().addAll("Newest First", "Oldest First", "Name A-Z", "Name Z-A");

        // Set default selections
        categoryFilter.setValue("All");
        locationFilter.setValue("All");
        sortFilter.setValue("Newest First");
    }

    private void loadAllItems() {
        allItems = ItemDatabase.readItems();

        // Populate filter options based on actual data
        updateFilterOptions();
    }

    private void updateFilterOptions() {
        // Clear existing options (except "All")
        categoryFilter.getItems().clear();
        locationFilter.getItems().clear();

        categoryFilter.getItems().add("All");
        locationFilter.getItems().add("All");

        // Get unique categories and locations from items
        List<String> categories = allItems.stream()
                .map(Item::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> locations = allItems.stream()
                .map(Item::getLocation)
                .filter(location -> location != null && !location.isEmpty() && !"Location not specified".equals(location))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        categoryFilter.getItems().addAll(categories);
        locationFilter.getItems().addAll(locations);
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            currentPage = 1;
            applyFiltersAndLoad();
        });

        categoryFilter.setOnAction(e -> {
            currentPage = 1;
            applyFiltersAndLoad();
        });

        locationFilter.setOnAction(e -> {
            currentPage = 1;
            applyFiltersAndLoad();
        });

        sortFilter.setOnAction(e -> applyFiltersAndLoad());
    }

    /**
     * Applies filters, search, sort, then paginates
     */
    private void applyFiltersAndLoad() {
        filteredItems = new ArrayList<>(allItems);

        // Search filter
        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchLower = searchText.toLowerCase();
            filteredItems = filteredItems.stream()
                    .filter(item -> {
                        String name = (item.getName() != null) ? item.getName().toLowerCase() : "";
                        String desc = (item.getDescription() != null) ? item.getDescription().toLowerCase() : "";
                        return name.contains(searchLower) || desc.contains(searchLower);
                    })
                    .collect(Collectors.toList());
        }

        // Category filter
        String category = categoryFilter.getValue();
        if (category != null && !"All".equals(category)) {
            filteredItems = filteredItems.stream()
                    .filter(item -> category.equalsIgnoreCase(item.getCategory()))
                    .collect(Collectors.toList());
        }

        // Location filter
        String location = locationFilter.getValue();
        if (location != null && !"All".equals(location)) {
            filteredItems = filteredItems.stream()
                    .filter(item -> location.equalsIgnoreCase(item.getLocation()))
                    .collect(Collectors.toList());
        }

        // Sort filter
        String sort = sortFilter.getValue();
        if (sort != null) {
            switch (sort) {
                case "Newest First":
                    filteredItems.sort((a, b) -> {
                        String dateA = a.getDatePosted() != null ? a.getDatePosted() : "";
                        String dateB = b.getDatePosted() != null ? b.getDatePosted() : "";
                        return dateB.compareTo(dateA); // Reverse for newest first
                    });
                    break;
                case "Oldest First":
                    filteredItems.sort((a, b) -> {
                        String dateA = a.getDatePosted() != null ? a.getDatePosted() : "";
                        String dateB = b.getDatePosted() != null ? b.getDatePosted() : "";
                        return dateA.compareTo(dateB);
                    });
                    break;
                case "Name A-Z":
                    filteredItems.sort((a, b) -> {
                        String nameA = a.getName() != null ? a.getName() : "";
                        String nameB = b.getName() != null ? b.getName() : "";
                        return nameA.compareToIgnoreCase(nameB);
                    });
                    break;
                case "Name Z-A":
                    filteredItems.sort((a, b) -> {
                        String nameA = a.getName() != null ? a.getName() : "";
                        String nameB = b.getName() != null ? b.getName() : "";
                        return nameB.compareToIgnoreCase(nameA);
                    });
                    break;
            }
        }

        // Paginate
        paginateAndLoad();
        updatePaginationButtons();
    }

    private void paginateAndLoad() {
        if (filteredItems.isEmpty()) {
            galleryPane.getChildren().clear();
            Label noItems = new Label("No items found matching your criteria.");
            noItems.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 20;");
            galleryPane.getChildren().add(noItems);
            return;
        }

        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredItems.size());

        if (fromIndex >= filteredItems.size()) {
            fromIndex = 0;
            currentPage = 1;
            toIndex = Math.min(itemsPerPage, filteredItems.size());
        }

        List<Item> pageItems = filteredItems.subList(fromIndex, toIndex);
        loadItems(pageItems);
    }

    /**
     * Loads the given list of items into the gallery
     */
    private void loadItems(List<Item> items) {
        galleryPane.getChildren().clear();

        for (Item item : items) {
            VBox card = createItemCard(item);
            galleryPane.getChildren().add(card);
        }
    }

    /**
     * Creates a single item card UI with actual item details
     */
    private VBox createItemCard(Item item) {
        VBox card = new VBox();
        card.setSpacing(8);
        card.setStyle("-fx-padding: 12; -fx-border-color: #ccc; -fx-background-color: #f9f9f9; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        card.setPrefWidth(200);

        // Title
        String displayTitle = (item.getName() != null && !item.getName().isEmpty()) ? item.getName() : "Untitled";
        Label title = new Label(displayTitle);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 0 8 0 8;");
        title.setWrapText(true);

        // Description
        String description = item.getDescription() != null ? item.getDescription() : "No description";
        if (description.length() > 100) {
            description = description.substring(0, 97) + "...";
        }
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-padding: 0 8 0 8; -fx-text-fill: #666;");

        // Location - use actual location from item
        String location = item.getLocation() != null ? item.getLocation() : "Location not specified";
        Label locationLabel = new Label("📍 " + location);
        locationLabel.setStyle("-fx-padding: 0 8 0 8; -fx-text-fill: #888; -fx-font-size: 12;");

        // Status
        Label statusLabel = new Label(item.getStatus() != null ? item.getStatus() : "Unknown");
        statusLabel.setStyle("-fx-text-fill: " + getStatusColor(item.getStatus()) + "; -fx-font-weight: bold;");

        // Date - use actual date from item
        String datePosted = item.getDatePosted() != null ? item.getDatePosted() : "Date not specified";
        Label dateLabel = new Label("🕒 " + datePosted);
        dateLabel.setStyle("-fx-padding: 0 8 0 8; -fx-text-fill: #888; -fx-font-size: 12;");

        // Founder information - get from uploader ID
        String founderName = "Unknown Finder";
        if (item.getUploaderIdNumber() != null) {
            User founder = Database.findUserByIdNumber(item.getUploaderIdNumber());
            if (founder != null) {
                founderName = founder.getFullName();
            }
        }
        Label founderLabel = new Label("👤 " + founderName);
        founderLabel.setStyle("-fx-padding: 0 8 0 8; -fx-text-fill: #888; -fx-font-size: 12;");

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                Image image = new Image(item.getImagePath());
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading image: " + item.getImagePath());
            }
        }

        // Make entire card clickable
        card.setOnMouseClicked(event -> openItemDetails(item));

        // Hover effect
        card.setOnMouseEntered(event ->
                card.setStyle("-fx-padding: 12; -fx-border-color: #007bff; -fx-background-color: #f0f8ff; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;"));

        card.setOnMouseExited(event ->
                card.setStyle("-fx-padding: 12; -fx-border-color: #ccc; -fx-background-color: #f9f9f9; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;"));

        card.getChildren().addAll(imageView, title, descLabel, locationLabel, dateLabel, founderLabel, statusLabel);
        return card;
    }

    private void openItemDetails(Item item) {
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
            System.err.println("Error opening item details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns color code for status
     */
    private String getStatusColor(String status) {
        if (status == null) return "#666";
        switch (status.toLowerCase()) {
            case "pending":
                return "orange";
            case "claimed":
                return "green";
            case "unclaimed":
                return "red";
            default:
                return "#666";
        }
    }

    private void updatePaginationButtons() {
        int totalPages = (int) Math.ceil((double) filteredItems.size() / itemsPerPage);

        // Update button states
        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= totalPages || totalPages == 0);

        // Update page number buttons (simplified)
        page1.setText(String.valueOf(Math.max(1, currentPage - 1)));
        page2.setText(String.valueOf(currentPage));
        page3.setText(String.valueOf(Math.min(totalPages, currentPage + 1)));

        // Hide page10 if not needed
        page10.setVisible(totalPages > 5);
        if (totalPages > 5) {
            page10.setText(String.valueOf(totalPages));
        }
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            applyFiltersAndLoad();
        }
    }

    @FXML
    private void nextPage() {
        int totalPages = (int) Math.ceil((double) filteredItems.size() / itemsPerPage);
        if (currentPage < totalPages) {
            currentPage++;
            applyFiltersAndLoad();
        }
    }

    @FXML
    private void goToPage(MouseEvent event) {
        Button clicked = (Button) event.getSource();
        try {
            int targetPage = Integer.parseInt(clicked.getText());
            int totalPages = (int) Math.ceil((double) filteredItems.size() / itemsPerPage);

            if (targetPage >= 1 && targetPage <= totalPages) {
                currentPage = targetPage;
                applyFiltersAndLoad();
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid page number clicked.");
        }
    }

    @FXML
    private void refreshGallery() {
        loadAllItems();
        currentPage = 1;
        applyFiltersAndLoad();
    }

    // Navigation methods
    @FXML
    private void goToDashboard() {
        loadScene("dashboard.fxml");
    }

    @FXML
    private void goToReportItem() {
        loadScene("dashboard.fxml"); // Dashboard contains report form
    }

    @FXML
    private void logout() {
        loadScene("Login.fxml");
    }

    private void loadScene(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/" + fxmlFile));
            Stage stage = (Stage) galleryPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true); // Move this AFTER setScene
            stage.show(); // Optional: ensure the stage is shown
        } catch (IOException e) {
            System.err.println("Error loading scene: " + fxmlFile);
            e.printStackTrace();
        }
    }
}