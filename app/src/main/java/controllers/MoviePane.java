package controllers;

import MoviesAndActors.Movie;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class MoviePane extends StackPane {

    @FXML private ImageView cover;
    @FXML private Label duration;

    public void setMovie(Movie movie) {
        Image image = new Image(movie.getImagePath().toUri().toString());
        this.cover.setImage(image);
        this.duration.setText(movie.getDurationShortFormatted());
    }


}
