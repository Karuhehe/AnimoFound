package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.User;
import model.Database;
import model.Item;
import model.ItemDatabase;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    // Users List components
    @FXML
    private TextField searchUsersField;
    @FXML
    private TableView<AdminUser> usersTable;
    @FXML
    private TableColumn<AdminUser, String> userIdColumn;
    @FXML
    private TableColumn<AdminUser, String> userNameColumn;
    @FXML
    private TableColumn<AdminUser, String> userEmailColumn;
    @FXML
    private TableColumn<AdminUser, String> userDateJoinedColumn;
    @FXML
    private TableColumn<AdminUser, String> userStatusColumn;
    @FXML
    private TableColumn<AdminUser, Void> userActionsColumn;

    // Reported Items components
    @FXML
    private ComboBox<String> filterReasonComboBox;
    @FXML
    private TableView<AdminItem> reportedItemsTable;
    @FXML
    private TableColumn<AdminItem, String> reportIdColumn;
    @FXML
    private TableColumn<AdminItem, String> itemNameColumn;
    @FXML
    private TableColumn<AdminItem, String> reportedByColumn;
    @FXML
    private TableColumn<AdminItem, String> reasonColumn;
    @FXML
    private TableColumn<AdminItem, String> reportDateColumn;
    @FXML
    private TableColumn<AdminItem, Void> reportActionsColumn;

    private ObservableList<AdminUser> usersList;
    private ObservableList<AdminItem> reportedItemsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeUsersTable();
        initializeReportedItemsTable();
        loadDataFromFiles();
    }



    private void initializeUsersTable() {
        // Use lambda expressions instead of PropertyValueFactory
        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().userId);
        userNameColumn.setCellValueFactory(cellData -> cellData.getValue().name);
        userEmailColumn.setCellValueFactory(cellData -> cellData.getValue().email);
        userDateJoinedColumn.setCellValueFactory(cellData -> cellData.getValue().dateJoined);
        userStatusColumn.setCellValueFactory(cellData -> cellData.getValue().status);

        // Add colored status badges for user status
        userStatusColumn.setCellFactory(column -> new TableCell<AdminUser, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label statusLabel = new Label(status);
                    statusLabel.getStyleClass().add("status-badge");

                    // Apply different styles based on status
                    switch (status.toLowerCase()) {
                        case "active":
                            statusLabel.getStyleClass().add("status-active");
                            break;
                        case "inactive":
                            statusLabel.getStyleClass().add("status-inactive");
                            break;
                        case "suspended":
                            statusLabel.getStyleClass().add("status-suspended");
                            break;
                        case "pending":
                            statusLabel.getStyleClass().add("status-pending");
                            break;
                        default:
                            statusLabel.getStyleClass().add("status-default");
                            break;
                    }

                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });

        // Add action buttons to users table
        userActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button();

            {
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.setText("🗑");
                deleteButton.setOnAction(event -> {
                    AdminUser user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        usersList = FXCollections.observableArrayList();
        usersTable.setItems(usersList);
    }

    private void initializeReportedItemsTable() {
        // Use lambda expressions instead of PropertyValueFactory
        reportIdColumn.setCellValueFactory(cellData -> cellData.getValue().reportId);
        itemNameColumn.setCellValueFactory(cellData -> cellData.getValue().itemName);
        reportedByColumn.setCellValueFactory(cellData -> cellData.getValue().reportedBy);
        reasonColumn.setCellValueFactory(cellData -> cellData.getValue().reason);
        reportDateColumn.setCellValueFactory(cellData -> cellData.getValue().reportDate);

        // Add colored status badges for reported items reason/status
        reasonColumn.setCellFactory(column -> new TableCell<AdminItem, String>() {
            @Override
            protected void updateItem(String reason, boolean empty) {
                super.updateItem(reason, empty);

                if (empty || reason == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label reasonLabel = new Label(reason);
                    reasonLabel.getStyleClass().add("status-badge");

                    // Apply different styles based on reason/status
                    switch (reason.toLowerCase()) {
                        case "unclaimed":
                            reasonLabel.getStyleClass().add("status-unclaimed");
                            break;
                        case "claimed":
                            reasonLabel.getStyleClass().add("status-claimed");
                            break;
                        case "pending":
                            reasonLabel.getStyleClass().add("status-pending");
                            break;
                        case "fake item":
                            reasonLabel.getStyleClass().add("status-fake");
                            break;
                        case "inappropriate":
                            reasonLabel.getStyleClass().add("status-inappropriate");
                            break;
                        case "spam":
                            reasonLabel.getStyleClass().add("status-spam");
                            break;
                        case "returned":
                            reasonLabel.getStyleClass().add("status-returned");
                            break;
                        default:
                            reasonLabel.getStyleClass().add("status-default");
                            break;
                    }

                    setGraphic(reasonLabel);
                    setText(null);
                }
            }
        });

        // Add action buttons to reported items table
        reportActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button();

            {
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.setText("🗑");
                deleteButton.setOnAction(event -> {
                    AdminItem item = getTableView().getItems().get(getIndex());
                    deleteReportedItem(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        reportedItemsList = FXCollections.observableArrayList();
        reportedItemsTable.setItems(reportedItemsList);

        // Initialize filter dropdown
        filterReasonComboBox.setItems(FXCollections.observableArrayList(
                "Filter by Reason", "Fake Item", "Inappropriate", "Spam", "Unclaimed", "Pending", "Claimed", "Returned"
        ));
        filterReasonComboBox.setValue("Filter by Reason");
        filterReasonComboBox.setOnAction(e -> filterReportedItems());
    }

    private void loadDataFromFiles() {
        // Load users from users.json
        usersList.clear();
        List<User> users = Database.loadUsers();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String currentDate = LocalDateTime.now().format(formatter);

        int userCounter = 1;
        for (User user : users) {
            AdminUser adminUser = new AdminUser(
                    "#" + String.format("%03d", userCounter++),
                    user.getFullName(),
                    user.getEmail(),
                    currentDate, // Since we don't track join date, use current date
                    "Active"
            );
            usersList.add(adminUser);
        }

        // Load items from items.json
        reportedItemsList.clear();
        List<Item> items = ItemDatabase.readItems();

        int itemCounter = 1;
        for (Item item : items) {
            AdminItem adminItem = new AdminItem(
                    "#R" + String.format("%03d", itemCounter++),
                    item.getName() != null ? item.getName() : "Unnamed Item",
                    "Anonymous User", // Since we don't track who reported it
                    item.getStatus() != null ? item.getStatus() : "Unknown",
                    item.getDatePosted() != null ? item.getDatePosted() : "Unknown"
            );
            reportedItemsList.add(adminItem);
        }
    }

    @FXML
    private void handleSearchUsers() {
        String searchText = searchUsersField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            usersTable.setItems(usersList);
        } else {
            ObservableList<AdminUser> filteredList = usersList.filtered(user ->
                    user.getName().toLowerCase().contains(searchText) ||
                            user.getEmail().toLowerCase().contains(searchText) ||
                            user.getUserId().toLowerCase().contains(searchText)
            );
            usersTable.setItems(filteredList);
        }
    }

    private void filterReportedItems() {
        String selectedReason = filterReasonComboBox.getValue();
        if (selectedReason.equals("Filter by Reason")) {
            reportedItemsTable.setItems(reportedItemsList);
        } else {
            ObservableList<AdminItem> filteredList = reportedItemsList.filtered(item ->
                    item.getReason().equalsIgnoreCase(selectedReason)
            );
            reportedItemsTable.setItems(filteredList);
        }
    }

    private void deleteUser(AdminUser user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Are you sure you want to delete this user?");
        alert.setContentText("User: " + user.getName());

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Remove from display list
            usersList.remove(user);

            // Also remove from actual users.json file
            List<User> users = Database.loadUsers();
            users.removeIf(u -> u.getFullName().equals(user.getName()) && u.getEmail().equals(user.getEmail()));
            Database.saveUsers(users);

            showAlert(Alert.AlertType.INFORMATION, "User Deleted", "User has been deleted successfully.");
        }
    }

    private void deleteReportedItem(AdminItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Reported Item");
        alert.setHeaderText("Are you sure you want to delete this reported item?");
        alert.setContentText("Item: " + item.getItemName());

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Remove from display list
            reportedItemsList.remove(item);

            // Also remove from actual items.json file
            List<Item> items = ItemDatabase.readItems();
            items.removeIf(i -> (i.getName() != null ? i.getName() : "Unnamed Item").equals(item.getItemName()));
            ItemDatabase.saveItems(items);

            showAlert(Alert.AlertType.INFORMATION, "Item Deleted", "Item has been deleted successfully.");
        }
    }

    @FXML
    private void refreshData() {
        loadDataFromFiles();
    }

    @FXML
    private void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Stage stage = (Stage) usersTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login - Animo Found");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load login screen: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner classes for table data

    public static class AdminUser {
        private final SimpleStringProperty userId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty email;
        private final SimpleStringProperty dateJoined;
        private final SimpleStringProperty status;

        public AdminUser(String userId, String name, String email, String dateJoined, String status) {
            this.userId = new SimpleStringProperty(userId);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.dateJoined = new SimpleStringProperty(dateJoined);
            this.status = new SimpleStringProperty(status);
        }

        // Getter methods for PropertyValueFactory
        public String getUserId() {
            return userId.get();
        }

        public String getName() {
            return name.get();
        }

        public String getEmail() {
            return email.get();
        }

        public String getDateJoined() {
            return dateJoined.get();
        }

        public String getStatus() {
            return status.get();
        }

        // Property methods for PropertyValueFactory (THESE ARE REQUIRED)
        public SimpleStringProperty userIdProperty() {
            return userId;
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public SimpleStringProperty emailProperty() {
            return email;
        }

        public SimpleStringProperty dateJoinedProperty() {
            return dateJoined;
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }
    }

    // Replace AdminItem class with this:
    public static class AdminItem {
        private final SimpleStringProperty reportId;
        private final SimpleStringProperty itemName;
        private final SimpleStringProperty reportedBy;
        private final SimpleStringProperty reason;
        private final SimpleStringProperty reportDate;

        public AdminItem(String reportId, String itemName, String reportedBy, String reason, String reportDate) {
            this.reportId = new SimpleStringProperty(reportId);
            this.itemName = new SimpleStringProperty(itemName);
            this.reportedBy = new SimpleStringProperty(reportedBy);
            this.reason = new SimpleStringProperty(reason);
            this.reportDate = new SimpleStringProperty(reportDate);
        }

        // Getter methods
        public String getReportId() {
            return reportId.get();
        }

        public String getItemName() {
            return itemName.get();
        }

        public String getReportedBy() {
            return reportedBy.get();
        }

        public String getReason() {
            return reason.get();
        }

        public String getReportDate() {
            return reportDate.get();
        }

        // Property methods for PropertyValueFactory (THESE ARE REQUIRED)
        public SimpleStringProperty reportIdProperty() {
            return reportId;
        }

        public SimpleStringProperty itemNameProperty() {
            return itemName;
        }

        public SimpleStringProperty reportedByProperty() {
            return reportedBy;
        }

        public SimpleStringProperty reasonProperty() {
            return reason;
        }

        public SimpleStringProperty reportDateProperty() {
            return reportDate;
        }
    }
}