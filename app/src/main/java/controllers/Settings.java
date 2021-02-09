package controllers;

import Configuration.Config;
import app.Main;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import lombok.Setter;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Settings implements Initializable {

//    == fields ==
    @FXML private Label saveLabel, watchedLabel, mainDirLabel;
    @FXML private TextField mainDir, watched, save;
    @Setter private MainController mainController;
    @Setter public Dialog<ButtonType> dialog;
    private ResourceBundle resourceBundle;

//    == init ==
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        saveLabel.setText(Config.getSAVE_PATH().toString());
        watchedLabel.setText(Config.getRECENTLY_WATCHED().toString());
        mainDirLabel.setText(Config.getMAIN_MOVIE_FOLDER().toString());
    }


//    == methods ==
    @FXML
    private void chooseDirectory(MouseEvent mouseEvent) {
        File chosenDir = mainController.chooseDirectoryDialog(resourceBundle.getString("settings.choose_file_prompt"));
        if(chosenDir != null) {
            ((TextField) mouseEvent.getSource()).setText(chosenDir.toString());
        }
    }

    @FXML
    private void switchLanguage() {
        if(resourceBundle.getLocale().getDisplayLanguage().equals(Main.localePl.getDisplayLanguage())) {
            Main.loadView(Main.localeEn, Main.primaryStage);
        } else if(resourceBundle.getLocale().getDisplayLanguage().equals(Main.localeEn.getDisplayLanguage())) {
            Main.loadView(Main.localePl, Main.primaryStage);
        }
        dialog.close();
    }

    public void processResult() {
        if (!mainDir.getText().isEmpty()) {
            Config.setMAIN_MOVIE_FOLDER(Paths.get(mainDir.getText()));
        }
        if (!watched.getText().isEmpty()) {
            Config.setRECENTLY_WATCHED(Paths.get(watched.getText()));
        }
        if (!save.getText().isEmpty()) {
            Config.setSAVE_PATH(new File(save.getText()), true);
        }
    }

}
