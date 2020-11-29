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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class MainController {

    //    == fields ==
    ContentList<Actor> allActors;
    ContentList<Movie> allMovies;
    ContentList<Movie> moviesToWatch;

    @FXML public Button addContentMovies, addContentActors, addFolderWithMovies, addSingleMovie, addMovieFromWeb;
    @FXML public FlowPane flowPaneContentList = new FlowPane();
    @FXML public ScrollPane scrollPane;

    //    == init ==
    public void initialize() throws InterruptedException {
        // load all data
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

        // populate list
        populateFlowPaneContentList(moviesToWatch);

        // control speed of scroll pane
        final double SPEED = 0.005;
        scrollPane.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            scrollPane.setVvalue(scrollPane.getVvalue() - deltaY);
        });


    }

//    == methods ==
    private void populateFlowPaneContentList(ContentList<Movie> contentList) {
        flowPaneContentList.getChildren().clear();
        for(int i =0; i < contentList.size(); i++) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml-views/movie-pane.fxml"));
            Parent moviePane;
            try {
                moviePane = loader.load();
            } catch (IOException e) {
                log.warn("Failed to load fxml view in MoviePane");
                continue;
            }
            MoviePane moviePaneController = loader.getController();
            moviePaneController.setMovie(contentList.get(i));
            flowPaneContentList.getChildren().add(moviePane);
        }

    }

//    == fxml handling ==
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
