package app;

import FileOperations.AutoSave;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

@Slf4j
public class Main extends Application {

    private AutoSave autoSave;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        Locale locale = new Locale("pl", "PL");
//        Locale locale = new Locale("en", "EN");
        ResourceBundle bundle = ResourceBundle.getBundle("bundles.messages", locale);

        primaryStage.setTitle("MyMovieManager");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/favicon.ico")));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml-views/main-view.fxml"), bundle);
        Parent root = loader.load();
        Scene mainScene = new Scene(root);
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
