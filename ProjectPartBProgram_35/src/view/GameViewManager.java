package view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.text.Font;
import model.GamePlatform;
import model.Monster;
import model.PowerUp;

public class GameViewManager {

    //GameViewManager Class Attributes
    private AnchorPane gamePane;
    private Stage gameStage;
    private Scene gameScene;

    private static final int GAME_WIDTH = 600;
    private static final int GAME_HEIGHT = 800;
    private final String BACKGROUND_PATH = "view/resources/game_background.jpg";
    private final String GAME_TITLE = "Space Odyssey : The Afterlife Of A Crewmate";

    private Stage menuStage;
    private Player player;
    private Label scoreLabel;
    private int highestScore = 0;
    private int lastScore = 0;
    private static final String SCORE_FILE = "highest_score.bin";

    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private boolean isPaused = false;
    private Timeline movementTimeline;

    //GamePlatform Attributes
    private List<GamePlatform> platforms;
    private static int PLATFORM_COUNT = 12;
    private static final double PLATFORM_WIDTH = 70;
    private static final double PLATFORM_HEIGHT = 20;
    private static final double JUMP_VELOCITY = -15;
    private static final double GRAVITY = 0.5;
    private double velocityY = 0;
    private boolean initialPlatformAdded = false;

    //PowerUp Attributes
    private List<PowerUp> powerUps;
    private static final int POWER_UP_COUNT = 3;
    private static final double POWER_UP_WIDTH = 40;
    private static final double POWER_UP_HEIGHT = 40;
    private Random random = new Random();

    //Monster Attributes
    private List<Monster> monsters;
    private static final int MONSTER_COUNT = 1;
    private static final double MONSTER_WIDTH = 50;
    private static final double MONSTER_HEIGHT = 50;

    //AudioClip Objects fot the Sound Effects
    private AudioClip jumpSound;
    private AudioClip gameOverSound;
    private AudioClip oofSound;
    private AudioClip powerUpSound;

    //Sound Effects Paths
    private final String JUMPSOUND_PATH = "/view/resources/jump_sound.wav";
    private final String GAMEOVERSOUND_PATH = "/view/resources/game_over_sound.wav";
    private final String OOFSOUND_PATH = "/view/resources/oof_sound.wav";
    private final String POWERUPSOUND_PATH = "/view/resources/power-up_sound.wav";
    private boolean sfxEnabled = true;

    //GameViewManager Class Constructor
    public GameViewManager() {
        loadHighestScore();
        initializeStage();
        initializeSoundEffects();
        createKeyListeners();
        createMovementTimeline();
        createScoreLabel();
        createBackground();
    }

    //Method to create a new game after restarting
    public void createNewGame(Stage menuStage) {
        this.menuStage = menuStage;
        this.menuStage.hide();
        player = new Player();
        player.setLayoutX(GAME_WIDTH / 2 - player.getImage().getWidth() / 2);
        player.setLayoutY(GAME_HEIGHT - 100);  // Adjust the initial Y position
        initializePlatforms();  // Initialize platforms first
        initializePowerUps();   // Initialize power-ups
        initializeMonsters();
        gamePane.getChildren().add(player);  // Add player last to ensure it is in front
        createScoreLabel();  // Reset score label
        resetMovementFlags();  // Reset movement flags
        giveInitialJump();
        gameStage.show();
    }

    // Method to set the movement & pausing boolean attributes
    private void createKeyListeners() {
        gameScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.LEFT) {
                    isMovingLeft = true;
                    player.setImageLeft();
                } else if (event.getCode() == KeyCode.RIGHT) {
                    isMovingRight = true;
                    player.setImageRight();
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    if (!isPaused) {
                        pauseGame();
                    }
                }
            }
        });

        gameScene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.LEFT) {
                    isMovingLeft = false;
                } else if (event.getCode() == KeyCode.RIGHT) {
                    isMovingRight = false;
                }
            }
        });
    }

    //Method for creating the background of the game panel
    private void createBackground() {
        Image backGImage = new Image(BACKGROUND_PATH, 600, 800, false, true);
        BackgroundImage background = new BackgroundImage(backGImage, BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, null);
        gamePane.setBackground(new Background(background));
    }

    //Sets up and starts the game loop for player movement and collision detection.
    private void createMovementTimeline() {
        movementTimeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (!isPaused) {
                movePlayer();
                checkPlatformCollision();
                checkPowerUpCollision();  // Add this line
                checkMonsterCollision();
                applyGravity();
                checkGameOver();
                handleCameraMovement();
            }
        }));
        movementTimeline.setCycleCount(Timeline.INDEFINITE);
        movementTimeline.play();
    }

    //Moves the player left or right based on input and wraps around game borders.
    private void movePlayer() {
        if (isMovingLeft) {
            player.setLayoutX(player.getLayoutX() - 5);
        }
        if (isMovingRight) {
            player.setLayoutX(player.getLayoutX() + 5);
        }

        // Wrap player around the game borders
        if (player.getLayoutX() < -player.getImage().getWidth()) {
            player.setLayoutX(GAME_WIDTH);
        }
        if (player.getLayoutX() > GAME_WIDTH) {
            player.setLayoutX(-player.getImage().getWidth());
        }
    }

    /*
    Checks and handles collisions between the player and platforms.
    If the player lands on a platform, the player is allowed to jump again.
    Increases score and difficulty when touching a new platform.
     */
    private void checkPlatformCollision() {
        for (GamePlatform platform : platforms) {
            if (velocityY > 0 && player.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                double playerBottom = player.getLayoutY() + player.getImage().getHeight();
                double platformTop = platform.getLayoutY();

                if (playerBottom >= platformTop && playerBottom <= platformTop + 17) {
                    player.setLayoutY(platform.getLayoutY() - player.getImage().getHeight());
                    jump();
                    if (!platform.isTouched()) {
                        player.increaseScore();
                        updateScoreLabel();
                        platform.setTouched(true);
                        increaseDifficulty();  // Increase difficulty dynamically
                    }
                    break;
                }
            }
        }
    }

    /*
    Checks and handles collisions between the player and power-ups.
    Collecting a power-up increases the player's score and removes the power-up from the game.
     */
    private void checkPowerUpCollision() {
        for (PowerUp powerUp : powerUps) {
            if (!powerUp.isCollected() && player.getBoundsInParent().intersects(powerUp.getBoundsInParent())) {
                powerUp.setCollected(true);
                player.increaseScoreBy(25);
                updateScoreLabel();
                gamePane.getChildren().remove(powerUp);

                if (sfxEnabled) {
                    powerUpSound.play();
                }
            }
        }
    }

    /*
    Checks and handles collisions between the player and monsters.
    Colliding with a monster decreases the player's score and removes the monster from the game.
     */
    private void checkMonsterCollision() {
        for (Monster monster : monsters) {
            if (player.getBoundsInParent().intersects(monster.getBoundsInParent())) {
                if (!monster.isTouched()) {
                    monster.setTouched(true);
                    player.decreaseScoreBy(50);
                    updateScoreLabel();
                    gamePane.getChildren().remove(monster);

                    if (sfxEnabled) {
                        oofSound.play();
                    }
                }
            }
        }
    }

    // Increases the game's difficulty by adding new monsters based on the player's score.
    private void increaseDifficulty() {
        int currentScore = player.getScore();
        if (currentScore - lastScore >= 150) {
            // Increase number of monsters
            if (monsters.size() < 3) {
                lastScore = currentScore;
                addNewMonster();
            }
        }
    }

    // Adds a new monster to the game at a random position.
    private void addNewMonster() {
        double x = random.nextDouble() * (GAME_WIDTH - MONSTER_WIDTH);
        double y = random.nextDouble() * (GAME_HEIGHT - MONSTER_HEIGHT * PLATFORM_COUNT);
        Monster monster = new Monster(x, y);
        monsters.add(monster);
        gamePane.getChildren().add(monster);
    }

    // Makes the player jump by setting the vertical velocity to the jump velocity.
    private void jump() {
        velocityY = JUMP_VELOCITY;
        if (sfxEnabled) {
            jumpSound.play();
        }
    }

    // Applies gravity to the player, updating their vertical position.
    private void applyGravity() {
        velocityY += GRAVITY;
        player.setLayoutY(player.getLayoutY() + velocityY);
    }

    // Gives the player an initial jump at the start of the game.
    private void giveInitialJump() {
        jump();
    }

    // Checks if the player has fallen below the game area to end the game.    
    private void checkGameOver() {
        if (player.getLayoutY() > GAME_HEIGHT) {
            endGame();
        }
    }

    // Handles camera movement to follow the player, moving platforms, power-ups, and monsters downwards as necessary.
    private void handleCameraMovement() {
        if (player.getLayoutY() < GAME_HEIGHT / 2) {
            double offset = GAME_HEIGHT / 2 - player.getLayoutY();
            player.setLayoutY(GAME_HEIGHT / 2);

            // Move platforms downwards
            for (GamePlatform platform : platforms) {
                platform.setLayoutY(platform.getLayoutY() + offset);

                // Regenerate platform at the top if it moves out of view
                if (platform.getLayoutY() > GAME_HEIGHT) {
                    platform.setLayoutY(-PLATFORM_HEIGHT);
                    platform.setLayoutX(new Random().nextDouble() * (GAME_WIDTH - PLATFORM_WIDTH));
                    platform.setTouched(false);
                }
            }

            // Move power-ups downwards
            for (PowerUp powerUp : powerUps) {
                powerUp.setLayoutY(powerUp.getLayoutY() + offset);

                // Regenerate power-up at the top if it moves out of view
                if (powerUp.getLayoutY() > GAME_HEIGHT) {
                    powerUp.setLayoutY(-POWER_UP_HEIGHT);
                    powerUp.setLayoutX(new Random().nextDouble() * (GAME_WIDTH - POWER_UP_WIDTH));
                    powerUp.setCollected(false);  // Reset collected state

                    // Check if powerUp is already a child before adding
                    if (!gamePane.getChildren().contains(powerUp)) {
                        gamePane.getChildren().add(powerUp);
                    }
                }
            }

            // Move monsters downwards
            for (Monster monster : monsters) {
                monster.setLayoutY(monster.getLayoutY() + offset);

                // Regenerate monster at the top if it moves out of view
                if (monster.getLayoutY() > GAME_HEIGHT) {
                    monster.setLayoutY(-MONSTER_HEIGHT);
                    monster.setLayoutX(new Random().nextDouble() * (GAME_WIDTH - MONSTER_WIDTH));
                    monster.setTouched(false);  // Reset touched state

                    // Check if monster is already a child before adding
                    if (!gamePane.getChildren().contains(monster)) {
                        gamePane.getChildren().add(monster);
                    }
                }
            }
        }
    }

    // Initializes the game stage and scene, setting up the game window properties.
    private void initializeStage() {
        gamePane = new AnchorPane();
        gameScene = new Scene(gamePane, GAME_WIDTH, GAME_HEIGHT);
        gameStage = new Stage();
        gameStage.setResizable(false);
        gameStage.setScene(gameScene);
        gameStage.setTitle(GAME_TITLE);
        gameStage.getIcons().add(new Image("model/resources/playerR.png"));
    }

    // Initializes game platforms at the start of the game.
    private void initializePlatforms() {
        if (initialPlatformAdded) {
            return;
        }

        platforms = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < PLATFORM_COUNT; i++) {
            double x = random.nextDouble() * (GAME_WIDTH - PLATFORM_WIDTH);
            double y = i * (GAME_HEIGHT / PLATFORM_COUNT);

            GamePlatform platform = new GamePlatform(x, y);
            platforms.add(platform);
            gamePane.getChildren().add(platform);
        }

        // Add a stable platform directly under the player's initial position
        GamePlatform initialPlatform = new GamePlatform(GAME_WIDTH / 2 - PLATFORM_WIDTH / 2, GAME_HEIGHT - 50);
        platforms.add(initialPlatform);
        gamePane.getChildren().add(initialPlatform);

        initialPlatformAdded = true;
    }

    // Initializes power-ups at the start of the game.
    private void initializePowerUps() {
        powerUps = new ArrayList<>();
        for (int i = 0; i < POWER_UP_COUNT; i++) {
            double x = random.nextDouble() * (GAME_WIDTH - POWER_UP_WIDTH);
            double y = random.nextDouble() * (GAME_HEIGHT - POWER_UP_HEIGHT * PLATFORM_COUNT); // Position them above the initial platforms

            PowerUp powerUp = new PowerUp(x, y);
            powerUps.add(powerUp);
            gamePane.getChildren().add(powerUp);
        }
    }

    // Initializes monsters at the start of the game.
    private void initializeMonsters() {
        monsters = new ArrayList<>();
        for (int i = 0; i < MONSTER_COUNT; i++) {
            double x = random.nextDouble() * (GAME_WIDTH - MONSTER_WIDTH);
            double y = random.nextDouble() * (GAME_HEIGHT - MONSTER_HEIGHT * PLATFORM_COUNT);

            Monster monster = new Monster(x, y);
            monsters.add(monster);
            gamePane.getChildren().add(monster);
        }
    }

    // Initializes sound effects for the game.
    private void initializeSoundEffects() {
        jumpSound = new AudioClip(getClass().getResource(JUMPSOUND_PATH).toString());
        gameOverSound = new AudioClip(getClass().getResource(GAMEOVERSOUND_PATH).toString());
        oofSound = new AudioClip(getClass().getResource(OOFSOUND_PATH).toString());
        powerUpSound = new AudioClip(getClass().getResource(POWERUPSOUND_PATH).toString());
    }

    // Enables or disables sound effects based on the provided flag.
    public void setSfxEnabled(boolean sfxEnabled) {
        this.sfxEnabled = sfxEnabled;
    }

    // Resets movement flags for the player.
    private void resetMovementFlags() {
        isMovingLeft = false;
        isMovingRight = false;
    }

    // Creates and displays the score label on the game screen.
    private void createScoreLabel() {
        scoreLabel = new Label();
        scoreLabel.setStyle("-fx-text-fill: black; -fx-background-color: orange");
        scoreLabel.setLayoutX(10);
        scoreLabel.setLayoutY(10);
        gamePane.getChildren().add(scoreLabel);
        try {
            scoreLabel.setFont(Font.loadFont(new FileInputStream("src/model/resources/kenvector_future.ttf"), 15));
        } catch (FileNotFoundException ex) {
            scoreLabel.setFont(Font.font("Verdana", 15));
        }
    }

    // Updates the score label with the current and highest scores.
    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + player.getScore() + " | Highest Score: " + highestScore);
    }

    // Loads the highest score from a file.
    // This method creates the file if the file is not created in the project directory
    private void loadHighestScore() {
        File file = new File(SCORE_FILE);
        if (file.exists()) {
            try (DataInputStream inputStream = new DataInputStream(new FileInputStream(file))) {
                highestScore = inputStream.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Saves the highest score to a file.
    private void saveHighestScore() {
        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(SCORE_FILE))) {
            outputStream.writeInt(highestScore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Checks if the current score is higher than the highest score and updates it if necessary.
    private void checkAndUpdateHighestScore() {
        if (player.getScore() > highestScore) {
            highestScore = player.getScore();
            saveHighestScore();
            updateScoreLabel();
        }
    }

    // Resumes the game from a paused state.
    private void resumeGame() {
        isPaused = false;
        movementTimeline.play();
    }

    // Restarts the game, resetting the state and starting a new game.
    private void restartGame() {
        gamePane.getChildren().clear();
        createScoreLabel();
        initialPlatformAdded = false;
        initializePlatforms();
        createNewGame(menuStage);
        movementTimeline.play();
    }

    // Pauses the game and shows a pause menu.
    private void pauseGame() {
        isPaused = true;
        movementTimeline.pause();

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Pause Menu");
            alert.setHeaderText(null);
            alert.setContentText("Game is paused. Do you want to resume?");

            ButtonType resumeButton = new ButtonType("Resume");
            ButtonType exitButton = new ButtonType("Exit");

            alert.getButtonTypes().setAll(resumeButton, exitButton);

            // Load the icon image
            Image icon = new Image("model/resources/playerR.png");

            // Get the dialog pane's scene window and cast it to a Stage
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();

            // Set the icon for the alert's stage
            alertStage.getIcons().add(icon);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == resumeButton) {
                resumeGame();
            } else {
                menuStage.show();
                gameStage.close();
            }
        });
    }

    // Ends the game, checking and updating the highest score, stopping the game loop, and showing the game over alert.
    public void endGame() {
        checkAndUpdateHighestScore();
        if (sfxEnabled) {
            gameOverSound.play();
        }
        movementTimeline.stop();
        showGameOverAlert();
    }

    // Shows a game over alert with options to restart or exit.
    private void showGameOverAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText("Game Over! Your score: " + player.getScore() + ". Highest score: " + highestScore + ". Do you want to restart?");

            ButtonType restartButton = new ButtonType("Restart");
            ButtonType exitButton = new ButtonType("Exit");

            alert.getButtonTypes().setAll(restartButton, exitButton);

            Image icon = new Image("model/resources/playerR.png");

            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(icon);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == restartButton) {
                restartGame();
            } else {
                menuStage.show();
                ViewManager.showHighestScore();
                gameStage.close();
            }
        });
    }

}
