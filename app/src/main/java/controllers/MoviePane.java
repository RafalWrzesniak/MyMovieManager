package controllers;

import Configuration.Files;
import MoviesAndActors.Movie;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class MoviePane implements Initializable {

    //    == fields ==
    @FXML StackPane moviePane;
    @FXML private Label title, duration;
    @FXML private Rectangle blue_background;
    @FXML Rectangle green_background;
    @FXML private ImageView cover;
    private boolean iHaveImage;
    private Movie movie;
    private MainController mainController;
    private ResourceBundle resourceBundle;
    private ContextMenu contextMenu;

//    == init ==
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        contextMenu = new ContextMenu();
        final MenuItem item1 = new MenuItem("Otwórz");
        final MenuItem item2 = new MenuItem("Oznacz jako obejrzane");
        final MenuItem item3 = new MenuItem("Zmień okładkę");
        final MenuItem item4 = new MenuItem("Dodaj do listy");
        final MenuItem item5 = new MenuItem("Usuń");
        item1.setOnAction(actionEvent -> System.out.println("item1 pressed"));
        contextMenu.getItems().addAll(item1, item2, item3, item4, item5);
        moviePane.setOnContextMenuRequested(e -> contextMenu.show(moviePane, e.getScreenX(), e.getScreenY()));
    }

    //    == methods ==
    public void setMovie(Movie movie) {
        this.movie = movie;
        Image image;
        try {
            image = new Image(movie.getImagePath().toUri().toString());
            iHaveImage = true;
        } catch (NullPointerException e) {
            image = new Image(Files.NO_MOVIE_COVER.toUri().toString());
        }
        this.cover.setImage(image);
        this.duration.setText(movie.getDurationShortFormatted());
        this.title.setText(movie.getTitle());
        if(iHaveImage) {
            this.title.setVisible(false);
        }
    }

    void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void lightBackground() {
        if(!green_background.isVisible()) {
            blue_background.setVisible(true);
        }
        title.setVisible(true);
    }

    @FXML
    public void hideBackground() {
        blue_background.setVisible(false);
        if(iHaveImage) {
            title.setVisible(false);
        }
    }

    @FXML
    public void selectMovie() {
        contextMenu.hide();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml-views/movie-detail.fxml"), resourceBundle);
        Parent movieDetails;
        try {
            movieDetails = loader.load();
        } catch (IOException e) {
            log.warn("Failed to load fxml view in MovieDetail");
            return;
        }
        MovieDetail movieDetailController = loader.getController();
        movieDetailController.setMovie(movie);
        movieDetailController.setCover(cover.getImage());
        mainController.rightDetail.getChildren().clear();
        mainController.rightDetail.getChildren().add(movieDetails);
        blue_background.setVisible(false);
        green_background.setVisible(true);
    }


    @FXML
    public void unGreen() {
        green_background.setVisible(false);
    }

}
