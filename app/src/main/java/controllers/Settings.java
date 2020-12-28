package controllers;

import Configuration.Config;
import app.Main;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Settings implements Initializable {

//    == fields ==
    @FXML public TextField mainDir, watched, save;
    public Dialog<ButtonType> dialog;
    private ResourceBundle resourceBundle;
    private MainController mainController;


//    == init ==
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }


//  == methods ==
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setDialog(Dialog<ButtonType> dialog) {
        this.dialog = dialog;
    }


    @FXML
    public void chooseDirectory(MouseEvent mouseEvent) {
        File chosenDir = mainController.chooseDirectoryDialog(resourceBundle.getString("settings.choose_file_prompt"));
        if(chosenDir != null) {
            ((TextField) mouseEvent.getSource()).setText(chosenDir.toString());
        }
    }


    @FXML
    public void switchLanguage() {
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
