package org.example.magua;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.magua.config.Config;
import org.example.magua.ui.MainLayout;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.UNDECORATED);

        MainLayout mainLayout = new MainLayout();
        Scene scene = new Scene(mainLayout.getView(), 1280, 800);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Maguan");
        primaryStage.show();
        Config.getInstance();
    }
}
