package application;

import javafx.application.Application;
import javafx.stage.Stage;
import view.ViewManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        ViewManager viewManager = new ViewManager();
        primaryStage = viewManager.getMainStage();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
