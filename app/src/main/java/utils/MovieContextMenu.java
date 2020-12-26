package utils;

import Configuration.Config;
import FileOperations.IO;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import app.Main;
import controllers.DialogController;
import controllers.MainController;
import controllers.movie.MovieDetail;
import controllers.movie.MovieEdit;
import controllers.movie.MovieKind;
import controllers.movie.MoviePane;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public final class MovieContextMenu {

    @Getter
    private final ContextMenu contextMenu;
    private final ResourceBundle resourceBundle;
    private final MovieKind owner;
    private final Movie movie;

    public MovieContextMenu(Movie movie, ResourceBundle resourceBundle, MovieKind owner) {
        if(movie == null || resourceBundle == null || owner == null) {
            throw new IllegalArgumentException("Args cannot be null!");
        }
        this.owner = owner;
        this.movie = movie;
        this.resourceBundle = resourceBundle;
        contextMenu = new ContextMenu();

        final MenuItem openItem = new MenuItem(resourceBundle.getString("context_menu.open"));


        final MenuItem markAsWatchedItem = new MenuItem(resourceBundle.getString("context_menu.mark_as_watched"));
        markAsWatchedItem.setOnAction(event -> markAsWatched());

        final MenuItem editItem = new MenuItem(resourceBundle.getString("context_menu.edit"));
        editItem.setOnAction(event -> editMovie());


        final MenuItem changeCoverItem = new MenuItem(resourceBundle.getString("context_menu.change_cover"));
        changeCoverItem.setOnAction(event -> changeCover());

        final Menu addToListItem = new Menu(resourceBundle.getString("context_menu.add_to_list"));
        MainController.observableContentMovies.forEach(list -> handleAddingToList(list, addToListItem));

        final MenuItem removeItem = new MenuItem(resourceBundle.getString("context_menu.remove_from_list"));
        removeItem.setOnAction(event -> handleRemovingFromList());


        contextMenu.getItems().addAll(openItem, editItem, changeCoverItem, addToListItem, removeItem);
        contextMenu.setOnShowing(event -> {
            ContentList<Movie> selectedList = owner.getMainController().getMovieListView().getSelectionModel().getSelectedItem();
            if(selectedList.equals(MainController.moviesToWatch)) {
                contextMenu.getItems().add(1, markAsWatchedItem);
            }
            if(selectedList.equals(MainController.allMovies)) {
                removeItem.setDisable(true);
            }

        });
    }

    private void editMovie() {
        log.info("Edit movie \"{}\" called", movie);
        FXMLLoader loader = Main.createLoader(PaneNames.MOVIE_EDIT, resourceBundle);
        Dialog<ButtonType> dialog;
        try {
            dialog = Main.createDialog(loader, Main.primaryStage);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        MovieEdit movieEdit = loader.getController();
        movieEdit.setMovie(movie);
        dialog.getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(Bindings.not(movieEdit.getValid()));
        movieEdit.changeCover.setOnAction(event -> {
            changeCover();
            movieEdit.cover.setImage(new Image(movie.getImagePath().toUri().toString()));
            MainController.recForDialogs.visibleProperty().bind(dialog.showingProperty());
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            movieEdit.processResult();
        }

        owner.setMovie(movie);
        if(owner instanceof MoviePane) Platform.runLater(((MoviePane) owner)::selectItem);
        if(owner instanceof MovieDetail && ((MovieDetail) owner).getOwner() != null) Platform.runLater(((MovieDetail) owner).getOwner()::selectItem);
    }

    private void handleAddingToList(ContentList<Movie> list, Menu addToListItem) {
        MenuItem listMenu = new MenuItem(list.getDisplayName());
        if(list.get(movie) != null) {
            listMenu.setDisable(true);
        }

        listMenu.setOnAction(actionEvent -> {
            list.add(movie);
            if(list.equals(MainController.moviesToWatch)) {
                File movieFile = new File(String.valueOf(Config.getMAIN_MOVIE_FOLDER().resolve(movie.getTitle().replaceAll("[]\\[*./:;|,\"]", ""))));
                Map<File, Integer> mainMovieMap = IO.readLastStateOfMainMovieFolder();
                mainMovieMap.putIfAbsent(movieFile, movie.getId());
                IO.writeLastRideFile(mainMovieMap);
                log.info("Movie folder creation ends with status \"{}\"", movieFile.mkdir());
            }
            owner.getMainController().getMovieListView().refresh();
        });
        addToListItem.getItems().add(listMenu);
    }

    private void handleRemovingFromList() {
        owner.getMainController().getMovieListView().getSelectionModel().getSelectedItem().remove(movie);
        owner.getMainController().populateFlowPaneContentList(owner.getMainController().currentlyDisplayedList, true);
        owner.getMainController().getMovieListView().refresh();
        if(owner.getMainController().getMovieListView().getSelectionModel().getSelectedItem().equals(MainController.moviesToWatch)) {
            final File[] movieFile = new File[1];
            IO.readLastStateOfMainMovieFolder().forEach((file, id) -> { if(id == movie.getId()) movieFile[0] = file; });
            try {
                Files.move(movieFile[0].toPath(), Config.getRECENTLY_WATCHED().resolve(movieFile[0].getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (DirectoryNotEmptyException directoryNotEmptyException) {
                IO.deleteDirectoryRecursively(movieFile[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void markAsWatched() {
        if(MainController.recentlyWatched == null) {
            MainController.recentlyWatched = new ContentList<>(ContentList.RECENTLY_WATCHED);
            MainController.observableContentMovies.add(MainController.recentlyWatched);
        }
        MainController.recentlyWatched.add(movie);
        if(MainController.recentlyWatched.size() > 10) {
            MainController.recentlyWatched.remove(0);
        }
        handleRemovingFromList();

    }

    private void changeCover() {
        log.info("Changing photo for \"{}\"", movie);
        FXMLLoader loader = Main.createLoader(PaneNames.SINGLE_DIALOG, resourceBundle);
        Dialog<ButtonType> dialog = Main.createChangePhotoDialog(loader, contextMenu.getOwnerWindow(), resourceBundle, movie);
        DialogController dialogController = loader.getController();
        if(dialog == null) return;
        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK) {
            Main.processChangePhotoResult(dialogController, movie);
            owner.setMovie(movie);
            if(owner instanceof MoviePane) Platform.runLater(((MoviePane) owner)::selectItem);
            if(owner instanceof MovieDetail && ((MovieDetail) owner).getOwner() != null) Platform.runLater(((MovieDetail) owner).getOwner()::selectItem);
        }
    }


}
