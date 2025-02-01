package model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PowerUp extends ImageView {

    // PowerUp Class Attributes
    private boolean isCollected;

    //PowerUp Class Constructor
    public PowerUp(double x, double y) {
        this.setImage(new Image("model/resources/score_power_up.png"));
        this.setLayoutX(x);
        this.setLayoutY(y);
        this.isCollected = false;
    }

    //Getters & Setters for the boolean value
    public boolean isCollected() {
        return isCollected;
    }

    public void setCollected(boolean collected) {
        this.isCollected = collected;
    }
}
