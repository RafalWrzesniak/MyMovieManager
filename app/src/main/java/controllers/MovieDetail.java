package controllers;

import MoviesAndActors.Actor;
import MoviesAndActors.Movie;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class MovieDetail {

    @FXML private VBox vbox;
    @FXML private ImageView cover;
    @FXML private Label title, year, duration, director, description;
    @Getter private static final double PREF_WIDTH = 250.0;

    public void setMovie(Movie movie) {
        title.setText(movie.getTitle());
        year.setText(movie.getPremiere().toString());
        duration.setText(movie.getDurationFormatted());
        if(movie.getDirectors().size() > 0) {
            StringBuilder directorsBuild = new StringBuilder();
            for(Actor director : movie.getDirectors()) {
                directorsBuild.append(director.getNameAndSurname());
                directorsBuild.append(", ");
            }
            int dirLen = directorsBuild.length();
            directorsBuild.delete(dirLen-2, dirLen);
            director.setText(directorsBuild.toString());
        } else {
            vbox.getChildren().remove(director);
        }
        description.setText(movie.getDescription());
    }

    void setCover(Image image) {
        cover.setImage(image);
    }
}
