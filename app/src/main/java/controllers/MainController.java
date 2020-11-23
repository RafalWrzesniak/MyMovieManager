package controllers;

import FileOperations.XMLOperator;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import MyMovieManager.MovieMainFolder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

import java.io.IOException;


public class MainController {

    ContentList<Actor> allActors;
    ContentList<Movie> allMovies;
    ContentList<Movie> moviesToWatch;

    @FXML public Button addContentMovies, addContentActors, addFolderWithMovies, addSingleMovie, addMovieFromWeb;
    @FXML public FlowPane flowPaneContentList = new FlowPane();


    public void initialize() throws InterruptedException {
        XMLOperator.ReadAllDataFromFiles readData = new XMLOperator.ReadAllDataFromFiles();
        readData.start();
        readData.join();
        allActors = readData.getAllActors();
        allMovies = readData.getAllMovies();

        MovieMainFolder movieMainFolder = new MovieMainFolder(allMovies, allActors);
        movieMainFolder.start();
        movieMainFolder.join();
        moviesToWatch = movieMainFolder.getMoviesToWatch();

        System.out.println(allMovies);
        System.out.println(allActors);
        System.out.println(moviesToWatch);

    }


    @FXML
    public void addContentList(ActionEvent actionEvent) {
        String message;
        if(actionEvent.getSource().equals(addContentMovies)) {
            message = Movie.class.getSimpleName();
        } else if(actionEvent.getSource().equals(addContentActors)) {
            message = Actor.class.getSimpleName();
        } else {
            return;
        }
        System.out.println("I am adding content list with " + message + "s from " + actionEvent.getSource());
    }

    @FXML
    public void refreshMainFolder() throws IOException {
        System.out.println("Starting to refresh...");


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml-views/movie-pane.fxml"));
        Parent moviePane = loader.load();
        MoviePane moviePaneController = loader.getController();
        moviePaneController.setMovie(moviesToWatch.getById(25));

        flowPaneContentList.getChildren().add(moviePane);

    }

    @FXML
    public void addMoviesToApp(ActionEvent actionEvent) {
        System.out.println("Adding movies from " + actionEvent.getSource());
    }

    @FXML
    public void openSettings() {
        System.out.println("Opening settings dialog");
    }
}
