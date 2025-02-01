package model;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class Monster extends ImageView {

    //Monster Class Attributes
    private Image[] monsterImages;
    private int currentImageIndex = 0;
    private boolean isTouched = false;

    //Monster Class Constructor
    public Monster(double x, double y) {
        monsterImages = new Image[]{
            new Image("model/resources/monster1.png"),
            new Image("model/resources/monster2.png"),
            new Image("model/resources/monster3.png")
        };
        setImage(monsterImages[0]);
        setLayoutX(x);
        setLayoutY(y);

        //Timeline object to create the animation effect for the monster (Iterating through the images)
        Timeline animationTimeline = new Timeline(new KeyFrame(Duration.millis(200), e -> animate()));
        animationTimeline.setCycleCount(Timeline.INDEFINITE);
        animationTimeline.play();
    }

    //Animation Method
    private void animate() {
        currentImageIndex = (currentImageIndex + 1) % monsterImages.length;
        setImage(monsterImages[currentImageIndex]);
    }
    
    //Getters & Setters for the boolean value
    public boolean isTouched() {
        return isTouched;
    }

    public void setTouched(boolean isTouched) {
        this.isTouched = isTouched;
    }
}
