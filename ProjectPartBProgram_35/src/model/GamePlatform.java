package model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GamePlatform extends ImageView {

    //GamePlatform Class Attributes
    private boolean isTouched;

    //GamePlatform Class Constructor
    public GamePlatform(double x, double y) {
        super(new Image("model/resources/platform.png"));
        this.setLayoutX(x);
        this.setLayoutY(y);
        this.isTouched = false;
    }

    //Getters & Setters for the boolean value
    public boolean isTouched() {
        return isTouched;
    }

    public void setTouched(boolean touched) {
        this.isTouched = touched;
    }
}
