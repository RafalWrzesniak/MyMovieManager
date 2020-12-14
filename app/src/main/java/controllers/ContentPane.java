package controllers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public abstract class ContentPane {

//    == fields ==
    protected static BooleanProperty clearBackSthWasClicked = new SimpleBooleanProperty(false);
    protected BooleanProperty iHaveImage = new SimpleBooleanProperty(false);
    StackPane contentPane;
    @FXML Rectangle blue_background;
    @FXML Rectangle green_background;
    @FXML protected ImageView cover;
    protected MainController mainController;
    protected ContextMenu contextMenu;


    //    == methods ==
    void setMainController(MainController mainController) {
        this.mainController = mainController;
        clearBackSthWasClicked.addListener((observableValue, aBoolean, t1) -> {
            if(t1) {
                blue_background.setVisible(false);
                green_background.setVisible(false);
            }
        });
    }

    void selectItem() {
        clearBackSthWasClicked.setValue(true);
        green_background.setVisible(true);
        contextMenu.hide();
        clearBackSthWasClicked.setValue(false);
    }

    @FXML
    public void lightBackground() {
        if(!green_background.isVisible()) {
            blue_background.setVisible(true);
        }
    }

    @FXML
    public void hideBackground() {
        blue_background.setVisible(false);
    }


}