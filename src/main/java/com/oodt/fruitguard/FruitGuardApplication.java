package com.oodt.fruitguard;

import com.oodt.fruitguard.ui.GameView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class FruitGuardApplication extends Application {
    @Override
    public void start(Stage stage) {
        GameView root = new GameView();
        Scene scene = new Scene(root, 1320, 800);
        scene.getStylesheets().add(getClass().getResource("/com/oodt/fruitguard/style.css").toExternalForm());
        stage.setTitle("保卫水果 · Fruit Guard");
        stage.setMinWidth(1180);
        stage.setMinHeight(740);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> root.shutdown());
        stage.show();
        root.startLoop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
