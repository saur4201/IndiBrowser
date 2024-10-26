import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.File;
import java.util.regex.Pattern;

// Main class extending JavaFX Application
public class Indi extends Application {
    // Lists to store history, bookmarks, and downloads
    private ObservableList<String> history = FXCollections.observableArrayList();
    private ObservableList<String> bookmarks = FXCollections.observableArrayList();
    private ObservableList<String> downloads = FXCollections.observableArrayList();
    // Pattern to identify ads
    private static final Pattern adPattern = Pattern.compile(".*ad.*|.*banner.*|.*sponsor.*|.*popup.*", Pattern.CASE_INSENSITIVE);

    @Override
    public void start(Stage primaryStage) {
        // Load logo image
        Image logoImage = new Image("file:src/logo.png");
        ImageView logoImageView = new ImageView(logoImage);
        logoImageView.setFitWidth(50);
        logoImageView.setFitHeight(50);

        // Create a TabPane to manage browser tabs
        TabPane tabPane = new TabPane();
        addNewTab(tabPane, false);

        // Buttons for new tab, incognito tab, history, bookmarks, etc.
        Button newTabButton = new Button("+");
        newTabButton.setOnAction(e -> addNewTab(tabPane, false));
        Button incognitoTabButton = new Button("Incognito");
        incognitoTabButton.setOnAction(e -> addNewTab(tabPane, true));
        Button historyButton = new Button("History");
        historyButton.setOnAction(e -> showHistory());
        Button bookmarksButton = new Button("Bookmarks");
        bookmarksButton.setOnAction(e -> showBookmarks());
        Button clearHistoryButton = new Button("Clear History");
        clearHistoryButton.setOnAction(e -> clearHistory());
        Button clearBookmarksButton = new Button("Clear Bookmarks");
        clearBookmarksButton.setOnAction(e -> clearBookmarks());
        Button downloadsButton = new Button("Downloads");
        downloadsButton.setOnAction(e -> showDownloads());

        // Toolbar to hold the logo and buttons
        HBox toolbar = new HBox(logoImageView, newTabButton, incognitoTabButton, historyButton, bookmarksButton, clearHistoryButton, clearBookmarksButton, downloadsButton);
        toolbar.setSpacing(10);

        // Main layout with toolbar on top and TabPane in the center
        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setTop(toolbar);

        // Set the scene and show the stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Indi Browser");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to add new tabs
    private void addNewTab(TabPane tabPane, boolean isIncognito) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // Load the home page
        File homePage = new File("home.html");
        webEngine.load(homePage.toURI().toString());

        // URL field and buttons for navigation
        TextField urlField = new TextField(homePage.toURI().toString());
        Button goButton = new Button("Go");
        goButton.setOnAction(e -> loadUrl(webEngine, urlField.getText()));

        // Handle Enter key press for loading URL
        urlField.setOnAction(e -> loadUrl(webEngine, urlField.getText()));
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> webEngine.executeScript("history.back()"));
        Button forwardButton = new Button("Forward");
        forwardButton.setOnAction(e -> webEngine.executeScript("history.forward()"));
        Button reloadButton = new Button("Reload");
        reloadButton.setOnAction(e -> webEngine.reload());
        Button bookmarkButton = new Button("Bookmark");
        bookmarkButton.setOnAction(e -> {
            if (!isIncognito) {
                bookmarks.add(webEngine.getLocation());
            }
        });
        Button downloadButton = new Button("Download");
        downloadButton.setOnAction(e -> downloadFile(webEngine.getLocation()));

        // Navigation bar to hold the URL field and buttons
        HBox navigationBar = new HBox(backButton, forwardButton, reloadButton, urlField, goButton, bookmarkButton, downloadButton);
        BorderPane tabContent = new BorderPane();
        tabContent.setCenter(webView);
        tabContent.setTop(navigationBar);

        // Set the tab title
        String tabTitle = isIncognito ? "Incognito Tab" : "New Tab";
        Tab newTab = new Tab(tabTitle, tabContent);
        newTab.setOnClosed(e -> {
            if (tabPane.getTabs().isEmpty()) {
                addNewTab(tabPane, false);
            }
        });

        // Add history listener if not incognito
        if (!isIncognito) {
            webEngine.getHistory().getEntries().addListener((ListChangeListener<WebHistory.Entry>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        change.getAddedSubList().forEach(entry -> history.add(entry.getUrl()));
                    }
                }
            });
        }

        // Load the adblock CSS
        webEngine.setUserStyleSheetLocation(getClass().getResource("/resources/adblock.css").toString());
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    // Method to load URL in WebView
    private void loadUrl(WebEngine webEngine, String url) {
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }
        if (!url.contains("http://")) {
            url = url.replace("http://", "https://");
        }
        webEngine.load(url);
    }

    // Method to show browsing history
    private void showHistory() {
        Alert historyAlert = new Alert(Alert.AlertType.INFORMATION);
        historyAlert.setTitle("Browsing History");
        historyAlert.setHeaderText("Visited Pages");
        historyAlert.setContentText(String.join("\n", history));
        historyAlert.showAndWait();
    }

    // Method to show bookmarks
    private void showBookmarks() {
        Alert bookmarksAlert = new Alert(Alert.AlertType.INFORMATION);
        bookmarksAlert.setTitle("Bookmarks");
        bookmarksAlert.setHeaderText("Saved Pages");
        bookmarksAlert.setContentText(String.join("\n", bookmarks));
        bookmarksAlert.showAndWait();
    }

    // Method to clear browsing history
    private void clearHistory() {
        history.clear();
        Alert clearedAlert = new Alert(Alert.AlertType.INFORMATION);
        clearedAlert.setTitle("History Cleared");
        clearedAlert.setHeaderText(null);
        clearedAlert.setContentText("Browsing history has been cleared.");
        clearedAlert.showAndWait();
    }

    // Method to clear bookmarks
    private void clearBookmarks() {
        bookmarks.clear();
        Alert clearedAlert = new Alert(Alert.AlertType.INFORMATION);
        clearedAlert.setTitle("Bookmarks Cleared");
        clearedAlert.setHeaderText(null);
        clearedAlert.setContentText("All bookmarks have been cleared.");
        clearedAlert.showAndWait();
    }

    // Method to show downloads
    private void showDownloads() {
        Alert downloadsAlert = new Alert(Alert.AlertType.INFORMATION);
        downloadsAlert.setTitle("Downloads");
        downloadsAlert.setHeaderText("Downloaded Files");
        downloadsAlert.setContentText(String.join("\n", downloads));
        downloadsAlert.showAndWait();
    }

    // Method to handle file download
    private void downloadFile(String url) {
        // Simulating a download action, add the URL to downloads list
        downloads.add(url);
        Alert downloadAlert = new Alert(Alert.AlertType.INFORMATION);
        downloadAlert.setTitle("Download Started");
        downloadAlert.setHeaderText(null);
        downloadAlert.setContentText("File is being downloaded from: " + url);
        downloadAlert.showAndWait();
    }

    // Main method to launch the application
    public static void main(String[] args) {
        launch(args);
    }
}
