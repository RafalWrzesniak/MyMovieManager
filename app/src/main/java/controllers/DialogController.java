package controllers;

import Configuration.Config;
import MoviesAndActors.ContentType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class DialogController {

    //   == fields ==
    @FXML public VBox contentVBox;
    @FXML public TextField textField;
    @FXML private Label label;
    private ImageView warningImage;
    private String warningInfo = "Warning info";
    private String orgLabelMessage;

    //    == init ==
    public void initialize() {
        warningImage = new ImageView(new Image("/icons/warning.png"));
        warningImage.setFitHeight(30);
        warningImage.setFitWidth(30);
    }

//    == methods ==
    public void setTexts(String labelName, String promptText) {
        label.setText(labelName);
        textField.setPromptText(promptText);
        orgLabelMessage = labelName;
    }

    public void setTexts(String labelName, String promptText, String warningInfo) {
        setTexts(labelName, promptText);
        this.warningInfo = warningInfo;
    }

    public void showWarning(String warningInfo) {
        label.setGraphic(warningImage);
        label.setStyle("-fx-text-fill: red;");
        label.setText(warningInfo);
    }

    public void hideWarning() {
        label.setGraphic(null);
        label.setStyle("-fx-text-fill: almost_white;");
        label.setText(orgLabelMessage);
    }

    public void setTextFieldListener(Node okButton) {
        if(okButton == null) return;
        okButton.setDisable(true);
        textField.textProperty().addListener((observableValue, old, value) -> {
            try {
                if (ContentType.checkForNullOrEmptyOrIllegalChar(value, "listName").length() > 30) {
                    textField.setText(old);
                    DialogController.this.showWarning(warningInfo);
                } else {
                    okButton.setDisable(false);
                    DialogController.this.hideWarning();
                }
            } catch (Config.ArgumentIssue argumentIssue) {
                okButton.setDisable(true);
            }
        });
    }

    public void setUrlTextListener(Node okButton) {
        if(okButton == null) return;
        okButton.setDisable(true);
        textField.textProperty().addListener((observableValue, old, value) -> {
            if(value.matches("^https://www\\.filmweb\\.pl/film/.+")) {
                okButton.setDisable(false);
                hideWarning();
            } else {
                okButton.setDisable(true);
                showWarning(warningInfo);
            }
        });
    }



}
