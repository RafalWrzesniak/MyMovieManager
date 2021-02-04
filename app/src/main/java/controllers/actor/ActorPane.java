package controllers.actor;

import Configuration.Files;
import MoviesAndActors.Actor;
import controllers.ContentPane;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import utils.ActorContextMenu;
import utils.MultiActorContextMenu;

import java.net.URL;
import java.util.*;

@Slf4j
public class ActorPane extends ContentPane implements Initializable, ActorKind {

//    == fields ==
    protected static Set<Actor> selectedActors = new HashSet<>();
    private ResourceBundle resourceBundle;
    @FXML public StackPane actorPane;
    @FXML public Label name;
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
        if(!actor.getImagePath().toFile().exists()) {
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
        mainController.openActorDetail(actor, this, false);
    }

    @Override
    public void selectItemClicked(MouseEvent mouseEvent) {
        contextMenu.hide();
        if(mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            if(mouseEvent.isControlDown()) {
                if(!selectedActors.contains(actor)) {
                    selectedActors.add(actor);
                    blue_background.setVisible(false);
                    green_background.setVisible(true);
                } else {
                    selectedActors.remove(actor);
                    blue_background.setVisible(true);
                    green_background.setVisible(false);
                }
            } else {
                selectedActors.clear();
                selectedActors.add(actor);
                selectItem();
            }
        } else if(mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
            if(selectedActors.size() == 0) selectedActors.add(actor);
            if(selectedActors.size() == 1) {
                selectedActors.clear();
                selectedActors.add(actor);
                selectItem();
                contextMenu = new ActorContextMenu(actor, resourceBundle, this).getContextMenu();
                contextMenu.show(contentPane, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            } else if(selectedActors.size() > 1 && green_background.isVisible()) {
                contextMenu = new MultiActorContextMenu(new ArrayList<>(selectedActors), resourceBundle, this).getContextMenu();
                contextMenu.show(contentPane, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            } else {
                selectedActors.clear();
                selectedActors.add(actor);
                selectItem();
            }
        }
    }

}
