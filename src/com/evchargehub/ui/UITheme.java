package com.evchargehub.ui;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * UI Theme constants, Effects, Transforms, and reusable component builders.
 * - Effects (DropShadow, glow, blur)
 * - Transforms (ScaleTransition)
 */
public class UITheme {

    public static final String COLOR_NAVY_DARK = "#0b1120";
    public static final String COLOR_NAVY_CARD = "#1e293b";
    public static final String COLOR_GREEN_PRIMARY = "#10b981";
    public static final String COLOR_GREEN_HOVER = "#059669";
    public static final String COLOR_SLATE = "#94a3b8";
    public static final String COLOR_BORDER = "#334155";

    /**
     * Creates a DropShadow effect for cards.
     */
    public static DropShadow createCardShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.35));
        shadow.setRadius(12);
        shadow.setOffsetY(4);
        return shadow;
    }

    /**
     * Creates a subtle electric green glow effect.
     */
    public static DropShadow createGreenGlow() {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(16, 185, 129, 0.6));
        glow.setRadius(15);
        glow.setSpread(0.2);
        return glow;
    }

    /**
     * Applies a subtle scale animation on hover.
     */
    public static void applyHoverScale(Node node) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), node);
        scaleUp.setToX(1.02);
        scaleUp.setToY(1.02);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), node);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        node.setOnMouseEntered(e -> scaleUp.playFromStart());
        node.setOnMouseExited(e -> scaleDown.playFromStart());
    }

    /**
     * Reusable KPI Stat Card builder.
     */
    public static VBox createStatCard(String title, String value, String subtitle, String accentColor) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");
        card.setEffect(createCardShadow());
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setPrefWidth(220);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("stat-label");

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("stat-value");
        if (accentColor != null) {
            lblValue.setStyle("-fx-text-fill: " + accentColor + ";");
        }

        Label lblSub = new Label(subtitle);
        lblSub.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        card.getChildren().addAll(lblTitle, lblValue, lblSub);
        applyHoverScale(card);
        return card;
    }

    /**
     * Creates a status badge label (AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE).
     */
    public static Label createStatusBadge(String status) {
        Label badge = new Label(status);
        badge.setPadding(new Insets(3, 10, 3, 10));
        badge.setStyle("-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        switch (status.toUpperCase()) {
            case "AVAILABLE" -> badge.setStyle(badge.getStyle() + " -fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #34d399;");
            case "RESERVED" -> badge.setStyle(badge.getStyle() + " -fx-background-color: rgba(245, 158, 11, 0.2); -fx-text-fill: #fbbf24;");
            case "OCCUPIED", "IN_PROGRESS" -> badge.setStyle(badge.getStyle() + " -fx-background-color: rgba(59, 130, 246, 0.2); -fx-text-fill: #60a5fa;");
            case "MAINTENANCE", "FAILED", "CANCELLED" -> badge.setStyle(badge.getStyle() + " -fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #f87171;");
            case "COMPLETED", "PAID", "ACTIVE" -> badge.setStyle(badge.getStyle() + " -fx-background-color: rgba(16, 185, 129, 0.25); -fx-text-fill: #10b981;");
            default -> badge.setStyle(badge.getStyle() + " -fx-background-color: #334155; -fx-text-fill: #94a3b8;");
        }

        return badge;
    }

    public static HBox createSectionHeader(String title, String subtitle) {
        VBox vbox = new VBox(2);
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label lblSub = new Label(subtitle);
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        vbox.getChildren().addAll(lblTitle, lblSub);

        HBox hbox = new HBox(vbox);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(0, 0, 10, 0));
        return hbox;
    }
}
