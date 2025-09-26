package com.shopmanager.core;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import com.shopmanager.model.User;

public class SceneManager {
    private static Stage primaryStage;
    private static User currentUser;

    public static void init(Stage stage) {
        primaryStage = stage;
        try {
            var iconStream = SceneManager.class.getResourceAsStream("/icons/app.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {
        }
    }

    public static Scene load(String fxmlPath) {
        try {
            URL fxmlUrl = SceneManager.class.getClassLoader().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IllegalArgumentException("FXML not found: " + fxmlPath);
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                applyTheme(scene);
            } else {
                scene.setRoot(root);
            }
            applyAppStyles(scene);
            addFadeIn(root);
            return scene;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load scene: " + fxmlPath + " -> " + e.getMessage(), e);
        }
    }

    public static void navigate(String fxmlPath) {
        Scene scene = load(fxmlPath);
        primaryStage.setScene(scene);
    }

    private static void addFadeIn(Parent root) {
        FadeTransition ft = new FadeTransition(Duration.millis(220), root);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    public static void applyTheme(Scene scene) {
        scene.getStylesheets().removeIf(s -> s.contains("css/theme-"));
        String theme = AppConfig.get("app.theme", "light");
        String themeCss = "/css/theme-" + ("dark".equalsIgnoreCase(theme) ? "dark" : "light") + ".css";
        scene.getStylesheets().add(SceneManager.class.getResource(themeCss).toExternalForm());
    }

    public static void applyAppStyles(Scene scene) {
        String appCss = SceneManager.class.getResource("/css/app.css").toExternalForm();
        if (!scene.getStylesheets().contains(appCss)) {
            scene.getStylesheets().add(appCss);
        }
    }

    public static void toggleTheme() {
        String current = AppConfig.get("app.theme", "light");
        String next = "dark".equalsIgnoreCase(current) ? "light" : "dark";
        AppConfig.set("app.theme", next);
        if (primaryStage != null && primaryStage.getScene() != null) {
            applyTheme(primaryStage.getScene());
        }
    }

    // Session utilisateur courante
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }
}
