package com.pfp.desktop.presentation;

import com.pfp.desktop.control.AppNavigator;
import com.pfp.desktop.foundation.api.AccountCharacterCard;
import com.pfp.desktop.foundation.api.AccountCharacterSheet;
import com.pfp.desktop.foundation.api.AuthException;
import com.pfp.desktop.foundation.api.AuthSession;
import com.pfp.desktop.foundation.api.AuthUser;
import com.pfp.desktop.foundation.audio.AudioTheme;
import com.pfp.desktop.foundation.audio.DesktopAudio;
import com.pfp.desktop.foundation.audio.SoundEffect;
import com.pfp.desktop.foundation.content.BundledExtraContentSource;
import com.pfp.desktop.foundation.content.ContentNode;
import com.pfp.desktop.foundation.content.ContentSection;
import com.pfp.desktop.foundation.content.ExtraContentSource;
import com.pfp.desktop.foundation.json.GuestCharacterArchive;
import com.pfp.desktop.foundation.json.LocalCharacterSheet;
import com.pfp.desktop.foundation.json.LocalCharacterRecord;
import com.pfp.desktop.foundation.settings.DesktopSettings;
import com.pfp.desktop.foundation.settings.HotkeyBinding;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Window;

public final class DesktopViews {
    private static final List<String> CURRENCY_CODES = List.of("CURRENCY_1", "CURRENCY_2", "CURRENCY_3", "CURRENCY_4");
    private static final int[] DIE_SIDES = {4, 6, 8, 10, 12};
    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();
    private static final ExtraContentSource EXTRA_CONTENT = new BundledExtraContentSource();

    private DesktopViews() {
    }

    public static Parent mainMenu(AppNavigator navigator) {
        BorderPane shell = new BorderPane();
        shell.getStyleClass().add("home-shell");

        VBox menu = new VBox(20,
                brandBlock(),
                menuButton("Characters", navigator::showCharacterAccess),
                menuButton("Extra Content", navigator::showExtraContent),
                menuButton("Settings", navigator::showSettings),
                menuDivider(),
                menuButton("Exit", navigator::exit)
        );
        menu.getStyleClass().add("home-menu");

        VBox ambient = new VBox(18,
                eyebrow("LOCAL DESKTOP CLIENT"),
                h1("PfP Companion"),
                body("Guest archive, account sync, JSON transfer, and a full desktop character sheet."),
                ambientFrame()
        );
        ambient.getStyleClass().add("home-ambient");

        shell.setLeft(menu);
        shell.setCenter(ambient);
        return page(null, shell);
    }

    public static Parent settings(AppNavigator navigator) {
        DesktopSettings settings = copySettings(navigator.settings());
        ComboBox<String> resolution = new ComboBox<>();
        resolution.getItems().addAll(availableResolutions());
        String currentResolution = settings.getWindowWidth() + " x " + settings.getWindowHeight();
        if (!resolution.getItems().contains(currentResolution)) {
            resolution.getItems().add(currentResolution);
        }
        resolution.setValue(currentResolution);
        resolution.getStyleClass().add("settings-control");

        CheckBox fullscreen = new CheckBox("Fullscreen");
        fullscreen.setSelected(settings.isFullscreen());
        fullscreen.getStyleClass().add("settings-checkbox");

        ComboBox<String> musicTheme = new ComboBox<>();
        musicTheme.getItems().addAll(AudioTheme.displayNames());
        musicTheme.setValue(settings.getMusicTheme() == null || settings.getMusicTheme().isBlank()
                ? "None"
                : AudioTheme.fromDisplayName(settings.getMusicTheme()).displayName());
        musicTheme.getStyleClass().add("settings-control");

        CheckBox musicEnabled = new CheckBox("Music enabled");
        musicEnabled.setSelected(settings.isMusicEnabled());
        musicEnabled.getStyleClass().add("settings-checkbox");

        CheckBox soundEnabled = new CheckBox("Sounds enabled");
        soundEnabled.setSelected(settings.isSoundEnabled());
        soundEnabled.getStyleClass().add("settings-checkbox");

        Slider musicVolume = percentSlider(settings.getMusicVolume());
        Slider soundVolume = percentSlider(settings.getSoundVolume());

        VBox hotkeyRows = new VBox(8);
        hotkeyRows.getStyleClass().add("settings-hotkey-list");
        List<HotkeyBinding> hotkeys = new ArrayList<>(settings.getHotkeys());
        rebuildHotkeyRows(hotkeyRows, hotkeys);

        Label message = formMessage("");
        Button resetHotkeys = secondaryButton("Reset hotkeys", () -> {
            hotkeys.clear();
            hotkeys.addAll(DesktopSettings.defaultHotkeys());
            rebuildHotkeyRows(hotkeyRows, hotkeys);
            showMessage(message, "Default hotkeys restored. Save settings to keep them.", true);
        });
        Button save = primaryButton("Save Settings", () -> {
            int[] dimensions = parseResolution(resolution.getValue());
            settings.setWindowWidth(dimensions[0]);
            settings.setWindowHeight(dimensions[1]);
            settings.setFullscreen(fullscreen.isSelected());
            settings.setMusicTheme(musicTheme.getValue());
            settings.setMusicEnabled(musicEnabled.isSelected());
            settings.setSoundEnabled(soundEnabled.isSelected());
            settings.setMusicVolume((int) Math.round(musicVolume.getValue()));
            settings.setSoundVolume((int) Math.round(soundVolume.getValue()));
            settings.setHotkeys(hotkeys);
            try {
                navigator.saveSettings(settings);
                showMessage(message, "Settings saved.", true);
            } catch (IOException exception) {
                showMessage(message, "Could not save settings: " + exception.getMessage(), false);
            }
        });

        HBox actions = new HBox(14, save, resetHotkeys, secondaryButton("Main Menu", navigator::showMainMenu));
        actions.setAlignment(Pos.CENTER_RIGHT);
        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);
        HBox hero = new HBox(34, titleBlock("SETTINGS", "Settings"), heroSpacer, actions);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.getStyleClass().add("settings-hero");

        VBox generalSection = new VBox(14,
                settingRow("Window resolution", resolution),
                settingRow("Display mode", fullscreen)
        );
        VBox controlsSection = new VBox(14,
                body("Click a shortcut field and press the new key combination."),
                hotkeyRows
        );
        VBox audioSection = new VBox(14,
                settingRow("Application theme", musicTheme),
                settingRow("Music", musicEnabled),
                settingRow("Music volume", labeledSlider(musicVolume)),
                settingRow("Sounds", soundEnabled),
                settingRow("Sound volume", labeledSlider(soundVolume))
        );
        Optional<AuthSession> session = navigator.currentSession();
        Label modeValue = body(session.isPresent() ? "Account mode" : "Guest mode");
        Label emailValue = body(session.map(value -> value.user().email()).orElse("Not signed in"));
        Label sessionValue = body(session.isPresent() ? "Stored session" : "No account session");
        Label connectionMessage = formMessage("");
        Button checkConnection = secondaryButton("Check connection", () -> {
        });
        checkConnection.setOnAction(event -> {
            clearMessage(connectionMessage);
            setAuthBusy(checkConnection, true, "Checking...");
            Optional<AuthSession> currentSession = navigator.currentSession();
            if (currentSession.isPresent()) {
                AuthSession activeSession = currentSession.get();
                runAuthTask(() -> navigator.authClient().currentUser(activeSession), currentUser -> {
                    try {
                        navigator.saveSession(new AuthSession(
                                activeSession.accessToken(),
                                activeSession.refreshToken(),
                                activeSession.tokenType(),
                                currentUser
                        ));
                        modeValue.setText("Account mode");
                        emailValue.setText(currentUser.email());
                        sessionValue.setText(currentUser.emailVerified() ? "Session verified" : "Email verification required");
                        showMessage(connectionMessage, "Backend is reachable. Account session is valid.", true);
                    } catch (IOException exception) {
                        showMessage(connectionMessage, "Connected, but local session could not be updated: "
                                + exception.getMessage(), false);
                    }
                    setAuthBusy(checkConnection, false, "Check connection");
                }, exception -> {
                    sessionValue.setText("Session check failed");
                    showMessage(connectionMessage, exception.getMessage(), false);
                    setAuthBusy(checkConnection, false, "Check connection");
                });
                return;
            }
            runAuthTask(() -> {
                navigator.authClient().checkServerAvailable();
                return true;
            }, result -> {
                modeValue.setText("Guest mode");
                emailValue.setText("Not signed in");
                sessionValue.setText("Backend reachable");
                showMessage(connectionMessage, "Backend is reachable. Sign in to sync account characters.", true);
                setAuthBusy(checkConnection, false, "Check connection");
            }, exception -> {
                sessionValue.setText("Backend unavailable");
                showMessage(connectionMessage, exception.getMessage(), false);
                setAuthBusy(checkConnection, false, "Check connection");
            });
        });
        VBox connectionSection = new VBox(14,
                settingRow("Current mode", modeValue),
                settingRow("Account email", emailValue),
                settingRow("Session status", sessionValue),
                new HBox(checkConnection),
                connectionMessage
        );

        VBox sectionBody = new VBox(settingsCard("General", generalSection));
        sectionBody.getStyleClass().add("settings-section-body");
        Button general = settingsNavButton("General");
        Button controls = settingsNavButton("Controls");
        Button audio = settingsNavButton("Music and Sounds");
        Button connection = settingsNavButton("Connection");
        List<Button> navButtons = List.of(general, controls, audio, connection);
        general.setOnAction(event -> {
            DesktopAudio.play(SoundEffect.BUTTON_CLICK);
            selectSettingsSection(navButtons, general, sectionBody, "General", generalSection);
        });
        controls.setOnAction(event -> {
            DesktopAudio.play(SoundEffect.BUTTON_CLICK);
            selectSettingsSection(navButtons, controls, sectionBody, "Controls", controlsSection);
        });
        audio.setOnAction(event -> {
            DesktopAudio.play(SoundEffect.BUTTON_CLICK);
            selectSettingsSection(navButtons, audio, sectionBody, "Music and Sounds", audioSection);
        });
        connection.setOnAction(event -> {
            DesktopAudio.play(SoundEffect.BUTTON_CLICK);
            selectSettingsSection(navButtons, connection, sectionBody, "Connection", connectionSection);
        });
        selectSettingsSection(navButtons, general, sectionBody, "General", generalSection);

        VBox sidebar = new VBox(12, general, controls, audio, connection);
        sidebar.getStyleClass().add("settings-sidebar");
        HBox settingsShell = new HBox(24, sidebar, sectionBody);
        settingsShell.getStyleClass().add("settings-shell");
        HBox.setHgrow(sectionBody, Priority.ALWAYS);

        VBox content = new VBox(28,
                hero,
                message,
                settingsShell
        );
        content.getStyleClass().add("content");
        return page(header(navigator, true), scroll(content));
    }

    public static Parent characterAccess(AppNavigator navigator) {
        Optional<AuthSession> session = navigator.currentSession();
        VBox panel = new VBox(18,
                titleBlock("CHARACTER ACCESS", "Choose profile"),
                primaryButton("Continue as Guest", navigator::showGuestArchive),
                secondaryButton(session.isPresent() ? "Account" : "Login", session.isPresent() ? navigator::showAccount : navigator::showLogin),
                secondaryButton("Register", navigator::showRegister),
                secondaryButton("Main Menu", navigator::showMainMenu)
        );
        panel.getStyleClass().add("access-panel");

        VBox content = new VBox(panel);
        content.getStyleClass().add("center-content");
        return page(header(navigator, false), content);
    }

    public static Parent login(AppNavigator navigator) {
        TextField email = authTextField("");
        PasswordField password = passwordField();
        Label message = formMessage("");
        Button submit = primaryButton("Sign In", () -> {
        });
        submit.setOnAction(event -> {
            clearMessage(message);
            if (email.getText().trim().isBlank() || password.getText().isBlank()) {
                showMessage(message, "Enter email and password.", false);
                return;
            }
            setAuthBusy(submit, true, "Signing in...");
            runAuthTask(() -> navigator.authClient().login(email.getText().trim(), password.getText()), session -> {
                try {
                    navigator.saveSession(session);
                    navigator.showAccountArchive();
                } catch (IOException exception) {
                    showMessage(message, "Could not save local session: " + exception.getMessage(), false);
                    setAuthBusy(submit, false, "Sign In");
                }
            }, exception -> {
                showMessage(message, exception.getMessage(), false);
                setAuthBusy(submit, false, "Sign In");
            });
        });

        VBox panel = authPanel(
                "COMPANION ACCESS",
                "Welcome back",
                body("Sign in with the same account you use in the web version."),
                authField("Email", email),
                authField("Password", password),
                message,
                submit,
                secondaryButton("Create account", navigator::showRegister),
                secondaryButton("Character access", navigator::showCharacterAccess)
        );
        return page(header(navigator, false), centered(panel));
    }

    public static Parent register(AppNavigator navigator) {
        TextField email = authTextField("");
        PasswordField password = passwordField();
        PasswordField confirm = passwordField();
        Label message = formMessage("");
        Button submit = primaryButton("Register", () -> {
        });
        submit.setOnAction(event -> {
            clearMessage(message);
            if (email.getText().trim().isBlank() || password.getText().isBlank() || confirm.getText().isBlank()) {
                showMessage(message, "Fill in every registration field.", false);
                return;
            }
            if (!password.getText().equals(confirm.getText())) {
                showMessage(message, "The password confirmation does not match.", false);
                return;
            }
            setAuthBusy(submit, true, "Registering...");
            runAuthTask(() -> navigator.authClient().register(email.getText().trim(), password.getText(), confirm.getText()), user -> {
                showMessage(message, "Account created. Check your verification email before signing in.", true);
                setAuthBusy(submit, false, "Register");
            }, exception -> {
                showMessage(message, exception.getMessage(), false);
                setAuthBusy(submit, false, "Register");
            });
        });

        VBox panel = authPanel(
                "COMPANION ACCESS",
                "Create account",
                body("Registration sends a verification email through the configured backend mail service."),
                authField("Email", email),
                authField("Password", password),
                authField("Confirm password", confirm),
                message,
                submit,
                secondaryButton("Sign in", navigator::showLogin),
                secondaryButton("Character access", navigator::showCharacterAccess)
        );
        return page(header(navigator, false), centered(panel));
    }

    public static Parent account(AppNavigator navigator) {
        Optional<AuthSession> session = navigator.currentSession();
        if (session.isEmpty()) {
            return login(navigator);
        }
        AuthUser user = session.get().user();
        Label message = formMessage("");
        Label email = body(user.email());
        Label role = body(displayLabel(user.role()));
        Label verified = body(user.emailVerified() ? "Verified" : "Email verification required");
        Button refresh = secondaryButton("Refresh Profile", () -> {
        });
        refresh.setOnAction(event -> {
            clearMessage(message);
            setAuthBusy(refresh, true, "Refreshing...");
            runAuthTask(() -> navigator.authClient().currentUser(session.get()), currentUser -> {
                AuthSession refreshed = new AuthSession(session.get().accessToken(), session.get().refreshToken(),
                        session.get().tokenType(), currentUser);
                try {
                    navigator.saveSession(refreshed);
                    navigator.showAccount();
                } catch (IOException exception) {
                    showMessage(message, "Could not update local session: " + exception.getMessage(), false);
                    setAuthBusy(refresh, false, "Refresh Profile");
                }
            }, exception -> {
                showMessage(message, exception.getMessage(), false);
                setAuthBusy(refresh, false, "Refresh Profile");
            });
        });
        Button resend = secondaryButton("Resend Verification", () -> {
        });
        resend.setDisable(user.emailVerified());
        resend.setOnAction(event -> {
            clearMessage(message);
            setAuthBusy(resend, true, "Sending...");
            runAuthTask(() -> navigator.authClient().resendVerification(user.email()), result -> {
                showMessage(message, result, true);
                setAuthBusy(resend, false, "Resend Verification");
            }, exception -> {
                showMessage(message, exception.getMessage(), false);
                setAuthBusy(resend, false, "Resend Verification");
            });
        });
        Button logout = dangerButton("Logout", () -> {
        });
        logout.setOnAction(event -> {
            setAuthBusy(logout, true, "Logging out...");
            runAuthTask(() -> {
                navigator.authClient().logout(session.get());
                return true;
            }, result -> {
                try {
                    navigator.clearSession();
                } catch (IOException ignored) {
                    // The server logout is more important than a stale local file here.
                }
                navigator.showCharacterAccess();
            }, exception -> {
                try {
                    navigator.clearSession();
                    navigator.showCharacterAccess();
                } catch (IOException ioException) {
                    showMessage(message, ioException.getMessage(), false);
                    setAuthBusy(logout, false, "Logout");
                }
            });
        });

        GridPane details = new GridPane();
        details.getStyleClass().add("auth-details-grid");
        details.setHgap(14);
        details.setVgap(14);
        addAuthDetail(details, 0, "Email", email);
        addAuthDetail(details, 1, "Role", role);
        addAuthDetail(details, 2, "Status", verified);
        VBox panel = authPanel(
                "ACCOUNT MODE",
                "Profile",
                body("Server character sync will use this session in the next desktop stage."),
                details,
                message,
                new HBox(12, refresh, resend, logout),
                primaryButton("Account Characters", navigator::showAccountArchive),
                secondaryButton("Continue as Guest", navigator::showGuestArchive),
                secondaryButton("Character access", navigator::showCharacterAccess)
        );
        return page(header(navigator, false), centered(panel));
    }

    private static Parent settingsCard(String title, Parent body) {
        VBox card = new VBox(16, eyebrow(title.toUpperCase()), body);
        card.getStyleClass().add("settings-card");
        return card;
    }

    private static Button settingsNavButton(String text) {
        Button button = secondaryButton(text, () -> {
        });
        button.getStyleClass().add("settings-nav-button");
        return button;
    }

    private static void selectSettingsSection(
            List<Button> buttons,
            Button selected,
            VBox sectionBody,
            String title,
            Parent body
    ) {
        for (Button button : buttons) {
            button.getStyleClass().remove("active");
        }
        selected.getStyleClass().add("active");
        sectionBody.getChildren().setAll(settingsCard(title, body));
    }

    private static Parent settingRow(String labelText, Parent control) {
        Label label = eyebrow(labelText.toUpperCase());
        label.setMinWidth(210);
        HBox row = new HBox(18, label, control);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("settings-row");
        HBox.setHgrow(control, Priority.ALWAYS);
        return row;
    }

    private static Slider percentSlider(int value) {
        Slider slider = new Slider(0, 100, Math.max(0, Math.min(100, value)));
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(25);
        slider.setBlockIncrement(5);
        slider.getStyleClass().add("settings-slider");
        return slider;
    }

    private static Parent labeledSlider(Slider slider) {
        Label value = body(Math.round(slider.getValue()) + "%");
        value.getStyleClass().add("settings-percent");
        slider.valueProperty().addListener((observable, oldValue, newValue) ->
                value.setText(Math.round(newValue.doubleValue()) + "%"));
        HBox row = new HBox(14, slider, value);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(slider, Priority.ALWAYS);
        return row;
    }

    private static void rebuildHotkeyRows(VBox rows, List<HotkeyBinding> hotkeys) {
        rows.getChildren().clear();
        for (int index = 0; index < hotkeys.size(); index++) {
            int hotkeyIndex = index;
            HotkeyBinding binding = hotkeys.get(index);
            Label action = body(binding.label());
            action.getStyleClass().add("settings-hotkey-action");
            TextField combination = new TextField(binding.combination());
            combination.setEditable(false);
            combination.getStyleClass().add("settings-hotkey-input");
            combination.setOnMouseClicked(event -> {
                combination.requestFocus();
                combination.setText("Press keys...");
                event.consume();
            });
            combination.setOnKeyPressed(event -> {
                String display = keyDisplay(event);
                if (!display.isBlank()) {
                    hotkeys.set(hotkeyIndex, new HotkeyBinding(binding.actionId(), binding.label(), display));
                    combination.setText(display);
                }
                event.consume();
            });
            HBox row = new HBox(14, action, combination);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("settings-hotkey-row");
            HBox.setHgrow(action, Priority.ALWAYS);
            rows.getChildren().add(row);
        }
    }

    private static String keyDisplay(javafx.scene.input.KeyEvent event) {
        String code = switch (event.getCode()) {
            case COMMA -> ",";
            case DIGIT1 -> "1";
            case DIGIT2 -> "2";
            case DIGIT3 -> "3";
            case DIGIT4 -> "4";
            case ESCAPE -> "Esc";
            case ALT -> "Alt";
            default -> event.getCode().getName();
        };
        if ("Undefined".equals(code) || code.isBlank()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
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

    private static List<String> availableResolutions() {
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        int maxWidth = Math.max(1920, (int) Math.floor(bounds.getWidth()));
        int maxHeight = Math.max(1080, (int) Math.floor(bounds.getHeight()));
        List<String> candidates = List.of(
                "1024 x 640",
                "1100 x 720",
                "1280 x 720",
                "1366 x 768",
                "1440 x 900",
                "1600 x 900",
                "1920 x 1080",
                maxWidth + " x " + maxHeight
        );
        return candidates.stream()
                .distinct()
                .filter(value -> {
                    int[] dimensions = parseResolution(value);
                    return dimensions[0] <= 1920 && dimensions[1] <= 1080
                            && dimensions[0] <= maxWidth && dimensions[1] <= maxHeight;
                })
                .toList();
    }

    private static int[] parseResolution(String value) {
        if (value == null || !value.contains("x")) {
            return new int[]{1100, 720};
        }
        String[] parts = value.toLowerCase().split("x");
        try {
            return new int[]{
                    Math.max(1024, Integer.parseInt(parts[0].trim())),
                    Math.max(640, Integer.parseInt(parts[1].trim()))
            };
        } catch (NumberFormatException exception) {
            return new int[]{1100, 720};
        }
    }

    private static DesktopSettings copySettings(DesktopSettings source) {
        DesktopSettings copy = DesktopSettings.defaults();
        copy.setWindowWidth(source.getWindowWidth());
        copy.setWindowHeight(source.getWindowHeight());
        copy.setFullscreen(source.isFullscreen());
        copy.setMusicTheme(source.getMusicTheme());
        copy.setMusicEnabled(source.isMusicEnabled());
        copy.setSoundEnabled(source.isSoundEnabled());
        copy.setMusicVolume(source.getMusicVolume());
        copy.setSoundVolume(source.getSoundVolume());
        copy.setHotkeys(source.getHotkeys());
        return copy;
    }

    public static Parent guestArchive(AppNavigator navigator, GuestCharacterArchive archive) {
        VBox content = new VBox(28);
        content.getStyleClass().add("content");

        BorderPane hero = new BorderPane();
        hero.getStyleClass().add("archive-hero");
        VBox copy = new VBox(10, eyebrow("GUEST MODE"), h1("Your local characters"),
                body("Local archive stored on this machine."));
        copy.getStyleClass().add("hero-copy");

        Label counter = new Label(archive.list().size() + " / " + GuestCharacterArchive.CHARACTER_LIMIT);
        counter.getStyleClass().add("counter");
        Button create = primaryButton("New Character", () -> createCharacter(navigator, archive));
        Button importJson = secondaryButton("Import JSON", () -> importCharacter(navigator, archive));
        HBox actions = new HBox(16, counter, create, importJson);
        actions.getStyleClass().add("hero-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        hero.setLeft(copy);
        hero.setRight(actions);

        FlowPane cards = new FlowPane(18, 18);
        cards.getStyleClass().add("card-grid");
        cards.setPrefWrapLength(1140);
        archive.list().forEach(record -> cards.getChildren().add(characterCard(navigator, archive, record)));
        if (archive.list().isEmpty()) {
            cards.getChildren().add(emptyState());
        }

        content.getChildren().addAll(hero, cards);
        return page(header(navigator, true), scroll(content));
    }

    public static Parent accountArchive(AppNavigator navigator) {
        Optional<AuthSession> session = navigator.currentSession();
        if (session.isEmpty()) {
            return login(navigator);
        }

        VBox content = new VBox(28);
        content.getStyleClass().add("content");
        Label counter = new Label("... / 100");
        counter.getStyleClass().add("counter");
        FlowPane cards = new FlowPane(18, 18);
        cards.getStyleClass().add("card-grid");
        cards.setPrefWrapLength(1140);
        Label message = formMessage("Loading account characters...");
        showMessage(message, "Loading account characters...", true);

        BorderPane hero = new BorderPane();
        hero.getStyleClass().add("archive-hero");
        VBox copy = new VBox(10, eyebrow("ACCOUNT MODE"), h1("Your synced characters"),
                body("Characters loaded from the same account used in the web version."));
        copy.getStyleClass().add("hero-copy");
        Button create = primaryButton("New Character", () -> createAccountCharacter(navigator, session.get()));
        Button importJson = secondaryButton("Import JSON", () -> importAccountCharacter(navigator, session.get()));
        Button refresh = secondaryButton("Refresh", navigator::showAccountArchive);
        Button guest = secondaryButton("Guest Archive", navigator::showGuestArchive);
        HBox actions = new HBox(16, counter, create, importJson, refresh, guest);
        actions.getStyleClass().add("hero-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        hero.setLeft(copy);
        hero.setRight(actions);

        content.getChildren().addAll(hero, message, cards);
        runAuthTask(() -> navigator.characterClient().list(session.get()), characters -> {
            counter.setText(characters.size() + " / 100");
            clearMessage(message);
            cards.getChildren().clear();
            characters.forEach(character -> cards.getChildren().add(accountCharacterCard(navigator, session.get(), character)));
            if (characters.isEmpty()) {
                VBox empty = new VBox(8, cardTitle("No synced characters yet"),
                        body("Create a character here or in the web version, then refresh this archive."));
                empty.getStyleClass().add("empty-state");
                cards.getChildren().add(empty);
            }
        }, exception -> {
            counter.setText("0 / 100");
            cards.getChildren().clear();
            showMessage(message, exception.getMessage(), false);
        });

        return page(header(navigator, true), scroll(content));
    }

    public static Parent characterSheet(
            AppNavigator navigator,
            GuestCharacterArchive archive,
            LocalCharacterRecord record,
            boolean editing,
            Double initialScroll
    ) {
        LocalCharacterSheet sheet;
        try {
            sheet = archive.readCharacterSheet(record.id());
        } catch (IOException exception) {
            error("Could not open character", exception);
            return guestArchive(navigator, archive);
        }

        return characterSheetFromLocal(navigator, archive, record, sheet, editing, initialScroll);
    }

    private static Parent characterSheetFromLocal(
            AppNavigator navigator,
            GuestCharacterArchive archive,
            LocalCharacterRecord record,
            LocalCharacterSheet sheet,
            boolean editing,
            Double initialScroll
    ) {
        VBox content = new VBox(24);
        content.getStyleClass().add("content");
        content.getStyleClass().add("sheet-content");

        BorderPane hero = new BorderPane();
        hero.getStyleClass().add("archive-hero");
        VBox copy = new VBox(10, eyebrow(editing ? "EDITING LOCAL CHARACTER" : "LOCAL CHARACTER"), h1(sheet.name()),
                body("Level " + sheet.info().level() + " / " + classLine(sheet)));
        copy.getStyleClass().add("hero-copy");
        FlowPane actions = new FlowPane(16, 10);
        actions.getChildren().add(secondaryButton("Back to Archive", navigator::showGuestArchive));
        if (editing) {
            actions.getChildren().add(secondaryButton("Cancel", () -> navigator.showCharacterSheet(record)));
        } else {
            actions.getChildren().add(primaryButton("Edit Sheet", () -> navigator.editCharacterSheet(record)));
        }
        actions.getChildren().add(secondaryButton("Export JSON", () -> exportCharacter(archive, record)));
        actions.getStyleClass().add("hero-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        hero.setLeft(copy);
        hero.setRight(actions);

        SheetForm form = new SheetForm(sheet, editing, navigator.stage());
        content.getChildren().addAll(hero, form.root());
        return page(header(navigator, true), scroll(content, initialScroll), sheetActionBar(navigator, archive, record, form, editing));
    }

    public static Parent accountCharacterSheet(
            AppNavigator navigator,
            UUID characterId,
            boolean editing,
            Double initialScroll
    ) {
        Optional<AuthSession> session = navigator.currentSession();
        if (session.isEmpty()) {
            return login(navigator);
        }

        BorderPane page = page(header(navigator, true), centered(new VBox(14,
                titleBlock("ACCOUNT CHARACTER", "Opening sheet"),
                body("Loading character sheet from the server...")
        )));
        runAuthTask(() -> navigator.characterClient().getSheet(session.get(), characterId), loaded -> {
            BorderPane loadedPage = (BorderPane) accountCharacterSheetFromLocal(
                    navigator,
                    session.get(),
                    loaded.toLocalSheet(),
                    editing,
                    initialScroll
            );
            page.setCenter(loadedPage.getCenter());
            page.setBottom(loadedPage.getBottom());
        }, exception -> page.setCenter(centered(new VBox(14,
                titleBlock("ACCOUNT CHARACTER", "Could not open sheet"),
                formError(exception.getMessage()),
                secondaryButton("Account Archive", navigator::showAccountArchive),
                secondaryButton("Sign in again", navigator::showLogin)
        ))));
        return page;
    }

    private static Parent accountCharacterSheetFromLocal(
            AppNavigator navigator,
            AuthSession session,
            LocalCharacterSheet sheet,
            boolean editing,
            Double initialScroll
    ) {
        UUID characterId = UUID.fromString(sheet.id());
        VBox content = new VBox(24);
        content.getStyleClass().add("content");
        content.getStyleClass().add("sheet-content");

        BorderPane hero = new BorderPane();
        hero.getStyleClass().add("archive-hero");
        VBox copy = new VBox(10, eyebrow(editing ? "EDITING ACCOUNT CHARACTER" : "ACCOUNT CHARACTER"),
                h1(sheet.name()), body("Level " + sheet.info().level() + " / " + classLine(sheet)));
        copy.getStyleClass().add("hero-copy");
        FlowPane actions = new FlowPane(16, 10);
        actions.getChildren().add(secondaryButton("Account Archive", navigator::showAccountArchive));
        if (editing) {
            actions.getChildren().add(secondaryButton("Cancel", () -> navigator.showAccountCharacterSheet(characterId)));
        } else {
            actions.getChildren().add(primaryButton("Edit Sheet", () -> navigator.editAccountCharacterSheet(characterId)));
        }
        actions.getChildren().add(secondaryButton("Export JSON", () -> exportAccountCharacter(navigator, session, sheet)));
        actions.getStyleClass().add("hero-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        hero.setLeft(copy);
        hero.setRight(actions);

        SheetForm[] formRef = new SheetForm[1];
        AssetSyncActions assetActions = accountAssetActions(navigator, session, characterId, formRef);
        SheetForm form = new SheetForm(sheet, editing, true, assetActions, navigator.stage());
        formRef[0] = form;
        content.getChildren().setAll(hero, form.root());
        return page(header(navigator, true), scroll(content, initialScroll), accountSheetActionBar(navigator, session, form, editing));
    }

    public static Parent extraContent(AppNavigator navigator, String sectionId, String selectedSlug) {
        ContentSection section = ContentSection.fromId(sectionId);
        List<ContentNode> tree;
        try {
            tree = EXTRA_CONTENT.load(section);
        } catch (IOException exception) {
            VBox error = new VBox(18,
                    titleBlock("EXTRA CONTENT", "Archive unavailable"),
                    formError(exception.getMessage()),
                    secondaryButton("Main Menu", navigator::showMainMenu)
            );
            error.getStyleClass().add("access-panel");
            return page(header(navigator, true), centered(error));
        }

        ContentNode selected = selectedSlug == null || selectedSlug.isBlank()
                ? null
                : findContentNode(tree, selectedSlug).orElse(null);
        if (selected == null && !tree.isEmpty()) {
            selected = tree.get(0);
        }

        VBox content = new VBox(28);
        content.getStyleClass().add("content");
        BorderPane hero = new BorderPane();
        hero.getStyleClass().add("archive-hero");
        VBox copy = new VBox(10, eyebrow(section.eyebrow()), h1(section.title()), body(section.description()));
        copy.getStyleClass().add("hero-copy");
        HBox actions = new HBox(12,
                contentSectionButton(navigator, ContentSection.LORE, section),
                contentSectionButton(navigator, ContentSection.RULES, section)
        );
        actions.getStyleClass().add("hero-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        hero.setLeft(copy);
        hero.setRight(actions);

        if (selected == null) {
            VBox empty = new VBox(10, eyebrow("EMPTY ARCHIVE"), cardTitle("No articles yet."),
                    body("The structure is ready for the first content pass."));
            empty.getStyleClass().add("empty-state");
            content.getChildren().addAll(hero, empty);
            return page(header(navigator, true), scroll(content));
        }

        HBox browser = new HBox(24);
        browser.getStyleClass().add("content-browser");
        browser.getChildren().addAll(
                contentSidebar(navigator, section, tree, selected.slug()),
                contentArticle(navigator, section, selected)
        );
        content.getChildren().addAll(hero, browser);
        return page(header(navigator, true), scroll(content));
    }

    public static Parent placeholder(AppNavigator navigator, String title) {
        VBox content = new VBox(18,
                titleBlock(title.toUpperCase(), title),
                body("This desktop section belongs to a later implementation stage."),
                secondaryButton("Main Menu", navigator::showMainMenu)
        );
        content.getStyleClass().add("access-panel");
        return page(header(navigator, false), content);
    }

    private static Button contentSectionButton(
            AppNavigator navigator,
            ContentSection target,
            ContentSection current
    ) {
        Button button = target == current
                ? primaryButton(target.title(), () -> {
                })
                : secondaryButton(target.title(), SoundEffect.PAGE_TURN, () -> navigator.showExtraContent(target.id(), null));
        button.getStyleClass().add("content-section-tab");
        return button;
    }

    private static Parent contentSidebar(
            AppNavigator navigator,
            ContentSection section,
            List<ContentNode> tree,
            String selectedSlug
    ) {
        VBox links = new VBox(4);
        links.getStyleClass().add("content-tree");
        for (ContentNode node : tree) {
            links.getChildren().add(contentTreeNode(navigator, section, node, selectedSlug));
        }
        VBox sidebar = new VBox(14, eyebrow("CONTENTS"), links);
        sidebar.getStyleClass().add("content-sidebar");
        return sidebar;
    }

    private static Parent contentTreeNode(
            AppNavigator navigator,
            ContentSection section,
            ContentNode node,
            String selectedSlug
    ) {
        Button link = new Button(node.title());
        link.getStyleClass().add("content-tree-link");
        if (node.slug().equals(selectedSlug)) {
            link.getStyleClass().add("active");
        }
        link.setMaxWidth(Double.MAX_VALUE);
        link.setWrapText(true);
        link.setOnAction(event -> {
            DesktopAudio.play(SoundEffect.PAGE_TURN);
            navigator.showExtraContent(section.id(), node.slug());
        });
        VBox wrapper = new VBox(4, link);
        wrapper.getStyleClass().add("content-tree-node");
        if (!node.children().isEmpty()) {
            VBox children = new VBox(3);
            children.getStyleClass().add("content-tree-children");
            for (ContentNode child : node.children()) {
                children.getChildren().add(contentTreeNode(navigator, section, child, selectedSlug));
            }
            wrapper.getChildren().add(children);
        }
        return wrapper;
    }

    private static Parent contentArticle(AppNavigator navigator, ContentSection section, ContentNode selected) {
        VBox article = new VBox(14);
        article.getStyleClass().add("content-article");
        Label title = new Label(selected.title());
        title.getStyleClass().add("content-article-title");
        title.setWrapText(true);
        article.getChildren().addAll(eyebrow(selected.category() ? "CATEGORY" : "ARTICLE"), title);
        if (!selected.summary().isBlank()) {
            Label summary = body(selected.summary());
            summary.getStyleClass().add("content-summary");
            article.getChildren().add(summary);
        }
        if (selected.contentMarkdown().trim().isBlank()) {
            Label empty = body("This article is waiting for content.");
            empty.getStyleClass().add("empty-copy");
            article.getChildren().add(empty);
        } else {
            article.getChildren().add(markdownContent(selected.contentMarkdown()));
        }
        if (!selected.children().isEmpty()) {
            article.getChildren().add(contentChildList(navigator, section, selected.children()));
        }
        HBox.setHgrow(article, Priority.ALWAYS);
        return article;
    }

    private static Parent contentChildList(AppNavigator navigator, ContentSection section, List<ContentNode> children) {
        FlowPane cards = new FlowPane(10, 10);
        cards.getStyleClass().add("content-child-cards");
        for (ContentNode child : children) {
            VBox card = new VBox(6, eyebrow(child.title().toUpperCase()),
                    body(child.summary().isBlank() ? "Open article." : child.summary()));
            card.getStyleClass().add("content-child-card");
            card.setOnMouseClicked(event -> {
                DesktopAudio.play(SoundEffect.PAGE_TURN);
                navigator.showExtraContent(section.id(), child.slug());
            });
            cards.getChildren().add(card);
        }
        VBox list = new VBox(12, eyebrow("IN THIS CATEGORY"), cards);
        list.getStyleClass().add("content-child-list");
        return list;
    }

    private static Optional<ContentNode> findContentNode(List<ContentNode> nodes, String slug) {
        for (ContentNode node : nodes) {
            if (node.slug().equals(slug)) {
                return Optional.of(node);
            }
            Optional<ContentNode> child = findContentNode(node.children(), slug);
            if (child.isPresent()) {
                return child;
            }
        }
        return Optional.empty();
    }

    private static Parent markdownContent(String markdown) {
        VBox content = new VBox(10);
        content.getStyleClass().add("markdown-content");
        String[] lines = markdown.replace("\r\n", "\n").split("\n");
        int index = 0;
        while (index < lines.length) {
            String line = lines[index];
            if (line.trim().isEmpty()) {
                index++;
                continue;
            }
            int headingLevel = headingLevel(line);
            if (headingLevel > 0) {
                Label heading = new Label(line.trim().substring(headingLevel).trim());
                heading.getStyleClass().add("markdown-heading-" + headingLevel);
                heading.setWrapText(true);
                content.getChildren().add(heading);
                index++;
                continue;
            }
            if (isMarkdownListLine(line)) {
                boolean ordered = isOrderedMarkdownListLine(line);
                VBox list = new VBox(7);
                list.getStyleClass().add("markdown-list");
                int itemNumber = 1;
                while (index < lines.length) {
                    if (isMarkdownListLine(lines[index]) && isOrderedMarkdownListLine(lines[index]) == ordered) {
                        String marker = ordered ? markdownListNumber(lines[index], itemNumber) + "." : "-";
                        list.getChildren().add(markdownListItem(stripMarkdownListMarker(lines[index]), marker));
                        itemNumber++;
                        index++;
                        continue;
                    }
                    if (lines[index].trim().isEmpty() && nextNonEmptyMarkdownListLine(lines, index, ordered)) {
                        index++;
                        continue;
                    }
                    break;
                }
                content.getChildren().add(list);
                continue;
            }
            StringBuilder paragraph = new StringBuilder();
            while (index < lines.length
                    && !lines[index].trim().isEmpty()
                    && headingLevel(lines[index]) == 0
                    && !isMarkdownListLine(lines[index])) {
                if (!paragraph.isEmpty()) {
                    paragraph.append(' ');
                }
                paragraph.append(lines[index].trim());
                index++;
            }
            TextFlow text = inlineMarkdown(paragraph.toString());
            text.getStyleClass().add("markdown-paragraph");
            content.getChildren().add(text);
        }
        return content;
    }

    private static Parent markdownListItem(String text, String markerText) {
        Label marker = new Label(markerText);
        marker.getStyleClass().add("markdown-list-marker");
        TextFlow copy = inlineMarkdown(text);
        copy.getStyleClass().add("markdown-list-copy");
        HBox row = new HBox(8, marker, copy);
        row.getStyleClass().add("markdown-list-row");
        HBox.setHgrow(copy, Priority.ALWAYS);
        return row;
    }

    private static TextFlow inlineMarkdown(String text) {
        TextFlow flow = new TextFlow();
        flow.getStyleClass().add("markdown-inline");
        int cursor = 0;
        while (cursor < text.length()) {
            int start = text.indexOf("**", cursor);
            if (start < 0) {
                flow.getChildren().add(markdownText(text.substring(cursor), false));
                break;
            }
            if (start > cursor) {
                flow.getChildren().add(markdownText(text.substring(cursor, start), false));
            }
            int end = text.indexOf("**", start + 2);
            if (end < 0) {
                flow.getChildren().add(markdownText(text.substring(start), false));
                break;
            }
            flow.getChildren().add(markdownText(text.substring(start + 2, end), true));
            cursor = end + 2;
        }
        return flow;
    }

    private static Text markdownText(String value, boolean strong) {
        Text text = new Text(value);
        text.getStyleClass().add(strong ? "markdown-strong" : "markdown-text");
        return text;
    }

    private static int headingLevel(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("# ")) {
            return 1;
        }
        if (trimmed.startsWith("## ")) {
            return 2;
        }
        if (trimmed.startsWith("### ")) {
            return 3;
        }
        return 0;
    }

    private static boolean isMarkdownListLine(String line) {
        return line.matches("\\s*(?:[-*]|\\d+[).])\\s+.+");
    }

    private static boolean isOrderedMarkdownListLine(String line) {
        return line.matches("\\s*\\d+[).]\\s+.+");
    }

    private static boolean nextNonEmptyMarkdownListLine(String[] lines, int fromIndex, boolean ordered) {
        int index = fromIndex + 1;
        while (index < lines.length && lines[index].trim().isEmpty()) {
            index++;
        }
        return index < lines.length && isMarkdownListLine(lines[index])
                && isOrderedMarkdownListLine(lines[index]) == ordered;
    }

    private static int markdownListNumber(String line, int fallback) {
        String trimmed = line.trim();
        int end = 0;
        while (end < trimmed.length() && Character.isDigit(trimmed.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return fallback;
        }
        try {
            return Integer.parseInt(trimmed.substring(0, end));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static String stripMarkdownListMarker(String line) {
        return line.replaceFirst("^\\s*(?:[-*]|\\d+[).])\\s+", "");
    }

    private static BorderPane page(Parent header, Parent content) {
        return page(header, content, null);
    }

    private static BorderPane page(Parent header, Parent content, Parent footer) {
        BorderPane page = new BorderPane();
        page.getStyleClass().add("page");
        if (header != null) {
            page.setTop(header);
        }
        page.setCenter(content);
        if (footer != null) {
            page.setBottom(footer);
        }
        return page;
    }

    private static Parent sheetActionBar(
            AppNavigator navigator,
            GuestCharacterArchive archive,
            LocalCharacterRecord record,
            SheetForm form,
            boolean editing
    ) {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("sheet-action-bar");
        bar.setAlignment(Pos.CENTER_RIGHT);
        Label state = new Label(editing ? "Editing local sheet" : "Viewing local sheet");
        state.getStyleClass().add("sheet-action-state");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(state, spacer);
        if (editing) {
            bar.getChildren().addAll(
                    primaryButton("Save Changes", SoundEffect.WRITE, () -> saveCharacterSheet(navigator, archive, form)),
                    secondaryButton("Cancel", () -> navigator.showCharacterSheet(record))
            );
        } else {
            bar.getChildren().add(primaryButton("Edit Sheet", () -> navigator.editCharacterSheet(record)));
        }
        bar.getChildren().addAll(
                secondaryButton("Export JSON", () -> exportCharacter(archive, record)),
                secondaryButton("Back to Archive", navigator::showGuestArchive)
        );
        return bar;
    }

    private static Parent accountSheetActionBar(
            AppNavigator navigator,
            AuthSession session,
            SheetForm form,
            boolean editing
    ) {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("sheet-action-bar");
        bar.setAlignment(Pos.CENTER_RIGHT);
        Label state = new Label(editing ? "Editing server sheet" : "Viewing server sheet");
        state.getStyleClass().add("sheet-action-state");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(state, spacer);
        if (editing) {
            bar.getChildren().addAll(
                    primaryButton("Save Changes", SoundEffect.WRITE, () -> saveAccountCharacterSheet(navigator, session, form)),
                    secondaryButton("Cancel", () -> navigator.showAccountCharacterSheet(UUID.fromString(form.originalId())))
            );
        } else {
            bar.getChildren().add(primaryButton("Edit Sheet", () -> navigator.editAccountCharacterSheet(UUID.fromString(form.originalId()))));
        }
        bar.getChildren().add(secondaryButton("Account Archive", navigator::showAccountArchive));
        bar.getChildren().add(secondaryButton("Export JSON", () -> exportAccountCharacter(navigator, session, form.toSheet())));
        return bar;
    }

    private static Parent header(AppNavigator navigator, boolean guestActions) {
        HBox header = new HBox(26);
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label brand = new Label("PFP COMPANION");
        brand.getStyleClass().add("brand");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button main = navButton("Main Menu", navigator::showMainMenu);
        Button characters = navButton("Characters", navigator::showCharacterAccess);
        header.getChildren().addAll(brand, spacer, main, characters);
        if (guestActions) {
            header.getChildren().add(navButton(navigator.currentSession().isPresent() ? "Account" : "Login",
                    navigator.currentSession().isPresent() ? navigator::showAccount : navigator::showLogin));
        }
        return header;
    }

    private static VBox brandBlock() {
        Label pfp = new Label("PFP");
        pfp.getStyleClass().add("home-brand-strong");
        Label companion = new Label("COMPANION");
        companion.getStyleClass().add("home-brand-wide");
        HBox brand = new HBox(10, pfp, companion);
        brand.setAlignment(Pos.BASELINE_LEFT);
        return new VBox(8, brand, eyebrow("DESKTOP EDITION"));
    }

    private static Parent ambientFrame() {
        HBox stats = new HBox(14,
                ambientStat("30", "guest slots"),
                ambientStat("JSON", "portable sheets"),
                ambientStat("LOCAL", "offline first")
        );
        stats.getStyleClass().add("ambient-stats");

        VBox frame = new VBox(18,
                eyebrow("GUEST-FIRST WORKFLOW"),
                body("Local characters stay available without a server. Account mode keeps your archive aligned with the web version."),
                stats
        );
        frame.getStyleClass().add("ambient-frame");
        return frame;
    }

    private static VBox ambientStat(String value, String label) {
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("ambient-stat-value");
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("ambient-stat-label");
        return new VBox(4, valueLabel, labelLabel);
    }

    private static Region menuDivider() {
        Region divider = new Region();
        divider.getStyleClass().add("menu-divider");
        return divider;
    }

    private static VBox titleBlock(String eyebrow, String title) {
        return new VBox(10, eyebrow(eyebrow), h1(title));
    }

    private static VBox centered(Parent panel) {
        VBox content = new VBox(panel);
        content.getStyleClass().add("center-content");
        return content;
    }

    private static VBox authPanel(String eyebrow, String title, Parent... children) {
        VBox panel = new VBox(16);
        panel.getStyleClass().add("auth-panel");
        panel.getChildren().add(titleBlock(eyebrow, title));
        panel.getChildren().addAll(children);
        return panel;
    }

    private static TextField authTextField(String value) {
        TextField field = textField(value);
        field.getStyleClass().add("auth-input");
        return field;
    }

    private static PasswordField passwordField() {
        PasswordField field = new PasswordField();
        field.getStyleClass().add("sheet-input");
        field.getStyleClass().add("auth-input");
        return field;
    }

    private static VBox authField(String label, Parent control) {
        VBox field = new VBox(7, eyebrow(label.toUpperCase()), control);
        field.getStyleClass().add("auth-field");
        return field;
    }

    private static Label formMessage(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-message");
        label.setWrapText(true);
        label.setVisible(text != null && !text.isBlank());
        label.setManaged(text != null && !text.isBlank());
        return label;
    }

    private static Label formError(String text) {
        Label label = formMessage(text);
        showMessage(label, text, false);
        return label;
    }

    private static void showMessage(Label label, String message, boolean success) {
        label.setText(message == null ? "" : message);
        label.getStyleClass().removeAll("form-message-success", "form-message-error");
        label.getStyleClass().add(success ? "form-message-success" : "form-message-error");
        label.setVisible(true);
        label.setManaged(true);
    }

    private static void clearMessage(Label label) {
        label.setText("");
        label.getStyleClass().removeAll("form-message-success", "form-message-error");
        label.setVisible(false);
        label.setManaged(false);
    }

    private static void setAuthBusy(Button button, boolean busy, String text) {
        button.setDisable(busy);
        button.setText(text);
    }

    private static void addAuthDetail(GridPane grid, int index, String label, Parent value) {
        VBox box = new VBox(6, eyebrow(label.toUpperCase()), value);
        box.getStyleClass().add("auth-detail");
        GridPane.setHgrow(box, Priority.ALWAYS);
        grid.add(box, index % 3, index / 3);
    }

    private static <T> void runAuthTask(Callable<T> work, Consumer<T> onSuccess, Consumer<Exception> onError) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return work.call();
            } catch (Exception exception) {
                throw new CompletionException(exception);
            }
        }).whenComplete((result, throwable) -> Platform.runLater(() -> {
            if (throwable == null) {
                onSuccess.accept(result);
                return;
            }
            Throwable cause = throwable instanceof CompletionException && throwable.getCause() != null
                    ? throwable.getCause()
                    : throwable;
            onError.accept(cause instanceof Exception exception ? exception : new AuthException(cause.getMessage()));
        }));
    }

    private static Label eyebrow(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("eyebrow");
        return label;
    }

    private static Label h1(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("h1");
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(760);
        return label;
    }

    private static Label body(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("body");
        label.setWrapText(true);
        return label;
    }

    private static Button primaryButton(String text, Runnable action) {
        return primaryButton(text, SoundEffect.BUTTON_CLICK, action);
    }

    private static Button primaryButton(String text, SoundEffect effect, Runnable action) {
        Button button = button(text, effect, action);
        button.getStyleClass().add("primary-button");
        return button;
    }

    private static Button secondaryButton(String text, Runnable action) {
        return secondaryButton(text, SoundEffect.BUTTON_CLICK, action);
    }

    private static Button secondaryButton(String text, SoundEffect effect, Runnable action) {
        Button button = button(text, effect, action);
        button.getStyleClass().add("secondary-button");
        return button;
    }

    private static Button menuButton(String text, Runnable action) {
        Button button = button(text, SoundEffect.BUTTON_CLICK, action);
        button.getStyleClass().add("menu-button");
        return button;
    }

    private static Button navButton(String text, Runnable action) {
        Button button = button(text, SoundEffect.BUTTON_CLICK, action);
        button.getStyleClass().add("nav-button");
        return button;
    }

    private static Button button(String text, SoundEffect effect, Runnable action) {
        Button button = new Button(text);
        button.setMinHeight(Region.USE_PREF_SIZE);
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setOnAction(event -> {
            DesktopAudio.play(effect);
            action.run();
        });
        return button;
    }

    private static Parent scroll(Parent content) {
        return scroll(content, null);
    }

    private static Parent scroll(Parent content, Double initialVvalue) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("scroll");
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double scrollableHeight = scrollPane.getContent().getBoundsInLocal().getHeight()
                    - scrollPane.getViewportBounds().getHeight();
            if (scrollableHeight <= 0) {
                return;
            }
            double acceleratedDelta = event.getDeltaY() * 2.8;
            double nextValue = scrollPane.getVvalue() - acceleratedDelta / scrollableHeight;
            scrollPane.setVvalue(Math.max(0, Math.min(1, nextValue)));
            event.consume();
        });
        if (initialVvalue != null) {
            Platform.runLater(() -> scrollPane.setVvalue(Math.max(0, Math.min(1, initialVvalue))));
        }
        return scrollPane;
    }

    private static Parent characterCard(
            AppNavigator navigator,
            GuestCharacterArchive archive,
            LocalCharacterRecord record
    ) {
        VBox card = new VBox(16);
        card.getStyleClass().add("character-card");
        card.setPrefWidth(334);

        HBox summary = new HBox(16);
        summary.setAlignment(Pos.CENTER_LEFT);
        StackPane portrait = archivePortrait(record);
        portrait.getStyleClass().add("portrait");
        VBox text = new VBox(6, eyebrow("LEVEL " + record.level()), cardTitle(record.name()), body(record.classLine()));
        text.getStyleClass().add("card-copy");
        summary.getChildren().addAll(portrait, text);

        GridPane actions = new GridPane();
        actions.getStyleClass().add("card-actions");
        actions.setHgap(12);
        actions.add(secondaryButton("Open", () -> navigator.showCharacterSheet(record)), 0, 0);
        actions.add(secondaryButton("Export", () -> exportCharacter(archive, record)), 1, 0);
        actions.add(dangerButton("Delete", () -> deleteCharacter(navigator, archive, record)), 2, 0);
        card.getChildren().addAll(summary, actions);
        return card;
    }

    private static Parent accountCharacterCard(
            AppNavigator navigator,
            AuthSession session,
            AccountCharacterCard record
    ) {
        VBox card = new VBox(16);
        card.getStyleClass().add("character-card");
        card.setPrefWidth(334);

        HBox summary = new HBox(16);
        summary.setAlignment(Pos.CENTER_LEFT);
        StackPane portrait = imagePortrait(record.imageUrl(), record.name(), 76, 76);
        portrait.getStyleClass().add("portrait");
        VBox text = new VBox(6, eyebrow("LEVEL " + record.level()), cardTitle(record.name()), body(record.classLine()));
        text.getStyleClass().add("card-copy");
        summary.getChildren().addAll(portrait, text);

        GridPane actions = new GridPane();
        actions.getStyleClass().add("card-actions");
        actions.setHgap(12);
        actions.add(secondaryButton("Open", () -> navigator.showAccountCharacterSheet(record.id())), 0, 0);
        actions.add(secondaryButton("Export", () -> exportAccountCharacter(navigator, session, record)), 1, 0);
        actions.add(dangerButton("Delete", () -> deleteAccountCharacter(navigator, session, record)), 2, 0);

        card.getChildren().addAll(summary, actions);
        return card;
    }

    private static StackPane imagePortrait(String imageUrl, String name, double width, double height) {
        StackPane portrait = new StackPane();
        String preparedUrl = imageUrl == null ? "" : imageUrl.trim();
        if (!preparedUrl.isBlank() && !"local-placeholder".equals(preparedUrl)) {
            ImageView imageView = new ImageView(cachedImage(preparedUrl));
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
            portrait.getChildren().add(imageView);
        } else {
            portrait.getChildren().add(new Label(initial(name)));
        }
        return portrait;
    }

    private static Label cardTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("card-title");
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    private static StackPane archivePortrait(LocalCharacterRecord record) {
        StackPane portrait = new StackPane();
        String imageUrl = record.image() == null ? "" : record.image().trim();
        if (!imageUrl.isBlank() && !"local-placeholder".equals(imageUrl)) {
            ImageView imageView = new ImageView(cachedImage(imageUrl));
            imageView.setFitWidth(76);
            imageView.setFitHeight(76);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
            portrait.getChildren().add(imageView);
        } else {
            portrait.getChildren().add(new Label(initial(record.name())));
        }
        return portrait;
    }

    private static Button dangerButton(String text, Runnable action) {
        Button button = button(text, SoundEffect.BUTTON_CLICK, action);
        button.getStyleClass().add("danger-button");
        return button;
    }

    private static Parent emptyState() {
        VBox empty = new VBox(8, cardTitle("No local characters yet"),
                body("Create a guest character or import a PfP JSON file."));
        empty.getStyleClass().add("empty-state");
        return empty;
    }

    private static void createCharacter(AppNavigator navigator, GuestCharacterArchive archive) {
        Optional<String> result = showCharacterNameDialog(navigator.stage(), "New Character", "LOCAL ARCHIVE", "Create character");
        result.ifPresent(name -> {
            try {
                archive.createCharacter(name);
                navigator.showGuestArchive();
            } catch (Exception exception) {
                error("Could not create character", exception);
            }
        });
    }

    private static void importCharacter(AppNavigator navigator, GuestCharacterArchive archive) {
        FileChooser chooser = jsonChooser("Import character JSON");
        Path file = selectedPath(chooser.showOpenDialog(navigator.stage()));
        if (file == null) {
            return;
        }
        try {
            archive.importCharacter(file);
            navigator.showGuestArchive();
        } catch (Exception exception) {
            error("Could not import character", exception);
        }
    }

    private static void importAccountCharacter(AppNavigator navigator, AuthSession session) {
        FileChooser chooser = jsonChooser("Import character JSON to account");
        Path file = selectedPath(chooser.showOpenDialog(navigator.stage()));
        if (file == null) {
            return;
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            runAuthTask(() -> navigator.characterClient().importJson(session, json), created -> {
                info("Imported " + created.name() + " to your account.");
                navigator.showAccountArchive();
            }, exception -> error("Could not import account character", exception));
        } catch (Exception exception) {
            error("Could not read character JSON", exception);
        }
    }

    private static void createAccountCharacter(AppNavigator navigator, AuthSession session) {
        Optional<String> result = showCharacterNameDialog(navigator.stage(), "New Server Character", "ACCOUNT ARCHIVE", "Create character");
        if (result.isEmpty()) {
            return;
        }
        runAuthTask(() -> navigator.characterClient().create(session, result.get()), created -> {
            info("Created " + created.name() + ".");
            navigator.showAccountArchive();
        }, exception -> error("Could not create account character", exception));
    }

    private static Optional<String> showCharacterNameDialog(Window owner, String title, String eyebrowText, String cardTitleText) {
        ModalState<String> state = new ModalState<>();
        TextField name = textField("New Character");
        name.selectAll();

        VBox content = new VBox(14);
        content.getStyleClass().add("item-dialog-content");
        GridPane form = new GridPane();
        form.getStyleClass().add("sheet-grid");
        form.getStyleClass().add("sheet-grid-character");
        form.setHgap(14);
        form.setVgap(14);
        VBox field = new VBox(6, eyebrow("NAME"), name);
        field.getStyleClass().add("sheet-field");
        GridPane.setHgrow(field, Priority.ALWAYS);
        form.add(field, 0, 0);
        content.getChildren().addAll(new VBox(5, eyebrow(eyebrowText), cardTitle(cardTitleText)), form);

        Button ok = primaryButton("OK", SoundEffect.WRITE, () -> {
            String value = name.getText() == null ? "" : name.getText().trim();
            if (!value.isBlank()) {
                closeSceneModal(state, value, true);
            }
        });
        Button cancel = modalCancel("Cancel", state);
        HBox actions = new HBox(12, ok, cancel);
        actions.setAlignment(Pos.CENTER_RIGHT);
        content.getChildren().add(actions);
        Platform.runLater(name::requestFocus);
        return showSceneModal(owner, state, content);
    }

    private static Window activeOwner() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .filter(window -> window instanceof javafx.stage.Stage)
                .findFirst()
                .orElse(null);
    }

    private static <T> Optional<T> showSceneModal(Window owner, ModalState<T> state, Parent card) {
        Optional<StackPane> host = overlayHost(owner);
        if (host.isEmpty()) {
            return Optional.empty();
        }
        StackPane overlay = new StackPane(card);
        overlay.getStyleClass().add("scene-modal-overlay");
        state.host = host.get();
        state.overlay = overlay;
        state.host.getChildren().add(overlay);
        Platform.enterNestedEventLoop(state);
        return state.hasResult ? Optional.ofNullable(state.result) : Optional.empty();
    }

    private static <T> void closeSceneModal(ModalState<T> state, T result, boolean hasResult) {
        state.result = result;
        state.hasResult = hasResult;
        if (state.host != null && state.overlay != null) {
            state.host.getChildren().remove(state.overlay);
        }
        Platform.exitNestedEventLoop(state, null);
    }

    private static Optional<StackPane> overlayHost(Window owner) {
        Window target = owner == null ? activeOwner() : owner;
        if (target == null || target.getScene() == null) {
            return Optional.empty();
        }
        Parent root = target.getScene().getRoot();
        if (root instanceof StackPane stackPane && stackPane.getStyleClass().contains("app-root-shell")) {
            return Optional.of(stackPane);
        }
        return Optional.empty();
    }

    private static <T> Button modalPrimary(String text, ModalState<T> state, T result) {
        return primaryButton(text, () -> closeSceneModal(state, result, true));
    }

    private static <T> Button modalCancel(String text, ModalState<T> state) {
        return secondaryButton(text, () -> closeSceneModal(state, null, false));
    }

    private static boolean confirmOverlay(Window owner, String header, String contentText) {
        ModalState<Boolean> state = new ModalState<>();
        VBox content = new VBox(14,
                new VBox(5, eyebrow("CONFIRM ACTION"), cardTitle(header)),
                body(contentText)
        );
        content.getStyleClass().add("item-dialog-content");
        Button ok = primaryButton("OK", () -> closeSceneModal(state, true, true));
        Button cancel = modalCancel("Cancel", state);
        HBox actions = new HBox(12, ok, cancel);
        actions.setAlignment(Pos.CENTER_RIGHT);
        content.getChildren().add(actions);
        return showSceneModal(owner, state, content).orElse(false);
    }

    private static void messageOverlay(Window owner, String eyebrowText, String header, String message) {
        ModalState<Boolean> state = new ModalState<>();
        VBox content = new VBox(14,
                new VBox(5, eyebrow(eyebrowText), cardTitle(header)),
                body(message == null || message.isBlank() ? "No details." : message)
        );
        content.getStyleClass().add("item-dialog-content");
        HBox actions = new HBox(modalPrimary("OK", state, true));
        actions.setAlignment(Pos.CENTER_RIGHT);
        content.getChildren().add(actions);
        showSceneModal(owner, state, content);
    }

    private static final class ModalState<T> {
        private StackPane host;
        private StackPane overlay;
        private T result;
        private boolean hasResult;
    }

    private static void exportCharacter(GuestCharacterArchive archive, LocalCharacterRecord record) {
        FileChooser chooser = jsonChooser("Export character JSON");
        chooser.setInitialFileName(safeFileName(record.name()) + ".json");
        Path file = selectedPath(chooser.showSaveDialog(null));
        if (file == null) {
            return;
        }
        try {
            archive.exportCharacter(record.id(), file);
        } catch (Exception exception) {
            error("Could not export character", exception);
        }
    }

    private static void exportAccountCharacter(AppNavigator navigator, AuthSession session, LocalCharacterSheet sheet) {
        exportAccountCharacter(navigator, session, UUID.fromString(sheet.id()), sheet.name());
    }

    private static void exportAccountCharacter(AppNavigator navigator, AuthSession session, AccountCharacterCard record) {
        exportAccountCharacter(navigator, session, record.id(), record.name());
    }

    private static void exportAccountCharacter(AppNavigator navigator, AuthSession session, UUID characterId, String name) {
        FileChooser chooser = jsonChooser("Export account character JSON");
        chooser.setInitialFileName(safeFileName(name) + ".json");
        Path file = selectedPath(chooser.showSaveDialog(navigator.stage()));
        if (file == null) {
            return;
        }
        runAuthTask(() -> navigator.characterClient().exportJson(session, characterId), json -> {
            try {
                Files.writeString(file, json, StandardCharsets.UTF_8);
                info("Exported " + name + ".");
            } catch (IOException exception) {
                error("Could not write character JSON", exception);
            }
        }, exception -> error("Could not export account character", exception));
    }

    private static void saveCharacterSheet(AppNavigator navigator, GuestCharacterArchive archive, SheetForm form) {
        try {
            LocalCharacterSheet savedSheet = form.toSheet();
            Double initialScroll = navigator.currentSheetScroll().orElse(null);
            LocalCharacterRecord updated = archive.saveCharacterSheet(savedSheet);
            navigator.replaceRoot(characterSheetFromLocal(navigator, archive, updated, savedSheet, false, initialScroll));
        } catch (Exception exception) {
            error("Could not save character", exception);
        }
    }

    private static void saveAccountCharacterSheet(
            AppNavigator navigator,
            AuthSession session,
            SheetForm form
    ) {
        LocalCharacterSheet savedSheet = form.toSheet();
        Double initialScroll = navigator.currentSheetScroll().orElse(null);
        runAuthTask(() -> {
            navigator.characterClient().saveSheetChanges(session, form.originalSheet(), savedSheet);
            return true;
        }, saved -> {
            navigator.replaceRoot(accountCharacterSheetFromLocal(navigator, session, savedSheet, false, initialScroll));
        }, exception -> error("Could not save account character", exception));
    }

    private static AssetSyncActions accountAssetActions(
            AppNavigator navigator,
            AuthSession session,
            UUID characterId,
            SheetForm[] formRef
    ) {
        return new AssetSyncActions() {
            @Override
            public void addInventoryRow() {
                sync("Could not add inventory row",
                        () -> navigator.characterClient().addInventoryRow(session, characterId), formRef);
            }

            @Override
            public void removeInventoryRow() {
                sync("Could not remove inventory row",
                        () -> navigator.characterClient().removeInventoryRow(session, characterId), formRef);
            }

            @Override
            public void moveInventoryItem(int fromSlotIndex, int toSlotIndex) {
                sync("Could not move inventory item",
                        () -> navigator.characterClient().moveInventoryItem(session, characterId, fromSlotIndex, toSlotIndex),
                        formRef);
            }

            @Override
            public void createItem(LocalCharacterSheet.InventoryItem item) {
                sync("Could not create item",
                        () -> navigator.characterClient().createItem(session, characterId, item), formRef);
            }

            @Override
            public void createItemAtSlot(LocalCharacterSheet.InventoryItem item, int slotIndex) {
                sync("Could not create item",
                        () -> createAccountItemAtSlot(navigator, session, characterId, formRef, item, slotIndex), formRef);
            }

            @Override
            public void updateItem(LocalCharacterSheet.InventoryItem item) {
                sync("Could not update item",
                        () -> navigator.characterClient().updateItem(session, characterId, item), formRef);
            }

            @Override
            public void throwAwayItem(String itemId) {
                sync("Could not throw away item",
                        () -> navigator.characterClient().throwAwayItem(session, characterId, itemId), formRef);
            }

            @Override
            public void sellItem(String itemId) {
                sync("Could not sell item",
                        () -> navigator.characterClient().sellTradeItem(session, characterId, itemId), formRef);
            }

            @Override
            public void equipItem(String itemId, String slotCode) {
                sync("Could not equip item",
                        () -> navigator.characterClient().equipItem(session, characterId, itemId, slotCode), formRef);
            }

            @Override
            public void unequipItem(String slotCode) {
                sync("Could not unequip item",
                        () -> navigator.characterClient().unequipItem(session, characterId, slotCode), formRef);
            }

            @Override
            public void createSpell(LocalCharacterSheet.SpellPreview spell) {
                sync("Could not create spell",
                        () -> navigator.characterClient().createSpell(session, characterId, spell), formRef);
            }

            @Override
            public void updateSpell(LocalCharacterSheet.SpellPreview spell) {
                sync("Could not update spell",
                        () -> navigator.characterClient().updateSpell(session, characterId, spell), formRef);
            }

            @Override
            public void deleteSpell(String spellId) {
                sync("Could not delete spell",
                        () -> navigator.characterClient().deleteSpell(session, characterId, spellId), formRef);
            }
        };
    }

    private static void sync(
            String errorTitle,
            Callable<AccountCharacterSheet> action,
            SheetForm[] formRef
    ) {
        runAuthTask(action, updated -> {
            if (formRef[0] != null) {
                formRef[0].applyServerSheet(updated.toLocalSheet());
            }
        }, exception -> error(errorTitle, exception));
    }

    private static AccountCharacterSheet createAccountItemAtSlot(
            AppNavigator navigator,
            AuthSession session,
            UUID characterId,
            SheetForm[] formRef,
            LocalCharacterSheet.InventoryItem item,
            int targetSlotIndex
    ) throws Exception {
        LocalCharacterSheet before = formRef[0] == null ? null : formRef[0].toSheet();
        AccountCharacterSheet created = navigator.characterClient().createItem(session, characterId, item);
        LocalCharacterSheet after = created.toLocalSheet();
        Optional<String> createdItemId = createdItemId(before, after, item.id());
        if (createdItemId.isEmpty()) {
            return created;
        }
        Optional<Integer> fromSlot = after.inventory().slots().stream()
                .filter(slot -> slot.itemId().equals(createdItemId.get()))
                .map(LocalCharacterSheet.InventorySlot::index)
                .findFirst();
        if (fromSlot.isEmpty() || fromSlot.get() == targetSlotIndex) {
            return created;
        }
        return navigator.characterClient().moveInventoryItem(session, characterId, fromSlot.get(), targetSlotIndex);
    }

    private static Optional<String> createdItemId(
            LocalCharacterSheet before,
            LocalCharacterSheet after,
            String fallbackId
    ) {
        if (after.inventory().items().stream().anyMatch(item -> item.id().equals(fallbackId))
                && (before == null || before.inventory().items().stream().noneMatch(item -> item.id().equals(fallbackId)))) {
            return Optional.of(fallbackId);
        }
        if (before == null) {
            return Optional.empty();
        }
        List<String> beforeIds = before.inventory().items().stream()
                .map(LocalCharacterSheet.InventoryItem::id)
                .toList();
        return after.inventory().items().stream()
                .map(LocalCharacterSheet.InventoryItem::id)
                .filter(id -> !beforeIds.contains(id))
                .findFirst();
    }

    private static String classLine(LocalCharacterSheet sheet) {
        String className = sheet.info().className();
        String specialization = sheet.info().specialization();
        if (!className.isBlank() && !specialization.isBlank()) {
            return className + " / " + specialization;
        }
        if (!className.isBlank()) {
            return className;
        }
        if (!specialization.isBlank()) {
            return specialization;
        }
        return "Unwritten class";
    }

    private static Label value(String text) {
        Label label = new Label(text == null || text.isBlank() ? "—" : text);
        label.getStyleClass().add("sheet-value");
        label.setWrapText(true);
        return label;
    }

    private static TextField textField(String value) {
        TextField field = new TextField(value == null ? "" : value);
        field.getStyleClass().add("sheet-input");
        return field;
    }

    private static TextArea textArea(String value) {
        TextArea area = new TextArea(value == null ? "" : value);
        area.getStyleClass().add("sheet-textarea");
        area.setWrapText(true);
        area.setPrefRowCount(4);
        area.setScrollLeft(0);
        return area;
    }

    private static TextField numberField(Number value) {
        TextField field = textField(value == null ? "0" : value.toString());
        field.getStyleClass().add("sheet-number-input");
        field.setMinWidth(72);
        field.setPrefWidth(96);
        return field;
    }

    private static TextField skillNumberField(Number value) {
        TextField field = numberField(value);
        field.getStyleClass().add("skill-level-input");
        field.setMinWidth(76);
        field.setPrefWidth(76);
        field.setMaxWidth(76);
        return field;
    }

    private static ComboBox<String> currencySelect(String value) {
        ComboBox<String> select = new ComboBox<>();
        select.getItems().addAll(CURRENCY_CODES);
        select.setValue(CURRENCY_CODES.contains(value) ? value : "CURRENCY_1");
        select.getStyleClass().add("sheet-input");
        select.getStyleClass().add("sheet-select");
        return select;
    }

    private static int intValue(TextField field, int fallback, int min) {
        try {
            return Math.max(min, Integer.parseInt(field.getText().trim()));
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static BigDecimal decimalValue(TextField field, BigDecimal fallback) {
        try {
            BigDecimal value = new BigDecimal(field.getText().trim().replace(',', '.'));
            return value.signum() < 0 ? BigDecimal.ZERO : value;
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static Optional<BigDecimal> parseNonNegativeDecimal(String value) {
        try {
            BigDecimal parsed = new BigDecimal(value.trim().replace(',', '.'));
            return Optional.of(parsed.signum() < 0 ? BigDecimal.ZERO : parsed);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static BigDecimal displayAmountForCurrency(BigDecimal amountBase, String displayCurrency) {
        BigDecimal rate = currencyRate(displayCurrency);
        if (rate.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return amountBase.divideToIntegralValue(rate);
    }

    private static BigDecimal baseAmountFromDisplay(BigDecimal displayAmount, String displayCurrency) {
        return displayAmount.multiply(currencyRate(displayCurrency)).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal currencyRate(String displayCurrency) {
        return switch (displayCurrency) {
            case "CURRENCY_2" -> BigDecimal.TEN;
            case "CURRENCY_3" -> new BigDecimal("100");
            case "CURRENCY_4" -> new BigDecimal("1000");
            default -> BigDecimal.ONE;
        };
    }

    private interface AssetSyncActions {
        void addInventoryRow();

        void removeInventoryRow();

        void moveInventoryItem(int fromSlotIndex, int toSlotIndex);

        void createItem(LocalCharacterSheet.InventoryItem item);

        void createItemAtSlot(LocalCharacterSheet.InventoryItem item, int slotIndex);

        void updateItem(LocalCharacterSheet.InventoryItem item);

        void throwAwayItem(String itemId);

        void sellItem(String itemId);

        void equipItem(String itemId, String slotCode);

        void unequipItem(String slotCode);

        void createSpell(LocalCharacterSheet.SpellPreview spell);

        void updateSpell(LocalCharacterSheet.SpellPreview spell);

        void deleteSpell(String spellId);
    }

    private static final class SheetForm {
        private final LocalCharacterSheet original;
        private final AssetSyncActions assetActions;
        private final Window dialogOwner;
        private final boolean editing;
        private final boolean assetEditing;
        private final VBox root = new VBox(18);
        private final List<SkillEditor> skillEditors = new ArrayList<>();
        private final TextField name;
        private final TextField image;
        private final TextField level;
        private final TextField origin;
        private final TextField background;
        private final TextField className;
        private final TextField specialization;
        private final TextField strength;
        private final TextField dexterity;
        private final TextField stamina;
        private final TextField intelligence;
        private final TextField charisma;
        private final TextField luck;
        private final TextField mind;
        private final TextField passiveDefense;
        private final TextField movementSpeed;
        private final TextField maxCarryWeight;
        private final TextField hpHeadCurrent;
        private final TextField hpHeadMax;
        private final TextField hpNeckCurrent;
        private final TextField hpNeckMax;
        private final TextField hpTorsoCurrent;
        private final TextField hpTorsoMax;
        private final TextField hpLeftArmCurrent;
        private final TextField hpLeftArmMax;
        private final TextField hpRightArmCurrent;
        private final TextField hpRightArmMax;
        private final TextField hpLeftLegCurrent;
        private final TextField hpLeftLegMax;
        private final TextField hpRightLegCurrent;
        private final TextField hpRightLegMax;
        private final TextField blessings;
        private final TextField inspirations;
        private final TextField moneyAmount;
        private final ComboBox<String> moneyCurrency;
        private BigDecimal moneyAmountBase;
        private boolean updatingMoneyDisplay;
        private final List<LocalCharacterSheet.InventoryItem> inventoryItems;
        private final List<LocalCharacterSheet.InventorySlot> inventorySlots;
        private final List<LocalCharacterSheet.EquipmentSlot> equipmentSlots;
        private final VBox inventoryContent = new VBox(14);
        private final VBox equipmentContent = new VBox(14);
        private final List<LocalCharacterSheet.SpellPreview> spells;
        private final VBox spellsContent = new VBox(12);
        private String selectedItemId = "";
        private String selectedSpellId = "";
        private Integer draggedSlotIndex;
        private final TextArea appearance;
        private final TextArea detailedOrigin;
        private final TextArea allies;
        private final TextArea notesPrimary;
        private final TextArea notesSecondary;

        private SheetForm(LocalCharacterSheet sheet, boolean editing, Window dialogOwner) {
            this(sheet, editing, true, null, dialogOwner);
        }

        private SheetForm(LocalCharacterSheet sheet, boolean editing, boolean assetEditing) {
            this(sheet, editing, assetEditing, null, null);
        }

        private SheetForm(
                LocalCharacterSheet sheet,
                boolean editing,
                boolean assetEditing,
                AssetSyncActions assetActions,
                Window dialogOwner
        ) {
            this.original = sheet;
            this.assetActions = assetActions;
            this.dialogOwner = dialogOwner;
            this.editing = editing;
            this.assetEditing = assetEditing;
            this.name = textField(sheet.name());
            this.image = textField(sheet.image());
            this.level = numberField(sheet.info().level());
            this.origin = textField(sheet.info().origin());
            this.background = textField(sheet.info().background());
            this.className = textField(sheet.info().className());
            this.specialization = textField(sheet.info().specialization());
            this.strength = numberField(sheet.stats().strength());
            this.dexterity = numberField(sheet.stats().dexterity());
            this.stamina = numberField(sheet.stats().stamina());
            this.intelligence = numberField(sheet.stats().intelligence());
            this.charisma = numberField(sheet.stats().charisma());
            this.luck = numberField(sheet.stats().luck());
            this.mind = numberField(sheet.stats().mind());
            this.passiveDefense = numberField(sheet.condition().passiveDefense());
            this.movementSpeed = numberField(sheet.condition().movementSpeed());
            this.maxCarryWeight = numberField(sheet.condition().maxCarryWeight());
            this.hpHeadCurrent = numberField(sheet.condition().hp().head().current());
            this.hpHeadMax = numberField(sheet.condition().hp().head().max());
            this.hpNeckCurrent = numberField(sheet.condition().hp().neck().current());
            this.hpNeckMax = numberField(sheet.condition().hp().neck().max());
            this.hpTorsoCurrent = numberField(sheet.condition().hp().torso().current());
            this.hpTorsoMax = numberField(sheet.condition().hp().torso().max());
            this.hpLeftArmCurrent = numberField(sheet.condition().hp().leftArm().current());
            this.hpLeftArmMax = numberField(sheet.condition().hp().leftArm().max());
            this.hpRightArmCurrent = numberField(sheet.condition().hp().rightArm().current());
            this.hpRightArmMax = numberField(sheet.condition().hp().rightArm().max());
            this.hpLeftLegCurrent = numberField(sheet.condition().hp().leftLeg().current());
            this.hpLeftLegMax = numberField(sheet.condition().hp().leftLeg().max());
            this.hpRightLegCurrent = numberField(sheet.condition().hp().rightLeg().current());
            this.hpRightLegMax = numberField(sheet.condition().hp().rightLeg().max());
            this.blessings = numberField(sheet.blessings().blessings());
            this.inspirations = numberField(sheet.blessings().inspirations());
            this.moneyAmountBase = sheet.money().amountBase();
            this.moneyAmount = numberField(displayAmountForCurrency(sheet.money().amountBase(), sheet.money().displayCurrency()));
            this.moneyCurrency = currencySelect(sheet.money().displayCurrency());
            installMoneySync();
            this.inventoryItems = new ArrayList<>(sheet.inventory().items());
            this.inventorySlots = new ArrayList<>(sheet.inventory().slots());
            this.inventorySlots.sort(Comparator.comparingInt(LocalCharacterSheet.InventorySlot::index));
            this.equipmentSlots = new ArrayList<>(sheet.equipment());
            this.spells = new ArrayList<>(sheet.spells());
            this.appearance = textArea(sheet.additionalInfo().appearance());
            this.detailedOrigin = textArea(sheet.additionalInfo().detailedOrigin());
            this.allies = textArea(sheet.additionalInfo().allies());
            this.notesPrimary = textArea(sheet.additionalInfo().notesPrimary());
            this.notesSecondary = textArea(sheet.additionalInfo().notesSecondary());

            root.getStyleClass().add("sheet-form");
            root.getChildren().addAll(
                    assetEditing ? guestNotice() : accountNotice(),
                    portraitSection(sheet, editing),
                    identitySection(sheet, editing),
                    metricsSection(sheet, editing),
                    upperSheetGrid(sheet, editing, assetEditing),
                    previewSection(sheet, editing && assetEditing),
                    notesSection(sheet, editing)
            );
        }

        private VBox root() {
            return root;
        }

        private void installMoneySync() {
            moneyAmount.textProperty().addListener((observable, previousValue, nextValue) -> {
                if (updatingMoneyDisplay || nextValue == null || nextValue.isBlank()) {
                    return;
                }
                parseNonNegativeDecimal(nextValue).ifPresent(value -> {
                    moneyAmountBase = baseAmountFromDisplay(value, moneyCurrency.getValue());
                });
            });
            moneyCurrency.valueProperty().addListener((observable, previousCurrency, selectedCurrency) -> {
                if (previousCurrency == null || selectedCurrency == null || previousCurrency.equals(selectedCurrency)) {
                    return;
                }
                renderMoneyAmount(selectedCurrency);
            });
        }

        private void renderMoneyAmount(String displayCurrency) {
            updatingMoneyDisplay = true;
            try {
                moneyAmount.setText(displayAmountForCurrency(moneyAmountBase, displayCurrency).stripTrailingZeros().toPlainString());
            } finally {
                updatingMoneyDisplay = false;
            }
        }

        private LocalCharacterSheet toSheet() {
            return new LocalCharacterSheet(
                    original.id(),
                    name.getText(),
                    image.getText(),
                    new LocalCharacterSheet.Info(
                            intValue(level, original.info().level(), 1),
                            origin.getText(),
                            background.getText(),
                            className.getText(),
                            specialization.getText()
                    ),
                    new LocalCharacterSheet.Stats(
                            intValue(strength, original.stats().strength(), 0),
                            intValue(dexterity, original.stats().dexterity(), 0),
                            intValue(stamina, original.stats().stamina(), 0),
                            intValue(intelligence, original.stats().intelligence(), 0),
                            intValue(charisma, original.stats().charisma(), 0),
                            intValue(luck, original.stats().luck(), 0),
                            intValue(mind, original.stats().mind(), 0)
                    ),
                    skillEditors.stream()
                            .map(editor -> new LocalCharacterSheet.Skill(
                                    editor.skill().stat(),
                                    editor.skill().name(),
                                    intValue(editor.level(), editor.skill().level(), 0)
                            ))
                            .toList(),
                    new LocalCharacterSheet.Condition(
                            new LocalCharacterSheet.BodyHealth(
                                    bodyPart(hpHeadCurrent, hpHeadMax, original.condition().hp().head()),
                                    bodyPart(hpNeckCurrent, hpNeckMax, original.condition().hp().neck()),
                                    bodyPart(hpTorsoCurrent, hpTorsoMax, original.condition().hp().torso()),
                                    bodyPart(hpLeftArmCurrent, hpLeftArmMax, original.condition().hp().leftArm()),
                                    bodyPart(hpRightArmCurrent, hpRightArmMax, original.condition().hp().rightArm()),
                                    bodyPart(hpLeftLegCurrent, hpLeftLegMax, original.condition().hp().leftLeg()),
                                    bodyPart(hpRightLegCurrent, hpRightLegMax, original.condition().hp().rightLeg())
                            ),
                            intValue(passiveDefense, original.condition().passiveDefense(), 0),
                            decimalValue(movementSpeed, original.condition().movementSpeed()),
                            decimalValue(maxCarryWeight, original.condition().maxCarryWeight())
                    ),
                    new LocalCharacterSheet.Blessings(
                            intValue(blessings, original.blessings().blessings(), 0),
                            intValue(inspirations, original.blessings().inspirations(), 0)
                    ),
                    new LocalCharacterSheet.Money(
                            moneyAmountBase,
                            moneyCurrency.getValue()
                    ),
                    new LocalCharacterSheet.Inventory(inventoryItems, inventorySlots),
                    List.copyOf(equipmentSlots),
                    List.copyOf(spells),
                    new LocalCharacterSheet.AdditionalInfo(
                            appearance.getText(),
                            detailedOrigin.getText(),
                            allies.getText(),
                            notesPrimary.getText(),
                            notesSecondary.getText()
                    )
            );
        }

        private String originalId() {
            return original.id();
        }

        private LocalCharacterSheet originalSheet() {
            return original;
        }

        private Double currentScroll() {
            return null;
        }

        private void applyServerSheet(LocalCharacterSheet sheet) {
            moneyAmountBase = sheet.money().amountBase();
            String displayCurrency = sheet.money().displayCurrency().isBlank() ? "CURRENCY_1" : sheet.money().displayCurrency();
            if (!moneyCurrency.getItems().contains(displayCurrency)) {
                moneyCurrency.getItems().add(displayCurrency);
            }
            updatingMoneyDisplay = true;
            try {
                moneyCurrency.setValue(displayCurrency);
                moneyAmount.setText(displayAmountForCurrency(moneyAmountBase, displayCurrency)
                        .stripTrailingZeros()
                        .toPlainString());
            } finally {
                updatingMoneyDisplay = false;
            }

            inventoryItems.clear();
            inventoryItems.addAll(sheet.inventory().items());
            inventorySlots.clear();
            inventorySlots.addAll(sheet.inventory().slots());
            inventorySlots.sort(Comparator.comparingInt(LocalCharacterSheet.InventorySlot::index));
            equipmentSlots.clear();
            equipmentSlots.addAll(sheet.equipment());
            spells.clear();
            spells.addAll(sheet.spells());

            if (selectedItem() == null) {
                selectedItemId = "";
            }
            if (selectedSpell() == null) {
                selectedSpellId = "";
            }

            boolean editableAssets = editing && assetEditing;
            refreshInventory(editableAssets);
            refreshEquipment(editableAssets);
            refreshSpells(editableAssets);
        }

        private static LocalCharacterSheet.BodyPartHealth bodyPart(
                TextField current,
                TextField max,
                LocalCharacterSheet.BodyPartHealth original
        ) {
            return new LocalCharacterSheet.BodyPartHealth(
                    intValue(current, original.current(), 0),
                    intValue(max, original.max(), 0)
            );
        }

        private Parent guestNotice() {
            HBox notice = new HBox(14, eyebrow("GUEST MODE"), body("Local character sheet. Export JSON when you need a portable copy."));
            notice.getStyleClass().add("guest-notice");
            notice.setAlignment(Pos.CENTER_LEFT);
            return notice;
        }

        private Parent accountNotice() {
            HBox notice = new HBox(14, eyebrow("ACCOUNT MODE"),
                    body("Server character sheet. Inventory, equipment, and spells sync with your account."));
            notice.getStyleClass().add("guest-notice");
            notice.setAlignment(Pos.CENTER_LEFT);
            return notice;
        }

        private Parent portraitSection(LocalCharacterSheet sheet, boolean editing) {
            HBox panel = new HBox(24);
            panel.getStyleClass().add("portrait-panel");
            panel.setAlignment(Pos.CENTER_LEFT);
            VBox copy = new VBox(10,
                    eyebrow("CHARACTER PORTRAIT"),
                    cardTitle(sheet.image().isBlank() ? "No portrait yet" : "Portrait linked"),
                    editing ? image : body(sheet.image().isBlank() ? "Add an image URL while editing the sheet." : sheet.image())
            );
            copy.getStyleClass().add("portrait-copy");
            panel.getChildren().addAll(portraitNode(sheet), copy);
            return panel;
        }

        private Parent identitySection(LocalCharacterSheet sheet, boolean editing) {
            GridPane grid = sectionGrid("IDENTITY");
            addField(grid, 0, "Name", editing ? name : value(sheet.name()));
            addField(grid, 1, "Origin", editing ? origin : value(sheet.info().origin()));
            addField(grid, 2, "Background", editing ? background : value(sheet.info().background()));
            addField(grid, 3, "Class", editing ? className : value(sheet.info().className()));
            addField(grid, 4, "Specialization", editing ? specialization : value(sheet.info().specialization()));
            return section("Character info", grid);
        }

        private Parent metricsSection(LocalCharacterSheet sheet, boolean editing) {
            FlowPane grid = new FlowPane(8, 8);
            grid.getStyleClass().add("metric-grid");
            int health = globalHealth(sheet.condition().hp());
            grid.getChildren().addAll(
                    framedMetric("Level", editing ? level : value(Integer.toString(sheet.info().level())), "level-metric"),
                    illustratedMetric("Defense", editing ? passiveDefense : value(Integer.toString(sheet.condition().passiveDefense())),
                            "metric-defense", "defence"),
                    illustratedMetric("Dodge", value(Integer.toString(passiveDodge(sheet.stats().dexterity()))),
                            "derived-metric metric-dodge", "dodge"),
                    illustratedMetric("Blessings", editing ? blessings : value(Integer.toString(sheet.blessings().blessings())),
                            "metric-blessings", "blessings"),
                    illustratedMetric("Inspirations", editing ? inspirations : value(Integer.toString(sheet.blessings().inspirations())),
                            "metric-inspirations", "inspirations"),
                    illustratedMetric("Movement", editing ? movementSpeed : value(sheet.condition().movementSpeed().toPlainString()),
                            "metric-movement", "movement"),
                    illustratedMetric("Health", value(health + "%"),
                            health <= 30 ? "derived-metric metric-health danger" : "derived-metric metric-health", "health"),
                    framedMetric("Money", moneyMetric(sheet, editing), "")
            );
            return grid;
        }

        private Parent moneyMetric(LocalCharacterSheet sheet, boolean editing) {
            if (editing) {
                VBox editor = new VBox(6, moneyAmount, moneyCurrency);
                editor.getStyleClass().add("money-editor");
                return editor;
            }
            VBox value = new VBox(4,
                    value(displayAmountForCurrency(sheet.money().amountBase(), sheet.money().displayCurrency()).toPlainString()),
                    body(label(sheet.money().displayCurrency()))
            );
            value.getStyleClass().add("money-value");
            return value;
        }

        private Parent upperSheetGrid(LocalCharacterSheet sheet, boolean editing, boolean assetEditing) {
            FlowPane grid = new FlowPane(14, 14, statsSection(sheet, editing), sideStack(sheet, editing, assetEditing));
            grid.getStyleClass().add("sheet-upper-grid");
            return grid;
        }

        private Parent statsSection(LocalCharacterSheet sheet, boolean editing) {
            VBox stack = new VBox(10);
            stack.getStyleClass().add("stat-stack");
            stack.getChildren().addAll(
                    statBlock("Strength", sheet.stats().strength(), strength, skillsFor(sheet, "STRENGTH"), editing),
                    statBlock("Dexterity", sheet.stats().dexterity(), dexterity, skillsFor(sheet, "DEXTERITY"), editing),
                    statBlock("Stamina", sheet.stats().stamina(), stamina, skillsFor(sheet, "STAMINA"), editing),
                    statBlock("Intelligence", sheet.stats().intelligence(), intelligence, skillsFor(sheet, "INTELLIGENCE"), editing),
                    statBlock("Charisma", sheet.stats().charisma(), charisma, skillsFor(sheet, "CHARISMA"), editing),
                    statBlock("Luck", sheet.stats().luck(), luck, skillsFor(sheet, "LUCK"), editing),
                    statBlock("Mind", sheet.stats().mind(), mind, skillsFor(sheet, "MIND"), editing)
            );
            return section("Stats & skills", stack);
        }

        private Parent sideStack(LocalCharacterSheet sheet, boolean editing, boolean assetEditing) {
            Parent equipment = equipmentPreview(editing && assetEditing);
            equipment.getStyleClass().add("equipment-section");
            Parent condition = conditionSection(sheet, editing);
            condition.getStyleClass().add("condition-section");
            FlowPane stack = new FlowPane(14, 14, equipment, condition);
            stack.getStyleClass().add("sheet-side-stack");
            return stack;
        }

        private Parent conditionSection(LocalCharacterSheet sheet, boolean editing) {
            int health = globalHealth(sheet.condition().hp());
            VBox list = new VBox(6);
            list.getStyleClass().add("health-list");
            Label summary = eyebrow(health + "% GLOBAL HEALTH");
            summary.getStyleClass().add("condition-health-summary");
            list.getChildren().addAll(
                    summary,
                    bodyPartRow("Head", sheet.condition().hp().head(), hpHeadCurrent, hpHeadMax, editing),
                    bodyPartRow("Neck", sheet.condition().hp().neck(), hpNeckCurrent, hpNeckMax, editing),
                    bodyPartRow("Torso", sheet.condition().hp().torso(), hpTorsoCurrent, hpTorsoMax, editing),
                    bodyPartRow("Left arm", sheet.condition().hp().leftArm(), hpLeftArmCurrent, hpLeftArmMax, editing),
                    bodyPartRow("Right arm", sheet.condition().hp().rightArm(), hpRightArmCurrent, hpRightArmMax, editing),
                    bodyPartRow("Left leg", sheet.condition().hp().leftLeg(), hpLeftLegCurrent, hpLeftLegMax, editing),
                    bodyPartRow("Right leg", sheet.condition().hp().rightLeg(), hpRightLegCurrent, hpRightLegMax, editing)
            );
            return section("Condition", list);
        }

        private Parent previewSection(LocalCharacterSheet sheet, boolean editing) {
            VBox previews = new VBox(14, inventorySection(editing), spellsSection(editing));
            previews.getStyleClass().add("sheet-preview-column");
            return previews;
        }

        private Parent notesSection(LocalCharacterSheet sheet, boolean editing) {
            GridPane grid = sectionGrid("NOTES");
            addField(grid, 0, "Appearance", editing ? appearance : value(sheet.additionalInfo().appearance()));
            addField(grid, 1, "Detailed origin", editing ? detailedOrigin : value(sheet.additionalInfo().detailedOrigin()));
            addField(grid, 2, "Allies", editing ? allies : value(sheet.additionalInfo().allies()));
            addField(grid, 3, "Primary notes", editing ? notesPrimary : value(sheet.additionalInfo().notesPrimary()));
            addField(grid, 4, "Secondary notes", editing ? notesSecondary : value(sheet.additionalInfo().notesSecondary()));
            return section("Additional info", grid);
        }

        private Parent portraitNode(LocalCharacterSheet sheet) {
            StackPane frame = new StackPane();
            frame.getStyleClass().add("sheet-portrait-frame");
            if (!sheet.image().isBlank()) {
                ImageView imageView = new ImageView(cachedImage(sheet.image()));
                imageView.getStyleClass().add("sheet-portrait");
                imageView.setFitWidth(178);
                imageView.setFitHeight(230);
                imageView.setPreserveRatio(false);
                frame.getChildren().add(imageView);
                return frame;
            }
            frame.getChildren().add(new Label(initial(sheet.name())));
            frame.getStyleClass().add("sheet-portrait-placeholder");
            return frame;
        }

        private static Parent metric(String label, Parent valueNode, String styleClass) {
            VBox metric = new VBox(8, eyebrow(label.toUpperCase()), valueNode);
            metric.getStyleClass().add("metric");
            if (!styleClass.isBlank()) {
                metric.getStyleClass().addAll(styleClass.split("\\s+"));
            }
            return metric;
        }

        private static Parent framedMetric(String label, Parent valueNode, String styleClass) {
            StackPane metric = new StackPane();
            metric.getStyleClass().add("metric");
            if (!styleClass.isBlank()) {
                metric.getStyleClass().addAll(styleClass.split("\\s+"));
            }
            VBox content = new VBox(8, eyebrow(label.toUpperCase()), valueNode);
            content.getStyleClass().add("metric-content");
            content.getStyleClass().add("metric-content-plain");
            content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            metric.getChildren().add(content);
            return metric;
        }

        private static Parent illustratedMetric(String label, Parent valueNode, String styleClass, String imageName) {
            StackPane metric = new StackPane();
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(metric.widthProperty());
            clip.heightProperty().bind(metric.heightProperty());
            metric.setClip(clip);
            metric.getStyleClass().addAll("metric", "metric-illustrated");
            if (!styleClass.isBlank()) {
                metric.getStyleClass().addAll(styleClass.split("\\s+"));
            }
            metricImage(imageName).ifPresent(image -> {
                image.getStyleClass().add("metric-illustration");
                metric.getChildren().add(image);
            });
            VBox content = new VBox(8, eyebrow(label.toUpperCase()), valueNode);
            content.getStyleClass().add("metric-content");
            content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            metric.getChildren().add(content);
            return metric;
        }

        private static Optional<ImageView> metricImage(String name) {
            var resource = DesktopViews.class.getResource("/com/pfp/desktop/images/sheet/" + name + ".png");
            if (resource == null) {
                return Optional.empty();
            }
            ImageView image = new ImageView(cachedImage(resource.toExternalForm()));
            image.setPreserveRatio(true);
            image.setSmooth(true);
            image.setMouseTransparent(true);
            image.setOpacity(0.22);
            image.setFitHeight(metricImageHeight(name));
            ColorAdjust color = new ColorAdjust();
            color.setBrightness(0.82);
            color.setContrast(0.18);
            image.setEffect(color);
            StackPane.setAlignment(image, Pos.CENTER_RIGHT);
            return Optional.of(image);
        }

        private static double metricImageHeight(String name) {
            return switch (name) {
                case "health" -> 118;
                case "defence" -> 104;
                case "dodge", "blessings", "inspirations" -> 96;
                case "movement" -> 88;
                default -> 112;
            };
        }

        private Parent statBlock(
                String title,
                int levelValue,
                TextField levelField,
                List<LocalCharacterSheet.Skill> skills,
                boolean editing
        ) {
            VBox block = new VBox(8);
            block.getStyleClass().add("stat-block");
            Parent levelNode = editing ? levelField : compactValue(Integer.toString(levelValue), "stat-level-value");
            levelNode.getStyleClass().add("stat-level-control");
            HBox header = new HBox(10, statTitle(title), levelNode, compactValue(rollForLevel(levelValue), "stat-roll-value"));
            header.getStyleClass().add("stat-header");
            block.getChildren().add(header);
            for (LocalCharacterSheet.Skill skill : skills) {
                TextField skillLevel = skillNumberField(skill.level());
                skillEditors.add(new SkillEditor(skill, skillLevel));
                Parent skillLevelNode = editing ? skillLevel : compactValue(Integer.toString(skill.level()), "skill-level-value");
                skillLevelNode.getStyleClass().add("skill-level-control");
                HBox row = new HBox(8,
                        compactValue(label(skill.name()), "skill-name-value"),
                        skillLevelNode,
                        compactValue(rollForLevel(skill.level()), "skill-roll-value"),
                        compactValue(effectiveSkillRoll(levelValue, skill.level()), "skill-roll-value"));
                row.getStyleClass().add("skill-row");
                block.getChildren().add(row);
            }
            return block;
        }

        private static Label statTitle(String title) {
            Label label = eyebrow(title.toUpperCase());
            label.getStyleClass().add("stat-title");
            return label;
        }

        private static Label compactValue(String text, String styleClass) {
            Label label = value(text);
            label.getStyleClass().add(styleClass);
            label.setWrapText(false);
            return label;
        }

        private static List<LocalCharacterSheet.Skill> skillsFor(LocalCharacterSheet sheet, String stat) {
            return sheet.skills().stream()
                    .filter(skill -> skill.stat().equals(stat))
                    .toList();
        }

        private Parent bodyPartRow(
                String label,
                LocalCharacterSheet.BodyPartHealth part,
                TextField current,
                TextField max,
                boolean editing
        ) {
            Label title = body(label);
            title.getStyleClass().add("health-part-label");
            HBox row = new HBox(8);
            row.getStyleClass().add("health-row");
            if (part.current() == 0) {
                row.getStyleClass().add("health-row-danger");
            }
            row.setAlignment(Pos.CENTER_LEFT);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            if (editing) {
                current.getStyleClass().add("health-input");
                max.getStyleClass().add("health-input");
                row.getChildren().addAll(title, spacer, current, max);
            } else {
                Label currentValue = value(Integer.toString(part.current()));
                currentValue.getStyleClass().add("health-current");
                Label maxValue = body("/ " + part.max());
                maxValue.getStyleClass().add("health-max");
                row.getChildren().addAll(title, spacer, currentValue, maxValue);
            }
            return row;
        }

        private Parent equipmentPreview(boolean editing) {
            equipmentContent.getStyleClass().add("equipment-groups");
            refreshEquipment(editing);
            return section("Equipment", equipmentContent);
        }

        private void refreshEquipment(boolean editing) {
            equipmentContent.getChildren().clear();
            equipmentContent.getChildren().addAll(
                    equipmentGroup("Armor", List.of("HEAD", "NECK", "TORSO", "ARMS", "LEGS"), editing),
                    equipmentGroup("Weapons", List.of("WEAPON_1", "WEAPON_2"), editing),
                    equipmentGroup("Trinkets", List.of("TALISMAN_1", "TALISMAN_2", "TALISMAN_3", "TALISMAN_4"), editing)
            );
        }

        private Parent equipmentGroup(String title, List<String> slots, boolean editing) {
            Label heading = eyebrow(title.toUpperCase());
            heading.getStyleClass().add("equipment-group-title");
            FlowPane slotGrid = new FlowPane(7, 7);
            slotGrid.getStyleClass().add("equipment-grid");
            for (String slot : slots) {
                slotGrid.getChildren().add(equipmentSlot(slot, editing));
            }
            VBox group = new VBox(8, heading, slotGrid);
            group.getStyleClass().add("equipment-group");
            return group;
        }

        private Parent equipmentSlot(String code, boolean editing) {
            String itemId = equipmentSlots.stream()
                    .filter(slot -> slot.code().equals(code))
                    .map(LocalCharacterSheet.EquipmentSlot::itemId)
                    .findFirst()
                    .orElse("");
            Optional<LocalCharacterSheet.InventoryItem> item = itemById(itemId);
            String titleText = item.map(LocalCharacterSheet.InventoryItem::title).orElse(itemId);
            VBox slot = new VBox(6);
            slot.getStyleClass().add(itemId.isBlank() ? "equipment-slot" : "equipment-slot-occupied");
            slot.setAlignment(Pos.CENTER);

            StackPane icon = item
                    .map(this::equipmentItemIcon)
                    .orElseGet(() -> new StackPane(new Label(initial(label(code)))));
            icon.getStyleClass().add(itemId.isBlank() ? "equipment-slot-icon" : "equipment-slot-icon-occupied");
            Label title = body(itemId.isBlank() ? label(code) : titleText);
            title.getStyleClass().add("equipment-slot-label");
            title.setWrapText(false);
            title.setTextOverrun(OverrunStyle.ELLIPSIS);
            slot.getChildren().addAll(icon, title);
            if (editing && !itemId.isBlank()) {
                Button unequip = secondaryButton("Unequip", soundForEquipment(code), () -> {
                    if (assetActions != null) {
                        assetActions.unequipItem(code);
                    } else {
                        unequipItem(code);
                        refreshEquipment(true);
                    }
                });
                unequip.getStyleClass().add("equipment-unequip-button");
                slot.getChildren().add(unequip);
            }
            return slot;
        }

        private Parent inventorySection(boolean editing) {
            inventoryContent.getStyleClass().add("inventory-content");
            refreshInventory(editing);
            return section("Inventory", inventoryContent);
        }

        private void refreshInventory(boolean editing) {
            inventoryContent.getChildren().clear();
            BigDecimal currentWeight = currentInventoryWeight();
            boolean overweight = currentWeight.compareTo(decimalValue(maxCarryWeight, original.condition().maxCarryWeight())) > 0;
            Label weightLabel = body("Weight: " + currentWeight.stripTrailingZeros().toPlainString() + " /");
            weightLabel.getStyleClass().add(overweight ? "inventory-weight-danger" : "inventory-weight");
            HBox weight = new HBox(8, weightLabel, editing ? maxCarryWeight : value(maxCarryWeight.getText()));
            weight.getStyleClass().add("inventory-weight-row");
            weight.setAlignment(Pos.CENTER_RIGHT);

            FlowPane grid = new FlowPane(6, 6);
            grid.getStyleClass().add("inventory-grid");
            inventorySlots.stream()
                    .sorted(Comparator.comparingInt(LocalCharacterSheet.InventorySlot::index))
                    .forEach(slot -> grid.getChildren().add(inventorySlot(slot, editing)));

            inventoryContent.getChildren().addAll(weight, grid);
            LocalCharacterSheet.InventoryItem selected = selectedItem();
            if (selected != null) {
                inventoryContent.getChildren().add(itemDetails(selected, editing));
            }
            if (editing) {
                Button addRow = secondaryButton("+ Add row", () -> {
                    if (assetActions != null) {
                        assetActions.addInventoryRow();
                    } else {
                        addInventoryRow();
                        refreshInventory(true);
                    }
                });
                Button removeRow = secondaryButton("- Remove row", () -> {
                    if (assetActions != null) {
                        assetActions.removeInventoryRow();
                    } else {
                        removeInventoryRow();
                        refreshInventory(true);
                    }
                });
                removeRow.setDisable(!canRemoveInventoryRow());
                Button addItem = primaryButton("Add item", SoundEffect.ADD_ITEM, () -> showAddItemDialog().ifPresent(item -> {
                    if (assetActions != null) {
                        assetActions.createItem(item);
                    } else {
                        addInventoryItem(item);
                        selectedItemId = item.id();
                        refreshInventory(true);
                    }
                }));
                HBox actions = new HBox(12, addRow, removeRow, addItem);
                actions.getStyleClass().add("inventory-actions");
                inventoryContent.getChildren().add(actions);
            }
        }

        private Parent inventorySlot(LocalCharacterSheet.InventorySlot slot, boolean editing) {
            Optional<LocalCharacterSheet.InventoryItem> item = itemById(slot.itemId());
            VBox button = new VBox(4);
            button.getStyleClass().add("inventory-slot");
            if (item.isPresent() || editing) {
                button.getStyleClass().add("interactive");
            }
            if (item.isPresent()) {
                button.getStyleClass().add("occupied");
            } else if (editing) {
                button.getStyleClass().add("empty");
            }
            double slotSize = inventorySlotSize();
            button.setMinSize(slotSize, slotSize);
            button.setPrefSize(slotSize, slotSize);
            button.setMaxSize(slotSize, slotSize);
            if (item.isPresent() && item.get().id().equals(selectedItemId)) {
                button.getStyleClass().add("selected");
            }
            button.setAlignment(Pos.CENTER);
            button.setOnMouseClicked(event -> {
                if (item.isPresent()) {
                    DesktopAudio.play(SoundEffect.ITEM_PICK);
                    selectedItemId = item.get().id();
                    refreshInventory(editing);
                } else if (editing) {
                    DesktopAudio.play(SoundEffect.ADD_ITEM);
                    showAddItemDialog().ifPresent(created -> {
                        if (assetActions != null) {
                            assetActions.createItemAtSlot(created, slot.index());
                        } else {
                            addInventoryItem(created, slot.index());
                            selectedItemId = created.id();
                            refreshInventory(true);
                        }
                    });
                }
            });
            if (editing && item.isPresent()) {
                button.setOnDragDetected(event -> {
                    draggedSlotIndex = slot.index();
                    button.getStyleClass().add("drag-source");
                    button.startFullDrag();
                    event.consume();
                });
            }
            if (editing) {
                button.setOnMouseDragEntered(event -> {
                    if (draggedSlotIndex != null && draggedSlotIndex != slot.index()) {
                        button.getStyleClass().add("drag-target");
                    }
                    event.consume();
                });
                button.setOnMouseDragExited(event -> {
                    button.getStyleClass().remove("drag-target");
                    event.consume();
                });
                button.setOnMouseDragReleased(event -> {
                    if (draggedSlotIndex != null) {
                        if (assetActions != null) {
                            assetActions.moveInventoryItem(draggedSlotIndex, slot.index());
                        } else {
                            moveInventoryItem(draggedSlotIndex, slot.index());
                            refreshInventory(true);
                        }
                        if (!draggedSlotIndex.equals(slot.index())) {
                            DesktopAudio.play(SoundEffect.INVENTORY_MOVE);
                        }
                        draggedSlotIndex = null;
                    }
                    event.consume();
                });
            }
            if (item.isPresent()) {
                StackPane icon = itemIcon(item.get());
                Label title = body(item.get().title());
                title.getStyleClass().add("inventory-slot-title");
                title.setWrapText(false);
                title.setTextOverrun(OverrunStyle.ELLIPSIS);
                button.getChildren().addAll(icon, title);
            } else {
                Label empty = new Label("+");
                empty.getStyleClass().add("inventory-empty-marker");
                button.getChildren().add(empty);
            }
            return button;
        }

        private Parent itemDetails(LocalCharacterSheet.InventoryItem item, boolean editing) {
            VBox details = new VBox(12);
            details.getStyleClass().add("item-actions-panel");
            HBox heading = new HBox(12, itemIcon(item), new VBox(4, eyebrow(label(item.type()).toUpperCase()), cardTitle(item.title())));
            heading.setAlignment(Pos.CENTER_LEFT);
            details.getChildren().add(heading);
            if (editing) {
                TextField title = textField(item.title());
                TextField image = textField(item.image());
                TextArea description = textArea(item.description());
                TextField weight = numberField(item.weight());
                title.textProperty().addListener((observable, oldValue, newValue) -> updateInventoryItem(item.id(), item.withTitle(newValue)));
                image.textProperty().addListener((observable, oldValue, newValue) -> updateInventoryItem(item.id(), item.withImage(newValue)));
                description.textProperty().addListener((observable, oldValue, newValue) -> updateInventoryItem(item.id(), item.withDescription(newValue)));
                weight.textProperty().addListener((observable, oldValue, newValue) ->
                        parseNonNegativeDecimal(newValue).ifPresent(value -> updateInventoryItem(item.id(), item.withWeight(value))));
                GridPane form = sectionGrid("ITEM");
                addField(form, 0, "Title", title);
                addField(form, 1, "Image URL", image);
                addField(form, 2, "Weight", weight);
                addField(form, 3, "Description", description);
                if ("TRADE".equals(item.type())) {
                    TextField sellPrice = numberField(item.sellPriceBase());
                    sellPrice.textProperty().addListener((observable, oldValue, newValue) ->
                            parseNonNegativeDecimal(newValue).ifPresent(value -> updateInventoryItem(item.id(), item.withSellPrice(value))));
                    addField(form, 4, "Sell price", sellPrice);
                }
                details.getChildren().add(form);
                HBox actions = new HBox(10);
                if (assetActions != null) {
                    actions.getChildren().add(primaryButton("Save item", () -> {
                        LocalCharacterSheet.InventoryItem selected = selectedItem();
                        if (selected != null) {
                            assetActions.updateItem(selected);
                        }
                    }));
                }
                if ("EQUIPMENT".equals(item.type())) {
                    ComboBox<String> slotSelect = equipmentSlotSelect(item.equipmentType());
                    Button equip = primaryButton("Equip", soundForEquipment(item.equipmentType()), () -> {
                        if (assetActions != null) {
                            assetActions.equipItem(item.id(), slotSelect.getValue());
                        } else {
                            equipItem(item.id(), slotSelect.getValue());
                            refreshInventory(true);
                        }
                    });
                    actions.getChildren().addAll(slotSelect, equip);
                }
                if ("TRADE".equals(item.type())) {
                    actions.getChildren().add(primaryButton("Sell", SoundEffect.SELL_TRADE, () -> {
                        if (assetActions != null) {
                            assetActions.sellItem(item.id());
                        } else {
                            sellItem(item.id());
                            refreshInventory(true);
                        }
                    }));
                }
                actions.getChildren().addAll(
                        secondaryButton("Throw away", () -> {
                            if (confirm("Throw away this item?", "This removes the item from inventory and equipment.")) {
                                if (assetActions != null) {
                                    assetActions.throwAwayItem(item.id());
                                } else {
                                    throwAwayItem(item.id());
                                    refreshInventory(true);
                                }
                            }
                        }),
                        secondaryButton("Close", () -> {
                            selectedItemId = "";
                            refreshInventory(true);
                        })
                );
                details.getChildren().add(actions);
            } else {
                details.getChildren().addAll(
                        body(item.description().isBlank() ? "No description." : item.description()),
                        body("Weight: " + item.weight().stripTrailingZeros().toPlainString())
                );
                if ("TRADE".equals(item.type())) {
                    details.getChildren().add(body("Sell price: " + item.sellPriceBase().stripTrailingZeros().toPlainString()));
                }
                details.getChildren().add(secondaryButton("Close", () -> {
                    selectedItemId = "";
                    refreshInventory(false);
                }));
            }
            return details;
        }

        private Optional<LocalCharacterSheet.InventoryItem> showAddItemDialog() {
            ModalState<LocalCharacterSheet.InventoryItem> state = new ModalState<>();
            ComboBox<String> type = new ComboBox<>();
            type.getItems().addAll("ITEM", "EQUIPMENT", "TRADE");
            type.setValue("ITEM");
            type.getStyleClass().add("sheet-input");
            type.getStyleClass().add("sheet-select");
            TextField title = textField("");
            TextField image = textField("");
            TextField weight = numberField(BigDecimal.ZERO);
            TextField sellPrice = numberField(BigDecimal.ZERO);
            TextArea description = textArea("");
            ComboBox<String> equipmentType = equipmentTypeSelect();

            VBox content = new VBox(14);
            content.getStyleClass().add("item-dialog-content");
            VBox formHolder = new VBox(14);
            Button ok = primaryButton("OK", SoundEffect.ADD_ITEM, () -> {
                if (!title.getText().trim().isBlank()) {
                    String selectedType = type.getValue();
                    closeSceneModal(state, new LocalCharacterSheet.InventoryItem(
                            UUID.randomUUID().toString(),
                            selectedType,
                            title.getText().trim(),
                            image.getText().trim(),
                            decimalValue(weight, BigDecimal.ZERO),
                            description.getText(),
                            "EQUIPMENT".equals(selectedType) ? equipmentType.getValue() : "",
                            "TRADE".equals(selectedType) ? decimalValue(sellPrice, BigDecimal.ZERO) : BigDecimal.ZERO
                    ), true);
                }
            });
            Button cancel = modalCancel("Cancel", state);
            HBox actions = new HBox(12, ok, cancel);
            actions.setAlignment(Pos.CENTER_RIGHT);
            Runnable rebuild = () -> {
                GridPane form = sectionGrid("ITEM");
                addField(form, 0, "Type", type);
                addField(form, 1, "Title", title);
                addField(form, 2, "Image URL", image);
                addField(form, 3, "Weight", weight);
                int next = 4;
                if ("EQUIPMENT".equals(type.getValue())) {
                    addField(form, next++, "Equipment type", equipmentType);
                }
                if ("TRADE".equals(type.getValue())) {
                    addField(form, next++, "Sell price", sellPrice);
                }
                addField(form, next, "Description", description);
                formHolder.getChildren().setAll(
                        new VBox(5, eyebrow("INVENTORY"), cardTitle("Add item")),
                        form
                );
            };
            type.valueProperty().addListener((observable, previous, selected) -> rebuild.run());
            rebuild.run();
            content.getChildren().addAll(formHolder, actions);
            Platform.runLater(title::requestFocus);
            return showSceneModal(dialogOwner, state, content);
        }

        private void addInventoryRow() {
            int firstIndex = inventorySlots.stream()
                    .mapToInt(LocalCharacterSheet.InventorySlot::index)
                    .max()
                    .orElse(-1) + 1;
            for (int offset = 0; offset < 10; offset++) {
                inventorySlots.add(new LocalCharacterSheet.InventorySlot(firstIndex + offset, ""));
            }
            inventorySlots.sort(Comparator.comparingInt(LocalCharacterSheet.InventorySlot::index));
        }

        private boolean canRemoveInventoryRow() {
            if (inventorySlots.size() <= 10) {
                return false;
            }
            List<LocalCharacterSheet.InventorySlot> sorted = sortedInventorySlots();
            List<LocalCharacterSheet.InventorySlot> lastRow = sorted.subList(Math.max(0, sorted.size() - 10), sorted.size());
            return lastRow.size() == 10 && lastRow.stream().allMatch(slot -> slot.itemId().isBlank());
        }

        private void removeInventoryRow() {
            if (!canRemoveInventoryRow()) {
                return;
            }
            List<Integer> lastIndexes = sortedInventorySlots().subList(inventorySlots.size() - 10, inventorySlots.size()).stream()
                    .map(LocalCharacterSheet.InventorySlot::index)
                    .toList();
            inventorySlots.removeIf(slot -> lastIndexes.contains(slot.index()));
        }

        private void addInventoryItem(LocalCharacterSheet.InventoryItem item) {
            if (inventorySlots.stream().noneMatch(slot -> slot.itemId().isBlank())) {
                addInventoryRow();
            }
            inventoryItems.add(item);
            boolean placed = false;
            for (int index = 0; index < inventorySlots.size(); index++) {
                LocalCharacterSheet.InventorySlot slot = inventorySlots.get(index);
                if (!placed && slot.itemId().isBlank()) {
                    inventorySlots.set(index, new LocalCharacterSheet.InventorySlot(slot.index(), item.id()));
                    placed = true;
                }
            }
        }

        private void addInventoryItem(LocalCharacterSheet.InventoryItem item, int targetSlotIndex) {
            if (inventorySlots.stream().noneMatch(slot -> slot.index() == targetSlotIndex && slot.itemId().isBlank())) {
                addInventoryItem(item);
                return;
            }
            inventoryItems.add(item);
            for (int index = 0; index < inventorySlots.size(); index++) {
                LocalCharacterSheet.InventorySlot slot = inventorySlots.get(index);
                if (slot.index() == targetSlotIndex) {
                    inventorySlots.set(index, new LocalCharacterSheet.InventorySlot(slot.index(), item.id()));
                    return;
                }
            }
        }

        private void updateInventoryItem(String itemId, LocalCharacterSheet.InventoryItem nextItem) {
            for (int index = 0; index < inventoryItems.size(); index++) {
                if (inventoryItems.get(index).id().equals(itemId)) {
                    inventoryItems.set(index, nextItem);
                    if (equipmentSlots.stream().anyMatch(slot -> slot.itemId().equals(itemId))) {
                        refreshEquipment(true);
                    }
                    return;
                }
            }
        }

        private void throwAwayItem(String itemId) {
            inventoryItems.removeIf(item -> item.id().equals(itemId));
            for (int index = 0; index < inventorySlots.size(); index++) {
                LocalCharacterSheet.InventorySlot slot = inventorySlots.get(index);
                if (slot.itemId().equals(itemId)) {
                    inventorySlots.set(index, new LocalCharacterSheet.InventorySlot(slot.index(), ""));
                }
            }
            for (int index = 0; index < equipmentSlots.size(); index++) {
                LocalCharacterSheet.EquipmentSlot slot = equipmentSlots.get(index);
                if (slot.itemId().equals(itemId)) {
                    equipmentSlots.set(index, new LocalCharacterSheet.EquipmentSlot(slot.code(), ""));
                }
            }
            refreshEquipment(true);
            selectedItemId = "";
        }

        private boolean confirm(String header, String content) {
            return confirmOverlay(dialogOwner, header, content);
        }

        private void sellItem(String itemId) {
            itemById(itemId)
                    .filter(item -> "TRADE".equals(item.type()))
                    .ifPresent(item -> moneyAmountBase = moneyAmountBase.add(item.sellPriceBase()));
            renderMoneyAmount(moneyCurrency.getValue());
            throwAwayItem(itemId);
        }

        private void equipItem(String itemId, String slotCode) {
            if (slotCode == null || slotCode.isBlank()) {
                return;
            }
            for (int index = 0; index < equipmentSlots.size(); index++) {
                LocalCharacterSheet.EquipmentSlot slot = equipmentSlots.get(index);
                if (slot.itemId().equals(itemId) || slot.code().equals(slotCode)) {
                    equipmentSlots.set(index, new LocalCharacterSheet.EquipmentSlot(slot.code(), ""));
                }
            }
            boolean updated = false;
            for (int index = 0; index < equipmentSlots.size(); index++) {
                LocalCharacterSheet.EquipmentSlot slot = equipmentSlots.get(index);
                if (slot.code().equals(slotCode)) {
                    equipmentSlots.set(index, new LocalCharacterSheet.EquipmentSlot(slotCode, itemId));
                    updated = true;
                }
            }
            if (!updated) {
                equipmentSlots.add(new LocalCharacterSheet.EquipmentSlot(slotCode, itemId));
            }
            refreshEquipment(true);
        }

        private void unequipItem(String slotCode) {
            for (int index = 0; index < equipmentSlots.size(); index++) {
                LocalCharacterSheet.EquipmentSlot slot = equipmentSlots.get(index);
                if (slot.code().equals(slotCode)) {
                    equipmentSlots.set(index, new LocalCharacterSheet.EquipmentSlot(slotCode, ""));
                    return;
                }
            }
        }

        private void moveInventoryItem(int fromSlotIndex, int toSlotIndex) {
            if (fromSlotIndex == toSlotIndex) {
                return;
            }
            int fromIndex = slotListIndex(fromSlotIndex);
            int toIndex = slotListIndex(toSlotIndex);
            if (fromIndex < 0 || toIndex < 0 || inventorySlots.get(fromIndex).itemId().isBlank()) {
                return;
            }
            String fromItem = inventorySlots.get(fromIndex).itemId();
            String toItem = inventorySlots.get(toIndex).itemId();
            inventorySlots.set(fromIndex, new LocalCharacterSheet.InventorySlot(fromSlotIndex, toItem));
            inventorySlots.set(toIndex, new LocalCharacterSheet.InventorySlot(toSlotIndex, fromItem));
        }

        private int slotListIndex(int slotIndex) {
            for (int index = 0; index < inventorySlots.size(); index++) {
                if (inventorySlots.get(index).index() == slotIndex) {
                    return index;
                }
            }
            return -1;
        }

        private List<LocalCharacterSheet.InventorySlot> sortedInventorySlots() {
            return inventorySlots.stream()
                    .sorted(Comparator.comparingInt(LocalCharacterSheet.InventorySlot::index))
                    .toList();
        }

        private Optional<LocalCharacterSheet.InventoryItem> itemById(String itemId) {
            if (itemId == null || itemId.isBlank()) {
                return Optional.empty();
            }
            return inventoryItems.stream().filter(item -> item.id().equals(itemId)).findFirst();
        }

        private LocalCharacterSheet.InventoryItem selectedItem() {
            return itemById(selectedItemId).orElse(null);
        }

        private BigDecimal currentInventoryWeight() {
            BigDecimal weight = BigDecimal.ZERO;
            for (LocalCharacterSheet.InventoryItem item : inventoryItems) {
                weight = weight.add(item.weight());
            }
            return weight;
        }

        private StackPane itemIcon(LocalCharacterSheet.InventoryItem item) {
            StackPane icon = new StackPane();
            icon.getStyleClass().add("inventory-item-icon");
            double iconSize = inventoryIconSize();
            icon.setMinSize(iconSize, iconSize);
            icon.setPrefSize(iconSize, iconSize);
            icon.setMaxSize(iconSize, iconSize);
            imageView(item.image(), iconSize, iconSize).ifPresentOrElse(imageView -> {
                icon.getChildren().add(imageView);
            }, () -> icon.getChildren().add(new Label(initial(item.title()))));
            return icon;
        }

        private double inventorySlotSize() {
            if (dialogOwner != null
                    && dialogOwner.getScene() != null
                    && dialogOwner.getScene().getRoot() != null
                    && (dialogOwner.getScene().getRoot().getStyleClass().contains("tiny-layout")
                    || dialogOwner.getScene().getRoot().getStyleClass().contains("compact-layout"))) {
                return 70;
            }
            return dialogOwner != null && dialogOwner.getWidth() < 1180 ? 70 : 82;
        }

        private double inventoryIconSize() {
            return inventorySlotSize() < 80 ? 42 : 52;
        }

        private StackPane equipmentItemIcon(LocalCharacterSheet.InventoryItem item) {
            StackPane icon = new StackPane();
            imageView(item.image(), 52, 52).ifPresentOrElse(imageView -> {
                icon.getChildren().add(imageView);
            }, () -> icon.getChildren().add(new Label(initial(item.title()))));
            return icon;
        }

        private Optional<ImageView> imageView(String url, double width, double height) {
            String preparedUrl = url == null ? "" : url.trim();
            if (preparedUrl.isBlank() || "local-placeholder".equals(preparedUrl)) {
                return Optional.empty();
            }
            Image image = cachedImage(preparedUrl);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setMouseTransparent(true);
            return Optional.of(imageView);
        }

        private ComboBox<String> equipmentTypeSelect() {
            ComboBox<String> select = new ComboBox<>();
            select.getItems().addAll("HEAD", "NECK", "TORSO", "ARMS", "LEGS", "WEAPON", "TALISMAN");
            select.setValue("HEAD");
            select.getStyleClass().add("sheet-input");
            select.getStyleClass().add("sheet-select");
            return select;
        }

        private ComboBox<String> equipmentSlotSelect(String equipmentType) {
            ComboBox<String> select = new ComboBox<>();
            select.getItems().addAll(slotsForEquipmentType(equipmentType));
            if (!select.getItems().isEmpty()) {
                select.setValue(select.getItems().get(0));
            }
            select.getStyleClass().add("sheet-input");
            select.getStyleClass().add("sheet-select");
            return select;
        }

        private static List<String> slotsForEquipmentType(String equipmentType) {
            return switch (equipmentType) {
                case "WEAPON" -> List.of("WEAPON_1", "WEAPON_2");
                case "TALISMAN" -> List.of("TALISMAN_1", "TALISMAN_2", "TALISMAN_3", "TALISMAN_4");
                case "HEAD", "NECK", "TORSO", "ARMS", "LEGS" -> List.of(equipmentType);
                default -> List.of();
            };
        }

        private static SoundEffect soundForEquipment(String equipmentType) {
            String type = equipmentType == null ? "" : equipmentType;
            if (type.startsWith("WEAPON")) {
                return SoundEffect.EQUIP_WEAPON;
            }
            if (type.startsWith("TALISMAN")) {
                return SoundEffect.EQUIP_TALISMAN;
            }
            return switch (type) {
                case "WEAPON" -> SoundEffect.EQUIP_WEAPON;
                case "TALISMAN" -> SoundEffect.EQUIP_TALISMAN;
                default -> SoundEffect.EQUIP_ARMOR;
            };
        }

        private Parent spellsSection(boolean editing) {
            spellsContent.getStyleClass().add("spells-content");
            refreshSpells(editing);
            return section("Spells", spellsContent);
        }

        private void refreshSpells(boolean editing) {
            spellsContent.getChildren().clear();
            if (spells.isEmpty()) {
                spellsContent.getChildren().add(body("No spells recorded yet."));
            } else {
                VBox list = new VBox(8);
                list.getStyleClass().add("spell-list");
                for (LocalCharacterSheet.SpellPreview spell : spells) {
                    list.getChildren().add(spellCard(spell, editing));
                }
                spellsContent.getChildren().add(list);
            }
            LocalCharacterSheet.SpellPreview selected = selectedSpell();
            if (selected != null) {
                spellsContent.getChildren().add(spellDetails(selected, editing));
            }
            if (editing) {
                spellsContent.getChildren().add(primaryButton("Add spell", SoundEffect.ADD_SPELL, () -> showAddSpellDialog().ifPresent(spell -> {
                    if (assetActions != null) {
                        assetActions.createSpell(spell);
                    } else {
                        spells.add(spell);
                        selectedSpellId = spell.id();
                        refreshSpells(true);
                    }
                })));
            }
        }

        private Parent spellCard(LocalCharacterSheet.SpellPreview spell, boolean editing) {
            HBox card = new HBox(11);
            card.getStyleClass().add(spell.id().equals(selectedSpellId) ? "spell-card selected" : "spell-card");
            card.setAlignment(Pos.CENTER_LEFT);
            card.setOnMouseClicked(event -> {
                selectedSpellId = spell.id();
                refreshSpells(editing);
            });
            VBox copy = new VBox(5,
                    cardTitle(spell.name()),
                    body(label(spell.type()) + " / " + label(spell.spellClass())),
                    body(spell.requirements())
            );
            copy.getStyleClass().add("spell-card-copy");
            card.getChildren().addAll(spellIcon(spell), copy);
            return card;
        }

        private Parent spellDetails(LocalCharacterSheet.SpellPreview spell, boolean editing) {
            VBox details = new VBox(12);
            details.getStyleClass().add("spell-details-panel");
            if (editing) {
                TextField name = textField(spell.name());
                TextField image = textField(spell.image());
                ComboBox<String> type = spellTypeSelect(spell.type());
                ComboBox<String> spellClass = spellClassSelect(spell.spellClass());
                TextField requirements = textField(spell.requirements());
                TextArea description = textArea(spell.description());
                name.textProperty().addListener((observable, oldValue, value) -> updateSpell(spell.id(), spell.withName(value)));
                image.textProperty().addListener((observable, oldValue, value) -> updateSpell(spell.id(), spell.withImage(value)));
                type.valueProperty().addListener((observable, oldValue, value) -> updateSpell(spell.id(), spell.withType(value)));
                spellClass.valueProperty().addListener((observable, oldValue, value) -> updateSpell(spell.id(), spell.withSpellClass(value)));
                requirements.textProperty().addListener((observable, oldValue, value) -> updateSpell(spell.id(), spell.withRequirements(value)));
                description.textProperty().addListener((observable, oldValue, value) -> updateSpell(spell.id(), spell.withDescription(value)));
                GridPane form = sectionGrid("SPELL");
                addField(form, 0, "Name", name);
                addField(form, 1, "Type", type);
                addField(form, 2, "Class", spellClass);
                addField(form, 3, "Image URL", image);
                addField(form, 4, "Requirements", requirements);
                addField(form, 5, "Description", description);
                details.getChildren().add(form);
                HBox actions = new HBox(10);
                if (assetActions != null) {
                    actions.getChildren().add(primaryButton("Save spell", () -> {
                        LocalCharacterSheet.SpellPreview selected = selectedSpell();
                        if (selected != null) {
                            assetActions.updateSpell(selected);
                        }
                    }));
                }
                actions.getChildren().addAll(
                        dangerButton("Delete spell", () -> {
                            if (confirm("Delete this spell?", "This removes the spell from this sheet.")) {
                                if (assetActions != null) {
                                    assetActions.deleteSpell(spell.id());
                                } else {
                                    deleteSpell(spell.id());
                                    refreshSpells(true);
                                }
                            }
                        }),
                        secondaryButton("Close", () -> {
                            selectedSpellId = "";
                            refreshSpells(true);
                        })
                );
                details.getChildren().add(actions);
            } else {
                details.getChildren().addAll(
                        cardTitle(spell.name()),
                        body(label(spell.type()) + " / " + label(spell.spellClass())),
                        body(spell.requirements()),
                        body(spell.description().isBlank() ? "No description." : spell.description()),
                        secondaryButton("Close", () -> {
                            selectedSpellId = "";
                            refreshSpells(false);
                        })
                );
            }
            return details;
        }

        private Optional<LocalCharacterSheet.SpellPreview> showAddSpellDialog() {
            ModalState<LocalCharacterSheet.SpellPreview> state = new ModalState<>();
            TextField name = textField("");
            TextField image = textField("");
            ComboBox<String> type = spellTypeSelect("SPELL");
            ComboBox<String> spellClass = spellClassSelect("PRIEST");
            TextField requirements = textField("");
            TextArea description = textArea("");

            VBox content = new VBox(14);
            content.getStyleClass().add("item-dialog-content");
            GridPane form = sectionGrid("SPELL");
            addField(form, 0, "Name", name);
            addField(form, 1, "Type", type);
            addField(form, 2, "Class", spellClass);
            addField(form, 3, "Image URL", image);
            addField(form, 4, "Requirements", requirements);
            addField(form, 5, "Description", description);
            content.getChildren().addAll(new VBox(5, eyebrow("PREPARED MAGIC"), cardTitle("Add spell")), form);
            Button ok = primaryButton("OK", SoundEffect.ADD_SPELL, () -> {
                if (!name.getText().trim().isBlank() && !requirements.getText().trim().isBlank()) {
                    closeSceneModal(state, new LocalCharacterSheet.SpellPreview(
                            UUID.randomUUID().toString(),
                            name.getText().trim(),
                            type.getValue(),
                            spellClass.getValue(),
                            requirements.getText().trim(),
                            image.getText().trim(),
                            description.getText()
                    ), true);
                }
            });
            Button cancel = modalCancel("Cancel", state);
            HBox actions = new HBox(12, ok, cancel);
            actions.setAlignment(Pos.CENTER_RIGHT);
            content.getChildren().add(actions);
            Platform.runLater(name::requestFocus);
            return showSceneModal(dialogOwner, state, content);
        }

        private StackPane spellIcon(LocalCharacterSheet.SpellPreview spell) {
            StackPane icon = new StackPane();
            icon.getStyleClass().add("spell-icon");
            imageView(spell.image(), 52, 52).ifPresentOrElse(imageView -> {
                icon.getChildren().add(imageView);
            }, () -> icon.getChildren().add(new Label(initial(spell.name()))));
            return icon;
        }

        private void updateSpell(String spellId, LocalCharacterSheet.SpellPreview nextSpell) {
            for (int index = 0; index < spells.size(); index++) {
                if (spells.get(index).id().equals(spellId)) {
                    spells.set(index, nextSpell);
                    return;
                }
            }
        }

        private void deleteSpell(String spellId) {
            spells.removeIf(spell -> spell.id().equals(spellId));
            selectedSpellId = "";
        }

        private LocalCharacterSheet.SpellPreview selectedSpell() {
            return spells.stream().filter(spell -> spell.id().equals(selectedSpellId)).findFirst().orElse(null);
        }

        private ComboBox<String> spellTypeSelect(String selected) {
            ComboBox<String> select = new ComboBox<>();
            select.getItems().addAll("SPELL", "CANTRIP", "RITUAL");
            select.setValue(select.getItems().contains(selected) ? selected : "SPELL");
            select.getStyleClass().add("sheet-input");
            select.getStyleClass().add("sheet-select");
            return select;
        }

        private ComboBox<String> spellClassSelect(String selected) {
            ComboBox<String> select = new ComboBox<>();
            select.getItems().addAll("PRIEST", "SPELLCASTER", "WARLOCK", "DRUID", "ARTIST", "INQUISITOR", "SAVAGE");
            select.setValue(select.getItems().contains(selected) ? selected : "PRIEST");
            select.getStyleClass().add("sheet-input");
            select.getStyleClass().add("sheet-select");
            return select;
        }

        private static int globalHealth(LocalCharacterSheet.BodyHealth hp) {
            double damage = bodyDamage(hp.head(), false)
                    + bodyDamage(hp.neck(), false)
                    + bodyDamage(hp.torso(), false)
                    + bodyDamage(hp.leftArm(), true)
                    + bodyDamage(hp.rightArm(), true)
                    + bodyDamage(hp.leftLeg(), true)
                    + bodyDamage(hp.rightLeg(), true);
            return Math.max(0, Math.min(100, (int) Math.round(100 - damage)));
        }

        private static double bodyDamage(LocalCharacterSheet.BodyPartHealth part, boolean limb) {
            if (part.max() == 0) {
                return 0;
            }
            int denominator = limb ? part.max() * 3 : part.max();
            return ((part.max() - part.current()) * 100.0) / denominator;
        }

        private static int passiveDodge(int dexterity) {
            if (dexterity == 0) {
                return 3;
            }
            int normalized = Math.max(0, dexterity - 1);
            return (normalized / DIE_SIDES.length) * 12 + DIE_SIDES[normalized % DIE_SIDES.length];
        }

        private static String rollForLevel(int level) {
            int normalized = Math.max(0, level);
            if (normalized == 0) {
                return "3";
            }
            int cycleIndex = (normalized - 1) % DIE_SIDES.length;
            int bonus = ((normalized - 1) / DIE_SIDES.length) * 13;
            return "3d" + DIE_SIDES[cycleIndex] + (bonus > 0 ? "+" + bonus : "");
        }

        private static String effectiveSkillRoll(int statLevel, int skillLevel) {
            return rollForLevel(Math.floorDiv(Math.max(0, statLevel) + Math.max(0, skillLevel), 2));
        }

        private static String label(String code) {
            if (code == null || code.isBlank()) {
                return "—";
            }
            String[] parts = code.toLowerCase().split("_");
            StringBuilder builder = new StringBuilder();
            for (String part : parts) {
                if (part.isBlank()) {
                    continue;
                }
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
            return builder.toString();
        }

        private static VBox section(String title, Parent content) {
            VBox box = new VBox(14, eyebrow(title.toUpperCase()), content);
            box.getStyleClass().add("sheet-section");
            return box;
        }

        private static GridPane sectionGrid(String styleClass) {
            GridPane grid = new GridPane();
            grid.getStyleClass().add("sheet-grid");
            grid.getStyleClass().add("sheet-grid-" + styleClass.toLowerCase());
            grid.setHgap(14);
            grid.setVgap(14);
            return grid;
        }

        private static void addField(GridPane grid, int index, String label, Parent control) {
            VBox box = new VBox(6, eyebrow(label.toUpperCase()), control);
            box.getStyleClass().add("sheet-field");
            GridPane.setHgrow(box, Priority.ALWAYS);
            grid.add(box, index % 3, index / 3);
        }

        private record SkillEditor(LocalCharacterSheet.Skill skill, TextField level) {
        }
    }

    private static void deleteCharacter(
            AppNavigator navigator,
            GuestCharacterArchive archive,
            LocalCharacterRecord record
    ) {
        if (!confirmOverlay(navigator.stage(), "Delete " + record.name() + "?",
                "This removes the local guest JSON from this machine.")) {
            return;
        }
        try {
            archive.deleteCharacter(record.id());
            navigator.showGuestArchive();
        } catch (Exception exception) {
            error("Could not delete character", exception);
        }
    }

    private static void deleteAccountCharacter(
            AppNavigator navigator,
            AuthSession session,
            AccountCharacterCard record
    ) {
        if (!confirmOverlay(navigator.stage(), "Delete " + record.name() + "?",
                "This removes the character from your server account.")) {
            return;
        }
        runAuthTask(() -> {
            navigator.characterClient().delete(session, record.id());
            return true;
        }, deleted -> navigator.showAccountArchive(), exception -> error("Could not delete account character", exception));
    }

    private static FileChooser jsonChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
        return chooser;
    }

    private static Path selectedPath(java.io.File file) {
        return file == null ? null : file.toPath();
    }

    private static String initial(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        return name.trim().substring(0, 1).toUpperCase();
    }

    private static Image cachedImage(String url) {
        return IMAGE_CACHE.computeIfAbsent(url, key -> new Image(key, true));
    }

    private static String displayLabel(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        String[] parts = code.toLowerCase().split("[_\\s-]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    private static String safeFileName(String name) {
        String value = name == null || name.isBlank() ? "character" : name.trim();
        return value.replaceAll("[^a-zA-Z0-9а-яА-Я._-]+", "_");
    }

    private static void info(String message) {
        messageOverlay(activeOwner(), "PFP COMPANION", "Done", message);
    }

    private static void error(String header, Exception exception) {
        messageOverlay(activeOwner(), "PFP COMPANION", header, exception.getMessage());
    }
}
