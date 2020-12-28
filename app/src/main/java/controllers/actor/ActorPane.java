package controllers.actor;

import Configuration.Files;
import MoviesAndActors.Actor;
import app.Main;
import controllers.ContentPane;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import utils.ActorContextMenu;
import utils.PaneNames;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class ActorPane extends ContentPane implements Initializable, ActorKind {

//    == fields ==
    @FXML public StackPane actorPane;
    @FXML public Label name;
    private ResourceBundle resourceBundle;
    private Actor actor;

//    == init ==
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        contentPane = actorPane;
    }


//    == methods ==
    @Override
    public void setActor(Actor actor) {
        this.actor = actor;
        this.contextMenu = new ActorContextMenu(actor, resourceBundle, this).getContextMenu();
        if(actor.getImagePath() == null || !actor.getImagePath().toFile().exists()) {
            actor.setImagePath(Files.NO_ACTOR_IMAGE);
        } else if(!actor.getImagePath().equals(Files.NO_ACTOR_IMAGE)) {
            super.iHaveImage.setValue(true);
        }
        cover.setImage(new Image(actor.getImagePath().toUri().toString()));
        name.setText(actor.getNameAndSurname());
        if(!actor.isActor() && !actor.isDirector() && !actor.isWriter()) {
            log.warn("\"{}\" is not playing, directing or writing any movie", actor);
        }
    }


    @Override
    public void selectItem() {
        super.selectItem();
        FXMLLoader loader = Main.createLoader(PaneNames.ACTOR_DETAIL, resourceBundle);
        Parent actorDetails;
        try {
            actorDetails = loader.load();
        } catch (IOException e) {
            log.warn("Failed to load fxml view in ActorDetail");
            return;
        }
        ActorDetail actorDetailController = loader.getController();
        actorDetailController.setOwner(this);
        actorDetailController.setActor(actor);
        actorDetailController.setMainController(mainController);
        mainController.getRightDetail().getChildren().clear();
        mainController.getRightDetail().getChildren().add(actorDetails);
    }

}
