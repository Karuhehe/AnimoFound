package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Database;
import model.Item;
import model.ItemDatabase;
import model.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClaimController {

    @FXML private ImageView itemImage;
    @FXML private Label itemName;
    @FXML private Label itemDesc;
    @FXML private Label itemLocation;
    @FXML private Label itemDateTime;
    @FXML private Label statusLabel;
    @FXML private Label statusIndicator;
    @FXML private Button claimButton;
    @FXML private Button confirmButton;
    @FXML private Button messageButton;
    @FXML private Button backButton;


    // Additional labels that might be in FXML
    @FXML private Label founderName;
    @FXML private Label founderEmail;
    @FXML private Label claimerNameLabel;
    @FXML private Label claimerEmailLabel;
    @FXML private Label claimerMessageLabel;
    @FXML private Label successLabel;
    @FXML private Label actionRequiredLabel;

    private Item selectedItem;
    private String currentUserId; // The currently logged-in user's ID

    /**
     * Set the current user (should be called when navigating to this page)
     */
    public void setCurrentUser(String userId) {
        this.currentUserId = userId;
    }

    /**
     * Initialize with a default user for testing (call this if no user is set)
     */
    public void initializeWithDefaultUser() {
        if (this.currentUserId == null) {
            List<User> users = Database.loadUsers();
            if (!users.isEmpty()) {
                this.currentUserId = users.get(0).getIdNumber(); // Use first user as default
                System.out.println("Initialized with default user: " + users.get(0).getFullName());
            }
        }
    }

    /** Called when user clicks an item to view details */
    public void setItem(Item item) {
        this.selectedItem = item;

        // Initialize with default user if none set (for testing)
        initializeWithDefaultUser();

        populateItemDetails();
    }

    private void populateItemDetails() {
        if (selectedItem == null) return;

        // Set item name/title
        String displayName = selectedItem.getName() != null ? selectedItem.getName() : "Unnamed Item";
        if (itemName != null) {
            itemName.setText(displayName);
        }

        // Set description
        String description = selectedItem.getDescription() != null ? selectedItem.getDescription() : "No description available";
        if (itemDesc != null) {
            itemDesc.setText(description);
        }

        // Set location - use actual location from the item
        String location = selectedItem.getLocation() != null ? selectedItem.getLocation() : "Location not specified";
        if (itemLocation != null) {
            itemLocation.setText(location);
        }

        // Set date/time - use actual date from the item
        String dateTime = selectedItem.getDatePosted() != null ? selectedItem.getDatePosted() : "Date not specified";
        if (itemDateTime != null) {
            itemDateTime.setText(dateTime);
        }

        // Set status
        String status = selectedItem.getStatus() != null ? selectedItem.getStatus() : "Unknown";
        updateStatusDisplay(status);

        // Load image
        if (itemImage != null && selectedItem.getImagePath() != null && !selectedItem.getImagePath().isEmpty()) {
            try {
                Image image = new Image(selectedItem.getImagePath());
                itemImage.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }

        // Set founder information
        populateFounderInfo();

        // Set claimer information if item is pending or claimed
        if ("pending".equalsIgnoreCase(status) || "claimed".equalsIgnoreCase(status)) {
            populateClaimerInfo();
        }

        // Update button states based on status
        updateButtonStates(status);
    }

    private void populateFounderInfo() {
        // FIRST: Try to use actual finder information from the form
        if (selectedItem.getFinderName() != null && !selectedItem.getFinderName().isEmpty()) {
            if (founderName != null) {
                founderName.setText(selectedItem.getFinderName());
            }

            if (founderEmail != null) {
                String contact = selectedItem.getFinderContact();
                if (contact != null && !contact.isEmpty()) {
                    founderEmail.setText(contact);
                } else {
                    founderEmail.setText("No contact provided");
                }
            }

            System.out.println("Using actual finder data: " + selectedItem.getFinderName());
            return;
        }

        // FALLBACK: Get founder information from the item's uploader ID
        if (selectedItem.getUploaderIdNumber() != null) {
            User founder = Database.findUserByIdNumber(selectedItem.getUploaderIdNumber());
            if (founder != null) {
                if (founderName != null) {
                    founderName.setText("Found by: " + founder.getFullName());
                }
                if (founderEmail != null) {
                    founderEmail.setText(founder.getEmail());
                }
                System.out.println("Found item founder from user database: " + founder.getFullName());
                return;
            } else {
                System.out.println("Could not find user with ID: " + selectedItem.getUploaderIdNumber());
            }
        } else {
            System.out.println("Item has no uploader ID set");
        }

        // LAST RESORT: If no uploader info found, try to assign to a user for demo
        List<User> users = Database.loadUsers();
        if (!users.isEmpty()) {
            User fallbackUser = users.get(0); // Use first user as fallback
            if (founderName != null) {
                founderName.setText("Found by: " + fallbackUser.getFullName());
            }
            if (founderEmail != null) {
                founderEmail.setText(fallbackUser.getEmail());
            }

            // Update the item to remember this user as uploader
            selectedItem.setUploaderIdNumber(fallbackUser.getIdNumber());
            ItemDatabase.updateItem(selectedItem);

            System.out.println("Assigned fallback founder: " + fallbackUser.getFullName());
        } else {
            // Absolute fallback if no users exist
            if (founderName != null) {
                founderName.setText("Found by: Anonymous User");
            }
            if (founderEmail != null) {
                founderEmail.setText("contact@university.edu");
            }
        }
    }

    private void populateClaimerInfo() {
        if (selectedItem.getClaimerIdNumber() != null) {
            User claimer = Database.findUserByIdNumber(selectedItem.getClaimerIdNumber());
            if (claimer != null) {
                if (claimerNameLabel != null) {
                    claimerNameLabel.setText(claimer.getFullName());
                }
                if (claimerEmailLabel != null) {
                    claimerEmailLabel.setText(claimer.getEmail());
                }
                System.out.println("Found item claimer: " + claimer.getFullName());
            }
        } else {
            // For testing/demo purposes, show current user as potential claimer if no claimer set
            if (currentUserId != null) {
                User currentUser = Database.findUserByIdNumber(currentUserId);
                if (currentUser != null) {
                    if (claimerNameLabel != null) {
                        claimerNameLabel.setText(currentUser.getFullName());
                    }
                    if (claimerEmailLabel != null) {
                        claimerEmailLabel.setText(currentUser.getEmail());
                    }
                }
            }
        }

        // Set claim message
        if (claimerMessageLabel != null) {
            if (selectedItem.getClaimMessage() != null && !selectedItem.getClaimMessage().trim().isEmpty()) {
                claimerMessageLabel.setText("\"" + selectedItem.getClaimMessage() + "\"");
            } else {
                // Default message for demo
                claimerMessageLabel.setText("\"Hi! I believe this is my item. Thank you so much for finding it!\"");
            }
        }
    }

    private void updateStatusDisplay(String status) {
        if (statusLabel != null) {
            switch (status.toLowerCase()) {
                case "pending":
                    statusLabel.setText("Claim Pending Review");
                    if (statusIndicator != null) {
                        statusIndicator.setText("●");
                        statusIndicator.setStyle("-fx-text-fill: orange;");
                    }
                    break;
                case "claimed":
                    statusLabel.setText("Item Successfully Claimed");
                    if (statusIndicator != null) {
                        statusIndicator.setText("●");
                        statusIndicator.setStyle("-fx-text-fill: green;");
                    }
                    break;
                case "unclaimed":
                default:
                    statusLabel.setText("Available for Claim");
                    if (statusIndicator != null) {
                        statusIndicator.setText("●");
                        statusIndicator.setStyle("-fx-text-fill: red;");
                    }
                    break;
            }
        }
    }

    private void updateButtonStates(String status) {
        if (claimButton != null) {
            switch (status.toLowerCase()) {
                case "pending":
                    claimButton.setText("👤 Claim Request Pending");
                    claimButton.setDisable(true);
                    break;
                case "claimed":
                    claimButton.setText("✓ Already Claimed");
                    claimButton.setDisable(true);
                    break;
                case "unclaimed":
                default:
                    claimButton.setText("👤 Request Claim");
                    claimButton.setDisable(false);
                    break;
            }
        }
    }

    @FXML
    private void requestClaim() {
        if (selectedItem == null || currentUserId == null) return;

        // Show a dialog to get claim message from user
        String claimMessage = showClaimMessageDialog();
        if (claimMessage == null || claimMessage.trim().isEmpty()) {
            return; // User cancelled or entered empty message
        }

        // Update item with claimer info
        selectedItem.setStatus("Pending");
        selectedItem.setClaimerIdNumber(currentUserId);
        selectedItem.setClaimMessage(claimMessage.trim());

        // Save to database
        ItemDatabase.updateItem(selectedItem);

        // Update UI
        updateStatusDisplay("Pending");
        updateButtonStates("Pending");
        populateClaimerInfo();

        // Show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Claim Request Submitted");
        alert.setHeaderText("Your claim request has been submitted!");
        alert.setContentText("The item finder will be notified and you will receive an update soon.");
        alert.showAndWait();
    }

    private String showClaimMessageDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Claim Request");
        dialog.setHeaderText("Please provide a message to the item finder:");
        dialog.setContentText("Message:");

        // Set default message
        String defaultMessage = "Hi! I believe this item belongs to me. Thank you for finding it!";
        dialog.getEditor().setText(defaultMessage);

        // Show dialog and return result
        return dialog.showAndWait().orElse(null);
    }

    @FXML
    private void confirmClaim() {
        if (selectedItem == null) return;

        // Update item status to claimed
        selectedItem.setStatus("Claimed");

        // Save to database
        ItemDatabase.updateItem(selectedItem);

        // Update UI
        updateStatusDisplay("Claimed");
        updateButtonStates("Claimed");

        // Show success message
        if (successLabel != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            String currentDate = LocalDateTime.now().format(formatter);
            successLabel.setText("This item has been claimed and the finder has been notified. Thank you for using Animo Found!\n\nClaimed on " + currentDate);
        }

        // Disable action buttons
        if (confirmButton != null) confirmButton.setDisable(true);
        if (messageButton != null) messageButton.setDisable(true);
        if (actionRequiredLabel != null) actionRequiredLabel.setVisible(false);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Claim Confirmed");
        alert.setHeaderText("Item Successfully Claimed!");
        alert.setContentText("The claim has been confirmed. The owner has been notified.");
        alert.showAndWait();
    }

    @FXML
    private void sendMessage() {
        if (selectedItem == null) return;

        // Get founder and claimer information
        User founder = null;
        User claimer = null;
        String founderContactInfo = "Not available";

        // Get founder info - prioritize actual form data over user lookup
        if (selectedItem.getFinderName() != null && !selectedItem.getFinderName().isEmpty()) {
            // Use actual finder data from the form
            founderContactInfo = selectedItem.getFinderName();
            if (selectedItem.getFinderContact() != null && !selectedItem.getFinderContact().isEmpty()) {
                founderContactInfo += " - " + selectedItem.getFinderContact();
            }
        } else if (selectedItem.getUploaderIdNumber() != null) {
            // Fallback to user database lookup
            founder = Database.findUserByIdNumber(selectedItem.getUploaderIdNumber());
            if (founder != null) {
                founderContactInfo = founder.getFullName() + " - " + founder.getEmail();
            }
        }

        // Get claimer info
        if (selectedItem.getClaimerIdNumber() != null) {
            claimer = Database.findUserByIdNumber(selectedItem.getClaimerIdNumber());
        } else if (currentUserId != null) {
            // If no claimer set, show current user as potential claimer
            claimer = Database.findUserByIdNumber(currentUserId);
        }

        // Build detailed contact information message
        StringBuilder message = new StringBuilder();
        message.append("CONTACT INFORMATION\n");
        message.append("===================\n\n");

        // Show actual finder information from form
        message.append("📍 ITEM FINDER:\n");
        if (selectedItem.getFinderName() != null && !selectedItem.getFinderName().isEmpty()) {
            message.append("   Name: ").append(selectedItem.getFinderName()).append("\n");
            if (selectedItem.getFinderContact() != null && !selectedItem.getFinderContact().isEmpty()) {
                message.append("   Contact: ").append(selectedItem.getFinderContact()).append("\n");
            } else {
                message.append("   Contact: No contact information provided\n");
            }
            if (selectedItem.getUploaderIdNumber() != null) {
                message.append("   ID: ").append(selectedItem.getUploaderIdNumber()).append("\n");
            }
        } else if (founder != null) {
            message.append("   Name: ").append(founder.getFullName()).append("\n");
            message.append("   Email: ").append(founder.getEmail()).append("\n");
            message.append("   ID: ").append(founder.getIdNumber()).append("\n");
        } else {
            message.append("   Information not available\n");
        }
        message.append("\n");

        if (claimer != null) {
            message.append("👤 CLAIMER:\n");
            message.append("   Name: ").append(claimer.getFullName()).append("\n");
            message.append("   Email: ").append(claimer.getEmail()).append("\n");
            message.append("   ID: ").append(claimer.getIdNumber()).append("\n\n");
        } else {
            message.append("👤 CLAIMER: No one has claimed this item yet\n\n");
        }

        if (selectedItem.getClaimMessage() != null && !selectedItem.getClaimMessage().trim().isEmpty()) {
            message.append("💬 CLAIM MESSAGE:\n");
            message.append("   \"").append(selectedItem.getClaimMessage()).append("\"\n\n");
        }

        // Add item details for context
        message.append("📦 ITEM DETAILS:\n");
        message.append("   Item: ").append(selectedItem.getName()).append("\n");
        message.append("   Location Found: ").append(selectedItem.getLocation() != null ? selectedItem.getLocation() : "Not specified").append("\n");
        message.append("   Date Found: ").append(selectedItem.getDatePosted() != null ? selectedItem.getDatePosted() : "Not specified").append("\n");
        message.append("   Status: ").append(selectedItem.getStatus()).append("\n\n");

        // Add communication tips
        message.append("💡 COMMUNICATION TIPS:\n");
        message.append("   • Use university email for verification\n");
        message.append("   • Meet in a safe, public campus location\n");
        message.append("   • Bring valid ID for item pickup\n");
        message.append("   • Verify item ownership through description");

        // Show the contact information dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contact Information");
        alert.setHeaderText("User Details & Communication");
        alert.setContentText(message.toString());

        // Make dialog resizable and larger
        alert.getDialogPane().setPrefSize(500, 600);
        alert.setResizable(true);

        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        try {
            // Try to go back to gallery
            Parent root = FXMLLoader.load(getClass().getResource("/view/gallery.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            // If gallery not available, go to dashboard
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/view/dashboard.fxml"));
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException ex) {
                System.err.println("Error loading previous scene: " + ex.getMessage());
                // Just close the window as last resort
                closeWindow();
            }
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}