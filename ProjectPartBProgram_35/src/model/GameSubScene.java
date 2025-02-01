package model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class GameSubScene extends SubScene {

    //GameSubScene Class Attributes
    private final static String FONT_PATH = "src/model/resources/kenvector_future.ttf";
    private boolean isHidden;

    //GameSubScene Class Constructor
    public GameSubScene(String backgroundImagePath, String content) {
        super(new AnchorPane(), 600, 400);
        prefWidth(400);
        prefHeight(600);

        BackgroundImage image = new BackgroundImage(new Image(backgroundImagePath, 600, 400, false, true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, null);

        AnchorPane root2 = (AnchorPane) this.getRoot();
        root2.setBackground(new Background(image));

        if (content != null && !content.isEmpty()) {
            createContent(root2, content);
        }

        isHidden = true;
        setLayoutX(1024);
        setLayoutY(200);
    }

    //Method to move the subscene in & out of the screen
    public void moveSubScene() {
        TranslateTransition transition = new TranslateTransition();
        transition.setDuration(Duration.seconds(0.3));
        transition.setNode(this);

        if (isHidden) {
            transition.setToX(-676);
            isHidden = false;
        } else {
            transition.setToX(0);
            isHidden = true;
        }

        transition.play();
    }

    //Method to add content to the subsene (for example, help message)
    private void createContent(AnchorPane root, String content) {
        Label label = new Label();
        label.setText(content);
        try {
            label.setFont(Font.loadFont(new FileInputStream(FONT_PATH), 16));
        } catch (FileNotFoundException ex) {
            label.setFont(Font.font("Verdana", 23));
        }

        label.setOnMouseEntered(event -> {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.DARKGRAY);
            dropShadow.setRadius(5.0);
            dropShadow.setOffsetX(3.0);
            dropShadow.setOffsetY(3.0);
            label.setEffect(dropShadow);
        });

        label.setOnMouseExited(event -> {
            label.setEffect(null);
        });

        label.setWrapText(true);
        label.setPrefWidth(550);
        label.setPrefHeight(350);
        label.setLayoutX(25);
        label.setLayoutY(25);

        root.getChildren().add(label);
    }
}
