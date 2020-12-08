package controllers;

import app.Main;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Settings implements Initializable {

//    == fields ==
    @FXML public TextField mainDir, watched, save;
    public Dialog<ButtonType> dialog;
    private ResourceBundle resourceBundle;
    private MainController mainController;
    public static boolean reOpen;



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
        mainController.reOpenSettings = true;
        dialog.close();
    }

}
