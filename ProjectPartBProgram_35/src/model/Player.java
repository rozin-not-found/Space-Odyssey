package model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Player extends ImageView {

    //Player Class Attributes
    private static final String PLAYER_IMAGE_RIGHT = "model/resources/playerR.png";
    private static final String PLAYER_IMAGE_LEFT = "model/resources/playerL.png";
    private int score;

    //Player Class Constructor
    public Player() {
        Image image = new Image(PLAYER_IMAGE_RIGHT);
        this.setImage(image);
        this.score = 0;
    }

    //Methods to set the images of the player
    public void setImageLeft() {
        this.setImage(new Image(PLAYER_IMAGE_LEFT));
    }

    public void setImageRight() {
        this.setImage(new Image(PLAYER_IMAGE_RIGHT));
    }

    
    //Getter method for the score value
    public int getScore() {
        return score;
    }

    //Method to increase the score by 1
    public void increaseScore() {
        score++;
    }

    //Setters method for the score value
    public void increaseScoreBy(int value) {
        score += value;
    }

    public void decreaseScoreBy(int value) {
        score -= value;
    }
}
