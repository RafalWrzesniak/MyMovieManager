package app;

import FileOperations.AutoSave;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import utils.AppColors;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml-views/main-view.fxml"));
        Parent root = loader.load();
//        Controller mainController = loader.getController();


        primaryStage.setTitle("MyMovieManager");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/favicon.ico")));
        Scene mainScene = new Scene(root);


        Button button = new Button("My button");
        Label label = new Label();
//        label.setEditable(false);
//        label.setMaxWidth(200);
        label.getStyleClass().add("blue_text");


        label.setVisible(false);
        button.setOnAction(actionEvent -> label.setVisible(true));
        button.setOnMousePressed(actionEvent -> label.setText("I am pressed!"));
        button.setOnMouseReleased(actionEvent -> label.setText("I am released!"));

        VBox vBox = new VBox();
//        root.getChildren().add(vBox);
        vBox.getChildren().add(button);
        vBox.getChildren().add(label);


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
