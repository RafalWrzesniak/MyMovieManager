package utils;

import Configuration.Config;
import Errors.MovieNotFoundException;
import FileOperations.IO;
import FileOperations.XMLOperator;
import Internet.FilmwebClientActor;
import Internet.FilmwebClientMovie;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import Service.MovieCreatorService;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public class MovieContextMenu {

//    == fields ==
    @Getter protected final ContextMenu contextMenu;
    protected final ResourceBundle resourceBundle;
    protected final MovieKind owner;
    private Movie movie;

//    == constructor ==
    protected MovieContextMenu(ResourceBundle resourceBundle, MovieKind owner) {
        if(resourceBundle == null) {
            throw new IllegalArgumentException("Args cannot be null!");
        }
        this.resourceBundle = resourceBundle;
        this.owner = owner;
        contextMenu = new ContextMenu();
    }

    public MovieContextMenu(Movie movie, ResourceBundle resourceBundle, MovieKind owner) {
        if(movie == null || resourceBundle == null || owner == null) {
            throw new IllegalArgumentException("Args cannot be null!");
        }
        this.owner = owner;
        this.movie = movie;
        this.resourceBundle = resourceBundle;
        contextMenu = new ContextMenu();

        final MenuItem openItem = new MenuItem(resourceBundle.getString("context_menu.open"));
        openItem.setOnAction(event -> owner.getMainController().openMovieInfo(movie, resourceBundle));

        final MenuItem markAsWatchedItem = new MenuItem(resourceBundle.getString("context_menu.mark_as_watched"));
        markAsWatchedItem.setOnAction(event -> markAsWatched(movie));

        final MenuItem openDirectory = new MenuItem(resourceBundle.getString("context_menu.open_movie_directory"));
        openDirectory.setOnAction(actionEvent -> openWindowsDirectoryContainingMovie());

        final MenuItem editItem = new MenuItem(resourceBundle.getString("context_menu.edit"));
        editItem.setOnAction(event -> editMovie());

        final MenuItem reDownload = new MenuItem(resourceBundle.getString("context_menu.re_download"));
        reDownload.setOnAction(event -> downloadAgain(movie));


        final MenuItem changeCoverItem = new MenuItem(resourceBundle.getString("context_menu.change_cover"));
        changeCoverItem.setOnAction(event -> changeCover());

        final Menu addToListItem = new Menu(resourceBundle.getString("context_menu.add_to_list"));

        final MenuItem removeItem = new MenuItem(resourceBundle.getString("context_menu.remove_from_list"));
        removeItem.setOnAction(event -> handleRemovingFromList(movie));


        contextMenu.getItems().addAll(openItem, editItem, reDownload, changeCoverItem, addToListItem, removeItem);
        contextMenu.setOnShowing(event -> {
            ContentList<Movie> selectedList = owner.getMainController().getMovieListView().getSelectionModel().getSelectedItem();
            if(selectedList != null && selectedList.equals(MainController.moviesToWatch)) {
                contextMenu.getItems().add(1, markAsWatchedItem);
                contextMenu.getItems().add(2, openDirectory);
            }
            removeItem.setDisable(selectedList == null || selectedList.get(movie) == null);
            addToListItem.getItems().clear();
            for(ContentList<Movie> list : MainController.observableContentMovies) {
                MenuItem listMenu = new MenuItem(list.getDisplayName());
                if(list.get(movie) != null) {
                    listMenu.setDisable(true);
                }
                listMenu.setOnAction(actionEvent -> handleAddingToList(movie, list));
                addToListItem.getItems().add(listMenu);
            }
        });
    }

//   == methods ==

    protected void downloadAgain(Movie movie) {
        log.info("Downloading data again for \"{}\"", movie);
        Thread download = new Thread(() -> {
            MovieCreatorService mcs = new MovieCreatorService(FilmwebClientMovie.getInstance(), FilmwebClientActor.getInstance(),
                    MainController.allMovies, MainController.allActors, MainController.actorStringList);
            Movie downloadedMovie;
            try {
                downloadedMovie = mcs.createMovieFromUrl(movie.getFilmweb());
            } catch (MovieNotFoundException e) {
                log.warn("Invalid URL {}, cannot redownload movie data", movie.getFilmweb());
                return;
            }
            this.movie = downloadedMovie;
            Platform.runLater(() -> {
                owner.setMovie(downloadedMovie);
                owner.getMainController().getActorListView().refresh();
            });

            if(owner instanceof MoviePane) Platform.runLater(((MoviePane) owner)::selectItem);
            if(owner instanceof MovieDetail && ((MovieDetail) owner).getOwner() != null) Platform.runLater(((MovieDetail) owner).getOwner()::selectItem);

        });
        download.start();
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

    protected void handleAddingToList(Movie movie, ContentList<Movie> list) {
        list.add(movie);
        if(list.equals(MainController.moviesToWatch)) {
            File movieFile = new File(String.valueOf(Config.getMAIN_MOVIE_FOLDER().resolve(movie.getTitle().replaceAll("[]\\[*./:;|,\"]", ""))));
            Map<File, Integer> mainMovieMap = IO.readLastStateOfMainMovieFolder();
            mainMovieMap.putIfAbsent(movieFile, movie.getId());
            IO.writeLastRideFile(mainMovieMap);
            log.info("Movie folder creation ends with status \"{}\"", movieFile.mkdir());
        }
        owner.getMainController().getMovieListView().refresh();
    }

    protected void handleRemovingFromList(Movie movie) {
        owner.getMainController().getMovieListView().getSelectionModel().getSelectedItem().remove(movie);
        owner.getMainController().displayPanesByPopulating(true);
        owner.getMainController().getMovieListView().refresh();
        if(owner.getMainController().getMovieListView().getSelectionModel().getSelectedItem().equals(MainController.moviesToWatch)) {
            final File[] movieFile = new File[1];
            IO.readLastStateOfMainMovieFolder().forEach((file, id) -> { if(id == movie.getId()) movieFile[0] = file; });
            if(movieFile[0] == null) return;
            try {
                Files.move(movieFile[0].toPath(), Config.getRECENTLY_WATCHED().resolve(movieFile[0].getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (DirectoryNotEmptyException directoryNotEmptyException) {
                IO.deleteDirectoryRecursively(movieFile[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void markAsWatched(Movie movie) {
        if(MainController.recentlyWatched == null) {
            MainController.recentlyWatched = new ContentList<>(ContentList.RECENTLY_WATCHED);
            MainController.observableContentMovies.add(MainController.recentlyWatched);
        }
        MainController.recentlyWatched.add(movie);
        if(MainController.recentlyWatched.size() > 12) {
            File recentlyFile = Config.getSAVE_PATH_MOVIE().resolve(Paths.get(ContentList.RECENTLY_WATCHED.concat(".xml"))).toFile();
            Movie movieToRemove = MainController.recentlyWatched.getById(XMLOperator.getFirstIdFromListFile(recentlyFile));
            MainController.recentlyWatched.remove(movieToRemove);
        }
        handleRemovingFromList(movie);
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


    @SneakyThrows
    private void openWindowsDirectoryContainingMovie() {
        Map<File, Integer> lastRideMap = IO.readLastStateOfMainMovieFolder();
        File fileToOpen = null;
        for (Map.Entry<File, Integer> entry : lastRideMap.entrySet()) {
            File file = entry.getKey();
            Integer integer = entry.getValue();
            if (integer == movie.getId()) fileToOpen = file;
        }
        if(fileToOpen != null) {
            Runtime.getRuntime().exec("explorer.exe /open," + fileToOpen.getPath());
        }
    }

}
