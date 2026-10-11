package ru.mirea.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ItOutsourceApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ru/mirea/project/view/main-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(
                getClass().getResource("/ru/mirea/project/view/main.css").toExternalForm());

        stage.setTitle("IT-OUTSOURCE");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
