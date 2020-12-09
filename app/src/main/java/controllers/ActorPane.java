package controllers;

import Configuration.Files;
import MoviesAndActors.Actor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class ActorPane extends ContentPane implements Initializable {

    @FXML public StackPane actorPane;
    @FXML public Label name;
    private ResourceBundle resourceBundle;
    private Actor actor;

    //    == init ==
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        contentPane = actorPane;
        contextMenu = new ContextMenu();
        final MenuItem item1 = new MenuItem("Otwórz");
        final MenuItem item2 = new MenuItem("Zmień zdjęcie");
        item1.setOnAction(actionEvent -> System.out.println("item1 pressed"));
        contextMenu.getItems().addAll(item1, item2);
        actorPane.setOnContextMenuRequested(e -> contextMenu.show(actorPane, e.getScreenX(), e.getScreenY()));
    }

//    == methods ==
    public void setActor(Actor actor) {
        this.actor = actor;
        Image image;
        if(actor.getImagePath() != null && actor.getImagePath().toFile().exists()) {
            image = new Image(actor.getImagePath().toUri().toString());
            super.iHaveImage.setValue(true);
        } else {
            image = new Image(Files.NO_ACTOR_IMAGE.toUri().toString());
        }
        cover.setImage(image);
        name.setText(actor.getNameAndSurname());
        if(!actor.isActor() && !actor.isDirector() && !actor.isWriter()) {
            System.out.println(actor);
        }
    }

    @FXML
    @Override
    public void selectItem() {
        super.selectItem();
        FXMLLoader loader = mainController.createLoader(MainController.ACTOR_DETAIL, resourceBundle);
        Parent actorDetails;
        try {
            actorDetails = loader.load();
        } catch (IOException e) {
            log.warn("Failed to load fxml view in ActorDetail");
            return;
        }
        ActorDetail actorDetailController = loader.getController();
        actorDetailController.setActor(actor);
        actorDetailController.setCover(cover.getImage());
        mainController.rightDetail.getChildren().clear();
        mainController.rightDetail.getChildren().add(actorDetails);
    }
}
