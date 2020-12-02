package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class DialogController {

    //   == fields ==
    @FXML private Label label;
    @FXML private TextField textField;

//    == methods ==
    public void setTexts(String labelName, String promptText) {
        label.setText(labelName);
        textField.setPromptText(promptText);
    }

    public String getResult() {
        return textField.getText();
    }
}
