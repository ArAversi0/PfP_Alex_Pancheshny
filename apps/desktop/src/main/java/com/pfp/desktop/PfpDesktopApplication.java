package com.pfp.desktop;

import com.pfp.desktop.control.AppNavigator;
import com.pfp.desktop.foundation.api.DesktopAuthClient;
import com.pfp.desktop.foundation.api.DesktopCharacterClient;
import com.pfp.desktop.foundation.api.DesktopSessionStore;
import com.pfp.desktop.foundation.json.GuestCharacterArchive;
import com.pfp.desktop.foundation.settings.DesktopSettingsStore;
import java.io.IOException;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class PfpDesktopApplication extends Application {

    @Override
    public void start(Stage stage) {
        Path storageRoot = defaultStorageRoot();
        GuestCharacterArchive archive = new GuestCharacterArchive(storageRoot);
        try {
            archive.initialize();
        } catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("PfP Companion");
            alert.setHeaderText("Local storage is unavailable");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
            return;
        }

        stage.setTitle("PfP Companion System");
        stage.setMinWidth(1024);
        stage.setMinHeight(640);
        DesktopAuthClient authClient = new DesktopAuthClient();
        new AppNavigator(stage, archive, authClient, new DesktopCharacterClient(authClient.baseUri()),
                new DesktopSessionStore(storageRoot), new DesktopSettingsStore(storageRoot)).showMainMenu();
        stage.show();
    }

    private static Path defaultStorageRoot() {
        return Path.of(System.getProperty("user.home"), ".pfp-companion", "desktop");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
