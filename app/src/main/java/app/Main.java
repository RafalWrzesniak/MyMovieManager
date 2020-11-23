package app;

import FileOperations.AutoSave;
import controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Main extends Application {

    private AutoSave autoSave;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        primaryStage.setTitle("MyMovieManager");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/favicon.ico")));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml-views/main-view.fxml"));
        Parent root = loader.load();
        Scene mainScene = new Scene(root);
        MainController mainController = loader.getController();




        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    @Override
    public void init() {
        log.info("Application MyMovieManager has started");
        AutoSave.NEW_OBJECTS.clear();
        autoSave = new AutoSave();
        autoSave.start();
    }

    @Override
    public void stop() throws Exception {
        autoSave.interrupt();
        autoSave.join();
        log.info("Application is being terminated now...");
    }
}
