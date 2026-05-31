package com.pfp.desktop.control;

import com.pfp.desktop.foundation.json.GuestCharacterArchive;
import com.pfp.desktop.foundation.json.LocalCharacterRecord;
import com.pfp.desktop.foundation.api.AuthSession;
import com.pfp.desktop.foundation.api.DesktopAuthClient;
import com.pfp.desktop.foundation.api.DesktopCharacterClient;
import com.pfp.desktop.foundation.api.DesktopSessionStore;
import com.pfp.desktop.foundation.audio.DesktopAudio;
import com.pfp.desktop.foundation.audio.SoundEffect;
import com.pfp.desktop.foundation.settings.DesktopSettings;
import com.pfp.desktop.foundation.settings.DesktopSettingsStore;
import com.pfp.desktop.presentation.DesktopViews;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class AppNavigator {

    private final Stage stage;
    private final GuestCharacterArchive archive;
    private final DesktopAuthClient authClient;
    private final DesktopCharacterClient characterClient;
    private final DesktopSessionStore sessionStore;
    private final DesktopSettingsStore settingsStore;
    private DesktopSettings settings;

    public AppNavigator(
            Stage stage,
            GuestCharacterArchive archive,
            DesktopAuthClient authClient,
            DesktopCharacterClient characterClient,
            DesktopSessionStore sessionStore,
            DesktopSettingsStore settingsStore
    ) {
        this.stage = stage;
        this.archive = archive;
        this.authClient = authClient;
        this.characterClient = characterClient;
        this.sessionStore = sessionStore;
        this.settingsStore = settingsStore;
        this.settings = loadSettings();
        DesktopAudio.applySettings(settings);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        applyWindowSettings();
    }

    public void showMainMenu() {
        setRoot(DesktopViews.mainMenu(this));
    }

    public void showSettings() {
        setRoot(DesktopViews.settings(this));
    }

    public void showCharacterAccess() {
        setRoot(DesktopViews.characterAccess(this));
    }

    public void showExtraContent() {
        showExtraContent("lore", null);
    }

    public void showExtraContent(String section, String slug) {
        setRoot(DesktopViews.extraContent(this, section, slug));
    }

    public void showLogin() {
        setRoot(DesktopViews.login(this));
    }

    public void showRegister() {
        setRoot(DesktopViews.register(this));
    }

    public void showAccount() {
        setRoot(DesktopViews.account(this));
    }

    public void showAccountArchive() {
        setRoot(DesktopViews.accountArchive(this));
    }

    public void showAccountCharacterSheet(UUID characterId) {
        setRoot(DesktopViews.accountCharacterSheet(this, characterId, false, currentCharacterSheetScroll().orElse(null)));
    }

    public void editAccountCharacterSheet(UUID characterId) {
        setRoot(DesktopViews.accountCharacterSheet(this, characterId, true, currentCharacterSheetScroll().orElse(null)));
    }

    public void showGuestArchive() {
        setRoot(DesktopViews.guestArchive(this, archive));
    }

    public void showCharacterSheet(LocalCharacterRecord record) {
        setRoot(DesktopViews.characterSheet(this, archive, record, false, currentCharacterSheetScroll().orElse(null)));
    }

    public void editCharacterSheet(LocalCharacterRecord record) {
        setRoot(DesktopViews.characterSheet(this, archive, record, true, currentCharacterSheetScroll().orElse(null)));
    }

    public void showPlaceholder(String title) {
        setRoot(DesktopViews.placeholder(this, title));
    }

    public void exit() {
        stage.close();
    }

    public Stage stage() {
        return stage;
    }

    public DesktopAuthClient authClient() {
        return authClient;
    }

    public DesktopCharacterClient characterClient() {
        return characterClient;
    }

    public Optional<AuthSession> currentSession() {
        try {
            return sessionStore.load();
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    public void saveSession(AuthSession session) throws IOException {
        sessionStore.save(session);
    }

    public void clearSession() throws IOException {
        sessionStore.clear();
    }

    public void replaceRoot(Parent root) {
        setRoot(root);
    }

    public Optional<Double> currentSheetScroll() {
        return currentCharacterSheetScroll();
    }

    public DesktopSettings settings() {
        return settings;
    }

    public void saveSettings(DesktopSettings nextSettings) throws IOException {
        settingsStore.save(nextSettings);
        this.settings = nextSettings;
        DesktopAudio.applySettings(settings);
        applyWindowSettings();
        updateResponsiveClasses(stage.getScene());
    }

    private void setRoot(Parent root) {
        Scene scene = stage.getScene();
        if (scene == null) {
            StackPane shell = new StackPane(root);
            shell.getStyleClass().add("app-root-shell");
            scene = new Scene(shell, 1100, 720);
            String css = AppNavigator.class.getResource("/com/pfp/desktop/presentation/desktop.css").toExternalForm();
            scene.getStylesheets().add(css);
            scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcut);
            stage.setScene(scene);
            installResponsiveClasses(scene);
        } else {
            if (scene.getRoot() instanceof StackPane shell && shell.getStyleClass().contains("app-root-shell")) {
                if (shell.getChildren().isEmpty()) {
                    shell.getChildren().add(root);
                } else {
                    shell.getChildren().set(0, root);
                }
            } else {
                StackPane shell = new StackPane(root);
                shell.getStyleClass().add("app-root-shell");
                scene.setRoot(shell);
            }
            installResponsiveClasses(scene);
        }
    }

    private void installResponsiveClasses(Scene scene) {
        if (!Boolean.TRUE.equals(scene.getProperties().get("pfp-responsive-listener"))) {
            scene.widthProperty().addListener((observable, oldValue, newValue) -> updateResponsiveClasses(scene));
            scene.getProperties().put("pfp-responsive-listener", true);
        }
        updateResponsiveClasses(scene);
    }

    private void updateResponsiveClasses(Scene scene) {
        if (scene == null) {
            return;
        }
        if (scene.getRoot() == null) {
            return;
        }
        scene.getRoot().getStyleClass().removeAll("tiny-layout", "compact-layout", "side-layout", "wide-layout");
        double width = settings.getWindowWidth();
        if (width > 0 && width < 1080) {
            scene.getRoot().getStyleClass().add("tiny-layout");
        } else if (width > 0 && width < 1180) {
            scene.getRoot().getStyleClass().add("compact-layout");
        } else if (width >= 1366) {
            scene.getRoot().getStyleClass().add("side-layout");
        }
        if (width >= 1440) {
            scene.getRoot().getStyleClass().add("wide-layout");
        }
    }

    private void handleShortcut(KeyEvent event) {
        if (shouldIgnoreShortcut(event)) {
            return;
        }
        String action = settings.actionFor(keyDisplay(event)).orElse("");
        if (action.isBlank()) {
            return;
        }
        boolean handled = switch (action) {
            case "fullscreen" -> {
                toggleFullscreen();
                yield true;
            }
            case "settings" -> {
                showSettings();
                yield true;
            }
            case "exit" -> {
                exit();
                yield true;
            }
            case "lore" -> {
                DesktopAudio.play(SoundEffect.PAGE_TURN);
                showExtraContent("lore", null);
                yield true;
            }
            case "rule-book" -> {
                DesktopAudio.play(SoundEffect.PAGE_TURN);
                showExtraContent("rules", null);
                yield true;
            }
            case "new-character" -> fireButton("New Character");
            case "import-json" -> fireButton("Import JSON");
            case "edit-sheet" -> fireButton("Edit Sheet") || fireButton("Save Changes");
            case "export-open-character" -> fireButton("Export JSON");
            case "archive" -> fireButton("Back to Archive") || fireButton("Account Archive") || fireButton("Guest Archive");
            case "add-item" -> fireButton("Add item");
            case "equip-active-item" -> fireButton("Equip");
            case "sell-active-item" -> fireButton("Sell");
            case "add-spell" -> fireButton("Add spell");
            case "close-dialog" -> fireButton("Close");
            case "sheet-character-info" -> scrollToText("CHARACTER INFO");
            case "sheet-condition" -> scrollToText("CONDITION");
            case "sheet-inventory" -> scrollToText("INVENTORY");
            case "sheet-additional-info" -> scrollToText("ADDITIONAL INFO");
            default -> false;
        };
        if (handled) {
            event.consume();
        }
    }

    private boolean shouldIgnoreShortcut(KeyEvent event) {
        Node focusOwner = stage.getScene() == null ? null : stage.getScene().getFocusOwner();
        boolean plainTextInput = focusOwner instanceof TextInputControl
                && !event.isControlDown()
                && !event.isAltDown()
                && !event.isMetaDown();
        boolean textEditingShortcut = focusOwner instanceof TextInputControl
                && (event.isControlDown() || event.isMetaDown())
                && ("A".equals(event.getCode().getName()) || "C".equals(event.getCode().getName())
                || "V".equals(event.getCode().getName()) || "X".equals(event.getCode().getName())
                || "Z".equals(event.getCode().getName()));
        return plainTextInput || textEditingShortcut;
    }

    private void toggleFullscreen() {
        stage.setFullScreen(!stage.isFullScreen());
        settings.setFullscreen(stage.isFullScreen());
        updateResponsiveClasses(stage.getScene());
        try {
            settingsStore.save(settings);
        } catch (IOException ignored) {
            // Fullscreen toggling should remain responsive even if the settings file is temporarily unavailable.
        }
    }

    private boolean fireButton(String text) {
        Scene scene = stage.getScene();
        if (scene == null || scene.getRoot() == null) {
            return false;
        }
        return findButton(scene.getRoot(), text).map(button -> {
            button.fire();
            return true;
        }).orElse(false);
    }

    private boolean scrollToText(String text) {
        Scene scene = stage.getScene();
        if (scene == null || scene.getRoot() == null) {
            return false;
        }
        Optional<ScrollPane> scrollPane = findScrollPane(scene.getRoot());
        Optional<Node> target = findNodeWithText(scene.getRoot(), text);
        if (scrollPane.isEmpty() || target.isEmpty()) {
            return false;
        }
        Node content = scrollPane.get().getContent();
        double targetY = target.get().localToScene(target.get().getBoundsInLocal()).getMinY();
        double contentY = content.localToScene(content.getBoundsInLocal()).getMinY();
        double scrollableHeight = content.getBoundsInLocal().getHeight() - scrollPane.get().getViewportBounds().getHeight();
        if (scrollableHeight <= 0) {
            return false;
        }
        double next = Math.max(0, Math.min(1, (targetY - contentY - 24) / scrollableHeight));
        scrollPane.get().setVvalue(next);
        return true;
    }

    private DesktopSettings loadSettings() {
        try {
            return settingsStore.load();
        } catch (IOException exception) {
            return DesktopSettings.defaults();
        }
    }

    private void applyWindowSettings() {
        boolean wasFullscreen = stage.isFullScreen();
        if (settings.isFullscreen()) {
            if (!wasFullscreen) {
                stage.setFullScreen(true);
            }
            return;
        }

        if (wasFullscreen) {
            stage.setFullScreen(false);
        }
        stage.setWidth(Math.max(1024, settings.getWindowWidth()));
        stage.setHeight(Math.max(640, settings.getWindowHeight()));
        keepWindowVisible();
    }

    private void keepWindowVisible() {
        Screen screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
                .stream()
                .findFirst()
                .orElse(Screen.getPrimary());
        var bounds = screen.getVisualBounds();
        if (stage.getY() < bounds.getMinY() || stage.getX() < bounds.getMinX()
                || stage.getX() + 80 > bounds.getMaxX() || stage.getY() + 80 > bounds.getMaxY()) {
            stage.centerOnScreen();
        }
    }

    private static String keyDisplay(KeyEvent event) {
        String code = keyName(event);
        if (code.isBlank()) {
            return "";
        }
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (event.isControlDown()) {
            parts.add("Ctrl");
        }
        if (event.isShiftDown()) {
            parts.add("Shift");
        }
        if (event.isAltDown() && !"Alt".equals(code)) {
            parts.add("Alt");
        }
        if (event.isMetaDown()) {
            parts.add("Meta");
        }
        parts.add(code);
        return String.join("+", parts);
    }

    private static String keyName(KeyEvent event) {
        return switch (event.getCode()) {
            case COMMA -> ",";
            case DIGIT1 -> "1";
            case DIGIT2 -> "2";
            case DIGIT3 -> "3";
            case DIGIT4 -> "4";
            case ESCAPE -> "Esc";
            case ALT -> "Alt";
            default -> event.getCode().getName();
        };
    }

    private static Optional<Button> findButton(Node node, String text) {
        if (node instanceof Button button && text.equals(button.getText()) && !button.isDisabled()) {
            return Optional.of(button);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Optional<Button> found = findButton(child, text);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<Node> findNodeWithText(Node node, String text) {
        if (node instanceof javafx.scene.control.Labeled labeled && text.equalsIgnoreCase(labeled.getText())) {
            return Optional.of(node);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Optional<Node> found = findNodeWithText(child, text);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Double> currentCharacterSheetScroll() {
        Scene scene = stage.getScene();
        if (scene == null || scene.getRoot() == null || !containsStyleClass(scene.getRoot(), "sheet-action-bar")) {
            return Optional.empty();
        }
        return findScrollPane(scene.getRoot()).map(ScrollPane::getVvalue);
    }

    private static Optional<ScrollPane> findScrollPane(Node node) {
        if (node instanceof ScrollPane scrollPane) {
            return Optional.of(scrollPane);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Optional<ScrollPane> found = findScrollPane(child);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    private static boolean containsStyleClass(Node node, String styleClass) {
        if (node.getStyleClass().contains(styleClass)) {
            return true;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (containsStyleClass(child, styleClass)) {
                    return true;
                }
            }
        }
        return false;
    }
}
