package view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.GameSubScene;
import model.GameButton;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ViewManager {

    // ViewManager Class Attributes
    private final int HIGHT = 768;
    private final int WIDTH = 1024;
    private AnchorPane mainPane;
    private Stage mainStage;
    private Scene mainScene;

    //Button Dimension Attributes
    private final static int MENU_BUTTONS_START_X = 100;
    private final static int MENU_BUTTONS_START_Y = 220;

    List<GameButton> menuButtons;

    private GameSubScene helpSubScene;
    private GameSubScene settingsSubScene;

    private final String GAME_TITLE = "Space Odyssey : The Afterlife Of A Crewmate";

    private AudioClip buttonClickSound;

    private final String BUTTONCLICKSOUND_PATH = "/view/resources/button_click_sound.wav";
    
    private final String FONT_PATH = "src/model/resources/kenvector_future.ttf";

    private static final String SCORE_FILE = "highest_score.bin";

    private boolean sfxEnabled = true;

    private static Label highestScoreLabel;

    //ViewManager Class Constructor
    public ViewManager() {
        menuButtons = new ArrayList<>();
        mainPane = new AnchorPane();
        mainStage = new Stage();
        mainScene = new Scene(mainPane, WIDTH, HIGHT);
        mainStage.setScene(mainScene);
        mainStage.setTitle(GAME_TITLE);
        mainStage.setResizable(false);
        initializeSoundEffects();
        setGameIcon();
        createButtons();
        createBackground();
        createLogo();
        createSubScenes();
        createHighestScoreLabel();
        showHighestScore();
    }

    // Returns the main stage of the application.
    public Stage getMainStage() {
        return mainStage;
    }

    // Initializes the sound effects used in the main menu.
    private void initializeSoundEffects() {
        buttonClickSound = new AudioClip(getClass().getResource(BUTTONCLICKSOUND_PATH).toString());
    }

    // Creates sub-scenes for help and settings.
    private void createSubScenes() {
        helpSubScene = new GameSubScene("model/resources/yellow_panel.png", "Welcome to the Ultimate Adventure!\n\n"
                + "Your mission is simple: ACHIEVE THE HIGHEST SCORE POSSIBLE!\n\n"
                + "But beware, it's not just about getting points...\n"
                + "You must also AVOID the enemy creatures lurking around!\n\n"
                + "Controls:\n"
                + "- Use the RIGHT ARROW key to move RIGHT\n"
                + "- Use the LEFT ARROW key to move LEFT\n\n"
                + "Stay sharp, stay quick, and most importantly, HAVE FUN!\n\n"
                + "Can you become the ultimate champion?\n"
                + "The adventure awaits... Good luck, brave player!");
        mainPane.getChildren().add(helpSubScene);

        settingsSubScene = new GameSubScene("model/resources/yellow_panel.png", null);
        mainPane.getChildren().add(settingsSubScene);
    }

    // Creates and adds buttons to the main menu.
    private void createButtons() {
        createPlayButton();
        createHelpButton();
        createSettingsButton();
        createExitButton();
    }

    // Adds a menu button to the main pane.
    private void addMenuButton(GameButton button) {
        button.setLayoutX(MENU_BUTTONS_START_X);
        button.setLayoutY(MENU_BUTTONS_START_Y + menuButtons.size() * 100);
        menuButtons.add(button);
        mainPane.getChildren().add(button);
    }

    // Creates the play button and its action event.
    private void createPlayButton() {
        GameButton playButton = new GameButton("PLAY");
        addMenuButton(playButton);

        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                GameViewManager gameViewManager = new GameViewManager();
                gameViewManager.setSfxEnabled(sfxEnabled); // Pass the SFX setting to the game window
                gameViewManager.createNewGame(mainStage);

                if (sfxEnabled) {
                    buttonClickSound.play();
                }
            }
        });
    }

    // Creates the help button and its action event.
    private void createHelpButton() {
        GameButton helpButton = new GameButton("HELP");
        addMenuButton(helpButton);

        helpButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                helpSubScene.moveSubScene();
                if (sfxEnabled) {
                    buttonClickSound.play();
                }
            }
        });
    }

    // Creates the settings button and its action event.
    private void createSettingsButton() {
        GameButton settingsButton = new GameButton("SETTINGS");
        addMenuButton(settingsButton);

        settingsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showSettingsPanel();
                if (sfxEnabled) {
                    buttonClickSound.play();
                }
            }
        });
    }

    // Creates the exit button and its action event.
    private void createExitButton() {
        GameButton exitButton = new GameButton("EXIT");
        addMenuButton(exitButton);

        exitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                if (sfxEnabled) {
                    buttonClickSound.play();
                }
                mainStage.close();
            }
        });
    }

    // Sets the background image for the main pane.
    private void createBackground() {
        Image backGImage = new Image("/view/resources/hiclipart.png", 800, 601, false, true);
        BackgroundImage background = new BackgroundImage(backGImage,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, null);
        mainPane.setBackground(new Background(background));
    }

    // Sets the game icon for the main stage.
    private void setGameIcon() {
        Image gameIcon = new Image("model/resources/playerR.png");
        mainStage.getIcons().add(gameIcon);
    }

    // Creates the game logo label and adds shadow effect on mouse events.
    private void createLogo() {
        Label label = new Label("Space Odyssey\nThe Afterlife Of A Crewmate");
        label.setLayoutX(350);
        label.setLayoutY(50);
        label.setStyle("-fx-text-fill: white;");
        try {
            label.setFont(Font.loadFont(new FileInputStream("src/model/resources/kenvector_future.ttf"), 30));
        } catch (FileNotFoundException ex) {
            label.setFont(Font.font("Verdana", 23));
        }

        label.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                DropShadow dropShadow = new DropShadow();
                dropShadow.setColor(Color.CYAN);
                label.setEffect(dropShadow);
            }
        });

        label.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                label.setEffect(null);
            }
        });

        mainPane.getChildren().add(label);
    }

    // Creates and sets up the highest score label.
    private void createHighestScoreLabel() {
        highestScoreLabel = new Label();
        highestScoreLabel.setLayoutX(350);
        highestScoreLabel.setLayoutY(150);
        highestScoreLabel.setStyle("-fx-text-fill: white;");
        try {
            highestScoreLabel.setFont(Font.loadFont(new FileInputStream("src/model/resources/kenvector_future.ttf"), 30));
        } catch (FileNotFoundException ex) {
            highestScoreLabel.setFont(Font.font("Verdana", 15));
        }

        highestScoreLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                DropShadow dropShadow = new DropShadow();
                dropShadow.setColor(Color.CYAN);
                highestScoreLabel.setEffect(dropShadow);
            }
        });

        highestScoreLabel.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                highestScoreLabel.setEffect(null);
            }
        });

        mainPane.getChildren().add(highestScoreLabel);
    }

    // Loads the highest score from a file.
    // Creates the file if it is not created.
    private static int loadHighestScore() {
        int highestScore = 0;
        File file = new File(SCORE_FILE);
        if (file.exists()) {
            try (DataInputStream inputStream = new DataInputStream(new FileInputStream(file))) {
                highestScore = inputStream.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return highestScore;
    }

    // Displays the highest score on the highest score label.
    public static void showHighestScore() {
        int highestScore = loadHighestScore();
        highestScoreLabel.setText("Highest Score = " + highestScore);
    }

    // Resets the highest score to zero and saves it to a file.
    private void resetHighestScore() {
        int score = 0;
        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(SCORE_FILE))) {
            outputStream.writeInt(score);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Sets the font for the specified radio button.
    private void setButtonsFont(RadioButton button) {
        try {
            button.setFont(Font.loadFont(new FileInputStream(FONT_PATH), 15));
        } catch (FileNotFoundException ex) {
            ex.getMessage();
        }
    }

    // Sets the font for the specified label.
    private void setLabelFont(Label label) {
        try {
            label.setFont(Font.loadFont(new FileInputStream(FONT_PATH), 25));
        } catch (FileNotFoundException ex) {
            ex.getMessage();
        }
    }

    // Shows the settings panel with options to toggle SFX and reset the highest score.
    private void showSettingsPanel() {
        VBox settingsBox = new VBox(20);
        settingsBox.setLayoutX(100);
        settingsBox.setLayoutY(100);

        Label sfxLabel = new Label("SFX");
        setLabelFont(sfxLabel);

        ToggleGroup sfxToggleGroup = new ToggleGroup();

        RadioButton sfxOnButton = new RadioButton("ON");
        setButtonsFont(sfxOnButton);
        sfxOnButton.setToggleGroup(sfxToggleGroup);
        sfxOnButton.setSelected(sfxEnabled);

        RadioButton sfxOffButton = new RadioButton("OFF");
        setButtonsFont(sfxOffButton);
        sfxOffButton.setToggleGroup(sfxToggleGroup);
        sfxOffButton.setSelected(!sfxEnabled);

        sfxToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            sfxEnabled = newValue == sfxOnButton;
        });

        Label resetScoreLabel = new Label("Reset Score");
        setLabelFont(resetScoreLabel);
        Button resetScoreButton = new Button("Reset");
        try {
            resetScoreButton.setFont(Font.loadFont(new FileInputStream(FONT_PATH), 20));
        } catch (FileNotFoundException ex) {
            ex.getMessage();
        }
        resetScoreButton.setStyle("-fx-text-fill: black; -fx-background-color: red");
        resetScoreButton.setOnAction(event -> {

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Menu");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to reset your highest score?");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");

                alert.getButtonTypes().setAll(yesButton, noButton);

                // Load the icon image
                Image icon = new Image("model/resources/playerR.png");

                // Get the dialog pane's scene window and cast it to a Stage
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();

                // Set the icon for the alert's stage
                alertStage.getIcons().add(icon);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == yesButton) {
                    resetHighestScore();
                    showHighestScore();
                } else {
                    alert.close();
                }
            });

        });

        settingsBox.getChildren().addAll(sfxLabel, sfxOnButton, sfxOffButton, resetScoreLabel, resetScoreButton);
        ((AnchorPane) settingsSubScene.getRoot()).getChildren().add(settingsBox);
        settingsSubScene.moveSubScene();
    }
}
