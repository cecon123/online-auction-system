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
        row.setPadding(new Insets(12, 16, 12, 16));

        double[] widths = {16, 24, 12, 14, 18, 16};
        for (double w : widths) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(w);
            row.getColumnConstraints().add(cc);
        }

        row.add(createCell(user.fullName(), "admin-table-cell"), 0, 0);
        row.add(createCell(user.username(), "admin-table-cell"), 1, 0);
        row.add(createCell(user.role().toString(), "admin-table-cell"), 2, 0);
        
        String statusText = user.active() ? "Active" : "Suspended";
        Label statusLabel = createCell(statusText, user.active() ? "admin-status-active" : "admin-status-suspended");
        row.add(statusLabel, 3, 0);
        
        String joinedDate = user.createdAt() != null ? user.createdAt().format(DATE_FORMATTER) : "N/A";
        row.add(createCell(joinedDate, "admin-table-cell"), 4, 0);

        HBox actions = new HBox(16);
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("admin-link-button");
        viewBtn.setOnAction(e -> handleViewUser(user));

        Button actionBtn = new Button(user.active() ? "Disable" : "Enable");
        actionBtn.getStyleClass().add(user.active() ? "admin-danger-link-button" : "admin-link-button");
        actionBtn.setOnAction(e -> handleToggleUserStatus(user));

        actions.getChildren().addAll(viewBtn, actionBtn);
        row.add(actions, 5, 0);

        return row;
    }

    private GridPane createAuctionRow(AuctionDetailDto auction, boolean alt) {
        GridPane row = new GridPane();
        row.getStyleClass().add(alt ? "admin-table-row-alt" : "admin-table-row");
        row.setHgap(14);
        row.setPadding(new Insets(10, 16, 10, 16));

        double[] widths = {20, 12, 12, 14, 14, 10, 10, 8};
        for (double w : widths) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(w);
            row.getColumnConstraints().add(cc);
        }

        HBox itemBox = new HBox(10);
        itemBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox thumb = new VBox();
        thumb.getStyleClass().add("admin-item-thumb");
        itemBox.getChildren().addAll(thumb, createCell(auction.title(), "admin-table-cell"));
        row.add(itemBox, 0, 0);

        row.add(createCell(auction.itemType().toString(), "admin-table-cell"), 1, 0);
        row.add(createCell("Seller #" + auction.sellerId(), "admin-table-cell"), 2, 0);
        row.add(createCell(formatMoney(auction.startingPrice()), "admin-table-cell"), 3, 0);
        row.add(createCell(formatMoney(auction.currentPrice()), "admin-money-cell"), 4, 0);
        
        String status = auction.status().toString();
        Label statusLbl = createCell(status, getStatusStyleClass(status));
        row.add(statusLbl, 5, 0);
        
        row.add(createCell("N/A", "admin-table-cell"), 6, 0); // End time placeholder

        HBox actions = new HBox(12);
        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("admin-link-button");
        viewBtn.setOnAction(e -> handleViewAuction(auction));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("admin-danger-link-button");
        cancelBtn.setDisable(!status.equals("OPEN") && !status.equals("RUNNING"));
        cancelBtn.setOnAction(e -> handleCancelAuction(auction));

        actions.getChildren().addAll(viewBtn, cancelBtn);
        row.add(actions, 7, 0);

        return row;
    }

    private Label createCell(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private String getStatusStyleClass(String status) {
        return switch (status) {
            case "RUNNING" -> "admin-status-running";
            case "FINISHED" -> "admin-status-finished";
            case "OPEN" -> "admin-status-open";
            default -> "admin-table-cell";
        };
    }

    private String formatMoney(BigDecimal amount) {
        return amount != null ? USD_FORMAT.format(amount) : "N/A";
    }

    @FXML
    private void handleExportReport() {
        messageLabel.setText("Report export functionality is not implemented yet.");
    }

    private void handleViewUser(UserDto user) {
        messageLabel.setText("Viewing detail for: " + user.fullName());
    }

    private void handleToggleUserStatus(UserDto user) {
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

    private void handleViewAuction(AuctionDetailDto auction) {
        messageLabel.setText("Viewing detail for auction: " + auction.title());
    }

    private void handleCancelAuction(AuctionDetailDto auction) {
        messageLabel.setText("Auction cancellation functionality is not implemented yet.");
    }
}
