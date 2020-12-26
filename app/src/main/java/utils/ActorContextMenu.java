package utils;

import Configuration.Files;
import FileOperations.IO;
import Internet.Connection;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import app.Main;
import controllers.DialogController;
import controllers.MainController;
import controllers.actor.ActorDetail;
import controllers.actor.ActorEdit;
import controllers.actor.ActorKind;
import controllers.actor.ActorPane;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public final class ActorContextMenu {

//    == fields ==
    @Getter private final ContextMenu contextMenu;
    private final ResourceBundle resourceBundle;
    private final ActorKind owner;
    private Actor actor;

//    == constructor ==
    public ActorContextMenu(Actor actor, ResourceBundle resourceBundle, ActorKind owner) {
        if(actor == null || resourceBundle == null || owner == null) {
            throw new IllegalArgumentException("Args cannot be null!");
        }
        this.owner = owner;
        this.actor = actor;
        this.resourceBundle = resourceBundle;
        contextMenu = new ContextMenu();
        final MenuItem reDownload = new MenuItem(resourceBundle.getString("context_menu.re_download"));
        reDownload.setOnAction(event -> reDownload());
        final MenuItem editItem = new MenuItem(resourceBundle.getString("context_menu.edit"));
        editItem.setOnAction(event -> edit());
        final MenuItem changePhotoItem = new MenuItem(resourceBundle.getString("context_menu.change_photo"));
        changePhotoItem.setOnAction(event -> changePhoto());

        final Menu addToContentList = new Menu(resourceBundle.getString("context_menu.add_to_list"));
        MainController.observableContentActors.forEach(list -> {
            MenuItem listMenu = new MenuItem(list.getDisplayName());
            if(list.get(actor) != null) listMenu.setDisable(true);
            listMenu.setOnAction(actionEvent -> {
                list.add(actor);
                owner.getMainController().getActorListView().refresh();
            });
            addToContentList.getItems().add(listMenu);
        });

        final MenuItem removeFromList = new MenuItem(resourceBundle.getString("context_menu.remove_from_list"));
        removeFromList.setOnAction(event -> {
            owner.getMainController().getActorListView().getSelectionModel().getSelectedItem().remove(actor);
            owner.getMainController().populateFlowPaneContentList(owner.getMainController().currentlyDisplayedList, true);
            owner.getMainController().getActorListView().refresh();
        });

        contextMenu.setOnShown(event -> {
            ContentList<Actor> list = owner.getMainController().getActorListView().getSelectionModel().getSelectedItem();
            if(list == null || list.getListName().equals(ContentList.ALL_ACTORS_DEFAULT)) {
                removeFromList.setDisable(true);
            }
        });

        contextMenu.getItems().addAll(reDownload, editItem, changePhotoItem,  new SeparatorMenuItem(), addToContentList, removeFromList);
    }



    //    == methods ==
    private void reDownload() {
        log.info("Downloading data again for \"{}\"", actor);
        Thread download = new Thread(() -> {
            Connection connection;
            Actor downloadedActor;
            try {
                connection = new Connection(actor.getFilmweb());
                Map<String, String> map = connection.grabActorDataFromFilmweb();
                if(map == null) return;
                map.put(Actor.ID, String.valueOf(actor.getId()));
                downloadedActor= new Actor(map);
            } catch (IOException e) {
                log.warn("Invalid URL - no such actor \"{}\"", actor);
                return;
            }
            actor.getAllMoviesActorPlayedIn().forEach(downloadedActor::addMovieActorPlayedIn);
            actor.getAllMoviesDirectedBy().forEach(downloadedActor::addMovieDirectedBy);
            actor.getAllMoviesWrittenBy().forEach(downloadedActor::addMovieWrittenBy);
            actor = downloadedActor;
            actor.saveMe();

            if(actor.getImagePath() == null || actor.getImagePath().equals(Files.NO_ACTOR_IMAGE))  {
                Path downloadedImagePath = Paths.get(IO.createContentDirectory(actor).toString(), actor.getReprName().concat(".jpg"));
                if (Connection.downloadImage(actor.getImageUrl(), downloadedImagePath)) {
                    actor.setImagePath(downloadedImagePath);
                }
            }

            owner.setActor(actor);
            if(owner instanceof ActorPane) Platform.runLater(((ActorPane) owner)::selectItem);
            if(owner instanceof ActorDetail && ((ActorDetail) owner).getOwner() != null) Platform.runLater(((ActorDetail) owner).getOwner()::selectItem);
        });
        download.start();
    }

    private void edit() {
        log.info("Edit actor \"{}\" called", actor);
        FXMLLoader loader = Main.createLoader(PaneNames.ACTOR_EDIT, resourceBundle);
        Dialog<ButtonType> dialog;
        try {
           dialog = Main.createDialog(loader, Main.primaryStage);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ActorEdit actorEdit = loader.getController();
        actorEdit.setActor(actor);
        dialog.getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(Bindings.not(actorEdit.getValid()));
        actorEdit.changeImage.setOnAction(event -> {
            changePhoto();
            actorEdit.image.setImage(new Image(actor.getImagePath().toUri().toString()));
            MainController.recForDialogs.visibleProperty().bind(dialog.showingProperty());
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            actorEdit.processResult();
        }

        owner.setActor(actor);
        if(owner instanceof ActorPane) Platform.runLater(((ActorPane) owner)::selectItem);
        if(owner instanceof ActorDetail && ((ActorDetail) owner).getOwner() != null) Platform.runLater(((ActorDetail) owner).getOwner()::selectItem);
    }

    private void changePhoto() {
        log.info("Changing photo for \"{}\"", actor);
        FXMLLoader loader = Main.createLoader(PaneNames.SINGLE_DIALOG, resourceBundle);
        Dialog<ButtonType> dialog = Main.createChangePhotoDialog(loader, contextMenu.getOwnerWindow(), resourceBundle, actor);
        DialogController dialogController = loader.getController();
        if(dialog == null) return;
        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK) {
            Main.processChangePhotoResult(dialogController, actor);
            owner.setActor(actor);
            if(owner instanceof ActorPane) Platform.runLater(((ActorPane) owner)::selectItem);
            if(owner instanceof ActorDetail && ((ActorDetail) owner).getOwner() != null) Platform.runLater(((ActorDetail) owner).getOwner()::selectItem);
        }
    }


}
