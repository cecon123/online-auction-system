package com.auction.client.controller;

import com.auction.client.service.AdminClientService;
import com.auction.client.service.AuctionClientService;
import com.auction.common.dto.auction.AuctionDetailDto;
import com.auction.common.dto.auth.UserDto;
import com.auction.common.dto.dashboard.DashboardDto;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;

public class AdminPanelController {

    private static final Logger logger = LoggerFactory.getLogger(AdminPanelController.class);
    private static final NumberFormat USD_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label totalAuctionsLabel;
    @FXML
    private Label runningAuctionsLabel;
    @FXML
    private Label finishedAuctionsLabel;

    @FXML
    private VBox userTableContainer;
    @FXML
    private VBox auctionTableContainer;

    @FXML
    private Label messageLabel;

    private final AdminClientService adminService = new AdminClientService();
    private final AuctionClientService auctionService = new AuctionClientService();

    @FXML
    private void initialize() {
        messageLabel.setText("");
        loadData();
    }

    @FXML
    private void handleRefresh() {
        loadData();
        messageLabel.setText("Data refreshed successfully.");
    }

    private void loadData() {
        loadDashboardStats();
        loadUsers();
        loadAuctions();
    }

    private void loadDashboardStats() {
        auctionService.getDashboard().thenAccept(response -> {
            if (response.isSuccess()) {
                DashboardDto stats = response.getData();
                Platform.runLater(() -> {
                    totalUsersLabel.setText(String.format("%,d", stats.totalUsersCount()));
                    totalAuctionsLabel.setText(String.format("%,d", stats.totalAuctionsCount()));
                    runningAuctionsLabel.setText(String.format("%,d", stats.activeAuctionsCount()));
                    finishedAuctionsLabel.setText(String.format("%,d", stats.totalAuctionsCount() - stats.activeAuctionsCount()));
                });
            }
        });
    }

    private void loadUsers() {
        adminService.getUsers().thenAccept(response -> {
            if (response.isSuccess()) {
                List<UserDto> users = response.getData();
                Platform.runLater(() -> {
                    // Keep only the header
                    if (userTableContainer.getChildren().size() > 1) {
                        userTableContainer.getChildren().remove(1, userTableContainer.getChildren().size());
                    }
                    
                    for (int i = 0; i < users.size(); i++) {
                        userTableContainer.getChildren().add(createUserRow(users.get(i), i % 2 != 0));
                    }
                });
            } else {
                Platform.runLater(() -> messageLabel.setText("Failed to load users: " + response.getMessage()));
            }
        });
    }

    private void loadAuctions() {
        adminService.getAuctions().thenAccept(response -> {
            if (response.isSuccess()) {
                List<AuctionDetailDto> auctions = response.getData();
                Platform.runLater(() -> {
                    // Keep only the header
                    if (auctionTableContainer.getChildren().size() > 1) {
                        auctionTableContainer.getChildren().remove(1, auctionTableContainer.getChildren().size());
                    }

                    for (int i = 0; i < auctions.size(); i++) {
                        auctionTableContainer.getChildren().add(createAuctionRow(auctions.get(i), i % 2 != 0));
                    }
                });
            } else {
                Platform.runLater(() -> messageLabel.setText("Failed to load auctions: " + response.getMessage()));
            }
        });
    }

    private GridPane createUserRow(UserDto user, boolean alt) {
        GridPane row = new GridPane();
        row.getStyleClass().add(alt ? "admin-table-row-alt" : "admin-table-row");
        row.setHgap(16);
        row.setPadding(new Insets(8, 16, 8, 16));

        double[] widths = {18, 20, 10, 12, 18, 22};
        for (double w : widths) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(w);
            row.getColumnConstraints().add(cc);
        }

        row.add(createCell(user.fullName(), "admin-table-cell"), 0, 0);
        row.add(createCell(user.username(), "admin-table-cell"), 1, 0);
        row.add(createCell(user.role().toString(), "admin-table-cell"), 2, 0);
        
        String statusText = user.active() ? "Active" : "Suspended";
        Label statusLabel = createStatusBadge(statusText, user.active() ? "status-active" : "status-suspended");
        row.add(statusLabel, 3, 0);
        
        String joinedDate = user.createdAt() != null ? user.createdAt().format(DATE_FORMATTER) : "N/A";
        row.add(createCell(joinedDate, "admin-table-cell"), 4, 0);

        HBox actions = new HBox(16);
        Button actionBtn = new Button(user.active() ? "Disable" : "Enable");
        actionBtn.getStyleClass().addAll("admin-pill-btn", user.active() ? "admin-btn-danger" : "admin-btn-success");
        
        // Admins cannot be deactivated
        if (user.role() == com.auction.common.enums.Role.ADMIN && user.active()) {
            actionBtn.setDisable(true);
            actionBtn.getStyleClass().add("admin-btn-disabled");
        }
        
        actionBtn.setOnAction(e -> handleToggleUserStatus(user));

        actions.getChildren().add(actionBtn);
        row.add(actions, 5, 0);

        return row;
    }

    private GridPane createAuctionRow(AuctionDetailDto auction, boolean alt) {
        GridPane row = new GridPane();
        row.getStyleClass().add(alt ? "admin-table-row-alt" : "admin-table-row");
        row.setHgap(14);
        row.setPadding(new Insets(8, 16, 8, 16));

        double[] widths = {18, 10, 10, 12, 12, 10, 14, 14};
        for (double w : widths) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(w);
            row.getColumnConstraints().add(cc);
        }

        HBox itemBox = new HBox(10);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox thumb = new VBox();
        thumb.getStyleClass().add("admin-item-thumb");
        thumb.setAlignment(Pos.CENTER);
        
        if (auction.imagePath() != null && !auction.imagePath().isEmpty()) {
            try {
                String fullUrl = com.auction.client.util.ImageUrlUtil.getImageUrl(auction.imagePath());
                Image img = new Image(fullUrl, 32, 32, true, true, true);
                if (!img.isError()) {
                    ImageView iv = new ImageView(img);
                    thumb.getChildren().add(iv);
                } else {
                    thumb.getChildren().add(new org.kordamp.ikonli.javafx.FontIcon("mdi2p-package-variant"));
                }
            } catch (Exception e) {
                thumb.getChildren().add(new org.kordamp.ikonli.javafx.FontIcon("mdi2p-package-variant"));
            }
        } else {
            thumb.getChildren().add(new org.kordamp.ikonli.javafx.FontIcon("mdi2p-package-variant"));
        }
        
        itemBox.getChildren().addAll(thumb, createCell(auction.title(), "admin-table-cell"));
        row.add(itemBox, 0, 0);

        row.add(createCell(auction.itemType().toString(), "admin-table-cell"), 1, 0);
        row.add(createCell("Seller #" + auction.sellerId(), "admin-table-cell"), 2, 0);
        row.add(createCell(formatMoney(auction.startingPrice()), "admin-table-cell"), 3, 0);
        row.add(createCell(formatMoney(auction.currentPrice()), "admin-money-cell"), 4, 0);
        
        String status = auction.status().toString();
        Label statusLbl = createStatusBadge(status, getStatusStyleClass(status));
        row.add(statusLbl, 5, 0);
        
        String endTimeFormatted = auction.endTime() != null ? auction.endTime().format(DATE_FORMATTER) : "N/A";
        row.add(createCell(endTimeFormatted, "admin-table-cell"), 6, 0);

        HBox actions = new HBox(12);
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("admin-pill-btn", "admin-btn-danger");
        if (!status.equals("OPEN") && !status.equals("RUNNING")) {
            cancelBtn.getStyleClass().add("admin-btn-disabled");
            cancelBtn.setDisable(true);
        }
        cancelBtn.setOnAction(e -> handleCancelAuction(auction));

        actions.getChildren().add(cancelBtn);
        row.add(actions, 7, 0);

        return row;
    }

    private Label createCell(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private Label createStatusBadge(String text, String statusClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("status-badge", statusClass);
        return label;
    }

    private String getStatusStyleClass(String status) {
        return switch (status) {
            case "RUNNING" -> "status-running";
            case "FINISHED" -> "status-finished";
            case "OPEN" -> "status-open";
            case "CANCELLED" -> "status-cancelled";
            default -> "status-ended";
        };
    }

    private String formatMoney(BigDecimal amount) {
        return amount != null ? USD_FORMAT.format(amount) : "N/A";
    }

    private void handleToggleUserStatus(UserDto user) {
        if (user.role() == com.auction.common.enums.Role.ADMIN && user.active()) {
            messageLabel.setText("Administrative accounts cannot be deactivated.");
            return;
        }
        boolean newStatus = !user.active();
        adminService.updateUserStatus(user.id(), newStatus).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.isSuccess()) {
                    messageLabel.setText("User " + user.username() + " status updated.");
                    loadUsers(); // Refresh
                } else {
                    messageLabel.setText("Failed to update user: " + response.getMessage());
                }
            });
        });
    }

    private void handleCancelAuction(AuctionDetailDto auction) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancellation");
        alert.setHeaderText("Cancel Auction");
        alert.setContentText("Are you sure you want to cancel the auction '" + auction.title() + "'? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                adminService.cancelAuction(auction.auctionId()).thenAccept(res -> {
                    Platform.runLater(() -> {
                        if (res.isSuccess()) {
                            messageLabel.setText("Auction canceled successfully.");
                            loadAuctions();
                        } else {
                            messageLabel.setText("Failed to cancel auction: " + res.getMessage());
                        }
                    });
                });
            }
        });
    }
}
